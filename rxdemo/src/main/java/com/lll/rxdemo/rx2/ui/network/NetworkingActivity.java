package com.lll.rxdemo.rx2.ui.network;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Pair;
import android.view.View;

import com.lll.rxdemo.R;
import com.lll.rxdemo.rx2.model.ApiUser;
import com.lll.rxdemo.rx2.model.User;
import com.lll.rxdemo.rx2.model.UserDetail;
import com.rx2androidnetworking.Rx2AndroidNetworking;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedules.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;

/**
 * Version 1.0
 * Created by lll on 17/8/7.
 * Description
 * copyright generalray4239@gmail.com
 */
public class NetworkingActivity extends AppCompatActivity {


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_networking);
    }

    public void map(View view) {
        Rx2AndroidNetworking.get("https://fierce-cove-29863.herokuapp.com/getAnUser/{userId}")
                .addPathParameter("userId", "1")
                .build()
                .getObjectObservable(ApiUser.class)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(new Function<ApiUser, User>() {
                    @Override
                    public User apply(@NonNull ApiUser apiUser) throws Exception {
                        // here we get ApiUser from server
                        User user = new User(apiUser);
                        // then by converting, we are returning user
                        return user;
                    }
                })
                .subscribe(new Observer<User>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }

                    @Override
                    public void onNext(@NonNull User user) {
                        Log.d("lll", "user : " + user.toString());
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        Log.e("lll", "-------", e);
                    }

                    @Override
                    public void onComplete() {
                        Log.d("lll", "onComplete");
                    }
                });
    }

    /**
     * This observable return the list of User who loves cricket
     */
    private Observable<List<User>> getCricketFansObservable() {
        return Rx2AndroidNetworking.get("https://fierce-cove-29863.herokuapp.com/getAllCricketFans")
                .build()
                .getObjectListObservable(User.class);
    }

    /**
     * This observable return the list of User who loves Football
     *
     * @return
     */
    private Observable<List<User>> getFootballFansObservable() {
        return Rx2AndroidNetworking.get("https://fierce-cove-29863.herokuapp.com/getAllFootballFans")
                .build()
                .getObjectListObservable(User.class);
    }

    /**
     * This do the complete magic, make both network call
     * and then returns the list of user who loves both
     * Using zip operator to get both response at a time
     */
    private void findUsersWhoLovesBoth() {
        // here we are using zip operator to combine both request
        Observable.zip(getCricketFansObservable(), getFootballFansObservable(),
                new BiFunction<List<User>, List<User>, List<User>>() {
                    @Override
                    public List<User> apply(@NonNull List<User> cricketFans, @NonNull List<User> footballFans) throws Exception {
                        List<User> userWhoLovesBoth = filterUserWhoLovesBoth(cricketFans, footballFans);
                        return userWhoLovesBoth;
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<User>>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }

                    @Override
                    public void onNext(@NonNull List<User> users) {
                        // do anything with user who loves both
                        Log.d("lll", "userList size : " + users.size());
                        for (User user : users) {
                            Log.d("lll", "user : " + user.toString());
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        Log.e("lll", " error", e);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private List<User> filterUserWhoLovesBoth(List<User> cricketFans, List<User> footballFans) {
        List<User> userWhoLovesBoth = new ArrayList<>();
        for (User cricketFan : cricketFans) {
            for (User footballFan : footballFans) {
                if (cricketFan.id == footballFan.id) {
                    userWhoLovesBoth.add(cricketFan);
                }
            }
        }
        return userWhoLovesBoth;
    }

    public void zip(View view) {
        findUsersWhoLovesBoth();
    }

    /**
     * flatMap and filter Operators Example
     */

    private Observable<List<User>> getAllMyFriendsObservable() {
        return Rx2AndroidNetworking.get("https://fierce-cove-29863.herokuapp.com/getAllFriends/{userId}")
                .addPathParameter("userId", "1")
                .build()
                .getObjectListObservable(User.class);
    }

    public void flatMapAndFilter(View view) {
        getAllMyFriendsObservable().flatMap(new Function<List<User>, ObservableSource<User>>() {
            @Override
            public ObservableSource<User> apply(@NonNull List<User> usersList) throws Exception {
                return Observable.fromIterable(usersList); // returning user one by one from usersList.
            }
        })
                .filter(new Predicate<User>() {
                    @Override
                    public boolean test(@NonNull User user) throws Exception {
                        // filtering user who follows me.
                        return user.isFollowing;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<User>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }

                    @Override
                    public void onNext(@NonNull User user) {
                        // only the user who is following me comes here one by one
                        Log.d("lll", "user : " + user.toString());
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        Log.e("lll", "-----onError------", e);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    /**
     * flatMapWithZip Operator Example
     */

    private Observable<List<User>> getUserListObservable() {
        return Rx2AndroidNetworking.get("https://fierce-cove-29863.herokuapp.com/getAllUsers/{pageNumber}")
                .addPathParameter("pageNumber", "0")
                .addQueryParameter("limit", "10")
                .build()
                .getObjectListObservable(User.class);
    }

    private Observable<UserDetail> getUserDetailObservable(long id) {
        return Rx2AndroidNetworking.get("https://fierce-cove-29863.herokuapp.com/getAnUserDetail/{userId}")
                .addPathParameter("userId", String.valueOf(id))
                .build()
                .getObjectObservable(UserDetail.class);
    }

    /**
     * take Operator Example
     */
    public void take(View view) {
        getUserListObservable()
                .flatMap(new Function<List<User>, ObservableSource<User>>() {// flatMap - to return users one by one
                    @Override
                    public ObservableSource<User> apply(@NonNull List<User> users) throws Exception {
                        return Observable.fromIterable(users); // returning user one by one from usersList.
                    }
                })
                .take(4)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<User>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }

                    @Override
                    public void onNext(@NonNull User user) {
                        // // only four user comes here one by one
                        Log.d("lll", "user : " + user.toString());
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    /**
     * flatMap Operator Example
     */
    public void flatMap(View view) {
        getUserListObservable().flatMap(new Function<List<User>, ObservableSource<User>>() {// flatMap - to return users one by one
            @Override
            public ObservableSource<User> apply(@NonNull List<User> users) throws Exception {
                return Observable.fromIterable(users); // returning user one by one from usersList.
            }
        }).flatMap(new Function<User, ObservableSource<UserDetail>>() {
            @Override
            public ObservableSource<UserDetail> apply(@NonNull User user) throws Exception {
                // here we get the user one by one
                // and returns corresponding getUserDetailObservable
                // for that userId
                return getUserDetailObservable(user.id);
            }
        })
                .subscribeOn(Schedulers.newThread())
                .observeOn(io.reactivex.android.schedulers.AndroidSchedulers.mainThread())
                .subscribe(new Observer<UserDetail>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }

                    @Override
                    public void onNext(@NonNull UserDetail userDetail) {

                    }

                    @Override
                    public void onError(@NonNull Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    public void flatMapWithZip(View view) {
        getUserListObservable().flatMap(new Function<List<User>, ObservableSource<User>>() {
            @Override
            public ObservableSource<User> apply(@NonNull List<User> users) throws Exception {
                return Observable.fromIterable(users); // returning user one by one from usersList.
            }
        })
                .flatMap(new Function<User, ObservableSource<Pair<UserDetail, User>>>() {
                    @Override
                    public ObservableSource<Pair<UserDetail, User>> apply(@NonNull User user) throws Exception {
                        // here we get the user one by one and then we are zipping
                        // two observable - one getUserDetailObservable (network call to get userDetail)
                        // and another Observable.just(user) - just to emit user
                        return Observable.zip(getUserDetailObservable(user.id),
                                Observable.just(user),
                                new BiFunction<UserDetail, User, Pair<UserDetail, User>>() {
                                    @Override
                                    public Pair<UserDetail, User> apply(UserDetail userDetail, User user) throws Exception {
                                        // runs when network call completes
                                        // we get here userDetail for the corresponding user
                                        return new Pair<>(userDetail, user); // returning the pair(userDetail, user)
                                    }
                                });
                    }
                }).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Pair<UserDetail, User>>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }

                    @Override
                    public void onNext(@NonNull Pair<UserDetail, User> userDetailUserPair) {

                    }

                    @Override
                    public void onError(@NonNull Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }


}
