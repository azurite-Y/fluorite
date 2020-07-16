package org.zy.fluorite.beans.support;

import org.zy.fluorite.core.interfaces.BeanMetadataElement;
import org.zy.fluorite.core.subject.AttributeAccessorSupport;

/**
 * @author: zy(azurite-Y);
 * @DateTime: 2020年6月5日 上午1:14:09;
 * @Description bean元数据持有者
 */
@SuppressWarnings("serial")
public class BeanMetadataAttributeAccessor extends AttributeAccessorSupport implements BeanMetadataElement {

	private Object source;

	public void setSource(Object source) {
		this.source = source;
	}

	@Override
	public Object getSource() {
		return this.source;
	}

	public void addMetadataAttribute(BeanMetadataAttribute attribute) {
		super.setAttribute(attribute.getName(), attribute);
	}

	public BeanMetadataAttribute getMetadataAttribute(String name) {
		return (BeanMetadataAttribute) super.getAttribute(name);
	}

	@Override
	public void setAttribute(String name, Object value) {
		super.setAttribute(name, new BeanMetadataAttribute(name, value));
	}

	@Override
	public Object getAttribute(String name) {
		BeanMetadataAttribute attribute = (BeanMetadataAttribute) super.getAttribute(name);
		return (attribute != null ? attribute.getValue() : null);
	}

	@Override
	public Object removeAttribute(String name) {
		BeanMetadataAttribute attribute = (BeanMetadataAttribute) super.removeAttribute(name);
		return (attribute != null ? attribute.getValue() : null);
	}

}
