package org.zy.fluorite.beans.factory.interfaces;

import org.zy.fluorite.beans.interfaces.BeanDefinition;

/**
 * @DateTime 2020年6月17日 下午10:59:26;
 * @author zy(azurite-Y);
 * @Description bean名称发现程序实现接口
 */
public interface BeanNameGenerator {
	/**
	 * 为给定的BeanDefinition生成bean名称
	 * @param definition
	 * @param registry
	 * @return
	 */
	String generateBeanName(BeanDefinition definition, BeanDefinitionRegistry registry);
}
