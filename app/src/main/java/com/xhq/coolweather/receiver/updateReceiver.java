package com.xhq.coolweather.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.xhq.coolweather.service.updateWeatherService;

/**
 * Created by Xiaoq on 2016-5-25.
 */

public class updateReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(context, updateWeatherService.class);
        context.startService(i);

    }
}
