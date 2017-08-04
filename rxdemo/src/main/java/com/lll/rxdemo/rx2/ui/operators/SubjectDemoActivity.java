package com.lll.rxdemo.rx2.ui.operators;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.lll.rxdemo.R;
import com.lll.rxdemo.rx2.ui.RxDemoBaseActivity;
import com.lll.rxdemo.rx2.utils.AppConstant;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.AsyncSubject;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.ReplaySubject;
import io.reactivex.subjects.Subject;

/**
 * Version 1.0
 * Created by lll on 17/8/3.
 * Description 各种subject的区别：http://www.jianshu.com/p/1257c8ba7c0c
 * copyright generalray4239@gmail.com
 */
public class SubjectDemoActivity extends AppCompatActivity {
    private TextView mTextView;
    private static final String TAG = "SubjectDemoActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subject);
        mTextView = (TextView) findViewById(R.id.textView);
    }

    public void doSubject(View view) {
        Subject<Integer> source = null;
        int id = view.getId();
        if (id == R.id.async) {
            /** An AsyncSubject emits the last value (and only the last value) emitted by the source
             * Observable, and only after that source Observable completes. (If the source Observable
             * does not emit any values, the AsyncSubject also completes without emitting any values.)
             */
            source = AsyncSubject.create();
        } else if (id == R.id.behavior) {
            /** When an observer subscribes to a BehaviorSubject, it begins by emitting the item most
             * recently emitted by the source Observable (or a seed/default value if none has yet been
             * emitted) and then continues to emit any other items emitted later by the source Observable(s).
             * It is different from Async Subject as async emits the last value (and only the last value)
             * but the Behavior Subject emits the last and the subsequent values also.
             */
            source = BehaviorSubject.create();
        } else if (id == R.id.publish) {
            /** PublishSubject emits to an observer only those items that are emitted
             * by the source Observable, subsequent to the time of the subscription.
             */
            source = PublishSubject.create();
        } else if (id == R.id.replay) {
            /** ReplaySubject emits to any observer all of the items that were emitted
             * by the source Observable, regardless of when the observer subscribes.
             */
            source = ReplaySubject.create();
        }

        source.subscribe(getFirstObserver());
        source.onNext(1);
        source.onNext(2);
        source.onNext(3);


        source.subscribe(getSecondObserver());
        source.onNext(4);
        source.onComplete();
    }


    private Observer<Integer> getFirstObserver() {
        return new Observer<Integer>() {

            @Override
            public void onSubscribe(@NonNull Disposable d) {
                Log.d(TAG, "SubjectDemoActivity First onSubscribe : " + d.isDisposed());
            }

            @Override
            public void onNext(@NonNull Integer integer) {
                mTextView.append(" First onNext : value : " + integer);
                mTextView.append(AppConstant.LINE_SEPARATOR);
                Log.d(TAG, " First onNext value : " + integer);
            }

            @Override
            public void onError(@NonNull Throwable e) {
                mTextView.append(" First onError : " + e.getMessage());
                mTextView.append(AppConstant.LINE_SEPARATOR);
                Log.d(TAG, " First onError : " + e.getMessage());
            }

            @Override
            public void onComplete() {
                mTextView.append(" First onComplete");
                mTextView.append(AppConstant.LINE_SEPARATOR);
                Log.d(TAG, " First onComplete");
            }
        };
    }

    private Observer<Integer> getSecondObserver() {
        return new Observer<Integer>() {

            @Override
            public void onSubscribe(Disposable d) {
                mTextView.append(" Second onSubscribe : isDisposed :" + d.isDisposed());
                Log.d(TAG, " Second onSubscribe : " + d.isDisposed());
                mTextView.append(AppConstant.LINE_SEPARATOR);
            }

            @Override
            public void onNext(Integer value) {
                mTextView.append(" Second onNext : value : " + value);
                mTextView.append(AppConstant.LINE_SEPARATOR);
                Log.d(TAG, " Second onNext value : " + value);
            }

            @Override
            public void onError(Throwable e) {
                mTextView.append(" Second onError : " + e.getMessage());
                mTextView.append(AppConstant.LINE_SEPARATOR);
                Log.d(TAG, " Second onError : " + e.getMessage());
            }

            @Override
            public void onComplete() {
                mTextView.append(" Second onComplete");
                mTextView.append(AppConstant.LINE_SEPARATOR);
                Log.d(TAG, " Second onComplete");
            }
        };
    }


}
