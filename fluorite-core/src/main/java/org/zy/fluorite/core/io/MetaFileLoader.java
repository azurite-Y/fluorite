package org.zy.fluorite.core.io;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zy.fluorite.core.utils.Assert;
import org.zy.fluorite.core.utils.PropertiesUtils;
import org.zy.fluorite.core.utils.StringUtils;

/**
 * @DateTime 2020年6月26日 下午1:55:42;
 * @author zy(azurite-Y);
 * @Description 加载META-INF文件夹下的指定名称资源
 */
public final class MetaFileLoader {
	protected  static final Logger logger = LoggerFactory.getLogger(MetaFileLoader.class);

	private static final Map<String , MetaFileAttribute> cache = new ConcurrentHashMap<>();

	/**
	 * @param moduleName - 要查找的jar包模块名称。如fluorite-core、fluorite-beans等
	 * @param filePath - META-INF目录下的相对路径路径，
	 * @param keyName - 指定文件中要获取的key
	 * @return - 返回目标文件中key相关的value值，若未找到对应的key则返回null
	 */
	public static List<String> loadFactories(String moduleName ,String filePath ,String keyName) {
		Assert.hasText(moduleName, "'moduleName'不能为null或空串");
		Assert.hasText(filePath, "'filePath'不能为null或空串");
		
		MetaFileAttribute loadMetaFile = loadMetaFile(moduleName , filePath);
		if ( loadMetaFile != null) {
			return loadMetaFile.get(keyName);
		}
		return null;
	}

	/**
	 * 加载项目中符合指定路径下的所有文件，只返回指定模块下的文件信息
	 * @param moduleName - 要查找的jar包模块名称
	 * @param filePath - META-INF目录下的路径
	 * @return
	 */
	public static MetaFileAttribute loadMetaFile(String moduleName ,String filePath) {
		StringBuilder reverse = new StringBuilder();
		boolean[] contain = {false};
		String[] moduleNameArr = {""};
		
		StringBuilder builder = new StringBuilder();
		builder.append(moduleName).append("-").append(filePath);
		
		MetaFileAttribute metaFileAttributeSource = cache.get(builder.toString());
		if (metaFileAttributeSource != null) {
			return metaFileAttributeSource;
		}
		
		try {
			Enumeration<URL> resources = ClassLoader.getSystemResources("META-INF/"+filePath);
			while (resources.hasMoreElements()) {
				// 数据初始化
				contain[0] = false;
				moduleNameArr[0] = "";

				reverse.delete(0, reverse.length());

				URL url = resources.nextElement();
				// 如：/...Workspace/Eclipse/fluorite/fluorite-core(moduleName)/target/classes/META-INF/b.txt
				String path = url.getPath();
//				System.out.println(path);
				
				String[] array = StringUtils.tokenizeToStringArray(path, "/" , (str) -> {
					if (str.equals("target")) {
						contain[0] = true;
					} 
					if (!contain[0]) {
						moduleNameArr[0] = str;
					}
				});
				
				reverse.append(moduleNameArr[0]).append("-").append(array[array.length -1]);
//				System.out.println("key：" +reverse.toString());
				MetaFileAttribute metaFileAttribute = cache.get(reverse.toString());

				if (metaFileAttribute == null) {
					metaFileAttribute = new MetaFileAttribute(url);
				}

				// 不抑制异常
				Properties load = PropertiesUtils.load(url.openStream(),false);
				if (load != null) {
					for (Entry<Object, Object> entry : load.entrySet()) {
						String[] stringArray = StringUtils.tokenizeToStringArray(entry.getValue().toString(), ",",null);
						for (String string : stringArray) {
							metaFileAttribute.add(entry.getKey().toString(), string);
						}
					}
					cache.put(reverse.toString(), metaFileAttribute);
					
//					System.out.println("["+builder.toString()+"] - - ["+reverse.toString()+"]");
					if (builder.toString() .equals(reverse.toString())) {
						return metaFileAttribute;
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
}
