package beans;

import java.lang.annotation.Annotation;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.zy.fluorite.beans.support.AnnotationMetadataHolder;
import org.zy.fluorite.core.annotation.Lazy;
import org.zy.fluorite.core.annotation.Qualifier;
import org.zy.fluorite.core.subject.AnnotationAttributes;
import org.zy.fluorite.core.utils.AnnotationUtils;

/**
 * @DateTime 2020年6月24日 上午9:38:50;
 * @author zy(azurite-Y);
 * @Description
 */
@Qualifier
class AnnotationUtilsTest extends App {

	@Test
	void testDoWithLocalClassAnnotation() {
		Set<Annotation> result = new LinkedHashSet<>();
		AnnotationUtils.doWithTraceClassAnnotation(AnnotationUtilsTest.class, result);
		System.out.println(result);
	}

	@Test
	void testDoWithLocalAnnotation() {
		Set<Annotation> result = new LinkedHashSet<>();
		AnnotationUtils.doWithLocalAnnotation(AnnotationUtilsTest.class.getAnnotations(), result);
		System.out.println(result);
	}
	
	@Test
	void testDoWithLocalAnnotationForMap() {
		AnnotationAttributes annotationAttributes = AnnotationUtils.getAnnotationAttributes(AnnotationUtilsTest.class);
		System.out.println(annotationAttributes);
	}
	
	@Test
	void testAnnotationMetadataHolder() {
		AnnotationMetadataHolder annotationMetadataHolder = new AnnotationMetadataHolder(AnnotationUtilsTest.class);
		System.out.println(annotationMetadataHolder);
		
	}
}
@Lazy
class App {
	
}
