package com.lll.mediademo.facebook.gles;

import android.opengl.GLES20;
import android.util.Log;

public class ShaderProgram {
    private int shaderProgramHandle;


    private static void checkLocation(int location, String name) {
        if (location >= 0) {
            return;
        }
        throw new RuntimeException("Could not find location for " + name);
    }

    private static int loadShader(int shaderType, String source) {
        int shader = GLES20.glCreateShader(shaderType);
        GLHelpers.checkGLError("glCreateShader type=" + shaderType);
        GLES20.glShaderSource(shader, source);
        GLES20.glCompileShader(shader);

        int[] compiled = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
        if (compiled[0] == 0) {
            GLES20.glDeleteShader(shader);
            shader = 0;
        }
        return shader;
    }

    private static int createProgram(String vertexSource, String fragmentSource) {
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource);
        int pixelShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource);
        int program = GLES20.glCreateProgram();
        GLHelpers.checkGLError("glCreateProgram");
        if (program == 0) {
            return 0;
        }
        GLES20.glAttachShader(program, vertexShader);
        GLHelpers.checkGLError("glAttachShader");
        GLES20.glAttachShader(program, pixelShader);
        GLHelpers.checkGLError("glAttachShader");
        GLES20.glLinkProgram(program);
        int[] linkStatus = new int[1];
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
        if (linkStatus[0] != GLES20.GL_TRUE) {
            GLES20.glDeleteProgram(program);
            program = 0;
        }
        return program;
    }

    public ShaderProgram(String vertexShader, String fragmentShader) {
        shaderProgramHandle = createProgram(vertexShader, fragmentShader);
    }

    public int getShaderHandle() {
        return shaderProgramHandle;
    }

    public void release() {
        GLES20.glDeleteProgram(shaderProgramHandle);
        shaderProgramHandle = -1;
    }

    public int getAttribute(String name) {
        int loc = GLES20.glGetAttribLocation(shaderProgramHandle, name);
        checkLocation(loc, name);
        return loc;
    }

    public int getUniform(String name) {
        int loc = GLES20.glGetUniformLocation(shaderProgramHandle, name);
        checkLocation(loc, name);
        return loc;
    }


}
