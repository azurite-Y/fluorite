package org.zy.fluorite.core.utils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.List;

import org.zy.fluorite.core.interfaces.function.InvorkFunction;

public final class IOUtils {
	/**
	 * 工具类关闭流 可变参数: ... 只能形参最后一个位置,处理方式与数组一致
	 * 
	 * @param stream - 关闭流对象
	 * @throws IOException
	 */
	public static void close(Closeable... stream) {
		for (Closeable temp : stream) {
			if (null != temp) {
				try {
					temp.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	// 文件内容操作
	/**
	 * 文件内容读取到控制台
	 * 
	 * @param srcPath - 读取文件路径
	 */
	public static void readFileToConsole(String srcPath) {
		// 换行占两个字节
		File src = new File(srcPath);
//		System.out.println("文件内容：");
		InputStream is = null;
		char[] ch = null;
		try {
			is = new BufferedInputStream(new FileInputStream(src));
			byte[] array = new byte[1024];
			int len = 0;
			while (-1 != (len = is.read(array))) {
				ch = new String(array, 0, len).toCharArray();
				System.out.println(ch);
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		IOUtils.close(is);
//		System.out.println("=========文件读取完成============");
	}

	/**
	 * 通过字符数组实现文件的拷贝
	 * 
	 * @param strSrc  - 要拷贝的源文件路径
	 * @param strDest - 拷贝文件的存放路径
	 */
	public static void byteArrayForFileCopy(String strSrc, String strDest) {
		File src = new File(strSrc);
		File dest = new File(strDest);

		FileInputStream fis = null;
		ByteArrayOutputStream baos = null;
		ByteArrayInputStream bais = null;
		FileOutputStream fos = null;

		try {
			fis = new FileInputStream(src);
			baos = new ByteArrayOutputStream();

			int len = 0;
			byte[] arr = new byte[1024 * 60];

			while (-1 != (len = fis.read(arr))) {
				baos.write(arr, 0, len);
				baos.flush();
			}
			byte[] arrays = baos.toByteArray();

			bais = new ByteArrayInputStream(arrays);
			fos = new FileOutputStream(dest);

			while (-1 != (len = bais.read(arr))) {
				fos.write(arr, 0, len);
				fos.flush();
			}
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		} finally {
			close(fis, baos, bais, fos);
			if (dest.exists()) {
//				System.out.println("文件操作成功");
			}
		}
	}

	/**
	 * 将文件读取到字符数组当中</br>
	 * IO 文件-->文件输入流-->程序-->字符数组输出流-->return byte[]
	 * 
	 * @param strSrc 读取的源文件路径
	 * @return byte[] - 存储源文件数据的字符数组
	 */
	public static byte[] fileToByteArray(String strSrc) {
		File src = new File(strSrc);

		FileInputStream fis = null;
		ByteArrayOutputStream baos = null;

		try {
			fis = new FileInputStream(src);
			baos = new ByteArrayOutputStream();

			int len = 0;
			byte[] arr = new byte[1024 * 60];

			while (-1 != (len = fis.read(arr))) {
				baos.write(arr, 0, len);
				baos.flush();
			}
			byte[] arrays = baos.toByteArray();

			return arrays;
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		} finally {
			close(fis, baos);
		}
		return null;
	}

//	addPropertyFile("")
	
	/**
	 * 将字符数组中的数据通过文件输出流写入到目标文件当中</br>
	 * io--> byte[]-->字符数组输入流-->程序-->文件输出流-->文件
	 * 
	 * @param strDest - 写出数据的文件保存路径
	 * @param bytes   - 存储数据的字符数组
	 */
	public static void byteArrayToFile(String strDest, byte[] bytes) {
		File dest = new File(strDest);

		ByteArrayInputStream bais = null;
		FileOutputStream fos = null;

		try {
			int len = 0;
			byte[] arr = new byte[1024 * 60];
			bais = new ByteArrayInputStream(bytes);
			fos = new FileOutputStream(dest);

			while (-1 != (len = bais.read(arr))) {
				fos.write(arr, 0, len);
				fos.flush();
			}
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		} finally {
			close(bais, fos);
		}
	}

	/**
	 * 工具类关闭流
	 * 
	 * @param strThrow - 异常信息
	 * @param stream   - 关闭的IO流对象
	 */
	public static void close(String strThrow, Closeable... stream) {
		for (Closeable temp : stream) {
			if (null != temp) {
				try {
					temp.close();
					throw new IOException(strThrow);
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		}
	}
	
	/**
	 * 重置字节输入流标记
	 * @param reader
	 * @param readAheadLimit
	 */
	public static InputStream reset(InputStream inputStream, int readAheadLimit) {
		Assert.notNull(inputStream , "’InputStream‘不能为null");
		if (inputStream.markSupported()) {
			inputStream.mark(readAheadLimit);
			try {
				inputStream.reset();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return inputStream;
	}

	/**
	 * 重置字符输入流标记
	 * @param reader
	 * @param readAheadLimit
	 */
	public static Reader reset(Reader reader,int readAheadLimit) {
		Assert.notNull(reader , "’Reader‘不能为null");
		if (reader.markSupported()) {
			try {
				reader.mark(readAheadLimit);
				reader.reset();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return reader;
	}
	
	/**
	 * 使用指定输出流写出指定内容
	 * @param writer
	 * @param function - 自定义写入动作的方法调用索引，传递指定的输出流作为其参数。为null则不进行调用
	 * @param append
	 * @return 在写入成功并刷新之后的指定流对象
	 * @throws IOException 
	 */
	public static BufferedWriter writerFile(BufferedWriter writer ,InvorkFunction<BufferedWriter> function,String... append) throws IOException {
		Assert.notNull(writer , "’BufferedWriter‘不能为null");
		for (String string : append) {
			writer.write(string);
			if (function != null) function.invork(writer);
			writer.flush();
		}
		return writer;
	}
	
	/**
	 * 使用指定输入流读取文件内容并执行不为null的自定义读取动作
	 * @param reader
	 * @param function - 自定义读取动作的方法调用索引，传递指定的输入流作为其参数。为null则不进行调用
	 * @return 在读取成功之后的为重置指定流对象
	 * @throws IOException 
	 */
	public static BufferedReader readerFile(BufferedReader reader, InvorkFunction<BufferedReader> function) {
		Assert.notNull(reader , "’BufferedReader‘不能为null");
		try {
			while(reader.ready()) {
				if (function != null) function.invork(reader);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return reader;
	}
	
	/**
	 * 使用指定输入流读取文件内容的每一行保存到指定List容器中
	 * @param reader
	 * @param function - 自定义读取动作的方法调用索引，传递指定的输入流作为其参数。为null则不进行调用
	 * @param result
	 * @return 在读取成功之后的为重置指定流对象
	 * @throws IOException 
	 */
	public static BufferedReader readerFile(BufferedReader reader, InvorkFunction<BufferedReader> function, List<String> result) {
		Assert.notNull(result, "’result’-结果存储容器不能为null");
		Assert.notNull(reader,"'BufferedReader'不能为null");
		try {
			while(reader.ready()) {
				if (function != null) function.invork(reader);
				result.add(reader.readLine().trim());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return reader;
	}
}
