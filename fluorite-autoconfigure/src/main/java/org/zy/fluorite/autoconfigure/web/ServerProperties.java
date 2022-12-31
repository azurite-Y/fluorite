package org.zy.fluorite.autoconfigure.web;

import java.io.File;
import java.net.InetAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.zy.fluorite.autoconfigure.web.server.Compression;
import org.zy.fluorite.autoconfigure.web.servlet.server.Encoding;
import org.zy.fluorite.autoconfigure.web.servlet.server.ErrorProperties;
import org.zy.fluorite.autoconfigure.web.servlet.server.Session;
import org.zy.fluorite.core.annotation.ConfigurationProperties;
import org.zy.fluorite.core.annotation.NestedConfigurationProperty;

/**
 * @dateTime 2022年12月9日;
 * @author zy(azurite-Y);
 * @description
 */
@ConfigurationProperties(prefix = "server", ignoreUnknownFields = true)
public class ServerProperties {
	/** 服务器HTTP端口 */
	private Integer port;

	/** 服务器应绑定到的网络地址 */
	private InetAddress address;
	
	/** 用于服务器响应报头的值(如果为空，则不发送报头) */
	private String serverHeader;
	
	/**
	 * HTTP消息头的最大大小
	 */
	private int maxHttpHeaderSize = 8192;
	
	@NestedConfigurationProperty
	private ErrorProperties error = new ErrorProperties();
	
	@NestedConfigurationProperty
	private Servlet servlet = new Servlet();
	
	@NestedConfigurationProperty
	private MoonStone moonStone = new MoonStone();
	
	@NestedConfigurationProperty
	private Compression compression = new Compression();
	
	
	
	public String getServerHeader() {
		return serverHeader;
	}
	public void setServerHeader(String serverHeader) {
		this.serverHeader = serverHeader;
	}
	public Servlet getServlet() {
		return servlet;
	}
	public void setServlet(Servlet servlet) {
		this.servlet = servlet;
	}
	public Compression getCompression() {
		return compression;
	}
	public void setCompression(Compression compression) {
		this.compression = compression;
	}
	public Integer getPort() {
		return port;
	}
	public void setPort(Integer port) {
		this.port = port;
	}
	public InetAddress getAddress() {
		return address;
	}
	public void setAddress(InetAddress address) {
		this.address = address;
	}
	public ErrorProperties getError() {
		return error;
	}
	public void setError(ErrorProperties error) {
		this.error = error;
	}
	public int getMaxHttpHeaderSize() {
		return maxHttpHeaderSize;
	}
	public void setMaxHttpHeaderSize(int maxHttpHeaderSize) {
		this.maxHttpHeaderSize = maxHttpHeaderSize;
	}
	public MoonStone getMoonStone() {
		return moonStone;
	}
	public void setMoonStone(MoonStone moonStone) {
		this.moonStone = moonStone;
	}

	
	public static class MoonStone {
		/** 保存传入协议的报头，通常命名为“X-Forwarded-Proto” */
		private String protocolHeader;

		/** 协议头的值，该值指示传入请求是否使用SSL */
		private String protocolHeaderHttpsValue = "https";

		/** 用于覆盖原始端口值的HTTP报头的名称 */
		private String portHeader = "X-Forwarded-Port";

		/** 从中提取远程IP的HTTP报头的名称。例如,“X-FORWARDED-FOR” */
		private String remoteIpHeader;

		/** 从中提取远程主机的HTTP报头的名称  */
		private String hostHeader = "X-Forwarded-Host";
		
		/** 线程相关配置 */
		private Threads threads = new Threads();
		
		/** MoonStone 基本目录。如果未指定，则依据 {@link ServerProperties.MoonStone#useTempBaseDir } 决定使用临时目录还是类加载目录。 */
		private File basedir;
		
		/** 使用临时目录作为 {@link ServerProperties.MoonStone#basedir } */
		private boolean useTempBaseDir = false;
		
		/** 设置应用程序加载根目录，若使用的是相对目录则相对于 {@link ServerProperties.MoonStone#basedir } */
		private String appBaseDir;
		
		/** 调用BackationProcess方法之间的延迟(以秒为单位)。默认值为-1则不使用 backgroundProcess() */
//		private Duration backgroundProcessorDelay = Duration.ofSeconds(10);
		private int backgroundProcessorDelay = -1;

		/** 此 Web 应用程序的可重新加载标识 */
		private boolean reloadableContext = false;
		
		/** 最大工作线程数 */
		private int maxThreads = 200;

		/** 最小数量的工作线程 */
		private int minSpareThreads = 10;
		
		/** 任何HTTP POST请求中表单内容的最大大小(20M) */
		private int maxHttpFormPostSize = 20971520;
		
		/** 是否应通过将/附加到路径来重定向对上下文根的请求 */
		private Boolean redirectContextRoot = true;

		/** 调用sendReDirect生成的HTTP1.1及更高版本的Location标头将使用相对重定向还是绝对重定向。  */
		private boolean useRelativeRedirects;

		/** 用于解码URI的字符编码 */
		private Charset uriEncoding = StandardCharsets.UTF_8;

		/** 服务器在任何给定时间接受和处理的最大连接数。一旦达到限制，操作系统仍然可以根据“acceptCount”属性接受连接。 */
		private int maxConnections = 10000;
		
		/** 当所有可能的请求处理线程都在使用时，传入连接请求的最大队列长度。 */
		private int acceptCount = 100;

		/** 将保留在缓存中并在后续请求中重用的空闲处理器的最大数量。当设置为-1时，缓存将不受限制，理论上最大大小等于最大连接数。 */
		private int processorCache = 200;
		
		/** 连接器在接受连接后等待呈现请求URI行的时间 */
		private Duration connectionTimeout = Duration.ofMillis(20000l);

		
		
		public boolean isUseTempBaseDir() {
			return useTempBaseDir;
		}
		public void setUseTempBaseDir(boolean useTempBaseDir) {
			this.useTempBaseDir = useTempBaseDir;
		}
		public String getProtocolHeader() {
			return protocolHeader;
		}
		public void setProtocolHeader(String protocolHeader) {
			this.protocolHeader = protocolHeader;
		}
		public String getProtocolHeaderHttpsValue() {
			return protocolHeaderHttpsValue;
		}
		public void setProtocolHeaderHttpsValue(String protocolHeaderHttpsValue) {
			this.protocolHeaderHttpsValue = protocolHeaderHttpsValue;
		}
		public String getPortHeader() {
			return portHeader;
		}
		public void setPortHeader(String portHeader) {
			this.portHeader = portHeader;
		}
		public String getRemoteIpHeader() {
			return remoteIpHeader;
		}
		public void setRemoteIpHeader(String remoteIpHeader) {
			this.remoteIpHeader = remoteIpHeader;
		}
		public String getHostHeader() {
			return hostHeader;
		}
		public void setHostHeader(String hostHeader) {
			this.hostHeader = hostHeader;
		}
		public int getMaxThreads() {
			return maxThreads;
		}
		public void setMaxThreads(int maxThreads) {
			this.maxThreads = maxThreads;
		}
		public int getMinSpareThreads() {
			return minSpareThreads;
		}
		public void setMinSpareThreads(int minSpareThreads) {
			this.minSpareThreads = minSpareThreads;
		}
		public Threads getThreads() {
			return threads;
		}
		public void setThreads(Threads threads) {
			this.threads = threads;
		}
		public File getBasedir() {
			return basedir;
		}
		public void setBasedir(File basedir) {
			this.basedir = basedir;
		}
		public String getAppBaseDir() {
			return appBaseDir;
		}
		public void setAppBaseDir(String appBaseDir) {
			this.appBaseDir = appBaseDir;
		}
		public int getBackgroundProcessorDelay() {
			return backgroundProcessorDelay;
		}
		public void setBackgroundProcessorDelay(int backgroundProcessorDelay) {
			this.backgroundProcessorDelay = backgroundProcessorDelay;
		}
		public boolean isReloadableContext() {
			return reloadableContext;
		}
		public void setReloadableContext(boolean reloadableContext) {
			this.reloadableContext = reloadableContext;
		}
		public int getMaxHttpFormPostSize() {
			return maxHttpFormPostSize;
		}
		public void setMaxHttpFormPostSize(int maxHttpFormPostSize) {
			this.maxHttpFormPostSize = maxHttpFormPostSize;
		}
		public Boolean getRedirectContextRoot() {
			return redirectContextRoot;
		}
		public void setRedirectContextRoot(Boolean redirectContextRoot) {
			this.redirectContextRoot = redirectContextRoot;
		}
		public boolean isUseRelativeRedirects() {
			return useRelativeRedirects;
		}
		public void setUseRelativeRedirects(boolean useRelativeRedirects) {
			this.useRelativeRedirects = useRelativeRedirects;
		}
		public Charset getUriEncoding() {
			return uriEncoding;
		}
		public void setUriEncoding(Charset uriEncoding) {
			this.uriEncoding = uriEncoding;
		}
		public int getMaxConnections() {
			return maxConnections;
		}
		public void setMaxConnections(int maxConnections) {
			this.maxConnections = maxConnections;
		}
		public int getAcceptCount() {
			return acceptCount;
		}
		public void setAcceptCount(int acceptCount) {
			this.acceptCount = acceptCount;
		}
		public int getProcessorCache() {
			return processorCache;
		}
		public void setProcessorCache(int processorCache) {
			this.processorCache = processorCache;
		}
		public Duration getConnectionTimeout() {
			return connectionTimeout;
		}
		public void setConnectionTimeout(Duration connectionTimeout) {
			this.connectionTimeout = connectionTimeout;
		}
		

		/** MoonStone 线程属性 */
		public static class Threads {

			/** 工作线程的最大数量 */
			private int max = 200;

			/** 工作线程的最小数量 */
			private int minSpare = 10;

			public int getMax() {
				return this.max;
			}
			public void setMax(int max) {
				this.max = max;
			}
			public int getMinSpare() {
				return this.minSpare;
			}
			public void setMinSpare(int minSpare) {
				this.minSpare = minSpare;
			}
		}
	}
	
	/**
	 * Servlet properties.
	 */
	public static class Servlet {
		/** Servlet上下文初始化参数 */
		private Map<String, String> contextParameters = new HashMap<>();

		/** 是否启用Servlet环境支持 */
		private boolean enable = true;
		/** 应用程序的主上下文路径 */
//		private String contextPath;

		/** 应用程序的显示名称 */
		private String applicationDisplayName = "application";

		/** 是否向容器注册默认Servlet */
		private boolean registerDefaultServlet = true;

		@NestedConfigurationProperty
		private Encoding encoding = new Encoding();

		@NestedConfigurationProperty
		private Session session = new Session();

		
		public boolean isEnable() {
			return enable;
		}
		public void setEnable(boolean enable) {
			this.enable = enable;
		}
		public Map<String, String> getContextParameters() {
			return contextParameters;
		}
		public void setContextParameters(Map<String, String> contextParameters) {
			this.contextParameters = contextParameters;
		}
//		public String getContextPath() {
//			return contextPath;
//		}
//		public void setContextPath(String contextPath) {
//			this.contextPath = contextPath;
//		}
		public String getApplicationDisplayName() {
			return applicationDisplayName;
		}
		public void setApplicationDisplayName(String applicationDisplayName) {
			this.applicationDisplayName = applicationDisplayName;
		}
		public boolean isRegisterDefaultServlet() {
			return registerDefaultServlet;
		}
		public void setRegisterDefaultServlet(boolean registerDefaultServlet) {
			this.registerDefaultServlet = registerDefaultServlet;
		}
		public Encoding getEncoding() {
			return encoding;
		}
		public void setEncoding(Encoding encoding) {
			this.encoding = encoding;
		}
		public Session getSession() {
			return session;
		}
		public void setSession(Session session) {
			this.session = session;
		}
	}
}
