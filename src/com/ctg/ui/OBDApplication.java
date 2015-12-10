package com.ctg.ui;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.baidu.location.GeofenceClient;
import com.baidu.location.LocationClient;
import com.ctg.bluetooth.BluetoothService;
import com.ctg.crash.LogRecord;
import com.ctg.crash.UpLoadLog;
import com.ctg.group.Group;
import com.ctg.group.Member;
//import com.ctg.net.HttpThread;
import com.ctg.sensor.OBDSensor;
import com.ctg.util.Preference;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.widget.TextView;

public class OBDApplication extends Application{
	private int backRunFocusId;
	private String obdName;
//	public HttpThread httpConnect = null;
	public boolean hasEntered = false;
	//public boolean login_stat;
	public int downApkLen;
	private int isBackRun = 0;
	public int isBackRun_history = 0;
	public Handler bHandler;
	public Handler serHandler;
	public boolean obdGpsEnable;//local track upload switch
	public int landScapeMode; // 0 portrait 1 common land 2 group land
	public int callStat;
	public boolean gpsEnablePrompt = false;
	//0: first splash page
	//1: normal splash page; 
	//2: non splash page
	int currentfoucs = 3;
	
	public Login logindialog;
	public Forgetpw forgetpw;
//	public ResetPassword resetpw;
	
	public volatile int globalcmd = 0;
	static public Typeface mTf; 
//	public Context mcontext;//base activity
	
	private Context splashContext;
	public boolean ifBtOpenInitial; //when enter OBD if BT enabled
	public boolean weizhangInit;
	
	public Map<String, Object> wzMapItem;
    //location
    public LocationClient mLocationClient;
    public GeofenceClient mGeofenceClient;
//    public OBDLocationListener mLocationListener;
    public TextView mLocationResult;
//    public double latitude;
//    public double longitude;
//    public BDLocation mBDLoc;
    public Base baseAct;
    
    //offlinemap center location
    public double x = -1;
    public double y = -1;
    
    private static RequestQueue sHttpClient;
    private static ThreadPoolExecutor threadPool = (ThreadPoolExecutor) Executors.newCachedThreadPool();
    private static  OBDApplication sInstance;
    public int mqttStat; //0 stop; 1 start
//    public String creatGrpName;
//    public String creatGrpDesc;
	//get splash page
    public String fileName;
    public String driveBehaviorData;
    public boolean appHasEnter;
    public DriveDetailActivity driveDetail;
    public DrivehabitActivity driveHabit;
    LogRecord logrecord;
    public UpLoadLog myUpLoadLog;
    public boolean driveHabitDragDownNotice;
   
    public boolean sensorState;
    public Handler playTimeHandler;
    
	public static ArrayList<Group> grpResLst;
	public static ArrayList<Group> grpCreatorLst;
	public static ArrayList<Group> grpSearchLst;
	public static ArrayList<Member> grpSearchMemberLst;
	public static ArrayList<Member> grpSelMemberLst;
	public static ArrayList<Member> friendLst;    
    /**
     * get request queue 
     * @return
     */
    public static  ThreadPoolExecutor getThreadPool() {
        return threadPool;
    }
    public static  RequestQueue getHttpClient() {
        return sHttpClient;
    }
    
    public static  OBDApplication getInstance() {
        return sInstance;
    }
    
	public int getglobalcmd(){
		return globalcmd;
	}
	
	public void setglobalcmd(int cmd){
		globalcmd = cmd;
	}	
	
	
//	public Context getContext(){
//		return mcontext;
//	}
//	
//	public void setContext(Context context){
//		mcontext = context;
//	}
		
	public Context getSplashContext(){
		return splashContext;
	}
	
	public void setSplashContext(Context context){
		splashContext = context;
	}
	
	//get base activity handler
	public Handler getHandler(){
		return bHandler;
	}
	
	public void setPlayTimeHander(Handler handler){
		playTimeHandler = handler;
	}
	
	public void setHandler(Handler handler){
		bHandler = handler;
	}
	
	//get splash page
	public int getSplash(){
		return currentfoucs;
	}
	
	public void setSplash(int CurrentFocus){
		currentfoucs = CurrentFocus;
	}
	
	//get service handler;
	public Handler getSerHandler(){
		return serHandler;
	}
	
	public void setSerHandler(Handler handler){
		serHandler = handler;
	}
	
	//set and get several dialogs:
	public void setLoginDialog(Login logind){
		logindialog = logind;
	}
	
	public Login getLoginDialog(){
		return logindialog;
	}

	public void setForgetpwDialog(Forgetpw forgetd){
		forgetpw = forgetd;
	}
	
	public Forgetpw getForgetpwDialog(){
		return forgetpw;
	}
	
//	public void setResetpwDialog(ResetPassword resetd){
//		resetpw = resetd;
//	}
//	
//	public ResetPassword getResetpwDialog(){
//		return resetpw;
//	}	
	
	public int getFocusId(){
		return backRunFocusId;
	}
	
	public void setFocusId(int id){
		backRunFocusId = id;
	}
	
	public String getOBDName(){
		return obdName;
	}
	
	public void setOBDName(String name){
		obdName = name;
	}
	
	public String getVersion() {
	     try {
	         PackageManager manager = getPackageManager();
	         PackageInfo info = manager.getPackageInfo(getPackageName(), 0);
	         String version = info.versionName;
	         return version;
	     } catch (Exception e) {
	         e.printStackTrace();
	         return null;
	     }
	}
	
	public synchronized void setActivityBack(int isBack){	
		isBackRun_history = isBackRun;
		isBackRun = isBack;
	}

	public int getActivityBack(){
		return isBackRun;
	}
	

	public void onCreate(){
		super.onCreate();
		backRunFocusId = -1;
		landScapeMode = 1;
		obdName = null;
		
		sInstance = this;
		mTf = Typeface.createFromAsset(getAssets(),
				"OpenSans-Regular.ttf");
		//For JPush:
//		JPushInterface.setDebugMode(true);
//		JPushInterface.init(this);
		
		Base.initOBDRootPath();	
	
		String p = Preference.getInstance(this).getLocale();
		if (p != null && !p.equals("")) {
			Locale locale;
			// workaround due to region code
			if (p.equals("zh-TW")) {
				locale = Locale.TRADITIONAL_CHINESE;
			} else if (p.startsWith("zh")) {
				locale = Locale.CHINA;
			} else {
				locale = new Locale(p);
			}
			Locale.setDefault(locale);
			Configuration config = new Configuration();
			config.locale = locale;
			getBaseContext().getResources().updateConfiguration(config,
					getBaseContext().getResources().getDisplayMetrics());
		}
		
		//location
//        mLocationClient = new LocationClient(getApplicationContext());
//        mLocationListener = new OBDLocationListener();
//        mLocationClient.registerLocationListener(mLocationListener);        
        mGeofenceClient = new GeofenceClient(getApplicationContext());
        
        sHttpClient = Volley.newRequestQueue(this);
//        SDKInitializer.initialize(getApplicationContext());
        
        logrecord = new LogRecord(this);
        
        new Thread(runnable).start();

	}
	
	Runnable runnable = new Runnable() {
		@Override
		public void run() {
			boolean append = true;//if upload succeed, log file truncate, otherwise append
			
			if(getNewWorkType() == ConnectivityManager.TYPE_WIFI){
				append = !LogRecord.UploadLogFiles(Base.ycblog,BluetoothService.testURL);				
			}
			LogRecord.SaveSysInfo2File(Base.BTlog, append);
			LogRecord.SaveSysInfo2File(Base.OBDinit, append);
			LogRecord.SaveSysInfo2File(Base.WeathInfo, append);
			LogRecord.SaveSysInfo2File(Base.OperateInfo, append);
		}

	};
	
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
//    private void InitLocation(){
//        LocationClientOption option = new LocationClientOption();
//        option.setLocationMode(LocationClientOption.LocationMode.Battery_Saving);//设置定位模式
//        option.setCoorType("bd09ll");//返回的定位结果是百度经纬度，默认值gcj02
//        int span=1000;
//        try {
//            span = Integer.valueOf("1000");
//        } catch (Exception e) {
//            // TODO: handle exception
//        }
//        option.setScanSpan(span);//设置发起定位请求的间隔时间为5000ms
//        option.setIsNeedAddress(true);
//        mLocationClient.setLocOption(option);
//       
//    }
    
   
}
