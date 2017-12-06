package com.lll.learn.weidget;

import android.content.Context;
import android.support.v4.view.ViewConfigurationCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.OverScroller;
import android.widget.RelativeLayout;
import android.widget.Scroller;

/**
 * Version 1.0
 * Created by lll on 11/17/17.
 * Description
 * copyright generalray4239@gmail.com
 */
public class ScrollViewGroup extends RelativeLayout {

    private OverScroller mScroller;
//    private Scroller

    private int mTouchSlop;
    private int mMinimumVelocity;
    private int mMaximumVelocity;

    private int mOverscrollDistance;
    private int mOverflingDistance;

    public ScrollViewGroup(Context context) {
        this(context, null);
    }

    public ScrollViewGroup(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScrollViewGroup(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initGroup();
    }


    private void initGroup() {
        mScroller = new OverScroller(getContext());
        final ViewConfiguration configuration = ViewConfiguration.get(getContext());
        mTouchSlop = configuration.getScaledTouchSlop();
        mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
        mOverflingDistance = configuration.getScaledOverflingDistance();
        mOverscrollDistance = configuration.getScaledOverscrollDistance();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        int lastX = 0;
        int lastY = 0;
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                lastX = (int) event.getX();
                lastY = (int) event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                int currentX = (int) event.getX();
                int currentY = (int) event.getY();
//                scrollby(currentX, currentY);

                int offsetX = currentX-lastX;
                int offsetY = currentY - lastX;
                //方式一：
//                scrollBy(-offsetX,-offsetY);
                //方式二：
//                layout(getLeft()+offsetX,getTop()+offsetY,getRight()+offsetX,getBottom()+offsetY);
                //方式三：
                mScroller.startScroll(lastX,lastY,currentX,currentX,200);

                lastX = currentX;
                lastY = currentX;
                break;
            case MotionEvent.ACTION_UP:
                if (!mScroller.isFinished()) {
                    mScroller.abortAnimation();
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                break;

        }

        return true;
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if(mScroller.computeScrollOffset()){
            ((View)getParent()).scrollTo(mScroller.getCurrX(),mScroller.getCurrY());
            postInvalidate();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

        View child = getChildAt(1);
        int left = (b - l) / 2 - child.getMeasuredWidth() / 2;
        int top = (b - t) / 2 - child.getMeasuredHeight() / 2;
        child.layout(left, top, left + child.getMeasuredWidth(), top + child.getMeasuredHeight());
    }


}
