package com.lll.supportotherdemos.leanback;

import android.app.Activity;
import android.os.Bundle;

import com.lll.supportotherdemos.R;
import com.lll.supportotherdemos.leanback.fragment.PlaybackOverlayFragment;

import java.util.ArrayList;
import java.util.List;

public class PlaybackOverlayActivity extends Activity {

    private List<PictureInPictureListener> mListeners = new ArrayList<>();

    public interface PictureInPictureListener {
        void onPictureInPictureModeChanged(boolean isInPictureInPictureMode);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_framelayout);
        getFragmentManager().beginTransaction().add(R.id.fl_container, new PlaybackOverlayFragment()).commit();
    }

    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode) {
        for (PictureInPictureListener listener : mListeners) {
            listener.onPictureInPictureModeChanged(isInPictureInPictureMode);
        }
    }

    public void registerPictureInPictureListener(PictureInPictureListener listener) {
        mListeners.add(listener);
    }

    public void unregisterPictureInPictureListener(PictureInPictureListener listener) {
        mListeners.remove(listener);
    }
}
