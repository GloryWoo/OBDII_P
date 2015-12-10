package com.ctg.plat.navit;

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
import com.ctg.land.FrndGrpAdapt;
import com.ctg.net.HttpQueue;
import com.ctg.service.CarDataService;
import com.ctg.trafficViolation.OnBackKeyInterface;
import com.ctg.trafficViolation.TrafficVioView;
import com.ctg.ui.BNavigatorActivity;
import com.ctg.ui.BaiduMapView;
import com.ctg.ui.Base;
import com.ctg.ui.R;
import com.ctg.ui.Setting;
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
import com.example.swipelistview.SwipeAdapter;

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
import android.view.inputmethod.EditorInfo;
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

public class NavitMain implements View.OnClickListener{
	final private String TAG = "Cascade";
	public final static int MSG_TIMER_MINUTE = 60000;
	Base baseAct;
//	public LinearLayout main_l;
	public LinearLayout cas_l;
	//BaiduMapView baidu_v;
	LinearLayout navit_linear;
	LinearLayout grp_linear;
	LinearLayout dbhvr_linear;
	LinearLayout violet_linear;
//	LinearLayout fence_linear;
//	LinearLayout ecall_linear;
//	TextView dt_date_t;
//	TextView dt_time_t;
	TextView navit_t;
	TextView group_t;
//	TextView geofence_t;
//	TextView ecall_t;
//	View func_rela;
//	ImageView func_img;
//	TextView carspd;
//	ImageView func_panel_img;
	
	public byte func_panel_stat; //0 common state; 1 func panel; 2 navit panel
	ImageView navit_i;
	ImageView group_i;
	ImageView dbhv_i;
	ImageView violet_i;
	ImageView set_i;
//	ImageView fence_i;
//	ImageView ecall_i;
	LinearLayout navit_panel;
	ImageView home_l;
	ImageView company_l;
	LinearLayout save_l;
	LinearLayout history_l;	
	LinearLayout set_l;
	TextView home_t;
	TextView company_t;
	TextView save_t;
	TextView history_t;
	EditText input_dest;
	public LinearLayout main_frm;
	public FrameLayout map_frm;
	FrameLayout sub_frm;
	NavitPoint nvPtH;
	NavitPoint nvPtC;
	MyViewPager myVp;
	List<View> listViews;
//	LinearLayout navit_l;
	LinearLayout group_l;	
	DrvBehave drvbhv;
	TrafficVioView violat_l;
	ImageView search_icon;
	EditText search_edit;
	
	EditText grp_search_e;
	public ListView  group_lv;
//	ListView  gfence_lv;
//	View ecall_v;
//	LinearLayout gf_add_linear;
//	LinearLayout gfence_linear;
	RelativeLayout ecall_r;
	static public int curFocusIdx;
	public FrndGrpAdapt listAdapt;
	//FenceAdapt fenceAdapt;
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
//	public int friendOrGrpIdx = -1;
	Group curGrp;
	Member curMember;
	SearchPoiDlg searchDlg;
	Thread timeRunThd;


	
	public NavitMain(Context context){
		baseAct = (Base) context;
		Base.baidu_v = new BaiduMapView(context);
		Base.baidu_v.top_bar_l.setOnClickListener(null);
		Base.baidu_v.search_icon.setOnClickListener(this);

		main_frm = (LinearLayout)View.inflate(context, R.layout.navit_whole, null);
		map_frm = (FrameLayout)main_frm.findViewById(R.id.map_frame);

		map_frm.addView(Base.baidu_v, 0);
		sub_frm = (FrameLayout) main_frm.findViewById(R.id.sub_cas_frm);
		cas_l = (LinearLayout) main_frm.findViewById(R.id.cas_linear);

		navit_linear = (LinearLayout) main_frm.findViewById(R.id.navit_sub);
		grp_linear = (LinearLayout) main_frm.findViewById(R.id.share_pos);
		dbhvr_linear = (LinearLayout) main_frm.findViewById(R.id.dbhvr_l);
		violet_linear = (LinearLayout) main_frm.findViewById(R.id.violet_l);
		set_l = (LinearLayout) main_frm.findViewById(R.id.set_l);
		navit_i = (ImageView) main_frm.findViewById(R.id.navit_sub_img);
		group_i = (ImageView) main_frm.findViewById(R.id.share_pos_img);
		dbhv_i = (ImageView) main_frm.findViewById(R.id.dbhvr_img);
		violet_i = (ImageView) main_frm.findViewById(R.id.violet_img);
		set_i = (ImageView) main_frm.findViewById(R.id.set_i);
		navit_t = (TextView) main_frm.findViewById(R.id.navit_t);
		group_t = (TextView) main_frm.findViewById(R.id.pos_share_t);

//		func_img = (ImageView)main_frm.findViewById(R.id.func_img);
//		func_rela = main_frm.findViewById(R.id.func);
		//navit_panel = (LinearLayout)main_frm.findViewById(R.id.navit);
		input_dest = (EditText)main_frm.findViewById(R.id.mapview_search_edit);
		
		myVp = (MyViewPager) main_frm.findViewById(R.id.vpager);
		
//		navit_l = (LinearLayout) View.inflate(context, R.layout.sub_navit, null);
//		search_icon = (ImageView) navit_l.findViewById(R.id.cas_search_img);
//		View rela_search_icon = navit_l.findViewById(R.id.cas_search_img);
//		rela_search_icon.setOnClickListener(this);
//		search_edit = (EditText) navit_l.findViewById(R.id.cas_search_edit);
//		home_t = (TextView)navit_l.findViewById(R.id.go_home_t);
//		company_t = (TextView)navit_l.findViewById(R.id.go_company_t);
//		save_t = (TextView)navit_l.findViewById(R.id.save_addr_t);
//		history_t = (TextView)navit_l.findViewById(R.id.hist_dest_t);
		home_l = (ImageView)Base.baidu_v.findViewById(R.id.go_home_i);
		company_l = (ImageView)Base.baidu_v.findViewById(R.id.go_company_i);
//		save_l = (LinearLayout)navit_l.findViewById(R.id.save_addr);
//		history_l = (LinearLayout)navit_l.findViewById(R.id.hist_dest);
		
		group_l = (LinearLayout) View.inflate(context, R.layout.sub_pos_share, null);
		grp_search_e = (EditText) group_l.findViewById(R.id.sub_group_search_e);
		grp_search_e.setOnEditorActionListener(new TextView.OnEditorActionListener(){

			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				// TODO Auto-generated method stub

				return false;
		}});	
		group_lv = (ListView) group_l.findViewById(R.id.grp_lv);	
		group_lv.setDivider(null);		

		group_lv.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub	
				//Group grp = null;
				Base.friendOrGrpIdx = arg2;
//				Base.me_v.friendOrGrpIdx = arg2;
				if(Base.friendOrGrpIdx < listAdapt.frndLen){
					curMember = listAdapt.frndLst.get(Base.friendOrGrpIdx);
					LogRecord.SaveLogInfo2File(Base.OperateInfo, TAG+"friend item click");
				}
				else{
					curGrp = listAdapt.grpLst.get(Base.friendOrGrpIdx-listAdapt.frndLen);
					LogRecord.SaveLogInfo2File(Base.OperateInfo, TAG+"group item click");
				}				
				Base.baidu_v.enterGpsShareMode();	
				exitOpt();
			}
			
		});		
		
		drvbhv = new DrvBehave(context);
		violat_l = new TrafficVioView(context);
		violat_l.setOnBackKey(new OnBackKeyInterface() {
			
			@Override
			public boolean onBackKey() {
				// TODO Auto-generated method stub
				onSystemBack();
				return false;
			}
		});
		listViews = new ArrayList<View>();
//		listViews.add(navit_l);
		listViews.add(group_l);
		listViews.add(drvbhv.frame);
		listViews.add(violat_l.frame_lay);
		if(baseAct.setting_s == null)
			baseAct.setting_s = new Setting(baseAct);
		listViews.add(baseAct.setting_s.scrollView);
		myVp.setAdapter(new MyPagerAdapter1(listViews));
			
		if(HttpQueue.friendLst != null || HttpQueue.grpResLst != null){
			listAdapt = new FrndGrpAdapt(context, HttpQueue.friendLst, HttpQueue.grpResLst);
			group_lv.setAdapter(listAdapt);
			
		}
		group_lv.setDivider(null);
		curFocusIdx = 0;
		myVp.setCurrentItem(curFocusIdx, false);

		navit_linear.setOnClickListener(this);
		grp_linear.setOnClickListener(this);
		dbhvr_linear.setOnClickListener(this);
		violet_linear.setOnClickListener(this);
		set_l.setOnClickListener(this);
		home_l.setOnClickListener(this);
		company_l.setOnClickListener(this);
//		save_l.setOnClickListener(this);
//		history_l.setOnClickListener(this);

		
		nvPtH = Preference.getInstance(context).getNaviPointHome();
		nvPtC = Preference.getInstance(context).getNaviPointCmpy();

		
		input_dest.setOnEditorActionListener(new OnEditorActionListener(){

			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				// TODO Auto-generated method stub
				if(actionId==EditorInfo.IME_ACTION_DONE || event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER){
					Base.baidu_v.mPoiSearch.searchInCity((new PoiCitySearchOption())  
						    .city(Base.baidu_v.mCity)  
						    .keyword(v.getText().toString()) 
						    .pageNum(0));
	                Base.baidu_v.isSelectAddrMode = 4;
					listAdapt.setFrndGrpLst(v.getText().toString());
				}
				return true;
			}
			
		});
//		search_edit.setOnEditorActionListener(new OnEditorActionListener(){
//
//			@Override
//			public boolean onEditorAction(TextView v, int actionId,
//					KeyEvent event) {
//				// TODO Auto-generated method stub
//				if(actionId==EditorInfo.IME_ACTION_DONE || event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER){
//					Base.baidu_v.mPoiSearch.searchInCity((new PoiCitySearchOption())  
//						    .city(Base.baidu_v.mCity)  
//						    .keyword(v.getText().toString()) 
//						    .pageNum(0));
//	                Base.baidu_v.isSelectAddrMode = 0;		                
//					return true;
//				}
//				return false;
//			}
//			
//		});
		initSaveFileData();
		search_poi = (LinearLayout) View.inflate(context, R.layout.search_poi, null);
		search_text = (EditText) search_poi.findViewById(R.id.search_s_text);
		search_text.addTextChangedListener(search_watcher);
		search_btn = (Button) search_poi.findViewById(R.id.search_s_btn);
		search_btn.setOnClickListener(this);
		poi_list  = (LinearLayout) View.inflate(context, R.layout.poi_list, null);
		
		//setImageFocus(-1);

	}
	

	
	public void setFrndGrpList(){
		if(listAdapt == null){
			listAdapt = new FrndGrpAdapt(baseAct, HttpQueue.friendLst, HttpQueue.grpResLst);
			group_lv.setAdapter(listAdapt);
		}
		listAdapt.setFrndGrpLst(null);
	}
	
	public void onDestroy(){
		if(timeRunThd != null)
			timeRunThd.interrupt();
		timeRunThd = null;
		Base.navitMain = null;
		violat_l.onDestroy();
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub

		switch(v.getId()){
		case R.id.navit_sub:
//			if(myVp.getVisibility() == View.INVISIBLE){
//				
//			}	
			myVp.setVisibility(View.INVISIBLE);
			if(Base.baidu_v.isGrpShareMode)
				Base.baidu_v.exitGpsShareMode();
			func_panel_stat = 2;		
			Base.baidu_v.top_bar_l.setVisibility(View.VISIBLE);
//			myVp.setCurrentItem(0,false);
			setImageFocus(0);
			curOperatStat = 0;
			break;
		case R.id.share_pos:
			if(myVp.getVisibility() == View.INVISIBLE){
				myVp.setVisibility(View.VISIBLE);
			}
			myVp.setCurrentItem(0,false);
			setImageFocus(1);
			curOperatStat = 1;
			break;
		case R.id.dbhvr_l:
			if(myVp.getVisibility() == View.INVISIBLE){
				myVp.setVisibility(View.VISIBLE);
			}			
			if(Base.baidu_v.isGrpShareMode)
				Base.baidu_v.exitGpsShareMode();
			Base.baidu_v.top_bar_l.setVisibility(View.INVISIBLE);
			myVp.setCurrentItem(1,false);
			setImageFocus(2);
			curOperatStat = 2;
			Base.inDrvBhvStat = true;
			drvbhv.drawLastRoutine();
			if(Base.baidu_v.myHeadMarker != null)
				Base.baidu_v.myHeadMarker.remove();
			break;
		case R.id.violet_l:
			if(myVp.getVisibility() == View.INVISIBLE){
				myVp.setVisibility(View.VISIBLE);
			}
			myVp.setCurrentItem(2,false);
			curOperatStat = 3;
			setImageFocus(3);
			break;
		case R.id.set_l:
			if(myVp.getVisibility() == View.INVISIBLE){
				myVp.setVisibility(View.VISIBLE);
			}
			myVp.setCurrentItem(3,false);
			curOperatStat = 4;
			setImageFocus(4);

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
			
		case R.id.go_home_i:
			curOperatStat = 0;
			nvPtH = Preference.getInstance(baseAct).getNaviPointHome();			
			if(!nvPtH.addr.equals("") && Math.abs(Base.baidu_v.mCurLongitude) > 0.1){
		        BNaviPoint startPoint = new BNaviPoint(Base.baidu_v.mCurLongitude,Base.baidu_v.mCurLatitude,
		        		Base.baidu_v.curPoiName, BNaviPoint.CoordinateType.BD09_MC);//WGS84
		        BNaviPoint endPoint = new BNaviPoint(nvPtH.lon,nvPtH.lat,
		        		nvPtH.addr, BNaviPoint.CoordinateType.BD09_MC);
		        
		        startNavit(baseAct, startPoint, endPoint);
		        curOperatStat = 0;
		        exitOpt();
			}
			else{
				createAlertDlg(0);	
			}
			break;
		case R.id.go_company_i:
			curOperatStat = 0x100;
			nvPtC = Preference.getInstance(baseAct).getNaviPointCmpy();
			if(!nvPtC.addr.equals("")&& Math.abs(Base.baidu_v.mCurLongitude) > 0.1){
		        BNaviPoint startPoint = new BNaviPoint(Base.baidu_v.mCurLongitude,Base.baidu_v.mCurLatitude,
		        		Base.baidu_v.curPoiName, BNaviPoint.CoordinateType.BD09_MC);//WGS84
		        BNaviPoint endPoint = new BNaviPoint(nvPtC.lon,nvPtC.lat,
		        		nvPtC.addr, BNaviPoint.CoordinateType.BD09_MC);
		        
		        startNavit(baseAct, startPoint, endPoint);
		        curOperatStat = 0;
		        exitOpt();
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
//		cas_l.setVisibility(View.VISIBLE);
//		
//		func_panel_stat = 1;				

	}
	
	void closeSubFrame(){
//		cas_l.setVisibility(View.INVISIBLE);
//		
//		func_panel_stat = 0;				
//		
//		if(sub_frm.getChildCount() > 1)
//			sub_frm.removeViews(1, sub_frm.getChildCount()-1);
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
		navit_i.setImageResource(R.drawable.navigation);
		group_i.setImageResource(R.drawable.location);
		dbhv_i.setImageResource(R.drawable.drive_n);
		violet_i.setImageResource(R.drawable.violet_w);
		set_i.setImageResource(R.drawable.set_w66);
		navit_linear.setBackgroundResource(R.drawable.dot_one);
		grp_linear.setBackgroundResource(R.drawable.dot_one);
		dbhvr_linear.setBackgroundResource(R.drawable.dot_one);
		violet_linear.setBackgroundResource(R.drawable.dot_one);
		set_l.setBackgroundResource(R.drawable.dot_one);
		switch(focusId){
		case 0:
			navit_i.setImageResource(R.drawable.navigation_active);			
			navit_linear.setBackgroundDrawable(null);
			break;
		case 1:
			group_i.setImageResource(R.drawable.location_active);		
			grp_linear.setBackgroundDrawable(null);
			break;
		case 2:
			dbhv_i.setImageResource(R.drawable.drive_active);
			dbhvr_linear.setBackgroundDrawable(null);			
			break;
		case 3:		
			violet_i.setImageResource(R.drawable.violet_active);
			violet_linear.setBackgroundDrawable(null);	
			break;
		case 4:
			set_i.setImageResource(R.drawable.set_blu66);
			set_l.setBackgroundDrawable(null);
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
	
	boolean exitOpt(){
		if(myVp.getVisibility() == View.VISIBLE){
			setImageFocus(0);
			myVp.setVisibility(View.INVISIBLE);
			curOperatStat = 0;
			return true;
		}
		return false;
	}
	
	public boolean onSystemBack(){
		boolean ret = false;
		if(grp_search_e != null)
			grp_search_e.setText("");
		if(listAdapt != null)
			listAdapt.setFrndGrpLst("");
		switch (curOperatStat){
		case -1:
			break;
		//navit
		case 0:
//			if(cas_l.getVisibility() == View.VISIBLE){
//				cas_l.setVisibility(View.INVISIBLE);
//				func_img.setImageResource(R.drawable.icon_open);
//				curOperatStat = 0;
//				return true;
//			}
			ret = exitOpt();
			break;
		case 1:
			ret = exitOpt();
			break;
		case 2:
			if(drvbhv.exitDrvBhv())
				return true;
			Base.inDrvBhvStat = false;
			Base.baidu_v.top_bar_l.setVisibility(View.VISIBLE);			
			ret = exitOpt();
			break;
		case 3:
			if(violat_l.onSystemBack())
				return true;
			ret = exitOpt();
			break;
		case 4:
			ret = exitOpt();
			break;
		//pos share
		case 0x100:
			curOperatStat = 0;
			func_panel_stat = 0;
			ret = exitOpt();
//			if(cas_l.getVisibility() == View.VISIBLE){
//				cas_l.setVisibility(View.INVISIBLE);
//
//				curOperatStat = 0;
//				return true;
//			}
			break;
			//geofence
		case 0x200:
//			if(cas_l.getVisibility() == View.VISIBLE){
//				cas_l.setVisibility(View.INVISIBLE);
//
//				curOperatStat = 0;
//				return true;
//			}
			
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
		return ret;
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