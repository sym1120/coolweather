package com.xhq.coolweather.activity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.xhq.coolweather.R;
import com.xhq.coolweather.db.CoolWeatherDB;
import com.xhq.coolweather.model.City;
import com.xhq.coolweather.model.County;
import com.xhq.coolweather.model.Province;
import com.xhq.coolweather.util.HttpCallbackListner;
import com.xhq.coolweather.util.HttpUtil;
import com.xhq.coolweather.util.Utility;

import java.util.ArrayList;
import java.util.List;

public class ChooseAreaActivity extends AppCompatActivity {
    private ListView listView;
    private List<String> dataList = new ArrayList<>();
    private TextView title;
    private ArrayAdapter<String> adapter;
    private CoolWeatherDB coolWeatherDB;
    private List<Province> provinceList;
    private List<City> cityList;
    private List<County> countyList;
    private County selectedCounty;
    private City selectedCity;
    private Province selectedProvince;
    private ProgressDialog progressDialog;
    private int currentLevel;
    private static final int CURRENT_PROVINCE = 1;
    private static final int CURRENT_CITY = 2;
    private static final int CURRENT_COUNTY = 3;

    //和风天气获取城市API接口
    private static final String ADDRESS =
            "https://api.heweather.com/x3/citylist?search=allchina&key=31bbdc7a7c624edf9868b220ec7eac0e";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_area);
        coolWeatherDB = CoolWeatherDB.getInstance(this);
        listView = (ListView) findViewById(R.id.list_view);
        title = (TextView) findViewById(R.id.title);
        adapter = new ArrayAdapter<>(MyApplication.getContext(), android.R.layout.simple_list_item_1,
                dataList);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (currentLevel == CURRENT_PROVINCE) {
                    selectedProvince = provinceList.get(i);
                    queryCityInfo(selectedProvince.getId());

                } else if (currentLevel == CURRENT_CITY) {
                    selectedCity = cityList.get(i);
                    queryCountyInfo(selectedCity.getId());
                }

            }
        });
        queryProvinceInfo();
    }

    //查询省份信息，优先从数据库查询，如果没有则从网上查询
    private void queryProvinceInfo() {
        provinceList = coolWeatherDB.loadProvince();
        if (provinceList.size() > 0) {
            dataList.clear();
            for (Province province : provinceList) {
                dataList.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            currentLevel = CURRENT_PROVINCE;
            title.setText("中国");
        } else {
            querySeverInfo("province");
        }

    }

    //查询城市信息，优先从数据库查询，如果没有则从网上查询
    private void queryCityInfo(int provinceId) {
        cityList = coolWeatherDB.loadCity(provinceId);
        if (cityList.size() > 0) {
            dataList.clear();
            for (City city : cityList) {
                dataList.add(city.getCityName());
            }
            currentLevel = CURRENT_CITY;
            adapter.notifyDataSetChanged();
            title.setText(selectedProvince.getProvinceName());
        } else {

            querySeverInfo("city");
        }

    }

    //查询县市信息，优先从数据库查询，如果没有则从网上查询
    private void queryCountyInfo(int cityId) {
        countyList = coolWeatherDB.loadCounty(cityId);
        if (countyList.size() > 0) {
            dataList.clear();
            for (County county : countyList) {
                dataList.add(county.getCountyName());
            }
            currentLevel = CURRENT_COUNTY;
            adapter.notifyDataSetChanged();
            title.setText(selectedCity.getCityName());
        } else {

            querySeverInfo("county");
        }

    }

    //发送网络请求，获取被查询的省份、城市信息
    private void querySeverInfo(final String type) {
        showProgressDialog();
        HttpUtil.sendHttpRequest(ADDRESS, new HttpCallbackListner() {
            boolean result;

            @Override
            public void onFinish(String response) {
                if ("province".equals(type)) {
                    result = Utility.handleProvincesResponse(coolWeatherDB, response);
                } else if ("city".equals(type)) {
                    result = Utility.handleCitiesResponse(coolWeatherDB, response, selectedProvince.getId(),
                            selectedProvince.getProvinceCode());
                } else if ("county".equals(type)) {
                    result = Utility.handleCountiesResponse(coolWeatherDB, response, selectedCity.getId(),
                            selectedCity.getCityCode());
                }
                //对处理的结果进行判断
                if (result) {
                    //回到主线程，修改UI
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if ("province".equals(type))
                                queryProvinceInfo();
                            else if ("city".equals(type))
                                queryCityInfo(selectedProvince.getId());
                            else if ("county".equals(type))
                                queryCountyInfo(selectedCity.getId());
                        }
                    });

                }
            }

            @Override
            public void onError(Exception e) {
                e.printStackTrace();

            }
        });
    }

    //显示对话框
    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Loading...");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    //关闭对话框
    private void closeProgressDialog() {
        if (progressDialog != null)
            progressDialog.dismiss();
    }

    //对后退键做处理
    @Override
    public void onBackPressed() {
        if (currentLevel == CURRENT_COUNTY) {
            //此处注意！传入的是provinceID,二级菜单是根据所选择的省份展示的！
            queryCityInfo(selectedProvince.getId());
        } else if (currentLevel == CURRENT_CITY)
            queryProvinceInfo();
        else
            finish();
        //super.onBackPressed();
        //super语句害惨我了！！！导致BACK键失效！！
    }
}
