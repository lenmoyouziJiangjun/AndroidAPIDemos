package com.lll.rxdemo.rx2.ui;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.lll.rxdemo.R;
import com.lll.rxdemo.rx2.utils.AppConstant;

import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;

public abstract class RxDemoBaseActivity extends Activity {
    public Button mButton;
    public TextView mTextView;
    public static final String TAG = "lll";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rx_example);
        mButton = (Button) findViewById(R.id.btn);
        mTextView = (TextView) findViewById(R.id.textView);

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonClick(v);
            }
        });
    }

    public abstract void buttonClick(View view);


    public <T> Observer<T> getObserver() {
        return new Observer<T>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {
                Log.d(TAG, " onSubscribe : " + d.isDisposed());
            }

            @Override
            public void onNext(@NonNull T t) {
                mTextView.append(" onNext : ");
                mTextView.append(AppConstant.LINE_SEPARATOR);
                Log.d(TAG, " onNext : value : " + t);
                doOtherNext(t);
            }

            @Override
            public void onError(@NonNull Throwable e) {
                mTextView.append(" onError : " + e.getMessage());
                mTextView.append(AppConstant.LINE_SEPARATOR);
                Log.d(TAG, " onError : " + e.getMessage());
            }

            @Override
            public void onComplete() {
                mTextView.append(" onComplete");
                mTextView.append(AppConstant.LINE_SEPARATOR);
                Log.d(TAG, " onComplete");
            }
        };
    }

    public <T> void doOtherNext(T t) {

    }
}
