package org.zy.fluorite.core.subject;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.zy.fluorite.core.utils.Assert;

/**
 * @author: zy(azurite-Y);
 * @DateTime: 2020年6月5日 下午1:18:08;
 * @Description 存储单个构造器、方法、属性的多个注解信息[key：注解名，value：注解对象].
 */
@SuppressWarnings("serial")
public class AnnotationAttributes extends LinkedHashMap<Class<? extends Annotation>, List<Annotation>> {
	private final String UNKNOWN = "unknown";

	/** 存储单个构造器、方法、属性、参数的对象 */
	private AnnotatedElement element;
	
	/** 存储当前Member是类对象（-1）或是构造器（0）还是方法（1）亦或者是属性（2）、参数（3）的整数 */
	private Integer modifiers = null;

	/** 存储类、构造器、方法、属性、参数的全限定名称 */
	private String displayName;

	public AnnotationAttributes() {
		super();
		this.element = null;
		this.displayName = UNKNOWN;
	}

	public AnnotationAttributes(AnnotatedElement element, Map<Class<? extends Annotation>, List<Annotation>> map) {
		super(map);
		this.element = element;
		
		if (element instanceof Class<?>) {
			this.modifiers = -1;
			this.displayName = ((Class<?>)element).getName();
		} else if (element instanceof Constructor<?>) {
			this.modifiers = 0;
			this.displayName = ((Constructor<?>)element).getName();
		} else if (element instanceof Method) {
			this.modifiers = 1;
			this.displayName = ((Method)element).getName();
		} else if (element instanceof Field) {
			this.modifiers = 2;
			this.displayName = ((Field)element).getName();
		} else if (element instanceof Parameter) {
			this.modifiers = 3;
			this.displayName = ((Parameter)element).getName();
		} else {
			Assert.isTrue(true, "参数传递错误，'element'参数类型必须是[Class<?>、Constructor<?>、Method、Field、Parameter]其中之一。by element："+element.getClass().getName());
		}
	}

	public AnnotationAttributes(Class<?> clz) {
		super();
		this.element = clz;
		this.modifiers = -1;
		this.displayName = clz.getName();
	}

	public AnnotationAttributes(Constructor<?> constructor) {
		super();
		this.element = constructor;
		this.modifiers = 0;
		this.displayName = constructor.getName();
	}

	public AnnotationAttributes(Method method) {
		this.modifiers = 1;
		this.element = method;
		this.displayName = method.getName();
	}

	public AnnotationAttributes(Field field) {
		super();
		this.element = field;
		this.modifiers = 2;
		this.displayName = field.getName();
	}

	public AnnotationAttributes(Parameter parameter) {
		super();
		this.element = parameter;
		this.modifiers = 3;
		this.displayName = parameter.getName();
	}

	public AnnotationAttributes(AnnotationAttributes other) {
		super(other);
		this.element = other.element;
		this.displayName = other.displayName;
		this.modifiers = other.modifiers;
	}

	public Integer getModifiers() {
		return modifiers;
	}

	public String getDisplayName() {
		return displayName;
	}

	public AnnotatedElement getElement() {
		return element;
	}

	/**
	 * 获得指定类型的非java注解（能标注在注解上的注解）
	 * @param <T>
	 * 
	 * @param <T>
	 * @param annoClz
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> List<T> getAnnotationList(Class<T> annoClz) {
		return (List<T>)this.get(annoClz);
	}
	
	/**
	 * 获得指定类型的非java注解（不能标注在注解上的注解）
	 * @param <T>
	 * @param annoClz
	 * @return 若指定类型的注解不能标注在其他注解上，
	 * 那么在getAnnotationList()方法返回值不为null的情况下，List容器长度最大为1
	 */
	public <T> T getAnnotation(Class<T> annoClz) {
		List<T> annotation = getAnnotationList(annoClz);
		return Assert.notNull(annotation) ? annotation.get(0) : null;
	}

	@Override
	public String toString() {
		return "AnnotationAttributes [element=" + element + ", modifiers=" + modifiers + ", displayName=" + displayName
				+ "，Annotation={"+super.entrySet()+ "} ]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((element == null) ? 0 : element.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		AnnotationAttributes other = (AnnotationAttributes) obj;
		if (element == null) {
			if (other.element != null)
				return false;
		} else if (!element.equals(other.element))
			return false;
		return true;
	}
}
