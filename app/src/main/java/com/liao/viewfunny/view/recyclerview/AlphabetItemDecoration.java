package com.liao.viewfunny.view.recyclerview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by liaozhongjun on 2017/5/5.
 */

public class AlphabetItemDecoration extends DividerItemDecoration {

    public static final int CHAR_DECORATION_HEIGHT = 48;
    private static final int LEFT_PADDING = 36;

    private Rect mBounds = new Rect();
    private Rect mTmpRect = new Rect();
    private Paint mPaint;

    public AlphabetItemDecoration(Context context, int orientation) {
        super(context, orientation);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setTextSize(42);
        mPaint.setTextAlign(Paint.Align.CENTER);
    }

    @Override
    public void onDraw(Canvas canvas, RecyclerView parent, RecyclerView.State state) {
        super.onDraw(canvas, parent, state);

        AlphabetAdapter adapter = (AlphabetAdapter) parent.getAdapter();
        for (int i = 0; i < parent.getChildCount(); i++) {
            View child = parent.getChildAt(i);
            int adpterPos = parent.getChildAdapterPosition(child);
            char firstChar = adapter.getItem(adpterPos).mKey.charAt(0);
            if (adpterPos == adapter.getStartPositinForChar(firstChar)) {
                parent.getDecoratedBoundsWithMargins(child, mBounds);
                mPaint.setColor(Color.WHITE);
                canvas.drawRect(mBounds.left, mBounds.top, mBounds.right, child.getTop(), mPaint);
                mPaint.setColor(Color.BLACK);
                String s = Character.toUpperCase(firstChar) + "";
                mPaint.getTextBounds(s, 0, 1, mTmpRect);
                canvas.drawText(s, 0, 1, LEFT_PADDING + mBounds.left, (child.getTop() + mBounds.top + mTmpRect.height()) / 2, mPaint);
            }
        }

    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDrawOver(c, parent, state);
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        int adapterPosition = parent.getChildAdapterPosition(view);
        AlphabetAdapter adapter = (AlphabetAdapter) parent.getAdapter();
        if (adapterPosition == adapter.getStartPositinForChar(adapter.getItem(adapterPosition).mKey.charAt(0))) {
            outRect.set(outRect.left, outRect.top + CHAR_DECORATION_HEIGHT, outRect.right, outRect.bottom);
        }
    }
}
