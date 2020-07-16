package org.zy.fluorite.core.interfaces;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * @author: zy(azurite-Y);
 * @DateTime: 2020年6月4日 下午5:07:15;
 * @Description
 */
public interface ReadableResource extends Resource {
	/**
	 * 判断此资源文件是否可读
	 * @param reset - true则重置此输出流，反之则原封不动的返回
	 * @return
	 */
	boolean isReadable();
	
	/**
	 * 获得此资源的字节输入流
	 * @param reset - true则重置此输出流，反之则原封不动的返回
	 * @return
	 */
	InputStream getInputStream(boolean reset);
	
	/**
	 * 获得此资源的字节缓冲输入流
	 * @param reset - true则重置此输出流，反之则原封不动的返回
	 * @return
	 */
	BufferedInputStream getBufferedInputStream(boolean reset);

	/**
	 * 获得此资源的字节转换输入流
	 * @param reset - true则重置此输出流，反之则原封不动的返回
	 * @return
	 */
	InputStreamReader getInputStreamReader(boolean reset);

	/**
	 * 获得此资源的字符输入流
	 * @param reset - true则重置此输出流，反之则原封不动的返回
	 * @return
	 */
	Reader getReader(boolean reset);
	
	/**
	 * 获得此资源的字符缓冲输入流
	 * @param reset - true则重置此输出流，反之则原封不动的返回
	 * @return
	 */
	BufferedReader getBufferedReader(boolean reset);
}
