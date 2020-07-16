package core.convert;

import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import org.junit.jupiter.api.Test;
import org.zy.fluorite.core.utils.PropertiesUtils;

/**
 * @DateTime 2020年7月13日 下午5:58:06;
 * @author zy(azurite-Y);
 * @Description
 */
public class SystemResourceTest {
	@Test
	void testGetPropertyDescriptors() throws IOException {
		URL url = ClassLoader.getSystemResource("META-INF/fluorite.factories");
		Properties load = PropertiesUtils.load(url.openStream(),false);
		Object object = load.get("org.zy.fluorite.context.interfaces.ApplicationContextInitializer");
		String property = load.getProperty("org.zy.fluorite.context.interfaces.ApplicationContextInitializer");
		String property2 = load.getProperty("org.zy.fluorite.context.interfaces.ApplicationContextInitializer2");
		System.out.println(object instanceof String);
		System.out.println(object);
		System.out.println(property);
		System.out.println(property2);
	}
}
