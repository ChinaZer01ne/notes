# Spring

## 1、IOC

## 2、DI

## 3、AOP

面向切面编程是一种思想。

她允许程序员对横切关注点或横切典型的职责分界线的行为（例如日志和事务管理）进行模块化

关注切面。

关注规则。

把规则单独分离出来作为一个独立的模块，这就是一个切面。大家都使用这个规则，那么就可以在这个切面上做一些通用的事情。

这个规则做专门的事。

举例：事物，日志，我想盖一栋房子，我不可能自己造，这时候由建筑公司来造房子，这就是个切面，造房子都去找建筑公司，专人做专事，解耦。

Spring的AOP是代理模式的体现。

## 4、spring中的工厂模式

BeanFactory生产生产不同种类的Bean，比如单例的，代理的，List类型的bean，作用于不同的bean，工厂模式隐藏了bean的实现，松耦合，便于维护。



 ## 5、进入Spring

定位、加载、注册

### 1）BeanFactory

访问Spring容器的根接口，他是所有组件的中央注册中心，Spring的依赖注入功能是被她和她的子类实现的。

```java
public interface BeanFactory {

   /**
    *对bean的转义定义，因为
    */
   String FACTORY_BEAN_PREFIX = "&";


   /**
    * 根据bean的名字，获取IOC容器的bean的实例
    */
   Object getBean(String name) throws BeansException;

   /**
    * 根据bean的名字和类型，获取IOC容器中bean的实例，增加了安全验证
    * 如果类型不匹配，会抛出BeanNotOfRequiredTypeException异常
    */
   <T> T getBean(String name, @Nullable Class<T> requiredType) throws BeansException;

   /**
    * Return an instance, which may be shared or independent, of the specified bean.
    * <p>Allows for specifying explicit constructor arguments / factory method arguments,
    * overriding the specified default arguments (if any) in the bean definition.
    * @param name the name of the bean to retrieve
    * @param args arguments to use when creating a bean instance using explicit arguments
    * (only applied when creating a new instance as opposed to retrieving an existing one)
    * @return an instance of the bean
    * @throws NoSuchBeanDefinitionException if there is no such bean definition
    * @throws BeanDefinitionStoreException if arguments have been given but
    * the affected bean isn't a prototype
    * @throws BeansException if the bean could not be created
    * @since 2.5
    */
   Object getBean(String name, Object... args) throws BeansException;

   /**
    */
   <T> T getBean(Class<T> requiredType) throws BeansException;

   /**
    */
   <T> T getBean(Class<T> requiredType, Object... args) throws BeansException;


   /**
    * 是否包含指定名称的bean 
    */
   boolean containsBean(String name);

   /**
    * 是否是单例bean
    */
   boolean isSingleton(String name) throws NoSuchBeanDefinitionException;

   /**
    * 是否是原型bean（多例）
    */
   boolean isPrototype(String name) throws NoSuchBeanDefinitionException;

   /**
    * Check whether the bean with the given name matches the specified type.
    * More specifically, check whether a {@link #getBean} call for the given name
    * would return an object that is assignable to the specified target type.
    * <p>Translates aliases back to the corresponding canonical bean name.
    * Will ask the parent factory if the bean cannot be found in this factory instance.
    * @param name the name of the bean to query
    * @param typeToMatch the type to match against (as a {@code ResolvableType})
    * @return {@code true} if the bean type matches,
    * {@code false} if it doesn't match or cannot be determined yet
    * @throws NoSuchBeanDefinitionException if there is no bean with the given name
    * @since 4.2
    * @see #getBean
    * @see #getType
    */
   boolean isTypeMatch(String name, ResolvableType typeToMatch) throws NoSuchBeanDefinitionException;

   /**
    * Check whether the bean with the given name matches the specified type.
    * More specifically, check whether a {@link #getBean} call for the given name
    * would return an object that is assignable to the specified target type.
    * <p>Translates aliases back to the corresponding canonical bean name.
    * Will ask the parent factory if the bean cannot be found in this factory instance.
    * @param name the name of the bean to query
    * @param typeToMatch the type to match against (as a {@code Class})
    * @return {@code true} if the bean type matches,
    * {@code false} if it doesn't match or cannot be determined yet
    * @throws NoSuchBeanDefinitionException if there is no bean with the given name
    * @since 2.0.1
    * @see #getBean
    * @see #getType
    */
   boolean isTypeMatch(String name, @Nullable Class<?> typeToMatch) throws NoSuchBeanDefinitionException;

   /**
    * 获取指定名称bean的Class类型
    */
   @Nullable
   Class<?> getType(String name) throws NoSuchBeanDefinitionException;

   /**
    * 得到bean的别名
    */
   String[] getAliases(String name);

}
```

### 2）ClassPathXMLApplicationContext

```java
public ClassPathXmlApplicationContext(
      String[] configLocations, boolean refresh, @Nullable ApplicationContext parent)
      throws BeansException {
	//动态确定用哪个加载器加载配置文件
   super(parent);
   //设置配置文件的位置
   setConfigLocations(configLocations);
   //如果refresh属性是true，执行刷新容器操作
   if (refresh) {
      refresh();
   }
}
```

setConfigLocations(configLocations)

```java
//AbstractRefreshableConfigApplicationContext
public void setConfigLocations(@Nullable String... locations) {
   if (locations != null) {
      Assert.noNullElements(locations, "Config locations must not be null");
       //将locations转换成数组
      this.configLocations = new String[locations.length];
      for (int i = 0; i < locations.length; i++) {
         this.configLocations[i] = resolvePath(locations[i]).trim();
      }
   }
   else {
      this.configLocations = null;
   }
}
//AbstractRefreshableConfigApplicationContext
protected String resolvePath(String path) {
    return getEnvironment().resolveRequiredPlaceholders(path);
}
//AbstractApplicationContext
@Override
public ConfigurableEnvironment getEnvironment() {
    if (this.environment == null) {
        this.environment = createEnvironment();
    }
    return this.environment;
}
//AbstractApplicationContext
protected ConfigurableEnvironment createEnvironment() {
    //创建了一个标准环境
    return new StandardEnvironment();
}
```



创建`ClassPathXMLApplicationContext`对象的时候追踪到父类`AbstractApplicationContext`,其中有静态代码块是一定要执行的

```java
//整个容器初始化只执行一次
static {
   // Eagerly load the ContextClosedEvent class to avoid weird classloader issues
   // on application shutdown in WebLogic 8.1. (Reported by Dustin Woods.)
   //为了避免应用程序在WebLogic 8.1关闭时出现类加载异常的问题，加载IOC容器关闭事件ContextClosedEvent类
   ContextClosedEvent.class.getName();
}
```

构造方法

```java
public AbstractApplicationContext() {
    /** ResourcePatternResolver used by this context */
    //获取资源文件解析器
   this.resourcePatternResolver = getResourcePatternResolver();
}
//AbstractApplicationContext
protected ResourcePatternResolver getResourcePatternResolver() {
   // AbstractApplicationContext继承了DefaultResourceLoader，可以加载配置文件
    return new PathMatchingResourcePatternResolver(this);
}
//PathMatchingResourcePatternResolver
public PathMatchingResourcePatternResolver(ResourceLoader resourceLoader) {
    Assert.notNull(resourceLoader, "ResourceLoader must not be null");
    this.resourceLoader = resourceLoader;
}
```

### 3）Refresh

刷新容器操作

```java
public void refresh() throws BeansException, IllegalStateException {
   synchronized (this.startupShutdownMonitor) {
      // Prepare this context for refreshing.
      prepareRefresh();

      // Tell the subclass to refresh the internal bean factory.
      ConfigurableListableBeanFactory beanFactory = obtainFreshBeanFactory();

      // Prepare the bean factory for use in this context.
      prepareBeanFactory(beanFactory);

      try {
         // Allows post-processing of the bean factory in context subclasses.
         postProcessBeanFactory(beanFactory);

         // Invoke factory processors registered as beans in the context.
         invokeBeanFactoryPostProcessors(beanFactory);

         // Register bean processors that intercept bean creation.
         registerBeanPostProcessors(beanFactory);

         // Initialize message source for this context.
         initMessageSource();

         // Initialize event multicaster for this context.
         initApplicationEventMulticaster();

         // Initialize other special beans in specific context subclasses.
         onRefresh();

         // Check for listener beans and register them.
         registerListeners();

         // Instantiate all remaining (non-lazy-init) singletons.
         finishBeanFactoryInitialization(beanFactory);

         // Last step: publish corresponding event.
         finishRefresh();
      }

      catch (BeansException ex) {
         if (logger.isWarnEnabled()) {
            logger.warn("Exception encountered during context initialization - " +
                  "cancelling refresh attempt: " + ex);
         }

         // Destroy already created singletons to avoid dangling resources.
         destroyBeans();

         // Reset 'active' flag.
         cancelRefresh(ex);

         // Propagate exception to caller.
         throw ex;
      }

      finally {
         // Reset common introspection caches in Spring's core, since we
         // might not ever need metadata for singleton beans anymore...
         resetCommonCaches();
      }
   }
}
```





插曲：实现ApplicationContextAware可以获取Spring容器

### 

```java
//AbstractApplicationContext
// Tell the subclass to refresh the internal bean factory.
ConfigurableListableBeanFactory beanFactory = obtainFreshBeanFactory();

//AbstractApplicationContext
protected ConfigurableListableBeanFactory obtainFreshBeanFactory() {
    //使用了委派模式，子类做事
    refreshBeanFactory();
    ConfigurableListableBeanFactory beanFactory = getBeanFactory();
    if (logger.isDebugEnabled()) {
        logger.debug("Bean factory for " + getDisplayName() + ": " + beanFactory);
    }
    return beanFactory;
}

//AbstractRefreshableApplicationContext
protected final void refreshBeanFactory() throws BeansException {
    //如果之前有工厂了，先销毁关闭
    if (hasBeanFactory()) {
        destroyBeans();
        closeBeanFactory();
    }
    try {
        //创建工厂
        DefaultListableBeanFactory beanFactory = createBeanFactory();
        //设置序列化
        beanFactory.setSerializationId(getId());
        //自定义BeanFactory
        customizeBeanFactory(beanFactory);
        //加载BeanDefinition，委派模式，调用子类的方法
        loadBeanDefinitions(beanFactory);
        synchronized (this.beanFactoryMonitor) {
            this.beanFactory = beanFactory;
        }
    }
    catch (IOException ex) {
        throw new ApplicationContextException("I/O error parsing bean definition source for " + getDisplayName(), ex);
    }
}

//loadBeanDefinitions(beanFactory);
//AbstractXmlApplicationContext#initBeanDefinitionReader
protected void loadBeanDefinitions(DefaultListableBeanFactory beanFactory) throws BeansException, IOException {
    // Create a new XmlBeanDefinitionReader for the given BeanFactory.
    //创建了读取流
    XmlBeanDefinitionReader beanDefinitionReader = new XmlBeanDefinitionReader(beanFactory);

    // Configure the bean definition reader with this context's
    // resource loading environment.
    //设置环境
    beanDefinitionReader.setEnvironment(this.getEnvironment());
    beanDefinitionReader.setResourceLoader(this);
    beanDefinitionReader.setEntityResolver(new ResourceEntityResolver(this));

    // Allow a subclass to provide custom initialization of the reader,
    // then proceed with actually loading the bean definitions.
   //校验文件信息
    initBeanDefinitionReader(beanDefinitionReader);
     //加载BeanDefinition信息
    loadBeanDefinitions(beanDefinitionReader);
}

```

