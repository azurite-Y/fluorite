package org.zy.fluorite.core.interfaces.function;

import java.util.Iterator;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.zy.fluorite.core.exception.BeansException;

/**
 * @DateTime 2021年9月14日;
 * @author zy(azurite-Y);
 * @Description ObjectFactory的一个变体，专门为注入点设计，允许编程可选性和宽松而非唯一的处理，提供了延时依赖注入的方法
 * @param <T> 实例化bean类型
 */
public interface ObjectProvider<T> extends ObjectFactory<T>, Iterable<T> {
	
	/**
	 * 返回指定类型的bean, 如果容器中不存在, 抛出NoSuchBeanDefinitionException异常.如果容器中有多个此类型的bean, 抛出NoUniqueBeanDefinitionException异常
	 * @param args
	 * @return
	 * @throws BeansException
	 */
	T getObject(Object... args) throws BeansException;

	/**
	 * 如果指定类型的bean注册到容器中, 返回 bean 实例, 否则返回 null
	 * @return
	 * @throws BeansException
	 */
	T getIfAvailable() throws BeansException;

	/**
	 * 如果返回对象不存在，则进行回调，回调对象由Supplier传入
	 * @param defaultSupplier
	 * @return
	 * @throws BeansException
	 */
	default T getIfAvailable(Supplier<T> defaultSupplier) throws BeansException {
		T dependency = getIfAvailable();
		return (dependency != null ? dependency : defaultSupplier.get());
	}

	/**
	 * 消费对象的一个实例（可能是共享的或独立的），如果存在通过Consumer回调消耗目标对象
	 * @param dependencyConsumer
	 * @throws BeansException
	 */
	default void ifAvailable(Consumer<T> dependencyConsumer) throws BeansException {
		T dependency = getIfAvailable();
		if (dependency != null) {
			dependencyConsumer.accept(dependency);
		}
	}

	/**
	 * 如果不可用或不唯一（没有指定primary）则返回null。否则，返回对象
	 * @return
	 * @throws BeansException
	 */
	T getIfUnique() throws BeansException;

	/**
	 * 如果存在唯一对象，则调用Supplier的回调函数
	 * @param defaultSupplier
	 * @return
	 * @throws BeansException
	 */
	default T getIfUnique(Supplier<T> defaultSupplier) throws BeansException {
		T dependency = getIfUnique();
		return (dependency != null ? dependency : defaultSupplier.get());
	}

	/**
	 * 返回符合条件的对象的Iterator，没有特殊顺序保证（一般为注册顺序）
	 * @param dependencyConsumer
	 * @throws BeansException
	 */
	default void ifUnique(Consumer<T> dependencyConsumer) throws BeansException {
		T dependency = getIfUnique();
		if (dependency != null) {
			dependencyConsumer.accept(dependency);
		}
	}

	@Override
	default Iterator<T> iterator() {
		return stream().iterator();
	}

	/**
	 * 返回符合条件对象的连续的Stream，没有特殊顺序保证（一般为注册顺序）
	 * @return
	 */
	default Stream<T> stream() {
		throw new UnsupportedOperationException("不支持多元素访问");
	}

	default Stream<T> orderedStream() {
		throw new UnsupportedOperationException("不支持有序元素访问");
	}
}
