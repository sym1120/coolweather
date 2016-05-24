package com.xhq.coolweather.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.xhq.coolweather.db.CoolWeatherDB;
import com.xhq.coolweather.model.City;
import com.xhq.coolweather.model.County;
import com.xhq.coolweather.model.Province;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static com.xhq.coolweather.R.id.temp1;

/**
 * Created by Xiaoq on 2016-5-19.
 * 工具类，解析省市县城市信息，通过listview展示三级列表
 */
public class Utility {

    /**
     * 解析和处理Province信息数据，并存到数据库
     */
    public synchronized static boolean handleProvincesResponse(CoolWeatherDB coolWeatherDB, String response) {
        if (!TextUtils.isEmpty(response)) {
            try {
                Map<String, String> map = new HashMap<>();
                String provinceCode;
                String provinceName;
                JSONObject jsonObject = new JSONObject(response);
                JSONArray jsonArray = jsonObject.getJSONArray("city_info");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject subObject = jsonArray.getJSONObject(i);
                    String id = subObject.getString("id");
                    provinceCode = id.substring(5, 7);//获取province代码

                    //分析JSON数据，对四个直辖市和特别行政区做特殊分析，因为这几个城市的prov信息为“直辖市”或者“特别行政区”
                    //不能直接保存prov信息

                    if ("01".equals(provinceCode)) {
                        provinceName = "北京";
                    } else if ("02".equals(provinceCode)) {
                        provinceName = "上海";
                    } else if ("03".equals(provinceCode)) {
                        provinceName = "天津";
                    } else if ("04".equals(provinceCode)) {
                        provinceName = "重庆";
                    } else if ("32".equals(provinceCode)) {
                        provinceName = "香港特别行政区";
                    } else if ("33".equals(provinceCode)) {
                        provinceName = "澳门特别行政区";
                    } else {
                        provinceName = subObject.getString("prov");
                    }

                    //将数据以键值对形式保存到map集合中，通知map集合会对重复的键值对进行二次覆盖
                    map.put(provinceCode, provinceName);

                }
                //将map中的数据存到数据库中
                Set<Map.Entry<String, String>> entrySet = map.entrySet();
                Iterator<Map.Entry<String, String>> iterator = entrySet.iterator();
                while (iterator.hasNext()) {
                    Province province = new Province();
                    Map.Entry<String, String> me = iterator.next();

                    //保存省份code时，加上前缀“CN101”
                    province.setProvinceCode("CN101" + me.getKey());
                    province.setProvinceName(me.getValue());

                    coolWeatherDB.saveProvince(province);
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return true;
        }
        return false;

    }

    /**
     * 根据传入的proviceId,解析具体某个城市CITY的信息，并保存到数据库中
     *
     * @param coolWeatherDB
     * @param response
     * @param provinceId    省份的id
     * @param provinceCode  省份的code
     * @return
     */
    public static boolean handleCitiesResponse(CoolWeatherDB coolWeatherDB, String response,
                                               int provinceId, String provinceCode) {
        if (!TextUtils.isEmpty(response)) {
            Map<String, String> map = new HashMap<>();

            String cityCode;
            String cityName;
            String checkCode;
            try {
                JSONObject object = new JSONObject(response);
                JSONArray jsonArray = object.getJSONArray("city_info");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject subObject = jsonArray.getJSONObject(i);
                    String id = subObject.getString("id");
                    //第7-8位的code代表某个城市,第9-10为“01”用来记录该城市的名称
                    cityCode = id.substring(7, 9);
                    checkCode = id.substring(9, 11);

                    //首先检测是需要的省份，减少查询次数
                    if (!(id.startsWith(provinceCode)))
                        continue;
                    //查询JSON数据可知，需要对直辖市做特殊处理，因为7-8的code不统一
                    if (id.startsWith("CN10101")) {
                        cityName = "北京";
                        cityCode = "";
                        map.put(cityCode, cityName);
                        continue;
                    } else if (id.startsWith("CN10102")) {
                        cityName = "上海";
                        cityCode = "";
                        map.put(cityCode, cityName);
                        continue;


                    } else if (id.startsWith("CN10103")) {
                        cityName = "天津";
                        cityCode = "";
                        map.put(cityCode, cityName);
                        continue;


                    } else if (id.startsWith("CN10104")) {
                        cityName = "重庆";
                        cityCode = "";
                        map.put(cityCode, cityName);
                        continue;


                    } else {
                        if ("01".equals(checkCode)) {
                            cityName = subObject.getString("city");
                            map.put(cityCode, cityName);
                        } else
                            continue;
                    }
                }
                //将map中的数据存到数据库中
                Set<Map.Entry<String, String>> entrySet = map.entrySet();
                Iterator<Map.Entry<String, String>> iterator = entrySet.iterator();
                while (iterator.hasNext()) {
                    City city = new City();
                    Map.Entry<String, String> me = iterator.next();
                    city.setProvinceId(provinceId);
                    //对城市code存储，"CN10132"+"xx"存储，直辖市依然为传入的参数即proviceCode
                    city.setCityCode(provinceCode + me.getKey());
                    city.setCityName(me.getValue());

                    coolWeatherDB.saveCity(city);
                }
                return true;


            } catch (JSONException e) {
                e.printStackTrace();
            }
            return true;
        }
        return false;
    }

    /**
     * 解析县地区信息，并保存到数据库
     *
     * @param coolWeatherDB
     * @param response
     * @param cityId        查询的城市的ID
     * @param cityCode      查询的城市的CODE
     * @return
     */

    public static boolean handleCountiesResponse(CoolWeatherDB coolWeatherDB, String response,
                                                 int cityId, String cityCode) {
        if (!TextUtils.isEmpty(response)) {
            String countyCode;
            String countyName;
            Map<String, String> map = new HashMap<>();
            try {
                JSONObject jsonObject = new JSONObject(response);
                JSONArray jsonArray = jsonObject.getJSONArray("city_info");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject subObject = jsonArray.getJSONObject(i);
                    String id = subObject.getString("id");

                    if (!(id.startsWith(cityCode)))
                        continue;
                    else {
                        countyCode = subObject.getString("id");
                        countyName = subObject.getString("city");
                    }
                    map.put(countyCode, countyName);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            Set<Map.Entry<String, String>> entrySet = map.entrySet();
            Iterator<Map.Entry<String, String>> iterator = entrySet.iterator();
            while (iterator.hasNext()) {
                County county = new County();
                Map.Entry<String, String> me = iterator.next();
                county.setCityId(cityId);
                county.setCountyCode(me.getKey());
                county.setCountyName(me.getValue());
                coolWeatherDB.saveCounty(county);
            }
            return true;
        }

        return false;
    }

    /**
     * 解析天气信息JSON数据，并保存到本地文件
     *
     * @param context  上下文
     * @param response JSON数据
     */

    public static boolean handleWeatherInfo(Context context, String response) {
        try {
            JSONObject object = new JSONObject(response);
            JSONArray array = object.getJSONArray("HeWeather data service 3.0");
            JSONObject subObject = null;
            if (array.length() > 0) {
                subObject = array.getJSONObject(0);
            }
            if (subObject != null) {
                //获取城市名称和ID
                JSONObject basic = subObject.getJSONObject("basic");
                String cityName = basic.getString("city");
                String cityId = basic.getString("id");
                //获取更新时间
                String updateTime = basic.getJSONObject("update").getString("loc").substring(11);
                //获取天气最低最高温度
                String temp1 = subObject.getJSONArray("daily_forecast").getJSONObject(0).getJSONObject("tmp").getString("min");
                String temp2 = subObject.getJSONArray("daily_forecast").getJSONObject(0).getJSONObject("tmp").getString("max");
                //获取天气状况信息
                String weatherInfo = subObject.getJSONObject("now").getJSONObject("cond").getString("txt");

                //将天气信息存入本地文件保存
                saveWeatherInfo(context, cityName, cityId, updateTime, temp1, temp2, weatherInfo);

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return true;

    }

    /**
     * 将天气信息保存到本地文件当中
     *
     * @param context
     * @param cityName
     * @param cityId
     * @param updateTime
     * @param temp1
     * @param temp2
     * @param weatherInfo
     */
    public static void saveWeatherInfo(Context context, String cityName, String cityId, String updateTime, String temp1,
                                       String temp2, String weatherInfo) {
        //存入当前日期
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy年M月d日", Locale.CHINA);
        //存入天气信息
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putBoolean("city_selected", true)
                .putString("city_name", cityName)
                .putString("city_id", cityId)
                .putString("update_time", updateTime)
                .putString("temp1", temp1)
                .putString("temp2", temp2)
                .putString("weather_info", weatherInfo)
                .putString("current_date", simpleDateFormat.format(new Date()));
        editor.apply();
    }
}
