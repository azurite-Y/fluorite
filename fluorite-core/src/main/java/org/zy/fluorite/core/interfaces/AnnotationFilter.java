package org.zy.fluorite.core.interfaces;

import java.lang.annotation.Annotation;
import java.util.List;

/**
 * @author: zy(azurite-Y);
 * @DateTime: 2020年6月5日 下午2:10:33;
 * @Description 注解过滤
 */
public interface AnnotationFilter {
	
	/**
	 * 过滤注解
	 * @return 过滤之后的注解集合
	 */
	List<Annotation> matcher(Annotation[] annos);
	List<Annotation> matcher(List<Annotation> annos);
}
