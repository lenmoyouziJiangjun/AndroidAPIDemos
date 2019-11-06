package com.lll.anim.utils;

import android.app.Activity;
import android.content.Intent;

import com.lll.commonanimationdemo.R;

/**
 * Version 1.0
 * Created by lll on 12/6/17.
 * Description  activity 转场动画切换
 * copyright generalray4239@gmail.com
 */
public class ActivityTranslateUtils {

    /**
     * 左右动画打开activity
     *
     * @param activity
     * @param intent
     */
    public static void startActivityWithAnimation(Activity activity, Intent intent) {
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.anim_in, R.anim.anim_out);
    }

    /**
     * 指定动画打开Activity
     *
     * @param activity
     * @param intent
     * @param inId     进入动画
     * @param outId    取消动画
     */
    public static void startActivityWithAnimation(Activity activity, Intent intent, int inId, int outId) {
        activity.startActivity(intent);
        activity.overridePendingTransition(inId, outId);
    }


    /**
     * 左右动画startActivityForResult
     *
     * @param activity
     * @param intent
     */
    public static void startActivityForResultWithAnimation(Activity activity, Intent intent, int resultCode) {
        activity.startActivityForResult(intent, resultCode);
        activity.overridePendingTransition(R.anim.anim_in, R.anim.anim_out);
    }

}
