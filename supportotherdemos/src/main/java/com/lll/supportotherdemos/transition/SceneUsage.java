package com.lll.supportotherdemos.transition;

import android.annotation.TargetApi;
import android.support.v4.app.ActivityOptionsCompat;
import android.transition.Scene;
import android.transition.TransitionManager;
import android.view.ViewGroup;

import com.lll.supportotherdemos.R;

public class SceneUsage extends SceneUsageBase {
    @TargetApi(19)
    @Override
    Scene[] setUpScenes(ViewGroup root) {

        return new Scene[]{
                Scene.getSceneForLayout(root, R.layout.custom0, this),//转场界面1
                Scene.getSceneForLayout(root, R.layout.custom1, this)//转场界面2
        };
    }

    @TargetApi(19)
    @Override
    void go(Scene scene) {
        TransitionManager.go(scene);//界面切换
//        TransitionManager.go(scene,new ChangeColor());//自定义转场动画的
    }
}
