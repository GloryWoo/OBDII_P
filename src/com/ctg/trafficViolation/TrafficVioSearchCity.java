package com.ctg.trafficViolation;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.ctg.bean.City;
import com.ctg.bean.Province;
import com.ctg.trafficViolation.TrafficVioSearchCityAdapter.OnCitySelected;
import com.ctg.trafficViolation.TrafficVioSearchCityAdapter.OnProvinceSelected;
import com.ctg.ui.Base;
import com.ctg.ui.R;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class TrafficVioSearchCity extends Activity implements OnClickListener {
	// private final st
	private GridView gridView;
	private TrafficVioSearchCityAdapter adapter;
	private List<Province> provinces;
	private List<City> cityList;
	private Province province;
	private City city;
	private TextView txt_choose;
	private TextView txt_back;
	private Button btn_close;
	private int type = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.trafficvio_searchcity);
		
		initWindow();
		initView();
		initListener();
	}

	private void initWindow() {
		// TODO Auto-generated method stub
		Display display = getWindowManager().getDefaultDisplay(); // 为获取屏幕宽、高  
		 Window window = getWindow();  
		LayoutParams windowLayoutParams = window.getAttributes(); // 获取对话框当前的参数值  
		windowLayoutParams.width = (int) (display.getWidth() * 0.8); // 宽度设置为屏幕的0.8 
		windowLayoutParams.height = (int) (display.getHeight() * 0.7); // 高度设置为屏幕的0.8 
		//windowLayoutParams.alpha = 0.5f;// 设置透明度
	}

	public void initView() {

		provinces = new ArrayList<Province>();
		cityList = new ArrayList<City>();
		txt_choose = (TextView) findViewById(R.id.weizh_search_text);
		txt_back = (TextView) findViewById(R.id.weizh_re_search_text);
		btn_close = (Button) findViewById(R.id.weizh_search_close);

		//getCityFromAssets("cheshouye.json");
		getCityFromAssets("juhe.json");

		gridView = (GridView) findViewById(R.id.weizh_search_citygrid);
		adapter = new TrafficVioSearchCityAdapter(TrafficVioSearchCity.this, provinces,
				cityList, type);

		gridView.setAdapter(adapter);
	}

	public Comparator<City> comparator = new Comparator<City>(){

		@Override
		public int compare(City lhs, City rhs) {
			// TODO Auto-generated method stub
			return lhs.getCarHead().compareTo(rhs.getCarHead());
		}
		
	};
	public void initListener() {
		txt_choose.setOnClickListener(this);
		txt_back.setOnClickListener(this);
		btn_close.setOnClickListener(this);

		gridView.setOnItemClickListener(new OnItemClickListener() {

			@SuppressWarnings("unchecked")
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				if (type == 0) {// 如果是省份线判断是不是直辖市					
					txt_back.setVisibility(View.VISIBLE);
					txt_choose.setText("选择城市");
					cityList.clear();
					cityList.addAll(provinces.get(position).getCitys());
//					Collections.sort(cityList,);
					Collections.sort(cityList, comparator);
					onProvinceSelected(cityList);
				} else if (type == 1) {
					// 给主Activity发送一个广播

					onCitySelected(cityList.get(position).getEngineno(),
							cityList.get(position).getClassno(),
							cityList.get(position).getRegistno(),
							cityList.get(position).getEngine(),
							cityList.get(position).getClassa(),
							cityList.get(position).getRegist(),
							cityList.get(position).getAbbr(),
							cityList.get(position).getCity_name(),
							cityList.get(position).getCarHead());

					TrafficVioSearchCity.this.finish();
				}
			}
		});
	}

	@SuppressWarnings("finally")
	public void getCityFromAssets(String fileName) {
		Map<String, String> name2head = parseCityName2CarHead("cheshouye.json");
		
		// String line = "";
		// StringBuilder result = new StringBuilder();
		InputStream is;
		Log.v("json", "start");
		try {

			is = getResources().getAssets().open(fileName);
			byte[] buffer = new byte[is.available()];
			is.read(buffer);
			String json = new String(buffer, "utf-8");
			is.close();

			JSONObject jsonObj = new JSONObject(json);
			JSONObject obj = jsonObj.getJSONObject("result");
			Iterator<?> it = obj.keys();
			while( it.hasNext() ){
				String key = (String) it.next();  
				JSONObject value = obj.getJSONObject(key);				
				String provinceName = value.getString("province");
				String provinceCode = value.getString("province_code");
				province = new Province();
				// province.setProvince_id((Integer) proObj.get("province_id"));
				province.setProvince_name(provinceName);

				JSONArray cityArray = value.getJSONArray("citys");
				List<City> cities = new ArrayList<City>();

				for (int i = 0; i < cityArray.length(); i++) {
					city = new City();
					JSONObject citObj = (JSONObject) cityArray.get(i);				
					city.setCity_name(citObj.getString("city_name"));
					city.setCity_code(citObj.getString("city_code"));
					city.setAbbr(citObj.getString("abbr"));
					city.setEngine(citObj.getInt("engine"));
					city.setClassa(citObj.getInt("classa"));
					city.setRegist(citObj.getInt("regist"));
					city.setClassno(citObj.getInt("classno"));
					city.setEngineno(citObj.getInt("engineno"));
					city.setRegistno(citObj.getInt("registno"));
					if(name2head.containsKey(city.getCity_name())){
						city.setCarHead(name2head.get(city.getCity_name()));
						cities.add(city);
					}
				}
				province.setCitys(cities);
				provinces.add(province);
			}

		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private Map<String, String> parseCityName2CarHead(String file_name){
		InputStream is;
		String content = "";
		
		try {
			is = getResources().getAssets().open(file_name);
			byte[] buffer = new byte[is.available()];
			is.read(buffer);
			content = new String(buffer, "utf-8");
			is.close();
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		Map<String, String> map = new HashMap<String,String>();
		
		try {
			JSONObject jsonstr = new JSONObject(content);
			JSONArray jsonArray = jsonstr.getJSONArray("configs");
			
			for (int i = 0; i < jsonArray.length(); i++){
				JSONObject provinces = (JSONObject) jsonArray.get(i);
				JSONArray citys =(JSONArray) provinces.get("citys");
				for (int j = 0; j < citys.length(); j++) {
					JSONObject data= (JSONObject) citys.get(j);
					String head = data.getString("car_head");
					String city_name = data.getString("city_name");
					
					if( city_name != null && !city_name.equals("")){
						if(head != null && !head.equals("")){
							map.put(city_name, head);
						}
					}
					
				}
			}
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// not contained in cheshouye.json
		map.put("铁岭", "辽M");
		map.put("昭通", "云C");
		map.put("曲靖", "云D");
		map.put("楚雄", "云E");
		map.put("玉溪", "云F");
		map.put("红河", "云G");
		map.put("文山", "云H");
		map.put("普洱", "云J");
		map.put("西双版纳", "云K");
		map.put("大理", "云L");
		map.put("保山", "云M");		
		map.put("德宏", "云N");
		map.put("丽江", "云P");
		map.put("迪庆", "云R");		
		map.put("临沧", "云S");
		map.put("汕头", "粤D");
		map.put("韶关", "粤F");
		map.put("湛江", "粤G");
		map.put("茂名", "粤K");
		map.put("汕尾", "粤N");
		map.put("河源", "粤P");
		map.put("清远", "粤R");
		map.put("阳江", "粤Q");		
		map.put("云浮", "粤W");		
		map.put("揭阳", "粤V");		
		map.put("汉中", "陕F");
		map.put("海东", "青B");
		map.put("海北", "青C");
		map.put("海南", "青E");
		map.put("果洛", "青F");
		map.put("海西", "青H");
		map.put("玉树", "青G");
		
		return map;
	}

	public void onProvinceSelected(List<City> cityList) {
		// TODO Auto-generated method stub
		type = 1;
		adapter.setList(null, cityList, type);
		adapter.notifyDataSetChanged();
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.weizh_re_search_text:
			txt_back.setVisibility(View.GONE);
			txt_choose.setText("选择省份");
			type = 0;
			adapter.setList(provinces, null, type);
			adapter.notifyDataSetChanged();
			break;
		case R.id.weizh_search_close:
			this.finish();
			break;
		}
	}

	public void onCitySelected(int engineno, int classno, int registno, int engine, int classa, int regist, String abbr, String cityName, String cityHead) {
		// TODO Auto-generated method stub
		Intent intent = new Intent("com.search_city.SUCCESS");
		Bundle bundle = new Bundle();
		bundle.putInt("engine", engine);
		bundle.putInt("classa", classa);
		bundle.putInt("regist", regist);
		bundle.putInt("registno", registno);
		bundle.putInt("engineno", engineno);
		bundle.putInt("classno", classno);
		bundle.putString("abbr", abbr);
		bundle.putString("cityName", cityName);
		bundle.putString("cityHead", cityHead);
		intent.putExtras(bundle);
		this.sendBroadcast(intent);
	}
}
