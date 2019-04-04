# Mybatis



## SpringBoot整合pageHelper

1.在pom.xml中引入依赖

```xml
<dependency>
    <groupId>com.github.pagehelper</groupId>
    <artifactId>pagehelper-spring-boot-starter</artifactId>
</dependency>
```

2.在Application启动类中添加

```java
//配置mybatis的分页插件pageHelper
@Bean
public PageHelper pageHelper(){
    System.out.println("开始配置数据分页插件");
    PageHelper pageHelper = new PageHelper();
    Properties properties = new Properties();
    properties.setProperty("offsetAsPageNum","true");
    properties.setProperty("rowBoundsWithCount","true");
    properties.setProperty("reasonable","true");
    //配置mysql数据库的方言
    properties.setProperty("dialect","mysql");
    pageHelper.setProperties(properties);
    return pageHelper;
}
```



## Mybatis实际操作中的疑问

1、为什么业务逻辑层接口返回值经常是返回一个int类型的值？

在并发程度高的系统中，数据库悲观锁操作中可能用到，当根据某个字段进行更新操作时，有可能数据已经被其他线程更新过了，所以当前线程更新返回影响的行数会是0，也就意味着更新失败了，需要进行其他业务逻辑操作。所以需要int类型的返回值。