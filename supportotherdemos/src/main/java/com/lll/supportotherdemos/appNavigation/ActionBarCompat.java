package com.lll.supportotherdemos.appNavigation;

import android.app.Activity;
import android.os.Build;

/**
 * Very limited shim for enabling the action bar's up button on devices that support it.
 */
public class ActionBarCompat {

    static class ActionBarCompatImpl {
        static void setDisplayHomeAsUpEnabled(Activity activity, boolean enable) {
            activity.getActionBar().setDisplayHomeAsUpEnabled(enable);
        }
    }

    public static void setDisplayHomeAsUpEnabled(Activity activity, boolean enable) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            ActionBarCompatImpl.setDisplayHomeAsUpEnabled(activity, enable);
        }
    }
}
