package com.ctg.land;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import com.baidu.mapapi.map.MapView;
import com.ctg.group.Member;
import com.ctg.ui.BaiduMapView;
import com.ctg.ui.Base;
import com.ctg.ui.R;
import com.ctg.util.Preference;
import com.ctg.util.Util;
import com.ctg.weather.WeatherReport;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class Common implements View.OnClickListener{
	public LinearLayout common_mapv;
	//public BaiduMapView baidu_v;
	//public MapView baidumapv;
	Base baseAct;
	ImageView setting_i;
	ImageView portrait_i;
	TextView headname_t;
	TextView date_t;
	TextView time_t;
	ImageView wea_i;
	TextView wea_temp_t;
	TextView wea_pm25_t;
	TextView speed_t;
	TextView geofence_t;
	TextView dtc_t;
	TextView ecall_t;
	TextView bcall_t;
	ImageView grp_i;
	ImageView searchIcon;
	EditText searchEdit;
	View date_r;
	View wea_r;
	View speed_r;
	View gfence_r;
	View dtc_r;
	View ecall_r;
	View bcall_r;
	
	//ImageView grpIcon;
	boolean geofenceMode;
	
	public Common(Context context, boolean exist) {
		//common_mapv = v;
		// TODO Auto-generated constructor stub
		baseAct = (Base)context;
		common_mapv = (LinearLayout) View.inflate(context, R.layout.common_mapv, null);
		if(!exist)
			Base.baidu_v = new BaiduMapView(context);
		else
			Base.baidu_v.mMapView.onResume();
		LinearLayout.LayoutParams layout = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 1.0f);
		common_mapv.addView(Base.baidu_v, layout);
		Base.baidu_v.search_rela.setVisibility(View.VISIBLE);
		
		//baidu_v = (BaiduMapView) common_mapv.findViewById(R.id.baiduView);
		//baidumapv = (MapView) common_mapv.findViewById(R.id.bmapView);
		date_r = common_mapv.findViewById(R.id.head_rela);
		wea_r = common_mapv.findViewById(R.id.weather);
		speed_r = common_mapv.findViewById(R.id.speed);
		gfence_r = common_mapv.findViewById(R.id.gfence);
		dtc_r = common_mapv.findViewById(R.id.dtc);
		ecall_r = common_mapv.findViewById(R.id.ecall);
		bcall_r = common_mapv.findViewById(R.id.bcall);
		//setting_i = (ImageView) common_mapv.findViewById(R.id.setting_img);
		portrait_i = (ImageView) common_mapv.findViewById(R.id.head_portrait);
		Bitmap headbitmap = Member.getHeadBitmapUser(Base.loginUser);
		if(headbitmap != null){
			Bitmap bitProc = Util.getRoundedCornerImage(headbitmap);
			portrait_i.setImageBitmap(bitProc);			
		}
		
		//portrait_i.setImage
		headname_t = (TextView) common_mapv.findViewById(R.id.head_username);
		headname_t.setText(Base.loginUser);
		
		date_t = (TextView) common_mapv.findViewById(R.id.date);
		time_t = (TextView) common_mapv.findViewById(R.id.time);
		wea_i = (ImageView) common_mapv.findViewById(R.id.weather_img);
		wea_temp_t = (TextView) common_mapv.findViewById(R.id.temperature);
		wea_pm25_t = (TextView) common_mapv.findViewById(R.id.pm25);
		speed_t = (TextView) common_mapv.findViewById(R.id.speed_t);
		geofence_t = (TextView) common_mapv.findViewById(R.id.gfence_t);
		dtc_t = (TextView) common_mapv.findViewById(R.id.dtc_t);
		ecall_t = (TextView) common_mapv.findViewById(R.id.ecall_t);	
		bcall_t = (TextView) common_mapv.findViewById(R.id.bcall_t);
		//grp_i = (ImageView) common_mapv.findViewById(R.id.mapview_grp_icon);
		//grp_i.setVisibility(View.VISIBLE);
		//grp_i.setBackground(baseAct.getResources().getDrawable(R.drawable.imageview_sel));
		searchIcon = (ImageView) common_mapv.findViewById(R.id.mapview_search_icon);
		//setting_i.setOnClickListener(this);
		ecall_t.setOnClickListener(this);
		bcall_t.setOnClickListener(this);
		grp_i.setOnClickListener(this);
		geofence_t.setOnClickListener(this);
//		searchIcon.setOnClickListener(this);
//		searchEdit = (EditText) common_mapv.findViewById(R.id.mapview_search_edit);
//		searchEdit.setOnKeyListener(new OnKeyListener(){
//			@Override
//			public boolean onKey(View v, int keyCode, KeyEvent event) {
//				// TODO Auto-generated method stub
//				return false;
//			}
//			
//		});
		
		Date curDate = new Date( System.currentTimeMillis());
		SimpleDateFormat formatter1 = new SimpleDateFormat("yyyy/MM/dd", Locale.CHINA);		
		String dateStr = formatter1.format(curDate);
		date_t.setText(dateStr);
		SimpleDateFormat formatter2 = new SimpleDateFormat("HH:mm", Locale.CHINA);
		String timeStr = formatter2.format(curDate);		
		int hour = curDate.getHours();
		if(hour < 12)
			timeStr += " AM";
		else
			timeStr += " PM";
		time_t.setText(timeStr);
		
		if(baseAct.weatherInitOK && baseAct.myWeatherInfo.pm25Report != null){			
			WeatherReport report = baseAct.myWeatherInfo.listWeatherReport.get(0);
			setWeather(report.getWeather(), report.getTemperature(), baseAct.myWeatherInfo.pm25Report[0]);
		}
				
	}

	public void setWeather(String weather, String temp, String pm25){
	    int weatherIcon[][] = {{0, 1, 3, 13, 18}, //sun
				   {1, 2}, //cloud
				   {7, 8, 9, 10, 11, 12, 6}, //rain
				   {4, 5},  //thunder
				   {14, 15, 16, 17}, //snow
				   {20, 31}, //wind
				   {53}, //fog
				   {29}, //hail 
				   {30}};  //wave
	    int iconId = 0;
	    String iconStr;
	    int imgId;
	    boolean dayOrNight = false;
	    wea_r.setVisibility(View.VISIBLE);
		if(weather.contains(baseAct.getResources().getString(R.string.sun))){
			if(weather.contains(baseAct.getResources().getString(R.string.cloud))){
				iconId = weatherIcon[0][1];
			}
			else if(weather.contains(baseAct.getResources().getString(R.string.rain))){
				iconId = weatherIcon[0][2];
			}
			else if(weather.contains(baseAct.getResources().getString(R.string.snow))){
				iconId = weatherIcon[0][3];
			}
			else if(weather.contains(baseAct.getResources().getString(R.string.fog))){
				iconId = weatherIcon[0][4];
			}
			else{
				iconId = weatherIcon[0][0];
			}
				
		}
		else if(weather.contains(baseAct.getResources().getString(R.string.cloud))
			  ||weather.contains(baseAct.getResources().getString(R.string.cloud1))){
			if(weather.contains(baseAct.getResources().getString(R.string.sun))){
				iconId = weatherIcon[1][0];
			}
			else{
				iconId = weatherIcon[1][1];
			}			
		}
		else if(weather.contains(baseAct.getResources().getString(R.string.rain))){
			if(weather.contains(baseAct.getResources().getString(R.string.lit2mid))){
				iconId = weatherIcon[2][1];
			}
			else if(weather.contains(baseAct.getResources().getString(R.string.little))){
				iconId = weatherIcon[2][0];
			}
			else if(weather.contains(baseAct.getResources().getString(R.string.middle))){
				iconId = weatherIcon[2][2];
			}
			else if(weather.contains(baseAct.getResources().getString(R.string.big2storm))){
				iconId = weatherIcon[2][4];
			}
			else if(weather.contains(baseAct.getResources().getString(R.string.big))){
				iconId = weatherIcon[2][3];
			}			
			else if(weather.contains(baseAct.getResources().getString(R.string.storm))){
				iconId = weatherIcon[2][5];
			}
			else if(weather.contains(baseAct.getResources().getString(R.string.snow))){
				iconId = weatherIcon[2][6];
			}
			else{
				iconId = weatherIcon[2][1];
			}
		}
		else if(weather.contains(baseAct.getResources().getString(R.string.snow))){
			if(weather.contains(baseAct.getResources().getString(R.string.little))){
				iconId = weatherIcon[4][0];
			}
			else if(weather.contains(baseAct.getResources().getString(R.string.middle))){
				iconId = weatherIcon[4][1];
			}		
			else if(weather.contains(baseAct.getResources().getString(R.string.big))){
				iconId = weatherIcon[4][2];
			}			
			else if(weather.contains(baseAct.getResources().getString(R.string.storm))){
				iconId = weatherIcon[4][3];
			}
			else{
				iconId = weatherIcon[4][1];
			}
		}
		else if(weather.contains(baseAct.getResources().getString(R.string.fog))){
			iconId = weatherIcon[6][0];
		}
		else if(weather.contains(baseAct.getResources().getString(R.string.hail))){
			iconId = weatherIcon[7][0];
		}
		else if(weather.contains(baseAct.getResources().getString(R.string.wind))){
			if(weather.contains(baseAct.getResources().getString(R.string.storm))){
				iconId = weatherIcon[5][1];
			}
			else{
				iconId = weatherIcon[5][0];
			}
		}
		else if(weather.contains(baseAct.getResources().getString(R.string.thunder))){
			if(weather.contains(baseAct.getResources().getString(R.string.snow))){
				iconId = weatherIcon[3][1];
			}
			else{
				iconId = weatherIcon[3][0];
			}
		}
		iconStr = Integer.toString(iconId);
		if(iconStr.length() == 1)
			iconStr = "0" + iconStr;
		
		if(dayOrNight)
			iconStr = "w" + iconStr;
		else
			iconStr = "n" + iconStr;
		imgId = Util.getImage(iconStr);	
		wea_i.setImageResource(imgId);
		wea_temp_t.setText(temp + " ℃");
		wea_pm25_t.setText("PM2.5 " + pm25);
	}
	
	void setCarSpeed(String spd){
		if(speed_r.getVisibility() != View.VISIBLE)
			speed_r.setVisibility(View.VISIBLE);
		speed_t.setText(spd +"Km/h");
	}
	
	void setDtc(String code, String title){
		if(dtc_r.getVisibility() != View.VISIBLE)
			dtc_r.setVisibility(View.VISIBLE);
		dtc_t.setText(code +"\n" + title);
	}
	
	@Override
	public void onClick(View v) {
		String callnumber;
		Intent intent;
		// TODO Auto-generated method stub
		switch(v.getId()){
		case R.id.ecall_t:			
			callnumber = Preference.getInstance(baseAct.getApplicationContext()).getBcall();
	    	intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + callnumber));
	    	baseAct.startActivity(intent);			
			break;
			
		case R.id.bcall_t:
			callnumber = Preference.getInstance(baseAct.getApplicationContext()).getEcall();
	    	intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + callnumber));
	    	baseAct.startActivity(intent);				
			break;
			
		case R.id.gfence_t:
			Base.baidu_v.enterFenceAddMode();
			break;
			
//		case R.id.mapview_grp_icon:
//			enterGrpMapView();
//			break;
			
		case R.id.mapview_search_icon:
			break;		
			
		default:break;	
		}
		
	}	
	
	public void enterGrpMapView(){
		common_mapv.removeView(Base.baidu_v);
		View view = null;
//		if((view = Base.baidu_v.findViewById(R.id.mapview_grp_icon)) != null)
//			view.setVisibility(View.INVISIBLE);
		Base.baidu_v.search_rela.setVisibility(View.INVISIBLE);
		Base.landGroupM = new GroupMap(baseAct);
		Base.OBDApp.landScapeMode = 2;
		baseAct.setContentView(Base.landGroupM.group_mapv);
	}
}
