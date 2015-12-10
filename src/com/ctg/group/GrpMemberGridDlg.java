package com.ctg.group;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapView;
import com.ctg.bluetooth.BluetoothSet;
import com.ctg.net.HttpQueue;
import com.ctg.ui.Base;
import com.ctg.ui.R;
import com.ctg.util.GrpAddMemberDlg;

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
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class GrpMemberGridDlg extends Dialog implements
	DialogInterface.OnCancelListener{ 
	private static final String TAG = "CustomDialog";
    Button cancelButton;		
	private Base baseAct;

	GridView grdv;
	private static int default_width = 160; //
	private static int default_height = 120;//
	public GridViewAdapter adapter; 
	public ArrayList<Member> friendlist;
	//public boolean mWithCheck;
	//public HashMap<Integer, Boolean> isSelected;
	TextView nameTextV;
	//ArrayList<Member> memberList;
	LinearLayout grpLinear;
	Button confirm;
	ImageView location;
	ImageView track;
	ImageView instant;
	ImageView shield;
	public Group group;
	public int grid_type; //0 group; 1 friend
	public MapView mMapView = null;
	public BaiduMap mBaiduMap = null;
	@Override
	public void onCancel(DialogInterface dialog) {
		// TODO Auto-generated method stub		
		Base.me_v.grpGridDlg = null;
	}
	
	public GrpMemberGridDlg(Context context, int layout, int style, int type) {
		this(context, default_width, default_height, layout, style, type);
		
	}
	
	protected void onDestroy(){
	}
	
	public GrpMemberGridDlg(Context context, int width, int height, int x, int y, int layout, int style, int type){
		super(context, style);
		setContentView(layout);
		

		Window window = getWindow();
		WindowManager.LayoutParams params = window.getAttributes();
		//set width,height by density and gravity	
		params.width = width;
		params.height = height;
		params.gravity = Gravity.TOP;
		params.x = x;		
		params.y = y;
		grid_type = type;
		//params.verticalMargin = 2.0F;
		window.setAttributes(params);
		baseAct = (Base)context;
		setOnCancelListener(this);
	}
	
	public GrpMemberGridDlg(Context context, int width, int height, int layout, int style, int type) {
		super(context, style);
		//set content
		setContentView(layout);
		
		//mac_address_init();
		//set window params
		Window window = getWindow();
		WindowManager.LayoutParams params = window.getAttributes();
		//set width,height by density and gravity
		params.width = width;
		params.height = height;
		params.gravity = Gravity.TOP;		
		//params.verticalMargin = 2.0F;
		window.setAttributes(params);
		baseAct = (Base)context;
		setOnCancelListener(this);
		setCanceledOnTouchOutside(false);
		grdv = (GridView) findViewById(R.id.group_members);
		grid_type = type;
		nameTextV = (TextView) findViewById(R.id.group_name);
		mMapView = (MapView) findViewById(R.id.bmapView);
		mMapView.onResume();
		mBaiduMap = mMapView.getMap();
		
		if(grid_type == 0)
		{
			group = HttpQueue.grpResLst.get(baseAct.me_v.editIdx);	
			adapter = new GridViewAdapter(baseAct, R.layout.group_list_member_info, group.memberList, false, grid_type);
			nameTextV.setText(group.name);			
		}
		else if (grid_type == 1){
			friendlist = new  ArrayList<Member>();
			Member friend = HttpQueue.friendLst.get(baseAct.me_v.editIdx);
			friendlist.add(friend);
			adapter = new GridViewAdapter(baseAct, R.layout.group_list_member_info, friendlist, false, grid_type);
			nameTextV.setText("我的临时群");
		}
		else if(grid_type == 2){
			ArrayList<Member> list = new  ArrayList<Member>();
			int i = 0;
			for(Member friend :HttpQueue.friendLst){
				if(Base.me_v.frndAdpt.isSelected.get(i))
					list.add(friend);
				i++;
			}
			adapter = new GridViewAdapter(baseAct, R.layout.group_list_member_info, list, false, grid_type);
			nameTextV.setText("我的临时群");
		}
		grdv.setAdapter(adapter);
		setOnCancelListener(this);
		
		
		grdv.setOnItemClickListener(new OnItemClickListener() {  
    		@Override  
    		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) { 
    			if(adapter.mWithCheck){
	    			GridViewAdapter.ViewHolder holder = (GridViewAdapter.ViewHolder) arg1.getTag();
	    			holder.check.toggle();
	    			adapter.isSelected.put(arg2, holder.check.isChecked()); 
    			}
    			else if(arg2 == adapter.gMemberList.size()-1 && adapter.addIconExist){//add member to the group
    				//baseAct.searchUserDlg = new GrpAddMemberDlg(baseAct, 200*baseAct.mDensityInt, 360*baseAct.mDensityInt, R.layout.group_search_user, R.style.Theme_dialog);
    				baseAct.searchUserDlg.show();
    				if(Base.me_v.vPager.getCurrentItem() == 0)
    					baseAct.searchUserMode = 2;
    				else
    					baseAct.searchUserMode = 3;
    			}
    		}
    	});
		grpLinear = (LinearLayout)findViewById(R.id.grp_grid_linear);
    	Button bt_selectall = (Button) grpLinear.findViewById(R.id.grp_grid_selectall);  
    	Button bt_deselectall = (Button) grpLinear.findViewById(R.id.grp_grid_deselectall);  
    	Button bt_cancel = (Button) grpLinear.findViewById(R.id.grp_grid_cancelselectall);  
    	confirm = (Button)findViewById(R.id.grp_grid_confirm);
    	bt_selectall.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// 遍历list的长度，将MyAdapter中的map值全部设为tru
				//GridView gridv = (GridView) findViewById(R.id.group_members);				
				for (int i = 0; i < group.memberList.size(); i++) {
					adapter.isSelected.put(i, true);
					View itemv = grdv.getChildAt(i);
					CheckBox check_v = (CheckBox)itemv.findViewById(R.id.group_item_select);
					check_v.setChecked(true);
				}
			}
		});

		// 反选按钮的回调接口
    	bt_deselectall.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//GridView gridv = (GridView) findViewById(R.id.group_members);				
				// 遍历list的长度，将已选的设为未选，未选的设为已选
				for (int i = 0; i < group.memberList.size(); i++) {
					View itemv = grdv.getChildAt(i);
					CheckBox check_v = (CheckBox)itemv.findViewById(R.id.group_item_select);
					if (adapter.isSelected.get(i)) {
						adapter.isSelected.put(i, false);
						check_v.setChecked(false);
						// checkNum--;
					} else {
						adapter.isSelected.put(i, true);
						check_v.setChecked(true);
						// checkNum++;
					}
				}
//				adapter_local.notifyDataSetChanged();
				// 刷新listview和TextView的显示
				// dataChanged();
			}
		});

		// 取消按钮的回调接口
    	bt_cancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//GridView gridv = (GridView) findViewById(R.id.group_members);		
				// 遍历list的长度，将已选的按钮设为未选
				for (int i = 0; i < group.memberList.size(); i++) {					
					if (adapter.isSelected.get(i)) {
						View itemv = grdv.getChildAt(i);
						CheckBox check_v = (CheckBox)itemv.findViewById(R.id.group_item_select);
						adapter.isSelected.put(i, false);
						check_v.setChecked(false);
						// checkNum--;// 数量减1
					}
				}
				//adapter.notifyDataSetChanged();
				// 刷新listview和TextView的显示
				// dataChanged();
			}
		});
		
		confirm.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				exitSelectMode();
			}
		});
		location = (ImageView) findViewById(R.id.location_i);
		track = (ImageView) findViewById(R.id.track_i);
		instant = (ImageView) findViewById(R.id.instant_i);
		shield = (ImageView) findViewById(R.id.shield_i);
		location.setOnClickListener(imagev_onClick);
		track.setOnClickListener(imagev_onClick);
		instant.setOnClickListener(imagev_onClick);
		shield.setOnClickListener(imagev_onClick);
		grpLinear.setVisibility(View.GONE);
		confirm.setVisibility(View.GONE);
	}

	View.OnClickListener imagev_onClick = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			switch(v.getId()){
			case R.id.location_i:
				break;
			case R.id.track_i:
				break;				
			case R.id.instant_i: 	
				break;
			case R.id.shield_i:
				enterSelectMode();
				break;
			}
		}
	};
	
	public void enterSelectMode(){
		adapter.mWithCheck = true;		
		grpLinear.setVisibility(View.VISIBLE);
		confirm.setVisibility(View.VISIBLE);
		
		for (int i = 0; i < adapter.gMemberList.size(); i++) {									 
			View itemv = grdv.getChildAt(i);
			CheckBox check_v = (CheckBox)itemv.findViewById(R.id.group_item_select);
			check_v.setVisibility(View.VISIBLE);
			if (adapter.isSelected.get(i))
				check_v.setChecked(true);
			else
				check_v.setChecked(false);
				// checkNum--;// 数量减1				
		}					
	}
	
	public void exitSelectMode(){		
		adapter.mWithCheck = false;		
		grpLinear.setVisibility(View.GONE);
		confirm.setVisibility(View.GONE);				
		int i = 0;
		for (Member member : adapter.gMemberList) {
			View itemv = grdv.getChildAt(i);
			CheckBox check_v = (CheckBox)itemv.findViewById(R.id.group_item_select);
			check_v.setVisibility(View.INVISIBLE);
			if(adapter.isSelected.get(i))
				member.shield = true;
			else
				member.shield = false; 
			i++;
        } 
	}


}
