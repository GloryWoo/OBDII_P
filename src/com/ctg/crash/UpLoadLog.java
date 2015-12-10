


package com.ctg.crash;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import com.ctg.ui.Base;
import com.ctg.util.Preference;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;


public class UpLoadLog {
	private static boolean D = false;
    private static final String TAG = "UploadLog";
    private static final int TIME_OUT = 10*1000;   //Upload timeout
    private static final String CHARSET = "utf-8"; //set encode format
    private static String localeDirectory = "/crash/";
    private static String ServerURL = Base.HTTP_ROOT_PATH;// 116.236.202.130
    private static String ServerDirectory = "/services/log";
    public static String  testurl =ServerURL + ServerDirectory;
    
	private Context context;
	private String url;
	private int result;
	private String sessionid;
	private JSONObject param;

    
	public UpLoadLog(Context context, String url) {
		this.context = context;
		this.url = url;
		//this.param = param;
		//result = 0;
	}
	public UpLoadLog(Context context) {
		this.context = context;
		//this.url = url;
		//this.param = param;
		//result = 0;
	}

    public String uploadFile(File file, String RequestURL)
    {
        String result = null;
        String BOUNDARY =  UUID.randomUUID().toString();  
        String PREFIX = "--" , LINE_END = "\r\n"; 
        String CONTENT_TYPE = "multipart/form-data";  

		String version = Base.OBDApp.getVersion();
		String xtoken = Preference.getInstance(context.getApplicationContext()).getSessionId();
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
                dos.close();
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
                    //Log.e(TAG, "result : "+ result);
                }
                else{
                    Log.e(TAG, "request error");
                }
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
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
    
	
	
	
	public void HttpFiles(String logfile, String path, long size){
		DefaultHttpClient mHttpClient = new DefaultHttpClient();	
		
		HttpResponse response = null;
		int reslut = 0;
		//String version = Preference.getInstance(context.getApplicationContext()).getVersion();
		String version = Base.OBDApp.getVersion();
		String xtoken = Preference.getInstance(context.getApplicationContext()).getSessionId();
		if(xtoken != null)
			sessionid = xtoken;
		//HttpPost mPost =new HttpPost(testurl); //new HttpPost(url);
		HttpPost mPost =new HttpPost(url);
		mPost.setHeader("X-API-version", String.format("%s", version));
		mPost.setHeader("X-token",sessionid);
		//mPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
		//mPost.setHeader("Content-Disposition:form-data; name=logfile; filename="path";");
		mPost.setHeader("Content-Disposition", "form-data; name=\"file\"; filename=\""+path+logfile+"\"");
		mPost.setHeader("Content-Type", "multipart/form-data");
				
		File f=new File(path+logfile); 
		FileInputStream fi = null;
		try {
			fi = new FileInputStream(f);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}   
		InputStreamEntity fr=new InputStreamEntity(fi, size); 			
		//mPost.setRequestEntity((RequestEntity)fr); 
		mPost.setEntity(fr);				
		try {
			response = mHttpClient.execute(mPost);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(response != null){
			result = response.getStatusLine().getStatusCode();
			System.out.println("----Upload Result is----");
			System.out.println(result);
			if(reslut == 200 || result == 201 || result == 202 || result == 204)
				System.out.println("----Upload log OK----");
			
		}		
	}
	
	
	public void UploadFiles(String subDirectory){
		File[] fm = null;
		boolean ret;
		String filepath = getSDPath() + subDirectory;
		File dir = new File(filepath);
		fm = dir.listFiles();
		if(fm == null)
			return;
		for(File file:fm){
			ret = file.exists();
			Log.d("----file name is ----",file.getName());
			if(ret){
				//HttpFiles(file.getName(),filepath,file.length());
				//file.delete();
				if( uploadFile(file,testurl) != null ){
					//file.delete();
				}
				
			}
		}
		
	}
    
}

