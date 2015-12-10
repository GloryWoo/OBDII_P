package com.ctg.land;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.baidu.mapapi.map.MapView;
import com.ctg.group.Group;
import com.ctg.group.Member;
import com.ctg.net.CacheManager;
import com.ctg.net.HttpQueue;
import com.ctg.net.IHttpCallback;
import com.ctg.shareUserInfo.UserPos;
import com.ctg.shareUserInfo.UserTrace;
import com.ctg.ui.BaiduMapView;
import com.ctg.ui.Base;
import com.ctg.ui.R;
import com.ctg.util.Preference;

import android.content.Context;
import android.support.v4.widget.SimpleCursorAdapter.ViewBinder;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

public class GroupMap implements OnGroupClickListener, OnChildClickListener, OnClickListener{
	public LinearLayout group_mapv;
	//public BaiduMapView baidu_v;
	public MapView baidumapv;
	Base baseAct;
	public FrameLayout grp_land_frm;
	ExpandableListView grp_land_lv;
	GrpMapExpAdapter grp_land_adp;
	
	ImageView locate_imgv;
	ImageView track_imgv;
	ImageView instant_imgv;
	ImageView clear_imgv;
	public ListView examp_lv;
	ArrayList<Member> exam_lst;
	GrpMapExamAdapter grp_exam_adp;
	Group curGrp;
	ArrayList<JSONObject> posList;
	
	//String -- fromUser; Integer -- 0 loc 1 instant loc 2 track 
	static HashMap<String, HashMap<Integer, ArrayList<UserPos>>> historicLoc;
	static HashMap<String, Integer> historicLocColor;
	int colorSz[] = {R.color.blue, R.color.yellow, R.color.red, R.color.purple, R.color.orange, 
			         R.color.green, R.color.darkgrey, R.color.chocolate, R.color.white, R.color.violet};
	static int curColorIdx = 0;
	
	public GroupMap(Context context) {
		
		baseAct = (Base) context;
		group_mapv = (LinearLayout) View.inflate(context, R.layout.group_mapv, null);
		locate_imgv = (ImageView) group_mapv.findViewById(R.id.grp_mapv_loc);
		track_imgv = (ImageView) group_mapv.findViewById(R.id.grp_mapv_track);
		instant_imgv = (ImageView) group_mapv.findViewById(R.id.grp_mapv_instant);
		clear_imgv = (ImageView) group_mapv.findViewById(R.id.grp_mapv_clear);
		examp_lv = (ListView) group_mapv.findViewById(R.id.grp_example);
		if(historicLoc == null)
			historicLoc = new HashMap<String, HashMap<Integer, ArrayList<UserPos>>>();
		if(historicLocColor == null)
			historicLocColor = new HashMap<String, Integer>();
		
		exam_lst = new ArrayList<Member>();
		grp_exam_adp = new GrpMapExamAdapter(historicLocColor, context);
		examp_lv.setAdapter(grp_exam_adp);
		locate_imgv.setOnClickListener(this);
		track_imgv.setOnClickListener(this);
		instant_imgv.setOnClickListener(this);
		clear_imgv.setOnClickListener(this);
		// TODO Auto-generated constructor stub
		grp_land_frm = (FrameLayout) group_mapv.findViewById(R.id.grp_land_frm);
		//Base.baidu_v = new BaiduMapView(context);		
		FrameLayout.LayoutParams layout = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		grp_land_frm.addView(Base.baidu_v, 0, layout);
		grp_land_lv = (ExpandableListView)group_mapv.findViewById(R.id.grp_lv);
		grp_land_adp = new GrpMapExpAdapter(context, grp_land_lv, HttpQueue.grpResLst);
		grp_land_lv.setAdapter(grp_land_adp);
		grp_land_lv.setGroupIndicator(null);
		grp_land_lv.setOnGroupClickListener(this);
		grp_land_lv.setOnChildClickListener(this);
		
		grp_land_frm.post(new Runnable(){
			@Override
			public void run() {
				// TODO Auto-generated method stub
				Base.baidu_v.mMapView.onResume();
			}
			
		});
				
		
	}

	public void drawPosOrTrace(JSONObject json){
//		posList.add(json);
		if(examp_lv.getVisibility() != View.VISIBLE)
			examp_lv.setVisibility(View.VISIBLE);
		UserPos pos = new UserPos(json);
		HashMap<Integer, ArrayList<UserPos>> mapItem;
		ArrayList<UserPos> arraylist0, arraylist1, arraylist2;
		
		mapItem = historicLoc.get(pos.name);
		if(mapItem == null){
			mapItem = new HashMap<Integer, ArrayList<UserPos>>();
			arraylist0 = new ArrayList<UserPos>();
			arraylist1 = new ArrayList<UserPos>();
			arraylist2 = new ArrayList<UserPos>();
			mapItem.put(0, arraylist0);
			mapItem.put(1, arraylist1);
			mapItem.put(2, arraylist2);
			historicLoc.put(pos.name, mapItem);
			historicLocColor.put(pos.name, colorSz[curColorIdx++]);
			if(curColorIdx == 10)
				curColorIdx = 0;
			grp_exam_adp.notifyDataSetChanged();
		}
		else{
			arraylist0 = mapItem.get(0);
			arraylist1 = mapItem.get(1);
			arraylist2 = mapItem.get(2);
		}
		int curColor = historicLocColor.get(pos.name);
		if(pos.action.equals("gps_share")){
			if(pos.count == 1){
				arraylist0.add(pos);
				Base.baidu_v.drawSharePosColor(pos, curColor);
			}
			else{
				arraylist1.add(pos);
				Base.baidu_v.drawSharePosColor(pos, curColor);//
			}
		}
		else if(pos.action.equals("track_share")){
			arraylist2.add(pos);
			UserTrace userTrace = new UserTrace(json);
			Base.baidu_v.drawShareTraceColor(userTrace, curColor);
		}
		
		
	}
	
	@Override
	public boolean onGroupClick(ExpandableListView parent, View v,
			int groupPosition, long id) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onChildClick(ExpandableListView parent, View v,
			int groupPosition, int childPosition, long id) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		String url = Base.HTTP_ROOT_PATH;
		Map<String, String> postData = new HashMap<String, String>();
		ArrayList<String> usersLst = new ArrayList<String>();
		
		for(Group grpItm : HttpQueue.grpResLst){
			if(grpItm.checked){
				curGrp = grpItm;
				
				for(Member mem : curGrp.memberList){
					if(mem.checked){
						if(!mem.name.equals(Base.loginUser))
							usersLst.add(mem.name);
					}
				}
				break;
			}
		}		
		if((curGrp == null || usersLst.size() == 0) && v.getId() != R.id.grp_mapv_clear){
			Toast.makeText(baseAct, R.string.no_member_for_track, Toast.LENGTH_SHORT).show();
			return;
		}
		switch(v.getId()){
			case R.id.grp_mapv_loc:				
				url += "/services/openSendGpsPush";
				postData.put("isOpen", Boolean.toString(true));
				postData.put("appID", "appid");
				postData.put("groupName", curGrp.name);
				postData.put("count", Integer.toString(1));		
				postData.put("users", usersLst.toString());	
				CacheManager.getJson(baseAct, url, new IHttpCallback() {					
					@Override
					public void handle(int retCode, Object response) {
						// TODO Auto-generated method stub
						Log.d("GrpMap", "retCode:" + retCode);
					}
				}, postData);
				break;
			case R.id.grp_mapv_track:
				url += "/services/shareGpsTrace";
				postData.put("isOpen", Boolean.toString(true));
				postData.put("traceId", Integer.toString(1));
				postData.put("groupName", curGrp.name);
				postData.put("users", usersLst.toString());	 		
				CacheManager.getJson(baseAct, url, new IHttpCallback() {					
					@Override
					public void handle(int retCode, Object response) {
						// TODO Auto-generated method stub
						Log.d("GrpMap", "retCode:" + retCode);
					}
				}, postData);
				break;
			case R.id.grp_mapv_instant:
				url += "/services/openSendGpsPush";
				postData.put("isOpen", Boolean.toString(true));
				postData.put("appID", "appid");
				postData.put("groupName", curGrp.name);
				postData.put("count", Integer.toString(2));	
				postData.put("users", usersLst.toString());	
				CacheManager.getJson(baseAct, url, new IHttpCallback() {					
					@Override
					public void handle(int retCode, Object response) {
						// TODO Auto-generated method stub
						Log.d("GrpMap", "retCode:" + retCode);
					}
				}, postData);				
				break;
			case R.id.grp_mapv_clear:
				Base.baidu_v.mBaiduMap.clear();
				examp_lv.setVisibility(View.INVISIBLE);
				break;
			default:break;
		}
		
	}
}
