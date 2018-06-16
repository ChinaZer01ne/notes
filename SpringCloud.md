# SpringCloud

Spring -----> Spring Boot ----->Spring Cloud ----->微服务



内容：Eureka、Config、Ribbon、Zuul、Hystrix。



# 架构的发展

单体架构：

​	优点：容易测试、容易部署

​	缺点：开发效率低、代码维护难、部署不够灵活、稳定性不高、扩展性不够

# 服务治理方案

阿里系：dubbo + ZooKeeper

SpringCloud：Spring全家桶系列

# Eureka

## 注册中心

1.启动主类上加`@EnableEurekaServer`注解

2.yml文件配置注册中心地址，`eureka.client.service-url:defaultZone:`

​	`eureka.server.enable-self-preservation:`是否把自己也注册到注册中心（可选）

## 客户端

1.启动主类加`@EurekaClientDiscover`注解

2.yml文件配置注册中心地址，`eureka.client.service-url:defaultZone:`

## 高可用

搭建集群，EurekaServer之间实现相互注册。

在`eureka.client.service-url:defaultZone:`中配置其他的注册中心地址

#分布式系统服务发现

## 服务发现

1.服务注册表

2.服务注册

3.健康检查

## 服务发现的方式

客户端发现

服务端发现，需要一个代理

# 异构

每个服务可以是不同语言，不同数据库

#REST or RPC?

#微服务和SOA差别是一个ESB？

