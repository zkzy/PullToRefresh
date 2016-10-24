package com.example.library;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import static android.support.v7.widget.RecyclerView.LayoutManager;
import static android.support.v7.widget.RecyclerView.OnScrollListener;
import static android.support.v7.widget.RecyclerView.VISIBLE;
import static android.support.v7.widget.RecyclerView.ViewHolder;

/**
 * Created by hasee on 2016/10/21.
 */

public abstract class BaseRecycleViewAdapter<T> extends RecyclerView.Adapter<BaseRecycleViewAdapter.BaseViewHolder> implements SwipeRefreshLayout.OnRefreshListener {

    private ArrayList<T> mDataRes;
    public final Context mContext;
    private final RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private static final String TAG = "BaseRecycleViewAdapter";

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
    private String STATUS_NO_MORE = "没有更多了";
    private String STATUS_NOMAL = "正在加载...";
    private String STATUS_NO_NET = "没有网络了";

    private int mLoadingTextColor;


    public enum Status {
        STATUS_NO_MORE, STATUS_NOMAL, STATUS_NO_NET
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch ((Status)msg.obj){
                case STATUS_NO_MORE:
                    Toast.makeText(mContext, ""+STATUS_NO_MORE, Toast.LENGTH_SHORT).show();
                    break;
                case STATUS_NO_NET:
                    Toast.makeText(mContext, ""+STATUS_NO_NET, Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };



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
        }

    }
    /**
     * 特殊的条目占几个位置
     * @param recyclerView
     */
    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        LayoutManager layoutManager=recyclerView.getLayoutManager();
        if(layoutManager instanceof GridLayoutManager){
            final GridLayoutManager gridLayoutManager= (GridLayoutManager) layoutManager;
            gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    int viewType=getItemViewType(position);
                    return viewType==LOADING_TYPE ? gridLayoutManager.getSpanCount() : 1;

                }
            });
        }
    }

    OnScrollListener mOnScrollListener = new OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            if (dy > 0) {
                LayoutManager layoutManager =recyclerView.getLayoutManager();
                int firstVisiblePosition=-1;
                int totalItemCount=-1;
                int visibleItemCount=-1;
                if(layoutManager instanceof LinearLayoutManager){
                    LinearLayoutManager linearLayoutManager= (LinearLayoutManager) layoutManager;
                    firstVisiblePosition = linearLayoutManager.findFirstVisibleItemPosition();
                    totalItemCount = linearLayoutManager.getItemCount();
                    visibleItemCount = linearLayoutManager.getChildCount();
                }else if(layoutManager instanceof GridLayoutManager){
                    GridLayoutManager gridLayoutManager= (GridLayoutManager) layoutManager;
                    firstVisiblePosition = gridLayoutManager.findFirstVisibleItemPosition();
                    totalItemCount = gridLayoutManager.getItemCount();
                    visibleItemCount = gridLayoutManager.getChildCount();
                }

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

    /**
     * 正在刷新
     */
    @Override
    public void onRefresh() {
        if (mRefreshAndLoadMoreListener != null) {
            mRefreshAndLoadMoreListener.onRefreshingData();
        }
    }

    /**
     * 触发开始加载更多，显示最后一个刷新条目
     */
    private void stratLoadingMore() {
        if (!isLoadingNow || mDataRes.size() < 1) return;
        int position = getLastItemPosition();
        mDataRes.add(mDataRes.get(0));
        notifyItemInserted(position + 1);
    }

    /**
     * 删除刷新，删除刷新时候添加的数据
     */
    private void finishLoadingMore() {
        if (!isLoadingNow) return;
        int position = getLastItemPosition();
        isLoadingNow = false;
        mDataRes.remove(position);
        notifyItemRemoved(position);

    }

    /**
     * 获取最后一个条目的位置
     * @return
     */
    private int getLastItemPosition() {
        return isLoadingNow ? getItemCount() - 1 : RecyclerView.NO_POSITION;
    }

    /**
     * 提供给外界的方法，停止加载更多
     * @param status
     */
    public void finishLode(Status status) {
        Message msg=Message.obtain(handler);
        msg.obj=status;
        handler.handleMessage(msg);
        finishLoadingMore();
    }

    /**
     * 停止刷新
     */
    public void finishRefresh() {
        if (mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout.setRefreshing(false);
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
        LoadingHolder loadingHolder = (LoadingHolder) holder;
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

    /***
     * 设置加载更多，刷新的监听
     * @param refreshAndLoadMoreListener
     */
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

    public void setLoadingTextColor(int color){
        mLoadingTextColor = color;
    }

    public void setRefreshEnabled(boolean enabled){
        if(mSwipeRefreshLayout!=null&&!enabled){
            mSwipeRefreshLayout.setOnRefreshListener(null);
        }
    }
    public void setLoadMoreEnabled(boolean enabled){
        if(mRecyclerView!=null&&!enabled){
            mRecyclerView.removeOnScrollListener(mOnScrollListener);
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
