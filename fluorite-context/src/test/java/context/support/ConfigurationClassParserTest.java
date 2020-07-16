/**
 * 
 */
package context.support;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.zy.fluorite.context.support.AbstractApplicationContext;
import org.zy.fluorite.context.utils.ConfigurationClassUtils;
import org.zy.fluorite.core.annotation.Bean;
import org.zy.fluorite.core.annotation.ComponentScan;
import org.zy.fluorite.core.utils.CollectionUtils;
import org.zy.fluorite.core.utils.ReflectionUtils;
import org.zy.fluorite.core.utils.StringUtils;

/**
 * @DateTime 2020年6月21日 下午1:30:25;
 * @author zy(azurite-Y);
 * @Description
 */
@ComponentScan(basePackageClasses = {ConfigurationClassUtils.class, AbstractApplicationContext.class},value= {"org.zy.context","org.zy","com.test","org","com"})
class ConfigurationClassParserTest {

	/**
	 * 标注@Bean注解的接口方法获取测试
	 */
	@Test
	void processInterfacesTest() {
		Class<?>[] interfaces = App.class.getInterfaces();
		for (Class<?> class1 : interfaces) {
			ReflectionUtils.doWithLocalMethods(class1, method -> {
				if (method.getAnnotation(Bean.class) != null) {
					System.out.println(class1.getSimpleName()+"--"+method.getName()+"--"+method.isDefault());
				}
			});
		}
	}

	@Test
	void basePackageClassesTest2 () {
		ComponentScan annotation = ConfigurationClassParserTest.class.getAnnotation(ComponentScan.class);
		List<String> pathList = CollectionUtils.asList(annotation.value());
		Set<String> candidate = new LinkedHashSet<>();
		Class<?>[] basePackageClasses = annotation.basePackageClasses();
		for (Class<?> clz : basePackageClasses) {
			String packagePath = clz.getPackage().getName();
			pathList.add(packagePath);
		}
		System.out.println(pathList);
		
		// 标识是否找到更高级别的路径
		boolean hight = false;
		// 最短路径的索引
		int index = 0;
		// 存储根据分隔符分割路径后的最短路径数组长度
		int len = Integer.MAX_VALUE;
		for (int i = 0; i < pathList.size(); i++) {
			String element = pathList.get(i);
			String[] tokenizeToStringArray = StringUtils.tokenizeToStringArray(element, "." , null);
			if (tokenizeToStringArray.length < len) {
				index = i;
				len = tokenizeToStringArray.length;
				if (hight) { // 意味着找到更高级别的路径，所有放弃之前的选择
					System.out.print("找到更高级别的路径，所有放弃之前的选择："+candidate+"\t");
					candidate.clear();
				}
				hight = true;
				candidate.add(element);
				System.out.println("更高级别的路径："+element);
			} else if (tokenizeToStringArray.length == len) {
				String string = pathList.get(index);
				if (string == element) {
					System.out.println("相同的路径："+element);
					continue ;
				} else {
					System.out.println("同级别的路径："+element);
					candidate.add(element);
				}
			}
		}
		candidate.add(pathList.get(index));
		System.out.println("最终选择："+candidate);
	}

	@SuppressWarnings("unused")
	private void equalsLog(String name , String element,Set<String> candidate) {
		if (name.isEmpty()) {
			return ;
		}
		
		if (element.equals(name)) {
			System.out.println("重复的路径："+name+"--"+element+" 候选["+name+"]");
			candidate.add(name);
			return ;
		} else if (element.startsWith(name)) {
			System.out.println("路径冲突："+name+"--"+element+" 忽略["+ element+"]"+" 候选["+name+"]");
			candidate.add(name);
			return ;
		} else if (name.startsWith(element)) {
			System.out.println("路径冲突："+name+"--"+element+" 忽略["+ name+"]"+" 候选["+element+"]");
			candidate.add(element);
			return ;
		}
		candidate.add(element);
		candidate.add(name);
//		System.out.println(name+"--"+element);
	} 
}
class App implements AppService{
	@Override
	public App load() {
		return this;
	}
	@Override
	public App load2() {
		return this;
	}
	@Override
	public App load3() {
		return this;
	}
}

interface AppService {
	@Bean
	App load();
	@Bean
	App load2();
	@Bean
	App load3();
}