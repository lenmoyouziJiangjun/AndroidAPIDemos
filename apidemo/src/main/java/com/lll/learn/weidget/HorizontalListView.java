package com.lll.learn.weidget;

import android.content.Context;
import android.widget.AbsListView;
import android.widget.ListAdapter;

/**
 * 水平滚动的ListView
 */
public class HorizontalListView extends AbsListView {

    public HorizontalListView(Context context) {
        super(context);
    }

    @Override
    public ListAdapter getAdapter() {
        return null;
    }

    @Override
    public void setSelection(int position) {

    }
}
