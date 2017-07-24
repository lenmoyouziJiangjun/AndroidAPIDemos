package com.lll.supportotherdemos.leanback;

import android.app.Activity;
import android.os.Bundle;

import com.lll.supportotherdemos.R;
import com.lll.supportotherdemos.leanback.fragment.DetailsFragment;
import com.lll.supportotherdemos.leanback.fragment.NewDetailsFragment;
import com.lll.supportotherdemos.leanback.helper.BackgroundHelper;
import com.lll.supportotherdemos.leanback.pojo.PhotoItem;


/**
 * Version 1.0
 * Created by lll on 17/7/21.
 * Description
 * copyright generalray4239@gmail.com
 */
public class DetailsActivity extends Activity {

    public static final String EXTRA_ITEM = "item";
    public static final String SHARED_ELEMENT_NAME = "hero";

    private boolean useLegacyFragment() {
        return (DetailsPresenterSelectionActivity.USE_LEGACY_PRESENTER
                && !(this instanceof SearchDetailsActivity));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(useLegacyFragment() ? R.layout.activity_legacy_details : R.layout.activity_details);
        // Only pass object to fragment when activity is first time created,
        // later object is modified and persisted with fragment state.
        if (useLegacyFragment()) {
            ((DetailsFragment) getFragmentManager().findFragmentById(R.id.details_fragment))
                    .setItem((PhotoItem) getIntent().getParcelableExtra(EXTRA_ITEM));
        } else {
            ((NewDetailsFragment) getFragmentManager().findFragmentById(R.id.details_fragment))
                    .setItem((PhotoItem) getIntent().getParcelableExtra(EXTRA_ITEM));
        }
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        BackgroundHelper.attach(this);
    }

    @Override
    public void onStop() {
        BackgroundHelper.release(this);
        super.onStop();
    }
}
