package org.zy.fluorite.aop.aspectj.interfaces;

import org.zy.fluorite.core.interfaces.AnnotationMetadata;

/**
 * @DateTime 2020年7月5日 下午3:34:58;
 * @author zy(azurite-Y);
 * @Description
 */
public interface MetadataAwareAspectInstanceFactory extends AspectInstanceFactory{
	/** 获得此工厂的切面元数据 */
	AnnotationMetadata getAspectMetadata();

	/** 返回此工厂的最佳创建互斥体 */
	Object getAspectCreationMutex();

}
