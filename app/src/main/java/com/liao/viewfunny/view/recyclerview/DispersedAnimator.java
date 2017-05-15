package com.liao.viewfunny.view.recyclerview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;


import android.animation.TimeInterpolator;

import android.animation.ValueAnimator;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by liaozhongjun on 2017/5/12.
 */

public class DispersedAnimator extends DefaultItemAnimator {
    List<RecyclerView.ViewHolder> mPendingAddActions = new ArrayList<>();
    private static ViewHolderCompator sCompator = new ViewHolderCompator();
    private static TimeInterpolator sInterpolator = new FastOutSlowInInterpolator();

    private static class ViewHolderCompator implements Comparator<RecyclerView.ViewHolder> {
        @Override
        public int compare(RecyclerView.ViewHolder o1, RecyclerView.ViewHolder o2) {
            int o1P = o1.getAdapterPosition();
            int o2P = o2.getAdapterPosition();
            return o1P > o2P ? 1 : (o1P == o2P ? 0 : -1);
        }
    }

    public DispersedAnimator() {
        super();
        setAddDuration(800);
    }

    @Override
    public boolean animateAdd(RecyclerView.ViewHolder holder) {
        mPendingAddActions.add(holder);
        return true;
    }


    @Override
    public void runPendingAnimations() {
        super.runPendingAnimations();
        if (!mPendingAddActions.isEmpty()) {
            Collections.sort(mPendingAddActions, sCompator);
            int maxWidth = 0;
            for (int i = 0; i < mPendingAddActions.size(); i++) {
                RecyclerView.ViewHolder viewHolder = mPendingAddActions.get(i);
                maxWidth = Math.max(maxWidth, Math.abs(viewHolder.itemView.getWidth()));
                //viewHolder.itemView.setAlpha(0f);
            }
            final ValueAnimator valueAnimator = ValueAnimator.ofFloat(maxWidth, 0);
            final float finalMaxWidth = maxWidth;
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float animationValue = (float) animation.getAnimatedValue();
                    float alpha=Math.min(1, 1-animationValue / (finalMaxWidth));
                    for (int i = 0; i < mPendingAddActions.size(); i++) {
                        final RecyclerView.ViewHolder viewHolder = mPendingAddActions.get(i);
                        if (i % 2 == 0) {
                            viewHolder.itemView.setTranslationX(-animationValue);
                        } else {
                            viewHolder.itemView.setTranslationX(animationValue);
                        }
                       viewHolder.itemView.setAlpha(alpha);
                    }
                }
            });
            valueAnimator.setDuration(getAddDuration()).setInterpolator(sInterpolator);
            valueAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationCancel(Animator animation) {
                    for (int i = 0; i < mPendingAddActions.size(); i++) {
                        final RecyclerView.ViewHolder viewHolder = mPendingAddActions.get(i);
                        clearAnimatedValues(viewHolder.itemView);
                    }
                    mPendingAddActions.clear();
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    animation.removeListener(this);
                    for (int i = 0; i < mPendingAddActions.size(); i++) {
                        final RecyclerView.ViewHolder viewHolder = mPendingAddActions.get(i);
                        dispatchAddFinished(viewHolder);
                    }
                    if (!isRunning()) {
                        dispatchAnimationsFinished();
                    }
                    mPendingAddActions.clear();
                }

                @Override
                public void onAnimationStart(Animator animation) {
                    for (int i = 0; i < mPendingAddActions.size(); i++) {
                        final RecyclerView.ViewHolder viewHolder = mPendingAddActions.get(i);
                        dispatchAddStarting(viewHolder);
                    }
                }
            });
            valueAnimator.start();
        }

    }

    @Override
    public void endAnimation(RecyclerView.ViewHolder holder) {
        holder.itemView.animate().cancel();
        if (mPendingAddActions.remove(holder)) {
            dispatchAddFinished(holder);
            clearAnimatedValues(holder.itemView);
        }
        super.endAnimation(holder);
    }

    @Override
    public void endAnimations() {
        for (int i = mPendingAddActions.size() - 1; i >= 0; i--) {
            final RecyclerView.ViewHolder holder = mPendingAddActions.get(i);
            clearAnimatedValues(holder.itemView);
            dispatchAddFinished(holder);
            mPendingAddActions.remove(i);
        }
        super.endAnimations();
    }

    @Override
    public boolean isRunning() {
        return !mPendingAddActions.isEmpty() || super.isRunning();
    }


    private void clearAnimatedValues(final View view) {
        view.setAlpha(1f);
        view.setTranslationX(0f);
        view.setTranslationY(0f);
    }


}
