package org.zy.fluorite.core.environment;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zy.fluorite.core.convert.SimpleConversionServiceStrategy;
import org.zy.fluorite.core.environment.interfaces.ConfigurableEnvironment;
import org.zy.fluorite.core.environment.interfaces.PropertySource;
import org.zy.fluorite.core.environment.interfaces.PropertySource.SimplePropertySource;
import org.zy.fluorite.core.interfaces.ConversionServiceStrategy;
import org.zy.fluorite.core.utils.Assert;
import org.zy.fluorite.core.utils.CollectionUtils;
import org.zy.fluorite.core.utils.DebugUtils;
import org.zy.fluorite.core.utils.PropertiesUtils;
import org.zy.fluorite.core.utils.StringUtils;

/**
 * @DateTime 2020年6月16日 下午6:24:08;
 * @author zy(azurite-Y);
 * @Description
 */
public abstract class AbstractEnvironment implements ConfigurableEnvironment {
	protected final Logger logger = LoggerFactory.getLogger(getClass());
	
	private ConversionServiceStrategy conversionServiceStrategy = new SimpleConversionServiceStrategy();
	
	protected final MutablePropertySources propertySources = new MutablePropertySources(conversionServiceStrategy);
	
	protected final Set<String> activeProfiles = new LinkedHashSet<>();
	
	protected final Set<String> defaultProfiles = new LinkedHashSet<>(getReservedDefaultProfiles());
	
	/** 配置文件路径： /application.properties、/config/application.properties*/
	static final String[] APPLICATION_PROPERTY_PAYH = {"application.properties","config/application.properties"};
	
	protected Set<String> getReservedDefaultProfiles() {
		return Collections.singleton(Property.PROFILES_DEFAULT);
	}
	
	@Override
	public String[] getActiveProfiles() {
		return StringUtils.toStringArray(doGetActiveProfiles());
	}

	@Override
	public void setActiveProfiles(String... profiles) {
		Assert.hasText("Profile数组不能为null" , profiles);
		if (DebugUtils.debug) {
			logger.info("启用的 profiles：" + Arrays.asList(profiles));
		}
		synchronized (this.activeProfiles) {
			this.activeProfiles.clear();
			for (String profile : profiles) {
				validateProfile(profile);
				this.activeProfiles.add(profile);
			}
		}
	}

	private Set<String> doGetActiveProfiles() {
		synchronized (this.activeProfiles) {
			if (this.activeProfiles.isEmpty()) {
				String profiles = this.getProperty(Property.PROFILES_ACTIVVE);
				if (Assert.hasText(profiles)) {
					setActiveProfiles(profiles);
				}
			}
			return this.activeProfiles;
		}
	}

	private Set<String> doGetDefaultProfiles() {
		synchronized (this.defaultProfiles) {
			if (this.defaultProfiles.equals(getReservedDefaultProfiles())) {
				String profiles = getProperty(Property.PROFILES_DEFAULT);
				if (Assert.hasText(profiles)) {
					setDefaultProfiles(profiles);
				}
			}
			return this.defaultProfiles;
		}
	}

	@Override
	public String[] getDefaultProfiles() {
		return StringUtils.toStringArray(this.doGetDefaultProfiles());
	}

	@Override
	public boolean acceptsProfiles(String... profiles) {
		Assert.hasText("必须至少指定一个配置文件",profiles);
		for (String profile : profiles) {
			if (Assert.hasText(profile) && profile.charAt(0) == 33) {
				if (!isProfileActive(new String(profile.toCharArray(),1,profile.length()-1))) {
					return true;
				}
			} else if (isProfileActive(profile)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void setDefaultProfiles(String... profiles) {
		Assert.notNull(profiles, "Profile数组不能为null");
		synchronized (this.defaultProfiles) {
			this.defaultProfiles.clear();
			for (String profile : profiles) {
				validateProfile(profile);
				this.defaultProfiles.add(profile);
			}
		}
	}

	@Override
	public MutablePropertySources getPropertySources() {
		return this.propertySources;
	}
	
	@Override
	public boolean containPropertySources(String name) {
		return this.propertySources.contains(name);
	}

	@Override
	public void addActiveProfile(String profile) {
		if (logger.isDebugEnabled()) {
			logger.info("启用的 profile：" + profile);
		}
		validateProfile(profile);
		doGetActiveProfiles();
		synchronized (this.activeProfiles) {
			this.activeProfiles.add(profile);
		}
	}

	/**
	 * 判断指定后缀是否是启用的后缀
	 * @param profile
	 * @return
	 */
	protected boolean isProfileActive(String profile) {
		validateProfile(profile);
		Set<String> currentActiveProfiles = doGetActiveProfiles();
		return (currentActiveProfiles.contains(profile) ||
				(currentActiveProfiles.isEmpty() && doGetDefaultProfiles().contains(profile)));
	}

	private void validateProfile(String profile) {
		Assert.hasText(profile, "失效的profiles：必须包含文本");
		Assert.isTrue(profile.charAt(0) != 33, "无效的profiles：其包含!，by："+profile);
	}

	@Override
	public boolean containsProperty(String key) {
		return this.propertySources.containsProperty(key);
	}

	@Override
	public String getProperty(String key) {
		return this.propertySources.getProperty(key);
	}

	@Override
	public List<String> getPropertyToList(String key) {
		return this.propertySources.getPropertyToList(key);
	}
	
	@Override
	public String getProperty(String key, String defaultValue) {
		return this.propertySources.getProperty(key,defaultValue);
	}

	@Override
	public <T> T getProperty(String key, Class<T> targetType) {
		return this.propertySources.getProperty(key,targetType);
	}

	@Override
	public <T> T getProperty(String key, Class<T> targetType, T defaultValue) {
		return this.propertySources.getProperty(key,targetType,defaultValue);
	}

	@Override
	public <T> T getRequiredProperty(String key, Class<T> targetType) throws IllegalStateException {
		return this.propertySources.getRequiredProperty(key,targetType);
	}

	@Override
	public String getRequiredProperty(String key) throws IllegalStateException {
		return this.propertySources.getRequiredProperty(key);
	}

	@Override
	public void setRequiredProperties(String... requiredProperties) {
		this.propertySources.setRequiredProperties(requiredProperties);
	}

	@Override
	public void validateRequiredProperties() {
		this.propertySources.validateRequiredProperties();
	}

	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Map<String, String> getSystemProperties() {
		return (Map)System.getProperties();
	}

	@Override
	public Map<String, String> getSystemEnvironment() {
		return System.getenv();
	}

	@Override
	public void merge(ConfigurableEnvironment parent) {
		for (PropertySource<?> ps : parent.getPropertySources()) {
			if (!this.propertySources.contains(ps.getName())) {
				this.propertySources.addLast(ps);
			}
		}
		String[] parentActiveProfiles = parent.getActiveProfiles();
		if (Assert.notNull(parentActiveProfiles)) {
			synchronized (this.activeProfiles) {
				Collections.addAll(this.activeProfiles, parentActiveProfiles);
			}
		}
		String[] parentDefaultProfiles = parent.getDefaultProfiles();
		if (Assert.notNull(parentDefaultProfiles)) {
			synchronized (this.defaultProfiles) {
				this.defaultProfiles.remove(Property.PROFILES_DEFAULT);
				Collections.addAll(this.defaultProfiles, parentDefaultProfiles);
			}
		}
	}

	/**
	 * 自定义添加PropertySource对象
	 * @param propertySources
	 */
	@Override
	public void customizePropertySources() {
		Map<String, String> property = getApplicationProperty();
		this.propertySources.addLast(new SimplePropertySource(CONFIG_PROPERTY , property ));
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Map<String,String> getApplicationProperty() {
		for (String configPath : APPLICATION_PROPERTY_PAYH) {
			// 抑制未找到配置文件的异常
			Map<String, String> properties = (Map)PropertiesUtils.load(configPath, "UTF-8", true);
			if (properties != null) {
				DebugUtils.log(logger, "加载配置文件成功，by path："+configPath);
				return properties;
			}
		}
		logger.warn("未在默认的配置文件路径下找到所需的配置文件，by path："+CollectionUtils.asList(APPLICATION_PROPERTY_PAYH));
		return new HashMap<>();
	}
}
