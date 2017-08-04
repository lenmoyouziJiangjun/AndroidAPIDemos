package com.lll.rxdemo.rx2.ui.operators;

import android.util.Log;
import android.view.View;

import com.lll.rxdemo.rx2.ui.RxDemoBaseActivity;
import com.lll.rxdemo.rx2.utils.AppConstant;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class WindowExampleActivity extends RxDemoBaseActivity {
    @Override
    public void buttonClick(View view) {

    }

    /**
     * Example using window operator -> It periodically
     * subdivide items from an Observable into
     * Observable windows and emit these windows rather than
     * emitting the items one at a time
     */
    private void doSomeWork() {
        Observable.interval(1, TimeUnit.SECONDS)
                .take(12).window(3, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(getConsumer());
    }

    private Consumer<Observable<Long>> getConsumer() {
        return new Consumer<Observable<Long>>() {
            @Override
            public void accept(@NonNull Observable<Long> longObservable) throws Exception {
                mTextView.append("Sub Divide begin ....");
                mTextView.append(AppConstant.LINE_SEPARATOR);
                longObservable.subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<Long>() {
                            @Override
                            public void accept(Long value) {
                                Log.d(TAG, "Next:" + value);
                                mTextView.append("Next:" + value);
                                mTextView.append(AppConstant.LINE_SEPARATOR);
                            }
                        });
            }
        };
    }


}
