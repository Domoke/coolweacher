package com.coolweather.app.ui;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.coolweather.app.R;
import com.coolweather.app.common.DatabaseUtils;
import com.coolweather.app.common.DoubleClickExitHelper;
import com.coolweather.app.common.HttpCallbackListener;
import com.coolweather.app.common.HttpUtils4Android;
import com.coolweather.app.common.LogUtils;
import com.coolweather.app.common.UIHelper;
import com.coolweather.app.service.AutoUpdateService;

public class WeatherActivity extends BaseActivity {
	
	private Button switchCity;
	private Button refreshWeather;
	
	private LinearLayout weatherInfoLayout;
	private TextView tvCityName;
	private TextView tvPublishTime;
	private TextView tvCurrentDate;
	private TextView tvWeatherDesp;
	private TextView tvTemp1;
	private TextView tvTemp2;
	
	private Button btnCity1;
	private Button btnCity2;
	private Button btnCity3;
	
	private String countyCode;
	
	// 存储城市信息
	private Map<String, String> cityMap = new HashMap<String, String>();
	
	/**
	 * 进度条对话框
	 */
	private ProgressDialog progressDialog;
	
	/**
	 * 双击退出帮助对象
	 */
	private DoubleClickExitHelper mDoubleClickExitHelper;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.weather_layout);
		
		// 初始化双击退出帮助对象
		mDoubleClickExitHelper = new DoubleClickExitHelper(this);
		// 初始化布局
		initView();
		
		Intent intent = getIntent();
		countyCode = intent.getStringExtra("county_code");
		Log.d("method", "countyCode:" + countyCode);
		if (!TextUtils.isEmpty(countyCode)) {
			tvPublishTime.setText("同步中...");
			weatherInfoLayout.setVisibility(View.INVISIBLE);
			tvCityName.setVisibility(View.INVISIBLE);
			queryWeatherCode(countyCode);
		} else {
			// 没有县级代号时就直接显示本地天气
			showWeather();
		}
		
	}
	
	/**
	 * 初始化布局组件，注册按钮点击事件
	 */
	private void initView() {
		switchCity = (Button) findViewById(R.id.switch_city);
		refreshWeather = (Button) findViewById(R.id.refresh_weather);
		weatherInfoLayout = (LinearLayout) findViewById(R.id.weather_info_layout);
		tvCityName = (TextView) findViewById(R.id.city_name);
		tvCurrentDate = (TextView) findViewById(R.id.current_date);
		tvPublishTime = (TextView) findViewById(R.id.publish_time);
		tvTemp1 = (TextView) findViewById(R.id.temp1);
		tvTemp2 = (TextView) findViewById(R.id.temp2);
		tvWeatherDesp = (TextView) findViewById(R.id.weather_desp);
		btnCity1 = (Button) findViewById(R.id.city1);
		btnCity2 = (Button) findViewById(R.id.city2);
		btnCity3 = (Button) findViewById(R.id.city3);
		
		switchCity.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				UIHelper.showSwitchCity(WeatherActivity.this, true);
			}
		});
		
		refreshWeather.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				queryWeatherCode(countyCode);
			}
		});
		
		btnCity1.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String cityName = (String) btnCity1.getText();
				if (!TextUtils.isEmpty(cityName)) {
					LogUtils.d("method", cityMap.get(cityName));
					queryWeatherInfo(cityMap.get(cityName));
				}
			}
		});
		
		btnCity2.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String cityName = (String) btnCity2.getText();
				if (!TextUtils.isEmpty(cityName)) {
					LogUtils.d("method", cityMap.get(cityName));
					queryWeatherInfo(cityMap.get(cityName));
				}
			}
		});
		
		btnCity3.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String cityName = (String) btnCity3.getText();
				if (!TextUtils.isEmpty(cityName)) {
					LogUtils.d("method", cityMap.get(cityName));
					queryWeatherInfo(cityMap.get(cityName));
				}
			}
		});
	}
	
	/**
	 * 查询县级代号所对应的天气代号
	 * @param countyCode
	 */
	private void queryWeatherCode(String countyCode) {
		String address = "http://www.weather.com.cn/data/list3/city" + countyCode + ".xml";
		queryFromServer(address, "countyCode");
	}
	
	/**
	 * 查询天气代号对应的天气
	 * @param weatherCode
	 */
	private void queryWeatherInfo(String weatherCode) {
		String address = "http://www.weather.com.cn/data/cityinfo/" + weatherCode + ".html";
		queryFromServer(address, "weatherCode");
	}
	
	/**
	 * 从服务器端获取数据
	 * @param address
	 * @param type
	 */
	private void queryFromServer(final String address, final String type) {
		Log.d("method", "WeatherActivity.queryFromServer");
		// 打开进度对话框
		showProgressDialog();
		HttpUtils4Android.sendHttpRequest(address, new HttpCallbackListener() {
			
			@Override
			public void onFinish(String response) {
				if("countyCode".equals(type)) {
					String weatherCode = DatabaseUtils.findWeatherCode(response);
					if (!TextUtils.isEmpty(weatherCode)) {
						queryWeatherInfo(weatherCode);
					} else {
						// 关闭进度对话框
						closeProgressDialog();
					}
				} else if ("weatherCode".equals(type)) {
					if (DatabaseUtils.handleWeatherResponse(WeatherActivity.this, response)) {
						runOnUiThread(new Runnable() {
							public void run() {
								// 关闭进度对话框
								closeProgressDialog();
								showWeather();
							}
						});
					} else {
						closeProgressDialog();
						Toast.makeText(WeatherActivity.this, "加载失败", Toast.LENGTH_SHORT).show();
					}
				}
			}
			
			@Override
			public void onError(Exception e) {
				closeProgressDialog();
				Toast.makeText(WeatherActivity.this, "加载失败", Toast.LENGTH_SHORT).show();
			}
		});
	}
	
	/**
	 * 从SharedPreferences文件中读取已存储的天气信息，并显示
	 */
	private void showWeather() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		tvCityName.setText(prefs.getString("city_name", ""));
		tvPublishTime.setText("今天" + prefs.getString("publish_time", "") + "发布");
		tvCurrentDate.setText(prefs.getString("current_date", ""));
		tvTemp1.setText(prefs.getString("temp1", ""));
		tvTemp2.setText(prefs.getString("temp2", ""));
		tvWeatherDesp.setText(prefs.getString("weather_desp", ""));
		weatherInfoLayout.setVisibility(View.VISIBLE);
		tvCityName.setVisibility(View.VISIBLE);
		Set<String> citySet = prefs.getStringSet("city_set", new LinkedHashSet<String>(3));
		for (String string : citySet) {
			// cityName|weatherCode
			String[] array = string.split("\\|");
			cityMap.put(array[0], array[1]);
			LogUtils.d("method", string);
		}
		if(!cityMap.isEmpty()) {
			int i = 1;
			for (String str : cityMap.keySet()) {
				if(i == 1) {
					btnCity1.setText(str);
					btnCity1.setVisibility(View.VISIBLE);
				}
				if(i == 2) {
					btnCity2.setText(str);
					btnCity2.setVisibility(View.VISIBLE);
				}
				if(i == 3) {
					btnCity3.setText(str);
					btnCity3.setVisibility(View.VISIBLE);
				}
				i++;
			}
		}
		// 启动自动更新服务
		Intent service = new Intent(this, AutoUpdateService.class);
		startService(service);
	}
	
	/**
	 * 显示进度对话框
	 */
	private void showProgressDialog() {
		if (progressDialog == null) {
			progressDialog = new ProgressDialog(this);
			progressDialog.setMessage("正在加载...");
			progressDialog.setCanceledOnTouchOutside(false);
		}
		progressDialog.show();
	}
	
	/**
	 * 关闭进度对话框
	 */
	private void closeProgressDialog() {
		if (progressDialog != null) {
			progressDialog.dismiss();
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		boolean flag = true;
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			// 是否退出应用
			return mDoubleClickExitHelper.onKeyDown(keyCode, event);
		}
		return flag;
	}

}
