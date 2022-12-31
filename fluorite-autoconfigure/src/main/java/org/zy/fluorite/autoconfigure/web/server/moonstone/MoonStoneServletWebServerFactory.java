package org.zy.fluorite.autoconfigure.web.server.moonstone;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.servlet.MultipartConfigElement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zy.fluorite.autoconfigure.web.server.AbstractServletWebServerFactory;
import org.zy.fluorite.autoconfigure.web.server.ErrorPage;
import org.zy.fluorite.autoconfigure.web.server.MimeMappings;
import org.zy.fluorite.autoconfigure.web.server.moonstone.interfaces.ConfigurableMoonStoneWebServerFactory;
import org.zy.fluorite.autoconfigure.web.server.moonstone.interfaces.MoonStoneConnectorCustomizer;
import org.zy.fluorite.autoconfigure.web.server.moonstone.interfaces.MoonStoneContextCustomizer;
import org.zy.fluorite.autoconfigure.web.server.moonstone.interfaces.MoonStoneProtocolHandlerCustomizer;
import org.zy.fluorite.autoconfigure.web.servlet.interfaces.ServletContextInitializer;
import org.zy.fluorite.core.utils.Assert;
import org.zy.fluorite.core.utils.ClassUtils;
import org.zy.fluorite.core.utils.LambdaSafe;
import org.zy.fluorite.web.server.interfaces.WebServer;
import org.zy.moonStone.core.LifecycleEvent;
import org.zy.moonStone.core.connector.Connector;
import org.zy.moonStone.core.http.AbstractProtocol;
import org.zy.moonStone.core.interfaces.connector.ProtocolHandler;
import org.zy.moonStone.core.interfaces.container.Context;
import org.zy.moonStone.core.interfaces.container.Engine;
import org.zy.moonStone.core.interfaces.container.Host;
import org.zy.moonStone.core.interfaces.container.Lifecycle;
import org.zy.moonStone.core.interfaces.container.LifecycleListener;
import org.zy.moonStone.core.interfaces.container.Valve;
import org.zy.moonStone.core.interfaces.container.Wrapper;
import org.zy.moonStone.core.loaer.WebappLoader;
import org.zy.moonStone.core.session.StandardManager;
import org.zy.moonStone.core.session.interfaces.Manager;
import org.zy.moonStone.core.startup.MoonStone;
import org.zy.moonStone.core.startup.MoonStone.FixContextListener;

/**
 * @dateTime 2021年12月23日;
 * @author zy(azurite-Y);
 * @description
 */
public class MoonStoneServletWebServerFactory extends AbstractServletWebServerFactory implements ConfigurableMoonStoneWebServerFactory {
	protected final Logger logger = LoggerFactory.getLogger(getClass());

	private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

	private static final Set<Class<?>> NO_CLASSES = Collections.emptySet();

	/**
	 * 使用的默认协议处理器的类名.
	 */
	public static final String DEFAULT_PROTOCOL = "org.zy.moonStone.core.http.Http11NioProtocol";

//	private ResourceLoader resourceLoader;
	
	private File baseDirectory;
	
	private boolean useTempBaseDir = false;
	
	private String appBaseDir;
	
	private List<Valve> engineValves = new ArrayList<>();

	private List<Valve> contextValves = new ArrayList<>();

	private List<LifecycleListener> contextLifecycleListeners = new ArrayList<>();

	private Set<MoonStoneContextCustomizer> moonStoneContextCustomizers = new LinkedHashSet<>();

	private Set<MoonStoneConnectorCustomizer> moonStoneConnectorCustomizers = new LinkedHashSet<>();

	private Set<MoonStoneProtocolHandlerCustomizer<?>> moonStoneProtocolHandlerCustomizers = new LinkedHashSet<>();

	private final List<Connector> additionalMoonStoneConnectors = new ArrayList<>();

	private String protocol = DEFAULT_PROTOCOL;

	private Charset uriEncoding = DEFAULT_CHARSET;

	private int backgroundProcessorDelay = -1;

	private boolean reloadableContext;

	private MultipartConfigElement multipartConfigElement; 
	
	
	public MoonStoneServletWebServerFactory() {}

	
	@Override
	public WebServer getWebServer(ServletContextInitializer... initializers) {
		MoonStone moonStone = new MoonStone();
		moonStone.setAppBaseDir(appBaseDir);
		
		// 创建MoonStone使用的目录
		
		File baseDir = (this.baseDirectory != null) ? 
				this.baseDirectory : 
				(useTempBaseDir) ? createTempDir("moonStone") : new File(ClassLoader.getSystemResource("").getFile());
		
		String baseDirAbsolutePath = baseDir.getAbsolutePath();
		// 设置 MoonStone 的根目录
		moonStone.setBaseDir(baseDirAbsolutePath);
		if (logger.isDebugEnabled()) {
			logger.debug("baseDir: {}", baseDirAbsolutePath);
		}
		
		Connector connector = new Connector(this.protocol);
		connector.setThrowOnFailure(true);
		/*
		 * 创建服务器和服务对象
		 * 将新连接器添加到已定义的连接器集，并将其与此服务的容器相关联
		 */
		moonStone.getService().addConnector(connector);
		
		customizeConnector(connector);
		
		moonStone.setConnector(connector);
		// 创建一个Host接口实现StandardHost的实例并设置”localhost”为name属性，然后保存到Engine(访问引擎)中，后关闭自动部署
		Host host = moonStone.getHost();
		host.setAutoDeploy(false);
		
		// 设置这个容器上的execute方法的调用与其子容器之间的延迟 10
		configureEngine(moonStone.getEngine());
		
		for (Connector additionalConnector : this.additionalMoonStoneConnectors) {
			moonStone.getService().addConnector(additionalConnector);
		}
		
		prepareContext(moonStone.getHost(), initializers);
		
		return getMoonStoneWebServer(moonStone);
	}

	/**
	 * 定制化默认连接器
	 * @param connector
	 */
	protected void customizeConnector(Connector connector) {
		// 设置端口号，若超过最大值则设置为0
		int port = Math.max(getPort(), 0);
		connector.setPort(port);
		
		if (connector.getProtocolHandler() instanceof AbstractProtocol) {
			// 定制化协议处理器
			customizeProtocol( (AbstractProtocol<?>) connector.getProtocolHandler() );
		}
		
		invokeProtocolHandlerCustomizers(connector.getProtocolHandler());
		
		if (getUriEncoding() != null) {
			// 设置编码字符集 UTF-8
			connector.setURIEncoding(getUriEncoding().name());
		}

		// 如果ApplicationContext启动缓慢，请不要过早绑定到套接字
		connector.setBindOnInit(false);
		if (getSsl() != null && getSsl().isEnabled()) {
			customizeSsl(connector);
		}
		MoonStoneConnectorCustomizer compression = new CompressionConnectorCustomizer(getCompression());
		
		compression.customize(connector);
		
		/**
		 * (maxThreads)最大工作线程数 200
		 * (minSpareThreads)最小数量的工作线程 10
		 * (maxHttpHeaderSize)HTTP消息头的最大大小 8192 (8K)
		 * (maxHttpPostSize)设置容器将自动解析的 POST 最大字节数 2097162 (2M)
		 * (maxConnections)服务器在任何给定时间接受和处理的最大连接数
		 * (acceptCount)当所有可能的请求处理线程都在使用时，传入连接请求的最大队列长度。设置服务器套接字的连接数
		 * (processorCache)将保留在缓存中并在后续请求中重用的空闲处理器的最大数量。当设置为-1时，缓存将不受限制，理论上最大大小等于最大连接数。 (processorCache)
		 */	
		for (MoonStoneConnectorCustomizer customizer : this.moonStoneConnectorCustomizers) {
			customizer.customize(connector);
		}
	}

	private void customizeProtocol(AbstractProtocol<?> protocol) {
		if (getAddress() != null) {
			protocol.setAddress(getAddress());
		}
	}
	
	@SuppressWarnings("unchecked")
	private void invokeProtocolHandlerCustomizers(ProtocolHandler protocolHandler) {
		LambdaSafe.callbacks(MoonStoneProtocolHandlerCustomizer.class, this.moonStoneProtocolHandlerCustomizers, protocolHandler)
			.invoke((customizer) -> customizer.customize(protocolHandler));
	}
	
	private void customizeSsl(Connector connector) {}
	
	private void configureEngine(Engine engine) {
		engine.setBackgroundProcessorDelay(this.backgroundProcessorDelay);
		for (Valve valve : this.engineValves) {
			engine.getPipeline().addValve(valve);
		}
	}
	
	protected void prepareContext(Host host, ServletContextInitializer[] initializers) {
		MoonStoneEmbeddedContext context = new MoonStoneEmbeddedContext();
		// 设置一个描述此容器的名称字符串。在属于特定父容器的子容器集中，容器名称必须是惟一的。【"”】
		context.setName(getContextPath());
		// 设置这个web应用程序的显示名称
		context.setDisplayName(getDisplayName());
		// 设置此上下文的上下文路径【"”】
		context.setPath(getContextPath());
		
		// 例如:  C:\Users\PC\AppData\Local\Temp\MoonStone-docbase.3023348462044690601.80
//		File docBase = createTempDir("MoonStone-docbase");
		File docBase = new File(ClassLoader.getSystemResource("").getFile());
		String docBaseAbsolutePath = docBase.getAbsolutePath();
		// 设置此上下文的文档根。这可以是绝对路径名，也可以是相对路径名。相对路径名相对于托管主机应用的根目录
		context.setDocBase(docBaseAbsolutePath);
		if (logger.isDebugEnabled()) {
			logger.debug("Context. contextPath: [{}], contextName: [{}], displayName: [{}], docBase: {}", getContextPath(), getContextPath(), getDisplayName(), docBaseAbsolutePath);
		}
		
		// 向该组件添加LifecycleEvent侦听器
		context.addLifecycleListener(new FixContextListener());
		
		// 设置这个web应用程序的父类加载器(如果有的话)。这个调用只有在配置加载器之前才有意义，并且指定的值(如果是非空)应该作为参数传递给类加载器构造函数
//		context.setParentClassLoader((this.resourceLoader != null) ? this.resourceLoader.getClassLoader() : ClassUtils.getDefaultClassLoader());
		context.setParentClassLoader(ClassUtils.getDefaultClassLoader());
		
		if (backgroundProcessorDelay > 0) {}
			context.setBackgroundProcessorDelay(backgroundProcessorDelay);
		
		// 覆盖 MoonStone 的默认语言环境映射，以与其他服务器保持一致，将en和fr映射的ISO-8869-1替换为UTF-8
		resetDefaultLocaleMapping(context);
		// 未进行任何变更
		addLocaleMappings(context);

		// 使用相对重定向为true，使用绝对重定向为false
		context.setUseRelativeRedirects(false);

		// 配置 MoonStone 是否会尝试创建此web应用程序使用的上载目标(如果web应用程序尝试使用时上载目标不存在)。
		context.setCreateUploadTargets(true);


		// 构造一个新的WebappLoader，将指定的类加载器定义为我们最终创建的类加载器的父类
		WebappLoader loader = new WebappLoader();
		// MoonStoneEmbeddedWebappClassLoader：MoonStone 的ParallelWebappClassLoader的扩展，不考虑系统类加载器。这是为了确保始终使用任何自定义上下文类装入器(就像某些可执行的存档一样)所必需的。
		loader.setLoaderClass(MoonStoneEmbeddedWebappClassLoader.class.getName());
		// 首先搜索本地库，搜索无果在委托父类
		loader.setDelegate(false);
		
		context.setReloadable(reloadableContext);
		context.setLoader(loader);
		
		if (isRegisterDefaultServlet()) { // 是否应该注册默认servlet【true】
			addDefaultServlet(context);
		}
//		context.addLifecycleListener(new StaticResourceConfigurer(context));
		
		// 关联子容器
		host.addChild(context);
		ServletContextInitializer[] initializersToUse = mergeInitializers(initializers);
		configureContext(context, initializersToUse);
		// 在服务器使用上下文之前，Post处理它。子类可以重写此方法，以便对上下文应用额外的处理。空方法
		postProcessContext(context);
	}

	protected MoonStoneWebServer getMoonStoneWebServer(MoonStone moonStone) {
		return new MoonStoneWebServer(moonStone, getPort() >= 0);
	}
	
	
	//-------------------------------------------------------getter、setter----------------------------------------------------------
	@Override
	public void setBackgroundProcessorDelay(int delay) {
		this.backgroundProcessorDelay = delay;
	}
	@Override
	public void setMultipartConfigElement(MultipartConfigElement multipartConfigElement) {
		this.multipartConfigElement = multipartConfigElement;
	}
	@Override
	public void setReloadableContext(boolean reloadableContext) {
		this.reloadableContext = reloadableContext;
	}
	@Override
	public void setBaseDirectory(File baseDirectory) {
		this.baseDirectory = baseDirectory;
	}
	@Override
	public void setUseTempBaseDir(boolean useTempBaseDir) {
		this.useTempBaseDir = useTempBaseDir;
	}
	@Override
	public void setAppBaseDir(String appBaseDir) {
		this.appBaseDir = appBaseDir;
	}
	/**
	 * 设置应用与Engine 的 {@link Valve}。调用此方法将替换任何现有Valve
	 * @param engineValves
	 */
	public void setEngineValves(Collection<? extends Valve> engineValves) {
		Assert.notNull(engineValves, "Valves must not be null");
		this.engineValves = new ArrayList<>(engineValves);
	}

	/**
	 * @return 将应用于 Engine 的可变Valve集合
	 */
	public Collection<Valve> getEngineValves() {
		return this.engineValves;
	}

	@Override
	public void addEngineValves(Valve... engineValves) {
		Assert.notNull(engineValves, "Valves不能为null");
		this.engineValves.addAll(Arrays.asList(engineValves));
	}

	/**
	 * 设置应用于{@link Context} 的 {@link Valve}. 调用此方法将替换任何现有Valve.
	 * @param contextValves the valves to set
	 */
	public void setContextValves(Collection<? extends Valve> contextValves) {
		Assert.notNull(contextValves, "Valves must not be null");
		this.contextValves = new ArrayList<>(contextValves);
	}

	/**
	 * @return 将应用于 {@link Context} 的可变{@link Valve}集合
	 * @see #getEngineValves()
	 */
	public Collection<Valve> getContextValves() {
		return this.contextValves;
	}

	@Override
	public void addContextValves(Valve... contextValves) {
		Assert.notNull(contextValves, "Valves must not be null");
		this.contextValves.addAll(Arrays.asList(contextValves));
	}

	/**
	 * 设置应应用于 {@link Context} 的 {@link LifecycleListener}. 调用此方法将替换任何现有侦听器.
	 * @param contextLifecycleListeners
	 */
	public void setContextLifecycleListeners(Collection<? extends LifecycleListener> contextLifecycleListeners) {
		Assert.notNull(contextLifecycleListeners, "ContextLifecycleListeners不能为null");
		this.contextLifecycleListeners = new ArrayList<>(contextLifecycleListeners);
	}

	/**
	 * @return 将应用于 {@link Context} 的可变 {@link LifecycleListener} 集合
	 */
	public Collection<LifecycleListener> getContextLifecycleListeners() {
		return this.contextLifecycleListeners;
	}

	/**
	 * 添加将应用于 {@link Context} 的可变 {@link LifecycleListener} 集合.
	 * @param contextLifecycleListeners
	 */
	public void addContextLifecycleListeners(LifecycleListener... contextLifecycleListeners) {
		Assert.notNull(contextLifecycleListeners, "ContextLifecycleListeners不能为null");
		this.contextLifecycleListeners.addAll(Arrays.asList(contextLifecycleListeners));
	}

	/**
	 * 设置将应用于{@link Context} 的 {@link MoonStoneContextCustomizer} 集合. 调用此方法将替换任何现有的自定义项.
	 * @param moonStoneContextCustomizers the customizers to set
	 */
	public void setMoonStoneContextCustomizers(Collection<? extends MoonStoneContextCustomizer> moonStoneContextCustomizers) {
		Assert.notNull(moonStoneContextCustomizers, "moonStoneContextCustomizers 不能为 null");
		this.moonStoneContextCustomizers = new LinkedHashSet<>(moonStoneContextCustomizers);
	}

	/**
	 * @return 将应用于 {@link Context} 的 {@link MoonStoneContextCustomizer} 可变集合
	 */
	public Collection<MoonStoneContextCustomizer> getMoonStoneContextCustomizers() {
		return this.moonStoneContextCustomizers;
	}

	@Override
	public void addContextCustomizers(MoonStoneContextCustomizer... moonStoneContextCustomizers) {
		Assert.notNull(moonStoneContextCustomizers, "MoonStoneContextCustomizers 不能为 null");
		this.moonStoneContextCustomizers.addAll(Arrays.asList(moonStoneContextCustomizers));
	}

	/**
	 * 设置将应用于{@link Connector} 的 {@link MoonStoneConnectorCustomizer} 集合. 调用此方法将替换任何现有的自定义项.
	 * @param moonStoneConnectorCustomizers
	 */
	public void setMoonStoneConnectorCustomizers(Collection<? extends MoonStoneConnectorCustomizer> moonStoneConnectorCustomizers) {
		Assert.notNull(moonStoneConnectorCustomizers, "MoonStoneConnectorCustomizers 不能为 null");
		this.moonStoneConnectorCustomizers = new LinkedHashSet<>(moonStoneConnectorCustomizers);
	}

	@Override
	public void addConnectorCustomizers(MoonStoneConnectorCustomizer... moonStoneConnectorCustomizers) {
		Assert.notNull(moonStoneConnectorCustomizers, "MoonStoneConnectorCustomizers 不能为 null");
		this.moonStoneConnectorCustomizers.addAll(Arrays.asList(moonStoneConnectorCustomizers));
	}

	/**
	 * @return 将应用于 {@link Connector} 的 {@link MoonStoneConnectorCustomizer} 可变集合.
	 */
	public Collection<MoonStoneConnectorCustomizer> getMoonStoneConnectorCustomizers() {
		return this.moonStoneConnectorCustomizers;
	}

	/**
	 * 设置将应用于 {@link Connector} 的 {@link MoonStoneProtocolHandlerCustomizer} 集合. 调用此方法将替换任何现有的自定义项.
	 * @param moonStoneProtocolHandlerCustomizer
	 */
	public void setMoonStoneProtocolHandlerCustomizers(Collection<? extends MoonStoneProtocolHandlerCustomizer<?>> moonStoneProtocolHandlerCustomizer) {
		Assert.notNull(moonStoneProtocolHandlerCustomizer, "MoonStoneProtocolHandlerCustomizers 不能为 null");
		this.moonStoneProtocolHandlerCustomizers = new LinkedHashSet<>(moonStoneProtocolHandlerCustomizer);
	}

	@Override
	public void addProtocolHandlerCustomizers(MoonStoneProtocolHandlerCustomizer<?>... moonStoneProtocolHandlerCustomizers) {
		Assert.notNull(moonStoneProtocolHandlerCustomizers, "MoonStoneProtocolHandlerCustomizers 不能为 null");
		this.moonStoneProtocolHandlerCustomizers.addAll(Arrays.asList(moonStoneProtocolHandlerCustomizers));
	}

	/**
	 * @return 将应用于 {@link Connector} 的可变 {@link MoonStoneProtocolHandlerCustomizer} 集合
	 */
	public Collection<MoonStoneProtocolHandlerCustomizer<?>> getMoonStoneProtocolHandlerCustomizers() {
		return this.moonStoneProtocolHandlerCustomizers;
	}

	/**
	 * 重写 MoonStone 的默认语言环境映射以与其他服务器保持一致
	 * 
	 * @param context - 要重置的上下文
	 * 
	 * @see org.zy.moonStone.core.util.CharsetMapperDefault.properties
	 */
	private void resetDefaultLocaleMapping(MoonStoneEmbeddedContext context) {
		context.addLocaleEncodingMappingParameter(Locale.ENGLISH.toString(), DEFAULT_CHARSET.displayName());
		context.addLocaleEncodingMappingParameter(Locale.FRENCH.toString(), DEFAULT_CHARSET.displayName());
	}

	private void addLocaleMappings(MoonStoneEmbeddedContext context) {
		getLocaleCharsetMappings().forEach(
				(locale, charset) -> context.addLocaleEncodingMappingParameter(locale.toString(), charset.toString()));
	}
	
	/**
	 * 在默认 {@link Connector} 之外添加额外的 {@link Connector} , e.g SSL
	 * @param connectors - 添加的连接器
	 */
	public void addAdditionalMoonStoneConnectors(Connector... connectors) {
		Assert.notNull(connectors, "Connectors 不能为 null");
		this.additionalMoonStoneConnectors.addAll(Arrays.asList(connectors));
	}

	@Override
	public void setUriEncoding(Charset uriEncoding) {
		this.uriEncoding = uriEncoding;		
	}
	
	/**
	 * @return 用于URL解码的字符编码
	 */
	public Charset getUriEncoding() {
		return this.uriEncoding;
	}
	
	private void addDefaultServlet(Context context) {
		Wrapper defaultServlet = context.createWrapper();
		defaultServlet.setName("default");
		defaultServlet.setServletClass("org.zy.moonStone.core.servlets.DefaultServlet");
		defaultServlet.addInitParameter("fileEncoding", "utf-8");
		defaultServlet.setLoadOnStartup(1);
		defaultServlet.setOverridable(true);
		defaultServlet.setMultipartConfigElement(multipartConfigElement);
		
		context.addChild(defaultServlet);
		context.addServletMappingDecoded("/", "default");
	}
	
	
	/**
	 * 配置 MoonStone 的{@link Context}
	 * 
	 * @param context - MoonStone 的 {@link Context}
	 * @param initializers - 要应用的初始化程序
	 */
	protected void configureContext(Context context, ServletContextInitializer[] initializers) {
		MoonStoneStarter starter = new MoonStoneStarter(initializers);
		if (context instanceof MoonStoneEmbeddedContext) {
			MoonStoneEmbeddedContext embeddedContext = (MoonStoneEmbeddedContext) context;
			embeddedContext.setStarter(starter);
			embeddedContext.setFailCtxIfServletStartFails(true);
		}
		
		context.addServletContainerInitializer(starter, NO_CLASSES);
		
		for (LifecycleListener lifecycleListener : this.contextLifecycleListeners) {
			context.addLifecycleListener(lifecycleListener);
		}
		
		for (Valve valve : this.contextValves) {
			context.getPipeline().addValve(valve);
		}
		
		for (ErrorPage errorPage : getErrorPages()) {
			org.zy.moonStone.core.util.descriptor.ErrorPage MoonStoneErrorPage = new org.zy.moonStone.core.util.descriptor.ErrorPage();
			
			MoonStoneErrorPage.setLocation(errorPage.getPath());
			MoonStoneErrorPage.setErrorCode(errorPage.getStatusCode());
			MoonStoneErrorPage.setExceptionType(errorPage.getExceptionName());
			context.addErrorPage(MoonStoneErrorPage);
		}
		
		for (MimeMappings.Mapping mapping : getMimeMappings()) {
			context.addMimeMapping(mapping.getExtension(), mapping.getMimeType());
		}
		
		configureSession(context);
		
		new DisableReferenceClearingContextCustomizer().customize(context);
		for (MoonStoneContextCustomizer customizer : this.moonStoneContextCustomizers) {
			customizer.customize(context);
		}
	}

	private void configureSession(Context context) {
		long sessionTimeout = getSessionTimeoutInMinutes();
		context.setSessionTimeout((int) sessionTimeout);
		
		Boolean httpOnly = getSession().getCookie().getHttpOnly();
		if (httpOnly != null) {
			context.setUseHttpOnly(httpOnly);
		}
		
		if (getSession().isPersistent()) {
			Manager manager = context.getManager();
			if (manager == null) {
				manager = new StandardManager();
				context.setManager(manager);
			}
			configurePersistSession(manager);
		} else {
			context.addLifecycleListener(new DisablePersistSessionListener());
		}
	}

	/**
	 * 配置持久化 Session
	 * @param manager - 会话管理器
	 */
	private void configurePersistSession(Manager manager) {
		Assert.isTrue(manager instanceof StandardManager, () -> "无法使用管理器类型" + manager.getClass().getName() + "保持HTTP会话状态");
		File dir = getValidSessionStoreDir();
		File file = new File(dir, "SESSIONS.ser");
		((StandardManager) manager).setPathname(file.getAbsolutePath());
	}

	protected final File getValidSessionStoreDir() {
		return getValidSessionStoreDir(true);
	}
	

	
	private long getSessionTimeoutInMinutes() {
		Duration sessionTimeout = getSession().getTimeout();
		if (isZeroOrLess(sessionTimeout)) {
			return 0;
		}
		return Math.max(sessionTimeout.toMinutes(), 1);
	}

	private boolean isZeroOrLess(Duration sessionTimeout) {
		return sessionTimeout == null || sessionTimeout.isNegative() || sessionTimeout.isZero();
	}

	/**
	 * 在与 MoonStone 服务器一起使用之前，对 MoonStone 上下文进行后处理。子类可以重写此方法以对 {@link Context} 应用附加处理
	 * 
	 * @param context - MoonStone 的 {@link Context}
	 */
	protected void postProcessContext(Context context) {}
	
	
	// -------------------------------------------------------------------------------------
	// 内部类
	// -------------------------------------------------------------------------------------
	/**
	 * 用于禁用 {@link StandardManager} 中的持久性。使用 {@link LifecycleListener} 是为了不干扰 MoonStone 的默认管理器创建逻辑。
	 * 
	 */
	private static class DisablePersistSessionListener implements LifecycleListener {

		@Override
		public void lifecycleEvent(LifecycleEvent event) {
			if (event.getType().equals(Lifecycle.START_EVENT)) {
				Context context = (Context) event.getLifecycle();
				Manager manager = context.getManager();
				if (manager instanceof StandardManager) {
					((StandardManager) manager).setPathname(null);
				}
			}
		}

	}
}
