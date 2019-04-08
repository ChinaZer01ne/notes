# Git

##Git和Svn的区别

![](images\git\Git和Svn的区别.png)



Svn是集中式的，存储的是变化。中心化的。

Git是分布式的，存储的是完整的文件，去中心化，和区块链的思想相似。

Git保证完整性。

## 集中式版本控制（svn）和分布式版本控制的区别

集中式版本控制:(svn是这种形式)

有一个包含所有版本文件的单个服务器和一个数字(版本号),众多客户端从这个server上去检出文件(只是文件,本地没有仓库的概念)。

但是集中式的版本控制，有个严重的缺陷。就是中央服务器的单点故障。如果服务宕机一个小时，在这期间，没有任何人可以在正在工作的版本上很好的合作或者去保存某一个版本的改变。另外如果中央数据库的磁盘坏了，并且可能没有保存备份，那么将丢失所有的东西。你失去了绝对一切 - 除了单一的任何人的快照恰好有在本地计算机上项目的整个历史。当然本地的版本控制系统也有相同的问题。虽然，你能够把每个人的本地代码，进行合并得到一个相对完整的版本，但是当你把这个相对完整的版本重新部署到服务器的新仓库时，将会丢失所有的历史版本包括日志。

 ③分布式版本控制：（git是这种形式，GIT跟SVN一样有自己的集中式版本库或服务器）

 这是在分布式版本控制系统（DVCSs）步在DVCS（如GIT中），客户端不只是检查出文件的最新快照：他们完全镜像的存储库（本地有仓库，这就是分布式的意义）。因此，如果出现上述问题，任何客户机库的可复制备份到服务器，以恢复它。每一个克隆确实是所有数据的完整备份(除了没有push的代码，这个也是理所当然的)。

 那么针对于本地版本控制系统，和集中式版本控制系统的最严重的缺陷，就被分布式版本控制系统解决了。

1、git是分布式的scm,svn是集中式的。(最核心)

2、git是每个历史版本都存储完整的文件,便于恢复,svn是存储差异文件,历史版本不可恢复。(核心)

3、git可离线完成大部分操作,svn则不能。

4、git有着更优雅的分支和合并实现。

5、git有着更强的撤销修改和修改历史版本的能力

6、git速度更快,效率更高。

基于以上区别,git有了很明显的优势,特别在于它具有的本地仓库。

## 概念

工作区：

暂存区：通过add命令，让文件进入暂存区

本地版本库：通过commit命令，文件进入本地版本库

远程版本库：通过push命令，文件进入远程版本库

**git文件状态：已修改，已暂存，已提交。**

## 安装

a）git config --global user.username 'xxx'

b）git config --global user.email 'xxx'

c）ssh-keygen -t rsa -C ‘xxx’



对于user.name和user.email来说，有3个地方可以设置,查找顺序最近原则3，2，1.

1、/etc/gitconfig（几乎不会使用），针对于操作系统，git config --system

2、~/.gitconfig（很常用），针对用用户，git config --global

3、.git/config文件中，针对于特定项目的，git config --local



## git常用命令

```shell
# 文件进入stage
git add <file>

# 让文件从暂存区回到工作区
git rm --cached <file> 
git reset HEAD <file>` 

# 删除文件并且将其放入暂存区
git rm <file>
# 修改的文件还原到未修改状态，内容找不回了。	
git checkout -- <file>

# 进入本地仓库，-m指定提交信息
git commit

# 修正最近一条的提交信息
git commit amend -m "修正"

# 到远端仓库
git push

# 查看当前仓库状态。会提示那些文件发生修改，哪些内容需要add&commit。
git status

# 日志
git log
# 前三条
git log -3
# 日志列表
git log pretty=oneline

# 帮助文档
git config --help
git help config

# 从远程仓库克隆
git clone [地址]

# 删除分支
git checkout -d [分支名]

# 查看远程仓库的信息
git remote

# 创建新分支，并且切换到该分支，等价于： 
# git brach [分支名]`创建分支 
# git checkout [分支名]`切换分支 
# 如果此时有未提交的修改，是无法切换分支的，这时候就可以用`git stash`进行暂存
git checkout -b [分支名]

# 查看分支，-r显示所有远程分支，-a显示所有本地分支和远程分支
git branch

# 关联远程仓库
git remote add origin https://github.com/ChinaZer01ne/utils.git

# 将本地的分支和远程分支进行关联了
git branch --set-upstream-to=origin/<branch>

# 合并两个独立的仓库
git pull origin master –allow-unrelated-histories
```



## .gitignore文件

```properties
# 忽略所有以d结尾的文件
*.d
# 忽略当前文件的mydir文件下的所有文件
mydir/
# 忽略根目录下test.txt文件
/test.txt
# 忽略所有目录下test.txt文件，**代表所有层次
/**/test.txt
```



## 分支

```shell
# 查看分支，-r显示所有远程分支，-a显示所有本地分支和远程分支
git branch
# 创建分支 
git brach [分支名]
# 切换分支 
# 如果此时有未提交的修改，是无法切换分支的，这时候就可以用`git stash`进行暂存
git checkout [分支名]
# 创建新分支，并且切换到该分支
git checkout -b [分支名]
```



## 其他

git的提交id（commit id）是一个摘要值，是通过sha1计算出来的。

如果新创建一个文件夹mydir，如果mydir中没有文件，git是不识别的



