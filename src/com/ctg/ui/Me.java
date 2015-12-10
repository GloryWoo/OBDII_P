package com.ctg.ui;

import android.content.Context;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.baidu.mapapi.model.LatLng;
import com.ctg.crash.LogRecord;
import com.ctg.group.ChatMsgEntity;
import com.ctg.group.FriendLstAdapter;
import com.ctg.group.GridViewAdapter;
import com.ctg.group.Group;
import com.ctg.group.GrpDetailDlg;
//import com.ctg.group.GroupListViewAdapter;
import com.ctg.group.GrpListAdapter;
import com.ctg.group.GrpMemberGridDlg;
import com.ctg.group.Member;
import com.ctg.land.FrndGrpAdapt;
import com.ctg.net.HttpQueue;
import com.ctg.ui.R;
import com.ctg.util.CustomDialog;
import com.ctg.util.GrpCreateDlg;
import com.ctg.util.GrpSearchDlg;
import com.ctg.util.MyPagerAdapter;
import com.ctg.util.MyViewPager;
import com.ctg.util.Preference;
import com.ctg.util.Util;
import com.example.combinebitmap.LogUtil;
import com.example.combinebitmap.PropertiesUtil;
import com.example.swipelistview.SwipeAdapter;
import com.example.swipelistview.SwipeListView;




import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;
import android.content.Context;
import android.graphics.Bitmap;

public class Me extends LinearLayout implements View.OnClickListener{
	public Base baseAct;
	
	/// Declare variables
	ProgressDialog dlg;
	public ArrayList<Member> onlineList;

//	public GroupListViewAdapter vGroupList;
	final private String TAG = "Me ";

	public LinearLayout rela_v;	
	public View line_v;
	public MyViewPager vPager;
	public ListView group_v;
	public ListView friend_v;
	public GrpListAdapter grpLstAdpt;
	public FriendLstAdapter frndAdpt;
	public LinearLayout selLinear;
	public LinearLayout operLinear;
	View grp_add;
	View grp_search;
	Button selectAll;
	Button deSelectAll;
	Button cancelAll;
	Button delete;
	Button createTempGroup;
	View grpBack;
	public SwipeListView grp_frnd_lv;
	public SwipeAdapter listAdapt;
	
	String userName;
	String obdii_path;	
	TextView grp_title;
	TextView frnd_title;
	LinearLayout group_linear;
	LinearLayout friend_linear;
	
	public boolean editMode = false;
	public int editIdx;
	public GrpMemberGridDlg grpGridDlg;
	public GrpMemberGridDlg frndGridDlg;
	
	public Dialog grpShareDlg;
	public GrpDetailDlg grpDetailDlg;
	
//	public ArrayList<ArrayList<ChatMsgEntity>> chatMsgLst;
	Dialog enterShareModeDlg;
	
	public Dialog createGroupDlg;//add and modify name use one handle
	public Dialog delGroupDlg;
	public Dialog selGroupDlg;
	public Dialog selMemDlg;
	public Dialog selGroupJoinDlg;
	public Dialog renameGroupDlg;
	public Dialog searchGroupDlg;
	public Dialog searchUserDlg;
	public Dialog grpListMemberDlg;
	public ProgressBar waitProgress;
	
	public Me(Context context){
		super(context);
		baseAct = (Base) context;
		

		obdii_path = Base.getSDPath() +"/OBDII";
		userName = Preference.getInstance(context).getUser();

		line_v = View.inflate(context, R.layout.grp_main, this);
		grpBack = line_v.findViewById(R.id.grp_back);
		grpBack.setOnClickListener(this);
		grp_frnd_lv = (SwipeListView) line_v.findViewById(R.id.grp_frnd_lv);
		waitProgress = (ProgressBar) line_v.findViewById(R.id.me_progressbar);
//		grp_frnd_lv.setDivider(null);
		
		grp_add = line_v.findViewById(R.id.grp_add);
		grp_search = line_v.findViewById(R.id.grp_search);
		grp_add.setOnClickListener(this);
		grp_search.setOnClickListener(this);
		
		if(HttpQueue.friendLst != null || HttpQueue.grpResLst != null){
//			if(Base.OBDApp.landScapeMode == 1)
//				listAdapt = new FrndGrpAdapt(context, HttpQueue.friendLst, HttpQueue.grpResLst);
//			else
				listAdapt = new SwipeAdapter(context, HttpQueue.friendLst, HttpQueue.grpResLst);
//			listAdapt = new SwipeAdapter(baseAct);
			grp_frnd_lv.setAdapter(listAdapt);
		}
		grp_frnd_lv.setDivider(baseAct.gray_line_draw);
//		chatMsgLst = new ArrayList<ArrayList<ChatMsgEntity>>();
		int len = HttpQueue.friendLst.size() + HttpQueue.grpResLst.size();
//		for(int i = 0; i < len; i++){
//			ArrayList<ChatMsgEntity> oneList = new ArrayList<ChatMsgEntity>();
//			chatMsgLst.add(oneList);
//		}
		grp_frnd_lv.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub	
				Group grp = null;
				Base.friendOrGrpIdx = arg2;
				if(Base.friendOrGrpIdx < HttpQueue.friendLst.size())
					LogRecord.SaveLogInfo2File(Base.OperateInfo, TAG+"friend item click");
				else
					LogRecord.SaveLogInfo2File(Base.OperateInfo, TAG+"group item click");
				if((grp = Base.me_v.getCurrentGrp()) != null){
					if(grp.memberList.size() == 1){
						new AlertDialog.Builder(baseAct)
					 	.setTitle(R.string.string_confirm1)
					 	.setMessage("群组没有其他成员，无法分享位置，是否添加群成员？")
					 	.setPositiveButton("是", 
				 			new DialogInterface.OnClickListener(){
								@Override
								public void onClick(DialogInterface dialog, int which) {
//									Base.me_v.grpDetailDlg = new GrpDetailDlg(baseAct);
//									Base.me_v.grpDetailDlg.show();
									Base.me_v.searchGroupDlg = new GrpSearchDlg(baseAct, 300*Base.mDensityInt, 320*Base.mDensityInt, R.layout.grp_user_search, R.style.Theme_dialog, true);
									Base.me_v.searchGroupDlg.show();
									dialog.cancel();
								}
						 	})
					 	.setNegativeButton("否", 
				 			new DialogInterface.OnClickListener(){
								@Override
								public void onClick(DialogInterface dialog, int which){
									dialog.cancel();
								}
							})								
					 	.show();
								
						//enterShareModeDlg.cancel();
						return;
					}
				}
				Base.baidu_v.enterShareModeCheck();		
				TextView numv = (TextView) arg1.findViewById(R.id.msg_cnt);
				TextView lastTimeV = (TextView) arg1.findViewById(R.id.grp_item_last_time);
				if(numv != null)
					numv.setVisibility(View.INVISIBLE);
				if(lastTimeV != null)
					lastTimeV.setVisibility(View.INVISIBLE);
			}
			
		});				
	}
	
	public boolean onBack(){
		boolean ret = false;
		
		ret = grp_frnd_lv.closeIfMenuOpen();
		if(ret){
			return true;
		}
		return ret;
	}
	
	public void setFrndGrpList(){
		if(listAdapt == null){
			listAdapt = new SwipeAdapter(baseAct, HttpQueue.friendLst, HttpQueue.grpResLst);
			grp_frnd_lv.setAdapter(listAdapt);
		}
		listAdapt.setList(HttpQueue.friendLst, HttpQueue.grpResLst);

//		chatMsgLst = new ArrayList<ArrayList<ChatMsgEntity>>();
//		int len = HttpQueue.friendLst.size() + HttpQueue.grpResLst.size();
//		for(int i = 0; i < len; i++){
//			ArrayList<ChatMsgEntity> oneList = new ArrayList<ChatMsgEntity>();
//			chatMsgLst.add(oneList);
//		}
	}
	
	public Member getCurrentFrnd(){
        if(Base.friendOrGrpIdx != -1 && Base.friendOrGrpIdx < HttpQueue.friendLst.size()){
        	return HttpQueue.friendLst.get(Base.friendOrGrpIdx);        	
        }
        return null;
	}
	
	public Group getCurrentGrp(){
		Group grp = null;
		if(Base.friendOrGrpIdx == -1)
			return null;
        if(Base.friendOrGrpIdx < HttpQueue.friendLst.size()){
        	return null;
        }
        else if(Base.friendOrGrpIdx - HttpQueue.friendLst.size() < HttpQueue.grpResLst.size()){
        	grp = HttpQueue.grpResLst.get(Base.friendOrGrpIdx - HttpQueue.friendLst.size());
        	return grp;
        } 
        return null;
	}
	
	public boolean processChatMsg(ChatMsgEntity msgEnti){
		int idx = 0, realIdx = 0;
		Member member = null;
		int shareMemberCount = 1;
		if(msgEnti.groupName == null || msgEnti.groupName.equals("")){
			realIdx = HttpQueue.friendLst.indexOf(new Member(msgEnti.name));
			if(realIdx == -1)
				return false;
			if(realIdx == Base.friendOrGrpIdx)//在当前窗口
			{
				
			}
			member = HttpQueue.friendLst.get(realIdx);

		}
		else{
			idx = Group.indexOfByName(HttpQueue.grpResLst, msgEnti.groupName);
			if(idx == -1)
				return false;
			realIdx = idx+HttpQueue.friendLst.size();
			Group grp = HttpQueue.grpResLst.get(idx);
			int idx1 = grp.memberList.indexOf(new Member(msgEnti.name));
			if(idx1 == -1)
				return false;
			member = grp.memberList.get(idx1);	
			for(Member mem : grp.memberList){//there are others in share pos mode
				if(mem.isInSharePosMode && !mem.equals(member))
					shareMemberCount++;
			}
			
		}

		member.posTime = System.currentTimeMillis();
		member.latlon = msgEnti.latlon_loc;
		member.latlon = new LatLng(member.latlon.latitude, member.latlon.longitude);
		
		member.chatMsg.latlon_track.add(msgEnti.latlon_loc);
		if(Base.baidu_v.isGrpShareMode && realIdx == Base.friendOrGrpIdx){
//			Base.baidu_v.mBaiduMap.clear();
//			Base.baidu_v.addLocationMark(msgEnti, member);						
		}
		else{			
			ViewGroup rela = (ViewGroup) grp_frnd_lv.getChildAt(realIdx);
			if(rela != null){
				TextView numv = (TextView) rela.findViewById(R.id.msg_cnt);
				TextView lastTimeV = (TextView) rela.findViewById(R.id.grp_item_last_time);
				numv.setVisibility(View.VISIBLE);
				numv.setText(""+shareMemberCount);
				lastTimeV.setVisibility(View.VISIBLE);
				lastTimeV.setText("好友"+member.getName()+"正在分享位置");
			}
			
		}
		return true;
	}
	
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()){
			case R.id.grp_add:
				LogRecord.SaveLogInfo2File(Base.OperateInfo, TAG+"create group");
				createGroupDlg = new GrpCreateDlg(baseAct, 320*Base.mDensityInt, 320*Base.mDensityInt, R.layout.group_create, R.style.Theme_dialog);
				createGroupDlg.show();
				break;
			case R.id.grp_search:
				LogRecord.SaveLogInfo2File(Base.OperateInfo, TAG+"search friend or group");
				searchGroupDlg = new GrpSearchDlg(baseAct, 320*Base.mDensityInt, 320*Base.mDensityInt, R.layout.grp_user_search, R.style.Theme_dialog);
				searchGroupDlg.show();				
				break;
			case R.id.grp_bt_selectall:
				for(int i = 0; i < frndAdpt.getCount(); i++){
					frndAdpt.isSelected.put(i, true);							
				}	
				break;
			case R.id.grp_bt_deselectall:
				for(int i = 0; i < frndAdpt.getCount(); i++){
					frndAdpt.isSelected.put(i, !frndAdpt.isSelected.get(i));							
				}
				break;
			case R.id.grp_bt_cancelall:
				for(int i = 0; i < frndAdpt.getCount(); i++){
					frndAdpt.isSelected.put(i, false);							
				}
				break;
			case R.id.grp_bt_delete:
				int len = getHashSelectSize(frndAdpt.isSelected);
				int i = 0;
				if(len == 0){
					Toast.makeText(baseAct, "没有选择，无法删除", Toast.LENGTH_SHORT).show();		
					break;
				}
				String usr = Preference.getInstance(baseAct).getUser();
				String url = Base.HTTP_FRIEND_PATH+"/deleteFriends";	
				JSONObject object = new JSONObject();
				JSONArray userlist = new JSONArray();
				for(Member mem : HttpQueue.friendLst){
					if(frndAdpt.isSelected.get(i)){
						userlist.put(mem.name);
					}
					i++;
				}
				try {
					object.put("users", userlist);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Base.OBDApp.baseAct.httpQueueInstance.EnQueue(url, object, 53);
				frndAdpt.ExitCheckMode();
				break;
			case R.id.grp_bt_create_tmp_grp:	
				if(getHashSelectSize(frndAdpt.isSelected) == 0){
					Toast.makeText(baseAct, "没有选择，无法创建临时群组", Toast.LENGTH_SHORT).show();	
					break;
				}
				frndGridDlg = new GrpMemberGridDlg(baseAct, baseAct.mWidth, baseAct.realHeight, R.layout.grp_grid_list,  R.style.Theme_dialog, 2);
				frndGridDlg.show();
				frndAdpt.ExitCheckMode();
				break;	
			case R.id.grp_back:
				baseAct.setVpagerItem0();
				break;
			default:break;
		}
		//frndAdpt.notifyDataSetChanged();
	}
	
	public static int getHashSelectSize(HashMap<Integer, Boolean> hash){
		int len = hash.size();
		int sel = 0;
		for(int i = 0; i < len; i++){
			if(hash.get(i))
				sel++;
		}
		return sel;
	}
	
	public void addFriend(String user, String alias, int online){
		Member member = new Member(user, alias, online);
		HttpQueue.friendLst.add(member);	
		setFrndGrpList();
	}
	
	public boolean delGroupMember(JSONObject json) throws JSONException{
		String GrpNm = json.getString("groupName");					
		String memNm = null;
		JSONArray array = (JSONArray) json.get("fromUser");
		Group group = null;
		for(int i = 0; i < array.length(); i++){
			Object obj = array.get(i);		
			if(obj instanceof JSONObject){
				memNm = ((JSONObject)obj).getString("user");
			}
			else if(obj instanceof String){
				memNm = (String)obj;
			}
			int grpIdx = Group.indexOfByName(HttpQueue.grpResLst, GrpNm);
			if(grpIdx == -1)
				return false;
			
			group = HttpQueue.grpResLst.get(grpIdx);
			int memIdx = Member.indexOfByName(group.memberList, memNm);
			
			if(memIdx == -1)
				return false;
			Member member = group.memberList.get(memIdx);
			member.destroy();
			group.memberList.remove(memIdx);			
		}
		if(group != null){
			if(group.grpHead != null)
				group.grpHead.recycle();
			group.grpHead = null;			
			listAdapt.setList(HttpQueue.friendLst, HttpQueue.grpResLst);
			if(getCurrentGrp() != null && group.equals(getCurrentGrp())){
				if(Base.baidu_v.honAdapter != null)
					Base.baidu_v.honAdapter.refreshGrpList();
				if(grpDetailDlg != null){
					grpDetailDlg.adapter.refreshGrpList();
				}
			}
		}
		return true;
	}
	
	public boolean addGroupMember(JSONObject json) throws JSONException{
		String GrpNm = json.getString("groupName");
		String memNm = json.getString("fromUser");
		String imgNm = null;
		if(json.has("fromUserImage")){
			imgNm = json.getString("fromUserImage");
		}
		
		
		Member member = new Member(memNm, 1, false, imgNm);
		Group group = null;
//		int idx = 0;
//		GridViewAdapter adapter;
		for(Group grp : HttpQueue.grpResLst){
			if(grp.name.equals(GrpNm)){
				group = grp;				
				break;
			}
//			idx++;
		}
		if(group != null){
			if(group.memberList.contains(member)){				
				return false;
			}
			group.memberList.add(member);

			if(group.grpHead != null)
				group.grpHead.recycle();
			group.grpHead = null;			
			listAdapt.setList(HttpQueue.friendLst, HttpQueue.grpResLst);
			if(getCurrentGrp() != null && group.equals(getCurrentGrp())){
				if(Base.baidu_v.honAdapter != null)
					Base.baidu_v.honAdapter.refreshGrpList();
				if(grpDetailDlg != null){
					grpDetailDlg.adapter.refreshGrpList();
				}
			}
	    	return true;
		}
		else{
			
		}
		return false;
	}
	
	public void setEditMode(boolean mode){
		editMode = mode;
		if(editMode){
			vPager.setScrollable(false);
		}
		else{
			vPager.setScrollable(true);
		}
	}
	
	//groupname, creatorname, member1, member2...;	

	
	private void updateGridView(Member member, ArrayList<Integer> groupIDList)
	{	
//	    Iterator<Integer> itr = groupIDList.iterator();
//	    while (itr.hasNext()) {
//	    	Integer element = itr.next();
//			if(vGroupList.contains(element))
//			{
//				int position = vGroupList.indexOf(element);
//				vGroupList.updateAdapter(position, member);
//			}
//		}
		
	}

	
	public void addGroup(Group group){
//		vGroupList.add(group.groupID, group.creator, group.name, group.memberList);
//		vGroupList.pagerAdapter.notifyDataSetChanged();
	}
	
	public void initViewPager()
	{
		//mGestureDetector_act = new GestureDetectorCompat(this, new GroupListGestureDetector(activity));

//	    vGroupList = new GroupListViewAdapter(this);
//
//	    for(Group element : HttpQueue.grpResLst){
//	    	vGroupList.add(element.GetGroupID(), element.creator, element.name, element.GetMemberList());
//	    }
//	    
//	    vGroupList.setAdapter();
//
//	    vGroupList.viewPager.setOnTouchListener(new View.OnTouchListener() {
//			@Override
//			public boolean onTouch(View v, MotionEvent event) {
//				//mGestureDetector_act.onTouchEvent(event);
//			    switch (event.getAction()) {
//			    case MotionEvent.ACTION_MOVE: 
//			    	v.getParent().requestDisallowInterceptTouchEvent(true);
//			    	break;
//			    case MotionEvent.ACTION_UP:
//			    case MotionEvent.ACTION_DOWN:
//			    case MotionEvent.ACTION_CANCEL:
//			    	v.getParent().requestDisallowInterceptTouchEvent(false);
//			        break;
//			    }
//				return false;
//			}
//		});
	}

	
    @Override  
    public void onConfigurationChanged(Configuration newConfig) {  
        // TODO Auto-generated method stub   
        super.onConfigurationChanged(newConfig);  
        /* 
         * 横竖屏
         */  
        if (this.getResources().getConfiguration().orientation  
        		== Configuration.ORIENTATION_LANDSCAPE) {
            // 当前为横   实现代码    	
        }  
  
        else if (this.getResources().getConfiguration().orientation  
        		== Configuration.ORIENTATION_PORTRAIT) {  
            // 当前为  实现代码 
        }  
  
        /* 
         *  实体键盘
         */  
  
        if (newConfig.hardKeyboardHidden  
        		== Configuration.HARDKEYBOARDHIDDEN_NO) {  
            // 实体键盘处于退出状  实现代码  
        }  
  
        else if (newConfig.hardKeyboardHidden  
        		== Configuration.HARDKEYBOARDHIDDEN_YES) {  
            // 实体键盘处于合上状  实现代码  
        }  
    }  


	@Override
	public boolean onTouchEvent(MotionEvent event){
		//this.mGestureDetector_act.onTouchEvent(event);
		return super.onTouchEvent(event);
	}
	
	
	// TODO add setting to change the user information value.
 
    private static Boolean isExit = false;

    



}





