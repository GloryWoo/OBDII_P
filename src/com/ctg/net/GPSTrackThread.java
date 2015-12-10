package com.ctg.net;

import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.ctg.crash.LogRecord;
import com.ctg.service.CarDataService;
import com.ctg.ui.Base;
import com.ctg.util.Preference;
import com.ctg.weather.WeatherInfo;

public class GPSTrackThread extends Thread {
    private static final String TAG = "GPSTrackThread";
    private static final int TIME_OUT = 10*1000;   //Upload timeout
    private static final String CHARSET = "utf-8"; //set encode format
	public static final String KEY_TRANSFER_STATUS = "httpstatus";
	public static final String KEY_RET_CODE = "ret_code";
	public static final String VERSION = "version";

	
	public static final int CONNECT_FAILED = 0;

	public static final int CONNECT_SUCCEED = 1;
	
	final public static int UP_GPS_STATUES = 0x106;
	

	private Handler handler;

	private Context context;

	private String mUrl;

	private int result;

	private byte[] param;

	private int retCode;

	private volatile Thread runner;

	//public static int uploadTime = 0;
	
	public GPSTrackThread(Context context, Handler handler, String url, byte[] param) {
		this.context = context;
		this.handler = handler;
		this.mUrl = url;
		this.param = new byte[param.length];
		System.arraycopy(param, 0, this.param, 0, param.length);
		result = 0;

	}

	public GPSTrackThread(Context context, Handler handler) {
		this.context = context;
		this.handler = handler;
	}

	public synchronized void startHttp() {
		if (runner == null) {
			runner = new Thread(this);
			runner.start();
		}
	}

	public synchronized void stopThread() {
		if (runner != null) {
			Thread moribund = runner;
			runner = null;
			moribund.interrupt();
		}
	}
	
	private synchronized void sendMsg(int what, int val) {
		if (Thread.currentThread() != runner) {
			return;
		}
		
		if (handler != null) {
			Message msg = handler.obtainMessage();
			msg.what = what;
			Bundle bb = new Bundle();
			bb.putInt(KEY_TRANSFER_STATUS, val);				
			bb.putInt(KEY_RET_CODE, retCode);	
			msg.setData(bb);
			handler.sendMessage(msg);
		}
	}

	public void run() {			
		
        String BOUNDARY =  UUID.randomUUID().toString();  
        String PREFIX = "--" , LINE_END = "\r\n"; 
		String CONTENT_TYPE = "multipart/form-data"; 
		result = 0;	
		String version = Base.OBDApp.getVersion();
		String sessionid = Preference.getInstance(context.getApplicationContext()).getSessionId();

        try {
            URL url = new URL(mUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(TIME_OUT);
            conn.setConnectTimeout(TIME_OUT);
            conn.setDoInput(true);  
            conn.setDoOutput(true); 
            conn.setUseCaches(false);  
            conn.setRequestMethod("POST");  
            conn.setRequestProperty("Charset", CHARSET);  //set encode format
            conn.setRequestProperty("connection", "keep-alive");   
            conn.setRequestProperty("Content-Type", CONTENT_TYPE + ";boundary=" + BOUNDARY); 
            conn.addRequestProperty("X-token", sessionid);
            conn.addRequestProperty("X-API-version", version);
            conn.addRequestProperty("Content-Size", Integer.toString(param.length));
            
            if(param!=null && param.length != 0)
            {
                /**
                 * If file is null, then transfer file and prepare to upload it;
                 */
                DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
                StringBuffer sb = new StringBuffer();
                sb.append(PREFIX);
                sb.append(BOUNDARY);
                sb.append(LINE_END);

                sb.append("Content-Disposition: form-data; name=\"gps_log\"; filename=\"gps_temp_file_name\""+LINE_END);                 
                sb.append("Content-Type: application/octet-stream; charset="+CHARSET+LINE_END);
                sb.append(LINE_END);
                
                dos.write(sb.toString().getBytes());
                InputStream is = new ByteArrayInputStream(param);
                byte[] bytes = new byte[1024];
                int len = 0;
                while((len=is.read(bytes))!=-1)
                {
                    dos.write(bytes, 0, len);
                }
                is.close();
                dos.write(LINE_END.getBytes());
                byte[] end_data = (PREFIX+BOUNDARY+PREFIX+LINE_END).getBytes();
                dos.write(end_data);
                dos.flush();
                dos.close();
                int res = conn.getResponseCode();  
                Log.e(TAG, "response code:"+res);
                LogRecord.SaveLogInfo2File(Base.WeathInfo,
                		TAG+" response code:" + res);
                if(res==200)
                {
                    Log.e(TAG, "request success");
                    result = 1;
                }
                else{
                    Log.e(TAG, "request error");
                }
            }
        }catch(MalformedURLException e){
            e.printStackTrace();
        }catch(IOException e){
            e.printStackTrace();
        }finally{ 		
        	if(result == 0){
        		CarDataService.sync_write(param);
        	}
        }
		if(Thread.currentThread() == runner)
			sendMsg(UP_GPS_STATUES, result);						
		
	}
	
}
