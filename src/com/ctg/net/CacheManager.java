package com.ctg.net;

import java.util.Map;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.ctg.sensor.OBDSensor;
import com.ctg.ui.OBDApplication;
import com.ctg.util.Preference;
import com.ctg.util.Util;
/**
 * 
 * @author Administrator
 *
 */

public class CacheManager {
	private static final int NET_ALL_UNAVAILABLE = 0;
	public static final int OK = 200;
	
	public static void getJson(final Context context,final String url,final IHttpCallback callback,final Map<String,String> postData){

        boolean isFromCache = false;//判断是否先从本地缓存获取
        if (!isFromCache) {//不从缓存获取
            int netStatus = Util.check();
            if( netStatus == NET_ALL_UNAVAILABLE )//网络不可用
            {                
               Toast.makeText(context, "请打开网络连接", 1).show();               
            }
            else{//网可用                
                Response.Listener<String> okHandler = new Response.Listener<String>() {
    				@Override
    				public void onResponse(String response) {
    					// Logger.info(LOGLEVEL, TAG,
    					// "response:"+prefix+url+"\n"+response);
    					Log.v("updata", "success");
    					if (callback!=null) {
                            callback.handle(OK, response);
                        }
    				}
    			};

    			Response.ErrorListener errorHandler = new Response.ErrorListener() {
    				@Override
    				public void onErrorResponse(VolleyError error) {
    					if (callback!=null) {
    						if(error != null && error.networkResponse != null){
    							callback.handle(error.networkResponse.statusCode,error.networkResponse);
    						}
    					}
    				}
    			};
                MyStringRequest req=null;
                if (postData!=null) {
                    req = new MyStringRequest(Request.Method.POST, url, okHandler,errorHandler) {
                        @Override
                        protected Map<String, String> getParams() throws AuthFailureError {

                            return postData;
                        }
                    };
                } else {
                	req = new MyStringRequest(url,okHandler,errorHandler);
                }
//                req.setHeader(((OBDApplication) context
//    					.getApplicationContext()).getVersion(), Preference
//    					.getInstance(context.getApplicationContext())
//    					.getSessionId());
                req.setHeader("Content-Type","application/x-www-form-urlencoded");                
                req.setHeader("X-appKey", "6830b011-3007-4798-ad27-a7b79af40b20");
                OBDApplication.getHttpClient().add(req);
            }

        }
        if(isFromCache){

        }
	} 
}
