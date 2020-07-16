package org.zy.fluorite.context.interfaces;

import java.util.Locale;

import org.zy.fluorite.context.exception.NoSuchMessageException;

/**
 * @DateTime 2020年6月17日 下午1:24:59;
 * @author zy(azurite-Y);
 * @Description 用于解析消息的策略接口，支持此类消息的参数化和国际化
 */
public interface MessageSource {
	/**
	 * 尝试解析消息。如果找不到消息，则返回默认消息
	 * @param code - 要查找的消息代码
	 * @param args - 将为消息中的参数（参数在消息中类似于“{0}”、“{1，date}”、“{2，time}”）填充的参数数组，如果没有则为空
	 * @param defaultMessage - 查找失败时返回的默认消息
	 * @param locale - 进行查找的区域设置
	 * @return - 如果查找成功，则为解析消息，否则为作为参数传递的默认消息（可能为空）
	 */
	String getMessage(String code, Object[] args, String defaultMessage, Locale locale);

	/**
	 * 尝试解析消息。
	 * @param code - 要查找的消息代码
	 * @param args - 将为消息中的参数（参数在消息中类似于“{0}”、“{1，date}”、“{2，time}”）填充的参数数组，如果没有则为空
	 * @param locale - 进行查找的区域设置
	 * @return - 已解析的消息（从不为空）
	 */
	String getMessage(String code, Object[] args, Locale locale) throws NoSuchMessageException;

	/**
	 * 尝试使用传入的MessageSourceResolvable参数中包含的所有属性解析消息。
	 * <p>因为无法确定可解析的defaultMessage属性是否为空所有必须引发NoSuchMessageException一场</p>
	 * @param resolvable - 存储解析消息所需属性的值对象（可能包括默认消息）
	 */
	String getMessage(MessageSourceResolvable resolvable, Locale locale) throws NoSuchMessageException;
}
