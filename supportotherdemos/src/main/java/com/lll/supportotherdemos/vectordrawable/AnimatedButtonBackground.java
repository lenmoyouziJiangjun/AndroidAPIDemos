package com.lll.supportotherdemos.vectordrawable;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.graphics.drawable.AnimatedVectorDrawableCompat;
import android.support.v4.view.ScrollingView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.lll.supportotherdemos.R;

import java.text.DecimalFormat;

/**
 * Version 1.0
 * Created by lll on 17/7/20.
 * Description :
 * <p>
 * https://developer.android.com/reference/android/graphics/drawable/AnimatedVectorDrawable.html
 * </p>
 * 1、animated-vector:
 * 2、vector-drawable:
 * copyright generalray4239@gmail.com
 */
public class AnimatedButtonBackground extends Activity implements View.OnClickListener {

    private static final String LOG_TAG = "TestActivity";
    private static final String LOGCAT = "VectorDrawable1";

    protected int[] icon = {
            R.drawable.animation_vector_drawable_grouping_1,
            R.drawable.animation_vector_progress_bar,
            R.drawable.btn_radio_on_to_off_bundle
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ObjectAnimator oa = new ObjectAnimator();
        ScrollView scrollView = new ScrollView(this);
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setBackgroundColor(0XFF888888);
        scrollView.addView(linearLayout);

        Resources res = getResources();

        AnimatedVectorDrawableCompat[] d = new AnimatedVectorDrawableCompat[icon.length];
        long time = SystemClock.currentThreadTimeMillis();

        for (int i = 0; i < icon.length; i++) {
            d[i] = AnimatedVectorDrawableCompat.create(this, icon[i]);
        }
        time = SystemClock.currentThreadTimeMillis() - time;

        TextView tv = new TextView(this);
        tv.setTextColor(0XFF808080);

        DecimalFormat df = new DecimalFormat("#.##");
        tv.setText("avgL=" + df.format(time / (icon.length)) + " ms");
        linearLayout.addView(tv);

        addDrawableButtons(linearLayout, d);

        //Now test constant state and mutate a bit.
        if (d[0].getConstantState() != null) {
            AnimatedVectorDrawableCompat[] copies = new AnimatedVectorDrawableCompat[3];
            copies[0] = (AnimatedVectorDrawableCompat) d[0].getConstantState().newDrawable();
            copies[1] = (AnimatedVectorDrawableCompat) d[0].getConstantState().newDrawable();
            copies[2] = (AnimatedVectorDrawableCompat) d[0].getConstantState().newDrawable();
            copies[0].setAlpha(128);

            // Expect to see the copies[0, 1] are showing alpha 128, and [2] are showing 255.
            copies[2].mutate();
            copies[2].setAlpha(255);

            addDrawableButtons(linearLayout, copies);
        }
        setContentView(scrollView);
    }

    private void addDrawableButtons(LinearLayout container, AnimatedVectorDrawableCompat[] vd) {
        for (int i = 0; i < vd.length; i++) {
            Button button = new Button(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 200);
            params.bottomMargin = 10;
            button.setLayoutParams(params);

            button.setBackground(vd[i]);
            button.setOnClickListener(this);
            container.addView(button);
        }
    }

    @Override
    public void onClick(View v) {
        AnimatedVectorDrawableCompat d = (AnimatedVectorDrawableCompat) v.getBackground();
        d.start();
    }
}
