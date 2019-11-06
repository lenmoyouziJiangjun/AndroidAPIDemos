package com.lll.anim.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import com.lll.anim.animation.MyRotateAnimation;

/**
 * Description:可旋转的imageView
 * Version:
 * Created by lll on 2016/2/2.
 * CopyRight lll
 */
public class RotateImageView extends ImageView {
    MyRotateAnimation mAnimation;

    public RotateImageView(Context context) {
        super(context);
        mAnimation = null;
    }

    public RotateImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mAnimation = null;
    }

    public RotateImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mAnimation = null;
    }

    private void initAnimation(long duration, boolean isForward, int repeatCount) {
        LinearInterpolator lir = new LinearInterpolator();
        float from;
        float to;
        if (isForward) {
            from = (mAnimation == null || mAnimation.getRepeatCount() != Animation.INFINITE) ? 0.0f
                    : mAnimation.getDegree();
            to = from + 360.0f;
        } else {
            from = (mAnimation == null || mAnimation.getRepeatCount() != Animation.INFINITE) ? 360.0f
                    : mAnimation.getDegree();
            to = from - 360.0f;
        }

        if (isForward) {
            mAnimation = new MyRotateAnimation(from, to, Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f);
        } else {
            mAnimation = new MyRotateAnimation(from, to, Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f);
        }
        mAnimation.setDuration(duration);
        mAnimation.setRepeatMode(Animation.RESTART);
        mAnimation.setRepeatCount(repeatCount);
        mAnimation.setInterpolator(lir);
    }

    public void startAnimation(long duration, boolean isForward) {
        startAnimation(duration, isForward, Animation.INFINITE);
    }

    public void startAnimation(long duration, boolean isForward, int repeatCount) {
        initAnimation(duration, isForward, repeatCount);
        startAnimation(mAnimation);
    }

    public void stopAnimation() {
        if (mAnimation != null) {
            mAnimation.cancel();
            mAnimation = null;
            clearAnimation();
        }
    }
}
