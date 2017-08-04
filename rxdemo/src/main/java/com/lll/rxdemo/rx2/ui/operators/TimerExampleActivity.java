package com.lll.rxdemo.rx2.ui.operators;

import android.view.View;

import com.lll.rxdemo.rx2.ui.RxDemoBaseActivity;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class TimerExampleActivity extends RxDemoBaseActivity {
    @Override
    public void buttonClick(View view) {
        doSomeWork();
    }

    /*
     * simple example using timer to do something after 2 second
     */
    private void doSomeWork() {
        getObservable()
                // Run on a background thread
                .subscribeOn(Schedulers.io())
                // Be notified on the main thread
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(getObserver());
    }

    private Observable<? extends Long> getObservable() {
        return Observable.timer(2, TimeUnit.SECONDS);
    }
}
