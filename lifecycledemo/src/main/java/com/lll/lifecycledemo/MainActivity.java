package com.lll.lifecycledemo;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.lll.lifecycledemo.mvp.login.ILoginView;
import com.lll.lifecycledemo.mvp.login.LoginPresenter;
import com.lll.lifecycledemo.ui.main.MainFragment;

public class MainActivity extends AppCompatActivity implements ILoginView<LoginPresenter> {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        //
        getLifecycle().addObserver(new MainLifeCycleOberver());

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, MainFragment.newInstance())
                    .commitNow();
        }
    }

    @Override
    public void loginSuccess() {

    }

    @Override
    public LoginPresenter getPresenter() {
        return null;
    }

    @Override
    public Context getContext() {
        return null;
    }

    @Override
    public String getTag() {
        return null;
    }

    @Override
    public void showLoadingView() {

    }

    @Override
    public void dismissLoadingView() {

    }
}
