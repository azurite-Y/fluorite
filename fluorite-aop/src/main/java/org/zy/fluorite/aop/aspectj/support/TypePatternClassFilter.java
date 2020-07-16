package org.zy.fluorite.aop.aspectj.support;

import org.zy.fluorite.aop.interfaces.function.ClassFilter;
import org.zy.fluorite.core.utils.Assert;
import org.zy.fluorite.core.utils.ReflectionUtils;

/**
 * @DateTime 2020年7月7日 下午3:49:27;
 * @author zy(azurite-Y);
 * @Description
 */
public class TypePatternClassFilter implements ClassFilter {
	private String typePattern;
	
	/**
	 * @param typePattern - 形如：com.zy.aop.UserServiceImpl或com.zy.aop.com.zy.aop.UserServiceImpl+的字符串
	 */
	public TypePatternClassFilter(String typePattern) {
		Assert.hasText(typePattern,"'typePattern'不能为null");
		this.typePattern = typePattern;
	}
	
	public String getTypePattern() {
		return typePattern;
	}
	public void setTypePattern(String typePattern) {
		this.typePattern = typePattern;
	}

	@Override
	public boolean matches(Class<?> clazz) {
		int indexOf = this.typePattern.indexOf("+");
		Class<?> forName = null;
		if (indexOf != -1 ) { // typePattern携带“+”
			// 截去"+"
			this.typePattern = this.typePattern.substring(0 , indexOf);
			forName = ReflectionUtils.forName(typePattern);
			return forName.isAssignableFrom(clazz);
		}
		// 未携带"+"则严格的匹配是否就是它自身
		forName = ReflectionUtils.forName(typePattern);
		return forName.equals(clazz);
	}

}
