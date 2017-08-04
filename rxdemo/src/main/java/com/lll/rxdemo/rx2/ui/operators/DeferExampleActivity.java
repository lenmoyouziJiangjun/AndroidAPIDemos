package com.lll.rxdemo.rx2.ui.operators;

import android.view.View;

import com.lll.rxdemo.rx2.model.Car;
import com.lll.rxdemo.rx2.ui.RxDemoBaseActivity;

import io.reactivex.Emitter;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;

public class DeferExampleActivity extends RxDemoBaseActivity {
    @Override
    public void buttonClick(View view) {
        doSomeWork();
    }

    private void doSomeWork() {
        Car car = new Car();
        Observable<String> brandDeferObservable = car.brandDeferObservable();

        car.setBrand("BMW");
        // Even if we are setting the brand after creating Observable
        // we will get the brand as BMW.

        // If we had not used defer, we would have got null as the brand.
        brandDeferObservable.subscribe(this.<String>getObserver());
    }
}
