# Dubbo

## dubbo架构图

![dubbo-architecture](C:\Users\Lion\Desktop\资料\images\dubbo\dubbo-architecture.png)

## Springboot整合dubbo

1、dubbo依赖的引入

```xml
<dependency>
    <groupId>com.alibaba.boot</groupId>
    <artifactId>dubbo-spring-boot-starter</artifactId>
    <version>0.2.0</version>
</dependency>
```

| versions | Java | Spring Boot | Dubbo     |
| -------- | ---- | ----------- | --------- |
| `0.2.0`  | 1.8+ | `2.0.x`     | `2.6.2` + |
| `0.1.1`  | 1.7+ | `1.5.x`     | `2.6.2` + |

因为spring2.0+需要搭配0.2.0版本使用，而0.2.0版本还为发布到maven仓库，所以需要自己下载安装。从github下载后，运行

`mvn clean install -Dmaven.test.skip=true -Drat.numUnapprovedLicenses=100`

安装到本地仓库。



2、暴露接口的扫描

只需要再springboot的主配置类上添加`@DubboComponentScan(指定要扫描的包名)`注解即可。



3、详细配置

服务端：

```yaml
server:
  port: 9004


spring:
  datasource:
    username: lion
    password: lion
    url: jdbc:mysql://localhost:3306/pinyougoudb?useUnicode=true&characterEncoding=utf8&autoReconnect=true&useSSL=false&serverTimezone=GMT
    driver-class-name: com.mysql.cj.jdbc.Driver
    type: com.alibaba.druid.pool.DruidDataSource

dubbo:
  application:
    name: products-service
  registry:
    address: zookeeper://192.168.1.127:2181
  protocol:
    name: dubbo
    port: 20881 # 发布服务的端口

```

消费端：

```yaml
server:
  port: 9101


dubbo:
  application:
    name: consumer
  registry:
    address: zookeeper://192.168.1.127:2181
  protocol:
    port: 20880 # 默认不配就是20880，是dubbo服务的端口
```

