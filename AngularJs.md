# AngularJs

## 入门

1、引入AngularJs的核心js文件

2、

```js
<html>
<head>
    //引入js
	<script src="angular.min.js"></script>	
	<script>
		var app=angular.module('myApp',[]); //定义了一个叫myApp的模块
		//定义控制器，通过js给$scope绑定属性，然后再body体中操作数据
		app.controller('myController',function($scope){		
			$scope.list= [
				{name:'张三',shuxue:100,yuwen:93},
				{name:'李四',shuxue:88,yuwen:87},
				{name:'王五',shuxue:77,yuwen:56}
			];//定义数组			
		});	
	</script>	
</head>
//ng-app 指定模块名
//ng-controller 指定控制器名
<body ng-app="myApp" ng-controller="myController">
<table>
<tr>
	<td>姓名</td>
	<td>数学</td>
	<td>语文</td>
</tr>
<tr ng-repeat="entity in list">
	<td>{{entity.name}}</td>
	<td>{{entity.shuxue}}</td>
	<td>{{entity.yuwen}}</td>
</tr>
</table>
</body>
</html>
```



