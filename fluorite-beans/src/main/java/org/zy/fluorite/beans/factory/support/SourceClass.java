package org.zy.fluorite.beans.factory.support;

import org.zy.fluorite.beans.support.AnnotationMetadataHolder;
import org.zy.fluorite.core.annotation.Order;
import org.zy.fluorite.core.interfaces.AnnotationMetadata;
import org.zy.fluorite.core.interfaces.Ordered;

/**
 * @DateTime 2020年6月24日 下午5:31:21;
 * @author zy(azurite-Y);
 * @Description 封装指定Class对象的非java注解集合，并提供相关获取方法
 */
public class SourceClass implements Ordered {
	private final Class<?> source;
	
	private AnnotationMetadata annotationMetadata;
	
	public SourceClass(Class<?> source) {
		super();
		this.source = source;
		this.annotationMetadata = new AnnotationMetadataHolder(source);
	}
	
	public SourceClass(Class<?> source, AnnotationMetadata annotationMetadata) {
		super();
		this.source = source;
		this.annotationMetadata = annotationMetadata;
	}

	public AnnotationMetadata getAnnotationMetadata() {
		return annotationMetadata;
	}

	public Object getSimpleName() {
		return this.source.getSimpleName();
	}
	
	public String getName() {
		return this.source.getName();
	}
	
	public Class<?> getSource() {
		return source;
	}
	

	@Override
	public int getOrder() {
		Order order = this.annotationMetadata.getAnnotationForClass(Order.class);
		return order== null ? Ordered.LOWEST_PRECEDENCE : order.value();
	}

	@Override
	public String toString() {
		return "SourceClass [source=" + source + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((source == null) ? 0 : source.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SourceClass other = (SourceClass) obj;
		if (source == null) {
			if (other.source != null)
				return false;
		} else if (!source.equals(other.source))
			return false;
		return true;
	}
	
}
