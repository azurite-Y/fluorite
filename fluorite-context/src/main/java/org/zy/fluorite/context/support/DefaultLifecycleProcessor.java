package org.zy.fluorite.context.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zy.fluorite.beans.factory.aware.BeanFactoryAware;
import org.zy.fluorite.beans.factory.interfaces.BeanFactory;
import org.zy.fluorite.beans.factory.interfaces.ConfigurableListableBeanFactory;
import org.zy.fluorite.beans.factory.utils.BeanFactoryUtils;
import org.zy.fluorite.context.exception.ApplicationContextException;
import org.zy.fluorite.context.interfaces.Lifecycle;
import org.zy.fluorite.context.interfaces.LifecycleProcessor;
import org.zy.fluorite.context.interfaces.Phased;
import org.zy.fluorite.context.interfaces.SmartLifecycle;
import org.zy.fluorite.core.utils.Assert;
import org.zy.fluorite.core.utils.DebugUtils;

/**
 * @DateTime 2020年6月18日 下午4:25:56;
 * @author zy(azurite-Y);
 * @Description 生命周期处理器策略的默认实现
 */
public class DefaultLifecycleProcessor implements LifecycleProcessor, BeanFactoryAware {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	private volatile ConfigurableListableBeanFactory beanFactory;

	/** 每次关闭阶段的超时时间(毫秒) */
	private volatile long timeoutPerShutdownPhase = 30000;

	private volatile boolean running;
	
	@Override
	public void start() {
		startBeans(false);
		this.running = true;
	}

	@Override
	public void onRefresh() {
		startBeans(true);
		this.running = true;
	}

	@Override
	public void stop() {
		stopBeans();
		this.running = false;
	}

	@Override
	public void onClose() {
		stopBeans();
		this.running = false;
	}

	@Override
	public boolean isRunning() {
		return this.running;
	}

	public void setTimeoutPerShutdownPhase(long timeoutPerShutdownPhase) {
		this.timeoutPerShutdownPhase = timeoutPerShutdownPhase;
	}

	@Override
	public void setBeanFactory(BeanFactory beanFactory) {
		Assert.isTrue( beanFactory instanceof ConfigurableListableBeanFactory , "设置的BeanFactory必须实现ConfigurableListableBeanFactory接口，by："+this.getClass().getName());
		this.beanFactory = (ConfigurableListableBeanFactory) beanFactory;
	}
	/**
	 * 确定给定bean的生命周期阶段
	 * @param bean
	 * @return
	 */
	public int getPhase(Lifecycle bean) {
		return (bean instanceof Phased ? ((Phased) bean).getPhase() : 0);
	}

	
	private ConfigurableListableBeanFactory getBeanFactory() {
		ConfigurableListableBeanFactory beanFactory = this.beanFactory;
		Assert.isTrue(beanFactory != null , "缺少可使用的ConfigurableListableBeanFactory实现，by："+this.getClass().getName());
		return beanFactory;
	}

	private void startBeans(boolean autoStartupOnly) {
		Map<String, Lifecycle> lifecycleBeans = getLifecycleBeans();
		Map<Integer, LifecycleGroup> phases = new HashMap<>();
		boolean[] flag = {false};
		lifecycleBeans.forEach((beanName, bean) -> {
			if (bean instanceof SmartLifecycle) {
				flag[0] = ((SmartLifecycle) bean).isAutoStartup();
			} else {
				flag[0] = autoStartupOnly;
			}
			
			if (flag[0]) {
				// 分组归纳生命周期Bean
				int phase = getPhase(bean);
				LifecycleGroup group = phases.get(phase);
				if (group == null) {
					group = new LifecycleGroup(phase, this.timeoutPerShutdownPhase, lifecycleBeans, autoStartupOnly);
					phases.put(phase, group);
				}
				group.add(beanName, bean);
			}
		});
		if (!phases.isEmpty()) {
			List<Integer> keys = new ArrayList<>(phases.keySet());
			// phase自然排序
			Collections.sort(keys);
			for (Integer key : keys) {
				// 逐个调用调用生命周期Bean工作组的start()方法
				phases.get(key).start();
			}
		}
	}

	private void stopBeans() {
		Map<String, Lifecycle> lifecycleBeans = getLifecycleBeans();
		Map<Integer, LifecycleGroup> phases = new HashMap<>();
		lifecycleBeans.forEach((beanName, bean) -> {
			// 分组归纳生命周期Bean
			int shutdownPhase = getPhase(bean);
			LifecycleGroup group = phases.get(shutdownPhase);
			if (group == null) {
				group = new LifecycleGroup(shutdownPhase, this.timeoutPerShutdownPhase, lifecycleBeans, false);
				phases.put(shutdownPhase, group);
			}
			group.add(beanName, bean);
		});
		if (!phases.isEmpty()) {
			List<Integer> keys = new ArrayList<>(phases.keySet());
			keys.sort(Collections.reverseOrder());
			for (Integer key : keys) {
				// 逐个调用生命周期Bean工作组的stop方法
				phases.get(key).stop();
			}
		}
	}

	/**
	 * 从BeanFactory中获得实现Lifecycle接口的Bean
	 * @return
	 */
	protected Map<String, Lifecycle> getLifecycleBeans() {
		ConfigurableListableBeanFactory beanFactory = getBeanFactory();
		Map<String, Lifecycle> beans = new LinkedHashMap<>();
		// 获得实现Lifecycle接口的beanName
		String[] beanNames = beanFactory.getBeanNamesForTypeInclusionSingle(Lifecycle.class);
		for (String beanName : beanNames) {
			String beanNameToRegister = BeanFactoryUtils.transformedBeanName(beanName);
			boolean isFactoryBean = beanFactory.isFactoryBean(beanNameToRegister);
			String beanNameToCheck = (isFactoryBean ? BeanFactory.FACTORY_BEAN_PREFIX + beanName : beanName);
			if ((beanFactory.containsSingleton(beanNameToRegister) &&
					// 非FactoryBean创建的Lifecycle接口实现Bean则此条件判断为true
					( !isFactoryBean || matchesBeanType(Lifecycle.class, beanNameToCheck, beanFactory)) ) ||
					// Bean为SmartLifecycle接口实现则此条件判断为true
					matchesBeanType(SmartLifecycle.class, beanNameToCheck, beanFactory)) {
				Object bean = beanFactory.getBean(beanNameToCheck);
				if (bean != this && bean instanceof Lifecycle) {
					beans.put(beanNameToRegister, (Lifecycle) bean);
				}
			}
		}
		return beans;
	}

	/**
	 * 通过beanName从BeanFactory获得其Class类型
	 * @param targetType
	 * @param beanName
	 * @param beanFactory
	 * @return 若为targetType的实现类、子类或本身则返回true
	 */
	private boolean matchesBeanType(Class<?> targetType, String beanName,ConfigurableListableBeanFactory beanFactory) {
		Class<?> beanType = beanFactory.getType(beanName);
		return (beanType != null && targetType.isAssignableFrom(beanType));
	}

	/**
	 * 在调用生命周期Bean的start()方法之前优先调用依赖项的start()方法，倘若是生命周期Bean的话
	 * @param lifecycleBeans
	 * @param beanName
	 * @param autoStartupOnly
	 */
	private void doStart(Map<String, ? extends Lifecycle> lifecycleBeans, String beanName, boolean autoStartupOnly) {
		Lifecycle bean = lifecycleBeans.remove(beanName);
		if (bean != null && bean != this) {
			String[] dependenciesForBean = getBeanFactory().getDependenciesForBean(beanName);
			for (String dependency : dependenciesForBean) {
				doStart(lifecycleBeans, dependency, autoStartupOnly);
			}
			// autoStartupOnly：默认为false
			if (!bean.isRunning() &&	(!autoStartupOnly || !(bean instanceof SmartLifecycle) || ((SmartLifecycle) bean).isAutoStartup())) {
				DebugUtils.log(logger, "启动Bean，by name：" + beanName + " by type：[" + bean.getClass().getName() + "]");
				try {
					bean.start();
				} catch (Throwable ex) {
					throw new ApplicationContextException("启动Bean失败：by name：" + beanName, ex);
				}
				if (DebugUtils.debug) {
					logger.info("启动Bean成功，by name：" + beanName);
				}
			}
		}
	}
	
	/**
	 * 在调用生命周期Bean的stop()方法之前优先调用依赖项的stop()方法，倘若是生命周期Bean的话
	 * @param lifecycleBeans
	 * @param beanName
	 * @param autoStartupOnly
	 */
	private void doStop(Map<String, ? extends Lifecycle> lifecycleBeans, final String beanName,final CountDownLatch latch, final Set<String> countDownBeanNames) {
		Lifecycle bean = lifecycleBeans.remove(beanName);
		if (bean != null) {
			String[] dependentBeans = getBeanFactory().getDependentBeans(beanName);
			for (String dependentBean : dependentBeans) {
				doStop(lifecycleBeans, dependentBean, latch, countDownBeanNames);
			}
			try {
				if (bean.isRunning()) {
					if (bean instanceof SmartLifecycle) {
						// 关闭实现了SmartLifecycle接口的生命周期Bean
						countDownBeanNames.add(beanName);
						((SmartLifecycle) bean).stop(() -> {
							latch.countDown();
							countDownBeanNames.remove(beanName);
							if (logger.isDebugEnabled()) {
								logger.info("关闭bean完成，by name："+beanName);
							}
						});
					} else {
						bean.stop();
						if (DebugUtils.debug) {
							logger.info("关闭Bean成功，by name：" + beanName);
						}
					}
				} else if (bean instanceof SmartLifecycle) {
					latch.countDown();
				}
			} catch (Throwable ex) {
				if (DebugUtils.debug) {
					logger.warn("关闭Bean失败，by name：" + beanName, ex);
				}
			}
		}
	}

	/** 用于维护一组生命周期bean，这些bean应根据其“phase”值（默认值0）一起启动和停止 */
	private class LifecycleGroup {

		/** 指代bean的生命周期阶段 */
		private final int phase;
		/** */
		private final long timeout;
		/** */
		private final Map<String, ? extends Lifecycle> lifecycleBeans;
		/** 是否仅限自动启动，默认为false，若需更改则实现SmartLifecycle */
		private final boolean autoStartupOnly;
		/** */
		private final List<LifecycleGroupMember> members = new ArrayList<>();

		private int smartMemberCount;

		public LifecycleGroup(	int phase, long timeout, Map<String, ? extends Lifecycle> lifecycleBeans, boolean autoStartupOnly) {
			this.phase = phase;
			this.timeout = timeout;
			this.lifecycleBeans = lifecycleBeans;
			this.autoStartupOnly = autoStartupOnly;
		}

		public void add(String name, Lifecycle bean) {
			this.members.add(new LifecycleGroupMember(name, bean));
			if (bean instanceof SmartLifecycle) {
				this.smartMemberCount++;
			}
		}

		public void start() {
			if (this.members.isEmpty()) {
				return;
			}
			if (DebugUtils.debug) {
				logger.info("启动Bean，by phase：" + this.phase);
			}
			// 排序
			Collections.sort(this.members);
			for (LifecycleGroupMember member : this.members) {
				doStart(this.lifecycleBeans, member.name, this.autoStartupOnly);
			}
		}

		public void stop() {
			if (this.members.isEmpty()) {
				return;
			}
			if (DebugUtils.debug) {
				logger.info("关闭Bean，by phase：" + this.phase);
			}
			// 按原来的自然顺序取反，即phase大的在前
			this.members.sort(Collections.reverseOrder());
			CountDownLatch latch = new CountDownLatch(this.smartMemberCount);
			Set<String> countDownBeanNames = Collections.synchronizedSet(new LinkedHashSet<>());
			Set<String> lifecycleBeanNames = new HashSet<>(this.lifecycleBeans.keySet());
			for (LifecycleGroupMember member : this.members) {
				if (lifecycleBeanNames.contains(member.name)) {
					doStop(this.lifecycleBeans, member.name, latch, countDownBeanNames);
				}
				else if (member.bean instanceof SmartLifecycle) {
					latch.countDown();
				}
			}
			try {
				latch.await(this.timeout, TimeUnit.MILLISECONDS);
				if (latch.getCount() > 0 && !countDownBeanNames.isEmpty()) {
					logger.info("关闭Bean失败，by phase：" + this.phase + " timeout：" + this.timeout +" countDownBeanNames：["+countDownBeanNames+"]");
				}
			} catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
			}
		}
	}


	/**
	 * 将可比较的接口适配到生命周期阶段模型上
	 */
	private class LifecycleGroupMember implements Comparable<LifecycleGroupMember> {

		private final String name;

		private final Lifecycle bean;

		LifecycleGroupMember(String name, Lifecycle bean) {
			this.name = name;
			this.bean = bean;
		}

		@Override
		public int compareTo(LifecycleGroupMember other) {
			int thisPhase = getPhase(this.bean);
			int otherPhase = getPhase(other.bean);
			return Integer.compare(thisPhase, otherPhase);
		}
	}
}
