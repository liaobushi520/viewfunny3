package com.liao.viewfunny.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.support.annotation.Nullable;
import android.support.annotation.Px;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ScrollerCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Interpolator;

import com.liao.viewfunny.R;

import java.util.Arrays;


/**
 * Created by liaozhongjun on 2017/3/10.
 */

public class MagicMinLayout extends ViewGroup implements View.OnClickListener {
    public static final String TAG = MagicMinLayout.class.getSimpleName();
    private View mTopView;
    private View mBottomView;
    private View mBelowView;

    private int mBottomViewLayoutTop;
    private int mTopViewLayoutTop;
    private int mTopViewLayoutRight;

    private ScrollerCompat mScroller;
    private int touchSlop;

    private int mMode = EXPAND_MODE;
    private static final int EXPAND_MODE = 1;
    private static final int FOLD_MODE = 2;


    private float[] mInitialMotionX, mInitialMotionY, mLastMotionX, mLastMotionY;
    private int[] mOrderedPointerIds;//按先后顺序保存pointer,以便在多点触控时，找到被激活的pointer


    private int mActivePointerId;
    private static final int INVALID_POINTER = -1;

    private final WindowManager windowManager;
    private Point screenWH;//保存屏幕长宽
    float mTopViewViewScaleRadio = 0.6f;//最终mTopView的scale
    private int mTopViewRightReseted;

    private int mDragState = STATE_IDLE;
    public static final int STATE_IDLE = 0;
    public static final int STATE_DRAGGING = 1;
    public static final int STATE_SETTLING = 2;

    private int mSettleMode;
    private static final int SETTLE_DISMISS_DRAG = 1;
    private static final int SETTLE_EXPAND_OR_CLOSE = 2;

    private boolean doDimiss = false;

    //    private boolean settleByClick;
    private static final Interpolator sInterpolator = new Interpolator() {
        @Override
        public float getInterpolation(float t) {
            t -= 1.0f;
            return t * t * t * t * t + 1.0f;
        }
    };

    public MagicMinLayout(Context context) {
        this(context, null);
    }

    public MagicMinLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MagicMinLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        mScroller = ScrollerCompat.create(context, sInterpolator);
        windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        screenWH = new Point();
        windowManager.getDefaultDisplay().getSize(screenWH);
        screenWH.set(screenWH.x, screenWH.y - getStatusBarHeight(context));
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mTopView = findViewById(R.id.video);
        mBottomView = findViewById(R.id.other);
        mBelowView = findViewById(R.id.imgs_rv);
        mTopView.setVisibility(View.INVISIBLE);
        mBottomView.setVisibility(View.INVISIBLE);
    }

    public View findTopChildUnder(float x, float y) {
        final int childCount = getChildCount();
        for (int i = childCount - 1; i >= 0; i--) {
            final View child = getChildAt(i);
            if (x >= child.getLeft() && x < child.getRight()
                    && y >= child.getTop() && y < child.getBottom()) {
                return child;
            }
        }
        return null;
    }


    private void saveInitialMotion(float x, float y, int pointerid) {
        ensureMotionArraySize(pointerid);
        mInitialMotionY[pointerid] = mLastMotionY[pointerid] = y;
        mInitialMotionX[pointerid] = mLastMotionX[pointerid] = x;
        for (int i = 0; i < mOrderedPointerIds.length; i++) {
            if (pointerid == mOrderedPointerIds[i]) {
                break;
            }
            if (mOrderedPointerIds[i] == -1) {
                mOrderedPointerIds[i] = pointerid;
                break;
            }
        }

    }

    private void saveLastMotion(MotionEvent motionEvent) {
        for (int i = 0; i < motionEvent.getPointerCount(); i++) {
            int pointerId = motionEvent.getPointerId(i);
            mLastMotionY[pointerId] = motionEvent.getY(i);
            mLastMotionX[pointerId] = motionEvent.getX(i);
        }
    }

    private void ensureMotionArraySize(int pointerId) {

        if (mInitialMotionX == null || mInitialMotionX.length <= pointerId) {
            float[] imx = new float[pointerId + 1];
            float[] imy = new float[pointerId + 1];
            float[] lmx = new float[pointerId + 1];
            float[] lmy = new float[pointerId + 1];
            int[] opi = new int[pointerId + 1];
            Arrays.fill(opi, INVALID_POINTER);

            if (mInitialMotionX != null) {
                System.arraycopy(mInitialMotionX, 0, imx, 0, mInitialMotionX.length);
                System.arraycopy(mInitialMotionY, 0, imy, 0, mInitialMotionY.length);
                System.arraycopy(mLastMotionX, 0, lmx, 0, mLastMotionX.length);
                System.arraycopy(mLastMotionY, 0, lmy, 0, mLastMotionY.length);
                System.arraycopy(mOrderedPointerIds, 0, opi, 0, mOrderedPointerIds.length);
            }
            mInitialMotionX = imx;
            mInitialMotionY = imy;
            mLastMotionX = lmx;
            mLastMotionY = lmy;
            mOrderedPointerIds = opi;
        }

    }

    private void clearMotionHistory(int pointerId) {
        if (mInitialMotionX == null) {
            return;
        }
        mInitialMotionX[pointerId] = 0;
        mInitialMotionY[pointerId] = 0;
        mLastMotionX[pointerId] = 0;
        mLastMotionY[pointerId] = 0;

        for (int i = 0; i < mOrderedPointerIds.length; i++) {
            if (mOrderedPointerIds[i] == pointerId) {
                if (i <= mOrderedPointerIds.length - 1) {
                    System.arraycopy(mOrderedPointerIds, i + 1, mOrderedPointerIds, i, mOrderedPointerIds.length - i - 1);
                    break;
                }
            }
        }
        mOrderedPointerIds[mOrderedPointerIds.length - 1] = INVALID_POINTER;

    }

    private void clearMotionHistory() {
        if (mInitialMotionX == null) {
            return;
        }
        Arrays.fill(mInitialMotionX, 0);
        Arrays.fill(mInitialMotionY, 0);
        Arrays.fill(mLastMotionX, 0);
        Arrays.fill(mLastMotionY, 0);
        Arrays.fill(mOrderedPointerIds, INVALID_POINTER);
        ;
    }

    public void cancel() {
        mActivePointerId = INVALID_POINTER;
        clearMotionHistory();
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        Log.e(TAG, "onMeasure");
        setMeasuredDimension(screenWH.x, screenWH.y);
        widthMeasureSpec = MeasureSpec.makeMeasureSpec(screenWH.x, MeasureSpec.EXACTLY);

        int ph = getMeasuredHeight();
        int pw = getMeasuredWidth();
        int ppl = getPaddingLeft();
        int ppr = getPaddingRight();
        int ppt = getPaddingTop();
        int ppb = getPaddingBottom();
        LayoutParams blp = (LayoutParams) mBelowView.getLayoutParams();
        mBelowView.measure(getChildMeasureSpec(widthMeasureSpec, blp.leftMargin + blp.rightMargin, blp.width),
                getChildMeasureSpec(MeasureSpec.makeMeasureSpec(getMeasuredHeight(), MeasureSpec.EXACTLY), blp.topMargin + blp.bottomMargin, blp.height)
        );

        LayoutParams vlp = (LayoutParams) mTopView.getLayoutParams();
        mTopView.measure(
                getChildMeasureSpec(widthMeasureSpec, vlp.leftMargin + vlp.rightMargin, vlp.width)
                /*MeasureSpec.makeMeasureSpec(pw - vlp.leftMargin - vlp.rightMargin, MeasureSpec.EXACTLY)*/
                , getChildMeasureSpec(heightMeasureSpec, vlp.topMargin + vlp.bottomMargin, vlp.height)/*MeasureSpec.makeMeasureSpec(vlp.height, MeasureSpec.EXACTLY)*/);


        LayoutParams olp = (LayoutParams) mBottomView.getLayoutParams();
        int oh = getMeasuredHeight() - mTopView.getMeasuredHeight();
        mBottomView.measure(MeasureSpec.makeMeasureSpec(pw - olp.leftMargin - olp.rightMargin, MeasureSpec.EXACTLY)
                , MeasureSpec.makeMeasureSpec(oh, MeasureSpec.EXACTLY));

    }

    private boolean mFirstLayout;

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mFirstLayout = true;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mFirstLayout = false;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        Log.e(TAG, "onLayout");

        LayoutParams vlp = (LayoutParams) mTopView.getLayoutParams();
        LayoutParams olp = (LayoutParams) mBottomView.getLayoutParams();
        LayoutParams blp = (LayoutParams) mBelowView.getLayoutParams();

        int bl = getPaddingLeft() + blp.leftMargin;
        int bt = getPaddingTop() + blp.topMargin;
        mBelowView.layout(bl, bt, bl + mBelowView.getMeasuredWidth(), bt + mBelowView.getMeasuredHeight());

        if (mFirstLayout) {
            setMagicMinLayoutParams(mTopView, getPaddingLeft() + vlp.leftMargin, getPaddingTop() + vlp.topMargin, 1, 1);
        }

        mTopView.layout(vlp.left, vlp.top, vlp.left + mTopView.getMeasuredWidth(), vlp.top + mTopView.getMeasuredHeight());
        mTopView.setAlpha(vlp.alpha);
        mTopView.setScaleX(vlp.scale);
        mTopView.setScaleY(vlp.scale);

        if (mFirstLayout) {
            setMagicMinLayoutParams(mBottomView, getPaddingLeft() + olp.leftMargin, mTopView.getBottom() + olp.topMargin, 1, 1);
        }

        mBottomView.layout(olp.left, olp.top, olp.left + mBottomView.getMeasuredWidth(), olp.top + mBottomView.getMeasuredHeight());
        mBottomView.setAlpha(olp.alpha);
        mBottomView.setScaleY(olp.scale);
        mBottomView.setScaleX(olp.scale);

        if (mFirstLayout) {
            mBottomViewLayoutTop = mBottomView.getTop();
            mTopViewLayoutTop = mTopView.getTop();
            mTopViewLayoutRight = mTopView.getRight();
        }
        mFirstLayout = false;

    }

    private void setMagicMinLayoutParams(View view, int left, int top, float scale, float alpha) {
        LayoutParams lp = (LayoutParams) view.getLayoutParams();
        lp.left = left;
        lp.top = top;
        lp.scale = scale;
        lp.alpha = alpha;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final int action = MotionEventCompat.getActionMasked(ev);
        final int actionIndex = MotionEventCompat.getActionIndex(ev);

        if (MotionEvent.ACTION_DOWN == action) {
            cancel();
        }
        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                Log.e("intercept", "down");
                int x = (int) ev.getX();
                int y = (int) ev.getY();
                saveInitialMotion(x, y, ev.getPointerId(actionIndex));
            }
            break;
            case MotionEventCompat.ACTION_POINTER_DOWN: {
                final int pointerId = ev.getPointerId(actionIndex);
                final float x = ev.getX(actionIndex);
                final float y = ev.getY(actionIndex);
                Log.e("intercept", "pointer down id=" + pointerId);
                saveInitialMotion(x, y, pointerId);
                break;
            }

            case MotionEvent.ACTION_MOVE:
                Log.e("intercept", "move");
                for (int i = 0; i < ev.getPointerCount(); i++) {
                    if (mActivePointerId != INVALID_POINTER) {
                        break;
                    }
                    int pointerid = ev.getPointerId(i);
                    final int index = ev.findPointerIndex(pointerid);
                    final float y = ev.getY(index);
                    final float x = ev.getX(index);
                    if ((mTopView.getVisibility() == View.VISIBLE) && findTopChildUnder(mInitialMotionX[pointerid], mInitialMotionY[pointerid]) == mTopView && (Math.abs(ev.getY(index) - mInitialMotionY[pointerid]) > touchSlop || Math.abs(x - mInitialMotionX[pointerid]) > touchSlop)) {

                        mDragState = STATE_DRAGGING;
                        mActivePointerId = pointerid;
                        double angle = Math.abs(Math.atan((y - mInitialMotionY[pointerid]) / (x - mInitialMotionX[pointerid])));
                        mSettleMode = mMode == FOLD_MODE && angle < (Math.PI / 4) ? SETTLE_DISMISS_DRAG : 0;
                        mTopViewRightReseted = mTopView.getRight();

                        break;
                    }
                }

                saveLastMotion(ev);

                break;
            case MotionEventCompat.ACTION_POINTER_UP: {
                final int pointerId = ev.getPointerId(actionIndex);
                clearMotionHistory(pointerId);
                break;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
                Log.e("intercept", "up or cancel");
                cancel();
                break;
            }

        }
        return mDragState == STATE_DRAGGING;
    }


    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        final int action = MotionEventCompat.getActionMasked(ev);
        final int actionIndex = MotionEventCompat.getActionIndex(ev);

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                Log.e("touch", "down");
                saveInitialMotion(ev.getX(), ev.getY(), ev.getPointerId(actionIndex));
                break;
            case MotionEvent.ACTION_POINTER_DOWN: {
                int pointerId = ev.getPointerId(actionIndex);
                Log.e("touch", "pointer down ,index=" + actionIndex + " id" + pointerId);
                final float x = ev.getX(actionIndex);
                final float y = ev.getY(actionIndex);
                saveInitialMotion(x, y, pointerId);
                break;
            }
            case MotionEvent.ACTION_MOVE:
                Log.e("touch", "move");
                for (int i = 0; i < ev.getPointerCount(); i++) {
                    if (mActivePointerId != INVALID_POINTER) {//已经有活跃的pointer在进行视图拖拽
                        break;
                    }
                    int pointerid = ev.getPointerId(i);
                    final int index = ev.findPointerIndex(pointerid);
                    final float y = ev.getY(index);
                    final float x = ev.getX(index);
                    if ((mTopView.getVisibility() == View.VISIBLE) && findTopChildUnder(mInitialMotionX[pointerid], mInitialMotionY[pointerid]) == mTopView && (Math.abs(y - mInitialMotionY[pointerid]) > touchSlop || Math.abs(x - mInitialMotionX[pointerid]) > touchSlop)) {

                        mDragState = STATE_DRAGGING;
                        mActivePointerId = pointerid;

                        double angle = Math.abs(Math.atan((y - mInitialMotionY[pointerid]) / (x - mInitialMotionX[pointerid])));
                        mSettleMode = mMode == FOLD_MODE && angle < (Math.PI / 4) ? SETTLE_DISMISS_DRAG : SETTLE_EXPAND_OR_CLOSE;
                        mTopViewRightReseted = mTopView.getRight();
                        break;
                    }
                }

                if (mDragState == STATE_DRAGGING) {
                    final int index = ev.findPointerIndex(mActivePointerId);
                    if (mSettleMode == SETTLE_DISMISS_DRAG) {
                        settleDismissingView((int) (ev.getX(index) - mLastMotionX[mActivePointerId]));
                    } else {
                        Log.e("state", "draging offset=" + (int) (ev.getY(index) - mLastMotionY[mActivePointerId]));
                        int newBottomViewTop = mBottomView.getTop() + (int) (ev.getY(index) - mLastMotionY[mActivePointerId]);
                        int clampedBottomViewPos = (int) clamp(newBottomViewTop, mBottomViewLayoutTop, getHeight()) - mBottomView.getTop();
                        settleViews(clampedBottomViewPos);
                    }
                }

                saveLastMotion(ev);
                break;
            case MotionEvent.ACTION_POINTER_UP: {
                Log.e("touch", "pointer up");
                final int pointerId = ev.getPointerId(actionIndex);
                if (mDragState == STATE_DRAGGING && pointerId == mActivePointerId) {
                    int newActivePointerId = INVALID_POINTER;
                    for (int i = 0; i < mOrderedPointerIds.length; i++) {
                        if (mActivePointerId != mOrderedPointerIds[i] && mOrderedPointerIds[i] != -1) {
                            newActivePointerId = mOrderedPointerIds[i];
                            break;
                        }
                    }
                    if (newActivePointerId != INVALID_POINTER) {
                        mActivePointerId = newActivePointerId;
                        Log.e("change pointer", "new active id=" + mActivePointerId);
                    }

                }
                clearMotionHistory(pointerId);
                break;
            }
            case MotionEvent.ACTION_UP: {
                Log.e("touch", "up");
                if (STATE_DRAGGING == mDragState) {
                    int mid;
                    int finalRight;
                    if (mSettleMode == SETTLE_DISMISS_DRAG) {
                        mid = mTopViewRightReseted / 2;
                        if (mTopView.getRight() > mid) {
                            finalRight = mTopViewRightReseted;
                        } else {
                            finalRight = 0;
                            doDimiss = true;
                        }
                        mScroller.startScroll(mTopView.getLeft(), mTopView.getTop(), finalRight - mTopView.getRight(), 0);
                        mDragState = STATE_SETTLING;
                        ViewCompat.postInvalidateOnAnimation(this);
                    } else {
                        mid = (mBottomViewLayoutTop + getHeight()) / 2;
                        if (mBottomView.getTop() < mid) {
                            expandOrClose(true, false);
                        } else
                            expandOrClose(false, false);
                    }
                }
                cancel();
                break;
            }

        }
        return true;
    }



    protected void expandOrClose(boolean expand, boolean fromOuter) {
        boolean invalide = false;
        if ((mMode == FOLD_MODE || !fromOuter) && expand) {//展开
            mScroller.startScroll(mBottomView.getLeft(), mBottomView.getTop(), 0, mBottomViewLayoutTop - mBottomView.getTop());
            invalide = true;
        }
        if ((mMode == EXPAND_MODE | !fromOuter) && !expand) {//折叠
            mScroller.startScroll(mBottomView.getLeft(), mBottomView.getTop(), 0, getHeight() - mBottomView.getTop());
            invalide = true;
        }
        if (invalide) {
            mDragState = STATE_SETTLING;
            mSettleMode = SETTLE_EXPAND_OR_CLOSE;
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    public void expandOrClose(boolean expand) {
        expandOrClose(expand, true);
    }


    private void settleDismissingView(int dissmissViewOffset) {
        mTopView.offsetLeftAndRight(dissmissViewOffset);
        float alpha = 1 - (Math.abs(mTopViewRightReseted - mTopView.getRight() - 0.001f)) / mTopViewRightReseted;
        float clampedAlpha = clamp(alpha, 0, 1);
        mTopView.setAlpha(clampedAlpha);
        LayoutParams layoutParams = (LayoutParams) mTopView.getLayoutParams();
        setMagicMinLayoutParams(mTopView, mTopView.getLeft(), mTopView.getTop(), layoutParams.scale, mTopView.getAlpha());
        ViewCompat.postInvalidateOnAnimation(this);
    }

    private void settleViews(int bottomViewOffset) {
        mBottomView.offsetTopAndBottom(bottomViewOffset);
        float scaleRadio = clamp(1 - (mBottomView.getTop() - mBottomViewLayoutTop + 0.001f) / (getHeight() - mBottomViewLayoutTop) * mTopViewViewScaleRadio, mTopViewViewScaleRadio, 1);

        mBottomView.setAlpha(scaleRadio);
        setMagicMinLayoutParams(mBottomView, mBottomView.getLeft(), mBottomView.getTop(), 1f, scaleRadio);
        mTopView.setScaleX(scaleRadio);
        mTopView.setScaleY(scaleRadio);

        setBackgroundAlpha((float) (2.5 * scaleRadio - 1.5));

        int newTopViewTop = (int) (mBottomView.getTop() - mTopView.getHeight() * (.5f + scaleRadio / 2));
        int clampedTopViewTop = (int) clamp(newTopViewTop, mTopViewLayoutTop, getHeight() - mTopView.getHeight() * (.5f + scaleRadio / 2)) - mTopView.getTop();
        int clampedTopViewLeft = (int) (mTopViewLayoutRight - mTopView.getWidth() * (0.5f + scaleRadio / 2)) - mTopView.getLeft();

        mTopView.offsetTopAndBottom(clampedTopViewTop);
        mTopView.offsetLeftAndRight(clampedTopViewLeft);
        setMagicMinLayoutParams(mTopView, mTopView.getLeft(), mTopView.getTop(), scaleRadio, mTopView.getAlpha());

        ViewCompat.postInvalidateOnAnimation(this);
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (mDragState == STATE_SETTLING) {
            if (mScroller.computeScrollOffset() && !mScroller.isFinished()) {
                int newLeft = mScroller.getCurrX();
                int newTop = mScroller.getCurrY();
                if (mSettleMode == SETTLE_DISMISS_DRAG) {
                    settleDismissingView(newLeft - mTopView.getLeft());
                } else {
                    settleViews((int) clamp(newTop, mBottomViewLayoutTop, getHeight()) - mBottomView.getTop());
                }
            } else {

                if (isFolding() || mSettleMode == SETTLE_DISMISS_DRAG) {//折叠View
                    if (mSettleMode == SETTLE_DISMISS_DRAG && doDimiss) {
                        doDimiss = false;
                        mTopView.setVisibility(View.INVISIBLE);
                        mBottomView.setVisibility(View.INVISIBLE);
                        mTopView.offsetTopAndBottom(mTopViewLayoutTop - mTopView.getTop());
                        mTopView.offsetLeftAndRight(mTopViewLayoutRight - mTopView.getRight());
                        mBottomView.offsetTopAndBottom(mBottomViewLayoutTop - mBottomView.getTop());
                        setMagicMinLayoutParams(mTopView, mTopView.getLeft(), mTopView.getTop(), 1f, 1);
                        setMagicMinLayoutParams(mBottomView, mBottomView.getLeft(), mBottomView.getTop(), 1, 1);
                    }
                    setBackgroundAlpha(0);
                    mMode = FOLD_MODE;
                    mScroller.abortAnimation();
                } else {
                    mMode = EXPAND_MODE;
                    setBackgroundAlpha(1);
                    setMagicMinLayoutParams(mTopView, mTopView.getLeft(), mTopView.getTop(), 1, 1);
                    setMagicMinLayoutParams(mBottomView, mBottomView.getLeft(), mBottomViewLayoutTop, 1, 1);
                }
                mDragState = STATE_IDLE;
            }
        }
    }


    private void setBackgroundAlpha(float alpha) {
        setBackgroundColor(Color.argb((int) (alpha * 255), 0, 0, 0));
    }

    private boolean isFolding() {
        return mDragState == STATE_SETTLING && mScroller.getFinalY() == getHeight();
    }

    private float clamp(float newPos, float min, float max) {
        if (newPos <= min) {
            return min;
        } else if (newPos >= max) {
            return max;
        } else {
            return newPos;
        }
    }

    @Override
    public void onClick(View v) {
        if (mTopView.getVisibility() == View.VISIBLE) {
            expandOrClose(true);
        } else {
            int[] topViewOnWindow = new int[2];
            mTopView.getLocationInWindow(topViewOnWindow);
            int[] clickedViewOnWindow = new int[2];
            v.getLocationInWindow(clickedViewOnWindow);
            mTopView.setTranslationY(clickedViewOnWindow[1] - topViewOnWindow[1]);
            createTopViewAnimator(mTopView).start();
            mTopView.setVisibility(View.VISIBLE);
            mBottomView.setTranslationY(getHeight() - mBottomView.getTop());
            createBottomViewAnimator(mBottomView).start();
            mBottomView.setVisibility(View.VISIBLE);
        }
    }

    private ObjectAnimator createBottomViewAnimator(View view) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, View.TRANSLATION_Y, 0f);
        animator.setDuration(400);
        animator.setInterpolator(new AccelerateInterpolator());
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mMode = EXPAND_MODE;
                setBackgroundAlpha(1);
                setMagicMinLayoutParams(mTopView, mTopView.getLeft(), mTopView.getTop(), 1, 1);
                setMagicMinLayoutParams(mBottomView, mBottomView.getLeft(), mBottomViewLayoutTop, 1, 1);
                mDragState = STATE_IDLE;
            }
        });
        return animator;
    }


    private ObjectAnimator createTopViewAnimator(View view) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, View.TRANSLATION_Y, 0f);
        animator.setDuration(400);
        animator.setInterpolator(new AccelerateInterpolator());
        return animator;
    }


    public static class LayoutParams extends MarginLayoutParams {

        private int left;
        private int top;
        private float scale;
        private float alpha;


        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }

        public LayoutParams(@Px int width, @Px int height) {
            super(width, height);
        }

        public LayoutParams(MarginLayoutParams source) {
            super(source);
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }

    }

    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof MagicMinLayout.LayoutParams
                ? new MagicMinLayout.LayoutParams((MagicMinLayout.LayoutParams) p)
                : p instanceof MarginLayoutParams
                ? new LayoutParams((MarginLayoutParams) p)
                : new LayoutParams(p);
    }

    @Override
    protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams && super.checkLayoutParams(p);
    }

    @Override
    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }


    public static int getStatusBarHeight(Context context) {
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return context.getResources().getDimensionPixelSize(resourceId);
        }
        return 0;
    }
}
