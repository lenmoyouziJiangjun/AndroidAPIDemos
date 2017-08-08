package com.example.android.supportv7.widget.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.supportv7.R;

import java.util.List;

/**
 * Version 1.0
 * Created by lll on 17/8/7.
 * Description
 * copyright generalray4239@gmail.com
 */
public class AnimatorAdapter extends RecyclerView.Adapter<AnimatorAdapter.ViewHolder> {

    private Context mContext;
    private List<String> mDataSet;

    public AnimatorAdapter(Context context, List<String> dataSet) {
        mContext = context;
        mDataSet = dataSet;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.layout_animator_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
//        Picasso.with(mContext).load(R.drawable.chip).into(holder.image);
        holder.image.setImageResource(R.drawable.chip);
        holder.text.setText(mDataSet.get(position));
    }

    @Override
    public int getItemCount() {
        return mDataSet.size();
    }

    public void remove(int position) {
        mDataSet.remove(position);
        notifyItemRemoved(position);
    }

    public void add(String text, int position) {
        mDataSet.add(position, text);
        notifyItemInserted(position);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView image;
        public TextView text;

        public ViewHolder(View itemView) {
            super(itemView);
            image = (ImageView) itemView.findViewById(R.id.image);
            text = (TextView) itemView.findViewById(R.id.text);
        }
    }
}
