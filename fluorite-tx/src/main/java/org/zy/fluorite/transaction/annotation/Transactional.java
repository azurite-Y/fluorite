package org.zy.fluorite.transaction.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.zy.fluorite.transaction.dataSource.DataSourceTransactionManager;
import org.zy.fluorite.transaction.interfaces.TransactionDefinition;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
/**
 * @DateTime 2021年9月16日;
 * @author zy(azurite-Y);
 * @Description 事务标记注解
 * <p>@Transactional如果标注在类上那么其设置的事务属性都将被其类中的方法所“继承”，方法可以再次标注@Transactional注释自定义事务属性，对标注于类上的事务属性进行“覆写”</p>
 * <p>@Transactional如果仅标注在方法上那么其就直接作为此方法最终对应的事务属性</p>
 */
public @interface Transactional {
	/**
	 * 指定使用的事务管理器的Bean名称或其别名，默认仅有 'dataSourceTransactionManager'
	 * @see DataSourceTransactionManager
	 */
	String transactionManager() default "";

	/**
	 * 事务传播类型。默认为Propagation.REQUIRED
	 */
	Propagation propagation() default Propagation.REQUIRED;

	/**
	 * 事务隔离级别。默认设置为Isolation.DEFAULT。
	 */
	Isolation isolation() default Isolation.DEFAULT;

	/**
	 * 事务的超时（秒）。默认为基础事务系统的默认超时
	 */
	int timeout() default TransactionDefinition.TIMEOUT_DEFAULT;

	/**
	 * 设置事务的只读模式
	 */
	boolean readOnly() default false;

	/**
	 * 指示哪些异常类型必须导致事务回滚。默认情况下，事务将在运行时异常和错误时回滚，但不会在已检查异常（业务异常）时回滚
	 */
	Class<? extends Throwable>[] rollbackFor() default {};

	/**
	 * 指示哪些异常类型必须导致事务回滚。默认情况下，事务将在运行时异常和错误时回滚，但不会在已检查异常（业务异常）时回滚
	 * @return 异常类的全限定类名数组
	 */
	String[] rollbackForClassName() default {};

	/**
	 * 指示哪些异常类型不能导致事务回滚
	 */
	Class<? extends Throwable>[] noRollbackFor() default {};

	/**
	 * 指示哪些异常类型不能导致事务回滚
	 * @return 异常类的全限定类名数组
	 */
	String[] noRollbackForClassName() default {};
}
