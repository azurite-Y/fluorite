package org.zy.fluorite.core.utils;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * @dateTime 2022年12月9日;
 * @author zy(azurite-Y);
 * @description
 */
public class PropertyMapper {
	private static final Predicate<?> ALWAYS = (t) -> true;
	private static final PropertyMapper INSTANCE = new PropertyMapper(null, null);

	private final PropertyMapper parent;

	private final SourceOperator sourceOperator;
	
	
	private PropertyMapper(PropertyMapper parent, SourceOperator sourceOperator) {
		this.parent = parent;
		this.sourceOperator = sourceOperator;
	}
	
	
	
	// -------------------------------------------------------------------------------------
	// 方法
	// -------------------------------------------------------------------------------------
	/**
	 * 返回属性映射器
	 * 
	 * @return 属性映射器
	 */
	public static PropertyMapper get() {
		return INSTANCE;
	}
	
	/**
	 * 返回一个新的 {@link PropertyMapper} 实例，将 {@link Source#whenNonNull() whenNonNull} 应用到每个源。
	 * 
	 * @return 一个新的属性映射器实例
	 */
	public PropertyMapper alwaysApplyingWhenNonNull() {
		return alwaysApplying(this::whenNonNull);
	}

	private <T> Source<T> whenNonNull(Source<T> source) {
		return source.whenNonNull();
	}

	/**
	 * 返回一个新的 {@link PropertyMapper} 实例，该实例将给定的 {@link SourceOperator} 应用到每个源。
	 * 
	 * @param operator - 要以应用于 Source 的操作
	 * @return 一个新的属性映射器实例
	 */
	public PropertyMapper alwaysApplying(SourceOperator operator) {
		Assert.notNull(operator, "Operator 不能为 null");
		return new PropertyMapper(this, operator);
	}
	
	/**
	 * 从指定的值返回一个可用于执行映射的新 {@link Source}
	 * 
	 * @param <T> - source 类型
	 * @param value - source 值
	 * @return 一个可以用来完成映射的 @link Source}
	 */
	public <T> Source<T> from(T value) {
		return from(() -> value);
	}
	
	/**
	 * 从指定的供给值返回一个新的 {@link Source}，该 {@link Source}可用于执行映射。
	 * 
	 * @param <T> - source 类型
	 * @param supplier - 供给值
	 * @return 可用于完成映射的 {@link Source}
	 * @see #from(Object)
	 */
	public <T> Source<T> from(Supplier<T> supplier) {
		Assert.notNull(supplier, "Supplier must not be null");
		Source<T> source = getSource(supplier);
		if (this.sourceOperator != null) {
			source = this.sourceOperator.apply(source);
		}
		return source;
	}
	
	@SuppressWarnings("unchecked")
	private <T> Source<T> getSource(Supplier<T> supplier) {
		if (this.parent != null) {
			return this.parent.from(supplier);
		}
		return new Source<>(new CachingSupplier<>(supplier), (Predicate<T>) ALWAYS);
	}

	
	
	
	// -------------------------------------------------------------------------------------
	// 内部类
	// -------------------------------------------------------------------------------------
	/**
	 * 供应商，该供应商缓存该值以防止多次调用。
	 */
	private static class CachingSupplier<T> implements Supplier<T> {
		private final Supplier<T> supplier;

		private boolean hasResult;

		private T result;

		CachingSupplier(Supplier<T> supplier) {
			this.supplier = supplier;
		}

		@Override
		public T get() {
			if (!this.hasResult) {
				this.result = this.supplier.get();
				this.hasResult = true;
			}
			return this.result;
		}

	}
	
	/**
	 * 正在被映射的源
	 *
	 * @param <T> - 源对象类型
	 */
	public static final class Source<T> {
		private final Supplier<T> supplier;

		private final Predicate<T> predicate;

		
		private Source(Supplier<T> supplier, Predicate<T> predicate) {
			Assert.notNull(predicate, "Predicate 不能为 null");
			this.supplier = supplier;
			this.predicate = predicate;
		}
		
		
		/**
		 * 返回通过给定适配器函数更改的源的改编版本，若 {@link #predicate } 断言结果为true则执行 adapter 参数的逻辑
		 * 
		 * @param <R> - 结果类型
		 * @param adapter - 要应用的适配器
		 * @return 一个新的经过调整的源实例
		 */
		public <R> Source<R> as(Function<T, R> adapter) {
			Assert.notNull(adapter, "Adapter 不能为 null");
			Supplier<Boolean> test = () -> this.predicate.test(this.supplier.get());
			
			// predicate 保存"this.predicate"对"this.supplier.get()"的断言逻辑方法
			Predicate<R> predicate = (t) -> test.get();
			// supplier 在原有的 predicate 断言结果为true则执行 adapter 参数的逻辑方法
			Supplier<R> supplier = () -> {
				if (test.get()) {
					return adapter.apply(this.supplier.get());
				}
				return null;
			};
			return new Source<>(supplier, predicate);
		}
		
		/**
		 * 返回具有 {@link Integer} 类型的源的改编版本
		 * 
		 * @param <T> - 参数类型
		 * @param <R> - 结果类型
		 * @param adapter - 将当前值转换为数字的适配器
		 * @return 一个新的经过调整的源实例
		 * 
		 */
		public <R extends Number> Source<Integer> asInt(Function<T, R> adapter) {
			/**
			 * intValue(): 以整型形式返回指定数字的值，该值可能涉及舍入或截断。
			 * @return 该对象在转换为int类型后表示的数值。
			 */
			return as(adapter).as(Number::intValue);
		}
		
		/**
		 * 返回源的过滤版本，该版本不会映射与给定谓词不匹配的值。
		 * 
		 * @param predicate - 用于筛选值的谓词（逻辑true或false）
		 * @return 新的筛选源实例
		 */
		public Source<T> when(Predicate<T> predicate) {
			Assert.notNull(predicate, "Predicate 不能为 null");
			return new Source<>(this.supplier, (this.predicate != null) ? this.predicate.and(predicate) : predicate);
		}
		
		/**
		 * 返回源的过滤版本，该版本不会映射非空值或引发 {@link NullPointerException} 的 suppliers
		 * 
		 * @return 一个新的过滤源实例
		 */
		public Source<T> whenNonNull() {
			return new Source<>(new NullPointerExceptionSafeSupplier<>(this.supplier), Objects::nonNull);
		}

		/**
		 * 返回源的过滤版本，该版本只映射为true的值
		 * 
		 * @return 一个新的过滤源实例
		 */
		public Source<T> whenTrue() {
			return when(Boolean.TRUE::equals);
		}

		/**
		 * 返回源的过滤版本，只映射为false的值
		 * 
		 * @return 一个新的过滤源实例
		 */
		public Source<T> whenFalse() {
			return when(Boolean.FALSE::equals);
		}

		/**
		 * 返回源的过滤版本，该版本只会映射包含实际文本的 {@code toString()} 值
		 * 
		 * @return 一个新的过滤源实例
		 */
		public Source<T> whenHasText() {
			return when( (value) -> Assert.hasText(Objects.toString(value, null)) );
		}

		/**
		 * 返回源的过滤版本，该版本只映射与指定对象相等的值
		 * 
		 * @param object - 要匹配的对象
		 * @return 一个新的过滤源实例
		 */
		public Source<T> whenEqualTo(Object object) {
			return when(object::equals);
		}

		/**
		 * 返回源的过滤版本，该版本将仅映射属于给定类型实例的值
		 * 
		 * @param <R> - 目标类型
		 * @param target - 要匹配的目标类型
		 * @return 新的筛选源实例
		 */
		public <R extends T> Source<R> whenInstanceOf(Class<R> target) {
			/**
			 * isInstance(Object): 如果指定的Object参数为非null，并且可以在不超过ClassCastException的情况下转换为该Class对象表示的引用类型，则该方法返回true。否则返回false。
			 * cast(Object): 将对象强制转换到此class对象表示的类或接口
			 */
			return when(target::isInstance).as(target::cast);
		}

		/**
		 * 返回源的过滤版本，该版本不会映射与给定谓词匹配的值。
		 * 
		 * @param predicate - 用于筛选值的谓词
		 * @return 新的筛选源实例
		 */
		public Source<T> whenNot(Predicate<T> predicate) {
			Assert.notNull(predicate, "Predicate 不能为 null");
			/**
			 * negate(): 返回表示此谓词的逻辑否定的谓词。
			 */
			return when(predicate.negate());
		}
		
		/**
		 * 通过将任何未过滤的值传递给指定的消费者来完成映射
		 * 
		 * @param consumer - 如果该值未被过滤，则应接受该值的消费者
		 */
		public void to(Consumer<T> consumer) {
			Assert.notNull(consumer, "Consumer 不能为 null");
			T value = this.supplier.get();
			if (this.predicate.test(value)) {
				consumer.accept(value);
			}
		}
		
		/**
		 * 通过从未过滤的值创建一个新实例来完成映射
		 * 
		 * @param <R> - 结果类型
		 * @param factory - 用于创建实例的工厂
		 * @return 实例
		 * @throws NoSuchElementException - 如果该值已被过滤
		 */
		public <R> R toInstance(Function<T, R> factory) {
			Assert.notNull(factory, "Factory 不能为 null");
			T value = this.supplier.get();
			if (!this.predicate.test(value)) {
				throw new NoSuchElementException("不存在值");
			}
			return factory.apply(value);
		}

		/**
		 * 在未筛选值时，通过调用指定的方法来完成映射
		 * 
		 * @param runnable - 在未筛选值时调用的方法
		 */
		public void toCall(Runnable runnable) {
			Assert.notNull(runnable, "Runnable 不能为 null");
			T value = this.supplier.get();
			if (this.predicate.test(value)) {
				runnable.run();
			}
		}
		
	}
	
	/**
	 * 将捕获并忽略任何 supplier 引发的 {@link NullPointerException} 
	 */
	private static class NullPointerExceptionSafeSupplier<T> implements Supplier<T> {
		private final Supplier<T> supplier;

		NullPointerExceptionSafeSupplier(Supplier<T> supplier) {
			this.supplier = supplier;
		}

		@Override
		public T get() {
			try {
				return this.supplier.get();
			}
			catch (NullPointerException ex) {
				return null;
			}
		}
	}
	
	/**
	 * 可以应用于 {@link Source} 的操作.
	 */
	@FunctionalInterface
	public interface SourceOperator {
		/**
		 * 将操作应用到给定的源
		 * 
		 * @param <T> - 源类型
		 * @param source - 要操作的源
		 * @return 更新后的源
		 */
		<T> Source<T> apply(Source<T> source);
	}
}
