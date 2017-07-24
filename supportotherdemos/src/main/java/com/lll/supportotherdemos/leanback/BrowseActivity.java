package com.lll.supportotherdemos.leanback;

import android.app.Activity;
import android.os.Bundle;

import com.lll.supportotherdemos.R;
import com.lll.supportotherdemos.leanback.helper.BackgroundHelper;

/**
 * Version 1.0
 * Created by lll on 17/7/21.
 * Description
 * copyright generalray4239@gmail.com
 */
public class BrowseActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse);
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        BackgroundHelper.attach(this);
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        BackgroundHelper.release(this);
    }
}
