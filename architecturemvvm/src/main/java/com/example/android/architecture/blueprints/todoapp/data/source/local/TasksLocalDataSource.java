package com.example.android.architecture.blueprints.todoapp.data.source.local;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.example.android.architecture.blueprints.todoapp.data.Task;
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource;
import com.example.android.architecture.blueprints.todoapp.util.schedulers.BaseSchedulerProvider;
import com.squareup.sqlbrite.BriteDatabase;
import com.squareup.sqlbrite.SqlBrite;

import java.util.List;

import rx.Completable;
import rx.Observable;
import rx.functions.Func1;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Version 1.0
 * Created by lll on 10/25/17.
 * Description
 * copyright generalray4239@gmail.com
 */
public class TasksLocalDataSource implements TasksDataSource {

    @NonNull
    private static TasksLocalDataSource INSTANCE;

    @NonNull
    private  BriteDatabase mDatabaseHelper;

    @NonNull
    private Func1<Cursor, Task> mTaskMapperFunction;

    private TasksLocalDataSource(@NonNull Context context, @NonNull BaseSchedulerProvider schedulerProvider) {
        checkNotNull(context, "context cannot be null");
        checkNotNull(schedulerProvider, "scheduleProvider cannot be null");

        TasksDataHelper dbHelper = new TasksDataHelper(context);
        SqlBrite sqlBrite = new SqlBrite.Builder().build();
        mDatabaseHelper = sqlBrite.wrapDatabaseHelper(dbHelper, schedulerProvider.io());
        mTaskMapperFunction = this::getTask;//等于下面的逻辑
//        mTaskMapperFunction = new Func1<Cursor, Task>(){
//
//            @Override
//            public Task call(Cursor cursor) {
//                return getTask (cursor);
//            }
//        };
    }

    public static TasksLocalDataSource getInstance(
            @NonNull Context context,
            @NonNull BaseSchedulerProvider schedulerProvider) {
        if (INSTANCE == null) {
            INSTANCE = new TasksLocalDataSource(context, schedulerProvider);
        }
        return INSTANCE;
    }

    public static void destroyInstance() {
        INSTANCE = null;
    }

    private Task getTask(@NonNull Cursor c) {
        String itemId = c.getString(c.getColumnIndexOrThrow(TasksPersistenceContract.TaskEntry.COLUMN_NAME_ENTRY_ID));
        String title = c.getString(c.getColumnIndexOrThrow(TasksPersistenceContract.TaskEntry.COLUMN_NAME_TITLE));
        String description =
                c.getString(c.getColumnIndexOrThrow(TasksPersistenceContract.TaskEntry.COLUMN_NAME_DESCRIPTION));
        boolean completed =
                c.getInt(c.getColumnIndexOrThrow(TasksPersistenceContract.TaskEntry.COLUMN_NAME_COMPLETED)) == 1;
        return new Task(title, description, itemId, completed);
    }

    @NonNull
    @Override
    public Observable<List<Task>> getTasks() {
        String[] projection = {
                TasksPersistenceContract.TaskEntry.COLUMN_NAME_ENTRY_ID,
                TasksPersistenceContract.TaskEntry.COLUMN_NAME_TITLE,
                TasksPersistenceContract.TaskEntry.COLUMN_NAME_DESCRIPTION,
                TasksPersistenceContract.TaskEntry.COLUMN_NAME_COMPLETED
        };
        String sql = String.format("SELECT %s FROM %s", TextUtils.join(",", projection), TasksPersistenceContract.TaskEntry.TABLE_NAME);

        return mDatabaseHelper.createQuery(TasksPersistenceContract.TaskEntry.TABLE_NAME, sql).mapToList(mTaskMapperFunction);
    }

    @NonNull
    @Override
    public Observable<Task> getTask(@NonNull String taskId) {
        String[] projection = {
                TasksPersistenceContract.TaskEntry.COLUMN_NAME_ENTRY_ID,
                TasksPersistenceContract.TaskEntry.COLUMN_NAME_TITLE,
                TasksPersistenceContract.TaskEntry.COLUMN_NAME_DESCRIPTION,
                TasksPersistenceContract.TaskEntry.COLUMN_NAME_COMPLETED
        };
        String sql = String.format("SELECT %s FROM %s WHERE %s LIKE ?",
                TextUtils.join(",", projection), TasksPersistenceContract.TaskEntry.TABLE_NAME, TasksPersistenceContract.TaskEntry.COLUMN_NAME_ENTRY_ID);
        return mDatabaseHelper.createQuery(TasksPersistenceContract.TaskEntry.TABLE_NAME, sql, taskId)
                .mapToOneOrDefault(mTaskMapperFunction, null);
    }

    private ContentValues toContentValues(Task task) {
        ContentValues values = new ContentValues();
        values.put(TasksPersistenceContract.TaskEntry.COLUMN_NAME_ENTRY_ID, task.getId());
        values.put(TasksPersistenceContract.TaskEntry.COLUMN_NAME_TITLE, task.getTitle());
        values.put(TasksPersistenceContract.TaskEntry.COLUMN_NAME_DESCRIPTION, task.getDescription());
        values.put(TasksPersistenceContract.TaskEntry.COLUMN_NAME_COMPLETED, task.isCompleted());
        return values;
    }

    @NonNull
    @Override
    public Completable saveTask(@NonNull Task task) {
        checkNotNull(task);
        return Completable.fromAction(() -> {
            ContentValues values = toContentValues(task);
            mDatabaseHelper.insert(TasksPersistenceContract.TaskEntry.TABLE_NAME, values, SQLiteDatabase.CONFLICT_REPLACE);
        });
    }

    @NonNull
    @Override
    public Completable saveTasks(@NonNull List<Task> tasks) {
        checkNotNull(tasks);
        return Observable.using(mDatabaseHelper::newTransaction,
                transaction -> inTransactionInsert(tasks, transaction),
                BriteDatabase.Transaction::end).toCompletable();
    }

    private Observable<List<Task>> inTransactionInsert(@NonNull List<Task> tasks,
                                                       @NonNull BriteDatabase.Transaction transaction) {
        checkNotNull(tasks);
        checkNotNull(transaction);

        return Observable.from(tasks).doOnNext(task -> {
            ContentValues values = toContentValues(task);
            mDatabaseHelper.insert(TasksPersistenceContract.TaskEntry.TABLE_NAME, values);
        }).doOnCompleted(transaction::markSuccessful).toList();

    }

    @NonNull
    @Override
    public Completable completeTask(@NonNull Task task) {
        checkNotNull(task);
        return completeTask(task.getId());
    }

    @NonNull
    @Override
    public Completable completeTask(@NonNull String taskId) {
        return Completable.fromAction(() -> {
            ContentValues values = new ContentValues();
            values.put(TasksPersistenceContract.TaskEntry.COLUMN_NAME_COMPLETED, true);

            String selection = TasksPersistenceContract.TaskEntry.COLUMN_NAME_ENTRY_ID + " LIKE ?";
            String[] selectionArgs = {taskId};

            mDatabaseHelper.update(TasksPersistenceContract.TaskEntry.TABLE_NAME, values, selection, selectionArgs);
        });
    }

    @Override
    public Completable activateTask(@NonNull Task task) {
        return activateTask(task.getId());
    }

    @Override
    public Completable activateTask(@NonNull String taskId) {
        return Completable.fromAction(() -> {
            ContentValues values = new ContentValues();
            values.put(TasksPersistenceContract.TaskEntry.COLUMN_NAME_COMPLETED, false);

            String selection = TasksPersistenceContract.TaskEntry.COLUMN_NAME_ENTRY_ID + " LIKE ?";
            String[] selectionArgs = {taskId};
            mDatabaseHelper.update(TasksPersistenceContract.TaskEntry.TABLE_NAME, values, selection, selectionArgs);
        });
    }

    @Override
    public void clearCompletedTasks() {
        String selection = TasksPersistenceContract.TaskEntry.COLUMN_NAME_COMPLETED + " LIKE ?";
        String[] selectionArgs = {"1"};
        mDatabaseHelper.delete(TasksPersistenceContract.TaskEntry.TABLE_NAME, selection, selectionArgs);
    }

    @Override
    public Completable refreshTasks() {
        // Not required because the {@link TasksRepository} handles the logic of refreshing the
        // tasks from all the available data sources.
        return Completable.complete();
    }

    @Override
    public void deleteAllTasks() {
        mDatabaseHelper.delete(TasksPersistenceContract.TaskEntry.TABLE_NAME, null);
    }

    @Override
    public void deleteTask(@NonNull String taskId) {
        String selection = TasksPersistenceContract.TaskEntry.COLUMN_NAME_ENTRY_ID + " LIKE ?";
        String[] selectionArgs = {taskId};
        mDatabaseHelper.delete(TasksPersistenceContract.TaskEntry.TABLE_NAME, selection, selectionArgs);
    }
}
