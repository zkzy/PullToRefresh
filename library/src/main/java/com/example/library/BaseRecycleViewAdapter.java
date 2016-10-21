package com.example.library;

import android.content.Context;

import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;


import java.util.ArrayList;

import static android.support.v7.widget.RecyclerView.*;

/**
 * Created by hasee on 2016/10/21.
 */

public abstract class BaseRecycleViewAdapter<T> extends RecyclerView.Adapter<BaseRecycleViewAdapter.BaseViewHolder> implements SwipeRefreshLayout.OnRefreshListener {

    private ArrayList<T> mDataRes;
    public final Context mContext;
    private final RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private static final String TAG = "BaseRecycleViewAdapter";

    private String STATUS_NO_MORE = "没有更多了";
    private String STATUS_NOMAL = "正在加载...";
    private String STATUS_NO_NET = "没有网络了";
    private LoadingHolder loadingHolder;

    public enum Status {
        STATUS_NO_MORE, STATUS_NOMAL, STATUS_NO_NET
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

        }
    };

    /**
     * 正常的条目
     */
    private final int NOMAL_TYPE = 0;
    /**
     * 加载更多的条目
     */
    private final int LOADING_TYPE = 1;

    /**
     * 是否正在刷新
     */
    private boolean isLoadingNow;

    OnScrollListener mOnScrollListener = new OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
            if (dy > 0) {
                int firstVisiblePosition = linearLayoutManager.findFirstVisibleItemPosition();
                int totalItemCount = linearLayoutManager.getItemCount();
                int visibleItemCount = linearLayoutManager.getChildCount();
                Log.e(TAG, "onScrolled:firstVisiblePosition= " + firstVisiblePosition
                        + "visibleItemCount=" + visibleItemCount + "totalItemCount=" + totalItemCount);
                if (!isLoadingNow && (firstVisiblePosition + visibleItemCount) >= totalItemCount) {
                    isLoadingNow = true;
                    stratLoadingMore();
                    if (mRefreshAndLoadMoreListener != null) {
                        mRefreshAndLoadMoreListener.onLoadingMoreData();
                    }
                }
            }
        }
    };
    private RefreshAndLoadMoreListener mRefreshAndLoadMoreListener;

    @Override
    public void onRefresh() {
        if (mRefreshAndLoadMoreListener != null) {
            mRefreshAndLoadMoreListener.onRefreshingData();
        }
    }


    private void stratLoadingMore() {
        if (!isLoadingNow || mDataRes.size() < 1) return;
        int position = getLastItemPosition();
        mDataRes.add(mDataRes.get(0));
        notifyItemInserted(position + 1);
    }

    private void finishLoadingMore() {
        if (!isLoadingNow) return;
        int position = getLastItemPosition();
        isLoadingNow = false;
        mDataRes.remove(position);
        notifyItemRemoved(position);

    }

    private int getLastItemPosition() {
        return isLoadingNow ? getItemCount() - 1 : RecyclerView.NO_POSITION;
    }

    public void finishLode(Status status) {
        finishLoadingMore();
    }

    public void finishRefresh() {
        if (mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }

    public BaseRecycleViewAdapter(Context context, RecyclerView recyclerView, SwipeRefreshLayout swipeRefreshLayout) {
        this.mContext = context;
        this.mRecyclerView = recyclerView;
        mSwipeRefreshLayout = swipeRefreshLayout;
        this.mDataRes = new ArrayList<>();
        if (recyclerView != null) {
            this.mRecyclerView.addOnScrollListener(mOnScrollListener);
        }
        if (mSwipeRefreshLayout != null) {
            this.mSwipeRefreshLayout.setOnRefreshListener(this);
            swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_light, android.R.color.holo_red_light, android.R.color.holo_orange_light, android.R.color.holo_green_light);
        }

    }

    @Override
    public BaseRecycleViewAdapter.BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = null;
        if (viewType == NOMAL_TYPE) {
            itemView = getItemView();
            return getViewHolder(itemView);
        } else {
            itemView = View.inflate(mContext, R.layout.item_loading, null);
            return new LoadingHolder(itemView);
        }
    }

    protected abstract BaseViewHolder getViewHolder(View itemView);


    protected abstract View getItemView();


    @Override
    public void onBindViewHolder(BaseRecycleViewAdapter.BaseViewHolder holder, int position) {
        int viewType = getItemViewType(position);
        if (viewType == NOMAL_TYPE) {
            bindNomalViewHolder(holder, position);
        } else {
            bindLoadingViewHolder(holder, position);
        }
    }

    private void bindLoadingViewHolder(BaseViewHolder holder, int position) {
        loadingHolder = (LoadingHolder) holder;
        loadingHolder.mProgressBar.setVisibility(VISIBLE);
        loadingHolder.mTevLoad.setText(STATUS_NOMAL);
        loadingHolder.mTevLoad.setVisibility(VISIBLE);
    }


    private void bindNomalViewHolder(BaseViewHolder holder, int position) {
        fillViewData(holder, position, mDataRes.get(position));
    }

    protected abstract void fillViewData(BaseViewHolder holder, int position, T data);

    @Override
    public int getItemCount() {
        return mDataRes.size();
    }


    public void setOnRefreshAndLoadMoreListener(RefreshAndLoadMoreListener refreshAndLoadMoreListener) {
        mRefreshAndLoadMoreListener = refreshAndLoadMoreListener;
    }

    /**
     * 正在刷新，而且显示的是最后一个Position时候显示刷新布局
     *
     * @param position
     * @return
     */
    @Override
    public int getItemViewType(int position) {
        if (isLoadingNow && position == getItemCount() - 1) {
            return LOADING_TYPE;
        }
        return NOMAL_TYPE;
    }

    /**
     * 添加数据
     *
     * @param list
     */
    public void addLoadMoreItmes(ArrayList<T> list) {
        if (list != null && list.size() > 0) {
            mDataRes.addAll(list);
            notifyDataSetChanged();
        }
    }

    public void addRefreshItmes(ArrayList<T> list) {
        if (list != null && list.size() > 0) {
            mDataRes.addAll(0,list);
            notifyDataSetChanged();
        }
    }


    /**
     * 清空数据
     */
    public void clearData() {
        mDataRes.clear();
        notifyDataSetChanged();
    }

    public class BaseViewHolder extends ViewHolder {
        public BaseViewHolder(View itemView) {
            super(itemView);
        }
    }

    class LoadingHolder extends BaseViewHolder {
        ProgressBar mProgressBar;
        TextView mTevLoad;

        public LoadingHolder(View itemView) {
            super(itemView);
            mProgressBar = (ProgressBar) itemView.findViewById(R.id.progressbar);
            mTevLoad = (TextView) itemView.findViewById(R.id.tev_load);
        }
    }
}
