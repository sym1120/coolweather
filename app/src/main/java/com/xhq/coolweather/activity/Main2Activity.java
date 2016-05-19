package com.xhq.coolweather.activity;


import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.xhq.coolweather.R;
import com.xhq.coolweather.db.CoolWeatherDB;
import com.xhq.coolweather.util.HttpCallbackListner;
import com.xhq.coolweather.util.HttpUtil;
import com.xhq.coolweather.util.Utility;

public class Main2Activity extends AppCompatActivity {
    private CoolWeatherDB coolWeatherDB;
    private Button set;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        set = (Button) findViewById(R.id.set);
        set.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                init();
            }
        });

    }

    private void init() {
        coolWeatherDB = CoolWeatherDB.getInstance(this);
        HttpUtil.sendHttpRequest("https://api.heweather.com/x3/citylist?search=allchina&key=31bbdc7a7c624edf9868b220ec7eac0e",
                new HttpCallbackListner() {
                    @Override
                    public void onFinish(String response) {
                /*        Utility.handleProvincesResponse(coolWeatherDB, response);
                        Utility.handleCitiesResponse(coolWeatherDB, response, 49, "CN10132");
                        Utility.handleCitiesResponse(coolWeatherDB, response, 22, "CN10121");
                        Utility.handleCitiesResponse(coolWeatherDB, response, 30, "CN10102");
                        Utility.handleCountiesResponse(coolWeatherDB,response,9,"CN1012101");
                        Utility.handleCountiesResponse(coolWeatherDB,response,33,"CN1013201");
                        Utility.handleCountiesResponse(coolWeatherDB,response,55,"CN10101");*/
                    }

                    @Override
                    public void onError(Exception e) {

                    }
                });
    }
}
