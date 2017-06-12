package com.lll.mediademo.facebook;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Choreographer;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;

import com.lll.mediademo.facebook.gles.EGLRenderTarget;
import com.lll.mediademo.facebook.gles.GLHelpers;
import com.lll.mediademo.facebook.gles.SphericalSceneRenderer;

public class SphericalVideoPlayer extends TextureView {
    private static final String RENDER_THREAD_NAME = "360RenderThread";
    private MediaPlayer mVideoPlayerInternal;
    private RenderThread mRenderThread;
    private String mVideoPath;
    private boolean mReadyToPlay;
    private GestureDetector mGestureDetector;

    private GestureDetector.SimpleOnGestureListener mDragListener = new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onDown(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (mRenderThread == null) {
                return false;
            }
            Message msg = Message.obtain();
            msg.what = RenderThread.MSG_ON_SCROLL;
            msg.obj = new ScrollDeltaHolder(distanceX, distanceY);
            mRenderThread.handler.sendMessage(msg);
            return true;
        }
    };

    private class ScrollDeltaHolder {
        float deltaX, deltaY;

        ScrollDeltaHolder(float dx, float dy) {
            deltaX = dx;
            deltaY = dy;
        }
    }


    public SphericalVideoPlayer(Context context) {
        this(context, null);
    }

    public SphericalVideoPlayer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SphericalVideoPlayer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mGestureDetector = new GestureDetector(context, mDragListener);

        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return mGestureDetector.onTouchEvent(motionEvent);
            }
        });
    }

    public void initRenderThread(SurfaceTexture surfaceTexture, int width, int height) {
        mRenderThread = new RenderThread(RENDER_THREAD_NAME);
        mRenderThread.start();

        Message msg = Message.obtain();
        msg.what = RenderThread.MSG_SURFACE_AVAILABLE;
        msg.obj = surfaceTexture;
        msg.arg1 = width;
        msg.arg2 = height;
        mRenderThread.handler.sendMessage(msg);
    }

    public void setVideoURLPath(String path) {
        mVideoPath = path;
    }

    public void playWhenReady() {
        // Wait for render surface creation to start preparing the video.
        mReadyToPlay = true;
    }

    private void prepareVideo(String videoPath) {
        if (mRenderThread == null) {
            throw new IllegalStateException("RenderThread has not been initialized");
        }
        if (TextUtils.isEmpty(videoPath)) {
            throw new RuntimeException("Cannot begin playback: video path is empty");
        }
        try {
            mVideoPlayerInternal = new MediaPlayer();
            mVideoPlayerInternal.setSurface(mRenderThread.getVideoDecodeSurface());
            mVideoPlayerInternal.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mVideoPlayerInternal.setDataSource(getContext(), Uri.parse(videoPath), null);
            mVideoPlayerInternal.setLooping(true);

            mVideoPlayerInternal.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    play();
                }
            });
            mVideoPlayerInternal.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
                @Override
                public void onBufferingUpdate(MediaPlayer mp, int percent) {

                }
            });
            mVideoPlayerInternal.prepareAsync();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void play() {
        if (!mVideoPlayerInternal.isPlaying()) {
            mVideoPlayerInternal.start();
        }
    }

    public void releaseResources() {
        mRenderThread.handler.sendEmptyMessage(RenderThread.MSG_SURFACE_DESTROYED);
    }

    /**
     * RenderThread waits for the SphericalVideoPlayer's SurfaceTexture to be
     * available then sets up a GL rendering context, creates an external
     * texture for the video decoder to output to, asks for vsync updates from
     * the Choreographer, attaches a frame available listener the video decode
     * SurfaceTexture, then begins video playback.
     * <p>
     * Drag events from the main thread will be forwarded to the RenderThread's
     * message queue so that it may update the view state.
     * <p>
     * SphericalSceneRenderer draws the 360 video scene each frame using the
     * latest latched video texture frame.
     */
    private class RenderThread extends HandlerThread {
        private static final int MSG_SURFACE_AVAILABLE = 0x1;
        private static final int MSG_VSYNC = 0x2;
        private static final int MSG_FRAME_AVAILABLE = 0x3;
        private static final int MSG_SURFACE_DESTROYED = 0x4;
        private static final int MSG_ON_SCROLL = 0x5;

        private static final float FOVY = 70f;
        private static final float Z_NEAR = 1f;
        private static final float Z_FAR = 1000f;
        private static final float DRAG_FRICTION = 0.1f;
        private static final float INITIAL_PITCH_DEGREES = 90.f;

        private Handler handler;
        private EGLRenderTarget eglRenderTarget;
        private SurfaceTexture videoSurfaceTexture;
        private int videoDecodeTextureId = -1;

        private float[] videoTextureMatrix = new float[16];
        private float[] modelMatrix = new float[16];
        private float[] viewMatrix = new float[16];
        private float[] projectionMatrix = new float[16];

        private float[] camera = new float[3];
        private float lon;
        private float lat;

        private boolean frameAvailable;
        private boolean pendingCameraUpdate;

        private SphericalSceneRenderer renderer;

        /**
         * Choreographer：Choreographer这个类来控制同步处理输入(Input)、动画(Animation)、绘制(Draw)三个UI操作
         * 参考网站：http://www.jianshu.com/p/996bca12eb1d
         * <p>
         * 加密刷新回调
         */
        private Choreographer.FrameCallback frameCallback = new Choreographer.FrameCallback() {
            @Override
            public void doFrame(long l) {
                handler.sendEmptyMessage(MSG_VSYNC);
            }
        };

        public RenderThread(String name) {
            super(name);
            eglRenderTarget = new EGLRenderTarget();
        }

        public synchronized void start() {
            super.start();
            handler = new Handler(getLooper()) {
                @Override
                public void handleMessage(Message msg) {
                    switch (msg.what) {
                        case MSG_SURFACE_AVAILABLE:
                            onSurfaceAvailable((SurfaceTexture) msg.obj, msg.arg1, msg.arg2);
                            break;
                        case MSG_VSYNC:
                            onVSync();
                            break;
                        case MSG_FRAME_AVAILABLE:
                            onFrameAvailable();
                            break;
                        case MSG_SURFACE_DESTROYED:
                            onSurfaceDestroyed();
                            break;
                        case MSG_ON_SCROLL:
                            onScroll((ScrollDeltaHolder) msg.obj);
                            break;
                    }
                }
            };
        }

        private Surface getVideoDecodeSurface() {
            if (!eglRenderTarget.hasValidContext()) {
                throw new IllegalStateException(
                        "Cannot get video decode surface without GL context");
            }
            videoDecodeTextureId = GLHelpers.generateExternalTexture();
            videoSurfaceTexture = new SurfaceTexture(videoDecodeTextureId);
            videoSurfaceTexture.setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
                @Override
                public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                    handler.sendEmptyMessage(RenderThread.MSG_FRAME_AVAILABLE);
                }
            });
            return new Surface(videoSurfaceTexture);
        }

        private void onSurfaceAvailable(SurfaceTexture surfaceTexture, int width, int height) {
            eglRenderTarget.createRenderSurface(surfaceTexture);

            Choreographer.getInstance().postFrameCallback(frameCallback);

            GLES20.glViewport(0, 0, width, height);
            GLHelpers.checkGLError("glViewport");

            float aspectRatio = (float) width / height;
            Matrix.perspectiveM(projectionMatrix, 0, FOVY, aspectRatio, Z_NEAR, Z_FAR);
            Matrix.setIdentityM(viewMatrix, 0);
            Matrix.setRotateM(modelMatrix, 0, INITIAL_PITCH_DEGREES, 1, 0, 0);

            GLES20.glClearColor(1.0f, 0.f, 0.f, 1.f);

            renderer = new SphericalSceneRenderer(getContext());

            if (mReadyToPlay) {
                prepareVideo(mVideoPath);
            }
        }

        private void onVSync() {
            if (!eglRenderTarget.hasValidContext()) {
                return;
            }

            Choreographer.getInstance().postFrameCallback(frameCallback);

            // We only redraw when there's a new video frame or if a drag event happened.
            if (!frameAvailable && !pendingCameraUpdate) {
                return;
            }

            eglRenderTarget.makeCurrent();
            // Have to be sure to balance onFrameAvailable and updateTexImage calls so that
            // the internal queue buffers will be freed. For this example, we can rely on the
            // display refresh rate being higher or equal to the video frame rate (sample is 30fps).
            videoSurfaceTexture.updateTexImage();
            videoSurfaceTexture.getTransformMatrix(videoTextureMatrix);

            updateCamera();

            renderer.onDrawFrame(videoDecodeTextureId, videoTextureMatrix, modelMatrix, viewMatrix, projectionMatrix);

            eglRenderTarget.swapBuffers();

            if (frameAvailable) {
                frameAvailable = false;
            }
            if (pendingCameraUpdate) {
                pendingCameraUpdate = false;
            }
        }

        private void updateCamera() {
            lat = Math.max(-85, Math.min(85, lat));

            float phi = (float) Math.toRadians(90 - lat);
            float theta = (float) Math.toRadians(lon);

            camera[0] = (float) (100.f * Math.sin(phi) * Math.cos(theta));
            camera[1] = (float) (100.f * Math.cos(phi));
            camera[2] = (float) (100.f * Math.sin(phi) * Math.sin(theta));

            Matrix.setLookAtM(
                    viewMatrix, 0,
                    camera[0], camera[1], camera[2],
                    0, 0, 0,
                    0, 1, 0
            );
        }

        private void onFrameAvailable() {
            frameAvailable = true;
        }

        private void onSurfaceDestroyed() {
            if (mVideoPlayerInternal != null) {
                mVideoPlayerInternal.stop();
                mVideoPlayerInternal.release();
                mVideoPlayerInternal = null;
            }

            if (videoDecodeTextureId != -1) {
                int[] textures = new int[1];
                textures[0] = videoDecodeTextureId;
                GLES20.glDeleteTextures(1, textures, 0);
                videoDecodeTextureId = -1;
            }

            if (videoSurfaceTexture != null) {
                videoSurfaceTexture.release();
                videoSurfaceTexture = null;
                frameAvailable = false;
            }

            pendingCameraUpdate = false;

            eglRenderTarget.release();
            renderer.release();
        }

        private void onScroll(ScrollDeltaHolder deltaHolder) {
            lon = (deltaHolder.deltaX) * DRAG_FRICTION + lon;
            lat = -(deltaHolder.deltaY) * DRAG_FRICTION + lat;
            pendingCameraUpdate = true;
        }
    }
}
