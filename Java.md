# Java



### 数组

对于数组下标`index`，对应到数组长度中就是`index+1`，在计算的时候要下标和下标运算，长度和长度运算



## 异常体系



### 异常的分类



父类`Throwable`，主要的两个子类`Error`和`Exception`。

`Error`程序员并不需要关心，而是由JVM负责。

`Exception`包含两种：**checked异常**和**unchecked异常**。



**继承自Exception，就是一个CheckedException**

**继承自RuntimeException，就是一个UnCheckedException**



顾名思义：

**checked异常**就是编译器会检查的异常，这就代表每次遇到的时候，必须向上抛出异常或者`try-catch`

**unchecked异常**就是编译不会检查的异常，使用的时候，不需要`try-catch`或者抛出，但一旦出现异常，程序就会停止，当然你也可以选择捕获或抛出。

Joshua Bloch（Effective Java，条目41：**避免checked异常的不必要的使用**）建议使用**unchecked异常**。至少在一个工程中尝试过。我总结了以下原因：

- Unchecked异常不会使代码显得杂乱，因为其避免了不必要的try-catch块。
- Unchecked异常不会因为异常声明聚集使方法声明显得杂乱。
- 关于容易忘记处理unchecked异常的观点在我的实践中没有发生。
- 关于无法获知如何处理未声明异常的观点在我的实践中没有发生。
- Unchecked异常避免了版本问题。

```java
public class ExceptionTest {


    public static void main(String[] args) {

        //UnCheckedException，不需要try-catch或者抛出，但一旦出现异常，程序就会停止
        testUnCheckedException();
		//当然你也可以选择捕获或抛出，让程序继续运行。
        /**
        try {
            testUnCheckedException();
        } catch (Exception e) {
            e.printStackTrace();
        }
        */
            
        //CheckedException必须try-catch或者抛出
        try {
            testCheckedException();
        } catch (CheckedException e) {
            e.printStackTrace();
        }

    }
    /**
     * 对于一个CheckedException，要么向上抛出，要么捕获
     */
    public static void testCheckedException() throws CheckedException {
        throw new CheckedException();
    }
    /**
     * 对于一个UnCheckedException，不需要处理
     */
    public static void testUnCheckedException() {
        throw new UnCheckedException();
    }
}

/**
 * 继承自Exception，就是一个CheckedException
 */
class CheckedException extends Exception{
    public CheckedException() {
        super();
    }

    public CheckedException(String message) {
        super(message);
    }
}
/**
 * 继承自RuntimeException，就是一个UnCheckedException
 */
class UnCheckedException extends RuntimeException{
    public UnCheckedException() {
        super();
    }

    public UnCheckedException(String message) {
        super(message);
    }
}

```



### 常见的异常



**RuntimeException**

1. NullPointerException
2. ClassCastException
3. IllegalArgumentException：非法参数异常
4. IndexOutOfBoundsException
5. NumberFormatException：数组格式异常



**非RuntimeException**

1. ClassNotFoundException
2. IOException



**Error**

1. **NoClassDefException**
   1. 类依赖的class或者jar不存在
   2. 类文件存在，但是存在不同的域中
   3. 大小写问题，javac编译的时候是无视大小写的，很可能编译出来的class文件就与想要的不一样
2. StackOverflowError
3. OutOfMemoryError



### 性能



* try-catch块影响JVM的优化
* 异常对象实例需要保存栈快照等信息，开销较大
  * 仅捕获可能出现异常的必要的代码段，不要用大的try去包裹代码块， 不要用用异常控制代码流程，因为没有if，switch效率高





## 集合



**HashMap**


put方法：

1. 如果HashMap未被初始化过，则初始化
2. 对Key求Hash值，然后再计算下标
3. 如果没有碰撞，直接放入桶中
4. 如果碰撞了，以链表的方式链接到后面
5. 如果链表长度超过阈值，就把链表转换成红黑树
6. 如果链表长度低于6,就把红黑树转回链表
7. 如果节点已经存在就替换旧值
8. 如果桶满了（容量16 * 负载因子0.75），就需要resize（扩容两倍后rehash）



在HashMap中，**哈希桶数组table的长度length大小必须为2的n次方**(一定是合数)，这是一种非常规的设计，常规的设计是把桶的大小设计为素数。相对来说素数导致冲突的概率要小于合数[2].

HashMap采用这种非常规设计，主要是**为了在取模和扩容时做优化**，同时为了减少冲突，HashMap定位哈希桶索引位置时，也加入了高位参与运算的过程。

只有传入的元素是可比较的，才会转红黑树，因为红黑树需要元素是比较的，这也是对存入元素必须重写equal，hashcode的原因吧。





**ConcurrentHashMap**

put方法：

1. 判断Node[]数组是否初始化，没有则进行初始化操作
2. 通过hash定位数组的索引坐标，是否由Node节点，如果没有则使用CAS进行添加（链表的头节点），添加失败则进入下次循环
3. 检查到内部正在扩容，就帮助它一块扩容
4. 如果f!= null,则使用synchronized锁住f元素（链表/红黑树的头元素）
   1. 如果是Node（链表结构）则执行链表的添加操作
   2. 如果时TreeNode（树型结构），则执行树的添加操作
   3. 判断链表的长度已经达到临界值8,当然这个8时默认值，大家也可以去做调整，当节点数超过这个值就需要把链表转换为树结构



总结：

* 首先使用无锁操作CAS插入头节点，失败则循环重试
* 若头节点已存在，则尝试获取头节点的同步锁，在进行操作