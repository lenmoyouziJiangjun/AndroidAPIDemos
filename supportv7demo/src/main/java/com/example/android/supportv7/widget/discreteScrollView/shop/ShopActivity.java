package com.example.android.supportv7.widget.discreteScrollView.shop;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.supportv7.R;
import com.example.android.supportv7.widget.discreteScrollView.DiscreteScrollViewActivity;
import com.example.android.supportv7.widget.discreteScrollView.DiscreteScrollViewOptions;
import com.yarolegovich.discretescrollview.DiscreteScrollView;
import com.yarolegovich.discretescrollview.InfiniteScrollAdapter;
import com.yarolegovich.discretescrollview.Orientation;
import com.yarolegovich.discretescrollview.transform.ScaleTransformer;

import java.util.List;

public class ShopActivity extends DiscreteScrollViewActivity implements View.OnClickListener, DiscreteScrollView.OnItemChangedListener<RecyclerView.ViewHolder> {

    private List<Item> data;
    private Shop shop;
    private TextView currentItemName;
    private TextView currentItemPrice;
    private ImageView rateItemButton;

    private DiscreteScrollView itemPicker;

    private InfiniteScrollAdapter infiniteScrollAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop);

        currentItemName = (TextView) findViewById(R.id.item_name);
        currentItemPrice = (TextView) findViewById(R.id.item_price);
        rateItemButton = (ImageView) findViewById(R.id.item_btn_rate);

        shop = Shop.get(this);
        data = shop.getData();

        findViewById(R.id.item_btn_rate).setOnClickListener(this);
        findViewById(R.id.item_btn_buy).setOnClickListener(this);
        findViewById(R.id.item_btn_comment).setOnClickListener(this);
        findViewById(R.id.home).setOnClickListener(this);
        findViewById(R.id.btn_smooth_scroll).setOnClickListener(this);
        findViewById(R.id.btn_transition_time).setOnClickListener(this);

        itemPicker = (DiscreteScrollView) findViewById(R.id.item_picker);
        itemPicker.setOrientation(Orientation.HORIZONTAL);
        itemPicker.addOnItemChangedListener(this);

        infiniteScrollAdapter = InfiniteScrollAdapter.wrap(new ShopAdapter(data));
        itemPicker.setAdapter(infiniteScrollAdapter);
        itemPicker.setItemTransitionTimeMillis(DiscreteScrollViewOptions.getTransitionTime(this));
        itemPicker.setItemTransformer(new ScaleTransformer.Builder()
                .setMinScale(0.8f)
                .build());

    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.item_btn_rate) {
            int realPosition = infiniteScrollAdapter.getRealPosition(itemPicker.getCurrentItem());
            Item current = data.get(realPosition);
            shop.setRated(current.id, !shop.isRated(current.id));
            changeRateButtonState(current);
        } else if (id == R.id.home) {
            finish();
        } else if (id == R.id.btn_transition_time) {
            DiscreteScrollViewOptions.configureTransitionTime(itemPicker, this);
        } else if (id == R.id.btn_smooth_scroll) {
            DiscreteScrollViewOptions.smoothScrollToUserSelectedPosition(itemPicker, v);
        } else {
            showUnsupportedSnackBar();
        }
    }

    private void showUnsupportedSnackBar() {
        Snackbar.make(itemPicker, R.string.msg_unsupported_op, Snackbar.LENGTH_SHORT).show();
    }

    private void onItemChanged(Item item) {
        currentItemName.setText(item.name);
        currentItemPrice.setText(item.price);
        changeRateButtonState(item);
    }

    private void changeRateButtonState(Item item) {
        if (shop.isRated(item.id)) {
            rateItemButton.setImageResource(R.drawable.ic_star_black_24dp);
            rateItemButton.setColorFilter(ContextCompat.getColor(this, R.color.shopRatedStar));
        } else {
            rateItemButton.setImageResource(R.drawable.ic_star_border_black_24dp);
            rateItemButton.setColorFilter(ContextCompat.getColor(this, R.color.shopSecondary));
        }
    }

    @Override
    public void onCurrentItemChanged(@Nullable RecyclerView.ViewHolder viewHolder, int adapterPosition) {
        int positionInDataSet = infiniteScrollAdapter.getRealPosition(adapterPosition);
        onItemChanged(data.get(positionInDataSet));
    }
}
