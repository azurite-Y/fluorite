/**
 * 
 */
package beans;

import org.junit.jupiter.api.Test;
import org.zy.fluorite.beans.factory.utils.BeanFactoryUtils;

/**
 * @author: zy(azurite-Y);
 * @DateTime: 2020年6月9日 下午1:00:57;
 * @Description
 */
class BeanFactoryUtilsTest {

	/**
	 * {@link beans.factory.utils.BeanFactoryUtils#transformedBeanName(java.lang.String)} 的测试方法。
	 */
	@Test
	void testTransformedBeanName() {
		String transformedBeanName = BeanFactoryUtils.transformedBeanName("&BeanName");
		System.out.println(transformedBeanName);
	}

}
