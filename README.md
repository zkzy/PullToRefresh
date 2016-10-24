# PullToRefresh

一个简单的上拉加载下拉刷新的Adapter的封装，先看效果：

![](https://github.com/zkzy/PullToRefresh/raw/master/library/src/main/res/values/pic/lin.gif) </br>

![](https://github.com/zkzy/PullToRefresh/raw/master/library/src/main/res/values/pic/grid.gif)

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

```

###实现思路：
在显示最后一个条目的时候，修改数据源，添加一个条目，条目显示的是刷新布局，在完成刷新以后，删除数据源中刚刚添加的数据。判断是GridLayoutManager时候，设置刷新布局占据的列数。

##使用：
```
public class MainAdapter extends BaseRecycleViewAdapter<String> {
    public MainAdapter(Context context, RecyclerView recyclerView, SwipeRefreshLayout swipeRefreshLayout) {
        super(context, recyclerView, swipeRefreshLayout);
    }
    }
```
MainAdapter继承BaseRecycleViewAdapter

```
mainAdapter.setOnRefreshAndLoadMoreListener(new RefreshAndLoadMoreListener() {
            @Override
            public void onLoadingMoreData() {
                handler.sendEmptyMessageDelayed(0, 3000);

            }

            @Override
            public void onRefreshingData() {
                handler.sendEmptyMessageDelayed(1, 3000);
            }
        });
```
MainActivity中给Adapter添加刷新和加载更多的监听


###项目中很多不足，希望大家谅解。
