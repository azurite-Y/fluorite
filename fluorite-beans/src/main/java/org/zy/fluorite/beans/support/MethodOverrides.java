package org.zy.fluorite.beans.support;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @author: zy(azurite-Y);
 * @DateTime: 2020年6月5日 上午12:15:21;
 * @Description 重写方法持有者
 */
public class MethodOverrides {
	private final Set<MethodOverride> overrides = new CopyOnWriteArraySet<>();
	
	public MethodOverrides() {
	}

	public MethodOverrides(MethodOverrides other) {
		addOverrides(other);
	}


	public void addOverrides(MethodOverrides other) {
		if (other != null) {
			this.overrides.addAll(other.overrides);
		}
	}

	public void addOverride(MethodOverride override) {
		this.overrides.add(override);
	}

	public Set<MethodOverride> getOverrides() {
		return this.overrides;
	}

	public boolean isEmpty() {
		return this.overrides.isEmpty();
	}

	public MethodOverride getOverride(Method method) {
		MethodOverride match = null;
		for (MethodOverride candidate : this.overrides) {
			if (candidate.matches(method)) {
				match = candidate;
			}
		}
		return match;
	}

}
