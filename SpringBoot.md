# Spring Boot

## 1、Spring Boot简介



## 2、Spring Boot注解

**@SpringBootApplication**: SpringBoot应用标注在某个类上说明这个类是SpringBoot的主配置类，SpringBoot就应该运行这个类的main方法来启动SpringBoot应用。

这个注解是一个组合注解：

```
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@SpringBootConfiguration
@EnableAutoConfiguration
@ComponentScan(
    excludeFilters = {@Filter(
    type = FilterType.CUSTOM,
    classes = {TypeExcludeFilter.class}
), @Filter(
    type = FilterType.CUSTOM,
    classes = {AutoConfigurationExcludeFilter.class}
)}
)
public @interface SpringBootApplication {
```

**@SpringBootConfiguration**：表示一个SpringBoot的配置类；

该注解也是一个组合注解：

```
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Configuration
public @interface SpringBootConfiguration {
```

​	**@Configuration**:标注这个注解表示该类是一个配置类，是容器中的一个组件；

**@EnableAutoConfiguration**：开启自动配置功能；

​	以前我们需要配置的东西，SpringBoot帮我们自动配置；该注解告诉SpringBoot开启自动配置功能；这样自动配置才能生效；

该注解是个组合注解：

```java
@AutoConfigurationPackage
@Import(EnableAutoConfigurationImportSelector.class)
public @interface EnableAutoConfiguration {
```

​	 	@**AutoConfigurationPackage**：自动配置包

```java
@Import({Registrar.class})
public @interface AutoConfigurationPackage {
```

​			@**Import**(AutoConfigurationPackages.Registrar.class)：

​			Spring的底层注解@Import，给容器中导入一个组件；导入的组件由AutoConfigurationPackages.Registrar.class；将主配置类（@SpringBootApplication标注的类）的所在包及其所有子包里面的所有组件扫描到Spring容器中；

​			@**Import**(EnableAutoConfigurationImportSelector.class)；

​				**EnableAutoConfigurationImportSelector**：导入那些组件选择器？

```java
List<String> configurations = this.getCandidateConfigurations(annotationMetadata, attributes);
```

​				将所有需要导入的组件以全类名的方式返回，这些组件会被添加到容器中；

​				会给容器中导入非常多的自动配置类（xxAutoConfiguration）;就是给容器中导入这个场景需要的所有组件，并自动配置。

![1529308081199](images\springboot\1529308081199.png)

有了自动配置类，免去了我们手动编写配置注入功能组件等工作；

```java
List<String> configurations = this.getCandidateConfigurations(annotationMetadata, attributes);
```

```java
List<String> configurations = SpringFactoriesLoader.loadFactoryNames(this.getSpringFactoriesLoaderFactoryClass(), this.getBeanClassLoader());
```

```java
return (List)loadSpringFactories(classLoader).getOrDefault(factoryClassName, Collections.emptyList());
```

```java
Enumeration<URL> urls = classLoader != null ? classLoader.getResources("META-INF/spring.factories") : ClassLoader.getSystemResources("META-INF/spring.factories");
```

从断点运行可以追踪到，SpringBoot在启动的时候从类路径下的`META-INF/spring.factories`中获取`EnableAutoConfiguration`指定的值，将这些值作为自动配置类导入到容器中，自动配置类就生效，帮我们进行自动配置工作；

J2EE的整体整合解决方案和自动配置都在`spring-boot-autoconfigure-version.RELEASE.jar`；

## 3、yml文件

### 1)、值的写法

#### 字面量：普通的值（数字，字符串，布尔）

​	k: v: 字面直接来写；

​		字符串默认不用加上单引号或者双引号

​		"":双引号；不会转移字符串里面的特殊字符；特殊字符会作为本身想表示的意思

​			name："zhangsan \n lisi":输出：zhangsan 换行 lisi

​		''：单引号；会转移特殊字符，特殊字符最终知识一个普通的资字符转数据

​			name:'zhangsan \n list':输出：zhangsan \n lisi

#### 对象、Map（属性和值）（键值对）：

​	k: v: 在下一行来写对象的属性和值的关系；注意缩进

​		对象还是k: v:的方式

```yacas
friends:
    lastName: zhangsan
    age: 20
```

行内写法：

```yaml
friends: {lastName: zhangsan,age: 18}
```

#### 数组（List、Set）

用-值表示数组中的一个元素

```yaml
pets:
 - cat
 - dog
 - pig
```

行内写法：

```yaml
pets: [cat,dog,pig]
```



###2）配置文件值注入











## Springboot整合fastjson

Springboot默认是用的是jackson来进行json转化，我在项目中遇到了

```tex
"JSON parse error: Cannot deserialize instance of `java.lang.String` out of START_ARRAY token; nested exception is com.fasterxml.jackson.databind.exc.MismatchedInputException: Cannot deserialize instance of `java.lang.String` out of START_ARRAY token↵ at [Source: (PushbackInputStream); line: 1, column: 25] (through reference chain: com.lion.pinyougou.pojo.TbTypeTemplate["customAttributeItems"])"
```

这个错误，整合fastjson后完美解决。

在主配置类中添加bean，整合fastjson：

```
@Bean
	public HttpMessageConverters fastjsonHttpMessageConverter(){
		//定义一个转换消息的对象
		FastJsonHttpMessageConverter fastConverter = new FastJsonHttpMessageConverter();
		//添加fastjson的配置信息 比如 ：是否要格式化返回的json数据
		FastJsonConfig fastJsonConfig = new FastJsonConfig();
		fastJsonConfig.setSerializerFeatures(SerializerFeature.PrettyFormat);
		//在转换器中添加配置信息
		fastConverter.setFastJsonConfig(fastJsonConfig);
		HttpMessageConverter<?> converter = fastConverter;
		return new HttpMessageConverters(converter);
	}
```

## Springboot整合Security

1、引入Spring Security的启动器

```xml
<dependency>
<groupId>org.springframework.boot</groupId>
<artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

2、编写Security的配置类，`extends WebSecurityConfigurerAdapter`并且增加`@EnableWebSecurity`注解

```java
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter{
```

3、控制请求的访问权限

​	1）定制授权规则

```java
@Override
protected void configure(HttpSecurity http) throws Exception {
```

​	2）定制认证规则

```java
@Override
protected void configure(AuthenticationManagerBuilder auth) throws Exception {
```

4、示例

```java
package com.lion.pinyougou.manager.security;

import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;


/**
 * @author Lion
 * @since 2018/6/20 19:47
 */

@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter{
    @Override
    public void configure(WebSecurity web) throws Exception {
        //super.configure(web);
        web.ignoring().antMatchers("/css/**", "/img/**","/js/**","/plugins/**","/*.html");
    }

    //定制授权规则
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        //super.configure(http);
        http.authorizeRequests()
                //.antMatchers("/css/**").anonymous()
                //.antMatchers("/img/**").anonymous()
                //.antMatchers("/js/**").anonymous()
                //.antMatchers("/plugin/**").anonymous()
                //.antMatchers("/*.html").anonymous()
                .antMatchers("/admin/*").hasRole("ADMIN")
                .and()
                .headers().frameOptions().disable();
        //开启自动配置的登录功能   loginPage表示登录页面
        http.formLogin()
                .loginProcessingUrl("/login")
                .loginPage("/login.html")
                .failureUrl("/login.html")
                .defaultSuccessUrl("/admin/index.html", true);
               // .successHandler(new AuthenticationSuccessHandler() {
               //     @Override
               //     public void onAuthenticationSuccess(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Authentication authentication) throws IOException, ServletException {
               //         System.out.println("登陆成功");
               //     }
               // })
               // .failureHandler(new AuthenticationFailureHandler() {
               //     @Override
               //     public void onAuthenticationFailure(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, AuthenticationException e) throws IOException, ServletException {
               //         httpServletRequest.getRequestDispatcher("/login.html").forward(httpServletRequest,httpServletResponse);
               //         System.out.println("登陆失败");
               // }});
        //1、/login来到登录页
        //2、/login?error表示登录失败
        //3、更多功能

        //开启自动配置的注销功能
        //访问logout，表示用户退出，清空session,logoutSuccessUrl设置退出成功跳到的页面
        http.logout();
        http.csrf().disable();
    }
    //定制认证规则
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        //super.configure(auth);
        auth.inMemoryAuthentication().passwordEncoder(new BCryptPasswordEncoder())
                .withUser("admin").password(new BCryptPasswordEncoder().encode("admin")).roles("ADMIN");
                //.and()
                //.withUser("zhangsan").password(new BCryptPasswordEncoder().encode("123")).roles("ADMIN");
       //auth.inMemoryAuthentication().withUser("admin").password("admin").roles("ADMIN");
    }
}
```

5、使用认证类的方式来认证授权

```java
package com.lion.pinyougou.shop.security;

import com.alibaba.dubbo.config.annotation.Reference;
import com.lion.pinyougou.pojo.TbSeller;
import com.lion.pinyougou.products.service.SellerService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 认证类
 * @author Lion
 * @since 2018/6/22 9:56
 */
@Component
public class UserDetailsServiceImpl implements UserDetailsService {

    @Reference
    private SellerService sellerService;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("登录操作。。。");
        //构建角色列表
        List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        //注意使用SimpleGrantedAuthority必须以“ROLE_”为前缀
        grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_SELLER"));
        TbSeller seller = sellerService.findOne(username);
        return new User(username, seller.getPassword(), grantedAuthorities)  ;
    }
}
```

```java
@Override
protected void configure(AuthenticationManagerBuilder auth) throws Exception {
   //此处如果选择了加密方式，那么数据库也需要是相应的加密方式
  auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
}

@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

### 常见问题

1、There is no PasswordEncoder mapped for the id "null" 

这是密码加密问题造成的。需要指定密码加密方式。

```java
auth.inMemoryAuthentication().passwordEncoder(new BCryptPasswordEncoder())
        .withUser("admin").password(new BCryptPasswordEncoder().encode("admin")).roles("admin")
        .and()
        .withUser("zhangsan").password(new BCryptPasswordEncoder().encode("123")).roles("admin");
```

2、POST请求出先403错误

解决方式一：关闭csrf过滤

```java
http.csrf().disable();
```

解决方式二：在页面加上（没试过）

```xml
<input type="hidden" name="${_csrf.parameterName}" value="_csrf.token">
```

3、问题(HTTP Status 405 - Request method 'POST' not supported)

```java
http.formLogin()
        .loginProcessingUrl("/login")
        .loginPage("/login.html")
        .failureUrl("/login.html")
        //.successForwardUrl("/admin/index.html");
        //.failureForwardUrl("/admin/index.html")
        .defaultSuccessUrl("/admin/index.html", true);
```

我发现如果使用`.successForwardUrl("/admin/index.html");`会出现

```
HTTP Status 405 - Request method 'POST' not supported
```

这个错误。如果使用`.loginPage("/login.html")`就必须增加`.loginProcessingUrl("/login")`

## Springboot文件上传

springboot项目里用MultipartFile获取前端传的file为null问题

`@EnableAutoConfiguration(exclude = {MultipartAutoConfiguration.class})  `

注：springboot自带的Mutipartfile简单一点，所以将前面排除依赖的注解去掉，完全使用springboot自带的Mutipartfile就行。 

## Springboot整合FastDFS

注意：一定要保证linux上tracker和storage的端口是开放的。

## Springboot整合SpringDataRedis

```xml
<!-- https://mvnrepository.com/artifact/org.springframework.boot/spring-boot-starter-data-redis -->
<dependency>
   <groupId>org.springframework.boot</groupId>
   <artifactId>spring-boot-starter-data-redis</artifactId>
   <version>2.0.3.RELEASE</version>
</dependency>
```

```yaml
spring:
  redis:
    host: 192.168.1.127
    port: 6379
```

基本操作：

```java
//设置值
redisTemplate.boundValueOps("key").set("lion");
//取值
redisTemplate.boundValueOps("key").get();
//删除值
redisTemplate.delete("key");
```

set集合操作：

```java
//集合中添加值
redisTemplate.boundSetOps("setName").add("lion");
redisTemplate.boundSetOps("setName").add("jack");
redisTemplate.boundSetOps("setName").add("lucy");
//集合成员
redisTemplate.boundSetOps("setName").members();
//删除值
redisTemplate.boundSetOps("setName").members();
//删除集合
redisTemplate.delete("setName");
```

list集合操作：

```java
//右压栈：后添加的元素排在后面（队列）
redisTemplate.boundListOps("listName").rightPush("lion");
redisTemplate.boundListOps("listName").rightPush("jack");
redisTemplate.boundListOps("listName").rightPush("lucy");
//前10个元素，下标从0开始
redisTemplate.boundListOps("listName").range(0, 10);
//左压栈:先添加的元素排在后面（栈）
redisTemplate.boundListOps("listName").leftPush("lion");
redisTemplate.boundListOps("listName").leftPush("jack");
redisTemplate.boundListOps("listName").leftPush("lucy");
//删除集合
redisTemplate.delete("listName");
//按索引位置查询
redisTemplate.boundListOps("listName").index(2);
//删除两个“jack”，2代表数量，从前往后算起
redisTemplate.boundListOps("listName").remove(2, "jack");
```

hash操作：

```java
//添加值
redisTemplate.boundHashOps("hashName").put("hacker","lion");
redisTemplate.boundHashOps("hashName").put("girl","lucy");
redisTemplate.boundHashOps("hashName").put("boy","jack");
//获取所有key
redisTemplate.boundHashOps("hashName").keys();
//获取所有值
redisTemplate.boundHashOps("hashName").values();
//根据key取值
redisTemplate.boundHashOps("hashName").get("hacker");
//删除某个键值对
redisTemplate.boundHashOps("hashName").delete("boy")
//删除整个hash
redisTemplate.boundHashOps("hashName").keys()
```

## SpringBootTest

问题：

```powershell
Found multiple @SpringBootConfiguration annotated classes
```

测试的时候，要把所有服务都停掉。