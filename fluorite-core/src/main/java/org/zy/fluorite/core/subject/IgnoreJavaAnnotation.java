package org.zy.fluorite.core.subject;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import org.zy.fluorite.core.interfaces.AnnotationFilter;

/**
 * @author: zy(azurite-Y);
 * @DateTime: 2020年6月5日 下午2:23:14;
 * @Description 过滤java自身带有的注解
 */
public class IgnoreJavaAnnotation implements AnnotationFilter {
	
	public final String JVM_ANNOTATION_PROFIX = "java.lang.annotation.";
	
	@Override
	public List<Annotation> matcher(Annotation[] annos) {
		List<Annotation> list = new ArrayList<>();
		for (Annotation annotation : annos) {
			System.out.println(annotation);
			if (! isIgnore(annotation) ) {
				list.add(annotation);
			}
		}
		return list;
	}
	
	@Override
	public List<Annotation> matcher(List<Annotation> annos) {
		List<Annotation> list = new ArrayList<>();
		for (Annotation annotation : annos) {
			if (! isIgnore(annotation) ) {
				list.add(annotation);
			}
		}
		return list;
	}
	
	private boolean isIgnore(Annotation anno)  {
		return anno.annotationType().getName().startsWith(JVM_ANNOTATION_PROFIX);
		
	}
	
}
