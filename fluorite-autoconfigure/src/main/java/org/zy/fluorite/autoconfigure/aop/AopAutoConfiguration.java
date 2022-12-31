package org.zy.fluorite.autoconfigure.aop;

import org.zy.fluorite.aop.autoproxy.AnnotationAwareAspectJAutoProxyCreator;
import org.zy.fluorite.context.annotation.conditional.ConditionalOnClass;
import org.zy.fluorite.core.annotation.Bean;

/**
 * @DateTime 2020年7月13日 上午1:10:07;
 * @author zy(azurite-Y);
 * @Description 自 /fluorite-autoconfigure/src/main/resources/META-INF/fluorite.factories 加载
 */
public class AopAutoConfiguration {
	@Bean
	@ConditionalOnClass(type =  {"net.sf.cglib.proxy.Enhancer"} )
	public AnnotationAwareAspectJAutoProxyCreator createAnnotationAwareAspectJAutoProxyCreator() {
		return new AnnotationAwareAspectJAutoProxyCreator();
	}
}
