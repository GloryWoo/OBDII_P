package com.ctg.sensor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.ctg.crash.LogRecord;
import com.ctg.ui.Base;
import com.ctg.ui.OBDApplication;
import com.ctg.util.Preference;
import com.ctg.util.Util;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.widget.TextView;

public class OBDSensor implements SensorEventListener {
	public static final int UPLOAD_TIME_INTERVAL = 180000;	// 3 MIN
	public static final int GPS_UPDATE_MSG = 0x500;
	private static final int SAMPLE_RATE = 50000;	// us	
	private static boolean D = true;
	private static final int AverageCount = 10;
	private static final int ArrayCount = 20;
	private SensorManager mSensorManager;
	private Context mContext;
	private SimpleDateFormat sdf;
	private SimpleDateFormat sdf_up;
	private SimpleDateFormat sdf_param;
	private SimpleDateFormat sdf_file;
	private float[] sensorVals;
	private float[] avgSensorVal;
	float[][] sensorValArr;
	boolean startRecordBehavior;
	boolean startUploadData;
	String sensor_dir;
	String sensor_path;
	FileWriter sensor_writer;
	long mStartTime;
	long mCurTime;
	Queue<String> uploadPathQue;
	Queue<String> uploadURLQue;
	long mLastLocationMillis;
	Location mCurLocation, mLastLocation;
	boolean isGPSFix;
	AlertDialog enableGPSDlg;
	public boolean gpsEnable; 
	int curIdx;
	UpLoadSensor upSensor;
	int block_id = 1;
	int timer_count = 0;
	String startTime;
	String endTime;
	int overSpdCnt, brakeCnt, turnCnt, gasCnt, criticalPoint;
	String timeStamp;
	StringBuffer mSb;
	String lastName;
	String curDate;
	float[] accelerometerValues = null;
	float[] magneticFieldValues = null;
	String sessionId;
//	TextView sensor_switch;
	LocationManager locationManager;
	LocationListener locationListener;
	
	public OBDSensor(Context context){
		mContext = context;
		sessionId = Preference.getInstance(mContext.getApplicationContext()).getSessionId();
		initData();
	}
	
	void downExecute(){
		long end_time = System.currentTimeMillis();
		String url = Base.DB_BEHAVIOR_SERVER + "/getDriverBehavior";
		endTime = sdf_up.format(new Date(end_time));		
		StringBuffer regStrBuf;
		
		regStrBuf = new StringBuffer();
		regStrBuf.append("start_time=");
		regStrBuf.append(startTime);
		regStrBuf.append("&end_time=");
		regStrBuf.append(endTime);
		byte retBuf[] = UpLoadSensor.sendPost(url, regStrBuf.toString(), sessionId);
		if(retBuf == null)
			return;
		String retStr = new String(retBuf);
		
		overSpdCnt = 0; 
		brakeCnt = 0;
		turnCnt = 0;
		gasCnt = 0;
		try {
			JSONArray jsarr = new JSONArray(retStr);
			JSONObject obj;
			JSONObject subObj;
			JSONArray subArr;
			boolean ret = false;
			for(int i = 0; i < jsarr.length(); i++){
				obj = jsarr.getJSONObject(i);
				if(obj.has("m_safe_score_per_trip"))
					criticalPoint = Integer.parseInt(obj.getString("m_safe_score_per_trip"));
				if(obj.has("m_analyzed_events")){
					ret = true;
					subObj = obj.getJSONObject("m_analyzed_events");
					Iterator<?> it = subObj.keys();
					String key;
					while(it.hasNext()){
						key = (String) it.next();
						subArr = (JSONArray)subObj.get(key);
						if(key.equals("10"))
							overSpdCnt = subArr.length();
						else if(key.equals("7"))
							brakeCnt = subArr.length();
						else if(key.equals("3"))
							turnCnt = subArr.length();
						else if(key.equals("6"))
							gasCnt = subArr.length();
					}
				}
			}
//			if(ret)
//				handler.obtainMessage(1).sendToTarget();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	public boolean startLocate(){
		boolean ret = initAndroidLocation();
		if(!ret){
			return ret;
		}
		registSensor();
		return true;
	}
	
	public void stopLocate(){
		unRegistSensor();
		locationManager.removeUpdates(locationListener);
		if(sensor_writer != null){
			try {
				
				sensor_writer = new FileWriter(sensor_path, true);
				sensor_writer.write(mSb.toString());
				sensor_writer.close();
			 
				block_id = -1;
				String url = Base.DB_BEHAVIOR_SERVER + "/sensorLog?trip_id="+timeStamp+"&block_id="+block_id;
				uploadPathQue.add(sensor_path);
				uploadURLQue.add(url);
				OBDApplication.getThreadPool().execute(runnable);					
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public boolean initAndroidLocation(){
		Log.v("Android LOCATION", "init");
		
		locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
		gpsEnable = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER); 
		if(!gpsEnable){
			//sensor_switch.setText("关");
			gpsEnableNotify();
			return false;
		}
		else{
			//sensor_switch.setText("开");
		}

		locationListener = new LocationListener() {
			public void onLocationChanged(Location location) {
				
				mLastLocationMillis = System.currentTimeMillis();
				mLastLocation = location;
				
				if(startRecordBehavior){					
			    	if (location != null) {			    		
			    		
			    		mCurTime =  System.currentTimeMillis();

			    		if(mCurLocation == null){
							mStartTime = mCurTime;
							sensor_path = sensor_dir + "OBDII_DB" + Base.loginUser + sdf_file.format(new Date(mStartTime)) + ".csv";			
							block_id = 1;
							timeStamp = sdf_param.format(new Date(mStartTime));		
							mSb = new StringBuffer();
						}
						mCurLocation = location;
			    		
						double lat = 0.0, lng = 0.0;
						String provider = "";
						float speed = 0f, bear = 0f, accuracy = 0f;	
						int i = 0;
					
						provider = mCurLocation.getProvider();
						speed = mCurLocation.getSpeed();
						bear = mCurLocation.getBearing();
						accuracy = mCurLocation.getAccuracy();
						if(isGPSFix)
						{
							lng = mCurLocation.getLongitude();
							lat = mCurLocation.getLatitude();
						}
									    		
						//StringBuffer sb = new StringBuffer();					
						mSb.append(sdf.format(new Date(mCurTime)));
						mSb.append("," +provider);
						mSb.append("," +String.format("%.6f,%.6f,%.6f", speed, bear, accuracy));
						mSb.append("," +String.format("%.6f", lng));
						mSb.append("," + String.format("%.6f", lat));
						
						for(i = 0; i < 9; i++){
							avgSensorVal[i] = averageOfArr(sensorValArr[i], curIdx+1, 10);
						}
						mSb.append("," + String.format("%.6f,%.6f,%.6f,%.6f,%.6f,%.6f,%.6f,%.6f,%.6f", 
								avgSensorVal[0], avgSensorVal[1], avgSensorVal[2],
								avgSensorVal[3], avgSensorVal[4], avgSensorVal[5],	
								avgSensorVal[6], avgSensorVal[7], avgSensorVal[8]));
						for(i = 0; i < 9; i++){
							avgSensorVal[i] = averageOfArr(sensorValArr[i], curIdx+11, 10);
						}
						mSb.append("," + String.format("%.6f,%.6f,%.6f,%.6f,%.6f,%.6f,%.6f,%.6f,%.6f", 
								avgSensorVal[0], avgSensorVal[1], avgSensorVal[2],
								avgSensorVal[3], avgSensorVal[4], avgSensorVal[5],	
								avgSensorVal[6], avgSensorVal[7], avgSensorVal[8]));						
					
						//if(mBluetoothSet != null && mBluetoothSet.connect_state != 2)
							mSb.append(String.format(",%d, ,\n", 0));						
						//else{
						//	mSb.append(String.format(",%d,%s,\n", 1, obdCarSpd));
						//}
						
			    					    		
						if(mCurTime - mStartTime > UPLOAD_TIME_INTERVAL){
							try {
								sensor_writer = new FileWriter(sensor_path, true);
								sensor_writer.write(mSb.toString());
								sensor_writer.close();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} 
							mSb = new StringBuffer();
							String url = Base.DB_BEHAVIOR_SERVER + "/sensorLog?trip_id="+timeStamp+"&block_id="+block_id;
							uploadPathQue.add(sensor_path);
							uploadURLQue.add(url);
							mStartTime = mCurTime;
							block_id++;
							
							OBDApplication.getThreadPool().execute(runnable);
							// 新建下一个文件
							sensor_path = Base.getSDPath() + "/OBDII/sensor/OBDII_DB" + Base.loginUser + sdf_file.format(new Date(mCurTime))+".csv";
						}
			    	}
				}
		    }

		    public void onStatusChanged(String provider, int status, Bundle extras) {}

		    public void onProviderEnabled(String provider) {}

		    public void onProviderDisabled(String provider) {}
		  };
		//locationManager.removeUpdates(locationListener);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
		locationManager.addGpsStatusListener(new MyGPSListener());	
		return true;
	}
	
	private class MyGPSListener implements GpsStatus.Listener {
	    public void onGpsStatusChanged(int event) {
	        switch (event) {
	            case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
	                if (mLastLocation != null)
	                    isGPSFix = (System.currentTimeMillis() - mLastLocationMillis) < 3000;
	                if (isGPSFix) { // A fix has been acquired.
	                	//sensor_status.setText("有信号");
	                } else { // The fix has been lost.
	                	//sensor_status.setText("无信号");
	                }
	                break;
	            case GpsStatus.GPS_EVENT_FIRST_FIX:
	                isGPSFix = true;
	                //sensor_status.setText("有信号");
	                break;
	             
	            case GpsStatus.GPS_EVENT_STARTED:  
	            	//sensor_switch.setText("开");
	            	//sensor_status.setText("无信号");
	            	gpsEnable = true;
	            	break;
	            	
	            case GpsStatus.GPS_EVENT_STOPPED:  
	            	//sensor_switch.setText("关");
	            	//sensor_status.setText("");
	            	isGPSFix = false;
	            	gpsEnable = false;
	            	break;
	            default:
	            	break;	            		
	        }
	    }
	}
	
//	public Handler gpsHander = new Handler(){
//		public void handleMessage(Message msg) {
//			switch(msg.what){
//			case GPS_UPDATE_MSG:
//				Bundle bundle = msg.getData();
//				Location location =  (Location) bundle.get("location");
//		    	if (location != null) {			    		
//		    		
//		    		mCurTime =  System.currentTimeMillis();
//
//		    		if(mCurLocation == null){
//						mStartTime = mCurTime;
//						sensor_path = sensor_dir + "OBDII_DB" + Base.loginUser + sdf_file.format(new Date(mStartTime)) + ".csv";			
//						block_id = 1;
//						timeStamp = sdf_param.format(new Date(mStartTime));		
//						mSb = new StringBuffer();
//					}
//					mCurLocation = location;
//		    		
//					double lat = 0.0, lng = 0.0;
//					String provider = "";
//					float speed = 0f, bear = 0f, accuracy = 0f;	
//					int i = 0;
//				
//					provider = mCurLocation.getProvider();
//					speed = mCurLocation.getSpeed();
//					bear = mCurLocation.getBearing();
//					accuracy = mCurLocation.getAccuracy();
//					if(isGPSFix)
//					{
//						lng = mCurLocation.getLongitude();
//						lat = mCurLocation.getLatitude();
//					}
//								    		
//					//StringBuffer sb = new StringBuffer();					
//					mSb.append(sdf.format(new Date(mCurTime)));
//					mSb.append("," +provider);
//					mSb.append("," +String.format("%.6f,%.6f,%.6f", speed, bear, accuracy));
//					mSb.append("," +String.format("%.6f", lng));
//					mSb.append("," + String.format("%.6f", lat));
//					
//					for(i = 0; i < 9; i++){
//						avgSensorVal[i] = averageOfArr(sensorValArr[i], curIdx+1, 10);
//					}
//					mSb.append("," + String.format("%.6f,%.6f,%.6f,%.6f,%.6f,%.6f,%.6f,%.6f,%.6f", 
//							avgSensorVal[0], avgSensorVal[1], avgSensorVal[2],
//							avgSensorVal[3], avgSensorVal[4], avgSensorVal[5],	
//							avgSensorVal[6], avgSensorVal[7], avgSensorVal[8]));
//					for(i = 0; i < 9; i++){
//						avgSensorVal[i] = averageOfArr(sensorValArr[i], curIdx+11, 10);
//					}
//					mSb.append("," + String.format("%.6f,%.6f,%.6f,%.6f,%.6f,%.6f,%.6f,%.6f,%.6f", 
//							avgSensorVal[0], avgSensorVal[1], avgSensorVal[2],
//							avgSensorVal[3], avgSensorVal[4], avgSensorVal[5],	
//							avgSensorVal[6], avgSensorVal[7], avgSensorVal[8]));						
//				
//					mSb.append(String.format(",%d, ,\n", 0));						
//					
//					
//		    					    		
//					if(mCurTime - mStartTime > UPLOAD_TIME_INTERVAL){
//						try {
//							sensor_writer = new FileWriter(sensor_path, true);
//							sensor_writer.write(mSb.toString());
//							sensor_writer.close();
//						} catch (IOException e) {
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//						} 
//						mSb = new StringBuffer();
//						String url = Base.DB_BEHAVIOR_SERVER + "/sensorLog?trip_id="+timeStamp+"&block_id="+block_id;
//						uploadPathQue.add(sensor_path);
//						uploadURLQue.add(url);
//						mStartTime = mCurTime;
//						block_id++;
//						
//						Base.OBDApp.getThreadPool().execute(runnable);
//						// 新建下一个文件
//						sensor_path = Base.getSDPath() + "/OBDII/sensor/OBDII_DB" + Base.loginUser + sdf_file.format(new Date(mCurTime))+".csv";
//					}
//		    	}
//
//				break;
//			}
//		}
//	};
	
	void uploadExecute(){
		String path;
		String zipPath;
		String url;
		String md5str;
		String ret;
		
		while(!uploadPathQue.isEmpty()){
			path = uploadPathQue.remove();
			url = uploadURLQue.remove();
			
			zipPath = path+".gz";
			try {
				Util.zipFile(path, zipPath);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				continue;
			}
			md5str = Util.md5sum(zipPath);
			url += "&md5_code="+md5str;
			ret = UpLoadSensor.uploadFile(new File(zipPath), sessionId, url);
			if(ret != null && ret.contains("200")){
				new File(zipPath).delete();
			}
		}
	}
	
	
	Runnable runnable = new Runnable() {
		@Override
		public void run() {
			uploadExecute();
			try {
				Thread.sleep(10000);	// download after 10 seconds
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			//downExecute();
		}

	};


	
	public void initData(){
		uploadPathQue = new ConcurrentLinkedQueue<String>();
		uploadURLQue = new ConcurrentLinkedQueue<String>();
		sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		sdf_up = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		sdf_param = new SimpleDateFormat("yyyyMMddHHmmss");
		sdf_file = new SimpleDateFormat("HHmmss");
		sensor_dir = Base.getSDPath() + "/OBDII/sensor/";
		File dir = new File(sensor_dir);
		dir.mkdirs();
		curIdx = 0;
		sensorVals = new float[9];
		avgSensorVal = new float[9];
		for(int i = 0; i < sensorVals.length; i++) {
			sensorVals[i] = 0f;
			avgSensorVal[i] = 0f;
		}
		sensorValArr = new float[9][ArrayCount];
	}


		
	public void gpsEnableNotify() {
		if (enableGPSDlg == null) {
			enableGPSDlg = new AlertDialog.Builder(mContext)
				.setTitle("GPS未打开")
				.setMessage("请打开GPS定位开关，否则无法进行驾驶行为分析！")
				.setPositiveButton("确定",new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog,
							int which) {
						dialog.cancel();
						enableGPSDlg = null;
						Intent intent = new Intent();
						intent.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
						intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						try {
							mContext.startActivity(intent);
						} 
						catch (ActivityNotFoundException ex) {

							// The Android SDK doc says that the
							// location settings activity
							// may not be found. In that case show
							// the general settings.

							// General settings activity
							intent.setAction(Settings.ACTION_SETTINGS);
							try {
								mContext.startActivity(intent);
							} 
							catch (Exception e) {
							}
						}
					}
				}).create();
			enableGPSDlg.setOnCancelListener(new OnCancelListener(){

				@Override
				public void onCancel(DialogInterface dialog) {
					// TODO Auto-generated method stub
					enableGPSDlg = null;
				}
				
			});
		}
		enableGPSDlg.show();
	}
	
	float averageOfArr(float[] arr, int start, int len){
		int length = arr.length;
		float total = 0;
		float average = 0;
		
		if(len > length)
			return 0;
		
		if(start > length - 1){
			start %= length;
		}
		
		for(int i = 0; i < len; i++){
			total += arr[(start+i)%length];
		}
		average = total/len;
		return average;
	}
	

	public void registSensor() {
		mSensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
		mSensorManager.registerListener(OBDSensor.this,
				mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),SAMPLE_RATE);
		mSensorManager.registerListener(OBDSensor.this,
				mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),SAMPLE_RATE);
		mSensorManager.registerListener(OBDSensor.this,
				mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SAMPLE_RATE);
		startTime = sdf_up.format(new Date(System.currentTimeMillis()));

	}
	
	public void unRegistSensor() {
		mSensorManager.unregisterListener(OBDSensor.this);
	}	

	@Override
	public void onSensorChanged(SensorEvent event) {
		mCurTime = System.currentTimeMillis();	
		
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			sensorVals[0] = event.values[0];
			sensorVals[1] = event.values[1];
			sensorVals[2] = event.values[2];
			
			for(int i = 0; i<sensorValArr.length; i++) {
				sensorValArr[i][curIdx] = sensorVals[i];
			}
			
			curIdx++;	
			if(curIdx == ArrayCount){
				curIdx = 0;
			}
		}
		else if(event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
			sensorVals[3] = event.values[0];
			sensorVals[4] = event.values[1];
			sensorVals[5] = event.values[2];
		}
		else if(event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
			sensorVals[6] = event.values[0];
			sensorVals[7] = event.values[1];
			sensorVals[8] = event.values[2];
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub

	}



}
