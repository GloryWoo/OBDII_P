package com.ctg.group;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.ctg.crash.LogRecord;
import com.ctg.net.HttpQueue;
import com.ctg.ui.Base;
import com.ctg.ui.R;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

public class GrpDetailDlg extends Dialog implements DialogInterface.OnCancelListener, View.OnClickListener { 
	private static final String TAG = "GrpDetailDlg";
    Button cancelButton;		
	private Base baseAct;
	View back;
	ImageView shield;
	View delRecord;
	public TextView delAndQuit;
	GridView grid_v;
	public GridViewAdapterSlim adapter;
	public TextView title_tv;
	private static int default_width = 160; //
	private static int default_height = 120;//
//	FrameLayout frame;
	Group grp;
	Member member;
	
	public GrpDetailDlg(Context context, int layout, int style) {
		this(context, default_width, default_height, layout, style);
		
	}
	
	protected void onDestroy(){
	}
	
	public GrpDetailDlg(Context context){
		super(context, R.style.Theme_dialog);
//		frame = (FrameLayout) View.inflate(context, R.layout.grp_grid_detail, null);
		setContentView(R.layout.grp_grid_detail);
		
		baseAct = (Base)context;
		Window window = getWindow();
		WindowManager.LayoutParams params = window.getAttributes();
		//set width,height by density and gravity	
		params.width = Base.mWidth;
		params.height = Base.mHeight;
		params.gravity = Gravity.TOP;
//		params.x = 0;		
//		params.y = 0;
		//params.verticalMargin = 2.0F;
		window.setAttributes(params);
		
		back = findViewById(R.id.grp_grid_back);
		shield = (ImageView) findViewById(R.id.shield_i);
		delRecord = findViewById(R.id.grp_clear_record);
		delAndQuit = (TextView) findViewById(R.id.del_quit);
		grid_v = (GridView) findViewById(R.id.grp_grid);
		title_tv = (TextView) findViewById(R.id.grp_grid_tv);

		if(Base.friendOrGrpIdx < HttpQueue.friendLst.size()){
			title_tv.setText("群组信息(1)人");
			member = HttpQueue.friendLst.get(Base.friendOrGrpIdx);
			if(member.shield)
				shield.setImageResource(R.drawable.icon_radio_enable);
			grp = null;
			title_tv.setText("好友"+member.name);			
		}
		else{
			grp = HttpQueue.grpResLst.get(Base.friendOrGrpIdx-HttpQueue.friendLst.size());
			member = null;
			title_tv.setText("群组"+grp.name+"("+grp.memberList.size() +")人");
			if(grp.shield)
				shield.setImageResource(R.drawable.icon_radio_enable);
		}
		adapter = new GridViewAdapterSlim(baseAct, false);
		grid_v.setAdapter(adapter);
		grid_v.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				
			}
			
		});
		back.setOnClickListener(this);
		shield.setOnClickListener(this);
		delRecord.setOnClickListener(this);
		delAndQuit.setOnClickListener(this);
	}
	
	public GrpDetailDlg(Context context, int width, int height, int layout, int style) {
		super(context, style);
		//set content
		setContentView(layout);
		
		//mac_address_init();
		//set window params
		Window window = getWindow();
		WindowManager.LayoutParams params = window.getAttributes();
		//set width,height by density and gravity

		if(width > 0)
			params.width = (int) (width);
		else 
			params.width = width;
		if(height > 0)
			params.height = (int) (height);
		else
			params.height = height;
		params.gravity = Gravity.TOP;		
		//params.verticalMargin = 2.0F;
		window.setAttributes(params);
		baseAct = (Base)context;
		setOnKeyListener(new OnKeyListener(){

			@Override
			public boolean onKey(DialogInterface dialog, int keyCode,
					KeyEvent event) {
				// TODO Auto-generated method stub
				switch(keyCode) {
					case KeyEvent.KEYCODE_BACK:	
						if(adapter.mWithCheck)
							adapter.quitCheck();
						else
							GrpDetailDlg.this.cancel();
						break;
				}
				return false;
			}
			
		});

	}

	@Override
	public void onCancel(DialogInterface dialog) {
		// TODO Auto-generated method stub
		 Base.me_v.grpDetailDlg = null;
	}

	@Override
	public void onClick(View v) {
		boolean shld = false;
		// TODO Auto-generated method stub
		switch(v.getId()){
		case R.id.del_quit:
			LogRecord.SaveLogInfo2File(Base.OperateInfo, TAG+"delete and quit group");
			if(adapter != null && adapter.mWithCheck){
				adapter.quitCheck();
			}
			else{
				if (Base.friendOrGrpIdx < HttpQueue.friendLst.size()) {
					member = HttpQueue.friendLst.get(Base.friendOrGrpIdx);
					JSONObject obj = new JSONObject();
					JSONArray userlist = new JSONArray();
					String url = Base.HTTP_FRIEND_PATH
							+ "/deleteFriends";
					try {
						userlist.put(member.name);
						obj.put("users", userlist);
						obj.put("appid", "appid");
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					baseAct.httpQueueInstance.EnQueue(url, obj, 53);
				} else {
					Group group = HttpQueue.grpResLst.get(Base.friendOrGrpIdx - HttpQueue.friendLst.size());
					if (group.creator.equals(Base.loginUser)) {
						JSONObject obj = new JSONObject();
						String url = Base.HTTP_GROUP_PATH + "/delete";
						try {
							obj.put("groupName", group.name);
							obj.put("appid", "appid");
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						baseAct.httpQueueInstance.EnQueue(url, obj, 12);
					} else {
						JSONObject obj = new JSONObject();
						String url = Base.HTTP_GROUP_PATH + "/quitFromGroup";
						try {
							obj.put("groupName", group.name);
							obj.put("appid", "appid");
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						baseAct.httpQueueInstance.EnQueue(url, obj, 16);
					}
				}
				GrpDetailDlg.this.cancel();
			}
			break;
		case R.id.grp_grid_back:
			if(adapter.mWithCheck)
			{
				adapter.quitCheck();
				return;
			}
			GrpDetailDlg.this.cancel();
			break;
		case R.id.shield_i:
			LogRecord.SaveLogInfo2File(Base.OperateInfo, TAG+"shield group or friend");
			if(member != null){
				member.shield = !member.shield;
				shld = member.shield;
			}
			if(grp != null){
				grp.shield = !grp.shield;
				shld = grp.shield;
			}
			if(shld)
				shield.setImageResource(R.drawable.icon_radio_enable);
			else
				shield.setImageResource(R.drawable.icon_radio_disable);
			break;
		}
	}
	


}
