package com.lll.lifecycledemo.mvp;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;

public interface ILifecyclePresenter extends LifecycleObserver {


    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    void onCreate();


    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    void onStart();



    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    void onResume();



    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    void onPause();



    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    void onDestroy();



}
