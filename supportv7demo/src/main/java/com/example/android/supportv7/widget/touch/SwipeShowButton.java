package com.example.android.supportv7.widget.touch;

import android.graphics.Canvas;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.supportv7.Cheeses;
import com.example.android.supportv7.R;
import com.example.android.supportv7.widget.ui.SwipeContainer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Version 1.0
 * Created by lll on 17/8/8.
 * Description 滑动显示操作按钮，
 * copyright generalray4239@gmail.com
 */
public class SwipeShowButton extends AppCompatActivity {

    private RecyclerView mRecyclerView;

    private ItemTouchHelper mItemTouchHelper;
    private ItemAdapter mAdapter;

    private ItemTouchHelper.Callback mTouchCallback = new ItemTouchHelper.Callback() {
        @Override
        public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            return mTouchCallback.makeMovementFlags(0,
                    ItemTouchHelper.START | ItemTouchHelper.END);
        }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            return true;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {

        }

        @Override
        public boolean isItemViewSwipeEnabled() {
            return true;
        }

        @Override
        public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            if (Math.abs(dX) > 30 && (Math.abs(dX) > Math.abs(dY))) {
                ((ItemViewHolder) viewHolder).showOpeButton(dX);
            } else {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_touch);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mAdapter = new ItemAdapter();
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        setUpItemTouch();
    }

    private void setUpItemTouch() {
        mItemTouchHelper = new ItemTouchHelper(mTouchCallback);
        mItemTouchHelper.attachToRecyclerView(mRecyclerView);
    }


    private class ItemAdapter extends RecyclerView.Adapter<ItemViewHolder> {

        private List<String> mItems = new ArrayList<String>();

        public ItemAdapter() {
            mItems.addAll(Arrays.asList(Cheeses.sCheeseStrings));
        }

        @Override
        public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            ItemViewHolder holder = new ItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.swipe_ope_item, parent, false));
            return holder;
        }

        @Override
        public void onBindViewHolder(ItemViewHolder holder, int position) {
            holder.bind(mItems.get(position), position);
        }

        public void delete(int itemPosition) {
            mItems.remove(itemPosition);
            notifyItemRemoved(itemPosition);
        }

        @Override
        public int getItemCount() {
            return mItems.size();
        }
    }


    private class ItemViewHolder extends RecyclerView.ViewHolder {

        private TextView mTitle;
        private View mItemView;
        private SwipeContainer mContainer;

        public ItemViewHolder(View itemView) {
            super(itemView);
            mItemView = itemView;
            mTitle = (TextView) itemView.findViewById(R.id.text_view);
            mContainer = (SwipeContainer) itemView.findViewById(R.id.ll_container);
            View menu = LayoutInflater.from(itemView.getContext()).inflate(R.layout.layoutmenu, null, false);
            mContainer.setLeftMenu(menu);
            Log.e("lll", "---------mContainer width-----====" + mContainer.getMeasuredWidth());
        }


        private void bind(String title, final int itemPosition) {
            mTitle.setText(title);
        }

        private void showOpeButton(float dx) {
            final float width = mContainer.getLeftMenuWidth();
            final float dir = Math.signum(dx);
            final float overlayOffset = dx - dir * width;
//            mContainer.setTranslationX(overlayOffset);
            Log.i("lll", "---------overlayOffset ====" + overlayOffset);
            mContainer.scrollBy((int) overlayOffset/100, 0);
        }
    }


}
