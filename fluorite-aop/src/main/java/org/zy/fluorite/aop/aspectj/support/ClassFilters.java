package org.zy.fluorite.aop.aspectj.support;

import java.io.Serializable;
import java.util.Arrays;

import org.zy.fluorite.aop.interfaces.function.ClassFilter;
import org.zy.fluorite.core.utils.Assert;

/**
 * @DateTime 2020年7月9日 上午11:42:18;
 * @author zy(azurite-Y);
 * @Description 用于组合类过滤器的静态实用方法。
 */
public abstract class ClassFilters {

	public static ClassFilter union(ClassFilter cf1, ClassFilter cf2) {
		Assert.notNull(cf1, "第一个ClassFilter不能为null");
		Assert.notNull(cf2, "第二个ClassFilter不能为null");
		return new UnionClassFilter(new ClassFilter[] {cf1, cf2});
	}
	public static ClassFilter union(ClassFilter[] classFilters) {
		Assert.notNull(classFilters, "ClassFilter数组不能为null或空集");
		return new UnionClassFilter(classFilters);
	}
	
	public static ClassFilter intersection(ClassFilter cf1, ClassFilter cf2) {
		Assert.notNull(cf1, "第一个ClassFilter不能为null");
		Assert.notNull(cf2, "第二个ClassFilter不能为null");
		return new IntersectionClassFilter(new ClassFilter[] {cf1, cf2});
	}
	public static ClassFilter intersection(ClassFilter[] classFilters) {
		Assert.notNull(classFilters, "ClassFilter数组不能为null或空集");
		return new IntersectionClassFilter(classFilters);
	}

	/** */
	@SuppressWarnings("serial")
	private static class UnionClassFilter implements ClassFilter, Serializable {

		private final ClassFilter[] filters;

		UnionClassFilter(ClassFilter[] filters) {
			this.filters = filters;
		}

		@Override
		public boolean matches(Class<?> clazz) {
			for (ClassFilter filter : this.filters) {
				if (filter.matches(clazz)) {
					return true;
				}
			}
			return false;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + Arrays.hashCode(filters);
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			UnionClassFilter other = (UnionClassFilter) obj;
			if (!Arrays.equals(filters, other.filters))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return getClass().getName() + ": " + Arrays.toString(this.filters);
		}
	}
	
	@SuppressWarnings("serial")
	private static class IntersectionClassFilter implements ClassFilter, Serializable {
		private final ClassFilter[] filters;

		IntersectionClassFilter(ClassFilter[] filters) {
			this.filters = filters;
		}

		@Override
		public boolean matches(Class<?> clazz) {
			for (ClassFilter filter : this.filters) {
				if (!filter.matches(clazz)) {
					return false;
				}
			}
			return true;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			IntersectionClassFilter other = (IntersectionClassFilter) obj;
			if (!Arrays.equals(filters, other.filters))
				return false;
			return true;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + Arrays.hashCode(filters);
			return result;
		}

		@Override
		public String toString() {
			return getClass().getName() + ": " + Arrays.toString(this.filters);
		}

	}
}
