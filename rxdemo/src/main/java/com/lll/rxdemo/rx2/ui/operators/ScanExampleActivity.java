package com.lll.rxdemo.rx2.ui.operators;

import android.view.View;

import com.lll.rxdemo.rx2.ui.RxDemoBaseActivity;

import io.reactivex.Observable;
import io.reactivex.android.schedules.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.BiFunction;
import io.reactivex.schedulers.Schedulers;

public class ScanExampleActivity extends RxDemoBaseActivity {
    @Override
    public void buttonClick(View view) {
        doSomeWork();
    }

    private void doSomeWork() {
        getObservable().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .scan(new BiFunction<Integer, Integer, Integer>() {
                    @Override
                    public Integer apply(@NonNull Integer integer, @NonNull Integer integer2) throws Exception {
                        return integer + integer2;
                    }
                })
                .subscribe(this.<Integer>getObserver());
    }


    private Observable<Integer> getObservable() {
        return Observable.just(1, 2, 3, 4, 5);
    }
}
