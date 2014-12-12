package com.coolweather.app.common;

import android.text.TextUtils;

import com.coolweather.app.db.CoolWeatherDB;
import com.coolweather.app.model.City;
import com.coolweather.app.model.County;
import com.coolweather.app.model.Province;

public class DatabaseUtils {
	
	public static final String COMMA_SYMBOL_EN = ",";
	public static final String VERTICAL_LINE = "\\|";
	
	/**
	 * 解析和处理服务器返回的省级数据，并存入数据库
	 * @param db
	 * @param response
	 * @return
	 */
	public synchronized static boolean handleProvincesResponse(CoolWeatherDB db, String response) {
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

}
