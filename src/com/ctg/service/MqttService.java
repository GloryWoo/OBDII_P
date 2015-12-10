package com.ctg.service;




import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.List;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.ctg.ui.Base;
import com.ctg.ui.R;
import com.ctg.util.Preference;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class MqttService extends Service {
	// this is the log tag
	public static final String TAG = "MqttService";
	public static final int INVITE_NOTIFY = 100;
	public static final int MSG_NOTIFY = 101;
	public static final int AGREE_NOTIFY = 102;
	public static final int INVITE_FRIEND_NOTIFY = 103;
	public static final int AGREE_FRIEND_NOTIFY = 104;
	public static final int INVITE_CARPOOL = 105;
	public static final int REPLY_CARPOOL = 106;
	
	public static final int DELETE_GRP_USER = 107;
    //gps share
    public static final int GPS_SHARE_NOTIFY = 201;
    public static final int TRACE_SHARE_NOTIFY = 202;

	// appid
	public static String MQTT_APP_ID = "appid";

	// the default IP address port, where your MQTT broker is running.
	private String host = "tcp://116.236.202.130:8090";//"tcp://192.168.1.51:1884";
	// private String userName = "admin";
	// private String passWord = "password";
	private MqttClient client;
	private MqttConnectOptions options;

	// These are the actions for the service (name are descriptive enough)
	private static final String ACTION_START = MQTT_APP_ID + ".START";
	private static final String ACTION_STOP = MQTT_APP_ID + ".STOP";

	// Connectivity manager to determining, when the phone loses connection
	private ConnectivityManager mConnMan;
	private Bundle bundle = null;
	private Intent messageIntent = null;
	
	NotificationManager mNotificationManager;
    NotificationCompat.Builder mBuilder; 

	@Override
	public IBinder onBind(Intent arg0) {
		if (arg0.getAction().equals(ACTION_STOP) == true) {
			Log.d(TAG, "Service stop with intent----------------------");
			stop();
			stopSelf();
		} else if (arg0.getAction().equals(ACTION_START) == true) {
			Log.d(TAG, "Service start with intent----------------------");
			start();
			Base.OBDApp.mqttStat = 1;
		}
		return null;
	}


	// Static method to start the service
	public static void actionStart(Context ctx) {
		Intent i = new Intent(ctx, MqttService.class);
		i.setAction(ACTION_START);
		ctx.startService(i);
		Log.d(TAG, "-----------------actionStart");
	}

	// Static method to stop the service
	public static void actionStop(Context ctx) {
		Intent i = new Intent(ctx, MqttService.class);
		i.setAction(ACTION_STOP);
		ctx.startService(i);
		Log.d(TAG, "-----------------actionStop");
	}

	// Check if we are online
	private boolean isNetworkAvailable() {
		mConnMan = (ConnectivityManager) this
				.getSystemService(CONNECTIVITY_SERVICE);
		NetworkInfo info = mConnMan.getActiveNetworkInfo();
		if (info == null) {
			return false;
		}
		Log.d(TAG,
				"check isNetworkAvailable--------------" + info.isConnected());
		return info.isConnected();
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
	    mBuilder = new NotificationCompat.Builder(this);  
		Log.d(TAG, "onCreate------------------------");
		init();
	}

	private void init() {
		Log.d(TAG, "init------------start");
		try {
			//clientID 必须唯一
			String clientID="testid"+ System.currentTimeMillis();
			Log.d(TAG, "clientID------------"+clientID);
			// host为主机名，test为clientid即连接MQTT的客户端ID，一般以客户端唯一标识符表示，MemoryPersistence设置clientid的保存形式，默认为以内存保存
			client = new MqttClient(host, clientID, new MemoryPersistence());
			// MQTT的连接设置
			options = new MqttConnectOptions();
			// 设置是否清空session,这里如果设置为false表示服务器会保留客户端的连接记录，这里设置为true表示每次连接到服务器都以新的身份连接
			options.setCleanSession(true);
			// 设置连接的用户名
			// options.setUserName(userName);
			// 设置连接的密码
			// options.setPassword(passWord.toCharArray());
			// 设置超时时间 单位为秒
			options.setConnectionTimeout(10);
			// 设置会话心跳时间 单位为秒 服务器会每隔1.5*20秒的时间向客户端发送个消息判断客户端是否在线，但这个方法并没有重连的机制
			options.setKeepAliveInterval(20);
			// 设置回调
			client.setCallback(new MqttCallback() {
				@Override
				public void connectionLost(Throwable cause) {
					// restart connect,when connection is lost
					System.out.println("connectionLost----------");
					// mConnected=false;
					Log.d(TAG, "  NetworkAvailable---is--------2----------");
					if (isNetworkAvailable() == true) {
						connect();
					} else {
						sendMessageBroadcast("Network is unAvailable"
								.toString());
					}
				}

				@Override
				public void deliveryComplete(IMqttDeliveryToken token) {
					// publish ok
					System.out.println("deliveryComplete---------"
							+ token.isComplete());
				}

				@Override
				public void messageArrived(String topicName, MqttMessage message)
						throws Exception {
					// todo
					// subscribe后得到的消息会执行到这里面
					System.out.println("messageArrived----------"+message.toString());
					JSONObject json=new JSONObject(message.toString());
					String action=json.getString("action");

					if(action.equals("invite")){
						//invite(json);
						processIncomingMessage(INVITE_NOTIFY, json);
					}else if(action.equals("message")){
						//getMessage(json);
						processIncomingMessage(MSG_NOTIFY, json);
					}else if(action.equals("agree")){
                        //agreeMessage(json);
						processIncomingMessage(AGREE_NOTIFY, json);
                    }else if(action.equals("leave")){
                    	processIncomingMessage(DELETE_GRP_USER, json);
                    }
					else if(action.equals("inviteFriend")){
						processIncomingMessage(INVITE_FRIEND_NOTIFY, json);
					}
					else if(action.equals("agreeFriend")){
						processIncomingMessage(AGREE_FRIEND_NOTIFY, json);
					}
					else if(action.equals("carPool")){
						processIncomingMessage(INVITE_CARPOOL, json);
					}
					else if(action.equals("replyCarPool")){
						processIncomingMessage(REPLY_CARPOOL, json);
					}
                    else if(action.equals("gps_share")){
                    	Log.d(TAG, "get_gps_share");
                        getUserShare(GPS_SHARE_NOTIFY, json);
                    }
                    else if(action.equals("trace_share")){
                    	Log.d(TAG, "get_trace_share");
                        getUserShare(TRACE_SHARE_NOTIFY, json);
                    }


				}
			});
			Log.d(TAG, "init----------------------end");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		if(client == null)
			return;
//		if(isServiceRunning(this, "com.ctg.service.MqttService")){
//			Log.d(TAG, "MqttService running");
//			return;
//		}
//		Log.d(TAG, "MqttService running not");
//		if (intent.getAction().equals(ACTION_STOP) == true) {
//			Log.d(TAG, "Service stop with intent----------------------");
//			stop();
//			stopSelf();
//		} else if (intent.getAction().equals(ACTION_START) == true) {
//			Log.d(TAG, "Service start with intent----------------------");
//			start();
//			Base.OBDApp.mqttStat = 1;
//		}
	}

	private synchronized void start() {
		Log.d(TAG, "Attempt Starting service-------------------");
		// Do nothing, if the service is already running.
		if (client.isConnected()) {
			Log.w(TAG, "Attempt to start connection that is already active");
			return;
		} else {
			Log.d(TAG, "Service start with intent---------connect-------------");
			connect();
		}
	}

	private synchronized void stop() {
		// Do nothing, if the service is not running.
		Log.w(TAG, "Attempt to Stop service-------------------.");
		try {
			Log.w(TAG, "Attempt to Stop service----------disconnect---------.");
			if(client.isConnected())
			      client.disconnect();
			client = null;
			Base.OBDApp.mqttStat = 0;
		} catch (MqttException e) {
			e.printStackTrace();
		}
	}

	// start connect
	private void connect() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				Log.d(TAG, "Starting connect--------------");
				try {
					if (client != null) {
						while (!client.isConnected()) {
							Log.d(TAG, "----------------while--------------");
							client.connect(options);
							client.subscribe(MQTT_APP_ID, 1);
						}
					}
					Log.d(TAG, "Starting connect--------------end");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
	}


//	// sure service don't kill
//	@Override
//	public int onStartCommand(Intent intent, int flags, int startId) {
//		return START_STICKY;
//	}
	void processReceivedInfo(int type, JSONObject json){
		Intent intent = new Intent();
		
		intent.putExtra("type", type);
		intent.putExtra("json", json.toString());
		if(Base.OBDApp.baseAct != null){
			Base.OBDApp.baseAct.setIntent(intent);
			Base.OBDApp.baseAct.msgHandler.obtainMessage(Base.INCOMING_MSG).sendToTarget();
		}
//		try {
//			Base.OBDApp.baseAct.processExtraData();
//		} catch (JSONException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}
    
    void processIncomingMessage(int type, JSONObject json){
    	
    	 String loginuser=Preference.getInstance(getApplicationContext()).getUser();//get login user
         try {
             Object userlistObj=json.get("users");
             JSONArray userlist = null;
             Object obj = null;
             JSONObject jsonObj = null;
             String user = "";
             if(userlistObj instanceof JSONArray){
	             userlist = (JSONArray)userlistObj;
	             for (int i = 0; i < userlist.length(); i++) {
	            	obj = userlist.get(i);
	     			if(obj instanceof JSONObject){
	     				jsonObj = (JSONObject)obj;
	     				user = jsonObj.getString("user");
	    			}
	    			else if(obj instanceof String){
	    				user = (String)obj;
	    			}
	     			if(user.equals(loginuser)){
	                 	break;
	                }
	                 
	             }
             }
  			if(userlistObj instanceof JSONObject){
 				jsonObj = (JSONObject)userlistObj;
 				user = jsonObj.getString("user");
			}
			else if(userlistObj instanceof String){
				user = (String)userlistObj;
			}
 			if(user.equals(loginuser)){
             	if(Base.OBDApp != null && Base.isApplicationForeground(Base.OBDApp.baseAct))
             		processReceivedInfo(type, json);
             	else
             		notificationMessage(type, json);
            }
         } catch (JSONException e) {
             e.printStackTrace();
         }
    }

    
    protected void getUserShare(int type, JSONObject json) {
        String loginuser=Preference.getInstance(getApplicationContext()).getUser();
        try {
            String user = json.getString("fromUser");
            if(!user.equals(loginuser)){
                if(Base.OBDApp != null && Base.isApplicationForeground(Base.OBDApp.baseAct))
                    processReceivedInfo(type, json);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }



    /**
	 * 发送广播，通知UI层时间已改变
	 */
	private void sendMessageBroadcast(String message) {
//		Log.d(TAG,"BroadcastReceiver-------send-------" );
//		bundle=new Bundle();
//		bundle.putString("message", message);
//		messageIntent=new Intent();
//		messageIntent.putExtras(bundle);
//		messageIntent.setAction(myActivitity.MQTT);
//		// 发送广播，通知UI层时间改变了
//		sendBroadcast(messageIntent);
		if(Base.OBDApp != null){
			
		}
	}
	
	
	private void notificationMessage(int type, JSONObject json){
		Log.d(TAG, "notificationMessage --------------message"+json.toString());
		try{
			String titleSz[] = {"收到邀请加群", "收到服务器消息", "对方同意加群", "收到邀请添加好友","对方同意成为好友", "收到邀请", "你发出邀请收到回应"};
			String msgSz[] = {json.getString("fromUser")+"邀请加入群组："+json.getString("groupName"),
							  "收到"+json.getString("fromUser")+"消息",
							  json.getString("fromUser")+"同意加入群组："+json.getString("groupName"),
							  json.getString("fromUser")+"邀请你成为好友",
							  json.getString("fromUser")+"同意加为好友",
							  json.getString("fromUser")+"将开车来接你",
							  json.getString("fromUser")+ (json.getString("type").equals(0)?"拒绝":"接受")+"你的邀请"
			};
		
			String title = titleSz[type-INVITE_NOTIFY];
			String content = msgSz[type-INVITE_NOTIFY];
		    mBuilder.setContentTitle(title)//设置通知栏标题  
	        .setContentText(content)  
	        .setTicker("收到优车宝服务器消息") //通知首次出现在通知栏，带上升动画效果的  
	        .setWhen(System.currentTimeMillis())//通知产生的时间，会在通知信息里显示，一般是系统获取到的时间  
	        .setPriority(Notification.PRIORITY_DEFAULT) //设置该通知优先级  
	        .setAutoCancel(true)//设置这个标志当用户单击面板就可以让通知将自动取消    
	        .setOngoing(false)//ture，设置他为一个正在进行的通知。他们通常是用来表示一个后台任务,用户积极参与(如播放音乐)或以某种方式正在等待,因此占用设备(如一个文件下载,同步操作,主动网络连接)  
	        .setDefaults(Notification.DEFAULT_VIBRATE)//向通知添加声音、闪灯和振动效果的最简单、最一致的方式是使用当前的用户默认设置，使用defaults属性，可以组合  
	        //Notification.DEFAULT_ALL  Notification.DEFAULT_SOUND 添加声音 // requires VIBRATE permission  
	        .setSmallIcon(android.R.drawable.star_on);//设置通知小ICON 
		    
		    Intent notifi_i = new Intent(this, Base.class);
			notifi_i.putExtra("type", type);
			notifi_i.putExtra("json", json.toString());
			notifi_i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//FLAG_ACTIVITY_SINGLE_TOP
			PendingIntent pending_i = PendingIntent.getActivity(this, type, notifi_i, PendingIntent.FLAG_UPDATE_CURRENT);		
			
			mBuilder.setContentIntent(pending_i);
		    mNotificationManager.notify(type, mBuilder.build());  
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally{}
	}

	@Override
	// 当Service不在使用时调用
	public void onDestroy() {
		Log.i(TAG, "Service onDestroy--->");
		super.onDestroy();
		Base.OBDApp.mqttStat = 0;
		try {
			client.disconnect();
			client.close();
		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		client = null;
	}

	public static boolean isServiceRunning(Context mContext,String className) {
		boolean isRunning = false;
		ActivityManager activityManager = (ActivityManager) mContext
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<ActivityManager.RunningServiceInfo> serviceList = activityManager
				.getRunningServices(30);
		if (!(serviceList.size() > 0)) {
			return false;
		}
		for (int i = 0; i < serviceList.size(); i++) {
			if (serviceList.get(i).service.getClassName().equals(className) == true) {
				isRunning = true;
				break;
			}
		}
		return isRunning;
	}
}