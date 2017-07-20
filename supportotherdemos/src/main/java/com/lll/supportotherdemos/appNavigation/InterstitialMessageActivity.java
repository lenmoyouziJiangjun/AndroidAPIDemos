package com.lll.supportotherdemos.appNavigation;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.TaskStackBuilder;
import android.view.View;

import com.lll.supportotherdemos.R;

public class InterstitialMessageActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.interstitial_message);
    }

    public void onViewContent(View v) {
        TaskStackBuilder.from(this)
                .addParentStack(ContentViewActivity.class)
                .addNextIntent(new Intent(this, ContentViewActivity.class)
                        .putExtra(ContentViewActivity.EXTRA_TEXT, "From Interstitial Notification"))
                .startActivities();
        finish();
    }
}
