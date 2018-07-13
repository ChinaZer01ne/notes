# Spring Security

问题：

1、配置拦截路径的顺序对程序运行是有影响的，应该是按顺序执行的。

2、配置了anonymous()方法拦截的路径也会经过Security，用户名是anonymousUser。别的用户没有权限。。不太明白这么设计的理由

3、配置了permitAll()方法拦截的路径，如果没有登陆，用户是anonymousUser，如果登录就是登陆用户名