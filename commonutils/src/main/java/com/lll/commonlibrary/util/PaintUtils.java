package com.lll.commonlibrary.util;

import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;

/**
 * Created by root on 15-10-19.
 */
public class PaintUtils {

    private static Paint meausepaint = new Paint();

    private static Paint destin, dstover, alphaPaint, iconPaint;

    /**
     * 获取DST_IN类型画笔，非主线程获取时，注意可能因同步问题产生alpha值不一致！
     *
     * @param alpha 画笔透明度
     * @return Paint
     */
    public static final Paint getDestin(int alpha) {
        if (destin != null) {
            destin.setAlpha(alpha);
            return destin;
        }

        destin = new Paint();
        destin.setAlpha(alpha);
        destin.setFilterBitmap(true);
        destin.setAntiAlias(true);
        destin.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        return destin;
    }

    /**
     * 获取DST_OVER类型画笔，非主线程获取时，注意可能因同步问题产生alpha值不一致！
     *
     * @param alpha 画笔透明度
     * @return Paint
     */
    public static final Paint getDstover(int alpha) {
        if (dstover != null) {
            dstover.setAlpha(alpha);
            return dstover;
        }

        dstover = new Paint();
        dstover.setAlpha(alpha);
        dstover.setFilterBitmap(true);
        dstover.setAntiAlias(true);
        dstover.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OVER));
        return dstover;
    }

    /**
     * 获取画笔，非主线程获取时，注意可能因同步问题产生alpha值不一致！
     *
     * @param alpha 画笔透明度
     * @return Paint
     */
    public static final Paint getStaticAlphaPaint(int alpha) {
        if (alphaPaint != null) {
            alphaPaint.setAlpha(alpha);
            return alphaPaint;
        }

        alphaPaint = new Paint();
        alphaPaint.setAlpha(alpha);
        alphaPaint.setFilterBitmap(true);
        alphaPaint.setAntiAlias(true);
        return alphaPaint;
    }

    /**
     * 获取新画笔
     *
     * @return Paint
     */
    public static final Paint getNewPaint() {
        Paint alphaPaint = new Paint();
        alphaPaint.setAntiAlias(true);
        return alphaPaint;
    }

    /**
     * 获取画笔文字占用空间大小
     *
     * @param size 画笔的文字size
     * @return int
     */
    public static final int getLineHeight(float size) {
        meausepaint.setTextSize(size);
        return meausepaint.getFontMetricsInt(null);
    }

    /**
     * 获取图标画笔
     *
     * @return Paint
     */
    public static final Paint getIconPaint() {
        if (iconPaint != null) {
            return iconPaint;
        }

        iconPaint = new Paint();
        iconPaint.setAntiAlias(true);
        iconPaint.setDither(true);
        return iconPaint;
    }
}
