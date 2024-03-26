package org.zy.fluorite.core.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Priority;

import org.zy.fluorite.core.annotation.ComponentScan;
import org.zy.fluorite.core.annotation.ComponentScans;
import org.zy.fluorite.core.annotation.Order;
import org.zy.fluorite.core.interfaces.AnnotationFilter;
import org.zy.fluorite.core.interfaces.AnnotationMetadata;
import org.zy.fluorite.core.interfaces.MethodFilter;
import org.zy.fluorite.core.interfaces.function.InvorkFunction;
import org.zy.fluorite.core.subject.AnnotationAttributes;
import org.zy.fluorite.core.subject.AnnotationValuesAttributes;
import org.zy.fluorite.core.subject.IgnoreAnnotationMethod;
import org.zy.fluorite.core.subject.IgnoreJavaAnnotation;

/**
 * @author: zy(azurite-Y);
 * @DateTime: 2020年6月5日 上午10:13:48;
 * @Description 注解操作工具类
 */
public final class AnnotationUtils {
	/**
	 * 过滤java自身带有的注解
	 */
	public static AnnotationFilter filter = new IgnoreJavaAnnotation();
	/**
	 * 过滤注解的toString、equals、hashCode、annotationTyped等方法
	 */
	public static MethodFilter methodFilter = new IgnoreAnnotationMethod();

	/**
	 * 判断类、构造器、方法、属性、参数上是否标注有注解
	 * 
	 * @param clz
	 * @return true则标注，false则未标注
	 */
	public static boolean hasAnnotation(AnnotatedElement element) {
		return element.getAnnotations().length != 0 ? true : false;
	}

	/**
	 * 递归指定类及其父类的非Java注解，将找到的非java注解保存到指定容器中
	 * 
	 * @param annos
	 * @param result - 存储非java注解的容器
	 */
	public static void doWithTraceClassAnnotation(Class<?> clz, Set<Annotation> result) {
		do {
			doWithLocalAnnotation(clz.getAnnotations(), result);
//    		Class<?> source = clz;
			clz = clz.getSuperclass();
//    		System.out.println("查找："+source+"--->"+clz);
		} while (clz != Object.class);
	}

	/**
	 * 递归找寻非Java注解，将找到的非java注解保存到指定容器中
	 * 
	 * @param annos
	 * @param result - 存储非java注解的容器
	 */
	public static void doWithLocalAnnotation(Annotation[] annos, Set<Annotation> result) {
		doWithLocalAnnotation(annos, (anno) -> {
			// 能执行到此就代表此注解非java自身定义的注解。
			result.add(anno);
		});
	}

	/**
	 * 递归找寻非Java注解，将找到的非java注解保存到指定容器中
	 * 
	 * @param annos
	 * @param result - 存储非java注解的容器
	 */
	public static void doWithLocalAnnotation(Annotation[] annos, Map<Class<? extends Annotation>, List<Annotation>> result) {
		doWithLocalAnnotation(annos,(anno) -> {
			// 能执行到此就代表此注解非java自身定义的注解。
			Class<? extends Annotation> annotationType = anno.annotationType();
			List<Annotation> orDefault = result.getOrDefault(annotationType, new ArrayList<Annotation>());
			orDefault.add(anno);
		});
	}

	/**
	 * 递归找寻非Java注解。总共递归次数取决于有多少的非java注解，每有一个则需递归一次。<br/>
	 * 单次递归的for循环次数取决于标注与非java注解上的注解个数，最少循环3此。<br/>
	 * 如一个类标注了一个 @Component 注解，则需迭代1+1此，循环次数为4+3<br/>
	 * 
	 * @param annos    - 需要检查的注解数组
	 * @param function - 自定义检查动作的方法引用
	 */
	public static void doWithLocalAnnotation(Annotation[] annos, InvorkFunction<Annotation> function) {
		for (Annotation annotation : annos) {
			if (annotation.annotationType().getName().startsWith("java.lang")) {
//    			System.out.println("忽略："+annotation.annotationType().getName()+"-"+annotation.hashCode());
				continue;
			} else {
//    			System.out.println("处理："+annotation.annotationType().getName()+"-"+annotation.hashCode());
				try {
					function.invork(annotation);
				} catch (Throwable e) {
					e.printStackTrace();
				}
				
				Annotation[] annotations = annotation.annotationType().getAnnotations();
				if (annotations.length != 2 && annotations.length >= 3 ) { // 注解标注默认有@Target、@Retention两个，有些注解还会带有@Documented
					doWithLocalAnnotation(annotations, function);
				}
			}
		}
	}

	/**
	 * 获得类、构造器、方法、属性、参数上标注的有效注解集合，即非java自身所定义的注解
	 * 
	 * @param clz
	 * @return
	 */
	public static Set<Annotation> getAnnotationMetaToSet(AnnotatedElement element) {
		Set<Annotation> set = new LinkedHashSet<>();
		doWithLocalAnnotation(element.getAnnotations(), set);
		return set;
	}

	/**
	 * 获得类、构造器、方法、属性、参数上标注的有效注解集合，即非java自身所定义的注解
	 * @param clz
	 * @return
	 */
	public static Map<Class<? extends Annotation>, List<Annotation>> getAnnotationMetaToMap(AnnotatedElement element) {
		Map<Class<? extends Annotation>, List<Annotation>> result = new LinkedHashMap<>();
		doWithLocalAnnotation(element.getAnnotations(), (anno) -> {
			Class<? extends Annotation> annotationType = anno.annotationType();
			List<Annotation> orDefault = result.getOrDefault(annotationType, new ArrayList<Annotation>());
			orDefault.add(anno);
			result.put(annotationType, orDefault);
		});
		return result;
	}
	
	/**
	 * 获得指定注解的属性映射信息
	 * @param annoSource
	 * @return
	 */
	public static AnnotationValuesAttributes getAnnotationValueAttributes(Annotation annoSource) {
		Method[] declaredMethods = annoSource.getClass().getDeclaredMethods();
		AnnotationValuesAttributes valuesAttributes = new AnnotationValuesAttributes();
		for (Method method : declaredMethods) {
			if (method.getParameterCount() > 0 || "hashCode".equals(method.getName()) || "toString".equals(method.getName())) {
				continue ;
			}
			try {
				method.setAccessible(true);
				valuesAttributes.put(method.getName(), method.invoke(annoSource));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return valuesAttributes;
	}

	/**
	 * 获得类、构造器、方法、属性、参数上标注的有效注解集的注解类型与注解对象映射
	 * @param obj
	 * @return
	 */
	public static AnnotationAttributes getAnnotationAttributes(AnnotatedElement element) {
		Map<Class<? extends Annotation>, List<Annotation>> annotationMeta = getAnnotationMetaToMap(element);
		return new AnnotationAttributes(element, annotationMeta);
	}

	/**
	 * 调用注解的指定名称的方法
	 * @param <T>
	 * @param annotation
	 * @return 
	 */
	public static Object invokeAnnotationMethods(Annotation annotation, String methodName) {
		if (annotation == null || !Assert.hasText(methodName)) {
			return null;
		}
		Method method;
		try {
			method = annotation.annotationType().getDeclaredMethod(methodName);
			method.setAccessible(true);
			return method.invoke(annotation);
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 提取@Order注解的value值，若未找到有效值则返回null
	 * 
	 * @param obj
	 * @return
	 */
	public static Integer findOrderFromAnnotation(Object obj) {
		Order order = obj instanceof AnnotatedElement ? ((AnnotatedElement) obj).getAnnotation(Order.class)
				: obj.getClass().getAnnotation(Order.class);
		return order == null ? null : order.value();
	}

	/**
	 * 获得指定类上标注的@ComponentScan注解
	 * 
	 * @param obj - 此对象不能是包装类，只能是原始类对象或原始Class。 若参数的obj为SourceClass类型则之后获得此类上的注解
	 * @return
	 */
	public static List<ComponentScan> findComponentScan(AnnotationMetadata annotationMetadata) {
		List<ComponentScan> list = new ArrayList<>();
		ComponentScan annotation = annotationMetadata.getAnnotationForClass(ComponentScan.class);
		if (annotation == null) {
			ComponentScans scans = annotationMetadata.getAnnotationForClass(ComponentScans.class);
			if (scans != null) {
				for (ComponentScan componentScan : scans.value()) {
					list.add(componentScan);
				}
			}
		} else {
			list.add(annotation);
		}
		return list;
	}

	/**
	 * 提取@ComponentScan、@ComponentScans注解保存的包扫描路径，若未找到有效值则返回null
	 * 
	 * @return
	 */
	public static List<String> findComponentScanFromAnnotation(AnnotationMetadata metadata) {
		Assert.notNull(metadata, "'metadata'不能为null");
		List<String> list = new ArrayList<>();
		ComponentScan annotation = metadata.getAnnotationForClass(ComponentScan.class);
		if (annotation == null) {
			ComponentScans annotations = metadata.getAnnotationForClass(ComponentScans.class);
			if (annotations != null) {
				for (ComponentScan anno : annotations.value()) {
					for (String string : anno.value()) {
						list.add(string.trim());
					}
				}
			}
		} else {
			for (String an : annotation.value()) {
				list.add(an.trim());
			}
		}
		return list;
	}

	/**
	 * 提取@Priority注解的value值，若未找到有效值则返回null
	 * 
	 * @param obj
	 * @return
	 */
	public static Integer findPriorityFromAnnotation(Object obj) {
		Annotation[] annos = obj instanceof AnnotatedElement ? ((AnnotatedElement) obj).getAnnotations()
				: obj.getClass().getAnnotations();
		int[] arr = { -1 };
		doWithLocalAnnotation(annos, (anno) -> {
			if (anno.annotationType() == Priority.class) {
				arr[0] = ((Priority) anno).value();
			}
		});
		return arr[0];
	}
}