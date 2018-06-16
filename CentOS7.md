# CentOS7



## 命令

centOS7中service命令被systemctl替代

### 防火墙

1、查看已开放的端口(默认不开放任何端口)

` firewall-cmd --list-ports`

 2、开启80端口 

`firewall-cmd --zone=public(作用域) --add-port=80/tcp(端口和访问类型) --permanent(永久生效)`

 3、重启防火墙 

`firewall-cmd --reload`

 4、停止防火墙 

`systemctl stop firewalld.service`

 5、禁止防火墙开机启动 

`systemctl disable firewalld.service`

 6、删除 

`firewall-cmd --zone= public --remove-port=80/tcp --permanent `



## JDK的安装

1、查看是否已经安装JDK：rpm -qa | grep  -i java 

2、若有则删除：rpm -e --nodeps java-xxx，删除所有相关的java 

3、解压

4、配置`/etc/profile`文件，需要先`su root`

```
[root@lion software]# vim /etc/profile
export JAVA_HOME=/usr/local/java/jdk1.8.0_101
export JRE_HOME=${JAVA_HOME}/jre
export CLASSPATH=.:${JAVA_HOME}/lib:${JRE_HOME}/lib
export PATH=${JAVA_HOME}/bin:$PATH
```

5、使文件生效

```
[root@lion jdk1.8.0_171]# source /etc/profile
```

6、检测是否安装成功



## Tomcat的安装

1、下载

2、解压



## Zookeeper的安装

1、下载安装包

2、解压

3、进入安装目录

```
[lion@localhost soft]$ cd zookeeper-3.4.12/
```

4、创建data文件夹

```
[lion@localhost zookeeper-3.4.12]$ mkdir data
```

5、进入config目录,重命名zoo_sample.cfg文件

```
[lion@localhost conf]$ mv zoo_sample.cfg zoo.cfg
```

6.编辑zoo.cfg文件，指定安装目录位置

```
dataDir=/home/lion/soft/zookeeper-3.4.12
```

7、启动zookeeper，首先要进入安装目录的bin目录下

```
[lion@localhost bin]$ ./zkServer.sh start
```

8、查看状态

```
[lion@localhost bin]$ ./zkServer.sh status
```

9、停止

```
[lion@localhost bin]$ ./zkServer.sh stop
```



## Redis的安装

1、用lion账号登入虚拟机

2、wget 下载redis。发现没有wget命令，那么就 yum install wget

3、yum命令必须sudo，然后我的lion用户没有加入sudo组中

4、把lion加入sudo组里

5、安装wget，下载redis

6、解压

7、进入redis目录，make，报错，发现我机器上没有gcc

8、sudo yum install gcc

9、执行make后，Linux让我尝试执行make test

10、报错了，缺少tcl

11、sudo yum install tcl

12、make install PREFIX=指定目录



## Solr的安装



## dubbo-admin的部署

1、You can get a release of dubbo monitor in two steps:

- Step 1:

```
git clone https://github.com/apache/incubator-dubbo-ops
```

- Step 2:

```
cd incubator-dubbo-ops && mvn package
```

2、将dubbo-admin部署到tomcat中

3、访问



## Maven的安装

1、下载

 wget http://mirrors.shu.edu.cn/apache/maven/maven-3/3.5.3/binaries/apache-maven-3.5.3-bin.tar.gz

2、解压

3、在`/ect/profile`中配置

```
export MAVEN_HOME=/home/lion/soft/apache-maven-3.5.3
export PATH=$PATH:$MAVEN_HOME/bin
```

4、测试`mvn -version`

## Nexus的搭建

1、下载

2、解压，解压出两个文件，一个是程序，一个是数据

`nexus-3.12.1-01  sonatype-work`

3、运行

```
[lion@localhost bin]$ ./nexus start  
```



