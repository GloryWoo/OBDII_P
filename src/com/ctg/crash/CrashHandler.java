package com.ctg.crash;

import java.io.File;  
import java.io.FileOutputStream;  
import java.io.PrintWriter;  
import java.io.StringWriter;  
import java.io.Writer;  
import java.lang.Thread.UncaughtExceptionHandler;  
import java.lang.reflect.Field;  
import java.text.DateFormat;  
import java.text.SimpleDateFormat;  
import java.util.Date;  
import java.util.HashMap;  
import java.util.Map;  

import com.ctg.ui.R;
import com.ctg.util.Preference;
 
import android.content.Context;  
import android.content.pm.PackageInfo;  
import android.content.pm.PackageManager;  
import android.content.pm.PackageManager.NameNotFoundException;  
import android.os.Build;  
import android.os.Environment;  
import android.os.Looper;  
import android.util.Log;  
import android.widget.Toast; 


public class CrashHandler implements UncaughtExceptionHandler{
	
	public UpLoadLog crashloadlog;
	private static boolean D = false;
	public static final String TAG = "CrashLog";
	//Use the default uncaught exception handler;
	private Thread.UncaughtExceptionHandler mDefaultHandler;
	//Crashhandler instance;
	private static CrashHandler INSTANCE = new CrashHandler();
	
	private Context mContext;
	public static String crashDirectory = "/OBDII/crash/";
	
	private Map<String, String> infos = new HashMap<String, String>();
	  DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
	
	//Keep only one CrashHandler instance existed;
	private CrashHandler(){
		
	}
	
	public static CrashHandler getInstance(){
		return INSTANCE;	
	}
	
	public void init(Context context,UpLoadLog crashload){
		mContext = context;
		mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
		//Set CrashHandler as the default deputy for crash log;
		Thread.setDefaultUncaughtExceptionHandler(this);		
//		Thread.setDefaultUncaughtExceptionHandler(mDefaultHandler);
		crashloadlog = crashload;
	}
	
	
	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		// TODO Auto-generated method stub
		if(!handlerException(ex) && mDefaultHandler != null){
			mDefaultHandler.uncaughtException(thread, ex);		
		}
		else{
			try{
				Thread.sleep(3000);
			}
			catch(InterruptedException e){
				Log.e(TAG,"Error:",e);
			}
			android.os.Process.killProcess(android.os.Process.myPid());
			System.exit(1);
		}
			
	}
	
	
	private boolean handlerException(Throwable ex){
		if(ex == null){
			return false;
		}
		new Thread(){
			public void run(){
				Looper.prepare();
				Toast.makeText(mContext, R.string.exit_notice, Toast.LENGTH_LONG).show();
				Looper.loop();
			}
		}.start();
		//collect device info;
		collectDeviceInfo(mContext);
		//save log;
		saveCrashInfo2File(ex);
		//upload log;
		crashloadlog.UploadFiles(crashDirectory);
		return true;
	}

	
	public void collectDeviceInfo(Context ctx){
		try{
			PackageManager pm = ctx.getPackageManager();
			PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(), PackageManager.GET_ACTIVITIES);
			if(pi != null){
				String versionName = pi.versionName == null ? "null" :pi.versionName;
				String versionCode = pi.versionCode + "";
				infos.put("versionName", versionName);
				infos.put("versionCode", versionCode);	
			}
		}catch(NameNotFoundException e){
			Log.e(TAG,"an error occur when collect package info", e);			
		}
		Field[] fields = Build.class.getDeclaredFields();
		for(Field field : fields){
			try{
				field.setAccessible(true);
				infos.put(field.getName(), field.get(null).toString());
				Log.d(TAG,field.getName() + ":" + field.get(null));
			}catch(Exception e){
				Log.e(TAG,"an error occur when collect package info");
			}
		}		
	}
	
	private String saveCrashInfo2File(Throwable ex){
		boolean ret;
		StringBuffer sb = new StringBuffer();
		for(Map.Entry<String, String> entry : infos.entrySet()){
			String key = entry.getKey();
			String value = entry.getValue();
			sb.append(key + "=" + value +"\r\n");		
		}
		
		Writer writer = new StringWriter();
		PrintWriter printWriter = new PrintWriter(writer);
		ex.printStackTrace(printWriter);
		Throwable cause = ex.getCause();
		while(cause != null){
			cause.printStackTrace(printWriter);
			cause = cause.getCause();
		}
		printWriter.close();
		String result = writer.toString();
		sb.append(result);
		String UserAccount = Preference.getInstance(mContext.getApplicationContext()).getUser();
		try{
			long timestamp = System.currentTimeMillis();
			String time = formatter.format(new Date());
			String filename = UserAccount + "crash-" + time + /*timestamp*/  "-log" + ".txt";
			String path = getSDPath() + crashDirectory;
			File dir = new File(path);
			if(!dir.exists()){
				ret = dir.mkdirs();
			}
			FileOutputStream fos = new FileOutputStream(path + filename);
			fos.write(sb.toString().getBytes());
			fos.close();
			return filename;
        } catch (Exception e) {  
        	Log.e(TAG, "an error occured while writing file...", e);  
        }  
        return null;			
	}
	
	//Get external SD card directory:
	public String getSDPath(){
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
	         Log.d(TAG, "SD card path is :" + SDcardpath );
	 	     return SDcardpath; 
	    } 
	}	
	
}
















