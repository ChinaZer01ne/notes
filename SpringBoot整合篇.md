# SpringBoot整合篇

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

## Springboot整合Mysql8.0

```yaml
spring:
  datasource:
    username: lion
    password: lion
    url: jdbc:mysql://localhost:3306/pinyougoudb?useUnicode=true&characterEncoding=utf8&autoReconnect=true&useSSL=false&serverTimezone=GMT
    driver-class-name: com.mysql.cj.jdbc.Driver
    type: com.alibaba.druid.pool.DruidDataSource
```

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



## Springboot整合SpringDataSolr

### 1、引入依赖

```xml
<!-- https://mvnrepository.com/artifact/org.springframework.boot/spring-boot-starter-data-solr -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-solr</artifactId>
    <version>2.0.2.RELEASE</version>
</dependency>
```

### 2、配置yml

```yaml
spring:
  data:
    solr:
      host: http://192.168.1.127:8983/solr
      repositories:
       enabled: false
```

### 3.自动注入

#### 第一种方式：

首先在SpringBoot启动类中配置`SolrTemplate`

```java
@Autowired
private SolrClient solrClient;
@Bean
public SolrTemplate solrTemplate(){
    return new SolrTemplate(solrClient);
}
```

使用

```java
@Autowired
private SolrTemplate solrTemplate;
```

#### 第二种方式：

```java
@Configuration
public class SolrConfig {

    @Value("${spring.data.solr.host}")
    String solrURL;

    @Bean
    public SolrClient solrClient() {
    return new HttpSolrClient.Builder(solrURL).build();
    }

    @Bean
    public SolrTemplate solrTemplate(SolrClient client) throws Exception {
    return new SolrTemplate(client);
    }
}
```

#### 导入测试

```java
@Autowired
private TbItemMapper tbItemMapper;
@Autowired
private SolrTemplate solrTemplate;
public void importTbItemData(){
    TbItemExample example = new TbItemExample();
    TbItemExample.Criteria criteria = example.createCriteria();
    criteria.andStatusEqualTo("1");//审核成功的才导入
    List<TbItem> list = tbItemMapper.selectByExample(example);
    solrTemplate.saveBeans("core1",list);
    solrTemplate.commit("core1");
}
```



### 4.基本操作

注意：实体类必须加入对应注解（`@Field`，动态域需要在`@Field`多加一个`@Dynamic`），否则无法索引储存

```java
@Field
private Long id;
@Field
private String title;
@Field
private Long goodsId;
@Field
private String category;
@Field
private String brand;
@Field
private String seller;
```

添加

```java
//添加
TbItem tbItem = new TbItem();
tbItem.setId(1L);
tbItem.setTitle("小米");
tbItem.setCategory("手机");
tbItem.setBrand("小米");
tbItem.setSeller("小米旗舰店");
tbItem.setGoodsId(10L);
//添加BigDecimal类型的时候出现了异常，不清楚是怎么回事
//tbItem.setPrice(new BigDecimal(3000.01));
solrTemplate.saveBean("core1",tbItem);
solrTemplate.commit("core1");
```

根据id获取

```java
//根据id获取
Optional<TbItem> optional = solrTemplate.getById("core1", 1L, TbItem.class);
if (optional.isPresent()){			
    TbItem tbItem = optional.get();	
    System.out.println(tbItem.getTitle());
}

```

删除记录

```java
//删除记录
solrTemplate.deleteByIds("core1",String.valueOf(1L));
solrTemplate.commit("core1");
```

添加集合

```java
//添加集合
List list = new ArrayList();
for (int i = 0; i < 100; i++) {
   TbItem tbItem = new TbItem();
   tbItem.setId(Long.valueOf(i));
   tbItem.setTitle("小米");
   tbItem.setCategory("手机");
   tbItem.setBrand("小米");
   tbItem.setSeller("小米旗舰店");
   tbItem.setGoodsId(10L);
   //tbItem.setPrice(new BigDecimal(3000.01));
   list.add(tbItem);
}
solrTemplate.saveBeans("core1",list);
solrTemplate.commit("core1");
```

执行查询

```java
//执行查询
Query query = new SimpleQuery("*:*");
query.setOffset(20L);	//查询条件
query.setRows(20);
ScoredPage<TbItem> page = solrTemplate.queryForPage("core1", query, TbItem.class);
for (TbItem tbItem : page.getContent()){
   System.out.println(tbItem.getTitle() + " " + tbItem.getBrand());
}
System.out.println(page.getTotalElements());	//总记录数
System.out.println(page.getTotalPages());		//总页数
```

条件查询

```java
//条件查询
Query query = new SimpleQuery("*:*");
Criteria criteria = new Criteria("item_category").contains("手机");
criteria = criteria.and("item_brand").contains("2");
query.addCriteria(criteria);
query.setOffset(20L);
query.setRows(20);
ScoredPage<TbItem> page = solrTemplate.queryForPage("core1", query, TbItem.class);
for (TbItem tbItem : page.getContent()){
    System.out.println(tbItem.getTitle() + " " + tbItem.getBrand());
}
System.out.println(page.getTotalElements());
System.out.println(page.getTotalPages());
```

删除所有记录

```java
//删除所有记录
Query query = new SimpleQuery("*:*");
solrTemplate.delete("core1",query);
solrTemplate.commit("core1");
```



以下代码基于SpringBoot2.0.



搜索高亮显示业务代码

```java
//高亮显示
HighlightQuery query = new SimpleHighlightQuery();
//构建高亮显示对象
HighlightOptions highlightOptions = new HighlightOptions().addField("item_title");
highlightOptions.setSimplePrefix("<em style='color:red'>");
highlightOptions.setSimplePostfix("</em>");
//为查询对象设置高亮选项
query.setHighlightOptions(highlightOptions);
Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
query.addCriteria(criteria);
//高亮页对象
HighlightPage<TbItem> highlightPage = solrTemplate.queryForHighlightPage("core1", query, TbItem.class);
//高亮入口集合
List<HighlightEntry<TbItem>> highlighted = highlightPage.getHighlighted();
for (HighlightEntry<TbItem> entry :
        highlighted) {
    //获取高亮列表(包含所有的高亮域)
    List<HighlightEntry.Highlight> list = entry.getHighlights();
    //for (HighlightEntry.Highlight highlight:    //每个高亮域可能有多个值
    //        list){
    //    List<String> snipplets = highlight.getSnipplets();
    //    System.out.println(snipplets);
    //}
    if (list.size() > 0 && list.get(0).getSnipplets().size() > 0){
        TbItem item = entry.getEntity();       
        item.setTitle(list.get(0).getSnipplets().get(0));
    }
}
map.put("rows",highlightPage.getContent()); // entry.getEntity() 和 highlightPage.getContent()是同一个引用，改变了entry.getEntity()就相当于改变highlightPage.getContent()
return map;
```



分组查询

```java
private List searchCategoryList(Map searchMap){
    List<String> list = new ArrayList();
    Query query = new SimpleQuery("*:*");
    //根据关键字查询
    Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));    //where..
    query.addCriteria(criteria);
    //设置分组选项
    GroupOptions groupOptions = new GroupOptions().addGroupByField("item_category"); //group by
    groupOptions.setOffset(0);  //看源码发现必须有这两个属性才能从GroupOptions获取PageRequest对象，否则报错，Pageable must not be null!
    groupOptions.setLimit(0);
    System.out.println(groupOptions.getPageRequest());
    query.setGroupOptions(groupOptions);
    //分组页
    GroupPage<TbItem> groupPage = solrTemplate.queryForGroupPage("core1", query, TbItem.class);
    //获取分组结果对象
    GroupResult<TbItem> groupResult = groupPage.getGroupResult("item_category");
    //获取分组入口页
    Page<GroupEntry<TbItem>> groupEntries = groupResult.getGroupEntries();
    //获取分组入口集合
    List<GroupEntry<TbItem>> content = groupEntries.getContent();

    for (GroupEntry entry:
         content) {
        //将分组结果添加到返回值中
        list.add(entry.getGroupValue());
    }
    return list;
}
```

注意：GroupOptions必须设置`offset`和`limit`属性，看源码发现必须有这两个属性才能从GroupOptions获取PageRequest对象，否则有如下异常：

`Pageable must not be null!`



过滤查询

```java
//1.2 商品分类过滤查询
if (!Objects.equals(searchMap.get("category"), "")){
    FilterQuery filterQuery = new SimpleQuery();
    Criteria filterCriteria = new Criteria("item_category").is(searchMap.get("category"));
    filterQuery.addCriteria(filterCriteria);
    query.addFilterQuery(filterQuery);
}
```

按价格查询

```java
//1.5 按价格查询
if (!Objects.equals(searchMap.get("price"), "")){
    String price = (String) searchMap.get("price");
    String[] split = price.split("-");
    if (!Objects.equals(split[0], "0")){
        FilterQuery filterQuery = new SimpleQuery();
        Criteria filterCriteria = new Criteria("item_price").greaterThanEqual(split[0]);
        filterQuery.addCriteria(filterCriteria);
        query.addFilterQuery(filterQuery);
    }
    if (!Objects.equals(split[1], "*")){
        FilterQuery filterQuery = new SimpleQuery();
        Criteria filterCriteria = new Criteria("item_price").lessThanEqual(split[1]);
        filterQuery.addCriteria(filterCriteria);
        query.addFilterQuery(filterQuery);
    }

}
```

分页

```java
//1.6 分页
Integer pageNo = (Integer) searchMap.get("pageNo");
if (pageNo == null){
    pageNo = 1;
}
Integer pageSize = (Integer) searchMap.get("pageSize");
if (pageSize == null){
    pageSize = 20;
}
query.setOffset((long) ((pageNo - 1) * pageSize));
query.setRows(pageSize);
```

按价格排序

```java
//1.7 按价格排序
String sortField = (String) searchMap.get("sortField");
String sort = (String) searchMap.get("sort");
if (sort != null && !Objects.equals(sort, "")){
    Sort querySort = new Sort(Sort.Direction.fromString(sort), sortField);
    query.addSort(querySort);
}
```





###  5、动态域的添加

```java
@Dynamic
@Field("item_spec_*")
private Map<String, String> specMap;	//>必须写泛型
```

注意：Map<String, String>必须写泛型，否则报如下异常:

`org.springframework.data.solr.UncategorizedSolrException: Can't resolve required map value type for interface java.util.Map!`

#### 6、定时更新索引

##### 在spring配置文件中配置地址

```properties
# solr定时更新地址
# command=delta-import 增量更新 command=full-import 全量更新
# entity=document表示要更新的entity的name,但是注意此名称非java中实体类的名称，而是solr_core中data-config.xml中的entity的name
# clean=false  表示不清空solr中数据,true表示清空数据。全量更新操作的时候设为true，增量更新的时候设为false
# commit=true  提交必须是true
# 地址可以从网页端抓包看到
SOLR_DELTA_PARAM=/core1/dataimport?command=delta-import&entity=person&clean=false&commit=true
SOLR_FULL_PARAM=/core1/dataimport?command=full-import&entity=person&clean=true&commit=true&core=core1&name=dataimport&indent=on

```

##### 添加定时任务

```java
//注入全量更新地址或者增量更新地址
@Value("${SOLR_DELTA_PARAM}")
private String SOLR_DELTA_PARAM;
//设置定时任务
@Scheduled(cron = "0/10 * * * * ? ")
public void scheduled(){
    //System.out.println(new Date());
    //创建SolrQuery，设置请求
    SolrQuery solrQuery = new SolrQuery();
    solrQuery.setRequestHandler(SOLR_DELTA_PARAM);
    try {
        //发送请求
        solrClient.query(solrQuery);
    } catch (Exception e) {
        e.printStackTrace();
    }
}
```

**注意：需要在配置类上添加注解`@EnableScheduling`来开启定时任务**

```java
@EnableScheduling
public class GithubApplication {
    ....
}
```

## SpringBoot整合FreeMarker

1、引入依赖

```xml
<!-- https://mvnrepository.com/artifact/org.springframework.boot/spring-boot-starter-freemarker -->
<dependency>
   <groupId>org.springframework.boot</groupId>
   <artifactId>spring-boot-starter-freemarker</artifactId>
</dependency>
```

2.测试

```java
@Test
public void testFreeMarker() throws IOException, TemplateException {
   //1.创建一个配置对象
   Configuration configuration = new Configuration(Configuration.getVersion());
   //2.设置模板所在目录
   configuration.setDirectoryForTemplateLoading(new File("C:\\idea_work\\pinyougou-parent\\pinyougou-portal-web\\src\\main\\resources\\templates"));
   //3.设置字符集
   configuration.setDefaultEncoding("utf-8");
   //4.获取模板对象
   Template template = configuration.getTemplate("test.ftl");
   //5.创建数据模型
   Map map = new HashMap();
   map.put("name","张三");
   map.put("message","good morning!");
   //6.创建一个输出流
   FileWriter fileWriter = new FileWriter("C:\\UserSoft\\index.html");
   //7.属楚
   template.process(map,fileWriter);
    fileWriter.close();
}
```

3、整合

```yaml
spring:
  freemarker:
      template-loader-path: classpath:/templates	#模板的路径
      cache: false
      charset: UTF-8
      check-template-location: true
      content-type: text/html
      expose-request-attributes: false
      expose-session-attributes: false
      request-context-attribute: request
      suffix: .ftl
```

```java
Map map = new HashMap<>();
TbGoods tbGoods = goodsMapper.selectByPrimaryKey(goodsId);
TbGoodsDesc tbGoodsDesc = goodsDescMapper.selectByPrimaryKey(goodsId);
map.put("goods",tbGoods);
map.put("goodsDesc",tbGoodsDesc);
try {
    Template temp = configuration.getTemplate("item.ftl");	//因为yaml中已经配置了模板所在路径，所															以这里只写文件名就可以了。
    String path="C:\\UserSoft\\item.ftl";
    Writer file = new FileWriter(path);
    temp.process(map, file);
    file.flush();
    file.close();
} catch (IOException e) {
    e.printStackTrace();
} catch (TemplateException e) {
    e.printStackTrace();
}
```

## SpringBootTest

问题：

```powershell
Found multiple @SpringBootConfiguration annotated classes
```

测试的时候，要把所有服务都停掉。

## SpringTask定时任务

```java
@EnableScheduling	//开启任务调度扫描
@Component
public class SeckillTask {
    @Scheduled(cron = "* * * * * ?")	//每秒执行一次
    public void refreshSeckillGoods(){
        System.out.println("执行了任务调度" + new Date());
    }
}
```

## SpringBoot整合ActiveMQ

1、引入依赖

```xml
<!-- activemq -->
<dependency>
   <groupId>org.springframework.boot</groupId>
   <artifactId>spring-boot-starter-activemq</artifactId>
</dependency>
```

2、yml配置

```yaml
spring:
  data:
    solr:
      host: http://192.168.1.127:8983/solr
  activemq:
    broker-url: tcp://192.168.1.127:61616
    user: admin
    password: admin
    pool:
      enabled: false
```

3、整合

点对点模式与发布订阅模式大同小异。

生产者：

```java
@Autowired // 也可以注入JmsTemplate，JmsMessagingTemplate对JmsTemplate进行了封装
private JmsMessagingTemplate jmsTemplate;

public void sendMessage(Destination destination, String message){//Destination参数也可以选择自己@Bean，然后注入进来
    /*@Bean
    public Queue queue(){
        return new ActiveMQQueue("queue");
    }*/
    
    // 发送消息，destination发送到的队列的名称，message是发送的消息
    jmsTemplate.convertAndSend(destination, message);
}
```

消费者：

第一种方式：（简单）

```java
/**
 *	JmsListener注解默认只接收queue消息,如果要接收topic消息,需要设置containerFactory
 */
@JmsListener(destination = "test-queue")	//队列名称
public void testJmsTemplateConsumer(String text){
    System.out.println(text);
}
```



第二种方式：

```java
public void testJmsTemplateConsumer(){
    //如果是Topic模式，那么就换成Topic类
    Destination destination = new Queue() {
        @Override
        public String getQueueName() throws JMSException {
        	return "test-queue";
        }
    };
    System.out.println(jmsTemplate.receive(destination).getPayload());
}
```



**注意**：**如果采用发布订阅模式（Topic），JmsListener注解默认只接收queue消息,如果要接收topic消息,需要设置containerFactory属性**

```java
@JmsListener(destination = "${topicDestination}", containerFactory = "jmsListenerContainerFactory")
```

```java
@Bean
public JmsListenerContainerFactory jmsListenerContainerFactory(ConnectionFactory activeMQConnectionFactory){
   DefaultJmsListenerContainerFactory topicListenerContainer = new DefaultJmsListenerContainerFactory();
   topicListenerContainer.setPubSubDomain(true);
   topicListenerContainer.setConnectionFactory(activeMQConnectionFactory);
   return topicListenerContainer;

}
```

