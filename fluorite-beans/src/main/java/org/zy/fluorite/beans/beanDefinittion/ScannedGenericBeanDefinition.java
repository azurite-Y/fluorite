package org.zy.fluorite.beans.beanDefinittion;

import org.zy.fluorite.core.interfaces.Resource;
import org.zy.fluorite.core.utils.ReflectionUtils;

/**
 * @author: zy(azurite-Y);
 * @DateTime: 2020年6月5日 下午3:14:25;
 * @Description
 */
@SuppressWarnings("serial")
public class ScannedGenericBeanDefinition extends AbstractBeanDefinition {
	/**
	 * ClassMetadata : StandardClassMetadata 
	 * AccessibleObjectMetadate : 
	 * StandardConstructorMetadata 
	 * StandardFieldMetadata
	 * StandardMethodMetadata
	 */
//	private final AnnotationMetadata metadata;
	
	private Resource resource;
	
	@Override
	public RootBeanDefinition cloneBeanDefinition() {
		return new RootBeanDefinition(this);
	}
//	public ScannedGenericBeanDefinition(AnnotationMetadata annotationMetadata) {
//		Assert.notNull(annotationMetadata, "AnnotationMetadata不能为null");
//		this.metadata = annotationMetadata;
//	}
	public ScannedGenericBeanDefinition(Class<?> clz,Resource resource) {
//		this(new AnnotationMetadataHolder(clz));
		this.resource = resource;
		this.beanClass = clz;
	}

//	@Override
//	public final AnnotationMetadata getMetadata() {
//		return this.metadata;
//	}

//	@Override
//	public void setMetadata(AnnotationMetadata metadata) {}

//	@Override
//	public void setParentName(String parentName) {}

	@Override
	public String getParentName() {
		return null;
	}
	@Override
	public Class<?> getBeanClass() throws IllegalStateException {
		Object beanClassObject = super.beanClass;
		if (beanClassObject instanceof String) {
			return ReflectionUtils.forName(beanClassObject.toString());
		}
		return (Class<?>) beanClassObject;

	}
	@Override
	public void setBeanClass(Class<?> beanClass) {
		super.beanClass = this.beanClass;
	}
	public Resource getResource() {
		return resource;
	}
	public void setResource(Resource resource) {
		this.resource = resource;
	}
	@Override
	public void setParentName(String parentName) {
		
	}
}
