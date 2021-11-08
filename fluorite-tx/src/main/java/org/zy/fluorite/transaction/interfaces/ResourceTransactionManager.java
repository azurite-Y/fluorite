package org.zy.fluorite.transaction.interfaces;

/**
 * @DateTime 2021年9月14日;
 * @author zy(azurite-Y);
 * @Description
 */
public interface ResourceTransactionManager extends PlatformTransactionManager {
	/**
	 * 返回此事务管理器操作的资源工厂，例如JDBC数据源
	 * @return
	 */
	Object getResourceFactory();
}
