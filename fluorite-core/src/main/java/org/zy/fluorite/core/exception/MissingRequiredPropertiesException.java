package org.zy.fluorite.core.exception;

/**
 * @author: zy(azurite-Y);
 * @DateTime: 2020年6月16日 下午3:46:46;
 * @Description 因缺少必要属性而引发的异常
 */
@SuppressWarnings("serial")
public class MissingRequiredPropertiesException extends IllegalStateException {
	  public MissingRequiredPropertiesException() {
	        super();
	    }
	    public MissingRequiredPropertiesException(String msg) {
	        super(msg);
	    }
	    public MissingRequiredPropertiesException(String message, Throwable cause) {
	        super(message, cause);
	    }
	    public MissingRequiredPropertiesException(Throwable cause) {
	        super(cause);
	    }
}
