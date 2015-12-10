package com.ctg.crash;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.ctg.ui.OBDApplication;
import com.ctg.ui.Base;
import com.ctg.util.Preference;

public class LogRecord {

	private static boolean D = false;
	public static final String TAG = "LogRecord";
	
	private static Context mContext = null;
	
	
	private static String UserCount = "";
	private static String xtoken = "";
	
	private int result;
	private static String sessionid = "";
	
    private static final int TIME_OUT = 10*1000;   //Upload timeout
    private static final String CHARSET = "utf-8"; //set encode format
    private static String ServerURL = "http://116.236.202.130:8080/obd";
    private static String ServerDirectory = "/services/log";
    private static String  testurl =ServerURL + ServerDirectory;
	
	private static Map<String, String> infos;// = new HashMap<String, String>();
	static DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS");
	
	
	public LogRecord(Context context) {
		mContext = context;
		UserCount = Preference.getInstance(mContext.getApplicationContext()).getUser();
		xtoken = Preference.getInstance(mContext.getApplicationContext()).getSessionId();
	}	
	
	
	public static void SetSessionid(String sessionid){
		xtoken = sessionid;
	}

	public static boolean UploadLogFiles(String subDirectory,String requestURL){
		File[] fm = null;
		boolean ret;
		String filepath = getSDPath() + subDirectory;
		File dir = new File(filepath);
		fm = dir.listFiles();
		if(fm == null)
			return false;
		for(File file:fm){
			ret = file.exists();
			Log.d("----file name is ----",file.getName());
			if(ret){
				//HttpFiles(file.getName(),filepath,file.length());				
				ret = uploadFile(file,requestURL);
				return ret;
//				if(ret)
//					file.delete();
			}
		}
		return false;
	}
	
	
    public static boolean uploadFile(File file, String RequestURL)
    {
        String result = null;
        boolean ret = false;
        String BOUNDARY =  UUID.randomUUID().toString();  
        String PREFIX = "--" , LINE_END = "\r\n"; 
        String CONTENT_TYPE = "multipart/form-data";  

		String version = ((OBDApplication)mContext).getVersion();
		//String xtoken = Preference.getInstance(mContext.getApplicationContext()).getSessionId();
		if(xtoken != null)
			sessionid = xtoken;
		//HttpPost mPost =new HttpPost(testurl); //new HttpPost(url);
		//mPost.setHeader("X-API-version", String.format("%s", version));
		//mPost.setHeader("X-token",sessionid);
        
        
        try {
            URL url = new URL(RequestURL);
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
            conn.addRequestProperty("X-API-version", String.format("%s", version));
            conn.setRequestProperty("X-app_key", "6830b011-3007-4798-ad27-a7b79af40b20");
            
            if(file!=null)
            {
                /**
                 * If file is null, then transfer file and prepare to upload it;
                 */
                DataOutputStream dos = new DataOutputStream( conn.getOutputStream());
                StringBuffer sb = new StringBuffer();
                sb.append(PREFIX);
                sb.append(BOUNDARY);
                sb.append(LINE_END);

                sb.append("Content-Disposition: form-data; name=\"file\"; filename=\""+file.getName()+"\""+LINE_END); 
                sb.append("Content-Type: application/octet-stream; charset="+CHARSET+LINE_END);
                sb.append(LINE_END);
                
                dos.write(sb.toString().getBytes());
                InputStream is = new FileInputStream(file);
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

                int res = conn.getResponseCode();  
                Log.e(TAG, "response code:"+res);
                if(res==200)
                {
                    //Log.e(TAG, "request success");
                    InputStream input =  conn.getInputStream();
                    StringBuffer sb1= new StringBuffer();
                    int ss ;
                    while((ss=input.read())!=-1)
                    {
                        sb1.append((char)ss);
                    }
                    result = sb1.toString();
                    Log.e(TAG, "result : "+ result);
                    ret = true;
                }
                else{
                    Log.e(TAG, "request error");
                    //Toast.makeText(mContext, "Network access fail", Toast.LENGTH_LONG).show();
                    ret = false;
                }
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
            ret = false;
        } catch (IOException e) {
            e.printStackTrace();
            ret = false;
        }
        return ret;
    }
	
    public static boolean uploadFile(File file, String token, String RequestURL)
    {
    	xtoken = token;
    	return uploadFile(file, RequestURL);
       
    }
    
	public static Map<String, String> CollectDeviceInfo(Context ctx){
		Map<String, String> temp = new HashMap<String, String>();
		try{
			PackageManager pm = ctx.getPackageManager();
			PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(), PackageManager.GET_ACTIVITIES);
			if(pi != null){
				String versionName = pi.versionName == null ? "null" :pi.versionName;
				String versionCode = pi.versionCode + "";
				temp.put("versionName", versionName);
				temp.put("versionCode", versionCode);	
			}
		}catch(NameNotFoundException e){
			Log.e(TAG,"an error occur when collect package info", e);
			return null;
		}
		Field[] fields = Build.class.getDeclaredFields();
		for(Field field : fields){
			try{
				field.setAccessible(true);
				temp.put(field.getName(), field.get(null).toString());
				Log.d(TAG,field.getName() + ":" + field.get(null));
			}catch(Exception e){
				Log.e(TAG,"an error occur when collect package info");
				return null;
			}
		}
		return temp;
	}
	
	public static String SaveSysInfo2File(String modularlog, boolean op){
		boolean ret;
		
		if( mContext == null ){
			return null;
		}		
		//String UserAccount = Preference.getInstance(mContext.getApplicationContext()).getUser();
		try{
//			long timestamp = System.currentTimeMillis();
			String time = formatter.format(new Date());
			String filename = UserCount + "-" + modularlog  +  "-log" + ".txt";
			String path = getSDPath() + Base.ycblog;
			
			time = "\n" + time + "\n";
			File dir = new File(path);
			if(!dir.exists()){
				ret = dir.mkdirs();
			}
			FileOutputStream fos = new FileOutputStream((path + filename),op);
			if(!op)// first create input info set.
			{
				Map<String, String> info = new HashMap<String, String>();
				info = CollectDeviceInfo(mContext);
				StringBuffer sb = null;
				if(info != null)
				{
					sb = new StringBuffer();
					for(Map.Entry<String, String> entry : info.entrySet()){
						String key = entry.getKey();
						String value = entry.getValue();
						sb.append(key + "=" + value +"\r\n");		
					}
					fos.write(sb.toString().getBytes());
				}
				
			}		
			fos.write(time.getBytes());
			fos.close();
			return filename;
        } catch (Exception e) {  
        	Log.e(TAG, "an error occured while writing file...", e);  
        }  
        return null;			
	}
	
	public static String SaveLogInfo2File(String modularlog, String content){
		boolean ret;
		//String UserAccount = Preference.getInstance(mContext.getApplicationContext()).getUser();
		try{
			long timestamp = System.currentTimeMillis();
			String time = formatter.format(new Date());
			String filename = UserCount + "-" + modularlog  + "-log" + ".txt";
			String path = getSDPath() + Base.ycblog;
			File dir = new File(path);
			String temp = time + "--" + content + "\r\n";
			if(!dir.exists()){
				ret = dir.mkdirs();
			}
			FileOutputStream fos = new FileOutputStream((path + filename),true);
			fos.write(temp.getBytes());
			fos.close();
			return filename;
        } catch (Exception e) {  
        	Log.e(TAG, "an error occured while writing file...", e);  
        }  
        return null;			
	}			
	
	//Get external SD card directory:
	public static String getSDPath(){
		File sdDir = null;
		boolean sdCardExist = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);   //test if sd exist;
	    if(!sdCardExist)   
	    {  
	    	 return ""; 		
	    } 
	    else
	    {
	         sdDir = Environment.getExternalStorageDirectory();   //get root directory
	         String SDcardpath = sdDir.toString();
//	         Log.d(TAG, "SD card path is :" + SDcardpath );
	 	     return SDcardpath; 
	    } 
	}	

}
