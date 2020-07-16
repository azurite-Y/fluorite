package org.zy.fluorite.aop.support;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.zy.fluorite.aop.exception.AopConfigException;
import org.zy.fluorite.aop.interfaces.Advice;
import org.zy.fluorite.aop.interfaces.Advised;
import org.zy.fluorite.aop.interfaces.Advisor;
import org.zy.fluorite.aop.interfaces.AdvisorChainFactory;
import org.zy.fluorite.aop.interfaces.DynamicIntroductionAdvice;
import org.zy.fluorite.aop.interfaces.IntroductionAdvisor;
import org.zy.fluorite.aop.interfaces.IntroductionInfo;
import org.zy.fluorite.aop.interfaces.TargetSource;
import org.zy.fluorite.aop.proxy.ProxyConfig;
import org.zy.fluorite.aop.target.EmptyTargetSource;
import org.zy.fluorite.aop.target.SingletonTargetSource;
import org.zy.fluorite.core.utils.Assert;
import org.zy.fluorite.core.utils.ClassUtils;

/**
 * @DateTime 2020年7月4日 下午2:05:06;
 * @author zy(azurite-Y);
 * @Description AOP代理配置管理器的基类。这些本身不是AOP代理，但此类的子类通常是直接从中获取AOP代理实例的工厂。
 *   这个类释放了Advices和advisor的内务处理子类，但实际上并不实现代理创建方法，这些方法是由子类提供的。
 */
@SuppressWarnings("serial")
public class AdvisedSupport extends ProxyConfig implements Advised {
	
	public static final TargetSource EMPTY_TARGET_SOURCE = EmptyTargetSource.INSTANCE;

	/** 包可见字段，提升效率 */
	TargetSource targetSource = EMPTY_TARGET_SOURCE;

	/** 顾问是否已经针对特定的目标类进行了筛选 */
	private boolean preFiltered = false;

	AdvisorChainFactory advisorChainFactory = new DefaultAdvisorChainFactory();

	private transient Map<String, List<Object>> methodCache;

	/**
	 * 由代理实现的接口。保留在列表中以保持注册顺序，以创建具有指定接口顺序的JDK代理
	 */
	private List<Class<?>> interfaces = new ArrayList<>();

	/**
	 * 顾问名单。如果添加了一个建议，则在添加到此列表之前，它将被包装在顾问中。
	 */
	private List<Advisor> advisors = new ArrayList<>();

	/**
	 * 数组更新了对顾问列表的更改，这更便于内部操作
	 */
	private Advisor[] advisorArray = new Advisor[0];

	public AdvisedSupport() {
		this.methodCache = new ConcurrentHashMap<>(32);
	}

	/**
	 * 根据指定的代理接口创建AdvisedSupport实例
	 * 
	 * @param interfaces - 代理接口
	 */
	public AdvisedSupport(Class<?>... interfaces) {
		this();
		setInterfaces(interfaces);
	}

	/**
	 * 将给定对象设置为目标。并为单个目标创建一个目标对象
	 */
	public void setTarget(Object target) {
		setTargetSource(new SingletonTargetSource(target));
	}

	@Override
	public void setTargetSource(TargetSource targetSource) {
		this.targetSource = (targetSource != null ? targetSource : EMPTY_TARGET_SOURCE);
	}

	@Override
	public TargetSource getTargetSource() {
		return this.targetSource;
	}

	/**
	 * 设置要代理的目标类，指示代理应可转换为给定类
	 */
	public void setTargetClass(Class<?> targetClass) {
		this.targetSource = EmptyTargetSource.forClass(targetClass);
	}

	@Override
	public Class<?> getTargetClass() {
		return this.targetSource.getTargetClass();
	}

	@Override
	public void setPreFiltered(boolean preFiltered) {
		this.preFiltered = preFiltered;
	}

	@Override
	public boolean isPreFiltered() {
		return this.preFiltered;
	}

	/**
	 * 设置要使用的AdvisorChainFactory对象，默认为{@link DefaultAdvisorChainFactory}.
	 */
	public void setAdvisorChainFactory(AdvisorChainFactory advisorChainFactory) {
		Assert.notNull(advisorChainFactory, "AdvisorChainFactory不能为null");
		this.advisorChainFactory = advisorChainFactory;
	}

	public AdvisorChainFactory getAdvisorChainFactory() {
		return this.advisorChainFactory;
	}

	public void setInterfaces(Class<?>... interfaces) {
		Assert.notNull(interfaces, "指定的代理接口不能为null");
		this.interfaces.clear();
		for (Class<?> ifc : interfaces) {
			addInterface(ifc);
		}
	}

	/**
	 * 添加新的代理接口
	 * @param intf - 代理的附加接口
	 */
	public void addInterface(Class<?> intf) {
		Assert.notNull(intf, "代理接口不能为null");
		Assert.isTrue(intf.isInterface(), "指定的Class对象不是一个接口：" + intf.getName());
		if (!this.interfaces.contains(intf)) {
			this.interfaces.add(intf);
			adviceChanged();
		}
	}

	/** 删除代理接口。如果给定接口未被代理，则不执行任何操作 */
	public boolean removeInterface(Class<?> intf) {
		return this.interfaces.remove(intf);
	}

	@Override
	public Class<?>[] getProxiedInterfaces() {
		return ClassUtils.toClassArray(this.interfaces);
	}

	@Override
	public boolean isInterfaceProxied(Class<?> intf) {
		for (Class<?> proxyIntf : this.interfaces) {
			if (intf.isAssignableFrom(proxyIntf)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public final Advisor[] getAdvisors() {
		return this.advisorArray;
	}

	@Override
	public void addAdvisor(Advisor advisor) {
		int pos = this.advisors.size();
		addAdvisor(pos, advisor);
	}

	@Override
	public void addAdvisor(int pos, Advisor advisor) throws AopConfigException {
		if (advisor instanceof IntroductionAdvisor) {
			validateIntroductionAdvisor((IntroductionAdvisor) advisor);
		}
		addAdvisorInternal(pos, advisor);
	}

	@Override
	public boolean removeAdvisor(Advisor advisor) {
		int index = indexOf(advisor);
		if (index == -1) {
			return false;
		} else {
			removeAdvisor(index);
			return true;
		}
	}

	@Override
	public void removeAdvisor(int index) throws AopConfigException {
		if (isFrozen()) {
			throw new AopConfigException("无法删除顾问：配置已冻结");
		}
		if (index < 0 || index > this.advisors.size() - 1) {
			throw new AopConfigException("Advisor下标'" + index + "'越界，最大索引下标：" + this.advisors.size());
		}

		Advisor advisor = this.advisors.remove(index);
		if (advisor instanceof IntroductionAdvisor) {
			IntroductionAdvisor ia = (IntroductionAdvisor) advisor;
			// 我们需要删除introduction接口
			for (Class<?> ifc : ia.getInterfaces()) {
				removeInterface(ifc);
			}
		}
		updateAdvisorArray();
		adviceChanged();
	}

	@Override
	public int indexOf(Advisor advisor) {
		Assert.notNull(advisor, "Advisor不能为null");
		return this.advisors.indexOf(advisor);
	}

	@Override
	public boolean replaceAdvisor(Advisor a, Advisor b) throws AopConfigException {
		Assert.notNull(a, "Advisor不能为null");
		Assert.notNull(b, "Advisor不能为null");
		int index = indexOf(a);
		if (index == -1) {
			return false;
		}
		removeAdvisor(index);
		addAdvisor(index, b);
		return true;
	}

	public void addAdvisors(Advisor... advisors) {
		addAdvisors(Arrays.asList(advisors));
	}

	public void addAdvisors(Collection<Advisor> advisors) {
		if (isFrozen()) {
			throw new AopConfigException("无法添加顾问：配置已冻结");
		}
		if (Assert.notNull(advisors)) {
			for (Advisor advisor : advisors) {
				if (advisor instanceof IntroductionAdvisor) {
					validateIntroductionAdvisor((IntroductionAdvisor) advisor);
				}
				Assert.notNull(advisor, "Advisor不能为null");
				this.advisors.add(advisor);
			}
			updateAdvisorArray();
			adviceChanged();
		}
	}

	private void validateIntroductionAdvisor(IntroductionAdvisor advisor) {
		advisor.validateInterfaces();
		// 如果顾问通过了验证，我们可以进行更改
		Class<?>[] ifcs = advisor.getInterfaces();
		for (Class<?> ifc : ifcs) {
			addInterface(ifc);
		}
	}

	private void addAdvisorInternal(int pos, Advisor advisor) throws AopConfigException {
		Assert.notNull(advisor, "Advisor不能为null");
		if (isFrozen()) {
			throw new AopConfigException("无法添加Advisor：配置被冻结");
		}
		if (pos > this.advisors.size()) {
			throw new IllegalArgumentException("指定的Advisors位置无效：" + pos);
		}
		this.advisors.add(pos, advisor);
		updateAdvisorArray();
		adviceChanged();
	}

	/** 使数组与列表保持最新 */
	protected final void updateAdvisorArray() {
		this.advisorArray = this.advisors.toArray(new Advisor[0]);
	}

	/**
	 * 允许不受控制地访问顾问列表。 小心使用，并记住在进行任何修改时刷新顾问阵列和Advisors修改事件事件
	 */
	protected final List<Advisor> getAdvisorsInternal() {
		return this.advisors;
	}

	@Override
	public void addAdvice(Advice advice) throws AopConfigException {
		int pos = this.advisors.size();
		addAdvice(pos, advice);
	}

	/** 除非通知实现了IntroductionInfo，否则无法以这种方式添加Advice */
	@Override
	public void addAdvice(int pos, Advice advice) throws AopConfigException {
		Assert.notNull(advice, "Advice不能为null");
		if (advice instanceof IntroductionInfo) {
			addAdvisor(pos, new DefaultIntroductionAdvisor(advice, (IntroductionInfo) advice));
		} else if (advice instanceof DynamicIntroductionAdvice) {
			throw new AopConfigException("DynamicIntroductionAdvice只能作为IntroductionAdvisor的一部分添加");
		} else {
			addAdvisor(pos, new DefaultPointcutAdvisor(advice));
		}
	}

	@Override
	public boolean removeAdvice(Advice advice) throws AopConfigException {
		int index = indexOf(advice);
		if (index == -1) {
			return false;
		} else {
			removeAdvisor(index);
			return true;
		}
	}

	@Override
	public int indexOf(Advice advice) {
		Assert.notNull(advice, "Advice不能为null");
		for (int i = 0; i < this.advisors.size(); i++) {
			Advisor advisor = this.advisors.get(i);
			if (advisor.getAdvice() == advice) {
				return i;
			}
		}
		return -1;
	}

	/** 给定的建议是否包含在这个代理配置中的任何顾问中 */
	public boolean adviceIncluded(Advice advice) {
		if (advice != null) {
			for (Advisor advisor : this.advisors) {
				if (advisor.getAdvice() == advice) {
					return true;
				}
			}
		}
		return false;
	}

	/** 计算给定类的advices数量 */
	public int countAdvicesOfType(Class<?> adviceClass) {
		int count = 0;
		if (adviceClass != null) {
			for (Advisor advisor : this.advisors) {
				if (adviceClass.isInstance(advisor.getAdvice())) {
					count++;
				}
			}
		}
		return count;
	}

	/**
	 * 根据此配置确定MethodInterceptor对象
	 * 
	 * @param method  - 代理方法
	 * @param targetClass - 被代理类
	 * @return MethodInterceptor列表（可能还包括Interceptor和DynamicMethodMatchers）
	 */
	public List<Object> getInterceptorsAndDynamicInterceptionAdvice(Method method, Class<?> targetClass) {
		String cacheKey = method.toString();
		List<Object> cached = this.methodCache.get(method.toString());
		if (cached == null) {
			cached = this.advisorChainFactory.getInterceptorsAndDynamicInterceptionAdvice(this, method, targetClass);
			this.methodCache.put(cacheKey, cached);
		}
		return cached;
	}

	/** 在通知更改时调用 */
	protected void adviceChanged() {
		this.methodCache.clear();
	}

	/** 在无参数构造函数创建的新实例上调用此方法，以从给定对象创建配置的独立副本 */
	protected void copyConfigurationFrom(AdvisedSupport other) {
		copyConfigurationFrom(other, other.targetSource, new ArrayList<>(other.advisors));
	}

	/**
	 * 从给定的AdvisedSupport对象复制AOP配置，但允许替换新的TargetSource和给定的拦截器链
	 * 
	 * @param other        - 要从中获取代理的AdvisedSupport对象
	 * @param targetSource - 新的被代理类
	 * @param advisors     - Advisor集合
	 */
	protected void copyConfigurationFrom(AdvisedSupport other, TargetSource targetSource, List<Advisor> advisors) {
		copyFrom(other);
		this.targetSource = targetSource;
		this.advisorChainFactory = other.advisorChainFactory;
		this.interfaces = new ArrayList<>(other.interfaces);
		for (Advisor advisor : advisors) {
			if (advisor instanceof IntroductionAdvisor) {
				validateIntroductionAdvisor((IntroductionAdvisor) advisor);
			}
			Assert.notNull(advisor, "Advisor不能为null");
			this.advisors.add(advisor);
		}
		updateAdvisorArray();
		adviceChanged();
	}

	/** 生成此AdvisedSupport的仅供配置的副本，替换TargetSource */
	public AdvisedSupport getConfigurationOnlyCopy() {
		AdvisedSupport copy = new AdvisedSupport();
		copy.copyFrom(this);
		copy.targetSource = EmptyTargetSource.forClass(getTargetClass(), getTargetSource().isStatic());
		copy.advisorChainFactory = this.advisorChainFactory;
		copy.interfaces = this.interfaces;
		copy.advisors = this.advisors;
		copy.preFiltered = this.preFiltered;
		copy.updateAdvisorArray();
		return copy;
	}

	private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
		// 依赖于默认序列化；只需在反序列化后初始化状态
		ois.defaultReadObject();
		this.methodCache = new ConcurrentHashMap<>(32);
	}

	@Override
	public String toProxyConfigString() {
		return toString();
	}

	@Override
	public String toString() {
		return "AdvisedSupport [targetSource=" + targetSource + ", preFiltered=" + preFiltered + ", interfaces="
				+ interfaces + ", advisors=" + advisors + ", advisorArray=" + Arrays.toString(advisorArray) + "]";
	}
}
