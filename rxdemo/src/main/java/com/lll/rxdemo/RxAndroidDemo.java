package com.lll.rxdemo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import com.lll.basedemo.DemoBaseActivity;

import java.util.concurrent.Callable;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedules.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

/**
 * Version 1.0
 * Created by lll on 17/8/2.
 * Description
 * <p>
 * 参考网站1：https://github.com/yydcdut/PhotoNoter
 * 参考网站2：https://github.com/amitshekhariitbhu/RxJava2-Android-Samples
 * </p>
 * copyright generalray4239@gmail.com
 */
public class RxAndroidDemo extends DemoBaseActivity {

//    private final CompositeDisposable disposable = new CompositeDisposable();

    @Override
    public String setCategoryName() {
        return "com.lll.rxdemo.SAMPLE_CODE";
    }


//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        disposable.clear();
//    }
//
//    void onRunSchedulerExampleButtonClicked() {
//        disposable.add(sampleObservable()
//                // Run on a background thread
//                .subscribeOn(Schedulers.io())
//                // Be notified on the main thread
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribeWith(new DisposableObserver<String>() {
//
//                    @Override
//                    public void onNext(@NonNull String string) {
//                        Log.e("lll", "----------" + string);
//                    }
//
//                    @Override
//                    public void onError(@NonNull Throwable e) {
//
//                    }
//
//                    @Override
//                    public void onComplete() {
//
//                    }
//                }));
//    }
//
//    static Observable<String> sampleObservable() {
//        return Observable.defer(new Callable<ObservableSource<? extends String>>() {
//            @Override
//            public ObservableSource<? extends String> call() throws Exception {
//                //Do some long running operation
//                return Observable.just("one", "two", "three", "four", "five");
//            }
//        });
//    }


}
