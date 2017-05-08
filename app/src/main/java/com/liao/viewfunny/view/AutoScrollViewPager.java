package com.liao.viewfunny.view;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

/**
 * Created by liaozhongjun on 2016/11/21.
 */

public class AutoScrollViewPager extends ViewPager {
    private static final int MSG_SCROLL_TO_NEXT = 100;
    private boolean dir;//true 增加 false 减小
    private boolean pauseAutoScroll = false;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_SCROLL_TO_NEXT:
                    if (mHandler.hasMessages(MSG_SCROLL_TO_NEXT)) {
                        mHandler.removeMessages(MSG_SCROLL_TO_NEXT);
                    }
                    if (pauseAutoScroll) {
                        return;
                    }
                    int cur = getCurrentItem();
                    if (cur == getAdapter().getCount() - 1) {
                        dir = false;
                    } else if (cur == 0) {
                        dir = true;
                    }
                    if (dir) {
                        cur++;
                    } else {
                        cur--;
                    }
                    setCurrentItem(cur, true);
                    Message next = Message.obtain(this, MSG_SCROLL_TO_NEXT);
                    sendMessageDelayed(next, 8000);
                    break;
            }
        }
    };

    public AutoScrollViewPager(Context context) {
        super(context);
    }

    public AutoScrollViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void startAutoScroll() {
        if (getAdapter().getCount() <= 1) {
            Log.i("ViewPager", "only one child can not scroll");
            return;
        }
        Message msg = Message.obtain(mHandler, MSG_SCROLL_TO_NEXT);
        mHandler.sendMessageDelayed(msg, 8000);
        dir = true;
    }

    public void stopAutoScroll() {
        pauseAutoScroll=true;
        mHandler.removeMessages(MSG_SCROLL_TO_NEXT);
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        int action = ev.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        int action = ev.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                pauseAutoScroll = true;
                requestDisallowInterceptTouchEvent(true);
                break;
            case MotionEvent.ACTION_UP:
                pauseAutoScroll = false;
                startAutoScroll();
                break;
        }
        return super.onTouchEvent(ev);
    }
}
