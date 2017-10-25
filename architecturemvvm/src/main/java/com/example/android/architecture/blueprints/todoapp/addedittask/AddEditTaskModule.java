package com.example.android.architecture.blueprints.todoapp.addedittask;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.example.android.architecture.blueprints.todoapp.mock.Injection;
import com.example.android.architecture.blueprints.todoapp.util.provider.BaseNavigator;

public class AddEditTaskModule {

    /**
     * @param taskId
     * @param activity
     * @return
     */
    @NonNull
    public static AddEditTaskViewModel createAddEditTaskViewModel(@Nullable String taskId,
                                                                  @NonNull Activity activity) {
        Context appContext = activity.getApplicationContext();
        BaseNavigator navigationProvider = Injection.createNavigationProvider(activity);
        return new AddEditTaskViewModel(taskId, Injection.provideTasksRepository(appContext),
                createAddEditTaskNavigator(navigationProvider));
    }

    public static AddEditTaskNavigator createAddEditTaskNavigator(@NonNull BaseNavigator navigationProvider) {
        return new AddEditTaskNavigator(navigationProvider);
    }

}
