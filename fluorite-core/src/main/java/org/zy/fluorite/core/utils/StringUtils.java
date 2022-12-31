package org.zy.fluorite.core.utils;

import static java.util.Locale.ENGLISH;

import java.beans.Introspector;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

import org.zy.fluorite.core.interfaces.function.InvorkFunction;

/**
 * @author: zy(azurite-Y);
 * @DateTime: 2020年6月5日 上午1:02:54;
 * @Description
 */
public class StringUtils {
//	private static final String[] EMPTY_STRING_ARRAY = {};
    /**
     * 'A'.
     */
    public static final byte A = (byte) 'A';

    /**
     * 'a'.
     */
    public static final byte a = (byte) 'a';

    /**
     * 'Z'.
     */
    public static final byte Z = (byte) 'Z';
    
    /**
     * 大写字节的极小值
     */
    public static final byte uppercaseByteMin = A - 1;
    
    /**
     * 大写字节数的极大值
     */
    public static final byte uppercaseByteMax = Z + 1;
    
    /**
     * 转换小写字节偏移量
     */
    public static final byte LC_OFFSET = A - a;

	public static String[] toStringArray(Collection<String> collection) {
		return (collection != null ? collection.toArray(new String[0]) : new String[0]);
	}

	/**
	 * 根据分隔符将指定字符串分割，将分割内容保存到字符串数组中并返回
	 * 
	 * @param str - 将要分割的字符串
	 * @param delimiters - 分割符
	 * @return
	 */
	public static String[] tokenizeToStringArray(String str, String delimiters) {
		return tokenizeToStringArray(str, delimiters, true, true, null);
	}
	
	/**
	 * 根据分隔符将指定字符串分割，将分割内容保存到字符串容器中并返回
	 * 
	 * @param str - 将要分割的字符串
	 * @param delimiters - 分割符
	 * @return
	 */
	public static List<String> tokenizeToStringList(String str, String delimiters) {
		return tokenizeToStringList(str, delimiters, true, true, null);
	}
	
	/**
	 * 根据分隔符将指定字符串分割，将分割内容保存到字符串数组中并返回
	 * 
	 * @param str - 将要分割的字符串
	 * @param delimiters - 分割符
	 * @return
	 */
	public static String[] tokenizeToStringArray(String str, String delimiters,InvorkFunction<String> function) {
		return tokenizeToStringArray(str, delimiters, true, true, function);
	}
	
	/**
	 * 根据分隔符将指定字符串分割，将分割内容保存到字符串容器中并返回
	 * 
	 * @param str - 将要分割的字符串
	 * @param delimiters - 分割符
	 * @return
	 */
	public static List<String> tokenizeToStringList(String str, String delimiters,InvorkFunction<String> function) {
		return tokenizeToStringList(str, delimiters, true, true, function);
	}

	/**
	 * 根据分隔符将指定字符串分割，返回存储字符串数组
	 * @param str - 将要分割的字符串
	 * @param delimiters - 分割符
	 * @param trimTokens - 是否截去字符串两端空白字符串
	 * @param ignoreEmptyTokens - 是否忽略空白字符串
	 * @return
	 */
	public static String[] tokenizeToStringArray(String str, String delimiters,
			boolean trimTokens, boolean ignoreEmptyTokens,InvorkFunction<String> function) {
		return toStringArray(tokenizeToStringList(str, delimiters, trimTokens, ignoreEmptyTokens, function));
	}

	/**
	 * 根据分隔符将指定字符串分割，返回存储字符串容器
	 * @param str - 将要分割的字符串
	 * @param delimiters - 分割符
	 * @param trimTokens - 是否截去字符串两端空白字符串
	 * @param ignoreEmptyTokens - 是否忽略空白字符串
	 * @return
	 */
	public static List<String> tokenizeToStringList(String str, String delimiters,
			boolean trimTokens, boolean ignoreEmptyTokens,InvorkFunction<String> function) {

		if (str == null) {
			return Collections.emptyList();
		}

		StringTokenizer st = new StringTokenizer(str, delimiters);
		List<String> tokens = new ArrayList<>();
		while (st.hasMoreTokens()) {
			String token = st.nextToken();
			if (trimTokens) {
				token = token.trim();
			}
			if (!ignoreEmptyTokens || token.length() > 0) {
				if (function != null) function.invork(token);
				tokens.add(token);
			}
		}
		return tokens;
	}
	
	
	/**
	 * 字符串拼接
	 * @param separator - 分隔符
	 * @param strings - 拼接的字符串数组
	 * @return
	 */
	public static String append(String separator,Object... strings) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < strings.length; i++) {
			builder.append(strings[i]);
			if (i < strings.length - 1) {
				builder.append(separator);
			}
		}
		return builder.toString();
	}

	/**
	 * 字符串拼接
	 * @param separator - 分隔符
	 * @param strings - 拼接的字符串数组
	 * @return
	 */
	public static String append(String separator,String[] strings) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < strings.length; i++) {
			builder.append(strings[i]);
			if (i < strings.length - 1) {
				builder.append(separator);
			}
		}
		return builder.toString();
	}
	
	/**
	 * 首字母小写
	 * @param name
	 * @return
	 */
	public static String initialLowerCase(String name) {
		return Introspector.decapitalize(name);
	}

	/**
	 * 首字母大写
	 */
	public static String capitalize(String name) {
		if (name == null || name.length() == 0) {
			return name;
		}
		return name.substring(0, 1).toUpperCase(ENGLISH) + name.substring(1);
	}
	
	/**
	 * 将驼峰式命名转为指定分隔符的字符串
	 * @param fieldName
	 * @return
	 */
	public static String createPropertiesName(String fieldName, String separator) {
		StringBuilder builder = new StringBuilder();

		for (byte readData : fieldName.getBytes()) {
			if (readData >= uppercaseByteMin && readData <= uppercaseByteMax) {
				// 大小转小写
				readData -= LC_OFFSET;
				builder.append(separator);
			}
			builder.append((char)readData);
		}
		return builder.toString();
	}
	
	/**
	 * 指定值是否JSON字符串
	 * 
	 * @param str
	 * @return true则为是
	 */
	public static boolean isJSONValue(String str) {
		boolean result = false;
		if (Assert.hasText(str)) {
			str = str.trim();
			if (str.startsWith("{") && str.endsWith("}")) {
				result = true;
			} else if (str.startsWith("[") && str.endsWith("]")) {
				result = true;
			}
		}
		return result;
	}
}
