package org.zy.fluorite.beans.factory.utils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.zy.fluorite.beans.factory.interfaces.BeanFactory;
import org.zy.fluorite.core.utils.Assert;

/**
 * @author: zy(azurite-Y);
 * @DateTime: 2020年6月9日 下午12:52:52;
 * @Description
 */
public class BeanFactoryUtils {
	/** 存储截去“&”符号的beanName */
	private static final Map<String, String> transformedBeanNameCache = new ConcurrentHashMap<>();
	
	/**
	 * 截除工厂Bean的标识“&”
	 * @param name
	 * @return
	 */
	public static String transformedBeanName(String name) {
		Assert.hasText(name, "'name' 不能为null或空串");
		if (!name.startsWith(BeanFactory.FACTORY_BEAN_PREFIX)) {
			return name;
		}
		return transformedBeanNameCache.computeIfAbsent(name, beanName -> {
			return 	new String(beanName.toCharArray(),1,beanName.length() - 1);
		});
	}

	public static boolean isFactoryBeanInstance(String name) {
		return (name != null && name.startsWith(BeanFactory.FACTORY_BEAN_PREFIX));
	}
}
