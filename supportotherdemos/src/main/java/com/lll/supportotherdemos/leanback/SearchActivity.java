package com.lll.supportotherdemos.leanback;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v17.leanback.widget.SpeechRecognitionCallback;
import android.util.Log;

import com.lll.supportotherdemos.R;
import com.lll.supportotherdemos.leanback.fragment.SearchFragment;

/**
 * Version 1.0
 * Created by lll on 17/7/21.
 * Description
 * copyright generalray4239@gmail.com
 */
public class SearchActivity extends Activity {

    private static final String TAG = "SearchActivity";
    private static boolean DEBUG = true;

    /**
     * If using internal speech recognizer, you must have RECORD_AUDIO permission
     */
    private static boolean USE_INTERNAL_SPEECH_RECOGNIZER = true;
    private static final int REQUEST_SPEECH = 1;

    private SearchFragment mFragment;
    private SpeechRecognitionCallback mSpeechRecognitionCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        mFragment = (SearchFragment) getFragmentManager().findFragmentById(R.id.search_fragment);

        if(!USE_INTERNAL_SPEECH_RECOGNIZER){
            mSpeechRecognitionCallback = new SpeechRecognitionCallback() {
                @Override
                public void recognizeSpeech() {
                    if (DEBUG) Log.v(TAG, "recognizeSpeech");
                    startActivityForResult(mFragment.getRecognizerIntent(), REQUEST_SPEECH);
                }
            };
            mFragment.setSpeechRecognitionCallback(mSpeechRecognitionCallback);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (DEBUG) Log.v(TAG, "onActivityResult requestCode=" + requestCode +
                " resultCode=" + resultCode +
                " data=" + data);
        if (requestCode == REQUEST_SPEECH && resultCode == RESULT_OK) {
            mFragment.setSearchQuery(data, true);
        }
    }
}
