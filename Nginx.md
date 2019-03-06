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

> 可能出现的错误：
>
> 1、
>
> ```shell
> ./configure: error: the HTTP rewrite module requires the PCRE library.
> You can either disable the module by using --without-http_rewrite_module
> option, or install the PCRE library into the system, or build the PCRE library
> statically from the source with nginx by using --with-pcre=<path> option.
> ```
>
> 解决方法：
>
> ```shell
> yum -y install pcre-devel
> ```
>
> 2、
>
> ```shell
> ./configure: error: the HTTP gzip module requires the zlib library.
> You can either disable the module by using --without-http_gzip_module
> option, or install the zlib library into the system, or build the zlib library
> statically from the source with nginx by using --with-zlib=<path> option.
> 
> ```
>
> 解决方法：
>
> ```shell
> yum install -y zlib-devel
> ```
>
>



4、编译`make`

5、安装`make install`

6、启动

注意：启动nginx 之前，上边将临时文件目录指定为/var/temp/nginx/client， 需要在/var下

创建此目录`mkdir /var/temp/nginx/client -p`

进入到Nginx目录下的sbin目录

`cd /usr/local/ngiux/sbins`

输入命令启动Nginx

`./nginx `

平滑重启

`./nginx -s reload`

启动后查看进程

`ps aux|grep nginxs`

查看nginx的其他操作

`./nginx -h`

> 创建全局命令,只需要创建软连接到`$PATH`的目录下(`echo $PATH`查看)
>
> `ln -n /usr/local/nginx/sbin/nginx /usr/local/sbin/`

## 配置

修改安装目录下`/conf/nginx.conf`

```perl
# 运行用户
#user  nobody;
# nginx进程,一般设置为和cpu核数一样
worker_processes  1;
# 错误日志存放目录 
#error_log  logs/error.log;
#error_log  logs/error.log  notice;
#error_log  logs/error.log  info;
# 进程pid存放位置
#pid        logs/nginx.pid;
# 工作模式及连接数上限
events {
	# 单个后台worker process进程的最大并发链接数
    worker_connections  1024;
}

http {
    include       mime.types;	#文件扩展名与类型映射表
    default_type  application/octet-stream;	#默认文件类型

    #log_format  main  '$remote_addr - $remote_user [$time_local] "$request" '
    #                  '$status $body_bytes_sent "$http_referer" '
    #                  '"$http_user_agent" "$http_x_forwarded_for"';

    #access_log  logs/access.log  main;

    sendfile        on;	  #开启高效传输模式
    #激活tcp_nopush参数可以允许把httpresponse header和文件的开始放在一个文件里发布，积极的作用是减少网络报文段的数量
    #tcp_nopush     on;	

    #keepalive_timeout  0;
    keepalive_timeout  65;	# 连接超时时间，单位是秒

    #gzip  on;	# 开启gzip压缩功能
	##cache缓存##
	#跟后端服务器连接的超时时间_发起握手等候响应超时时间
    proxy_connect_timeout 500;
    #连接成功后_等候后端服务器响应的时间_其实已经进入后端的排队之中等候处理
    proxy_read_timeout 600;
    #后端服务器数据回传时间_就是在规定时间内后端服务器必须传完所有数据
    proxy_send_timeout 500;
    #代理请求缓存区_这个缓存区间会保存用户的头信息以供Nginx进行规则处理_一般只要能保存下头信息即可  
    proxy_buffer_size 128k;
    #同上 告诉Nginx保存单个用的几个Buffer最大用多大空间
    proxy_buffers 4 128k;
    #如果系统很忙的时候可以申请更大的proxy_buffers 官方推荐*2
    proxy_busy_buffers_size 256k;
     #proxy缓存临时文件的大小
    proxy_temp_file_write_size 128k;
    #用于指定本地目录来缓冲较大的代理请求
    proxy_temp_path /usr/local/nginx/temp;
    #设置web缓存区名为cache_one,内存缓存空间大小为12000M，自动清除超过15天没有被访问过的缓存数据，硬盘缓存空间大小200g
    proxy_cache_path /usr/local/nginx/cache levels=1:2 keys_zone=cache_one:200m inactive=1d max_size=30g;
  
	# 反向代理负载均衡设定部分
    # upstream表示负载服务器池，定义名字为backend_server的服务器池
    # upstream backend_server {
    ##   ip_hash; # 可以指定负载均衡策略
    #    server   10.254.244.20:81 weight=1 max_fails=2 fail_timeout=30s;
    #    server   10.254.242.40:81 weight=1 max_fails=2 fail_timeout=30s;
    #    server   10.254.245.19:81 weight=1 max_fails=2 fail_timeout=30s;
    #    server   10.254.243.39:81 weight=1 max_fails=2 fail_timeout=30s;
      #设置由 fail_timeout 定义的时间段内连接该主机的失败次数，以此来断定 fail_timeout 定义的时间段内该主机是否可用。默认情况下这个数值设置为 1。零值的话禁用这个数量的尝试。
    #设置在指定时间内连接到主机的失败次数，超过该次数该主机被认为不可用。
    #这里是在30s内尝试2次失败即认为主机不可用！
    #  }
    
    #基于域名的虚拟主机
     server {
    listen 80;
    server_name www.test.com;
    index index.html;
    location / {
    #缓存配置
        proxy_pass http://backend_server;
        proxy_cache cache;
        proxy_cache_valid   200 304 12h;
        proxy_cache_valid   any 10m;
        add_header  Nginx-Cache "$upstream_cache_status";
        proxy_next_upstream error timeout invalid_header http_500 http_502 http_503 http_504;
        }
    }
    server {
   		#监听端口
        listen       80;
        server_name  localhost;

        #charset koi8-r; #gbk,utf-8,gb2312,gb18030 可以实现多种编码识别

        #access_log  logs/host.access.log  main; #日志格式及日志存放路径
		# 当url访问/的时候，找的是html/index.html或htm文件（nginx主目录）
		# location定位是唯一的，不要出现交叉，否则会出现404
        location / {
            root   html;
            index  index.html index.htm;
        }
        # 当url访问/xxx/lion/1.html的时候，找的是 /home/abc/lion/1.html文件
		location /xxx {
            root   /home/abc;
            index  index.html;
        }
        location ~.*\.(jpg|png|css|js)$ {
            root   /home/static;
            expires      30d; #客户端缓存上述js,css数据30天
        }
        #location ~.*\.(xls)$ {
        #	 default_type  application/octet-stream;
        #	 可以配置文件下载
        #    add_header Content_disposition ""attachment;
        #    root   /home/file;
        #}
        # 搭配upstream实现负载均衡
        #location / { 
        #    root  html; 
        #    index  index.html index.htm; 
        #    proxy_pass http://backend_server; 
		#}	
        #error_page  404              /404.html;

        # redirect server error pages to the static page /50x.html
        # 错误跳转页面
        error_page   500 502 503 504  /50x.html;
        location = /50x.html {
            root   html;
        }

        # proxy the PHP scripts to Apache listening on 127.0.0.1:80
        #
        #location ~ \.php$ {
        #    proxy_pass   http://127.0.0.1;
        #}

        # pass the PHP scripts to FastCGI server listening on 127.0.0.1:9000
        #
        #location ~ \.php$ {
        #    root           html;
        #    fastcgi_pass   127.0.0.1:9000;
        #    fastcgi_index  index.php;
        #    fastcgi_param  SCRIPT_FILENAME  /scripts$fastcgi_script_name;
        #    include        fastcgi_params;
        #}

        # deny access to .htaccess files, if Apache's document root
        # concurs with nginx's one
        #
        #location ~ /\.ht {
        #    deny  all;
        #}
    }
  # another virtual host using mix of IP-, name-, and port-based configuration
    #
    #server {
    #    listen       8000;
    #    listen       somename:8080;
    #    server_name  somename  alias  another.alias;

    #    location / {
    #        root   html;
    #        index  index.html index.htm;
    #    }
    #}


    # HTTPS server
    #
    #server {
    #    listen       443 ssl;
    #    server_name  localhost;

    #    ssl_certificate      cert.pem;
    #    ssl_certificate_key  cert.key;

    #    ssl_session_cache    shared:SSL:1m;
    #    ssl_session_timeout  5m;

    #    ssl_ciphers  HIGH:!aNULL:!MD5;
    #    ssl_prefer_server_ciphers  on;

    #    location / {
    #        root   html;
    #        index  index.html index.htm;
    #    }
    #}

}
```

[[Nginx配置upstream实现负载均衡](https://www.cnblogs.com/wzjhoutai/p/6932007.html)]