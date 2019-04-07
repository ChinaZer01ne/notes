# Java新特性



## Java 8

### 函数式接口

1、如果一个接口只有一个抽象方法，那么该接口就是一个函数式接口。

2、如果我们在某接口上声明了FunctionalInterfae注解，那么编译器就会按照函数式接口的定义来要求该接口。

3、如果某个接口只有一个抽象方法，但我们斌没有给该接口声明FunctionalInterface注解，那么编译器依旧会将该接口看作是函数式接口。

对于一个函数式接口加不加`@FunctionalInterface`注解，程序都可以正常运行，但我们最好加上，好比`@override`，类似是一种规范吧。

**注意：函数式接口只有一个抽象接口，但可以复写`Object`对象的方法。因为接口本身就继承自`Object`对象。**



函数式接口的实例我们可以通过**lambda表达式，方法引用，构造方法引用**的方式来表示。



### lambda表达式



其它大多数语言中lambda表达式的类型是函数，但在Java中，lambda表达式的类型是对象，他们必须依附于一类特别的对象类型——函数式接口（functional interface）。



通过lambda表达式创建函数式接口的引用。

```java
//外部迭代，通过外部迭代器的方式迭代
for(ele : list){
    ....
}
//内部迭代,不依靠外部迭代器
list.forEach(Consumer)
```



### 方法引用

通过方法引用创建函数式接口的引用。

```java
list.forEach(System.out:println());
```



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

