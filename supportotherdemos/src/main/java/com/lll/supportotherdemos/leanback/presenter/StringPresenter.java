package com.lll.supportotherdemos.leanback.presenter;

import android.support.v17.leanback.widget.Presenter;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.TextView;

import com.lll.supportotherdemos.R;

public class StringPresenter extends Presenter {
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        Log.d("lll", "onCreateViewHolder");
        TextView tv = new TextView(parent.getContext());
        tv.setFocusable(true);
        tv.setFocusableInTouchMode(true);
        tv.setBackground(
                parent.getContext().getResources().getDrawable(R.drawable.text_bg));
        return new ViewHolder(tv);
    }

    public void onBindViewHolder(ViewHolder viewHolder, Object item) {
        Log.d("lll", "onBindViewHolder for " + item.toString());
        ((TextView) viewHolder.view).setText(item.toString());
    }

    public void onUnbindViewHolder(ViewHolder viewHolder) {
        Log.d("lll", "onUnbindViewHolder");
    }
}
