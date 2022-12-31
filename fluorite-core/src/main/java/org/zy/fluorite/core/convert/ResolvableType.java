package org.zy.fluorite.core.convert;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;

import org.zy.fluorite.core.utils.Assert;

/**
 * @author: zy(azurite-Y);
 * @DateTime: 2020年6月7日 下午5:19:20;
 * @Description 封装Javajava.lang.reflect.Type实现，提供对超类型、接口和泛型参数的访问，以及最终解析为java.lang.Class类.
 */
public class ResolvableType {
	private final Type requiredType;

	private Class<?> resolved;

	private static final ResolvableType[] EMPTY_TYPES_ARRAY = new ResolvableType[0];
	public static final ResolvableType NONE = new ResolvableType(null);

	/** Class对象的直接父类 */
	private volatile ResolvableType superType;

	/** Class对象的接口数组 */
	private volatile ResolvableType[] interfaces;

	/** Class对象的泛型数组 */
	private volatile ResolvableType[] generics;

	private ResolvableType(Class<?> requiredType) {
		this.requiredType = (requiredType == null) ? Object.class : requiredType;
	}

	private ResolvableType(Type requiredType) {
		this.requiredType = requiredType == null ? Object.class : requiredType;
	}

	/**
	 * 根据指定Type对象创建一个ResolvableType对象
	 * 
	 * @param requiredType
	 * @return
	 */
	public static ResolvableType forClass(Type type) {
		return new ResolvableType(type);
	}

	/**
	 * 根据指定Class创建一个ResolvableType对象
	 * 
	 * @apiNote 传入Class对象 获取的泛型只能获取到父类和接口的泛型，而无法获取当前Class的泛型。
	 * 若无获取则使用 {@link ResolvableType#forClass(Type) } 方法
	 * @param requiredType
	 * @return 一个 ResolvableType 实例
	 */ 
	public static ResolvableType forClass(Class<?> requiredType) {
		return new ResolvableType(requiredType);
	}

	/**
	 * 获得ResolvableType持有的Class类解析结果
	 * @return
	 */
	public Class<?> resolve() {
		return this.resolved != null ? this.resolved : resolveClass();
	}

	/** 
	 * 获得ResolvableType持有的Class类解析结果，但不管本类是否持有解析结果都会解析原始Class<br/>
	 * 若只是获取ResolvableType持有的解析类推荐 {@linkplain ResolvableType#resolve()} 方法
	 */
	public Class<?> resolveClass() {
		if (this.requiredType instanceof Class) {
			return (Class<?>) this.requiredType;
		} else if (this.requiredType instanceof ParameterizedType) {
			Type rawType = ((ParameterizedType)this.requiredType).getRawType();
			if (rawType instanceof Class)
			return (Class<?>) rawType;
		}
		return null;
	}

	/**
	 * 如果此类型包含泛型参数，则返回true。
	 * 
	 * @return
	 */
	public boolean hasGenerics() {
		return (getGenerics().length > 0);
	}

	public ResolvableType[] getGenerics() {
		if (this.requiredType == Object.class) {
			return EMPTY_TYPES_ARRAY;
		}

		if (this.generics == null) {
			if (this.requiredType instanceof Class) {
				// 获得TypeVariable对象数组，表示此泛型声明的类型
				Type[] typeParams = ((Class<?>) this.requiredType).getTypeParameters();
				generics = new ResolvableType[typeParams.length];
				for (int i = 0; i < generics.length; i++) {
					generics[i] = ResolvableType.forClass(typeParams[i]);
				}
			} else if (this.requiredType instanceof ParameterizedType) {
				Type[] actualTypeArguments = ((ParameterizedType) this.requiredType).getActualTypeArguments();
				generics = new ResolvableType[actualTypeArguments.length];
				for (int i = 0; i < actualTypeArguments.length; i++) {
					generics[i] = ResolvableType.forClass(actualTypeArguments[i]);
					// 递归解析
					generics[i].getGenerics();
				}
			}
		}
		return generics;
	}

	/**
	 * 获得第一个泛型信息
	 * @return
	 */
	public Class<?> getGenericToClass() {
		return getGeneric(0).resolve();
	}
	
	/**
	 * 获得第一个泛型信息
	 * @return
	 */
	public ResolvableType getGeneric() {
		return getGeneric(0);
	}
	
	/**
	 * 获得指定位置的泛型信息，从0开始
	 * 
	 * @param indexe
	 * @return
	 */
	public ResolvableType getGeneric(int index) {
		ResolvableType[] generics = getGenerics();
		if (generics == null || index < 0 || index >= generics.length) {
			return null;
		}
		return generics[index];
	}

	/**
	 * 获得当前类的接口
	 * 
	 * @return
	 */
	public ResolvableType[] getInterfaces() {
		Class<?> resolved = resolve();
		if (resolved == Object.class) {
			return EMPTY_TYPES_ARRAY;
		}
		if (this.interfaces == null) {
			Type[] genericIfcs = resolved.getGenericInterfaces();
			this.interfaces = new ResolvableType[genericIfcs.length];
			for (int i = 0; i < genericIfcs.length; i++) {
				interfaces[i] = forClass(genericIfcs[i]);
			}
		}
		return interfaces;
	}

	/**
	 * 验证当期类是否实现了指定接口，实现了则返回此接口类型的ResolvableType对象，未实现则继续检查父类
	 * 
	 * @param type
	 * @return
	 */
	public ResolvableType as(Class<?> type) {
		if (this == NONE) {
			return NONE;
		}
		Class<?> resolved = resolve();
		if (resolved == null || resolved == type) {
			return this;
		}
		for (ResolvableType interfaceType : getInterfaces()) {
			ResolvableType interfaceAsType = interfaceType.as(type);
			if (interfaceAsType != NONE) {
				return interfaceAsType;
			}
		}
		return getSuperType().as(type);
	}
	
	/**
	 * 获得当前Class的父类
	 * 
	 * @return
	 */
	public ResolvableType getSuperType() {
		Class<?> resolved = resolve();
		if (resolved == Object.class || resolved.getGenericSuperclass() == null) {
			return null;
		}
		if (this.superType == null) {
			superType = forClass((Class<?>) resolved.getGenericSuperclass());
		}
		return superType;
	}

	/**
	 * 获得此ResolvableType对象的源类型
	 * @return
	 */
	public Type getRequiredType() {
		return requiredType;
	}

	/**
	 * 该类型是否是指定类型本身或其父类型
	 * 
	 * @param type
	 * @return true - 是指定类型本身或父类型
	 */
	public boolean isAssignableFrom(ResolvableType eventType) {
		return this.resolve().isAssignableFrom(eventType.resolveClass());
	}

	/**
	 * 判断当前类型是否是指定类型本身或其父类型
	 * 
	 * @param type
	 * @return true - 是指定类型本身或父类型
	 */
	public boolean isAssignableFrom(Class<?> type) {
		return this.resolve().isAssignableFrom(type);
	}

	/**
	 * 该类型是否是指定类型本身或其父类型
	 * 
	 * @param type
	 * @return true - 是指定类型本身或父类型
	 */
	public boolean isAssignableFrom(Object obj) {
		return obj instanceof ResolvableType ? this.isAssignableFrom((ResolvableType) obj)
				: (obj instanceof Class<?> ? this.isAssignableFrom((Class<?>) obj)
						: this.isAssignableFrom(obj.getClass()));
	}

	/**
	 * 返回具有给定实现类的指定基类型(接口或基类)的 {@link ResolvableType} 。例如: {@code ResolvableType.forClass(List.class, MyArrayList.class)}
	 * 
	 * @param baseType - 基类型(不能为空)
	 * @param implementationClass - 实现类
	 * @return 给定实现类支持的指定基类型的 {@link ResolvableType} 
	 * 
	 * @see #forClass(Class)
	 */
	public static ResolvableType forClass(Class<?> baseType, Class<?> implementationClass) {
		Assert.notNull(baseType, "baseType 不能为 null");
		return forClass(implementationClass).as(baseType);
	}
	
	@Override
	public String toString() {
		return "ResolvableType [requiredType=" + requiredType + ", superType=" + superType + ", interfaces="
				+ Arrays.toString(interfaces) + ", generics=" + Arrays.toString(generics) + "]";
	}
}
