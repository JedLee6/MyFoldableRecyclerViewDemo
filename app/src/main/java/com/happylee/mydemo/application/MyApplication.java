package com.happylee.mydemo.application;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.happylee.mydemo.utils.Utils;

/**
 * @author Jed Lee(李俊德)
 */
public class MyApplication extends Application {
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

    }
}
