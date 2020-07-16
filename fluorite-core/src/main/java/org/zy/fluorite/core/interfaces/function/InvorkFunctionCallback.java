package org.zy.fluorite.core.interfaces.function;

/**
 * @DateTime 2020年6月24日 下午5:48:51;
 * @author zy(azurite-Y);
 * @Description
 */
@FunctionalInterface
public interface InvorkFunctionCallback<P> {
	/**
	 * @param p
	 * @return
	 */
	boolean  invork(P p);
}
