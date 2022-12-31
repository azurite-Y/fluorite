package org.zy.fluorite.autoconfigure.web.servlet;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EventListener;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.Filter;
import javax.servlet.MultipartConfigElement;
import javax.servlet.Servlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zy.fluorite.autoconfigure.web.servlet.interfaces.ServletContextInitializer;
import org.zy.fluorite.beans.factory.interfaces.ListableBeanFactory;
import org.zy.fluorite.beans.support.AnnotationAwareOrderComparator;
import org.zy.fluorite.core.interfaces.MultiValueMap;
import org.zy.fluorite.core.utils.LinkedMultiValueMap;

/**
 * @dateTime 2022年12月7日;
 * @author zy(azurite-Y);
 * @description
 * 
 * 从 {@link ListableBeanFactory} 获得的 {@link ServletContextInitializer}s 集合。
 * 包括所有 {@link ServletContextInitializer} bean，也适应 {@link Servlet}, {@link Filter}和某些 {@link EventListener} bean。
 * <p>
 * adapted beans 在最上面( {@link Servlet}, {@link Filter} 然后 {@link EventListener} )，直接的 {@link ServletContextInitializer} bean在最后。
 * 使用 {@link AnnotationAwareOrderComparator} 在这些组中应用进一步的排序。
 */
public class ServletContextInitializerBeans extends AbstractCollection<ServletContextInitializer> {
	private static final Logger logger = LoggerFactory.getLogger(ServletContextInitializerBeans.class);
	
	private static final String DISPATCHER_SERVLET_NAME = "dispatcherServlet";

	private final MultiValueMap<Class<?>, ServletContextInitializer> initializers;

	private final List<Class<? extends ServletContextInitializer>> initializerTypes;
	
	private List<ServletContextInitializer> sortedList;
	
	private final Set<Object> seen = new HashSet<>();

	
	/**
	 * ServletContextInitializerBeans 构造器
	 * 
	 * @param beanFactory
	 * @param initializerTypes
	 */
	@SafeVarargs
	public ServletContextInitializerBeans(ListableBeanFactory beanFactory, Class<? extends ServletContextInitializer>... initializerTypes) {
		this.initializers = new LinkedMultiValueMap<>();
		
		this.initializerTypes = (initializerTypes.length != 0) ? Arrays.asList(initializerTypes) : Collections.singletonList(ServletContextInitializer.class);
		
		addServletContextInitializerBeans(beanFactory);
		addAdaptableBeans(beanFactory);
		
		// 排序之后归集数据到 List 容器中
		List<ServletContextInitializer> sortedInitializers = this.initializers.values().stream()
				.flatMap((value) -> value.stream().sorted(AnnotationAwareOrderComparator.INSTANCE))
				.collect(Collectors.toList());
		
		this.sortedList = Collections.unmodifiableList(sortedInitializers);
		logMappings(this.initializers);
	}
	
	private void addServletContextInitializerBeans(ListableBeanFactory beanFactory) {
		for (Class<? extends ServletContextInitializer> initializerType : this.initializerTypes) {
			for (Entry<String, ? extends ServletContextInitializer> initializerBean : getOrderedBeansOfType(beanFactory, initializerType)) {
				addServletContextInitializerBean(initializerBean.getKey(), initializerBean.getValue(), beanFactory);
			}
		}
	}
	
	private void addServletContextInitializerBean(String beanName, ServletContextInitializer initializer, ListableBeanFactory beanFactory) {
		if (initializer instanceof ServletRegistrationBean) {
			Servlet source = ((ServletRegistrationBean<?>) initializer).getServlet();
			addServletContextInitializerBean(Servlet.class, beanName, initializer, beanFactory, source);
		} else if (initializer instanceof FilterRegistrationBean) {
			Filter source = ((FilterRegistrationBean<?>) initializer).getFilter();
			addServletContextInitializerBean(Filter.class, beanName, initializer, beanFactory, source);
//		} else if (initializer instanceof DelegatingFilterProxyRegistrationBean) {
//			String source = ((DelegatingFilterProxyRegistrationBean) initializer).getTargetBeanName();
//			addServletContextInitializerBean(Filter.class, beanName, initializer, beanFactory, source);
		} else if (initializer instanceof ServletListenerRegistrationBean) {
			EventListener source = ((ServletListenerRegistrationBean<?>) initializer).getListener();
			addServletContextInitializerBean(EventListener.class, beanName, initializer, beanFactory, source);
		} else {
			addServletContextInitializerBean(ServletContextInitializer.class, beanName, initializer, beanFactory, initializer);
		}
	}
	
	private void addServletContextInitializerBean(Class<?> type, String beanName, ServletContextInitializer initializer, ListableBeanFactory beanFactory, Object source) {
		this.initializers.add(type, initializer);
		if (source != null) {
			// 标记底层源代码，以防它包装现有bean
			this.seen.add(source);
		}
	}
	
	private int getOrder(Object value) {
		return new AnnotationAwareOrderComparator() {
			@Override
			public int getOrder(Object obj) {
				return super.getOrder(obj);
			}
		}.getOrder(value);
	}
	
	/**
	 * @param <T> - Bean Class类泛型
	 * @param beanFactory - bean工厂
	 * @param type Bean Class 类型
	 * @return 排序过的指定bean类型的集合
	 */
	private <T> List<Entry<String, T>> getOrderedBeansOfType(ListableBeanFactory beanFactory, Class<T> type) {
		return getOrderedBeansOfType(beanFactory, type, Collections.emptySet());
	}

	private <T> List<Entry<String, T>> getOrderedBeansOfType(ListableBeanFactory beanFactory, Class<T> type, Set<?> excludes) {
		String[] names = beanFactory.getBeanNamesForType(type, true, false);
		Map<String, T> map = new LinkedHashMap<>();
		for (String name : names) {
			T bean = beanFactory.getBean(name, type);
			if (!excludes.contains(bean)) {
				map.put(name, bean);
			}
		}
		List<Entry<String, T>> beans = new ArrayList<>(map.entrySet());
		beans.sort((o1, o2) -> AnnotationAwareOrderComparator.INSTANCE.compare(o1.getValue(), o2.getValue()));
		return beans;
	}
	
	
	@SuppressWarnings("unchecked")
	protected void addAdaptableBeans(ListableBeanFactory beanFactory) {
		MultipartConfigElement multipartConfig = getMultipartConfig(beanFactory);
		addAsRegistrationBean(beanFactory, Servlet.class, new ServletRegistrationBeanAdapter(multipartConfig));
		addAsRegistrationBean(beanFactory, Filter.class, new FilterRegistrationBeanAdapter());
		for (Class<?> listenerType : ServletListenerRegistrationBean.getSupportedTypes()) {
			addAsRegistrationBean(beanFactory, EventListener.class, (Class<EventListener>) listenerType, new ServletListenerRegistrationBeanAdapter());
		}
	}
	
	private MultipartConfigElement getMultipartConfig(ListableBeanFactory beanFactory) {
		List<Entry<String, MultipartConfigElement>> beans = getOrderedBeansOfType(beanFactory, MultipartConfigElement.class);
		return beans.isEmpty() ? null : beans.get(0).getValue();
	}
	
	protected <T> void addAsRegistrationBean(ListableBeanFactory beanFactory, Class<T> type, RegistrationBeanAdapter<T> adapter) {
		addAsRegistrationBean(beanFactory, type, type, adapter);
	}
	
	private <T, B extends T> void addAsRegistrationBean(ListableBeanFactory beanFactory, Class<T> type, Class<B> beanType, RegistrationBeanAdapter<T> adapter) {
		List<Map.Entry<String, B>> entries = getOrderedBeansOfType(beanFactory, beanType, this.seen);
		for (Entry<String, B> entry : entries) {
			String beanName = entry.getKey();
			B bean = entry.getValue();
			if (this.seen.add(bean)) {
				// 一个我们还没见过的
				RegistrationBean registration = adapter.createRegistrationBean(beanName, bean, entries.size());
				int order = getOrder(bean);
				registration.setOrder(order);
				this.initializers.add(type, registration);
			}
		}
	}
	
	
	private void logMappings(MultiValueMap<Class<?>, ServletContextInitializer> initializers) {
		if (logger.isDebugEnabled()) {
			logMappings("filters", initializers, Filter.class, FilterRegistrationBean.class);
			logMappings("servlets", initializers, Servlet.class, ServletRegistrationBean.class);
		}
	}

	private void logMappings(String name, MultiValueMap<Class<?>, ServletContextInitializer> initializers, Class<?> type, Class<? extends RegistrationBean> registrationType) {
		List<ServletContextInitializer> registrations = new ArrayList<>();
		registrations.addAll(initializers.getOrDefault(registrationType, Collections.emptyList()));
		registrations.addAll(initializers.getOrDefault(type, Collections.emptyList()));
		String info = registrations.stream().map(Object::toString).collect(Collectors.joining(", "));
		logger.debug("Mapping " + name + ": [" + info + "]");
	}
	
	@Override
	public Iterator<ServletContextInitializer> iterator() {
		return this.sortedList.iterator();
	}

	@Override
	public int size() {
		return this.sortedList.size();
	}
	
	// -------------------------------------------------------------------------------------
	// 内部类
	// -------------------------------------------------------------------------------------
	/**
	 * 适配器，将给定Bean类型转换为 {@link RegistrationBean} (因此是 {@link ServletContextInitializer} )。
	 *
	 * @param <T> - Bean 类型
	 */
	@FunctionalInterface
	protected interface RegistrationBeanAdapter<T> {
		/**
		 * 
		 * @param name - bean名称
		 * @param source - bean对象
		 * @param totalNumberOfSourceBeans - 源bean对象的总数
		 * @return 转换的 {@link RegistrationBean} 
		 */
		RegistrationBean createRegistrationBean(String name, T source, int totalNumberOfSourceBeans);
	}

	/**
	 * {@link Servlet} beans 的 {@link RegistrationBeanAdapter}
	 */
	private static class ServletRegistrationBeanAdapter implements RegistrationBeanAdapter<Servlet> {

		private final MultipartConfigElement multipartConfig;

		ServletRegistrationBeanAdapter(MultipartConfigElement multipartConfig) {
			this.multipartConfig = multipartConfig;
		}

		@Override
		public RegistrationBean createRegistrationBean(String name, Servlet source, int totalNumberOfSourceBeans) {
			String url = (totalNumberOfSourceBeans != 1) ? "/" + name + "/" : "/";
			if (name.equals(DISPATCHER_SERVLET_NAME)) {
				url = "/"; // 始终将主 dispatcherServlet 映射到“/”
			}
			ServletRegistrationBean<Servlet> bean = new ServletRegistrationBean<>(source, url);
			bean.setName(name);
			bean.setMultipartConfig(this.multipartConfig);
			return bean;
		}

	}

	/**
	 * {@link Filter} beans 的 {@link RegistrationBeanAdapter}
	 */
	private static class FilterRegistrationBeanAdapter implements RegistrationBeanAdapter<Filter> {

		@Override
		public RegistrationBean createRegistrationBean(String name, Filter source, int totalNumberOfSourceBeans) {
			FilterRegistrationBean<Filter> bean = new FilterRegistrationBean<>(source);
			bean.setName(name);
			return bean;
		}

	}

	/**
	 * 某些 {@link EventListener} beans 的 {@link RegistrationBeanAdapter}.
	 */
	private static class ServletListenerRegistrationBeanAdapter implements RegistrationBeanAdapter<EventListener> {

		@Override
		public RegistrationBean createRegistrationBean(String name, EventListener source, int totalNumberOfSourceBeans) {
			return new ServletListenerRegistrationBean<>(source);
		}

	}
	
}
