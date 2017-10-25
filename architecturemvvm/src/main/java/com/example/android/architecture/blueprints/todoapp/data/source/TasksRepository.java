package com.example.android.architecture.blueprints.todoapp.data.source;

import android.support.annotation.NonNull;

import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.util.schedulers.BaseSchedulerProvider;

import java.util.List;

import rx.Completable;
import rx.Observable;

import static com.google.common.base.Preconditions.checkNotNull;

public class TasksRepository implements TasksDataSource {

    @NonNull
    private static TasksRepository INSTANCE = null;

    @NonNull
    private final TasksDataSource mTasksRemoteDataSource;

    @NonNull
    private final TasksDataSource mTasksLocalDataSource;

    @NonNull
    private final BaseSchedulerProvider mBaseSchedulerProvider;

    private TasksRepository(@NonNull TasksDataSource tasksRemoteDataSource,
                            @NonNull TasksDataSource tasksLocalDataSource,
                            @NonNull BaseSchedulerProvider schedulerProvider) {
        mTasksLocalDataSource = tasksLocalDataSource;
        mTasksRemoteDataSource = tasksRemoteDataSource;
        mBaseSchedulerProvider = schedulerProvider;
    }


    /**
     * Returns the single instance of this class, creating it if necessary.
     *
     * @param tasksRemoteDataSource the backend data source
     * @param tasksLocalDataSource  the device storage data source
     * @return the {@link TasksRepository} instance
     */
    public static TasksRepository getInstance(@NonNull TasksDataSource tasksRemoteDataSource,
                                              @NonNull TasksDataSource tasksLocalDataSource,
                                              @NonNull BaseSchedulerProvider schedulerProvider) {
        if (INSTANCE == null) {
            INSTANCE = new TasksRepository(tasksRemoteDataSource, tasksLocalDataSource,
                    schedulerProvider);
        }
        return INSTANCE;
    }

    /**
     * Used to force {@link #getInstance(TasksDataSource, TasksDataSource, BaseSchedulerProvider)}
     * to create a new instance next time it's called.
     */
    public static void destroyInstance() {
        INSTANCE = null;
    }


    @NonNull
    @Override
    public Observable<List<Task>> getTasks() {
        return mTasksLocalDataSource.getTasks();
    }

    @NonNull
    @Override
    public Observable<Task> getTask(@NonNull String taskId) {
        checkNotNull(taskId);
        return mTasksLocalDataSource.getTask(taskId);
    }

    @NonNull
    @Override
    public Completable saveTask(@NonNull Task task) {
        checkNotNull(task);
        return mTasksLocalDataSource.saveTask(task).andThen(mTasksRemoteDataSource.saveTask(task));
    }

    @NonNull
    @Override
    public Completable saveTasks(@NonNull List<Task> tasks) {
        checkNotNull(tasks);
        return mTasksLocalDataSource.saveTasks(tasks)
                .andThen(mTasksRemoteDataSource.saveTasks(tasks));
    }

    @Override
    public Completable completeTask(@NonNull Task task) {
        checkNotNull(task);
        return mTasksLocalDataSource.completeTask(task)
                .andThen(mTasksRemoteDataSource.completeTask(task));
    }

    @Override
    public Completable completeTask(@NonNull String taskId) {
        checkNotNull(taskId);
        return mTasksLocalDataSource.completeTask(taskId)
                .andThen(mTasksRemoteDataSource.completeTask(taskId));
    }

    @Override
    public Completable activateTask(@NonNull Task task) {
        checkNotNull(task);
        return mTasksLocalDataSource.activateTask(task)
                .andThen(mTasksRemoteDataSource.activateTask(task));
    }

    @Override
    public Completable activateTask(@NonNull String taskId) {
        checkNotNull(taskId);
        return mTasksLocalDataSource.activateTask(taskId)
                .andThen(mTasksRemoteDataSource.activateTask(taskId));
    }

    @Override
    public void clearCompletedTasks() {
        mTasksRemoteDataSource.clearCompletedTasks();
        mTasksLocalDataSource.clearCompletedTasks();
    }

    /**
     * Get the tasks from the remote data source and save them in the local data source.
     */
    @NonNull
    @Override
    public Completable refreshTasks() {
        return mTasksRemoteDataSource.getTasks()
                .subscribeOn(mBaseSchedulerProvider.io())
//                .doOnNext(mTasksLocalDataSource.saveTasks(mTasksRemoteDataSource.getTasks()))
                .toCompletable();
    }

    @Override
    public void deleteAllTasks() {
        mTasksRemoteDataSource.deleteAllTasks();
        mTasksLocalDataSource.deleteAllTasks();
    }

    @Override
    public void deleteTask(@NonNull String taskId) {
        mTasksRemoteDataSource.deleteTask(checkNotNull(taskId));
        mTasksLocalDataSource.deleteTask(checkNotNull(taskId));
    }
}
