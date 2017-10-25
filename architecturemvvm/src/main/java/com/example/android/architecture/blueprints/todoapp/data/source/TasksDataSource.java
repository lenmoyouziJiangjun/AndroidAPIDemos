package com.example.android.architecture.blueprints.todoapp.data.source;

import android.support.annotation.NonNull;

import com.example.android.architecture.blueprints.todoapp.data.Task;

import java.util.List;

import rx.Completable;
import rx.Observable;

/**
 * Version 1.0
 * Created by lll on 10/24/17.
 * Description
 * <p>
 * Main entry point for accessing tasks data.
 * <p>
 * copyright generalray4239@gmail.com
 */
public interface TasksDataSource {

    @NonNull
    Observable<List<Task>> getTasks();

    @NonNull
    Observable<Task> getTask(@NonNull String taskId);


    @NonNull
    Completable saveTask(@NonNull Task task);

    @NonNull
    Completable saveTasks(@NonNull List<Task> tasks);

    @NonNull
    Completable completeTask(@NonNull Task task);

    @NonNull
    Completable completeTask(@NonNull String taskId);

    Completable activateTask(@NonNull Task task);

    Completable activateTask(@NonNull String taskId);

    void clearCompletedTasks();

    @NonNull
    Completable refreshTasks();

    void deleteAllTasks();

    void deleteTask(@NonNull String taskId);
}
