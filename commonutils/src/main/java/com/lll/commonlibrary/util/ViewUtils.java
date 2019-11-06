package com.lll.commonlibrary.util;

import android.view.MotionEvent;
import android.view.View;

/**
 * Description: View工具类
 * Version:
 * Created by lll on 2016/2/2.
 * CopyRight lll
 */
public class ViewUtils {


    /**
     * 判断触摸是否在view范围内
     * @param view view控件
     * @param ev 触摸事件
     * @return
     */
    public static boolean inRangeOfView(View view, MotionEvent ev) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        int x = location[0];
        int y = location[1];
        if (ev.getX() < x
                || ev.getX() > (x + view.getWidth())
                || ev.getY() < y
                || ev.getY() > (y + view.getHeight())) {
            return false;
        }
        return true;
    }
}
