package com.lll.demo.performance;

import android.util.Log;

import java.io.BufferedReader;
import java.io.FileReader;

/**
 * Version 1.0
 * Created by lll on 19/12/2017.
 * Description
 * 文件性能调优
 * 1、对于配置、常量之类需要多次读取而文件内容又不变的文件。读取一次，保存到内存中。
 * <p>
 * copyright generalray4239@gmail.com
 */
public class FileIOTuning {
    public static String PROCESS_NAME;

    /**
     * 获取常量配置
     */
    public static void getCpuInfo() {
        try {
            FileReader fr = new FileReader("/proc/cpuinfo");//获取系统CPU信息
            BufferedReader br = new BufferedReader(fr);//使用缓冲区

            String text = br.readLine();
            while (text != null) {
                Log.d("lll", "getCpuInfo: -------" + text);
                if (text.startsWith("Processor")) {
                    int index = text.indexOf(":");
                    if (index > -1) {
                        PROCESS_NAME = text.substring(index + 1, text.length());
                    }
                }
                text = br.readLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
