package org.zy.fluorite.core.subject;

import java.lang.reflect.Executable;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;

import org.zy.fluorite.core.utils.Assert;

/**
 * @author: zy(azurite-Y);
 * @DateTime: 2020年6月10日 下午5:03:32;
 * @Description 封装方法参数规范的帮助程序类，即方法或构造函数加上参数索引和已声明泛型的嵌套类型索引。作为要传递的规范对象很有用。
 */
@Deprecated
public class MethodParameter {
	/** 存储构造器对象或者Method对象  */
	private final Executable executable;

	/** 根据参数索引映射其参数对象 */
	private Map<Integer,Parameter> parameterMap = new HashMap<>();
	
	/** 根据参数索引映射其参数对象的类型 */
	private Map<Integer,Class<?>> parameterTypeMap = new HashMap<>();

	public MethodParameter(Executable executable, Map<Integer, Parameter> parameterMap) {
		super();
		this.executable = executable;
		this.parameterMap = parameterMap;
		for (Integer index : this.parameterMap.keySet()) {
			this.parameterTypeMap.put(index, this.parameterMap.get(index).getType());
		}
	}

	/**
	 * 根据指定的Executable对象解析器参数集合，创建MethodParameter对象
	 * @param executable
	 */
	public MethodParameter(Executable executable) {
		this(executable, executable.getParameters());
	}
	
	public MethodParameter(Executable executable, Parameter[] parameters) {
		this.executable = executable;
		for (int i = 0; i < parameters.length; i++) {
			this.parameterMap.put(i, parameters[i]);
			this.parameterTypeMap.put(i, parameters[i].getType());
		}
	}

	/**
	 * 创建只有一个参数的MethodParameter对象
	 * @param executable
	 * @param i
	 */
	public MethodParameter(final Executable executable, int i) {
		this.executable = executable;
		Parameter[] parameters = executable.getParameters();
		Assert.isTrue(parameters.length > i || parameters.length == 0 ,() -> "参数下标异常，此Executable对象有多个参数或无参，by："+executable);
		this.parameterMap.put(i, parameters[i]);
		this.parameterTypeMap.put(i, parameters[i].getType());
	}
	/**
	 * 浅复制一个MethodParameter对象
	 * @param methodParameter
	 */
	public MethodParameter(MethodParameter methodParameter) {
		this.executable = methodParameter.executable;
		this.parameterMap.putAll(methodParameter.parameterMap);
	}

	public Map<Integer, Parameter> getParameterMap() {
		return parameterMap;
	}
	public void setParameterMap(Map<Integer, Parameter> parameterMap) {
		this.parameterMap = parameterMap;
	}
	public Executable getExecutable() {
		return executable;
	}

	/**
	 * 返回首个参数的类型
	 * @return
	 */
	public Class<?> getParameterType() {
		return this.parameterTypeMap.get(1);
	}
}
