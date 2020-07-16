package org.zy.fluorite.beans.support;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.zy.fluorite.core.interfaces.ExecutableMetadata;
import org.zy.fluorite.core.subject.AnnotationAttributes;
import org.zy.fluorite.core.utils.AnnotationUtils;

/**
 * @author: zy(azurite-Y);
 * @DateTime: 2020年6月5日 下午1:59:56;
 * @Description
 */
public class StandardMethodMetadata implements ExecutableMetadata {

	private final Method sourceMethod;

	/** Method对象上标注的有效注解集的属性名与属性值映射 */
	private AnnotationAttributes annotationAttributesList;

	private StandardParameterMetadata standardParameterMetadata;
	
	public StandardMethodMetadata(Method sourceMethod) {
		super();
		this.sourceMethod = sourceMethod;
	}

	public StandardMethodMetadata(Method sourceMethod, AnnotationAttributes annotationAttributesList) {
		this(sourceMethod);
		this.annotationAttributesList = annotationAttributesList;
	}

	public Method getSourceMethod() {
		return sourceMethod;
	}

	@Override
	public String getName() {
		return this.sourceMethod.getName();
	}

	@Override
	public Class<?> getType() {
		return this.sourceMethod.getReturnType();
	}

	@Override
	public boolean isAbstract() {
		return Modifier.isAbstract(this.sourceMethod.getModifiers());
	}

	@Override
	public boolean isStatic() {
		return Modifier.isStatic(this.sourceMethod.getModifiers());
	}

	@Override
	public boolean isFinal() {
		return Modifier.isFinal(this.sourceMethod.getModifiers());
	}

	@Override
	public boolean isOverridable() {
		return (!isStatic() && !isFinal() && !Modifier.isPrivate(this.sourceMethod.getModifiers()));
	}

	@Override
	public AnnotationAttributes getAnnotationAttributes() {
		if (annotationAttributesList==null) {
			annotationAttributesList = AnnotationUtils.getAnnotationAttributes(sourceMethod);
		}
		return annotationAttributesList;
	}

	@Override
	public AnnotationAttributes getAnnotationAttributes(String parameName) {
		return this.standardParameterMetadata.getAnnotationAttributes(parameName);
	}

	public boolean isFinal(String parameName) {
		return this.standardParameterMetadata.isFinal(parameName);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((sourceMethod == null) ? 0 : sourceMethod.hashCode());
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
		StandardMethodMetadata other = (StandardMethodMetadata) obj;
		if (sourceMethod == null) {
			if (other.sourceMethod != null)
				return false;
		} else if (!sourceMethod.equals(other.sourceMethod))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "StandardMethodMetadata [sourceMethod=" + sourceMethod + ", annotationAttributesList="
				+ annotationAttributesList + ", standardParameterMetadata=" + standardParameterMetadata + "]";
	}
}