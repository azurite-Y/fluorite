package org.zy.fluorite.aop.aspectj.support;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zy.fluorite.aop.aspectj.annotation.After;
import org.zy.fluorite.aop.aspectj.annotation.AfterReturning;
import org.zy.fluorite.aop.aspectj.annotation.AfterThrowing;
import org.zy.fluorite.aop.aspectj.annotation.Around;
import org.zy.fluorite.aop.aspectj.annotation.Aspect;
import org.zy.fluorite.aop.aspectj.annotation.Before;
import org.zy.fluorite.aop.aspectj.annotation.Pointcut;
import org.zy.fluorite.aop.aspectj.interfaces.AspectJAdvisorFactory;
import org.zy.fluorite.aop.exception.AopConfigException;
import org.zy.fluorite.core.exception.FluoriteRuntimeException;
import org.zy.fluorite.core.interfaces.AnnotationMetadata;
import org.zy.fluorite.core.interfaces.ParameterNameDiscoverer;
import org.zy.fluorite.core.subject.AnnotationValuesAttributes;
import org.zy.fluorite.core.utils.AnnotationUtils;
import org.zy.fluorite.core.utils.Assert;

/**
 * @DateTime 2020年7月5日 下午10:50:58;
 * @author zy(azurite-Y);
 * @Description 此类进行注解解析和验证功能。
 */
public abstract class AbstractAspectJAdvisorFactory implements AspectJAdvisorFactory {
	protected final Logger logger = LoggerFactory.getLogger(getClass());

//	protected final ParameterNameDiscoverer parameterNameDiscoverer = new AspectJAnnotationParameterNameDiscoverer();

	/** AfterThrowing、AfterReturning、After、Around、Before、Pointcut */
	protected static final Class<?>[] ASPECTJ_ANNOTATION_CLASSES = new Class<?>[] { 
		AfterThrowing.class , AfterReturning.class , After.class , Around.class, Before.class, Pointcut.class,  };	

	private static final Map<Method , AspectJAnnotation<?>> ASPECT_ANNOTATION_CACHE = new HashMap<>();
			
	@Override
	public boolean isAspect(AnnotationMetadata metadata) {
		return metadata.isAnnotatedForClass(Aspect.class);
	}

	@Override
	public void validate(Class<?> aspectClass) throws AopConfigException {
		// 如果父类有@Aspect注解但不是一个抽象类则抛出异常
		if (aspectClass.getSuperclass().getAnnotation(Aspect.class) != null
				&& !Modifier.isAbstract(aspectClass.getSuperclass().getModifiers())) {
			throw new AopConfigException(
					"[" + aspectClass.getName() + "] 继承了一个非抽象的切面类 [" + aspectClass.getSuperclass().getName() + "]");
		}
	}

	/**
	 * 获取此方法上标注的Aspectj注解
	 * @param method
	 * @param attributes
	 * @return
	 */
	@SuppressWarnings({ "unchecked" })
	public static AspectJAnnotation<?> findAspectJAnnotationOnMethod(Method method) {
		AspectJAnnotation<?> aspectJAnnotation = ASPECT_ANNOTATION_CACHE.get(method);
		if (aspectJAnnotation != null) {
			return aspectJAnnotation;
		}
		for (Class<?> annoClz : ASPECTJ_ANNOTATION_CLASSES) {
			AspectJAnnotation<?> foundAnnotation = findAnnotation(method, (Class<Annotation>) annoClz);
			if (foundAnnotation != null) {
				if ( aspectJAnnotation != null ) { // 条件成立则代表当前方法标注了多个Aspectj注解
					throw new FluoriteRuntimeException("方法用途不明，标注了多个不同职能的切面注解。此二者不可得兼：@"
							+ aspectJAnnotation.getAnnotationName() + "和@" + annoClz.getSimpleName());
				} 
				aspectJAnnotation = foundAnnotation;
				ASPECT_ANNOTATION_CACHE.put(method, foundAnnotation);
			}
		}
		return aspectJAnnotation;
	}

	private static <A extends Annotation> AspectJAnnotation<A> findAnnotation(Method method, Class<A> annoClz) {
		A result = method.getDeclaredAnnotation(annoClz);
		if (result != null) {
			return new AspectJAnnotation<>(result);
		} else {
			return null;
		}
	}

	protected enum AspectJAnnotationType {
		Pointcut, Around, Before, After, AfterReturning, AfterThrowing
	}

	/** AOP注解的模型对象 */
	public static class AspectJAnnotation<A extends Annotation> {
		private static Map<Class<?>, AspectJAnnotationType> annotationTypeMap = new HashMap<>(8);

		static {
			annotationTypeMap.put(Pointcut.class, AspectJAnnotationType.Pointcut);
			annotationTypeMap.put(Around.class, AspectJAnnotationType.Around);
			annotationTypeMap.put(Before.class, AspectJAnnotationType.Before);
			annotationTypeMap.put(After.class, AspectJAnnotationType.After);
			annotationTypeMap.put(AfterReturning.class, AspectJAnnotationType.AfterReturning);
			annotationTypeMap.put(AfterThrowing.class, AspectJAnnotationType.AfterThrowing);
		}

		/** AOP注解对象 */
		private final A annotation;

		private final AspectJAnnotationType annotationType;

		private final String pointcutExpression;

		private final String argumentNames;
		
		private AnnotationValuesAttributes valuesAttributes;
		
		public AspectJAnnotation(A annotation) {
			this.annotation = annotation;
			this.annotationType = determineAnnotationType();
			this.pointcutExpression = resolveExpression();
			valuesAttributes = AnnotationUtils.getAnnotationValueAttributes(annotation);
			Object argNames = valuesAttributes.get("argNames");
			this.argumentNames = (argNames instanceof String ? (String) argNames : "");
		}

		private String resolveExpression() {
			Object object = AnnotationUtils.invokeAnnotationMethods(this.annotation, "value");
			if (object instanceof String) {
				String str = (String) object;
				if (!str.isEmpty()) {
					return str;
				}
			}
			throw new IllegalArgumentException("指定的切点信息无效，by annotation：" + annotation);
		}

		private AspectJAnnotationType determineAnnotationType() {
			AspectJAnnotationType type = annotationTypeMap.get(this.annotation.annotationType());
			Assert.notNull(type, "未知的注解类型，可能不是有效的AspectJ注解：" + this.annotation);
			return type;
		}

		public AspectJAnnotationType getAnnotationType() {
			return this.annotationType;
		}
		public A getAnnotation() {
			return this.annotation;
		}
		public String getAnnotationName() {
			return this.annotation.annotationType().getSimpleName();
		}
		public String getPointcutExpression() {
			return pointcutExpression;
		}
		public String getArgumentNames() {
			return argumentNames;
		}
		public AnnotationValuesAttributes getAnnotationValuesAttributes() {
			return valuesAttributes;
		}
		public Object getAnnotationValue(String key) {
			return valuesAttributes.get(key);
		}
		@SuppressWarnings("unchecked")
		public <T>T getAnnotationValue(String key,Class<T> clz) {
			return (T)valuesAttributes.get(key);
		}
	}

	@SuppressWarnings("unused")
	private static class AspectJAnnotationParameterNameDiscoverer implements ParameterNameDiscoverer {

		/** 获得注解的argNames属性 */
		@Override
		public String[] getParameterNames(Method method) {
			if (method.getParameterCount() == 0) {
				return new String[0];
			}
			AspectJAnnotation<?> annotation = findAspectJAnnotationOnMethod(method);
			if (annotation == null) {
				return null;
			}
			StringTokenizer nameTokens = new StringTokenizer(annotation.getArgumentNames(), ",");
			if (nameTokens.countTokens() > 0) {
				String[] names = new String[nameTokens.countTokens()];
				for (int i = 0; i < names.length; i++) {
					names[i] = nameTokens.nextToken();
				}
				return names;
			} else {
				return null;
			}
		}

		@Override
		public String[] getParameterNames(Constructor<?> ctor) {
			throw new UnsupportedOperationException("Spring AOP cannot handle constructor advice");
		}
	}
}
