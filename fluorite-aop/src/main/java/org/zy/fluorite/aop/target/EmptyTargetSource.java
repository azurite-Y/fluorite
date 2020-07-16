package org.zy.fluorite.aop.target;

import java.io.Serializable;

import org.zy.fluorite.aop.interfaces.TargetSource;

/**
 * @DateTime 2020年7月4日 下午2:16:10;
 * @author zy(azurite-Y);
 * @Description 没有目标时的标准目标源
 */
@SuppressWarnings("serial")
public class EmptyTargetSource implements TargetSource , Serializable {
	public static final EmptyTargetSource INSTANCE = new EmptyTargetSource(null, true);

	private final Class<?> targetClass;

	private final boolean isStatic;
	
	public static EmptyTargetSource forClass(Class<?> targetClass) {
		return forClass(targetClass, true);
	}
	/** 根据参数创建一个EmptyTargetSource并返回 */
	public static EmptyTargetSource forClass(Class<?> targetClass, boolean isStatic) {
		return (targetClass == null && isStatic ? INSTANCE : new EmptyTargetSource(targetClass, isStatic));
	}

	/** 创建EmptyTargetSource类的新实例 */
	private EmptyTargetSource(Class<?> targetClass, boolean isStatic) {
		this.targetClass = targetClass;
		this.isStatic = isStatic;
	}


	/** 总是返回指定的target class 或null */
	@Override
	public Class<?> getTargetClass() {
		return this.targetClass;
	}

	@Override
	public boolean isStatic() {
		return this.isStatic;
	}

	@Override
	public Object getTarget() {
		return null;
	}

	@Override
	public void releaseTarget(Object target) {
	}


	/**
	 * 如果没有目标类，则返回反序列化时的规范实例
	 */
	private Object readResolve() {
		return (this.targetClass == null && this.isStatic ? INSTANCE : this);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (isStatic ? 1231 : 1237);
		result = prime * result + ((targetClass == null) ? 0 : targetClass.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		EmptyTargetSource other = (EmptyTargetSource) obj;
		if (isStatic != other.isStatic)
			return false;
		if (targetClass == null) {
			if (other.targetClass != null)
				return false;
		} else if (!targetClass.equals(other.targetClass))
			return false;
		return true;
	}

}
