package com.lll.commonlibrary.util;

/**
 * Version 1.0
 * Created by lll on 2019-11-06.
 * Description
 * <pre>
 *     泛型单例工具类
 *
 * </pre>
 * copyright generalray4239@gmail.com
 */
public abstract class Singleton<T> {

    private T mInstance;

    protected abstract T create();

    public final T get() {
        synchronized (this) {
            if (mInstance == null) {
                mInstance = create();
            }
            return mInstance;
        }
    }
}
