package com.lll.commonlibrary.util;

import android.content.Context;
import android.widget.Toast;

/**
 * Description: 工具toast显示,避免用户使劲点击循环弹出的问题。
 * Version:
 * Created by lll on 2016/1/22.
 * CopyRight lll
 */
public class ToastUtils {

    private static Toast mToast;

    public static void showShortToast(Context ctx,String msg){
        if (mToast == null) {
            mToast = Toast.makeText(ctx, msg, Toast.LENGTH_SHORT);
        } else {
            mToast.setText(msg);
            mToast.setDuration(Toast.LENGTH_SHORT);
        }
        mToast.show();
    }

    public static void showShortToast(Context ctx,int msgId){
        if (mToast == null) {
            mToast = Toast.makeText(ctx, msgId, Toast.LENGTH_SHORT);
        } else {
            mToast.setText(msgId);
            mToast.setDuration(Toast.LENGTH_SHORT);
        }
        mToast.show();
    }

    public static void showLongToast(Context ctx,String msg){
        if (mToast == null) {
            mToast = Toast.makeText(ctx, msg, Toast.LENGTH_LONG);
        } else {
            mToast.setText(msg);
            mToast.setDuration(Toast.LENGTH_SHORT);
        }
        mToast.show();
    }


    public static void showLongToast(Context ctx,int msgId){
        if (mToast == null) {
            mToast = Toast.makeText(ctx, msgId, Toast.LENGTH_LONG);
        } else {
            mToast.setText(msgId);
            mToast.setDuration(Toast.LENGTH_SHORT);
        }
        mToast.show();
    }
}
