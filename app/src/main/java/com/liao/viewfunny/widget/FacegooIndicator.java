package com.liao.viewfunny.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

/**
 * Created by liaozhongjun on 2017/2/21.
 */


public class FacegooIndicator extends LinearLayout implements ViewPager.OnPageChangeListener, ViewPager.OnAdapterChangeListener {

    private ViewPager viewPager;
    private final static int DEFAULT_MARKER_WH = 4;//dp
    private int markerWH;

    public FacegooIndicator(Context context) {
        this(context, null);
    }

    public FacegooIndicator(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FacegooIndicator(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        final float density = getResources().getDisplayMetrics().density;
        markerWH = (int) (DEFAULT_MARKER_WH * density + .5f);
    }

    public void setViewPager(ViewPager viewPager) {
        if (viewPager == null) {
            throw new IllegalArgumentException("ViewPager can not be null");
        }
        if (this.viewPager != viewPager) {
            this.viewPager = viewPager;
            this.viewPager.addOnPageChangeListener(this);
            this.viewPager.addOnAdapterChangeListener(this);
        }
    }


    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        if (getChildCount() <= 0) {
            return;
        }
        Marker curMarker = (Marker) getChildAt(position);
        curMarker.setProgress((int) (255 * (1 - positionOffset)));
        if (position + 1 < getChildCount()) {
            Marker nextMarker = (Marker) getChildAt(position + 1);
            nextMarker.setProgress((int) (255 * positionOffset));
        }
    }

    @Override
    public void onPageSelected(int position) {
        for (int i = 0; i < getChildCount(); i++) {
            Marker marker = (Marker) getChildAt(i);
            if (position == i) {
                marker.setProgress(255);
            } else {
                marker.setProgress(0);
            }

        }

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onAdapterChanged(@NonNull ViewPager vp, @Nullable PagerAdapter oldAdapter, @Nullable PagerAdapter newAdapter) {
        if (vp != viewPager) {
            throw new IllegalStateException("ViewPager changed");
        }
        removeAllViews();
        for (int i = 0; i < newAdapter.getCount(); i++) {
            Marker marker = new Marker(getContext());
            LayoutParams lp = new LayoutParams(markerWH, markerWH);
            lp.setMargins(markerWH / 2, 0, markerWH / 2, 0);
            addView(marker, lp);
        }

    }


    public static class Marker extends View {

        private Paint mPaint, mBasePaint;
        private static final String BASE_COLOR = "#44000000";
        private static final String ACCENT_COLOR = "#458bff";
        public Marker(Context context) {
            this(context, null);
        }

        public Marker(Context context, AttributeSet attrs) {
            this(context, attrs, 0);
        }

        public Marker(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
            init();
        }

        private void init() {
            mPaint = new Paint();
            mPaint.setAntiAlias(true);
            mBasePaint = new Paint();
            mBasePaint.setAntiAlias(true);
            mPaint.setColor(Color.parseColor(ACCENT_COLOR));
            mBasePaint.setColor(Color.parseColor(BASE_COLOR));
        }

        public void setProgress(int progress) {
            mPaint.setAlpha(progress);
            invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            int r = Math.min(getWidth() / 2, getHeight() / 2);
            canvas.drawCircle(getWidth() / 2, getHeight() / 2, r, mBasePaint);
            canvas.drawCircle(getWidth() / 2, getHeight() / 2, r, mPaint);
        }
    }
}
