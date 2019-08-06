# Manjaro遇到的问题以及解决方法



## 搜狗输入法安装

1、安装

之前：

```shell
sudo pacman -S fcitx-configtool \
fcitx-im \
fcitx-sogoupinyin
```

现在：

多安装一个fcitx-qt4，冲突的地方全部覆盖

```shell
sudo pacman -S fcitx-qt4
```



2、创建～/.xprofile，添加以下内容：

```
export XMODIFIERS="@im=fcitx"
export GTK_IM_MODULE=fcitx
export QT_IM_MODULE=fcitx
```

3、重启



注：如果遇到在Jetbrain系列软件不能使用中文输入法时，尝试一下解决办法

1、以idea为例，在idea.sh文件中添加以下内容

```bash
export XMODIFIERS="@im=fcitx"
export GTK_IM_MODULE="fcitx"
export QT_IM_MODULE="fcitx"
# 在此位置添加
======RUN IDEA========
```

2、

```shell
rm -rf ~/.config/fcitx  
rm -rf ~/.sogouinput #如果安装了sogou输入法也执行这两个个
rm -rf cat ~/.config/Sogou
```