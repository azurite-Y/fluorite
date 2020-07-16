package aop.pojo;

import java.util.List;

/**
 * @author: zy;
 * @DateTime: 2020年4月26日 上午10:37:33;
 * @Description: Aop测试使用的切点类
 */
public class UserServiceImplTest implements UserService{

	@Override
	public void say(String name,int len) {
		System.out.println("==say {String,int}==");
	}

	@Override
	public List<String> say(String name) {
		System.out.println("==say {Strign}==");
		return null;
	}

}
