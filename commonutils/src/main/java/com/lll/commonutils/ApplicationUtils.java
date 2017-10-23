package com.lll.commonutils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;

public class ApplicationUtils {


    public static ApplicationInfo getApplicationInfo(Context context) throws PackageManager.NameNotFoundException{
        PackageManager pm;
        String packageName;
        try {
            pm = context.getPackageManager();
            packageName = context.getPackageName();
        } catch (RuntimeException var4) {
            Log.w("MultiDex", "Failure while trying to obtain ApplicationInfo from Context. Must be running in test mode. Skip patching.", var4);
            return null;
        }

        if(pm != null && packageName != null) {
            android.content.pm.ApplicationInfo applicationInfo = pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
            return applicationInfo;
        } else {
            return null;
        }
    }


}
