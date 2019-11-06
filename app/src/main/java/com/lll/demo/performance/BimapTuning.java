package com.lll.demo.performance;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Version 1.0
 * Created by lll on 19/12/2017.
 * Description
 * copyright generalray4239@gmail.com
 */
public class BimapTuning {

    /**
     * 使用decodeStream 代替decodeFile
     * @param bitmapPath
     * @return
     * @throws IOException
     */
    public Bitmap getBitmap(String bitmapPath) throws IOException{
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(bitmapPath));
        return BitmapFactory.decodeStream(bis);
    }

    public void getMaxMamory(){
        Log.e("lll","最大堆内存为==="+Runtime.getRuntime().maxMemory());
        Log.e("lll","总共堆内存为==="+Runtime.getRuntime().totalMemory());
    }

}
