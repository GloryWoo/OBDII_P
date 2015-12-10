package com.ctg.util;


import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

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
import android.content.DialogInterface;

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

public class GrpSearchDlg extends Dialog implements DialogInterface.OnCancelListener, View.OnClickListener{ 
	private static final String TAG = "CreateGrpDlg";
//    Button cancelButton;		
	private Base baseAct;
	EditText grpName;
	String searchGrp;
	String searchUser;
	public int type;
//	EditText userName;
	View grpTab;
	View userTab;
	View grpLine;
	View userLine;
//	EditText member;
//	EditText distance;
	boolean grpGone;
	Button searchBtn; 
	private static int default_width = 160; //
	private static int default_height = 120;//

	@Override
	public void onCancel(DialogInterface dialog) {
		// TODO Auto-generated method stub
		Base.me_v.searchGroupDlg = null;
	}
	
	
	public GrpSearchDlg(Context context, int layout, int style) {
		this(context, default_width, default_height, layout, style);
		
	}
	
	protected void onDestroy(){
	}
	
	public GrpSearchDlg(Context context, int width, int height, int layout, int style, boolean gone){		
		this(context, width, height, layout, style);
		grpGone = gone;
		if(grpGone){
			grpTab.setVisibility(View.GONE);
			userLine.setBackgroundColor(0x80e3e3e3);
		}
		else{		
		}
	}
	
	public GrpSearchDlg(Context context, int width, int height, int layout, int style) {
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
		params.gravity = Gravity.CENTER;
		//params.verticalMargin = 2.0F;
		window.setAttributes(params);
		baseAct = (Base)context;
		grpName = (EditText) findViewById(R.id.grp_s_user_e);

		
		userTab = findViewById(R.id.grp_s_user);
		grpTab = findViewById(R.id.grp_s_grp);			
		userTab.setOnClickListener(this);
		grpTab.setOnClickListener(this);
		
		searchBtn = (Button) findViewById(R.id.group_search_user_b);
		searchBtn.setOnClickListener(this);

		
		userLine = findViewById(R.id.grp_s_user_line);
		grpLine = findViewById(R.id.grp_s_grp_line);		
		
		userLine.setBackgroundColor(0xff0087cb);
		grpLine.setBackgroundColor(0x80e3e3e3);	
	}


	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()){
		case R.id.group_search_user_b:
//			String name = grpName.getText().toString();
//			String distStr = distance.getText().toString();
//			
			String url = Base.HTTP_GROUP_PATH+"/findGroups?appID=appid";
//			if(creat.getText().toString() != null && !creat.getText().toString().equals(""))
//				url += "&creareUserName="+creat.getText().toString();
//			if(member.getText().toString() != null && !member.getText().toString().equals(""))
//				url += "&memberName="+member.getText().toString();
//			if(grpName.getText().toString() != null && !grpName.getText().toString().equals(""))
//				url += "&groupName="+grpName.getText().toString();
//			if(grpName.getText().toString() != null && !grpName.getText().toString().equals("")){
			String grpNameStr = grpName.getText().toString();
			if(grpNameStr == null || grpNameStr.equals("")){
				Toast.makeText(Base.OBDApp, "输入不得为空", Toast.LENGTH_SHORT).show();
				return;
			}
			if(type == 0){
				url = Base.HTTP_GROUP_PATH+"/filterUsers";
				JSONObject obj = new JSONObject();
				try {	
					obj.put("appid", "appid");
					obj.put("name", grpName.getText().toString());						
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}					
				if(grpGone)
					baseAct.searchUserMode = 0;
				else
					baseAct.searchUserMode = 1;
				baseAct.httpQueueInstance.EnQueue(url, obj, 18);
			}
			else{
				String grpStrUTF8 = "";
				try {
					grpStrUTF8 = URLEncoder.encode(grpNameStr,"UTF-8");
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				url += "&groupName="+grpStrUTF8+"&createUserName="+grpStrUTF8;
				baseAct.httpQueueInstance.EnQueue(url, null, 14);
				baseAct.searchUserMode = 0;
			}
			GrpSearchDlg.this.cancel();	
			Base.me_v.waitProgress.setVisibility(View.VISIBLE);
			
//			Toast.makeText(baseAct, "输入为空，无法查找", Toast.LENGTH_SHORT).show();
			break;
		case R.id.grp_s_user:
			userLine.setBackgroundColor(0xff0087cb);
			grpLine.setBackgroundColor(0x80e3e3e3);
			type = 0;
			grpName.setHint("好友名称/昵称");
			
			break;
		case R.id.grp_s_grp:
			userLine.setBackgroundColor(0x80e3e3e3);
			grpLine.setBackgroundColor(0xff0087cb);
			grpName.setHint("群名称/创建者/群成员名");
			type = 1;
			break;
		}
	}
}
