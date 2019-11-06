package com.lll.lifecycledemo.mvp.login;

import com.lll.lifecycledemo.mvp.IBasePresenter;

public interface ILoginPresenter<T extends  ILoginView> extends IBasePresenter<T> {

    void login(String userName, String pwd);

}
