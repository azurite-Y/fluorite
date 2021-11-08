package org.zy.fluorite.transaction.dao;

import org.zy.fluorite.beans.factory.exception.BeanInitializationException;
import org.zy.fluorite.core.interfaces.instantiation.InitializingBean;

/**
 * @DateTime 2021年9月6日;
 * @author zy(azurite-Y);
 * @Description DAO的通用基类，定义DAO初始化的模板方法
 */
public abstract class DaoSupport  implements InitializingBean{
	@Override
	public final void afterPropertiesSet() throws IllegalArgumentException, BeanInitializationException {
		// 检出Dao配置
		checkDaoConfig();

		try {
			initDao();
		} catch (Exception ex) {
			throw new BeanInitializationException("初始化DAO失败", ex);
		}
	}

	/**
	 *  抽象子类必须重写此项以检查其配置。
	 * <p>如果具体的子类不应该重写这个模板方法本身，那么实现者应该被标记为final
	 */
	protected abstract void checkDaoConfig() throws IllegalArgumentException;

	/**
	 *  具体的子类可以覆盖自定义初始化行为。在填充此实例的bean属性后调用
	 */
	protected void initDao() throws Exception {
	}
}
