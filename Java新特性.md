# Java新特性



## Java 8

### 函数式接口

1、如果一个接口只有一个抽象方法，那么该接口就是一个函数式接口。

2、如果我们在某接口上声明了FunctionalInterfae注解，那么编译器就会按照函数式接口的定义来要求该接口。

3、如果某个接口只有一个抽象方法，但我们斌没有给该接口声明FunctionalInterface注解，那么编译器依旧会将该接口看作是函数式接口。

对于一个函数式接口加不加`@FunctionalInterface`注解，程序都可以正常运行，但我们最好加上，好比`@override`，类似是一种规范吧。

**注意：函数式接口只有一个抽象接口，但可以复写`Object`对象的方法。因为接口本身就继承自`Object`对象。**



函数式接口的实例我们可以通过**lambda表达式，方法引用，构造方法引用**的方式来表示。



#### 常用接口

**java.util.function.Function**

实现一个参数一个返回结果

```java
//一个参数一个返回结果
java.util.function.Function
//abstract method
R apply(T t);
//default method
//将function组合起来，先应用before function，然后应用当前function
Function<V, R> compose(Function<? super V, ? extends T> before){}
//将function组合起来，先应用当前function，然后应用after function
Function<T, V> andThen(Function<? super R, ? extends V> after){}
//static method
//返回当前参数
Function<T, T> identity(){}
```

**java.util.function.BiFunction**

实现两个参数产生一个结果的操作

```java
//实现两个参数产生一个结果的操作
java.util.function.BiFunction
//接收两个参数
R apply(T t, U u);
//先应用当前BiFunction，然后应用after function，因为BiFunction是接收两个参数的，所以没有类似于java.util.function.Function.compose()的方法
BiFunction<T, U, V> andThen(Function<? super R, ? extends V> after){}
```

**java.util.function.Predicate**

表示一个断言

```java
//表示一个断言
java.util.function.Predicate
//抽象方法
//根据给定的条件判断返回true或false
boolean test(T t);
//默认方法
//并且条件
Predicate<T> and(Predicate<? super T> other){}
//取反
Predicate<T> negate(){}
//或者条件
Predicate<T> or(Predicate<? super T> other){}
//静态方法
Predicate<T> isEqual(Object targetRef){}
Predicate<T> not(Predicate<? super T> target){}
```

**java.util.function.Supplier**

```java
java.util.function.Supplier
//不接受参数返回一个结果
T get();
```

**java.util.function.BinaryOperator**

一个`BiFunction`的特例，`extends BiFunction<T,T,T>`

```java
//静态方法
//根据比较器返回较小的
BinaryOperator<T> minBy(Comparator<? super T> comparator){}
//根据比较器返回较大的
BinaryOperator<T> maxBy(Comparator<? super T> comparator){}
```



> 其他函数式接口，请参照`package java.util.function`包下。





### Optional

一个基于值的对象。可以用来规避`NullPointException`

This is a value-based class.

日常我们可以解决一些NPE的判断。

```java
Optional<String> optional = Optional.of("hello");

//不推荐使用
if (optional.isPresent()){
    System.out.println(optional.get());
}

//推荐使用
optional.ifPresent(str -> System.out.println(str));
```

**常用方法**

```java
//java.util.Optional
//常用方法
void ifPresent(Consumer<? super T> action){}
void ifPresentOrElse(Consumer<? super T> action, Runnable emptyAction){}
T orElse(T other)
T orElseGet(Supplier<? extends T> supplier)
.....
```



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

​	方法引用实际上是lambda表达式的语法糖。如果lambda的方法体恰好有一个方法客观存在，就可以是用方法引用代替。可以通过方法引用创建函数式接口的引用。

​	我们可以将方法引用看作是一个**函数指针**，function pointer。



**方法引用分为4类表现形式：**

* 类名 :: 静态方法名    **classname::staticmethod**

  >```java
  >//students.sort(Student::compareStudent);
  >public static int compareStudent(Student student1, Student student2){
  >    return student1.getScore() - student2.getScore();
  >}
  >```

* 引用名（对象名）:: 实例方法名    **objectname :: initialmethod**

  > ```java
  > //StudentComparator comparator = new StudentComparator();     //students.sort(comparator::compareStudentByScore);
  > public class StudentComparator {
  >     public int compareStudentByScore(Student student1,Student student2){
  >         return student1.getScore() - student2.getScore();
  >     }
  > ```

* 类名 :: 实例方法名     **classname::initialmethod**

  > ```java
  > //students.sort(Student::compareStudent);
  > //只有一个参数，方法调用者是lambda表达式的第一个参数。
  > public int compareStudent(Student student){
  >     return student.getScore() - this.getScore();
  > }
  > ```

* 构造方法引用，类名 :: new    classname::new

  >```java
  >//getStudent(Student::new);
  >public Student getStudent(Supplier<Student> supplier){
  >    return supplier.get();
  >}
  >```



### 接口的默认方法

​	对于多实现接口中的同名的默认方法，需要在子类中重写继承的方法。如果想要调用父接口的实现，需要`MyInterface.super.method();`的形式。

​	如果是继承的类和实现的接口中有同名方法，类中的会比接口的default方法优先级高。



### Stream



* 流不存储值，通过管道的方式获取值。

* 本质是函数式的，对流的操作会产生一个结果，不过并不会修改底层的数据源，集合可以作为流的底层数据源。

* 延迟查找，很多流操作（过滤、映射、排序等）都可以延迟实现。


####构成

* 源
* 零个或多个中间操作
* 终止操作



####分类

* 惰性求职

* 及早求值



#### 创建

```tex
Stream.of()
Arrays.stream()
list.stream()
```



#### 常用方法



```java
/**
 * 这是一个中间操作
 * 过滤出符合断言表达式的元素，返回一个Stream.
 */
Stream<T> filter(Predicate<? super T> predicate);
/**
 * 返回一个流，包含了经过function处理后的的元素
 * 这是一个中间操作
 */
Stream<R> map(Function<? super T, ? extends R> mapper);
/**
 * 返回一个IntStream，包含了经过function处理后的的元素
 * 这是一个中间操作
 */
IntStream mapToInt(ToIntFunction<? super T> mapper);
//返回一个LongStream
LongStream mapToLong(ToLongFunction<? super T> mapper);
//返回一个DoubleStream
DoubleStream mapToDouble(ToDoubleFunction<? super T> mapper);
/**
 * 负责通过function处理后生成stream，把所有stream聚合起来放到一个stream中
 */
Stream<R> flatMap(Function<? super T, ? extends Stream<? extends R>> mapper);
/**
 * 筛选，通过流所所生成元素的hashCode()和equals()去除重复元素
 */
Stream<T> distinct();
/**
 * 排序
 */
Stream<T> sorted();	//根据Comparable比较
Stream<T> sorted(Comparator<? super T> comparator);
/**
 * 截断流，使其元素不超过给定数量
 */
Stream<T> limit(long maxSize);
/**
 * 对应limit方法，会跳过给定个数的元素
 */
Stream<T> skip(long n);
/**
 * 生成一个包含原Stream的所有元素的新Stream，同时会提供一个消费函数（Consumer实 例），新Stream每个元素被消费的时候都会执行给定的消费函数
 */
Stream<T> peek(Consumer<? super T> action);
/**
 * 返回有顺序的流，无限流，使用需要注意死循环的问题，一般配合limit()方法
 */
Stream<T> iterate(T seed, Predicate<? super T> hasNext, UnaryOperator<T> next){}
```



#### Collector类

​	这是流操作中及其重要的类，一般配合`stream#collect`使用。

​	他是一个可变的汇聚操作，将输入元素累积到一个可变的结果容器中；在所有输入元素处理完毕后，他会将累积的结果转换为一个最终的表示（可选操作），它支持串行和并行。

* creation of a new result container (supplier())
* incorporating a new data element into a result container (accumulator())
* combining two result containers into one (combiner())
* performing an optional final transform on the container (finisher())

​	

```java
/**
 A function that creates and returns a new mutable result container.
*/
Supplier<A> supplier();
/**
A function that folds a value into a mutable result container.
*/
BiConsumer<A, T> accumulator();

/**
A function that accepts two partial results and merges them.  
The combiner function may fold state from one argument into the other and return that, or may return a new result container.
接收两个集合，可能是将两个集合折叠成一个集合（将B集合内容全部放到A集合）可能是将两个集合合并成一个新的集合（将AB两个集合合并新集合C集合）
 */
BinaryOperator<A> combiner();
/**
Perform the final transformation from the intermediate accumulation type
*/
Function<A, R> finisher();
```



> **A sequential implementation** of a reduction using a collector would **create a single result container using the supplier function**, **and invoke the accumulator** function once for each input element. **A parallel implementation** would partition the input, **create a result container for each partition**, **accumulate the contents** of each partition into a subresult for that partition, **and then use the combiner function** to merge the subresults into a combined result.
>
> ​												——java.util.stream.Collector



​	  一旦指定了`Characteristics.CONCURRENT`属性（执行了多次`accumulator`方法），就算是一个并行流，也只会存在一个`supplier`的集合对象。如果没有指定，串行流只存在一个`supplier`集合对象，而并行流会存在多个`supplier`集合对象。

​	一个串行流的collect是不会调用`Collector#combiner`方法中返回的`BinaryOperator函数接口`的。 在并行流中，没有`Characteristics.CONCURRENT`属性，才会调用`Collector#combiner`方法。因为此时的串行流只存在一个`supplier`的集合对象。

​      



并行和串行需要满足同一性（identity）和结合性（associativity），[详情请参照](java.util.stream.Collectors)



#### Collectors类

​	jdk8提供了对集合操作的类Collectors。它提供了关于Collector的可变的汇聚操作，比如分组分区的功能。一般返回一个Collector对象。是通过内部的CollectorImpl实现的。

[详情请看](java.util.stream.Collectors)



#### 其他

1、所有中间操作都会返回一个新的Stream对象。

2、流是不可以重复使用的。

3、流不会对侵入原数据，不会修改源中的数据。流更关注的是对数据的操作。

4、在多个stream操作中，会依次把元素应用到所有给定操作上。如果遇到一个短路终端操作，遇到满足条件的元素就终止掉了，不会继续往下执行。



#### 资料



https://blog.csdn.net/ycj_xiyang/article/details/83624642



### 日期API

```java
LocalDate
LocalTime
Clock
ZoneId
YearMonth
Period
```



## Java 11

