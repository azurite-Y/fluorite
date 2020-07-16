package org.zy.fluorite.aop.aspectj.support;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zy.fluorite.aop.aspectj.annotation.Pointcut;
import org.zy.fluorite.aop.aspectj.interfaces.PointcutExpressionParse;
import org.zy.fluorite.aop.aspectj.support.AbstractAspectJAdvisorFactory.AspectJAnnotation;
import org.zy.fluorite.core.utils.Assert;
import org.zy.fluorite.core.utils.DebugUtils;
import org.zy.fluorite.core.utils.PropertiesUtils;
import org.zy.fluorite.core.utils.ReflectionUtils;

/**
 * @DateTime 2020年7月9日 下午5:58:19;
 * @author zy(azurite-Y);
 * @Description 单例的切点匹配器
 */
public final class PointcutExpression {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	/** 引用切点和连接点方法缓存 String：”切点表达式-切面类“ */
	private final Map<String, Method> pointcutExpressionCache = new ConcurrentHashMap<>();
	
	/** 连接点方法与方法注解模型对象缓存 */
	private final Map<Method,AspectJAnnotation<?>> aspectJPointcutAnnotationCache = new ConcurrentHashMap<>();
	
	/** 连接点匹配结果缓存 String：cacheKey */
	private final Map<String , PointcutMatcher> parseResultCache = new ConcurrentHashMap<>();
	
	private PointcutExpression() {
		super();
	}
	private static PointcutExpression INSTANCE;
	
	private static PointcutExpressionParse expressionParse;
	
	/**
	 * {@linkplain PointcutExpressionParse }和{@linkplain PointcutExpression }实例。<br/>
	 * 可使用 {@linkplain PointcutExpressionParse#PROPERTY_KEY }作为属性键在项目根目录下的fluorite.factories中 <br/>
	 * 指定一个自定义的 {@linkplain  PointcutExpressionParse}实现类来处理切面映射关系。 <br/>
	 * @param pointcutClassLoader
	 * @return
	 */
	public static PointcutExpression buildPointcutExpression(ClassLoader pointcutClassLoader) {
		if (expressionParse == null) {
			URL url = ClassLoader.getSystemResource("fluorite.factories");
			if (url == null) {
				expressionParse = new DefaultPointcutExpressionParse();
			}else {
				try {
					Properties load = PropertiesUtils.load(url.openStream(),false);
					String property = load.getProperty(PointcutExpressionParse.PROPERTY_KEY);
					if (property != null) {
						Class<?> forName = ReflectionUtils.forName(property);
						expressionParse = (PointcutExpressionParse) ReflectionUtils.instantiateClass(forName);
					} else {
						expressionParse = new DefaultPointcutExpressionParse();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return INSTANCE = (INSTANCE == null ? new PointcutExpression() : INSTANCE);
	}
	
	/**
	 * 根据给定的切面信息判断此切面方法是否适配与当前Bean
	 * @param targetClass - 适配Bean的Class对象
	 * @param aspectJAnnotation - 切面方法的注解包装对象
	 * @param aspectClass - 切面类
	 * @param method - 切面方法
	 * @return 如果适配则返回true，反之则返回false
	 */
	public boolean parse(Class<?> targetClass , AspectJAnnotation<?>  aspectJAnnotation ,  Class<?> aspectClass , Method adviceMethod) {
		String cacheKey = null;
		String expression = aspectJAnnotation.getAnnotationValue("value", String.class);
		// 切面类中定义的切点
		AspectJAnnotation<?> aspectJoinPointcutAnnotation = null;
		Method aspectJPointcutMethod = null;
		/*
		 * 若表达式中未包含“.”则为引用切面类中的连接点方法，如"pointcut()"
		 * 反之则为注解独有的切点表达式
		 */
		if (expression.indexOf(".") == -1) { // 引用切面类中的连接点方法
			StringBuilder builder = new StringBuilder();
			builder.append(expression).append("-").append(aspectClass.getName());
			Method aspectJPointcutMethodCache = this.pointcutExpressionCache.get(builder.toString());
			if (aspectJPointcutMethodCache == null) {
				// 查找此方法，并获取其@Pointcut注解。连接点方法必须为无参方法
				try {
					aspectJPointcutMethod = aspectClass.getDeclaredMethod(expression.substring(0, expression.length()-2));
					Assert.notNull(aspectJPointcutMethod , "未找到引用的无参连接点方法，by expression："+expression +"，class"+ aspectClass);
					
					Pointcut pointcut = aspectJPointcutMethod.getDeclaredAnnotation(Pointcut.class);
					Assert.notNull(pointcut , "引用的连接点方法未标注有@Pointcut注解，by expression："+expression +"，class"+ aspectClass);

					// 覆盖原始的切点注解对象，也就是说若引用了连接点方法则忽略除value属性以外的其他属性
					aspectJoinPointcutAnnotation = new AspectJAnnotation<>(pointcut);
					
					// 添加缓存
					this.pointcutExpressionCache.put(builder.toString(), aspectJPointcutMethod);
					this.aspectJPointcutAnnotationCache.put(aspectJPointcutMethod, aspectJoinPointcutAnnotation);
				} catch (NoSuchMethodException e) {
					throw new IllegalArgumentException("切面中定义的连接点方法必须为无参方法！" , e);
				} catch (SecurityException e) {
					e.printStackTrace();
				}
			} else {
				aspectJPointcutMethod = aspectJPointcutMethodCache;
				// 尝试查找缓存的匹配结果
				cacheKey = getCacheKey(aspectJPointcutMethod, targetClass);
				PointcutMatcher falg = this.parseResultCache.get(cacheKey);
				if (falg != null) {
//					DebugUtils.logFromAop(logger, "缓存的切面匹配结果：["+!falg.isExpire()+"]"+"，by aspectClass："+aspectClass.toString()+"，targetClass："+targetClass.getSimpleName()
//						+"，aspectMethod："+adviceMethod.getName()+"，on expression："+expression);
					return !falg.isExpire();
				}
				
				// 执行到此都没有返回则代表需判断切面与当前Bean是否适配，则在此获得之前缓存的连接点注解对象
				aspectJoinPointcutAnnotation = this.aspectJPointcutAnnotationCache.get(aspectJPointcutMethod);
			}
		} else { // 那么就使用参数传递的数值
			aspectJoinPointcutAnnotation = aspectJAnnotation; 
			aspectJPointcutMethod = adviceMethod;
			/*
			 * 若一个通知方法没有引用连接点方法，那么就视之为切面类中唯一的通知方法，所以此处不加入到缓存序列中。
			 * 若一个切面类中若存多个通知方法，且多个通知方法都指向了一个切点，那么就宜采用引用连接点方法的方式来组织切面通知，
			 * 而不是为它们单独的设置切面表达式。
			 * 本实现鼓励使用统一的连接点来对切面中定义的通知方法进行分组，通过连接点的第一次匹配结果来描述这组通知方法是否适配给定Bean。
			 */
//			this.aspectJPointcutAnnotationCache.put(adviceMethod, aspectJAnnotation);
			
			// 尝试查找缓存的匹配结果
			cacheKey = getCacheKey(aspectJPointcutMethod, targetClass);
			PointcutMatcher falg = this.parseResultCache.get(cacheKey);
			if (falg != null) {
//				logger.info("缓存的切面匹配结果：["+!falg.isExpire()+"]"+"，by aspectClass："+aspectClass.toString()+"，targetClass："+targetClass.getSimpleName()
//					+"，aspectMethod："+adviceMethod.getName()+"，on expression："+expression);
				return !falg.isExpire();
			}
		}
		PointcutMatcher parse = expressionParse.parse(targetClass, aspectJoinPointcutAnnotation, aspectClass, aspectJPointcutMethod);
		cacheKey = (cacheKey == null ? getCacheKey(aspectJPointcutMethod, targetClass) :  cacheKey);
		this.parseResultCache.put(cacheKey, parse);
		if (!parse.isExpire()) {
			DebugUtils.logFromAop(logger, "找到的适配切面："+aspectClass.toString() +"，织入范围："
					+ (parse.isMatcherMethods() ? "所有方法" :   "单个方法-["+parse.getPointcutMethod().toString()+"]")  +"，by："+targetClass.toString());
			return true;
		}
//		DebugUtils.logFromAop(logger, "缓存的切面匹配结果：["+false+"]"+"，by aspectClass："+aspectClass.toString()+"，targetClass："+targetClass.getSimpleName()
//			+"，aspectMethod："+adviceMethod.getName()+"，on expression："+expression);
		return false;
	}
	
	/**
	 * 根据给定的参数信息生成缓存key
	 * @param aspectJPointcutMethod - 完整的切点表达式持有方法，可能是连接点方法亦或者不引用连接点方法的Advice方法
	 * @param targetClass - 适配类的Class对象
	 * @return
	 */
	private String getCacheKey(Method aspectJPointcutMethod , Class<?> targetClass ) {
		StringBuilder builder = new StringBuilder();
		return builder.append(aspectJPointcutMethod.toString())	. append("-").append(targetClass.getName()). toString();
	}

	/**
	 * 根据给定的切面信息判断此切面方法是否适配于当前Bean的给定方法
	 * @param targetClass - 适配Bean的Class对象
	 * @param aspectJAnnotation - 切面方法的注解包装对象
	 * @param aspectClass - 切面类
	 * @param method - 切面方法
	 * @return 如果适配则返回true，反之则返回false
	 */
	public boolean parse(Class<?> targetClass, Method method, AspectJAnnotation<?> aspectJAnnotation,
			Class<?> aspectClass, Method aspectMethod) {
		String expression = aspectJAnnotation.getAnnotationValue("value", String.class);
		String cacheKey = null;
		PointcutMatcher pointcutMatcher = null;
		if (expression.indexOf(".") == -1) { // 引用切面类中的连接点方法
			StringBuilder builder = new StringBuilder();
			builder.append(expression).append("-").append(aspectClass.getName());
			Method aspectJPointcutMethodCache = this.pointcutExpressionCache.get(builder.toString());
			Assert.notNull(aspectJPointcutMethodCache , "当前方法必须在四个参数的parse(...)方法调用返回true时才能调用当前方法。");
			
			cacheKey = this.getCacheKey(aspectJPointcutMethodCache, targetClass);
			pointcutMatcher = this.parseResultCache.get(cacheKey);
		} else {
			cacheKey = this.getCacheKey(aspectMethod, targetClass);
			pointcutMatcher = this.parseResultCache.get(cacheKey);
		}
		
		if (!pointcutMatcher.isExpire()) {
			if (pointcutMatcher.isMatcherMethods() || pointcutMatcher.getPointcutMethod().equals(method)) {
				return true;
			}
		}
		return false ;
	}
}
