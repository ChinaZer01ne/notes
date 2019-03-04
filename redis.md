# Redis

## Redis的安装

1、用lion账号登入虚拟机

2、wget 下载redis。发现没有wget命令，那么就 yum install wget

3、yum命令必须sudo，然后我的lion用户没有加入sudo组中

4、把lion加入sudo组里

5、安装wget，下载redis

6、解压

7、进入redis目录，make，报错，发现我机器上没有gcc

8、sudo yum install gcc

9、执行make后，Linux让我尝试执行make test

10、报错了，缺少tcl

11、sudo yum install tcl

12、make install PREFIX=指定目录

## Redis高可用哨兵模式搭建

一主二从三哨兵模式

### Redis安装

1、Redis下载

```shell
wget http://download.redis.io/releases/redis-4.0.11.tar.gz
```

2、解压

```shell
tar zxcf redis-4.0.11.tar.gz
```

3、进入目录

```shell
cd redis-4.0.11/
```

4、安装

```shell
 cd src 
 make
 yum install -y tcl
 make test
 make install PREFIX=/home/redis-sentinel #指定目录安装，注意 PREFIX 大写
```

### Redis主从配置

5、配置文件

```shell
mkdir /home/redis-sentinel/conf
cp redis.conf /home/redis-sentinel/conf
cd /home/redis-sentinel/conf
vi redis.conf	#编辑配置文件
```

将`bind 127.0.0.1`注释掉，这个是绑定能访问redis的ip地址，如果指定，那么只有指定的ip可以访问redis

将`daemonize no`修改为`daemonize yes`,设置为后台启动

将`protected-mode yes`修改为`protected-mode no`，设置为非保护模式，这样可以进行远程访问，如果不改，哨兵配置的如果是真实ip地址（非127.0.0.1），则没法访问。

非保护模式会有安全问题，我们可以给redis和哨兵设置密码，增强安全性。

```shell
cp redis.conf redis-6380.conf	
cp redis.conf redis-6381.conf	#复制两份
```

分别修改``redis-6380.conf`和`redis-6381.conf`的端口号等并且加入一下配置。

```tex
slaveof [主节点ip] [主节点端口号]	如：slaveof 192.168.1.27
```

6、启动redis

```shell
./redis-server ../conf/redis.conf 
./redis-server ../conf/redis-6380.conf 
./redis-server ../conf/redis-6381.conf 
```

到此Redis的主从配置就完成了，可以通过

```shell
 ./redis-cli -p [端口号]
```

登录redis进行测试主从复制是否成功。

### Redis哨兵配置

1、从下载文件中复制`sentinel.conf`配置文件到安装目录下

2、在配置文件中修改以下内容

```shell
port 26379	#哨兵的端口
sentinel monitor s1 <ip地址> 6379 2	
sentinel monitor s2 <ip地址> 6379 2	# 这里是主节点的配置，如果有多个master节点，则配置多行，2代表选举的时候新的leader需要获得2票
protected-mode no
```

注意：

- 此处的`<ip地址>`不要写`127.0.0.1`,否则在使用程序连接哨兵的时候，主从切换后，程序自动获取到的IP地址是`127.0.0.1:端口`，这意味着应用程序服务器上可能是没有Redis的（如果有也可能不是同一个Redis）。
- protected-mode ：关闭保护模式（默认情况下，redis node和sentinel的protected-mode都是yes，在搭建集群时，若想从远程连接redis集群，需要将redis node和sentinel的protected-mode修改为no，若只修改redis node，从远程连接sentinel后，依然是无法正常使用的，且sentinel的配置文件中没有protected-mode配置项，需要手工添加。依据redis文档的说明，若protected-mode设置为no后，需要增加密码证或是IP限制等保护机制，否则是极度危险的。）

3、将此配置文件再复制两份，分别是`sentinel-26479.conf`和`sentinel-26579.conf`

4、分别修改复制配置文件中的端口号

5、启动哨兵

```shell
./redis-sentinel ../conf/sentinel.conf
./redis-sentinel ../conf/sentinel-26479.conf
./redis-server ../conf/sentinel-26579.conf	#或者./redis-server ../conf/sentinel-26579.conf --sentinel
```

到此哨兵配置就结束了，可以模拟宕机的情况来测试主从切换。

### Springboot整合哨兵模式

1、依赖引入

```xml
<dependency>
   <groupId>org.springframework.boot</groupId>
   <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
```

1、yaml文件的配置

```yml
spring.redis.sentinel.master=mymaster	#哨兵集群的名称
spring.redis.sentinel.nodes=192.168.1.112:26379,192.168.1.112:26479,192.168.1.112:26579	#哨兵节点
#spring.redis.sentinel.password=123456	如果有密码则设置密码
# 以下配置是org.springframework.boot.autoconfigure.data.redis低版本才有的。。
spring.redis.pool.max-active=50	#最大连接数
spring.redis.pool.max-idle=10	#最大等待连接数
spring.redis.pool.max-wait=10000	#最大等待毫秒数
spring.redis.pool.min-idle=5	#最小等待连接数
spring.redis.timeout=0	#超时时间
# 连接池相关配置需要通过spring.redis.lettuce.pool或者spring.redis.jedis.pool进行配置了，选一个就可以
#spring.redis.jedis.pool.max-active=50
#spring.redis.jedis.pool.max-idle=10
#spring.redis.jedis.pool.max-wait=10000ms
#spring.redis.jedis.pool.min-idle=5
#spring.redis.jedis.timeout=0
spring.redis.lettuce.pool.max-active=50
spring.redis.lettuce.pool.max-wait=10000ms
spring.redis.lettuce.pool.max-idle=8
spring.redis.lettuce.pool.min-idle=5
```

2、使用`spring-data-redis`中的`RedisTemplate`可以完成自动配置，开箱即用

```java
@Autowired
private RedisTemplate redisTemplate;
```

注意`redisTemplate`必须是这个名，才能自动注入,这是源码中写好的`org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration`

```java
@Bean
@ConditionalOnMissingBean(
    name = {"redisTemplate"}
)
```

### 整合的问题

```xml
<!--整合redis-->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
    <!-- 1.5的版本默认采用的连接池技术是jedis  2.0以上版本默认连接池是lettuce,
    在这里采用jedis，所以需要排除lettuce的jar -->
    <exclusions>
        <exclusion>
            <groupId>redis.clients</groupId>
            <artifactId>jedis</artifactId>
        </exclusion>
        <exclusion>
            <groupId>io.lettuce</groupId>
            <artifactId>lettuce-core</artifactId>
        </exclusion>
    </exclusions>
</dependency>
<!-- 添加jedis客户端 -->
<dependency>
    <groupId>redis.clients</groupId>
    <artifactId>jedis</artifactId>
</dependency>
<!--spring2.0集成redis所需common-pool2 必须加上，jedis依赖此 -->
<!-- spring boot 2.0 的操作手册有标注
地址是：https://docs.spring.io/spring-boot/docs/2.0.3.RELEASE/reference/htmlsingle/-->
<dependency>
    <groupId>org.apache.commons</groupId>
    <artifactId>commons-pool2</artifactId>
    <version>2.5.0</version>
    <!--<version>2.4.2</version>-->
</dependency>
```





## 缓存

1、配置properties

```properties
spring.cache.type=redis
spring.cache.cache-names=redisCache
```

2、使用缓存（注解方式）

```java
/**
 * @Cacheable(value = "reidsCache",key = "'person' + #id")
 * 表示查询出来的结果放入缓存，value与properties配置名称相同，key代表缓存的key
 * #id 表示该方法的参数id，这是可以取到的
 */
@Cacheable(value = "reidsCache",key = "'person' + #id")
@Override
public Person findById(Integer id) {
    return null;
}
/**
 * @CacheEvict(value = "reidsCache",allEntries = true)
 * 表示添加新数据的时候清空缓存
 */
@CacheEvict(value = "reidsCache",allEntries = true)
@Override
public boolean add(Person person) {
    return false;
}
```

3、使用缓存，手动添加

调用redis的Api进行手动的添加，略。

## 热点缓存问题

如果一个缓存设置了过期问题，那么在高并发的环境下就有可能出现热点缓存问题，更有可能导致缓存雪崩。

**解决方式：**

1、使用双重检测锁解决热点缓存问题



## 分布式锁

```java
/**
 * redis分布式锁
 * setnx:如果不存在这个key才设置
 * @Author: Zer01ne
 * @Date: 2019/2/19 10:11
 * @Version 1.0
 */
public class DistributedLock {

    private static final String host;
    private static final int post;
    private static final JedisPool jedisPool;

    static {
        host = "127.0.0.1";
        post = 6379;
        jedisPool = new JedisPool(host,post);
    }
   
    /**
     * Jedis set的方式（正确方式）
     * @param lockKey 锁
     * @param requestId 请求标识
     * @param timeout 超时时间
     */
    public boolean getLockByJedis(String lockKey, String requestId, int timeout){
        Jedis jedis = getJedis();
        String result = jedis.set(lockKey, requestId, "NX", "EX", timeout);
        if (Objects.equals(result,"OK")){
            return true;
        }
        return false;
    }
  
    /**
     * 释放分布式锁(正确方式，Lua语言)
     * @param jedis Redis客户端
     * @param lockKey 锁
     * @param requestId 请求标识
     * @return 是否释放成功
     */
    public static boolean releaseDistributedLock(Jedis jedis, String lockKey, String requestId) {

        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        Object result = jedis.eval(script, Collections.singletonList(lockKey), Collections.singletonList(requestId));

        if (Objects.equals(1L,result)) {
            return true;
        }
        return false;

    }
    private static Jedis getJedis(){
        return jedisPool.getResource();
    }
    
}
```