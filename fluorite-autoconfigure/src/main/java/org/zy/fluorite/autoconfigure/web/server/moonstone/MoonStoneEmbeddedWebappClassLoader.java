package org.zy.fluorite.autoconfigure.web.server.moonstone;

import org.zy.moonStone.core.loaer.ParallelWebappClassLoader;

/**
 * @dateTime 2022年12月7日;
 * @author zy(azurite-Y);
 * @description
 */
public class MoonStoneEmbeddedWebappClassLoader extends ParallelWebappClassLoader {
	public MoonStoneEmbeddedWebappClassLoader() {}

	public MoonStoneEmbeddedWebappClassLoader(ClassLoader parent) {
		super(parent);
	}
}
