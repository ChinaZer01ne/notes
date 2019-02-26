# ZooKeeper

## ZooKeeper介绍



## ZooKeeper集群搭建

 ZooKeeper集群最少需要三台Zookeeper

```te
192.168.1.63
192.168.1.64
192.168.1.65
```

### 第一步：安装jdk

ZooKeeper是使用java开发的

### 第二步：下载解压缩

解压缩ZooKeeper安装包，并改名为zk

```shell
wget http://mirror.bit.edu.cn/apache/zookeeper/stable/zookeeper-3.4.12.tar.gz
tar zxvf zookeeper-3.4.12
mv zookeeper-3.4.12 zk
```

### 第三步：创建zoo.cfg

进入`zk/conf`目录，将`zoo_sample.cfg`改为zoo.cfg

```shell
cd zk/conf
mv zoo_sample.cfg zoo.cfg
```

### 第四步：修改zoo.cfg

```shell
vim zoo.cfg
```

添加以下代码

```shell
# 表示客户端所连接的服务器所监听的端口号，默认是2181。即zookeeper对外提供访问的端口号。
clientPort=2181
# 存储快照文件snapshot的目录。默认情况下，事务日志也会存储在这里。建议同时配置参数dataLogDir, 事务日志的写性能直接影响zk性能。
dataDir=/home/soft/zookeeper/zk/data
dataLogDir=/usr/local/cndmss/zk/dataLog
# 集群中每台机器都是以下配置
# server.X，这个X值即为集群机器中myid文件中的值；后面的IP即为集群机器的IP
# 2881系列端口是zookeeper之间的通信端口
# 3881系列端口是zookeeper投票选举端口
server.1=192.168.1.63:2881:3881
server.2=192.168.1.64:2881:3881
server.3=192.168.1.65:2881:3881
```

> 注：其他属性说明
>
> tickTime=2000： ZK中的一个时间单元，ZK中所有时间都是以这个时间单元为基础。
> initLimit=10：对于从节点最初连接到主节点时的超时时间，单位为tick值的倍数。
> syncLimit=5 ：对于主节点与从节点进行同步操作时的超时时间，单位为tick值的倍数。
>
> 详情参数参照：http://www.aboutyun.com/forum.php?mod=viewthread&tid=13909

### 第五步：创建myid文件

在dataDir指向的文件下创建myid文件，文件内容为X，对应server.X=X中的X（文件内容为1，对应server.1=1中的1）

```shell
vi /home/soft/zookeeper/zk/data/myid
```

添加以下内容（其他的zookeeper配置`对应的id值`，这里是`2`和`3`）：

```tex
1
```

至此，配置就算基本完成了，**所有Zookeeper集群中的节点机器，除了`myid`文件中的值不一样外，其它配置均一样**。

### 第六步：启动zookeeper服务

```shell
./zk/bin/zkServer.sh start
```

### 第七步：查看zookeeper状态

```shell
./zk/bin/zkServer.sh status
```

