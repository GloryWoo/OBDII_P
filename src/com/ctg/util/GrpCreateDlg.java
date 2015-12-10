package com.ctg.util;


import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.ctg.bluetooth.BluetoothSet;
import com.ctg.ui.Base;
import com.ctg.ui.OBDApplication;
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

public class GrpCreateDlg extends Dialog { 
	private static final String TAG = "CreateGrpDlg";
    Button createButton;	
    TextView title;
    TextView grpNameT;
    EditText groupEdit;
    public String groupNameStr;
	private Base baseAct;
	public int mOption; //0 create ; 1 modify name
	String mOldGrpName;
	private static int default_width = 160; //
	private static int default_height = 120;//

	public GrpCreateDlg(Context context, int layout, int style) {
		this(context, default_width, default_height, layout, style);
		
	}
	
	protected void onDestroy(){
	}
	
	public GrpCreateDlg(Context context, int width, int height, int layout, int style, int option, String oldGrpName){
		this(context, width, height, layout, style);
		mOption = option;
		title.setText("修改群组名称");
		createButton.setText("确定");
		grpNameT.setText("新群组名称");
		mOldGrpName = oldGrpName;
	}
	
	public GrpCreateDlg(Context context, int width, int height, int layout, int style) {
		super(context, style);
		//set content
		setContentView(layout);
		
		//mac_address_init();
		//set window params
		title = (TextView) findViewById(R.id.group_title);
		groupEdit = (EditText) findViewById(R.id.group_name_e);
		createButton = (Button) findViewById(R.id.group_create_b);
		grpNameT = (TextView) findViewById(R.id.group_name_t);
		Window window = getWindow();
		WindowManager.LayoutParams params = window.getAttributes();
		//set width,height by density and gravity
		
		params.width = (int) width;
		params.height = (int) height;
		params.gravity = Gravity.CENTER;
		//params.verticalMargin = 2.0F;
		window.setAttributes(params);
		baseAct = (Base)context;
		
		createButton.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				groupNameStr = groupEdit.getText().toString();
				if(groupNameStr != null && !groupNameStr.equals("")){
					if(mOption == 0){
						JSONObject obj = new JSONObject();
						String url = Base.HTTP_GROUP_PATH+"/add";
						try {												
							obj.put("groupName", groupNameStr);						
							obj.put("appid", "appid");
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}			
						baseAct.httpQueueInstance.EnQueue(url, obj, 11);
					}
					else{
						JSONObject obj = new JSONObject();
						String url = Base.HTTP_GROUP_PATH+"/update";
						try {												
							obj.put("newGroupName", groupNameStr);	
							obj.put("oldGroupName", mOldGrpName);
							obj.put("appid", "appid");
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}			
						baseAct.httpQueueInstance.EnQueue(url, obj, 13);
					}
					cancel();
				}
				else{
					Toast.makeText(baseAct, R.string.group_name_invalid, Toast.LENGTH_SHORT).show();
				}
			}
			
		});
	}
	


}
