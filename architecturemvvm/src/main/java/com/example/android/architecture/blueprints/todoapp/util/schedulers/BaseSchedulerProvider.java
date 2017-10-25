package com.example.android.architecture.blueprints.todoapp.util.schedulers;

import android.support.annotation.NonNull;

import rx.Scheduler;

/**
 * Version 1.0
 * Created by lll on 10/24/17.
 * Description
 * Allow providing different types of {@link Scheduler}s.
 * copyright generalray4239@gmail.com
 */
public interface BaseSchedulerProvider {

    @NonNull
    Scheduler computation();

    @NonNull
    Scheduler io();

    @NonNull
    Scheduler ui();

}
