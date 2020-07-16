package org.zy.fluorite.core.subject;

import java.lang.annotation.Annotation;
import java.util.LinkedHashMap;

/**
 * @DateTime 2020年7月9日 下午5:35:52;
 * @author zy(azurite-Y);
 * @Description 存储注解的属性名与属性映射
 */
@SuppressWarnings("serial")
public class AnnotationValuesAttributes extends LinkedHashMap<String, Object> {
	private String displayName;
	
	private Annotation annotation;

	public AnnotationValuesAttributes() {
		super();
	}
	public AnnotationValuesAttributes(Annotation annotation) {
		super();
		this.annotation = annotation;
		this.displayName = annotation.getClass().getSimpleName();
	}
	
	/**
	 * 获得指定类型的映射值，若类型不符则抛出 {@linkplain ClassCastException }
	 * @param <T>
	 * @param key
	 * @param clz
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T>T get(String key , Class<T> clz) {
		return (T)this.get(key);
	}
	
	public String getDisplayName() {
		return displayName;
	}
	public Annotation getAnnotation() {
		return annotation;
	}
	public void setAnnotation(Annotation annotation) {
		this.annotation = annotation;
	}
}
