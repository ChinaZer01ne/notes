# Redis

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