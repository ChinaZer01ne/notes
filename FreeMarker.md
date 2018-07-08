# FreeMarker

## 指令

```java
<#include "head.ftl"><br>	//包含指令
<#--我是一个注释，不会输出-->	//注释
<!--html注释-->
${name}, 你好，${message}	//取值

<#assign linkman="lion"><br>	//定义变量
author：${linkman}<br>		
<#if success= true><br>	//判断指令
    you can see ~
</#if>
<br>
-------商品列表-------<br>
<#list goodList as good> 	//遍历集合
    序号：good_index<br>
    商品名称：${good.name}<br>
    商品价格：${good.price}
</#list>
```

## 内建函数

```java
一共${goodList?size}条记录	//？代表调用函数
<#assign text="{'bank':'工商银行','account':'12341234123'}">
<#assign data=text?eval>	//将定义的json转化为json对象
开户行：${data.bank} 账号:${data.account}<br>

当前日期：${today?date}<br>	//时间的处理
当前时间：${today?time}<br>
当前日期+时间：${today?datetime}<br>
日期格式化：${today?string('yyyy年MM月')}<br>

当前积分：${point}<br>	//以逗号作为分隔符，如 193,123
当前积分：${point?c}<br>	//正常输出	如 193123
```

## 空值处理运算符

```java
//第一种方法
<#if aa??>
    aa变量存在 ${aa}
</#else>
    aa变量不存在
</#if>
//第二种方法
${bb!'bb没有被赋值'} //如果bb有值，原样输出，没有值则输出后面的值
```

## 比较运算符

```java
<#if point > 1 >	//会报错
...
</#if>
大于号用 gt，同理。。。
也可以
<#if （point > 1） >	这样写 
...
</#if>
```

