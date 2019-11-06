package com.lll.commonlibrary.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;

/**
 * Created by lll on 2015/8/12.
 * version：
 * description：
 * copyright ：timanetwork
 */
public class TelephoneUtils {


    /**
     * 返回网络状态
     *
     * @param context
     * @return
     */
    public static boolean getNetWorkState(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = manager.getActiveNetworkInfo();
        if (info != null) {
            return info.isAvailable();
        }
        return false;
    }

    /**
     * 获取版本信息
     *
     * @return
     */
    public static int getSDKVersion() {
        return Build.VERSION.SDK_INT;
    }

    /**
     * 获取当前应用的版本号
     *
     * @param ctx
     * @return
     */
    public static float getAppVersion(Context ctx) {
        return 1.0f;
    }
}
