package com.lll.lifecycledemo.mvp;

public interface IBasePresenter<T extends IBaseView> {

    T getView();

}
