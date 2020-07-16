package org.zy.fluorite.core.subject;

import java.util.ArrayList;
import java.util.List;

import org.zy.fluorite.core.interfaces.BeanMetadataElement;
import org.zy.fluorite.core.interfaces.Mergeable;

/**
 * @author: zy(azurite-Y);
 * @DateTime: 2020年6月5日 上午9:16:27;
 * @Description
 */
@SuppressWarnings("serial")
public class ManagedList<E> extends  ArrayList<E> implements Mergeable,BeanMetadataElement {

	private Object source;
	
	private String elementTypeName;

	private boolean mergeEnabled;
	
	public ManagedList() {
	}
	public ManagedList(int initialCapacity) {
		super(initialCapacity);
	}
	
	@Override
	public boolean isMergeEnabled() {
		return this.mergeEnabled;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object merge(Object parent) {
		if (!this.mergeEnabled) {
			throw new IllegalStateException("当“mergeEnabled”属性设置为“false”时，不允许合并");
		}
		if (parent == null) {
			return this;
		}
		if (!(parent instanceof List)) {
			throw new IllegalArgumentException("不能合并的对象，by：" + parent.getClass());
		}
		List<E> merged = new ManagedList<>();
		merged.addAll((List<E>) parent);
		merged.addAll(this);
		return merged;
	}

	@Override
	public Object getSource() {
		return this.source;
	}
	public String getElementTypeName() {
		return elementTypeName;
	}
	public void setElementTypeName(String elementTypeName) {
		this.elementTypeName = elementTypeName;
	}
	public void setSource(Object source) {
		this.source = source;
	}
	public void setMergeEnabled(boolean mergeEnabled) {
		this.mergeEnabled = mergeEnabled;
	}
}
