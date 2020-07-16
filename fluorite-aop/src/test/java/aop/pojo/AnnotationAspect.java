package aop.pojo;

import org.zy.fluorite.aop.aspectj.annotation.Aspect;
import org.zy.fluorite.aop.aspectj.annotation.Before;
import org.zy.fluorite.aop.aspectj.annotation.Pointcut;

/**
 * @DateTime 2020年7月10日 上午9:27:44;
 * @author zy(azurite-Y);
 * @Description Aop测试所使用的切面类
 */
@Aspect
public class AnnotationAspect {
	/**
	 * 定义一个方法,用于声明切点表达式,该方法方法体一般没有内容
	 * @Pointcut - 用来声明切点表达式 通知直接使用定义的方法名即可引入当前的切点表达式
	 */
	@Pointcut(value = "aop.pojo.UserServiceImplTest.say()" ,args = {String.class,int.class} 
		, prefix = "execution" , returnType = "void" )
	public void Pointcut() {}
	
	@Before(value = "Pointcut()")
	public void myBefore() {
	}
	
	//---
	@Pointcut(value = "aop.pojo..")
	public void Pointcut2() {}

	@Before(value = "Pointcut2()")
	public void myAfter() {}
	
	//---
	@Pointcut(value = "aop.pojo..say()" , args = {String.class} , returnType = "List<String>" )
	public void Pointcut3() {}

	@Before(value = "Pointcut3()")
	public void myAfterReturn() {}
	
	//---
	@Pointcut(value = "aop.pojo.UserServiceImplTest.*")
	public void Pointcut4() {}

	@Before(value = "Pointcut4()")
	public void myThrow() {}
}
