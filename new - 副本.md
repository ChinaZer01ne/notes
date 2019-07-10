# 权限管理



## Shiro



### 数据库设计：

#### 1、菜单权限：

* 用户表（user）: 存储用户基本信息
* 角色表（role）: 存储系统角色信息
* 角色-用户关联表（role-user）：存储角色和用户的对应关系（n:n）
* 资源表（resource）：存储系统菜单资源，包括上级菜单，子菜单，按钮，按层级进行标识
* 资源-角色关联表（resource-role）：存储资源和角色的对应关系（n:n）

```sql
CREATE TABLE `user` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `username` varchar(32) DEFAULT NULL COMMENT '用户名',
  `password` varchar(32) DEFAULT NULL COMMENT '密码',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='用户表';

CREATE TABLE `role` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(32) DEFAULT NULL COMMENT '角色名',
  `desc` varchar(32) DEFAULT NULL COMMENT '角色描述',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='角色表';

CREATE TABLE `resource` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `title` varchar(32) DEFAULT NULL COMMENT '资源标题',
  `uri` varchar(32) DEFAULT NULL COMMENT '资源uri  ',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='资源表';

CREATE TABLE `user_role` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `user_id` int(11) NOT NULL COMMENT '用户id',
  `role_id` int(11) NOT NULL COMMENT '角色id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='用户角色表';

CREATE TABLE `role_resource` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `role_id` int(11) DEFAULT NULL COMMENT '角色id',
  `resource_id` int(11) DEFAULT NULL COMMENT '资源id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='角色资源表';

db.sql
```



#### 2、数据权限：

* 应用表（application）：存储系统中应用信息
* 角色-应用关联表(role-application) ：对应用关联角色（n:n）
* 用户-应用关联表（user-application）：将用户和应用进行关联，为用户分配对应的应用权限，比如应用管理员，研发人员，用户等。





https://blog.csdn.net/lpy1239064101/article/details/79185170





### 基础demo



```java
@Configuration
public class ShiroConfig {


    @Resource
    private CustomizedRealm customizedRealm;
    @Resource
    private SessionDAO sessionDAO;

    /**
     * 自定义身份认证 realm;
     * 必须写这个类，并加上 @Bean 注解，目的是注入 CustomizedRealm，
     * 否则会影响 CustomizedRealm类 中其他类的依赖注入
     */
    @Bean
    public CustomizedRealm customizedRealm() {
        CustomizedRealm customizedRealm = new CustomizedRealm();
        customizedRealm.setCredentialsMatcher(sha256Matcher());
        return customizedRealm;
    }
    /**
     * 凭证验证器
     * 这里也可以配置一些其他的加盐处理
     * */
    @Bean
    public HashedCredentialsMatcher sha256Matcher() {
        HashedCredentialsMatcher hashedCredentialsMatcher = new HashedCredentialsMatcher();
        // 散列算法
        hashedCredentialsMatcher.setHashAlgorithmName("md5");
        //散列的次数，比如散列两次，相当于 md5(md5(""));
        hashedCredentialsMatcher.setHashIterations(1);
        return hashedCredentialsMatcher;
    }
    /**
     * 权限管理，配置主要是Realm的管理认证
     */
    @Bean
    public SessionsSecurityManager securityManager() {
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        // 将自己的验证方式加入容器
        securityManager.setRealm(customizedRealm());
        securityManager.setSessionManager(defaultWebSessionManager());
        return securityManager;
    }

    /**
     * 可以配置sessionDAO来做不允许同一账号，重复登录的功能，但总感觉这不是一个好方法
     * 一般都是用数据库记录token的方式来做避免重复登录，每次登录，对比数据库的token，如果不一样，就更新一下，那么之前登录的信息就失效了，如果一样，则不做什么，
     * 这个token，可以自己生成，也可以用JWT，也可以使用shiro的JSESSIONID
     */
    @Bean
    public SessionDAO sessionDAO(){
        return new MemorySessionDAO();
    }
    @Bean
    public DefaultWebSessionManager defaultWebSessionManager() {
        DefaultWebSessionManager defaultWebSessionManager = new DefaultWebSessionManager();
        defaultWebSessionManager.setSessionDAO(sessionDAO());
        return defaultWebSessionManager;
    }


    @Bean
    public ShiroFilterFactoryBean shiroFilter(SessionsSecurityManager securityManager) {
        ShiroFilterFactoryBean shiroFilterFactoryBean = new ShiroFilterFactoryBean();
        // 必须设置SecuritManager
        shiroFilterFactoryBean.setSecurityManager(securityManage
        // 如果不设置默认会自动寻找Web工程根目录下的"/login.jsp"页面
        shiroFilterFactoryBean.setLoginUrl("/user/login");
        // 登录成功后要跳转的链接
        shiroFilterFactoryBean.setSuccessUrl("/user/index");
        // 未授权界面;
        shiroFilterFactoryBean.setUnauthorizedUrl("/403");
        // 拦截器
        Map<String, String> filterChainDefinitionMap = new LinkedHashMap<>();
        // 过滤链定义，从上向下顺序执行，一般将 /**放在最为下边
        // authc:所有url都必须认证通过才可以访问; anon:所有url都都可以匿名访问
        filterChainDefinitionMap.put("/**", "authc");
        shiroFilterFactoryBean.setFilterChainDefinitionMap(filterChainDefinitionMap);

        return shiroFilterFactoryBean;
    }


}
```





```java
/**
 *
 * 自定义Realm
 * @author Zer01ne
 * @version 1.0
 * @date 2019/7/8 17:02
 */
public class CustomizedRealm extends AuthorizingRealm {

    /**
     * 用于用户查询
     */
    @Autowired
    private UserService userService;

    /**
     * 为当前登录的用户授予角色和权限
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        //获取登录用户名
        String userName = (String) principalCollection.getPrimaryPrincipal();
        //查询用户名称
        User user = userService.getByUserName(userName);
        //添加角色和权限
        SimpleAuthorizationInfo simpleAuthorizationInfo = new SimpleAuthorizationInfo();
        for (Role role : user.getRoles()) {
            //添加角色
            simpleAuthorizationInfo.addRole(role.getRoleName());
            //添加权限
            for (Permission permission : role.getPermissions()) {
                simpleAuthorizationInfo.addStringPermission(permission.getPermission());
            }
        }
        return simpleAuthorizationInfo;
    }
    /**
     * 验证当前登录的用户
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        //加这一步的目的是在Post请求的时候会先进认证，然后在到请求
        if (token.getPrincipal() == null) {
            return null;
        }
        String userName = (String) token.getPrincipal();
        User user = userService.getByUserName(userName);


        if (user != null){
            SecurityUtils.getSubject().getSession().setAttribute("curUser",user);
            return new SimpleAuthenticationInfo(user,user.getPassword(),getName());
        }
        return null;
    }
}
```





```java
@PostMapping("login")
public void login(@RequestParam("username")String username, @RequestParam("password") String password){
    Subject subject = SecurityUtils.getSubject();
    // 因为已经配置了凭证验证器，所以这里的password就不用手动进行加密了
    UsernamePasswordToken token = new UsernamePasswordToken(username, password);

    // 登录验证
    subject.login(token);
    System.out.println(subject.getSession().getId());
    Collection<Session> activeSessions = sessionDAO.getActiveSessions();

    if (subject.isAuthenticated()){
        //遍历所有的session做这个功能其实不是个好方法，一般用数据库存储token的方式做
        for (Session activeSession : activeSessions) {
            //方法一、当第二次登录时，给出提示“用户已登录”，停留在登录页面
            User user = (User) activeSession.getAttribute("curUser");
            //if (Objects.equals(user.getUserName(),username)){
            //    activeSessions.remove(activeSession);
            //    subject.logout();
            //    throw new RuntimeException("用户已登录");
            //}
            //方法二、当第二次登录时，把第其他同账号的session剔除
            if (Objects.equals(user.getUserName(),username) && activeSession.getId() != subject.getSession().getId()){
                activeSession.setTimeout(0);
            }
        }

    }



}
```

## Spring Security

