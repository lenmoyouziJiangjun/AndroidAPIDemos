package com.lll.mediademo.facebook.gles;

import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.util.Log;

public class GLHelpers {
    private static final String TAG = GLHelpers.class.getSimpleName();

    public static int generateExternalTexture() {
        int externalTextured = -1;
        int[] textures = new int[1];
        try {
            GLES20.glGenTextures(1, textures, 0);
            externalTextured = textures[0];
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                    externalTextured);
            GLES20.glTexParameterf(
                    GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                    GLES20.GL_TEXTURE_MIN_FILTER,
                    GLES20.GL_LINEAR);
            GLES20.glTexParameterf(
                    GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                    GLES20.GL_TEXTURE_MAG_FILTER,
                    GLES20.GL_LINEAR);
            GLES20.glTexParameteri(
                    GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                    GLES20.GL_TEXTURE_WRAP_S,
                    GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameteri(
                    GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                    GLES20.GL_TEXTURE_WRAP_T,
                    GLES20.GL_CLAMP_TO_EDGE);
        } catch (Exception e) {
            e.printStackTrace();
            if (externalTextured != -1) {
                GLES20.glDeleteTextures(1, textures, 0);
            }
            return -1;
        }
        return externalTextured;
    }

    public static void checkGLError(String op) {
        int error = GLES20.glGetError();
        if (error == GLES20.GL_NO_ERROR) {
            return;
        }
        String msg = op + ": glError 0x" + Integer.toHexString(error);
        Log.e(TAG, msg);
        throw new RuntimeException(msg);
    }
}
