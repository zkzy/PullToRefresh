package com.example.hasee.pulltorefresh;

import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.example.library.BaseRecycleViewAdapter;
import com.example.library.RefreshAndLoadMoreListener;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private MainAdapter mainAdapter;
    Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            mainAdapter.finishLode(BaseRecycleViewAdapter.Status.STATUS_NOMAL);
            mainAdapter.finishRefresh();
            if(msg.what==0){
                mainAdapter.addLoadMoreItmes(initData("加载更多"));
            }
            if(msg.what==1){
                mainAdapter.addRefreshItmes(initData("刷新"));
            }

        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        mainAdapter.addLoadMoreItmes(initData(""));
    }

    private ArrayList<String>  initData(String data) {
        ArrayList<String> list = new ArrayList<>();
        for (char i = 'A'; i <='Z' ; i++) {
            list.add(String.valueOf(i)+data);
        }
      return list;
    }

    private void initView() {
        mRecyclerView= (RecyclerView) findViewById(R.id.recycleview);
        mSwipeRefreshLayout= (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mainAdapter = new MainAdapter(this,mRecyclerView,mSwipeRefreshLayout);
        mRecyclerView.setAdapter(mainAdapter);
        mainAdapter.setOnRefreshAndLoadMoreListener(new RefreshAndLoadMoreListener() {
            @Override
            public void onLoadingMoreData() {
                handler.sendEmptyMessageDelayed(0,3000);

            }

            @Override
            public void onRefreshingData() {
                handler.sendEmptyMessageDelayed(1,3000);
            }
        });
    }


}
