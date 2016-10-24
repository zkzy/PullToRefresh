# PullToRefresh

一个简单的上拉加载下拉刷新的Adapter的封装，先看效果：

![](https://github.com/zkzy/PullToRefresh/tree/master/library/src/main/res/values/pic/lin.gif) 

![](https://github.com/zkzy/PullToRefresh/tree/master/library/src/main/res/values/pic/grid.gif)

</br>

gradle引用：

```java
allprojects {
		repositories {
			...
			maven { url "https://jitpack.io" }
		}
	}
  
  	dependencies {
	        compile 'com.github.zkzy:PullToRefresh:1.0'
	}



###实现思路：
在显示最后一个条目的时候，修改数据源，添加一个条目，条目显示的是刷新布局，在完成刷新以后，删除数据源中刚刚添加的数据。判断是GridLayoutManager时候，设置刷新布局占据的列数。

#项目中很多不足，希望大家谅解。
