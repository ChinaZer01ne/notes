# Spring5源码分析

我么获得Spring容器的时候，究竟执行了什么操作？

##1.进入Spring



我们从这一句开始入手：

```java
ClassPathXmlApplicationContext context = 
    new ClassPathXmlApplicationContext("classpath:*Context.xml");
```

这句代码，是间接的执行了ClassPathXmlApplicationContext的这个构造方法

```java
public class ClassPathXmlApplicationContext extends AbstractXmlApplicationContext {
    
	//...省略
    
    public ClassPathXmlApplicationContext(String[] configLocations, boolean refresh, ApplicationContext parent) throws BeansException {
        //调用父类的构造方法
        super(parent);
        //设置配置文件
        this.setConfigLocations(configLocations);
        //是否刷新容器（加载bean的操作）
        if (refresh) {
            this.refresh();
        }
    }
 
	//...省略

}
```



## 2.Spring容器的初始化



Spring容器的初始化过程中，AbstractApplicationContext是一个核心类，大部分操作都是在这里进行的。



下面我们就一步一步进行详细的解释

### 2.1 super(parent)

从ClassPathXmlApplicationContext追溯到他的父类，我发现是在AbstractApplicationContext类的构造器中执行了如下操作



程序里的源码：

```java
public AbstractApplicationContext() {
    //日志记录器
    this.logger = LogFactory.getLog(this.getClass());
    //上下文的唯一id
    this.id = ObjectUtils.identityToString(this);
    //显示的名称
    this.displayName = ObjectUtils.identityToString(this);
    //在refresh中应用beanfactorypost处理器
    this.beanFactoryPostProcessors = new ArrayList();
    //当前上下文的状态
    this.active = new AtomicBoolean();
    this.closed = new AtomicBoolean();
    //用于“刷新”和“销毁”的同步监视器
    this.startupShutdownMonitor = new Object();
    //静态指定监听器
    this.applicationListeners = new LinkedHashSet();
    //解析资源文件
    this.resourcePatternResolver = this.getResourcePatternResolver();
}
```



**小伙伴们注意了！**



你可能从官网down下源码来发现和程序里运行的源码不一样！

官网的源码：

```java
public AbstractApplicationContext() {
	this.resourcePatternResolver = getResourcePatternResolver();
}
```

其实在官网源码缺少的部分，在定义属性的时候直接赋值操作了，这都是题外话了。



### 2.2 refresh方法

这是Spring容器初始化的一个核心方法！

```java
public void refresh() throws BeansException, IllegalStateException {
    //之前说过了
    Object var1 = this.startupShutdownMonitor;
    //同步处理，否则还没有加载完容器，执行其他操作，会有异常
    synchronized(this.startupShutdownMonitor) {
        /*
         准备工作，主要是设置启动时间，设置容器的启动状态，初始化占位符资源，验证所有被标记为required			的属性是不是可以被解析的
        */
        this.prepareRefresh();
        ConfigurableListableBeanFactory beanFactory = this.obtainFreshBeanFactory();
        this.prepareBeanFactory(beanFactory);

        try {
            this.postProcessBeanFactory(beanFactory);
            this.invokeBeanFactoryPostProcessors(beanFactory);
            this.registerBeanPostProcessors(beanFactory);
            this.initMessageSource();
            this.initApplicationEventMulticaster();
            this.onRefresh();
            this.registerListeners();
            this.finishBeanFactoryInitialization(beanFactory);
            this.finishRefresh();
        } catch (BeansException var9) {
            if (this.logger.isWarnEnabled()) {
                this.logger.warn("Exception encountered during context initialization - cancelling refresh attempt: " + var9);
            }

            this.destroyBeans();
            this.cancelRefresh(var9);
            throw var9;
        } finally {
            this.resetCommonCaches();
        }

    }
}
```



#### 2.2.1 prepareRefresh方法



```java
protected void prepareRefresh() {
    //设置启动时间
    this.startupDate = System.currentTimeMillis();
    //设置容器的启动状态
    this.closed.set(false);
    this.active.set(true);
	//记录信息
    if (logger.isInfoEnabled()) {
        logger.info("Refreshing " + this);
    }

    //初始化占位符资源
    initPropertySources();

    // 验证所有被标记为required的属性是不是可以被解析的，
    getEnvironment().validateRequiredProperties();

    // Allow for the collection of early ApplicationEvents,
    // to be published once the multicaster is available...
    this.earlyApplicationEvents = new LinkedHashSet<>();
}
```



#### 2.2.2 obtainFreshBeanFactory方法



```java
protected ConfigurableListableBeanFactory obtainFreshBeanFactory() {
    /*
    当前的this是ClassPathXmlApplicationContext，我追溯了一下，执行的是
    AbstractRefreshableApplicationContext的	refreshBeanFactory方法
    */
    this.refreshBeanFactory();
    ConfigurableListableBeanFactory beanFactory = this.getBeanFactory();
    if (this.logger.isDebugEnabled()) {
        this.logger.debug("Bean factory for " + this.getDisplayName() + ": " + beanFactory);
    }

    return beanFactory;
}
```



##### 2.2.2.1 refreshBeanFactory方法



```java
protected final void refreshBeanFactory() throws BeansException {
    //如果当前DefaultListableBeanFactory加载过DefaultListableBeanFactory，先销毁关掉
    if (this.hasBeanFactory()) {
        this.destroyBeans();
        this.closeBeanFactory();
    }
	//初始化新的bean Factory
    try {
        //创建了一个DefaultListableBeanFactory（这个类很重要，一定要说一下）
        DefaultListableBeanFactory beanFactory = this.createBeanFactory();
        //设置BeanFactory的序列化id
        beanFactory.setSerializationId(this.getId());
        this.customizeBeanFactory(beanFactory);
        //加载bean到beanFactory中
        this.loadBeanDefinitions(beanFactory);
        Object var2 = this.beanFactoryMonitor;
        synchronized(this.beanFactoryMonitor) {
            this.beanFactory = beanFactory;
        }
    } catch (IOException var5) {
        throw new ApplicationContextException("I/O error parsing bean definition source for " + this.getDisplayName(), var5);
    }
}
```



##### 2.2.2.2 customizeBeanFactory



```java
protected void customizeBeanFactory(DefaultListableBeanFactory beanFactory) {
    //设置bean是否覆盖操作
   if (this.allowBeanDefinitionOverriding != null) {
      beanFactory.setAllowBeanDefinitionOverriding(this.allowBeanDefinitionOverriding);
   }
    //设置bean是否是循环引用
   if (this.allowCircularReferences != null) {
      beanFactory.setAllowCircularReferences(this.allowCircularReferences);
   }
}
```





##### 2.2.2.3 loadBeanDefinitions

```java
protected void loadBeanDefinitions(DefaultListableBeanFactory beanFactory) throws BeansException, IOException {
   // 给beanFactory创建了一个 XmlBeanDefinitionReader 
   XmlBeanDefinitionReader beanDefinitionReader = new XmlBeanDefinitionReader(beanFactory);

   // Configure the bean definition reader with this context's
   // resource loading environment.
   beanDefinitionReader.setEnvironment(this.getEnvironment());
   beanDefinitionReader.setResourceLoader(this);
   beanDefinitionReader.setEntityResolver(new ResourceEntityResolver(this));

   // Allow a subclass to provide custom initialization of the reader,
   // then proceed with actually loading the bean definitions.
   initBeanDefinitionReader(beanDefinitionReader);
    //注意，此时开始来加载bean了
   loadBeanDefinitions(beanDefinitionReader);
}
```



##### 2.2.2.4 loadBeanDefinitions

```java
protected void loadBeanDefinitions(XmlBeanDefinitionReader reader) throws BeansException, IOException {

   Resource[] configResources = getConfigResources();
   if (configResources != null) {
      reader.loadBeanDefinitions(configResources);
   }
    //从我们的本地路径加载了xml文件
   String[] configLocations = getConfigLocations();
   if (configLocations != null) {
      reader.loadBeanDefinitions(configLocations);
   }
}

//以下是调用AbstractBeanDefinitionReader类中的方法
public int loadBeanDefinitions(String... locations) throws BeanDefinitionStoreException {
    Assert.notNull(locations, "Location array must not be null");
    int counter = 0;
    for (String location : locations) {
        counter += loadBeanDefinitions(location);
    }
    return counter;
}

public int loadBeanDefinitions(String location) throws BeanDefinitionStoreException {
    return loadBeanDefinitions(location, null);
}

public int loadBeanDefinitions(String location, @Nullable Set<Resource> actualResources) throws BeanDefinitionStoreException {
    ResourceLoader resourceLoader = getResourceLoader();
    if (resourceLoader == null) {
        throw new BeanDefinitionStoreException(
            "Cannot import bean definitions from location [" + location + "]: no ResourceLoader available");
    }

    if (resourceLoader instanceof ResourcePatternResolver) {
        // Resource pattern matching available.
        try {
            //获取所有配置文件，转换成了Resource，是个数组
            Resource[] resources = ((ResourcePatternResolver) resourceLoader).getResources(location);
            //注意：这一句开始很重要，通过资源
            int loadCount = loadBeanDefinitions(resources);
            if (actualResources != null) {
                for (Resource resource : resources) {
                    actualResources.add(resource);
                }
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Loaded " + loadCount + " bean definitions from location pattern [" + location + "]");
            }
            return loadCount;
        }
        catch (IOException ex) {
            throw new BeanDefinitionStoreException(
                "Could not resolve bean definition resource pattern [" + location + "]", ex);
        }
    }
    else {
        // Can only load single resources by absolute URL.
        Resource resource = resourceLoader.getResource(location);
        int loadCount = loadBeanDefinitions(resource);
        if (actualResources != null) {
            actualResources.add(resource);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Loaded " + loadCount + " bean definitions from location [" + location + "]");
        }
        return loadCount;
    }
}


// XmlBeanDefinitionReader 303
@Override
public int loadBeanDefinitions(Resource resource) throws BeanDefinitionStoreException {
   return loadBeanDefinitions(new EncodedResource(resource));
}

// XmlBeanDefinitionReader 314
public int loadBeanDefinitions(EncodedResource encodedResource) throws BeanDefinitionStoreException {
   Assert.notNull(encodedResource, "EncodedResource must not be null");
   if (logger.isInfoEnabled()) {
      logger.info("Loading XML bean definitions from " + encodedResource.getResource());
   }
   // 创建了ThreadLocal的set，存放所有的配置文件
   Set<EncodedResource> currentResources = this.resourcesCurrentlyBeingLoaded.get();
   if (currentResources == null) {
      currentResources = new HashSet<EncodedResource>(4);
      this.resourcesCurrentlyBeingLoaded.set(currentResources);
   }
   if (!currentResources.add(encodedResource)) {
      throw new BeanDefinitionStoreException(
            "Detected cyclic loading of " + encodedResource + " - check your import definitions!");
   }
   try {
      InputStream inputStream = encodedResource.getResource().getInputStream();
      try {
         InputSource inputSource = new InputSource(inputStream);
         if (encodedResource.getEncoding() != null) {
            inputSource.setEncoding(encodedResource.getEncoding());
         }
         // 核心部分
         return doLoadBeanDefinitions(inputSource, encodedResource.getResource());
      }
      finally {
         inputStream.close();
      }
   }
   catch (IOException ex) {
      throw new BeanDefinitionStoreException(
            "IOException parsing XML document from " + encodedResource.getResource(), ex);
   }
   finally {
      currentResources.remove(encodedResource);
      if (currentResources.isEmpty()) {
         this.resourcesCurrentlyBeingLoaded.remove();
      }
   }
}

// XmlBeanDefinitionReader
//从XML开始加载我们bean的定义了！
protected int doLoadBeanDefinitions(InputSource inputSource, Resource resource)
			throws BeanDefinitionStoreException {
		try {
			Document doc = doLoadDocument(inputSource, resource);
			return registerBeanDefinitions(doc, resource);
		}
		catch (BeanDefinitionStoreException ex) {
			throw ex;
		}
		catch (SAXParseException ex) {
			throw new XmlBeanDefinitionStoreException(resource.getDescription(),
					"Line " + ex.getLineNumber() + " in XML document from " + resource + " is invalid", ex);
		}
		catch (SAXException ex) {
			throw new XmlBeanDefinitionStoreException(resource.getDescription(),
					"XML document from " + resource + " is invalid", ex);
		}
		catch (ParserConfigurationException ex) {
			throw new BeanDefinitionStoreException(resource.getDescription(),
					"Parser configuration exception parsing XML from " + resource, ex);
		}
		catch (IOException ex) {
			throw new BeanDefinitionStoreException(resource.getDescription(),
					"IOException parsing XML document from " + resource, ex);
		}
		catch (Throwable ex) {
			throw new BeanDefinitionStoreException(resource.getDescription(),
					"Unexpected exception parsing XML document from " + resource, ex);
		}
	}


// 返回从当前配置文件加载了多少数量的 Bean
public int registerBeanDefinitions(Document doc, Resource resource) throws BeanDefinitionStoreException {
   BeanDefinitionDocumentReader documentReader = createBeanDefinitionDocumentReader();
   int countBefore = getRegistry().getBeanDefinitionCount();
   documentReader.registerBeanDefinitions(doc, createReaderContext(resource));
   return getRegistry().getBeanDefinitionCount() - countBefore;
}

public void registerBeanDefinitions(Document doc, XmlReaderContext readerContext) {
   this.readerContext = readerContext;
   logger.debug("Loading bean definitions");
   Element root = doc.getDocumentElement();
   doRegisterBeanDefinitions(root);
}
```





```java

```

































## BeanDefinition



```java
/**
 * 描述了bean的信息
 *
 * <p>This is just a minimal interface: The main intention is to allow a
 * {@link BeanFactoryPostProcessor} such as {@link PropertyPlaceholderConfigurer}
 * to introspect and modify property values and other bean metadata.
 *
 */
public interface BeanDefinition extends AttributeAccessor, BeanMetadataElement {

	
	/**作用域*/
	String SCOPE_SINGLETON = ConfigurableBeanFactory.SCOPE_SINGLETON;


	String SCOPE_PROTOTYPE = ConfigurableBeanFactory.SCOPE_PROTOTYPE;


	/**
	 * Role hint indicating that a {@code BeanDefinition} is a major part
	 * of the application. Typically corresponds to a user-defined bean.
	 */
	int ROLE_APPLICATION = 0;

	/**
	 * Role hint indicating that a {@code BeanDefinition} is a supporting
	 * part of some larger configuration, typically an outer
	 * {@link org.springframework.beans.factory.parsing.ComponentDefinition}.
	 * {@code SUPPORT} beans are considered important enough to be aware
	 * of when looking more closely at a particular
	 * {@link org.springframework.beans.factory.parsing.ComponentDefinition},
	 * but not when looking at the overall configuration of an application.
	 */
	int ROLE_SUPPORT = 1;

	/**
	 * Role hint indicating that a {@code BeanDefinition} is providing an
	 * entirely background role and has no relevance to the end-user. This hint is
	 * used when registering beans that are completely part of the internal workings
	 * of a {@link org.springframework.beans.factory.parsing.ComponentDefinition}.
	 */
	int ROLE_INFRASTRUCTURE = 2;


	// Modifiable attributes

	// 设置父 Bean，这里涉及到 bean 继承，
	void setParentName(@Nullable String parentName);

	// 设置父 Bean，这里涉及到 bean 继承，
	@Nullable
	String getParentName();

	/**
	 * 设置当前beanDefinition类名
	 */
	void setBeanClassName(@Nullable String beanClassName);

	/**
	 * 返回当前beanDefinition的类名
	 */
	@Nullable
	String getBeanClassName();

	/**
	 * 设置Scope
	 */
	void setScope(@Nullable String scope);

	/**
	 *获取Scope
	 */
	@Nullable
	String getScope();

	/**
	 *是否懒加载
	 */
	void setLazyInit(boolean lazyInit);

	/**
	 * Return whether this bean should be lazily initialized, i.e. not
	 * eagerly instantiated on startup. Only applicable to a singleton bean.
	 */
	boolean isLazyInit();

	/**
	 * 设置bean依赖类的名称depends on ="" 属性设置的
	 * beanFactory确保依赖bean会率先初始化
	 * Set the names of the beans that this bean depends on being initialized.
	 * The bean factory will guarantee that these beans get initialized first.
	 */
	void setDependsOn(@Nullable String... dependsOn);

	/**
	 * 返回依赖bean的名称
	 */
	@Nullable
	String[] getDependsOn();

	/**
	 * 设置该bean是否能够注入其他类中，只对按类型注入有效，不影响按名称注入
	 */
	void setAutowireCandidate(boolean autowireCandidate);

	/**
	 * 该bean是否可以注入到其他的类中
	 */
	boolean isAutowireCandidate();

	/**
	 * 主要的。同一接口的多个实现，如果不指定名字的话，Spring 会优先选择设置 primary 为 true 的 bean
	 * Set whether this bean is a primary autowire candidate.
	 * <p>If this value is {@code true} for exactly one bean among multiple
	 * matching candidates, it will serve as a tie-breaker.
	 */
	void setPrimary(boolean primary);

	/**
	 * 是否是主要的
	 */
	boolean isPrimary();

	/**
	 *如果该 Bean 采用工厂方法生成，这个方法就指定了工厂名称。
	 */
	void setFactoryBeanName(@Nullable String factoryBeanName);

	/**
	 * Return the factory bean name, if any.
	 */
	@Nullable
	String getFactoryBeanName();

	/**
	 *  指定工厂类中的 工厂方法名称
	 */
	void setFactoryMethodName(@Nullable String factoryMethodName);

	/**
	 * Return a factory method, if any.
	 */
	@Nullable
	String getFactoryMethodName();

	/**
	 * 返回这个bean的构造器参数的值
	 */
	ConstructorArgumentValues getConstructorArgumentValues();

	/**
	 * 是否有构造器参数
	 * @since 5.0.2
	 */
	default boolean hasConstructorArgumentValues() {
		return !getConstructorArgumentValues().isEmpty();
	}

	/**
	 * 返回 Bean 中的属性值
	 */
	MutablePropertyValues getPropertyValues();

	/**
	 * Return if there are property values values defined for this bean.
	 * @since 5.0.2
	 */
	default boolean hasPropertyValues() {
		return !getPropertyValues().isEmpty();
	}


	// Read-only attributes

	/**
	 * Return whether this a <b>Singleton</b>, with a single, shared instance
	 * returned on all calls.
	 * @see #SCOPE_SINGLETON
	 */
	boolean isSingleton();

	/**
	 * Return whether this a <b>Prototype</b>, with an independent instance
	 * returned for each call.
	 * @since 3.0
	 * @see #SCOPE_PROTOTYPE
	 */
	boolean isPrototype();

	/**
	 * Return whether this bean is "abstract", that is, not meant to be instantiated.
	 */
	boolean isAbstract();

	/**
	 * Get the role hint for this {@code BeanDefinition}. The role hint
	 * provides the frameworks as well as tools with an indication of
	 * the role and importance of a particular {@code BeanDefinition}.
	 * @see #ROLE_APPLICATION
	 * @see #ROLE_SUPPORT
	 * @see #ROLE_INFRASTRUCTURE
	 */
	int getRole();

	/**
	 * Return a human-readable description of this bean definition.
	 */
	@Nullable
	String getDescription();

	/**
	 * Return a description of the resource that this bean definition
	 * came from (for the purpose of showing context in case of errors).
	 */
	@Nullable
	String getResourceDescription();

	/**
	 * Return the originating BeanDefinition, or {@code null} if none.
	 * Allows for retrieving the decorated bean definition, if any.
	 * <p>Note that this method returns the immediate originator. Iterate through the
	 * originator chain to find the original BeanDefinition as defined by the user.
	 */
	@Nullable
	BeanDefinition getOriginatingBeanDefinition();

}

```

