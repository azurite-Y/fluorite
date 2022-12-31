package org.zy.fluorite.autoconfigure.web.server.moonstone;

import org.zy.moonStone.core.LifecycleState;
import org.zy.moonStone.core.exceptions.LifecycleException;
import org.zy.moonStone.core.session.StandardSessionIdGenerator;

/**
 * @dateTime 2022年12月7日;
 * @author zy(azurite-Y);
 * @description {@link StandardSessionIdGenerator } 的特化，它惰性初始化 SecureRandom
 */
class LazySessionIdGenerator extends StandardSessionIdGenerator {
	
	@Override
	protected void startInternal() throws LifecycleException {
		setState(LifecycleState.STARTING);
	}
	
}
