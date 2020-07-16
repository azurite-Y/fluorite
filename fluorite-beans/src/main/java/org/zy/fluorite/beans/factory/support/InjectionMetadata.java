package org.zy.fluorite.beans.factory.support;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zy.fluorite.beans.beanDefinittion.RootBeanDefinition;
import org.zy.fluorite.beans.interfaces.PropertyValues;
import org.zy.fluorite.core.utils.DebugUtils;
import org.zy.fluorite.core.utils.ReflectionUtils;

/**
 * @author: zy(azurite-Y);
 * @DateTime: 2020年6月6日 下午2:30:41;
 * @Description 封装注入元数据的相关属性
 */
public class InjectionMetadata {
	protected static Logger logger = LoggerFactory.getLogger(InjectionMetadata.class);

	private final Class<?> targetClass;

	private final List<InjectedElement> injectedElements;

	public InjectionMetadata(Class<?> targetClass, List<InjectedElement> elements) {
		this.targetClass = targetClass;
		this.injectedElements = elements;
	}

	/**
	 * 将注入属性保存到RootBeanDefinition对象中
	 * 
	 * @param beanDefinition
	 */
	public void checkConfigMembers(RootBeanDefinition beanDefinition) {
		for (InjectedElement element : this.injectedElements) {
			Member member = element.getMember();
			if (!beanDefinition.isConfigMember(member)) {
				element.setChecked(true);
				beanDefinition.registerConfigMember(member);
				if (DebugUtils.debug) {
					logger.info("注册注入"+(member instanceof Field ? "属性" : "方法")+" ，by class：" + this.targetClass.getName() + "，InjectedElement：" + member.getName());
				}
			}
		}
	}

	public void inject(Object bean, String beanName, PropertyValues pvs) throws Throwable {
		if (!this.injectedElements.isEmpty()) {
			for (InjectedElement element : this.injectedElements) {
				element.inject(bean, beanName, pvs);
			}
		}
	}

	/**
	 * 是否需要刷新
	 * 
	 * @param metadata
	 * @param clazz
	 * @return
	 */
	public static boolean needsRefresh(InjectionMetadata metadata, Class<?> clazz) {
		return (metadata == null || metadata.targetClass != clazz);
	}

	/** 一个单例的注入元素 */
	public abstract static class InjectedElement {

		/** 可存储构造器、方法、属性对象 */
		protected final Member member;

		protected final boolean isField;

		/** PropertyDescriptor描述了Java Bean通过一对访问器方法导出的一个属性 */
		protected final PropertyDescriptor pd;

		protected volatile boolean checked;

		protected InjectedElement(Member member, PropertyDescriptor pd) {
			this.member = member;
			this.isField = (member instanceof Field);
			this.pd = pd;
		}

		public final Member getMember() {
			return this.member;
		}

		public boolean isChecked() {
			return checked;
		}

		public void setChecked(boolean checked) {
			this.checked = checked;
		}

		protected final Class<?> getResourceType() {
			if (this.isField) {
				return ((Field) this.member).getType();
			} else if (this.pd != null) {
				return this.pd.getPropertyType();
			} else {
				return ((Method) this.member).getParameterTypes()[0];
			}
		}

		protected final void checkResourceType(Class<?> resourceType) {
			if (this.isField) {
				Class<?> fieldType = ((Field) this.member).getType();
				if (!(resourceType.isAssignableFrom(fieldType) || fieldType.isAssignableFrom(resourceType))) {
					throw new IllegalStateException(
							"具体的属性类型 [" + fieldType + "] 和源属性类型 [" + resourceType.getName() + "]不相符");
				}
			} else {
				Class<?> paramType = (this.pd != null ? this.pd.getPropertyType()
						: ((Method) this.member).getParameterTypes()[0]);
				if (!(resourceType.isAssignableFrom(paramType) || paramType.isAssignableFrom(resourceType))) {
					throw new IllegalStateException(
							"具体的参数类型 [" + paramType + "] 和源参数类型 [" + resourceType.getName() + "]不相符");
				}
			}
		}

		/**
		 * 关联具体的属性类型和属性值
		 */
		protected void inject(Object bean, String requestingBeanName, PropertyValues pvs)	throws Throwable {
			if (this.isField) {
				Field field = (Field) this.member;
				ReflectionUtils.makeAccessible(field);
				Object inject = getResourceToInject(bean, requestingBeanName);
				// 设置属性值
				field.set(bean, inject);
				DebugUtils.log(logger, "进行属性注入，by fieldName："+field.getName()+"，fieldValue："+inject);
			} else {
				try {
					Method method = (Method) this.member;
					ReflectionUtils.makeAccessible(method);
					// 在设置方法的参数之后反射调用方法
					Object inject = getResourceToInject(bean, requestingBeanName);
					method.invoke(bean, inject);
					DebugUtils.log(logger, "调用自动注入方法，by methodName："+method.getName()+"，methodValue："+inject);
				}	catch (InvocationTargetException ex) {
					throw ex.getTargetException();
				}
			}
		}

		/**
		 * 返回属性值或单个方法参数值
		 * 
		 * @param target
		 * @param requestingBeanName
		 * @return
		 */
		protected Object getResourceToInject(Object target, String requestingBeanName) {
			return null;
		}

		@Override
		public boolean equals(Object other) {
			if (this == other) {
				return true;
			}
			if (!(other instanceof InjectedElement)) {
				return false;
			}
			InjectedElement otherElement = (InjectedElement) other;
			return this.member.equals(otherElement.member);
		}

		@Override
		public int hashCode() {
			return this.member.getClass().hashCode() * 29 + this.member.getName().hashCode();
		}

		@Override
		public String toString() {
			return getClass().getSimpleName() + " for " + this.member;
		}
	}
}
