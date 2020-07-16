package org.zy.fluorite.beans.factory.aware;

import org.zy.fluorite.core.interfaces.StringValueResolver;

/**
 * @DateTime 2020年6月17日 下午4:39:51;
 * @author zy(azurite-Y);
 * @Description
 */
public interface EmbeddedValueResolverAware {

	void setEmbeddedValueResolver(StringValueResolver embeddedValueResolver);

}
