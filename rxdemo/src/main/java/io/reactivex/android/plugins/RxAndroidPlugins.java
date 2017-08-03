package io.reactivex.android.plugins;

import android.support.annotation.NonNull;
import android.telecom.Call;

import java.util.concurrent.Callable;

import io.reactivex.Scheduler;
import io.reactivex.exceptions.Exceptions;
import io.reactivex.functions.Function;

public class RxAndroidPlugins {

    private static volatile Function<Callable<Scheduler>, Scheduler> onInitMainThreadHandler;

    private static volatile Function<Scheduler, Scheduler> onMainThreadHandler;

    public static void setInitMainThreadSchedulerHandler(Function<Callable<Scheduler>, Scheduler> handler) {
        onInitMainThreadHandler = handler;
    }

    public static void setMainThreadHandler(Function<Scheduler, Scheduler> handler) {
        onMainThreadHandler = handler;
    }

    /**
     * Returns the current hook function.
     *
     * @return the hook function, may be null
     */
    public static Function<Callable<Scheduler>, Scheduler> getInitMainThreadSchedulerHandler() {
        return onInitMainThreadHandler;
    }

    /**
     * Returns the current hook function.
     *
     * @return the hook function, may be null
     */
    public static Function<Scheduler, Scheduler> getOnMainThreadSchedulerHandler() {
        return onMainThreadHandler;
    }


    public static Scheduler initMainThreadScheduler(@NonNull Callable<Scheduler> scheduler) {
        Function<Callable<Scheduler>, Scheduler> function = onInitMainThreadHandler;

        if (function == null) {
            return callRequireNonNull(scheduler);
        }
        return applyRequireNonNull(function, scheduler);
    }

    public static Scheduler onMainThreadScheduler(@NonNull Scheduler scheduler) {
        Function<Scheduler, Scheduler> function = onMainThreadHandler;
        if (function == null) {
            return scheduler;
        }
        return apply(function, scheduler);
    }

    static Scheduler callRequireNonNull(Callable<Scheduler> schedulerCallable) {
        try {
            Scheduler scheduler = schedulerCallable.call();
            if (scheduler == null) {
                throw new NullPointerException("Scheduler Callable returned null");
            }
            return scheduler;
        } catch (Exception e) {
            throw Exceptions.propagate(e);
        }
    }

    static Scheduler applyRequireNonNull(Function<Callable<Scheduler>, Scheduler> f, Callable<Scheduler> s) {
        Scheduler scheduler = apply(f, s);
        if (scheduler == null) {
            throw new NullPointerException("Scheduler Callable returned null");
        }
        return scheduler;
    }

    static <T, R> R apply(Function<T, R> f, T t) {
        try {
            return f.apply(t);
        } catch (Throwable ex) {
            throw Exceptions.propagate(ex);
        }
    }

    private RxAndroidPlugins() {
        throw new AssertionError("No instances.");
    }
}
