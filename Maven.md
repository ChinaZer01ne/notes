# Maven

## 1.convention over configuration.

约定优于配置

## 2.超级POM文件位置：

maven-model-build-${version}.jar/org/apache/maven/model/pom.xml



##3.maven寻找setting.xml文件的优先级：

​	1）~/.m2/setting.xml

​	2）安装目录/conf/setting.xml



##4.setting：

​	localRepository : 定义jar包下载位置

​	mirros：镜像

​	...

##5.pom.xml

​	groupId: 分组名

​	artifactId: 功能名

​	version:	版本

​	packaging:  打包方式，jar、war、pom、maven-plugin，默认jar

​	dependenceManagement：

​		1）只能出现在父pom里面（虽然他也可以在子pom里面，但我们不这样写）	

​		2）统一版本号

​		3）声明（子pom中用到再引用）

​	dependency：

​		1）Type：默认jar

​		2）scope：解决了什么时候会用和会不会打包到项目中的问题

​			a）compile：编译的时候会用，打包，例如spring-core

​			b）test：测试的时候会用，不打包，例如junit

​			c）provided：编译的时候会用，不打包，例如servlet

​			d）runtime：运行时会用，打包。例如jdbc驱动实现

​			e）system：本地jar，不在maven仓库里，配合   `systemPath`使用，指定jar包位置。例如阿里的鱼卡短信调用

​			f）依赖传递

​				mvn dependency:tree > d.txt 查看包依赖

​				

| 第二依赖范围👉<br />第一依赖范围👇 | complie  | test | provided | runtime  |
| :------------------------------: | :------: | :--: | :------: | :------: |
|             compile              | compile  |  -   |    -     | runtime  |
|               test               |   test   |  -   |    -     |   test   |
|             provided             | provided |  -   | provided | provided |
|             runtime              | runtime  |  -   |    -     | runtime  |

​			g）依赖仲裁

​				i）最短路径原则，例如jar版本依赖，会依赖版本树中最近的版本

​				ii）加载顺序原则，pom加载的顺序

​				iii）exclusion，解决jar包冲突，排除

##6.生命周期	Lifecycle/Phases/Goals

​	A Build Lifecycle is Made Up of Phases

​	A Build Phases is Made Up of Plugin Goals



​	1）clean：

​			pre-clean:

​			clean:

​			post-clean:

​	2）default:

​			complie:

​			package:

​			install:

​			deploy:

​			...

​	3）site:

​			pre-site；

​			site：

​			post-site:

​			site-deploy:

![Maven生命周期](images\maven\Maven生命周期.png)

##7.版本管理：

​	a）1.0-SNAPSHOT（约定由于配置，不稳定版）

​	b）刷新本地仓库

​		1）从repository删除

​		2）mvn clean package -U（强制拉一次）

​	c）主版本号.此版本号.增量版本号-<里程碑版本>



##8.常用命令

​	a）compile

​	b）clean	删除 target/

​	c）test		运行test case  junit/testNG

​	d）package	打包

​	e）install	把项目 install 到 local repository

​	f）deploy	把本地的jar发布到私服上面去



##9.常用插件

​	i. <https://maven.apache.org/plugins/> 

​	ii. <http://www.mojohaus.org/plugins.html> 

​	iii. findbugs 静态代码检查

​	iv. versions 统一升级版本号

​		1. mvn versions:set -DnewVersion=1.1

​	v. source 打包源代码

​	vi. assembly 打包zip、war

​	vii. tomcat7

## 10.自定义插件

https://maven.apache.org/guides/plugin/guide-java-plugin-development.html

​	a）新建Maven项目

​	b）打包方式：maven-plugin（	`<packaging>maven-plugin</packaging>`）

​	c）引入以下两个插件

```xml
<dependencies>
        <dependency>
            <groupId>org.apache.maven</groupId>
            <artifactId>maven-plugin-api</artifactId>
            <version>3.5.0</version>
        </dependency>
        <dependency>
            <groupId>org.apache.maven.plugin-tools</groupId>
            <artifactId>maven-plugin-annotations</artifactId>
            <version>3.5</version>
            <scope>provided</scope>
        </dependency>
    </dependencies>
```

​	d）添加 	`@Mojo`注解，name表示goal名称，defaultPhase默认运行阶段。

​		继承 `AbstractMojo`类，实现 `execute`方法。

```java
@Mojo(name = "lion-plugin", defaultPhase = LifecyclePhase.PACKAGE)
public class LionMojo extends AbstractMojo {

    public void execute() throws MojoExecutionException, MojoFailureException {
        System.out.println("lion plugin");
    }
}
```

​	e）`mvn clean install`将插件安装到本地仓库就可以使用了。



​	f）插件只有挂载到某个Phase（阶段）或者直接双击插件才能运行。

```xml
<plugin>
    <groupId>com.zer01ne</groupId>
    <artifactId>lion-plugin</artifactId>
    <version>1.0-SNAPSHOT</version>
    <executions>
        <execution>
            <phase>clean</phase>	<!--clean阶段运行这个插件-->
            <goals>
                <goal>lion-plugin</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

​	g）插件类也可以进行传值

```java
 	@Parameter
    private String msg;	//传值
	@Parameter
    private List<String> options;//传值
    public void execute() throws MojoExecutionException, MojoFailureException {
        System.out.println("lion plugin " + msg);	//使用
        System.out.println("lion plugin " + options);	//使用
    }
```

```xml
<plugin>
    <groupId>com.zer01ne</groupId>
    <artifactId>lion-plugin</artifactId>
    <version>1.0-SNAPSHOT</version>
    <configuration>	<!--这里就是使用插件时的传值操作-->
        <msg>This is massage!</msg>
        <options>	<!-- 需要与插件中定义的属性名一致-->
            <param>one</param>	<!-- 这个标签可以随便写，不一定时param-->
            <param>two</param>
            <param>three</param>
        </options>
    </configuration>
    <executions>
        <execution>
            <phase>clean</phase>
            <goals>
                <goal>lion-plugin</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

传值问题参见：

https://maven.apache.org/guides/plugin/guide-java-plugin-development.html



## 11.profile多环境配置

使用场景,多环境的配置，dev/test/pro

例如工程目录：

![1528780694248](images\maven\工程目录.png)

```xml
<profiles>
    <!--dev环境-->
    <profile>
        <id>dev</id>
        <properties>
            <profiles.active>dev</profiles.active>
        </properties>
        <activation>
            <activeByDefault>true</activeByDefault>
        </activation>
    </profile>
     <!--test环境-->
    <profile>
        <id>test</id>
        <properties>
            <profiles.active>test</profiles.active>
        </properties>
    </profile>
     <!--pro环境-->
    <profile>
        <id>pro</id>
        <properties>
            <profiles.active>pro</profiles.active>
        </properties>
    </profile>
</profiles>
<build>
    <resources>
        <resource>
            <!--排除所有的配置文件-->
            <directory>${basedir}/src/main/resources</directory>
            <excludes>
                <exclude>conf/**</exclude>
            </excludes>
        </resource>
        <resource>
            <!--加载指定配置文件-->
            <directory>src/main/resources/conf/${profiles.active}</directory>
        </resource>
    </resources>
```

​	可以使用`mvn intall -P “环境id”`

​	多环境配置还可以在 maven 的 setting.xml 文件中配置，应用场景：可以配置在家工作和在公司工作的两套环境，与第一种方式应用场景不太一样。



## 12.仓库

a) 下载

b) 安装 解压

c) 使用<http://books.sonatype.com/nexus-book/reference3/index.html> 

​	i. <http://192.168.1.6:8081/nexus>

​	ii. admin/admin123

d) 发布

​	i. pom.xml 配置 

```xml
<distributionManagement>
     <!--如果版本不是snapshot，走这个-->
    <repository>
        <id>nexus-releases</id>	<!--与setting中的仓库id一致-->
        <name>Nexus Releases Repository</name>
        <url>http://192.168.1.127:8081/repository/maven-releases/</url>		<!--仓库地址-->
    </repository>
    <!--如果版本是snapshot，走这个-->
    <snapshotRepository>
        <id>nexus-snapshots</id>
        <name>Nexus Snapshot Repository</name>
        <url>http://192.168.1.127:8081/repository/maven-snapshots/</url>
    </snapshotRepository>
</distributionManagement>
```

​	ii.setting.xml配置

```xml
<server>
      <id>nexus-releases</id>	<!--与pom中的id一致-->
      <username>admin</username>
      <password>admin123</password>
    </server>
     <server>
      <id>nexus-snapshots</id>
      <username>admin</username>
      <password>admin123</password>
    </server>
  </servers>
```

e）从私服下载jar

​	第一种方式：配置mirror

​	第二种方式：setting文件的profile属性



## 13.archetype模板化

​	a） 生成一个archetype

​		i、生成archetype

​		`mvn archetype:create-from-project`

​		ii、进入以下目录

​		`cd /target/generated-sources/archetype`

​		iii、安装到本地仓库

​		`mvn install`

​		iiii、如果想要发布到私服

​		`mvn deploy`

​		注意如果想发布，那么当前文件夹的pom文件必须包含一下属性

```xml
<distributionManagement>
    <repository>
      <id>nexus-releases</id>
      <name>Nexus Releases Repository</name>
      <url>http://192.168.1.127:8081/repository/maven-releases/</url>
    </repository>
    <snapshotRepository>
      <id>nexus-snapshots</id>
      <name>Nexus Snapshot Repository</name>
      <url>http://192.168.1.127:8081/repository/maven-snapshots/</url>
    </snapshotRepository>
  </distributionManagement>
```



​		

​	b) 从生成的archetype创建新的项目 (命令的方式)

​		i、`mvn archetype:generate -DarchetypeCatalog=local`

​		ii、选择使用的archetype

​		iii、按步操作，略

## 14.springboot与maven整合

当出现找不到类的问题时，需要将项目安装到本地仓库。

如果直接运行`mvn install`，maven会将springboot的一个启动jar文件 安装到本地仓库，这个jar是我们项目中不能用的。如果别的模块引用了该模块，会出现找不到类或者包的问题。

比如：dao模块依赖了pojo模块，如果将由springboot构建的pojo模块安装到本地仓库，当我们再安装dao的时候，就会出现找不到pojo的问题。

解决方法：

1、执行`mvn package`

2、进入target目录，里面有个original文件

3、执行`mvn install:install-file -Dfile=xxx.original -DgroupId="your groupId" -DartifactId="your artifactId" -Dversion="your version" -Dpackaging=jar`

## 15.遇到的问题

1、依赖jar包的时候没有触发传递依赖。

​	我对接阿里大于的时候，引入`com.aliyun:aliyun-java-sdk-core:3.7.1`的时候，应该会把他需要的包同时依赖过来，但是没有。

​	原因：本地仓库里的``com.aliyun:aliyun-java-sdk-core:3.7.1`没有把pom给下下来（阿里云的私服上没有给出pom），本地下下来的是lastupdated后缀的。

​	解决：从down下来的jar中把pom文件内容拷贝给了本地仓库的pom文件。

