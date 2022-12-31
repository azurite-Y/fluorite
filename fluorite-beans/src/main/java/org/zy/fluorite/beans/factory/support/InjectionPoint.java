package org.zy.fluorite.beans.factory.support;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;

import org.zy.fluorite.core.interfaces.ParameterNameDiscoverer;
import org.zy.fluorite.core.interfaces.function.ObjectFactory;
import org.zy.fluorite.core.interfaces.function.ObjectProvider;
import org.zy.fluorite.core.subject.ExecutableParameter;
import org.zy.fluorite.core.utils.Assert;

/**
 * @author: zy(azurite-Y);
 * @DateTime: 2020年6月6日 下午3:00:09;
 * @Description 注入成员的简单描述符，指向方法、构造函数或字段
 */
public class InjectionPoint {
	protected ExecutableParameter executableParameter;

	protected Field field;

	private volatile Annotation[] fieldAnnotations;

	/**
	 * 当前注入参数的索引下标
	 */
	private int index;

	public InjectionPoint(ExecutableParameter executableParameter,int index) {
		Assert.notNull(executableParameter, "MethodParameter不能为null");
		this.executableParameter = executableParameter;
		this.index = index;
	}

	public InjectionPoint(Field field) {
		Assert.notNull(field, "Field不能为null");
		this.field = field;
	}
	
	public InjectionPoint(Method method) {
		Assert.notNull(method, "method不能为null");
	}

	protected InjectionPoint(InjectionPoint original) {
		this.executableParameter = (original.executableParameter != null ?
				new ExecutableParameter(original.executableParameter) : null);
		this.field = original.field;
		this.fieldAnnotations = original.fieldAnnotations;
	}

	protected InjectionPoint() {	}

	public ExecutableParameter getExecutableParameter() {
		return this.executableParameter;
	}

	public Field getField() {
		return this.field;
	}

	/**
	 * 获取与包装字段或方法/构造函数参数相关联的注释
	 */
//	public Annotation[] getAnnotations() {
//		if (this.field != null) {
//			Annotation[] fieldAnnotations = this.fieldAnnotations;
//			if (fieldAnnotations == null) {
//				fieldAnnotations = this.field.getAnnotations();
//				this.fieldAnnotations = fieldAnnotations;
//			}
//			return fieldAnnotations;
//		}
//		else {
//			return getMethodParameter().getParameterAnnotations();
//		}
//	}

	/**
	 * 返回给定类型的字段/参数的注解
	 */
//	public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
//		return (this.field != null ? this.field.getAnnotation(annotationType) :
//			getMethodParameter().getParameterAnnotation(annotationType));
//	}

	/**
	 * 返回由基础字段或方法/构造函数参数声明的类型
	 * 
	 * @param parameIndex - 获取参数的下标索引
	 */
	public Class<?> getResolveDependencyType() {
		return this.field != null ? this.field.getType() : this.executableParameter.getParameterType(index) ;
	}
	
	/**
	 * 返回由基础字段或方法/构造函数参数声明的类型，适配 {@link ObjectFactory } 依赖的解析
	 * 
	 * @param parameIndex - 获取参数的下标索引
	 */
	public Class<?> getProviderDependencyType() {
		if (this.field != null) {
			return this.field.getType();
		} else if ( ObjectFactory.class == this.executableParameter.getParameterType(index) || ObjectProvider.class == this.executableParameter.getParameterType(index) ) {
			Type[] types = this.executableParameter.getGenericParameterTypes().get(index);
			return (Class<?>) types[0];
		} else {
			return this.executableParameter.getParameterType(index);
		}
	}

	public void initParameterNameDiscovery(ParameterNameDiscoverer parameterNameDiscoverer) {
		if (this.executableParameter != null) {
			this.executableParameter.initParameterNameDiscovery(parameterNameDiscoverer);
		}
	}
	
	/**
	 * 返回包含注入点的包装成员
	 */
	public Member getMember() {
		return (this.field != null ? this.field : getExecutableParameter().getExecutable());
	}

	public AnnotatedElement getAnnotatedElement() {
		return (this.field != null ? this.field : getExecutableParameter().getExecutable());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((field == null) ? 0 : field.hashCode());
		result = prime * result + Arrays.hashCode(fieldAnnotations);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		InjectionPoint other = (InjectionPoint) obj;
		if (field == null) {
			if (other.field != null)
				return false;
		} else if (!field.equals(other.field))
			return false;
		if (!Arrays.equals(fieldAnnotations, other.fieldAnnotations))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return (this.field != null ? "field '" + this.field.getName() + "'" : String.valueOf(this.executableParameter));
	}
}
