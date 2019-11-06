package com.lll.lifecycledemo.mvp;

import android.content.Context;

/**
 * BaseView
 * @param <T>
 */
public interface IBaseView<T extends IBasePresenter> {

    /**
     *
     * @return
     */
    T getPresenter();


    /**
     *
     * @return
     */
    Context getContext();


    /**
     *
     * @return
     */
    String getTag();

    /**
     *
     */
    void showLoadingView();


    /**
     *
     */
    void dismissLoadingView();


}
