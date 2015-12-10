package com.ctg.land;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.sug.SuggestionSearchOption;
import com.baidu.navisdk.BNaviPoint;
import com.baidu.navisdk.BaiduNaviManager;
import com.baidu.navisdk.comapi.routeplan.RoutePlanParams;
import com.ctg.crash.LogRecord;
import com.ctg.group.Group;
import com.ctg.group.Member;
import com.ctg.net.HttpQueue;
import com.ctg.service.CarDataService;
import com.ctg.ui.BNavigatorActivity;
import com.ctg.ui.BaiduMapView;
import com.ctg.ui.Base;
import com.ctg.ui.R;
import com.ctg.util.GrpSearchDlg;
import com.ctg.util.MyBDLocation;
import com.ctg.util.MyPagerAdapter;
import com.ctg.util.MyPagerAdapter1;
import com.ctg.util.MyViewPager;
import com.ctg.util.NavitPoint;
import com.ctg.util.Preference;
import com.ctg.util.SearchPoiDlg;
import com.ctg.util.Util;
import com.ctg.weather.WeatherReport;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

public class Cascade implements View.OnClickListener, Runnable{
	final private String TAG = "Cascade";
	public final static int MSG_TIMER_MINUTE = 60000;
	Base baseAct;
//	public LinearLayout main_l;
	public LinearLayout cas_l;
	//BaiduMapView baidu_v;
	LinearLayout navit_linear;
	LinearLayout grp_linear;
	LinearLayout fence_linear;
	LinearLayout ecall_linear;
	TextView dt_date_t;
	TextView dt_time_t;
	TextView navit_t;
	TextView group_t;
	TextView geofence_t;
	TextView ecall_t;
	View func_rela;
	ImageView func_img;
	TextView carspd;
//	ImageView func_panel_img;
	
	public byte func_panel_stat; //0 common state; 1 func panel; 2 navit panel
	ImageView navit_i;
	ImageView group_i;
	ImageView fence_i;
	ImageView ecall_i;
	LinearLayout navit_panel;
	LinearLayout home_l;
	LinearLayout company_l;
	LinearLayout save_l;
	LinearLayout history_l;	
	TextView home_t;
	TextView company_t;
	TextView save_t;
	TextView history_t;
	EditText input_dest;
	public FrameLayout main_frm;
	FrameLayout sub_frm;
	NavitPoint nvPtH;
	NavitPoint nvPtC;
	MyViewPager myVp;
	List<View> listViews;
	LinearLayout navit_l;
	ImageView search_icon;
	EditText search_edit;
	LinearLayout group_l;
	EditText grp_search_e;
	ListView  group_lv;
	ListView  gfence_lv;
	View ecall_v;
	LinearLayout gf_add_linear;
	LinearLayout gfence_linear;
	RelativeLayout ecall_r;
	static public int curFocusIdx;
	FrndGrpAdapt listAdapt;
	FenceAdapt fenceAdapt;
	ArrayList<MyBDLocation> resrvBDLocLst;
	ArrayList<MyBDLocation> histBDLocLst;
	AlertDialog note_dialog;
	LinearLayout search_poi;
	LinearLayout poi_list;
	EditText search_text;
	Button search_btn;
	ListView poi_lv;
	SimpleAdapter poi_adpt;
	ArrayList<Map<String, Object>> poilistItem;
	public int curOperatStat = 0;
	public int friendOrGrpIdx = -1;
	Group curGrp;
	Member curMember;
	SearchPoiDlg searchDlg;
	Thread timeRunThd;
//	ListView poi_hist_list;
//	SimpleAdapter poi_hist_adpt;
//	ArrayList<Map<String, Object>> poiHistListItem;
	
	Handler timeHandler = new Handler(){
		public void handleMessage(Message msg) {
			if(msg.what == MSG_TIMER_MINUTE){
				setDateTime();
			}
		}
	};
	
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		while(true){
			
			try {
				timeHandler.obtainMessage(MSG_TIMER_MINUTE).sendToTarget();
				Thread.sleep(60000L);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				break;
			}
		}
	}
	
	public Cascade(Context context){
		baseAct = (Base) context;
		Base.baidu_v = new BaiduMapView(context);
		Base.baidu_v.search_rela.setOnClickListener(null);
		Base.baidu_v.search_icon.setOnClickListener(this);

		main_frm = (FrameLayout)View.inflate(context, R.layout.cascade_new, null);


		main_frm.addView(Base.baidu_v, 0);
		sub_frm = (FrameLayout) main_frm.findViewById(R.id.sub_cas_frm);
		cas_l = (LinearLayout) main_frm.findViewById(R.id.cas_linear);
		dt_date_t = (TextView) main_frm.findViewById(R.id.dt_ymd_t);
		dt_time_t = (TextView) main_frm.findViewById(R.id.dt_tm_t);
		navit_linear = (LinearLayout) main_frm.findViewById(R.id.navit);
		grp_linear = (LinearLayout) main_frm.findViewById(R.id.pos_share);
		fence_linear = (LinearLayout) main_frm.findViewById(R.id.geofence);
		ecall_linear = (LinearLayout) main_frm.findViewById(R.id.ecall);
		
		navit_i = (ImageView) main_frm.findViewById(R.id.navit_img);
		group_i = (ImageView) main_frm.findViewById(R.id.pos_share_img);
		fence_i = (ImageView) main_frm.findViewById(R.id.geofence_img);
		ecall_i = (ImageView) main_frm.findViewById(R.id.ecall_img);
		
		navit_t = (TextView) main_frm.findViewById(R.id.navit_t);
		group_t = (TextView) main_frm.findViewById(R.id.pos_share_t);
		geofence_t = (TextView) main_frm.findViewById(R.id.geofence_t);
		ecall_t = (TextView) main_frm.findViewById(R.id.ecall_t);
		func_img = (ImageView)main_frm.findViewById(R.id.func_img);
		func_rela = main_frm.findViewById(R.id.func);
		//navit_panel = (LinearLayout)main_frm.findViewById(R.id.navit);
		input_dest = (EditText)main_frm.findViewById(R.id.mapview_search_edit);
		
		myVp = (MyViewPager) main_frm.findViewById(R.id.vpager);
		
		navit_l = (LinearLayout) View.inflate(context, R.layout.sub_navit, null);
		search_icon = (ImageView) navit_l.findViewById(R.id.cas_search_img);
		View rela_search_icon = navit_l.findViewById(R.id.cas_search_img);
		rela_search_icon.setOnClickListener(this);
		search_edit = (EditText) navit_l.findViewById(R.id.cas_search_edit);
		home_t = (TextView)navit_l.findViewById(R.id.go_home_t);
		company_t = (TextView)navit_l.findViewById(R.id.go_company_t);
		save_t = (TextView)navit_l.findViewById(R.id.save_addr_t);
		history_t = (TextView)navit_l.findViewById(R.id.hist_dest_t);
		home_l = (LinearLayout)navit_l.findViewById(R.id.go_home);
		company_l = (LinearLayout)navit_l.findViewById(R.id.go_company);
		save_l = (LinearLayout)navit_l.findViewById(R.id.save_addr);
		history_l = (LinearLayout)navit_l.findViewById(R.id.hist_dest);
		group_l = (LinearLayout) View.inflate(context, R.layout.sub_pos_share, null);
		grp_search_e = (EditText) group_l.findViewById(R.id.sub_group_search_e);
		grp_search_e.setOnEditorActionListener(new TextView.OnEditorActionListener(){

			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				// TODO Auto-generated method stub
//				if(event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER){
//					listAdapt.setFrndGrpLst(v.getText().toString());
//				}
				listAdapt.setFrndGrpLst(v.getText().toString());
				return false;
		}});	
		group_lv = (ListView) group_l.findViewById(R.id.grp_lv);	
		group_lv.setDivider(null);		
		gfence_linear = (LinearLayout) View.inflate(context, R.layout.sub_geofence, null);
		gfence_lv = (ListView) gfence_linear.findViewById(R.id.geofence_lv);
		group_lv.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub	
				//Group grp = null;
				friendOrGrpIdx = arg2;
				Base.friendOrGrpIdx = arg2;
				if(friendOrGrpIdx < listAdapt.frndLen){
					curMember = listAdapt.frndLst.get(friendOrGrpIdx);
					LogRecord.SaveLogInfo2File(Base.OperateInfo, TAG+"friend item click");
				}
				else{
					curGrp = listAdapt.grpLst.get(friendOrGrpIdx-listAdapt.frndLen);
					LogRecord.SaveLogInfo2File(Base.OperateInfo, TAG+"group item click");
				}
				
				Base.baidu_v.enterGpsShareMode();	

				closeSubFrame();
			}
			
		});		
		gfence_lv.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				Base.baidu_v.enterFenceModeL(arg2);
				closeSubFrame();
			}
		
		});
		
		gf_add_linear = (LinearLayout)gfence_linear.findViewById(R.id.geofence_add_linear);
//		ecall_r = (RelativeLayout) View.inflate(context, R.layout.sub_ecall, null);
		listViews = new ArrayList<View>();
		ecall_v = new View(context);
		
		listViews.add(navit_l);
		listViews.add(group_l);
		listViews.add(gfence_linear); 
		listViews.add(ecall_v); 
		myVp.setAdapter(new MyPagerAdapter1(listViews));
	

		
		if(HttpQueue.friendLst != null || HttpQueue.grpResLst != null){
			listAdapt = new FrndGrpAdapt(context, HttpQueue.friendLst, HttpQueue.grpResLst);
			group_lv.setAdapter(listAdapt);
			group_lv.setDivider(null);
		}
		if(CarDataService.fenceList != null){
			fenceAdapt = new FenceAdapt(context, CarDataService.fenceList);
			gfence_lv.setAdapter(fenceAdapt);
			gfence_lv.setDivider(null);
		}
		curFocusIdx = 0;
		myVp.setCurrentItem(curFocusIdx, false);
		func_panel_stat = 0;
		func_img.setOnClickListener(this);
		navit_linear.setOnClickListener(this);
		grp_linear.setOnClickListener(this);
		fence_linear.setOnClickListener(this);
		ecall_linear.setOnClickListener(this);
		
		home_l.setOnClickListener(this);
		company_l.setOnClickListener(this);
		save_l.setOnClickListener(this);
		history_l.setOnClickListener(this);
		gf_add_linear.setOnClickListener(this);
		carspd = (TextView)main_frm.findViewById(R.id.spd_num);

		setDateTime();
		
		nvPtH = Preference.getInstance(context).getNaviPointHome();
		nvPtC = Preference.getInstance(context).getNaviPointCmpy();

		
		input_dest.setOnEditorActionListener(new OnEditorActionListener(){

			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				// TODO Auto-generated method stub
				return false;
			}
			
		});
		initSaveFileData();
		search_poi = (LinearLayout) View.inflate(context, R.layout.search_poi, null);
		search_text = (EditText) search_poi.findViewById(R.id.search_s_text);
		search_text.addTextChangedListener(search_watcher);
		search_btn = (Button) search_poi.findViewById(R.id.search_s_btn);
		search_btn.setOnClickListener(this);
		poi_list  = (LinearLayout) View.inflate(context, R.layout.poi_list, null);
		
		setImageFocus(0);
		timeRunThd = new Thread(this);
		timeRunThd.start();
	}
	
	void setDateTime(){
		Calendar cal = Calendar.getInstance();
		
		int y = cal.get(Calendar.YEAR);    
		int m = cal.get(Calendar.MONTH);    
		int d = cal.get(Calendar.DATE);
		int hour = cal.get(Calendar.HOUR);
		int min = cal.get(Calendar.MINUTE);
		int ampm = cal.get(Calendar.AM_PM);
		//int sec = cal.get(Calendar.SECOND);
		dt_date_t.setText(String.valueOf(m)+"-"+String.valueOf(d));
		
		//String timeStr = String.valueOf(hour)+":"+String.valueOf(min);
		String timeStr = String.format("%d:%02d", hour, min);
		if(ampm == Calendar.AM)
			timeStr += " AM";
		else
			timeStr += " PM";
		dt_time_t.setText(timeStr);
	}
	
	public void setSpeed(String v){
		int idx = v.indexOf("km");
		
		if(idx != -1)
			v = v.substring(0, idx);
			
		carspd.setText(v);
	}
	
	public void onDestroy(){
		if(timeRunThd != null)
			timeRunThd.interrupt();
		timeRunThd = null;
		Base.navitMain = null;
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()){
		case R.id.navit:
			func_panel_stat = 2;			
			myVp.setCurrentItem(0,false);
			setImageFocus(0);
			curOperatStat = 0;
			break;
		case R.id.pos_share:
			myVp.setCurrentItem(1,false);
			setImageFocus(1);
			curOperatStat = 1;
			break;
		case R.id.geofence:
			myVp.setCurrentItem(2,false);
			setImageFocus(2);
			curOperatStat = 2;
			break;
		case R.id.ecall:
			myVp.setCurrentItem(3,false);
			setImageFocus(3);
			String callnumber = Preference.getInstance(baseAct.getApplicationContext()).getEcall();
			if(!callnumber.equals("10086")){
		    	Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + callnumber));
		    	baseAct.startActivity(intent);	
			}
			else{
				Toast.makeText(Base.OBDApp, "没有设置紧急号码", Toast.LENGTH_LONG).show();
			}
			cas_l.setVisibility(View.INVISIBLE);
			func_img.setImageResource(R.drawable.icon_open);
			func_panel_stat = 0;
	    	curOperatStat = 3;
			break;
		case R.id.cas_search_img:	
			String textStr0 = search_edit.getText().toString();
			if(textStr0 != null && !textStr0.equals("")){
				Base.baidu_v.mPoiSearch.searchInCity((new PoiCitySearchOption())  
					    .city(Base.baidu_v.mCity)  
					    .keyword(textStr0) 
					    .pageNum(0));
                Base.baidu_v.isSelectAddrMode = 0;
			}			
			break;
		case R.id.func_img:
			if(cas_l.getVisibility() == View.INVISIBLE){
				cas_l.setVisibility(View.VISIBLE);
				func_panel_stat = 1;
				func_img.setImageResource(R.drawable.icon_close);
			}
			else{
				grp_search_e.setText("");
				cas_l.setVisibility(View.INVISIBLE);
				func_img.setImageResource(R.drawable.icon_open);
				func_panel_stat = 0;				
			}
			if(sub_frm.getChildCount() > 1)
				sub_frm.removeViews(1, sub_frm.getChildCount()-1);
			break;
		case R.id.go_home:
			curOperatStat = 0;
			nvPtH = Preference.getInstance(baseAct).getNaviPointHome();			
			if(!nvPtH.addr.equals("") && Math.abs(Base.baidu_v.mCurLongitude) > 0.1){
		        BNaviPoint startPoint = new BNaviPoint(Base.baidu_v.mCurLongitude,Base.baidu_v.mCurLatitude,
		        		Base.baidu_v.curPoiName, BNaviPoint.CoordinateType.BD09_MC);//WGS84
		        BNaviPoint endPoint = new BNaviPoint(nvPtH.lon,nvPtH.lat,
		        		nvPtH.addr, BNaviPoint.CoordinateType.BD09_MC);
		        
		        startNavit(baseAct, startPoint, endPoint);
		        curOperatStat = 0;
		        closeSubFrame();
			}
			else{
				createAlertDlg(0);	
			}
			break;
		case R.id.go_company:
			curOperatStat = 0x100;
			nvPtC = Preference.getInstance(baseAct).getNaviPointCmpy();
			if(!nvPtC.addr.equals("")&& Math.abs(Base.baidu_v.mCurLongitude) > 0.1){
		        BNaviPoint startPoint = new BNaviPoint(Base.baidu_v.mCurLongitude,Base.baidu_v.mCurLatitude,
		        		Base.baidu_v.curPoiName, BNaviPoint.CoordinateType.BD09_MC);//WGS84
		        BNaviPoint endPoint = new BNaviPoint(nvPtC.lon,nvPtC.lat,
		        		nvPtC.addr, BNaviPoint.CoordinateType.BD09_MC);
		        
		        startNavit(baseAct, startPoint, endPoint);
		        curOperatStat = 0;
		        closeSubFrame();
			}
			else{
				createAlertDlg(1);
			}
			break;
		case R.id.save_addr:
		case R.id.hist_dest:
//			poi_resrv = (ListView) View.inflate(baseAct, R.layout.poi_list_no_title, null);
//			poi_adpt = new SimpleAdapter(baseAct, null, R.layout.poi_item, 
//					new String[] {"image", "title", "addr"},
//					new int[] {R.id.poi_num,R.id.poi_title, R.id.poi_addr});
//			poi_resrv.setAdapter(poi_adpt);
			
			int len = 0;
			int i = 0;
			MyBDLocation bdloc = null;
			ArrayList<MyBDLocation> list = null;
			if(v.getId() == R.id.save_addr){
				curOperatStat = 0x200;
				list = resrvBDLocLst;
			}
				
			else{
				curOperatStat = 0x400;
				list = histBDLocLst;
			}
			if(list != null && (len = list.size()) != 0){
				poilistItem = new ArrayList<Map<String, Object>>();
				Map<String, Object> map = null;
				
				for(i = 0; i < len; i++){
					bdloc = list.get(i);
					map = new HashMap<String, Object>();
//					map.put("image", R.drawable.search_poi_01+i);
					map.put("title", bdloc.name);
					map.put("addr", bdloc.address);
					poilistItem.add(map);
				}
				poi_lv = (ListView) View.inflate(baseAct, R.layout.poi_list_resv, null);
				poi_adpt = new SimpleAdapter(baseAct, null, R.layout.poi_item, 
						new String[] {"title", "addr"},
						new int[] {R.id.poi_title, R.id.poi_addr});
				poi_lv.setAdapter(poi_adpt);	
				sub_frm.addView(poi_lv);
				poi_lv.setOnItemClickListener(new OnItemClickListener(){

					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1,
							int arg2, long arg3) {
						// TODO Auto-generated method stub
						ArrayList<MyBDLocation> list = null;
						if(curOperatStat == 0x200){
							list = resrvBDLocLst;
						}
						else{
							list = histBDLocLst;
						}
						MyBDLocation bdloc = list.get(arg2);
				        BNaviPoint startPoint = new BNaviPoint(Base.baidu_v.mCurLongitude,Base.baidu_v.mCurLatitude,
				        		Base.baidu_v.curPoiName, BNaviPoint.CoordinateType.BD09_MC);//WGS84
				        BNaviPoint endPoint = new BNaviPoint(bdloc.lon, bdloc.lat,
				        		bdloc.address,BNaviPoint.CoordinateType.BD09_MC);
				        
				        startNavit(baseAct, startPoint, endPoint);
				        curOperatStat = 0;
					}

				});
			}
			break;	
		case R.id.search_s_btn:
			if(Base.CheckNetwork(baseAct)){
				String searchText = search_text.getText().toString();
				if(searchText != null && !searchText.equals(""))
					Base.baidu_v.mPoiSearch.searchInCity((new PoiCitySearchOption())  
						    .city(Base.baidu_v.mCity)  
						    .keyword(searchText)  
						    .pageNum(0));
				Base.baidu_v.poiName = searchText;
				return;
			}
			else{
				Toast.makeText(baseAct, "没有网络连接，无法搜索地址", Toast.LENGTH_SHORT).show();
			}
			break;
			
		case R.id.geofence_add_linear:
			curOperatStat = 0x201;
			sub_frm.addView(search_poi, Base.mWidth*16/25, Base.mHeight);
			break;
		case R.id.mapview_search_icon:
			String textStr = Base.baidu_v.search_edit.getText().toString();
			if(textStr != null && !textStr.equals("")){
				Base.baidu_v.mPoiSearch.searchInCity((new PoiCitySearchOption())  
					    .city(Base.baidu_v.mCity)  
					    .keyword(textStr) 
					    .pageNum(0));
                Base.baidu_v.isSelectAddrMode = 4;
			}
			break;
		default:
			break;
		}
	}

	void openSubFrame(){
		cas_l.setVisibility(View.VISIBLE);
		func_img.setImageResource(R.drawable.icon_close);
		func_panel_stat = 1;				

	}
	
	void closeSubFrame(){
		cas_l.setVisibility(View.INVISIBLE);
		func_img.setImageResource(R.drawable.icon_open);
		func_panel_stat = 0;				
		
		if(sub_frm.getChildCount() > 1)
			sub_frm.removeViews(1, sub_frm.getChildCount()-1);
	}
	
	void createAlertDlg(final int type){
		String msg = "";
		if(type == 0){
			msg = "没有设置家地址，是否去设置？";
		}
		else{
			msg = "没有设置公司地址，是否去设置？";
		}
		note_dialog = new AlertDialog.Builder(baseAct).create();
		note_dialog.setMessage(msg);
		note_dialog.setButton(DialogInterface.BUTTON_NEGATIVE, baseAct.getResources().getString(R.string.str_return),
			new DialogInterface.OnClickListener(){

				@Override
				public void onClick(DialogInterface dialog,
						int which) {
					// TODO Auto-generated method stub
					note_dialog.cancel();
				}
	
		});
		note_dialog.setButton(DialogInterface.BUTTON_POSITIVE, baseAct.getResources().getString(R.string.string_confirm),
			new DialogInterface.OnClickListener(){

				@Override
				public void onClick(DialogInterface dialog,
						int which) {
					// TODO Auto-generated method stub
					if(type == 0)
						Base.baidu_v.isSelectAddrMode = 1;
					else
						Base.baidu_v.isSelectAddrMode = 2;
		        	Base.baidu_v.searchDlg = new SearchPoiDlg(baseAct, Base.mWidth*2/3,
		                    Base.mHeight, R.layout.search_poi,
		                    R.style.Theme_dialog);
		        	Base.baidu_v.searchDlg.show();
					note_dialog.cancel();							
				}
	
		});
		note_dialog.show();	
			
	}
	public static void startNavit(Context context, BNaviPoint st, BNaviPoint ed){
        BaiduNaviManager.getInstance().launchNavigator((Activity)context,
                st,
                ed,
                RoutePlanParams.NE_RoutePlan_Mode.ROUTE_PLAN_MOD_MIN_TIME, 		 //算路方式
                true, 									   		 //真实导航
                BaiduNaviManager.STRATEGY_FORCE_ONLINE_PRIORITY, //在离线策略
                new BaiduNaviManager.OnStartNavigationListener() {				 //跳转监听

                    @Override
                    public void onJumpToNavigator(Bundle configParams) {
                        Intent intent = new Intent(Base.OBDApp.baseAct, BNavigatorActivity.class);
                       
                        intent.putExtras(configParams);
                        Base.OBDApp.baseAct.startActivity(intent);
                    }

                    @Override
                    public void onJumpToDownloader() {
                    }
                });	
	}

	
	
	void setImageFocus(int focusId){
		navit_i.setImageResource(R.drawable.icon_navigation);
		group_i.setImageResource(R.drawable.icon_position);
		fence_i.setImageResource(R.drawable.icon_geofence);
		ecall_i.setImageResource(R.drawable.icon_ecall);
		
		
		switch(focusId){
		case 0:
			navit_i.setImageResource(R.drawable.icon_navigation_pressed);			
			navit_linear.setBackgroundDrawable(null);
			grp_linear.setBackgroundResource(R.drawable.dot_one);
			fence_linear.setBackgroundResource(R.drawable.dot_one);
			ecall_linear.setBackgroundResource(R.drawable.dot_one);
			break;
		case 1:
			group_i.setImageResource(R.drawable.icon_position_pressed);
			navit_linear.setBackgroundResource(R.drawable.dot_one);			
			grp_linear.setBackgroundDrawable(null);
			fence_linear.setBackgroundResource(R.drawable.dot_one);
			ecall_linear.setBackgroundResource(R.drawable.dot_one);
			break;
		case 2:
			fence_i.setImageResource(R.drawable.icon_geo_fence_pressed);
			navit_linear.setBackgroundResource(R.drawable.dot_one);
			grp_linear.setBackgroundResource(R.drawable.dot_one);			
			fence_linear.setBackgroundDrawable(null);
			ecall_linear.setBackgroundResource(R.drawable.dot_one);
			break;
		case 3:		
			ecall_i.setImageResource(R.drawable.icon_ecall_pressed);
			navit_linear.setBackgroundResource(R.drawable.dot_one);
			grp_linear.setBackgroundResource(R.drawable.dot_one);
			fence_linear.setBackgroundResource(R.drawable.dot_one);			
			ecall_linear.setBackgroundDrawable(null);
			break;
			default:break;
		}
	}
	
	private TextWatcher search_watcher = new TextWatcher() {
	    
	    @Override
	    public void onTextChanged(CharSequence s, int start, int before, int count) {
	        // TODO Auto-generated method stub
	    	String searchText = search_text.getText().toString();
			if(searchText != null && !searchText.equals("") && Base.CheckNetwork(baseAct)){
				Base.baidu_v.mSuggestionSearch.requestSuggestion((new SuggestionSearchOption())  
					    .keyword(searchText)  
					    .city(Base.baidu_v.mCity));
			}
	    }
	    
	    @Override
	    public void beforeTextChanged(CharSequence s, int start, int count,
	            int after) {
	        // TODO Auto-generated method stub
	        
	    }

		@Override
		public void afterTextChanged(Editable s) {
			// TODO Auto-generated method stub
			
		}

	};
	
	public boolean onSystemBack(){
		if(grp_search_e != null)
			grp_search_e.setText("");
		if(listAdapt != null)
			listAdapt.setFrndGrpLst("");
		switch (curOperatStat){
		//navit
		case 0:
			if(cas_l.getVisibility() == View.VISIBLE){
				cas_l.setVisibility(View.INVISIBLE);
				func_img.setImageResource(R.drawable.icon_open);
				curOperatStat = 0;
				return true;
			}
			break;
		case 1:
		case 2:
		case 4:
			curOperatStat = 0;
			func_panel_stat = 0;	
			if(sub_frm.getChildCount() > 1)
				sub_frm.removeViews(1, sub_frm.getChildCount()-1);
			cas_l.setVisibility(View.INVISIBLE);
			func_img.setImageResource(R.drawable.icon_open);
			return true;
		//pos share
		case 0x100:
			curOperatStat = 0;
			func_panel_stat = 0;
			if(cas_l.getVisibility() == View.VISIBLE){
				cas_l.setVisibility(View.INVISIBLE);
				func_img.setImageResource(R.drawable.icon_open);
				curOperatStat = 0;
				return true;
			}
			break;
			//geofence
		case 0x200:
			if(cas_l.getVisibility() == View.VISIBLE){
				cas_l.setVisibility(View.INVISIBLE);
				func_img.setImageResource(R.drawable.icon_open);
				curOperatStat = 0;
				return true;
			}
			
			break;
		case 0x201://
			if(sub_frm.getChildCount() > 1)
				sub_frm.removeViews(1, sub_frm.getChildCount()-1);
			curOperatStat = 200;
			return true;		
			//ecall
//		case 0x400:
//			if(cas_l.getVisibility() == View.VISIBLE){
//				cas_l.setVisibility(View.INVISIBLE);
//				return true;
//			}
//			if(sub_frm.getChildCount() > 1)
//				sub_frm.removeViews(1, sub_frm.getChildCount()-1);
//			curOperatStat = 0;
//		break;
		
		default:break;
		}
		return false;
	}
	
	void initSaveFileData(){
		String obdii_path = Base.getSDPath() +"/OBDII";
		String reserve_path = obdii_path + "/reserve";
		String history_path = obdii_path + "/history";
		try {
			FileInputStream resrv_f_in = new FileInputStream(reserve_path);
			MyBDLocation loc = null;
			if(resrv_f_in != null){
				ObjectInputStream obj_in = new ObjectInputStream(resrv_f_in);
				resrvBDLocLst = new ArrayList<MyBDLocation>();								
				while((loc = (MyBDLocation) obj_in.readObject()) != null)
					resrvBDLocLst.add(loc);		
				resrv_f_in.close();
			}
			
			
			FileInputStream hist_f_in = new FileInputStream(history_path);			
			if(hist_f_in != null){
				ObjectInputStream obj_in = new ObjectInputStream(hist_f_in);
				histBDLocLst = new ArrayList<MyBDLocation>();				
				while((loc = (MyBDLocation) obj_in.readObject()) != null)
					histBDLocLst.add(loc);	
				hist_f_in.close();
			}
			
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	void saveFileData(){
		String obdii_path = Base.getSDPath() +"/OBDII";
		String reserve_path = obdii_path + "/reserve";
		String history_path = obdii_path + "/history";
		MyBDLocation loc = null;
		int i = 0;
		try {
			if(resrvBDLocLst != null){				
				FileOutputStream resrv_f_out = new FileOutputStream(reserve_path);

				if(resrv_f_out != null){
					ObjectOutputStream obj_out = new ObjectOutputStream(resrv_f_out);															
					while((loc = resrvBDLocLst.get(i)) != null)
						obj_out.writeObject(loc);
					resrv_f_out.close();
				}
			}
			FileOutputStream hist_f_out = new FileOutputStream(history_path);			
			if(hist_f_out != null){
				ObjectOutputStream obj_out = new ObjectOutputStream(hist_f_out);
				i = 0;
				while((loc = histBDLocLst.get(i)) != null)
					obj_out.writeObject(loc);
				hist_f_out.close();
			}
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
}