# <font face="宋体" color=#ED7D31>Fluorite</font>

SpringBoot 基本实现，使用方法与 SpringBoot 大体相同，目前支持自动配置、IOC、AOP、事务、Servlet 容器功能。

## <font face="宋体" color=#5B9BD5>一、使用</font>

```java
@RunnerAs(debug = false , debugFormAop = false, debugFromTransaction = false)
public class App {
	public static void main(String[] args) {
		FluoriteApplication.run(App.class, args);
	}
}
```
## <font face="宋体" color=#5B9BD5>二、测试项目地址</font>

1. 框架测试项目地址：[fluorite-test](https://gitee.com/azurite_y/fluorite-test)
2. 事务测试项目地址：[dichroite-test](https://gitee.com/azurite_y/dichroite-test)
3. 内镶 Servlet 容器测试项目地址：[moonstone-test](https://gitee.com/azurite_y/moonstone-test)

## <font face="宋体" color=#5B9BD5>三、原理概述</font>

### <font face="宋体" color=#7030A0>1、Fluorite 启动与初始化</font>

1. 由标注了 @RunnerAs 注解的根启动类调用静态的 run 方法而启动。在 @RunnerAs 中可指定 "debug"、"debugFormAop"、"debugFromTransaction" 属性控制 Fluorite 运行期间是否显示 IOC、AOP 和事务相关的详细日志信息。
2. Environment，运行当中的实现为 StandardServletEnvironment，由 ConfigurableEnvironment 接口的 customizePropertySources() 方法读取配置文件和属性填充。
3. Banner，FluoriteBootBanner 为此接口的唯一实现，用来打印横幅信息。横幅信息可像 SpringBoot 那样自定义更改，同时也可在配置文件中指定路径位置。配置文件中指定的 Banner 文件优先于项目根目录下的 Banner.txt 文件，若两处均未读取到指定的 Banner 文件则默认使用 fluorite-boot 包下的 META-INF 文件夹下的 banner-stant.txt 文件。
4. ApplicationContext 运行当中的实现为 AnnotationConfigServletWebServerApplicationContext。
5. 对于要启动所使用的 ApplicationContextInitializer 与 ApplicationListener 实现则通过 MetaFileLoader 类来读取 META-INF 文件夹下的 fluorite.factories 文件获得。

### <font face="宋体" color=#7030A0>2、包扫描</font>

1. 包扫描路径不建议以 "org"、"org.zy"、"org.zy.fluorite" 开头。在根启动类上若指定了包扫描注解则根据获得的包路径求交集。即根据文件路径的包含与被包含关系获得最靠近项目根目录的路径集合。比如说指定了三个包扫描路径 ["com.zy.controller","com.zy.service","com.zy"]，那么最终的候选路径集为 ["com.zy"]。
2. 组件扫描结果的过滤，根据 @ComponentScan 注解的 Filter 属性来配置同一扫描周期所使用的 TypeFilter 实现。同一扫描周期是指：从解析一个类获得 @ComponentScan 集合开始，并通过注解属性进行包扫描，直到本次包扫描结束为止。此为一个扫描周期。比如说现有两个类 A 和 B，这两个类都标注了 @ComponentScan 注解，且指定 "com.zy.controller" 和 "com.zy.service" 分别作为其扫描路径，那么此时解析这两个类就有两个扫描周期。
3. 本实现与 SpringBoot 相比，同样具有 @indexed 注解。此注解仅标注于 @Compont 注解上。在包扫描结果通过 TypeFilter 实现筛选之后，判断其是否标注了 @indexed 注解，标注了则为其保存组件索引。但此处与 SpringBoot 不同的是保存组件索引这一动作只有在项目开始运行解析组件类和没有组件索引文件时才会保保存。即开始进行包扫描时首先判断是否有组件索引文件存在于项目中，没有则创建然后记录标注 @indexed 注解的组件。若存在则直接读取组件索引文件内容，根据其内容创建 BeanDefinition 实例。

### <font face="宋体" color=#7030A0>3、自动配置</font>

1. 在 @RunnerAs 注解上标注了 @EnableAutoConfiguration 注解，其中使用 @Import 注解导入了AutoConfigurationImportSelector 类。由此而实现动态的导入 fluorite-autoconfigure 包下的fluorite.factories 中预制的配置类。
2. 可使用条件注解 @Conditional 及其扩展注解来控制配置的生效与否。例如 @ConditionalOnBean 或 @ConditionalOnClass。其中与 Spring 的不同在于实现判断逻辑的类 (例如：OnClassCondition) 只限定实现于 ConfigurationCondition 接口而非 Condition 这个顶级接口。目的是为了让每一个 Condition 实现能应用于组件配置扫描和其 BeanDefinition 注册阶段的任意一个地方。也就是说，对于一个 ConfigurationCondition 接口的实现类来说，它可以自行控制自身在组件注册的哪个阶段生效。当前支持的条件注解如下表所示：
| 注解                          | 功能描述指示                                                 |
| ----------------------------- | ------------------------------------------------------------ |
| @Conditional                  | 只有所有指定条件都匹配时组件才有资格注册                     |
| @ConditionalOnBean            | 只有容器中存在指定 Bean 时组件才有资格注册                   |
| @ConditionalOnMissingBean     | 只有容器中不存在指定 Bean 时组件才有资格注册                 |
| @ConditionalOnClass           | 只有类加载路径中存在指定类时组件才有资格注册                 |
| @ConditionalOnMissingClass    | 只有类加载路径中存在指定类时组件才有资格注册                 |
| @ConditionalOnProperty        | 只有指定的属性为指定的值时组件才有资格注册                   |
| @ConditionalOnResource        | 只有类加载路径下有指定资源时组件才有资格注册                 |
| @ConditionalOnSingleCandidate | 只有容器中存在指定 Bean 且可以确定单个候选时组件才有资格注册 |

> @ConditionalOnClass 与 @ConditionalOnMissingClass 的实现逻辑是建立在 Maven 中的可选依赖功能上的。根据是否拥有某个依赖项中所提供的类而做到引入依赖项而使判断特定于此依赖项的相关配置生效与否。
>

> @ConditionalOnClass 与 @ConditionalOnMissingClass 注解的 value 方法 BUG 问题：可编译通过，但在缺少指定类的环境下运行时，获取此注解会导致 ArrayStoreException 异常，其本质还是一个 ClassNotFound 异常。目前尚为解决。
>

### <font face="宋体" color=#7030A0>4、IOC</font>

1. Bean 实例化和初始化与 Spring 相同，支持生命周期 Bean。但目前只支持单例和懒加载 Bean，不支持多例 Bean，即 @Scope 注解功能还未实现。
2. 提供 AutowiredAnnotationBeanPostProcessor 和 CommonAnnotationBeanPostProcessor 进行自动注入。
3. 支持使用 @ConfigurationProperties 注解对配置文件进行解析。

### <font face="宋体" color=#7030A0>5、APO</font>

1. 对于 AOP 来说，实现了 Cglib 和 JDK 动态代理。

2. 不依赖于 AspectJ 来做切点匹配，而由 PointcutExpression 和 ClassFilter 来代替。其中切面 Advice 使用 PointcutExpression，DeclareParentsAdvisor 使用 ClassFilter 进行切点匹配。在 PointcutExpression 中，持有一个 PointcutExpressionParse 接口的实现 DefaultPointcutExpressionParse 类。其为切点表达式解析器，负责具体的切点表达式预解析匹配事务，其 parse(…) 方法返回一个永不为 null 的 PointcutMatcher 实例来描述匹配结果。使用者可在项目根目录下的 fluorite.factories 文件中指定 "org.zy.fluorite.aop.aspectj.interfaces.PointcutExpressionParse" 属性值来设置自定义的 PointcutExpressionParse 实现。

3. 切点表达式定义与 AspectJ 相似，但为了避免进行大量的字符串分割，所以此切点表达式并不完全容纳于注解的 value 属性中，而是将 AspectJ 式的切点表达式进行拆分而分布于 @Pointcut 注解的各项属性中。
4. 切点表达式的定义有两种方式：自身使用的切点表达式即直接在切面注解中指定切点表达式信息和引用连接点方法式的切点表达式。以下为选择说明：
   1. 若一个切面类中存在多个通知方法且多个通知方法都指向了一个切点，那么就宜采用引用连接点方法的方式来组织切面通知，而不是为它们单独的设置切面表达式。
   2. 本实现鼓励使用统一的连接点来对切面中定义的通知方法进行分组，通过连接点的第一次匹配结果来描述这组通知方法是否适配给定 Bean。若一个通知方法没有引用连接点方法，那么就视之为切面类中唯一的通知方法，在 PointcutExpression 也就不会缓存其切点相关信息。

5. 关于 AOP 功能使用的限制：

   1. 在匹配单个方法时可以在任意切面方法中获得参数。

   2. 在匹配多个方法时不能在切面通知方法中使用额外参数 (即除了 JoinPoint 及其子类之外的其他类型参数)，否则在进行切面方法参数绑定时会报参数异常。而匹配单个或重载方法时可以使用额外的参数类型以获取目标切点的参数值。

   3. 在适配多个方法时使用 @DeclareParents 注解引入的接口方法也会被设置 AOP 环绕。

### <font face="宋体" color=#7030A0>6、事务</font>

1. 通过在启动类上标注 @EnableTransactionManagement 注解来导入事务相关的自动配置。
2. 对于数据源当前仅支持 "druid"、"HikariCP"、"commons-dbcp2" 三种数据源，若未指定则抛出空指针异常且暂不支持设置相关的连接池参数。参见 "org.zy.fluorite.transaction.dataSource.DataSourceBuilder"。
3. 若目标切点中类或方法标注 @Transactional 注解则为其应用事务管理，方法级注释优先与类级注释。而若存在从属于类的注释则代表此切点类整体方法都需进行事务代理。

### <font face="宋体" color=#7030A0>7、Servlet支持</font>

当前支持内嵌 Moonstone 实现 Servlet Web 环境，使用自动配置即可得到一个基本的 Servlet 容器。对于具体配置参见 "org.zy.fluorite.autoconfigure.web.ServerProperties"。默认情况下启用 Servlet 容器支持。但如果不需要该功能可在配置文件中使用 "server.servlet.enable=false" 禁用。

### <font face="宋体" color=#7030A0>8、devtools</font>

默认情况下监视类加载路径下的资源变更，若在 Fluorite 运行期间发生改变则重启应用程序。对于具体配置参见 "org.zy.fluorite.boot.devtools.autoconfigure.DevToolsProperties"。可在配置文件当中使用 "fluorite.devtools.restart.enabled=false" 关闭此功能。

> 因为开发 devtools 功能模块时未想起来连同组件索引功能逻辑部分一起更新。虽然设计组件索引功能时考虑到会依托 devtools 功能，但现在因为组件索引功能还未更新，这导致如果第一次运行程序在类加载路径下生成组件索引，那么之后若新增了新组件则需要手动删除此文件以进行文件扫描。否则程序将直接读取组件索引结果而不进行文件扫描。