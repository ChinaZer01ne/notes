# Mysql

## 安装与配置

### 安装

```shell
# 下载mysql源安装包
wget http://dev.mysql.com/get/mysql57-community-release-el7-8.noarch.rpm
# 安装mysql源
yum localinstall mysql57-community-release-el7-8.noarch.rpm
```

检查mysql源是否安装成功

```shell
yum repolist enabled | grep "mysql.*-community.*"
```

安装Mysql

```shell
yum install mysql-community-server
```

启动Mysql服务

```shell
systemctl start mysqld
```

查看MySQL的启动状态

```shell
systemctl status mysqld
```

### 开机启动

```shell
systemctl enable mysqld
systemctl daemon-reload
```

mysql安装完成之后，在/var/log/mysqld.log文件中给root生成了一个默认密码。通过下面的方式找到root默认密码，然后登录mysql进行修改：

```shell
# 首次安装必须先启动这个文件里才有密码
grep 'temporary password' /var/log/mysqld.log
```

### 修改初始密码

```shell
mysql> ALTER USER 'root'@'localhost' IDENTIFIED BY 'MyNewPass4!'; 
# 或者
mysql> set password for 'root'@'localhost'=password('MyNewPass4!');
```



mysql5.7默认安装了密码安全检查插件（validate_password），默认密码检查策略要求密码必须包含：大小写字母、数字和特殊符号，并且长度不能少于8位。否则会提示ERROR 1819 (HY000): Your password does not satisfy the current policy requirements错误。

### 修改密码策略

在/etc/my.cnf文件添加validate_password_policy配置，指定密码策略

```shell
# 选择0（LOW），1（MEDIUM），2（STRONG）其中一种，选择2需要提供密码字典文件
validate_password_policy=0
```

如果不需要密码策略，添加my.cnf文件中添加如下配置禁用即可：

```shell
validate_password = off
```

重新启动mysql服务使配置生效：

```shell
systemctl restart mysqld
```

### 添加远程登录用户

默认只允许root帐户在本地登录，如果要在其它机器上连接mysql，必须修改root允许远程连接，或者添加一个允许远程连接的帐户，为了安全起见，我添加一个新的帐户：

```shell
mysql> GRANT ALL PRIVILEGES ON *.* TO 'yangxin'@'%' IDENTIFIED BY 'Yangxin0917!' WITH GRANT OPTION;
```

### 配置默认编码为utf8

修改/etc/my.cnf配置文件，在[mysqld]下添加编码配置，如下所示：

```shell
[mysqld]
character_set_server=utf8
init_connect='SET NAMES utf8'
```









## 索引



## 性能优化



## InnoDB的锁机制



## 主从复制





## Mysql中遇到的坑

### order by， limit 排序分页数据重复问题

在`Mysql5.6以及5.7`版本（再往上的版本未测试）中，当我们进行排序并且分页的时候，或出现数据重复或丢失的问题。

原因：

> 在MySQL 5.6的以上的版本中，优化器在遇到order by limit语句的时候，做了一个优化，即使用了priority queue。使用 priority queue 的目的，就是在不能使用索引有序性的时候，如果要排序，并且使用了limit n，那么只需要在排序的过程中，保留n条记录即可，这样虽然不能解决所有记录都需要排序的开销，但是只需要 sort buffer 少量的内存就可以完成排序。之所以这些版本出现了第二页数据重复的问题，是因为 priority queue 使用了堆排序的排序方法，而堆排序是一个不稳定的排序方法，也就是相同的值可能排序出来的结果和读出来的数据顺序不一致。5.5 没有这个优化，所以也就不会出现这个问题。



---



解决办法：

>1、 加上索引排序
>​         `select * from table order by xx,id（任意有索引的字段） limit 0,10`
>
>2、给xx字段加上索引
>​         作为验证，您可以在这个字段上加索引 
>
>​	 `alter table tea_course_sort add index(course_sort_order)`
>
>​	然后由于这个表数目太小，以防加索引都未必能用得上，语句修改为
>​       `select * from tea_course_sort  force index(course_sort_order) order by tea_course_sort.course_sort_order desc  limit 0,10;`


​         