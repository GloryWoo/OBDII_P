package com.ctg.shareUserInfo;

import android.content.Context;

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
import org.json.JSONArray;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lee on 15/1/22.
 */
public class ShareUserTrace {
    private Context context;
    private long traceId;
    private String mGroupName;
    private List<String> mUserName;

    public ShareUserTrace(Context cxt){
        context = cxt;
    }

    public void shareTrace(long id, String group, List<String> userName){
        traceId = id;
        mGroupName = group;
        mUserName = userName;
        Thread thread = new Thread(runnable);
        thread.start();
    }


    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            sendSetSharePosCommand();
        }
    };

    private void sendSetSharePosCommand(){
        String version = ((com.ctg.ui.OBDApplication)context.getApplicationContext()).getVersion();
        String sessionidExist = Preference.getInstance(context.getApplicationContext()).getSessionId();

        HttpClient httpClient = new DefaultHttpClient();
        String url= Base.NEW_HTTP_ROOT_PATH + "/services/shareGpsTrace";
        HttpPost httpPost = new HttpPost(url);

        httpPost.setHeader("X-API-version", String.format("%s", version));
        httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
        httpPost.setHeader("X-token", sessionidExist);

        HttpResponse response=null;
        HttpEntity entity=null;
        InputStream input=null;
        List<NameValuePair> params=new ArrayList<NameValuePair>();
        try {
            JSONArray jsonArray = new JSONArray(mUserName);
            params.add(new BasicNameValuePair("appID", "appid"));
            params.add(new BasicNameValuePair("traceId", URLEncoder.encode(String.valueOf(traceId), "utf-8")));
            params.add(new BasicNameValuePair("groupName", URLEncoder.encode(mGroupName, "utf-8")));
            params.add(new BasicNameValuePair("users", jsonArray.toString()));

            httpPost.setEntity(new UrlEncodedFormEntity(params));
            response = httpClient.execute(httpPost);

            if(response.getStatusLine().getStatusCode()==200){
                System.out.println("200");
                entity=response.getEntity();
                input=entity.getContent();
            }
            else if(response.getStatusLine().getStatusCode()==500){
                System.out.println("500");
            }
            else if(response.getStatusLine().getStatusCode()==404){
                System.out.println("404");
            }
            else {
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
