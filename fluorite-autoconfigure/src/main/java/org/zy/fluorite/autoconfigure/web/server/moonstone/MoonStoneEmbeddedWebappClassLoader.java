package org.zy.fluorite.autoconfigure.web.server.moonstone;

import org.zy.moonstone.core.loaer.ParallelWebappClassLoader;

/**
 * @dateTime 2022年12月7日;
 * @author zy(azurite-Y);
 * @description
 */
public class MoonstoneEmbeddedWebappClassLoader extends ParallelWebappClassLoader {
	public MoonstoneEmbeddedWebappClassLoader() {}

	public MoonstoneEmbeddedWebappClassLoader(ClassLoader parent) {
		super(parent);
	}
}
