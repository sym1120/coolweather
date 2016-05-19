package com.xhq.coolweather.util;

/**
 * Created by Xiaoq on 2016-5-15.
 */
public interface HttpCallbackListner {
    void onFinish(String response);

    void onError(Exception e);
}
