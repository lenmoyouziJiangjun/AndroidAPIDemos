package com.lll.supportotherdemos.leanback.fragment;

import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.lll.supportotherdemos.R;

public class ErrorFragment extends android.support.v17.leanback.app.ErrorFragment {
    private static final String TAG = "leanback.ErrorFragment";
    private static final boolean TRANSLUCENT = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        setTitle("Leanback Sample App");
    }

    public void setErrorContent(Resources resources) {
        setImageDrawable(resources.getDrawable(R.drawable.lb_ic_sad_cloud));
        setMessage("An error occurred.");
        setDefaultBackground(TRANSLUCENT);

        setButtonText("Dismiss");
        setButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Log.i(TAG, "button clicked");
                getFragmentManager().beginTransaction().remove(ErrorFragment.this).commit();
            }
        });
    }
}
