package com.coolweather.app.ui;

import java.util.ArrayList;
import java.util.List;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.coolweather.app.R;
import com.coolweather.app.common.DatabaseUtils;
import com.coolweather.app.common.HttpCallbackListener;
import com.coolweather.app.common.HttpUtils4Android;
import com.coolweather.app.common.UIHelper;
import com.coolweather.app.db.CoolWeatherDB;
import com.coolweather.app.model.City;
import com.coolweather.app.model.County;
import com.coolweather.app.model.Province;

public class ChooseAreaActivity extends BaseActivity {
	
	private TextView tvTitle;
	private ListView listView;

	private ArrayAdapter<String> adapter;
	private CoolWeatherDB coolWeatherDB;
	private List<String> list = new ArrayList<String>();
	
	private List<Province> provinceList;
	private List<City> cityList;
	private List<County> countyList;
	
	/**
	 * 选中的省
	 */
	private Province selectedProvince;
	/**
	 * 选中的市
	 */
	private City selectedCity;
	
	/**
	 * 进度条对话框
	 */
	private ProgressDialog progressDialog;
	
	public static final int LEVEL_PROVINCE = 0;
	public static final int LEVEL_CITY = 1;
	public static final int LEVEL_COUNTY = 2;
	private int currentLevel = 0;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.choose_area);
		
		tvTitle = (TextView) findViewById(R.id.title_text);
		listView = (ListView) findViewById(R.id.list_view);
		
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, list);
		listView.setAdapter(adapter);
		coolWeatherDB = CoolWeatherDB.getInstance(this);
		// 加载省数据
		queryProvinces();
		// listview 点击事件
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (currentLevel == LEVEL_PROVINCE) {
					selectedProvince = provinceList.get(position);
					queryCities();
				} else if (currentLevel == LEVEL_CITY) {
					selectedCity = cityList.get(position);
					queryCounties();
				} else if (currentLevel == LEVEL_COUNTY) {
					UIHelper.showWeather(ChooseAreaActivity.this, countyList.get(position).getCountyCode());
				}
			}
		});
		
	}
	
	/**
	 * 获取省数据
	 */
	private void queryProvinces() {
		Log.d("method", "queryProvinces");
		if(coolWeatherDB != null) {
			provinceList = coolWeatherDB.loadProvinces();
		}
		if ((provinceList!=null) && (provinceList.size() > 0)) {
			// 从数据库中获取
			list.clear();
			for (Province p : provinceList) {
				list.add(p.getProvinceName());
			}
			adapter.notifyDataSetChanged(); // 刷新adapter
			listView.setSelection(0);
			tvTitle.setText("中国");
			currentLevel = LEVEL_PROVINCE;
		} else {
			Log.d("method", "queryFromServer");
			// 从服务器端请求
			queryFromServer(null, "province");
		}
	}
	
	/**
	 * 获取市数据
	 */
	private void queryCities() {
		if(coolWeatherDB != null) {
			cityList = coolWeatherDB.loadCities(selectedProvince.getId());
		}
		if ((cityList != null) && (cityList.size() > 0)) {
			list.clear();
			for (City c : cityList) {
				list.add(c.getCityName());
			}
			adapter.notifyDataSetChanged(); // 刷新adapter
			listView.setSelection(0);
			tvTitle.setText(selectedProvince.getProvinceName()); // 当前省份
			currentLevel = LEVEL_CITY;
		} else {
			queryFromServer(selectedProvince.getProvinceCode(), "city");
		}
	}
	
	/**
	 * 获取县级数据
	 */
	private void queryCounties() {
		if(coolWeatherDB != null) {
			countyList = coolWeatherDB.loadCounties(selectedCity.getId());
		}
		if((countyList != null) && (countyList.size() > 0)) {
			list.clear();
			for (County c : countyList) {
				list.add(c.getCountyName());
			}
			adapter.notifyDataSetChanged(); // 刷新adapter
			listView.setSelection(0);
			tvTitle.setText(selectedCity.getCityName()); // 当前城市
			currentLevel = LEVEL_COUNTY;
		} else {
			queryFromServer(selectedCity.getCityCode(), "county");
		}
	}
	
	/**
	 * 从服务器端获取数据
	 * @param code
	 * @param type
	 */
	private void queryFromServer(final String code, final String type) {
		String address = "http://www.weather.com.cn/data/list3/city";
		if (!TextUtils.isEmpty(code)) {
			address = address + code + ".xml";
		} else {
			address = address + ".xml";
		}
		// 打开进度对话框
		showProgressDialog();
		Log.d("method", "address:" + address);
		HttpUtils4Android.sendHttpRequest(address, new HttpCallbackListener() {
			
			@Override
			public void onFinish(String response) {
				Log.d("method", "response:" + response);
				boolean result = false;
				if ("province".equals(type)) {
					result = DatabaseUtils.handleProvincesResponse(coolWeatherDB, response);
				} else if ("city".equals(type)) {
					result = DatabaseUtils.handleCitiesResponse(coolWeatherDB, response, selectedProvince.getId());
				} else if ("county".equals(type)) {
					result = DatabaseUtils.handleCountiesResponse(coolWeatherDB, response, selectedCity.getId());
				}
				if (result) {
					runOnUiThread(new Runnable() {
						public void run() {
							// 关闭进度对话框
							closeProgressDialog();
							if ("province".equals(type)) {
								queryProvinces();
							} else if ("city".equals(type)) {
								queryCities();
							} else if ("county".equals(type)) {
								queryCounties();
							}
						}
					});
				}
			}
			
			@Override
			public void onError(Exception e) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(ChooseAreaActivity.this, "加载失败", Toast.LENGTH_SHORT).show();
					}
				});
			}
		});
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

}
