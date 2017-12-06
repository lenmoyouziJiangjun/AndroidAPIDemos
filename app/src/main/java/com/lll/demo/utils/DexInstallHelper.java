package com.lll.demo.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Process;
import android.util.Log;

import com.lll.demo.BuildConfig;
import com.lll.demo.SplashActivity;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.content.Context.MODE_MULTI_PROCESS;

/**
 * Version 1.0
 * Created by lll on 10/31/17.
 * Description
 * <p>
 * 1、google原来的multiDex在Application中加载第二个dex。如果第二个dex太大，容易出现ANR；
 * 2、所以我们将加载的逻辑放到SplashActivity的子线程中，等待第二个加载成功后在执行。
 * copyright generalray4239@gmail.com
 */
public class DexInstallHelper {

    private static final String KEY_DEX2_SHA1 = "dex2-SHA1-Digest";

    /**
     * @return
     */
    private static Class<? extends Activity> getDexInstallActivity() {
        return SplashActivity.class;
    }

    /**
     * @param context
     * @return {@code true} if current process is dex install process
     */
    public static boolean isDexInstallProcess(Context context) {
        return isDexInstallProcess(context, getDexInstallActivity());
    }

    /**
     * 判断是否在主线程
     *
     * @param context
     * @param activityClass
     * @return
     */
    public static boolean isDexInstallProcess(Context context, Class<? extends Activity> activityClass) {
        PackageManager pm = context.getPackageManager();
        PackageInfo pInfo;
        try {
            pInfo = pm.getPackageInfo(context.getPackageName(), PackageManager.GET_ACTIVITIES);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        String mainProcess = pInfo.applicationInfo.processName;
        ComponentName component = new ComponentName(context, activityClass);
        ActivityInfo activityInfo;
        try {
            activityInfo = pm.getActivityInfo(component, 0);
        } catch (Exception e) {
            return false;
        }
        if (activityInfo.processName.equals(mainProcess)) {
            return false;
        } else {
            int myPid = Process.myPid();
            ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            ActivityManager.RunningAppProcessInfo myProcess = null;
            List<ActivityManager.RunningAppProcessInfo> runningProcesses = activityManager.getRunningAppProcesses();
            if (runningProcesses != null) {
                for (ActivityManager.RunningAppProcessInfo process : runningProcesses) {
                    if (process.pid == myPid) {
                        myProcess = process;
                        break;
                    }
                }
            }
            return myProcess != null && myProcess.processName.equals(activityInfo.processName);
        }
    }

    /**
     * @return {@code true} if VM has multi dex support
     * true Art虚拟机
     * false Davlik虚拟机
     */
    public static boolean isVMMultiDexCapable() {
        boolean isMultiDexCapable = false;
        String versionString = System.getProperty("java.vm.version");
        Matcher matcher = Pattern.compile("(\\d+)\\.(\\d+)(\\.\\d+)?").matcher(versionString);
        if (matcher.matches()) {
            try {
                int e = Integer.parseInt(matcher.group(1));
                int minor = Integer.parseInt(matcher.group(2));
                isMultiDexCapable = e > 2 || e == 2 && minor >= 1;
            } catch (NumberFormatException ignore) {
            }
        }
        return isMultiDexCapable;
    }

    /**
     * @param context
     * @return {@code true} if we already install multi dex before
     */
    public static boolean isMultiDexInstalled(Context context) {
        String flag = get2thDexSHA1(context);
        SharedPreferences sp = context.getSharedPreferences(getPreferencesName(context), MODE_MULTI_PROCESS);
        String saveValue = sp.getString(KEY_DEX2_SHA1, "");
        return flag.equals(saveValue);
    }

    /**
     * 跳转到欢迎界面，等待dex 安装
     *
     * @param context
     */
    public static void waitForDexInstall(Context context) {
        //启动引导界面
        Intent intent = new Intent();
        ComponentName componentName = new ComponentName(context.getPackageName(), getDexInstallActivity().getName());
        intent.setComponent(componentName);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);

        //阻塞线程等待安装
        long waitTime = TimeUnit.SECONDS.toMillis(20);
        long startWait = System.currentTimeMillis();
        while (!isMultiDexInstalled(context)) {
            try {
                long nowWait = System.currentTimeMillis() - startWait;
                if (nowWait >= waitTime) {
                    break;
                }
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void markInstallFinish(Context context) {
        SharedPreferences sp = context.getSharedPreferences(getPreferencesName(context), MODE_MULTI_PROCESS);
        // do not use apply here
        sp.edit().putString(KEY_DEX2_SHA1, get2thDexSHA1(context)).commit();
    }

    /**
     * @param context
     * @return
     */
    private static String get2thDexSHA1(Context context) {
        ApplicationInfo info = context.getApplicationInfo();
        String source = info.sourceDir;
        Log.i("JM_BOOT", "source=" + source);
        try {
            JarFile jar = new JarFile(source);
            Manifest mf = jar.getManifest();
            Map<String, Attributes> map = mf.getEntries();
            Attributes a = map.get("classes2.dex");
            String result = a == null ? "" : a.getValue("SHA1-Digest");
            logManifestInfo4Debug(map);
            Log.i("JM_BOOT","get2thDexSHA1:classes2.dex的SHA1-Digest=" + result);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }

    private static void logManifestInfo4Debug(Map<String, Attributes> infoMap){
        if(BuildConfig.DEBUG && infoMap != null){//如果apk打包的时候是debug包，则打印日志
            Set<Map.Entry<String,Attributes>> entrys = infoMap.entrySet();
            if(entrys != null){
                Log.i("JM_BOOT","manifestInfo log start");
                for(Map.Entry entry : entrys){
                    if(entry != null){
                        String key = (String)entry.getKey();
                        if(!"AndroidManifest.xml".equals(key) && !"classes2.dex".equals(key)){
                            continue;
                        }
                        Log.i("JM_BOOT","manifestInfo key=" + key);
                        Attributes value = (Attributes)entry.getValue();
                        if(value != null) {
                            Set valueKeySet = value.keySet();
                            if (valueKeySet != null) {
                                for (Object valueKey : valueKeySet) {
                                    Log.i("JM_BOOT", "manifestInfo ------ valueKey=" + valueKey +
                                            ",valueValue=" + value.get(valueKey));
                                }
                            }
                        }
                    }
                }
                Log.i("JM_BOOT","manifestInfo log end");
            }
        }
    }


    private static String getPreferencesName(Context context) {
        PackageInfo packageInfo = PackageUtil.getPackageInfo(context);
        return context.getPackageName() + "." + packageInfo.versionName;
    }

}
