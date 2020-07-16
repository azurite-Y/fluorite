package org.zy.fluorite.aop.interfaces;

/**
 * @DateTime 2020年7月6日 下午7:01:27;
 * @author zy(azurite-Y);
 * @Description 扩展自MethodInvocation，以允许访问进行方法调用的代理
 */
public interface ProxyMethodInvocation extends MethodInvocation {
	/** 返回此方法调用所通过的代理 */
	Object getProxy();

	/** 创建此对象的克隆 */
	MethodInvocation invocableClone();

	/** 
	 * 创建此对象的克隆
	 * @param arguments - 克隆调用应该使用的参数，重写原始参数 
	 */
	MethodInvocation invocableClone(Object... arguments);

	/** 
	 * 设置要在此链中的所有advice的后续调用使用的参数
	 * @param
	 */
	void setArguments(Object... arguments);

	/**
	 * 将具有给定值的指定用户属性添加到此调用
	 * <p>这些属性在AOP框架本身中没有使用。它们只是作为调用对象的一部分保存，以便在特殊的拦截器中使用</>
	 * @param key
	 * @param value
	 */
	void setUserAttribute(String key, Object value);

	/**
	 * 返回指定用户属性的值
	 * @param key
	 */
	Object getUserAttribute(String key);

}
