package org.zy.fluorite.autoconfigure.web.server.moonstone;

import java.net.BindException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zy.fluorite.core.utils.Assert;
import org.zy.fluorite.core.utils.ThrowableUtils;
import org.zy.fluorite.web.server.exception.WebServerException;
import org.zy.fluorite.web.server.interfaces.WebServer;
import org.zy.moonStone.core.LifecycleState;
import org.zy.moonStone.core.connector.Connector;
import org.zy.moonStone.core.exceptions.LifecycleException;
import org.zy.moonStone.core.interfaces.container.Container;
import org.zy.moonStone.core.interfaces.container.Context;
import org.zy.moonStone.core.interfaces.container.Engine;
import org.zy.moonStone.core.interfaces.container.Lifecycle;
import org.zy.moonStone.core.interfaces.container.Service;
import org.zy.moonStone.core.startup.MoonStone;

/**
 * @dateTime 2022年12月7日;
 * @author zy(azurite-Y);
 * @description 可以用来控制 MoonStone web服务器的WebServer。
 * 通常这个类应该使用 {@link MoonStoneServletWebServerFactory } 的 {@link MoonStoneServletWebServerFactory } 来创建，但不是直接创建。
 * 
 * ServletWebServerApplicationContext
 */
public class MoonStoneWebServer implements WebServer {
	private static final Logger logger = LoggerFactory.getLogger(MoonStoneWebServer.class);

	private static final AtomicInteger containerCounter = new AtomicInteger(-1);

	private final Object monitor = new Object();

	private final Map<Service, Connector[]> serviceConnectors = new HashMap<>();

	private final MoonStone moonStone;

	private final boolean autoStart;

	private volatile boolean started;

	/**
	 * 创建一个新的 {@link MoonStoneWebServer} 实例
	 * 
	 * @param moonStone - 底层 MoonStone 服务器
	 */
	public MoonStoneWebServer(MoonStone moonStone) {
		this(moonStone, true);
	}

	/**
	 * 创建一个新的 {@link MoonStoneWebServer} 实例
	 * 
	 * @param moonStone - 底层 MoonStone 服务器
	 * @param autoStart - 服务器是否应该启动
	 * @param shutdown  - 服务器支持的关机类型
	 */
	public MoonStoneWebServer(MoonStone moonStone, boolean autoStart) {
		Assert.notNull(moonStone, "MoonStone Server 不能为 null");
		this.moonStone = moonStone;
		this.autoStart = autoStart;
		initialize();
	}

	@Override
	public int getPort() {
		Connector connector = this.moonStone.getConnector();
		if (connector != null) {
			return connector.getLocalPort();
		}
		return 0;
	}

	// -------------------------------------------------------------------------------------
	// 生命周期方法
	// -------------------------------------------------------------------------------------
	@Override
	public void start() throws WebServerException {
		synchronized (this.monitor) {
			if (this.started) {
				return;
			}
			try {
				addPreviouslyRemovedConnectors();
				Connector connector = this.moonStone.getConnector();
				
				if (connector != null && this.autoStart) {
					performDeferredLoadOnStartup();
				}
				
				checkThatConnectorsHaveStarted();
				this.started = true;
				logger.info("MoonStone started on port(s): " + getPortsDescription(true) + " with context path '" + getContextPath() + "'");
				
			}
			catch (ConnectorStartFailedException ex) {
				stopSilently();
				throw ex;
			}
			catch (Exception ex) {
				if (BindException.class.isInstance(ex)) {
					ThrowableUtils.ifCausedBy(ex, BindException.class, (candidate) -> {
						throw new PortInUseException( this.moonStone.getConnector().getPort() , ex);
					});
				}
				throw new WebServerException("无法启动嵌入式 MoonStone 服务器", ex);
			}
			finally {}
		}
	}

	@Override
	public void stop() throws WebServerException {
		synchronized (this.monitor) {
			boolean wasStarted = this.started;
			try {
				this.started = false;
				try {
					stopMoonStone();
					this.moonStone.destroy();
				} catch (LifecycleException ex) {
					// 吞咽并继续
//					ex.printStackTrace();
				}
			} catch (Exception ex) {
				throw new WebServerException("无法停止嵌入式 MoonStone", ex);
			} finally {
				if (wasStarted) {
					containerCounter.decrementAndGet();
				}
			}
		}
	}

	private void initialize() throws WebServerException {
		logger.info("MoonStone initialized with port(s): " + getPortsDescription(false));
		synchronized (this.monitor) {
			try {
				addInstanceIdToEngineName();

				Context context = findContext();
				context.addLifecycleListener((event) -> {
					if (context.equals(event.getSource()) && Lifecycle.START_EVENT.equals(event.getType())) {
						// 删除 Service 和 Connector 的联系，以便在服务启动时不会发生协议绑定地址与端口。
						removeServiceConnectors();
					}
				});

				// 启动服务器以触发初始化监听器
				this.moonStone.start();

				// 可以直接在主线程中重新抛出失败异常
				rethrowDeferredStartupExceptions();

				// 所有 MoonStone 线程都是守护线程。创建一个阻塞的非守护进程来阻止立即关机
				startDaemonAwaitThread();
			} catch (Exception ex) {
				stopSilently();
				destroySilently();
				throw new WebServerException("无法启动嵌入式 MoonStone", ex);
			}
		}
	}

	/**
	 * 设置 {@link Engine} 名称
	 */
	private void addInstanceIdToEngineName() {
		int instanceId = containerCounter.incrementAndGet();
		if (instanceId > 0) {
			Engine engine = this.moonStone.getEngine();
			engine.setName(engine.getName() + "-" + instanceId);
		}
	}

	private Context findContext() {
		for (Container child : this.moonStone.getHost().findChildren()) {
			if (child instanceof Context) {
				return (Context) child;
			}
		}
		throw new IllegalStateException("Host 不包含 Context");
	}

	/**
	 * 删除 Service 和 Connector 的联系，以便在 service 启动时不会发生协议绑定端口。
	 */
	private void removeServiceConnectors() {
		for (Service service : this.moonStone.getServer().findServices()) {
			Connector[] connectors = service.findConnectors().clone();
			
			this.serviceConnectors.put(service, connectors);
			
			for (Connector connector : connectors) {
				// 在连接器不可用时解除联系，防止停止在用的连接器
				if (!connector.getState().isAvailable()) {
					service.removeConnector(connector);
				}
			}
		}
	}

	private void startDaemonAwaitThread() {
		Thread awaitThread = new Thread("container-" + (containerCounter.get())) {

			@Override
			public void run() {
				MoonStoneWebServer.this.moonStone.getServer().await();
			}

		};
		awaitThread.setContextClassLoader(getClass().getClassLoader());
		awaitThread.setDaemon(false);
		awaitThread.start();
	}

	private String getPortsDescription(boolean localPort) {
		StringBuilder ports = new StringBuilder();
		for (Connector connector : this.moonStone.getService().findConnectors()) {
			if (ports.length() != 0) {
				ports.append(' ');
			}
			int port = localPort ? connector.getLocalPort() : connector.getPort();
			ports.append(port).append(" (").append(connector.getScheme()).append(')');
		}
		return ports.toString();
	}

	private String getContextPath() {
		return Arrays.stream(this.moonStone.getHost().findChildren()).filter(MoonStoneEmbeddedContext.class::isInstance)
				.map(MoonStoneEmbeddedContext.class::cast).map(MoonStoneEmbeddedContext::getPath)
				.collect(Collectors.joining(" "));
	}
	
	private void rethrowDeferredStartupExceptions() throws Exception {
		Container[] children = this.moonStone.getHost().findChildren();
		for (Container container : children) {
			if (container instanceof MoonStoneEmbeddedContext) {
				MoonStoneStarter moonStoneStarter = ((MoonStoneEmbeddedContext) container).getStarter();
				if (moonStoneStarter != null) {
					Exception exception = moonStoneStarter.getStartUpException();
					if (exception != null) {
						throw exception;
					}
				}
			}
			if (!LifecycleState.STARTED.equals(container.getState())) {
				throw new IllegalStateException(container + " failed to start");
			}
		}
	}

	private void stopSilently() {
		try {
			stopMoonStone();
		} catch (LifecycleException ex) {
			// Ignore
		}
	}

	private void stopMoonStone() throws LifecycleException {
		if (Thread.currentThread().getContextClassLoader() instanceof MoonStoneEmbeddedWebappClassLoader) {
			Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
		}
		this.moonStone.stop();
	}
	
	private void destroySilently() {
		try {
			this.moonStone.destroy();
		} catch (LifecycleException ex) {
			// Ignore
		}
	}
	
	/**
	 * 添加以前删除的连接器
	 */
	private void addPreviouslyRemovedConnectors() {
		Service[] services = this.moonStone.getServer().findServices();
		for (Service service : services) {
			Connector[] connectors = this.serviceConnectors.get(service);
			if (connectors != null) {
				for (Connector connector : connectors) {
					service.addConnector(connector);
					if (!this.autoStart) {
						stopProtocolHandler(connector);
					}
				}
				this.serviceConnectors.remove(service);
			}
		}
	}

	private void stopProtocolHandler(Connector connector) {
		try {
			connector.getProtocolHandler().stop();
		}
		catch (Exception ex) {
			logger.error("不能暂停连接器: ", ex);
		}
	}
	
	/**
	 * 执行 Wrapper 的延迟加载和启动
	 */
	private void performDeferredLoadOnStartup() {
		try {
			for (Container child : this.moonStone.getHost().findChildren()) {
				if (child instanceof MoonStoneEmbeddedContext) {
					((MoonStoneEmbeddedContext) child).deferredLoadOnStartup();
				}
			}
		}
		catch (Exception ex) {
			if (ex instanceof WebServerException) {
				throw (WebServerException) ex;
			}
			throw new WebServerException("无法启动嵌入式 MoonStone 连接器", ex);
		}
	}

	/**
	 * 检查给定连接器是否已正常启动
	 */
	private void checkThatConnectorsHaveStarted() {
		checkConnectorHasStarted(this.moonStone.getConnector());
		for (Connector connector : this.moonStone.getService().findConnectors()) {
			checkConnectorHasStarted(connector);
		}
	}

	private void checkConnectorHasStarted(Connector connector) {
		if (LifecycleState.FAILED.equals(connector.getState())) {
			throw new ConnectorStartFailedException(connector.getPort());
		}
	}
	
	Map<Service, Connector[]> getServiceConnectors() {
		return this.serviceConnectors;
	}
}
