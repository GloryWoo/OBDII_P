package com.ctg.dbtrace;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;


import com.ctg.sensor.OBDSensor;
import com.ctg.sensor.UpLoadSensor;
import com.ctg.ui.Base;
import com.ctg.util.Preference;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.OptionalDataException;
import java.io.OutputStreamWriter;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.GZIPInputStream;

/**
 * Created by lee on 12/19/2014.
 */
public class DownloadTrace {
    static final public int MSG_DOWNLOAD_COMPLETE = 0;
    static final public int MSG_DOWNLOAD_FAIL = 1;
    static final public int MSG_DOWNLOAD_NO_MORE = 2;
    private String mFileName;
    private Date mStartTime;
    private Date mEndTime;
    private Context context;

    public ArrayList<StatisticsData> traceList;
//    public List<List<GPS>> pointList;
    public ArrayList<DrivingEvent> eventList;

    public DownloadDelegate downloadDelegate=null;
    
    private boolean isOver = false;
    SimpleDateFormat sdf_up;
    public void Download(Context ctx, Date startTime, Date endTime, boolean trackable){
        context = ctx;
        isOver = trackable;

        long timeLong = endTime.getTime() - startTime.getTime();
        if (timeLong<60*1000) {//less 60 second
            if(downloadDelegate!=null)
                downloadDelegate.downloadComplete(MSG_DOWNLOAD_NO_MORE);
            return;
        }
        sdf_up = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        mStartTime = startTime;
        mEndTime = endTime;

        Thread thread = new Thread(runnable);
        thread.start();
        traceList = new ArrayList<StatisticsData>();
        eventList = new ArrayList<DrivingEvent>();
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            getDataFromServer();
        }
    };
 
    @SuppressWarnings("unchecked")
	private void getDataFromServer(){
        String version = "1.01";//((com.ctg.ui.OBDApplication)context.getApplicationContext()).getVersion();
        String sessionId = Preference.getInstance(context.getApplicationContext()).getDBSessionId();

        //HttpClient httpClient = new DefaultHttpClient();
        String url= Base.DB_BEHAVIOR_SERVER + "/getDriverBehavior";
 
//        HttpPost httpPost = new HttpPost(url);
//
//        httpPost.setHeader("X-API-version", String.format("%s", version));
//        httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
//        httpPost.setHeader("accept", "*/*");
//        httpPost.setHeader("connection", "Keep-Alive");
//        httpPost.setHeader("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
//        httpPost.setHeader("X-token", sessionidExist);
//
//        HttpResponse response=null;
//        HttpEntity entity=null;
//        InputStream input=null;
//        List<NameValuePair> params=new ArrayList<NameValuePair>();
		StringBuffer regStrBuf = new StringBuffer();
		regStrBuf.append("start_time=");
		regStrBuf.append(sdf_up.format(mStartTime));
		regStrBuf.append("&end_time=");
		regStrBuf.append(sdf_up.format(mEndTime));
		
    	byte retBuf[] = UpLoadSensor.sendPost(url, regStrBuf.toString(), sessionId);
		if(retBuf == null)
			return;
		String retStr = new String(retBuf);
		StatisticsData trip;
		DrivingEvent evt;
		int overSpdCnt = 0; 
		int brakeCnt = 0;
		int turnCnt = 0;
		int gasCnt = 0;
		long tripId;
		String userId;
		String startTmStr, endTmStr;
		Timestamp startTm;
		Timestamp endTm;
		double sLat, sLon, eLat, eLon;
		float distance;
		float averSpd;
		int scoreOne;
		int scoreAll;
		int i = 0;
		int j = 0;
		Timestamp evtStTm;
		Timestamp evtEnTm;
		double evtLat;
		double evtLon;
//		String evtTripId;
		String evtId;
		int evtType;
		try {
			JSONArray jsarr = new JSONArray(retStr);
			JSONObject obj;
			JSONObject subObj;
			JSONArray subArr;
			boolean ret = false;
			for(i = 0; i < jsarr.length(); i++){
				obj = jsarr.getJSONObject(i);
				tripId = obj.getLong("trip_id");
				userId = obj.getString("user_id"); 
				startTmStr = obj.getString("m_trip_start_time");
				endTmStr = obj.getString("m_trip_start_time");
				startTm = Timestamp.valueOf(startTmStr);
				endTm = Timestamp.valueOf(endTmStr);
				sLat = Double.parseDouble(obj.getString("m_trip_start_lat"));
				sLon = Double.parseDouble(obj.getString("m_trip_start_lon"));
				eLat = Double.parseDouble(obj.getString("m_trip_end_lat"));
				eLon = Double.parseDouble(obj.getString("m_trip_end_lon"));	
				distance = Float.parseFloat(obj.getString("m_trip_distance"));
				averSpd = Float.parseFloat(obj.getString("m_trip_avg_velocity"));
				scoreOne = Integer.parseInt(obj.getString("m_safe_score_per_trip"));
				scoreAll = Integer.parseInt(obj.getString("m_safe_score_total"));
				trip = new StatisticsData(tripId, userId, startTm, endTm, sLat, sLon, eLat, eLon,
						distance, averSpd, scoreOne, scoreAll);
				traceList.add(trip);
				if(obj.has("m_analyzed_events")){
					ret = true;
					subObj = obj.getJSONObject("m_analyzed_events");
					Iterator<?> it = subObj.keys();
					String key;
					while(it.hasNext()){
						key = (String) it.next();
						subArr = (JSONArray)subObj.get(key);
						evtType = Integer.parseInt(key);
						switch(evtType){
						case 10:
							overSpdCnt = subArr.length();							
							break;
						case 7:	
							brakeCnt = subArr.length();
							break;
						case 3:
							turnCnt = subArr.length();
							break;
						case 6:
							gasCnt = subArr.length();
							break;
						}
						for(j = 0; j < subArr.length(); j++){								
							JSONObject subobj = subArr.getJSONObject(j);
							evtId = subobj.getString("event_id");
							evtStTm = Timestamp.valueOf(subobj.getString("m_start_time"));
							evtEnTm = Timestamp.valueOf(subobj.getString("m_end_time"));
							evtLat = Double.parseDouble(subobj.getString("m_loc_lat"));
							evtLon = Double.parseDouble(subobj.getString("m_loc_lon"));
							
							evt = new DrivingEvent(evtId, tripId, evtType, evtStTm, evtEnTm, evtLon, evtLat);
							eventList.add(evt);
						}
					}
				}
			}
			 if(downloadDelegate!=null)
                 downloadDelegate.downloadComplete(MSG_DOWNLOAD_COMPLETE);
         
//			if(ret)
//				handler.obtainMessage(1).sendToTarget();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    // calculate distance with Baidu SDK 
    // applied to trips with gps data with speed set to -1 (no obd)
    private void updateTripParamsWithBaiduLBS(List<StatisticsData> triplist, List<List<GPS>> gps){
    	if(triplist == null || gps == null){
    		return;
    	}
    	
    	for(int i = 0; i < triplist.size(); i++){
    		StatisticsData trip = triplist.get(i);
    		List<GPS> gpslist = gps.get(i);
    		double total_dist = 0.0;	// km
    		for(int j = 0; j < gpslist.size() - 1; j++){
    			long timeInterval = gpslist.get(j+1).getCreatetime().getTime() 
    									- gpslist.get(j).getCreatetime().getTime();	// ms

//    			if(gpslist.get(j).getSpeed() == -1)	{// obd not connected
//        			LatLng p1 = new LatLng(gpslist.get(j).getLat(), gpslist.get(j).getLon());
//        			LatLng p2 = new LatLng(gpslist.get(j).getLat(), gpslist.get(j+1).getLon());
//    				total_dist += DistanceUtil.getDistance(p1, p2) / 1000;	// m -> km
//    			}
//    			else {	// obd connected
//    				total_dist += gpslist.get(j).getSpeed() * timeInterval / (1000 * 60 * 60);
//    			}
    		}
    		trip.setDistance((int)total_dist);
    		
    		long tripDuration = trip.getTripDuration();
    		trip.setAverageSpeed((float)(total_dist / (tripDuration / (1000 * 60 * 60))));
    		
    	}
    	
    	
    }

    private void unzipFile(){
        try{
            File[] files =new File(context.getFilesDir().getPath()).listFiles();
            File destDir = new File(context.getFilesDir()+"/Trace");
            if (!destDir.exists()) {
                destDir.mkdirs();
            }

//            copyFile(context.getFilesDir().getPath()+"/"+mFileName, Environment.getExternalStorageDirectory().getPath());
            ZipUtil.unzip(context.getFilesDir().getPath() + "/" + mFileName, context.getFilesDir() + "/Trace");

        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }

    public void copyFile(String oldPath, String newPath) {
        try {
            int bytesum = 0;
            int byteread = 0;
            File oldfile = new File(oldPath);
            if (oldfile.exists()) {
                InputStream inStream = new FileInputStream(oldPath);
                FileOutputStream fs = new FileOutputStream(newPath);
                byte[] buffer = new byte[1444];
                int length;
                while ( (byteread = inStream.read(buffer)) != -1) {
                    bytesum += byteread;
                    System.out.println(bytesum);
                    fs.write(buffer, 0, byteread);
                }
                inStream.close();
            }
        }
        catch (Exception e) {
            System.out.println("copy error");
            e.printStackTrace();

        }
    }

}