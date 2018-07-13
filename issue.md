# Issue

# 跨域请求

1、基于Cros

后端代码增加：

第一种方法：

```java
response.setHeader("Access-Control-Allow-Origin", "http://localhost:9105");//可以访问的源，当此方法不需要操作Cookie，只写这一句就可以了
response.setHeader("Access-Control-Allow-Credentials", "true"); //允许携带凭证，可以使用cookie，如果加了这句话，上面的域不能写通配符（*）
```

第二种方法：（Spring4.2之后）

```java
@CrossOrigin(origins = "http://localhost:9105",allowCredentials = "true")
```

前端代码增加：

```javascript
{'withCredentials': true}
```

2、基于Jsonp