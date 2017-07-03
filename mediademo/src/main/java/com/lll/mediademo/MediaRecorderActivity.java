package com.lll.mediademo;

import android.app.Activity;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;

import com.lll.mediademo.utils.CameraHelper;

import java.io.File;
import java.util.List;

/**
 * Version 1.0
 * Created by lll on 17/6/12.
 * Description  录音界面
 * copyright generalray4239@gmail.com
 */
public class MediaRecorderActivity extends Activity {

    private MediaRecorder mMediaRecorder;

    private TextureView mPreView;
    private Button mCaptureButton;
    private Camera mCamera;
    private File mOutputFile;

    private boolean isRecording = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_recorder);
        mPreView = (TextureView) findViewById(R.id.surface_view);
        mCaptureButton = (Button) findViewById(R.id.button_capture);
    }

    private void setCaptureButtonText(String title) {
        mCaptureButton.setText(title);
    }

    public void onCaptureClick(View view) {
        if (isRecording) {
            // BEGIN_INCLUDE(stop_release_media_recorder)

            // stop recording and release camera
            try {
                mMediaRecorder.stop();  // stop the recording
            } catch (RuntimeException e) {
                // RuntimeException is thrown when stop() is called immediately after start().
                // In this case the output file is not properly constructed ans should be deleted.
                //noinspection ResultOfMethodCallIgnored
                mOutputFile.delete();
            }
            releaseMediaRecorder(); // release the MediaRecorder object
            mCamera.lock();         // take camera access back from MediaRecorder

            // inform the user that recording has stopped
            setCaptureButtonText("Capture");
            isRecording = false;
            releaseCamera();
            // END_INCLUDE(stop_release_media_recorder)
        } else {
            new MediaPrepareTask().execute();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onPause() {
        super.onPause();
        if (mMediaRecorder != null) {
            mMediaRecorder.pause();
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onResume() {
        super.onResume();
        if (mMediaRecorder != null) {
            mMediaRecorder.resume();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseMediaRecorder();
        releaseCamera();
    }

    private boolean prepareVideoRecorder() {
        mCamera = CameraHelper.getDefaultCameraInstance();
        // We need to make sure that our preview and recording video size are supported by the
        // camera. Query camera to find all the sizes and choose the optimal size given the
        // dimensions of our preview surface.
        Camera.Parameters parameters = mCamera.getParameters();
        List<Camera.Size> supportPreviewSizes = parameters.getSupportedPreviewSizes();
        List<Camera.Size> supportVideoSizes = parameters.getSupportedVideoSizes();

        Camera.Size optimalSize = CameraHelper.getOptimalVideoSize(supportPreviewSizes, supportVideoSizes, mPreView.getWidth(), mPreView.getHeight());

        //Use the same size for recording profile.
        CamcorderProfile profile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
        profile.videoFrameWidth = optimalSize.width;
        profile.videoFrameHeight = optimalSize.height;

        //likewise for the camera object itself.
        parameters.setPreviewSize(profile.videoFrameWidth, profile.videoFrameHeight);

        mCamera.setParameters(parameters);
        try {
            mCamera.setPreviewTexture(mPreView.getSurfaceTexture());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMediaRecorder = new MediaRecorder();
        // Step 1: Unlock and set camera to MediaRecorder
        mCamera.unlock();
        mMediaRecorder.setCamera(mCamera);

        // Step 2: set sources
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        //Step3 :set profile
        mMediaRecorder.setProfile(profile);

        //Step 4: Set output file;
        mOutputFile = CameraHelper.getOutputMediaFile(CameraHelper.MEDIA_TYPE_VIDEO);
        if (mOutputFile == null) {
            return false;
//            throw new RuntimeException("output file is not exit");
        }
        mMediaRecorder.setOutputFile(mOutputFile.getPath());

        //step5: prepare Configured MediaRecorder;
        try {
            mMediaRecorder.prepare();
        } catch (Exception e) {
            e.printStackTrace();
            releaseMediaRecorder();
            return false;
        }
        return true;
    }

    private void releaseMediaRecorder() {
        if (mMediaRecorder != null) {
            // clear recorder configuration
            mMediaRecorder.reset();
            // release the recorder object
            mMediaRecorder.release();
            mMediaRecorder = null;
            // Lock camera for later use i.e taking it back from MediaRecorder.
            // MediaRecorder doesn't need it anymore and we will release it if the activity pauses.
            mCamera.lock();
        }
    }

    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }

    class MediaPrepareTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            if (prepareVideoRecorder()) {
                mMediaRecorder.start();
                isRecording = true;
            } else {
                releaseMediaRecorder();
                return false;
            }
            return true;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (!result) {
                finish();
            }
            // inform the user that recording has started
            setCaptureButtonText("Stop");
        }
    }

}
