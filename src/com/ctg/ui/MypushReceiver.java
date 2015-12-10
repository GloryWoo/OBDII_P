package com.ctg.ui;

import com.ctg.service.CarDataService;

//import cn.jpush.android.api.JPushInterface;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;


public class MypushReceiver extends BroadcastReceiver {
	
	private static final String TAG = "MyReceiver";  
	private static boolean D = true;
	private NotificationManager nm = null;
	private Notification mnotification = null;
	private PendingIntent pendingintent = null;
	public int notitoken = 0;

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		/*Bundle bundle = intent.getExtras();        
		Log.d(TAG, "onReceive - " + intent.getAction());                 
		if (JPushInterface.ACTION_REGISTRATION_ID.equals(intent.getAction())) 
		{                     
		} else if (JPushInterface.ACTION_MESSAGE_RECEIVED.equals(intent.getAction())) {            
			System.out.println("xx" + bundle.getString(JPushInterface.EXTRA_MESSAGE));                 
		} else if (JPushInterface.ACTION_NOTIFICATION_RECEIVED.equals(intent.getAction())) {            
			System.out.println("yy");                  
        } else if (JPushInterface.ACTION_NOTIFICATION_OPENED.equals(intent.getAction())) {            
        	System.out.println("zz");                    
        	Intent i = new Intent(context, Base.class);              
        	i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);            
        	context.startActivity(i);           
        }
        else {            
        	Log.d(TAG, "Unhandled intent - " + intent.getAction());        
        }*/
//		if (null == nm) {            
//			nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);        
//		}                 
//		Bundle bundle = intent.getExtras();        
//		Log.d(TAG, "onReceive - " + intent.getAction());                 
//		if (JPushInterface.ACTION_REGISTRATION_ID.equals(intent.getAction())) 
//		{            
//			if(D) Log.d(TAG, "JPush用户注册成功");                     
//		} else if (JPushInterface.ACTION_MESSAGE_RECEIVED.equals(intent.getAction())) 
//		{    
//			receivingMessage(context,bundle);
//			if(D) Log.d(TAG, "接受到推送下来的自定义消息");                             
//		} else if (JPushInterface.ACTION_NOTIFICATION_RECEIVED.equals(intent.getAction())) 
//		{            
//			if(D) Log.d(TAG, "接受到推送下来的通知");                 
//			receivingNotification(context,bundle);         
//		} else if (JPushInterface.ACTION_NOTIFICATION_OPENED.equals(intent.getAction())) 
//		{            
//			if(D) Log.d(TAG, "用户点击打开了通知");                   
//			openNotification(context,bundle);         
//		} else 
//		{            
//			if(D) Log.d(TAG, "Unhandled intent - " + intent.getAction());        
//		} 

	}
	
	private void receivingMessage(Context context, Bundle bundle){        
//		String title = bundle.getString(JPushInterface.EXTRA_TITLE);    
//		String message = bundle.getString(JPushInterface.EXTRA_MESSAGE);
//		String extras = bundle.getString(JPushInterface.EXTRA_EXTRA);
//		if(D) Log.d(TAG, " title : " + title);          
//		if(D) Log.d(TAG, "message : " + message);                
//		if(D) Log.d(TAG, "extras : " + extras);
//		if(title == null)
//			title = "标题：无";
//		if(message == null)
//			message = "内容：无";
//		initNotification(context,title,message);	
	}	
	
	
		
	private void receivingNotification(Context context, Bundle bundle){        
//		String title = bundle.getString(JPushInterface.EXTRA_NOTIFICATION_TITLE);        
//		if(D) Log.d(TAG, " title : " + title);        
//		String message = bundle.getString(JPushInterface.EXTRA_ALERT);        
//		if(D) Log.d(TAG, "message : " + message);        
//		String extras = bundle.getString(JPushInterface.EXTRA_EXTRA);        
//		if(D) Log.d(TAG, "extras : " + extras);    
	}     
	
	private void openNotification(Context context, Bundle bundle){        
//		String extras = bundle.getString(JPushInterface.EXTRA_EXTRA);        
		String myValue = "";                        
		Intent mIntent = new Intent(context, Base.class);            
		mIntent.putExtras(bundle);            
		mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);            
		context.startActivity(mIntent);        

	}
	
	
	
		
	public void initNotification(Context context,String title, String content){
		mnotification = new Notification(com.ctg.ui.R.drawable.listpage_more_like_seleted_normal, "优车宝消息", System.currentTimeMillis());
		mnotification.flags |=  Notification.FLAG_AUTO_CANCEL;
		mnotification.flags |=  Notification.FLAG_SHOW_LIGHTS;
		mnotification.defaults = Notification.DEFAULT_LIGHTS;
		mnotification.ledARGB = Color.BLUE;
		mnotification.ledOnMS = 3000;  //3s
		Intent notificationintent = new Intent(context,Base.class);
		if(D) Log.d(TAG, "temporary is title" + title);
		String temporary = title + "+" + content;
		if(D) Log.d(TAG, "temporary is" + temporary); 
		notificationintent.putExtra("PushInfoTitle", title);
		notificationintent.putExtra("PushInfocontent", content);
		notificationintent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);//FLAG_ACTIVITY_SINGLE_TOP
		pendingintent = PendingIntent.getActivity(context, notitoken, notificationintent, PendingIntent.FLAG_UPDATE_CURRENT);
		if(D) Log.d(TAG, "init notification"); 
		if(title != null && content != null){
			mnotification.setLatestEventInfo(context, title, content, pendingintent); 
			if(D) Log.d(TAG, "title and content is" + title + "+" + content); 
		}
		else{
			if(D) Log.d(TAG, "title/content is null!"); 
		}		
		//This way can be used to show notification on bar;
		if(nm != null && mnotification != null){
			nm.notify(notitoken, mnotification);
			if(D) Log.d(TAG, "send notification" + notitoken); 
		}
		notitoken ++;
	}

}
