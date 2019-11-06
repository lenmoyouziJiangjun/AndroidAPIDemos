package com.lll.lifecycledemo;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;

public class MainLifeCycleOberver implements LifecycleObserver{

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    public void onCreate(){

    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onStart(){

    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    public void onResume(){

    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    public void onPause(){

    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onStop(){

    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    public void onDestroy(){

    }

    @OnLifecycleEvent(Lifecycle.Event.ON_ANY)
    public void onAny(){
      //todo constant that can be used to match all events
    }
}
