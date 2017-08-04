package com.lll.rxdemo.rx2.ui.operators;

import android.view.View;

import com.lll.rxdemo.rx2.model.User;
import com.lll.rxdemo.rx2.ui.RxDemoBaseActivity;
import com.lll.rxdemo.rx2.utils.DataMock;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedules.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.BiFunction;
import io.reactivex.schedulers.Schedulers;

public class ZipExampleActivity extends RxDemoBaseActivity {
    @Override
    public void buttonClick(View view) {
        Observable.zip(getCricketFansObservable(), getFootballFansObservable(),
                new BiFunction<List<User>, List<User>, Object>() {
                    @Override
                    public Object apply(@NonNull List<User> cricketFans, @NonNull List<User> footballFans) throws Exception {
                        return DataMock.filterUserWhoLovesBoth(cricketFans, footballFans);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this.getObserver());
    }


    private Observable<List<User>> getCricketFansObservable() {
        return Observable.create(new ObservableOnSubscribe<List<User>>() {
            @Override
            public void subscribe(ObservableEmitter<List<User>> e) throws Exception {
                if (!e.isDisposed()) {
                    e.onNext(DataMock.getUserListWhoLovesCricket());
                    e.onComplete();
                }
            }
        });
    }

    private Observable<List<User>> getFootballFansObservable() {
        return Observable.create(new ObservableOnSubscribe<List<User>>() {
            @Override
            public void subscribe(ObservableEmitter<List<User>> e) throws Exception {
                if (!e.isDisposed()) {
                    e.onNext(DataMock.getUserListWhoLovesFootball());
                    e.onComplete();
                }
            }
        });
    }
}
