package org.zy.fluorite.autoconfigure.web.server;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.SessionCookieConfig;

import org.zy.fluorite.autoconfigure.web.server.interfaces.ConfigurableServletWebServerFactory;
import org.zy.fluorite.autoconfigure.web.servlet.interfaces.ServletContextInitializer;
import org.zy.fluorite.autoconfigure.web.servlet.server.Session;
import org.zy.fluorite.core.utils.Assert;

/**
 * @DateTime 2021年12月23日;
 * @author zy(azurite-Y);
 * @Description
 */
public abstract class AbstractServletWebServerFactory extends AbstractConfigurableWebServerFactory implements ConfigurableServletWebServerFactory {
	private String contextPath = "";

	private String displayName;

	private Session session = new Session();

	private boolean registerDefaultServlet = true;

	private MimeMappings mimeMappings = new MimeMappings(MimeMappings.DEFAULT);

	private List<ServletContextInitializer> initializers = new ArrayList<>();

	private Map<Locale, Charset> localeCharsetMappings = new HashMap<>();

	private Map<String, String> initParameters = Collections.emptyMap();

	/**
	 * 检查给定的上下文路径
	 * 
	 * @param contextPath - 上下文路径
	 */
	private void checkContextPath(String contextPath) {
		if (contextPath == null) {
			throw new IllegalArgumentException("指定的contextPath不能为null");
		}
		if (!contextPath.isEmpty()) {
			if ("/".equals(contextPath)) { // 防止再次注册一个根上下文
				throw new IllegalArgumentException("必须使用空字符串指定根ConextPath");
			}
			if (!contextPath.startsWith("/") || contextPath.endsWith("/")) {
				throw new IllegalArgumentException("ContextPath 需以 '/' 开头和 不以 '/' 结尾");
			}
		}
	}
	
	@Override
	public void setContextPath(String contextPath) {
		checkContextPath(contextPath);
		this.contextPath = contextPath;		
	}

	/**
	 * 返回web服务器的上下文路径。路径将以“/”开头，并以“/“结尾。根上下文由空字符串表示。
	 * @return 上下文路径
	 */
	public String getContextPath() {
		return this.contextPath;
	}
	
	@Override
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getDisplayName() {
		return this.displayName;
	}
	
	/**
	 * @return true代表必须注册默认的Servlet
	 */
	public boolean isRegisterDefaultServlet() {
		return this.registerDefaultServlet;
	}
	
	@Override
	public void setRegisterDefaultServlet(boolean registerDefaultServlet) {
		this.registerDefaultServlet = registerDefaultServlet;
	}
	
	@Override
	public void setSession(Session session) {
		this.session = session;
	}
	
	public Session getSession() {
		return this.session;
	}

	/**
	 * Returns the mime-type mappings.
	 * @return mime-type 的映射
	 */
	public MimeMappings getMimeMappings() {
		return this.mimeMappings;
	}

	@Override
	public void setMimeMappings(MimeMappings mimeMappings) {
		this.mimeMappings = new MimeMappings(mimeMappings);
	}

	@Override
	public void setDocumentRoot(File documentRoot) {
		// TODO
//		this.documentRoot.setDirectory(documentRoot);
	}

	@Override
	public void setInitializers(List<? extends ServletContextInitializer> initializers) {
		Assert.notNull(initializers, "Initializers 不能为 null");
		this.initializers = new ArrayList<>(initializers);		
	}

	@Override
	public void addInitializers(ServletContextInitializer... initializers) {
		Assert.notNull(initializers, "Initializers 不能为 null");
		this.initializers.addAll(Arrays.asList(initializers));		
	}

	/**
	 * 返回Locale到Charset的映射
	 * 
	 * @return Charset的映射
	 */
	public Map<Locale, Charset> getLocaleCharsetMappings() {
		return this.localeCharsetMappings;
	}
	
	@Override
	public void setLocaleCharsetMappings(Map<Locale, Charset> localeCharsetMappings) {
		Assert.notNull(localeCharsetMappings, "localeCharsetMappings 不能为 null");
		this.localeCharsetMappings = localeCharsetMappings;		
	}

	@Override
	public void setInitParameters(Map<String, String> initParameters) {
		this.initParameters = initParameters;
	}
	
	/**
	 * 实用程序方法，子类可以使用该方法将指定的 {@link ServletContextInitializer} 参数与此实例中定义的参数结合起来
	 * 
	 * @param initializers - 要合并的初始化器
	 * @return 合并初始化项的完整集合(指定的参数先出现)
	 */
	protected final ServletContextInitializer[] mergeInitializers(ServletContextInitializer... initializers) {
		List<ServletContextInitializer> mergedInitializers = new ArrayList<>();
		mergedInitializers.add((servletContext) -> this.initParameters.forEach(servletContext::setInitParameter));
		mergedInitializers.add(new SessionConfiguringInitializer(this.session));
		mergedInitializers.addAll(Arrays.asList(initializers));
		mergedInitializers.addAll(this.initializers);
		return mergedInitializers.toArray(new ServletContextInitializer[0]);
	}

	protected final File getValidSessionStoreDir(boolean mkdirs) {
		return this.session.getSessionStoreDirectory().getValidDirectory(mkdirs);
	}
	
	
	/**
	 * {@link ServletContextInitializer} 应用 {@link Session} 配置的适当部分
	 */
	private static class SessionConfiguringInitializer implements ServletContextInitializer {

		private final Session session;

		SessionConfiguringInitializer(Session session) {
			this.session = session;
		}

		@Override
		public void onStartup(ServletContext servletContext) throws ServletException {
			if (this.session.getTrackingModes() != null) {
				servletContext.setSessionTrackingModes(unwrap(this.session.getTrackingModes()));
			}
			configureSessionCookie(servletContext.getSessionCookieConfig());
		}

		private void configureSessionCookie(SessionCookieConfig config) {
			Session.Cookie cookie = this.session.getCookie();
			if (cookie.getName() != null) {
				config.setName(cookie.getName());
			}
			if (cookie.getDomain() != null) {
				config.setDomain(cookie.getDomain());
			}
			if (cookie.getPath() != null) {
				config.setPath(cookie.getPath());
			}
			if (cookie.getComment() != null) {
				config.setComment(cookie.getComment());
			}
			if (cookie.getHttpOnly() != null) {
				config.setHttpOnly(cookie.getHttpOnly());
			}
			if (cookie.getSecure() != null) {
				config.setSecure(cookie.getSecure());
			}
			if (cookie.getMaxAge() != null) {
				config.setMaxAge((int) cookie.getMaxAge().getSeconds());
			}
		}

		private Set<javax.servlet.SessionTrackingMode> unwrap(Set<Session.SessionTrackingMode> modes) {
			if (modes == null) {
				return null;
			}
			Set<javax.servlet.SessionTrackingMode> result = new LinkedHashSet<>();
			for (Session.SessionTrackingMode mode : modes) {
				result.add(javax.servlet.SessionTrackingMode.valueOf(mode.name()));
			}
			return result;
		}

	}
}
