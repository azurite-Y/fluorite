package org.zy.fluorite.core.io;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @DateTime 2020年6月23日 上午9:56:06;
 * @author zy(azurite-Y);
 * @Description 封装资源的输入输出流相关信息
 */
public final class SteamStore {
	// ---------------------------------字节流-------------------------------------------
	/** 字节输入流 - FileInputStream */
	private InputStream inputStream;

	/** 字节输出流 - FileOutputStream */
	private OutputStream outputStream;

	/** 字节缓冲输入流 */
	private BufferedInputStream bufferedInputStream;

	/** 字节缓冲输出流 */
	private BufferedOutputStream bufferedOutputStream;

	// ---------------------------------字符流-------------------------------------------
	/** 字节转换输入流 - FileReader */
	private InputStreamReader InputStreamReader;

	/** 字节转换输出流 - FileWriter */
	private OutputStreamWriter outputStreamWriter;

	/** 字符输入流 FileReader */
	private Reader reader;

	/** 字符输出流 FileWriter */
	private Writer Writer;

	/** 字符缓冲输入流流 */
	private BufferedReader bufferedReader;

	/** 字符缓冲输出流 */
	private BufferedWriter bufferedWriter;

	private File file;

	private Path path;

	public SteamStore(File file) {
		super();
		this.file = file;
		this.path = file.toPath();
	}

	public InputStream getInputStream() {
		if (inputStream == null) {
			try {
				inputStream = new FileInputStream(file);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		return inputStream;
	}

	public OutputStream getOutputStream() {
		if (outputStream == null) {
			try {
				outputStream = new FileOutputStream(file);
				return outputStream;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return outputStream;
	}

	public BufferedInputStream getBufferedInputStream() {
		if (bufferedInputStream == null) {
			bufferedInputStream = new BufferedInputStream(getInputStream(),1024);
		}
		return bufferedInputStream;
	}

	public BufferedOutputStream getBufferedOutputStream() {
		if (bufferedOutputStream == null) {
			bufferedOutputStream = new BufferedOutputStream(getOutputStream(),1024);
		}
		return bufferedOutputStream;
	}

	public InputStreamReader getInputStreamReader() {
		if (InputStreamReader == null) {
			try {
				InputStreamReader = new  InputStreamReader(getBufferedInputStream(), "utf-8");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return InputStreamReader;
	}

	public OutputStreamWriter getOutputStreamWriter() {
		if (outputStreamWriter == null) {
			try {
				outputStreamWriter = new OutputStreamWriter(getBufferedOutputStream(),"utf-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		return outputStreamWriter;
	}

	public Reader getReader() {
		if (reader == null) {
			try {
				reader = new FileReader(file);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		return reader;
	}

	public Writer getWriter() {
		if (Writer == null) {
			try {
				Writer = new FileWriter(file,true);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return Writer;
	}

	public BufferedReader getBufferedReader() {
		if (bufferedReader == null) {
			bufferedReader = new BufferedReader(getReader(),1024);
		}
		return bufferedReader;
	}

	public BufferedWriter getBufferedWriter() {
		if (bufferedWriter == null) {
			try {
				bufferedWriter = Files.newBufferedWriter(path);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return bufferedWriter;
	}

	public File getFile() {
		return file;
	}

	public Path getPath() {
		return path;
	}
}
