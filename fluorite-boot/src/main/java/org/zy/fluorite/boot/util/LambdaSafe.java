package org.zy.fluorite.boot.util;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.zy.fluorite.core.convert.ResolvableType;
import org.zy.fluorite.core.utils.Assert;

/**
 * @dateTime 2022年12月6日;
 * @author zy(azurite-Y);
 * @description 可用于以安全方式调用lambdas的实用程序。主要设计用于帮助支持泛型类型回调，其中由于类擦除需要处理类强制转换异常。
 */
public final class LambdaSafe {

	private LambdaSafe() {}
	
	
	/**
	 * 开始调用回调实例，处理常见的泛型类型问题和异常。
	 * 
	 * @param callbackType - 回调类型(函数接口)
	 * @param callbackInstances - 回调类型(函数接口)
	 * @param argument - 传递给回调函数的主参数
	 * @param additionalArguments - 传递给回调函数的任何附加参数
	 * @param <C> - 回调类型
	 * @param <A> - 主参数类型
	 * @return 一个可以调用的 {@link Callbacks} 实例
	 */
	public static <C, A> Callbacks<C, A> callbacks(Class<C> callbackType, Collection<? extends C> callbackInstances, A argument, Object... additionalArguments) {
		Assert.notNull(callbackType, "CallbackType 不能为 null");
		Assert.notNull(callbackInstances, "CallbackInstances 不能为 null");
		return new Callbacks<>(callbackType, callbackInstances, argument, additionalArguments);
	}
	
	
	// -------------------------------------------------------------------------------------
	// 内部类
	// -------------------------------------------------------------------------------------
	/**
	 * 表示可以以lambda安全方式调用的回调集合
	 *
	 * @param <C> - 回调类型
	 * @param <A> - 主参数类型
	 */
	public static final class Callbacks<C, A> extends LambdaSafeCallback<C, A, Callbacks<C, A>> {

		/**
		 * 回调对象集合
		 */
		private final Collection<? extends C> callbackInstances;

		private Callbacks(Class<C> callbackType, Collection<? extends C> callbackInstances, A argument, Object[] additionalArguments) {
			super(callbackType, argument, additionalArguments);
			this.callbackInstances = callbackInstances;
		}

		/**
		 * 调用回调方法返回void的回调实例
		 * 
		 * @param invoker - 用于调用回调的调用程序
		 */
		public void invoke(Consumer<C> invoker) {
			this.callbackInstances.forEach(
					(callbackInstance) -> invoke( callbackInstance, () -> {
							invoker.accept(callbackInstance);
							return null;
						} )
					);
		}

		/**
		 * 调用回调方法返回结果的回调实例
		 * 
		 * @param invoker - 用于调用回调的调用程序
		 * @param <R> - 结果类型
		 * @return 调用的结果（如果无法调用回调，则可能是空流）
		 */
//		public <R> Stream<R> invokeAnd(Function<C, R> invoker) {
//			Function<C, InvocationResult<R>> mapper = (callbackInstance) -> invoke(callbackInstance, () -> invoker.apply(callbackInstance));
//			return this.callbackInstances.stream().map(mapper).filter(InvocationResult::hasResult).map(InvocationResult::get);
//		}
	}
	
	/**
	 * 表示可以以lambda安全方式调用的单个回调
	 *
	 * @param <C> - 回调类型
	 * @param <A> - 主参数类型
	 */
	public static final class Callback<C, A> extends LambdaSafeCallback<C, A, Callback<C, A>> {

		private final C callbackInstance;

		private Callback(Class<C> callbackType, C callbackInstance, A argument, Object[] additionalArguments) {
			super(callbackType, argument, additionalArguments);
			this.callbackInstance = callbackInstance;
		}

		/**
		 * 调用回调方法返回空的回调实例
		 * @param invoker - 用于调用回调的调用程序
		 */
		public void invoke(Consumer<C> invoker) {
			invoke(this.callbackInstance, () -> {
				invoker.accept(this.callbackInstance);
				return null;
			});
		}

		/**
		 * Invoke the callback instance where the callback method returns a result.
		 * @param invoker the invoker used to invoke the callback
		 * @param <R> the result type
		 * @return the result of the invocation (may be {@link InvocationResult#noResult}
		 * if the callback was not invoked)
		 */
//		public <R> InvocationResult<R> invokeAnd(Function<C, R> invoker) {
//			return invoke(this.callbackInstance, () -> invoker.apply(this.callbackInstance));
//		}

	}
	
	/**
	 * lambda安全回调的抽象基类
	 *
	 * @param <C> - 回调类型
	 * @param <A> - 主参数类型
	 * @param <SELF> - self类引用
	 */
	protected abstract static class LambdaSafeCallback<C, A, SELF extends LambdaSafeCallback<C, A, SELF>> {

		private final Class<C> callbackType;

		/**
		 * 回调主参数
		 */
		private final A argument;

		/**
		 * 其他附属参数
		 */
		private final Object[] additionalArguments;

//		private Logger logger;

		private Filter<C, A> filter = new GenericTypeFilter<>();

		/**
		 * 
		 * @param callbackType
		 * @param argument
		 * @param additionalArguments
		 */
		LambdaSafeCallback(Class<C> callbackType, A argument, Object[] additionalArguments) {
			this.callbackType = callbackType;
			this.argument = argument;
			this.additionalArguments = additionalArguments;
//			this.logger = LoggerFactory.getLogger(callbackType);
		}
		
		protected final <R> InvocationResult<R> invoke(C callbackInstance, Supplier<R> supplier) {
			// 如果指定的 callbackInstance 是 callbackType 的实现类，且callbackInstance 的泛型和 argument 参数匹配则为true
			if (this.filter.match(this.callbackType, callbackInstance, this.argument, this.additionalArguments)) {
				try {
					return InvocationResult.of(supplier.get());
				}
				catch (ClassCastException ex) {
					throw ex;
				}
			}
			return InvocationResult.noResult();
		}
	}
	
	/**
	 * 回调的结果可能是一个值，如果回调不合适，则该值可能为 {@code null} 或完全不存在。在设计上类似于 {@link Optional}，但允许将 {@code null} 作为有效值。
	 *
	 * @param <R> - 结果类型
	 */
	public static final class InvocationResult<R> {
		private static final InvocationResult<?> NONE = new InvocationResult<>(null);

		private final R value;

		private InvocationResult(R value) {
			this.value = value;
		}

		/**
		 * 如果存在结果，则返回true
		 * 
		 * @return 如果存在结果
		 */
		public boolean hasResult() {
			return this != NONE;
		}

		/**
		 * 返回调用结果，如果回调不可用，则返回null
		 * 
		 * @return 调用的结果或null
		 */
		public R get() {
			return this.value;
		}

		/**
		 * 如果回调不合适，则返回调用的结果或给定的回调的结果。
		 * 
		 * @param fallback - 没有结果时要使用的回退
		 * @return 调用或回退的结果
		 */
		public R get(R fallback) {
			return (this != NONE) ? this.value : fallback;
		}

		/**
		 * 使用指定的值创建新的 {@link InvocationResult} 实例
		 * 
		 * @param value - 值（可以为空）
		 * @param <R> - 结果类型
		 * @return 一个 {@link InvocationResult}
		 */
		public static <R> InvocationResult<R> of(R value) {
			return new InvocationResult<>(value);
		}

		/**
		 * 返回不表示结果的 {@link InvocationResult} 实例
		 * 
		 * @param <R> - 结果类型
		 * @return 一个 {@link InvocationResult}
		 */
		@SuppressWarnings("unchecked")
		public static <R> InvocationResult<R> noResult() {
			return (InvocationResult<R>) NONE;
		}
	}
	
	
	/**
	 * 可用于限制何时使用回调的过滤器
	 *
	 * @param <C> - 回调类型
	 * @param <A> - 主参数类型
	 */
	@FunctionalInterface
	interface Filter<C, A> {

		/**
		 * 确定给定的回调是否匹配并应被调用
		 * 
		 * @param callbackType - 回调类型（函数接口）
		 * @param callbackInstance - 回调实例（实现）
		 * @param argument - 主要参数
		 * @param additionalArguments - 其他任何参数
		 * @return 如果回调匹配并且应该被调用
		 */
		boolean match(Class<C> callbackType, C callbackInstance, A argument, Object[] additionalArguments);

		/**
		 * 返回一个允许调用所有回调函数的 {@link Filter}
		 * 
		 * @param <C> - 回调类型
		 * @param <A> - 主参数类型
		 * @return 一个"允许所有"的{@link Filter}
		 */
		static <C, A> Filter<C, A> allowAll() {
			return (callbackType, callbackInstance, argument, additionalArguments) -> true;
		}
	}
	
	/**
	 * 当回调只有一个泛型参数且主参数是其实例时匹配的 {@link Filter}
	 */
	private static class GenericTypeFilter<C, A> implements Filter<C, A> {
		@Override
		public boolean match(Class<C> callbackType, C callbackInstance, A argument, Object[] additionalArguments) {
			ResolvableType type = ResolvableType.forClass(callbackType, callbackInstance.getClass());
			
			ResolvableType[] generics = type.getGenerics();
			if (generics.length == 1) {
				return generics[1].isAssignableFrom(argument);
			}

			return true;
		}
	}
}
