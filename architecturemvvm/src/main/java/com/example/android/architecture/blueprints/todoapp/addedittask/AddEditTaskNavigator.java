package com.example.android.architecture.blueprints.todoapp.addedittask;

import android.app.Activity;
import android.support.annotation.NonNull;

import com.example.android.architecture.blueprints.todoapp.util.provider.BaseNavigator;

/**
 * Version 1.0
 * Created by lll on 10/25/17.
 * Description
 * copyright generalray4239@gmail.com
 */
public class AddEditTaskNavigator {

    @NonNull
    private final BaseNavigator mNavigationProvider;

    public AddEditTaskNavigator(@NonNull BaseNavigator navigationProvider) {
        mNavigationProvider = navigationProvider;
    }

    /**
     * When the task was saved, the activity should finish with success.
     */
    public void onTaskSaved() {
        mNavigationProvider.finishActivityWithResult(Activity.RESULT_OK);
    }

}
