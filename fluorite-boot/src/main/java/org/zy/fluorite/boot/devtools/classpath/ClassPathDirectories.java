package org.zy.fluorite.boot.devtools.classpath;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zy.fluorite.core.utils.ResourceUtils;

/**
 * @dateTime 2022年12月24日;
 * @author zy(azurite-Y);
 * @description 提供对类路径中引用目录的条目的访问
 */
public class ClassPathDirectories implements Iterable<File> {
	private static final Logger logger = LoggerFactory.getLogger(ClassPathDirectories.class);

	private final List<File> directories = new ArrayList<>();

	public ClassPathDirectories(URL[] urls) {
		if (urls != null) {
			addUrls(urls);
		}
	}

	private void addUrls(URL[] urls) {
		for (URL url : urls) {
			addUrl(url);
		}
	}

	private void addUrl(URL url) {
		if (url.getProtocol().equals("file") && url.getPath().endsWith("/")) {
			try {
				this.directories.add(ResourceUtils.getFile(url));
			}
			catch (Exception ex) {
				logger.warn(String.format("Unable to get classpath URL: %s", url));
				logger.trace(String.format("Unable to get classpath URL: %s", url), ex);
			}
		}
	}

	@Override
	public Iterator<File> iterator() {
		return Collections.unmodifiableList(this.directories).iterator();
	}

}
