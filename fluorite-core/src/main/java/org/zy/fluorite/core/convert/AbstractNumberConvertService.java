package org.zy.fluorite.core.convert;

import java.util.ArrayList;
import java.util.List;

import org.zy.fluorite.core.interfaces.ConversionService;

/**
 * @DateTime 2020年7月2日 上午12:35:36;
 * @author zy(azurite-Y);
 * @param <S>
 * @Description 目标类型是基本数据类型的转换器基类
 */
public abstract class AbstractNumberConvertService<S> implements ConversionService<S,Number> {
	protected final List<Class<? extends Number>> list = new ArrayList<>();
	public AbstractNumberConvertService() {
		list.add(byte.class);
		list.add(Byte.class);
		list.add(short.class);
		list.add(Short.class);
		list.add(int.class);
		list.add(Integer.class);
		list.add(double.class);
		list.add(Double.class);
		list.add(float.class);
		list.add(Float.class);
		list.add(long.class);
		list.add(Long.class);
	}
}
