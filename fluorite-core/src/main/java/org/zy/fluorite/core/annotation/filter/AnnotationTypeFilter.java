package org.zy.fluorite.core.annotation.filter;

import java.lang.annotation.Annotation;
import java.util.LinkedHashSet;
import java.util.Set;

import org.zy.fluorite.core.annotation.Component;
import org.zy.fluorite.core.annotation.ComponentScan.Filter;
import org.zy.fluorite.core.interfaces.AnnotationMetadata;
import org.zy.fluorite.core.interfaces.TypeFilter;

/**
 * @DateTime 2020年6月22日 上午9:45:25;
 * @author zy(azurite-Y);
 * @Description	按注解类型匹配，排除未标注指定注解的包扫描结果（默认为@Component注解）
 */
public class AnnotationTypeFilter implements TypeFilter  {
	private Set<Class<? extends Annotation>> annotationTypes;
	
	public AnnotationTypeFilter() {
		super();
		this.annotationTypes = new LinkedHashSet<>();
		this.annotationTypes.add(Component.class);
	}

	public AnnotationTypeFilter(Set<Class<? extends Annotation>> annotationTypes) {
		super();
		if (annotationTypes.isEmpty()) {
			this.annotationTypes = new LinkedHashSet<>();
			this.annotationTypes.add(Component.class);
		}
		this.annotationTypes = annotationTypes;
	}

	/**
	 * 按注解类型匹配，"得其一者得天下"
	 */
	@Override
	public boolean match(AnnotationMetadata metadata , Filter filter) {
		for (Class<? extends Annotation> annoClz : annotationTypes) {
			if (metadata.getAnnotationForClass(annoClz) != null) {
				return false;
			}
		}
		return true;
	}

	@Override
	public void invorkAware(String pattern, Class<? extends Annotation>[] annos, Class<?> source) {
		this.annotationTypes.clear();
		if (annos.length  == 0) {
			this.annotationTypes.add(Component.class);
		} else {
			for (Class<? extends Annotation> anClz : annos) {
				this.annotationTypes.add(anClz);
			}
		} 
	}
	
}
