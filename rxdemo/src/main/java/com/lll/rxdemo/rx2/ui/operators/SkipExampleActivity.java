package com.lll.rxdemo.rx2.ui.operators;

import android.view.View;

import com.lll.rxdemo.rx2.ui.RxDemoBaseActivity;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class SkipExampleActivity extends RxDemoBaseActivity {
    @Override
    public void buttonClick(View view) {
        doSomeWork();
    }

    /* Using skip operator, it will not emit
    * the first 2 values.
    */
    private void doSomeWork() {
        getObservable()
                // Run on a background thread
                .subscribeOn(Schedulers.io())
                // Be notified on the main thread
                .observeOn(AndroidSchedulers.mainThread())
                .skip(2)
                .subscribe(getObserver());
    }

    private Observable<Integer> getObservable() {
        return Observable.just(1, 2, 3, 4, 5);
    }
}
