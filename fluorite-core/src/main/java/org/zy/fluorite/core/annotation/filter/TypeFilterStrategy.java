package org.zy.fluorite.core.annotation.filter;

import java.util.ArrayList;
import java.util.List;

import org.zy.fluorite.core.interfaces.AnnotationMetadata;
import org.zy.fluorite.core.interfaces.Resource;
import org.zy.fluorite.core.interfaces.TypeFilter;

/**
 * @DateTime 2020年6月22日 下午2:23:36;
 * @author zy(azurite-Y);
 * @Description TypeFilter实现类调用策略
 */
@SuppressWarnings("serial")
public class TypeFilterStrategy extends ArrayList<TypeFilter> {

	private List<FilterType> types = new ArrayList<>();

	/**
	 * 添加TypeFilter实现
	 * 
	 * @param typeFilter
	 * @return
	 */
	public void add(FilterType filterType, TypeFilter typeFilter) {
		this.types.add(filterType);
		this.add(typeFilter);
	}

	/**
	 * 调用TypeFilter实现类调用策略
	 * 
	 * @param resource  - 扫描到的资源对象
	 * @param searchClz - 此文件名转换Class对象，作为参数传递时为null
	 * @return false代表忽略当前扫描结果
	 */
	public boolean matcher(Resource resource, AnnotationMetadata matadata) {
		FilterType type = null;
		TypeFilter filter = null;
		for (int i = 0; i < types.size(); i++) {
			type = types.get(i);
			filter = this.get(i);

			if (type == FilterType.ASPECTJ || type == FilterType.REGEX) {
				if (filter.match(resource.getFileName(), null)) {
					return false;
				}
			} else {
				if (filter.match(matadata, null)) {
					return false;
				}
			}
		}
		return true;
	}
}
