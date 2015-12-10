package com.ctg.trafficViolation;


import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import javax.xml.parsers.ParserConfigurationException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.ctg.ui.Base;
import com.ctg.weather.WeatherUtil;


/**
 * title
 * 
 * 
 */
public class CheShouYe extends Thread{


	public static String URL ="http://www.cheshouye.com";
	public static final String JSON_FILE = com.ctg.ui.Base.getSDPath()+"/OBDII/plateCityId.json"; 
	public static final String appid = "158";
	public static final String appkey = "2b14fe1cf66493053746b9ac0b66daaf";
	String hphm;
	String classnum;
	String enginenum;
	public String cityname;
	String cartype;
	String registnum;
	String cityId;
	public String queryVal;
	Handler mHandler;
	/**  
	 * title:
	 * 
	 * @param args
	 * @throws IOException 
	 * @throws ParserConfigurationException 
	 * @throws JSONException 
	 */
//	public static void main(String[] args) {
//		// TODO Auto-generated method stub
//
//		try {
//			String carInfo = "{hphm=ÔÁB12345&classno=123456&engineno=1234&city_id=152&car_type=02}";
//			String appId="158";    //
//			String appKey="2b14fe1cf66493053746b9ac0b66daaf";//
//			
//			System.out.println("");
//			String sb = getWeizhangInfoPost(carInfo, appId, appKey);
//			System.out.println("" + sb);
//			
//			
//			//
//			System.out.println(""+getConfig());
//
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
	public CheShouYe(String plate, String classno, String engineno, String citynm, String cartyp, Handler handler){
		hphm = plate;
		classnum = classno;
		enginenum = engineno;
		cityname = citynm;
		cartype = cartyp;
		mHandler = handler;
	}
	    	
    public synchronized void run() {
    	cityId = "23";
    	registnum = "0";
    	try {
    		prepareCityID();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	try {
			JSONObject jsobj = getCityJsonByPlateNm(hphm.substring(0, 2));
			cityname = jsobj.getString("city_name");
			cityId = jsobj.getString("city_id");
    		registnum = jsobj.getString("registno");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	if(cityId == null)
    		return;
    	String carinfo = "{hphm="+hphm + "&classno="+classnum + "&engineno="+enginenum + "&registno="+registnum+"&city_id="+cityId + "&car_type="+cartype + "}";
    	queryVal = getWeizhangInfoPost(carinfo, appid, appkey);
    	Message msg = new Message();
    	msg.what = TrafficVioQueryDlg.QUERY_READY;    	
    	Bundle bundle = new Bundle();
    	msg.setData(bundle);
    	bundle.putString(TrafficVioQueryDlg.QUERY_RESULT, queryVal);    
    	mHandler.sendMessage(msg);
    }
    
	
	public static JSONObject getCityJsonByName(String cname) throws JSONException, IOException{

		byte[] buffer = new byte[100000];
		int readSize = 1000;
		int readLen = 0;
		int readPos = 0;
		FileInputStream in_s = new FileInputStream(JSON_FILE);
		
		while((readLen = in_s.read(buffer, readPos, readSize)) > 0){	
			readPos += readLen;
		}
		in_s.close();
		String param = new String(buffer, 0, readPos, "UTF-8");
		JSONObject jsObj = new JSONObject(param);				
		JSONArray jsArr = new JSONArray(jsObj.getString("configs"));
		
		int provIdx, cityIdx;
		for(provIdx = 0; provIdx < jsArr.length(); provIdx++){
			String jsArrStr =  jsArr.getString(provIdx);
			JSONObject jsObjCity = new JSONObject(jsArrStr);
			String jsCityStr = jsObjCity.getString("citys");
			JSONArray jsCityArr = new JSONArray(jsCityStr);
			for(cityIdx = 0; cityIdx < jsCityArr.length(); cityIdx++){
				JSONObject cityObj = jsCityArr.getJSONObject(cityIdx);
				if(cityObj.getString("city_name").equals(cname))
					return cityObj;
			}
		}
		return null;
	}
	
	public static JSONObject getCityJsonByPlateNm(String platehead) throws JSONException, IOException{
		StringBuffer strBuf = new StringBuffer(1000000);
		char[] buffer = new char[1024];
		int buffSize = 1024;
		int readLen = 0;
		FileReader in_s = new FileReader(JSON_FILE);
		
		while((readLen = in_s.read(buffer, 0, buffSize)) > 0){
			strBuf.append(buffer, 0, readLen);
		}
		in_s.close();
		String param = new String(strBuf.toString());
		JSONObject jsObj = new JSONObject(param);
		JSONArray jsArr = new JSONArray(jsObj.getString("configs"));
		int provIdx, cityIdx;
		for(provIdx = 0; provIdx < jsArr.length(); provIdx++){
			String jsArrStr =  jsArr.getString(provIdx);
			JSONObject jsObjCity = new JSONObject(jsArrStr);
			String jsCityStr = jsObjCity.getString("citys");
			JSONArray jsCityArr = new JSONArray(jsCityStr);
			for(cityIdx = 0; cityIdx < jsCityArr.length(); cityIdx++){
				JSONObject cityObj = jsCityArr.getJSONObject(cityIdx);
				if(cityObj.getString("car_head").equals(platehead))
					return cityObj;
			}
		}

		return null;
	}
	
 	public static void prepareCityID() throws IOException, ParserConfigurationException, JSONException{
        File file = new File(JSON_FILE);  
        if (file.exists()) {  
            // 提示xml文件位置，不需要可以注释掉。  
            System.out.println("找到城市id文件 ");  //OBDApp.weizhangInit
            return;  
        }
       
        getConfig();

 	}
	/**
	 * title:
	 * 
	 * @param carInfo
	 * @return
	 */
	public static String getWeizhangInfoPost(String carInfo, String appId, String appKey) {
		long timestamp = System.currentTimeMillis();

		String line = null;
		String signStr = appId + carInfo + timestamp + appKey;
		String sign = md5(signStr);
		try {
			URL postUrl = new URL(URL + "/api/weizhang/query_task?");
			String content = "car_info=" + URLEncoder.encode(carInfo, "utf-8") + "&sign=" + sign + "&timestamp=" + timestamp + "&app_id=" + appId;
		
			System.out.println("URL="+postUrl+content);
			
			line = postInfo(postUrl, content);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return line;
	}

	/**
	 * title:
	 * 
	 * @return
	 * @throws IOException
	 */
	public static void getConfig() throws IOException {
		
		try {
			URL postUrl = new URL(URL + "/api/weizhang/get_all_config?");
			String content = "";
			postCity(postUrl, content);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * title:
	 * 
	 * @param postUrl
	 * @param content
	 * @return
	 */
	private static boolean postCity(URL postUrl, String content) {
		String line = null;
		StringBuffer cityIdCont = new StringBuffer();
		int bufferSize = 1024;
		byte[] buffer = new byte[1024];
		int readLen = 0;
		try {
			HttpURLConnection connection = (HttpURLConnection) postUrl.openConnection();
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setRequestMethod("POST");
			connection.setUseCaches(false);
			connection.setInstanceFollowRedirects(true);
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			connection.connect();
			DataOutputStream out = new DataOutputStream(connection.getOutputStream());
			out.writeBytes(content);
			out.flush();
			out.close();
//			BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));
//
//			while ((line = reader.readLine()) != null) {
//				return line;	
//			}
//			reader.close();
			InputStream in_s = connection.getInputStream();
			FileOutputStream out_s = new FileOutputStream(JSON_FILE);
			while((readLen = in_s.read(buffer, 0, bufferSize)) > 0){
				out_s.write(buffer, 0, readLen);				
			}
			connection.disconnect();
			out_s.close();
			in_s.close();

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;

	}

	private static String postInfo(URL postUrl, String content) {
		String line = null;
		StringBuffer infoCont = new StringBuffer();
		int bufferSize = 1024;
		byte[] buffer = new byte[1024];
		int readLen = 0;
		try {
			HttpURLConnection connection = (HttpURLConnection) postUrl.openConnection();
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setRequestMethod("POST");
			connection.setUseCaches(false);
			connection.setInstanceFollowRedirects(true);
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			connection.connect();
			DataOutputStream out = new DataOutputStream(connection.getOutputStream());
			out.writeBytes(content);
			out.flush();
			out.close();
			BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));

			while ((line = reader.readLine()) != null) {
				infoCont.append(line);	
			}
			reader.close();
			
			

		} catch (Exception e) {
			e.printStackTrace();
		}
		return infoCont.toString();

	}
	/**
	 * title:md5 (http://tool.chinaz.com/Tools/MD5.aspx) 
	 * 
	 * @param password
	 * @return
	 */
	private static String md5(String msg) {
		try {
			MessageDigest instance = MessageDigest.getInstance("MD5");
			instance.update(msg.getBytes("UTF-8"));
			byte[] md = instance.digest();
			return byteArrayToHex(md);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private static String byteArrayToHex(byte[] a) {
		StringBuilder sb = new StringBuilder();
		for (byte b : a) {
			sb.append(String.format("%02x", b & 0xff));
		}
		return sb.toString();
	}
}