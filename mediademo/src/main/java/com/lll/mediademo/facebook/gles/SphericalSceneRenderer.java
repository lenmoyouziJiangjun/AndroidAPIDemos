package com.lll.mediademo.facebook.gles;

import android.content.Context;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.Matrix;

import com.lll.mediademo.R;
import com.lll.mediademo.facebook.SphericalPlayerActivity;

public class SphericalSceneRenderer {
    public static final int SPHERE_SLICES = 180;
    private static final int SPHERE_INDICES_PER_VERTEX = 1;
    private static final float SPHERE_RADIUS = 500.0f;

    private ShaderProgram mShaderProgram;

    private int aPositionLocation;
    private int uMVPMatrixLocation;
    private int uTextureMatrixLocation;
    private int aTextureCoordLocation;

    private float[] pvMatrix = new float[16];
    private float[] mvpMatrix = new float[16];

    private Sphere mSphere;

    public SphericalSceneRenderer(Context context) {
        mShaderProgram = new ShaderProgram(SphericalPlayerActivity.readRawTextFile(context, R.raw.video_vertex_shader),
                SphericalPlayerActivity.readRawTextFile(context, R.raw.video_fragment_shader));

        aPositionLocation = mShaderProgram.getAttribute("aPosition");
        uMVPMatrixLocation = mShaderProgram.getUniform("uMVPMatrix");
        uTextureMatrixLocation = mShaderProgram.getUniform("uTextureMatrix");
        aTextureCoordLocation = mShaderProgram.getAttribute("aTextureCoord");

        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        mSphere = new Sphere(SPHERE_SLICES, 0.f, 0.f, 0.f, SPHERE_RADIUS, SPHERE_INDICES_PER_VERTEX);

        GLES20.glUseProgram(mShaderProgram.getShaderHandle());
        GLES20.glEnableVertexAttribArray(aPositionLocation);
        GLHelpers.checkGLError("glEnableVertexAttribArray");
    }

    public void onDrawFrame(int textureId, float[] textureMatrix,
                            float[] modelMatrix,
                            float[] viewMatrix,
                            float[] projectionMatrix) {
        Matrix.translateM(textureMatrix, 0, 0, 1, 0);

        Matrix.multiplyMM(pvMatrix, 0, projectionMatrix, 0, viewMatrix, 0);
        Matrix.multiplyMM(mvpMatrix, 0, pvMatrix, 0, modelMatrix, 0);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        GLES20.glBindTexture(
                GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                textureId);

        GLES20.glUniformMatrix4fv(uTextureMatrixLocation, 1, false, textureMatrix, 0);
        GLES20.glUniformMatrix4fv(uMVPMatrixLocation, 1, false, mvpMatrix, 0);
        for (int j = 0; j < mSphere.getNumIndices().length; ++j) {
            GLES20.glDrawElements(GLES20.GL_TRIANGLES,
                    mSphere.getNumIndices()[j], GLES20.GL_UNSIGNED_SHORT,
                    mSphere.getIndices()[j]);
        }
    }

    public void release() {
        mShaderProgram.release();
    }
}
