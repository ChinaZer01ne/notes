# Mybatis



## SpringBoot整合pageHelper

1.在pom.xml中引入依赖

```
<dependency>
    <groupId>com.github.pagehelper</groupId>
    <artifactId>pagehelper-spring-boot-starter</artifactId>
</dependency>
```

2.在Application启动类中添加

```
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