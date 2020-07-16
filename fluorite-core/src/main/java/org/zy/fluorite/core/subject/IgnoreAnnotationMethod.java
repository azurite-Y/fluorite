package org.zy.fluorite.core.subject;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.zy.fluorite.core.interfaces.MethodFilter;

/**
 * @author: zy(azurite-Y);
 * @DateTime: 2020年6月5日 下午4:02:56;
 * @Description 过滤toString、equals、hashCode、annotationType、wait、getClass、notify、notifyAll方法
 */
public class IgnoreAnnotationMethod implements MethodFilter {
	private final String TO_STRING = "toString";
	private final String EQUALS = "equals";
	private final String HASH_CODE = "hashCode";
	private final String ANNOTATION_TYPE = "annotationType";
	private final String WAIT = "wait";
	private final String GET_CLASS = "getClass";
	private final String NOTIFY = "notify";
	private final String NOTIFY_ALL = "notifyAll";
	
	@Override
	public List<Method> matcher(List<Method> methods) {
		List<Method> list = new ArrayList<>();
		for (Method method : methods) {
			if (! isIgnore(method.getName()) ) {
				list.add(method);
			}
		}
		return list;
	}

	@Override
	public List<Method> matcher(Method[] methods) {
		List<Method> list = new ArrayList<>();
		for (Method method : methods) {
			if (! isIgnore(method.getName()) ) {
				list.add(method);
			}
		}
		return list;
	}

	private boolean isIgnore(String methodName)  {
		switch(methodName) {
			case TO_STRING : return true;
			case EQUALS : return true;
			case HASH_CODE : return true;
			case ANNOTATION_TYPE : return true;
			case WAIT : return true;
			case GET_CLASS : return true;
			case NOTIFY : return true;
			case NOTIFY_ALL : return true;
			default: return false;
		}
		
	}
	
}
