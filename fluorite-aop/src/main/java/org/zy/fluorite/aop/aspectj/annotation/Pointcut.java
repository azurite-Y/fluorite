package org.zy.fluorite.aop.aspectj.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Modifier;


/**
 * @DateTime 2020年7月5日 下午11:17:47;
 * @author zy(azurite-Y);
 * @Description
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface Pointcut {
	/**
	 * 切点的全限定名称，但方法参数无论切点方法是否有都不需在此指定。而在args()方法中指定。
	 * ps：
	 * <ol>语法示例：
	 * <li>org.zy.fluorite.aop.aspectj.annotation.Pointcut.*：将Pointcut类中定义的方法全部作为切点匹配。</li>
	 * <li>org.zy.fluorite.aop.aspectj.annotation.Pointcut.value()：将Pointcut类中定义的value方法作为切点匹配。</li>
	 * <li>org.zy.fluorite.aop.aspectj.annotation..：将此包名下的所有类中的所有方法作为切点匹配</li>
	 * <li>org.zy.fluorite.aop.aspectj.annotation..value()：将此包名下的所有类中的value方法作为切点匹配</li>
	 * </ol>
	 */
	String value() default "";

	/**
	 * 为了应对在编译之后方法参数名丢失但在程序运行时需要此信息的情况， 可在此指定参数名列表，并使用逗号分隔。
	 * 但此方法返回值的使用与否以切点表达式而定，若切点表达式中使用到了参数名但此处却未指定对应的参数名则会抛出异常
	 */
	@Deprecated
	String argNames() default "";
	
	/** 
	 * 切点方法所拥有的全部参数类型，若未指定则默认匹配无参的切点方法，未找到则抛出异常</br>
	 * ps：此数组内容会作为方法参数匹配的依据，所以若切点方法有参数则必须指定
	 */
	Class<?>[] args() default {};
	
	/**
	 * 切点表达式的语义补正（此功能暂未实现）
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
	int accessModifier() default Modifier.PUBLIC ;
	
	/** 
	 * 方法的返回值，若返回值有泛型则可连带着泛型信息。</br>
	 * <p>
	 * 如方法返回一个Object对象，那么此注解属性值就应该被定义为 {@code "Object" }</br>
	 * 如方法返回一个List对象，且其内元素类型为String类型，那么此注解属性值就应该被定义为 {@code "LIst<String>"}</br>
	 * 如方法没有返回值，那么此注解属性值就应该被定义为 {@code "void"}</br>
	 * </p>
	 * <ul>补充：
	 * <li>此返回值信息会作为方法返回值匹配的依据，所以若切点方法有返回值则必须指定，未指定则匹配失败。</li>
	 * <li>使用“*”可匹配所有的返回值包括void。</li>
	 * <li>此属性大小写不敏感。</li>
	 * <li>当前不支持泛型嵌套，只支持一对“<>”之中的泛型比对</li>
	 * <li>倘若方法返回值有泛型信息但此属性未携带泛型信息则视为泛型擦除。<br/>
	 * 此时只比对方法的返回值，返回值相同或满足继承实现关系则视为返回值匹配。</li>
	 * </ui>
	 */
	String returnType() default "*";
 }
