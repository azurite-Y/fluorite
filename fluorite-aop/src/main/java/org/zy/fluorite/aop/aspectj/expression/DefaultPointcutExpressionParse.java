package org.zy.fluorite.aop.aspectj.expression;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zy.fluorite.aop.aspectj.interfaces.PointcutExpressionParse;
import org.zy.fluorite.aop.aspectj.support.AbstractAspectJAdvisorFactory.AspectJAnnotation;
import org.zy.fluorite.aop.aspectj.support.PointcutMatcher;
import org.zy.fluorite.core.convert.ResolvableType;
import org.zy.fluorite.core.utils.Assert;
import org.zy.fluorite.core.utils.StringUtils;

/**
 * @DateTime 2020年7月9日 下午11:37:28;
 * @author zy(azurite-Y);
 * @Description 默认的切点表达式解析策略
 */
public class DefaultPointcutExpressionParse implements PointcutExpressionParse {
	public static final String EXECUTION = "execution";
	protected final Logger logger = LoggerFactory.getLogger(getClass());
	
	/** 是否还需匹配方法信息 */
	boolean matcherMethod;

	@Override
	public boolean support(String prefix) {
		return EXECUTION.equalsIgnoreCase(prefix);
	}
	
	@Override
	public PointcutMatcher parse(Class<?> targetClass, AspectJAnnotation<?> aspectJAnnotation, Class<?> aspectClass,
			Method method) {
		PointcutMatcher parseClassRank = parseClassRank(targetClass,aspectClass, aspectJAnnotation);
		if (!parseClassRank.isMark() && matcherMethod) {
			parseMethodRank(parseClassRank,targetClass,aspectClass);
		}
		return parseClassRank;
	}

	/**
	 * 此方法比对切点表达式中提供的包名、类名、方法名，且方法名不提供参数信息。<br/>
	 * 也就是说对于方法只预先比对方法名，而在此方法返回true和matcherMethod为true的情况下才正式开始比对方法。 <br/>
	 * 而当matcherMethod为false但方法返回true则代表切点表达式使用".."或"*"的方式匹配了所有的方法。 <br/>
	 * @param targetClass
	 * @param aspectClass
	 * @param aspectJAnnotation
	 * @return
	 */
	private PointcutMatcher parseClassRank(Class<?> targetClass , Class<?> aspectClass , AspectJAnnotation<?> aspectJAnnotation) {
		/*
		 * 考虑缓存解析切点表达式获得到的[packageStr、methodName、intactClassName、
		 * doubleDelimitresFlag、wildcardCharacterFlag]属性， 以提高性能。
		 */
		//----------------------------------开始解析-----------------------------------------------
		matcherMethod = true; // 设置默认值
		
		String expression = aspectJAnnotation.getAnnotationValue("value", String.class);
		int doubleDelimitres = expression.indexOf(PointcutExpressionParse.DOUBLE_DELIMITRES);
		int wildcardCharacter = expression.indexOf(PointcutExpressionParse.WILDCARD_CHARACTER);

		boolean doubleDelimitresFlag = (doubleDelimitres != -1 ? true : false);
		boolean wildcardCharacterFlag = (wildcardCharacter != -1 ? true : false);

		
		// 包路径
		String packageStr = "";
		// 方法名
		String methodName = "";
		// 类全限定名
		String intactClassName = "";
		PointcutMatcher pointcutMatcher = new PointcutMatcher();
		if (doubleDelimitresFlag && !wildcardCharacterFlag) {
			/*
			 * 处理以下格式的切点表达式：
			 * 	String a = "org.zy.fluorite.aop.aspectj.annotation..";
			 * String b = "org.zy.fluorite.aop.aspectj.annotation..value()"; 
			 */
			packageStr = expression.substring(0, doubleDelimitres);
			if (doubleDelimitres + 2 == expression.length()) { // ".."位于字符串结尾
				matcherMethod = false;
				pointcutMatcher.setMatcherMethods(true);
			} else { // ".."之后还有内容
				// 截除"()"
				methodName = expression.substring(doubleDelimitres + 2, expression.length() -2);
			}

			// 切点匹配{验证包路径和指定Class对象是否有指定名称的方法}
			if (targetClass.getPackage().getName().equals(packageStr)) {
				if (matcherMethod) { // ".."之后还有内容
					Method method = null;
					try {
						method = targetClass.getMethod(methodName
								,(Class<?> [])aspectJAnnotation.getAnnotationValue("args") );
					} catch (Exception e) {
						pointcutMatcher.setMark(false);
						return pointcutMatcher;
						// e.printStackTrace();
					}
					pointcutMatcher.setAspectJJoinPointcutAnnotation(aspectJAnnotation);
					pointcutMatcher.addPointcutMethod(method);
				}
				// 此时matcherMethod确定等于false
				return pointcutMatcher;
			}
		} else if (wildcardCharacterFlag && !doubleDelimitresFlag) {
			/*
			 * 处理以下格式的切点表达式：
			 * String a= "org.zy.fluorite.aop.aspectj.annotation.Pointcut.*";
			 */
			intactClassName = expression.substring(0, wildcardCharacter - 1);

			// 切点匹配
			if ( targetClass.getName().equals(intactClassName) ) {
				matcherMethod = false;
				pointcutMatcher.setMatcherMethods(true);
				return pointcutMatcher;
			}
		} else { // 不包含“..”和“*”
			/*
			 * 处理以下格式的切点表达式：
			 * String b= "org.zy.fluorite.aop.aspectj.annotation.Pointcut.value()";
			 */
			StringBuilder builder = new StringBuilder();
			StringBuilder cache = new StringBuilder();
			builder.append(expression);
			builder.reverse();
			int indexOf = builder.indexOf(".");
			// 方法名，从2开始截去字符串中的"()"
			String substring = builder.substring(2, indexOf);
			methodName = cache.append(substring).reverse().toString();

			// 类的全限定名，+1为排除符号"."
			String substring2 = builder.substring(indexOf + 1, builder.length());
			cache.delete(0, builder.length()).append(substring2);
			intactClassName = cache.reverse().toString();

			// 切点匹配
			if (targetClass.getName().equals(intactClassName)) {
				Method method = null;
				try {
					Class<?> [] args = (Class<?> []) aspectJAnnotation.getAnnotationValue("args");
					method = targetClass.getMethod(methodName , args);
				} catch (Exception e) {
					// 无法获取指定方法则代表不匹配
					pointcutMatcher.setMark(false);
					return pointcutMatcher;
				} 
				pointcutMatcher.setAspectJJoinPointcutAnnotation(aspectJAnnotation);
				pointcutMatcher.addPointcutMethod(method);
				return pointcutMatcher;
			} else {
				matcherMethod = false;
				pointcutMatcher.setMatcherMethods(false);
			}
		}
		
		return pointcutMatcher;
	}

	/**
	 * 比对方法值和方法的访问修饰符
	 * @param pointcutMatcher - 在{@linkplain #parseClassRank(String, Class, Class, AspectJAnnotation)} 方法中使用 
	 * {@linkplain PointcutMatcher#PointcutMatcher(Method, AspectJAnnotation, String, String, String) }创建的实例
	 * @param targetClass - 适配Bean的Class对象
	 * @param aspectClass - 切面Bean的Class对象
	 * @param aspectJAnnotation - 适配的切面方法注解信息
	 * @return 返回true则表示匹配，反之则代表不匹配
	 */
	private void parseMethodRank(PointcutMatcher pointcutMatcher, Class<?> targetClass , Class<?> aspectClass ) {
		// 因当前我还未完全掌握Aspectj的“execution”、“within”等Aspect语义字符的用法，所以prefix属性当前暂未拥有实际功能
		List<Method> methods = pointcutMatcher.getPointcutMethods();
		AspectJAnnotation<?> pointcutAnnotation = pointcutMatcher.getAspectJJoinPointcutAnnotation();
		List<Method> matcherMethod = new ArrayList<>();
		for (Method method : methods) {
			// args属性在获取方法对象的时候比对过了，若方法参数不符则无法正常获得Method对象，所以此处不再匹配
			if (parseReturnType(pointcutAnnotation,method) && parseAccessModifier(pointcutAnnotation,method)) {
				matcherMethod.add(method);
			}
		}
		pointcutMatcher.setPointcutMethods(matcherMethod);
	}

	/**
	 * 方法修饰符匹配
	 * @param pointcutAnnotation
	 * @param method
	 * @return
	 */
	private boolean parseAccessModifier(AspectJAnnotation<?> pointcutAnnotation, Method method) {
		Integer accessModifier = pointcutAnnotation.getAnnotationValue("accessModifier", Integer.class);
		return method.getModifiers() == accessModifier;
	}

	/**
	 * 返回值匹配
	 * @param pointcutAnnotation - 连接点方法@Pointcut注解模型对象
	 * @param method - 目标切点方法对象
	 * @return
	 */
	private boolean parseReturnType(AspectJAnnotation<?> pointcutAnnotation, Method method) {
		String annotationValue = pointcutAnnotation.getAnnotationValue("returnType", String.class);
		Assert.hasText(annotationValue,"@Pointcut的returnType()属性不能为空串.");
		if (!annotationValue.equals("*")) {
			int indexOf = annotationValue.indexOf("<");
			int indexOf2 = annotationValue.indexOf(">");

			boolean indexOfFlag = (indexOf != -1 ? true : false);
			boolean indexOf2Flag = (indexOf2 != -1 ? true : false);

			String simpleName = method.getReturnType().getSimpleName();
			if (indexOfFlag && indexOf2Flag) { 
				String substring = annotationValue.substring(0,indexOf);
				if (!simpleName.equalsIgnoreCase(substring)) {
					return false;
				}

				// 截取注解的returnType属性中的泛型信息
				String generic = annotationValue.substring(indexOf+1, indexOf2);
				String[] arr = StringUtils.tokenizeToStringArray(generic, ",", null);

				Type genericReturnType = method.getGenericReturnType();
				ResolvableType[] generics = ResolvableType.forClass(genericReturnType).getGenerics();
				// 返回值不同不构成方法的重载，所以此处严格匹配泛型参数
				Assert.isTrue(arr.length == generics.length, "泛型个数不匹配，by method："+method.toString()
				+ "，设置匹配的泛型："+Arrays.asList(arr));

				Class<?> current = null;
				for (int i = 0; i < generics.length; i++) {
					current = generics[i].resolve();
					if (!current.getSimpleName().equalsIgnoreCase(arr[i])) {
						return false;
					}
				}
			} else if ( (indexOfFlag && !indexOf2Flag) || (indexOfFlag && indexOf2Flag) ) {
				throw new  IllegalArgumentException("@Pointcut的returnType()属性格式错误，by："+annotationValue);
			} else { // 泛型擦除或方法返回值本身就没有泛型信息
				if (!simpleName.equalsIgnoreCase(annotationValue)) {
					return false;
				}
			}
		}
		return true;
	}
}
