package org.zy.fluorite.beans.factory.interfaces;

import org.zy.fluorite.core.interfaces.function.ObjectFactory;

/**
 * @author: zy(azurite-Y);
 * @DateTime: 2020年6月7日 下午4:46:34;
 * @Description 允许扩展BeanFactory的标准作用域“singleton”和“prototype”，使用为特定密钥注册的自定义进一步作用域。
 */
public interface Scope {
	/**
	 * 从基础作用域返回具有给定名称的对象，如果在基础存储机制中找不到，则创建该对象。
	 * 这是作用域的中心操作，也是唯一绝对需要的操作。
	 */
	Object get(String name, ObjectFactory<?> objectFactory);

	/**
	 * 从基础作用域中移除具有给定名称的对象。
	 * 如果找不到对象，则返回null；否则返回已删除的对象。
	 * 注意，实现还应该删除指定对象（如果有的话）的已注册销毁回调。
	 * 但是，在这种情况下，它不需要执行已注册的销毁回调，因为对象将被调用方销毁（如果合适）。
	 */
	Object remove(String name);

	/**
	 * 注册一个回调，以便在销毁作用域中的指定对象时执行（或者在销毁整个作用域时执行，如果作用域不销毁单个对象，而只销毁其整个对象）。
	 */
	void registerDestructionCallback(String name, Runnable callback);

	/**
	 * 解析给定键的上下文对象（如果有）。例如，键“request”的HttpServletRequest对象。
	 */
	Object resolveContextualObject(String key);

	/**
	 * 返回当前基础作用域的会话ID（如果有）。
	 * 
	 * 会话ID的确切含义取决于底层的存储机制。
	 * 在会话范围对象的情况下，会话ID通常等于（或派生自）会话ID；
	 * 在位于整个会话内的自定义会话的情况下，当前会话的特定ID将是适当的。
	 */
	String getConversationId();
}
