package com.happylee.mydemo.application;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.happylee.mydemo.utils.Utils;

/**
 * 在master修改的此处，修改dev分支的内容
 */
public class MyApplication extends Application {
    private String string;
    private int num;
    private static MyApplication myApplication;

    public static MyApplication getInstance() {
        return myApplication;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        myApplication = this;
        //当用户第一次启动APP时对SP初始化，否则不初始化
        Utils.init(myApplication);
        Utils.initSharedPreferences();

        //注册APP生命周期观察者，注意要在build.gradle中额外导入ProcessLifecycleOwner的依赖
        ProcessLifecycleOwner.get().getLifecycle().addObserver(new ApplicationLifecycleObserver());
    }

    /**
     * Application生命周期观察，提供整个应用进程的生命周期
     * <p>
     * Lifecycle.Event.ON_CREATE只会分发一次，Lifecycle.Event.ON_DESTROY不会被分发。
     * <p>
     * 第一个Activity进入时，ProcessLifecycleOwner将分派Lifecycle.Event.ON_START, Lifecycle.Event.ON_RESUME。
     * 而Lifecycle.Event.ON_PAUSE, Lifecycle.Event.ON_STOP，将在最后一个Activit退出后后延迟分发。如果由于配置更改而销毁并重新创建活动，则此延迟足以保证ProcessLifecycleOwner不会发送任何事件。
     * <p>
     * 作用：监听应用程序进入前台或后台
     */
    private static class ApplicationLifecycleObserver implements LifecycleObserver {
        @OnLifecycleEvent(Lifecycle.Event.ON_START)
        private void onAppForeground(){
            Toast.makeText(getInstance(),  "应用在前台", Toast.LENGTH_SHORT).show();
        }
        @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
        private void onAppBackground(){
            Toast.makeText(getInstance(),  "应用在后台", Toast.LENGTH_SHORT).show();
        }
    }
}
