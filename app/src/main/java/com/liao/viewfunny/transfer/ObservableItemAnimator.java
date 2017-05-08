package com.liao.viewfunny.transfer;

import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;

/**
 * Created by liaozhongjun on 2017/4/18.
 */

public class ObservableItemAnimator extends DefaultItemAnimator {
    private RecyclerView mRecyclerView;
    private RecyclerViewAnimationObserver mObserver;

    public ObservableItemAnimator(RecyclerView recyclerView, RecyclerViewAnimationObserver observer) {
        mRecyclerView = recyclerView;
        mObserver = observer;
    }

    @Override
    public void onAddStarting(RecyclerView.ViewHolder item) {
        mObserver.onAddStarting(mRecyclerView, item);
        super.onAddStarting(item);
    }

    @Override
    public void onAddFinished(RecyclerView.ViewHolder item) {
        mObserver.onAddFinished(mRecyclerView, item);
        super.onAddFinished(item);
    }
}
