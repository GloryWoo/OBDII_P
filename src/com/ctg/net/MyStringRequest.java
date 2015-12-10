package com.ctg.net;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Request.Method;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.HttpHeaderParser;

public class MyStringRequest extends Request<String>{
	private final Listener<String> mListener;
	
	private static Map<String, String> mHeader = new HashMap<String, String>();  
    /** 
     * 设置访问自己服务器时必须传递的参数，密钥等 
     */  
	public void setHeader(String token){
//		mHeader.clear();
//		mHeader.put("X-API-version", apiVersion);  
        mHeader.put("X-token", token);
	}
   
	public void setHeader(String key, String value){//String apiVersion, 
		mHeader.put(key, value);  
	}
	/**
     * Creates a new request with the given method.
     *
     * @param method the request {@link Method} to use
     * @param url URL to fetch the string at
     * @param listener Listener to receive the String response
     * @param errorListener Error listener, or null to ignore errors
     */
	public MyStringRequest(int method, String url, Listener<String> listener, ErrorListener errorListener) {
		super(method, url, errorListener);
		// TODO Auto-generated constructor stub
		mListener = listener;
	}
	
	/**
     * Creates a new GET request.
     *
     * @param url URL to fetch the string at
     * @param listener Listener to receive the String response
     * @param errorListener Error listener, or null to ignore errors
     */
    public MyStringRequest(String url, Listener<String> listener, ErrorListener errorListener) {
        this(Method.GET, url, listener, errorListener);
    }

	@Override
	protected Response<String> parseNetworkResponse(NetworkResponse response) {
		// TODO Auto-generated method stub
		String parsed;
        try {
            parsed = new String(response.data, HttpHeaderParser.parseCharset(response.headers));//;
        } catch (UnsupportedEncodingException e) {
            parsed = new String(response.data);
        }
        return Response.success(parsed, HttpHeaderParser.parseCacheHeaders(response));
	}

	@Override
	protected void deliverResponse(String response) {
		// TODO Auto-generated method stub
		mListener.onResponse(response);
	}

	@Override
	public Map<String, String> getHeaders() throws AuthFailureError {
		// TODO Auto-generated method stub
		return mHeader;
	}
	

}
