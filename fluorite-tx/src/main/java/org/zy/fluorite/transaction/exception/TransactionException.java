package org.zy.fluorite.transaction.exception;

import java.util.ArrayList;
import java.util.List;

import org.zy.fluorite.core.exception.FluoriteRuntimeException;

/**
 * @author: zy;
 * @DateTime: 2020年6月4日 下午3:14:35;
 * @Description 事务初始化和运行过程中可能触发的异常基类
 */
@SuppressWarnings("serial")
public class TransactionException extends FluoriteRuntimeException {

	private List<Throwable> relatedCauses;

	public TransactionException(String msg) {
		super(msg);
	}
	
	public TransactionException(String msg, Throwable cause) {
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
