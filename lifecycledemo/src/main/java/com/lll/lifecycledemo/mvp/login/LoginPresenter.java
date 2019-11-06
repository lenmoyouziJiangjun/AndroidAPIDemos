package com.lll.lifecycledemo.mvp.login;

import com.lll.lifecycledemo.mvp.BasePresenter;

public class LoginPresenter extends BasePresenter implements ILoginPresenter {



    @Override
    public void login(String userName, String pwd) {

    }


    @Override
    public ILoginView getView() {
        return null;
    }
}
