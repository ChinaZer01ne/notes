# Spring Boot



## 源码

SpringApplication.run()

```java
// SpringApplication#295
/**
	 * Run the Spring application, creating and refreshing a new
	 * {@link ApplicationContext}.
	 * @param args the application arguments (usually passed from a Java main method)
	 * @return a running {@link ApplicationContext}
	 */
	public ConfigurableApplicationContext run(String... args) {
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		ConfigurableApplicationContext context = null;
		Collection<SpringBootExceptionReporter> exceptionReporters = new ArrayList<>();
		configureHeadlessProperty();
		SpringApplicationRunListeners listeners = getRunListeners(args);
		listeners.starting();
		try {
			ApplicationArguments applicationArguments = new DefaultApplicationArguments(
					args);
			ConfigurableEnvironment environment = prepareEnvironment(listeners,
					applicationArguments);
			configureIgnoreBeanInfo(environment);
			Banner printedBanner = printBanner(environment);
            // 创建ApplicationContext
			context = createApplicationContext();
			exceptionReporters = getSpringFactoriesInstances(
					SpringBootExceptionReporter.class,
					new Class[] { ConfigurableApplicationContext.class }, context);
			prepareContext(context, environment, listeners, applicationArguments,
					printedBanner);
			refreshContext(context);
			afterRefresh(context, applicationArguments);
			stopWatch.stop();
			if (this.logStartupInfo) {
				new StartupInfoLogger(this.mainApplicationClass)
						.logStarted(getApplicationLog(), stopWatch);
			}
			listeners.started(context);
			callRunners(context, applicationArguments);
		}
		catch (Throwable ex) {
			handleRunFailure(context, ex, exceptionReporters, listeners);
			throw new IllegalStateException(ex);
		}

		try {
			listeners.running(context);
		}
		catch (Throwable ex) {
			handleRunFailure(context, ex, exceptionReporters, null);
			throw new IllegalStateException(ex);
		}
		return context;
	}

```





```java
// SpringApplication#585
/**
	 * Strategy method used to create the {@link ApplicationContext}. By default this
	 * method will respect any explicitly set application context or application context
	 * class before falling back to a suitable default.
	 * @return the application context (not yet refreshed)
	 * @see #setApplicationContextClass(Class)
	 */
	protected ConfigurableApplicationContext createApplicationContext() {
		Class<?> contextClass = this.applicationContextClass;
		if (contextClass == null) {
			try {
				switch (this.webApplicationType) {
				case SERVLET:
                        // 通过反射创建一个容器
					contextClass = Class.forName(DEFAULT_SERVLET_WEB_CONTEXT_CLASS);
					break;
				case REACTIVE:
					contextClass = Class.forName(DEFAULT_REACTIVE_WEB_CONTEXT_CLASS);
					break;
				default:
					contextClass = Class.forName(DEFAULT_CONTEXT_CLASS);
				}
			}
			catch (ClassNotFoundException ex) {
				throw new IllegalStateException(
						"Unable create a default ApplicationContext, "
								+ "please specify an ApplicationContextClass",
						ex);
			}
		}
		return (ConfigurableApplicationContext) BeanUtils.instantiateClass(contextClass);
	}

```



```java
// SpringApplication#368
/** 容器的一些配置*/
private void prepareContext(ConfigurableApplicationContext context,
			ConfigurableEnvironment environment, SpringApplicationRunListeners listeners,
			ApplicationArguments applicationArguments, Banner printedBanner) {
		context.setEnvironment(environment);
		postProcessApplicationContext(context);
    	// 设置springboot的初始化器
		applyInitializers(context);
		listeners.contextPrepared(context);
		if (this.logStartupInfo) {
			logStartupInfo(context.getParent() == null);
			logStartupProfileInfo(context);
		}
		// Add boot specific singleton beans
		ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
		beanFactory.registerSingleton("springApplicationArguments", applicationArguments);
		if (printedBanner != null) {
			beanFactory.registerSingleton("springBootBanner", printedBanner);
		}
		if (beanFactory instanceof DefaultListableBeanFactory) {
			((DefaultListableBeanFactory) beanFactory)
					.setAllowBeanDefinitionOverriding(this.allowBeanDefinitionOverriding);
		}
		// Load the sources
		Set<Object> sources = getAllSources();
		Assert.notEmpty(sources, "Sources must not be empty");
    	// 加载bean
		load(context, sources.toArray(new Object[0]));
		listeners.contextLoaded(context);
	}
```

## 1、Spring Boot简介



## 2、Spring Boot注解

**@SpringBootApplication**: SpringBoot应用标注在某个类上说明这个类是SpringBoot的主配置类，SpringBoot就应该运行这个类的main方法来启动SpringBoot应用。

这个注解是一个组合注解：

```
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@SpringBootConfiguration
@EnableAutoConfiguration
@ComponentScan(
    excludeFilters = {@Filter(
    type = FilterType.CUSTOM,
    classes = {TypeExcludeFilter.class}
), @Filter(
    type = FilterType.CUSTOM,
    classes = {AutoConfigurationExcludeFilter.class}
)}
)
public @interface SpringBootApplication {
```

**@SpringBootConfiguration**：表示一个SpringBoot的配置类；

该注解也是一个组合注解：

```
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Configuration
public @interface SpringBootConfiguration {
```

​	**@Configuration**:标注这个注解表示该类是一个配置类，是容器中的一个组件；

**@EnableAutoConfiguration**：开启自动配置功能；

​	以前我们需要配置的东西，SpringBoot帮我们自动配置；该注解告诉SpringBoot开启自动配置功能；这样自动配置才能生效；

该注解是个组合注解：

```java
@AutoConfigurationPackage
@Import(EnableAutoConfigurationImportSelector.class)
public @interface EnableAutoConfiguration {
```

​	 	@**AutoConfigurationPackage**：自动配置包

```java
@Import({Registrar.class})
public @interface AutoConfigurationPackage {
```

​			@**Import**(AutoConfigurationPackages.Registrar.class)：

​			Spring的底层注解@Import，给容器中导入一个组件；导入的组件由AutoConfigurationPackages.Registrar.class；将主配置类（@SpringBootApplication标注的类）的所在包及其所有子包里面的所有组件扫描到Spring容器中；

​			@**Import**(EnableAutoConfigurationImportSelector.class)；

​				**EnableAutoConfigurationImportSelector**：导入那些组件选择器？

```java
List<String> configurations = this.getCandidateConfigurations(annotationMetadata, attributes);
```

​				将所有需要导入的组件以全类名的方式返回，这些组件会被添加到容器中；

​				会给容器中导入非常多的自动配置类（xxAutoConfiguration）;就是给容器中导入这个场景需要的所有组件，并自动配置。

![1529308081199](images\springboot\1529308081199.png)

有了自动配置类，免去了我们手动编写配置注入功能组件等工作；

```java
List<String> configurations = this.getCandidateConfigurations(annotationMetadata, attributes);
```

```java
List<String> configurations = SpringFactoriesLoader.loadFactoryNames(this.getSpringFactoriesLoaderFactoryClass(), this.getBeanClassLoader());
```

```java
return (List)loadSpringFactories(classLoader).getOrDefault(factoryClassName, Collections.emptyList());
```

```java
Enumeration<URL> urls = classLoader != null ? classLoader.getResources("META-INF/spring.factories") : ClassLoader.getSystemResources("META-INF/spring.factories");
```

从断点运行可以追踪到，SpringBoot在启动的时候从类路径下的`META-INF/spring.factories`中获取`EnableAutoConfiguration`指定的值，将这些值作为自动配置类导入到容器中，自动配置类就生效，帮我们进行自动配置工作；

J2EE的整体整合解决方案和自动配置都在`spring-boot-autoconfigure-version.RELEASE.jar`；

## 3、yml文件

### 1)、值的写法

#### 字面量：普通的值（数字，字符串，布尔）

​	k: v: 字面直接来写；

​		字符串默认不用加上单引号或者双引号

​		"":双引号；不会转移字符串里面的特殊字符；特殊字符会作为本身想表示的意思

​			name："zhangsan \n lisi":输出：zhangsan 换行 lisi

​		''：单引号；会转移特殊字符，特殊字符最终知识一个普通的资字符转数据

​			name:'zhangsan \n list':输出：zhangsan \n lisi

#### 对象、Map（属性和值）（键值对）：

​	k: v: 在下一行来写对象的属性和值的关系；注意缩进

​		对象还是k: v:的方式

```yacas
friends:
    lastName: zhangsan
    age: 20
```

行内写法：

```yaml
friends: {lastName: zhangsan,age: 18}
```

#### 数组（List、Set）

用-值表示数组中的一个元素

```yaml
pets:
 - cat
 - dog
 - pig
```

行内写法：

```yaml
pets: [cat,dog,pig]
```



###2）配置文件值注入



或者直接从代码中读取配置文件的值：

yml文件：

```yaml
uri: www.www.www
```

注入Environment

```java
@Autowired
Environment environment;
```

获取值：

```java
environment.getProperty("uri")
```





## 问题



### 不同模块之间依赖，bean扫描不到的问题

https://blog.csdn.net/ignorewho/article/details/84978800