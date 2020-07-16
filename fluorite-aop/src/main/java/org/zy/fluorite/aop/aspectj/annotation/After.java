package org.zy.fluorite.aop.aspectj.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Modifier;

/**
 * @DateTime 2020年7月5日 下午11:28:29;
 * @author zy(azurite-Y);
 * @Description
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface After {
	/**
	 * 切点的全限定名称，但方法参数无论切点方法是否有都不需在此指定。
	 * 而在args()方法中指定。
	 * <p>
	 * ps：</br>
	 * 若要引用连接点的切点信息则只需指定连接点方法名称+“()”格式的字符串即可。</br>
	 * 如连接点方法名称为pointcut，则此属性只需设置为“pointcurt()就可关联”。</br>
	 * 但若指定了此格式的字符串就会默认忽略此注解的其他属性配置。
	 * * @see Pointcut#value() 
	 */
	String value() default "";

	/**
	 * 为了应对在编译之后方法参数名丢失但在程序运行时需要此信息的情况， 可在此指定参数名列表，并使用逗号分隔。
	 * 但此方法返回值的使用与否以切点表达式而定，若切点表达式中使用到了参数名但此处却未指定对应的参数名则会抛出异常
	 */
	@Deprecated
	String argNames() default "";
	
	/**
	 * 切点表达式的语义补正
	 * <ol>支持的语法前缀：
	 * <li>execution</li>
	 * <li>within</li>
	 * <li>args</li>
	 * <li>this</li>
	 * <li>target</li>
	 * </ol>
	 */
	String prefix() default "execution";
	
	/**
	 * 访问修饰符约束
	 * <ol>支持的修饰符约束：
	 * <li>public</li>
	 * <li>protected</li>
	 * <li>default</li>
	 * <li>private</li>
	 * <li>abstract</li>
	 * </ol>
	 */
	int[] accessModifier() default Modifier.PUBLIC ;
	
	/** 
	 * 方法的返回值，若返回值有泛型则可连带着泛型信息。</br>
	 * <p>
	 * 如方法返回一个Object对象，那么此注解属性值就应该被定义为 {@code Object }</br>
	 * 如方法返回一个List对象，且其内元素类型为String类型，那么此注解属性值就应该被定义为 {@code LIst<String>}</br>
	 * </p>
	 * ps：此返回值信息会作为方法返回值匹配的依据，所以若切点方法有返回值则必须指定
	 */
	String returnType() default "void";
}
