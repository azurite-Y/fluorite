package org.zy.fluorite.autoconfigure.web.server.moonstone;

import org.zy.fluorite.autoconfigure.web.server.moonstone.interfaces.MoonstoneContextCustomizer;
import org.zy.moonstone.core.container.context.StandardContext;
import org.zy.moonstone.core.interfaces.container.Context;

/**
 * @dateTime 2022年12月7日;
 * @author zy(azurite-Y);
 * @description 一个 {@link MoonstoneContextCustomizer} ，它禁用Tomcat的反射引用清除，以避免在Java9和更高版本的JVM上出现反射访问警告。
 */
public class DisableReferenceClearingContextCustomizer implements MoonstoneContextCustomizer {
	@Override
	public void customize(Context context) {
		if (!(context instanceof StandardContext)) {
			return;
		}
		StandardContext standardContext = (StandardContext) context;
		try {
			standardContext.setClearReferencesObjectStreamClassCaches(false);
//			standardContext.setClearReferencesRmiTargets(false);
			standardContext.setClearReferencesThreadLocals(false);
		}
		catch (NoSuchMethodError ex) {
			ex.printStackTrace();
		}
	}
}
