package com.lll.commonlibrary.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.TextView;

import com.lll.commonutils.R;


/**
 * 可旋转的textView
 * Created by root on 15-10-19.
 */
public class RotateTextView extends TextView {
    private int degree = 0;

    public RotateTextView(Context context) {
        super(context);
    }

    public RotateTextView(Context context, int degree) {
        super(context);
        this.degree = degree;
    }

    public RotateTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.rotate_textView);
        degree = array.getInteger(R.styleable.rotate_textView_degree, 0);
        array.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(getMeasuredWidth(), getMeasuredWidth());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();
        canvas.translate(getCompoundPaddingLeft(), getExtendedPaddingTop());
        //沿着中心旋转
        canvas.rotate(degree, this.getWidth() / 2f, this.getHeight() / 2f);
        super.onDraw(canvas);
        canvas.restore();
    }


}
