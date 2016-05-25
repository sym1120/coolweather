package com.xhq.coolweather.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.xhq.coolweather.activity.MyApplication;
import com.xhq.coolweather.receiver.updateReceiver;
import com.xhq.coolweather.util.HttpCallbackListner;
import com.xhq.coolweather.util.HttpUtil;
import com.xhq.coolweather.util.Utility;

/**
 * Created by Xiaoq on 2016-5-25.
 */

public class updateWeatherService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                updateWeatherInfo();
            }
        }).start();
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        long triggerAtMillis = SystemClock.elapsedRealtime() + 8 * 60 * 60 * 1000;
        Intent i = new Intent(updateWeatherService.this, updateReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(updateWeatherService.this, 0, i, 0);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtMillis, pi);
        return super.onStartCommand(intent, flags, startId);
    }

    private void updateWeatherInfo() {
        String code = PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext())
                .getString("city_id", "");
        if (!TextUtils.isEmpty(code)) {
            String address = "https://api.heweather.com/x3/weather?cityid=" + code +
                    "&key=31bbdc7a7c624edf9868b220ec7eac0e";
            HttpUtil.sendHttpRequest(address, new HttpCallbackListner() {
                @Override
                public void onFinish(String response) {
                    Utility.handleWeatherInfo(updateWeatherService.this, response);
                }

                @Override
                public void onError(Exception e) {
                    e.printStackTrace();

                }
            });

        }

    }
}
