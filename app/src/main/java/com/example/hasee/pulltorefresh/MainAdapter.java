package com.example.hasee.pulltorefresh;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.example.library.BaseRecycleViewAdapter;

/**
 * Created by hasee on 2016/10/21.
 */

public class MainAdapter extends BaseRecycleViewAdapter<String> {
    public MainAdapter(Context context, RecyclerView recyclerView, SwipeRefreshLayout swipeRefreshLayout) {
        super(context, recyclerView, swipeRefreshLayout);
    }

    @Override
    protected BaseRecycleViewAdapter<String>.BaseViewHolder getViewHolder(View itemView) {
        return new MainHolder(itemView);
    }

    @Override
    protected View getItemView() {
        return View.inflate(mContext, R.layout.item_nomal, null);
    }

    @Override
    protected void fillViewData(BaseRecycleViewAdapter<String>.BaseViewHolder holder, int position, String data) {
        MainHolder mainHolder = (MainHolder) holder;
        mainHolder.mTevContent.setText(data);
    }

    class MainHolder extends BaseViewHolder {
        TextView mTevContent;

        public MainHolder(View itemView) {
            super(itemView);
            mTevContent = (TextView) itemView.findViewById(R.id.tev_content);
        }
    }

}
