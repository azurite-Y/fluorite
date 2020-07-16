package org.zy.fluorite.core.interfaces;

/**
 * @DateTime 2020年6月27日 下午3:32:33;
 * @author zy(azurite-Y);
 * @Description
 */
@FunctionalInterface
public interface AnalysisReporter<T> {
	
	void report(T t);
	
}
