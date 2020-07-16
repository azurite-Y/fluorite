/**
 * 
 */
package beans.support;

import java.beans.ConstructorProperties;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.junit.jupiter.api.Test;
import org.zy.fluorite.beans.support.AnnotationMetadataHolder;
import org.zy.fluorite.core.annotation.Bean;
import org.zy.fluorite.core.annotation.Configuration;
import org.zy.fluorite.core.annotation.Qualifier;
import org.zy.fluorite.core.annotation.Service;
import org.zy.fluorite.core.interfaces.AnnotationMetadata;
import org.zy.fluorite.core.subject.AnnotationAttributes;

/**
 * @DateTime 2020年6月25日 下午1:14:15;
 * @author zy(azurite-Y);
 * @Description
 */
class AnnotationMetadataHolderTest {
	/**
	 * {@link beans.support.AnnotationMetadataHolder }的测试方法。
	 */
	@Test
	void testAnnotationMetadataHolder() {
		AnnotationMetadata holder = new AnnotationMetadataHolder(App.class);
		AnnotationAttributes annotationAttributesForClass = holder.getAnnotationAttributesForClass();
		System.out.println(annotationAttributesForClass); // 调用AnnotationAttributes的toString方法
		
		System.out.println("----------------------Constructor----------------------------");
		Class<?> sourceClass = holder.getSourceClass();
		Constructor<?>[] declaredConstructors = sourceClass.getDeclaredConstructors();
		for (Constructor<?> constructor : declaredConstructors) {
			AnnotationAttributes annotationAttributesForConstructor = holder.getAnnotationAttributesForConstructor(constructor);
			System.out.println(annotationAttributesForConstructor);
		}
		
		System.out.println("----------------------Method----------------------------");
		Method[] declaredMethods = sourceClass.getDeclaredMethods();
		for (Method method : declaredMethods) {
			AnnotationAttributes annotationAttributesForMethod = holder.getAnnotationAttributesForMethod(method);
			System.out.println(annotationAttributesForMethod);
		}
		
		System.out.println("----------------------Field----------------------------");
		Field[] declaredFields = sourceClass.getDeclaredFields();
		for (Field field : declaredFields) {
			AnnotationAttributes annotationAttributesForMethod = holder.getAnnotationAttributesForField(field);
			System.out.println(annotationAttributesForMethod);
		}
		
		System.out.println("----------------------innerClass----------------------------");
		Class<?>[] declaredClasses = sourceClass.getDeclaredClasses();
		for (Class<?> class1 : declaredClasses) {
			AnnotationAttributes annotationAttributesForMethod = holder.getAnnotationAttributesForInnerClass(class1);
			System.out.println(annotationAttributesForMethod);
		}
	}
}

@Service("app-test")
class App {
	@Qualifier("test-age")
	private Integer age;
	@Qualifier("test-age")
	private Integer clzNumber;
	
	public App() {
		super();
	}
	@ConstructorProperties(value = { "age","clzNumber" })
	public App(Integer age, Integer clzNumber) {
		super();
		this.age = age;
		this.clzNumber = clzNumber;
	}
	
	@Bean
	public App getTest() {
		return this;
	}
	
	@PostConstruct
	public void init() {
		System.out.println("--init--");
	}
	
	@PreDestroy
	public void destroy() {
		System.out.println("--destroy--");
	}
	
	class innerClz1{}
	
	@Configuration
	class innerClz2{}
}