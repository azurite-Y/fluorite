package org.zy.fluorite.boot.devtools.autoconfigure;

import java.util.ArrayList;
import java.util.List;

import org.zy.fluorite.core.annotation.ConfigurationProperties;
import org.zy.fluorite.core.annotation.NestedConfigurationProperty;
import org.zy.fluorite.core.utils.Assert;
import org.zy.fluorite.core.utils.StringUtils;

/**
 * @dateTime 2022年12月23日;
 * @author zy(azurite-Y);
 * @description
 */
@ConfigurationProperties(prefix = "fluorite.devtools")
public class DevToolsProperties {
	@NestedConfigurationProperty
	private Restart restart = new Restart();
	
	public Restart getRestart() {
		return this.restart;
	}
	public void setRestart(Restart restart) {
		this.restart = restart;
	}
	
	/**
	 * Restart properties.
	 */
	public static class Restart {
		/**
		 * work/**: 是MoonStone中各个上下文的工作目录
		 */
		private static final String DEFAULT_RESTART_EXCLUDES = "META-INF/maven/**,"
				+ "META-INF/resources/**,resources/**,static/**,public/**,templates/**,"
				+ "**/*Test.class,**/*Tests.class,git.properties,META-INF/build-info.properties,work/**";

		/** 文件更改后是否启用自动重启 */
		private boolean enabled = true;

		/** 应排除在触发完全重新启动之外的模式 */
		private String exclude = DEFAULT_RESTART_EXCLUDES;

		/** 应排除在触发完全重新启动之外的其他模式 */
		private String additionalExclude;

		/** 轮询类路径更改之间等待的时间量, 单位为ms */
		private int pollInterval = 1000;

		/** 触发重新启动之前不需要任何类路径更改的安静时间, 单位为ms */
		private int quietPeriod = 400;

		
		public boolean isEnabled() {
			return this.enabled;
		}
		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}
		public String[] getAllExclude() {
			List<String> allExclude = new ArrayList<>();
			if (Assert.hasText(this.exclude)) {
				allExclude.addAll(StringUtils.tokenizeToStringList(exclude, ","));
			}
			if (Assert.hasText(this.additionalExclude)) {
				allExclude.addAll(StringUtils.tokenizeToStringList(additionalExclude, ","));
			}
			return StringUtils.toStringArray(allExclude);
		}
		public String getExclude() {
			return this.exclude;
		}
		public void setExclude(String exclude) {
			this.exclude = exclude;
		}
		public String getAdditionalExclude() {
			return this.additionalExclude;
		}
		public void setAdditionalExclude(String additionalExclude) {
			this.additionalExclude = additionalExclude;
		}
		public int getPollInterval() {
			return this.pollInterval;
		}
		public void setPollInterval(int pollInterval) {
			this.pollInterval = pollInterval;
		}
		public int getQuietPeriod() {
			return this.quietPeriod;
		}
		public void setQuietPeriod(int quietPeriod) {
			this.quietPeriod = quietPeriod;
		}
	}
}
