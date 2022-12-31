package org.zy.fluorite.core.subject;

import java.lang.annotation.Annotation;
import java.lang.reflect.Executable;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.zy.fluorite.core.interfaces.ParameterNameDiscoverer;
import org.zy.fluorite.core.utils.Assert;

/**
 * @author: zy(azurite-Y);
 * @DateTime: 2020年6月6日 下午4:25:57;
 * @Description 方法或构造函数包装类，存储对象参数的索引映射和参数泛型映射
 */
public class ExecutableParameter {
	/** 源对象，可能是构造器函数也可能是方法对象 */
	private final Executable executable;

	/** 参数对象 */
	private volatile List<Parameter> parameter = new ArrayList<>();

	/** 参数类型 */
	private volatile List<Class<?>> parameterTypes = new ArrayList<>();

	/** 参数泛型类型 */
	private volatile List<Type[]> genericParameterTypes = new ArrayList<>();

	/** 参数的注解 */
	private volatile List<Annotation[]> parameterAnnotations = new ArrayList<>();

	private ParameterNameDiscoverer parameterNameDiscoverer;

	private boolean isAnalysis;

	public ExecutableParameter(Executable executable) {
		super();
		this.executable = executable;
		analysis(executable);
		isAnalysis = true;
	}
	public ExecutableParameter(ExecutableParameter executableParameter) {
		super();
		this.executable = executableParameter.executable;
		if (!executableParameter.isAnalysis) {
			analysis(executable);
		} else {
			this.parameter = executableParameter.parameter;
			this.parameterTypes = executableParameter.parameterTypes;
			this.parameterAnnotations = executableParameter.parameterAnnotations;
			this.genericParameterTypes = executableParameter.genericParameterTypes;
		}
	}

	/**
	 * 创建指定参数索引的ExecutableParameter对象，只存储索引处的参数信息。索引从0开始
	 * <p>如：new ExecutableParameter(method, 2);</br>
	 * 那么将存储第三个参数的信息，则getParameterType()方法将返回此参数的类型
	 * </p>
	 * @param method
	 * @param i
	 */
	public ExecutableParameter(Executable executable, int i) {
		this.executable = executable;
		Parameter[] parameters = executable.getParameters();
		Assert.isTrue( parameters.length-1 >=  i && i >= 0  ,  () -> "参数下标异常，期望的参数下标‘("+i+")’，实际的参数最大下标'("+ (parameters.length -1) +")' by："+executable);
		add(parameters[i]);
	}

	private void analysis(Executable executable) {
		Parameter[] parameters = executable.getParameters();
		for (Parameter parameter : parameters) {
			add(parameter);
		}
		
		Type[] genericParameterTypes = executable.getGenericParameterTypes();
		for (Type genericParameterType : genericParameterTypes) {
			if ( genericParameterType instanceof ParameterizedType ) {
				Type[] actualTypeArguments = ((ParameterizedType) genericParameterType).getActualTypeArguments();
				this.genericParameterTypes.add(actualTypeArguments);
			}
		}
	}

	private void add(Parameter parameter) {
		this.parameter.add(parameter);
		this.parameterAnnotations.add(parameter.getAnnotations());
		this.parameterTypes.add(parameter.getType());
//		this.genericParameterTypes.add(parameter.getClass().getGenericSuperclass());
	}

	// get
	public Executable getExecutable() {
		return executable;
	}
	public List<Parameter> getParameter() {
		return parameter;
	}
	public List<Class<?>> getParameterTypes() {
		return parameterTypes;
	}
	public List<Type[]> getGenericParameterTypes() {
		return genericParameterTypes;
	}
	public List<Annotation[]> getParameterAnnotations() {
		return parameterAnnotations;
	}
	public ParameterNameDiscoverer getParameterNameDiscoverer() {
		return parameterNameDiscoverer;
	}
	public void initParameterNameDiscovery(ParameterNameDiscoverer parameterNameDiscoverer) {
		this.parameterNameDiscoverer = parameterNameDiscoverer;
	}
	/**
	 * 返回首个参数的类型
	 * @return
	 */
	public Class<?> getParameterType(int index) {
		return this.parameterTypes.get(index);
	}
}
