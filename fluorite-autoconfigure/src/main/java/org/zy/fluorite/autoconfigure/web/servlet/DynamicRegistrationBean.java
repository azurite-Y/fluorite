package org.zy.fluorite.autoconfigure.web.servlet;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.Registration;
import javax.servlet.ServletContext;

import org.zy.fluorite.core.utils.Assert;
import org.zy.fluorite.core.utils.StringUtils;

/**
 * @dateTime 2022年12月7日;
 * @author zy(azurite-Y);
 * @description Servlet 3.0+基于动态的注册bean的基类
 */
public abstract class DynamicRegistrationBean<D extends Registration.Dynamic> extends RegistrationBean {
	private String name;
	/** 异步支持 */
	private boolean asyncSupported = true;
	/** 初始化参数集 */
	private Map<String, String> initParameters = new LinkedHashMap<>();

	/**
	 * 设置此注册的名称。如果未指定，将使用bean名称
	 * 
	 * @param name - 注册的名称
	 */
	public void setName(String name) {
		Assert.hasText(name, "Name 不能为 null 或空串");
		this.name = name;
	}

	/**
	 * 设置此注册是否支持异步操作。如果未指定，则默认为true。
	 * 
	 * @param asyncSupported - 如果支持异步
	 */
	public void setAsyncSupported(boolean asyncSupported) {
		this.asyncSupported = asyncSupported;
	}

	/**
	 * 返回此注册是否支持异步操作。
	 * 
	 * @return 如果支持异步
	 */
	public boolean isAsyncSupported() {
		return this.asyncSupported;
	}

	/**
	 * 为此注册设置init参数。调用此方法将替换任何现有的init参数。
	 * 
	 * @param initParameters - init参数
	 * 
	 * @see #getInitParameters
	 * @see #addInitParameter
	 */
	public void setInitParameters(Map<String, String> initParameters) {
		Assert.notNull(initParameters, "InitParameters 不能为 null");
		this.initParameters = new LinkedHashMap<>(initParameters);
	}

	/**
	 * 返回注册init参数的可变映射。
	 * 
	 * @return init 参数
	 */
	public Map<String, String> getInitParameters() {
		return this.initParameters;
	}

	/**
	 * 添加一个init参数，用相同的名称替换任何现有参数
	 * 
	 * @param name - init参数名
	 * @param value - init参数名值
	 */
	public void addInitParameter(String name, String value) {
		Assert.notNull(name, "Name 不能为 null");
		this.initParameters.put(name, value);
	}

	@Override
	protected final void register(String description, ServletContext servletContext) {
		D registration = addRegistration(description, servletContext);
		if (registration == null) {
			logger.info(StringUtils.capitalize(description) + " 未注册（可能已注册?)");
			return;
		}
		configure(registration);
	}

	protected abstract D addRegistration(String description, ServletContext servletContext);

	protected void configure(D registration) {
		registration.setAsyncSupported(this.asyncSupported);
		if (!this.initParameters.isEmpty()) {
			registration.setInitParameters(this.initParameters);
		}
	}

	/**
	 * 推断出此注册的名称。将返回用户指定的名称或回退到基于约定的命名。
	 * 
	 * @param value - 用于基于约定的名称的对象
	 * @return 推断出的名字
	 */
	protected final String getOrDeduceName(Object value) {
		return this.name;
	}
}
