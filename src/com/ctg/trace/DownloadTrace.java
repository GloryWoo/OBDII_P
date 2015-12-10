package com.ctg.trace;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
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

    public List<StatisticsData> traceList;
    public List<List<GPS>> pointList;
    public Map<Long, HashMap<Integer, ArrayList<DrivingEvent>>> eventList;

    public DownloadDelegate downloadDelegate=null;
    
    private boolean isOver = false;

    public void Download(Context ctx, Date startTime, Date endTime, boolean trackable){
        context = ctx;
        isOver = trackable;

        long timeLong = endTime.getTime() - startTime.getTime();
        if (timeLong<60*1000) {//less 60 second
            if(downloadDelegate!=null)
                downloadDelegate.downloadComplete(MSG_DOWNLOAD_NO_MORE);
            return;
        }

        mStartTime = startTime;
        mEndTime = endTime;

        Thread thread = new Thread(runnable);
        thread.start();
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            getDataFromServer();
        }
    };

    private void getDataFromServer_testGZip(){
        String version = ((com.ctg.ui.OBDApplication)context.getApplicationContext()).getVersion();
        String sessionidExist = Preference.getInstance(context.getApplicationContext()).getSessionId();

        HttpClient httpClient = new DefaultHttpClient();
        String url= "http://192.168.1.51:8080/obd/services/getDriverBehavior";
 
        HttpPost httpPost = new HttpPost(url);

        httpPost.setHeader("X-API-version", String.format("%s", version));
        httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
        httpPost.setHeader("accept", "*/*");
        httpPost.setHeader("connection", "Keep-Alive");
        httpPost.setHeader("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
        httpPost.setHeader("X-token", "e0745e3d-9ec7-4151-aab5-857fc8e9dce1");

        HttpResponse response=null;
        HttpEntity entity=null;
        InputStream input=null;
        List<NameValuePair> params=new ArrayList<NameValuePair>();
        try {
            params.add(new BasicNameValuePair("startTime", "2015-03-20 13:43:59.324"));
            params.add(new BasicNameValuePair("endTime", "2015-07-20 13:54:05.888"));
            httpPost.setEntity(new UrlEncodedFormEntity(params));
            response = httpClient.execute(httpPost);

            if(response.getStatusLine().getStatusCode()==200){
                entity=response.getEntity();
                input=entity.getContent();

                try {
                	GZIPInputStream gzipIn = new GZIPInputStream(input);
                    String line = "";
                    byte[] b = new byte[1024];
                    int i;
                    while ((i= gzipIn.read(b)) != -1) {
                        line+=new String(b);  
                    }        
                    //updateTripParamsWithBaiduLBS(traceList, pointList);
                	gzipIn.close();
                }catch (OptionalDataException e){
                    e.printStackTrace();
                }catch (IOException e){
                    e.printStackTrace();
                }

                //update data complete
                if(downloadDelegate!=null)
                    downloadDelegate.downloadComplete(MSG_DOWNLOAD_COMPLETE);
            }
            else if(response.getStatusLine().getStatusCode()==500){
                System.out.println("500");
                if(downloadDelegate!=null)
                    downloadDelegate.downloadComplete(MSG_DOWNLOAD_NO_MORE);
            }
            else if(response.getStatusLine().getStatusCode()==404){
                System.out.println("404");
                if(downloadDelegate!=null)
                    downloadDelegate.downloadComplete(MSG_DOWNLOAD_FAIL);
            }
            else {
                if(downloadDelegate!=null)
                    downloadDelegate.downloadComplete(MSG_DOWNLOAD_FAIL);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @SuppressWarnings("unchecked")
	private void getDataFromServer(){
        String version = ((com.ctg.ui.OBDApplication)context.getApplicationContext()).getVersion();
        String sessionidExist = Preference.getInstance(context.getApplicationContext()).getSessionId();

        HttpClient httpClient = new DefaultHttpClient();
        String url= Base.NEW_HTTP_ROOT_PATH + "/services/download/track";
 
        HttpPost httpPost = new HttpPost(url);

        httpPost.setHeader("X-API-version", String.format("%s", version));
        httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
        httpPost.setHeader("accept", "*/*");
        httpPost.setHeader("connection", "Keep-Alive");
        httpPost.setHeader("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
        httpPost.setHeader("X-token", sessionidExist);

        HttpResponse response=null;
        HttpEntity entity=null;
        InputStream input=null;
        List<NameValuePair> params=new ArrayList<NameValuePair>();
        try {
            params.add(new BasicNameValuePair("startTime", DateUtil.DateToDateStr(mStartTime)));
            params.add(new BasicNameValuePair("endTime", DateUtil.DateToDateStr(mEndTime)));
            params.add(new BasicNameValuePair("GPSSwtState", Boolean.toString(isOver)));
            httpPost.setEntity(new UrlEncodedFormEntity(params));
            response = httpClient.execute(httpPost);

            if(response.getStatusLine().getStatusCode()==200){
                entity=response.getEntity();
                input=entity.getContent();

                try {
                    ObjectInputStream in = new ObjectInputStream(input);
                    traceList = (List<StatisticsData>) in.readObject();
                    pointList = (List<List<GPS>>)in.readObject();
                    eventList = (Map<Long, HashMap<Integer, ArrayList<DrivingEvent>>>)in.readObject();                    
                    //updateTripParamsWithBaiduLBS(traceList, pointList);
                    in.close();
                }catch (OptionalDataException e){
                    e.printStackTrace();
                }catch (ClassNotFoundException e){
                    e.printStackTrace();
                }catch (IOException e){
                    e.printStackTrace();
                }

                //update data complete
                if(downloadDelegate!=null)
                    downloadDelegate.downloadComplete(MSG_DOWNLOAD_COMPLETE);
            }
            else if(response.getStatusLine().getStatusCode()==500){
                System.out.println("500");
                if(downloadDelegate!=null)
                    downloadDelegate.downloadComplete(MSG_DOWNLOAD_NO_MORE);
            }
            else if(response.getStatusLine().getStatusCode()==404){
                System.out.println("404");
                if(downloadDelegate!=null)
                    downloadDelegate.downloadComplete(MSG_DOWNLOAD_FAIL);
            }
            else {
                if(downloadDelegate!=null)
                    downloadDelegate.downloadComplete(MSG_DOWNLOAD_FAIL);
            }
        } catch (IOException e) {
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

    			if(gpslist.get(j).getSpeed() == -1)	{// obd not connected
        			LatLng p1 = new LatLng(gpslist.get(j).getLat(), gpslist.get(j).getLon());
        			LatLng p2 = new LatLng(gpslist.get(j).getLat(), gpslist.get(j+1).getLon());
    				total_dist += DistanceUtil.getDistance(p1, p2) / 1000;	// m -> km
    			}
    			else {	// obd connected
    				total_dist += gpslist.get(j).getSpeed() * timeInterval / (1000 * 60 * 60);
    			}
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