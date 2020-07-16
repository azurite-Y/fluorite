package org.zy.fluorite.beans.support;

import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.util.LinkedHashSet;
import java.util.Set;

import org.zy.fluorite.core.interfaces.ClassMetadata;
import org.zy.fluorite.core.subject.AnnotationAttributes;
import org.zy.fluorite.core.utils.AnnotationUtils;
import org.zy.fluorite.core.utils.StringUtils;

/**
 * @author: zy(azurite-Y);
 * @DateTime: 2020年6月5日 下午4:43:52;
 * @Description
 */
public class StandardClassMetadata implements ClassMetadata {
	private final Class<?> sourceClass;
	
	/** 类对象上标注的有效注解集的属性名与属性值映射 */
	private AnnotationAttributes annotationAttributesList;
	
	public StandardClassMetadata(Class<?> sourceClass) {
		super();
		this.sourceClass = sourceClass;
	}
	public StandardClassMetadata(Class<?> sourceClass, AnnotationAttributes annotationAttributesList) {
		this(sourceClass);
		this.annotationAttributesList = annotationAttributesList;
	}
	
	@Override
	public boolean isInterface() {
		return sourceClass.isInterface();
	}

	@Override
	public boolean isAnnotation() {
		return sourceClass.isAnnotation();
	}

	@Override
	public boolean isAbstract() {
		return Modifier.isAbstract(this.sourceClass.getModifiers());
	}

	@Override
	public boolean isConcrete() {
		return !(isInterface() || isAbstract());
	}

	@Override
	public boolean isFinal() {
		return Modifier.isFinal(this.sourceClass.getModifiers());
	}

	@Override
	public boolean hasSuperClass() {
		return (this.sourceClass.getSuperclass() != null);
	}

	@Override
	public String getSuperClassName() {
		Class<?> superClass = this.sourceClass.getSuperclass();
		return (superClass != null ? superClass.getName() : null);
	}

	@Override
	public String[] getInterfaceNames() {
		Class<?>[] ifcs = this.sourceClass.getInterfaces();
		String[] ifcNames = new String[ifcs.length];
		for (int i = 0; i < ifcs.length; i++) {
			ifcNames[i] = ifcs[i].getName();
		}
		return ifcNames;
	}

	@Override
	public String[] getMemberClassNames() {
		LinkedHashSet<String> memberClassNames = new LinkedHashSet<>(4);
		for (Class<?> nestedClass : this.sourceClass.getDeclaredClasses()) {
			memberClassNames.add(nestedClass.getName());
		}
		return StringUtils.toStringArray(memberClassNames);
	}
	
	@Override
	public AnnotationAttributes getAnnotationAttributes() {
		if (annotationAttributesList ==null) {
			annotationAttributesList = AnnotationUtils.getAnnotationAttributes(this.sourceClass);
		}
		return annotationAttributesList;
	}
	@Override
	public String toString() {
		return "StandardClassMetadata [sourceClass=" + sourceClass + ", annotationAttributesList="
				+ annotationAttributesList + "]";
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((sourceClass == null) ? 0 : sourceClass.hashCode());
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
		StandardClassMetadata other = (StandardClassMetadata) obj;
		if (sourceClass == null) {
			if (other.sourceClass != null)
				return false;
		} else if (!sourceClass.equals(other.sourceClass))
			return false;
		return true;
	}
	@Override
	public Set<Class<? extends Annotation>> getAnnotationTypes() {
		return getAnnotationAttributes().keySet();
	}
	@Override
	public boolean hasAnnotation(Class<? extends Annotation> annoClz) {
		return getAnnotationAttributes().get(annoClz) != null;
	}
	@Override
	public String getName() {
		return sourceClass.getSimpleName();
	}
	@Override
	public Class<?> getType() {
		return sourceClass;
	}
}
