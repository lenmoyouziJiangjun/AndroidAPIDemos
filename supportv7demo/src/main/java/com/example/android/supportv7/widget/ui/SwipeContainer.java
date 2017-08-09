package com.example.android.supportv7.widget.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;

/**
 * Version 1.0
 * Created by lll on 17/8/8.
 * Description 滑动显示menu
 * copyright generalray4239@gmail.com
 */
public class SwipeContainer extends FrameLayout {

    private View mLeftMenu;

    private View mRightMenu;

    private View mContainerView;

    /*滑动方向*/
    private static final int SWIPE_LEFT = 1;
    private static final int SWIPE_RIGHT = 2;
    private static final int SWIPE_BOTH = 3;

    /*当前滑动方向*/
    private int current_swipe;

    private int mScaledTouchSlop;


    public SwipeContainer(Context context) {
        super(context, null);
    }

    public SwipeContainer(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
    }

    public SwipeContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mScaledTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();


    }


    public void setLeftMenu(View view) {
        mLeftMenu = view;
        this.addView(mLeftMenu);
    }

    public void setRightMenu(View view) {
        mRightMenu = view;
        this.addView(mRightMenu);
    }

    public float getLeftMenuWidth() {
        if (mLeftMenu != null) {
            return mLeftMenu.getMeasuredWidth();
        }
        return 0;
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int size = getChildCount();
        for (int i = 0; i < size; i++) {
            View child = getChildAt(i);
            if (mLeftMenu != null && child == mLeftMenu) {
//               measureChild(mLeftMenu,);
            } else if (mRightMenu != null && child == mRightMenu) {

            } else {
                super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            }
        }

    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

        int size = getChildCount();
        for (int i = 0; i < size; i++) {
            View child = getChildAt(i);
            if (mLeftMenu != null && child == mLeftMenu) {
                MarginLayoutParams marginParams = (MarginLayoutParams) mLeftMenu.getLayoutParams();
                int left = mLeftMenu.getMeasuredWidth() + marginParams.rightMargin + marginParams.rightMargin;
                mLeftMenu.layout(-left, t, 0, b);
            } else if (mRightMenu != null && child == mRightMenu) {
                MarginLayoutParams marginParams = (MarginLayoutParams) mRightMenu.getLayoutParams();
                int right = mRightMenu.getMeasuredWidth() + marginParams.rightMargin + marginParams.rightMargin;
                mRightMenu.layout(l, t, right, b);
            } else {
                super.onLayout(changed, l, t, r, b);
            }

        }

    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }

    /**
     * 滑动
     *
     * @param dx
     */
    private void swipe(float dx) {
        this.setTranslationX(dx);
        postInvalidate();
    }
}
