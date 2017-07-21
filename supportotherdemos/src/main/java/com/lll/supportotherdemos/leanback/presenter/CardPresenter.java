package com.lll.supportotherdemos.leanback.presenter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v17.leanback.widget.ImageCardView;
import android.support.v17.leanback.widget.Presenter;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.ViewGroup;

import com.lll.supportotherdemos.leanback.pojo.PhotoItem;

import java.util.Random;

public class CardPresenter extends Presenter {

    private static final String TAG = "CardPresenter";

    private static final int IMAGE_HEIGHT_DP = 120;

    private static Random sRand = new Random();

    private int mRowHeight = 0;
    private int mExpandedRowHeight = 0;

    private int mCardThemeResId;
    private Context mContextThemeWrapper;

    public CardPresenter() {
        this(0);
    }

    public CardPresenter(int cardThemeResId) {
        this.mCardThemeResId = cardThemeResId;
    }

    private void setupRowHeights(Context context) {
        if (mRowHeight == 0) {
            float density = context.getResources().getDisplayMetrics().density;
            int height = (int) (IMAGE_HEIGHT_DP * density + 0.5f);

            ImageCardView cardView = new ImageCardView(context);
            cardView.setMainImageDimensions(ViewGroup.LayoutParams.WRAP_CONTENT, height);
            cardView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            mRowHeight = cardView.getMeasuredHeight();
            cardView.setActivated(true);
            cardView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            mExpandedRowHeight = cardView.getMeasuredHeight();
        }
    }

    public int getRowHeight(Context context) {
        setupRowHeights(context);
        return mRowHeight;
    }

    public int getExpandedRowHeight(Context context) {
        setupRowHeights(context);
        return mExpandedRowHeight;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        Context context = parent.getContext();
        if (mCardThemeResId != 0) {
            if (mContextThemeWrapper == null) {
                mContextThemeWrapper = new ContextThemeWrapper(context, mCardThemeResId);
            }
            context = mContextThemeWrapper;
        }
        ImageCardView cardView = new ImageCardView(context);
        cardView.setFocusable(true);
        cardView.setFocusableInTouchMode(true);
        // Randomly makes image view crop as a square or just stretch to original
        // aspect ratio.
        if (sRand.nextBoolean()) {
            cardView.setMainImageAdjustViewBounds(false);
            cardView.setMainImageDimensions(getRowHeight(parent.getContext()),
                    getRowHeight(parent.getContext()));
        } else {
            cardView.setMainImageAdjustViewBounds(true);
            cardView.setMainImageDimensions(ViewGroup.LayoutParams.WRAP_CONTENT,
                    getRowHeight(parent.getContext()));
        }

        return new ViewHolder(cardView);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, Object item) {
        PhotoItem pItem = (PhotoItem) item;
        ImageCardView cardView = (ImageCardView) viewHolder.view;
        Drawable d = cardView.getContext().getDrawable(pItem.getImageResourceId());
        cardView.setMainImage(d);
        cardView.setTitleText(pItem.getTitle());
        if (!TextUtils.isEmpty(pItem.getContent())) {
            cardView.setContentText(pItem.getContent());
        }
    }

    @Override
    public void onUnbindViewHolder(ViewHolder viewHolder) {
        Log.d(TAG, "onUnbindViewHolder");
    }
}
