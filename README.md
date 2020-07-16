Fluorite
=
SpringBoot基本实现，使用方法与SpringBoot大体相同，暂不支持内镶Servlet容器，目前只实现了自动配置、IOC、AOP三大功能。

#使用
```java
@RunnerAs(debug = false , debugFormAop = false)
public class App {
	public static void main(String[] args) {
		ConfigurableApplicationContext run = FluoriteApplication.run(App.class, args);
		run.close();
	}
}
```
---   
框架测试项目地址：https://github.com/azurite-Y/FluoriteTest<br/>
如果您有什么建议或发现的BUG，随时欢迎您的来信。联系方式：15969413461@163.com
