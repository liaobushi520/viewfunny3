package com.liao.viewfunny.transfer;

import android.support.v7.widget.RecyclerView;

/**
 * Created by liaozhongjun on 2017/4/18.
 */

public interface RecyclerViewAnimationObserver {
    public void onAddStarting(RecyclerView rv, RecyclerView.ViewHolder item);
    public void onAddFinished(RecyclerView rv, RecyclerView.ViewHolder item);
}
