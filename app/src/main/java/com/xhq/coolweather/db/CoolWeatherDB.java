package com.xhq.coolweather.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.xhq.coolweather.model.City;
import com.xhq.coolweather.model.County;
import com.xhq.coolweather.model.Province;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Xiaoq on 2016-5-15.
 * 常用数据库类操作封装类
 */
public class CoolWeatherDB {
    /**
     * 数据库名
     */
    public static final String DB_NAME = "cool_weather";
    /**
     * 数据库版本
     */
    public static final int VERSION = 1;

    private static CoolWeatherDB coolWeatherDB;
    private SQLiteDatabase db;

    /**
     * 构造方法私有化
     *
     * @param context
     */
    private CoolWeatherDB(Context context) {
        CoolWeatherOpenHelper dbHelper = new CoolWeatherOpenHelper(context, DB_NAME, null, VERSION);
        db = dbHelper.getWritableDatabase();
    }

    /**
     * 获取CoolWeatherDB实例
     */
    public synchronized static CoolWeatherDB getInstance(Context context) {
        if (coolWeatherDB == null) {
            coolWeatherDB = new CoolWeatherDB(context);
        }
        return coolWeatherDB;
    }

    /**
     * 将Province实例存储到数据库
     */
    public void saveProvince(Province province) {
        if (province != null) {
            ContentValues values = new ContentValues();
            values.put("province_name", province.getProvinceName());
            values.put("province_code", province.getProvinceCode());
            db.insert("Province", null, values);
        }
    }

    /**
     * 从数据库读取全国所有Province信息
     */
    public List<Province> loadProvince() {
        List<Province> list = new ArrayList<>();
        Cursor cursor = db.query("Province", null, null, null, null, null, null);
        while (cursor.moveToNext()) {
            Province province = new Province();
            province.setId(cursor.getInt(cursor.getColumnIndex("id")));
            province.setProvinceName(cursor.getString(cursor.getColumnIndex("province_name")));
            province.setProvinceCode(cursor.getString(cursor.getColumnIndex("province_code")));
            list.add(province);
        }
        if (cursor != null) {
            cursor.close();
        }
        return list;
    }

    /**
     * 将City实例存储到数据库
     */
    public void saveCity(City city) {
        if (city != null) {
            ContentValues values = new ContentValues();
            values.put("city_name", city.getCityName());
            values.put("city_code", city.getCityCode());
            values.put("province_id", city.getProvinceId());
            db.insert("City", null, values);
        }
    }

    /**
     * 从数据库读取所有City的信息
     */
    public List<City> loadCity(int proviceId) {
        List<City> list = new ArrayList<>();
        Cursor cursor = db.query("City", null, "province_id=?", new String[]{String.valueOf(proviceId)},
                null, null, null);
        while (cursor.moveToNext()) {
            City city = new City();
            city.setId(cursor.getInt(cursor.getColumnIndex("id")));
            city.setCityCode(cursor.getString(cursor.getColumnIndex("city_code")));
            city.setCityName(cursor.getString(cursor.getColumnIndex("city_name")));
            city.setProvinceId(cursor.getInt(cursor.getColumnIndex("province_id")));
        }
        if (cursor != null) {
            cursor.close();
        }
        return list;
    }

    /**
     * 将County实例存储到数据库
     */
    public void saveCounty(County county) {
        if (county != null) {
            ContentValues values = new ContentValues();
            values.put("county_name", county.getCountyName());
            values.put("county_code", county.getCountyCode());
            values.put("province_id", county.getCityId());
            db.insert("County", null, values);
        }
    }

    /**
     * 从数据中读取所有County信息
     */
    public List<County> loadCounty(int cityId) {
        Cursor cursor = db.query("County", null, "city_id=?", new String[]{String.valueOf(cityId)},
                null, null, null);
        List<County> list = new ArrayList<>();
        while (cursor.moveToNext()) {
            County county = new County();
            county.setCityId(cursor.getInt(cursor.getColumnIndex("city_id")));
            county.setCountyCode(cursor.getString(cursor.getColumnIndex("county_code")));
            county.setCountyName(cursor.getString(cursor.getColumnIndex("county_name")));
            county.setId(cursor.getInt(cursor.getColumnIndex("id")));
            list.add(county);
        }
        if (cursor != null) {
            cursor.close();
        }
        return list;
    }


}
