package org.zy.fluorite.context.annotation;

import java.beans.Introspector;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.zy.fluorite.beans.factory.support.BeanMethod;
import org.zy.fluorite.beans.factory.support.SourceClass;
import org.zy.fluorite.beans.interfaces.BeanDefinition;
import org.zy.fluorite.context.annotation.interfaces.ImportBeanDefinitionRegistrar;
import org.zy.fluorite.context.exception.BeanDefinitionParsingException;
import org.zy.fluorite.core.interfaces.AnnotationMetadata;
import org.zy.fluorite.core.subject.AnnotationAttributes;

/**
 * @DateTime 2020年6月19日 下午5:55:43;
 * @author zy(azurite-Y);
 * @Description 封装定义的@Configuration类。包括一组Bean方法。每一个ConfigurationClass代表一个组件
 */
public class ConfigurationClass {
	private Class<?> source;
	
	private SourceClass sourceClass;
	
	private AnnotationMetadata annotationMetadata;
	
	private String beanName;
	
	private BeanDefinition beanDefinition;
	
	/**
	 *  存储导入此类的组件类的ConfigurationClass对象
	 *  如：App.class使用@Import组件导入了Boot.class。那么当前ConfigurationClass就为Boot类的包装，此集合就存储的是App类的ConfigurationClass对象
	 */
	private final Set<ConfigurationClass> importedBy = new LinkedHashSet<>(1);

	private final Set<BeanMethod> beanMethods = new LinkedHashSet<>();

	private final Map<ImportBeanDefinitionRegistrar, AnnotationMetadata> importBeanDefinitionRegistrars = new LinkedHashMap<>();
	
	/** 忽略的Bean方法 */
	final Set<String> skippedBeanMethods = new HashSet<>();


	public ConfigurationClass(BeanDefinition beanDefinition) {
		super();
		this.source = beanDefinition.getBeanClass();
		this.beanName = beanDefinition.getBeanName();
		this.annotationMetadata = beanDefinition.getAnnotationMetadata();
		this.beanDefinition = beanDefinition;
	}
	public ConfigurationClass(SourceClass sourceClass) {
		super();
		this.sourceClass = sourceClass;
		this.source = sourceClass.getSource();
		this.annotationMetadata = sourceClass.getAnnotationMetadata();
	}
	public ConfigurationClass(Class<?> source,String beanName) {
		super();
		this.source = source;
		this.beanName = beanName;
	}
	public BeanDefinition getBeanDefinition() {
		return beanDefinition;
	}
	public void setBeanDefinition(BeanDefinition beanDefinition) {
		this.beanDefinition = beanDefinition;
	}
	public String getSimpleName() {
		return Introspector.decapitalize(source.getSimpleName());
	}
	public Class<?> getSource() {
		return source;
	}
	public Set<ConfigurationClass> getImportedBy() {
		return importedBy;
	}
	public Set<BeanMethod> getBeanMethods() {
		return beanMethods;
	}
	public String getBeanName() {
		return beanName;
	}
	public void setBeanName(String beanName) {
		this.beanName = beanName;
	}
	public Map<ImportBeanDefinitionRegistrar, AnnotationMetadata> getImportBeanDefinitionRegistrars() {
		return importBeanDefinitionRegistrars;
	}
	public Set<String> getSkippedBeanMethods() {
		return skippedBeanMethods;
	}
	
	/**
	 * 获取本类持有的SourceClass对象，没有则根据情况创建一个
	 * @return
	 */
	public SourceClass getSourceClass() {
		if (sourceClass == null) {
			if (annotationMetadata == null) {
				sourceClass = new SourceClass(source);
				this.annotationMetadata = sourceClass.getAnnotationMetadata();
			}else {
				sourceClass = new SourceClass(source , annotationMetadata);
			}
		}
		return sourceClass;
	}

	/**
	 * 获得Class对象的全限定类名
	 * @return
	 */
	public String getClassName() {
		return this.source.getName();
	}
	
	/**
	 * 添加标注@Bean注解的Method对象
	 * @param method
	 * @return
	 */
	public boolean addBeanMethod(Method method,AnnotationAttributes annotationAttributes) {
		return this.beanMethods.add(new BeanMethod(method,annotationAttributes));
	}
	/**
	 * 添加标注@Bean注解的Method对象的BeanMethod对象
	 * @param method
	 * @return
	 */
	public boolean addBeanMethod(BeanMethod beanMethod) {
		return this.beanMethods.add(beanMethod);
	}

	/**
	 * 返回此配置类是通过@Import注册的，还是由于嵌套在另一个配置类中而自动注册的（标注了@Configuration的内部类）。
	 * @return
	 */
	public boolean isImported() {
		return  !this.importedBy.isEmpty();
	}

	/**
	 * 添加导入类导入的配置类
	 * @param configClass2
	 */
	public void mergeImportedBy(ConfigurationClass otherConfigClass) {
		this.importedBy.addAll(otherConfigClass.importedBy);
	}
	
	/**
	 * 验证配置类和Bean方法的有效性
	 * 配置类不能被final修饰
	 * 静态@Bean方法没有要验证的约束
	 * @Bean 标记的方法不能为静态、final或private
	 */
	public void validate(Logger logger) throws BeanDefinitionParsingException {
		if ( Modifier.isFinal(this.source.getModifiers()) ) {
			logger.error("@Configuration不能标注于Final类，by："+this.source.getName());
			throw new BeanDefinitionParsingException("@Configuration不能标注于Final类，by："+this.source.getName());
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((source == null) ? 0 : source.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ConfigurationClass other = (ConfigurationClass) obj;
		if (source == null) {
			if (other.source != null)
				return false;
		} else if (!source.equals(other.source))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ConfigurationClass [source=" + source + ", importedBy=" + importedBy + ", beanMethods=" + beanMethods
				+ ", skippedBeanMethods=" + skippedBeanMethods + "]";
	}

	public void addImportBeanDefinitionRegistrar(ImportBeanDefinitionRegistrar registrar, AnnotationMetadata importForMetadata) {
		this.importBeanDefinitionRegistrars.put(registrar, importForMetadata);
	}

	public void setSourceClass(SourceClass parent) {
		this.sourceClass = parent;
	}

	public void addImportBy(ConfigurationClass configClass) {
		this.importedBy.add(configClass);
	}

	public AnnotationMetadata getAnnotationMetadata() {
		return this.annotationMetadata;
	}
}
