package com.liao.viewfunny.view;

import android.animation.TimeInterpolator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;

import com.liao.viewfunny.R;
import com.liao.viewfunny.view.recyclerview.AlphabetAdapter;
import com.liao.viewfunny.widget.QuickPositioningView;

/**
 * Created by liaozhongjun on 2017/5/8.
 */

public class QuickPoaitionLayout extends FrameLayout implements QuickPositioningView.SelectedListener {
    private RecyclerView mRecyclerView;
    private QuickPositioningView mQuickPositionView;
    private static Interpolator sInterpolar = new AccelerateInterpolator();
    private Rect mTipsRect;
    private Rect mTmpRect = new Rect();
    private boolean isDrawTip = false;
    private boolean isTouching = false;
    public static final int TIP_WH = 240;
    private Paint mPaint;
    private String mDrawText;

    public QuickPoaitionLayout(@NonNull Context context) {
        this(context, null);
    }

    public QuickPoaitionLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public QuickPoaitionLayout(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mTipsRect = new Rect();
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setTextAlign(Paint.Align.CENTER);
        mPaint.setTextSize(148);
        setWillNotDraw(false);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mRecyclerView = (RecyclerView) findViewById(R.id.rv);
        mQuickPositionView = (QuickPositioningView) findViewById(R.id.qp_view);
        mQuickPositionView.setSelectedListener(this);
        mQuickPositionView.setTranslationX(getWidth() - mQuickPositionView.getLeft());
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        int action = event.getAction();
        float x = event.getX();
        float y = event.getY();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                isTouching = true;
                if (mQuickPositionView.getLeft() <= x && x <= mQuickPositionView.getRight() && mQuickPositionView.getTop() <= y &&
                        mQuickPositionView.getBottom() >= y) {
                    if (mQuickPositionView.getTranslationX() != 0) {
                        ViewCompat.animate(mQuickPositionView).translationX(0).setInterpolator(sInterpolar).setDuration(400).start();
                    }
                } else {
                    ViewCompat.animate(mQuickPositionView).translationX(getWidth() - mQuickPositionView.getLeft()).setInterpolator(sInterpolar).setDuration(400).start();
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                isTouching = false;
                isDrawTip = false;
                invalidate();
                break;
        }

        return super.onInterceptTouchEvent(event);
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        int cx = w / 2;
        int cy = h / 2;
        mTipsRect.set(cx - TIP_WH / 2, cy - TIP_WH / 2, cx + TIP_WH / 2, cy + TIP_WH / 2);
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        if (isDrawTip) {
            mPaint.setColor(Color.parseColor("#ccbdbdbd"));
            canvas.drawRect(mTipsRect, mPaint);
            mPaint.setColor(Color.parseColor("#fffafafa"));
            mPaint.getTextBounds(mDrawText, 0, 1, mTmpRect);
            canvas.drawText(mDrawText, 0, 1, mTipsRect.centerX(), mTipsRect.centerY() + mTmpRect.height() / 2, mPaint);
            isDrawTip = false;
        }
    }

    @Override
    public void onSelected(char c) {
        AlphabetAdapter alphabetAdapter = (AlphabetAdapter) mRecyclerView.getAdapter();
        int pos = alphabetAdapter.getStartPositinForChar(c);

        if (isTouching) {
            isDrawTip = true;
            mDrawText = Character.toUpperCase(c) + "";
            ViewCompat.postInvalidateOnAnimation(this);

            if (pos >= 0) {
                mRecyclerView.scrollToPosition(pos);
            }
        }
    }
}
