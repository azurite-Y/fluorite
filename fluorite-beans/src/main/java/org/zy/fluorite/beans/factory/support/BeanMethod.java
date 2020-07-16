package org.zy.fluorite.beans.factory.support;

import java.lang.reflect.Method;

import org.zy.fluorite.core.subject.AnnotationAttributes;

/**
 * @DateTime 2020年7月1日 上午1:07:16;
 * @author zy(azurite-Y);
 * @Description
 */
public class BeanMethod {
	private final Method method;
	private AnnotationAttributes annotationAttributesForMethod;

	public BeanMethod(Method method) {
		this.method = method;
		this.annotationAttributesForMethod = new AnnotationAttributes(method);
	}

	public BeanMethod(Method method, AnnotationAttributes annotationAttributes) {
		this.method = method;
		this.annotationAttributesForMethod = annotationAttributes;
	}

	public AnnotationAttributes getAnnotationAttributesForMethod() {
		return annotationAttributesForMethod;
	}

	public void setAnnotationAttributesForMethod(AnnotationAttributes annotationAttributesForMethod) {
		this.annotationAttributesForMethod = annotationAttributesForMethod;
	}

	public Method getMethod() {
		return method;
	}

	@Override
	public String toString() {
		return "BeanMethod [method=" + method + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((method == null) ? 0 : method.hashCode());
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
		BeanMethod other = (BeanMethod) obj;
		if (method == null) {
			if (other.method != null)
				return false;
		} else if (!method.equals(other.method))
			return false;
		return true;
	}
}
