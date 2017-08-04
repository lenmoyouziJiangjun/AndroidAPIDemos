package com.lll.rxdemo.rx2.ui.operators;

import android.view.View;

import com.lll.rxdemo.rx2.ui.RxDemoBaseActivity;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class TakeExampleActivity extends RxDemoBaseActivity {
    @Override
    public void buttonClick(View view) {
        doSomeWork();
    }

    /* Using take operator, it only emits
    * required number of values. here only 3 out of 5
    */
    private void doSomeWork() {
        getObservable()
                // Run on a background thread
                .subscribeOn(Schedulers.io())
                // Be notified on the main thread
                .observeOn(AndroidSchedulers.mainThread())
                .take(3)
                .subscribe(getObserver());
    }

    private Observable<Integer> getObservable() {
        return Observable.just(1, 2, 3, 4, 5);
    }
}

