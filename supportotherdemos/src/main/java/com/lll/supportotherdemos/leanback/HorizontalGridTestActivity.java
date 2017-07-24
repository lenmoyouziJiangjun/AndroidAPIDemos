package com.lll.supportotherdemos.leanback;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v17.leanback.widget.HorizontalGridView;
import android.support.v17.leanback.widget.HorizontalHoverCardSwitcher;
import android.support.v17.leanback.widget.OnChildSelectedListener;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.lll.supportotherdemos.R;

public class HorizontalGridTestActivity extends Activity {
    private static final String TAG = "HorizontalGrid";
    private static final boolean DEBUG = true;
    private static final String SELECT_ACTION = "android.test.leanback.widget.SELECT";
    private static final int NUM_ITEMS = 100;
    private static final boolean STAGGERED = true;

    private HorizontalGridView mHorizontalGridView;
    private int mScrollState = RecyclerView.SCROLL_STATE_IDLE;

    private RecyclerView.OnScrollListener mScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            final String[] stateNames = {"IDLE", "DRAGGING", "SETTLING"};
            Log.v(TAG, "onScrollStateChanged "
                    + (newState < stateNames.length ? stateNames[newState] : newState));
            mScrollState = newState;
        }
    };

    private View.OnFocusChangeListener mItemFocusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus) {
                v.setBackgroundColor(Color.YELLOW);
            } else {
                v.setBackgroundColor(Color.LTGRAY);
            }
        }
    };

    private View.OnClickListener mItemClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mHorizontalGridView.getAdapter().notifyDataSetChanged();
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RecyclerView.Adapter adapter = new MyAdapter();
        View view = createView();
        mHorizontalGridView.setAdapter(adapter);
        setContentView(view);
        mHorizontalGridView.setOnScrollListener(mScrollListener);
    }

    private View createView() {
        View view = getLayoutInflater().inflate(R.layout.horizontal_grid, null, false);
        mHorizontalGridView = (HorizontalGridView) view.findViewById(R.id.gridview);
        mHorizontalGridView.setWindowAlignment(HorizontalGridView.WINDOW_ALIGN_BOTH_EDGE);
        mHorizontalGridView.setWindowAlignmentOffsetPercent(35);
        mHorizontalGridView.setOnChildSelectedListener(new OnChildSelectedListener() {
            @Override
            public void onChildSelected(ViewGroup parent, View view, int position, long id) {
                Log.d(TAG, "onChildSelected position=" + position + " id=" + id);
            }
        });
        return view;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Log.v(TAG, "onNewIntent " + intent);
        if (intent.getAction().equals(SELECT_ACTION)) {
            int position = intent.getIntExtra("SELECT_POSITION", -1);
            if (position >= 0) {
                mHorizontalGridView.setSelectedPosition(position);
            }
        }
        super.onNewIntent(intent);
    }


    class MyAdapter extends RecyclerView.Adapter<MyViewHolder> {

        private int[] mItemLengths;

        public MyAdapter() {
            mItemLengths = new int[NUM_ITEMS];
            for (int i = 0; i < mItemLengths.length; i++) {
                mItemLengths[i] = STAGGERED ? (int) (Math.random() * 180) + 180 : 240;
            }
        }


        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            TextView textView = new TextView(parent.getContext());
            textView.setTextColor(Color.BLACK);
            textView.setFocusable(true);
            textView.setFocusableInTouchMode(true);
            textView.setOnFocusChangeListener(mItemFocusChangeListener);
            textView.setOnClickListener(mItemClickListener);
            return new MyViewHolder(textView);
        }

        @Override
        public void onBindViewHolder(MyViewHolder baseHolder, int position) {
            MyViewHolder holder = (MyViewHolder) baseHolder;
            ((TextView) holder.itemView).setText("Item " + position);
            holder.itemView.setBackgroundColor(Color.LTGRAY);
            int margin = mItemLengths[position];
            holder.itemView.setLayoutParams(new ViewGroup.MarginLayoutParams(margin,
                    80));
        }

        @Override
        public int getItemCount() {
            return mItemLengths.length;
        }
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {

        public MyViewHolder(View v) {
            super(v);
        }
    }
}
