package com.coolweather.app.common;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import com.coolweather.app.db.CoolWeatherDB;
import com.coolweather.app.model.City;
import com.coolweather.app.model.County;
import com.coolweather.app.model.Province;

public class DatabaseUtils {
	
	public static final String COMMA_SYMBOL_EN = ",";
	public static final String VERTICAL_LINE = "\\|";
	private static List<String> lst = new ArrayList<String>();
	
	/**
	 * 解析天气代码
	 * @param response
	 * @return
	 */
	public static String findWeatherCode(String response) {
		if(!TextUtils.isEmpty(response)) {
			String[] array = response.split(VERTICAL_LINE);
			if ((array != null) && (array.length == 2)) {
				return array[1];
			}
		} 
		return "";
	}
	
	/**
	 * 解析和处理服务器返回的天气数据，并存入SharePerence中
	 * @param context
	 * @param response
	 */
	public static synchronized boolean handleWeatherResponse(Context context, String response) {
		if(!TextUtils.isEmpty(response)) {
			try {
				JSONObject jsonObject = new JSONObject(response);
				JSONObject weatherInfo = jsonObject.getJSONObject("weatherinfo");
				String cityName = weatherInfo.getString("city");
				String weatherCode = weatherInfo.getString("cityid");
				String temp1 = weatherInfo.getString("temp1");
				String temp2 = weatherInfo.getString("temp2");
				String weatherDesp = weatherInfo.getString("weather");
				String publishTime = weatherInfo.getString("ptime");
				saveWeatherInfo(context, cityName, weatherCode, temp1, temp2, weatherDesp, publishTime);
				return true;
			} catch (JSONException e) {
				e.printStackTrace();
				return false;
			}
		} else {
			return false;
		}
	}
	
	/**
	 * 将天气信息保存到SharePerence中
	 * @param context
	 * @param cityName
	 * @param weatherCode
	 * @param temp1
	 * @param temp2
	 * @param weatherDesp
	 * @param publishTime
	 */
	public static void saveWeatherInfo(Context context, String cityName, 
			String weatherCode, String temp1, String temp2, String weatherDesp, String publishTime) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy年M月d日", Locale.CHINA);
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		Set<String> citySet = prefs.getStringSet("city_set", new LinkedHashSet<String>(3));
		SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean("city_selected", true);
		editor.putString("city_name", cityName);
		editor.putString("weather_code", weatherCode);
		editor.putString("weather_desp", weatherDesp);
		editor.putString("temp1", temp1);
		editor.putString("temp2", temp2);
		editor.putString("publish_time", publishTime);
		editor.putString("current_date", sdf.format(new Date()));
		for (String string : citySet) {
			addPopLst(string);
		}
		addPopLst(cityName.trim() + "|" + weatherCode.trim());
		citySet.addAll(lst);
		editor.putStringSet("city_set", citySet);
		editor.commit();
	}
	
	/**
	 * 解析和处理服务器返回的省级数据，并存入数据库
	 * @param db
	 * @param response
	 * @return
	 */
	public synchronized static boolean handleProvincesResponse(CoolWeatherDB db, String response) {
		Log.d("method", "handleProvincesResponse response:" + response);
		if(!TextUtils.isEmpty(response)) {
			String[] allProvinces = response.split(COMMA_SYMBOL_EN);
			if (allProvinces != null && allProvinces.length > 0) {
				for (String p : allProvinces) {
					String[] array = p.split(VERTICAL_LINE);
					Province province = new Province();
					province.setProvinceCode(array[0]);
					province.setProvinceName(array[1]);
					db.saveProvince(province);
				}
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 解析和处理服务器返回的市级数据，并存入数据库
	 * @param db
	 * @param response
	 * @param provinceId 省ID
	 * @return
	 */
	public synchronized static boolean handleCitiesResponse(CoolWeatherDB db, String response, int provinceId) {
		if(!TextUtils.isEmpty(response)) {
			String[] allCities = response.split(COMMA_SYMBOL_EN);
			if (allCities != null && allCities.length > 0) {
				for (String c : allCities) {
					String[] array = c.split(VERTICAL_LINE);
					City city = new City();
					city.setCityCode(array[0]);
					city.setCityName(array[1]);
					city.setProvinceId(provinceId);
					db.saveCity(city);
				}
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 解析和处理服务器返回的县级数据，并存入数据库
	 * @param db
	 * @param response
	 * @param cityId
	 * @return
	 */
	public synchronized static boolean handleCountiesResponse(CoolWeatherDB db, String response, int cityId) {
		if(!TextUtils.isEmpty(response)) {
			String[] allCounties = response.split(COMMA_SYMBOL_EN);
			if (allCounties != null && allCounties.length > 0) {
				for (String c : allCounties) {
					String[] array = c.split(VERTICAL_LINE);
					County county = new County();
					county.setCountyCode(array[0]);
					county.setCountyName(array[1]);
					county.setCityId(cityId);
					db.saveCounty(county);
				}
				return true;
			}
		}
		return false;
	}
	
	
    /**
     * Java用ArrayList实现先进先出的队列通用方法,同时去除重复了
     * @param newstr
     * @return
     */
    public static List<String> addPopLst(String newstr){
    	if(!TextUtils.isEmpty(newstr)){
    		if(lst.contains(newstr))
    			return lst;
    		lst.add(0,newstr);
        	if(lst.size()>3){
        		lst.remove(3);
        	}
    	}
    	return lst;
    }

}
