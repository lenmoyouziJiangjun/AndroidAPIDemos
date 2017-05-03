package com.lll.supportotherdemos.transition;

import android.annotation.TargetApi;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.GravityCompat;
import android.transition.TransitionManager;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import com.lll.supportotherdemos.R;

public class BeginDelayedUsage extends TransitionUsageBase {
    private FrameLayout mRoot;
    private Button mButton;

    @Override
    int getLayoutResId() {
        return R.layout.begin_delayed;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRoot = (FrameLayout) findViewById(R.id.root);
        mButton = (Button) findViewById(R.id.button);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });
    }

    @TargetApi(19)
    private void toggle(){
        TransitionManager.beginDelayedTransition(mRoot);
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mButton.getLayoutParams();
        if ((params.gravity & GravityCompat.RELATIVE_HORIZONTAL_GRAVITY_MASK) == GravityCompat.END) {
            params.gravity = params.gravity ^ GravityCompat.END | GravityCompat.START;
        } else {
            params.gravity = params.gravity ^ GravityCompat.START | GravityCompat.END;
        }
        mButton.setLayoutParams(params);
    }
}
