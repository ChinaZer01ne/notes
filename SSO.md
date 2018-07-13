# SSO

## CAS

### CAS架构

![img](images\CAS\CAS架构.gif) 

### CAS配置

#### 关闭Https配置

原因：不关闭，对接的时候会有问题，单点登录会跳转，但是不会存储。如果你有Https证书就不需要了。

1、/WEB-INF/deployerConfigContext.xml

```xml
  <!-- Required for proxy ticket mechanism. -->
    <bean id="proxyAuthenticationHandler"
          class="org.jasig.cas.authentication.handler.support.HttpBasedServiceCredentialsAuthenticationHandler"
          p:httpClient-ref="httpClient" p:requireSecure="false"/>
```

2、/WEB-INF/spring-configuration/ticketGrantingTicketCookieGenerator.xml

```xml
<bean id="ticketGrantingTicketCookieGenerator" class="org.jasig.cas.web.support.CookieRetrievingCookieGenerator"
	p:cookieSecure="false"
	p:cookieMaxAge="3600"	<!-- 设置cookie失效时间-->
	p:cookieName="CASTGC"
	p:cookiePath="/cas" />
```
3、/WEB-INF/spring-configuration/warnCookieGenerator.xml

```xml
	<bean id="warnCookieGenerator" class="org.jasig.cas.web.support.CookieRetrievingCookieGenerator"
		p:cookieSecure="false"
		p:cookieMaxAge="3600"	<!-- 设置cookie失效时间-->
		p:cookieName="CASPRIVACY"
		p:cookiePath="/cas" />
</beans>
```



#### 单点登出重定向配置

/WEB-INF/cas-servlet.xml 

```xml
  <bean id="logoutAction" class="org.jasig.cas.web.flow.LogoutAction"
        p:servicesManager-ref="servicesManager"
        p:followServiceRedirects="${cas.logout.followServiceRedirects:true}"/>
```



#### 数据源配置

这一步是为了让CAS从数据库里查询用户。

在文件最后追加以下三个bean

```xml
<!--配置响应的数据源-->
<bean id="dataSource" class="com.mchange.v2.c3p0.ComboPooledDataSource"
        p:driverClass="com.mysql.cj.jdbc.Driver"	<!--因为我的数据库是mysql8.0，如果是5.7请用com.mysql.jdbc.Driver驱动-->
        p:jdbcUrl="jdbc:mysql://192.168.1.119:3306/db?useUnicode=true&amp; characterEncoding=utf8&amp;autoReconnect=true&amp;useSSL=false&amp;serverTimezone=GMT"
        p:user="lion"
        p:password="lion"
      />
	<!--认证处理器-->
      <bean id="dbAuthHandler"  
      class="org.jasig.cas.adaptors.jdbc.QueryDatabaseAuthenticationHandler"  
      p:dataSource-ref="dataSource"  
      p:sql="select password from tb_user where username = ?"  
      p:passwordEncoder-ref="passwordEncoder"/>  
		<!--如果数据库里的密码是加密的，需要配置响应的加密方式-->
      <bean id="passwordEncoder" 
class="org.jasig.cas.authentication.handler.DefaultPasswordEncoder"  
    c:encodingAlgorithm="MD5"  
    p:characterEncoding="UTF-8" /> 
```

修改以下位置：

```xml
<bean id="authenticationManager" class="org.jasig.cas.authentication.PolicyBasedAuthenticationManager">
        <constructor-arg>
            <map>
                <entry key-ref="proxyAuthenticationHandler" value-ref="proxyPrincipalResolver" />
                <!--将此处改为自己的认证处理器 -->
                <entry key-ref="dbAuthHandler" value-ref="primaryPrincipalResolver" />
            </map>
        </constructor-arg>
```

#### 引入jar包

因为配置数据源，还需要jar包，数据源配的c3p0，所以引入以下jar包，也可以选用其他数据源。将jar包放到CAS系统的lib目录下。

![1531375450708](images\CAS\CAS数据源需要jar包.png)

#### 将CAS登录界面换成自己需要的登录界面

替换CAS系统下`WEB-INF\view\jsp\default\ui`中的`casLoginView.jsp`文件

（1）添加指令

```jsp
<%@ page pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
```

（2）修改form标签

```jsp
<form:form method="post" id="fm1" commandName="${commandName}" htmlEscape="true" class="sui-form">
......
</form:form>
```

（3）修改用户名和密码框

```jsp
<form:input id="username" tabindex="1" 
	accesskey="${userNameAccessKey}" path="username" autocomplete="off" htmlEscape="true" 
	placeholder="邮箱/用户名/手机号" class="span2 input-xfat" />
<form:password  id="password" tabindex="2" path="password" 
      accesskey="${passwordAccessKey}" htmlEscape="true" autocomplete="off" 
	  placeholder="请输入密码" class="span2 input-xfat"   />
```

（4）修改登陆按钮

```jsp
<input type="hidden" name="lt" value="${loginTicket}" />
<input type="hidden" name="execution" value="${flowExecutionKey}" />
<input type="hidden" name="_eventId" value="submit" />
<input class="sui-btn btn-block btn-xlarge btn-danger" accesskey="l" value="登陆" type="submit" />
```



### Spring Security整合CAS

1、	yml配置一些自定义属性

```yaml
# CAS整合配置
security:
  cas:
    server:
      host: http://192.168.1.127:8080/cas   #CAS服务端地址
      login: ${security.cas.server.host}/login  #CAS服务登录地址
      logout: ${security.cas.server.host}/logout	#CAS服务登出地址
    client:
      host: http://localhost:9106 #CAS客户端地址
      login: /login/cas   #CAS客户端登录地址
      logout: /logout     #CAS客户端登出地址
```



2、有关CAS的配置：

```java
/**
 * CAS的整合配置
 * @author Lion
 * @since 2018/7/11 9:53
 */
@Configuration
public class CasConfig {

    @Autowired
    private Environment environment;

    @Bean
    public ServiceProperties serviceProperties(){
        ServiceProperties serviceProperties = new ServiceProperties();
        //固定写法：CAS客户端地址+/login/cas,如 http://localhost:9003/login/cas
        serviceProperties.setService(environment.getProperty("security.cas.client.host") + environment.getProperty("security.cas.client.login"));
        serviceProperties.setAuthenticateAllArtifacts(true);
        return serviceProperties;
    }
    /**
     * 入口点的配置
     * @param serviceProperties : 因为CasAuthenticationEntryPoint需要一个ServiceProperties，所以注入进来
     * @return org.springframework.security.web.AuthenticationEntryPoint
     * @throws
     */
    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint(ServiceProperties serviceProperties){
        CasAuthenticationEntryPoint casAuthenticationEntryPoint = new CasAuthenticationEntryPoint();
        casAuthenticationEntryPoint.setServiceProperties(serviceProperties);
        //服务端入口点地址
        casAuthenticationEntryPoint.setLoginUrl(environment.getProperty("security.cas.server.login"));
        return casAuthenticationEntryPoint;
    }
    @Bean
    public CasAuthenticationFilter casAuthenticationFilter(AuthenticationManager authenticationManager){
        CasAuthenticationFilter casAuthenticationFilter = new CasAuthenticationFilter();
        //需要认证管理器
        casAuthenticationFilter.setAuthenticationManager(authenticationManager);
        casAuthenticationFilter.setFilterProcessesUrl(environment.getProperty("security.cas.client.login"));
        casAuthenticationFilter.setContinueChainBeforeSuccessfulAuthentication(false);
        return casAuthenticationFilter;
    }
    /**
     * 认证管理器
     * @return org.springframework.security.authentication.AuthenticationManager
     * @throws
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationProvider authenticationProvider){

        return new ProviderManager(
                Arrays.asList(authenticationProvider));
    }
    /**
     * 票据验证器
     * @return org.jasig.cas.client.validation.Cas20ServiceTicketValidator
     * @throws
     */
    @Bean
    public Cas20ServiceTicketValidator cas20ServiceTicketValidator() {
        return new Cas20ServiceTicketValidator(environment.getProperty("security.cas.server.host"));
    }
    /**
     * 认证提供者
     * @param userDetailsService :  认证类
     * @param serviceProperties :
     * @param cas20ServiceTicketValidator : 票据验证器
     * @return org.springframework.security.authentication.AuthenticationProvider
     * @throws
     */
    @Bean
    public AuthenticationProvider authenticationProvider(UserDetailsService userDetailsService,
                                                         ServiceProperties serviceProperties,
                                                         Cas20ServiceTicketValidator cas20ServiceTicketValidator){
        CasAuthenticationProvider authenticationProvider = new CasAuthenticationProvider();
        authenticationProvider.setKey("casProvider");
        authenticationProvider.setUserDetailsService(userDetailsService);
        authenticationProvider.setServiceProperties(serviceProperties);
        authenticationProvider.setTicketValidator(cas20ServiceTicketValidator);
        return authenticationProvider;
    }
    /**
     * 单点登出
     * @return org.springframework.security.web.authentication.logout.LogoutFilter
     * @throws
     */
    @Bean
    public LogoutFilter logoutFilter(){
        String logoutPath = environment.getProperty("security.cas.server.logout") + "?service=" + environment.getProperty("security.cas.client.host");
        LogoutFilter logoutFilter = new LogoutFilter(logoutPath, new SecurityContextLogoutHandler());
        logoutFilter.setFilterProcessesUrl(environment.getProperty("security.cas.client.logout"));
        return logoutFilter;
    }
   
}

```

3、有关Spring Security的配置

```java
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter{


    @Autowired
    private Environment environment;

    @Autowired
    private CasAuthenticationFilter casAuthenticationFilter;

    @Autowired
    private LogoutFilter logoutFilter;

    @Autowired
    private CasAuthenticationProvider casAuthenticationProvider;

    @Autowired
    private AuthenticationEntryPoint authenticationEntryPoint;

    @Override
    public void configure(WebSecurity web) throws Exception {
        super.configure(web);
        web.ignoring().antMatchers("/css/**", "/img/**","/js/**","/plugins/**","/register.html","/user/add");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {


        // 单点注销的过滤器
        SingleSignOutFilter singleSignOutFilter = new SingleSignOutFilter();
        singleSignOutFilter.setCasServerUrlPrefix(environment.getProperty("security.cas.server.host"));

        http.authorizeRequests().antMatchers("/login/cas").permitAll()
                .and()
                .authorizeRequests().anyRequest().authenticated()
                .and()
                //入口点的配置（CAS），也可以是别的单点登陆框架,
                // 这里的参数是一个接口AuthenticationEntryPoint，
                // CasAuthenticationEntryPoint是一个实现类，可见spring制定了一个整合其他单点登陆规范
                .exceptionHandling().authenticationEntryPoint(authenticationEntryPoint)
                .and()
                .logout().logoutSuccessUrl("/logout")
                .and().addFilter(casAuthenticationFilter)
                .addFilterBefore(singleSignOutFilter, CasAuthenticationFilter.class).addFilterBefore(logoutFilter, LogoutFilter.class);

        http.headers().frameOptions().disable();
        http.csrf().disable();

    }


    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(casAuthenticationProvider);
    }

    @Bean
    public ServletListenerRegistrationBean<SingleSignOutHttpSessionListener> singleSignOutHttpSessionListener(){
        ServletListenerRegistrationBean<SingleSignOutHttpSessionListener> servletListenerRegistrationBean =
                new ServletListenerRegistrationBean<>();
        servletListenerRegistrationBean.setListener(new SingleSignOutHttpSessionListener());
        return servletListenerRegistrationBean;
    }

    /**
     * 自定义认证类(和CAS整合后，这个类并不起认证作用，主要是用来返回角色列表)
     * @return org.springframework.security.core.userdetails.UserDetailsService
     * @throws
     */
    @Bean
    public UserDetailsService authenticationUserDetailsServicel(){

        return new UserDetailsService(){
            @Override
            public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
                System.out.println(username + "路过~");
                List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
                grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_USER"));
                return new User(username,"",grantedAuthorities);
            }
        };
    }
}

```



