package com.lll.rxdemo.rx2.ui.operators;

import android.view.View;

import com.lll.rxdemo.rx2.ui.RxDemoBaseActivity;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedules.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class DelayExampleActivity extends RxDemoBaseActivity {
    @Override
    public void buttonClick(View view) {
        doSomeWork();
    }

    private void doSomeWork() {
        getObservable().delay(3000, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this.<String>getObserver());
    }

    private Observable<String> getObservable() {
        return Observable.just("Amit");
    }

}
