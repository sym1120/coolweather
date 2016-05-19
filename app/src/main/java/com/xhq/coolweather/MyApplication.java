package com.xhq.coolweather;

import android.app.Application;
import android.content.Context;

/**
 * Created by Xiaoq on 2016-5-15.
 * 全局获取Context参数
 */
public class MyApplication extends Application {
    public static Context context;

    @Override
    public void onCreate() {
        context = getApplicationContext();
        super.onCreate();
    }

    public static Context getContext() {
        return context;
    }
}
