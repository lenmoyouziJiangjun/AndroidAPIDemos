package com.lll.supportotherdemos.leanback;

import android.app.Activity;
import android.os.Bundle;

import com.lll.supportotherdemos.R;
import com.lll.supportotherdemos.leanback.fragment.OnboardingDemoFragment;

public class OnboardingActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_framelayout);
        getFragmentManager().beginTransaction().add(R.id.fl_container, new OnboardingDemoFragment());
    }
}
