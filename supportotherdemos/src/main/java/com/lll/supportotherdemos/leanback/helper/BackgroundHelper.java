package com.lll.supportotherdemos.leanback.helper;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v17.leanback.app.BackgroundManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;

/**
 * Version 1.0
 * Created by lll on 17/7/21.
 * Description
 * copyright generalray4239@gmail.com
 */
public class BackgroundHelper {
    private static final String TAG = "BackgroundHelper";
    private static final boolean DEBUG = false;
    private static final boolean ENABLED = true;

    // Background delay serves to avoid kicking off expensive bitmap loading
    // in case multiple backgrounds are set in quick succession.
    private static final int SET_BACKGROUND_DELAY_MS = 100;

    private Handler mHandler = new Handler();
    private LoadBackgroundRunnable mRunnable;
    private LoadBitmapTask mTask;

    static class Request {
        Object mImageToken;
        Activity mActivity;
        Bitmap mResult;

        Request(Activity activity, Object imageToken) {
            this.mActivity = activity;
            mImageToken = imageToken;
        }
    }

    public BackgroundHelper() {
        if (DEBUG && !ENABLED) {
            Log.v(TAG, "BackgroundHelper: disabled");
        }
    }

    public void setBackground(Activity activity, Object imageToken) {
        if (!ENABLED) {
            return;
        }
        if (mRunnable != null) {
            mHandler.removeCallbacks(mRunnable);
        }
        mRunnable = new LoadBackgroundRunnable(activity, imageToken);
        mHandler.postDelayed(mRunnable, SET_BACKGROUND_DELAY_MS);
    }

    public static void attach(Activity activity) {
        if (!ENABLED) {
            return;
        }
        BackgroundManager.getInstance(activity).attach(activity.getWindow());
    }

    public static void release(Activity activity) {
        if (!ENABLED) {
            return;
        }
        BackgroundManager.getInstance(activity).release();
    }

    class LoadBackgroundRunnable implements Runnable {
        Request mRequest;

        LoadBackgroundRunnable(Activity activity, Object imageToken) {
            mRequest = new Request(activity, imageToken);
        }

        @Override
        public void run() {
            if (mTask != null) {
                mTask.cancel(true);
            }
            mTask = new LoadBitmapTask();
            mTask.execute(mRequest);
            mRunnable = null;
        }
    }

    class LoadBitmapTask extends AsyncTask<Request, Object, Request> {

        @Override
        protected Request doInBackground(Request... params) {
            boolean cancelled = isCancelled();
            Request request = params[0];
            if (!cancelled) {
                request.mResult = loadBitmap(request.mActivity, request.mImageToken);
            }
            return request;
        }

        @Override
        protected void onPostExecute(Request request) {
            applyBackground(request.mActivity, request.mResult);
            if (mTask == this) {
                mTask = null;
            }
        }

        @Override
        protected void onCancelled() {
            if (DEBUG) Log.v(TAG, "onCancelled");
        }

        /**
         * 加载本地的BitmapDrawable 资源
         *
         * @param activity
         * @param imageToken
         * @return
         */
        private Bitmap loadBitmap(Activity activity, Object imageToken) {
            if (imageToken instanceof Integer) {//资源id
                final int resourceId = (int) imageToken;
                if (DEBUG) Log.v(TAG, "load resourceId " + resourceId);
                Drawable drawable = ContextCompat.getDrawable(activity, resourceId);
                if (drawable instanceof BitmapDrawable) {
                    return ((BitmapDrawable) drawable).getBitmap();
                }
            }
            return null;
        }

        private void applyBackground(Activity activity, Bitmap bitmap) {
            BackgroundManager backgroundManager = BackgroundManager.getInstance(activity);
            if (backgroundManager == null || !backgroundManager.isAttached()) {
                return;
            }
            backgroundManager.setBitmap(bitmap);
        }
    }
}
