package com.lll.learn.opengl;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;

/**
 * Version 1.0
 * Created by lll on 18/12/2017.
 * Description
 * copyright generalray4239@gmail.com
 */
public class EGLLearnActivity extends Activity {


    /**
     * EGL是本地平台和OpenGL ES之间的抽象层，其完成了本地相关的环境初始化和上下文控制工作，以保证OpenGL ES的平台无关性
     * 主要包含如下工作：
     * a、 选择显示设备
     * <p>
     * b, 选择像素格式。
     * <p>
     * c, 选择某些特性，比如如果你打算画中国水墨画，你需要额外指定宣纸和毛笔。
     * <p>
     * d, 申请显存。
     * <p>
     * e, 创建上下文（Context），上下文本质上是一组状态的集合，描述了在某个特定时刻系统的状态， 用于处理暂停、恢复、销毁、重建等情况；
     * <p>
     * f, 指定当前的环境为绘制环境 。
     * <p>
     * <p>
     * EGL的基本流程：
     * 1, 选择显示设备display，即上述的a.
     * <p>
     * 2，指定特性，包括上述的像素格式(b)和特定特性(c)，根据指定的特性来获取多个满足这些特性的config（比如你指定RGB中的R为5bits，那么可能会有RGB_565和RGB_555两种像素格式均满足此特性），用户从这些可用的configs中选择一个，根据display和config获取绘制用的buffer(一般为显存），即上述的d。
     * <p>
     * 3，使用display、config、buffer来创建context，及即上述的e.
     * <p>
     * 4, 使用display、buffer、context 设置当前的渲染环境，即上述的f.
     */
    public void printEGL10() {
        EGL10 g10 = (EGL10) EGLContext.getEGL();
        //获取显示设备
        EGLDisplay display = g10.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
        EGLDisplay display2 = g10.eglGetCurrentDisplay();

        // Init
        int[] versionInt = new int[2];
        g10.eglInitialize(display, versionInt); //version中存放EGL 版本号，int[0]为主版本号，int[1]为子版本号

        //获取egl的实现厂商
        String vendor = g10.eglQueryString(display, EGL10.EGL_VENDOR);

        //egl的版本号
        String versionStr = g10.eglQueryString(display, EGL10.EGL_VERSION);

        //egl的扩展
        String extension = g10.eglQueryString(display, EGL10.EGL_EXTENSIONS);


        Log.e("lll", "------EGL10 info----------vendor==" + vendor + "====versionStr==" + versionStr + "=====extension==" + extension);
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        get
    }
}
