package org.zy.fluorite.beans.support;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.zy.fluorite.core.interfaces.AccessibleObjectMetadate;
import org.zy.fluorite.core.subject.AnnotationAttributes;
import org.zy.fluorite.core.utils.AnnotationUtils;

/**
 * @author: zy(azurite-Y);
 * @DateTime: 2020年6月5日 下午4:44:33;
 * @Description
 */
public class StandardFieldMetadata implements AccessibleObjectMetadate {

	private final Field sourceField;

	/** Field对象上标注的有效注解集的属性名与属性值映射 */
	private AnnotationAttributes annotationAttributesList;

	public StandardFieldMetadata(Field sourceField) {
		super();
		this.sourceField = sourceField;
	}

	public StandardFieldMetadata(Field sourceField, AnnotationAttributes annotationAttributesList) {
		this(sourceField);
		this.annotationAttributesList = annotationAttributesList;
	}

	@Override
	public String getName() {
		return sourceField.getName();
	}

	@Override
	public Class<?> getType() {
		return sourceField.getType();
	}

	@Override
	public boolean isOverridable() {
		return (!isStatic() && !isFinal() && !Modifier.isPrivate(this.sourceField.getModifiers()));
	}

	@Override
	public AnnotationAttributes getAnnotationAttributes() {
		if (annotationAttributesList == null) {
			annotationAttributesList = AnnotationUtils.getAnnotationAttributes(this.sourceField);
		}
		return annotationAttributesList;
	}

	@Override
	public boolean isAbstract() {
		return Modifier.isAbstract(this.sourceField.getModifiers());
	}

	@Override
	public boolean isStatic() {
		return Modifier.isStatic(this.sourceField.getModifiers());
	}

	@Override
	public boolean isFinal() {
		return Modifier.isFinal(this.sourceField.getModifiers());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((sourceField == null) ? 0 : sourceField.hashCode());
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
		StandardFieldMetadata other = (StandardFieldMetadata) obj;
		if (sourceField == null) {
			if (other.sourceField != null)
				return false;
		} else if (!sourceField.equals(other.sourceField))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "StandardFieldMetadata [sourceField=" + sourceField + ", annotationAttributesList="
				+ annotationAttributesList + "]";
	}

}
