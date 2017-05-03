package com.lll.supportotherdemos.transition;

import android.annotation.TargetApi;
import android.transition.Scene;
import android.transition.TransitionManager;
import android.view.ViewGroup;

import com.lll.supportotherdemos.R;

public class SceneUsage extends SceneUsageBase {
    @TargetApi(19)
    @Override
    Scene[] setUpScenes(ViewGroup root) {
        return new Scene[]{
                Scene.getSceneForLayout(root, R.layout.custom0,this),
                Scene.getSceneForLayout(root, R.layout.custom1,this)
        };
    }

    @TargetApi(19)
    @Override
    void go(Scene scene) {
        TransitionManager.go(scene);
    }
}
