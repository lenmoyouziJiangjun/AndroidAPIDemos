package com.lll.rxdemo.rx2.ui.operators;

import android.os.SystemClock;
import android.util.Log;
import android.view.View;

import com.lll.rxdemo.rx2.ui.RxDemoBaseActivity;
import com.lll.rxdemo.rx2.utils.AppConstant;

import java.util.concurrent.Callable;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedules.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class DisposableExampleActivity extends RxDemoBaseActivity {

    private final CompositeDisposable mDisposable = new CompositeDisposable();

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDisposable.clear();
    }

    @Override
    public void buttonClick(View view) {
        doSomeWork();
    }


    private void doSomeWork() {
        mDisposable.add(getObservable()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<String>() {
                    @Override
                    public void onNext(@NonNull String value) {
                        mTextView.append(" onNext : value : " + value);
                        mTextView.append(AppConstant.LINE_SEPARATOR);
                        Log.d(TAG, " onNext value : " + value);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        mTextView.append(" onError : " + e.getMessage());
                        mTextView.append(AppConstant.LINE_SEPARATOR);
                        Log.d(TAG, " onError : " + e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        mTextView.append(" onComplete");
                        mTextView.append(AppConstant.LINE_SEPARATOR);
                        Log.d(TAG, " onComplete");
                    }
                })
        );
    }

    private Observable<String> getObservable() {
        return Observable.defer(new Callable<ObservableSource<? extends String>>() {
            @Override
            public ObservableSource<? extends String> call() throws Exception {
                // Do some long running operation
                SystemClock.sleep(10000);
                return Observable.just("one", "two", "three", "four", "five");
            }
        });
    }
}
