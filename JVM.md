# JVM

## 类加载器

### JDK1.8

JDK1.8 默认提供了如下几种ClassLoader

**1、Bootstrp loader**
`Bootstrp loader`是用C++语言写的，它是在Java虚拟机启动后初始化的，它**主要负责加载%JAVA_HOME%/jre/lib**，`-Xbootclasspath`参数指定的路径以及%JAVA_HOME%/jre/classes中的类。

**2、ExtClassLoader**  

`Bootstrp loader`加载``ExtClassLoader`,并且将`ExtClassLoader`的父加载器设置为`Bootstrp loader.ExtClassLoader`是用Java写的，具体来说就是 `sun.misc.Launcher$ExtClassLoader`，`ExtClassLoader`**主要加载%JAVA_HOME%/jre/lib/ext，此路径下的所有classes目录以及java.ext.dirs系统变量指定的路径中类库**。

**3、AppClassLoader** 
`Bootstrp loader`加载完`ExtClassLoader`后，就会加载`AppClassLoader`,并且将AppClassLoader的父加载器指定为 ExtClassLoader。AppClassLoader也是用Java写成的，它的实现类是 `sun.misc.Launcher$AppClassLoader`，另外我们知道ClassLoader中有个getSystemClassLoader方法,此方法返回的正是AppclassLoader.`AppClassLoader`主要负责**加载classpath所指定的位置的类或者是jar文档**，它也是Java程序默认的类加载器。

4、**CustomClassLoader**

除了系统提供的类加载器以外，开发人员可以通过继承 java.lang.ClassLoader类的方式实现自己的类加载器，以满足一些特殊的需求。

![img](images\JVM\jdk1.8前类加载器.png)

### JDK1.9+

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



## JVM内存模型