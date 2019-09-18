# SpringCloud

Spring -----> Spring Boot ----->Spring Cloud ----->微服务



内容：Eureka、Config、Ribbon、Zuul、Hystrix。



## 服务治理方案

阿里系：dubbo + ZooKeeper

SpringCloud：Spring全家桶系列



##分布式系统服务发现

### 服务发现

1.服务注册表

2.服务注册

3.健康检查

### 服务发现的方式

客户端发现

服务端发现，需要一个代理



## 异构

每个服务可以是不同语言，不同数据库



## REST or RPC?

dubbo：基于RPC

SpringCloud：给予Restful



##微服务和SOA差别是一个ESB？



## Eureka

### 注册中心

1、启动主类上加`@EnableEurekaServer`注解，表示这是Eureka服务端

2、配置

### 服务端单点

```properties
spring.application.name=spring-cloud-eureka-server-1

eureka.instance.hostname=eureka-1

server.port=8080
# 如果不做高可用，那么下面两项都是false
# 是否eureka自我注册
eureka.client.fetch-registry=false
# 是否检索服务
eureka.client.register-with-eureka=false
# 指定服务注册中心的位置
eureka.client.serviceUrl.defaultZone=http://eureka-2:8081/eureka/
# 关闭自我保护，并设置剔除时间为30秒
#eureka.server.enable-self-preservation=false
#eureka.server.eviction-interval-timer-in-ms=15000
```



### 服务端高可用

第一台Eureka Server

```properties
spring.application.name=spring-cloud-eureka-server-1
# 高可用的配置需要此项（域名），如果用localhost，在监控中心页面中registered-replicas项，显示的是空白
eureka.instance.hostname=eureka-1
server.port=8080
# 是否eureka自我注册
eureka.client.fetch-registry=true
# 是否检索服务
eureka.client.register-with-eureka=true
# 指定服务注册中心的位置（将该eureka服务注册到另一个eureka服务器上）
eureka.client.serviceUrl.defaultZone=http://eureka-2:8081/eureka/
# 关闭自我保护，并设置剔除时间为30秒(开发状态)
#eureka.server.enable-self-preservation=false
#eureka.server.eviction-interval-timer-in-ms=15000
```

第二台Eureka Server

```properties
spring.application.name=spring-cloud-eureka-server-1
# 高可用的配置需要此项（域名），如果用localhost，在dashbroad中registered-replicas项，显示的是空白
eureka.instance.hostname=eureka-1
server.port=8081
# 是否eureka自我注册
eureka.client.fetch-registry=true
# 是否检索服务
eureka.client.register-with-eureka=true
# 指定服务注册中心的位置（将该eureka服务注册到另一个eureka服务器上）
eureka.client.serviceUrl.defaultZone=http://eureka-2:8080/eureka/
# 关闭自我保护，并设置剔除时间为30秒(开发状态)
#eureka.server.enable-self-preservation=false
#eureka.server.eviction-interval-timer-in-ms=15000
```

### 客户端

1、启动主类加`@EurekaClientDiscover`注解，表示这是一个客户端

2、配置

### 高可用

```properties
spring.application.name=user-provider
# 随机端口
#server.port=${random.int[7070,7079]}
server.port=9090
# eureka注册地址，如果服务端是集群，那么以逗号分隔
eureka.client.service-url.defaultZone=http://localhost:8080/eureka/,http://localhost:8081/eureka/
```



> 由于Eureka客户端有注册表缓存信息，即使所有的eureka服务器都挂了，服务也能正常运行。







## Zuul

为什么要API网关？

1、跨横切面的逻辑，公共代码抽取

2、外部访问的单点入口

作用？

单点入口、路由转发、熔断降级、日志监控、安全认证。





## Hystrix



### 激活Hystrix

在启动类上添加`@EnableHystrix`注解，这仅仅是netflix的内容

2、

```java
@HystrixCommand(defaultFallback = "error")
```

3、配置特定的属性，https://github.com/Netflix/Hystrix/wiki/Configuration

```java
@HystrixCommand(defaultFallback = "error",
                //超时100毫秒，跳转
                commandProperties = {@HystrixProperty(name="execution.isolation.thread.timeoutInMilliseconds",value="100") })
@GetMapping("/hello")
public String hello() throws Exception{
    int value = random.nextInt(500);
    System.out.println(value);
    TimeUnit.MILLISECONDS.sleep(value);
    return "hello world";
}
```

另一种实现方式,Future,模拟熔断效果。

```java
public class FutureDemo {

    public static void main(String[] args) {

        Random random = new Random();

        ExecutorService service = Executors.newFixedThreadPool(1);

        Future<String> future = service.submit(() -> {
            int value = random.nextInt(200);

            System.out.println(value);
            TimeUnit.MILLISECONDS.sleep(value);
            return "Hello World";
        });

        try {
            String s = future.get(100,TimeUnit.MILLISECONDS);
            System.out.println(s);
        } catch (Exception e) {
            System.out.println("超时保护");
        }

    }
}
```



### 激活熔断保护（包含Hystrix）

启动类添加`@EnableCircuitBreaker`这是SpringCloud的内容，包含了`@EnableHystrix`（这是netflix的内容）





### Hystrix DashBoard

```java
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-hystrix-dashboard</artifactId>
</dependency>
```

被监控方：

```java
# 解决 hystrix监控 404 问题
management:
  endpoints:
    web:
      exposure:
        include: '*'
```

访问：

http://localhost:8761/hystrix

监控指定服务：

Hystrix Stream: http://ip:port/actuator/hystrix.stream



## Feign



```java
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-openfeign</artifactId>
</dependency>
```



1、在需要使用feign的项目启动类下添加`@EnableFeignClients`注解，开启

```java
@EnableFeignClients
public class SpringCloudUserServiceApplication {
```

2、创建feign调用接口

```java
//value：调用的服务名，path：服务地址，类似RequestMapping
@FeignClient(value = "order-service",path = "/api/order")
public interface OrderClient {

    @GetMapping("/{id}")
    String getOrder(@PathVariable(name = "id") Long id);
}
```



3、注入调用

```java
@Autowired
private OrderClient orderClient;

。。。
@GetMapping("/getOrder")
@HystrixCommand(fallbackMethod = "getOrderError")
public Map<String,String> getOrder(){
    String order = orderClient.getOrder(1L);
    。。。
}
```





### Feign整合Ribbn



## 学习网站

[官网](https://spring.io/projects/spring-cloud)

[Spring Cloud中文网](https://springcloud.cc/)

[Spring Cloud中国社区](http://www.springcloud.cn/)