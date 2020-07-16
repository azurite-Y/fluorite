package org.zy.fluorite.core.exception;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: zy;
 * @DateTime: 2020年6月4日 下午3:14:35;
 * @Description Bean对象在实例化和初始化过程中可能触发的异常基类
 */
@SuppressWarnings("serial")
public class BeansException extends FluoriteRuntimeException {

	private List<Throwable> relatedCauses;

	public BeansException(String msg) {
		super(msg);
	}
	
	public BeansException(String msg, Throwable cause) {
		super(msg, cause);
	}
	
	public void addRelatedCause(Throwable ex) {
		if (this.relatedCauses == null) {
			this.relatedCauses = new ArrayList<>();
		}
		this.relatedCauses.add(ex);
	}

	public List<Throwable> getRelatedCauses() {
		return relatedCauses;
	}
	public void setRelatedCauses(List<Throwable> relatedCauses) {
		this.relatedCauses = relatedCauses;
	}
}
