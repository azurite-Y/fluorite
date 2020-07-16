package beans;

import java.beans.PropertyDescriptor;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.zy.fluorite.beans.factory.utils.BeanUtils;


/**
 * @author: zy(azurite-Y);
 * @DateTime: 2020年6月6日 下午11:52:40;
 * @Description
 */
class BeanUtilsTest {
	private int a;
	
	public int getA() {
		return a;
	}
	public void setA(int a) {
		this.a = a;
	}
	
	@Test
	void testGetPropertyDescriptorFieldClassOfQ() {
		PropertyDescriptor propertyDescriptors;
		try {
			propertyDescriptors = BeanUtils.getPropertyDescriptor(BeanUtilsTest.class.getDeclaredField("a"),BeanUtilsTest.class);
			System.out.println(propertyDescriptors);
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		}
	}

	@Test
	void testGetPropertyDescriptorStringClassOfQ() {
		PropertyDescriptor propertyDescriptors2 = BeanUtils.getPropertyDescriptor("a",BeanUtilsTest.class);
		System.out.println(propertyDescriptors2);
	}

	@Test
	void testGetPropertyDescriptorMethodClassOfQ() {
		System.out.println(new String("setTest".toCharArray(),3,"setTest".length()-3));
		PropertyDescriptor propertyDescriptors3;
		try {
			propertyDescriptors3 = BeanUtils.getPropertyDescriptor(BeanUtilsTest.class.getMethod("setA",int.class),BeanUtilsTest.class);
			System.out.println(propertyDescriptors3);
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		}
		
	}

	@Test
	void testGetPropertyDescriptors() {
		List<PropertyDescriptor> propertyDescriptors = BeanUtils.getPropertyDescriptors(BeanUtilsTest.class);
		for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
			System.out.println(propertyDescriptor);
		}
	}

	
}
