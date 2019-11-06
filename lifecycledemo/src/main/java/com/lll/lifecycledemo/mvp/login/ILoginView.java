package com.lll.lifecycledemo.mvp.login;

import com.lll.lifecycledemo.mvp.IBaseView;

public interface ILoginView<T extends ILoginPresenter> extends IBaseView<T> {


    void loginSuccess();
}
