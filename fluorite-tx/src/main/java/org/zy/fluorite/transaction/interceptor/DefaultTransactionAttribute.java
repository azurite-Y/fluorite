package org.zy.fluorite.transaction.interceptor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.zy.fluorite.core.utils.Assert;
import org.zy.fluorite.transaction.annotation.Transactional;
import org.zy.fluorite.transaction.interfaces.TransactionAttribute;
import org.zy.fluorite.transaction.support.DefaultTransactionDefinition;

/**
 * @DateTime 2021年9月17日;
 * @author zy(azurite-Y);
 * @Description
 */
public class DefaultTransactionAttribute extends DefaultTransactionDefinition implements TransactionAttribute {
	private static final long serialVersionUID = -5715641436295778969L;

	/** 指定处理事务方法的事务管理器beanName */
	private String qualifier;

	/** 描述符，一般为连接点标识符 */
	private String descriptor;

	/** 不会触发回滚的异常全限定类名集合 */
	private List<Class<?>> noRollbackExceptions = new ArrayList<>();

	/** 会触发回滚的异常全限定类名集合 */
	private List<Class<?>> rollbackExceptions = new ArrayList<>();

	public DefaultTransactionAttribute() {
		super();
	}

	public DefaultTransactionAttribute(Transactional annotation) {
		this.qualifier = annotation.transactionManager();
		this.noRollbackExceptions = Arrays.asList(annotation.noRollbackFor());
		this.rollbackExceptions = Arrays.asList(annotation.rollbackFor());
		super.isolationLevel = annotation.isolation().value();
		super.propagationBehavior = annotation.propagation().value();
		super.timeout = annotation.timeout();
		super.readOnly = annotation.readOnly();
		
		for (String clzName : annotation.rollbackForClassName()) {
			this.addNoRollbackException(clzName);
		}
		
		for (String clzName : annotation.noRollbackForClassName()) {
			this.AddRollbackException(clzName);
		}
	}

	@Override
	public boolean rollbackOn(Throwable ex) {
		for (Class<?> rollbackClz : rollbackExceptions) {
			if (rollbackClz.isAssignableFrom(ex.getClass())) {
				return true;
			}
		}
		for (Class<?> noRollbackClz : noRollbackExceptions) {
			if (noRollbackClz.isAssignableFrom(ex.getClass())) {
				return false;
			}
		}
		return true; // 默认只要触发异常就进行回滚
	}

	@Override
	public String getQualifier() {
		return qualifier;
	}
	public String getDescriptor() {
		return descriptor;
	}
	public void setDescriptor(String descriptor) {
		this.descriptor = descriptor;
	}
	public void setQualifier(String qualifier) {
		this.qualifier = qualifier;
	}
	public boolean addNoRollbackException(String noRollbackException) {
		Class<?> clz = null;
		try {
			clz = Class.forName(noRollbackException);
		} catch (ClassNotFoundException e) {
			logger.error("反射异常，by className：" + noRollbackException + "，标记位置：" + this.descriptor, e);
			e.printStackTrace();
		}
		return noRollbackExceptions.add(clz);
	}
	public boolean AddRollbackException(String rollbackException) {
		Class<?> clz = null;
		try {
			clz = Class.forName(rollbackException);
		} catch (ClassNotFoundException e) {
			logger.error("反射异常，by className：" + rollbackException + "，标记位置：" + this.descriptor, e);
			e.printStackTrace();
		}
		return rollbackExceptions.add(clz);
	}
	public boolean addNoRollbackException(Class<?> noRollbackException) {
		Assert.isTrue(Throwable.class.isAssignableFrom(noRollbackException), "指定的 'noRollbackException' 非法，不是Throwable的子类，by noRollbackException：" + noRollbackException.getName() + "，标记位置：" + this.descriptor);
		return noRollbackExceptions.add(noRollbackException);
	}
	public boolean AddRollbackException(Class<?> rollbackException) {
		Assert.isTrue(Throwable.class.isAssignableFrom(rollbackException), "指定的 'rollbackException' 非法，不是Throwable的子类，by rollbackException：" + rollbackException.getName() + "，标记位置：" + this.descriptor);
		return rollbackExceptions.add(rollbackException);
	}
}
