package com.example.android.architecture.blueprints.todoapp.tasks;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;

import com.example.android.architecture.blueprints.todoapp.util.provider.BaseNavigator;

/**
 * Version 1.0
 * Created by lll on 10/24/17.
 * Description
 * <p>
 * Enables inversion of control of the ViewModel and Navigator classes for tasks screen.
 * <p>
 * copyright generalray4239@gmail.com
 */
public class TasksModule {

    @NonNull
    public static TasksViewModel createTasksViewModel(@NonNull Activity activity) {
        Context appContext = activity.getApplicationContext();
        BaseNavigator navigationProvider = Injection.createNavigationProvider(activity);
        return new TasksViewModel(Injection.provideTasksRepository(appContext),
                createTasksNavigator(navigationProvider), Injection.provideSchedulerProvider());
    }

    @NonNull
    public static TasksNavigator createTasksNavigator(
            @NonNull BaseNavigator navigationProvider) {
        return new TasksNavigator(navigationProvider);
    }

}
