package com.lll.mediademo.facebook.gles;

import android.graphics.SurfaceTexture;
import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLSurface;
import android.opengl.GLUtils;

/**
 * Version 1.0
 * Created by lll on 17/6/9.
 * Description 通俗上讲，OpenGL是一个操作GPU的API，它通过驱动向GPU发送相关指令，控制图形渲染管线状态机的运行状态。
 * 但OpenGL需要本地视窗系统进行交互，这就需要一个中间控制层，最好与平台无关。EGL——因此被独立的设计出来，它作为OpenGL ES和本地窗口的桥梁。
 * <p>
 * EGL 是 OpenGL ES（嵌入式）和底层 Native 平台视窗系统之间的接口。EGL API 是独立于OpenGL ES各版本标准的独立API ，
 * 其主要作用是为OpenGL指令创建 Context 、绘制目标Surface 、配置Framebuffer属性、Swap提交绘制结果等。此外，EGL为GPU厂商和OS窗口系统之间提供了一个标准配置接口。
 * <p>
 * 一般来说，OpenGL ES 图形管线的状态被存储于 EGL 管理的一个Context中。而Frame Buffers 和其他绘制 Surfaces 通过 EGL API进行创建、管理和销毁。
 * EGL 同时也控制和提供了对设备显示和可能的设备渲染配置的访问。
 * <p>
 * EGL标准是C的，在Android系统Java层封装了相关API
 * copyright generalray4239@gmail.com
 */
public class EGLRenderTarget {

    //系统显示 ID 或句柄，可以理解为一个前端的显示窗口
    private EGLDisplay mEGLDisplay;
    //Surface的EGL配置，可以理解为绘制目标framebuffer的配置属性
    private EGLConfig mEGLConfig;
    //OpenGL ES 图形上下文，它代表了OpenGL状态机；如果没有它，OpenGL指令就没有执行的环境
    private EGLContext mEGLContext;
    //系统窗口或 frame buffer 句柄 ，可以理解为一个后端的渲染目标窗口
    private EGLSurface mEGLSurface;

    public EGLRenderTarget() {
        init();
    }

    public void init() {
        mEGLDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);
        if (mEGLDisplay == EGL14.EGL_NO_DISPLAY) {
            abortWithEGLError("eglGetDisplay");
        }
        int[] version = new int[2];
        if (!EGL14.eglInitialize(mEGLDisplay, version, 0, version, 1)) {
            abortWithEGLError("eglInitialize");
        }
        final int[] attributeList = {
                EGL14.EGL_RED_SIZE, 8,
                EGL14.EGL_GREEN_SIZE, 8,
                EGL14.EGL_BLUE_SIZE, 8,
                EGL14.EGL_ALPHA_SIZE, 8,
                EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
                EGL14.EGL_NONE
        };
        EGLConfig[] configs = new EGLConfig[1];
        int[] numConfigs = new int[1];
        if (!EGL14.eglChooseConfig(mEGLDisplay, attributeList, 0, configs, 0, configs.length, numConfigs, 0)) {
            abortWithEGLError("eglChooseConfig");
        }

        if (numConfigs[0] <= 0) {
            abortWithEGLError("No EGL config found for attribute list");
        }

        mEGLConfig = configs[0];

        int[] contextAttribs = {
                EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,
                EGL14.EGL_NONE
        };
        mEGLContext = EGL14.eglCreateContext(mEGLDisplay, mEGLConfig, EGL14.EGL_NO_CONTEXT, contextAttribs, 0);
    }

    public void createRenderSurface(SurfaceTexture surfaceTexture) {
        if (!hasValidContext()) {
            init();
        }
        int[] surfaceAttribs = {
                EGL14.EGL_NONE
        };
        mEGLSurface = EGL14.eglCreateWindowSurface(mEGLDisplay, mEGLConfig, surfaceTexture, surfaceAttribs, 0);
        if (mEGLSurface == null || mEGLSurface == EGL14.EGL_NO_SURFACE) {
            abortWithEGLError("eglCreateWindowSurface");
        }
        makeCurrent();
    }

    public void swapBuffers() {
        if (!EGL14.eglSwapBuffers(mEGLDisplay, mEGLSurface)) {
            abortWithEGLError("eglSwapBuffers");
        }
    }

    public boolean hasValidContext() {
        return mEGLContext != EGL14.EGL_NO_CONTEXT;
    }

    public void release() {
        EGL14.eglDestroySurface(mEGLDisplay, mEGLSurface);
        EGL14.eglDestroyContext(mEGLDisplay, mEGLContext);
        mEGLDisplay = EGL14.EGL_NO_DISPLAY;
        mEGLSurface = EGL14.EGL_NO_SURFACE;
        mEGLContext = EGL14.EGL_NO_CONTEXT;
    }

    public void makeCurrent() {
        if (!EGL14.eglMakeCurrent(mEGLDisplay, mEGLSurface, mEGLSurface, mEGLContext)) {
            abortWithEGLError("eglMakeCurrent");
        }
    }

    private void abortWithEGLError(String msg) {
        int error = EGL14.eglGetError();
        throw new RuntimeException(msg + ": EGL error: 0x" +
                Integer.toHexString(error) + ": " +
                GLUtils.getEGLErrorString(error));
    }

}
