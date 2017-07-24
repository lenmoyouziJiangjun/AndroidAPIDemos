package com.lll.supportotherdemos.leanback;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;

import com.lll.supportotherdemos.R;
import com.lll.supportotherdemos.leanback.fragment.ErrorFragment;
import com.lll.supportotherdemos.leanback.helper.BackgroundHelper;

public class BrowseErrorActivity extends Activity {
    private ErrorFragment mErrorFragment;
    private SpinnerFragment mSpinnerFragment;

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

    private void testError() {
        mErrorFragment = new ErrorFragment();
        getFragmentManager().beginTransaction().add(R.id.main_browse_fragment, mErrorFragment).commit();

        mSpinnerFragment = new SpinnerFragment();
        getFragmentManager().beginTransaction().add(R.id.main_browse_fragment, mSpinnerFragment).commit();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (getFragmentManager().isDestroyed()) {
                    return;
                }
                getFragmentManager().beginTransaction().remove(mSpinnerFragment).commit();
                mErrorFragment.setErrorContent(getResources());
            }
        }, 3000);
    }

    public static class SpinnerFragment extends Fragment {

    }
}
