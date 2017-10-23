package com.lll.demo;

import android.support.multidex.MultiDexApplication;
import android.util.Log;

import com.squareup.leakcanary.LeakCanary;

import java.util.Properties;

/**
 * Version 1.0
 * Created by lll on 17/8/7.
 * Description
 * 1、内存检测 ：LeakCanary:https://github.com/square/leakcanary
 * 2、内存检测的原理是什么？那些地方容易出现内存泄漏和溢出？
 * <p>
 * 3、为什么使用MultiDexApplication？
 * <p>
 * <p>
 * <p>
 * <p>
 * 4、MultiDexApplication 的原理是什么？ http://blog.csdn.net/jiangwei0910410003/article/details/50799573
 *    1、从applicationInfo 的sourceDir ,dataDir加载dex
 *    2、反射调用dex里面文件类，加载类。
 * <p>
 * <p>
 * 5、MultiDexApplication 会带来什么问题？有什么优化方案？
 * 5.1: 包太多了(dex文件)，分包的时候会出现找不到某个类的bug
 * 5.2: app启动加载慢。
 * 5.3:优化1：http://blog.csdn.net/DJY1992/article/details/51162061
 *
 *
 * <p>
 * <p>
 * <p>
 * <p>
 * 6、Android的打包机制是什么？
 * <p>
 * <p>
 * <p>
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
        testSystemProperty();
    }

    /**
     * SystemProperty
     */
    private void testSystemProperty() {
        Properties properties = System.getProperties();
        while (properties.propertyNames().hasMoreElements()) {
            Log.i("lll", properties.get(properties.propertyNames().nextElement()).toString());
        }
    }


}
