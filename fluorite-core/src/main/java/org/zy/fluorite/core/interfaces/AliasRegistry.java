package org.zy.fluorite.core.interfaces;

/**
 * @author: zy(azurite-Y);
 * @DateTime: 2020年6月8日 下午2:10:21;
 * @Description 定义别名存取方法接口
 */
public interface AliasRegistry {
	/**
	 * 注册指定name的别名
	 * @param name
	 * @param alias
	 */
	void registerAlias(String name, String alias);

	/**
	 * 从别名集合中删除指定别名
	 */
	void removeAlias(String alias);

	/**
	 * 判断指定name是否存在于别名集合中
	 */
	boolean isAlias(String name);

	/**
	 * 如果已定义，则返回给定名称的别名
	 */
	String[] getAliases(String name);

	/**
	 * 别名解析
	 * @param name
	 * @return
	 */
//	String canonicalName(String name);
}
