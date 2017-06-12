package com.lll.mediademo.facebook.gles;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class Sphere {
    public static final int FLOAT_SIZE = 4;
    public static final int SHORT_SIZE = 2;

    private FloatBuffer mVertices;
    private ShortBuffer[] mIndices;
    private int[] mNumIndices;
    private int mTotalIndices;

    public FloatBuffer getVertices() {
        return mVertices;
    }

    public int getVerticesStride() {
        return 5 * FLOAT_SIZE;
    }

    public ShortBuffer[] getIndices() {
        return mIndices;
    }

    public int[] getNumIndices() {
        return mNumIndices;
    }

    public int getTotalIndices() {
        return mTotalIndices;
    }

    private int max(int[] array) {
        int max = array[0];
        for (int i = 1; i < array.length; i++) {
            if (array[i] > max) max = array[i];
        }
        return max;
    }

    /**
     * @param nSlices         how many slice in horizontal direction.
     *                        The same slice for vertical direction is applied.
     *                        nSlices should be > 1 and should be <= 180
     * @param x
     * @param y
     * @param z
     * @param r
     * @param numIndexBuffers
     */
    public Sphere(int nSlices, float x, float y, float z, float r, int numIndexBuffers) {
        int iMax = nSlices + 1;
        int nVertices = iMax * iMax;
        if (nVertices > Short.MAX_VALUE) {
            // this cannot be handled in one vertices / indices pair
            throw new RuntimeException("nSlices " + nSlices + " too big for vertex");
        }
        mTotalIndices = nSlices * nSlices * 6;
        float angleStepI = ((float) Math.PI / nSlices);
        float angleStepJ = ((2.0f * (float) Math.PI) / nSlices);
        // 3 vertex coords + 2 texture coords
        mVertices = ByteBuffer.allocateDirect(nVertices * 5 * FLOAT_SIZE)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mIndices = new ShortBuffer[numIndexBuffers];
        mNumIndices = new int[numIndexBuffers];
        // first evenly distribute to n-1 buffers, then put remaining ones to the last one.
        int noIndicesPerBuffer = (mTotalIndices / numIndexBuffers / 6) * 6;
        for (int i = 0; i < numIndexBuffers - 1; i++) {
            mNumIndices[i] = noIndicesPerBuffer;
        }
        mNumIndices[numIndexBuffers - 1] = mTotalIndices - noIndicesPerBuffer *
                (numIndexBuffers - 1);
        for (int i = 0; i < numIndexBuffers; i++) {
            mIndices[i] = ByteBuffer.allocateDirect(mNumIndices[i] * SHORT_SIZE)
                    .order(ByteOrder.nativeOrder()).asShortBuffer();
        }
        // calling put for each float took too much CPU time, so put by line instead
        float[] vLineBuffer = new float[iMax * 5];
        for (int i = 0; i < iMax; i++) {
            for (int j = 0; j < iMax; j++) {
                int vertexBase = j * 5;
                float sini = (float) Math.sin(angleStepI * i);
                float sinj = (float) Math.sin(angleStepJ * j);
                float cosi = (float) Math.cos(angleStepI * i);
                float cosj = (float) Math.cos(angleStepJ * j);
                // vertex x,y,z
                vLineBuffer[vertexBase + 0] = x + r * sini * sinj;
                vLineBuffer[vertexBase + 1] = y + r * sini * cosj;
                vLineBuffer[vertexBase + 2] = z + r * cosi;
                // texture s,t
                vLineBuffer[vertexBase + 3] = (float) j / (float) nSlices;
                vLineBuffer[vertexBase + 4] = (1.0f - i) / (float) nSlices;
            }
            mVertices.put(vLineBuffer, 0, vLineBuffer.length);
        }
        short[] indexBuffer = new short[max(mNumIndices)];
        int index = 0;
        int bufferNum = 0;
        for (int i = 0; i < nSlices; i++) {
            for (int j = 0; j < nSlices; j++) {
                int i1 = i + 1;
                int j1 = j + 1;
                if (index >= mNumIndices[bufferNum]) {
                    // buffer ready for moving to target
                    mIndices[bufferNum].put(indexBuffer, 0, mNumIndices[bufferNum]);
                    // move to the next one
                    index = 0;
                    bufferNum++;
                }
                indexBuffer[index++] = (short) (i * iMax + j);
                indexBuffer[index++] = (short) (i1 * iMax + j);
                indexBuffer[index++] = (short) (i1 * iMax + j1);
                indexBuffer[index++] = (short) (i * iMax + j);
                indexBuffer[index++] = (short) (i1 * iMax + j1);
                indexBuffer[index++] = (short) (i * iMax + j1);
            }
        }
        mIndices[bufferNum].put(indexBuffer, 0, mNumIndices[bufferNum]);
        mVertices.position(0);
        for (int i = 0; i < numIndexBuffers; i++) {
            mIndices[i].position(0);
        }
    }
}
