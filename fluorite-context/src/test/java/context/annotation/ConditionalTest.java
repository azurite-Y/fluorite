package context.annotation;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map.Entry;

import org.junit.jupiter.api.Test;
import org.zy.fluorite.context.annotation.conditional.Conditional;
import org.zy.fluorite.context.annotation.conditional.ConditionalOnBean;
import org.zy.fluorite.context.annotation.conditional.ConditionalOnClass;
import org.zy.fluorite.context.annotation.conditional.OnClassCondition;
import org.zy.fluorite.context.annotation.interfaces.Condition;
import org.zy.fluorite.core.subject.AnnotationAttributes;
import org.zy.fluorite.core.utils.AnnotationUtils;

/**
 * @DateTime 2020年7月3日 下午2:12:31;
 * @author zy(azurite-Y);
 * @Description
 */
@ConditionalOnBean(type = "context.annotation.ConditionalTest")
@ConditionalOnClass()
class ConditionalTest {

	/**
	 * 注解获取和给定类型注解的删除
	 */
	@Test
	void getAnnotationsTest() {
		AnnotationAttributes annotationAttributes = AnnotationUtils.getAnnotationAttributes(ConditionalTest.class);
		List<Conditional> annotations = annotationAttributes.getAnnotationList(Conditional.class);
		System.out.println(annotations);
		Conditional conditionalRemove = null;
		boolean isRemove = false;
		for (int j = 0; j < annotations.size(); j++) {
			Conditional conditional = annotations.get(j);
			for (Class<? extends Condition> clz : conditional.value()) {
				if (clz.equals(OnClassCondition.class)) {
					isRemove = true;
					conditionalRemove = conditional;
					break;
				}
			}
		}
		
		if (isRemove) {
			boolean remove = annotations.remove(conditionalRemove);
			System.out.println(remove? annotations : "删除失败.");
		}
		
		System.out.println("-------------------------------------------");
		for (Entry<Class<? extends Annotation>, List<Annotation>> entry : annotationAttributes.entrySet()) {
			System.out.println(entry.getKey().getSimpleName() +"--"+entry.getValue());
		}
	}

}
