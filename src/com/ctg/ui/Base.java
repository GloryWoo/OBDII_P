package com.ctg.ui;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.media.ThumbnailUtils;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.Settings;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewPropertyAnimator;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.offline.MKOLSearchRecord;
import com.baidu.mapapi.map.offline.MKOLUpdateElement;
import com.baidu.mapapi.map.offline.MKOfflineMap;
import com.baidu.mapapi.map.offline.MKOfflineMapListener;
import com.baidu.mapapi.model.LatLng;
import com.ctg.bluetooth.BluetoothSet;
import com.ctg.crash.CrashHandler;
import com.ctg.crash.LogRecord;
import com.ctg.crash.UpLoadLog;
import com.ctg.group.ChatMsgEntity;
import com.ctg.group.Group;
import com.ctg.group.Member;
import com.ctg.land.Cascade;
import com.ctg.land.Common;
import com.ctg.land.FrndGrpAdapt;
import com.ctg.land.GroupMap;
import com.ctg.net.CacheManager;
import com.ctg.net.GetDriveBehavior;
import com.ctg.net.HttpQueue;
//import com.ctg.net.HttpThread;
import com.ctg.net.IHttpCallback;
import com.ctg.net.UpdateManager;
import com.ctg.plat.phone.Dial;
import com.ctg.plat.phone.PhoneMain;
import com.ctg.plat.music.MusicPlay;
import com.ctg.plat.navit.NavitMain;
import com.ctg.sensor.OBDSensor;
import com.ctg.sensor.UpLoadSensor;
import com.ctg.service.CarDataService;
import com.ctg.service.MqttService;
import com.ctg.shareUserInfo.ShareUserPos;
import com.ctg.shareUserInfo.UserPos;
import com.ctg.shareUserInfo.UserTrace;
import com.ctg.trafficViolation.CheShouYe;
import com.ctg.util.CityDownDlg;
import com.ctg.util.CustomDialog;
import com.ctg.util.DTCsDetailDialog;
import com.ctg.util.DtcExpAdapter.CItemCont;
import com.ctg.util.FullScreenDialog;
import com.ctg.util.GeoFenceDlg;
import com.ctg.util.GrpAddMemberDlg;
import com.ctg.util.GrpCreateDlg;
import com.ctg.util.GrpDelDlg;
import com.ctg.util.GrpRenameDlg;
import com.ctg.util.GrpSearchDlg;
import com.ctg.util.MyGeoFenceCont;
import com.ctg.util.MyPagerAdapter;
import com.ctg.util.MyViewPager;
import com.ctg.util.NavitInputDlg;
import com.ctg.util.Preference;
import com.ctg.util.SearchPoiDlg;
import com.ctg.util.Util;
import com.ctg.util.VoltageHistory;
import com.ctg.weather.WeatherInfo;
import com.ctg.weather.WeatherReport;
import com.example.combinebitmap.LogUtil;
import com.example.combinebitmap.PropertiesUtil;
import com.harman.ctg.monitor.fragments.MonitorFragment;

@SuppressLint("NewApi")
@SuppressWarnings("deprecation")
public class Base extends Activity implements MKOfflineMapListener, Runnable, OnClickListener{
	private static final String TAG = "Base";
	public  final static String ACCESS_KEY = "18UzTFgyGvrzKRHqQau3DjXe";
	public static final String DB_BEHAVIOR_SERVER = "http://116.236.202.130:8080/DBehaviorServer";
	//public static final String DB_BEHAVIOR_SERVER = "http://116.236.202.130:8081/DBehaviorServer";
	public static final String HTTP_ROOT_PATH = "http://116.236.202.130:8081/obd"; //192.168.1.151  116.236.202.130 
	public static final String NEW_HTTP_ROOT_PATH = "http://116.236.202.130:8081/obd"; // 116.236.202.130
	public static final String HTTP_GROUP_PATH = "http://116.236.202.130:8081/obd/group"; //192.168.1.151  116.236.202.130
	public static final String HTTP_FRIEND_PATH = "http://116.236.202.130:8081/obd/friend"; //192.168.1.151  116.236.202.130
	public static final int APP_RUN_FOREGROUND = 1;
	public static final int APP_RUN_BACKGROUND = 2;
	public static final int APP_EXIT = 0;
	public static final int WEATHER_READY = 40;
	public static final int GPS_STAT_CHANGE = 50;
	public static final int TAB_WIDGET_GONE = 60;
	public static final int WEIZHANG_READY = 70;
	public static final int INCOMING_MSG = 80;
	public static final int SHARE_GPS_MSG = 90;
	public static final String MY_APK_NAME = "com.ctg.ui.Base";
	
    private static final int REQUEST_CONNECT_DEVICE = 1;
    public static final int IMAGE_REQUEST_CODE = 0;  
    public static final int CAMERA_REQUEST_CODE = 1;  
    public static final int RESIZE_REQUEST_CODE = 2;  
    
	private static String commData = "";
	public static final String ycblog = "/OBDII/ycbLog/";
	public static final String BTlog = "BTcon";
	public static final String OBDinit = "OBDinit";
	public static final String WeathInfo = "Weatherinfo";
	public static final String OperateInfo = "OperateInfo";
	public static final String SensorData = "SensorData";
	
	public static final int BAT_CHARGING = 1;
	public static final int BAT_NORMAL = 2;
	public static final int BAT_LOW = 3;
	public static final int BAT_NOTHING = 4;
	public static final int REGISTER = 0;
	
	public static final int LOGIN = 1;
	public static final int IGNORE = 2;
	public static final int MAP = 4;
	public static final int UNSUPPORT = 8;
	public static final int UNLOGIN = 16;
	
	public static boolean isBaseActive;
	private static boolean callType = true;
	public boolean weatherInitOK = false;
	private boolean D = true;
	static int curFocusIdx;
	private TextView mTitle;    					//
	public AlertDialog trackEnableDialog = null;
	public AlertDialog CallDialog = null;
    AlertDialog supportAlertDialog = null;
	private boolean tabWgtGone;
	Runnable tabGoneRunnable;
	public MyViewPager vPager = null;
	public FrameLayout framelayout;
	private int currIndex;
	int toastCount = 0;
	public LinearLayout dtc_c, info_layout;
	public static BaiduMapView baidu_v;
	public CityDownDlg cityDownDlg;

//	private final static int MONITOR_FRAGMENT_ID = 1010;
	FragmentManager  ftMng;
	FragmentTransaction  ftAct;
	public MonitorFragment monitor;
	private FrameLayout monitorFrame;
	public boolean monitoring = false;

	LinearLayout.LayoutParams layout_param;
	
	public Dialog createGroupDlg;
	public Dialog delGroupDlg;
	public Dialog selGroupJoinDlg;
	public Dialog renameGroupDlg;
	public Dialog searchUserDlg;
	public Dialog grpListMemberDlg;
	
	public Dialog navitDlg;
	public GeoFenceDlg fenceDlg;
	public View id_bottombar;
	public static int friendOrGrpIdx = -1;
	//0 add group member; 1 add friend; 2 add user to certain group 3 add user to temp group
	public int searchUserMode;  
	public static Me me_v;
	public static Car car_v;

	RelativeLayout carbody_rela;
	public DTCs_Scroll dtc_s;
	public Setting setting_s;
	public CustomDialog settingDlg;
	public FullScreenDialog fullScreenDlg;
	public Dialog fullScreenDlg1;
	public String queryLicence;
	OnClickListener btnListen = null;

	public WeatherInfo myWeatherInfo;
	boolean weatherInfoReady = false;
	
	public UpLoadLog myUpLoadLog;
	private Context context;

	private String weather_tips = "";
	private String instant_cont;
	private String dtc_cont;
	
	private String mac_addr_str = null;
	private boolean bt_connected;
	private static int btconection = 0;
	
	//===========bindservice=========================
	public CarDataService.myBinder localbinder;
	Intent bindintent;
	public boolean serviceConn = false;
	public static String loginUser;
	public static String nickname;

	public static OBDApplication OBDApp;
	public static Common landCommon;
	public static GroupMap landGroupM;
	public static View portraiView;
	public static float mDensity;
	public static int mDensityInt;
	public static float scale;
	public static ArrayList<VoltageHistory> volHistory;
	
	public UpdateManager updateMgr;
	public String mVersion;
	public String updateDesc;
	public AlertDialog batteryDialog;
	public Login loginDlg;
	public Register registDlg;
	public EditAccount editAccountDlg;
	static public int mWidth;
	static public int mHeight;
	static public int statusBarHeight;
	static public int realHeight;
	
	public int curPageId;
	
	private long mLastTime = 0;
	private long mCurrentTime = 0;
	
	public boolean gpsSwitchOn;
	public LocationManager locationManager;
	
	public float curVoltage = 12.0f;
	public CheShouYe cheshouye;
	
	LinearLayout navi_linear;
	LinearLayout me_linear;
	LinearLayout car_linear;
	
	ImageView topBar;
	
	RelativeLayout tab_relative;
	LinearLayout profile_linear;
	ViewPropertyAnimator animator;
	boolean profile_show;
	SimpleAdapter naviLstAdp;
	SimpleAdapter meLstAdp;
	SimpleAdapter carLstAdp;
	ArrayList<Map<String, Object>> navlistItem;
	ArrayList<Map<String, Object>> melistItem;
	ArrayList<Map<String, Object>> carlistItem;
	ListView lst_profile;
	public ImageView headportrait;
	public TextView headusername;
	public static Bitmap headbitmap;
	public static Bitmap myBitmap;
	public static Bitmap dftHeadBitmap;
	public static Bitmap dftMyBitmap;
	public OBDSensor obdSensor = null;
	public HttpQueue httpQueueInstance;
	private MKOfflineMap mOffline = null;
	double centerLon = -1;
	double centerLat = -1;
	boolean firstLocate = true;
	boolean chatOrAgreeNotProc;

    public ShareUserPos shareUserPos=null;
	private long exitTime = 0;
    LinkedList<JSONObject> mJsonList;
    public static boolean startUpLoadGPS;    
    public static Drawable gray_line_draw;
    public static Drawable gfencebackdraw;
    
    public static ArrayList<Map<String, Object>> dtcListItem;
    public static ArrayList<CItemCont> dtcLst[];
    DTCsDetailDialog dtcDetail;
    CustomDialog login_check;
    public static String myCity;
	MyViewPager myVp;
	int platCurIdx;
	List<View> listViews; 
//	public static Cascade cascade;
	public static NavitMain navitMain;
	public LinearLayout platMainLinear;
	public PhoneMain phoneM;
	public MusicPlay music;
	View obd_l;
	View dial_l;
	View music_l;
    public static boolean inDrvBhvStat;
	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}
    
    ServiceConnection connection_mqtt = new ServiceConnection(){

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			// TODO Auto-generated method stub
			
		}
    	
    };
    
	ServiceConnection connection = new ServiceConnection(){
		public void onServiceDisconnected(ComponentName name){
//			if(panel_s != null)
//				panel_s.setPanelReadData("/", "/", "/","/");
			serviceConn = false;
		}
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			// TODO Auto-generated method stub
			localbinder = (CarDataService.myBinder) service;
			serviceConn = true;
			//do your definition action;
			//localbinder.startload();	
			if(OBDApp.landScapeMode != 0)
				return;
			if(localbinder != null){
				//test
				if(setting_s == null)
					setting_s = new Setting(Base.this);
				
				// 1 line commented by WR, 5/21
				localbinder.setSetting(setting_s);				
			
				Log.d(TAG, "setActivityRun true" ); 
				int btStat = localbinder.getBTstate();
				
				if(setting_s != null && OBDApp.getOBDName() != null)
					setting_s.obdDevice = OBDApp.getOBDName();
				
				if(btStat == 0 || btStat == 1){
					if(setting_s != null){
						setting_s.bt_conn_stat.setText(R.string.title_not_connected);
//						if(panel_s != null)//add by wuzhr 5/27
//							panel_s.setPanelReadData("/", "/", "/","/");
						if(setting_s.bt_s != null){
							new Thread(setting_s.bt_s).start();
							Log.d(TAG, "BTConnect auto connect");
						}
					}
					//disableBodyCheck();
					vehicleBodyCheck(true,true,true,true,true);
				}
				else if(btStat == 2){
					setting_s.bt_conn_stat.setText(R.string.title_connecting);
//					if(panel_s != null)//add by wuzhr 5/27
//						panel_s.setPanelReadData("/", "/", "/","/");
					
				}
				else if(btStat == 3){
					String conn_state_str = Base.this.getResources().getString(R.string.title_connected_to);							
					conn_state_str += setting_s.obdDevice;
					setting_s.bt_conn_stat.setText(conn_state_str);	

				}
				
			}
		}
	};
	//===================================================
	public static int getBTconnection(){
		return btconection;
	}
	public void setBTconnection(int num){
		btconection = num;
	}
	
	
	public Handler msgHandler = new Handler(){
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			if(Base.OBDApp == null || Base.OBDApp.getActivityBack() != Base.APP_RUN_FOREGROUND){
				return;
			}
			switch(msg.what){
				case WEATHER_READY:
					weatherInitOK = true;
					if(landCommon != null){
						if(myWeatherInfo != null && myWeatherInfo.listWeatherReport != null){
							WeatherReport report = myWeatherInfo.listWeatherReport.get(0);
							if(report != null && myWeatherInfo!= null && myWeatherInfo.pm25Report != null)
								landCommon.setWeather(report.getWeather(), report.getTemperature(), myWeatherInfo.pm25Report[0]);
						}
					}
					break;
				case GPS_STAT_CHANGE:
					// we don't handle GPS change message anymore, it is done in Baidu Sdk
//					boolean trackEnable = Preference.getInstance(getApplicationContext()).getGpsMonitor();
//
//					if(setting_s != null){
//				        if(gpsEnable && trackEnable){
//				        	setting_s.gpsSwt.setImageResource(R.drawable.icon_radio_enable);
//				        }
//				        else{
//				        	Preference.getInstance(getApplicationContext()).setGpsMonitor(false);
//				        	setting_s.gpsSwt.setImageResource(R.drawable.icon_radio_disable);
//				        }
//					}
					break;
										
				case TAB_WIDGET_GONE:					
//					tabWgt.setVisibility(View.GONE);					
					break;
					
				case WEIZHANG_READY:
					String value = cheshouye.queryVal;
					break;
					
				case INCOMING_MSG:
					try {
						processExtraData();
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					break;
				default:
					break;
				
			}
		}
	};
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
	// TODO Auto-generated method stub
	//

		return super.onTouchEvent(event);
	}
	
	public void setVpagerItem0(){
		vPager.setCurrentItem(0, false);
		id_bottombar.setVisibility(View.VISIBLE);
//		baidu_v.mMapView.onResume();
	}

	
	public void onConfigurationChanged(Configuration newConfig){
		super.onConfigurationChanged(newConfig);
		if(OBDApp.callStat > 0){
			OBDApp.callStat--;
			return;
		}				
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                // land do nothing is ok
			OBDApp.landScapeMode = 1;
			switchScreenOrient();
        } else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                // port do nothing is ok
        	OBDApp.landScapeMode = 0;
			switchScreenOrient();
        }
	}
	
//    public void onConfigurationChanged(Configuration newConfig) {        
//    	super.onConfigurationChanged(newConfig);
//        if (newConfig.getLayoutDirection() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
//                // land do nothing is ok
//
//			OBDApp.landScapeMode = 1;
//			switchScreenOrient();
//        } else if (newConfig.getLayoutDirection() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {//getResources().getConfiguration().orientation
//                // port do nothing is ok
//
//			OBDApp.landScapeMode = 0;
//			switchScreenOrient();
//        }
//        
//    }
    
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		boolean ret = false;
		switch(keyCode) {
			case KeyEvent.KEYCODE_BACK:	
				if(OBDApp.landScapeMode == 0){
					if(vPager.getCurrentItem() != 0){
						if(vPager.getCurrentItem() == 1 && me_v.onBack())
							return true;
						
						setVpagerItem0();
						return true;
					}					
				}
				if(baidu_v.onBackKeyEnter()){
					return true;
				}
				if(OBDApp.landScapeMode == 1){
					if(platCurIdx == 0)						
						ret = navitMain.onSystemBack();
					else if(platCurIdx == 1){
						//ret = dial.onSystemBack();
					}
					else if(platCurIdx == 2){
						ret = music.onSystemBack();
					}
					if(ret)
						return false;
				}
				if(profile_show){
					tab_relative.clearAnimation();
					animator = tab_relative.animate();
					animator.translationXBy((int)-200*mDensity);
					animator.setStartDelay(0);
					animator.setDuration(500);
					animator.start();
					profile_show = false;
					return false;
				}
				
				if(localbinder != null){
					Log.d(TAG, "setActivityRun false" ); 
				}
				if(me_v != null){
					if(me_v.editMode){
						View childv = Base.me_v.group_v.getChildAt(Base.me_v.editIdx);
						childv.findViewById(R.id.grp_item_name).setVisibility(View.VISIBLE);
						childv.findViewById(R.id.grp_item_name_e).setVisibility(View.INVISIBLE);
						me_v.setEditMode(false);
						return false;
					}
					if(me_v.frndAdpt != null && me_v.frndAdpt.checkMode){
						me_v.frndAdpt.ExitCheckMode();
						return false;
					}
					if(me_v.waitProgress.getVisibility() == View.VISIBLE){
						me_v.waitProgress.setVisibility(View.INVISIBLE);
						return false;
					}
				}
				if(OBDApp.landScapeMode == 2){
					OBDApp.landScapeMode = 1;
					landGroupM.grp_land_frm.removeView(Base.baidu_v);
					landCommon = new Common(context, true);
					setContentView(landCommon.common_mapv);
					me_v = null;
					car_v = null;
					return false;
				}

				OBDApp.setActivityBack(APP_RUN_BACKGROUND);	
				mCurrentTime = System.currentTimeMillis();
				
				if(localbinder != null && localbinder.wasOBDConnected()){
					Preference.getInstance(getApplicationContext()).setTraceID(localbinder.getCurTraceId());
					Preference.getInstance(getApplicationContext()).setLastQuitTime();
				}								
				finish();	
				OBDApp.landScapeMode = 1;
				break;

				
			case KeyEvent.KEYCODE_HOME:
				break;
			case KeyEvent.KEYCODE_MENU:
				if(OBDApp.landScapeMode == 0){
					if(ftMng != null && monitor != null)
						ftMng.beginTransaction().remove(monitor).commit();
					
					setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
				}
				else{
					setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);				
				}
//				setCarVoltage("12.5");//test
				break;
			default:
				break;
		}
		return super.onKeyDown(keyCode, event);				
	}
	public void clear_dtc(){
		if(dtcListItem != null && dtcListItem.size() != 0){
			dtcListItem.clear();
			Toast.makeText(this, R.string.dtc_cleared, Toast.LENGTH_SHORT).show();			
		}
	}		
	public static void threadAddDtcAbstract(String dtcCodeTitle){
		
//		if(list_v == null)
//			initDtcSroll();
//		if(list_v.getAdapter() == null){
//			list_v.setAdapter(listItemAdapter);
//		}
//		list_v.setVisibility(View.VISIBLE);
//		none_dtc_rela.setVisibility(View.INVISIBLE);
		if(dtcListItem == null)
			dtcListItem = new ArrayList<Map<String, Object>>();
		int idx = dtcCodeTitle.indexOf("+");
		int len = dtcCodeTitle.length();
		String dtcPre = Base.OBDApp.getResources().getString(R.string.dtc_title) + " ";
		String dtcCode = dtcCodeTitle.substring(0, idx);
		String dtcTitle = dtcCodeTitle.substring(idx+1, len);
		for(Map<String, Object> m : dtcListItem){
			String titleItem = (String) m.get("title");
			if(titleItem.contains(dtcCode))
				return;
		}
		Map<String, Object> map = new HashMap<String, Object>();  
		map.put("title", dtcPre + dtcCode);  
		map.put("intepret", dtcTitle);  
		dtcListItem.add(map);  
		if(Base.OBDApp.baseAct != null && Base.OBDApp.baseAct.fullScreenDlg != null){
			Base.OBDApp.baseAct.fullScreenDlg.setList();
		}
//		listItemAdapter.notifyDataSetChanged();
	}
	
	public static void setCarVoltage(String vol){

		String notice = "";
		vol = vol.toLowerCase();
		if(vol.compareTo("12.8v") < 0){
			notice = "电池电压过低，请注意充电";
		}
		else{
			notice = "电池电压正常";
		}
		Map<String, Object> map = new HashMap<String, Object>();
		int mapIdx = -1;
		if(dtcListItem == null){
			dtcListItem = new ArrayList<Map<String, Object>>();
		}
		else{
			int itemIdx = 0;
			for(Map<String, Object> m : dtcListItem){
				String titleItem = (String) m.get("title");
				if(titleItem.contains("电池电压")){
					map = m;
					mapIdx = itemIdx;
					break;
				}
				itemIdx++;
			}
		}
		map.put("title", "电池电压 "+vol);  
		map.put("intepret", notice); 
		if(mapIdx == -1)
			dtcListItem.add(0,map);  
		else
			dtcListItem.set(mapIdx, map);
		if(Base.OBDApp.baseAct != null && Base.OBDApp.baseAct.fullScreenDlg != null){
			Base.OBDApp.baseAct.fullScreenDlg.setList();
		}
	}
	
	public void processExtraData() throws JSONException, UnsupportedEncodingException{		
		Intent intent = getIntent();
		if(intent == null)
			return;

		String dtcCodeTitle = intent.getStringExtra("dtcs");

		if(dtcCodeTitle != null){
			Log.d(TAG, "dtcCodeTitle is " + dtcCodeTitle); 
			
			if(dtcCodeTitle != null){
				threadAddDtcAbstract(dtcCodeTitle);
				int idx = dtcCodeTitle.indexOf("+");
				int len = dtcCodeTitle.length();
				String dtcCode = dtcCodeTitle.substring(0, idx);
				String dtcTitle = dtcCodeTitle.substring(idx+1, len);
            	String fullpath =  "xml/"+dtcCode + ".xml";
            	dtcDetail = new DTCsDetailDialog(Base.this, 320, 520, R.layout.dtcs_detail, R.style.Theme_dialog, fullpath, dtcTitle);
            	dtcDetail.show();	
			}
			return;
		//use the data received here
		}
		String gfenceCont = intent.getStringExtra("geofence");
		if(gfenceCont != null){
			int fenceIdx = intent.getIntExtra("geofenceIdx", 0);
			if(baidu_v.isGrpShareMode){
				baidu_v.exitGpsShareMode();
			}
			baidu_v.onBackKeyEnter();
			if(vPager.getCurrentItem() == 1){				
				setVpagerItem0();
			}
			else if(vPager.getCurrentItem() == 2){
				car_v.onBackKeyDown();
				setVpagerItem0();
			}
			else{
				if(navitDlg != null)
					navitDlg.hide();
				if(fenceDlg != null)
					fenceDlg.hide();
			}			
			baidu_v.gfenceState = 3;
			baidu_v.myGfence = CarDataService.fenceList.get(fenceIdx);
			baidu_v.addGeofenceMarker();
			return;
		}
		int type = intent.getIntExtra("type", 0);
		String jsonStr = intent.getStringExtra("json");
		JSONObject jsonMsg;
		String carPoolMsg = "";
		if(jsonStr == null)
			return;
	
		JSONObject json = new JSONObject(jsonStr);
		//invite group member or friend should save to list because there may be more than one invitation 
		//but process is not immediate
		if((type == MqttService.INVITE_NOTIFY || type==MqttService.INVITE_FRIEND_NOTIFY) && json != null){
			if(mJsonList == null){
				mJsonList = new LinkedList<JSONObject>();
			}
			mJsonList.add(json);
		}
		
		DialogInterface.OnClickListener buttonClick = new DialogInterface.OnClickListener(){

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				if(which == DialogInterface.BUTTON_POSITIVE){					
					JSONObject obj = new JSONObject();
					String url = Base.HTTP_GROUP_PATH+"/addUserToGroup";
					JSONObject tempJson = mJsonList.pollLast();
					if(tempJson== null)
						return;
					try {									
						obj.put("appid", "appid");
						obj.put("groupName", tempJson.getString("groupName"));
						obj.put("fromUser", tempJson.getString("fromUser"));
						obj.put("listMember", true);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}					
					Base.this.httpQueueInstance.EnQueue(url, obj, 15);
				}
				else if(which == DialogInterface.BUTTON_NEGATIVE){
					
				}
				dialog.cancel();
			}			
		};

		DialogInterface.OnClickListener buttonClick1 = new DialogInterface.OnClickListener(){

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub			
				if(which == DialogInterface.BUTTON_POSITIVE){					
					JSONObject obj = new JSONObject();
					String url = Base.HTTP_FRIEND_PATH+"/addFriend";
					JSONObject tempJson = mJsonList.pollLast();
					if(tempJson== null)
						return;
					try {									
						obj.put("appid", "appid");
						obj.put("fromUser", tempJson.getString("fromUser"));
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}					
					Base.this.httpQueueInstance.EnQueue(url, obj, 51);
				}
				else if(which == DialogInterface.BUTTON_NEGATIVE){
					
				}
				dialog.cancel();
			}			
		};
		
		DialogInterface.OnCancelListener onCancel = new DialogInterface.OnCancelListener(){

			@Override
			public void onCancel(DialogInterface dialog) {
				// TODO Auto-generated method stub
				mJsonList.pollLast();
			}
			
		};
		
		switch(type){
			case MqttService.INVITE_NOTIFY:
				new AlertDialog.Builder(this)
				 	.setTitle(R.string.string_confirm1)
				 	.setMessage("确定加入群组："+json.getString("groupName")+"？")
				 	.setPositiveButton("是", buttonClick)
				 	.setNegativeButton("否", buttonClick)
				 	.setOnCancelListener(onCancel)
				 	.show();
				break;				
			case MqttService.MSG_NOTIFY:
				if(me_v != null){// && me_v.vGroupList != null						
					ChatMsgEntity msgEnti = new ChatMsgEntity();
					JSONObject obj_enti = json.getJSONObject("messages");
					JSONArray jsonarray = obj_enti.getJSONArray("users");
					msgEnti.name = obj_enti.getString("from");
					if(obj_enti.has("group"))
						msgEnti.groupName = URLDecoder.decode(obj_enti.getString("group"), "UTF-8");
					msgEnti.msgType = obj_enti.getInt("type");
					msgEnti.lOrR = 0;
					msgEnti.date=Util.getDate();
					msgEnti.usrsList = new ArrayList<String>();
					if(msgEnti.msgType == ChatMsgEntity.CHAT_MSG_TEXT){														
						msgEnti.text = URLDecoder.decode(obj_enti.getString("text"), "UTF-8");
					}
					else if(msgEnti.msgType == ChatMsgEntity.CHAT_MSG_LOCATE){
						//obj_enti.put("latlon", msgEnti.latlon_loc);
						//JSONObject latlonObj = new JSONObject(obj_enti.getString("latlon"));
						int len = jsonarray.length();
						int i = 0;
						for(i = 0; i < len; i++){
							msgEnti.usrsList.add((String) jsonarray.get(i));
						}
						msgEnti.latlon_loc = new LatLng(obj_enti.getDouble("lat"), 
								obj_enti.getDouble("lon"));
						me_v.processChatMsg(msgEnti);
						
					}
					else if(msgEnti.msgType == ChatMsgEntity.CHAT_MSG_TRACK){
						JSONArray jsArr = obj_enti.getJSONArray("latlon_lst");	
						msgEnti.latlon_track = new ArrayList<LatLng>();
						for(int i = 0; i < jsArr.length(); i+=2){
							LatLng latlon = new LatLng(jsArr.getDouble(i), jsArr.getDouble(i+1));							
							msgEnti.latlon_track.add(latlon);
							//msgEnti.latlon_track
						}
						obj_enti.put("latlon_lst", jsArr);						
					}
				}
				else
					chatOrAgreeNotProc = true;
				break;
			case MqttService.AGREE_NOTIFY:
				if(me_v != null)
					me_v.addGroupMember(json);
				break;
			case MqttService.DELETE_GRP_USER:	
				if(me_v != null)
					me_v.delGroupMember(json);
				break;
			case MqttService.INVITE_FRIEND_NOTIFY:
				new AlertDialog.Builder(this) 
			 	.setTitle(R.string.string_confirm1)
			 	.setMessage("确定成为"+json.getString("fromUser")+"的好友？")
			 	.setPositiveButton("是", buttonClick1)
			 	.setNegativeButton("否", buttonClick1)
			 	.setOnCancelListener(onCancel)
			 	.show();
				break;
			case MqttService.AGREE_FRIEND_NOTIFY:
				me_v.addFriend(json.getString("fromUser"), "", 1);
				break;
			case MqttService.GPS_SHARE_NOTIFY:
	            Log.d(TAG, "GPS_SHARE_NOTIFY");
	            if(OBDApp.landScapeMode != 0){
	            	if(OBDApp.landScapeMode == 1){
	            	}
	            }
	            else{// if(baidu_v != null)
		            UserPos userPos = new UserPos(json);
		            baidu_v.drawSharePos(userPos);
	            }
	            break;
	        case MqttService.TRACE_SHARE_NOTIFY://收到别人分享的轨迹后的事件
	            Log.d(TAG, "TRACE_SHARE_NOTIFY");
	            if(baidu_v != null){
	            	if(OBDApp.landScapeMode != 0){
	            		if(OBDApp.landScapeMode == 1){
		        			Base.landCommon.enterGrpMapView();
		            	}
	            		Base.landGroupM.drawPosOrTrace(json);
	            	}
	            	else{
			            UserTrace userTrace = new UserTrace(json);            
			            baidu_v.drawShareTrace(userTrace);
			            TextView txt = new TextView(Base.this);
			            //txt.setTextSize(size);
			            info_layout.setVisibility(View.GONE);
			            txt.setText(userTrace.getFromUser() + "的轨迹");
			            txt.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			            info_layout.addView(txt, 0);
	            		
	            	}
	            }
	            break;	
	        case MqttService.INVITE_CARPOOL:	        
	        	baidu_v.receiveInvitor = json.getString("fromUser");
	        	baidu_v.receiveInviteType = Integer.parseInt(json.getString("type"));
	        	if(json.has("groupName"))
	        		baidu_v.receiveGrpNm = json.getString("groupName");
	        	else
	        		baidu_v.receiveGrpNm = null;
	        	
	        	if(baidu_v.receiveInviteType == 0){
	        		jsonMsg = json.getJSONObject("messages");
	        		baidu_v.receiveLat = Double.parseDouble(jsonMsg.getString("latitude"));
		        	baidu_v.recevielng = Double.parseDouble(jsonMsg.getString("longitude"));
	        	}
	        	else{
	        		carPoolMsg = json.getString("messages");
	        	}
	        	baidu_v.receivePickupInviteCheck();
	        	break;
	        case MqttService.REPLY_CARPOOL:
	        	baidu_v.replyUser = json.getString("fromUser");
	        	baidu_v.replyResult = Integer.parseInt(json.getString("reply"));
	        	if(json.has("type"))
	        		baidu_v.replyType = Integer.parseInt(json.getString("type"));
	        	else
	        		baidu_v.replyType = baidu_v.inviteType;
	        	if(json.has("groupName"))
	        		baidu_v.replyGrpNm = json.getString("groupName");
	        	else
	        		baidu_v.replyGrpNm = null;
	        	
	        	if(baidu_v.replyResult == 1){
	        		if(json.has("messages"))
	        		{
	        			Object obj = json.get("messages");
	        			if(obj instanceof JSONObject){
	        				jsonMsg = json.getJSONObject("messages");
	        				if(jsonMsg.has("latitude")){
					        	baidu_v.replyLat = Double.parseDouble(jsonMsg.getString("latitude"));
					        	baidu_v.replylng = Double.parseDouble(jsonMsg.getString("longitude"));
				        	}
	        			}
	        			else if(obj instanceof String){
	        				String msgStr = json.getString("messages");
	        			}
			        	
	        		}
	        	}
	        	baidu_v.receivePickupReply();
	        	break;
			default:break;
		}
		//vPager.setCurrentItem(1, false);
	}

	@Override
	protected void onDestroy()
	{
		if(setting_s != null && setting_s.bt_s != null)
			setting_s.bt_s.Auto_BT_OnDestroy();
		if(OBDApp.landScapeMode == 1){
			if(music != null){
				music.onDestroy();
			}
		}
		if(dtc_s != null)
			dtc_s.DTCs_Destroy();
		if(Base.baidu_v != null){
			Base.baidu_v.onDestroy();
		}
		
		saveGroup2Local(HttpQueue.grpResLst);
		saveFriend2Local(HttpQueue.friendLst);		
		
		if(serviceConn){
			unbindService(connection);
			unbindService(connection_mqtt);
		}
		OBDApp.setPlayTimeHander(null);
		CarDataService.saveFenceData();
		Member.getHeadBitmapHandler = null;
		serviceConn = false;
		getContentResolver().unregisterContentObserver(mGpsMonitor);
		OBDApp.baseAct = null;
		if(D) Log.e(TAG,"base onDestory");
		super.onDestroy();
	}
	
	
	protected void onPause(){
		super.onPause();
		isBaseActive = false;
		if(baidu_v != null)
			baidu_v.mMapView.onPause();
		if(music != null){
			music.onPause();
		}	
		if(D) Log.e(TAG, "base onPause"); 	
	}
	
	protected void onResume(){
		super.onResume();
		if(D) Log.e(TAG, "base onResume");
		isBaseActive = true;
		if(baidu_v != null)
			baidu_v.mMapView.onResume();
		if(music != null){
			music.onResume();
		}
		judgeComeFrom();


		if(setting_s != null)
			setting_s.setSettingLoginState();	
		     
	}
	
	public void onNewIntent(Intent intent){
		super.onNewIntent(intent);
		setIntent(intent);
		try {
			processExtraData();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

	}
	
	private final ContentObserver mGpsMonitor = new ContentObserver(null) {

		@Override
		public void onChange(boolean selfChange) {
			super.onChange(selfChange);
//			gpsEnable = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
			
			msgHandler.obtainMessage(GPS_STAT_CHANGE).sendToTarget();
		}

	};

		    

	class ProfileAnimaThread extends Thread{
		public synchronized void run() {
			int i = 0;
			
			while(i++ < 8){
				
					
				tab_relative.setLeft((int) (tab_relative.getLeft()+25*mDensity));
					
				
				try {
					sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	};

	protected void initOfflineMap() {
		// 初始化离线地图
		mOffline = new MKOfflineMap();
		mOffline.init(this);
		mOffline.importOfflineData();
	}

	private void judgeComeFrom() {	
		centerLon = getIntent().getDoubleExtra("x", 0);
		centerLat = getIntent().getDoubleExtra("y", 0);
		if( centerLon < 0.000001 && centerLon > -0.000001){
		}
		else{
			baidu_v.mBaiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(new LatLng(
					centerLat, centerLon)));
			if(navitDlg != null)
				navitDlg.cancel();
		}
		
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()){
		case R.id.obd_p:
			myVp.setCurrentItem(0, false);
			baidu_v.mMapView.onResume();
			platCurIdx = 0;
			break;
		case R.id.dial_p:
			phoneM.initPhoneMain();
			myVp.setCurrentItem(1, false);
			platCurIdx = 1;			
			break;
		case R.id.music_p:
			myVp.setCurrentItem(2, false);
			platCurIdx = 2;
			break;			
		}
	}
	
	@SuppressLint("NewApi")
	public void onCreate(Bundle savedInstanceState){
		long start_time = System.currentTimeMillis();
	
		if(D) Log.e(TAG, "base onCreate");
		super.onCreate(savedInstanceState); 
		
		gray_line_draw = getResources().getDrawable(R.drawable.gray_line);
		gfencebackdraw = getResources().getDrawable(R.drawable.shape_card_b);
		if(OBDApp == null)
			OBDApp = (OBDApplication) getApplication();
		
		OBDApp.setActivityBack(APP_RUN_FOREGROUND);
		OBDApp.setHandler(baseHandler);
		OBDApp.baseAct = this;
		context = this;	
		if(obdSensor == null)
			obdSensor = new OBDSensor(this);
		loginUser = Preference.getInstance(Base.OBDApp.getApplicationContext()).getUser();
		nickname = Preference.getInstance(Base.OBDApp.getApplicationContext()).getNickname();

		
		bindintent = new Intent(this, CarDataService.class);
		bindService(bindintent, connection, BIND_AUTO_CREATE);		
		serviceConn = true; 
		Intent mqttIntent = new Intent(this, MqttService.class);
		mqttIntent.setAction("appid.START");
		bindService(mqttIntent, connection_mqtt, BIND_AUTO_CREATE);		

		CarDataService.initFenceData();
		Log.d(TAG,"OBDApp.mqttStat = " + OBDApp.mqttStat);


//		HttpQueue.grpResLst = readGroupFromLocal();		
		if(HttpQueue.grpResLst == null){
			HttpQueue.grpResLst = new ArrayList<Group>();
		}
		
//		HttpQueue.friendLst = readFriendFromLocal();
		if(HttpQueue.friendLst == null){
			HttpQueue.friendLst = new ArrayList<Member>();
		}
		
		switchScreenOrient();
		//setContentView(frame, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)); //@xxm
		if(!loginUser.equals(""))
			headbitmap = Member.getHeadBitmapUser(loginUser);
		if(headbitmap != null)
			myBitmap = Util.getRoundedCornerImageColorTriangle(headbitmap, 50*Base.mDensityInt, 50*Base.mDensityInt, 0xff01d4fb);	
		dftHeadBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher_df);
		dftMyBitmap = Util.getRoundedCornerImageColorTriangle(dftHeadBitmap, 50*Base.mDensityInt, 50*Base.mDensityInt, 0xff01d4fb);
		if(shareUserPos == null)
			shareUserPos = new ShareUserPos(context);
		
		if(httpQueueInstance == null)
			httpQueueInstance = HttpQueue.getInstance(this);
			


		judgeComeFrom();
		initOfflineMap();		
		
		LatLng latlon = Preference.getInstance(getApplicationContext()).getPointLatLng();
		if(latlon.latitude > 0.000001 || latlon.latitude < -0.000001){
			baidu_v.mBaiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(latlon));
			if(Base.myBitmap != null && Preference.getInstance(this).getLoginStat()){						
				baidu_v.myHeadMarker = (Marker) baidu_v.mBaiduMap.addOverlay(new MarkerOptions().position(latlon)
	                    .icon(BitmapDescriptorFactory.fromBitmap(Base.myBitmap)));	//fromBitmap(myBitmap)
			}
			else{
				baidu_v.myHeadMarker = (Marker) baidu_v.mBaiduMap.addOverlay(new MarkerOptions().position(latlon)
	                    .icon(BitmapDescriptorFactory.fromBitmap(Base.dftMyBitmap)));	
			}
		}			
		String uploadurl = HTTP_ROOT_PATH +"/services/log";
		myUpLoadLog = new UpLoadLog(context,uploadurl);

		if(OBDApp.landScapeMode != 0){
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,   
	                WindowManager.LayoutParams.FLAG_FULLSCREEN);			
		}
		else
		{
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN,   
	                WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
		}
		
		try {
			processExtraData();
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		

		if(OBDApp.appHasEnter)//
			return;
		
		OBDApp.appHasEnter = true;
		
		if(!Preference.getInstance(getApplicationContext()).getLoginStat())//&& OBDApp.isBackRun_history != APP_RUN_BACKGROUND) 
		{	
//			Intent splash = new Intent(context, SplashActivity.class);
//			splash.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//			startActivity(splash);
			loginDlg = new Login(Base.this, Base.mWidth, Base.mHeight, R.layout.login, R.style.Theme_dialog);
			loginDlg.show();

		}
		else{
		}
		//start OBDII service
		IntentFilter obd_filter = new IntentFilter("android.com.ctg.service");

		

		//httpConnect = OBDApp.httpConnect;
		if(true){//!OBDApp.hasEntered
			String url = HTTP_ROOT_PATH + "/services/newAppVersion";
			String sessionid = Preference.getInstance(getApplicationContext()).getSessionId();
			Map<String, String> param = new HashMap<String, String>();
			
			param.put("token", sessionid);
				
			
//			if (OBDApp.httpConnect != null) {
//				OBDApp.httpConnect.stopThread();
//				OBDApp.httpConnect = null;
//			}
//			OBDApp.httpConnect = new HttpThread(this, httpHandler, url, param, 4);
//			OBDApp.httpConnect.startHttp();
			
			CacheManager.getJson(this, url,  new IHttpCallback() {
				
				@Override
				public void handle(int retCode, Object response) {
					// TODO Auto-generated method stub		
					try{
						if(retCode == 200){
							JSONObject jsonObject = new JSONObject(response.toString());
							//data = jsonObject.getJSONObject("data");
							mVersion = jsonObject.getString("version");
							Base.OBDApp.downApkLen = jsonObject.getInt("size");
							String downurl = jsonObject.getString("download_path");
							updateDesc = jsonObject.getString("desc");
				
							int idx = downurl.lastIndexOf("/");
							String apkName = downurl.substring(idx+1);
							String apkDir = downurl.substring(0, idx+1);
							try {
								downurl = apkDir + URLEncoder.encode(apkName, "UTF-8");
							} catch (UnsupportedEncodingException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
							downurl = HTTP_ROOT_PATH + downurl;
							//String curVersion = Preference.getInstance(Base.this.getApplicationContext()).getVersion();
							String curVersion = OBDApp.getVersion();
							if(curVersion == null || curVersion.equals("null"))
								curVersion = "1";		
	//						float localV = Float.parseFloat(curVersion);
	//						float serverV = Float.parseFloat(mVersion);
							if(Util.versionCompare(mVersion, curVersion) > 0){//mVersion.compareTo(curVersion)
								updateMgr = new UpdateManager(Base.this, downurl);
							    String saveFileName = Base.getSDPath()+"/OBDII/OBDII.apk"; 
							    String versionExist = "";
							    try {
							        PackageManager manager = getPackageManager();
							        PackageInfo info = manager.getPackageArchiveInfo(saveFileName, PackageManager.GET_ACTIVITIES);
							        versionExist = info.versionName;
	
							    } catch (Exception e) {
							        e.printStackTrace(); 
							    }
						        if(versionExist.equals(mVersion))//
						        {
						        	updateMgr.checkUpdateInfo();
						        }
						        else
						        	updateMgr.downloadApk();
							}
						}
						else{
						
						}
					}
					catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}, null);
			
			if(!sessionid.equals("1234567890")){			
//				getGroupAndFriendList();				
				String usr = Preference.getInstance(this).getUser();
				url = Base.HTTP_GROUP_PATH+"/findGroups?appID=appid&memberName="+usr+"&listMember=1";
				httpQueueInstance.EnQueue(url, null, 142);
				
				url = Base.HTTP_FRIEND_PATH+"/getFriends";
				httpQueueInstance.EnQueue(url, null, 52);
				
			}						
		}

		if (getNewWorkType() == ConnectivityManager.TYPE_MOBILE 
		  || getNewWorkType() == ConnectivityManager.TYPE_WIFI){
			if(checkNeedDownloadPriorCityMap(false))
				download_offline_map_check();
		}
		else{
			Toast.makeText(Base.OBDApp, "手机没有连接网络，无法使用在线地图群组分享等功能", Toast.LENGTH_SHORT).show();
		}
		
        getContentResolver().registerContentObserver(
            Settings.Secure.getUriFor(Settings.System.LOCATION_PROVIDERS_ALLOWED),false, mGpsMonitor);

        
		//Add to record crash log:
		CrashHandler crashHandler = CrashHandler.getInstance();
		crashHandler.init(getApplicationContext(), myUpLoadLog);
		
	
		//Add to record log:
		//test
		//setCarVoltage("14.2v");
		
		long end_time = System.currentTimeMillis();
		long spend_time = end_time - start_time;
		Log.i(TAG,"mView.draw: spend_time = " + spend_time);		
	}
	
	public void switchScreenOrient(){
		if(OBDApp.landScapeMode == 1){
			portraiView = null;
			//setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			
			locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);					
			WindowManager wm = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
		    mWidth = wm.getDefaultDisplay().getWidth();
		    mHeight = wm.getDefaultDisplay().getHeight();
			statusBarHeight = getStatusBarHeight();
			realHeight = mHeight-statusBarHeight;
			scale = getResources().getDisplayMetrics().density/2;
			mDensity = getResources().getDisplayMetrics().density;
			mDensityInt = (int) mDensity;
			
			platMainLinear = (LinearLayout) View.inflate(this, R.layout.plat_main, null);
			
			myVp = (MyViewPager) platMainLinear.findViewById(R.id.plat_vpager);
//			cascade = new Cascade(context);
			navitMain = new NavitMain(context);
			phoneM = new PhoneMain(context);
			music = new MusicPlay(context);
			listViews = new ArrayList<View>();
			listViews.add(navitMain.main_frm);			
			listViews.add(phoneM.linear);
			listViews.add(music.music_linear);
			myVp.setAdapter(new MyPagerAdapter(listViews));
			
			obd_l = platMainLinear.findViewById(R.id.obd_p);
			dial_l = platMainLinear.findViewById(R.id.dial_p);
			music_l = platMainLinear.findViewById(R.id.music_p);
			obd_l.setOnClickListener(this);
			dial_l.setOnClickListener(this);
			music_l.setOnClickListener(this);
			setContentView(platMainLinear);
			
		}
		else{	
			landCommon = null;
			//setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			
			locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);					
			WindowManager wm = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
		    mWidth = wm.getDefaultDisplay().getWidth();
		    mHeight = wm.getDefaultDisplay().getHeight();
			statusBarHeight = getStatusBarHeight();
			realHeight = mHeight-statusBarHeight;
			scale = getResources().getDisplayMetrics().density/2;
			mDensity = getResources().getDisplayMetrics().density;
			mDensityInt = (int) mDensity;
			
			portraiView = View.inflate(context, R.layout.table_lay, null);
			initPortraitView();
			setContentView(portraiView); 
		}
	}
	
	public void saveGroup2Local(List<Group> grpLst){
		
		File fd = new File(Base.getSDPath() +"/OBDII/grouplist_"+loginUser);
		if(grpLst == null || grpLst.size() == 0){
			fd.deleteOnExit();
			return;
		}
		
		StringBuffer sbf = new StringBuffer();
		for(Group grp: grpLst){
			sbf.append(grp.name);
			sbf.append(",");
			sbf.append(grp.creator);					
			for(Member member : grp.memberList){
				sbf.append(",");
				sbf.append(member.name);
				
			}
			sbf.append(";");
		}
		
		
		try {
			FileOutputStream ou_s = new FileOutputStream(fd);	
			ou_s.write(sbf.toString().getBytes("UTF-8"));
			ou_s.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public ArrayList<Group> readGroupFromLocal(){
		ArrayList<Group> grpLst = new ArrayList<Group>();


		File fd = new File(Base.getSDPath() +"/OBDII/grouplist_"+loginUser);
		if(!fd.exists()){
			return grpLst;
		}
		int size = (int) fd.length();
		if(size <= 0)
			return grpLst;
		byte byteSz[] = new byte[size];
		String strbuff;
		try {
			FileInputStream in_s = new FileInputStream(fd);	
			in_s.read(byteSz);
			in_s.close();			
			strbuff = new String(byteSz, "UTF-8");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return grpLst;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return grpLst;
		}
		String groupStr[] = strbuff.split(";");
		for(String grpItemStr : groupStr){
			String nameSz[] = grpItemStr.split(",");
			if(nameSz.length < 2)
				return grpLst;
			Group grp = new Group(nameSz[0], nameSz[1]);
			ArrayList<Member> memLst = new ArrayList<Member>();
			int len = nameSz.length;
			boolean creat = false;
			for(int i = 2; i < len; i++){			
				creat = nameSz[i].equals(nameSz[1]) ? true: false;
				
				Member member = new Member(nameSz[i], 0, creat);
				member.getHeadBitmapLocal();
				memLst.add(member);
				
			}
			grp.memberList = memLst;			
			grpLst.add(grp);
		}
		
		return grpLst;
	}
	
	public void saveFriend2Local(List<Member> frndLst){
		File fd = new File(Base.getSDPath() +"/OBDII/friendlist_"+loginUser);
		if(frndLst == null || frndLst.size() == 0){
			fd.deleteOnExit();
			return;
		}						

		try {
			ObjectOutputStream ou_obj = new ObjectOutputStream(new FileOutputStream(fd));	
			for(Member member : frndLst){
				ou_obj.writeObject(member);
			}
			ou_obj.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public ArrayList<Member> readFriendFromLocal(){
		ArrayList<Member> frndLst = new ArrayList<Member>();
		
		File fd = new File(Base.getSDPath() +"/OBDII/friendlist_"+loginUser);
		if(!fd.exists()){
			return frndLst;
		}
		Member member = null;
		ObjectInputStream in_obj = null;
		try {
			in_obj=new ObjectInputStream(new FileInputStream(fd));
			
			while((member = (Member) in_obj.readObject()) != null){
				member.getHeadBitmapLocal();
				frndLst.add(member);
			}
			in_obj.close();			
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
			
		} catch (EOFException e){
			if(in_obj != null)
				try {
					in_obj.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}	
			return frndLst;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return frndLst;
	}
	
	public void getGroupAndFriendList(){
		String usr = Preference.getInstance(this).getUser();
		String url = Base.HTTP_GROUP_PATH+"/findGroups?appID=appid&memberName="+usr+"&listMember=1";
		CacheManager.getJson(this, url,  new IHttpCallback() {
			
			@Override
			public void handle(int retCode, Object response) {
				// TODO Auto-generated method stub		
				int len, usrLen;
				try{
					String resp = response.toString();
					JSONArray jsonArray = new JSONArray(resp), usrJsArr;
					JSONObject jsonObj;
					String groupName, groupCreator, memName, headImgNm;
					Group group;
				
					if(retCode == 200){
						len = jsonArray.length();
						if(HttpQueue.grpResLst == null)
							HttpQueue.grpResLst = new ArrayList<Group>();
						else
							HttpQueue.grpResLst.clear();
						for(int i = 0; i < len; i++){
							jsonObj = jsonArray.getJSONObject(i);
							
							groupName = jsonObj.getString("groupName");
							
							groupName = URLDecoder.decode(groupName, "UTF-8");	
							groupCreator = jsonObj.getString("createUserName");
							usrJsArr = jsonObj.getJSONArray("users");
							usrLen = usrJsArr.length();								
							ArrayList<Member> tmplist  = new ArrayList<Member>();
							for(int j = 0; j < usrLen; j++){
								JSONObject obj = usrJsArr.getJSONObject(j);
								memName = obj.getString("user");
								memName = URLDecoder.decode(memName, "UTF-8");
								headImgNm = obj.getString("image");
								Member mem;
								if(groupCreator.equals(memName))
									mem = new Member(memName, obj.getInt("online"), true, headImgNm);
								else
									mem = new Member(memName, obj.getInt("online"), false, headImgNm);
								if(memName.equals(Base.loginUser))
									tmplist.add(0, mem);
								else
									tmplist.add(mem);
							}
							
							
							
							groupCreator = jsonObj.getString("createUserName");
							group = new Group(i, groupCreator, groupName,
									jsonObj.getString("groupDes"), tmplist);
							
							if(HttpQueue.grpCreatorLst == null)
								HttpQueue.grpCreatorLst = new ArrayList<Group>();
							else
								HttpQueue.grpCreatorLst.clear();
														
							if(groupCreator.equals(Base.loginUser)){
								HttpQueue.grpCreatorLst.add(group);
							}
							
							HttpQueue.grpResLst.add(group);
						}
						if(OBDApp.landScapeMode == 0){
							if(me_v != null){
								me_v.setFrndGrpList();
							}
						}
						else{
							if(navitMain != null){
								navitMain.setFrndGrpList();
							}
						}
						
					}
					
				
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				finally{
					
				}
			}
		}, null);	
		
		url = Base.HTTP_FRIEND_PATH+"/getFriends";
		CacheManager.getJson(this, url,  new IHttpCallback() {
			
			@Override
			public void handle(int retCode, Object response) {
				// TODO Auto-generated method stub		
				try{
					if(retCode == 200){
						JSONObject jsonObj;
						
						JSONArray jsonArray = new JSONArray(response.toString());						 
						int len = jsonArray.length();
						if(HttpQueue.friendLst == null)
							HttpQueue.friendLst = new ArrayList<Member>();
						else
							HttpQueue.friendLst.clear();
						for(int i = 0; i < len; i++){
							jsonObj = (JSONObject) jsonArray.get(i);
							String userName = jsonObj.getString("username");	
							String nickName = jsonObj.getString("alias");
							int online = jsonObj.getInt("online");
							String headImgNm = jsonObj.getString("image");
							Member member = new Member(userName, nickName, online, headImgNm);
							HttpQueue.friendLst.add(member);
						}	
									
						
						if(OBDApp.landScapeMode == 0){
							if(me_v != null){
								me_v.setFrndGrpList();
							}
						}
						else{
							if(navitMain != null){
								navitMain.setFrndGrpList();
							}
						}
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}, null);	
	}
	
	public boolean checkNeedDownloadPriorCityMap(boolean download){
		boolean ret = false;
		if(Base.myCity == null)
			return false;
		ArrayList<MKOLSearchRecord> curLst = mOffline.searchCity(Base.myCity);
		ArrayList<MKOLSearchRecord> availLst = mOffline.getOfflineCityList();
		int cityId;	
		MKOLUpdateElement elem;
		if(curLst != null && curLst.size() != 0){
			cityId = curLst.get(0).cityID;
			elem = mOffline.getUpdateInfo(cityId);
			if(elem != null && elem.status == MKOLUpdateElement.FINISHED)
				;
			else{
				ret = true;
				if(download){
					mOffline.start(cityId);
				}
			}
			
		}
		elem = null;
		if(availLst != null && availLst.size() != 0){
			cityId = availLst.get(0).cityID;
			elem = mOffline.getUpdateInfo(cityId);
			if(elem != null && elem.status == MKOLUpdateElement.FINISHED)
				;
			else{
				ret = true;
				if(download){
					mOffline.start(cityId);
				}
			}
		}
		return ret;		
	}
	
	void initPortraitView(){
		listViews = new ArrayList<View>();	
		vPager  = (MyViewPager) portraiView.findViewById(R.id.viewpager); 
		id_bottombar = portraiView.findViewById(R.id.id_bottombar); 
//		LinearLayout linear = (LinearLayout) portraiView.findViewById(R.id.table_lay_linear);
		baidu_v = new BaiduMapView(context, centerLon, centerLat);

		me_v = new Me(context);
		car_v = new Car(context);
		listViews.add(baidu_v);
		listViews.add(me_v);
		listViews.add(car_v);
//		vPager.addView(baidu_v);
//		vPager.addView(me_v);
//		vPager.addView(car_v);
		//初始化离线地图
		info_layout = (LinearLayout) portraiView.findViewById(R.id.share_info);
		
		tab_relative = (RelativeLayout) portraiView.findViewById(R.id.tab_relati);
		profile_linear = (LinearLayout) portraiView.findViewById(R.id.profile_linear);
		topBar = (ImageView) portraiView.findViewById(R.id.profile);
		lst_profile = (ListView) portraiView.findViewById(R.id.list_profile);
		
		navlistItem = new ArrayList<Map<String, Object>>();
		naviLstAdp = new SimpleAdapter(this,navlistItem,// 
	            R.layout.profile_list_item,
	            new String[] {"image", "title"},   
	            new int[] {R.id.map_profile_icon,R.id.map_profile_text}  
	        );
		
		Map<String, Object> map;
		map = new HashMap<String, Object>();
		map.put("image", R.drawable.fence_map);
		map.put("title", getResources().getString(R.string.geo_fence));
		navlistItem.add(map);
//		map = new HashMap<String, Object>();
//		map.put("image", R.drawable.map_detail);
//		map.put("title", getResources().getString(R.string.poi_share));
//		navlistItem.add(map);
//		map = new HashMap<String, Object>();
//		map.put("image", R.drawable.route);
//		map.put("title", getResources().getString(R.string.track_share));
//		navlistItem.add(map);
		map = new HashMap<String, Object>();
		map.put("image", R.drawable.icon_navi_h);
		map.put("title", getResources().getString(R.string.navigate));
		navlistItem.add(map);
		map = new HashMap<String, Object>();
		map.put("image", R.drawable.offline);
		map.put("title", getResources().getString(R.string.offline_map));
		navlistItem.add(map);
		headportrait = (ImageView) portraiView.findViewById(R.id.head_portrait);
		headusername = (TextView) portraiView.findViewById(R.id.head_username);
		
		headusername.setText(loginUser);
		
		if(headbitmap != null){
			Bitmap bitProc = Util.getRoundedCornerImage(headbitmap);
			headportrait.setImageBitmap(bitProc);			
		}
		lst_profile.setAdapter(naviLstAdp);
		
		melistItem = new ArrayList<Map<String, Object>>();
		meLstAdp = new SimpleAdapter(this,melistItem,// 
	            R.layout.profile_list_item,
	            new String[] {"image", "title"},   
	            new int[] {R.id.map_profile_icon,R.id.map_profile_text}  
	        );
		
		map = new HashMap<String, Object>();
		map.put("image", android.R.drawable.ic_menu_add);
		map.put("title", getResources().getString(R.string.group_create));
		melistItem.add(map);
		map = new HashMap<String, Object>();
		map.put("image", android.R.drawable.ic_menu_delete);
		map.put("title", getResources().getString(R.string.group_del));
		melistItem.add(map);
		map = new HashMap<String, Object>();
		map.put("image", android.R.drawable.ic_menu_edit);
		map.put("title", getResources().getString(R.string.group_modify));
		melistItem.add(map);
		map = new HashMap<String, Object>();
		map.put("image", android.R.drawable.ic_menu_search);
		map.put("title", getResources().getString(R.string.group_search));
		melistItem.add(map);
		map = new HashMap<String, Object>();
		map.put("image", R.drawable.ic_menu_invite);
		map.put("title", getResources().getString(R.string.group_add_user));
		melistItem.add(map);
		
		map = new HashMap<String, Object>();
		map.put("image", R.drawable.ic_menu_allfriends);
		map.put("title", getResources().getString(R.string.group_add_friend));
		melistItem.add(map);
		
		map = new HashMap<String, Object>();
		map.put("image", R.drawable.ic_menu_blocked_user);
		map.put("title", getResources().getString(R.string.group_select_del_user));
		melistItem.add(map);
		
		
		//dtc_c = new LinearLayout(this);
		layout_param = new LinearLayout.LayoutParams(LinearLayout.
	    		LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT);
//		dtc_l = new DTCs_List(this);
		initBodyChek();
		//dtc_c.setBackgroundColor(R.drawable.trans_car);
		lst_profile.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				// TODO Auto-generated method stub
//				Map mapItm = navlistItem.get(arg2);
				tab_relative.setX(0);
				tab_relative.clearAnimation();
				profile_show = false;
				if(vPager.getCurrentItem() == 0){
					switch(arg2){
					case 0:
//						if(baidu_v != null)
//							baidu_v.geofenceMode = true;
						break;
//					case 1:
//						break;
//					case 2:
//						break;
					case 1:
						if(baidu_v != null){
							baidu_v.searchDlg = new SearchPoiDlg(Base.this, mWidth, mHeight, R.layout.search_poi, R.style.Theme_dialog);
							baidu_v.searchDlg.show();
						}						
						break;
					case 2:
					Intent intent_cityDown = new Intent(Base.this,
							CityDownActivity.class);
					intent_cityDown
							.putExtra("curCity", Base.baidu_v.mCity);
					startActivity(intent_cityDown);
					// if(cityDownDlg == null){
					// cityDownDlg = new CityDownDlg(Base.this, mWidth, mHeight,
					// R.layout.down_whole, R.style.Theme_dialog);
					// }
					// cityDownDlg.show();
						break;
					default:
						break;
					}
				}
				else if(vPager.getCurrentItem() == 1){
					switch(arg2){
					case 0:						
//						createGroupDlg = new GrpCreateDlg(Base.this, 200*mDensityInt, 240*mDensityInt, R.layout.group_create, R.style.Theme_dialog, null);
//						createGroupDlg.show();
						break;
					case 1:
						if(HttpQueue.grpResLst == null){
							Toast.makeText(Base.OBDApp, R.string.group_none, Toast.LENGTH_SHORT).show();
							break;
						}
						delGroupDlg = new GrpDelDlg(Base.this, 320*mDensityInt, 564*mDensityInt, R.layout.group_list, R.style.Theme_dialog, 0, HttpQueue.grpResLst);
						delGroupDlg.show();
						break;
					case 2:
						if(HttpQueue.grpResLst == null){
							Toast.makeText(Base.OBDApp, R.string.group_none, Toast.LENGTH_LONG).show();
							break;
						}
						renameGroupDlg = new GrpRenameDlg(Base.this, 320*mDensityInt, 540*mDensityInt, R.layout.group_list, R.style.Theme_dialog, null);
						renameGroupDlg.show();
						break;
					case 3:		
//						searchGroupDlg = new GrpSearchDlg(Base.this, 200*mDensityInt, 360*mDensityInt, R.layout.group_search, R.style.Theme_dialog, null);
//						searchGroupDlg.show();
						break;
					case 4:	//add group member
						searchUserMode = 0;
						searchUserDlg = new GrpAddMemberDlg(Base.this, 200*mDensityInt, 360*mDensityInt, R.layout.group_search_user, R.style.Theme_dialog);
						searchUserDlg.show();
						break;
					case 5: //add friend
						searchUserMode = 1;
						searchUserDlg = new GrpAddMemberDlg(Base.this, 200*mDensityInt, 360*mDensityInt, R.layout.group_search_user, R.style.Theme_dialog);
						searchUserDlg.show();
						break;
					case 6:			
						//直接在当前群组视图操作
//						if(me_v.vGroupList != null && !me_v.vGroupList.mWithCheck){
//							me_v.vGroupList.enterSelectMode();
//							topBar.setVisibility(View.INVISIBLE);
//						}
						break;
					default:
						break;
					}
				}
			} 
			
		});
		topBar.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if(vPager.getCurrentItem() == 0 || vPager.getCurrentItem() == 1){
					if(!profile_show){
						tab_relative.clearAnimation();
						animator = tab_relative.animate();						
						animator.translationXBy((int)200*mDensity);
						animator.setStartDelay(0);
						animator.setDuration(500);
						animator.start();
					}
					else{
						tab_relative.clearAnimation();
						animator = tab_relative.animate();
						animator.translationXBy((int)-200*mDensity);
						animator.setStartDelay(0);
						animator.setDuration(500);
						animator.start();
					}
					profile_show = !profile_show;
				}
			}
			
		});
		
		OnTouchListener touch_listener = new OnTouchListener(){
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				if(event.getAction() == MotionEvent.ACTION_MOVE)
					return true;
				return true;
			}
			
		};
		vPager.setOnTouchListener(touch_listener);		
		

		vPager.setAdapter(new MyPagerAdapter(listViews));
		
		vPager.setOnPageChangeListener(new OnPageChangeListener(){  
            @Override  
            public void onPageSelected(int position) {  
            	if(position == 0){
					if(baidu_v != null)
						baidu_v.mMapView.onResume();
            	}
            }  
            
            @Override  
            public void onPageScrolled(int arg0, float arg1, int arg2){  
            }  
            
            @Override  
            public void onPageScrollStateChanged(int arg0){  
            }  
        });  		
		
		vPager.setCurrentItem(0);
		
		navi_linear = (LinearLayout) portraiView.findViewById(R.id.relative_navi);
		me_linear = (LinearLayout) portraiView.findViewById(R.id.relative_me);
		car_linear = (LinearLayout) portraiView.findViewById(R.id.relative_car);
	
		View.OnClickListener bottom_click = new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				switch(v.getId()){
					case R.id.relative_navi:
//						info_layout.setVisibility(View.VISIBLE);
						LogRecord.SaveLogInfo2File(Base.OperateInfo, "tab navi icon");
						baidu_v.onBackKeyEnter();
						if(vPager.getCurrentItem() == 0){							
							navitDlg = new NavitInputDlg(Base.this, Base.mWidth,
			                        Base.mHeight, R.layout.sub_navit_p,
			                        R.style.Theme_dialog);
							navitDlg.show();
							return;
						}
						vPager.setCurrentItem(0, false);
//						topBar.setVisibility(View.VISIBLE);
						lst_profile.setAdapter(naviLstAdp);
						break;
						
					case R.id.relative_me:	
						LogRecord.SaveLogInfo2File(Base.OperateInfo, "tab me icon");
//						baidu_v.exitFenceAddMode();
						baidu_v.onBackKeyEnter();
						info_layout.setVisibility(View.GONE);
						if(login_state_check()){
							vPager.setCurrentItem(1, false);
							id_bottombar.setVisibility(View.GONE);
							lst_profile.setAdapter(meLstAdp);
						}
						break;
						
					case R.id.relative_car:
						LogRecord.SaveLogInfo2File(Base.OperateInfo, "tab car icon");
//						baidu_v.exitFenceAddMode();
						baidu_v.onBackKeyEnter();
						info_layout.setVisibility(View.GONE);
						vPager.setCurrentItem(2, false);
						id_bottombar.setVisibility(View.GONE);
//						topBar.setVisibility(View.INVISIBLE);
						break;
						
					default:break;
				}
				tab_relative.setX(0);
				tab_relative.clearAnimation();
				profile_show = false;
			}			
		};
		navi_linear.setOnClickListener(bottom_click);
		me_linear.setOnClickListener(bottom_click);
		car_linear.setOnClickListener(bottom_click);		
	}
	
	
	public void download_offline_map_check(){
		new AlertDialog.Builder(this)
	 	.setTitle("建议下载离线地图")
	 	.setMessage("是否下载全国缩略图和本地地图")
	 	.setPositiveButton("是", 
 			new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface dialog, int which) {
					checkNeedDownloadPriorCityMap(true);
					dialog.cancel();
				}
		 	})
	 	.setNegativeButton("否", 
 			new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface dialog, int which){
					dialog.cancel();
				}
			})								
	 	.show();
		
	}
	public boolean login_state_check(){
		if(login_check != null)
			return false;
		if(Preference.getInstance(getApplicationContext()).getLoginStat())
			return true;

		
		new AlertDialog.Builder(this)
	 	.setTitle(R.string.string_confirm1)
	 	.setMessage("要使用该功能，请先登录！")
	 	.setPositiveButton("确定", 
 			new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface dialog, int which) {
					loginDlg = new Login(Base.this, Base.mWidth, Base.mHeight, R.layout.login, R.style.Theme_dialog);
					loginDlg.show();
					dialog.cancel();
				}
		 	})
	 	.setNegativeButton("取消", 
 			new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface dialog, int which){
					dialog.cancel();
				}
			})								
	 	.show();
		return false;
	}
	
	public static Handler httpQueueHandler = new Handler() {
		public void handleMessage(Message msg) {
			int retVal = 0;
			HttpQueue httpItem;
			
			//int size;
			Bundle bb;
			JSONArray jsonArray;
			JSONObject jsonObj;
			Group group = null;
			ArrayList<Member> list = null;			
			String groupCreator;
			String groupName;
//			String groupDesc;
			String headImgNm = null;
			String userName;
			String resStr = null;
			
			 
			int usrLen;	
			JSONArray usrJsArr;
			String memName;
			Member member;
			int position = -1;
			int len;
			int idx;
			if(Base.OBDApp == null || Base.OBDApp.getActivityBack() != Base.APP_RUN_FOREGROUND){
				return;
			}
//			if(Base.OBDApp.landScapeMode != 0)
//				return;
			bb = msg.getData();
			retVal = (Integer) bb.get(HttpQueue.KEY_TRANSFER_STATUS);
			httpItem = (HttpQueue) bb.get(HttpQueue.KEY_TRANSFER_CLASS);
			try {
				switch (msg.what) {			
					case 11:	
						resStr =  Base.OBDApp.getString(R.string.group_create);
						if(retVal == 1){
							resStr += Base.OBDApp.getString(R.string.success);							
							idx = HttpQueue.grpResLst.size();
							//String creat = Preference.getInstance(Base.OBDApp.getApplicationContext()).getUser();
							list = new ArrayList<Member>();
							Member mem = new Member(Base.loginUser, 1, true);
							list.add(mem);
							groupName = httpItem.param.getString("groupName");
//							groupDesc = httpItem.param.getString("groupDes");
							group = new Group(idx, Base.loginUser, groupName, "", list);
							HttpQueue.grpResLst.add(group);
							//me_v.addGroup(group);	
							if(me_v != null){
//								me_v.grpLstAdpt.setList(HttpQueue.grpResLst);
								me_v.setFrndGrpList();
//								ArrayList<ChatMsgEntity> oneList = new ArrayList<ChatMsgEntity>();
//								me_v.chatMsgLst.add(oneList);
							}
						}
						else{
							resStr += Base.OBDApp.getString(R.string.failed);
							Toast.makeText(Base.OBDApp, "服务器已存在相同群组名，添加失败", Toast.LENGTH_SHORT).show();
						}
						break;
					case 12:
						resStr =  Base.OBDApp.getString(R.string.group_del);					
						groupName = httpItem.param.getString("groupName");												
						position = Group.indexOfByName(HttpQueue.grpResLst,groupName);
						if(position >= 0){
							HttpQueue.grpResLst.remove(position);
							//me_v.vGroupList.remove(position);
							if(me_v != null){								
								if(friendOrGrpIdx - HttpQueue.friendLst.size() == position){
									if(baidu_v.isGrpShareMode){
										baidu_v.exitGpsShareMode();
									}
									if(Base.me_v.grpDetailDlg != null){
										Base.me_v.grpDetailDlg.cancel();
									}
								}
								me_v.setFrndGrpList();
							}
						}
						if(retVal == 1){
							resStr += Base.OBDApp.getString(R.string.success);
						}
						else{
							resStr += Base.OBDApp.getString(R.string.failed);
						}
						Toast.makeText(Base.OBDApp, resStr, Toast.LENGTH_LONG).show();
						break;
					case 13:
						resStr =  Base.OBDApp.getString(R.string.group_modify);
						String oldName = "";
						String newName = "";
						if(retVal == 1){							
							oldName = httpItem.param.getString("oldGroupName");	
							newName = httpItem.param.getString("newGroupName");							
							resStr += Base.OBDApp.getString(R.string.success);							
							position = Group.indexOfByName(HttpQueue.grpResLst,oldName);
							HttpQueue.grpResLst.get(position).name = newName;
							if(me_v != null){
								me_v.setFrndGrpList();
							}
						}
						else{
							resStr += Base.OBDApp.getString(R.string.failed);
						}
						Toast.makeText(Base.OBDApp, resStr, Toast.LENGTH_LONG).show();					
						break;
						
					case 14://find group conditionally	
						Base.me_v.waitProgress.setVisibility(View.INVISIBLE);
						if(retVal != 1){
							Toast.makeText(Base.OBDApp, "没有找到符合条件的群组", Toast.LENGTH_SHORT).show();
							break;
						}
						jsonArray = new JSONArray(httpItem.resultStr);						 
						len = jsonArray.length();
						if(HttpQueue.grpSearchLst == null)
							HttpQueue.grpSearchLst = new ArrayList<Group>();
						else
							HttpQueue.grpSearchLst.clear();
						for(int i = 0; i < len; i++){
							jsonObj = jsonArray.getJSONObject(i);						
							groupName = jsonObj.getString("groupName");
							groupCreator = jsonObj.getString("createUserName");
							groupName = URLDecoder.decode(groupName, "UTF-8");							
							group = new Group(i, jsonObj.getString("createUserName"), groupName,
									jsonObj.getString("groupDes"), null);
							if(HttpQueue.grpResLst == null || !HttpQueue.grpResLst.contains(group)){
								HttpQueue.grpSearchLst.add(group);
//								me_v.addGroup(group);
							}
						}	
						if(OBDApp.baseAct != null && me_v != null){
							me_v.selGroupDlg = new GrpDelDlg(OBDApp.baseAct, 320*mDensityInt, 564*mDensityInt, R.layout.group_list, R.style.Theme_dialog, 1, HttpQueue.grpSearchLst);
							me_v.selGroupDlg.show();
						}
						break;
						
					case 141://not list member
					case 142://list member	
						if(retVal != 1)
							break;
						jsonArray = new JSONArray(httpItem.resultStr);
						len = jsonArray.length();
						if(HttpQueue.grpResLst == null)
							HttpQueue.grpResLst = new ArrayList<Group>();
						else
							HttpQueue.grpResLst.clear();
						for(int i = 0; i < len; i++){
							jsonObj = jsonArray.getJSONObject(i);
							
							groupName = jsonObj.getString("groupName");
							groupCreator = jsonObj.getString("createUserName");
							groupName = URLDecoder.decode(groupName, "UTF-8");
							
							
							if(msg.what == 142){
								usrJsArr = jsonObj.getJSONArray("users");
								usrLen = usrJsArr.length();								
								ArrayList<Member> tmplist  = new ArrayList<Member>();
								for(int j = 0; j < usrLen; j++){
									JSONObject obj = usrJsArr.getJSONObject(j);
									memName = obj.getString("user");
									headImgNm = obj.getString("image");
									Member mem;
									if(groupCreator.equals(memName))
										mem = new Member(memName, obj.getInt("online"), true, headImgNm);
									else
										mem = new Member(memName, obj.getInt("online"), false, headImgNm);
									if(memName.equals(Base.loginUser))
										tmplist.add(0, mem);
									else
										tmplist.add(mem);
								}
								list = tmplist;
							}
							else{
								list = null;
							}
							groupCreator = jsonObj.getString("createUserName");
							group = new Group(i, groupCreator, groupName,
									jsonObj.getString("groupDes"), list);
							
							if(HttpQueue.grpCreatorLst == null)
								HttpQueue.grpCreatorLst = new ArrayList<Group>();
							else
								HttpQueue.grpCreatorLst.clear();
														
							if(groupCreator.equals(Base.loginUser)){
								HttpQueue.grpCreatorLst.add(group);
							}
							
							HttpQueue.grpResLst.add(group);
//							httpItem.url = Base.HTTP_GROUP_PATH+"/listMembers?appID=appid&groupName="+group.name;
//							httpItem.option = 17;
//							httpItem.grpIdx = i;
//							httpQueueInstance.EnQueue(httpItem.url, null, httpItem.option); 
						}						
						if(msg.what == 142){
							//me_v.initViewPager();	
							//me_v.grpLstAdpt.setList(HttpQueue.grpResLst);
							if(me_v != null){
								me_v.setFrndGrpList();
							}
							if(Base.OBDApp.baseAct.chatOrAgreeNotProc){
								Base.OBDApp.baseAct.chatOrAgreeNotProc = false;
								Base.OBDApp.baseAct.processExtraData();
							}
						}
						break;
					case 15:
						resStr =  Base.OBDApp.getString(R.string.group_join);
						if(retVal == 1){	
							jsonObj = new JSONObject(httpItem.resultStr);
							resStr += Base.OBDApp.getString(R.string.success);
							idx = HttpQueue.grpResLst.size();							
							groupCreator = jsonObj.getString("createUserName");
							if(!jsonObj.has("users"))
							{
								list = new ArrayList<Member>();
								Member mem = new Member(Base.loginUser, 1, true);
								list.add(mem);
								groupName = httpItem.param.getString("groupName");
	//							groupDesc = httpItem.param.getString("groupDes");
								
							}
							else{
								usrJsArr = jsonObj.getJSONArray("users");
								usrLen = usrJsArr.length();	
								ArrayList<Member> tmplist  = new ArrayList<Member>();
								for(int j = 0; j < usrLen; j++){
									JSONObject obj = usrJsArr.getJSONObject(j);
									memName = obj.getString("user");
									Member mem;
									headImgNm = obj.getString("image");
									if(groupCreator.equals(memName))
										mem = new Member(memName, obj.getInt("online"), true, headImgNm);
									else
										mem = new Member(memName, obj.getInt("online"), false, headImgNm);
									if(memName.equals(Base.loginUser))
										tmplist.add(0, mem);
									else
										tmplist.add(mem);
								}
								list = tmplist;
							}
							group = new Group(idx, jsonObj.getString("createUserName"), jsonObj.getString("groupName"), "", list);
							HttpQueue.grpResLst.add(group);	
							if(me_v != null){
								me_v.setFrndGrpList();
							}						
						}
						else{
							resStr += Base.OBDApp.getString(R.string.failed);
						}
						Toast.makeText(Base.OBDApp, resStr, Toast.LENGTH_LONG).show();
						break;
					case 16:
						resStr =  Base.OBDApp.getString(R.string.group_del_user);
						if(retVal == 1){
							boolean delGroup = false;
							jsonObj = new JSONObject(httpItem.resultStr);
							groupCreator = jsonObj.getString("createUserName");
							groupName = jsonObj.getString("groupName");
							jsonArray = jsonObj.getJSONArray("users");
							usrLen = jsonArray.length();
							do{								
								if(groupCreator.equals(Base.loginUser)){//				
									if(usrLen == 0){
										delGroup = true;
										break;
									}
									
								}
								else{
									int i;
									for(i = 0; i < usrLen; i++){
										JSONObject obj = jsonArray.getJSONObject(i);
										userName = obj.getString("user");
										if(userName.equals(Base.loginUser)){											
											break;
										}
									}
									if(i == usrLen){
										delGroup = true;
										break;
									}
								}
							}while(false);
							idx = HttpQueue.grpResLst.indexOf(new Group(groupName));
							if(idx == -1){
								return;
							}
							if(delGroup){
								//me_v.vGroupList.remove(idx);

								if(friendOrGrpIdx - HttpQueue.friendLst.size() == idx){
									if(baidu_v.isGrpShareMode){
										baidu_v.exitGpsShareMode();
									}
									if(Base.me_v.grpDetailDlg != null){
										Base.me_v.grpDetailDlg.cancel();
									}
								}
								HttpQueue.grpResLst.remove(idx);
								me_v.setFrndGrpList();
							}
							else{
								Group procGrp = HttpQueue.grpResLst.get(idx);
								ArrayList<Member> tmplist  = new ArrayList<Member>();
								usrLen = jsonArray.length();	
								for(int j = 0; j < usrLen; j++){
									JSONObject obj = jsonArray.getJSONObject(j);
									memName = obj.getString("user");
									Member mem;
									if(groupCreator.equals(memName))
										mem = new Member(memName, obj.getInt("online"), true);
									else
										mem = new Member(memName, obj.getInt("online"), false);
									tmplist.add(mem);
								}
								procGrp.memberList.clear();
								procGrp.memberList = tmplist;
								if(procGrp.grpHead != null)
								{
									procGrp.grpHead.recycle();
								}
								procGrp.grpHead = null;
								//me_v.vGroupList.set(idx, 0, null, tmplist);
								me_v.setFrndGrpList();
								if(friendOrGrpIdx - HttpQueue.friendLst.size() == idx){
									if(baidu_v.isGrpShareMode){
										if(baidu_v.honAdapter != null)
											baidu_v.honAdapter.refreshGrpList();
									}
									if(Base.me_v.grpDetailDlg != null){
										Base.me_v.grpDetailDlg.adapter.refreshGrpList();
									}
								}
							}
							resStr +=  Base.OBDApp.getString(R.string.success);
							
						}
						else{
							resStr +=  Base.OBDApp.getString(R.string.failed);
						}
						Toast.makeText(Base.OBDApp, resStr, Toast.LENGTH_SHORT).show();
						//me_v.vGroupList.exitSelectMode();
//						if(Base.OBDApp.baseAct.topBar != null)
//							Base.OBDApp.baseAct.topBar.setVisibility(View.VISIBLE);
						break;
						
					case 17:					
						list = new ArrayList<Member>();												
						jsonObj = new JSONObject(httpItem.resultStr);
						groupName = jsonObj.getString("groupName");
						try {
							groupName = URLDecoder.decode(groupName, "UTF-8");
						} catch (UnsupportedEncodingException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						jsonArray = jsonObj.getJSONArray("users");
						len = jsonArray.length();
						for(int i = 0; i < len; i++){
							JSONObject obj = jsonArray.getJSONObject(i);
							Member mem = new Member(obj.getString("user"), obj.getInt("online"), false);
							list.add(mem);
						}
//						group = HttpQueue.grpResLst.get(httpItem.grpIdx);
						for(Group ele : HttpQueue.grpResLst){
							if(ele.name.equals(groupName)){
								group = ele;
								break;
							}
						}
					
//						if(group != null){
//							group.memberList =  list;							
//							if(group.groupID == HttpQueue.grpResLst.size() - 1){
//								me_v.initViewPager();
//							}
//						}												
						break;
					case 18:
						Base.me_v.waitProgress.setVisibility(View.INVISIBLE);
						if(retVal != 1){
							Toast.makeText(Base.OBDApp, "没有找到符合条件的人", Toast.LENGTH_SHORT).show();
							break;
						}
						if(OBDApp.baseAct == null)
							return;
						Group grp = me_v.getCurrentGrp();
						jsonArray = new JSONArray(httpItem.resultStr);						 
						len = jsonArray.length();
						if(HttpQueue.grpSearchMemberLst == null)
							HttpQueue.grpSearchMemberLst = new ArrayList<Member>();
						else
							HttpQueue.grpSearchMemberLst.clear();
						for(int i = 0; i < len; i++){
							jsonObj = jsonArray.getJSONObject(i);						
							userName = jsonObj.getString("user");	
							Member mem = new Member(userName, 0, false);
							
							if(grp != null && grp.memberList.contains(mem))
								continue;
							else
								HttpQueue.grpSearchMemberLst.add(mem);
						}												
						me_v.selMemDlg = new GrpDelDlg(OBDApp.baseAct, 320*mDensityInt, 564*mDensityInt, R.layout.group_list, R.style.Theme_dialog, 2, HttpQueue.grpSearchMemberLst);
						me_v.selMemDlg.show();
						break;
						
					case 20:
						if(retVal == 1){
							ChatMsgEntity msgEnti = httpItem.msgEntity;
//							if(me_v != null && me_v.vGroupList.chagDlg != null){
//								me_v.vGroupList.chagDlg.myList.add(msgEnti);
//								me_v.vGroupList.chagDlg.myAdapter.notifyDataSetChanged();
//							}
							if(me_v != null){
//								me_v.processChatMsg(msgEnti);
							}
						}
						else{
								if(baidu_v.needFailMsg){
									baidu_v.needFailMsg = false;
									Toast.makeText(Base.OBDApp, "发送消息失败！", Toast.LENGTH_SHORT).show();
								}
							}
						break;
					case 51:			
						jsonObj = new JSONObject(httpItem.resultStr);
						headImgNm = jsonObj.getString("image");
						member = new Member(jsonObj.getString("username"), jsonObj.getString("alias"), jsonObj.getInt("online"), headImgNm);
						if(!HttpQueue.friendLst.contains(member)){
							HttpQueue.friendLst.add(member);
						}
						if(me_v != null){
							me_v.setFrndGrpList();
						}
						break;
					case 52://list friend
						String nickName = "";
						int online;
						if(retVal != 1)
							break;
						jsonArray = new JSONArray(httpItem.resultStr);						 
						len = jsonArray.length();
						if(HttpQueue.friendLst == null)
							HttpQueue.friendLst = new ArrayList<Member>();
						else
							HttpQueue.friendLst.clear();
						for(int i = 0; i < len; i++){
							jsonObj = (JSONObject) jsonArray.get(i);
							userName = jsonObj.getString("username");	
							nickName = jsonObj.getString("alias");
							online = jsonObj.getInt("online");
							headImgNm = jsonObj.getString("image");
							member = new Member(userName, nickName, online, headImgNm);
							HttpQueue.friendLst.add(member);
						}	
						if(me_v != null){
							me_v.setFrndGrpList();						
						}
						break;
						
					case 53://delete friend
						if(retVal != 1){
							Toast.makeText(Base.OBDApp, "删除失败，该好友可能已不在服务器好友列表", Toast.LENGTH_SHORT).show();
							break;
						}
						//jsonObj = new JSONObject(httpItem.resultStr);
						jsonArray = new JSONArray(httpItem.resultStr);						 
						len = jsonArray.length();
						//int lenOld = HttpQueue.friendLst.size();
						ArrayList<Member> newFriendLst = new ArrayList<Member>();
						idx = 0;
						for(int i = 0; i < len; i++){
							jsonObj = (JSONObject) jsonArray.get(i);
							userName = jsonObj.getString("username");	
							nickName = jsonObj.getString("alias");
							online = jsonObj.getInt("online");
							headImgNm = jsonObj.getString("image");
							member = new Member(userName, nickName, online, headImgNm);
							newFriendLst.add(member);
						}
						
						
						
						if(me_v != null){
							JSONArray delusers = httpItem.param.getJSONArray("users");
							String deluser = delusers.getString(0);
							position = HttpQueue.friendLst.indexOf(new Member(deluser));
							
							if(position != -1 && friendOrGrpIdx - HttpQueue.friendLst.size() == position){
								if(baidu_v.isGrpShareMode){
									baidu_v.exitGpsShareMode();
								}
								if(Base.me_v.grpDetailDlg != null){
									Base.me_v.grpDetailDlg.cancel();
								}
							}
							HttpQueue.friendLst = newFriendLst;
							me_v.setFrndGrpList();
						}
						
						Toast.makeText(Base.OBDApp, "删除成功", Toast.LENGTH_SHORT).show();
						break;
						
					default:break;
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			finally{
				
			}
		}
	};
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//    	if(vPager.getCurrentItem() == 1){
//	    	super.onCreateOptionsMenu(menu);
//	        menu.add(Menu.NONE, Menu.FIRST + 1, 1, "Setting").setIcon(android.R.drawable.ic_menu_info_details);
//	        menu.add(Menu.NONE, Menu.FIRST + 2, 2, "GroupManagment").setIcon(android.R.drawable.ic_menu_info_details);
//	        menu.add(Menu.NONE, Menu.FIRST + 3, 3, "Exit App").setIcon(android.R.drawable.ic_menu_info_details);
//    	}
        return true;
    }

    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        	// Button Way
            case Menu.FIRST + 1:
                break;
            // Setting Way: start group management activity and wait for the response from the group management activity;
            case Menu.FIRST + 2:
                break;
            // Exit app
            case Menu.FIRST + 3:
            	break;
        }
        return false;
    }
    
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {  
        // TODO Auto-generated method stub   
    	 if (resultCode != RESULT_OK) {  
             return;  
         } else {  
             switch (requestCode) {  
             case IMAGE_REQUEST_CODE:  
            	 if(registDlg != null)
            		 registDlg.resizeImage(data.getData());  
            	 else if(editAccountDlg != null)
            		 editAccountDlg.resizeImage(data.getData()); 
                 break;  
             case CAMERA_REQUEST_CODE:              	             	 
                 if (Base.getSDPath()!=null) {  
                	 if(registDlg != null)
                		 registDlg.resizeImage(Register.getImageUri());
            		 else if(editAccountDlg != null)
            			 editAccountDlg.resizeImage(Register.getImageUri());
                 } else {  
                     Toast.makeText(Base.this, "未找到存储卡，无法存储照片！",  
                             Toast.LENGTH_LONG).show();  
                 }              	 
                 break;  
   
             case RESIZE_REQUEST_CODE:             	 
                 if (data != null) { 
                	 if(registDlg != null)
                		 registDlg.showResizeImage(data);  
            		 else if(editAccountDlg != null)
            			 editAccountDlg.showResizeImage(data); 
                 }              	 
                 break;  
             }  
         }  
        super.onActivityResult(requestCode, resultCode, data);  
    } 
    
	public void uploadVoltage(String vol){
		String url = HTTP_ROOT_PATH + "/services/voltage";
		Map<String, String> param = new HashMap<String, String>();
		String plate = Preference.getInstance(Base.this.getApplicationContext()).getLicence();
		//String voltage = Preference.getInstance(Base.this.getApplicationContext()).getVoltage();
		String timestamp = Long.toString(System.currentTimeMillis());
		
		param.put("plate", plate);
		param.put("voltage", vol);
		param.put("timestamp", timestamp);
		
		
		CacheManager.getJson(this, url,  new IHttpCallback() {
				
				@Override
				public void handle(int retCode, Object response) {
					// TODO Auto-generated method stub		
					if(retCode == 200){
						generateBatteryInfo();
					}
				}
			}, null);
		
//		OBDApp.httpConnect = new HttpThread(Base.this, httpHandler, url, param, 5);
//		OBDApp.httpConnect.startHttp();
	}
	
	public void getVoltageHistory(){
		Calendar cal = Calendar.getInstance();
		
		int y = cal.get(Calendar.YEAR);    
		int m = cal.get(Calendar.MONTH);    
		int d = cal.get(Calendar.DATE);//test 
		String url = HTTP_ROOT_PATH + "/services/voltageHistory";
		//JSONObject param = new JSONObject();
		String plate = Preference.getInstance(Base.this.getApplicationContext()).getLicence();
		String date = "" + y + "-" + m + "-" + d;
		
		if(m > 1)
			m--;
		else{
			y--;
			m = 12;
		}
//		try {
//			param.put("plate", plate);
//			param.put("date", date);
//		} catch (JSONException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		url += "?plate=" + plate + "&date=" + date;
		
		CacheManager.getJson(this, url,  new IHttpCallback() {
			
			@Override
			public void handle(int retCode, Object response) {
				// TODO Auto-generated method stub		
				if(retCode == 200){
//					getVoltageHistory();															
				}
				else{
				
				}
				
			}
		}, null);
//		OBDApp.httpConnect = new HttpThread(Base.this, httpHandler, url, null, 6);
//		OBDApp.httpConnect.startHttp();
	}
	
	public void promptTrackEnableDialog(){
		if(true)
			return;
		OBDApp.gpsEnablePrompt = true;
		if(trackEnableDialog == null){
			trackEnableDialog = new AlertDialog.Builder(this).create();
			//trackEnableDialog.setIcon(android.R.drawable.);
			trackEnableDialog.setTitle(R.string.location_enable_title);
			trackEnableDialog.setMessage(context.getString(R.string.location_enable_msg));
			trackEnableDialog.setButton(DialogInterface.BUTTON_NEGATIVE, context.getString(R.string.string_cancel).toString(),
					new DialogInterface.OnClickListener(){
				public void onClick(DialogInterface dialog,int i){	
					try {
						Field field = dialog.getClass().getSuperclass()
								.getDeclaredField("mShowing");
						field.setAccessible(true);
						field.set(dialog, true);
					} catch (Exception e) {
						e.printStackTrace();
					}
					trackEnableDialog.cancel();
			}
		});
		
			trackEnableDialog.setButton(DialogInterface.BUTTON_POSITIVE, context.getString(R.string.string_confirm).toString(),
					new DialogInterface.OnClickListener(){
				public void onClick(DialogInterface dialog,int i){						
			    	Intent intent = new Intent();
			    	intent.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
			    	intent.setFlags(intent.getFlags() | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
			    	Base.this.startActivity(intent);

					try {
						Field field = dialog.getClass().getSuperclass()
								.getDeclaredField("mShowing");
						field.setAccessible(true);
						field.set(dialog, false);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
		}
		trackEnableDialog.show();
		gpsSwitchOn = true;
	}
	
	void testBatterInfo(){
		int i = 0;
		Calendar cal = Calendar.getInstance();
		String volArr[] = {"12.8", "12", "11.3"};
		VoltageHistory his;
		
		if(volHistory == null)
			volHistory = new ArrayList<VoltageHistory>();
		for(i = 0; i < 3; i++){
			cal.set(Calendar.DATE, cal.get(Calendar.DATE)-i);
			cal.set(Calendar.HOUR, cal.get(Calendar.HOUR)+i);
			his = new VoltageHistory("Harman1234", volArr[i], Long.toString(cal.getTimeInMillis()));
			volHistory.add(his);
		}
		generateBatteryInfo();
		
	}
	
	public void generateBatteryInfo(){
//		String curVol = setting_s.volVal.getText().toString();
		
		//if(curVol == null || curVol.equals(""))
		//	return;
		
		long curTime = System.currentTimeMillis();
		if(volHistory == null)
			return;
		int len = volHistory.size();
		int i = 0;
		int j = 0;
		if(len < 3)
			return;
		

		float volArr[] = new float[4];
		long timeArr[] = new long[4];
		
		String curVolDigit = "";
		String preItem=null;
		Float maxium = 0.1f;
//		if(curVol != null && curVol.length() > 1){
//			curVolDigit = curVol.substring(0, curVol.length()-1);
//			maxium = Float.parseFloat(curVolDigit);
//		}
		
		for(i = 0; i < len; i++){
			preItem = volHistory.get(len-1-i).voltage;			
			if(Float.parseFloat(preItem) > maxium){	
				maxium = Float.parseFloat(preItem);
				volArr[j] = maxium;
				timeArr[j] = Long.parseLong(volHistory.get(len-1-i).timeStamp);				
				j++;
				if(j > 3)
					break;
			}
			
			//maxium = Float.parseFloat(preItem);
			//curTime = Long.parseLong(volHistory.get(len-1-i).timeStamp);
			
		}
		double a[] = new double[j];
		double b[] = new double[j];
		for(i = 0; i < j; i++){
			a[i] = volArr[i];
			b[i] = (timeArr[i]);
		}
		if(i == 0 || j == 0)
			return;
		if(a[0]-a[1] > 0.001 || a[0]-a[1] < -0.001){
			double volExp = (b[1]-b[0])/1000/(a[0]-a[1]);
			if(volExp < 300000)
				volExp = 300000;
			Preference.getInstance(getApplicationContext()).setVolExp(Double.doubleToLongBits(volExp));
		}

	}
	
	public static void hideSoftKeyboard(Activity context) {
        InputMethodManager inputManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputManager != null){
	        inputManager.hideSoftInputFromWindow(context.getWindow().getDecorView().getApplicationWindowToken(), 0);
	        context.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        }
    }
	/**
	 * 
	 */
	public synchronized static String getCommData(){
		return commData;
	}
	
	/**
	 * 
	 * @param value
	 */
	public synchronized static void setCommData(String value){
		commData = value;
	}

	public static boolean isBackground(Context context){
	    ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
	    List<RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
	    for(RunningAppProcessInfo appProcess : appProcesses){
	    	if(appProcess.processName.equals(context.getPackageName())){
	    		if(appProcess.importance == RunningAppProcessInfo.IMPORTANCE_BACKGROUND){
	    			Log.i(TAG, "base_background true");
                    return true;
	            }else{	
	            	Log.i(TAG, "base_background false");
	                return false;
	            }
           }
	    }
	    return false;
	}
	
	public static boolean isForeground(Context context){
	    ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
	    List<RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
	    for(RunningAppProcessInfo appProcess : appProcesses){
	    	if(appProcess.processName.equals(context.getPackageName())){
	    		if(appProcess.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND){
	    			Log.i(TAG, "base_foreground true");
                    return true;
	            }else{	
	            	Log.i(TAG, "base_foreground false");
	                return false;
	            }
           }
	    }
	    return false;
	}
	
	public static void initOBDRootPath(){
		String obdii_path;
		if(getSDPath() != null){
			obdii_path = getSDPath() +"/OBDII";
			File obd_dir = new File(obdii_path);
			boolean ret = false; 
			if(!obd_dir.exists())
				ret = obd_dir.mkdir();
		}
	}
	
	public static String getSDPath(){ 
	       File sdDir = null; 
	       boolean sdCardExist = Environment.getExternalStorageState()   
	                           .equals(Environment.MEDIA_MOUNTED);   //
	       if(sdCardExist)   
	       {                               
	         sdDir = Environment.getExternalStorageDirectory();//
	       } 
	       if(sdDir == null)
	    	   return null;
	       return sdDir.toString();     
	}
	
	public int getNewWorkType(){
		ConnectivityManager manager = (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE); 
		boolean flag = false;
		if (manager != null && manager.getActiveNetworkInfo() != null) {
			flag = manager.getActiveNetworkInfo().isAvailable();
			
			if(flag){
				NetworkInfo networkinfo = manager.getActiveNetworkInfo();  
				
		        if (networkinfo != null && networkinfo.isAvailable()) {  
		    	   return  networkinfo.getType();
		        }  
			}
		}
		return -1;
	}
	
	private BroadcastReceiver mNetworkReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                Log.d("mark", "");
                ConnectivityManager connectivityManager = (ConnectivityManager)      
                                         getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo info = connectivityManager.getActiveNetworkInfo();  
                if(info != null && info.isAvailable()) {
                    String name = info.getTypeName();
                } else {

                }
            }
        }
    };

	
	public static boolean CheckNetwork(Context context) { 
		boolean flag = false; 
		ConnectivityManager manager  = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE); 
		
		if (manager != null && manager.getActiveNetworkInfo() != null) {
			flag = manager.getActiveNetworkInfo().isAvailable();
			
			if(flag){
				NetworkInfo networkinfo = manager.getActiveNetworkInfo();  
				
		        if (networkinfo == null || !networkinfo.isAvailable()) {  
		    	   flag =  false;  
		        }  
			}
		}
		return flag;
	}

	public long getAvailMemory() {// 
	  
	    ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);  
	    MemoryInfo mi = new MemoryInfo();  
	    am.getMemoryInfo(mi);  
	    //mi.availMem; 
	  
	        return  mi.availMem;// 
	}
	  
	public void enableBodyCheck(){
		 if(carbody_rela == null)
			  return;
		  TextView textpower = (TextView)carbody_rela.findViewById(R.id.pdtcs);
		  TextView textbody = (TextView)carbody_rela.findViewById(R.id.bdtcs);
		  TextView textchassis = (TextView)carbody_rela.findViewById(R.id.cdtcs);
		  TextView textunet = (TextView)carbody_rela.findViewById(R.id.udtcs);
		  textpower.setVisibility(0);
		  textbody.setVisibility(0);
		  textchassis.setVisibility(0);
		  textunet.setVisibility(0);		  
	  }
	  
		public void CallDialog(boolean selectB){
			String call_num = "";
			if(CallDialog == null){
				CallDialog = new AlertDialog.Builder(this).create();
				if(selectB){
					CallDialog.setTitle(R.string.bcall_title);
					call_num = Preference.getInstance(getApplicationContext()).getBcall();	
					callType = true;
				}
				else{
					CallDialog.setTitle(R.string.ecall_title);
					call_num = Preference.getInstance(getApplicationContext()).getEcall();
					callType = false;
				}
				CallDialog.setMessage(call_num);
				CallDialog.setButton(DialogInterface.BUTTON_NEGATIVE, context.getString(R.string.string_cancel).toString(),
						new DialogInterface.OnClickListener(){
					public void onClick(DialogInterface dialog,int i){	
						try {

						} catch (Exception e) {
							e.printStackTrace();
						}
						CallDialog.cancel();
				}
			});
			
				CallDialog.setButton(DialogInterface.BUTTON_POSITIVE, context.getString(R.string.string_dial).toString(),
						new DialogInterface.OnClickListener(){
					public void onClick(DialogInterface dialog,int i){
						String callnumber;
						if(callType)
							callnumber = Preference.getInstance(Base.this.getApplicationContext()).getBcall();
						else
							callnumber = Preference.getInstance(Base.this.getApplicationContext()).getEcall();
				    	Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + callnumber));
				    	startActivity(intent);
						try {
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});
			}
			CallDialog.show();
	  }

	  public void disableBodyCheck(){
		  if(carbody_rela == null)
			  return;
		  TextView textpower = (TextView)carbody_rela.findViewById(R.id.pdtcs);
		  TextView textbody = (TextView)carbody_rela.findViewById(R.id.bdtcs);
		  TextView textchassis = (TextView)carbody_rela.findViewById(R.id.cdtcs);
		  TextView textunet = (TextView)carbody_rela.findViewById(R.id.udtcs);
		  textpower.setVisibility(4);
		  textbody.setVisibility(4);
		  textchassis.setVisibility(4);
		  textunet.setVisibility(4);		  
	  }
	  
	  public void vehicleBodyCheck(boolean checkret, boolean power, boolean body,boolean chassis, boolean unet){
		   //initBodyChek();
		   //true means "no malfunction";
		  if(carbody_rela == null)
			  return;
		  
		  ImageView checkpower = (ImageView)carbody_rela.findViewById(R.id.pdtcs_pic);
		  ImageView checkbody = (ImageView)carbody_rela.findViewById(R.id.bdtcs_pic);
		  ImageView checkchassis = (ImageView)carbody_rela.findViewById(R.id.cdtcs_pic);
		  ImageView checkunet = (ImageView)carbody_rela.findViewById(R.id.udtcs_pic);
		   if(checkret){			   
		   //0: visible; 4: invisible; 8: gone
			   checkpower.setVisibility(4);
			   checkbody.setVisibility(4);
			   checkchassis.setVisibility(4);
			   checkunet.setVisibility(4);  				   
			   				   
		   }
		   else
		   {	
			   //true means "no malfunction"
			   enableBodyCheck();
			   checkpower.setVisibility(0);
			   checkbody.setVisibility(0);
			   checkchassis.setVisibility(0);
			   checkunet.setVisibility(0);
			   if(power){
				   checkpower.setImageResource(R.drawable.btn_check_on);  
			   }
			   else{
				   checkpower.setImageResource(R.drawable.btn_check_off);				   
			   }
			   
			   if(body){
				   checkbody.setImageResource(R.drawable.btn_check_on);				   				   
			   }
			   else
			   {
				   checkbody.setImageResource(R.drawable.btn_check_off);   
			   }
			   
			   if(chassis)
			   {
				   checkchassis.setImageResource(R.drawable.btn_check_on);
			   }
			   else{   
				   checkchassis.setImageResource(R.drawable.btn_check_off);					   
			   }
			   
			   if(unet){
				   checkunet.setImageResource(R.drawable.btn_check_on);
			   }
			   else{					   
				   checkunet.setImageResource(R.drawable.btn_check_off);
			   }
			   //CallDialog(true);
			   			   			   
		   }
	  }
	  
	  public void initBodyChek(){
		  	carbody_rela = (RelativeLayout) View.inflate(context, R.layout.body_check, null);
		  	//dtc_c.addView(carbody_rela);
		  	disableBodyCheck();
	  }
	  
	  
	  public final Handler baseHandler = new Handler(){
		  public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				Bundle bundle = msg.getData();
				switch (msg.what) {
				case REGISTER:
					gpsNotification();
					break;
				case UNLOGIN:	
					if(Base.this.setting_s != null)
						Base.this.setting_s.setSettingLoginState();					
					break;
				case LOGIN:
					if(Base.this.setting_s != null)
						Base.this.setting_s.setSettingLoginState();
					gpsNotification();
					break;	
				case IGNORE:
					gpsNotification();
					break;					
				case MAP:	
	    			Intent startmap = new Intent(Base.this, BaiduMapView.class);
	    			startActivity(startmap);
	    			break;
				case UNSUPPORT:
					supportNotice();
					String rootpath = myUpLoadLog.getSDPath();
					String directory = rootpath + "/OBDII/crash/";
					myUpLoadLog.UploadFiles(directory);
					directory = rootpath + Base.ycblog;
					myUpLoadLog.UploadFiles(directory);					
					break;										
				default:
					break;
				}
		  }		  
	  };
	  
	public void gpsNotification() {
//		gpsEnable = locationManager
//				.isProviderEnabled(LocationManager.GPS_PROVIDER);
		// boolean gpsEnable =
		// Preference.getInstance(getApplicationContext()).getGpsMonitor();
//		if (!gpsEnable && OBDApp.isBackRun_history != Base.APP_RUN_BACKGROUND) {
//			promptTrackEnableDialog();
//		}
	}

	public void supportNotice() {
		supportAlertDialog = new AlertDialog.Builder(this).create();
		supportAlertDialog.setTitle(R.string.vehicle_support);
		supportAlertDialog.setMessage(context.getString(R.string.retry_notice));

		supportAlertDialog.setButton(DialogInterface.BUTTON_POSITIVE, context
				.getString(R.string.close_notice).toString(),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int i) {
						// finish();
						dialog.cancel();
					}
				});
		supportAlertDialog.show();

	}

	public static boolean isApplicationBackground(final Context context) {
		ActivityManager am = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningTaskInfo> tasks = am.getRunningTasks(1);
		if (!tasks.isEmpty()) {
			ComponentName topActivity = tasks.get(0).topActivity;
			if (!topActivity.getPackageName().equals(context.getPackageName())) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean isApplicationForeground(final Context context) {
		ActivityManager am = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningTaskInfo> tasks = am.getRunningTasks(1);
		if (!tasks.isEmpty()) {
			ComponentName topActivity = tasks.get(0).topActivity;
			if (topActivity.getPackageName().equals(context.getPackageName())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void onGetOfflineMapState(int arg0, int arg1) {
		// TODO Auto-generated method stub

	}

	public static int getStatusBarHeight() {
		return Resources.getSystem().getDimensionPixelSize(
				Resources.getSystem().getIdentifier("status_bar_height",
						"dimen", "android"));
	}
	
	public static class MyBitmapEntity {
		public float x;
		public float y;
		public float width;
		public float height;
		static int devide = 1;
		int index = -1;

		@Override
		public String toString() {
			return "MyBitmap [x=" + x + ", y=" + y + ", width=" + width
					+ ", height=" + height + ", devide=" + devide + ", index="
					+ index + "]";
		}
	}
	
	public static List<MyBitmapEntity> getBitmapEntitys(int count) {
		List<MyBitmapEntity> mList = new LinkedList<MyBitmapEntity>();
		String value = PropertiesUtil.readData(Base.OBDApp, String.valueOf(count),
				R.raw.data);
		LogUtil.d("value=>" + value);
		String[] arr1 = value.split(";");
		int length = arr1.length;
		for (int i = 0; i < length; i++) {
			String content = arr1[i];
			String[] arr2 = content.split(",");
			MyBitmapEntity entity = null;
			for (int j = 0; j < arr2.length; j++) {
				entity = new MyBitmapEntity();
				entity.x = Float.valueOf(arr2[0]);
				entity.y = Float.valueOf(arr2[1]);
				entity.width = Float.valueOf(arr2[2]);
				entity.height = Float.valueOf(arr2[3]);
			}
			mList.add(entity);
		}
		return mList;
	}


}
