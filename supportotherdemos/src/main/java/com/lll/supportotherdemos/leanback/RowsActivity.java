package com.lll.supportotherdemos.leanback;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v17.leanback.widget.BrowseFrameLayout;
import android.support.v17.leanback.widget.TitleHelper;
import android.support.v17.leanback.widget.TitleView;
import android.view.View;

import com.lll.supportotherdemos.R;
import com.lll.supportotherdemos.leanback.fragment.RowsFragment;

public class RowsActivity extends Activity {

    private RowsFragment mRowsFragment;
    private TitleHelper mTitleHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rows);
        mRowsFragment = (RowsFragment) getFragmentManager().findFragmentById(
                R.id.main_rows_fragment);

        setupTitleFragment();
    }

    private void setupTitleFragment() {
        TitleView titleView = (TitleView) findViewById(R.id.title);
        titleView.setTitle("Rows Fragement");
        titleView.setOnSearchClickedListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RowsActivity.this, SearchActivity.class);
                startActivity(intent);
            }
        });

        BrowseFrameLayout layout = (BrowseFrameLayout) findViewById(R.id.rows_frame);
        mTitleHelper = new TitleHelper(layout, titleView);
        layout.setOnFocusSearchListener(mTitleHelper.getOnFocusSearchListener());
        mRowsFragment.setTitleHelper(mTitleHelper);
    }
}
