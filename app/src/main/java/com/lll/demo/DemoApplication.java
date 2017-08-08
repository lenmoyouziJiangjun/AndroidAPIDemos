package com.lll.demo;

import android.support.multidex.MultiDexApplication;

import com.squareup.leakcanary.LeakCanary;

/**
 * Version 1.0
 * Created by lll on 17/8/7.
 * Description
 * 1、LeakCanary:https://github.com/square/leakcanary
 * copyright generalray4239@gmail.com
 */
public class DemoApplication extends MultiDexApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        //增加内存检测
        if (!LeakCanary.isInAnalyzerProcess(this)) {
            LeakCanary.install(this);
        }
    }
}
