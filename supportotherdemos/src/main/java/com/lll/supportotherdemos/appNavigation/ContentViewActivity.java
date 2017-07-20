package com.lll.supportotherdemos.appNavigation;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.view.MenuItem;
import android.widget.TextView;

import com.lll.supportotherdemos.R;

public class ContentViewActivity extends Activity {

    public static final String EXTRA_TEXT = "com.example.android.appnavigation.EXTRA_TEXT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_view);
        ActionBarCompat.setDisplayHomeAsUpEnabled(this, true);

        Intent intent = getIntent();
        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            TextView tv = (TextView) findViewById(R.id.status_text);
            tv.setText("Viewing content from ACTION_VIEW");
        } else if (intent.hasExtra(EXTRA_TEXT)) {
            TextView tv = (TextView) findViewById(R.id.status_text);
            tv.setText(intent.getStringExtra(EXTRA_TEXT));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Intent upIntent = NavUtils.getParentActivityIntent(this);
            if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
                TaskStackBuilder.from(this).addParentStack(this)
                        .startActivities();
            } else {
                NavUtils.navigateUpTo(this, upIntent);
            }
        }
        return super.onOptionsItemSelected(item);
    }
}
