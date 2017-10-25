package com.example.android.architecture.blueprints.todoapp.tasks;

import android.support.annotation.NonNull;

import com.example.android.architecture.blueprints.todoapp.addedittask.AddEditTaskActivity;
import com.example.android.architecture.blueprints.todoapp.taskdetail.TaskDetailActivity;
import com.example.android.architecture.blueprints.todoapp.util.provider.BaseNavigator;

/**
 * Version 1.0
 * Created by lll on 10/24/17.
 * Description
 * Defines the navigation actions that can be called from the task list screen.
 * copyright generalray4239@gmail.com
 */
public class TasksNavigator {

    @NonNull
    private final BaseNavigator mNavigationProvider;

    public TasksNavigator(@NonNull BaseNavigator mNavigationProvider) {
        this.mNavigationProvider = mNavigationProvider;
    }

    /**
     * Start the activity that allows adding a new task.
     */
    void addNewTask() {
        mNavigationProvider.startActivityForResult(AddEditTaskActivity.class,
                AddEditTaskActivity.REQUEST_ADD_TASK);
    }

    /**
     * Open the details of a task.
     *
     * @param taskId id of the task.
     */
    void openTaskDetails(String taskId) {
        mNavigationProvider.startActivityForResultWithExtra(TaskDetailActivity.class, -1,
                TaskDetailActivity.EXTRA_TASK_ID, taskId);
    }


}
