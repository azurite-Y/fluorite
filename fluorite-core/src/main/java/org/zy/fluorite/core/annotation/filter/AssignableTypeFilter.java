package org.zy.fluorite.core.annotation.filter;

import java.lang.annotation.Annotation;

import org.zy.fluorite.core.annotation.ComponentScan.Filter;
import org.zy.fluorite.core.interfaces.AnnotationMetadata;
import org.zy.fluorite.core.interfaces.TypeFilter;

/**
 * @DateTime 2020年6月22日 上午9:39:15;
 * @author zy(azurite-Y);
 * @Description 按类型匹配
 */
public class AssignableTypeFilter implements TypeFilter {

	private Class<?> targetType;
	
	public AssignableTypeFilter(Class<?> targetType) {
		super();
		this.targetType = targetType;
	}



	/**
	 * 匹配指定类型的子类或其本身
	 */
	@Override
	public boolean match(AnnotationMetadata metadata, Filter filter) {
		if (this.targetType == null) {
			return false;
		}
		return this.targetType.isAssignableFrom(metadata.getSourceClass());
	}

	@Override
	public void invorkAware(String pattern, Class<? extends Annotation>[] annos, Class<?> source) {
		this.targetType = source;
	}
	
}
