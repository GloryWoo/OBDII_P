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

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lee on 15/1/6.
 */
public class ShareUserPos {

	private boolean bSharePos;
	private UserPos userPos;
	private Context context;
	private String groupName;
	private String[] usernames;
	private String count;

	public ShareUserPos(Context cxt) {
		context = cxt;
	}

	public boolean isbSharePos() {
		return bSharePos;
	}

	public void setbSharePos(boolean bSharePos, String groupName, String[] usernames,String count) {
		this.bSharePos = bSharePos;
		this.groupName = groupName;
		this.usernames = usernames;
		this.count = count;
		Thread thread = new Thread(runnable);
		thread.start();
	}

	public void setSharePos(boolean bSharePos, String groupName) {
		this.bSharePos = bSharePos;
		this.groupName = groupName;
		Thread thread = new Thread(runnable);
		thread.start();
	}

	Runnable runnable = new Runnable() {
		@Override
		public void run() {
			if(groupName == null)
				return;
			sendSetSharePosCommand(bSharePos, groupName);

		}
	};

	private void sendSetSharePosCommand(boolean bIsOpen, String groupName) {
		String version = ((com.ctg.ui.OBDApplication) context
				.getApplicationContext()).getVersion();
		String sessionidExist = Preference.getInstance(
				context.getApplicationContext()).getSessionId();

		HttpClient httpClient = new DefaultHttpClient();
		String url = Base.NEW_HTTP_ROOT_PATH + "/services/openSendGpsPush";
		HttpPost httpPost = new HttpPost(url);

		httpPost.setHeader("X-API-version", String.format("%s", version));
		httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
		httpPost.setHeader("X-token", sessionidExist);

		HttpResponse response = null;
		HttpEntity entity = null;
		//InputStream input = null;
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		try {
			params.add(new BasicNameValuePair("isOpen", Boolean
					.toString(bIsOpen)));
			params.add(new BasicNameValuePair("appID", "appid"));
			params.add(new BasicNameValuePair("groupName", URLEncoder.encode(
					groupName, "utf-8")));
			params.add(new BasicNameValuePair("users", URLEncoder.encode(
					groupName, "utf-8")));
			params.add(new BasicNameValuePair("count", count));
			httpPost.setEntity(new UrlEncodedFormEntity(params));
			response = httpClient.execute(httpPost);

			if (response.getStatusLine().getStatusCode() == 200) {
				System.out.println("200");
				entity = response.getEntity();
				//input = entity.getContent();
			} else if (response.getStatusLine().getStatusCode() == 500) {
				System.out.println("500");
			} else if (response.getStatusLine().getStatusCode() == 404) {
				System.out.println("404");
			} else {
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
