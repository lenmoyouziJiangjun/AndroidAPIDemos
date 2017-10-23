package com.example.android.supportv7.widget.discreteScrollView.gallery;

import android.animation.ArgbEvaluator;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.view.View;

import com.example.android.supportv7.R;
import com.example.android.supportv7.widget.discreteScrollView.DiscreteScrollViewActivity;
import com.yarolegovich.discretescrollview.DiscreteScrollView;
import com.yarolegovich.discretescrollview.Orientation;

import java.util.List;

/**
 * Version 1.0
 * Created by lll on 17/9/8.
 * Description
 * copyright generalray4239@gmail.com
 */
public class GalleryActivity extends DiscreteScrollViewActivity implements DiscreteScrollView.ScrollListener<GalleryAdapter.ViewHolder>, DiscreteScrollView.OnItemChangedListener<GalleryAdapter.ViewHolder>, View.OnClickListener {

    private ArgbEvaluator evaluator;
    private int currentOverlayColor;
    private int overlayColor;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        evaluator = new ArgbEvaluator();
        currentOverlayColor = ContextCompat.getColor(this, R.color.galleryCurrentItemOverlay);
        overlayColor = ContextCompat.getColor(this, R.color.galleryItemOverlay);

        Gallery gallery = Gallery.get();
        List<Image> data = gallery.getData();
        DiscreteScrollView itemPicker = (DiscreteScrollView) findViewById(R.id.item_picker);
        itemPicker.setAdapter(new GalleryAdapter(data));
        itemPicker.setOrientation(Orientation.HORIZONTAL);
        itemPicker.addScrollListener(this);
        itemPicker.addOnItemChangedListener(this);
        itemPicker.scrollToPosition(1);

        findViewById(R.id.home).setOnClickListener(this);
        findViewById(R.id.fab_share).setOnClickListener(this);
    }


    private void share(View view) {
        Snackbar.make(view, R.string.msg_unsupported_op, Snackbar.LENGTH_SHORT).show();
    }

    private int interpolate(float fraction, int c1, int c2) {
        return (int) evaluator.evaluate(fraction, c1, c2);
    }

    @Override
    public void onScroll(float scrollPosition, int currentPosition, int newPosition, @Nullable GalleryAdapter.ViewHolder currentHolder, @Nullable GalleryAdapter.ViewHolder newCurrent) {
        if (currentHolder != null && newCurrent != null) {
            float position = Math.abs(currentPosition);
            currentHolder.setOverlayColor(interpolate(position, currentOverlayColor, overlayColor));
            newCurrent.setOverlayColor(interpolate(position, overlayColor, currentOverlayColor));
        }
    }

    @Override
    public void onCurrentItemChanged(@Nullable GalleryAdapter.ViewHolder viewHolder, int adapterPosition) {

    }

    @Override
    public void onClick(View v) {

    }
}
