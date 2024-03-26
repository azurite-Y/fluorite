package org.zy.fluorite.core.utils;

import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import org.zy.fluorite.core.interfaces.PathMatcher;

/**
 * @dateTime 2022年12月23日;
 * @author zy(azurite-Y);
 * @description
 */
public class AntPathMatcher implements PathMatcher{
	/**
	 * 默认路径分隔符: "/"
	 */
	public static final String DEFAULT_PATH_SEPARATOR = "/";
	
	private final String SINGLE_VALUE_MATCHING = "(?:([a-zA-Z0-9.]))";
	private final String MULTIVALUE_MATCHING = "(?:([a-zA-Z0-9.]+))";
	private final String MULTI_STOREY_MATCHING = "(?:([a-zA-Z0-9./]+))";
	
	private final Map<String, Pattern> tokenizedPatternCache = new ConcurrentHashMap<>(256);

	/**
	 * ？：匹配一个字符
	 */
	private final char ONE_CHAR = '?';
	/**
	 * *：匹配零个或多个字符
	 */
	private final char MORE_CHAR = '*';
	
	/**
	 * **：匹配路径中的零个或多个目录
	 */
//	private final String MORE_PATH = "**";
	
	
	@Override
	public boolean isPattern(String path) {
		if (path == null) {
			return false;
		}
		boolean uriVar = false;
		for (int i = 0; i < path.length(); i++) {
			char c = path.charAt(i);
			if (c == '*' || c == '?') {
				return true;
			}
			if (c == '{') {
				uriVar = true;
				continue;
			}
			if (c == '}' && uriVar) {
				return true;
			}
		}
		return false;
	}


	@Override
	public boolean match(String patternStr, String path) {
		Pattern pattern = this.tokenizedPatternCache.get(patternStr);
		if (pattern != null) {
			return pattern.matcher(path).matches();
		}
		
		String patternStrTemp = patternStr;
		
		StringBuilder builder = new StringBuilder();
		// 对 " **/*Tests.class " 这类的特殊处理
		String temp = "**/*";
		if (patternStr.length() > temp.length()) {
			int indexOf = patternStr.indexOf(temp);
			if (indexOf != -1) {
				patternStrTemp = patternStr.substring(indexOf + 4);
				builder.append(MULTI_STOREY_MATCHING).append("/").append(MULTIVALUE_MATCHING);
			}
		}
		
		char[] charArray = patternStrTemp.toCharArray();
		int length = charArray.length - 1;
		for (int i = 0; i <= length; i++) {
			char c = charArray[i];
			if (c == ONE_CHAR) {
				builder.append(SINGLE_VALUE_MATCHING);
			} else if (c == MORE_CHAR) {
				if (length != i && charArray[i + 1] == MORE_CHAR) {
					builder.append(MULTI_STOREY_MATCHING);
					break;
				}
				builder.append(MULTIVALUE_MATCHING);
			} else {
				builder.append(c);
			}
		}
		
		pattern = Pattern.compile(builder.toString());
		tokenizedPatternCache.put(patternStr, pattern);
		return pattern.matcher(path).matches();
	}


	@Override
	public boolean matchStart(String pattern, String path) {
		// TODO 自动生成的方法存根
		return false;
	}


	@Override
	public String extractPathWithinPattern(String pattern, String path) {
		// TODO 自动生成的方法存根
		return null;
	}


	@Override
	public Map<String, String> extractUriTemplateVariables(String pattern, String path) {
		// TODO 自动生成的方法存根
		return null;
	}


	@Override
	public Comparator<String> getPatternComparator(String path) {
		// TODO 自动生成的方法存根
		return null;
	}


	@Override
	public String combine(String pattern1, String pattern2) {
		// TODO 自动生成的方法存根
		return null;
	}
}
