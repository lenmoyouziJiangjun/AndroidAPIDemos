package com.lll.rxdemo.rx2.ui.operators;

import android.view.View;

import com.lll.rxdemo.rx2.model.ApiUser;
import com.lll.rxdemo.rx2.model.User;
import com.lll.rxdemo.rx2.ui.RxDemoBaseActivity;
import com.lll.rxdemo.rx2.utils.AppConstant;
import com.lll.rxdemo.rx2.utils.DataMock;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedules.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class MapExampleActivity extends RxDemoBaseActivity {
    @Override
    public void buttonClick(View view) {
        doSomeWork();
    }

    private void doSomeWork() {
        getObservable().observeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(this.<List<ApiUser>>getObserver());
                .map(new Function<List<ApiUser>, Object>() {
                    @Override
                    public Object apply(@NonNull List<ApiUser> apiUsers) throws Exception {
                        return DataMock.convertApiUserListToUserList(apiUsers);
                    }
                })
                .subscribe(this.getObserver());
    }

    private Observable<List<ApiUser>> getObservable() {
        return Observable.create(new ObservableOnSubscribe<List<ApiUser>>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<List<ApiUser>> emitter) throws Exception {
                if (!emitter.isDisposed()) {
                    emitter.onNext(DataMock.getApiUserList());
                    emitter.onComplete();
                }
            }
        });
    }

    @Override
    public <T> void doOtherNext(T t) {
        List<User> userList = (List<User>) t;
        for (User user : userList) {
            mTextView.append(" firstname : " + user.firstname);
            mTextView.append(AppConstant.LINE_SEPARATOR);
        }
    }
}
