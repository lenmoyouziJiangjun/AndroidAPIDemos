package com.lll.anim.animation;

import android.view.animation.Animation;
import android.view.animation.Transformation;

/**
 * Version 1.0
 * Created by lll on 12/6/17.
 * Description
 *   自定义旋转animation，主要是度速的旋转，相比与xml可以自定义度速。
 * copyright generalray4239@gmail.com
 */
public class MyRotateAnimation extends Animation {

    /*起始度数*/
    private float mFromDegrees;
    private float mToDegrees;

    /*中心点*/
    private float mPivotX;
    private float mPivotY;

    /*x坐标类型
       One of Animation.ABSOLUTE, Animation.RELATIVE_TO_SELF, or Animation.RELATIVE_TO_PARENT.
    和值*/
    private int mPivotXType;
    private float mPivotXValue;

    private int mPivotYType;
    private float mPivotYValue;

    private boolean mCancelled;

    private float mDegree;


    /**
     * 代码定义的动画属性
     * @param fromDegrees
     * @param toDegrees
     * @param pivotXType
     * @param pivotXValue
     * @param pivotYType
     * @param pivotYValue
     */
    public MyRotateAnimation(float fromDegrees, float toDegrees, int pivotXType, float pivotXValue,
                             int pivotYType, float pivotYValue) {
        mFromDegrees = fromDegrees;
        mToDegrees = toDegrees;
        mPivotXType = pivotXType;
        mPivotXValue = pivotXValue;
        mPivotYType = pivotYType;
        mPivotYValue = pivotYValue;
        mCancelled = false;
        mDegree = fromDegrees;
    }

    public void initialize(int width, int height, int parentWidth, int parentHeight) {
        super.initialize(width, height, parentWidth, parentHeight);
        mPivotX = resolveSize(mPivotXType, mPivotXValue, width, parentWidth);
        mPivotY = resolveSize(mPivotYType, mPivotYValue, height, parentHeight);
    }

    public float getDegree() {
        return mDegree;
    }

    @Override
    public void cancel() {
        super.cancel();
        mCancelled = true;
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        if (!mCancelled) {
            mDegree = mFromDegrees + ((mToDegrees - mFromDegrees) * interpolatedTime);
        }
        t.getMatrix().setRotate(mDegree, mPivotX, mPivotY);
    }
}
