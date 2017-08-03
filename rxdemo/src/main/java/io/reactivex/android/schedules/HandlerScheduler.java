package io.reactivex.android.schedules;

import android.os.Handler;
import android.os.Message;

import java.util.concurrent.TimeUnit;

import io.reactivex.Scheduler;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.Disposables;
import io.reactivex.plugins.RxJavaPlugins;

/**
 * Version 1.0
 * Created by lll on 17/8/2.
 * Description
 * copyright generalray4239@gmail.com
 */
public class HandlerScheduler extends Scheduler {

    private final Handler fHandler;

    public HandlerScheduler(Handler handler) {
        this.fHandler = handler;
    }

    @Override
    public Disposable scheduleDirect(@NonNull Runnable run, long delay, TimeUnit unit) {
        if (run == null) {
            throw new NullPointerException("run == null");
        }
        if (unit == null) {
            throw new NullPointerException("unit == null");
        }
        run = RxJavaPlugins.onSchedule(run);
        ScheduledRunnable scheduledRunnable = new ScheduledRunnable(fHandler, run);
        fHandler.postDelayed(scheduledRunnable, Math.max(0L, unit.toMillis(delay)));
        return scheduledRunnable;
    }

    @Override
    public Worker createWorker() {
        return new HandlerWorker(fHandler);
    }

    private static final class HandlerWorker extends Worker {
        private final Handler handler;
        private volatile boolean disposed;

        HandlerWorker(Handler handler) {
            this.handler = handler;
        }


        @Override
        public Disposable schedule(@NonNull Runnable run, long delay, @NonNull TimeUnit unit) {
            if (run == null) throw new NullPointerException("run == null");
            if (unit == null) throw new NullPointerException("unit == null");
            if (disposed) {
                return Disposables.disposed();
            }
            run = RxJavaPlugins.onSchedule(run);

            ScheduledRunnable scheduledRunnable = new ScheduledRunnable(handler, run);
            Message message = Message.obtain(handler, scheduledRunnable);
            message.obj = this;// Used as token for batch disposal of this worker's runnables.

            handler.sendMessageDelayed(message, Math.max(0L, unit.toMillis(delay)));

            // Re-check disposed state for removing in case we were racing a call to dispose().
            if (disposed) {
                handler.removeCallbacks(scheduledRunnable);
                return Disposables.disposed();
            }

            return scheduledRunnable;
        }

        @Override
        public void dispose() {
            disposed = true;
            handler.removeCallbacksAndMessages(this /* token */);
        }

        @Override
        public boolean isDisposed() {
            return disposed;
        }
    }


    private static final class ScheduledRunnable implements Runnable, Disposable {
        private final Handler handler;
        private final Runnable delegate;

        private volatile boolean disposed;

        ScheduledRunnable(Handler handler, Runnable runnable) {
            this.handler = handler;
            this.delegate = runnable;
        }


        @Override
        public void dispose() {
            disposed = true;
            handler.removeCallbacks(this);
        }

        @Override
        public boolean isDisposed() {
            return disposed;
        }

        @Override
        public void run() {
            try {
                delegate.run();
            } catch (Throwable t) {
                IllegalStateException ie =
                        new IllegalStateException("Fatal Exception thrown on Scheduler.", t);
                RxJavaPlugins.onError(ie);
                Thread thread = Thread.currentThread();
                thread.getUncaughtExceptionHandler().uncaughtException(thread, ie);
            }
        }
    }

}
