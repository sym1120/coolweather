package com.xhq.coolweather.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.xhq.coolweather.R;
import com.xhq.coolweather.service.updateWeatherService;
import com.xhq.coolweather.util.HttpCallbackListner;
import com.xhq.coolweather.util.HttpUtil;
import com.xhq.coolweather.util.Utility;

import static com.xhq.coolweather.R.id.city_name;
import static com.xhq.coolweather.R.id.current_date;
import static com.xhq.coolweather.R.id.publish_text;
import static com.xhq.coolweather.R.id.temp1;
import static com.xhq.coolweather.R.id.temp2;
import static com.xhq.coolweather.R.id.weather_info;

public class WeatherActivity extends AppCompatActivity implements View.OnClickListener {
    private LinearLayout weatherLinearLayout;
    private TextView cityName;
    private TextView publishText;
    private TextView currentDate;
    private TextView weatherInfo;
    private TextView temp1Text;
    private TextView temp2Text;
    private Button home;
    private Button refresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_layout);
        weatherLinearLayout = (LinearLayout) findViewById(R.id.weather_info_layout);
        cityName = (TextView) findViewById(city_name);
        publishText = (TextView) findViewById(publish_text);
        currentDate = (TextView) findViewById(current_date);
        weatherInfo = (TextView) findViewById(weather_info);
        temp1Text = (TextView) findViewById(temp1);
        temp2Text = (TextView) findViewById(temp2);
        home = (Button) findViewById(R.id.home);
        refresh = (Button) findViewById(R.id.refresh);
        home.setOnClickListener(this);
        refresh.setOnClickListener(this);

        String countyCode = getIntent().getStringExtra("country_code");
        if (!TextUtils.isEmpty(countyCode)) {
            publishText.setText("同步中...");
            weatherLinearLayout.setVisibility(View.INVISIBLE);
            cityName.setVisibility(View.INVISIBLE);
            queryWeatherInfo(countyCode);
        } else
            showWeather();

    }

    private void queryWeatherInfo(String code) {
        String address = "https://api.heweather.com/x3/weather?cityid=" + code +
                "&key=31bbdc7a7c624edf9868b220ec7eac0e";
        HttpUtil.sendHttpRequest(address, new HttpCallbackListner() {
            @Override
            public void onFinish(String response) {
                boolean result = Utility.handleWeatherInfo(MyApplication.getContext(), response);
                if (result) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showWeather();
                        }
                    });

                }

            }

            @Override
            public void onError(Exception e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        publishText.setText("同步失败！");
                    }
                });

            }
        });


    }

    private void showWeather() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        cityName.setText(sharedPreferences.getString("city_name", ""));
        publishText.setText("今天" + sharedPreferences.getString("update_time", "") + "更新");
        currentDate.setText(sharedPreferences.getString("current_date", ""));
        weatherInfo.setText(sharedPreferences.getString("weather_info", ""));
        temp1Text.setText(sharedPreferences.getString("temp1", "") + "°C");
        temp2Text.setText(sharedPreferences.getString("temp2", "") + "°C");
        weatherLinearLayout.setVisibility(View.VISIBLE);
        cityName.setVisibility(View.VISIBLE);

        //开启后台服务,服务一旦开启，再次start
        startService(new Intent(this, updateWeatherService.class));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.home:
                Intent i = new Intent(WeatherActivity.this, ChooseAreaActivity.class);
                i.putExtra("isFromWeather", true);
                startActivity(i);
                finish();
                break;
            case R.id.refresh:
                publishText.setText("同步中...");
                String cityId = PreferenceManager.getDefaultSharedPreferences(this).getString("city_id", "");
                if (!TextUtils.isEmpty(cityId))
                    queryWeatherInfo(cityId);
                break;
        }
    }
}
