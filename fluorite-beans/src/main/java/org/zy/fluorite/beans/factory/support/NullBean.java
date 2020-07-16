package org.zy.fluorite.beans.factory.support;

/**
 * @author: zy(azurite-Y);
 * @DateTime: 2020年6月9日 下午1:21:09;
 * @Description
 */
public final class NullBean {
	NullBean() {}


	@Override
	public boolean equals(Object obj) {
		return (this == obj || obj == null);
	}

	@Override
	public int hashCode() {
		return NullBean.class.hashCode();
	}

	@Override
	public String toString() {
		return "null";
	}
}
