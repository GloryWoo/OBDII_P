package com.ctg.service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;
import com.ctg.Location.LocationData;
import com.ctg.bean.CarData;
import com.ctg.bluetooth.EST527.BluetoothService;
import com.ctg.bluetooth.EST527.BluetoothSet;
import com.ctg.crash.LogRecord;
import com.ctg.net.CacheManager;
import com.ctg.net.GPSTrackThread;
import com.ctg.net.UploadGPS;
import com.ctg.obdii.DTCindex;
import com.ctg.obdii.OBDcmd;
import com.ctg.sensor.OBDSensor;
import com.ctg.sensor.UpLoadSensor;
import com.ctg.trace.TraceDataSource;
import com.ctg.ui.Base;
import com.ctg.ui.DTCs_List;
import com.ctg.ui.OBDApplication;
import com.ctg.ui.R;
import com.ctg.ui.Setting;
import com.ctg.util.JsonUtil;
import com.ctg.util.MyGeoFenceCont;
import com.ctg.util.Preference;

public class CarDataService extends Service {
	public static final String TAG = "CarDataService";
	public static final boolean D = true;
	
	public static boolean iEST527 = true;
	public static long dataFlag = 0;
	public String notiTitle = null;
	public String notiContent = null;
	public int notitoken = 0;
	private NotificationManager localNM = null;
	private Notification mnotification = null;
	private PendingIntent pendingintent = null;

	private myBinder mBinder = new myBinder();
	private OBDApplication OBDApp;
	// public static CarDataService context;
	public Context mContext;
	private static Setting localsetting = null;

	public static Queue<String> dtcqueue = new LinkedList<String>();
	public static String dtcHistory = "";
	public static final int DTCNUM = 15;
	public static String dtcArray[] = new String[DTCNUM];
	public static Map<String, String> dtcSummaryMap;
	private BluetoothSet mBluetoothSet = null;		//蓝牙对象iEST527
	private getDataThread mgetDataThread = null;
	public static OBDcmd mOBDcmd = null;
	public static String obdDeviceName = null;
	private static volatile boolean baseActivityRun;
	// private static int isBackRun = 0;
	// private static int isBackRun_history = isBackRun;
	private static boolean isIntent = false;
	private static int dtcFocus = -1;

	// test time
	private static int sendDTCTime = 0;

	// gps track
	
	public static final int UPLOADMAXCOUNT = 1000;// 1000;
	public static int UPLOADMAXFREQUNCY = 5;// 50;
	public static final int UNITSIZE = 25;
	public static int uploadCount;
	// final Semaphore gpssemp = new Semaphore(1);
	public static String gpsDataSubPath = "/OBDII/gps/";
	public static String gpsDataFullPath;
	public static FileOutputStream gpsDataOutput;
	public static byte gpsuploadBuf[];
	// public static byte gpsuploadUnit[];
	public static String gpsuploadURL = Base.HTTP_ROOT_PATH + "/services/gps";

	// /////////////////////////////////////////////////lzy
	// public static String mNewGpsuploadURL =
	// "http://192.168.1.51:8080/obd/services/uploadInstantData";
	public static String mGpsuploadURL = "http://116.236.202.130:8089/obd/services/uploadInstantData";
	// "http://192.168.1.51:8080/obd/services/uploadInstantData";

	GPSTrackThread gpsTrack;
	// public static boolean gpsEnalbePopup;
	public static long gpsFileWriteByteCnt;
	public OBDLocationListener mLocationListener;
	private LocationClient mLocationClient;
	private LocationData locationData;
	public LocationManager locationManager;
	//LocationListener locationListener;
	public static final int MAX_REASONABLE_LATLNG_OFFSET = 500;
	ArrayList<LatLng> last5latLng;
	public UploadGPS upGPS;
	public UpLoadSensor upSensor;
	public static OBDSensor obdSensor;
	Handler supporthandler;
	Message supportmsg;

	public static ArrayList<MyGeoFenceCont> fenceList;
	int fenceIdx;
	static final public int GPS_UPLOAD_STOP = 0;
	static final public int GPS_UPLOAD_START = 1;
	static final public int GPS_UPLOAD_CONTINUE = 2;
	public int GPSUploadState = GPS_UPLOAD_START;// 0:stop 1:start 2:continue
	private TraceDataSource traceDataSource = null;
	private LatLng point_prev = null;
	private long create_time_prev = 0l;
	private Date create_date_prev = null;
	private double lat_prev = 0.0, lng_prev = 0.0, lat_non_outlier = 0.0, lng_non_outlier = 0.0;
	public static double curLat, curLng;
	private float vss_prev = 0, micro_dist = 0, total_dist = 0f;
	private boolean outlier = false;
	private int num_non_outlier = 0;
	private static final double GPS_OBD_RATIO_THRESH = 3.0;
	
	// temporary obd data
	private float bat = 0;
	private int rpm = 0;
	private float vss = 0;
	private float throtPos = 0;
	private float engineLoad = 0;
	private float temp = 0;
	private float mpg = 0;
	private float avg_mpg = 0;
	private float fli = 0;
	private int dtc = 0;	
	private boolean isIdle = false;
	private boolean hasOBDConn = false;

	// //////////////////////////////////////////////////////////////////////////
	// --------------Background Service
	// Start------------------------------------
	// //////////////////////////////////////////////////////////////////////////
	public class myBinder extends Binder {
		public void startRun() {
			if (D)
				Log.d(TAG, "Bind service start run;");
		}

		public void startload() {

		}

		public OBDcmd getOBDcmd() {
			return mOBDcmd;
		}

		public int getBTstate() {
			if (!iEST527) {
				if (mOBDcmd != null && mOBDcmd.mBluetoothSet != null)
					return mOBDcmd.mBluetoothSet.ConnectionState();
				else
					return 0;
			}
			else {
				if(mBluetoothSet != null)
					return mBluetoothSet.getConnectionState();
				else
					return 0;
			}
		}
		
//		public void bindInitFenceData(){
//			initFenceData();
//		}
		
		public boolean wasOBDConnected(){
			return hasOBDConn;
		}

		public void setObdName(String name) {
			obdDeviceName = new String(name);
		}

		public String getObdName() {
			return obdDeviceName;
		}

		// public synchronized void setActivityBack(int isBack){
		// if(D) Log.d(TAG, "Service : activity isBack " + isBack );
		// isBackRun_history = isBackRun;
		// isBackRun = isBack;
		// }
		//
		// public int getActivityBack(){
		// return isBackRun;
		// }

		// activity tell service if itself still exists;
		public boolean getActivityStatus() {
			return baseActivityRun;
		}

		public void setActivityRun(boolean isRun) {
			if (D)
				Log.d(TAG, "Service : baseActivityRun " + isRun);
			baseActivityRun = isRun;
		}

		// activity tell service if itself still exists;
		public boolean getIntentStatus() {
			return isIntent;
		}

		public void setIntentStatus(boolean isintent) {
			if (D)
				Log.d(TAG, "Service : isIntent " + isintent);
			isIntent = isintent;
		}

//		public void setPanel(Panel lpanel) {
//			localpanel = lpanel;
//		}

		public void setSetting(Setting setting) {
			localsetting = setting;
		}

		public void setDTCs(DTCs_List dtc) {
			int i = 0, j = 0;
			String title = "";
			String content = "";
			if (!DTCindex.dtcqueue_history.isEmpty())
				j = DTCindex.dtcqueue_history.size();
			if (((com.ctg.ui.OBDApplication) getApplication()).isBackRun_history == Base.APP_RUN_BACKGROUND
					&& ((com.ctg.ui.OBDApplication) getApplication())
							.getActivityBack() == Base.APP_RUN_FOREGROUND) {
				for (i = 0; i < j; i++) {
					// if(D) Log.d(TAG, "DTC queue is not null!");
					title = DTCindex.dtcqueue_history.poll();
					content = DTCindex.summaryqueue_history.poll();
					if (title == null)
						title = "";
					if (content == null)
						content = "";
					DTCindex.dtcqueue_history.offer(title);
					DTCindex.summaryqueue_history.offer(content);
					if (D)
						Log.d(TAG, "Resume DTC code---title is" + title);
					// if(D) Log.d(TAG, "Send notification!---content is" +
					// content);
					String temporary = title + "+" + content;
					// sleep(100);
//					if (localdtc != null) {
						Base.threadAddDtcAbstract(temporary);
						if (D)
							Log.d(TAG, "Resume DTC code---title is" + title);
//					}
				}
			}
		}

		public void clearDTCs() {
			if (mOBDcmd == null)
				return;
			int i = 0;
			mOBDcmd.ClearDTcs();
			// DTCindex.dtcqueue.clear();
			// DTCindex.summaryqueue.clear();
			DTCindex.dtcqueue_history.clear();
			DTCindex.summaryqueue_history.clear();
			for (i = 0; i < OBDcmd.DTCnum; i++) {
				mOBDcmd.DTCHistory[i] = "";
			}
			clearAllNotification();
		}

		public void ReadVoltage() {
			if (mOBDcmd == null)
				return;
			mOBDcmd.Readvolt();
		}

		public void setDTCFocus(int focusId) {
			dtcFocus = focusId;
		}

		public int getFocusId() {
			return dtcFocus;
		}

		public void clearSingleNoti(String title) {
			clearSingleNotification(title);
		}
		
		public BluetoothSet getBluetoothSet(){
			return mBluetoothSet;
		}
		
		public UploadGPS getGPSUploader(){
			return upGPS;
		}
		
		public void setGPSUpdateState(int state){
			GPSUploadState = state;
		}
		
		public long getCurTraceId(){
			if(traceDataSource != null)
				return traceDataSource.getCurTraceId();
			else 
				return 0l;
		}
	};
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		// add by Hu zhiming on 06-04
		// add by Hu Zhiming on 06-04
		return mBinder;
	}
	
	public Setting getLocalsetting(){
		return localsetting;
	}

	void initDTCSummaryHashMap() {
		// {"C0129", "P0369", "P0720", "P1426", "P1504"};
		dtcSummaryMap = new HashMap<String, String>();
		dtcSummaryMap.put("C0129", CarDataService.this.getResources()
				.getString(com.ctg.ui.R.string.C0129));
		dtcSummaryMap.put("P0369", CarDataService.this.getResources()
				.getString(com.ctg.ui.R.string.P0369));
		dtcSummaryMap.put("P0720", CarDataService.this.getResources()
				.getString(com.ctg.ui.R.string.P0720));
		dtcSummaryMap.put("P1426", CarDataService.this.getResources()
				.getString(com.ctg.ui.R.string.P1426));
		dtcSummaryMap.put("P1504", CarDataService.this.getResources()
				.getString(com.ctg.ui.R.string.P1504));
	}

	public void onCreate() {
		super.onCreate();
		OBDApp = (com.ctg.ui.OBDApplication) getApplication();
		mContext = this;
		initBTchannel();
		initDTCSummaryHashMap();
		if (((com.ctg.ui.OBDApplication) getApplication()).getActivityBack() == Base.APP_EXIT)
			sendDTCTime = 0;
		// mBinder.setActivityBack(Base.APP_RUN_FOREGROUND);
		// context = this;
		// if(D) Log.d(TAG, "Service onCreate isBackRun ");
		supportmsg = new Message();

		DateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
		String time = formatter.format(new Date());
		File dir = new File(Base.getSDPath() + gpsDataSubPath);
		if (!dir.exists()) {
			dir.mkdir();
		}
//		initAndroidLocation();
		initBaiduLocation();
//		initFenceData();
		
		//locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		//String provider = LocationManager.GPS_PROVIDER;
		// wifiMgr = (WifiManager)
		// context.getSystemService(Context.WIFI_SERVICE);
		// String provider = LocationManager.NETWORK_PROVIDER;

		// Criteria criteria = new Criteria();
		// criteria.setAccuracy(Criteria.ACCURACY_FINE);
		// criteria.setAltitudeRequired(false);
		// criteria.setBearingRequired(false);
		// criteria.setCostAllowed(true);
		// criteria.setPowerRequirement(Criteria.POWER_LOW);
		// Location location = locationManager.getLastKnownLocation(provider);

		locationData = new LocationData();
		last5latLng = new ArrayList<LatLng>();
		gpsDataFullPath = Base.getSDPath() + gpsDataSubPath + time + ".txt";

		// UPLOADMAXFREQUNCY may get from xml file, care about below sequence
		initPackageUnitCnt();
		gpsuploadBuf = new byte[UNITSIZE * UPLOADMAXFREQUNCY];
		uploadCount = 0;
		gpsFileWriteByteCnt = 0;
		// if(gpsuploadUnit == null)
		// gpsuploadUnit = new byte[UNITSIZE];

		// 在有wifi网络的情况下上传保存本地的GPS文件内容
		upGPS = new UploadGPS(this);
		if (getNewWorkType() == ConnectivityManager.TYPE_WIFI) {
			upGPS.UploadFiles(mGpsuploadURL);
		}
		
		IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
		this.registerReceiver(networkChangeReceiver, filter);
		
		// 在有wifi网络的情况下上传保存本地的sensor文件内容
		upSensor = new UpLoadSensor(mContext);
		if ( Base.OBDApp.sensorState == false && getNewWorkType() == ConnectivityManager.TYPE_WIFI) {
			upSensor.uploadGPSThreadStart();
		}
		
		if (D)
			Log.d(TAG, "Service onCreate");
		Log.v("gpsstate", GPSUploadState+"");
	}

	private void initBaiduLocation() {
		Log.v("LOCATION", "init");
		//location
        mLocationClient = new LocationClient(mContext);
        mLocationListener = new OBDLocationListener();
        mLocationClient.registerLocationListener(mLocationListener);    		
		LocationClientOption option = new LocationClientOption();
		option.setLocationMode(LocationMode.Hight_Accuracy);//
		option.setCoorType("bd09ll");//
		option.setScanSpan(1000);//
		option.setIsNeedAddress(true);
		mLocationClient.setLocOption(option);
		mLocationClient.start();		
	}

	private void initAndroidLocation(){
		Log.v("Android LOCATION", "init");
		
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		boolean gpsEnable = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER); 
		
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);		
	}

	private final LocationListener locationListener = new LocationListener() {
		@Override
		public void onLocationChanged(Location location) {
			// TODO Auto-generated method stub
			myUpdateWithNewLocation(location);
		}
	
		public void onProviderDisabled(String provider) {
			// updateWithNewLocation(null);
			myUpdateWithNewLocation(null);
		}
	
		public void onProviderEnabled(String provider) {
		}
	
		public void onStatusChanged(String provider, int status, Bundle extras) {
		}


	};
			
	private LatLng myUpdateWithNewLocation(Location location) {
		double lng = 0, lat = 0;
		Message msg = new Message();
		Bundle bundle = new Bundle();
		msg.what = OBDSensor.GPS_UPDATE_MSG;
		bundle.putSerializable("location", (Serializable) location);
		msg.setData(bundle);
		//if(Base.OBDApp.obdSensor != null){
			//Base.OBDApp.obdSensor.gpsHander.sendMessage(msg);
		//}
		
		
		return null;	
	}
		
	public static void initFenceData(){
		fenceList = new ArrayList<MyGeoFenceCont>();
		if(Preference.getInstance(Base.OBDApp).getLoginStat()){
			String obdii_path = Base.getSDPath() +"/OBDII/";
			String fence_path = obdii_path + Base.loginUser + "/geofence";			
			try {
				FileInputStream fence_in = new FileInputStream(fence_path);
				MyGeoFenceCont fence = null;
				if(fence_in != null){
					ObjectInputStream obj_in = new ObjectInputStream(fence_in);												
					while((fence = (MyGeoFenceCont) obj_in.readObject()) != null){
	//					fence.inFence = false;
						fenceList.add(fence);
	//					addCustomElementsDemo(fence.lat, fence.lon,fence.radius);
					}
					fence_in.close();
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
		
	}
	
	public static void saveFenceData(){
		if(Preference.getInstance(Base.OBDApp).getLoginStat()){
			String obdii_path = Base.getSDPath() +"/OBDII/";
			String fence_path = obdii_path + Base.loginUser + "/geofence";
			int i = 0;		
			try {
				if(fenceList != null){	
					int len = fenceList.size();
					FileOutputStream fence_out = new FileOutputStream(fence_path);
					MyGeoFenceCont fence = null;
					if(fence_out != null){
						ObjectOutputStream obj_out = new ObjectOutputStream(fence_out);															
						while(i < len){
							fence = fenceList.get(i);
							obj_out.writeObject(fence);
							i++;
						}
						fence_out.close();
					}
				}
			}
			catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (D)
			Log.d(TAG, "Service onStartCommand");
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onRebind(Intent intent) {
		if (D)
			Log.d(TAG, "Service onRebind");
		super.onRebind(intent);
	}

	@Override
	public boolean onUnbind(Intent intent) {
		// baseActivityRun = false;
		// if(D) Log.d(TAG, "baseActivityRun is false");
		if (Base.OBDApp.getActivityBack() == Base.APP_EXIT) {// 0
			connectionDestory();
			settingDestory();
		} else if (Base.OBDApp.getActivityBack() == Base.APP_RUN_BACKGROUND) {// 2
			settingDestory();
		} else {

		}
		if (D)
			Log.d(TAG, "Service onUnbind");

        if(GPSUploadState == GPS_UPLOAD_CONTINUE){
            GPSUploadState = GPS_UPLOAD_STOP;

            //send stop
        }

		return super.onUnbind(intent);
	}

	public void onStop() {
		if (D)
			Log.d(TAG, "Service onStop");
		super.stopSelf();

	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		if (true)
			Log.d(TAG, "Service onDestory");
		connectionDestory();
		if (D)
			Log.d(TAG, "Service onDestory");

        if(GPSUploadState == GPS_UPLOAD_CONTINUE){
            GPSUploadState = GPS_UPLOAD_STOP;

            //send stop
        }
        //saveFenceData();
		// if (gpsDataOutput != null) {
		// try {
		// gpsDataOutput.close();
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// gpsDataOutput = null;
		// }
		if (gpsFileWriteByteCnt == 0) {
			File f = new File(gpsDataFullPath);
			if (f.exists())
				f.delete();
		}
		BluetoothAdapter mBtAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBtAdapter.isEnabled() && !Base.OBDApp.ifBtOpenInitial)
			mBtAdapter.disable();
		if(mLocationClient != null)
		mLocationClient.unRegisterLocationListener(mLocationListener);
		super.onDestroy();
	}

	// //////////////////////////////////////////////////////////////////////////
	// --------------Background Service End------------------------------------
	// //////////////////////////////////////////////////////////////////////////

	// //////////////////////////////////////////////////////////////////////////
	// --------------GPS track
	// operation-----------------------------------------
	// //////////////////////////////////////////////////////////////////////////
	private boolean initPackageUnitCnt() {
		InputStream in_s = null;
		Document doc = null;
		File f = new File(Base.getSDPath() + "/OBDII/packunit.xml");

		if (!f.exists())
			return false;

		try {
			in_s = new FileInputStream(f);
			doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
					.parse(in_s);
			NodeList nodeList = doc.getElementsByTagName("packunit");
			String nodeVal = null;
			String nodeVal1 = null;
			Element ele;
			Node node;

			if (nodeList == null)
				return false;
			ele = (Element) nodeList.item(0);
			if (ele == null)
				return false;
			node = ele.getFirstChild();
			if (node == null)
				return false;
			nodeVal = node.getNodeValue();

			if (nodeVal != null && !nodeVal.equals(""))
				UPLOADMAXFREQUNCY = Integer.parseInt(nodeVal);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}

		return true;
	}


	Handler gpsHandler = new Handler() {
		public void handleMessage(Message message) {

		}
	};

	public static synchronized boolean my_sync_write(String content) {
		try {
			// byte[] content = str.getBytes();
			gpsDataOutput = new FileOutputStream(gpsDataFullPath, true);
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
					gpsDataOutput));
			bw.write(content + "\r\n");			
			bw.flush();
			gpsFileWriteByteCnt += content.length();
			bw.close();
			gpsDataOutput.close();
			LogRecord.SaveLogInfo2File(Base.WeathInfo, "sync_write count="
					+ gpsFileWriteByteCnt);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return false;
	}

	public static synchronized boolean sync_write(byte[] content) {
		try {
			gpsDataOutput = new FileOutputStream(gpsDataFullPath, true);

			gpsDataOutput.write(content);
			gpsFileWriteByteCnt += content.length;
			gpsDataOutput.close();
			LogRecord.SaveLogInfo2File(Base.WeathInfo, "sync_write count="
					+ gpsFileWriteByteCnt);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return false;
	}

	static int enterCount = 0;
	static int exitCount = 0;

	static int block_id = 0;
	/**
	 * lzy
	 * 
	 * @param location
	 */
	private LatLng myUpdateWithNewBDLocation(BDLocation location) {
		double lat = 0.0, lng = 0.0;
		Date cur_time = null;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		if (location != null) {
			lat = location.getLatitude();
			lng = location.getLongitude();
			if(lat < 0.01 && lng < 0.01)	// positioning not effective
				return null;
			curLat = lat;
			curLng = lng;
//			cur_time = new Date(System.currentTimeMillis());
//			if(create_date_prev != null){	// from the second point on
//				// if same second, lat and lon, skip
//				if(cur_time.getTime() / 1000 == create_date_prev.getTime() / 1000 && lat_prev == lat && lng_prev == lng){	
//					return null;
//				}
//				
////				boolean simulate_obd = true;
//				boolean loginStat = Preference.getInstance(getApplicationContext()).getLoginStat();		
//				if(mBluetoothSet != null && mBluetoothSet.isRegistered() && mBluetoothSet.isConnected())
//					hasOBDConn = true;
//				else
//					hasOBDConn = false;
//				
////				if (simulate_obd && loginStat) {
//				if (hasOBDConn && loginStat) {					
//					// remove outliers by obd data fusion
//					double t_diff_sec = (cur_time.getTime() - create_date_prev.getTime()) * 1.0 / 1000l;
//					double dist_obd = vss_prev * 0.2778 * t_diff_sec;
//					micro_dist += dist_obd;
//					total_dist += dist_obd;
//					
//					LatLng p1 = new LatLng(lat_non_outlier, lng_non_outlier);
//					LatLng p2 = new LatLng(lat, lng);
//					double dist_gps = DistanceUtil.getDistance(p1, p2);	// distance between the current point with the last non-outlier point					
//					
////					if(dist_gps > 0.0){	// only set outlier when gps point moves
//						if(dist_gps <= GPS_OBD_RATIO_THRESH * micro_dist){// dont't reset micro_dist until an non-outlier point found
//							outlier = false;
//							micro_dist = 0f;	
//							lat_non_outlier = lat;
//							lng_non_outlier = lng;
//							num_non_outlier++;
//						}							
//						else {
//							// if found N consecutive outliers, advance last non-outlier to the current point, to be implemented
//							outlier = true;
//						}						
////					}
//										
//					JSONObject jsonObj = null;
//					uploadCount++;
//					
//					if(!outlier)
//						jsonObj = addPosToDB(dealWithBT(sdf.format(cur_time), lat, lng, location.getSpeed()));
//					else	// send the last non-outlier point
//						jsonObj = addPosToDB(dealWithBT(sdf.format(cur_time), lat_non_outlier, lng_non_outlier, location.getSpeed()));
//					
//					if ((getNewWorkType() == ConnectivityManager.TYPE_MOBILE && uploadCount <= UPLOADMAXCOUNT)
//							|| getNewWorkType() == ConnectivityManager.TYPE_WIFI) {
//						addPosToHttp(jsonObj);
//					} else {
//						my_sync_write(jsonObj.toString());
//					}					
//					
//					if(D)LogRecord.SaveLogInfo2File(Base.WeathInfo, TAG + "," + sdf.format(cur_time) + "," + lng + "," + lat + "," + total_dist + "," + micro_dist + "," +
//							dist_gps + "," + lng_non_outlier + "," + lat_non_outlier + "," + outlier + "," + num_non_outlier + "," + uploadCount + "," + dist_gps / micro_dist);
//					if(D)Log.d(TAG, "outlier: " + outlier);
//					
//					create_date_prev = cur_time;
//					lat_prev = lat;
//					lng_prev = lng;
//					vss_prev = vss;
//					
//					if(!outlier)
//						return new LatLng(lat,lng);
//					else
//						return new LatLng(lat_non_outlier, lng_non_outlier);
//				}
//			} else {	// assmue the very first point is an non-outlier, opportunistically
//				lat_non_outlier = lat;
//				lng_non_outlier = lng;
//			}
			
			create_date_prev = cur_time;
			lat_prev = lat;
			lng_prev = lng;
			vss_prev = vss;
			
			return new LatLng(lat,lng);
		} else {
			return null;
		}	
	}

	private CarData dealWithBT(String cur, double lat, double lng, float speed) {		
		if (!iEST527 && mOBDcmd.mBluetoothSet.ConnectionState() == com.ctg.bluetooth.BluetoothService.STATE_CONNECTED) {// 有蓝牙

			int rpm = OBDcmd.enginerpm;
			float vss = OBDcmd.vehiclespeed;
			float temp = OBDcmd.enginetemp;

			CarData gpsDataBT = new CarData(cur, lat, lng, vss, rpm, temp);
			gpsDataBT.setHasOBDData(true);
			
			return gpsDataBT;
		} 
		else if (iEST527 && mBluetoothSet.isConnected()) {
//		else if (iEST527 && hasOBDConn) {	// simulate bt
			// get est527 data here
			CarData cd = new CarData(cur, lat, lng, bat, rpm, vss, throtPos, engineLoad, temp, mpg, avg_mpg, fli, dtc);
			cd.setHasOBDData(true);
			cd.setIdle(isIdle);
			
			return cd;
		}
		else {// 无蓝牙OBD
			CarData gpsData = null;
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			try {
				if (point_prev == null) { // first gps point				
					gpsData = new CarData(cur, lat, lng);
					gpsData.setVSS(0f);
					gpsData.setHasOBDData(false);
					
					point_prev = new LatLng(lat, lng);
					create_time_prev = sdf.parse(cur).getTime();
				} else {
					LatLng point_cur = new LatLng(lat, lng);
					long time_cur = sdf.parse(cur).getTime();
					double dist = DistanceUtil.getDistance(point_cur, point_prev) / 1000; // unit: km				
					double dur = (time_cur - create_time_prev) * 1.0 / (1000l * 60l * 60l); // unit: hour
					gpsData = new CarData(cur, lat, lng);
					gpsData.setVSS((float) (dist / dur));
					gpsData.setHasOBDData(false);
					
					point_prev = point_cur;
					create_time_prev = time_cur;
				}
			} catch (ParseException e) {
				// TODO: handle exception
				e.printStackTrace();
			}
			return gpsData;
		}
	}

	private JSONObject addPosToDB(CarData gpsData) {
		if (GPSUploadState == GPS_UPLOAD_START) {
			if (traceDataSource == null)
				traceDataSource = new TraceDataSource(mContext);
			Long cur = new Date(System.currentTimeMillis()).getTime();
			long last = Preference.getInstance(getApplicationContext()).getLastQuitTime();
			if (cur - last > 30l * 60l * 1000l) {	// greater than half hour
				traceDataSource.newTrace(); // only create new trace id
			}
			else{
				traceDataSource.setTraceID(Preference.getInstance(getApplicationContext()).getTraceID());
			}
		}
		
        long curTraceId = 0;
        if(traceDataSource!=null){
        	curTraceId = traceDataSource.getCurTraceId();
        }
            
		JSONObject jsonObj = JsonUtil.GPSdataToJson(gpsData, curTraceId);
		GPSUploadState = GPS_UPLOAD_CONTINUE;

		// no need to save points to database! by Weiran
//		Timestamp time = null;
//		try {
//			time = Timestamp.valueOf(gpsData.getCurrentTime());
//		} catch (Exception e) {
//		}
//		GPS gps = new GPS("userID", gpsData.getLon(), gpsData.getLat(), time,
//				gpsData.getCarSpeed(), gpsData.getRotate(),
//				gpsData.getWaterTemp(), acceleratespeed);
//		// GPS gps = new GPS("userID", 39.910286d, 116.432727d, time, 11.1f, 1f,
//		// 1.1f, acceleratespeed);
//
//		traceDataSource.addTracePt(gps);

		return jsonObj;
		// my_sync_write(jsonObj.toString());

	}

	private void addPosToHttp(JSONObject jsonObj) {
		Map<String, String> postData = new HashMap<String, String>();

		try {
			Iterator<?> keys = jsonObj.keys();
			while(keys.hasNext()){
				String key = (String)keys.next();
				postData.put(key, jsonObj.getString(key));
			}						

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		CacheManager.getJson(mContext, mGpsuploadURL, null, postData);
		LogRecord.SaveLogInfo2File(Base.WeathInfo, "upload gps count:"
				+ uploadCount);

	}

	private String getCurDate() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return sdf.format(new Date());
	}

	public int getNewWorkType() {
		ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		boolean flag = false;
		if (manager != null && manager.getActiveNetworkInfo() != null) {
			flag = manager.getActiveNetworkInfo().isAvailable();

			if (flag) {
				NetworkInfo networkinfo = manager.getActiveNetworkInfo();

				if (networkinfo != null && networkinfo.isAvailable()) {
					return networkinfo.getType();
				}
			}
		}
		return -1;
	}
	//baidu location listener
	public class OBDLocationListener implements BDLocationListener {

		@Override
		public void onReceiveLocation(BDLocation location) {
			//Receive Location 
			Log.v("LOCATION", "LOCATION");
			if( locationData != null ){
				locationData.setPoiName(location.getAddrStr());
				locationData.setCity(location.getCity());
				locationData.setLatitude(location.getLatitude());
				locationData.setLongitude(location.getLongitude());
			}
//			Intent intent = new Intent();
//			intent.putExtra("location", locationData);
//			intent.setAction("com.ctg.service.CarDataService");
//			mContext.sendBroadcast(intent);
			
			LatLng latlng = myUpdateWithNewBDLocation(location);	
			if (true) {
//				double lat = latlng.latitude;
//				double lng = latlng.longitude;
//				if(lat < 0.01 && lng < 0.01)	// positioning not effective
//					return;
//				LatLng latlng = new LatLng(lat, lng);
				//locationValidCheck(latlng) && 
				if(Base.OBDApp.getActivityBack()==Base.APP_RUN_FOREGROUND){
					Bundle bb = new Bundle();
					Message msg = new Message();
					bb.putDouble("latitude", location.getLatitude());
					bb.putDouble("longitude", location.getLongitude());
					bb.putString("city", location.getCity());
					bb.putString("address", location.getAddrStr());
					msg.setData(bb);				
					msg.what = 1;
					Base.baidu_v.baiduLocationHander.sendMessage(msg);
					geofenceNotice(new LatLng(location.getLatitude(),location.getLongitude()));
				}
			}
		}				
	}

	public boolean locationValidCheck(LatLng latlng){

		LatLng averlatlng;
		
		double totalLat = 0.0, totalLng = 0.0, averLat = 0.0, averLng = 0.0;
		double dist;
		int len = last5latLng.size();
		
		//first one  
		if(len == 0){
			last5latLng.add(latlng);
			return true;
		}
		for(LatLng item : last5latLng){
			totalLat += item.latitude;
			totalLng += item.longitude;
		}
		averLat = totalLat/len;
		averLng = totalLng/len;
		averlatlng = new LatLng(averLat, averLng);

		dist = DistanceUtil.getDistance(latlng, averlatlng);
		if(dist < MAX_REASONABLE_LATLNG_OFFSET){
			if(len < 5)
				;
			else
				last5latLng.remove(0);
			last5latLng.add(latlng);			
			return true;
		}
		return false;		
	}
	
//	static int j_tst = 0;
	public void geofenceNotice(LatLng latlng){
		MyGeoFenceCont cont;
		int i = 0;
		int len;
		double dist;
		
//		if(j_tst++ == 5){
//			latlng = new LatLng(latlng.latitude+30, latlng.longitude);
//			j_tst = 0;
//		}
		
		if (fenceList != null && (len = fenceList.size()) != 0) {
			for (i = 0; i < len; i++) {
				cont = fenceList.get(i);
				dist = DistanceUtil.getDistance(latlng, new LatLng(cont.lat, cont.lon));
				if (dist <= cont.radius) {// enter geofence
					if (!cont.inFence) {
						//Toast.makeText(baseAct, "进入电子围栏：" + cont.name,
						//		Toast.LENGTH_SHORT).show();
						//enter_exit_gfence(true, cont.name);
						fenceIdx = i;
						initNotification("电子围栏提示", "进入电子围栏:" + cont.name, 1);
					}
					cont.inFence = true;
				} else {
					if (cont.inFence) {
						//Toast.makeText(baseAct, "离开电子围栏：" + cont.name,
						//		Toast.LENGTH_SHORT).show();
						//enter_exit_gfence(false, cont.name);
						fenceIdx = i;
						initNotification("电子围栏提示", "离开电子围栏:" + cont.name, 1);
					}
					cont.inFence = false;
				}
			}
		}
		
	}
	
	// //////////////////////////////////////////////////////////////////////////
	// --------------GPS track End------------------------------------
	// //////////////////////////////////////////////////////////////////////////

	// //////////////////////////////////////////////////////////////////////////
	// --------------DTC queue
	// operation-----------------------------------------
	// //////////////////////////////////////////////////////////////////////////

	// //////////////////////////////////////////////////////////////////////////
	// --------------BT and Communication Initialization------------------------
	// //////////////////////////////////////////////////////////////////////////
	public void initBTchannel() {
		int i = 0;
		if(iEST527){
			if (mBluetoothSet == null) {
				mBluetoothSet = new BluetoothSet(this);
				if (!mBluetoothSet.isSupported()) {
					Toast.makeText(this, "Bluetooth SPP Not Supported!",
							Toast.LENGTH_SHORT).show();
					return;
				}
				mBluetoothSet.openBluetooth();
				mBluetoothSet.startBTService();
			}
			
			if(mgetDataThread == null){
				mgetDataThread = new getDataThread(mEST527Handler);
				mgetDataThread.start();
			}

		}
		else{
			if (mOBDcmd == null) {
				mOBDcmd = new OBDcmd(mEST327Handler);
				for (i = 0; i < DTCNUM; i++) {
					dtcArray[i] = "";
				}
			}
		}
	}

	public void connectionDestory() {
		if (!iEST527) {
			if (mOBDcmd != null && mOBDcmd.mBluetoothSet != null)
				mOBDcmd.mBluetoothSet.stopBTService();
			DTCindex.dtcqueue_history.clear();
			DTCindex.summaryqueue_history.clear();
			// mOBDcmd.mDTCindex = null;
			if (mOBDcmd.mreadthread != null)
				mOBDcmd.mreadthread.cancel();
			mOBDcmd = null;
			if (D)
				Log.d(TAG, "Service destory");
			notitoken = 0;
		}
		else {
			// Stop the Bluetooth chat services
			if ((mgetDataThread != null) && (!mgetDataThread.isInterrupted())) mgetDataThread.cancel();
			
			mBluetoothSet.stopBTService();
	        mBluetoothSet = null;
		}
	}

	public void settingDestory() {
localsetting = null;
	}

	// ==============Boardcast===================================================
	private void sendRTData(int rpm, int vss, int temp) {

		Bundle bundle = new Bundle();
		bundle.putInt("rpm", rpm);
		bundle.putInt("vss", vss);
		bundle.putInt("temp", temp);
		Intent sendintent = new Intent("android.com.ctg.service").putExtra(
				"realdata", bundle);// .sendOrderedBroadcast();
		sendOrderedBroadcast(sendintent, null);
	}

	private void sendBTState(String type, int num) {

		Bundle bundle = new Bundle();
		bundle.putInt(type, num);
		Intent sendintent = new Intent("android.com.ctg.service").putExtra(
				"btcon", bundle);// .sendOrderedBroadcast();
		sendOrderedBroadcast(sendintent, null);
	}

	private void sendDtcs(String type, String code) {
		Bundle bundle = new Bundle();
		bundle.putString(type, code);
		Intent sendintent = new Intent("android.com.ctg.service").putExtra(
				"dtccode", bundle);// .putExtra(type,
									// num);//.sendOrderedBroadcast();
		sendOrderedBroadcast(sendintent, null);
	}

	// --------------Vehicle real data update
	// handler----------------------------
	// //////////////////////////////////////////////////////////////////////////
	public final Handler mEST327Handler = new Handler() {
		int rpm = 0;
		int vss = 0;
		int temp = 0;
		int fuels = 0;
		int i = 0;
		int previousstate = 0, currentstate = 0;
		String title = "test", content = "testconent";
		boolean engineon = false;
		int uploadcount = 0;
		boolean power = true, body = true, chassis = true, unet = true;
		Context basecontent;

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			Bundle bundle = msg.getData();
			if (mOBDcmd == null)
				return;
			switch (msg.what) {
			case OBDcmd.READY:
				uploadcount = 0;
				engineon = true;
				dataFlag = 0;
				rpm = OBDcmd.enginerpm;
				vss = OBDcmd.vehiclespeed;
				temp = OBDcmd.enginetemp;
				fuels = OBDcmd.percentfuel;
				// if(D) Log.e(TAG, "UPDATE vss" + vss);
				if (D)
					Log.e(TAG, "UPDATE temp" + temp);
				if (((com.ctg.ui.OBDApplication) getApplication())
						.getActivityBack() == Base.APP_RUN_FOREGROUND) {
//					if (localpanel != null) {
//						if (rpm != 0)
//							localpanel.setPanelReadData(rpm, vss, temp, fuels);
//						else
//							localpanel.setPanelReadData(0, 0, 0, 0);
//					}
				}
				break;
			case OBDcmd.DTCEXIST:
				engineon = true;
				if (D)
					Log.e(TAG, "UPDATE real-time dtc");
				// if(DtcService.context.loadDtcIntoList)
				// return;
//				if (localdtc == null)
//					return;
//				if(sendDTCTime == 0)
//				 CarDataService.mOBDcmd.appendDTCcodes("P1504 P0720 C0129 P0369");
//				 sendDTCTime = 1;
				if (DTCindex.dtcqueue == null && DTCindex.summaryqueue == null)
					break;
				// Log.d(TAG, "READY AddDtc isBack=" + isBackRun
				// +" localdtc="+localdtc);
				while (!DTCindex.dtcqueue.isEmpty()
						&& !DTCindex.summaryqueue.isEmpty()) {
					// if(D) Log.d(TAG, "DTC queue is not null!");
					// OBDApp = (com.ctg.ui.OBDApplication)getApplication();
					basecontent = OBDApp.baseAct;
					title = DTCindex.fetchDTCcodes();
					content = DTCindex.fetchSummary();
					// if(D) Log.d(TAG, "Send notification!---title is" +
					// title);
					// if(D) Log.d(TAG, "Send notification!---content is" +
					// content);
					String temporary = title + "+" + content;
					// sleep(100);
					if (((com.ctg.ui.OBDApplication) getApplication())
							.getActivityBack() == Base.APP_RUN_FOREGROUND) {
//						if (localdtc != null)
							Base.threadAddDtcAbstract(temporary);
					}
					// update DTC code to notification here;
					// if(dtcArray[notitoken] != null)
					dtcArray[notitoken % DTCNUM] = title;
					initNotification(title, content, 0);
					if (title.contains("P"))
						power = false;
					if (title.contains("B"))
						body = false;
					if (title.contains("C"))
						chassis = false;
					if (title.contains("U"))
						unet = false;
					// if(D) Log.e(TAG, "Send notification!---notitoken is" +
					// notitoken);
					if (OBDApp.getActivityBack() == Base.APP_RUN_FOREGROUND
							&& basecontent != null) {
						((Base) basecontent).vehicleBodyCheck(false, power,
								body, chassis, unet);
					}
					mOBDcmd.setupdateFlag(false);
				}
				break;
			case OBDcmd.DTCNOEXIST:
				OBDApp = (com.ctg.ui.OBDApplication) getApplication();
				basecontent = OBDApp.baseAct;
				if (OBDApp.getActivityBack() == Base.APP_RUN_FOREGROUND) {
					if (basecontent != null)
						((Base) basecontent).vehicleBodyCheck(false, true,
								true, true, true);
					power = true;
					body = true;
					chassis = true;
					unet = true;
				}
				break;
			case OBDcmd.STATECHANGE:
				// 0: STATE_NONE;
				// 1: STATE_LISTEN;
				// 2: STATE_CONNECTING;
				// 3: STATE_CONNECTED;
				currentstate = msg.arg1;
				previousstate = msg.arg2;
//				if( obdSensor == null ){
//					obdSensor = new OBDSensor();
//				}
				
				if(sendDTCTime == 0){
					 CarDataService.mOBDcmd.appendDTCcodes("P1504 P0720 C0129 P0369");
					 sendDTCTime = 1;
					 this.obtainMessage(OBDcmd.DTCEXIST).sendToTarget();
				}
				if (((com.ctg.ui.OBDApplication) getApplication())
						.getActivityBack() == Base.APP_RUN_FOREGROUND) {
					if (localsetting != null) {
						localsetting.setBtStat(currentstate);
						//obdSensor.setBTState(currentstate);
						
					}
					if (currentstate == 0 || currentstate == 1
							|| currentstate == 2) {
						//obdSensor.setBTState(currentstate);
						basecontent = OBDApp.baseAct;
						if (OBDApp.getActivityBack() == Base.APP_RUN_FOREGROUND) {
							if (basecontent != null) {
								((Base) basecontent).disableBodyCheck();
								((Base) basecontent).vehicleBodyCheck(true,
										true, true, true, true);
							}
						}
					} else if (currentstate == 3) {
						//obdSensor.setBTState(currentstate);
						basecontent = OBDApp.baseAct;
						if (OBDApp.getActivityBack() == Base.APP_RUN_FOREGROUND) {
							if (basecontent != null)
								((Base) basecontent).enableBodyCheck();
						}
					} else {
						basecontent = OBDApp.baseAct;
						if (OBDApp.getActivityBack() == Base.APP_RUN_FOREGROUND) {
							if (basecontent != null)
								((Base) basecontent).disableBodyCheck();
						}
					}

					if (D)
						Log.e(TAG, "UPDATE real-time bt state");
				}
				if (currentstate == BluetoothService.STATE_CONNECTED)
					engineon = true;
				else
					engineon = false;
				if (D)
					Log.d(TAG, "BT connection state change as" + currentstate);
				break;
			case OBDcmd.VOLTVALUE:
				Log.e(TAG, "OBDcmd voltage" + OBDcmd.obdvoltage);
				if (engineon == false) {
					// voltage for engine shutdown
					// if(uploadcount < 2){
					if (((com.ctg.ui.OBDApplication) getApplication())
							.getActivityBack() == Base.APP_RUN_FOREGROUND) {
//						if (localsetting != null) {
//							localsetting.setVoltage(OBDcmd.obdvoltage, 2);
//						}
						Base.setCarVoltage(OBDcmd.obdvoltage);
					}
					// uploadcount ++;
					// }
					// Log.e(TAG, "The last engine shutdown OBDcmd voltage" +
					// OBDcmd.obdvoltage);
				} else {
					// then voltage for start engine
					// Log.e(TAG, "The engine start OBDcmd voltage" +
					// OBDcmd.obdvoltage);
					if (((com.ctg.ui.OBDApplication) getApplication())
							.getActivityBack() == Base.APP_RUN_FOREGROUND) {
						if (localsetting != null) {
							localsetting.setVoltage(OBDcmd.obdvoltage, 1);
						}
					}
				}
				// Update voltage info in seeting;
				// Write voltage/time into SD card;
				break;
			case OBDcmd.SHUTDOWN:
				if (((com.ctg.ui.OBDApplication) getApplication())
						.getActivityBack() == Base.APP_RUN_FOREGROUND) {
//					if (localpanel != null)
//						localpanel.setPanelReadData(0, 0, 0, 0);
				}
				engineon = false;
				break;
			case OBDcmd.VSUPPORT:
				supporthandler = OBDApp.getHandler();
				supportmsg.what = Base.UNSUPPORT;
				supporthandler.sendMessage(supportmsg);
				// Toast.makeText(mContext, R.string.vehicle_support,
				// Toast.LENGTH_SHORT).show();
				break;
			// case OBDcmd.TEST:
			// TestupdateWithNewLocation();
			// break;
			default:
				if (D)
					Log.d(TAG, "DTC response message without right type!");
				break;
			}
		}

	};
	
	private final Handler mEST527Handler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			Bundle bundle = msg.getData();
			String sa[] = null;
			switch (msg.what) {
			case getDataThread.RESPONSE_OK:
					String sOK = bundle.getString("RPS_OK");
					Toast.makeText(getApplicationContext(), sOK, Toast.LENGTH_SHORT).show();
					break;
			case getDataThread.RESPONSE_OBD_RT:
					String sRT = bundle.getString("RPS_RT").trim();
					sa = sRT.split(",");
					
					float mpg = getDataThread.getMPG(sa[7]);
					Log.i(TAG, "mEST527Handler mpg:"+mpg);
					if(((OBDApplication)getApplication()).getActivityBack() == Base.APP_RUN_FOREGROUND 
							&& Base.car_v.panelDlg != null){
						if(mpg == -100f){
							
						}							
						else{
							String txt = sa[7].substring(sa[7].indexOf("=") + 1);
							Base.car_v.panelDlg.setPanelReadData(txt, mpg);
						}
					}
					
				try {
					CarDataService.this.bat = Float.valueOf(sa[1].substring(sa[1].indexOf("=") + 1, sa[1].indexOf("v")));
					CarDataService.this.rpm = Integer.valueOf(sa[2].substring(sa[2].indexOf("=") + 1, sa[2].indexOf("rpm")));
					CarDataService.this.vss = Float.valueOf(sa[3].substring(sa[3].indexOf("=") + 1, sa[3].indexOf("km/h")));
					CarDataService.this.throtPos = Float.valueOf(sa[4].substring(sa[4].indexOf("=") + 1, sa[4].indexOf("%")));
					CarDataService.this.engineLoad = Float.valueOf(sa[5].substring(sa[5].indexOf("=") + 1, sa[5].indexOf("%")));
					CarDataService.this.temp = Float.valueOf(sa[6].substring(sa[6].indexOf("=") + 1, sa[6].length() - 1));
					if(sa[7].contains("L/h")){
						CarDataService.this.mpg = Float.valueOf(sa[7].substring(sa[7].indexOf("=") + 1, sa[7].indexOf("L/h")));
						CarDataService.this.isIdle = true;
					}
					else{
						CarDataService.this.mpg = Float.valueOf(sa[7].substring(sa[7].indexOf("=") + 1, sa[7].indexOf("L/100km")));
						CarDataService.this.isIdle = false;
					}
					CarDataService.this.avg_mpg = Float.valueOf(sa[8].substring(sa[8].indexOf("=") + 1, sa[8].indexOf("L/100km")));
					CarDataService.this.fli = Float.valueOf(sa[9].substring(sa[9].indexOf("=") + 1, sa[9].indexOf("%")));
					CarDataService.this.dtc = Integer.valueOf(sa[10].substring(sa[10].indexOf("=") + 1));
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					Toast.makeText(getApplicationContext(), "Car data is not properly parsed", Toast.LENGTH_SHORT).show();
				}
//					txvBAT.setText(getDataThread.cutString(strArray[1]));
//					txvRPM.setText(getDataThread.cutString(strArray[2]));
//					txvVSS.setText(getDataThread.cutString(strArray[3]));
//					txvTP.setText(getDataThread.cutString(strArray[4]));
//					txvLOD.setText(getDataThread.cutString(strArray[5]));
//					txvECT.setText(getDataThread.cutString(strArray[6]));
//					txvMPG.setText(getDataThread.cutString(strArray[7]));
//					txvAVM.setText(getDataThread.cutString(strArray[8]));
//					txvFLI.setText(getDataThread.cutString(strArray[9]));
//					txvDTN.setText(getDataThread.cutString(strArray[10]));
				break;
			case getDataThread.RESPONSE_OBD_AMT:
					String sAMT = bundle.getString("RPS_AMT");
					sa = sAMT.split(",");
					
//					txvDST.setText(getDataThread.cutString(strArray[1]));
//					txvADST.setText(getDataThread.cutString(strArray[2]));
//					txvTDST.setText(getDataThread.cutString(strArray[3]));
//					txvFUE.setText(getDataThread.cutString(strArray[4]));
//					txvTFUE.setText(getDataThread.cutString(strArray[5]));
//					txvCACC.setText(getDataThread.cutString(strArray[6]));
//					txvCDEC.setText(getDataThread.cutString(strArray[7]));
				break;			
			case getDataThread.RESPONSE_OBD_HBT:
					String sHBT = bundle.getString("RPS_HBT");
					sa = sHBT.split(",");
					
//					txvTPC.setText(getDataThread.cutString(strArray[1]));
//					txvTMT.setText(getDataThread.cutString(strArray[2]));
//					txvTST.setText(getDataThread.cutString(strArray[3]));
//					txvAWT.setText(getDataThread.cutString(strArray[4]));
//					txvASP.setText(getDataThread.cutString(strArray[5]));
//					txvMSP.setText(getDataThread.cutString(strArray[6]));
//					txvMRP.setText(getDataThread.cutString(strArray[7]));
//					txvTACC.setText(getDataThread.cutString(strArray[8]));
//					txvTDEC.setText(getDataThread.cutString(strArray[9]));
				break;
			case getDataThread.RESPONSE_OBD_DVC:
					String sDVC = bundle.getString("RPS_DVC");
					sa = sDVC.split(",");
//					
//					txvPROTOCOL.setText(getDataThread.cutString(strArray[1]));
//					txvSN.setText(getDataThread.cutString(strArray[2]));
//					txvBTName.setText(getDataThread.cutString(strArray[3]));
//					txvHARDVER.setText(getDataThread.cutString(strArray[4]));
//					txvSOFTVER.setText(getDataThread.cutString(strArray[5]));				
				break;
			case getDataThread.RESPONSE_OBD_DTC:
					String sDTC = bundle.getString("RPS_DTC");
					sa = sDTC.split(",");
					
//					txvTCC.setText(getDataThread.cutString(strArray[1]));
//					txvTCD.setText(getDataThread.cutString(strArray[2]));
					
//					if(mpDialog.isShowing())
//						mpDialog.cancel();
				break;
			case getDataThread.RESPONSE_OBD_RTC:					
					String sRTC = bundle.getString("RPS_RTC");
					sa = sRTC.split(",");
					
//					setCurDate(getDataThread.cutString(strArray[1]));
//					setCurTime(getDataThread.cutString(strArray[2]));					
				break;
			case getDataThread.RESPONSE_OBD_RTC_SET_OK:
//					Toast.makeText(getApplicationContext(), R.string.msg_rtc_set_ok, Toast.LENGTH_SHORT).show();
				break;
				
			case getDataThread.RESPONSE_OBD_HIS_RECORD:
				//Log.i("Handler", bundle.getString("RPS_HIS_RECORD"));
//				Intent HisRecordIntent = new Intent(HIS_RECORD_ACTION);
//				HisRecordIntent.putExtra("RPS_HIS_RECORD", bundle.getString("RPS_HIS_RECORD"));
//				sendBroadcast(HisRecordIntent);
				break;
			case getDataThread.RESPONSE_OBD_HIS_RECORD_SEND_OK:		
				//Log.i("Handler", bundle.getString("RPS_HIS_SENDOK"));
//				Intent SendOkIntent = new Intent(HIS_RECORD_SEND_OK_ACTION);
//				SendOkIntent.putExtra("RPS_HIS_SENDOK", bundle.getString("RPS_HIS_SENDOK"));
//				sendBroadcast(SendOkIntent);
				break;
			default:
				break;
			}
		}		
	};
	
	private final BroadcastReceiver networkChangeReceiver = new BroadcastReceiver(){
		
		@Override
		public void onReceive(final Context context, final Intent intent){
			  final ConnectivityManager connMgr = (ConnectivityManager) context
		                .getSystemService(Context.CONNECTIVITY_SERVICE);
			  
			  final android.net.NetworkInfo wifi = connMgr
		                .getNetworkInfo(ConnectivityManager.TYPE_WIFI);
			  
			  if(wifi.isConnected() && upGPS != null){
				  upGPS.UploadFiles(mGpsuploadURL);
			  }
		}
	};

	// //////////////////////////////////////////////////////////////////////////
	// --------------Notification
	// Start------------------------------------------
	// //////////////////////////////////////////////////////////////////////////
	public NotificationManager createNotiManager() {
		return (NotificationManager) this
				.getSystemService(android.content.Context.NOTIFICATION_SERVICE);
	}
	//type 0:dtc, 1:geofence
	public void initNotification(String title, String content, int type) {
		if(localNM == null)
			localNM = createNotiManager();		
		Intent notificationintent = new Intent(this, Base.class);
		if(type == 0){
			if (D)
				Log.d(TAG, "temporary is title" + title);
			String temporary = title + "+" + content;
			if (D)
				Log.d(TAG, "temporary is" + temporary);
			mnotification = new Notification(android.R.drawable.star_on,
					"Vehicle Malfunction Report", System.currentTimeMillis());
			notificationintent.putExtra("dtcs", temporary);
		}
		else if(type == 1){
			mnotification = new Notification(R.drawable.fence_gry,
					"geofence notification", System.currentTimeMillis());
			notificationintent.putExtra("geofence", content);
			notificationintent.putExtra("geofenceIdx", fenceIdx);
		}
		mnotification.flags |= Notification.FLAG_AUTO_CANCEL;
		mnotification.flags |= Notification.FLAG_SHOW_LIGHTS;
		mnotification.defaults = Notification.DEFAULT_LIGHTS;
		mnotification.ledARGB = Color.BLUE;
		mnotification.ledOnMS = 3000; // 3s
		
		notificationintent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);// FLAG_ACTIVITY_SINGLE_TOP

		pendingintent = PendingIntent.getActivity(this, notitoken,
				notificationintent, PendingIntent.FLAG_UPDATE_CURRENT);
		if (D)
			Log.d(TAG, "init notification");
		if (title != null && content != null) {
			mnotification.setLatestEventInfo(this, title, content,
					pendingintent);
			if (D)
				Log.d(TAG, "title and content is" + title + "+" + content);
		} else {
			if (D)
				Log.d(TAG, "title/content is null!");
		}
		// This way can be used to show notification on bar;
		if (localNM != null && mnotification != null) {
			localNM.notify(notitoken, mnotification);
			if (D)
				Log.d(TAG, "send notification" + notitoken);
		}
		notitoken++;
	}

	public void fillContent(String title, String content) {
		if (title != null && content != null) {
			mnotification.setLatestEventInfo(this, title, content,
					pendingintent);
			if (D)
				Log.d(TAG, "title and content is" + title + "+" + content);
		} else {
			if (D)
				Log.d(TAG, "title/content is null!");
		}
	}

	public void sendNotification(boolean updateflag, String title,
			String content) {
		if (updateflag && localNM != null && mnotification != null) {
			// update title and content of notification;
			fillContent(title, content);
			// send notification and show;
			localNM.notify(notitoken, mnotification);
			// localNM.notify(10, mnotification);
			// StartForeground(1, notificationintent);
			if (D)
				Log.d(TAG, "send notification");
		}
	}

	public void clearSingleNotification(String title) {
		NotificationManager notificationManager = (NotificationManager) this
				.getSystemService(NOTIFICATION_SERVICE);
		int i = 0;
		i = FindDtc(title);
		if (i != DTCNUM)
			notificationManager.cancel(i);
	}

	public void clearAllNotification() {
		NotificationManager notificationManager = (NotificationManager) this
				.getSystemService(NOTIFICATION_SERVICE);
		notificationManager.cancelAll();
	}

	public int FindDtc(String title) {
		int i = 0;
		for (i = 0; i < DTCNUM; i++) {
			if (dtcArray[i] != null && dtcArray[i].contentEquals(title) == true) {
				if (D)
					Log.d(TAG, "dtc array[] i" + i);
				if (D)
					Log.d(TAG, "dtc array[]" + dtcArray[i]);
				return i;
			}
		}
		return DTCNUM;
	}

	// //////////////////////////////////////////////////////////////////////////
	// --------------Notification
	// End--------------------------------------------
	// //////////////////////////////////////////////////////////////////////////

//	private GpsStatus.Listener statusListener = new GpsStatus.Listener()
//
//	{
//		public void onGpsStatusChanged(int event) {
//			// TODO Auto-generated method stub
//			GpsStatus gpsStatus = locationManager.getGpsStatus(null);
//			Log.v(TAG, "GPS status listener  ");
//			// Utils.DisplayToastShort(GPSService.this,
//			// "GPS status listener  ");
//			switch (event) {
//			case GpsStatus.GPS_EVENT_FIRST_FIX: {
//				break;
//			}
//
//			case GpsStatus.GPS_EVENT_SATELLITE_STATUS: {
//
//				Iterable<GpsSatellite> allSatellites;
//				allSatellites = gpsStatus.getSatellites();
//				Iterator<GpsSatellite> iterator = allSatellites.iterator();
//				int numOfSatellites = 0;
//				int maxSatellites = gpsStatus.getMaxSatellites();
//				while (iterator.hasNext() && numOfSatellites < maxSatellites) {
//					GpsSatellite gpsSatellite = (GpsSatellite) iterator.next();
//					if (gpsSatellite.usedInFix()) {
//						numOfSatellites++;
//					}
//					// numOfSatellites++;
//					// iterator.next();
//				}
//				if (numOfSatellites < 3) {
//					bGPSFixed = false;
//				} else {
//					bGPSFixed = true;
//				}
//				break;
//			}
//
//			case GpsStatus.GPS_EVENT_STARTED: {
//				break;
//			}
//
//			case GpsStatus.GPS_EVENT_STOPPED: {
//				break;
//			}
//
//			default:
//				break;
//			}
//		}
//	};
}