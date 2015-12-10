package com.ctg.util;


import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.ctg.bluetooth.BluetoothSet;
import com.ctg.ui.Base;
import com.ctg.ui.R;

import android.app.Dialog;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;

import android.util.DisplayMetrics;

import android.view.Gravity;
import android.view.View;

import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class GrpAddMemberDlg extends Dialog { 
	private static final String TAG = "CreateGrpDlg";
    Button cancelButton;		
	private Base baseAct;
	EditText userE;
	EditText distE;
	Button searchBtn;

	private static int default_width = 160; //
	private static int default_height = 120;//

	public GrpAddMemberDlg(Context context, int layout, int style) {
		this(context, default_width, default_height, layout, style);
		
	}
	
	protected void onDestroy(){
	}
	
	public GrpAddMemberDlg(Context context, int width, int height, int layout, int style) {
		super(context, style);
		//set content
		setContentView(layout);
		
		//mac_address_init();
		//set window params
		Window window = getWindow();
		WindowManager.LayoutParams params = window.getAttributes();
		//set width,height by density and gravity
		
	
		params.width = (int) width;
		params.height = (int) height;
		params.gravity = Gravity.TOP;
		//params.verticalMargin = 2.0F;
		window.setAttributes(params);
		baseAct = (Base)context;
		userE = (EditText) findViewById(R.id.group_s_user_name_e);
		distE = (EditText) findViewById(R.id.group_s_user_dist_e);
		searchBtn = (Button) findViewById(R.id.group_search_user_b);
		
		searchBtn.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String userStr = userE.getText().toString();
				String dstStr = distE.getText().toString();
				
				JSONObject obj ;
				JSONArray nameArray;
				String url;
				
				//test invite users to group
//				obj = new JSONObject();
//				nameArray = new JSONArray();
//				url = Base.HTTP_GROUP_PATH+"/inviteUsersToGroup";
//				try {	
//					obj.put("appid", "appid");
//					obj.put("groupName", "obd");						
//					JSONObject nameObj = new JSONObject();
//					nameArray.put("abc@abd.com");
//					nameArray.put("abd@abd.com");
//					
//					obj.put("users", nameArray);
//				} catch (JSONException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}					
//				baseAct.httpQueueInstance.EnQueue(baseAct.httpQueueHandler, url, obj, 19);
				
				//test list member
				obj = new JSONObject();
				nameArray = new JSONArray();
				url = Base.HTTP_GROUP_PATH+"/filterUsers";
				try {	
					obj.put("appid", "appid");
					obj.put("name", userStr);						
					obj.put("distance", dstStr);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}					
				baseAct.httpQueueInstance.EnQueue(url, obj, 18);
				GrpAddMemberDlg.this.cancel();
//				if(userStr != null && !userStr.equals("")){
//					obj = new JSONObject();
//					url = Base.HTTP_GROUP_PATH+"/filterUsers";
//					try {	
//						obj.put("appid", "appid");
//						obj.put("name", userStr);						
//						if(dstStr != null && !dstStr.equals(""))
//							obj.put("distance", dstStr);
//					} catch (JSONException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}					
//					baseAct.httpQueueInstance.EnQueue(baseAct.httpQueueHandler, url, obj, 18);
//					
//					cancel();
//				}
//				else{
//					Toast.makeText(baseAct, R.string.group_name_invalid, Toast.LENGTH_SHORT).show();
//				}
			}
			
		});
	}
	


}
