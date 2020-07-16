package org.zy.fluorite.beans.support;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import org.zy.fluorite.core.interfaces.ExecutableMetadata;
import org.zy.fluorite.core.subject.AnnotationAttributes;
import org.zy.fluorite.core.utils.AnnotationUtils;

/**
 * @author: zy(azurite-Y);
 * @DateTime: 2020年6月5日 下午4:44:33;
 * @Description 封装构造器相关信息
 */
public class StandardConstructorMetadata implements ExecutableMetadata {

	private final Constructor<?> sourceConstructor;

	/** 构造器对象上标注的有效注解集的注解名与注解对象映射 */
	private AnnotationAttributes annotationAttributesList;
	
	private StandardParameterMetadata standardParameterMetadata;
	
	public StandardConstructorMetadata(Constructor<?> sourceConstructor) {
		super();
		this.sourceConstructor = sourceConstructor;
	}

	public StandardConstructorMetadata(Constructor<?> sourceConstructor, AnnotationAttributes annotationAttributesList) {
		this(sourceConstructor);
		this.annotationAttributesList = annotationAttributesList;
	}

	@Override
	public String getName() {
		return sourceConstructor.getName();
	}


	@Override
	public Class<?> getType() {
		return this.sourceConstructor.getDeclaringClass();
	}

	@Override
	public boolean isOverridable() {
		return (!isStatic() && !isFinal() && !Modifier.isPrivate(this.sourceConstructor.getModifiers()));
	}

	@Override
	public AnnotationAttributes getAnnotationAttributes() {
		if (annotationAttributesList == null) {
			annotationAttributesList = AnnotationUtils.getAnnotationAttributes(this.sourceConstructor);	
		}
		return annotationAttributesList;
	}

	@Override
	public boolean isAbstract() {
		return Modifier.isAbstract(this.sourceConstructor.getModifiers());
	}

	@Override
	public boolean isStatic() {
		return Modifier.isStatic(this.sourceConstructor.getModifiers());
	}

	@Override
	public boolean isFinal() {
		return Modifier.isFinal(this.sourceConstructor.getModifiers());
	}
	
	@Override
	public AnnotationAttributes getAnnotationAttributes(String parameName) {
		return this.standardParameterMetadata.getAnnotationAttributes(parameName);
	}

	@Override
	public boolean isFinal(String parameName) {
		return this.standardParameterMetadata.isFinal(parameName);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((sourceConstructor == null) ? 0 : sourceConstructor.hashCode());
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
		StandardConstructorMetadata other = (StandardConstructorMetadata) obj;
		if (sourceConstructor == null) {
			if (other.sourceConstructor != null)
				return false;
		} else if (!sourceConstructor.equals(other.sourceConstructor))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "StandardConstructorMetadata [sourceConstructor=" + sourceConstructor + ", annotationAttributesList="
				+ annotationAttributesList + ", standardParameterMetadata=" + standardParameterMetadata + "]";
	}
}
