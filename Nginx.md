# Nginx

## 安装

1、下载`http://nginx.org/`

2、解压`tar zxvf nginx-1.8.0.tar.gz`

3、进入nginx目录   使用 configure 命令创建一 makeFile 文件。

```shell
./configure \
--prefix=/usr/local/nginx \	#指向安装目录
--pid-path=/var/run/nginx/nginx.pid \	#指向pid
--lock-path=/var/lock/nginx.lock \	#（安装文件锁定，防止安装文件被别人利用，或自己误操作。）
--error-log-path=/var/log/nginx/error.log \	#指向log
--http-log-path=/var/log/nginx/access.log \	#指向http-log
--with-http_gzip_static_module \	#启用ngx_http_gzip_static_module支持（在线实时压缩输出数据流）
--http-client-body-temp-path=/var/temp/nginx/client \ #设定http客户端请求临时文件路径
--http-proxy-temp-path=/var/temp/nginx/proxy \	#设定http代理临时文件路径
--http-fastcgi-temp-path=/var/temp/nginx/fastcgi \	#设定http代理临时文件路径
--http-uwsgi-temp-path=/var/temp/nginx/uwsgi \	#设定http代理临时文件路径
--http-scgi-temp-path=/var/temp/nginx/scgi	#设定http scgi临时文件路径
```

4、编译`make`

5、安装`make install`

6、启动

注意：启动nginx 之前，上边将临时文件目录指定为/var/temp/nginx/client， 需要在/var下

创建此目录`mkdir /var/temp/nginx/client -p`

进入到Nginx目录下的sbin目录

`cd /usr/local/ngiux/sbins`

输入命令启动Nginx

`./nginx start`

启动后查看进程

`ps aux|grep nginxs`

## 配置

修改安装目录下/conf/nginx.conf



