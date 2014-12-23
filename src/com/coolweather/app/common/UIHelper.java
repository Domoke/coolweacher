package com.coolweather.app.common;

import android.app.Activity;
import android.content.Intent;

import com.coolweather.app.ui.ChooseAreaActivity;
import com.coolweather.app.ui.WeatherActivity;

/**
 * 应用程序UI工具包：封装UI相关的一些操作
 * @author Chenxj
 *
 */
public class UIHelper {
	
	/**
	 * 切换城市
	 * @param activity
	 */
	public static void showSwitchCity(Activity activity, boolean isFromWeatherActivity) {
		Intent intent = new Intent(activity, ChooseAreaActivity.class);
		intent.putExtra("is_from_weather_activity", isFromWeatherActivity);
		activity.startActivity(intent);
		activity.finish();
	}
	
	/**
	 * 显示天气
	 * @param activity
	 * @param cityCode
	 */
	public static void showWeather(Activity activity, String countyCode) {
		Intent intent = new Intent(activity, WeatherActivity.class);
		intent.putExtra("county_code", countyCode);
		activity.startActivity(intent);
		activity.finish();
	}

}
