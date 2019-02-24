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



## 集群之主从复制

### master服务器配置

#### 第一步：修改my.cnf文件：

默认安装，my.cnf在`/etc/`下

在[mysqld]段下添加：

```shell
[mysqld]
#启用二进制日志（文件名类似mysql-bin000001.log）
log-bin=mysql-bin
#服务器唯一ID，一般取IP最后一段
server-id=63
```

#### 第二步：重启mysql服务

```shell
systemctl restart mysqld.service
```

#### 第三步：在主服务器上建立帐户并授权slave

```mysql
mysql>GRANT FILE ON *.* TO 'username'@'%' IDENTIFIED BY 'user-password';
mysql>GRANT REPLICATION SLAVE, REPLICATION CLIENT ON *.* to 'username'@'%' identified by 'user-password'; 
```

#一般不用root帐号，“%”表示所有客户端都可能连，只要帐号，密码正确，此处可用具体客户端IP代替，如192.168.145.226，加强安全。

刷新权限

```mysql
mysql> FLUSH PRIVILEGES;
```

查看mysql现在有哪些用户

```mysql
mysql>select user,host from mysql.user;
```

####  第四步：查询master的状态

```shell
show master status
```

### slave服务器配置

#### 第一步：修改my.cnf文件

在[mysqld]段下添加：

```shell
[mysqld]
#服务器唯一ID，一般取IP最后一段
server-id=64
```

####  第二步：删除UUID文件

错误处理：
如果出现此错误：

```shell
Fatal error: The slave I/O thread stops because master and slave have equal MySQL server UUIDs; these UUIDs must be different for replication to work.
```

因为是mysql是克隆的系统所以mysql的uuid是一样的，所以需要修改。

解决方法：
删除/var/lib/mysql/auto.cnf文件，重新启动服务。

#### 第三步：配置从服务器

```mysql
mysql>change master to master_host='192.168.1.63',master_port=3306,master_user='root',master_password='root',master_log_file='mysql-bin.000001',master_log_pos=120 
```

注意语句中间不要断开，master_port为mysql服务器端口号(无引号)，master_user为执行同步操作的数据库账户，“120”无单引号(此处的120就是show master status 中看到的position的值，这里的mysql-bin.000001就是file对应的值)。

> 注：
>
> **从服务器可以设置为为只读**
> 在从服务器上设置：
> read_only = ON 
>
> 或者
>
> ```mysql
> mysql> set global read_only=1; #1是只读，0是读写
> ```
>
> 注意:set global read_only=1 对拥有super权限的账号是不生效的，所以在授权账号的时候尽量避免添加super权限
>
> 然后
>
> ```mysql
> mysql>FLUSH TABLES WITH READ LOCK;
> ```



#### 第四步：启动从服务器复制功能

```mysql
mysql>start slave; 
```

####  第五步：检查从服务器复制功能状态：

```mysql
 mysql> show slave status
```

```te
……………………(省略部分)
Slave_IO_Running: Yes //此状态必须YES
Slave_SQL_Running: Yes //此状态必须YES
……………………(省略部分)
```

注：Slave_IO及Slave_SQL进程必须正常运行，即YES状态，否则都是错误的状态(如：其中一个NO均属错误)。

#### 注意：主从之间的防火墙需要配置

## 集群之读写分离

#### MySQL-Proxy下载

https://downloads.mysql.com/archives/proxy/

#### MySQL-Proxy安装

#### MySQL-Proxy配置

#### 创建mysql-proxy.cnf文件

#### 修改rw-splitting.lua脚本

#### MySQL-Proxy启动域测试

## 分库分表之MyCat实现



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