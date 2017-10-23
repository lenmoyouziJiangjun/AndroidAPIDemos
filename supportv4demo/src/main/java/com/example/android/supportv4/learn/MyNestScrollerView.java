package com.example.android.supportv4.learn;

import android.support.v4.view.NestedScrollingParent;
import android.view.View;

public class MyNestScrollerView implements NestedScrollingParent {
    @Override
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        return false;
    }

    @Override
    public void onNestedScrollAccepted(View child, View target, int nestedScrollAxes) {

    }

    @Override
    public void onStopNestedScroll(View target) {

    }

    @Override
    public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {

    }

    @Override
    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {

    }

    @Override
    public boolean onNestedFling(View target, float velocityX, float velocityY, boolean consumed) {
        return false;
    }

    @Override
    public boolean onNestedPreFling(View target, float velocityX, float velocityY) {
        return false;
    }

    @Override
    public int getNestedScrollAxes() {
        return 0;
    }
}
