package com.lll.rxdemo.rx2.ui.rxbus;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lll.rxdemo.rx2.model.Events;

import io.reactivex.android.schedules.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class RxBusActivity extends AppCompatActivity {

    private final CompositeDisposable mDisposable = new CompositeDisposable();

    private final RxBus mRxBus = new RxBus();

    private TextView mTextView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(initView());
        initDisposable();
    }

    private View initView() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        layout.setLayoutParams(params);
        layout.setHorizontalGravity(Gravity.CENTER);
        layout.setPadding(12, 20, 12, 20);
        mTextView = new TextView(this);
        mTextView.setTextSize(20);
        mTextView.setTextColor(Color.RED);
        ViewGroup.LayoutParams p = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 200);

        mTextView.setLayoutParams(p);
        mTextView.setGravity(Gravity.CENTER);
        mTextView.setBackgroundColor(Color.BLUE);
        mTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRxBus.send(new Events.TapEvent());
            }
        });

        layout.addView(mTextView);

        return layout;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDisposable.clear();
    }

    private void initDisposable() {
        mDisposable.add(mRxBus.toObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(@NonNull Object object) throws Exception {
                        if (object instanceof Events.AutoEvent) {
                            mTextView.setText("Auto Event Received");
                        } else if (object instanceof Events.TapEvent) {
                            mTextView.setText("Tap Event Received");
                        }
                    }
                }));
    }


}
