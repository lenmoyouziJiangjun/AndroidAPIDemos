package com.lll.mediademo.basicmediadecoder;

import android.animation.TimeAnimator;
import android.app.Activity;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.TextView;

import com.lll.mediademo.R;
import com.lll.mediademo.utils.MediaCodecWrapper;

/**
 * Version 1.0
 * Created by lll on 17/6/7.
 * Description
 * copyright generalray4239@gmail.com
 */
public class BasicMediaDecoderActivity extends Activity {

    private TextureView mPlaybackView;
    private TextView mAttribView;

    private TimeAnimator mTimeAnimator = new TimeAnimator();

    private MediaCodecWrapper mMediaCodecWrapper;
    private MediaExtractor mExtractor = new MediaExtractor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_decoder);
        mPlaybackView = (TextureView) findViewById(R.id.PlaybackView);
        mAttribView = (TextView) findViewById(R.id.AttribView);
        findViewById(R.id.btn_play).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAttribView.setVisibility(View.VISIBLE);
                startPlayback();
                view.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.media_decoder_menu, menu);
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mTimeAnimator != null && mTimeAnimator.isRunning()) {
            mTimeAnimator.end();
        }

        if (mMediaCodecWrapper != null) {
            mMediaCodecWrapper.stopAndRelease();
            mExtractor.release();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_play) {
            item.setEnabled(false);
        }
        return true;
    }

    private void startPlayback() {
        //获取raw目录的资源
        Uri videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.vid_bigbuckbunny);
        try {
            mExtractor.setDataSource(this, videoUri, null);
            int nTracks = mExtractor.getTrackCount();
            // Begin by unselecting all of the tracks in the extractor, so we won't see
            // any tracks that we haven't explicitly selected.
            for (int i = 0; i < nTracks; ++i) {
                mExtractor.unselectTrack(i);
            }

            // Find the first video track in the stream. In a real-world application
            // it's possible that the stream would contain multiple tracks, but this
            // sample assumes that we just want to play the first one.
            for (int i = 0; i < nTracks; ++i) {
                // Try to create a video codec for this track. This call will return null if the
                // track is not a video track, or not a recognized video format. Once it returns
                // a valid MediaCodecWrapper, we can break out of the loop.
                mMediaCodecWrapper = MediaCodecWrapper.fromVideoFormat(mExtractor.getTrackFormat(i),
                        new Surface(mPlaybackView.getSurfaceTexture()));
                if (mMediaCodecWrapper != null) {
                    mExtractor.selectTrack(i);
                    break;
                }
            }
            // By using a {@link TimeAnimator}, we can sync our media rendering commands with
            // the system display frame rendering. The animator ticks as the {@link Choreographer}
            // recieves VSYNC events.
            mTimeAnimator.setTimeListener(new TimeAnimator.TimeListener() {
                                              @Override
                                              public void onTimeUpdate(TimeAnimator timeAnimator, long totalTime, long deltaTime) {
                                                  boolean isEos = ((mExtractor.getSampleFlags() & MediaCodec
                                                          .BUFFER_FLAG_END_OF_STREAM) == MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                                                  if (!isEos) {
                                                      // Try to submit the sample to the codec and if successful advance the
                                                      // extractor to the next available sample to read.
                                                      boolean result = mMediaCodecWrapper.writeSample(mExtractor, false,
                                                              mExtractor.getSampleTime(), mExtractor.getSampleFlags());

                                                      if (result) {
                                                          // Advancing the extractor is a blocking operation and it MUST be
                                                          // executed outside the main thread in real applications.
                                                          mExtractor.advance();
                                                      }
                                                  }
                                                  // Examine the sample at the head of the queue to see if its ready to be
                                                  // rendered and is not zero sized End-of-Stream record.
                                                  MediaCodec.BufferInfo out_bufferInfo = new MediaCodec.BufferInfo();
                                                  mMediaCodecWrapper.peekSample(out_bufferInfo);
                                                  // BEGIN_INCLUDE(render_sample)
                                                  if (out_bufferInfo.size <= 0 && isEos) {
                                                      mTimeAnimator.end();
                                                      mMediaCodecWrapper.stopAndRelease();
                                                      mExtractor.release();
                                                  } else if (out_bufferInfo.presentationTimeUs / 1000 < totalTime) {
                                                      // Pop the sample off the queue and send it to {@link Surface}
                                                      mMediaCodecWrapper.popSample(true);
                                                  }
                                              }
                                          }

            );
            // We're all set. Kick off the animator to process buffers and render video frames as
            // they become available
            mTimeAnimator.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
