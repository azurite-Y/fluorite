package org.zy.fluorite.core.utils;

import static java.util.Locale.ENGLISH;

import java.beans.Introspector;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;

import org.zy.fluorite.core.interfaces.function.InvorkFunction;

/**
 * @author: zy(azurite-Y);
 * @DateTime: 2020年6月5日 上午1:02:54;
 * @Description
 */
public class StringUtils {

	private static final String[] EMPTY_STRING_ARRAY = {};

	public static String[] toStringArray(Collection<String> collection) {
		return (collection != null ? collection.toArray(new String[0]) : new String[0]);
	}

	/**
	 * 根据分隔符将指定字符串分割，将分割内容保存到字符串数组中并返回
	 * @param str
	 * @param delimiters
	 * @return
	 */
	public static String[] tokenizeToStringArray(String str, String delimiters,InvorkFunction<String> function) {
		return tokenizeToStringArray(str, delimiters, true, true,function);
	}

	/**
	 * 根据分隔符将指定字符串分割，返回字符串数组
	 * @param str - 将要分割的字符串
	 * @param delimiters - 分割符
	 * @param trimTokens - 是否截去字符串两端空白字符串
	 * @param ignoreEmptyTokens - 是否忽略空白字符串
	 * @return
	 */
	public static String[] tokenizeToStringArray(String str, String delimiters,
			boolean trimTokens, boolean ignoreEmptyTokens,InvorkFunction<String> function) {

		if (str == null) {
			return EMPTY_STRING_ARRAY;
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
		return toStringArray(tokens);
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
}
