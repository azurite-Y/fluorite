package org.zy.fluorite.core.subject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zy.fluorite.core.interfaces.AliasRegistry;
import org.zy.fluorite.core.utils.Assert;
import org.zy.fluorite.core.utils.DebugUtils;
import org.zy.fluorite.core.utils.StringUtils;

/**
 * @author: zy(azurite-Y);
 * @DateTime: 2020年6月8日 下午2:13:10;
 * @Description
 */
public class SimpleAliasRegistry implements AliasRegistry {
	protected final Logger logger = LoggerFactory.getLogger(getClass());
	
	/** 别名映射集合，alias：beanName */
	private final Map<String, String> aliasMap = new ConcurrentHashMap<>(16);
	
	public SimpleAliasRegistry() {}
	
	@Override
	public void registerAlias(String name, String alias) {
		Assert.hasText(name, "name不能为null或空串");
		Assert.hasText(alias, "alias不能为null或空串");
		
		synchronized (this.aliasMap) {
			if (alias.equals(name)) {
				this.aliasMap.remove(alias);
				if (DebugUtils.debug) {
					logger.info("忽略相同的别名与别名主体: {}",alias);
				}
			} else {
				String registeredName = this.aliasMap.get(alias);
				if (registeredName != null) {
					if (registeredName.equals(name)) {
						return;
					} else {
						if (DebugUtils.debug) {
							logger.info("{} 注册为 {}的别名" , alias , name);
						}
						this.aliasMap.put(alias, name);
					}
				}
			}
		}
	}

	@Override
	public void removeAlias(String alias) {
		Assert.hasText(alias, "alias不能为null或空串");
		String str = this.aliasMap.get(alias);
		Assert.hasText(str, () -> "未注册的别名" + alias);
	}

	@Override
	public boolean isAlias(String name) {
		return this.aliasMap.containsKey(name);
	}

	@Override
	public String[] getAliases(String name) {
		List<String> result = new ArrayList<>();
		synchronized (this.aliasMap) {
			retrieveAliases(name, result);
		}
		return StringUtils.toStringArray(result);
	}

	public String canonicalName(String name) {
		String canonicalName = name;
		String resolvedName;
		do {
			resolvedName = this.aliasMap.get(canonicalName);
			if (resolvedName != null) {
				canonicalName = resolvedName;
			}
		}
		while (resolvedName != null);
		return canonicalName;
	}
	
	/**
	 * 可传递地检索给定名称的所有别名
	 * @param name
	 * @param result
	 */
	protected void retrieveAliases(String name, List<String> result) {
		this.aliasMap.forEach((alias, registeredName) -> {
			if (registeredName.equals(name)) {
				result.add(alias);
				retrieveAliases(alias, result);
			}
		});
	}
}
