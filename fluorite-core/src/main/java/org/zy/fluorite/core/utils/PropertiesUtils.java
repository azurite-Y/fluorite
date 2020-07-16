package org.zy.fluorite.core.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Properties;

/**
 * @DateTime 2020年6月20日 下午6:05:12;
 * @author zy(azurite-Y);
 * @Description 属性文件工具类
 */
public class PropertiesUtils {
	/**
	 * 加载指定路径和编码格式的配置文件
	 * @param path
	 * @param encode
	 * @param ignoreResourceNotFound - 是否忽略为找到的配置文件
	 * @return
	 */
	public static Properties load(String path,String encode,boolean ignoreResourceNotFound) {
		try {
			URL resource = ClassLoader.getSystemResource(path);
			if (resource == null) {return null;}
			
			return load(resource.openStream(), encode, ignoreResourceNotFound);
		} catch (IOException e) {
			if (ignoreResourceNotFound) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	/**
	 * 加载指定路径和编码格式的配置文件.解码格式为“utf-8”，且不抑制异常
	 * @param path
	 * @return
	 */
	public static Properties load(String path) {
		return load(path, "utf-8", false);
	}
	
	/**
	 * 加载指定路径和编码格式的配置文件。解码格式为“utf-8”
	 * @param path
	 * @return
	 */
	public static Properties load(String path,boolean ignoreResourceNotFound) {
		return load(path, "utf-8", ignoreResourceNotFound);
	}
	
	/**
	 * 加载指定流和编码格式的配置文件
	 * @param path - 文件路径
	 * @param encode - 解码格式
	 * @param ignoreResourceNotFound -  是否不抑制异常（是否忽略为找到的配置文件）
	 * @return
	 */
	public static Properties load(InputStream input,String encode,boolean ignoreResourceNotFound) {
		Properties properties = new Properties();
		try {
			properties.load(new InputStreamReader(input,encode));
		} catch (Exception e) {
			if (ignoreResourceNotFound) {
				e.printStackTrace();
			}
		}
		return properties;
	}
	
	/**
	 * 加载指定流和‘utf-8’编码格式的配置文件
	 * @param input
	 * @param ignoreResourceNotFound - 是否抑制异常（是否忽略为找到的配置文件）
	 * @return
	 */
	public static Properties load(InputStream input,boolean ignoreResourceNotFound) {
		return load(input, "utf-8", ignoreResourceNotFound);
	}
	
	/**
	 * 加载指定流和‘utf-8’编码格式的配置文件，且不抑制异常
	 * @param input
	 * @return
	 */
	public static Properties load(InputStream input) {
		return load(input, "utf-8", false);
	}
}
