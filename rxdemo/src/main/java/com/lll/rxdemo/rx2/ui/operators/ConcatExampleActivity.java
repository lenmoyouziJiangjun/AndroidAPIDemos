package com.lll.rxdemo.rx2.ui.operators;

import android.util.Log;
import android.view.View;

import com.lll.rxdemo.rx2.ui.RxDemoBaseActivity;
import com.lll.rxdemo.rx2.utils.AppConstant;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;

/**
 * Version 1.0
 * Created by lll on 17/8/4.
 * Description
 * <p>
 * 1、Observable 、Observer、subscribe的关系：http://www.jianshu.com/p/98da2c2e3576
 * </p>
 * copyright generalray4239@gmail.com
 */
public class ConcatExampleActivity extends RxDemoBaseActivity {
    @Override
    public void buttonClick(View view) {
        doSomeThing();
    }

    private void doSomeThing() {
        final String[] aStrings = {"A1", "A2", "A3", "A4"};
        final String[] bStrings = {"B1", "B2", "B3"};

        final Observable<String> aObservable = Observable.fromArray(aStrings);
        final Observable<String> bObservable = Observable.fromArray(bStrings);

        /**
         * Using concat operator to combine Observable : concat maintain
         * the order of Observable.
         * It will emit all the 7 values in order
         * here - first "A1", "A2", "A3", "A4" and then "B1", "B2", "B3"
         * first all from the first Observable and then
         * all from the second Observable all in order
         */
        Observable.concat(aObservable, bObservable)
                .subscribe(getObserver());

    }

    /**
     * 定义observer观察者
     *
     * @return
     */
    public Observer<String> getObserver() {
        return new Observer<String>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {
                Log.d(TAG, " onSubscribe : " + d.isDisposed());
            }

            @Override
            public void onNext(@NonNull String value) {
                mTextView.append(" onNext : value : " + value);
                mTextView.append(AppConstant.LINE_SEPARATOR);
                Log.d(TAG, " onNext : value : " + value);
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
        };
    }


}
