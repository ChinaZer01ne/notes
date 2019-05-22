# Docker



## 安装

```shell
 yum install -y epel-release
 yum install docker-io # 安装docker
 systemctl start docker # 启动
 docker version
```



## 概念（concept）

* Container
  * Docker Container即Docker将宿主机隔开的一个个独立空间
* Image
  * Docker Image可以看做是一个特殊的文件系统，即对某一时刻容器状态的备份
* Registry
  * 官方：https://hub.docker.com/
  * 阿里：https://dev.aliyun.com/search.html



## 命令（command）



* 镜像

```shell
# 查看所有镜像
docker images
# 搜索镜像
docker search <关键词>
# 下载镜像
docker pull <镜像名称>
# 启动容器
# -d 后台运行
# --name 容器名称
# docker run -d -p 8080:8080 --name tomcat-test tomcat
docker run -d -p <宿主机端口号>:<容器端口号> --name <容器名> <镜像名>
# 查询所有容器
docker ps -a
# 查询启动容器
docker ps
# 删除容器，多个以空格分隔
docker rm <容器id>/<容器名>
# 进入容器
docker exec -it <容器id> /bin/bash
# 退出容器
exit
# 停止容器
docker stop <容器id>
# 启动容器
docker start <容器id>
# 删除容器
docker rm <容器id>
# 复制文件到容器中
docker cp <宿主机文件名> <容器id>:<目标目录>
# 复制文件到宿主机中
docker cp <容器id>:<目标目录> <宿主机文件名> 
```



## Dockerfile

用来创建镜像的文件。

### 内置命令

* FROM：以来的底层镜像
* MAINTAINER：指定镜像创建者
* ENV：设置环境变量
* RUN：运行shell命令（安装软件用）
* COPY：将宿主机S本地文件拷贝到镜像
* EXPOSE：指定监听端口
* ENTRYPOINT：与执行命令，创建容器并启动后才执行