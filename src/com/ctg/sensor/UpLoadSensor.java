


package com.ctg.sensor;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.UUID;
import java.util.zip.GZIPInputStream;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import com.ctg.ui.Base;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;


public class UpLoadSensor {
	private static boolean D = false;
    private static final String TAG = "UploadLog";
    private static final int TIME_OUT = 10*1000;   //Upload timeout
    private static final String CHARSET = "utf-8"; //set encode format
    private static String ServerDirectory = "/services/sensorLog";

    
	private Context context;
	private String url;
	private int result;
	private String sessionid;
	private JSONObject param;

    
	public UpLoadSensor(Context context, String session) {
		this.context = context;
		this.sessionid = session;
	}
	public UpLoadSensor(Context context) {
		this.context = context;
	}
		
    public String uploadFile(File file, String RequestURL)
    {
    	return uploadFile(file, sessionid, RequestURL);
    }
    
    public static byte[] sendPost(String url, String param, String token) {
		PrintWriter out = null;
		GZIPInputStream in = null;
		String result = "";
		try {
			URL realUrl = new URL(url);
			// �򿪺�URL֮�������
			URLConnection conn = realUrl.openConnection();
			// ����ͨ�õ���������
			conn.setRequestProperty("accept", "*/*");
			conn.setRequestProperty("connection", "Keep-Alive");
			conn.setRequestProperty("user-agent",
					"Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
			conn.setRequestProperty("X-token", token);
			conn.setRequestProperty("X-appKey", "6830b011-3007-4798-ad27-a7b79af40b20");
			conn.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
			// ����POST�������������������
			conn.setDoOutput(true);
			conn.setDoInput(true);
			// ��ȡURLConnection�����Ӧ�������
			out = new PrintWriter(conn.getOutputStream());
			if (!param.equals("")) {
				// �����������
				out.print(param);
			}
			// flush������Ļ���
			out.flush();
			int len = Integer.parseInt(conn.getHeaderField("x-size"));
			int count = 0;
			// ����BufferedReader����������ȡURL����Ӧ
			in = new GZIPInputStream(conn.getInputStream());
			ByteArrayOutputStream bos = new ByteArrayOutputStream(len);
			byte[] tmp = new byte[len];
			while ((count = in.read(tmp)) != -1) {
				bos.write(tmp, 0, count);
			}
			bos.close();
			return bos.toByteArray();
		} catch (Exception e) {
			System.out.println("���� POST ��������쳣��" + e);
			e.printStackTrace();
		}
		// ʹ��finally�����ر��������������
		finally {
			try {
				if (out != null) {
					out.close();
				}
				if (in != null) {
					in.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		return null;
	}
    
    
    public static String uploadFile(File file, String token, String RequestURL)
    {
        String result = null;
        String BOUNDARY =  UUID.randomUUID().toString();  
        String PREFIX = "--" , LINE_END = "\r\n"; 
        String CONTENT_TYPE = "multipart/form-data";  

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
            conn.addRequestProperty("X-token", token); 
            conn.addRequestProperty("X-appKey", "6830b011-3007-4798-ad27-a7b79af40b20");
            if(file!=null)
            {
                DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
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
                    // added by WR
                    file.delete();
                    // Log.e(TAG, "result : "+ result);
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
    

	private Runnable mUpGPSRunnable = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			UploadFiles("/OBDII/sensor");
		}

	};

	public void uploadGPSThreadStart() {
		new Thread(mUpGPSRunnable).start();
	}
	
	
	public void UploadFiles(String subDirectory){
		File[] fm = null;
		boolean ret;
		String filepath = Base.getSDPath() + subDirectory;
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
//				if( uploadFile(file,testurl) != null ){
//					file.delete();
//				}
				uploadFile(file,Base.DB_BEHAVIOR_SERVER);
			}
		}
		
	}
    
}

