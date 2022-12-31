package org.zy.fluorite.core.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * @dateTime 2022年12月24日;
 * @author zy(azurite-Y);
 * @description
 */
public abstract class ResourceUtils {
	/** 从类路径加载的伪URL前缀: "classpath:". */
	public static final String CLASSPATH_URL_PREFIX = "classpath:";

	/** 用于从文件系统加载的URL前缀: "file:". */
	public static final String FILE_URL_PREFIX = "file:";

	/** 从jar文件加载的URL前缀: "jar:". */
	public static final String JAR_URL_PREFIX = "jar:";

	/** 从war文件加载的URL前缀: "war:". */
	public static final String WAR_URL_PREFIX = "war:";

	/** 文件系统中文件的URL协议: "file". */
	public static final String URL_PROTOCOL_FILE = "file";

	/** jar文件中条目的URL协议: "jar". */
	public static final String URL_PROTOCOL_JAR = "jar";

	/** 来自war文件的条目的URL协议: "war". */
	public static final String URL_PROTOCOL_WAR = "war";

	/** 来自zip文件的条目的URL协议: "zip". */
	public static final String URL_PROTOCOL_ZIP = "zip";
	
	/** 普通jar文件的文件扩展名: ".jar". */
	public static final String JAR_FILE_EXTENSION = ".jar";

	/** JAR URL和JAR中的文件路径之间的分隔符: "!/". */
	public static final String JAR_URL_SEPARATOR = "!/";

	/** Tomcat上WAR URL和jar部分之间的特殊分隔符. */
	public static final String WAR_URL_SEPARATOR = "*/";
	
	
	/**
	 * 将给定的资源URL解析为一个 {@code java.io.File}，即文件系统中的一个文件
	 * 
	 * @param resourceUrl - 要解析的资源URL
	 * @return 对应的文件对象
	 * @throws FileNotFoundException - 如果无法将URL解析为文件系统中的文件
	 */
	public static File getFile(URL resourceUrl) throws FileNotFoundException {
		return getFile(resourceUrl, "URL");
	}
	
	/**
	 * 将给定的资源URL解析为一个 {@code java.io.File}，即文件系统中的一个文件
	 * 
	 * @param resourceUrl - 要解析的资源URL
	 * @param description - 为其创建URL的原始资源的描述(例如，类路径位置)
	 * @return 对应的文件对象
	 * @throws FileNotFoundException - 如果无法将URL解析为文件系统中的文件
	 */
	public static File getFile(URL resourceUrl, String description) throws FileNotFoundException {
		Assert.notNull(resourceUrl, "Resource URL 不能为 null");
		if (!URL_PROTOCOL_FILE.equals(resourceUrl.getProtocol())) {
			throw new FileNotFoundException(description + "无法解析为绝对文件路径，因为它不驻留在文件系统中: " + resourceUrl);
		}
		try {
			return new File(toURI(resourceUrl).getSchemeSpecificPart());
		}
		catch (URISyntaxException ex) {
			// Fallback for URLs that are not valid URIs (should hardly ever happen).
			return new File(resourceUrl.getFile());
		}
	}

	
	/**
	 * 将给定的资源URI解析为一个 {@code java.io.File}，即文件系统中的一个文件
	 * 
	 * @param resourceUrl - 要解析的资源URI
	 * @return 对应的文件对象
	 * @throws FileNotFoundException - 如果无法将URL解析为文件系统中的文件
	 */
	public static File getFile(URI resourceUri) throws FileNotFoundException {
		return getFile(resourceUri, "URI");
	}
	
	/**
	 * 将给定的资源URI解析为一个 {@code java.io.File}，即文件系统中的一个文件
	 * 
	 * @param resourceUrl - 要解析的资源URI
	 * @param description - 为其创建URL的原始资源的描述(例如，类路径位置)
	 * @return 对应的文件对象
	 * @throws FileNotFoundException - 如果无法将URL解析为文件系统中的文件
	 */
	public static File getFile(URI resourceUri, String description) throws FileNotFoundException {
		Assert.notNull(resourceUri, "Resource URI 不能为 null");
		if (!URL_PROTOCOL_FILE.equals(resourceUri.getScheme())) {
			throw new FileNotFoundException(description + " 无法解析为绝对文件路径，因为它不驻留在文件系统中: " + resourceUri);
		}
		return new File(resourceUri.getSchemeSpecificPart());
	}
	
	/**
	 * 为给定的URL创建一个URI实例，首先用“%20”URI编码替换空格。
	 * 
	 * @param url - 要转换为URI实例的URL
	 * @return URI 实例
	 * @throws URISyntaxException - 如果URL不是有效的URI
	 * @see java.net.URL#toURI()
	 */
	public static URI toURI(URL url) throws URISyntaxException {
		return toURI(url.toString());
	}

	/**
	 * 为给定的location字符串创建一个URI实例，首先用“%20”URI编码替换空格。
	 * 
	 * @param location - 要转换为URI实例的Location字符串
	 * @return the URI instance
	 * @throws URISyntaxException - 如果location不是有效的URI
	 */
	public static URI toURI(String location) throws URISyntaxException {
		Assert.notNull("location 不能为 null");
		return new URI(location.replace(" ", "%20"));
	}
}
