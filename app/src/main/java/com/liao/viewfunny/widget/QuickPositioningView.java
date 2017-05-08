package com.liao.viewfunny.widget;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by liaozhongjun on 2017/5/5.
 */

public class QuickPositioningView extends View {

    public interface SelectedListener {
         void onSelected(char c);
    }

    private static char[] CHAR_ARRAY = new char[]{'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T'
            , 'U', 'V', 'W', 'X', 'Y', 'Z'};
    float[] mPosition;
    private Paint mPaint;
    private boolean isTouching = false;
    private Rect mTmpRect;
    private int mCellH;
    private SelectedListener mListener;

    public static final float PADDING_RADIO_LR = 0.3f;
    public static final float PADDING_RADIO_TB = 0.2f;


    public QuickPositioningView(Context context) {
        this(context, null);
    }

    public QuickPositioningView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public QuickPositioningView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mPosition = new float[CHAR_ARRAY.length * 2];
        mTmpRect = new Rect();
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setTextAlign(Paint.Align.CENTER);
        mPaint.setStrokeWidth(4);
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        int paddingLR = (int) (w * PADDING_RADIO_LR);
        int paddingTB = (int) (w * PADDING_RADIO_TB);
        setPadding(paddingLR, paddingTB, paddingLR, paddingTB);
        findBestTextSize((w - getPaddingLeft() - getPaddingRight()), h - getPaddingTop() - getPaddingBottom(), mPaint);
        float left = w / 2;
        mCellH = (int) ((h - getPaddingTop() - getPaddingBottom()) / CHAR_ARRAY.length);
        int start = getPaddingTop();
        for (int i = 0; i < CHAR_ARRAY.length; i++) {
            //划算吗？
            mPaint.getTextBounds(CHAR_ARRAY, i, 1, mTmpRect);
            mPosition[2 * i] = left;
            mPosition[2 * i + 1] = start + mCellH / 2 + mTmpRect.height() / 2;
            start += mCellH;
        }
    }

    private int findPositionByTouchPoint(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        int start = 0;
        int end = getPaddingTop() + mCellH;
        for (int i = 0; i < CHAR_ARRAY.length; i++) {
            if (y > start && y < end) {
                return i;
            }
            start = end;
            if (i == CHAR_ARRAY.length - 2) {
                end = getHeight();
            } else {
                end = start + mCellH;
            }
        }
        throw new IllegalStateException("can not find point x:" + x + " y:" + y);
    }

    private void notifyListener(MotionEvent event) {
        if (mListener != null) {
            mListener.onSelected(CHAR_ARRAY[findPositionByTouchPoint(event)]);
        }
    }

    public void setSelectedListener(SelectedListener listener) {
        if (listener != null) {
            mListener = listener;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                isTouching = true;
                notifyListener(event);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                notifyListener(event);
                break;
            case MotionEvent.ACTION_UP:
                isTouching = false;
                invalidate();
                break;
        }
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (isTouching) {
            canvas.drawColor(Color.parseColor("#66bdbdbd"));
        } else {
            canvas.drawColor(Color.parseColor("#00ffffff"));
        }
        mPaint.setColor(Color.parseColor("#ff3f51b5"));
        canvas.drawPosText(CHAR_ARRAY, 0, CHAR_ARRAY.length, mPosition, mPaint);
    }

    private float findBestTextSize(int maxW, int maxH, Paint paint) {
        //fit in width
        float w = 0;
        do {
            float textSize = paint.getTextSize();
            if (maxW > w) {
                paint.setTextSize(textSize * 1.1f);
            } else {
                paint.setTextSize(textSize * 0.9f);
            }
            w = paint.measureText("w", 0, 1);
        } while (Math.abs(maxW - w) >= 1.5f);

        //fit in height

        return paint.getTextSize();


    }
}
