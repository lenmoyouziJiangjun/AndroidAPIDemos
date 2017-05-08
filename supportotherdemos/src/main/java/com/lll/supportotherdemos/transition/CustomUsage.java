package com.lll.supportotherdemos.transition;

import android.annotation.TargetApi;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.transition.Scene;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.view.ViewGroup;

import com.lll.supportotherdemos.R;

public class CustomUsage extends SceneUsageBase {

    private Transition mTransition;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTransition = new ChangeColor();
    }

    @TargetApi(19)
    @Override
    Scene[] setUpScenes(ViewGroup root) {
        return new Scene[]{
                Scene.getSceneForLayout(root, R.layout.custom0, this),
                Scene.getSceneForLayout(root, R.layout.custom1, this),
                Scene.getSceneForLayout(root, R.layout.custom2, this)
        };
    }

    @TargetApi(19)
    @Override
    void go(Scene scene) {

        TransitionManager.go(scene, mTransition);
    }
}
