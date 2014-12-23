package com.coolweather.app.common;

import com.coolweather.app.ui.ChooseAreaActivity;
import com.coolweather.app.ui.WeatherActivity;

import android.app.Activity;
import android.content.Intent;

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
	public static void showSwitchCity(Activity activity) {
		Intent intent = new Intent(activity, ChooseAreaActivity.class);
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
