package org.zy.fluorite.beans.beanDefinittion;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.zy.fluorite.beans.factory.exception.BeanDefinitionValidationException;
import org.zy.fluorite.beans.interfaces.BeanDefinition;
import org.zy.fluorite.beans.support.AnnotationMetadataHolder;
import org.zy.fluorite.beans.support.AutowireCandidateQualifier;
import org.zy.fluorite.beans.support.BeanMetadataAttributeAccessor;
import org.zy.fluorite.beans.support.ConstructorArgumentValues;
import org.zy.fluorite.beans.support.MethodOverride;
import org.zy.fluorite.beans.support.MethodOverrides;
import org.zy.fluorite.beans.support.MutablePropertyValues;
import org.zy.fluorite.core.convert.ResolvableType;
import org.zy.fluorite.core.interfaces.AnnotationMetadata;
import org.zy.fluorite.core.interfaces.Resource;
import org.zy.fluorite.core.utils.Assert;
import org.zy.fluorite.core.utils.AutowireUtils;
import org.zy.fluorite.core.utils.ClassUtils;
import org.zy.fluorite.core.utils.ScopeUtils;

/**
 * @author: zy(azurite-Y);
 * @DateTime: 2020年6月4日 下午4:47:41;
 * @Description BeanDefinition的基类
 */
@SuppressWarnings("serial")
public abstract class AbstractBeanDefinition extends BeanMetadataAttributeAccessor implements BeanDefinition {
	// volatile：在多线程环境下若有一线程更改了此值则会同步通知其他线程
	/** 存储组件的Class对象，可能为FactoryBean实现的Class */
	protected volatile Class<?> beanClass;

	protected String beanName;

	protected String qualifiedName;

	protected AnnotationMetadata annotationMetadata;

	protected String scope = ScopeUtils.SCOPE_SINGLETON;

	protected boolean abstractFlag = false;

	protected boolean lazyInit = false;

	protected int autowireMode = AutowireUtils.AUTOWIRE_NO;

	protected String[] dependsOn;

	protected boolean autowireCandidate = true;

	protected boolean primary = false;

	protected Integer priority;

	@Deprecated
	protected final Map<String, AutowireCandidateQualifier> qualifiers = new LinkedHashMap<>();

	protected boolean nonPublicAccessAllowed = true;

	protected String factoryBeanName;

	protected String factoryMethodName;

	protected ConstructorArgumentValues constructorArgumentValues;

	protected MutablePropertyValues propertyValues = new MutablePropertyValues();

	protected MethodOverrides methodOverrides = new MethodOverrides();

	protected String initMethodName;

	protected String destroyMethodName;

	/** 是否执行初始化方法 */
	protected boolean enforceInitMethod = true;

	/** 是否执行销毁方法 */
	protected boolean enforceDestroyMethod = true;

	/** 关于此BeanDefinition的描述 */
	protected String description;

	protected Resource resource;

	public AbstractBeanDefinition() {
		super();
	}

	public AbstractBeanDefinition(Class<?> beanClass) {
		super();
		this.beanClass = beanClass;
	}

	protected AbstractBeanDefinition(BeanDefinition original) {
		setBeanClass(original.getBeanClass());
		setScope(original.getScope());
		setAbstract(original.isAbstract());
		setLazyInit(original.isLazyInit());
		setFactoryBeanName(original.getFactoryBeanName());
		setFactoryMethodName(original.getFactoryMethodName());
		// 空集
		copyAttributesFrom(original);
		setQualifiedName(original.getQualifiedName());

//		setMetadata(original.getMetadata());

		if (original instanceof AbstractBeanDefinition) {
			AbstractBeanDefinition originalAbd = (AbstractBeanDefinition) original;
			if (originalAbd.getBeanClass() != null) {
				setBeanClass(originalAbd.getBeanClass());
			}
			if (originalAbd.hasConstructorArgumentValues()) {
				setConstructorArgumentValues(new ConstructorArgumentValues(original.getConstructorArgumentValues()));
			}
			if (originalAbd.hasPropertyValues()) {
				setPropertyValues(new MutablePropertyValues(original.getPropertyValues()));
			}
			if (originalAbd.hasMethodOverrides()) {
				setMethodOverrides(new MethodOverrides(originalAbd.getMethodOverrides()));
			}
			setAutowireMode(originalAbd.getAutowireMode());
			setDependsOn(originalAbd.getDependsOn());
			setAutowireCandidate(originalAbd.isAutowireCandidate());
			setPrimary(originalAbd.isPrimary());
			copyQualifiersFrom(originalAbd);
			setNonPublicAccessAllowed(originalAbd.isNonPublicAccessAllowed());
			setInitMethodName(originalAbd.getInitMethodName());
			setEnforceInitMethod(originalAbd.isEnforceInitMethod());
			setDestroyMethodName(originalAbd.getDestroyMethodName());
			setEnforceDestroyMethod(originalAbd.isEnforceDestroyMethod());
			setResource(originalAbd.getResource());
			setAnnotationMetadata(originalAbd.getAnnotationMetadata());
		} else {
			setConstructorArgumentValues(new ConstructorArgumentValues(original.getConstructorArgumentValues()));
			setPropertyValues(new MutablePropertyValues(original.getPropertyValues()));
		}
	}

	public AnnotationMetadata getAnnotationMetadata() {
		if (annotationMetadata == null) {
			annotationMetadata = new AnnotationMetadataHolder(this.beanClass);
		}
		return annotationMetadata;
	}

	public void setAnnotationMetadata(AnnotationMetadata annotationMetadata) {
		this.annotationMetadata = annotationMetadata;
	}

	public void setQualifiedName(String qualifiedName) {
		this.qualifiedName = qualifiedName;
	}

	public String getQualifiedName() {
		return this.qualifiedName;
	}

	@Override
	public void setScope(String scope) {
		this.scope = scope;
	}

	@Override
	public String getScope() {
		return this.scope;
	}

	@Override
	public boolean isSingleton() {
		return ScopeUtils.SCOPE_SINGLETON.equals(this.scope);
	}

	@Override
	public boolean isPrototype() {
		return ScopeUtils.SCOPE_PROTOTYPE.equals(this.scope);
	}

	public void setAbstract(boolean abstractFlag) {
		this.abstractFlag = abstractFlag;
	}

	@Override
	public boolean isAbstract() {
		return this.abstractFlag;
	}

	@Override
	public void setLazyInit(boolean lazyInit) {
		this.lazyInit = lazyInit;
	}

	@Override
	public boolean isLazyInit() {
		return this.lazyInit;
	}

	public void setAutowireMode(int autowireMode) {
		this.autowireMode = autowireMode;
	}

	public int getAutowireMode() {
		return this.autowireMode;
	}

	public int getResolvedAutowireMode() {
		return this.autowireMode;
	}

	@Override
	public void setDependsOn(String... dependsOn) {
		this.dependsOn = dependsOn;
	}

	@Override
	public String[] getDependsOn() {
		return this.dependsOn;
	}

	@Override
	public void setAutowireCandidate(boolean autowireCandidate) {
		this.autowireCandidate = autowireCandidate;
	}

	@Override
	public boolean isAutowireCandidate() {
		return this.autowireCandidate;
	}

	@Override
	public void setPrimary(boolean primary) {
		this.primary = primary;
	}

	@Override
	public boolean isPrimary() {
		return this.primary;
	}

	public void addQualifier(AutowireCandidateQualifier qualifier) {
		this.qualifiers.put(qualifier.getTypeName(), qualifier);
	}

	public boolean hasQualifier(String typeName) {
		return this.qualifiers.containsKey(typeName);
	}

	public AutowireCandidateQualifier getQualifier(String typeName) {
		return this.qualifiers.get(typeName);
	}

	public Set<AutowireCandidateQualifier> getQualifiers() {
		return new LinkedHashSet<>(this.qualifiers.values());
	}

	public void copyQualifiersFrom(AbstractBeanDefinition source) {
		Assert.notNull(source, "source不能为null");
		this.qualifiers.putAll(source.qualifiers);
	}

	public void setNonPublicAccessAllowed(boolean nonPublicAccessAllowed) {
		this.nonPublicAccessAllowed = nonPublicAccessAllowed;
	}

	/**
	 * 判断是否允许非公共访问，默认为true
	 */
	public boolean isNonPublicAccessAllowed() {
		return this.nonPublicAccessAllowed;
	}

	@Override
	public void setFactoryBeanName(String factoryBeanName) {
		this.factoryBeanName = factoryBeanName;
	}

	@Override
	public String getFactoryBeanName() {
		return this.factoryBeanName;
	}

	@Override
	public void setFactoryMethodName(String factoryMethodName) {
		this.factoryMethodName = factoryMethodName;
	}

	@Override
	public String getFactoryMethodName() {
		return this.factoryMethodName;
	}

	public void setConstructorArgumentValues(ConstructorArgumentValues constructorArgumentValues) {
		this.constructorArgumentValues = constructorArgumentValues;
	}

	@Override
	public ConstructorArgumentValues getConstructorArgumentValues() {
		if (this.constructorArgumentValues == null) {
			this.constructorArgumentValues = new ConstructorArgumentValues();
		}
		return this.constructorArgumentValues;
	}

	@Override
	public boolean hasConstructorArgumentValues() {
		return (this.constructorArgumentValues != null && !this.constructorArgumentValues.isEmpty());
	}

	public void setPropertyValues(MutablePropertyValues propertyValues) {
		this.propertyValues = propertyValues;
	}

	@Override
	public MutablePropertyValues getPropertyValues() {
		return this.propertyValues;
	}

	@Override
	public boolean hasPropertyValues() {
		return (this.propertyValues != null && !this.propertyValues.isEmpty());
	}

	public void setMethodOverrides(MethodOverrides methodOverrides) {
		this.methodOverrides = methodOverrides;
	}

	public MethodOverrides getMethodOverrides() {
		return this.methodOverrides;
	}

	public boolean hasMethodOverrides() {
		return !this.methodOverrides.isEmpty();
	}

	@Override
	public void setInitMethodName(String initMethodName) {
		this.initMethodName = initMethodName;
	}

	@Override
	public String getInitMethodName() {
		return this.initMethodName;
	}

	public void setEnforceInitMethod(boolean enforceInitMethod) {
		this.enforceInitMethod = enforceInitMethod;
	}

	/**
	 * 判断是否执行初始化方法
	 * 
	 * @return
	 */
	public boolean isEnforceInitMethod() {
		return this.enforceInitMethod;
	}

	@Override
	public void setDestroyMethodName(String destroyMethodName) {
		this.destroyMethodName = destroyMethodName;
	}

	@Override
	public String getDestroyMethodName() {
		return this.destroyMethodName;
	}

	public void setEnforceDestroyMethod(boolean enforceDestroyMethod) {
		this.enforceDestroyMethod = enforceDestroyMethod;
	}

	/**
	 * 判断是否执行销毁方法
	 */
	public boolean isEnforceDestroyMethod() {
		return this.enforceDestroyMethod;
	}

	@Override
	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String getDescription() {
		return this.description;
	}

	public void setResource(Resource resource) {
		this.resource = resource;
	}

	public Resource getResource() {
		return this.resource;
	}

	public Integer getPriority() {
		return priority;
	}

	public void setPriority(Integer priority) {
		this.priority = priority;
	}

	public Class<?> getBeanClass() {
		return beanClass;
	}

	/**
	 * 验证Bean定义.
	 */
	public void validate() throws BeanDefinitionValidationException {
		if (hasMethodOverrides() && getFactoryMethodName() != null) {
			throw new BeanDefinitionValidationException("无法将工厂方法与容器生成的方法重写结合起来：factory方法必须创建具体的bean实例.");
		}
		prepareMethodOverrides();
	}

	/**
	 * 验证重写方法与实际应用方法的匹配性，若有多个匹配则需进行方法重写
	 */
	public void prepareMethodOverrides() throws BeanDefinitionValidationException {
		if (hasMethodOverrides()) {
			getMethodOverrides().getOverrides().forEach(this::prepareMethodOverride);
		}
	}

	/**
	 * 准备方法重写
	 * 
	 * @param mo
	 * @throws BeanDefinitionValidationException
	 */
	protected void prepareMethodOverride(MethodOverride mo) throws BeanDefinitionValidationException {
		int count = ClassUtils.getMethodCountForName(getBeanClass(), mo.getMethodName());
		if (count == 0) {
			throw new BeanDefinitionValidationException("无效的方法重写: 无任何匹配值");
		} else if (count == 1) {
			mo.setOverloaded(false);
		}
	}

	@Override
	public void setBeanName(String beanClassName) {
		this.beanName = beanClassName;
	}

	@Override
	public String getBeanName() {
		return this.beanName;
	}
	
	/**
	 * 返回此bean定义的可解析类型.
	 * <p>
	 * 此实现委托 {@link #getBeanClass()}.
	 * @since 5.2
	 */
	@Override
	public ResolvableType getResolvableType() {
		return (hasBeanClass() ? ResolvableType.forClass(getBeanClass()) : ResolvableType.NONE);
	}
	
	/**
	 * 返回此定义是否指定bean类
	 * 
	 * @see #getBeanClass()
	 * @see #setBeanClass(Class)
	 * @see #resolveBeanClass(ClassLoader)
	 */
	public boolean hasBeanClass() {
		return (this.beanClass instanceof Class);
	}
}
