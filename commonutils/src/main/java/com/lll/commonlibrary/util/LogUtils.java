package com.lll.commonlibrary.util;

import android.util.Log;

/**
 * Description: 日志工具类
 * Version:
 * Created by lll on 2016/1/22.
 * CopyRight lll
 */
public class LogUtils {


    private static boolean isDebug=false;

    public static void e(String tag,String msg){
        if(isDebug){
            Log.e(tag,msg);
        }
    }


    public static void e(String tag,String msg,Throwable e){
        if(isDebug){
            Log.e(tag,msg,e);
        }
    }

}
