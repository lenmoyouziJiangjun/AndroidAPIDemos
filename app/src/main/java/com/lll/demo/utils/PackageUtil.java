package com.lll.demo.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

/**
 * Version 1.0
 * Created by lll on 10/31/17.
 * Description
 * copyright generalray4239@gmail.com
 */
public class PackageUtil {

    public static PackageInfo getPackageInfo(Context context) {
        PackageManager pm = context.getPackageManager();
        try {
            return pm.getPackageInfo(context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
        }
        return new PackageInfo();
    }
}
