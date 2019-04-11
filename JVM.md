# JVM

## JVM与程序的生命周期

在如下几种情况下，Java虚拟机将结束生命周期

* 执行了System.exit()方法

* 程序正常执行结束

* 程序执行过程中遇到了异常或错误而异常终止

* 由于操作系统出现错误而导致Java虚拟机进程终止


## 助记符

- `ldc`：表示将int，float或是String类型的常量值从常量池中推送到栈顶。（表示接下来要使用）。相关类`com.sun.org.apache.bcel.internal.generic.LDC`
- `iconst_`：对于`-1到5之间`，java虚拟机用（`iconst_m1`，`iconst_0`，`iconst_1`，`iconst_2`，`iconst_3`，`iconst_4`，`iconst_5`）来表示。表示将（`-1到5`）的常量值推送至栈顶。相关类`com.sun.org.apache.bcel.internal.generic.ICONST`

- `bipush`：表示将单字节（`-128到127`）的常量值推送至栈顶。相关类`com.sun.org.apache.bcel.internal.generic.BIPUSH`

- `sipush`：表示将短整形（`-32768到32767`）的常量值推送至栈顶。相关类`com.sun.org.apache.bcel.internal.generic.SIPUSH`

  > 对于数字的优先级：const_、bipush、sipush

* `newarray`：表示创建一个指定的原始类型（如：int、float、char等）的数组，并将其引用值压入栈顶。相关类`com.sun.org.apache.bcel.internal.generic.NEWARRAY`
* `anewarray`：表示创建一个引用类型的（如类、接口、数组）数组，并将其压入栈顶。相关类`com.sun.org.apache.bcel.internal.generic.ANEWARRAY`

## 类加载



### 类的加载

- 类加载器并不需要等到某个类“首次主动使用”时，再加载他。

- JVM规范允许类加载器在预料某个类将要被使用时就预先加载它，如果在预先加载的过程中遇到了`.class`文件缺失或存在错误，类加载器必须在程序首次主动使用该类时才报告错误（LinkageError错误）

- 如果这个类一直没有被程序主动使用，那么类加载器就不会报告错误



### 类加载过程

在Java代码中，类型的**加载、连接与初始化**过程都是在程序运行期间完成的。



- 加载
- 连接
  - 验证
  - 准备
  - 解析
- 初始化



#### 加载

​	查找并加载类的二进制数据。

​	根据虚拟机规范，在加载阶段，虚拟机需要完成下面三件事：

​	**1）通过一个类的全限定名，来获取类的二进制流。**

​	**2）将这个字节流代表的静态存储结构转化为方法区的运行时数据结构。**

​	**3）在内存中生成代表这个类的Class对象，作为方法区这个类各种数据访问的入口。**

>因为没有规定Class类读取的来源，所以出现了从jar读取，从网络读取，有其他文件生成，数据库读取等等方式。



#### 连接

​	加载和连接阶段是交叉进行的，如一部分字节码文件格式的验证动作，加载还没完成，验证阶段或许已经开始了。

​	连接过程又分为三个阶段：**验证、准备、解析**。

##### 验证

​	确保被加载类的正确性并且不会危害虚拟机自身安全。

​	验证阶段大致会完成下面4个阶段的检验动作。**文件格式验证、元数据验证、字节码验证、符号引用验证**。



###### 文件格式验证

* 是否以魔数0xCAFEBABE开头

* 主、次版本号是否在当前虚拟机处理范围内

* Class文件中各个部分及文件本身是否有被删除的或附加的其他信息

* 等等。。。

  **第一阶段的主要目的是保证输入的字节流能正确的解析并存储于方法区之内，格式上符合描述一个Java类型信息的要求。该阶段的验证是基于二进制字节流进行的，只有通过了这个阶段验证后，字节流才会进入内存的方法区中进行存储**

  也就是说，在加载的阶段连接阶段已经开始了，说明存在交叉。

###### 元数据验证

* 这个类是否有父类（除了Object，都存在父类）

* 这个类的父类是否继承了不允许被继承的类（final修饰的类）

* 如果这个类不是抽象类，是否实现了其父类或接口之中要求实现的所有方法。

* 等等。。。

  **第二阶段的主要目的是对类的元数据信息进行语义校验，保证不存在不符合Java语言规范的元数据信息。**

###### 字节码验证

* 保证任意时刻操作数栈的数据类型于指令代码序列都能配合工作，例如不会出现类似这样的情况：在操作栈放置了一个int类型的数据，使用时却按long类型来加载入本地变量表中。

* 保证跳转指令不会跳转到方法体以外的字节码指令上。

* 保证方法体中的类型转换是有效的。

* 等等。。。

  **第三阶段验证的主要目的是通过对数据流和控制流分析，确定程序语义是合法的、符合逻辑的。**

###### 符号引用验证

​	第四阶段的校验发生在虚拟机将符号引用转化为直接引用的时候，这个转化动作将在连接的第三阶段——解析阶段中发生。符号引用验证可以看作是常量池中各种符号引用信息的匹配性校验。

* 符号引用中通过字符串描述的全限定类名是否能找到对应的类。
* 在指定类中是否存在符合方法的字段描述符以及简单名称所描述的方法和字段。
* 符号引用中的类、字段、方法的访问性（private、protected、public、default）是否可被当前类访问。

**符号引用验证的目的是确保解析动作能正常执行。对于虚拟机的类加载机制来说，验证阶段是一个非常重要的，但不是一定必要（因为对程序运行期没有影响）的简短。如果所运行的全部代码（包括自己编写的及第三方包中的代码）都已经被反复使用和验证过，那么在实施阶段就可以考虑使用`-Xverify:none`参数来关闭大部分的类验证措施，以缩短虚拟机类加载的时间。**

##### 准备

​	为类的**静态变量**分配内存，并将其初始化为**默认值**的阶段。这些变量所使用的内存都将在方法区中进行分配。

##### 解析

​	把类中的符号引用转换为直接引用。

#### 初始化

​	为类的静态变量赋予正确的初始值。

> - 加入这个类还没有被加载和连接，那么先进行加载和连接
> - 加入类存在直接父类，并且这个父类还没有被初始化，那么先初始化直接父类
> - 假如类中存在初始化语句，那就依次执行这些初始化语句





### 类加载器



#### JDK1.8

JDK1.8 默认提供了如下几种ClassLoader

**1、Bootstrp loader**
`Bootstrp loader`是用C++语言写的，它是在Java虚拟机启动后初始化的，它**主要负责加载%JAVA_HOME%/jre/lib**，`-Xbootclasspath`参数指定的路径以及%JAVA_HOME%/jre/classes中的类。

**2、ExtClassLoader**  

`Bootstrp loader`加载``ExtClassLoader`,并且将`ExtClassLoader`的父加载器设置为`Bootstrp loader.ExtClassLoader`是用Java写的，具体来说就是 `sun.misc.Launcher$ExtClassLoader`，`ExtClassLoader`**主要加载%JAVA_HOME%/jre/lib/ext，此路径下的所有classes目录以及java.ext.dirs系统变量指定的路径中类库**。

**3、AppClassLoader** 
`Bootstrp loader`加载完`ExtClassLoader`后，就会加载`AppClassLoader`,并且将AppClassLoader的父加载器指定为 ExtClassLoader。AppClassLoader也是用Java写成的，它的实现类是 `sun.misc.Launcher$AppClassLoader`，另外我们知道ClassLoader中有个getSystemClassLoader方法,此方法返回的正是AppclassLoader.`AppClassLoader`主要负责**加载classpath所指定的位置的类或者是jar文档**，它也是Java程序默认的类加载器。

**4、CustomClassLoader**

除了系统提供的类加载器以外，开发人员可以通过继承 java.lang.ClassLoader类的方式实现自己的类加载器，以满足一些特殊的需求。



##### 双亲委派机制

![img](images\JVM\jdk1.8前类加载器.png)



#### JDK1.9+

TODO 平台加载器替代了扩展加载器

```java
class Member{

}

public class JVMClassloader {
    public static void main(String[] args) {

        //String类是个系统类，系统类的类加载器是不同的
        //静态常量池定义
        String str = "Hello";
        Member member = new Member();
        System.out.println(str.getClass().getClassLoader());
        System.out.println(member.getClass().getClassLoader());
        System.out.println(member.getClass().getClassLoader().getParent());        System.out.println(member.getClass().getClassLoader().getParent().getParent());
    }
}

```

打印：

```java
null
jdk.internal.loader.ClassLoaders$AppClassLoader@14514713
jdk.internal.loader.ClassLoaders$PlatformClassLoader@4e04a765
null
```



#### 类加载器的命名空间



**问题：一个类只会被加载一次？**

​	这牵扯到一个类加载器的命名空间的问题。

 * 每个类加载器都有自己的命名空间，命名空间由该加载器及所有父加载器所加载的类组成。
 * 在同一个命名空间中，不会出现类的完整名字（包括类的包名）相同的两个类。意思就是不会出现两个相同的类。
 * 在不同的命名空间中，有可能会出现类的完整名字（包括类的包名）相同的两个类。意思是可能出现多个相同的类。



​	当类加载器之间存在层级关系后，他们的命名空间就成了一个。（子类加载器包含父类加载器，他们是委派的关系）



**命名空间产生的一些问题：**



​	**如果`A类`引用了`B`类，`A类`是由系统加载器加载的，`B类`是由自定义加载器加载的，那么在运行时就会报错，因为系统类加载器的命名空间中不自定义加载器命名空间中的类（`B类`）；或者`A类`和`B类`是由两个不同的自定义加载器分别加载的，也会产生命名空间问题。**

​	**子加载器所加载的类能够访问到父加载器加载的类，但父加载器加载的类无法访问到子加载器所加载的类。**



#### ClassLoader获取方法

```java
// 获取当前类的ClassLoader
clazz.getClassLoader();
//获取当前线程上下文的ClassLoader，一般是应用类加载器
Thread.currentThread().getContextClassLoader();
//获取系统的ClassLoader
ClassLoader.getSystemClassLoader();
//获取调用者的ClassLoader
DriverMannager.getCallerClassLoader();
```





#### 关于类加载器

​	类加载器会去定位（读取已有的）或生成（运行期生成的）一些类的数据。每个类都包含一个定义它的类加载器的引用。

​	关于数组类`getClassLoader`返回的加载器，是和它元素的类加载器相同，如果该数组是原始类型（int等），则没有类加载器。注意String和int数组的类加载器都是`null`，但含义不一样，前者代表根类加载器，后者表示没有类加载器。当然数组类不是类加载器加载的，而是虚拟机运行期动态生成的。

​	类加载器除了加载类，还可以用来定位资源。

​	关于自定义类加载器，需要重写`ClassLoader#findClass`方法，并且提供自己的`loadClassData`加载类的方法（可能从某一位置读取返回一个字节数组）。

​	**如果`A类`中引用了`B类`，`B类`一般会由加载了`A类`的类加载器进行加载（会遵循双亲委派机制）。比如`A类`由自定义类加载器加载，`B类`的加载，会遵循双亲委派机制，自顶向下进行尝试加载，依次是根类加载器、扩展类加载器、系统加载器、自定义加载器；如果`A类`是由系统加载器加载的，那么`B类`的加载会自顶向下尝试，依次是根类加载器、扩展类加载器、系统加载器。**

​	

### 类加载过程的其他问题



#### 什么时候触发初始化过程？

首次主动使用类的时候。

主动使用：

- 创建类的实例

- 对类或接口的静态成员的使用（getstatic、putstatic）

- 对静态方法的调用（invokestatic)

- 反射（reflect）

- 初始化一个类的子类，会对父类主动使用（不适用于接口）

  > * 初始化一个类的时候，并不会先初始化它实现的接口
  > * 初始化一个接口的时候，并不会先初始化它的父接口
  >
  > 对于一个接口常量的调用，并不会要求其父接口的初始化。只有真正使用到父接口（常量或方法）时，父接口才会被初始化

- Java虚拟机启动时被标记为启动类的类（main方法）

- JDK1.7开始提供动态语言支持，java.lang.invoke.MethodHandler实例的解析结果REF_getStatic，REF_putStatic，REF_invokeStatic句柄对应的类如果没有初始化，则初始化。

  其他情况都为被动使用，被动使用**不会触发初始化过程**（有可能触发加载连接过程）。



  **注意：**

  ​	引用数组类型不会导致类的初始化，引用数组类型是运行期间jvm动态生成的。不属于主动使用。

  ​	调用`ClassLoader`类的`loadClass`方法加载一个类，并不是对类的主动使用，不会导致类的初始化。



#### 编译器常量和运行期常量的区别

​	1、当我们调用一个类的静态常量的时候，是不会触发类的的初始化的，在编译阶段，常量就已经放到调用方所在类的常量池中了。就算编译完成，把常量所在类的字节码删除也不影响运行结果。

```java
//这是一个编译期常量
public static final String str = "hello";
```

> 在字节码中，助记符是ldc之类的。

注意：如果常量是类似

```java
//这不是一个编译期常量
public static final String str = UUID.randomUUID().toString();
```

> 在字节码中，助记符是getstatic之类的。

因为随机数函数是编译期间未知的结果，所以在编译期间，这个常量是无法放入调用方所在类的常量池中的，在运行期间，会导致主动使用常量所在的类，所以会触发类的初始化。

> 我们可以通过助记符来看jvm时候对类进行了初始化。



####准备和初始化阶段的理解

1、下面程序的输出是什么？

```java
public class MyTest5 {
    public static void main(String[] args) {
        //主动使用触发类的初始化
        Singleton instance = Singleton.getInstance();
        System.out.println("counter1 : " + Singleton.counter1);
        System.out.println("counter2 : " + Singleton.counter2);
    }
}

class Singleton{
    //准备阶段赋值默认0，初始化阶段不变
    public static int counter1;
    //调用构造方法
    private static Singleton singleton = new Singleton();
    private Singleton(){
        //初始化阶段不变0，变成1
        counter1++;
        //准备阶段赋值默认0，变成1
        counter2++;
    }
    //初始化的时候又赋值成了0
    public static int counter2 = 0;

    public static Singleton getInstance(){
        return singleton;
    }
}
```

控制台打印：

```shell
counter1 : 1
counter2 : 0	
```



## 类的卸载

​	当一个类被加载、连接和初始化后，它的生命周期就开始了，当代表类的Class对象不再被引用，Class对象就会结束生命周期，类在方法区内的数据也会被卸载。从而结束类的生命周期。一个类何时结束生命周期，取决于代表它的Class对象何时结束生命周期。

​	由Java虚拟机自带的类加载器（类加载器，扩展加载器，系统加载器）所加载的类，在虚拟机的生命周期中，始终不会被卸载。因为虚拟机会一直引用这些加载器，这些加载器会一直引用他们加载的类。

​	**由用户自定义类加载器加载的类是可以被卸载的。可以通过`jvisualvm`工具来查看类的卸载情况**



## 类的实例化

* 为新的对象分配内存

* 为实例变量赋默认值

* 为实例变量赋正确的初始值

  > java编译器为它编译的每一个类都至少生成一个实例初始化方法，在java的class文件中，这个实例初始化方法被称为`<init>(表示对实例变量的初始化)`。针对源代码中每一个类的构造方法，java编译器都产生一个`<init>方法，类似对类的``<clinit>(表示对静态变量的初始化)`


## JVM内存模型





## JVM常用命令

```shell
-XX:+TraceClassLoading，用于追踪类的加载信息并打印出来
* -XX:-<option> ： 关闭option选项
* -XX:+<option> ： 开启option选项
* -XX:<option>=<value>，表示将option赋值
```



```shell
# 用于追踪类的加载信息并打印出来
-XX:+TraceClassLoading
# 用于追踪类的卸载情况
# deprecated
-XX:+TraceClassUnloading
-Xlog:class+unload=info
```



## 注意

