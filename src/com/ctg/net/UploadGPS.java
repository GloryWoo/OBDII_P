package com.ctg.net;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.Request.Method;
import com.android.volley.toolbox.StringRequest;
import com.ctg.crash.LogRecord;
import com.ctg.ui.Base;
import com.ctg.ui.OBDApplication;
import com.ctg.util.Preference;

public class UploadGPS {
	private static boolean D = false;
	private static final String TAG = "UploadGPS";
	private static final int TIME_OUT = 10 * 1000; // Upload timeout
	private static final String CHARSET = "utf-8"; // set encode format
	private static String localeDirectory = "/OBDII/gps/";
	private static String ServerURL = Base.HTTP_ROOT_PATH;
	private static String ServerDirectory = "/services/gps";
	private static String testurl = ServerURL + ServerDirectory;
	public static final String TEST_HTTP_ROOT_PATH = "http://192.168.1.157:8080/obd";

	private static String testurltemp = TEST_HTTP_ROOT_PATH + ServerDirectory;

	private Context context;
	// private String url;
	// private int result;
	private String sessionid;

	public UploadGPS(Context context) {
		this.context = context;
		// this.url = url;
		// this.param = param;
		// result = 0;
	}

	public String uploadFile(File file, String RequestURL) {
		String result = null;
		String BOUNDARY = UUID.randomUUID().toString();
		String PREFIX = "--", LINE_END = "\r\n";
		String CONTENT_TYPE = "multipart/form-data";

		String version = Base.OBDApp.getVersion();
		String sessionid = Preference.getInstance(
				context.getApplicationContext()).getSessionId();

		try {
			URL url = new URL(RequestURL);
			// URL url = new URL(testurltemp);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setReadTimeout(TIME_OUT);
			conn.setConnectTimeout(TIME_OUT);
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.setUseCaches(false);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Charset", CHARSET); // set encode format
			conn.setRequestProperty("connection", "keep-alive");
			conn.setRequestProperty("Content-Type", CONTENT_TYPE + ";boundary="
					+ BOUNDARY);
			conn.addRequestProperty("X-token", sessionid);
			conn.addRequestProperty("X-API-version", version);
			conn.addRequestProperty("Content-Size",
					Integer.toString((int) file.length()));
			if (file != null) {
				/**
				 * If file is null, then transfer file and prepare to upload it;
				 */
				DataOutputStream dos = new DataOutputStream(
						conn.getOutputStream());
				StringBuffer sb = new StringBuffer();
				sb.append(PREFIX);
				sb.append(BOUNDARY);
				sb.append(LINE_END);

				sb.append("Content-Disposition: form-data; name=\"gps_log\"; filename=\""
						+ file.getName() + "\"" + LINE_END);
				sb.append("Content-Type: application/octet-stream; charset="
						+ CHARSET + LINE_END);
				sb.append(LINE_END);

				dos.write(sb.toString().getBytes());
				InputStream is = new FileInputStream(file);
				byte[] bytes = new byte[1024];
				int len = 0;
				while ((len = is.read(bytes)) != -1) {
					dos.write(bytes, 0, len);
				}
				is.close();
				dos.write(LINE_END.getBytes());
				byte[] end_data = (PREFIX + BOUNDARY + PREFIX + LINE_END)
						.getBytes();
				dos.write(end_data);
				dos.flush();
				dos.close();
				int res = conn.getResponseCode();
				Log.e(TAG, "response code:" + res);
				LogRecord.SaveLogInfo2File(Base.WeathInfo, "response code:"
						+ res);
				if (res == 200) {
					Log.e(TAG, "request success");
					result = "OK";
					file.delete();
				} else {
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

	public void uploadJsonFile(final File file, String RequestURL) {
		Response.Listener<String> okHandler = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //Logger.info(LOGLEVEL, TAG, "response:"+prefix+url+"\n"+response);
            	Log.v("updata", "success");
            	file.delete();
            }
        };
		
		Response.ErrorListener errorHandler = new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				String message = VolleyErrorHelper.getMessage(error);
				Toast.makeText(context, message, 1).show();
				// Logger.info(LOGLEVEL, TAG,message);
				// if (callback!=null) {
				//
				// callback.handle(FAIL,errDefault);
				// }
			}
		};

		if (file != null) {
			/**
			 * If file is null, then transfer file and prepare to upload it;
			 */
			String res = null;
			FileInputStream fin;
			try {
				fin = new FileInputStream(file);
				BufferedReader br = new BufferedReader(new InputStreamReader(
						fin));
				while ((res = br.readLine()) != null) {
//					res = br.readLine();
					
					if (res.length() > 0) {
						Log.v("uploadFile", res);
						JSONObject jsonObj = new JSONObject(res);

						final Map<String, String> postData = new HashMap<String, String>();

						try {
							Iterator<?> keys = jsonObj.keys();
							while(keys.hasNext()){
								String key = (String)keys.next();
								postData.put(key, jsonObj.getString(key));
							}
//							if(jsonObj.getBoolean("hasOBD") == true){
//								
//							}
//							else {
//								postData.put("time", jsonObj.getString("time"));
//								postData.put("latitude", jsonObj.getString("latitude"));
//								postData.put("longitude",
//										jsonObj.getString("longitude"));
//								postData.put("speed", jsonObj.getString("speed"));
//								postData.put("traceId", jsonObj.getString("traceId"));
//							}

						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						MyStringRequest jsonObjReq = new MyStringRequest(Method.POST,
								RequestURL, okHandler, errorHandler) {

							@Override
							protected Map<String, String> getParams()
									throws AuthFailureError {
								// TODO Auto-generated method stub
								return postData;
							}

						};
						jsonObjReq.setHeader(((OBDApplication) context.getApplicationContext()).getVersion(), Preference.getInstance(
								context.getApplicationContext()).getSessionId());
						OBDApplication.getHttpClient().add(jsonObjReq);
					}
				}
				
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

		}

	}

	// Get external SD card directory:
	public String getSDPath() {
		File sdDir = null;
		boolean sdCardExist = Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED); // test if sd exist;
		if (!sdCardExist) {
			return "";
		} else {
			sdDir = Environment.getExternalStorageDirectory(); // get root
																// directory
			String SDcardpath = sdDir.toString();
			Log.d(TAG, "SD card path is :" + SDcardpath);
			return SDcardpath;
		}
	}

	public void UploadFiles(String url) {
		File[] fm = null;
		boolean ret;
		String filepath = getSDPath() + localeDirectory;
		File dir = new File(filepath);
		fm = dir.listFiles();
		if (fm == null)
			return;
		for (File file : fm) {
			ret = file.exists();
			Log.d("----file name is ----", file.getName());
			if (ret) {
				// lzy :
				uploadJsonFile(file, url);
			}
		}

	}

	private Runnable mUpGPSRunnable = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			// UploadFiles();
		}

	};

	public void uploadGPSThreadStart() {
		new Thread(mUpGPSRunnable).start();
	}
}
