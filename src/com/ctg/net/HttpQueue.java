package com.ctg.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.baidu.mapapi.model.LatLng;
import com.ctg.group.ChatMsgEntity;
import com.ctg.group.Group;
import com.ctg.group.Member;
import com.ctg.ui.Base;
import com.ctg.ui.OBDApplication;
import com.ctg.util.Preference;
import com.ctg.util.VoltageHistory;

public class HttpQueue implements Runnable, Serializable {

	public static final String KEY_TRANSFER_STATUS = "httpstatus";
	public static final String KEY_TRANSFER_CLASS = "HttpQueueClass";
	public static final String KEY_TRANSFER_RESULT_STR = "HttpQueueResultString";
	public static final String KEY_RET_CODE = "ret_code";
	public static final String SESSIONID = "sessionid";
	public static final String PLATE = "plate";
	public static final String BRAND = "brand";
	public static final String MODEL = "model";
	public static final String PURCHASE = "purchase_date";
	public static final String VERSION = "version";
	public static final String DOWNSIZE = "down_size";
	public static final String DOWNURL = "down_url";
	public static final String VERDESC = "version_description";
	public static final String KEY_FIND_PASSWORD = "receivepwmail";
	public static final String KEY_RESET_PW = "resetpassword";

	public static final int CONNECT_FAILED = 0;

	public static final int CONNECT_SUCCEED = 1;

	final public static int CREATE_GROUP = 0x108;
	final public static int DEL_GROUP = 0x109;
	final public static int UPDATE_GROUP = 0x10a;
	final public static int FIND_GROUP = 0x10b;
	final public static int ADD_USER2GROUP = 0x10c;
	final public static int DEL_USER_FROM_GROUP = 0x10d;
	final public static int LIST_MEMBER_IN_GROUP = 0x10e;
	final public static int FILTER_USERS = 0x10f;
	final public static int INVITE_USERS2GROUP = 0x110;

	private static Thread runner;

	public static Handler handler;
	public static Handler queryHandler;

	private static Base baseAct;

	public String url;

	public static int result;

	private String sessionid;

	private String plate;
	private String brand;
	private String model;
	private String purchase_date;

	public JSONObject param;

	public int option;// 1 register; 2 login; 3 logout; 4 check version

	// public int grpIdx;
	private static int retCode;
	private static Queue<HttpQueue> taskQue;

	public static ArrayList<Group> grpResLst;
	public static ArrayList<Group> grpCreatorLst;
	public static ArrayList<Group> grpSearchLst;
	public static ArrayList<Member> grpSearchMemberLst;
	public static ArrayList<Member> grpSelMemberLst;
	public static ArrayList<Member> friendLst;
	// public GroupRes groupRes;

	public String resultStr;
	public ChatMsgEntity msgEntity;

	static HttpQueue instance;// 类的单例

	public static HttpQueue getInstance(Context context)// 返回 单例
	{
		if (instance == null) {
			instance = new HttpQueue(context);
			taskQue = new LinkedList<HttpQueue>();
			handler = Base.httpQueueHandler;
		}
		return instance;
	}

	public synchronized void EnQueue(String url, JSONObject param, int option) {
		HttpQueue httpItem = new HttpQueue(url, param, option);
		taskQue.add(httpItem);
		startHttp();
	}
	//用于查询违章
	public synchronized void EnQueue(String url, JSONObject param, int option, Handler handler) {
		HttpQueue httpItem = new HttpQueue(url, param, option);
		this.queryHandler = handler;
		taskQue.add(httpItem);
		startHttp();
	}

	public synchronized void EnQueue(String url, JSONObject param, int option,
			ChatMsgEntity msgEntity) {
		HttpQueue httpItem = new HttpQueue(url, param, option, msgEntity);
		taskQue.add(httpItem);
		startHttp();
	}


	public HttpQueue(String url, JSONObject param, int option,
			ChatMsgEntity msgEntity) {
		this.url = url;
		this.param = param;
		// result = 0;
		this.option = option;
		this.msgEntity = msgEntity;
	}

	public HttpQueue(String url, JSONObject param, int option) {
		this.url = url;
		this.param = param;
		// result = 0;
		this.option = option;
	}

	public HttpQueue(Context context, Handler handler) {
		baseAct = (Base) context;
		this.handler = handler;
	}

	public HttpQueue(Context context) {
		// TODO Auto-generated constructor stub
		this.baseAct = (Base) context;

	}

	public synchronized void startHttp() {
		if (runner == null) {
			runner = new Thread(this);
			runner.start();
		}
	}

	public void stopHttp() {
		if (runner != null) {
			runner.stop();
			runner = null;
		}
	}

	public synchronized void stopThread() {
		if (runner != null) {
			Thread moribund = runner;
			runner = null;
			moribund.interrupt();
		}
	}

	private synchronized void sendMsg(int what, HttpQueue item, int val) {

		if (handler != null) {
			Message msg = handler.obtainMessage();
			msg.what = what;
			Bundle bb = new Bundle();
			bb.putInt(KEY_TRANSFER_STATUS, val);
			bb.putSerializable(KEY_TRANSFER_CLASS, item);
			msg.setData(bb);
			handler.sendMessage(msg);
		}
	}

	public static String sendPost(String url, String param, String token) {
		PrintWriter out = null;
		BufferedReader in = null;
		String result = "";
		try {
			URL realUrl = new URL(url);
			// 打开和URL之间的连接
			URLConnection conn = realUrl.openConnection();
			// 设置通用的请求属性
			conn.setRequestProperty("accept", "*/*");
			conn.setRequestProperty("connection", "Keep-Alive");
			conn.setRequestProperty("user-agent",
					"Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
			conn.setRequestProperty("X-token", token);
			// 发送POST请求必须设置如下两行
			conn.setDoOutput(true);
			conn.setDoInput(true);
			// 获取URLConnection对象对应的输出流
			out = new PrintWriter(conn.getOutputStream());
			if (!param.equals("")) {
				// 发送请求参数
				out.print(param);
			}
			// flush输出流的缓冲
			out.flush();
			// 定义BufferedReader输入流来读取URL的响应
			in = new BufferedReader(
					new InputStreamReader(conn.getInputStream()));
			String line;
			while ((line = in.readLine()) != null) {
				result += line;
			}
		} catch (Exception e) {
			System.out.println("发送 POST 请求出现异常！" + e);
			e.printStackTrace();
		}
		// 使用finally块来关闭输出流、输入流
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
		return result;
	}

	public synchronized void run() {
		DefaultHttpClient mHttpClient = new DefaultHttpClient();
		// 请求超时
		mHttpClient.getParams().setParameter(
				CoreConnectionPNames.CONNECTION_TIMEOUT, 30000);
		// 读取超时
		mHttpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT,
				30000);
		StringEntity se;
		HttpQueue httpItem = null;
		HttpResponse response = null;
		// String version =
		// Preference.getInstance(context.getApplicationContext()).getVersion();
		String version = ((com.ctg.ui.OBDApplication) baseAct
				.getApplicationContext()).getVersion();
		String sessionidExist = Preference.getInstance(
				baseAct.getApplicationContext()).getSessionId();
		StringBuffer regStrBuf = null;
		HttpGet mGet;
		HttpPost mPost;
		ChatMsgEntity msgEnti;
		// HttpPut mPut;

		while (!taskQue.isEmpty()) {
			try {
				result = 0;
				httpItem = taskQue.poll();
				switch (httpItem.option) {
				// group operation
				// create group
				case 11:
					mPost = new HttpPost(httpItem.url);
					mPost.setHeader("X-API-version",
							String.format("%s", version));
					mPost.setHeader("X-token", sessionidExist);
					mPost.setHeader("Content-Type",
							"application/x-www-form-urlencoded");
					// mPost.setHeader("accept", "*/*");
					// mPost.setHeader("connection", "Keep-Alive");
					// mPost.setHeader("user-agent",
					// "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
					regStrBuf = new StringBuffer();
					try {
						regStrBuf.append("appID=");
						regStrBuf.append(httpItem.param.get("appid"));
						regStrBuf.append("&groupName=");
						regStrBuf.append(URLEncoder.encode(
								(String) httpItem.param.get("groupName"),
								"UTF-8"));
						if (httpItem.param.get("groupdes") != null) {
							regStrBuf.append("&groupDes=");
							regStrBuf.append(httpItem.param.get("groupdes"));
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					se = new StringEntity(regStrBuf.toString());
					mPost.setEntity(se);
					response = mHttpClient.execute(mPost);
					// sendPost(httpItem.url, regStrBuf.toString(),
					// sessionidExist);
					break;
				// delete group
				case 12:
					mPost = new HttpPost(httpItem.url);
					mPost.setHeader("X-API-version",
							String.format("%s", version));
					mPost.setHeader("X-token", sessionidExist);
					mPost.setHeader("Content-Type",
							"application/x-www-form-urlencoded");
					regStrBuf = new StringBuffer();
					try {
						regStrBuf.append("appID=");
						regStrBuf.append(httpItem.param.get("appid"));
						regStrBuf.append("&groupName=");
						regStrBuf.append(URLEncoder.encode(
								(String) httpItem.param.get("groupName"),
								"UTF-8"));
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					se = new StringEntity(regStrBuf.toString());
					mPost.setEntity(se);
					response = mHttpClient.execute(mPost);
					break;
				// update group
				case 13:
					mPost = new HttpPost(httpItem.url);
					mPost.setHeader("X-API-version",
							String.format("%s", version));
					mPost.setHeader("X-token", sessionidExist);
					mPost.setHeader("Content-Type",
							"application/x-www-form-urlencoded");
					regStrBuf = new StringBuffer();
					try {
						regStrBuf.append("appID=");
						regStrBuf.append(httpItem.param.get("appid"));
						regStrBuf.append("&oldGroupName=");
						regStrBuf.append(URLEncoder.encode(
								(String) httpItem.param.get("oldGroupName"),
								"UTF-8"));
						regStrBuf.append("&newGroupName=");
						regStrBuf.append(URLEncoder.encode(
								(String) httpItem.param.get("newGroupName"),
								"UTF-8"));
						if (httpItem.param.get("groupDes") != null) {
							regStrBuf.append("&groupDes=");
							regStrBuf.append(httpItem.param.get("groupDes"));
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					se = new StringEntity(regStrBuf.toString());
					mPost.setEntity(se);
					response = mHttpClient.execute(mPost);
					// sendPost(httpItem.url, regStrBuf.toString(),
					// sessionidExist);
					break;

				// find group
				case 14:
				case 141:// not include memberlist
				case 142:// include memberlist
					mGet = new HttpGet(httpItem.url);
					mGet.setHeader("X-API-version",
							String.format("%s", version));
					mGet.setHeader("X-token", sessionidExist);
					response = mHttpClient.execute(mGet);
					break;
				// add user to a group
				case 15:
					mPost = new HttpPost(httpItem.url);
					mPost.setHeader("X-API-version",
							String.format("%s", version));
					mPost.setHeader("X-token", sessionidExist);
					mPost.setHeader("Content-Type",
							"application/x-www-form-urlencoded");
					regStrBuf = new StringBuffer();
					try {
						regStrBuf.append("appID=");
						regStrBuf.append(httpItem.param.get("appid"));
						regStrBuf.append("&groupName=");
						regStrBuf.append(URLEncoder.encode(
								(String) httpItem.param.get("groupName"),
								"UTF-8"));
						regStrBuf.append("&listMember=");
						regStrBuf.append(httpItem.param
								.getBoolean("listMember"));
						regStrBuf.append("&fromUser=");
						regStrBuf.append(httpItem.param.get("fromUser"));

					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					se = new StringEntity(regStrBuf.toString());
					mPost.setEntity(se);
					response = mHttpClient.execute(mPost);
					break;

				// usr quit from a group
				case 16:
					mPost = new HttpPost(httpItem.url);
					mPost.setHeader("X-API-version",
							String.format("%s", version));
					mPost.setHeader("X-token", sessionidExist);
					mPost.setHeader("Content-Type",
							"application/x-www-form-urlencoded");
					regStrBuf = new StringBuffer();
					try {
						regStrBuf.append("appID=");
						regStrBuf.append(httpItem.param.get("appid"));
						regStrBuf.append("&groupName=");
						regStrBuf.append(URLEncoder.encode(
								(String) httpItem.param.get("groupName"),
								"UTF-8"));
						regStrBuf.append("&userName=");
						regStrBuf.append(httpItem.param.get("userName"));
						if (httpItem.param.has("groupDes")) {
							regStrBuf.append("&groupDes=");
							regStrBuf.append(httpItem.param.get("groupDes"));
						}
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					se = new StringEntity(regStrBuf.toString());
					mPost.setEntity(se);
					response = mHttpClient.execute(mPost);
					break;

				// list group member
				case 17:
					mPost = new HttpPost(httpItem.url);
					mPost.setHeader("X-API-version",
							String.format("%s", version));
					mPost.setHeader("X-token", sessionidExist);
					mPost.setHeader("Content-Type",
							"application/x-www-form-urlencoded");
					regStrBuf = new StringBuffer();
					try {
						regStrBuf.append("appID=");
						regStrBuf.append(httpItem.param.get("appid"));
						regStrBuf.append("&name=");
						regStrBuf.append(URLEncoder.encode(
								(String) httpItem.param.get("groupName"),
								"UTF-8"));
						regStrBuf.append("&userName=");
						JSONArray jsonArr = (JSONArray) httpItem.param
								.get("users");
						regStrBuf.append(jsonArr);
						if (httpItem.param.get("groupDes") != null) {
							regStrBuf.append("&groupDes=");
							regStrBuf.append(httpItem.param.get("groupDes"));
						}
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					se = new StringEntity(regStrBuf.toString());
					mPost.setEntity(se);
					response = mHttpClient.execute(mPost);
					break;

				// search and filter users
				case 18:
					mPost = new HttpPost(httpItem.url);
					mPost.setHeader("X-API-version",
							String.format("%s", version));
					mPost.setHeader("X-token", sessionidExist);
					mPost.setHeader("Content-Type",
							"application/x-www-form-urlencoded");
					regStrBuf = new StringBuffer();
					// regStrBuf.append(httpItem.param.toString());
					try {
						regStrBuf.append("appID=");
						regStrBuf.append(httpItem.param.get("appid"));
						regStrBuf.append("&name=");
						regStrBuf.append(httpItem.param.get("name"));
						regStrBuf.append("&distance=");
						regStrBuf.append(httpItem.param.get("distance"));
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					se = new StringEntity(regStrBuf.toString());
					mPost.setEntity(se);
					response = mHttpClient.execute(mPost);
					break;

				// invite users to join into a group
				case 19:
					mPost = new HttpPost(httpItem.url);
					mPost.setHeader("X-API-version",
							String.format("%s", version));
					mPost.setHeader("X-token", sessionidExist);
					mPost.setHeader("Content-Type",
							"application/x-www-form-urlencoded");
					regStrBuf = new StringBuffer();
					try {
						regStrBuf.append("appID=");
						regStrBuf.append(httpItem.param.get("appid"));
						regStrBuf.append("&groupName=");
						regStrBuf.append(URLEncoder.encode(
								(String) httpItem.param.get("groupName"),
								"UTF-8"));
						regStrBuf.append("&users=");
						regStrBuf.append(httpItem.param.get("users"));
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					se = new StringEntity(regStrBuf.toString());
					mPost.setEntity(se);
					response = mHttpClient.execute(mPost);
					break;
				case 20:
					mPost = new HttpPost(httpItem.url);
					mPost.setHeader("X-API-version",
							String.format("%s", version));
					mPost.setHeader("X-token", sessionidExist);
					mPost.setHeader("Content-Type",
							"application/x-www-form-urlencoded");
					regStrBuf = new StringBuffer();
					msgEnti = httpItem.msgEntity;
					JSONArray jsonarray = new JSONArray();
					if(msgEnti.usrsList != null)
						for(String str:msgEnti.usrsList){
							jsonarray.put(str);
						}
					try {
						regStrBuf.append("appID=");
						regStrBuf.append("appid");						
						regStrBuf.append("&groupName=");
						if(msgEnti.groupName != null)
							regStrBuf.append(URLEncoder.encode(msgEnti.groupName,"UTF-8"));
						regStrBuf.append("&users=");
						if(msgEnti.groupName != null && !msgEnti.groupName.equals("")){
							int idx = Group.indexOfByName(HttpQueue.grpResLst, msgEnti.groupName);
							Group grp = HttpQueue.grpResLst.get(idx);
							for(Member member:grp.memberList){
								if(!member.name.equals(Base.loginUser))
									jsonarray.put(member.name);
							}
						}
						regStrBuf.append(jsonarray);
						JSONObject obj_enti = new JSONObject();
						obj_enti.put("from", msgEnti.name);
						if(msgEnti.groupName != null)
							obj_enti.put("group", URLEncoder.encode(msgEnti.groupName,"UTF-8"));
						obj_enti.put("users", jsonarray);													
						if (msgEnti.msgType == ChatMsgEntity.CHAT_MSG_TEXT) {
							obj_enti.put("type", ChatMsgEntity.CHAT_MSG_TEXT);
							obj_enti.put("text", URLEncoder.encode(msgEnti.text, "UTF-8"));
						} else if (msgEnti.msgType == ChatMsgEntity.CHAT_MSG_LOCATE) {
							obj_enti.put("type", ChatMsgEntity.CHAT_MSG_LOCATE);

							obj_enti.put("lat", msgEnti.latlon_loc.latitude);
							obj_enti.put("lon", msgEnti.latlon_loc.longitude);
							
						} else if (msgEnti.msgType == ChatMsgEntity.CHAT_MSG_TRACK) {
							obj_enti.put("type", ChatMsgEntity.CHAT_MSG_TRACK);
							JSONArray jsonArr = new JSONArray();
							for (LatLng latlon : msgEnti.latlon_track) {
								jsonArr.put(latlon.latitude);
								jsonArr.put(latlon.longitude);
							}
							obj_enti.put("latlon_lst", jsonArr);

						}
						regStrBuf.append("&messages=");
						regStrBuf.append(obj_enti);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					se = new StringEntity(regStrBuf.toString());
					mPost.setEntity(se);
					response = mHttpClient.execute(mPost);
					break;
				case 30:
					// Toast.makeText(baseAct, "enable", 0).show();
					mPost = new HttpPost(httpItem.url);
					mPost.setHeader("X-API-version",
							String.format("%s", version));
					mPost.setHeader("X-token", sessionidExist);
					mPost.setHeader("Content-Type",
							"application/x-www-form-urlencoded");
					regStrBuf = new StringBuffer();
					try {
						regStrBuf.append("time=");
						regStrBuf.append(httpItem.param.get("time"));
						regStrBuf.append("&latitude=");
						regStrBuf.append(httpItem.param.get("latitude"));
						regStrBuf.append("&longitude=");
						regStrBuf.append(httpItem.param.get("longitude"));
                        regStrBuf.append("&traceId=");
                        regStrBuf.append(httpItem.param.get("traceId"));

					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					se = new StringEntity(regStrBuf.toString());
					;
					mPost.setEntity(se);
					response = mHttpClient.execute(mPost);
					Log.v("lzy", "BlueTooth_disable----------" + "reponse="
							+ response.getStatusLine().getStatusCode() +  "data:" +regStrBuf);
				case 31:
					// Toast.makeText(baseAct, "enable", 0).show();
					mPost = new HttpPost(httpItem.url);
					mPost.setHeader("X-API-version",
							String.format("%s", version));
					mPost.setHeader("X-token", sessionidExist);
					mPost.setHeader("Content-Type",
							"application/x-www-form-urlencoded");
					regStrBuf = new StringBuffer();
					try {
						regStrBuf.append("time=");
						regStrBuf.append(httpItem.param.get("time"));
						regStrBuf.append("&latitude=");
						regStrBuf.append(httpItem.param.get("latitude"));
						regStrBuf.append("&longitude=");
						regStrBuf.append(httpItem.param.get("longitude"));
						regStrBuf.append("&speed=");
						regStrBuf.append(httpItem.param.get("speed"));
						regStrBuf.append("&rotate=");
						regStrBuf.append(httpItem.param.get("rotate"));
						regStrBuf.append("&temperature=");
						regStrBuf.append(httpItem.param.get("temperature"));
                        regStrBuf.append("&traceId=");
						regStrBuf.append(httpItem.param.get("traceId"));


					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					se = new StringEntity(regStrBuf.toString());
					mPost.setEntity(se);
					response = mHttpClient.execute(mPost);
					Log.v("lzy", "BlueTooth_able----------" + "reponse="
							+ response.getStatusLine().getStatusCode() + "data:" + regStrBuf);
					break;
			
				case 50://invite friend
					mPost = new HttpPost(httpItem.url);
					mPost.setHeader("X-API-version",
							String.format("%s", version));
					mPost.setHeader("X-token", sessionidExist);
					mPost.setHeader("Content-Type",
							"application/x-www-form-urlencoded");
					regStrBuf = new StringBuffer();
					try {
						regStrBuf.append("appID=");
						regStrBuf.append("appid");
						regStrBuf.append("&users=");
						regStrBuf.append(httpItem.param.get("users"));
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					se = new StringEntity(regStrBuf.toString());
					mPost.setEntity(se);
					response = mHttpClient.execute(mPost);
					break;
					
				case 51://add friend
					mPost = new HttpPost(httpItem.url);
					mPost.setHeader("X-API-version",
							String.format("%s", version));
					mPost.setHeader("X-token", sessionidExist);
					mPost.setHeader("Content-Type",
							"application/x-www-form-urlencoded");
					regStrBuf = new StringBuffer();
					try {
						regStrBuf.append("appID=");
						regStrBuf.append("appid");
						regStrBuf.append("&fromUser=");
						regStrBuf.append(httpItem.param.get("fromUser"));
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					se = new StringEntity(regStrBuf.toString());
					mPost.setEntity(se);
					response = mHttpClient.execute(mPost);
					break;	
					
				case 52://list friends
					mPost = new HttpPost(httpItem.url);
					mPost.setHeader("X-API-version",
							String.format("%s", version));
					mPost.setHeader("X-token", sessionidExist);
					mPost.setHeader("Content-Type",
							"application/x-www-form-urlencoded");
					regStrBuf = new StringBuffer();				
					regStrBuf.append("appID=");
					regStrBuf.append("appid");					
					se = new StringEntity(regStrBuf.toString());
					mPost.setEntity(se);
					response = mHttpClient.execute(mPost);
					break;
				
				case 53://delete friends
					mPost = new HttpPost(httpItem.url);
					mPost.setHeader("X-API-version",
							String.format("%s", version));
					mPost.setHeader("X-token", sessionidExist);
					mPost.setHeader("Content-Type",
							"application/x-www-form-urlencoded");
					regStrBuf = new StringBuffer();				
					try {
						regStrBuf.append("appID=");
						regStrBuf.append("appid");
						regStrBuf.append("&users=");
						regStrBuf.append(httpItem.param.get("users"));
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}				
					se = new StringEntity(regStrBuf.toString());
					mPost.setEntity(se);
					response = mHttpClient.execute(mPost);
					break;
					
				case 32:
					// Toast.makeText(baseAct, "enable", 0).show();
					mPost = new HttpPost(httpItem.url);
					mPost.setHeader("X-API-version",
							String.format("%s", version));
					mPost.setHeader("X-token", sessionidExist);
					mPost.setHeader("Content-Type",
							"application/x-www-form-urlencoded");
					regStrBuf = new StringBuffer();
					try {
						regStrBuf.append("PlateNo=");
						regStrBuf.append(httpItem.param.getString("plateNo"));
						regStrBuf.append("&Vin=");
						regStrBuf.append(httpItem.param.get("vin"));
						regStrBuf.append("&EngineNo=");
						regStrBuf.append(httpItem.param.get("engineNo"));
						
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					se = new StringEntity(regStrBuf.toString(), HTTP.UTF_8);
					mPost.setEntity(se);
					response = mHttpClient.execute(mPost);
					Log.v("lzy", "query traffic violation data----------" + "reponse="
							+ response.getStatusLine().getStatusCode() + "data:" + regStrBuf);
					Message msg = queryHandler.obtainMessage();
					msg.what = 100;//QUERY_READY
					int query_retCode = response.getStatusLine().getStatusCode();
					msg.arg1 = query_retCode;
					msg.obj = EntityUtils.toString(response.getEntity(), HTTP.UTF_8);
					msg.sendToTarget();
					response = null;
					break;

				default:
					break;
				}

				if (response != null) {
					retCode = response.getStatusLine().getStatusCode();
					if (retCode == 200) {
						HttpEntity entity = response.getEntity();

						if (entity != null) {
							String recResult;
							JSONObject jsonObject = null;
							JSONArray jsonArray = null;
							// JSONObject data = null;
							// boolean resultd = false;
							recResult = EntityUtils
									.toString(entity, HTTP.UTF_8);
							httpItem.resultStr = recResult;
							if(!httpItem.resultStr.equals("[]"))
								result = 1;
							else
								result = 0;
						} else {
							result = 0;
						}
					} else if (retCode == 401)// unauthorized
					{
						if (Preference.getInstance(
								baseAct.getApplicationContext()).getLoginStat()) {
							Preference.getInstance(
									baseAct.getApplicationContext())
									.setLoginStat(false);
							if (baseAct != null && baseAct.setting_s != null) {
								baseAct.baseHandler.obtainMessage(Base.UNLOGIN)
										.sendToTarget();
							}
						}

					}
				}
			} catch (UnsupportedEncodingException e3) {
				result = 0;
			} catch (ClientProtocolException e2) {
				result = 0;
			} catch (IOException e2) {
				result = 0;
			}
			sendMsg(httpItem.option, httpItem, result);
		}
		runner = null;
	}

}
