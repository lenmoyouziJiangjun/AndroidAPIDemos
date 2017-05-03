package com.lll.supportotherdemos.transition;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.transition.Scene;
import android.transition.TransitionManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.lll.supportotherdemos.R;

public abstract class SceneUsageBase extends TransitionUsageBase {

    private Scene[] mScenes;
    private int mCurrentScene;

    abstract Scene[] setUpScenes(ViewGroup root);

    abstract void go(Scene scene);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout root = (FrameLayout) findViewById(R.id.root);
        mScenes = setUpScenes(root);
        TransitionManager.go(mScenes[0]);
    }

    @Override
    int getLayoutResId() {
        return R.layout.scene_usage;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.basic_usage, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_toggle) {
            mCurrentScene = (mCurrentScene + 1) % mScenes.length;
            go(mScenes[mCurrentScene]);
            return true;
        }
        return false;
    }


}
