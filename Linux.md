# Linux



## 命令



### find

在指定目录下查找文件： 

```shell
find path [options] params
```

例：

```shell
# 当前目录下查找target.txt文件
find -name "target.txt"
# 全局搜索
find / -name "target.txt"
# 通配符
find / -name "target*"
# 忽略大小写
find / -iname "target*"
```



### grep

检索文件内容，查找文件里符合条件的字符串 

```shell
grep [options] pattern file
-v : 过滤
-o : 筛选
```

例：

```shell
# 在当前以target开头的文件中查找字符串
grep "what you wanna search" target*
# ~目录下的所有文件中包含某字符串的文件
find ~ | grep "str"
```



可以通过管道连接，层层筛选



### awk

一次读取一行文本，按输入分隔符进行切片，切成多个组成部分

将切片直接保存在内建变量中，$1,$2...($0表示行的全部)

支持对单个切片的判断，支持循环判断，默认分隔符为空格

```shell
awk [options] 'cmd' file
```

例：

```shell
# 对netstat.txt,打印第一个切片和第四个切片的内容
awk '{print $1,$4}' netstat.txt
# 打印满足某条件的行
awk '$1=="tcp" && $2 == 1 {print $0}' netstat.txt
# 包含表头信息
awk '($1=="tcp" && $2 == 1) || NR == 1 {print $0}' netstat.txt
# 以‘，’作为分隔符
awk -F "," '{print $2}' test.txt
# awk 统计
# 建议网络搜索，有点麻烦啊
```



### sed

批量替换文本内容

