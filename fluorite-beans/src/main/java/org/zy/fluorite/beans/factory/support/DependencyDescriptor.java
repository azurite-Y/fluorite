package org.zy.fluorite.beans.factory.support;

import java.io.Serializable;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;

import org.zy.fluorite.beans.factory.interfaces.BeanFactory;
import org.zy.fluorite.core.subject.ExecutableParameter;

/**
 * @author: zy(azurite-Y);
 * @DateTime: 2020年6月6日 下午4:03:13;
 * @Description
 */
@SuppressWarnings("serial")
public class DependencyDescriptor extends InjectionPoint implements Serializable {
	private final Class<?> sourceClass;

	private final boolean required;

	public DependencyDescriptor(ExecutableParameter executableParameter, Class<?> sourceClass, boolean required) {
		super(executableParameter);
		this.sourceClass = sourceClass;
		this.required = required;
	}
	public DependencyDescriptor(Executable executable) {
		super(new ExecutableParameter(executable));
		this.sourceClass = executable.getDeclaringClass();
		this.required = false;
	}
	public DependencyDescriptor(Executable executable, boolean required) {
		super(new ExecutableParameter(executable));
		this.sourceClass = executable.getDeclaringClass();
		this.required = required;
	}
	public DependencyDescriptor(Field field, boolean required) {
		super(field);
		this.sourceClass = field.getDeclaringClass();
		this.required = required;
	}
	public DependencyDescriptor(ExecutableParameter executableParameter, boolean required) {
		super(executableParameter);
		this.sourceClass = super.executableParameter.getExecutable().getDeclaringClass();
		this.required = required;
		
	}
	public DependencyDescriptor(DependencyDescriptor original) {
		super(original);
		this.sourceClass = original.sourceClass;
		this.required = original.required;
	}

	// setter、getter
	public Class<?> getSourceClass() {
		return sourceClass;
	}
	public boolean isRequired() {
		return required;
	}
	
	/**
	 * 从BeanFactory实现中获得已解析的依赖bean
	 * @param beanFactory
	 * @return
	 */
	public Object resolveShortcut(BeanFactory beanFactory) {
		return null;
	}
}
