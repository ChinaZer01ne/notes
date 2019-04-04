# Java新特性



## Java 8



### Stream



https://blog.csdn.net/ycj_xiyang/article/details/83624642



```java
    /**
     * 这是一个中间操作
     * 过滤出符合断言表达式的元素，返回一个Stream.
     */
    Stream<T> filter(Predicate<? super T> predicate);
```



```java
/**
 * 返回一个流，包含了经过function处理后的的元素
 * 这是一个中间操作
 */
<R> Stream<R> map(Function<? super T, ? extends R> mapper);
```



```java
/**
 * 返回一个IntStream，包含了经过function处理后的的元素
 * 这是一个中间操作
 */
IntStream mapToInt(ToIntFunction<? super T> mapper);
//返回一个LongStream
LongStream mapToLong(ToLongFunction<? super T> mapper);
//返回一个DoubleStream
DoubleStream mapToDouble(ToDoubleFunction<? super T> mapper);
```



```java
/**
 * 负责通过function处理后生成stream，把所有stream聚合起来放到一个stream中
 */
<R> Stream<R> flatMap(Function<? super T, ? extends Stream<? extends R>> mapper);
```



```java
/**
 * 筛选，通过流所所生成元素的hashCode()和equals()去除重复元素
 */
Stream<T> distinct();
```



```java
/**
 * 排序
 */
Stream<T> sorted();	//根据Comparable比较
Stream<T> sorted(Comparator<? super T> comparator);
```



```java
/**
 * 截断流，使其元素不超过给定数量
 */
Stream<T> limit(long maxSize);
```



```java
/**
 * 对应limit方法，会跳过给定个数的元素
 */
Stream<T> skip(long n);
```



```java
/**
 * 生成一个包含原Stream的所有元素的新Stream，同时会提供一个消费函数（Consumer实 例），新Stream每个元素被消费的时候都会执行给定的消费函数
 */
Stream<T> peek(Consumer<? super T> action);
```



## Java 11

