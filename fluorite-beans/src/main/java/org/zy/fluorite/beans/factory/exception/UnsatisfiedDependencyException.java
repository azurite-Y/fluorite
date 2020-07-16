package org.zy.fluorite.beans.factory.exception;

/**
 * @author: zy(azurite-Y);
 * @DateTime: 2020年6月12日 上午9:10:22;
 * @Description 未满足条件的依赖项异常
 */ 
@SuppressWarnings("serial")
public class UnsatisfiedDependencyException extends BeanCreationException {

	public UnsatisfiedDependencyException(String msg) {
		super(msg);
	}
	public UnsatisfiedDependencyException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
