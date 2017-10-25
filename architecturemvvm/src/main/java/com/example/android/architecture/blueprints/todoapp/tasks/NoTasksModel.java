package com.example.android.architecture.blueprints.todoapp.tasks;

import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;

/**
 * Version 1.0
 * Created by lll on 10/24/17.
 * Description The string and image that should be displayed when there are no tasks.
 * copyright generalray4239@gmail.com
 */
public class NoTasksModel {

    @StringRes
    private int mText;

    @DrawableRes
    private int mIcon;

    private boolean mIsAddNewTaskVisible;

    public NoTasksModel(int text, int icon, boolean isAddNewTaskVisible) {
        mText = text;
        mIcon = icon;
        mIsAddNewTaskVisible = isAddNewTaskVisible;
    }

    @StringRes
    public int getText() {
        return mText;
    }

    @DrawableRes
    public int getIcon() {
        return mIcon;
    }

    public boolean isAddNewTaskVisible() {
        return mIsAddNewTaskVisible;
    }
}
