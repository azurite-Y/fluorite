package org.zy.fluorite.autoconfigure.aop;

import org.zy.fluorite.aop.autoproxy.AnnotationAwareAspectJAutoProxyCreator;
import org.zy.fluorite.context.annotation.conditional.ConditionalOnClass;
import org.zy.fluorite.core.annotation.Bean;
import org.zy.fluorite.core.utils.ReflectionUtils;

/**
 * @DateTime 2020年7月13日 上午1:10:07;
 * @author zy(azurite-Y);
 * @Description
 */
public class AopAutoConfiguration {
	
	@Bean
	@ConditionalOnClass(type =  {"net.sf.cglib.proxy.Enhancer"} )
	public AnnotationAwareAspectJAutoProxyCreator createAnnotationAwareAspectJAutoProxyCreator() {
		return new AnnotationAwareAspectJAutoProxyCreator();
	}
}
