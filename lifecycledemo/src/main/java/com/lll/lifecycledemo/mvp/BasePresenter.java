package com.lll.lifecycledemo.mvp;

import android.util.Log;

/**
 * Version 1.0
 * Created by lll on 2019-11-05.
 * Description
 * <pre>
 *      记录生命周期log 和 埋点
 * </pre>
 * copyright generalray4239@gmail.com
 */
public abstract class BasePresenter<T extends IBaseView> implements ILifecyclePresenter, IBasePresenter<T> {

    private long lastResumeTime = 0L;

    @Override
    public void onCreate() {
        Log.d(getView().getTag(), "do onCreate");
    }

    @Override
    public void onStart() {
        Log.d(getView().getTag(), "do onStart");
    }

    @Override
    public void onResume() {
        lastResumeTime = System.currentTimeMillis();
        Log.d(getView().getTag(), "do onResume");
    }

    @Override
    public void onPause() {
        long visitTime = System.currentTimeMillis() - lastResumeTime;
        Log.d(getView().getTag(), "do onPause time==" + visitTime);
    }

    @Override
    public void onDestroy() {
        Log.d(getView().getTag(), "do onDestroy");
    }
}
