package org.zy.fluorite.aop.target;

import java.io.Serializable;

import org.zy.fluorite.aop.interfaces.TargetSource;
import org.zy.fluorite.core.utils.Assert;

/**
 * @DateTime 2020年7月4日 下午4:30:33;
 * @author zy(azurite-Y);
 * @Description 保存给定对象的TargetSource接口的实现。这是TargetSource接口的默认实现
 */
@SuppressWarnings("serial")
public class SingletonTargetSource implements TargetSource, Serializable {

	/** 使用反射缓存和调用目标，一般为切面适配的Bean对象 */
	private final Object target;

	public SingletonTargetSource(Object target) {
		Assert.notNull(target, "target不能为null");
		this.target = target;
	}

	@Override
	public Class<?> getTargetClass() {
		return this.target.getClass();
	}

	@Override
	public Object getTarget() {
		return this.target;
	}

	@Override
	public void releaseTarget(Object target) {}

	@Override
	public boolean isStatic() {
		return true;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof SingletonTargetSource)) {
			return false;
		}
		SingletonTargetSource otherTargetSource = (SingletonTargetSource) other;
		return this.target.equals(otherTargetSource.target);
	}

	@Override
	public int hashCode() {
		return this.target.hashCode();
	}

	@Override
	public String toString() {
		return "SingletonTargetSource [ target=" + target + " ]";
	}

}
