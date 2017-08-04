package com.lll.rxdemo.rx2.ui.compose;

import android.app.Activity;
import android.app.IntentService;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.lll.rxdemo.R;

import org.reactivestreams.Publisher;

import java.util.Random;
import java.util.concurrent.Callable;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.android.schedules.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class ComposeOperatorActivity extends Activity {
    private final RxSchedulers schedulers = new RxSchedulers();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        layout.setLayoutParams(params);
        layout.setHorizontalGravity(Gravity.CENTER);
        layout.setPadding(12, 20, 12, 20);
        setContentView(layout);

        test();
    }

    private void test() {
        Observable.just(1, 2, 3, 4, 5, 6)//准备数据
                .compose(schedulers.<Integer>applyObservableAsync())
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(@NonNull Integer integer) throws Exception {

                    }
                });

        Flowable.just(1, 2, 3, 4, 5, 6)
                .compose(schedulers.<Integer>applyFlowableAsysnc())
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(@NonNull Integer integer) throws Exception {

                    }
                });
        Flowable.fromCallable(new Callable<Long>() {
            @Override
            public Long call() throws Exception {
                //数据加载
                Thread.sleep(10000);
                return 10000L;
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(@NonNull Long ddd) throws Exception {
                        Log.e("lll", "-------ddd-----" + ddd);
                    }
                });
    }
}
