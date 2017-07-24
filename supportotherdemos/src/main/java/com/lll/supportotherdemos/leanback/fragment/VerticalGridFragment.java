package com.lll.supportotherdemos.leanback.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.OnItemViewSelectedListener;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.support.v17.leanback.widget.VerticalGridPresenter;
import android.util.Log;
import android.view.View;

import com.lll.supportotherdemos.R;
import com.lll.supportotherdemos.leanback.SearchActivity;
import com.lll.supportotherdemos.leanback.presenter.StringPresenter;

public class VerticalGridFragment extends android.support.v17.leanback.app.VerticalGridFragment {
    private static final String TAG = "VerticalGridFragment";

    private static final int NUM_COLUMNS = 3;
    private static final int NUM_ITEMS = 50;
    private static final int HEIGHT = 200;
    private static final boolean TEST_ENTRANCE_TRANSITION = true;

    private static class Adapter extends ArrayObjectAdapter {
        public Adapter(StringPresenter presenter) {
            super(presenter);
        }

        public void callNotifyChanged() {
            super.notifyChanged();
        }
    }

    private Adapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setBadgeDrawable(getResources().getDrawable(R.drawable.ic_title));
        setTitle("Leanback Vertical Grid Demo");
        setupFragment();
        if (TEST_ENTRANCE_TRANSITION) {
            // don't run entrance transition if fragment is restored.
            if (savedInstanceState == null) {
                prepareEntranceTransition();
            }
        }
        // simulates in a real world use case  data being loaded two seconds later
        new Handler().postDelayed(new Runnable() {
            public void run() {
                loadData();
                startEntranceTransition();
            }
        }, 2000);
    }

    private void loadData() {
        for (int i = 0; i < NUM_ITEMS; i++) {
            mAdapter.add(Integer.toString(i));
        }
    }

    private void setupFragment() {
        VerticalGridPresenter gridPresenter = new VerticalGridPresenter();
        gridPresenter.setNumberOfColumns(NUM_COLUMNS);
        setGridPresenter(gridPresenter);

        mAdapter = new Adapter(new StringPresenter());
        setAdapter(mAdapter);

        setOnItemViewClickedListener(new OnItemViewClickedListener() {
            @Override
            public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item, RowPresenter.ViewHolder rowViewHolder, Row row) {
                Log.i(TAG, "onItemClicked: " + item + " row " + row);
                mAdapter.callNotifyChanged();
            }
        });

        setOnItemViewSelectedListener(new OnItemViewSelectedListener() {
            @Override
            public void onItemSelected(Presenter.ViewHolder itemViewHolder, Object item, RowPresenter.ViewHolder rowViewHolder, Row row) {
                Log.i(TAG, "onItemSelected: " + item + " row " + row);
            }
        });

        setOnSearchClickedListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), SearchActivity.class);
                startActivity(intent);
            }
        });
    }

}
