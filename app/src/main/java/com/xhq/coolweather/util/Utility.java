package com.xhq.coolweather.util;

import android.text.TextUtils;
import android.util.Log;

import com.xhq.coolweather.db.CoolWeatherDB;
import com.xhq.coolweather.model.City;
import com.xhq.coolweather.model.County;
import com.xhq.coolweather.model.Province;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by Xiaoq on 2016-5-19.
 */
public class Utility {

    /**
     * 解析和处理Province信息数据，并存到数据库
     */
    public synchronized static boolean handleProvincesResponse(CoolWeatherDB coolWeatherDB, String response) {
        long start;
        long end;
        if (!TextUtils.isEmpty(response)) {
            start = System.currentTimeMillis();
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
                end = System.currentTimeMillis();
                Log.d("test", end - start + "(-查询省份运行时间millis-)");
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
        long start;
        long end;
        if (!TextUtils.isEmpty(response)) {
            start = System.currentTimeMillis();
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
                end = System.currentTimeMillis();
                Log.d("test", end - start + "(-查询city运行时间millis-)");
                return true;


            } catch (JSONException e) {
                e.printStackTrace();
            }
            return true;
        }
        return false;
    }

    public static boolean handleCountiesResponse(CoolWeatherDB coolWeatherDB, String response,
                                                 int cityId, String cityCode) {
        long start;
        long end;
        if (!TextUtils.isEmpty(response)) {
            start = System.currentTimeMillis();
            String countyCode;
            String countyName;
            Map<String, String> map = new HashMap<>();
            try {
                JSONObject jsonObject = new JSONObject(response);
                JSONArray jsonArray = jsonObject.getJSONArray("city_info");
                Log.d("test", "array" + jsonArray.length());
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
            Log.d("test",map.size()+"size-");
            while (iterator.hasNext()) {
                County county = new County();
                Map.Entry<String, String> me = iterator.next();
                county.setCityId(cityId);
                county.setCountyCode(me.getKey());
                county.setCountyName(me.getValue());
                coolWeatherDB.saveCounty(county);
            }
            end = System.currentTimeMillis();
            Log.d("test", end - start + "(--查询city时间millis)");
            return true;
        }

        return false;
    }
}
