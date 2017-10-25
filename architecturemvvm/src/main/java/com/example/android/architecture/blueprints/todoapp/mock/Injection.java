package com.example.android.architecture.blueprints.todoapp.mock;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;

import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository;
import com.example.android.architecture.blueprints.todoapp.data.source.local.TasksLocalDataSource;
import com.example.android.architecture.blueprints.todoapp.mock.data.FakeTasksRemoteDataSource;
import com.example.android.architecture.blueprints.todoapp.util.provider.BaseNavigator;
import com.example.android.architecture.blueprints.todoapp.util.provider.BaseResourceProvider;
import com.example.android.architecture.blueprints.todoapp.util.provider.Navigator;
import com.example.android.architecture.blueprints.todoapp.util.provider.ResourceProvider;
import com.example.android.architecture.blueprints.todoapp.util.schedulers.BaseSchedulerProvider;
import com.example.android.architecture.blueprints.todoapp.util.schedulers.SchedulerProvider;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Version 1.0
 * Created by lll on 10/25/17.
 * Description
 * copyright generalray4239@gmail.com
 */
public class Injection {

    @NonNull
    public static TasksRepository provideTasksRepository(@NonNull Context context) {
        checkNotNull(context);
        return TasksRepository.getInstance(FakeTasksRemoteDataSource.getInstance(),
                TasksLocalDataSource.getInstance(context, provideSchedulerProvider()),
                provideSchedulerProvider());
    }

    @NonNull
    public static BaseSchedulerProvider provideSchedulerProvider() {
        return SchedulerProvider.getInstance();
    }

    @NonNull
    public static BaseResourceProvider createResourceProvider(@NonNull Context context) {
        return new ResourceProvider(context);
    }

    @NonNull
    public static BaseNavigator createNavigationProvider(@NonNull Activity activity) {
        return new Navigator(activity);
    }
}
