package org.zy.fluorite.beans.support;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Executable;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.LinkedHashMap;
import java.util.Map;

import org.zy.fluorite.core.interfaces.ParameterMetadata;
import org.zy.fluorite.core.subject.AnnotationAttributes;
import org.zy.fluorite.core.utils.AnnotationUtils;

/**
 * @DateTime 2020年6月25日 上午12:37:06;
 * @author zy(azurite-Y);
 * @Description 
 */
public class StandardParameterMetadata implements ParameterMetadata {
	// key存储的是参数的名称或者合成形式为argN的名称
	Map<String , AnnotationAttributes> parames = new LinkedHashMap<>();
	/** 参数来源 */
	private Executable executable;
	
	public StandardParameterMetadata(Executable executable) {
		super();
		this.executable = executable;
		for (Parameter parameter : executable.getParameters()) {
			parames.put(parameter.getName(), AnnotationUtils.getAnnotationAttributes(parameter));
		}
	}

	public StandardParameterMetadata(Map<String, AnnotationAttributes> parames) {
		super();
		this.parames = parames;
	}

	public Class<?> getType(String parameName) {
		return getAnnotationAttributes(parameName).getElement().getClass();
	}

	public AnnotationAttributes getAnnotationAttributes(String parameName) {
		return parames.get(parameName);
	}

	public boolean isFinal(String parameName) {
		return Modifier.isFinal(parames.get(parameName).getModifiers());
	}
	
	public boolean isStatic(String parameName) {
		return Modifier.isStatic(parames.get(parameName).getModifiers());
	}

	public Map<String, AnnotationAttributes> getParames() {
		return parames;
	}

	@Override
	public AnnotatedElement getType() {
		return executable;
	}
}
