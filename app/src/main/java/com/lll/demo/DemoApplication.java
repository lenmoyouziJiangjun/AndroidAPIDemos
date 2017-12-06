package com.lll.demo;

import android.app.Application;
import android.content.Context;
import android.os.StrictMode;
import android.support.multidex.MultiDex;
import android.util.Log;

import com.lll.demo.utils.DexInstallHelper;
import com.squareup.leakcanary.LeakCanary;

import java.util.Enumeration;
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
 * 1、从applicationInfo 的sourceDir ,dataDir加载dex
 * 2、反射调用dex里面文件类，加载类。
 * <p>
 * <p>
 * 5、MultiDexApplication 会带来什么问题？有什么优化方案？
 * 5.1: 包太多了(dex文件)，分包的时候会出现找不到某个类的bug
 * 5.2: app启动加载慢。
 * 5.3:优化1：http://blog.csdn.net/DJY1992/article/details/51162061
 * 5.4：https://github.com/casidiablo/multidex
 * <p>
 * <p>
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
public class DemoApplication extends Application {

    private static final String TAG = "DemoApplication";

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        Log.d(TAG, "onBaseContextAttached start");

        //必须放在前面，解决MultiDex.install安装时间过长导致的黑屏问题。参考自https://github.com/shensky711/MultiDex
        if (DexInstallHelper.isDexInstallProcess(base)) {
            Log.d(TAG, "onBaseContextAttached end,isDexInstallProcess直接返回");
            return;
        }
        // if VM has multi dex support, MultiDex support library is disabled
        if (!DexInstallHelper.isVMMultiDexCapable()) {//如果是davlik虚拟机
            if (!DexInstallHelper.isMultiDexInstalled(base)) {
                DexInstallHelper.waitForDexInstall(base);//block当前进程
            }
        }
        long start = System.currentTimeMillis();
        MultiDex.install(base);
        Log.d(TAG, "MultiDex.install end: " + (System.currentTimeMillis() - start));
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (DexInstallHelper.isDexInstallProcess(this)) {
            return;
        }

        if (BuildConfig.DEBUG) {
            //增加内存检测
            if (!LeakCanary.isInAnalyzerProcess(this)) {
                LeakCanary.install(this);
            }

            testSystemProperty();
//
            setStrictMode();
        }
    }


    /**
     * SystemProperty
     */
    private void testSystemProperty() {
        Properties properties = System.getProperties();
        Enumeration ration= properties.propertyNames();
//        Object obj= ration.nextElement();
//        while ( obj !=null) {
//            obj =ration .nextElement();
//            Log.i("lll", properties.get(obj) !=null ? properties.get(obj).toString():obj.toString());
//        }
    }

    /**
     * 严苛模式： 用来制定一些策略，如果不满足，就报错
     * 目前，有两种类型的策略：
     * <p>
     * Thread Policy : 线程策略应用到特定的线程。
     * VM Policy : VM是Virtual Machine的缩写，表示“虚拟机”，不要搞错以为是Virtual Memory（虚拟内存）。应用于虚拟机进程中的所有线程。
     * ThreadPolicy.Builder中的一些方法:
     * <p>
     * detectAll() : 侦测一切潜在违规
     * detectCustomSlowCalls() : 侦测自定义的耗时操作
     * detectDiskReads() : 侦测磁盘读
     * detectDiskWrites() : 侦测磁盘写
     * detectNetwork() : 侦测网络操作
     * permitAll() : 禁用所有侦测
     * permitDiskReads() : 允许磁盘读
     * VmPolicy.Builder中的一些方法 :
     * <p>
     * detectAll() : 侦测一切潜在违规
     * detectActivityLeaks() : 侦测Activity（活动）泄露
     * detectLeakedClosableObjects() : 当显式中止方法调用之后，假如可被Closeable类或其他的对象没有被关闭。
     * <p>
     */
    private void setStrictMode() {
        //
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectAll().penaltyDialog().build());
        //
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectLeakedSqlLiteObjects().penaltyDeath().penaltyLog().detectAll().build());
    }


}
