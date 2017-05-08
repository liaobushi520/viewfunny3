package com.liao.viewfunny.widget;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Interpolator;

/**
 * Created by liaozhongjun on 2017/4/13.
 */

public class ScaleButton extends android.support.v7.widget.AppCompatButton {

    private final static Interpolator INTERPOLATOR = new FastOutSlowInInterpolator();
    private static final int DURATION = 400;

    private class OnClickListenerWrapper implements OnClickListener {

        private OnClickListener mBackend;

        public OnClickListenerWrapper(OnClickListener backend) {
            mBackend = backend;
        }

        @Override
        public void onClick(View v) {
            scale();
            if (mBackend != null) {
                mBackend.onClick(v);
            }
        }
    }

    public ScaleButton(Context context) {
        this(context, null);
    }

    public ScaleButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScaleButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setOnClickListener(null);
    }

    @Override
    public void setOnClickListener(@Nullable OnClickListener l) {
        super.setOnClickListener(new OnClickListenerWrapper(l));
    }

    private void scale() {
        ObjectAnimator scaleXAnimator = ObjectAnimator.ofFloat(this, View.SCALE_X, 1.3f, 1);
        ObjectAnimator scaleYAnimator = ObjectAnimator.ofFloat(this, View.SCALE_Y, 1.3f, 1);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(scaleXAnimator, scaleYAnimator);
        animatorSet.setDuration(DURATION).setInterpolator(INTERPOLATOR);
        animatorSet.start();
    }
}
