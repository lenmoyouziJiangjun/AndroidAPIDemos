package com.lll.rxdemo.rx2.ui.rxbus;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

/**
 * Version 1.0
 * Created by lll on 17/8/3.
 * Description
 * copyright generalray4239@gmail.com
 */
public class RxBus {

    public RxBus() {
    }

    private PublishSubject<Object> bus = PublishSubject.create();

    public void send(Object obj) {
        bus.onNext(obj);
    }

    public Observable<Object> toObservable() {
        return bus;
    }

    public boolean hasObservers() {
        return bus.hasObservers();
    }


}
