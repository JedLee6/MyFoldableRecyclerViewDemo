package com.happylee.mydemo.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * <pre>
 *     author: Blankj
 *     blog  : http://blankj.com
 *     time  : 16/12/08
 *     desc  : Utils初始化相关
 * </pre>
 */
public class Utils {

    private static Context context;

    private Utils() {
        throw new UnsupportedOperationException("u can't instantiate me...");
    }

    /**
     * 初始化工具类
     *
     * @param context 上下文
     */
    public static void init(Context context) {
        Utils.context = context.getApplicationContext();
    }

    /**
     * 获取ApplicationContext
     *
     * @return ApplicationContext
     */
    public static Context getContext() {
        if (context != null) {
            return context;
        }
        throw new NullPointerException("u should init first");
    }

    /**当用户第一次启动APP时对SP初始化，否则不初始化*/
    public static void initSharedPreferences() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("app_sharedpreferences", Context.MODE_PRIVATE);
        boolean isFirstRun = sharedPreferences.getBoolean("isFirstRun", true);
        SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
        if (isFirstRun) {
            Log.d("TAG", "第一次运行该APP");
            sharedPreferencesEditor.putBoolean("isFirstRun", false);
            sharedPreferencesEditor.apply();
        } else {
            Log.e("TAG", "不是第一次运行该APP");
        }
    }

}
