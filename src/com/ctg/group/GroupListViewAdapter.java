package com.ctg.group;


import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.ctg.net.HttpQueue;
import com.ctg.ui.Base;
import com.ctg.ui.Me;
import com.ctg.ui.R;
import com.ctg.util.Preference;
import com.ctg.util.GrpDelDlg.GrpLstSelAdapter;



import android.app.Activity;
import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;


public class GroupListViewAdapter {
	private Activity activity;
	public Base baseAct;
	private Context context;
	public ViewPager viewPager;
	// private PagerTitleStrip pagerTitleStrip;
	private PagerTabStrip pagerTabStrip;
	private List<View> viewList;
	private List<String> titleList;
	private List<GridViewAdapter>adapterList;
	public PagerAdapter pagerAdapter;
	private GestureDetector myExcuseGestureDetector;
	private final String TAG="GroupListView";
	View vi;
//	Button excuseView;
	public boolean mWithCheck;
//	public ChatDlg chagDlg;
	public int curGroupPos;
	
	public void updateAdapter(int position, Member member)
	{		
		GridViewAdapter adapter = adapterList.get(position);

		if(adapter.contains(member))
		{
			position = adapter.indexOf(member);
			adapter.set(position, member);
			adapter.notifyDataSetChanged();
		}
	}
	
	public void enterSelectMode(){
		int idx = 0;
		mWithCheck = !mWithCheck;
//		if(mWithCheck)
//			for(GridViewAdapter adapter : adapterList){				
//				adapter.mWithCheck = true;
////				adapter.notifyDataSetChanged();	
//				View view = viewList.get(idx);
//				LinearLayout grpLinear = (LinearLayout)view.findViewById(R.id.grp_linear);
//				grpLinear.setVisibility(View.VISIBLE);
//				Button commitBtn = (Button) view.findViewById(R.id.result_btn);
//				commitBtn.setVisibility(View.VISIBLE);
//				GridView gridv = (GridView) view.findViewById(R.id.listView_group_members);
//				int len = gridv.getChildCount();
//				for(int i = 0; i < len; i++){
//					View v = gridv.getChildAt(i);
//					View check_v = v.findViewById(R.id.group_item_select);
//					if(check_v != null)
//						check_v.setVisibility(View.VISIBLE);
//				}
//				idx++;
//			}
	}
	
	public void exitSelectMode(){
		int idx = 0;
		mWithCheck = !mWithCheck;
//		if(!mWithCheck)
//			for(GridViewAdapter adapter : adapterList){				
//				adapter.mWithCheck = false;
////				adapter.notifyDataSetChanged();		
//				View view = viewList.get(idx);
//				LinearLayout grpLinear = (LinearLayout)view.findViewById(R.id.grp_linear);
//				grpLinear.setVisibility(View.INVISIBLE);
//				Button commitBtn = (Button) view.findViewById(R.id.result_btn);
//				commitBtn.setVisibility(View.INVISIBLE);
//				GridView gridv = (GridView) view.findViewById(R.id.listView_group_members);
//				int len = gridv.getChildCount();
//				for(int i = 0; i < len; i++){
//					View v = gridv.getChildAt(i);
//					View check_v = v.findViewById(R.id.group_item_select);
//					if(check_v != null)
//						check_v.setVisibility(View.INVISIBLE);
//				}
//				idx++;
//			}
	}
	
	public GroupListViewAdapter(Me me)
	{
		super();
		activity = me.baseAct;
		baseAct = me.baseAct;
	    viewList = new ArrayList<View>();  
	    titleList = new ArrayList<String>();
	    adapterList = new ArrayList<GridViewAdapter>();
		 
	    viewPager = (ViewPager) me.rela_v.findViewById(R.id.viewpager); 
	    viewPager.setOnPageChangeListener(new OnPageChangeListener(){  
            @Override  
            public void onPageSelected(int position) {                  
            	curGroupPos = position;
            }  
            
            @Override  
            public void onPageScrolled(int arg0, float arg1, int arg2){  
            }  
            
            @Override  
            public void onPageScrollStateChanged(int arg0){  
            }  
        }); 
	    //pagerTitleStrip = (PagerTitleStrip) findViewById(R.id.pagertitle); 
	    pagerTabStrip=(PagerTabStrip) me.rela_v.findViewById(R.id.pagertab); 
	    pagerTabStrip.setTabIndicatorColor(activity.getResources().getColor(R.color.gold));
	    pagerTabStrip.setDrawFullUnderline(false); 
	    pagerTabStrip.setBackgroundColor(activity.getResources().getColor(R.color.azure)); 
	    pagerTabStrip.setTextSpacing(50);
	    pagerTabStrip.setTextColor(activity.getResources().getColor(R.color.gold));
	    pagerAdapter = new PagerAdapter() {

	    	@Override  
	        public boolean isViewFromObject(View arg0, Object arg1) {  
	
	            return arg0 == arg1;  
	        }  
	
	        @Override  
	        public int getCount() {  
	
	            return viewList.size();  
	        }

	        @Override  
	        public void destroyItem(ViewGroup container, int position,  
	                Object object) {  
	            container.removeView(viewList.get(position));  
	        }  
	
	        @Override  
	        public int getItemPosition(Object object) {  
	
	            return super.getItemPosition(object);  
	        }  
	
	        @Override  
	        public CharSequence getPageTitle(int position) {  
	
	            return titleList.get(position);
	
	        }  

	        @Override  
	        public Object instantiateItem(ViewGroup container, int position) {  
	            container.addView(viewList.get(position));  

	            return viewList.get(position);
	        }
	    };  
	}

	public boolean contains(int groupID)
	{
	    for(int i=0; i< viewList.size();i++)
		{
	    	if(titleList.get(i).equals("G "+groupID))
	    	{
	    		return true;
	    	}
		}
	    return false;
	}
	
	public int indexOf(int groupID)
	{
	    for(int i=0; i< viewList.size();i++)
		{
	    	if(titleList.get(i).equals(groupID))
	    	{
	    		return i;
	    	}
		}
	    return -1;
	}

	public boolean addGroupMember(String GrpNm, String memNm){
		Member member = new Member(memNm, 1, false);
		Group group = null;
		int idx = 0;
		GridViewAdapter adapter;
		for(Group grp : HttpQueue.grpResLst){
			if(grp.name.equals(GrpNm)){
				group = grp;				
				break;
			}
			idx++;
		}
		if(group != null){
			if(group.memberList.contains(member)){				
				return false;
			}
			group.memberList.add(member);
			adapter = adapterList.get(idx);
			adapter.clear();
	    	adapter.addAll(group.memberList);
	    	adapter.notifyDataSetChanged();
	    	return true;
		}
		else{
			
		}
		return false;
	}
	
	public boolean setGroupName(int position, String name){
		
		View view = null;
		GridViewAdapter adapter = null;
		
	    
		view = viewList.get(position);
//		Button groubBtn = (Button) view.findViewById(R.id.group_view);
//		groubBtn.setText(name);
		
		return true;
	}
	
	public boolean set(int position, int groupID, String groupDsc, ArrayList<Member> memberList)
	{
		//View view = null;
		GridViewAdapter adapter = null;
		
		if(memberList == null) memberList = new ArrayList<Member> ();
	    
	    //view = viewList.get(position);
	    adapter = adapterList.get(position);


    	//GridView gridView = (GridView) view.findViewById(R.id.listView_group_members);
		// Assign adapter to GridView
    	adapter.clear();
    	adapter.addAll(memberList);
    	adapter.notifyDataSetChanged();
		Log.i(TAG, " view group id " + groupID + memberList.size());
	    return true;
	}

	public void remove(int position) {
	    viewPager.setAdapter(null);
	    viewPager.removeView(viewList.get(position));
	    titleList.remove(position);
	    adapterList.remove(position);
	    viewList.remove(position);
	    viewPager.setAdapter(pagerAdapter);
	}  

	public boolean add(int groupID, String creator, String groupNm, ArrayList<Member> memberList) {
		View view = null;
		Button excuseView;
		GridViewAdapter adapter = new GridViewAdapter(activity, R.layout.group_list_member_info, memberList, false, 0);
		
		if(memberList == null) memberList = new ArrayList<Member> ();
    	LayoutInflater lf = (LayoutInflater) activity.getSystemService(activity.LAYOUT_INFLATER_SERVICE);
	    // LayoutInflater lf = activity.getLayoutInflater().from(activity);
//	    view    = lf.inflate(R.layout.group_grid_list, null);
//	    
//		excuseView = (Button) view.findViewById(R.id.group_view);
//	    excuseView.setText(groupNm);
//		//myExcuseGestureDetector = new GestureDetector(activity, new GroupListGestureDetector(activity));
//
//		excuseView.setOnTouchListener(new View.OnTouchListener() {
//			
//			@Override
//		    public boolean onTouch(View v, MotionEvent event) {
//		       Log.i(TAG, "excuseView onTouchEvent return false");
//		       return false;
//		    }
//		});
//
//		excuseView.setOnClickListener(new View.OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				// TODO Auto-generated method stub
////				chagDlg = new ChatDlg(activity, baseAct.mWidth, baseAct.realHeight, R.layout.chat_layout, R.style.Theme_dialog1, null);
////				chagDlg.show();
//			}
//		});
		
    	GridView gridView = null;//= (GridView) view.findViewById(R.id.listView_group_members);
    	gridView.setOnItemClickListener(new OnItemClickListener() {  
    		@Override  
    		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) { 
    			GridViewAdapter adapter_local = (GridViewAdapter) arg0.getAdapter();
    			if(adapter_local.mWithCheck){
	    			GridViewAdapter.ViewHolder holder = (GridViewAdapter.ViewHolder) arg1.getTag();
	    			holder.check.toggle();
	    			adapter_local.isSelected.put(arg2, holder.check.isChecked()); 
    			}
    		}
    	});
    	LinearLayout grpLinear = null;//(LinearLayout)view.findViewById(R.id.grp_linear);
    	Button bt_selectall = (Button) grpLinear.findViewById(R.id.bt_selectall);  
    	Button bt_cancel = (Button) grpLinear.findViewById(R.id.bt_cancleselectall);  
    	Button bt_deselectall = (Button) grpLinear.findViewById(R.id.bt_deselectall);  
    	Button commitBtn = (Button) view.findViewById(R.id.result_btn); 
    	bt_selectall.setTag(viewList.size());
    	bt_cancel.setTag(viewList.size());
    	bt_deselectall.setTag(viewList.size());
    	commitBtn.setTag(viewList.size());
//		bt_selectall.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				// 遍历list的长度，将MyAdapter中的map值全部设为true
//				int idx = (Integer) v.getTag();
//				View view = viewList.get(idx);
//				GridView gridv = (GridView) view.findViewById(R.id.listView_group_members);
//				GridViewAdapter adapter_local = adapterList.get(idx);
//				for (int i = 0; i < adapter_local.gMemberList.size(); i++) {
//					adapter_local.isSelected.put(i, true);
//					View itemv = gridv.getChildAt(i);
//					CheckBox check_v = (CheckBox)itemv.findViewById(R.id.group_item_select);
//					check_v.setChecked(true);
//				}
//			}
//		});

		// 反选按钮的回调接口
		bt_cancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				int idx = (Integer) v.getTag();
				View view = viewList.get(idx);
//				GridView gridv = (GridView) view.findViewById(R.id.listView_group_members);				
//				GridViewAdapter adapter_local = adapterList.get(idx);
//				// 遍历list的长度，将已选的设为未选，未选的设为已选
//				for (int i = 0; i < adapter_local.gMemberList.size(); i++) {
//					View itemv = gridv.getChildAt(i);
//					CheckBox check_v = (CheckBox)itemv.findViewById(R.id.group_item_select);
//					if (adapter_local.isSelected.get(i)) {
//						adapter_local.isSelected.put(i, false);
//						check_v.setChecked(false);
//						// checkNum--;
//					} else {
//						adapter_local.isSelected.put(i, true);
//						check_v.setChecked(true);
//						// checkNum++;
//					}
//				}
//				adapter_local.notifyDataSetChanged();
				// 刷新listview和TextView的显示
				// dataChanged();
			}
		});

		// 取消按钮的回调接口
		bt_deselectall.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				int idx = (Integer) v.getTag();
				View view = viewList.get(idx);
//				GridView gridv = (GridView) view.findViewById(R.id.listView_group_members);		
//				GridViewAdapter adapter_local = adapterList.get(idx);
//				// 遍历list的长度，将已选的按钮设为未选
//				for (int i = 0; i < adapter_local.gMemberList.size(); i++) {					
//					if (adapter_local.isSelected.get(i)) {
//						View itemv = gridv.getChildAt(i);
//						CheckBox check_v = (CheckBox)itemv.findViewById(R.id.group_item_select);
//						adapter_local.isSelected.put(i, false);
//						check_v.setChecked(false);
//						// checkNum--;// 数量减1
//					}
//				}
//				adapter_local.notifyDataSetChanged();
				// 刷新listview和TextView的显示
				// dataChanged();
			}
		});

		// 反选按钮的回调接口
		commitBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				int idx = (Integer) v.getTag();		
				String loginUser = Preference.getInstance(context).getUser();
				boolean isCreator = loginUser.equals(HttpQueue.grpResLst.get(idx).creator);
				int selectLen = 0;
				GridViewAdapter adapter_local = adapterList.get(idx);
				ArrayList<String> memberNameArr = new ArrayList<String>();				
				// 遍历list的长度，将已选的设为未选，未选的设为已选
				for (int i = 0; i < adapter_local.gMemberList.size(); i++) {
					if (adapter_local.isSelected.get(i)) {
						Member member = adapter_local.gMemberList.get(i);
						if(isCreator || member.name.equals(loginUser))
							memberNameArr.add(member.name);
						selectLen++;
					}
				}
				
				if(selectLen == 0){
					Toast.makeText(activity, "没有选择任何一项", Toast.LENGTH_SHORT).show();
				}
				else if(memberNameArr.size() == 0){
					Toast.makeText(activity, "你不是群主，只能删除自己而不能删除别人。", Toast.LENGTH_SHORT).show();
				}
				else{
					String url = Base.HTTP_GROUP_PATH+"/quitFromGroup";
					JSONObject obj = new JSONObject();
					JSONArray jsArr = new JSONArray();
					try {																	
						obj.put("groupName", HttpQueue.grpResLst.get(idx).name);
						obj.put("appid", "appid");
						for(String usrName : memberNameArr){
							jsArr.put(usrName);
						}
						obj.put("userName", jsArr);						
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} 				
					Base.OBDApp.baseAct.httpQueueInstance.EnQueue(url, obj, 16);
				}
				// 刷新listview和TextView的显示
				// dataChanged();
			}
		});
         
    	if(adapter.mWithCheck){
    		grpLinear.setVisibility(View.VISIBLE);
    		commitBtn.setVisibility(View.VISIBLE);
    	}
    	else{
    		grpLinear.setVisibility(View.INVISIBLE);
    		commitBtn.setVisibility(View.INVISIBLE);
    	}
		// Assign adapter to GridView
    	gridView.setAdapter(adapter);
		Log.i(TAG, " view group id " + groupID + memberList.size());

		gridView.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				Log.i(TAG, " view group id " + event.getAction() );
				if(myExcuseGestureDetector!=null && myExcuseGestureDetector.onTouchEvent(event)){
			         Log.i(TAG, "listView onTouchEvent return true");
			         return true;
			       }
			       Log.i(TAG, "listView onTouchEvent return false");
			       return false;
			}
		});

	    viewList.add(view);
	    adapterList.add(adapter);
	    titleList.add("G " + groupID);

	    return true;
	}  

	
	
	public void setCurrentItem(int groupID)
	{
		for(int i=0; i< viewList.size();i++)
		{
			Log.i(TAG, titleList.get(i)+ "    " + "G "+groupID + titleList.get(i).length()
					+ "   ");
			
			if(titleList.get(i).equals("G "+groupID))
			{
				viewPager.setCurrentItem(i, true);
			}
		}
	}
	
    public void setAdapter(){
    	
    	viewPager.setAdapter(pagerAdapter);
    }
   
}