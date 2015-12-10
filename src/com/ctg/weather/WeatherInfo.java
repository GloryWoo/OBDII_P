package com.ctg.weather;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.content.Context;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.ctg.ui.Base;
import com.ctg.util.Preference;

public class WeatherInfo{
	public static final String TAG = "WeatherInfo";
	public static final String REPORT7_URL = "http://m.weathercn.com/common/7d.do?cid=";

	public static final int MSG_WEATHER_DATA_PROCESSING = 0x01;
	public static final int MSG_WEATHER_DATA_READY = 0x10;
	public static final String WEATHER_DATA_READY_STR = "weather data ready";
	
	public static final String SUNSHINE_GUARD = "防晒指数";
	public static final String CLOTHING = "穿衣指数";
	public static final String COLDCATCH = "感冒指数";
	public static final String HEATSTROKE = "中暑指数";
	public static final String SPORT = "运动指数";
	public static final String ULTRAVIOLET = "紫外线指数";
	public static final String COLD = "风寒指数";
	
	public static final String CARWASHING = "洗车指数";
	public static final String COSMETIC = "化妆指数";
	public static final String HAIRCUTTING = "美发指数";
	public static final String ROAD = "路况指数";
	public static final String FITTINESS = "舒适度指数";
	public static final String CLOTHING_SHINE = "晾晒指数";
	public static final String TRAFFIC = "交通指数";
	public static final String AIRCONDITIONG = "空调开启指数";
	public static final String UMBRELLA = "雨伞指数";
	
	public static final String MODE = "心情指数";
	public static final String BOATING = "划船指数";
	public static final String BEER = "啤酒指数";
	public static final String TRAVEL = "旅游指数";
	public static final String APPOINT = "约会指数";
	public static final String STREETWALK = "逛街指数";
	public static final String NIGHTLIFE = "夜生活指数";
	public static final String FISHING = "钓鱼指数";
	public static final String KITING = "放风筝指数";
	
	public String weather_city = null;
	public String load_weather_city = null;
	public WeatherUtil myWeather = null;
	
	public List<WeatherReport> listWeatherReport = null;
	public String[] pm25Report = null;
	public Map<String, String> listWeatherIndexMp = null;
	public WeatherDetail todayDetail = null;
	
	GetWeatherThread mythread = null;
	GetAddListThread myGetAddThread = null;	
	boolean myGetAddThreadRun;
	//public Windshield windshld = null;
	Base baseAct;
	//boolean dataInited;
	List<Address> addList = null;  
	public static double latitude;
	public static double longitude;
	boolean getTodayWeather = false;
	//public static SocketClient upGpsSocket;

	//WifiManager wifiMgr; 
	
	public WeatherInfo(Context context) {
		LocationManager locationManager = null;  
		String serviceName = Context.LOCATION_SERVICE;  
		baseAct = (Base)context;
//	    locationManager = (LocationManager)baseAct.getSystemService(serviceName);  
//	    String provider = LocationManager.GPS_PROVIDER;  
	    //wifiMgr = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
	    //String provider = LocationManager.NETWORK_PROVIDER;  
//	     
//	    Criteria criteria = new Criteria();  
//	    criteria.setAccuracy(Criteria.ACCURACY_FINE);  
//	    criteria.setAltitudeRequired(false);  
//	    criteria.setBearingRequired(false);  
//	    criteria.setCostAllowed(true);  
//	    criteria.setPowerRequirement(Criteria.POWER_LOW);  	    
//	    Location location = locationManager.getLastKnownLocation(provider);  
	    //updateWithNewLocation(location); 
	   
//	    locationManager.requestLocationUpdates(provider, 3000, 10,  locationListener);//100
 

		load_weather_city = last_city_init();
	    
	    weatherGetStart();
        
        //test
//        new Thread(new testUpdateLocation()).start();
//        testStart = true;
	    //dataInited = false;
	}

	public void weatherGetStart(){        
//       if(myGetAddThread == null)
//    	   myGetAddThread = new GetAddListThread();
//       if(!myGetAddThreadRun){
//	       myGetAddThreadRun = true;
//	       new Thread(myGetAddThread).start();
//       }
		if(baseAct.baidu_v == null)
			return;
      	weather_city = baseAct.baidu_v.mCity;
    	if(mythread == null)
       	    mythread = new GetWeatherThread();	    	               
	    new Thread(mythread).start();
	}
	

	
	public int getSeason(){
		Calendar cal = Calendar.getInstance();
		int month = cal.get(Calendar.MONTH);
		int season = (month)/3;
				
		return season;
	}
	
//    private void updateWithNewLocation(Location location) {  
//	      
//	       if(Base.OBDApp.getActivityBack() != 1)
//	    	   return;
//	       String latLongString = "";  
//	       TextView myLocationText; 
//	       double lat = 0.0;  
//	       double lng = 0.0;
//	       long curMilli;
//	       boolean trackEnable = Preference.getInstance(baseAct.getApplicationContext()).getGpsMonitor();
//	       boolean gpsEnable = baseAct.locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
//	       //myLocationText = (TextView)baseAct.findViewById(R.id.myLocationText);  
//	       if (location != null) {  
//		        lat = location.getLatitude();  
//		        lng = location.getLongitude();  
//		        latLongString = "纬度:" + lat + "\n经度:" + lng;  
//	       } else {  
//	    	   latLongString = "获取经纬度失败"; 
//	    	   return;
//	       }  
//	       longitude = lng;
//	       latitude = lat;
//    }
//   private final LocationListener locationListener = new LocationListener() {  
//       public void onLocationChanged(Location location) {  
//    	   updateWithNewLocation(location);  
//       }  
//       public void onProviderDisabled(String provider){  
//    	   updateWithNewLocation(null);  
//       }  
//       public void onProviderEnabled(String provider){ }  
//       public void onStatusChanged(String provider, int status, Bundle extras){ }  
//   };
   
	/*private Handler mCommonHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {

			switch (msg.what) {
				case MSG_WEATHER_DATA_READY:
					weather_data = msg.getData().getByteArray(WEATHER_DATA_READY_STR);
					break;
			}
		}
	};*/
//    public boolean testStart = false;
//    static int testSendCount = 0;
//    Location testLoc = new Location("gps");
//    double testLati, testLogi;
//    class testUpdateLocation implements Runnable{
//
//		@Override
//		public void run() {
//			// TODO Auto-generated method stub
//			while(testStart){
//				try {
//					Thread.sleep(300L);
//				} catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//				testSendCount++;
//				testLati = 31.17061534747414D;
//				testLogi = 121.39410824768814D;
//				testLoc = new Location("gps");
//				testLati += 0.000000002 * testSendCount;
//				testLogi += 0.000000003 * testSendCount;
//				testLoc.setLatitude(testLati);
//				testLoc.setLongitude(testLogi);
//				updateWithNewLocation(testLoc);
//				
//			}
//		}
//    	
//    };	
    



   
    public void onDestroy(){

    }   
	
   class GetAddListThread extends Thread{
	    	
	    public synchronized void run() {
	    	while(myGetAddThreadRun){
		    	try {	    		   
	    	        Geocoder ge = new Geocoder(baseAct, Locale.getDefault()); 
	    	        List<WeatherReport> listReport = null;
	    	        WeatherUtil weather;
	    	        WebPageUtil webpage;
	    	        String city = null;
	    	        try {  
	    	        	myGetAddThreadRun = false;
	    	            addList = ge.getFromLocation(latitude, longitude, 1);//(24.463, 118.1, 1); 	    	            
	    	        } catch (IOException e) {  
	    	            // TODO Auto-generated catch block  
	    	            e.printStackTrace();  
	    	            //sleep(30000L);
	    	            //last_city_init();
	    	            //myGetAddThreadRun = false;
	    	            Log.i(TAG,"GetAddListThread not get Address list");
	    	            if(weather_city != null){
	    	            	if(mythread == null)
	    	               	    mythread = new GetWeatherThread();	    	               
	    	        	    new Thread(mythread).start();
	    	            }
	    	            else if(load_weather_city != null){
	    	            	weather_city = load_weather_city;
	    	            	if(mythread == null)
	    	               	    mythread = new GetWeatherThread();	    	               
	    	        	    new Thread(mythread).start();
	    	            }
	    	            else{
	    	              	weather_city = "上海市";
	    	            	if(mythread == null)
	    	               	    mythread = new GetWeatherThread();	    	               
	    	        	    new Thread(mythread).start();
	    	            }
	    	        }  
	    	        if(addList!=null && addList.size()>0){  
	    	            for(int i=0; i<1; i++){  //addList.size()
	    	                Address ad = addList.get(i);  
	    	                //latLongString += "\n";  
	    	                //latLongString += ad.getCountryName() + ";" + ad.getLocality(); 
	    	                //city = ad.getCountryName();
	    	                weather_city = ad.getAdminArea(); 
	    	                if(weather_city != null && (load_weather_city == null || !weather_city.equals(load_weather_city)))
	    	                	last_city_save(weather_city);
	    	                //listReport = new WeatherUtil().getWeatherReports(city);
	    	                //webpage = new WebPageUtil();
	    	                if(mythread == null)
	    	               	    mythread = new GetWeatherThread();	    	               
	    	        	    new Thread(mythread).start();
	    	        	    //dataInited = true;	    	               	    	               	
	    	            }  
	    	        } 
	    	        else{
	    	            if(weather_city != null){
	    	            	if(mythread == null)
	    	               	    mythread = new GetWeatherThread();	    	               
	    	        	    new Thread(mythread).start();
	    	            }
	    	            else if(load_weather_city != null){
	    	            	weather_city = load_weather_city;
	    	            	if(mythread == null)
	    	               	    mythread = new GetWeatherThread();	    	               
	    	        	    new Thread(mythread).start();
	    	            }
	    	        }
	    	        sleep(5000L);
				 } catch (Exception e) {
					 // TODO Auto-generated catch block
					 e.printStackTrace();
					 myGetAddThreadRun = false;
				 } finally{
					 myGetAddThreadRun = false;
				 }
	    	}
	    	
	    }
    }
   
    class GetWeatherThread extends Thread{
   	    	
	    public synchronized void run() {
	    	try {
	    		myWeather = new WeatherUtil(baseAct);
	    		listWeatherReport = myWeather.getWeatherReports(weather_city);	
	    		pm25Report = myWeather.getPM25Report(baseAct.baidu_v.mCityPY);
	    		//listWeatherIndexMp = myWeather.getWeatherIndex();
	    		//todayDetail =  myWeather.getTodayDetail();
	    		//windshld = new Windshield(listWeatherReport, todayDetail);
	    		if(listWeatherReport != null && listWeatherReport.size() != 0){
		    		baseAct.msgHandler.obtainMessage(Base.WEATHER_READY).sendToTarget();
		    		getTodayWeather = true;
	    		}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    	
	    }
    }
    
	public static boolean last_city_save(String city){
		String path = Base.getSDPath() + "/OBDII/city_name";
		File file = new File(path);  
		byte mac_byte[] = new byte[100];
		int len = 0;
		boolean ret;
		
		try {
//			ret = file.createNewFile();
//			if(ret){
				FileOutputStream outs = new FileOutputStream(file);
				mac_byte = city.getBytes();
				outs.write(mac_byte);
				outs.close();
//			}
//			else
//				return false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return true;
	}
	
	public static String last_city_init(){
		
		String path = Base.getSDPath() + "/OBDII/city_name";
		File file = new File(path);  
		byte mac_byte[] = new byte[100];
		int len = 0;
		
		
		if(!file.exists()){
			return null;
		} 
		
		try {
			FileInputStream ins = new FileInputStream(file);
			if((len = ins.available()) > 0){
				ins.read(mac_byte, 0, len);	
				return new String(mac_byte, 0, len);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return null;
		
	}
	
	public static boolean last_city_py_save(String city){
		String path = Base.getSDPath() + "/OBDII/city_name_py";
		File file = new File(path);  
		byte mac_byte[] = new byte[100];
		int len = 0;
		boolean ret;
		
		try {
			FileOutputStream outs = new FileOutputStream(file);
			mac_byte = city.getBytes();
			outs.write(mac_byte);
			outs.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return true;
	}
	
	public static String last_city_py_init(){
		
		String path = Base.getSDPath() + "/OBDII/city_name_py";
		File file = new File(path);  
		byte mac_byte[] = new byte[100];
		int len = 0;
		
		
		if(!file.exists()){
			return null;
		} 
		
		try {
			FileInputStream ins = new FileInputStream(file);
			if((len = ins.available()) > 0){
				ins.read(mac_byte, 0, len);	
				return new String(mac_byte, 0, len);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return null;
		
	}
}