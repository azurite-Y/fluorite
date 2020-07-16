package org.zy.fluorite.core.annotation;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.zy.fluorite.core.annotation.filter.FilterType;
import org.zy.fluorite.core.interfaces.TypeFilter;

/**
 * @author: zy(azurite-Y);
 * @DateTime: 2020年6月15日 下午5:16:10;
 * @Description
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Repeatable(ComponentScans.class)
public @interface ComponentScan {
	
	/**
	 * 定义包扫描路径，可以是单个路径，也可以是扫描的路径数组
	 * @return
	 */
	String[] value() default {};
	
	/**
	 * 指定具体的扫描的类
	 * @return
	 */
	Class<?>[] basePackageClasses() default {};
	
	 /**
	  * 控制符合组件检测条件的文件后缀，由24英文字母所组成
	 * @return
	 */
	String resourcePattern() default  "class";
	
    /**
     * 指定组件扫描的约束
     * @see org.zy.fluorite.core.annotation.filter.AnnotationTypeFilter
     * @see org.zy.fluorite.core.annotation.filter.AssignableTypeFilter
     * @see org.zy.fluorite.core.annotation.filter.RegexPatternTypeFilter
     * 
     * @return
     */
    Filter[] excludeFilters() default {};
    
    /**
     * 是否建立组件索引，为true则代表建立索引。在找到索引文件时就不会进行包扫描。
     * 索引默认为根目录下的"/Component.indexed"文件
     * @see org.springframework.core.type.filter.AspectJTypeFilter
     * @return
     */
    boolean useIndexed() default true;
    
    /**
     * 扫描到的类是否都开启懒加载 ，默认是不开启的
     * @see org.zy.fluorite.core.annotation.filter.RegexPatternTypeFilter
     * @return
     */
    boolean lazyInit() default false;
	
    @interface  Filter {
    	/**
    	 * 要使用的筛选器类型，根据类型的不同有不同的筛选行为。默认为FilterType.ANNOTATION
    	 * @return
    	 */
    	FilterType type() default FilterType.ANNOTATION;
    	
    	/**
    	 * 使用的筛选器Class对象
    	 * @return
    	 */
    	Class<? extends TypeFilter>[] value() default {};
    	
    	/**
    	 * 根据 {@link #type()} 的取值其内容的作用会发生改变。</br>
    	 * 有且仅当 type = FilterType.REGEX 或 FilterType.ASPECTJ 时才会生效。</br>
    	 * 如<code>type=FilterType.REGEX</code>时，</br>
    	 * 其返回值是作为正则表达式来处理文件扫描结果，匹配则排除此结果。</br>
    	 * 与@ComponentScan的resourcePattern属性不同，resourcePattern控制的是文件后缀。</br>
    	 * 而pattern控制的是文件名的格式内容。
    	 * @return
    	 */
    	String pattern() default "";

    	/**
    	 * 指定AnnotationTypeFilter的构造器参数，使之不排除标注指定注解的Class对象。
    	 * 默认为 @Component 注解
    	 * @see org.zy.fluorite.core.annotation.filter.AnnotationTypeFilter
    	 */
		Class<? extends Annotation>[] annotations() default Component.class;

		/**
		 * 指定AssignableTypeFilter的构造器参数，使之排除指定的Class对象，
		 * @return
		 * @see org.zy.fluorite.core.annotation.filter.AssignableTypeFilter
		 */
		Class<?> source() default Object.class;
    }
    
}
