package org.zy.fluorite.aop.exception;

/**
 * @DateTime 2020年7月4日 下午6:49:28;
 * @author zy(azurite-Y);
 * @Description 不可知的Advice类型异常
 */
@SuppressWarnings("serial")
public class UnknownAdviceTypeException extends IllegalArgumentException {
		public UnknownAdviceTypeException(Object advice) {
			super("不支持的Advice对象[" + advice + "] ，需是Advice或Advisor的子类型");
		}
		public UnknownAdviceTypeException(String message) {
			super(message);
		}
}
