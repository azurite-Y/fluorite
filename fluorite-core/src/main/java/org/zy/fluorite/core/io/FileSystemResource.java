package org.zy.fluorite.core.io;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Path;

import org.zy.fluorite.core.interfaces.ReadableResource;
import org.zy.fluorite.core.interfaces.WritableResource;
import org.zy.fluorite.core.utils.Assert;
import org.zy.fluorite.core.utils.IOUtils;
import org.zy.fluorite.core.utils.StringUtils;

/**
 * @author: zy(azurite-Y);
 * @DateTime: 2020年6月4日 下午5:04:34;
 * @Description 
 */
public class FileSystemResource implements WritableResource,ReadableResource {
	private final File file;
	
	/** 此File所代表文件的名称 */
	private String fileName;
	
	/** 包名+文件名称， 懒处理*/
	private String resourceName;
	
	/** 文件后缀名，懒处理 */
	private String extension;
	
	private SteamStore steamStore;

	private Path filePath;

	/** 绝对路径 */
	private String path;
	
	public FileSystemResource(File file) {
		super();
		Assert.notNull(file, "File不可为null！");
		this.file = file;
		this.filePath = file.toPath();
		this.path = file.getAbsolutePath();
		this.fileName = file.getName();
		steamStore = new SteamStore(file);
	}

	@Override
	public File getFile() {
		return this.file;
	}

	@Override
	public boolean exists() {
		return this.file.exists();
	}

	@Override
	public boolean isReadable() {
		return this.file.canRead() && !this.file.isDirectory();
	}
	
	@Override
	public String getFileName() {
		return this.fileName;
	}

	@Override
	public long contentLength() throws IOException {
		return this.file.length();
	}

	@Override
	public long lastModified() throws IOException {
		return this.file.lastModified();
	}

	@Override
	public boolean isWritable() {
		return this.file.canWrite() && !this.file.isDirectory();
	}
	
	public Path getFilePath() {
		return filePath;
	}

	public String getPath() {
		return path;
	}

	@Override
	public String getResourceName() {
		Assert.hasText( resourceName, "'resourceName'不能为null或空串");
		return resourceName;
	}

	@Override
	public void setResourceName(String name) {
		this.resourceName = name;
	}

	@Override
	public String getExtension() {
		if (extension == null) {
			extension = StringUtils.tokenizeToStringArray(fileName, ".",null)[1];
		}
		return extension;
	}

	@Override
	public void setExtension(String extension) {
		this.extension = extension;
	}

	@Override
	public InputStream getInputStream(boolean reset) {
		InputStream inputStream = steamStore.getInputStream();
		return  reset ? IOUtils.reset(inputStream, 0) : inputStream;
	}

	@Override
	public BufferedInputStream getBufferedInputStream(boolean reset) {
		BufferedInputStream bufferedInputStream = steamStore.getBufferedInputStream();
		return reset ? (BufferedInputStream) IOUtils.reset(bufferedInputStream, 0) : bufferedInputStream;
	}

	@Override
	public InputStreamReader getInputStreamReader(boolean reset) {
		InputStreamReader inputStreamReader = steamStore.getInputStreamReader();
		return reset ? (InputStreamReader) IOUtils.reset(inputStreamReader, 0) : inputStreamReader;
	}

	@Override
	public Reader getReader(boolean reset) {
		Reader reader = steamStore.getReader();
		return reset ? IOUtils.reset(reader, 0) : reader;
	}

	@Override
	public BufferedReader getBufferedReader(boolean reset) {
		BufferedReader bufferedReader = steamStore.getBufferedReader();
		return reset ? (BufferedReader) IOUtils.reset(bufferedReader, 0) : bufferedReader;
	}

	@Override
	public OutputStream getOutputStream() {
		return steamStore.getOutputStream();
	}

	@Override
	public BufferedOutputStream getBufferedOutputStream() {
		return steamStore.getBufferedOutputStream();
	}

	@Override
	public OutputStreamWriter getOutputStreamWriter() {
		return steamStore.getOutputStreamWriter();
	}

	@Override
	public Writer getWriter() {
		return steamStore.getWriter();
	}

	@Override
	public BufferedWriter getBufferedWriter() {
		return steamStore.getBufferedWriter();
	}

}
