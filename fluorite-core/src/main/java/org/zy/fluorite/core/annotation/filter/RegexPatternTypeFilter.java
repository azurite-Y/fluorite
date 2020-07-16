package org.zy.fluorite.core.annotation.filter;

import java.lang.annotation.Annotation;
import java.util.regex.Pattern;

import org.zy.fluorite.core.annotation.ComponentScan.Filter;
import org.zy.fluorite.core.interfaces.AnnotationMetadata;
import org.zy.fluorite.core.interfaces.TypeFilter;

/**
 * @DateTime 2020年6月22日 上午9:38:56;
 * @author zy(azurite-Y);
 * @Description 按正则表达式匹配
 */
public class RegexPatternTypeFilter implements TypeFilter {
	private Pattern pattern;
	
	
	public RegexPatternTypeFilter() {
		super();
	}
	public RegexPatternTypeFilter(String pattern) {
		super();
		this.pattern = Pattern.compile(pattern);
	}
	public RegexPatternTypeFilter(Pattern pattern) {
		super();
		this.pattern = pattern;
	}
	/**
	 * 按正则表达式匹配文件名
	 */
	@Override
	public boolean match(String fileName, Filter filter) {
		if (pattern == null) {
			return false;
		}
		return this.pattern.matcher(fileName).matches();
	}

	@Override
	public boolean match(AnnotationMetadata metadata, Filter filter) {
		return false;
	}

	@Override
	public void invorkAware(String pattern, Class<? extends Annotation>[] annos, Class<?> source) {
		this.pattern = Pattern.compile(pattern);
	}
}
