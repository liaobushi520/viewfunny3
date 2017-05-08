package com.liao.viewfunny.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pools;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.EditText;
import android.widget.FrameLayout;

import com.liao.viewfunny.utils.ViewGroupUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liaozhongjun on 2017/4/17.
 */

public class DeleteFlyEditText extends FrameLayout {

    public static final String TAG = "edit";

    private static final Interpolator LINEAR_INTERPOLATOR = new LinearInterpolator();
    private static final Interpolator ACCELERATE_INTERPOLATOR = new AccelerateInterpolator();
    private static final Interpolator ACCELERATE_DECELERATE_INTERPOLATOR = new AccelerateDecelerateInterpolator();
    private EditText mEditText;

    private Rect mSouceRect;
    private Rect mTmpRect;

    private Paint mTextPaint;

    private List<AnimationInfo> mCurrentAnimationInfos;


    public DeleteFlyEditText(@NonNull Context context) {
        this(context, null);
    }

    public DeleteFlyEditText(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DeleteFlyEditText(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setWillNotDraw(false);
        mSouceRect = new Rect();
        mTmpRect = new Rect();

        mCurrentAnimationInfos = new ArrayList<>();


    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        if (getChildCount() > 0) {
            throw new IllegalStateException("only have on child");
        }

        if (!(child instanceof EditText)) {
            throw new IllegalStateException("child must be instance if EditText");
        }
        LayoutParams layoutParams = new LayoutParams(params);
        layoutParams.gravity = Gravity.CENTER;

        super.addView(child, index, layoutParams);

        mEditText = (EditText) child;
        mEditText.setMaxLines(1);
        mTextPaint = new Paint(mEditText.getPaint());
        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                Log.e(TAG, "s=" + s + "  start=" + start + "   count=" + count + "  after=" + after);
                if (count == 0) {// add ,not delete
                    return;
                }
                Paint paint = mEditText.getPaint();

                mTmpRect.set(0, 0, mEditText.getWidth(), mEditText.getHeight());
                ViewGroupUtils.offsetDescendantRect(DeleteFlyEditText.this, mEditText, mTmpRect);

                int left = (int) (mTmpRect.left + mEditText.getCompoundPaddingLeft() + paint.measureText(s, 0, start));
                int top = mTmpRect.top + mEditText.getCompoundPaddingTop();

                mSouceRect.setEmpty();
                paint.getTextBounds(s.toString(), start, start + count, mSouceRect);
                mSouceRect.set(left, top, mSouceRect.width() + left, mSouceRect.height() + top);

                if (count == 1 && after == 0) {//delete one char
                    startSingleCharDeletedAnimation(AnimationInfo.obtain(mSouceRect, s.charAt(start) + "", AnimationInfo.SINGLE_ANIMATION));
                } else {
                    startMultTextDeletedAnimation();
                }

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {


            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        if (mTextPaint == null) {
            return;
        }
        for (AnimationInfo info : mCurrentAnimationInfos) {
            canvas.save();
            canvas.rotate(info.rorate, info.baselineX, info.baselineY);
            canvas.drawText((String) info.text, info.baselineX, info.baselineY, mTextPaint);
            canvas.restore();
        }


    }


    private ValueAnimator.AnimatorUpdateListener mAnimationUpdateListner = new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            invalidate();
        }
    };

    private void startSingleCharDeletedAnimation(final AnimationInfo animationInfo) {

        ObjectAnimator flyX = ObjectAnimator.ofFloat(animationInfo, "baselineX", animationInfo.startBound.left, animationInfo.startBound.left - animationInfo.startBound.width() * 4);
        flyX.setInterpolator(LINEAR_INTERPOLATOR);
        flyX.addUpdateListener(mAnimationUpdateListner);

        ObjectAnimator dropY = ObjectAnimator.ofFloat(animationInfo, "baselineY", mEditText.getBaseline(), mEditText.getBaseline() + getHeight());
        dropY.setInterpolator(ACCELERATE_INTERPOLATOR);
        dropY.addUpdateListener(mAnimationUpdateListner);

        ObjectAnimator rorate = ObjectAnimator.ofFloat(animationInfo, "rorate", 0, -90);
        rorate.setInterpolator(ACCELERATE_DECELERATE_INTERPOLATOR);
        rorate.addUpdateListener(mAnimationUpdateListner);

        AnimatorSet set = new AnimatorSet();
        set.playTogether(flyX, dropY, rorate);
        set.setDuration(400);

        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationCancel(Animator animation) {
                mCurrentAnimationInfos.remove(animationInfo);
                AnimationInfo.release(animationInfo);
                super.onAnimationCancel(animation);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mCurrentAnimationInfos.remove(animationInfo);
                AnimationInfo.release(animationInfo);
                super.onAnimationEnd(animation);
            }
        });
        mCurrentAnimationInfos.add(animationInfo);
        set.start();


    }

    private void startMultTextDeletedAnimation() {

    }



    private static class AnimationInfo {
        public static final int SINGLE_ANIMATION = 1;
        public static final int MULTI_ANIMATION = 2;

        Rect startBound;
        CharSequence text;
        int type;

        float baselineX;
        float baselineY;
        float rorate;

        public AnimationInfo(Rect rect, CharSequence s, int type) {
            startBound = rect;
            text = s;
            this.type = type;
        }


        public void setBaselineX(float baseline) {
            this.baselineX = baseline;
        }

        public float getBaselineX() {
            return baselineX;
        }

        public float getBaselineY() {
            return baselineY;
        }

        public void setBaselineY(float baselineY) {
            this.baselineY = baselineY;
        }

        public float getRorate() {
            return rorate;
        }

        public void setRorate(float rorate) {
            this.rorate = rorate;
        }

        private static final Pools.SynchronizedPool<AnimationInfo> sPool =
                new Pools.SynchronizedPool<AnimationInfo>(5);


        public static AnimationInfo obtain(Rect rect, CharSequence s, int type) {
            AnimationInfo animationInfo = sPool.acquire();
            if (animationInfo == null) {
                animationInfo = new AnimationInfo(rect, s, type);
            } else {
                animationInfo.startBound = rect;
                animationInfo.text = s;
                animationInfo.type = type;
            }
            return animationInfo;
        }

        public static void release(AnimationInfo animationInfo) {
            sPool.release(animationInfo);
        }
    }


}
