package org.zy.fluorite.core.interfaces;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

/**
 * @author: zy(azurite-Y);
 * @DateTime: 2020年6月4日 下午5:00:11;
 * @Description
 */
public interface WritableResource extends Resource{
	/**
	 * 判断资源文件是否可写
	 * @return
	 */
	boolean isWritable();
	
	/**
	 * 获得此资源的字节输出流
	 * @return
	 */
	OutputStream getOutputStream();
	
	/**
	 * 获得此资源的字节缓冲输出流
	 * @return
	 */
	BufferedOutputStream getBufferedOutputStream();

	/**
	 * 获得此资源的字节转换输出流
	 * @return
	 */
	OutputStreamWriter getOutputStreamWriter();
	
	/**
	 * 获得此资源的字符输出流
	 * @return
	 */
	Writer getWriter();
	
	/**
	 * 获得此资源的字符缓冲输出流
	 * @param reset - true则重置此输出流，反之则原封不动的返回
	 * @return
	 */
	BufferedWriter getBufferedWriter();
	
}
