package org.zy.fluorite.transaction.annotation;

import java.lang.reflect.Method;

import org.zy.fluorite.core.utils.ClassUtils;
import org.zy.fluorite.core.utils.DebugUtils;
import org.zy.fluorite.transaction.interceptor.DefaultTransactionAttribute;
import org.zy.fluorite.transaction.interfaces.TransactionAttribute;

/**
 * @DateTime 2021年9月17日;
 * @author zy(azurite-Y);
 * @Description 
 */
public class AnnotationTransactionAttributeSource extends AbstractTransactionAttributeSource{

	@Override
	protected TransactionAttribute findTransactionAttribute(Class<?> clazz) {
		Transactional annotation = clazz.getAnnotation(Transactional.class);
		TransactionAttribute source = null;
		if (annotation != null) {
			source = new DefaultTransactionAttribute(annotation);
			super.clzAttributeTemplateCache.put(clazz, source);
			DebugUtils.logFromTransaction(logger, "发现'@Transactional'注解，by class [" + clazz.getName() + "]");;
		}
		return source;
	}

	@Override
	protected TransactionAttribute findTransactionAttribute(Method method) {
		Transactional annotation = method.getAnnotation(Transactional.class);
		TransactionAttribute source = null;
		if (annotation != null) {
			source = new DefaultTransactionAttribute(annotation);
			super.methodAttributeCache.put(method, source);
			DebugUtils.logFromTransaction(logger, "发现'@Transactional'注解，by method [" + ClassUtils.getFullyQualifiedName(method) + "]");;
		}
		return source;
	}

}
