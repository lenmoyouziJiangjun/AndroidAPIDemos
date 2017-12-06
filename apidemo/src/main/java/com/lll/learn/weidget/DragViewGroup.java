package com.lll.learn.weidget;

import android.content.Context;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

/**
 * Version 1.0
 * Created by lll on 11/17/17.
 * Description
 * copyright generalray4239@gmail.com
 */
public class DragViewGroup extends FrameLayout {

    private ViewDragHelper mViewDragHelper;

    private  View mMenuView;
    private  View mContentView;


    public DragViewGroup(@NonNull Context context) {
        this(context, null);
    }

    public DragViewGroup(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DragViewGroup(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initDragViewGroup();
    }

    private void initDragViewGroup() {
        mViewDragHelper = ViewDragHelper.create(this, new MyViewDragHelperCallBack());
//        LayoutInflater.from().inflate()
    }

    /**
     *
     */
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return mViewDragHelper.shouldInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mViewDragHelper.processTouchEvent(event);
        return true;
    }

    @Override
    public void computeScroll() {
        if(mViewDragHelper.continueSettling(true)){
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }


    private class MyViewDragHelperCallBack extends ViewDragHelper.Callback {

        /**
         *
         * @param child
         * @param pointerId
         * @return true 表示可以拖动这个child
         */
        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            return mContentView == child;
        }

        /**
         * 水平方向上的滚动
         * @param child
         * @param left
         * @param dx
         * @return
         */
        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            return left;
        }

        /**
         * 垂直方向上的滚动
         * @param child
         * @param top
         * @param dy
         * @return 0，表示不滚动
         */
        @Override
        public int clampViewPositionVertical(View child, int top, int dy) {
            return 0;
        }

        /**
         * 手指释放的回调
         * @param releasedChild
         * @param xvel
         * @param yvel
         */
        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
             if(mContentView.getLeft()<200){//关闭菜单
                 mViewDragHelper.smoothSlideViewTo(mContentView,0,0);//回到起点
             }else{//打开菜单
                 mViewDragHelper.smoothSlideViewTo(mContentView,300,0);

             }
            ViewCompat.postInvalidateOnAnimation(DragViewGroup.this);
        }
    }
}
