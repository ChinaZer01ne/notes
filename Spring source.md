# Spring Source

## 启动过程分析（定位、加载、注册）

第一步，我们肯定要从 ClassPathXmlApplicationContext 的构造方法说起。 

```java
public static void main(String[] args){
    ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("classpath:*Context.xml");
     System.out.println(context.getBean("user"));
}
```

创建ClassPathXmlApplicationContext的时候会调用静态代码块以及父类的构造器之类的。

静态代码块：

```java
//AbstractApplicationContext.java 153
//整个容器初始化只执行一次
static {
   // Eagerly load the ContextClosedEvent class to avoid weird classloader issues
   // on application shutdown in WebLogic 8.1. (Reported by Dustin Woods.)
   //为了避免应用程序在WebLogic 8.1关闭时出现类加载异常的问题，加载IOC容器关闭事件ContextClosedEvent类
   ContextClosedEvent.class.getName();
}
```

构造方法：



```java
//ClassPathXmlApplicationContext.java
public ClassPathXmlApplicationContext(
      String[] configLocations, boolean refresh, @Nullable ApplicationContext parent)
      throws BeansException {
	//调用AbstractApplicationContext的构造方法，主要是设置环境和资源匹配解析器
   super(parent);
   // 根据提供的路径，处理成配置文件数组
   setConfigLocations(configLocations);
    //刷新容器
   if (refresh) {
      refresh();
   }
}
```



```java
//AbstractApplicationContext.java
public AbstractApplicationContext(@Nullable ApplicationContext parent) {
   //设置资源匹配解析器 
    this();
    //设置环境（此时parent为null）
   setParent(parent);
}
public AbstractApplicationContext() {
    //设置资源匹配解析器 
	this.resourcePatternResolver = getResourcePatternResolver();
}
```

创建环境的操作

```java
//AbstractApplicationContext.java
@Override
public ConfigurableEnvironment getEnvironment() {
    if (this.environment == null) {
        this.environment = createEnvironment();
    }
    return this.environment;
}
//AbstractApplicationContext
protected ConfigurableEnvironment createEnvironment() {
    //创建了一个标准环境（此时会执行父类的构造方法，执行了一系列操作，主要是一些解析占位符操作）
    return new StandardEnvironment();
}
```

创建环境完成后，就将传入的本地文件的路径解析成Resouce。

 setConfigLocations(configLocations)是如何处理配置文件的？

```java
//AbstractRefreshableConfigApplicationContext.java
public void setConfigLocations(@Nullable String... locations) {
   if (locations != null) {
      Assert.noNullElements(locations, "Config locations must not be null");
       //将locations转换成数组
      this.configLocations = new String[locations.length];
      for (int i = 0; i < locations.length; i++) {
          //解析路径
         this.configLocations[i] = resolvePath(locations[i]).trim();
      }
   }
   else {
      this.configLocations = null;
   }
}
...
//AbstractRefreshableConfigApplicationContext.java
protected String resolvePath(String path) {
    //先创建环境，然后解析
    return getEnvironment().resolveRequiredPlaceholders(path);
}
```

解析的操作

```java
//AbstractEnvironment.java
public String resolveRequiredPlaceholders(String text) throws IllegalArgumentException {
    //propertyResolver对象在创建环境的时候 创建出来的
    return this.propertyResolver.resolveRequiredPlaceholders(text);
}
//AbstractPropertyResolver
public String resolveRequiredPlaceholders(String text) throws IllegalArgumentException {
    if (this.strictHelper == null) {
       
        this.strictHelper = this.createPlaceholderHelper(false);
    }
	//处理占位符
    return this.doResolvePlaceholders(text, this.strictHelper);
}
...
```

这个过程有一个解析占位符的功能。

Spring加载的时候将系统变量等一些参数放到了`MapPropertySource`中，可以通过${key}的方式取到，比如：

`context = new ClassPathXmlApplicationContext(new String[] { "classpath*:spring/${java.vm.version}/propertyEditor.xml" }); `虽然不太清楚这个的意义，哈哈😂



前戏完成了，到了重要的阶段。

刷新容器。

```java
////AbstractApplicationContext.java
@Override
public void refresh() throws BeansException, IllegalStateException {
   synchronized (this.startupShutdownMonitor) {
     // 准备工作，记录下容器的启动时间、标记“已启动”状态、处理配置文件中的占位符
      prepareRefresh();

      // 这步比较关键，这步完成后，配置文件就会解析成一个个 BeanDefinition，注册到 BeanFactory 中，
      // 当然，这里说的 Bean 还没有初始化，只是配置信息都提取出来了，
      // 注册也只是将这些信息都保存到了注册中心(说到底核心是一个 beanName-> beanDefinition 的 map)
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

prepareRefresh();

```java
//AbstractApplicationContext.java
protected void prepareRefresh() {
   this.startupDate = System.currentTimeMillis();
   this.closed.set(false);
   this.active.set(true);

   if (logger.isInfoEnabled()) {
      logger.info("Refreshing " + this);
   }

   // 初始化容器中的占位符资源
   initPropertySources();

   // Validate that all properties marked as required are resolvable
   // see ConfigurablePropertyResolver#setRequiredProperties
    //// 校验 xml 配置文件
   getEnvironment().validateRequiredProperties();

   // Allow for the collection of early ApplicationEvents,
   // to be published once the multicaster is available...
   this.earlyApplicationEvents = new LinkedHashSet<>();
}
```

obtainFreshBeanFactory();(重要)

```java
//AbstractApplicationContext.java
protected ConfigurableListableBeanFactory obtainFreshBeanFactory() {
    //刷新BeanFactory
   refreshBeanFactory();
   ConfigurableListableBeanFactory beanFactory = getBeanFactory();
   if (logger.isDebugEnabled()) {
      logger.debug("Bean factory for " + getDisplayName() + ": " + beanFactory);
   }
   return beanFactory;
}

//AbstractRefreshableApplicationContext.java
protected final void refreshBeanFactory() throws BeansException {
   // 如果 ApplicationContext 中已经加载过 BeanFactory 了，销毁所有 Bean，关闭 BeanFactory
   // 注意，应用中 BeanFactory 本来就是可以多个的，这里可不是说应用全局是否有 BeanFactory，而是当前
   // ApplicationContext 是否有 BeanFactory
    if (hasBeanFactory()) {
        destroyBeans();
        closeBeanFactory();
    }
    try {
        //创建BeanFactory
        DefaultListableBeanFactory beanFactory = createBeanFactory();
        //序列化相关
        beanFactory.setSerializationId(getId());
        // 设置 BeanFactory 的两个配置属性：是否允许 Bean 覆盖、是否允许循环引用
        customizeBeanFactory(beanFactory);
        // 加载 Bean 到 BeanFactory 中（重要）
        loadBeanDefinitions(beanFactory);
        synchronized (this.beanFactoryMonitor) {
            this.beanFactory = beanFactory;
        }
    }
    catch (IOException ex) {
        throw new ApplicationContextException("I/O error parsing bean definition source for " + getDisplayName(), ex);
    }
}
```

customizeBeanFactory(beanFactory)

```java
//AbstractRefreshableApplicationContext.java
protected void customizeBeanFactory(DefaultListableBeanFactory beanFactory) {
   if (this.allowBeanDefinitionOverriding != null) {
        // 是否允许 Bean 定义覆盖，Spring 默认是不同文件的时候可以覆盖的。
      beanFactory.setAllowBeanDefinitionOverriding(this.allowBeanDefinitionOverriding);
   }
   if (this.allowCircularReferences != null) {
       // 是否允许 Bean 间的循环依赖
      beanFactory.setAllowCircularReferences(this.allowCircularReferences);
   }
}
```

 loadBeanDefinitions(beanFactory);

```java
// AbstractXmlApplicationContext.java
protected void loadBeanDefinitions(DefaultListableBeanFactory beanFactory) throws BeansException, IOException {
   // 给这个 BeanFactory 实例化一个 XmlBeanDefinitionReader
   XmlBeanDefinitionReader beanDefinitionReader = new XmlBeanDefinitionReader(beanFactory);

   // Configure the bean definition reader with this context's
   // resource loading environment.
    //配置beanDefinitionReader
   beanDefinitionReader.setEnvironment(this.getEnvironment());
   beanDefinitionReader.setResourceLoader(this);
   beanDefinitionReader.setEntityResolver(new ResourceEntityResolver(this));

   // Allow a subclass to provide custom initialization of the reader,
   // then proceed with actually loading the bean definitions.
    // 初始化 BeanDefinitionReader，其实这个是提供给子类覆写的，
   // 我看了一下，没有类覆写这个方法，我们姑且当做不重要吧
   initBeanDefinitionReader(beanDefinitionReader);
    // 重点来了，继续往下
   loadBeanDefinitions(beanDefinitionReader);
}
```

loadBeanDefinitions(beanDefinitionReader);

```java
// AbstractXmlApplicationContext.java
protected void loadBeanDefinitions(XmlBeanDefinitionReader reader) throws BeansException, IOException {
   Resource[] configResources = getConfigResources();
   if (configResources != null) {
      reader.loadBeanDefinitions(configResources);
   }
   String[] configLocations = getConfigLocations();
   if (configLocations != null) {
      reader.loadBeanDefinitions(configLocations);
   }
}
```

上面虽然有两个分支，不过第二个分支很快通过解析路径转换为 Resource 以后也会进到这里 

```java
////XmlBeanDefinitionReader.java
public int loadBeanDefinitions(Resource resource) throws BeanDefinitionStoreException {
   return loadBeanDefinitions(new EncodedResource(resource));
}
```

loadBeanDefinitions()

```java
//XmlBeanDefinitionReader.java
public int loadBeanDefinitions(EncodedResource encodedResource) throws BeanDefinitionStoreException {
   Assert.notNull(encodedResource, "EncodedResource must not be null");
   if (logger.isInfoEnabled()) {
      logger.info("Loading XML bean definitions from " + encodedResource.getResource());
   }
// 用一个 ThreadLocal 来存放所有的配置文件资源
   Set<EncodedResource> currentResources = this.resourcesCurrentlyBeingLoaded.get();
   if (currentResources == null) {
      currentResources = new HashSet<>(4);
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
          //终于开始要把配置文件加载成BeanDefinition了
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
```

doLoadBeanDefinitions(inputSource, encodedResource.getResource());

```java
//XmlBeanDefinitionReader.java
protected int doLoadBeanDefinitions(InputSource inputSource, Resource resource)
      throws BeanDefinitionStoreException {
   try {
       //dom对象
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
```

registerBeanDefinitions(doc, resource);

```java
// DefaultBeanDefinitionDocumentReader.java
// 返回从当前配置文件加载了多少数量的 Bean
public int registerBeanDefinitions(Document doc, Resource resource) throws BeanDefinitionStoreException {
   BeanDefinitionDocumentReader documentReader = createBeanDefinitionDocumentReader();
   int countBefore = getRegistry().getBeanDefinitionCount();
   documentReader.registerBeanDefinitions(doc, createReaderContext(resource));
   return getRegistry().getBeanDefinitionCount() - countBefore;
}
```

 documentReader.registerBeanDefinitions(doc, createReaderContext(resource));

```java
// DefaultBeanDefinitionDocumentReader.java
public void registerBeanDefinitions(Document doc, XmlReaderContext readerContext) {
   this.readerContext = readerContext;
   logger.debug("Loading bean definitions");
   Element root = doc.getDocumentElement();
   doRegisterBeanDefinitions(root);
}
```

doRegisterBeanDefinitions(root);

```java
// DefaultBeanDefinitionDocumentReader.java
//注册为BeanDefinition
protected void doRegisterBeanDefinitions(Element root) {
   // Any nested <beans> elements will cause recursion in this method. In
   // order to propagate and preserve <beans> default-* attributes correctly,
   // keep track of the current (parent) delegate, which may be null. Create
   // the new (child) delegate with a reference to the parent for fallback purposes,
   // then ultimately reset this.delegate back to its original (parent) reference.
   // this behavior emulates a stack of delegates without actually necessitating one.
    // 我们看名字就知道，BeanDefinitionParserDelegate 必定是一个重要的类，它负责解析 Bean 定义，
   // 这里为什么要定义一个 parent? 看到后面就知道了，是递归问题，
   // 因为 <beans /> 内部是可以定义 <beans /> 的，所以这个方法的 root 其实不一定就是 xml 的根节点，也可以是嵌套在里面的 <beans /> 节点，从源码分析的角度，我们当做根节点就好了
   BeanDefinitionParserDelegate parent = this.delegate;
   this.delegate = createDelegate(getReaderContext(), root, parent);

   if (this.delegate.isDefaultNamespace(root)) {
        // 这块说的是根节点 <beans ... profile="dev" /> 中的 profile 是否是当前环境需要的，
      // 如果当前环境配置的 profile 不包含此 profile，那就直接 return 了，不对此 <beans /> 解析
      String profileSpec = root.getAttribute(PROFILE_ATTRIBUTE);
      if (StringUtils.hasText(profileSpec)) {
         String[] specifiedProfiles = StringUtils.tokenizeToStringArray(
               profileSpec, BeanDefinitionParserDelegate.MULTI_VALUE_ATTRIBUTE_DELIMITERS);
         if (!getReaderContext().getEnvironment().acceptsProfiles(specifiedProfiles)) {
            if (logger.isInfoEnabled()) {
               logger.info("Skipped XML bean definition file due to specified profiles [" + profileSpec +
                     "] not matching: " + getReaderContext().getResource());
            }
            return;
         }
      }
   }

   preProcessXml(root);// 钩子
   parseBeanDefinitions(root, this.delegate);
   postProcessXml(root);// 钩子

   this.delegate = parent;
}
```

```java
//DefaultBeanDefinitionDocumentReader.java
// default namespace 涉及到的就四个标签 <import />、<alias />、<bean /> 和 <beans />，
// 其他的属于 custom 的
protected void parseBeanDefinitions(Element root, BeanDefinitionParserDelegate delegate) {
   if (delegate.isDefaultNamespace(root)) {
      NodeList nl = root.getChildNodes();
      for (int i = 0; i < nl.getLength(); i++) {
         Node node = nl.item(i);
         if (node instanceof Element) {
            Element ele = (Element) node;
            if (delegate.isDefaultNamespace(ele)) {
                //处理默认标签
               parseDefaultElement(ele, delegate);
            }
            else {
                //处理扩展标签
               delegate.parseCustomElement(ele);
            }
         }
      }
   }
   else {
      delegate.parseCustomElement(root);
   }
}
```

处理默认标签的操作

```java
//DefaultBeanDefinitionDocumentReader.java
private void parseDefaultElement(Element ele, BeanDefinitionParserDelegate delegate) {
   if (delegate.nodeNameEquals(ele, IMPORT_ELEMENT)) {
       // 处理 <import /> 标签
      importBeanDefinitionResource(ele);
   }
   else if (delegate.nodeNameEquals(ele, ALIAS_ELEMENT)) {
       // 处理 <alias /> 标签定义
      processAliasRegistration(ele);
   }
   else if (delegate.nodeNameEquals(ele, BEAN_ELEMENT)) {
       // 处理 <bean /> 标签定义，这也算是我们的重点吧
      processBeanDefinition(ele, delegate);
   }
   else if (delegate.nodeNameEquals(ele, NESTED_BEANS_ELEMENT)) {
      // recurse
         // 如果碰到的是嵌套的 <beans /> 标签，需要递归
      doRegisterBeanDefinitions(ele);
   }
}
```

以`<bean>`标签为例

```java
//DefaultBeanDefinitionDocumentReader.java
protected void processBeanDefinition(Element ele, BeanDefinitionParserDelegate delegate) {
      // 将 <bean /> 节点中的信息提取出来，然后封装到一个 BeanDefinitionHolder 中，细节往下看
   BeanDefinitionHolder bdHolder = delegate.parseBeanDefinitionElement(ele);
   if (bdHolder != null) {
      bdHolder = delegate.decorateBeanDefinitionIfRequired(ele, bdHolder);
      try {
         // Register the final decorated instance.
         BeanDefinitionReaderUtils.registerBeanDefinition(bdHolder, getReaderContext().getRegistry());
      }
      catch (BeanDefinitionStoreException ex) {
         getReaderContext().error("Failed to register bean definition with name '" +
               bdHolder.getBeanName() + "'", ele, ex);
      }
      // Send registration event.
      getReaderContext().fireComponentRegistered(new BeanComponentDefinition(bdHolder));
   }
}
```

delegate.parseBeanDefinitionElement(ele);

```java
//BeanDefinitionParserDelegate.java
public BeanDefinitionHolder parseBeanDefinitionElement(Element ele) {
   return parseBeanDefinitionElement(ele, null);
}
```

```java
//BeanDefinitionParserDelegate.java
public BeanDefinitionHolder parseBeanDefinitionElement(Element ele, @Nullable BeanDefinition containingBean) {
   String id = ele.getAttribute(ID_ATTRIBUTE);
   String nameAttr = ele.getAttribute(NAME_ATTRIBUTE);

   List<String> aliases = new ArrayList<>();
    // 将 name 属性的定义按照 ”逗号、分号、空格“ 切分，形成一个别名列表数组，
   // 当然，如果你不定义的话，就是空的了
   // 我在附录中简单介绍了一下 id 和 name 的配置，大家可以看一眼，有个20秒就可以了
   if (StringUtils.hasLength(nameAttr)) {
      String[] nameArr = StringUtils.tokenizeToStringArray(nameAttr, MULTI_VALUE_ATTRIBUTE_DELIMITERS);
      aliases.addAll(Arrays.asList(nameArr));
   }

   String beanName = id;
    // 如果没有指定id, 那么用别名列表的第一个名字作为beanName
   if (!StringUtils.hasText(beanName) && !aliases.isEmpty()) {
      beanName = aliases.remove(0);
      if (logger.isDebugEnabled()) {
         logger.debug("No XML 'id' specified - using '" + beanName +
               "' as bean name and " + aliases + " as aliases");
      }
   }

   if (containingBean == null) {
      checkNameUniqueness(beanName, aliases, ele);
   }
// 根据 <bean ...>...</bean> 中的配置创建 BeanDefinition，然后把配置中的信息都设置到实例中,
   // 细节后面再说
   AbstractBeanDefinition beanDefinition = parseBeanDefinitionElement(ele, beanName, containingBean);
    // 根据 <bean ...>...</bean> 中的配置创建 BeanDefinition，然后把配置中的信息都设置到实例中,
   // 细节后面再说
   if (beanDefinition != null) {
       // 如果都没有设置 id 和 name，那么此时的 beanName 就会为 null，进入下面这块代码产生
      // 如果读者不感兴趣的话，我觉得不需要关心这块代码，对本文源码分析来说，这些东西不重要
      if (!StringUtils.hasText(beanName)) {
         try {
            if (containingBean != null) {// 按照我们的思路，这里 containingBean 是 null 的
               beanName = BeanDefinitionReaderUtils.generateBeanName(
                     beanDefinition, this.readerContext.getRegistry(), true);
            }
            else {
               beanName = this.readerContext.generateBeanName(beanDefinition);
               // Register an alias for the plain bean class name, if still possible,
               // if the generator returned the class name plus a suffix.
               // This is expected for Spring 1.2/2.0 backwards compatibility.
               String beanClassName = beanDefinition.getBeanClassName();
               if (beanClassName != null &&
                     beanName.startsWith(beanClassName) && beanName.length() > beanClassName.length() &&
                     !this.readerContext.getRegistry().isBeanNameInUse(beanClassName)) {
                  // 把 beanClassName 设置为 Bean 的别名
                   aliases.add(beanClassName);
               }
            }
            if (logger.isDebugEnabled()) {
               logger.debug("Neither XML 'id' nor 'name' specified - " +
                     "using generated bean name [" + beanName + "]");
            }
         }
         catch (Exception ex) {
            error(ex.getMessage(), ele);
            return null;
         }
      }
      String[] aliasesArray = StringUtils.toStringArray(aliases);
      return new BeanDefinitionHolder(beanDefinition, beanName, aliasesArray);
   }

   return null;
}
```

parseBeanDefinitionElement(ele, beanName, containingBean);

```java
//BeanDefinitionParserDelegate.java
public AbstractBeanDefinition parseBeanDefinitionElement(
      Element ele, String beanName, @Nullable BeanDefinition containingBean) {

   this.parseState.push(new BeanEntry(beanName));

   String className = null;
   if (ele.hasAttribute(CLASS_ATTRIBUTE)) {
      className = ele.getAttribute(CLASS_ATTRIBUTE).trim();
   }
   String parent = null;
   if (ele.hasAttribute(PARENT_ATTRIBUTE)) {
      parent = ele.getAttribute(PARENT_ATTRIBUTE);
   }

   try {
       // 创建 BeanDefinition，然后设置类信息而已，很简单，就不贴代码了
      AbstractBeanDefinition bd = createBeanDefinition(className, parent);
// 设置 BeanDefinition 的一堆属性，这些属性定义在 AbstractBeanDefinition 中
      parseBeanDefinitionAttributes(ele, beanName, containingBean, bd);
      bd.setDescription(DomUtils.getChildElementValueByTagName(ele, DESCRIPTION_ELEMENT));
 /**
       * 下面的一堆是解析 <bean>......</bean> 内部的子元素，
       * 解析出来以后的信息都放到 bd 的属性中
       */
       // 解析 <meta />
      parseMetaElements(ele, bd);
       // 解析 <lookup-method />
      parseLookupOverrideSubElements(ele, bd.getMethodOverrides());
        // 解析 <replaced-method />
      parseReplacedMethodSubElements(ele, bd.getMethodOverrides());
 // 解析 <constructor-arg />
      parseConstructorArgElements(ele, bd);
        // 解析 <property />
      parsePropertyElements(ele, bd);
        // 解析 <qualifier />
      parseQualifierElements(ele, bd);

      bd.setResource(this.readerContext.getResource());
      bd.setSource(extractSource(ele));

      return bd;
   }
   catch (ClassNotFoundException ex) {
      error("Bean class [" + className + "] not found", ele, ex);
   }
   catch (NoClassDefFoundError err) {
      error("Class that bean class [" + className + "] depends on not found", ele, err);
   }
   catch (Throwable ex) {
      error("Unexpected failure during bean definition parsing", ele, ex);
   }
   finally {
      this.parseState.pop();
   }

   return null;
}
```

我们回到解析 `<bean />` 的入口方法: 

```java
//DefaultBeanDefinitionDocumentReader.java
protected void processBeanDefinition(Element ele, BeanDefinitionParserDelegate delegate) {
      // 将 <bean /> 节点转换为 BeanDefinitionHolder，就是上面说的一堆
   BeanDefinitionHolder bdHolder = delegate.parseBeanDefinitionElement(ele);
   if (bdHolder != null) {
       // 如果有自定义属性的话，进行相应的解析，先忽略
      bdHolder = delegate.decorateBeanDefinitionIfRequired(ele, bdHolder);
      try {
         // Register the final decorated instance.
            // 我们把这步叫做 注册Bean 吧
         BeanDefinitionReaderUtils.registerBeanDefinition(bdHolder, getReaderContext().getRegistry());
      }
      catch (BeanDefinitionStoreException ex) {
         getReaderContext().error("Failed to register bean definition with name '" +
               bdHolder.getBeanName() + "'", ele, ex);
      }
      // Send registration event.
       // 注册完成后，发送事件，本文不展开说这个
      getReaderContext().fireComponentRegistered(new BeanComponentDefinition(bdHolder));
   }
}
```

 BeanDefinitionReaderUtils.registerBeanDefinition(bdHolder, getReaderContext().getRegistry());

```java
public static void registerBeanDefinition(
      BeanDefinitionHolder definitionHolder, BeanDefinitionRegistry registry)
      throws BeanDefinitionStoreException {

   // Register bean definition under primary name.
   String beanName = definitionHolder.getBeanName();
    // 注册这个 Bean
   registry.registerBeanDefinition(beanName, definitionHolder.getBeanDefinition());

   // Register aliases for bean name, if any.
    // 如果还有别名的话，也要根据别名统统注册一遍，不然根据别名就找不到 Bean 了，这我们就不开心了
   String[] aliases = definitionHolder.getAliases();
   if (aliases != null) {
      for (String alias : aliases) {
          // alias -> beanName 保存它们的别名信息，这个很简单，用一个 map 保存一下就可以了，
         // 获取的时候，会先将 alias 转换为 beanName，然后再查找
         registry.registerAlias(beanName, alias);
      }
   }
}
```

registry.registerBeanDefinition(beanName, definitionHolder.getBeanDefinition());

```java
//DefaultListableBeanFactory.java
public void registerBeanDefinition(String beanName, BeanDefinition beanDefinition)
			throws BeanDefinitionStoreException {

		Assert.hasText(beanName, "Bean name must not be empty");
		Assert.notNull(beanDefinition, "BeanDefinition must not be null");

		if (beanDefinition instanceof AbstractBeanDefinition) {
			try {
				((AbstractBeanDefinition) beanDefinition).validate();
			}
			catch (BeanDefinitionValidationException ex) {
				throw new BeanDefinitionStoreException(beanDefinition.getResourceDescription(), beanName,
						"Validation of bean definition failed", ex);
			}
		}
// old? 还记得 “允许 bean 覆盖” 这个配置吗？allowBeanDefinitionOverriding
		BeanDefinition oldBeanDefinition;
// 之后会看到，所有的 Bean 注册后会放入这个 beanDefinitionMap 中
		oldBeanDefinition = this.beanDefinitionMap.get(beanName);
     // 处理重复名称的 Bean 定义的情况
		if (oldBeanDefinition != null) {
			if (!isAllowBeanDefinitionOverriding()) {
                 // 如果不允许覆盖的话，抛异常
				throw new BeanDefinitionStoreException(beanDefinition.getResourceDescription(), beanName,
						"Cannot register bean definition [" + beanDefinition + "] for bean '" + beanName +
						"': There is already [" + oldBeanDefinition + "] bound.");
			}
			else if (oldBeanDefinition.getRole() < beanDefinition.getRole()) {
                 // log...用框架定义的 Bean 覆盖用户自定义的 Bean 
				// e.g. was ROLE_APPLICATION, now overriding with ROLE_SUPPORT or ROLE_INFRASTRUCTURE
				
			}
			else if (!beanDefinition.equals(oldBeanDefinition)) {
                 // log...用框架定义的 Bean 覆盖用户自定义的 Bean 
				
			}
			else {
                // log...用同等的 Bean 覆盖旧的 Bean，这里指的是 equals 方法返回 true 的 Bean
				
			}
            // 覆盖
			this.beanDefinitionMap.put(beanName, beanDefinition);
		}
		else {
              // 判断是否已经有其他的 Bean 开始初始化了.
      // 注意，"注册Bean" 这个动作结束，Bean 依然还没有初始化，我们后面会有大篇幅说初始化过程，
      // 在 Spring 容器启动的最后，会 预初始化 所有的 singleton beans
			if (hasBeanCreationStarted()) {
				// Cannot modify startup-time collection elements anymore (for stable iteration)
				synchronized (this.beanDefinitionMap) {
					this.beanDefinitionMap.put(beanName, beanDefinition);
					List<String> updatedDefinitions = new ArrayList<>(this.beanDefinitionNames.size() + 1);
					updatedDefinitions.addAll(this.beanDefinitionNames);
					updatedDefinitions.add(beanName);
					this.beanDefinitionNames = updatedDefinitions;
					if (this.manualSingletonNames.contains(beanName)) {
						Set<String> updatedSingletons = new LinkedHashSet<>(this.manualSingletonNames);
						updatedSingletons.remove(beanName);
						this.manualSingletonNames = updatedSingletons;
					}
				}
			}
			else {
                // 最正常的应该是进到这里。
				// Still in startup registration phase
                  // 将 BeanDefinition 放到这个 map 中，这个 map 保存了所有的 BeanDefinition
				this.beanDefinitionMap.put(beanName, beanDefinition);
                 // 这是个 ArrayList，所以会按照 bean 配置的顺序保存每一个注册的 Bean 的名字
				this.beanDefinitionNames.add(beanName);
                // 这是个 LinkedHashSet，代表的是手动注册的 singleton bean，
         // 注意这里是 remove 方法，到这里的 Bean 当然不是手动注册的
         // 手动指的是通过调用以下方法注册的 bean ：
         //     registerSingleton(String beanName, Object singletonObject)
         //         这不是重点，解释只是为了不让大家疑惑。Spring 会在后面"手动"注册一些 Bean，如 "environment"、"systemProperties" 等 bean
				this.manualSingletonNames.remove(beanName);
			}
			this.frozenBeanDefinitionNames = null;
		}

		if (oldBeanDefinition != null || containsSingleton(beanName)) {
			resetBeanDefinition(beanName);
		}
	}
```

### Bean 容器实例化完成后 TODO

## 了解Bean

Bean就是BeanDefinition。

```java
/*
 * Copyright 2002-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.beans.factory.config;

import org.springframework.beans.BeanMetadataElement;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.core.AttributeAccessor;
import org.springframework.lang.Nullable;

/**
 * A BeanDefinition describes a bean instance, which has property values,
 * constructor argument values, and further information supplied by
 * concrete implementations.
 *
 * <p>This is just a minimal interface: The main intention is to allow a
 * {@link BeanFactoryPostProcessor} such as {@link PropertyPlaceholderConfigurer}
 * to introspect and modify property values and other bean metadata.
 *
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @since 19.03.2004
 * @see ConfigurableListableBeanFactory#getBeanDefinition
 * @see org.springframework.beans.factory.support.RootBeanDefinition
 * @see org.springframework.beans.factory.support.ChildBeanDefinition
 */
public interface BeanDefinition extends AttributeAccessor, BeanMetadataElement {

	 // 我们可以看到，默认只提供 sington 和 prototype 两种，
   	// 很多读者都知道还有 request, session, globalSession, application, websocket 这几种，
   // 不过，它们属于基于 web 的扩展。
   String SCOPE_SINGLETON = ConfigurableBeanFactory.SCOPE_SINGLETON;

   String SCOPE_PROTOTYPE = ConfigurableBeanFactory.SCOPE_PROTOTYPE;


   int ROLE_APPLICATION = 0;
   int ROLE_SUPPORT = 1;
   int ROLE_INFRASTRUCTURE = 2;


   // Modifiable attributes

   // 设置父 Bean，这里涉及到 bean 继承，不是 java 继承。请参见附录介绍
   void setParentName(@Nullable String parentName);

   // 设置父 Bean，这里涉及到 bean 继承，不是 java 继承。请参见附录介绍
   @Nullable
   String getParentName();

   // 设置 Bean 的类名称
   void setBeanClassName(@Nullable String beanClassName);

   // 获得 Bean 的类名称
   @Nullable
   String getBeanClassName();

   // 设置 Bean 的scope
   void setScope(@Nullable String scope);

  
   @Nullable
   String getScope();

   // 设置是否懒加载
   void setLazyInit(boolean lazyInit);

   /**
    * Return whether this bean should be lazily initialized, i.e. not
    * eagerly instantiated on startup. Only applicable to a singleton bean.
    */
   boolean isLazyInit();

   // 设置该 Bean 依赖的所有的 Bean，注意，这里的依赖不是指属性依赖(如 @Autowire 标记的)，
   // 是 depends-on="" 属性设置的值。
   void setDependsOn(@Nullable String... dependsOn);

   /**
    * Return the bean names that this bean depends on.
    */
   @Nullable
   String[] getDependsOn();

    // 该 Bean 是否可以注入到其他 Bean 中
   void setAutowireCandidate(boolean autowireCandidate);

   /**
    * Return whether this bean is a candidate for getting autowired into some other bean.
    */
   boolean isAutowireCandidate();

  // 主要的。同一接口的多个实现，如果不指定名字的话，Spring 会优先选择设置 primary 为 true 的 bean
   void setPrimary(boolean primary);

   /**
    * Return whether this bean is a primary autowire candidate.
    */
   boolean isPrimary();

   // 如果该 Bean 采用工厂方法生成，指定工厂名称
   void setFactoryBeanName(@Nullable String factoryBeanName);

   /**
    * Return the factory bean name, if any.
    */
   @Nullable
   String getFactoryBeanName();

   /**
    * Specify a factory method, if any. This method will be invoked with
    * constructor arguments, or with no arguments if none are specified.
    * The method will be invoked on the specified factory bean, if any,
    * or otherwise as a static method on the local bean class.
    * @see #setFactoryBeanName
    * @see #setBeanClassName
    */
   void setFactoryMethodName(@Nullable String factoryMethodName);

   /**
    * Return a factory method, if any.
    */
   @Nullable
   String getFactoryMethodName();

   /**
    * Return the constructor argument values for this bean.
    * <p>The returned instance can be modified during bean factory post-processing.
    * @return the ConstructorArgumentValues object (never {@code null})
    */
   ConstructorArgumentValues getConstructorArgumentValues();

   /**
    * Return if there are constructor argument values defined for this bean.
    * @since 5.0.2
    */
   default boolean hasConstructorArgumentValues() {
      return !getConstructorArgumentValues().isEmpty();
   }

   /**
    * Return the property values to be applied to a new instance of the bean.
    * <p>The returned instance can be modified during bean factory post-processing.
    * @return the MutablePropertyValues object (never {@code null})
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

   // 如果这个 Bean 原生是抽象类，那么不能实例化
   boolean isAbstract();

   
   int getRole();
    
   @Nullable
   String getDescription();
   @Nullable
   String getResourceDescription();
   @Nullable
   BeanDefinition getOriginatingBeanDefinition();

}
```