package com.liao.viewfunny.test;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;

import com.liao.viewfunny.utils.ViewGroupUtils;

/**
 * Created by liaozhongjun on 2017/4/18.
 */

public class TransferLayout extends FrameLayout implements RecyclerViewAnimationObserver {

    private static final Interpolator INTERPOLATOR = new AccelerateInterpolator();
    private static final RectTypeEvaluator RECT_TYPE_EVALUATOR = new RectTypeEvaluator();

    public static final String TAG = "liao";
    public static final String RECYCLER_VIEW_TAG = "recycler_view";
    public static final String EDIT_TEXT_TAG = "edit_text";

    private RecyclerView mRecyclerView;
    private EditText mEditText;

    private Paint mPaint;
    ObjectAnimator objectAnimator;

    private AnimationInfo mCurrentAnimationInfo;

    public TransferLayout(@NonNull Context context) {
        this(context, null);
    }

    public TransferLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TransferLayout(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setWillNotDraw(false);
        mCurrentAnimationInfo = new AnimationInfo();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ensureChildren();
    }

    private void ensureChildren() {
        View v = findViewWithTag(RECYCLER_VIEW_TAG);
        if (!(v instanceof RecyclerView)) {
            throw new IllegalStateException("can not find RecyclerView by tag--recycler_view");
        }
        mRecyclerView = (RecyclerView) v;
        v = findViewWithTag(EDIT_TEXT_TAG);
        if (!(v instanceof EditText)) {
            throw new IllegalStateException("can not find EditText by tag--edit_text");
        }
        mEditText = (EditText) v;
        mEditText.setMaxLines(1);

        final InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (imm.isActive() && newState != RecyclerView.SCROLL_STATE_IDLE) {
                    imm.hideSoftInputFromWindow(mEditText.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                }
            }
        });

        mEditText.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

             //   if (!imm.isActive()) {
                    mRecyclerView.scrollToPosition(mRecyclerView.getAdapter().getItemCount() - 1);
               // }

                return false;
            }
        });


        mPaint = new Paint(mEditText.getPaint());
        mPaint.setAntiAlias(true);
    }


    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        if (mPaint == null) {
            return;
        }

        if (objectAnimator != null && objectAnimator.isRunning()) {
            float r = mCurrentAnimationInfo.currentBound.height() / 2;
            mPaint.setColor(Color.parseColor("#ff000000"));
            canvas.drawText(mCurrentAnimationInfo.animationText, mCurrentAnimationInfo.currentBound.left + mCurrentAnimationInfo.extraWidth / 2, mCurrentAnimationInfo.currentBound.top + mCurrentAnimationInfo.baselineOffset, mPaint);
        }

    }

    @Override
    public void onAddStarting(RecyclerView recyclerView, RecyclerView.ViewHolder item) {

        RvTransferActivity.LiaoViewHolder viewHolder = (RvTransferActivity.LiaoViewHolder) item;
        View endView = viewHolder.chatText;
        endView.setVisibility(View.INVISIBLE);

        mCurrentAnimationInfo.endBound.set(0, 0, endView.getWidth(), endView.getHeight());
        ViewGroupUtils.offsetDescendantRect(this, endView, mCurrentAnimationInfo.endBound);
        //  Log.e(TAG, "item view rect" + mCurrentAnimationInfo.endBound.toString());
        mCurrentAnimationInfo.endView = endView;

        mCurrentAnimationInfo.startBound.set(0, 0, mEditText.getWidth(), mEditText.getHeight());
        ViewGroupUtils.offsetDescendantRect(this, mEditText, mCurrentAnimationInfo.startBound);
        Paint paint = mEditText.getPaint();
        Paint.FontMetrics fontMetrics = paint.getFontMetrics();
        mCurrentAnimationInfo.animationText = mEditText.getEditableText().toString();
        float left = mCurrentAnimationInfo.startBound.left + mEditText.getCompoundPaddingLeft();
        float top = mCurrentAnimationInfo.startBound.top + mEditText.getCompoundPaddingTop() + mEditText.getBaseline();
        float textW = paint.measureText(mCurrentAnimationInfo.animationText);
        float extraWHalf = mCurrentAnimationInfo.extraWidth / 2;
        float extraHHalf = mCurrentAnimationInfo.extraHeight / 2;
        mCurrentAnimationInfo.startBound.set(left - extraWHalf, (int) (top + fontMetrics.top - extraHHalf), left + textW + extraWHalf, (int) (top + fontMetrics.bottom + extraHHalf));
        mCurrentAnimationInfo.startView = mEditText;

        mCurrentAnimationInfo.baselineOffset = -fontMetrics.top + extraHHalf;

        mEditText.setText("");
        startTransferAnimation();
    }

    private ValueAnimator.AnimatorUpdateListener mAnimationUpdateListner = new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            invalidate();
        }
    };

    private AnimatorListenerAdapter mAnimatorListenerAdapter = new AnimatorListenerAdapter() {
        @Override
        public void onAnimationEnd(Animator animation) {
            super.onAnimationEnd(animation);
            if (mCurrentAnimationInfo.isValide()) {
                mCurrentAnimationInfo.endView.setVisibility(View.VISIBLE);
                mCurrentAnimationInfo.valide(false);
            }
        }
    };

    private void startTransferAnimation() {
        mCurrentAnimationInfo.valide(true);
        objectAnimator = ObjectAnimator.ofObject(mCurrentAnimationInfo, "currentBound", RECT_TYPE_EVALUATOR, mCurrentAnimationInfo.startBound, mCurrentAnimationInfo.endBound).setDuration(computeAnimationDuration(mCurrentAnimationInfo.startBound.left, mCurrentAnimationInfo.endBound.left));
        objectAnimator.setInterpolator(INTERPOLATOR);
        objectAnimator.addUpdateListener(mAnimationUpdateListner);
        objectAnimator.addListener(mAnimatorListenerAdapter);
        objectAnimator.start();
    }

    private int computeAnimationDuration(float start, float end) {
        int duration = (int) (Math.abs(end - start) * 400 / getHeight());
        return duration;
    }

    @Override
    public void onAddFinished(RecyclerView rv, RecyclerView.ViewHolder item) {

    }


    private static class RectTypeEvaluator implements TypeEvaluator<RectF> {

        public Float evaluate(float fraction, float startValue, float endValue) {
            float startInt = startValue;
            return startInt + fraction * (endValue - startInt);
        }

        @Override
        public RectF evaluate(float v, RectF start, RectF end) {
            start.set(evaluate(v, start.left, end.left), evaluate(v, start.top, end.top), evaluate(v, start.right, end.right), evaluate(v, start.bottom, end.bottom));
            return start;
        }
    }

    private static class AnimationInfo {
        private static final int EXTRA_WIDTH = 0;
        private static final int EXTRA_HEIGHT = 0;

        String animationText;
        float baselineOffset;
        int extraWidth = EXTRA_WIDTH;
        int extraHeight = EXTRA_HEIGHT;

        //start
        View startView;
        RectF startBound = new RectF();


        //end
        View endView;
        RectF endBound = new RectF();

        RectF currentBound = new RectF();

        private boolean isValide = false;

        public boolean isValide() {
            return isValide;
        }

        public void valide(boolean i) {
            isValide = i;
        }

        public RectF getCurrentBound() {
            return currentBound;
        }

        public void setCurrentBound(RectF mCurrentBound) {
            currentBound = mCurrentBound;
        }


    }
}
