package org.zy.fluorite.core.io;

import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @DateTime 2020年6月26日 下午1:58:16;
 * @author zy(azurite-Y);
 * @Description 存储多个键值对，且值可有多个。<br/>
 * 如：a.txt文件中有一键值对 ['a'=b,c,d]。<br/>
 * 那么a.txt就是本对象的fileName属性，key为a，value为List集合存储[b,c,d]<br/>
 */
@SuppressWarnings("serial")
public class MetaFileAttribute extends LinkedHashMap<String ,List<String>> {
	/** 文件名称 */
	private final String fileName;

	private final URL url;
	
	public MetaFileAttribute(URL url) {
		super();
		this.url = url;
		this.fileName = url.getPath();
	}

	public void add(String key ,String value) {
		List<String> list = super.get(key);
		list = (list == null ? new ArrayList<>() : list);
		list.add(value);
		super.put(key, list);
	}

	public List<String> get(String key) {
		return super.get(key);
	}
	
	public String getFileName() {
		return fileName;
	}
	public URL getUrl() {
		return url;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((fileName == null) ? 0 : fileName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		MetaFileAttribute other = (MetaFileAttribute) obj;
		if (fileName == null) {
			if (other.fileName != null)
				return false;
		} else if (!fileName.equals(other.fileName))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "MetaFileAttribute [fileName=" + fileName +" {"+super.toString()+ "}]";
	}
	
	
}
