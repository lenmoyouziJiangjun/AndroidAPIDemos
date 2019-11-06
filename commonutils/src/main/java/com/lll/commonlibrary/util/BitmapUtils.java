package com.lll.commonlibrary.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * Created by root on 15-10-19.
 */
public class BitmapUtils {


    /**
     * 设置圆角图片
     *
     * @param bitmap
     * @param roundPixels
     * @return
     */
    public static Bitmap toRoundCorner(Bitmap bitmap, int roundPixels) {
        if (bitmap == null)
            return null;

        final Rect srcRect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final Rect destRect = new Rect(srcRect);
        return toRoundCorner(bitmap, roundPixels, srcRect, destRect);
    }

    public static Bitmap toRoundCorner(Bitmap bitmap, int roundPixels, Rect srcRect, Rect destRect) {
        if (bitmap == null)
            return null;
        Bitmap rcBmp = null;
        try {
            rcBmp = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        } catch (Exception e) {
            e.printStackTrace();
            return rcBmp;
        }

        rcBmp.eraseColor(Color.TRANSPARENT);
        Canvas canvas = new Canvas(rcBmp);


        if (srcRect == null)
            srcRect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        if (destRect == null)
            destRect = new Rect(srcRect);
        final RectF destRectF = new RectF(destRect);
        ;
        final float roundPx = roundPixels;

        final Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(0);
        canvas.drawRoundRect(destRectF, roundPx, roundPx, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, srcRect, destRect, paint);
        return rcBmp;
    }


    //图片压缩
    public Bitmap compressImage(Bitmap image) {

        BitmapFactory.Options option = new BitmapFactory.Options();
        option.inPreferredConfig = Bitmap.Config.RGB_565;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos); //质量压缩方法，这里100表示不压缩，把压缩后的数据存放到 baos中
        int options = 100;
        while (baos.toByteArray().length / 1024 > 100) { //循环判断如果压缩后图片是否大于100kb,大于继续压缩
            baos.reset(); //重置baos 即清空baos
            options -= 10; //每次都减少10
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);//这里压缩options%，把压缩后的数据存放到 baos中
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray()); //把压缩后的数据 baos存放到ByteArrayInputStream中
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, option);//把ByteArrayInputStream数据生成图片
        return bitmap;
    }


    /**
     * 联系人头像蒙板合成
     */
    public static Drawable ComposeMaskIcon(Context context, Drawable icon, int backgroundId, int foregroundId) {
        Drawable drawableBackground = context.getResources().getDrawable(backgroundId);
        Drawable drawableForeground = context.getResources().getDrawable(foregroundId);

        if (drawableBackground != null && drawableForeground != null) {
            BitmapDrawable bgDrawable = (BitmapDrawable) drawableBackground;
            Bitmap bgBitmap = bgDrawable.getBitmap();

            BitmapDrawable fgDrawable = (BitmapDrawable) drawableForeground;
            Bitmap fgBitmap = fgDrawable.getBitmap();

            BitmapDrawable originDrawable = (BitmapDrawable) icon;
            Bitmap originBitmap = originDrawable.getBitmap();

            if (bgBitmap == null || bgBitmap.isRecycled() || fgBitmap == null || fgBitmap.isRecycled() || originBitmap == null || originBitmap.isRecycled()) {
                return null;
            }

            bgBitmap = Bitmap.createScaledBitmap(bgBitmap, originBitmap.getWidth(), originBitmap.getHeight(), false);
            fgBitmap = Bitmap.createScaledBitmap(fgBitmap, originBitmap.getWidth(), originBitmap.getHeight(), false);

            Bitmap composedBitmap = Bitmap.createBitmap(originBitmap.getWidth(), originBitmap.getHeight(), Bitmap.Config.ARGB_8888);
            if (composedBitmap == null || composedBitmap.isRecycled()) {
                return null;
            }

            Canvas cv = new Canvas(composedBitmap);
            cv.drawBitmap(originBitmap, 0, 0, PaintUtils.getIconPaint());
            cv.drawBitmap(bgBitmap, 0, 0, PaintUtils.getDestin(255));
            cv.drawBitmap(fgBitmap, 0, 0, PaintUtils.getIconPaint());
            cv.save(Canvas.ALL_SAVE_FLAG);
            cv.restore();

            return new BitmapDrawable(composedBitmap);
        }

        return icon;
    }

    public static Bitmap loadBitmapFormSD(String filePath) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 8;
        Bitmap bitmap = BitmapFactory.decodeFile(filePath, options);
        return bitmap;
    }
}
