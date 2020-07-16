package org.zy.fluorite.context.support.undetermined;

import org.zy.fluorite.beans.factory.aware.BeanNameAware;
import org.zy.fluorite.context.interfaces.ApplicationContext;
import org.zy.fluorite.core.interfaces.instantiation.InitializingBean;
import org.zy.fluorite.core.utils.Assert;
import org.zy.fluorite.core.utils.StringUtils;

/**
 * @DateTime 2020年6月17日 下午5:35:47;
 * @author zy(azurite-Y);
 * @Description AbstractRefreshableApplicationContext子类，用于添加指定配置位置的公共处理
 */
@Deprecated
public abstract class AbstractRefreshableConfigApplicationContext extends AbstractRefreshableApplicationContext
		implements BeanNameAware, InitializingBean {
	/** 配置路径之间的分隔符 */
	private static final String CONFIG_LOCATION_DELIMITERS = ",;";

	/** 配置路径数组 */
	private String[] configLocations;

	private boolean setIdCalled = false;

	public AbstractRefreshableConfigApplicationContext() {
	}

	public AbstractRefreshableConfigApplicationContext(ApplicationContext parent) {
		super(parent);
	}

	@Override
	public void setId(String id) {
		super.setId(id);
		this.setIdCalled = true;
	}

	@Override
	public void setBeanName(String name) {
		if (!this.setIdCalled) {
			super.setId(name);
			setDisplayName("ApplicationContext '" + name + "'");
		}
	}

	public void setConfigLocation(String location) {
		setConfigLocations(StringUtils.tokenizeToStringArray(location, CONFIG_LOCATION_DELIMITERS,null));
	}

	protected String[] getConfigLocations() {
		return (this.configLocations != null ? this.configLocations : getDefaultConfigLocations());
	}

	protected String[] getDefaultConfigLocations() {
		return null;
	}

	/**
	 * 不支持任何表达式
	 * 
	 * @param locations
	 */
	public void setConfigLocations(String... locations) {
		if (locations != null) {
			Assert.notNull(locations, "配置文件路径不能为null或空串");
			this.configLocations = new String[locations.length];
			for (int i = 0; i < locations.length; i++) {
				this.configLocations[i] = locations[i].trim();
			}
		} else {
			this.configLocations = null;
		}
	}

	@Override
	public void afterPropertiesSet() {
		if (!isActive()) {
			refresh();
		}
	}

}
