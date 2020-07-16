package org.zy.fluorite.core.interfaces;

import java.io.File;
import java.io.IOException;

/**
 * @author: zy(azurite-Y);
 * @DateTime: 2020年6月4日 下午4:51:34;
 * @Description 资源文件操作方法接口，每一个实现类对象代表一个文件或是一个目录。
 * 若代表一个目录则获取IO流会抛出异常
 */
public interface Resource {
	
	File getFile();
	
	/**
	 * 判断当前资源是否是一个文件
	 * @return true则是一个文件，false则是一个目录
	 */
	default boolean isFile() {
		return getFile().isFile();
	}
	
	/**
	 * 判断当前资源是否存在于硬盘中
	 * @return
	 */
	boolean exists();
	
	/**
	 * 获得资源的文件名
	 * @return
	 */
	String getFileName();
	
	/**
	 * 获得资源的尺寸
	 * @return
	 * @throws IOException
	 */
	long contentLength() throws IOException;
	
	/**
	 * 获得资源的最后修改时间
	 * @return
	 * @throws IOException
	 */
	long lastModified() throws IOException;
	
	/**
	 * 获得当前Resource的名称，此名称带包名即此文件转为Class的全称类名。如“com.zy.app”
	 * @return
	 */
	String getResourceName();
	
	/**
	 * 当前Resource的名称
	 * @param name
	 */
	void setResourceName(String name);
	
	/**
	 * 获得此资源文件的扩展名
	 * @return
	 */
	String getExtension();
	
	/**
	 * 设置此资源文件的扩展名
	 */
	void setExtension(String extension);
}
