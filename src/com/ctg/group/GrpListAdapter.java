package com.ctg.group;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.baidu.mapapi.model.LatLng;
import com.baidu.navisdk.BNaviPoint;
import com.baidu.navisdk.BaiduNaviManager;
import com.baidu.navisdk.comapi.routeplan.RoutePlanParams;
import com.ctg.ui.BNavigatorActivity;
import com.ctg.ui.Base;
import com.ctg.ui.R;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import com.ctg.ui.Base.MyBitmapEntity;
import com.example.combinebitmap.BitmapUtil;

public class GrpListAdapter extends BaseAdapter {
	private Base baseAct;

	private ArrayList<Group> mListItems;

	private LayoutInflater listContainer;
	
	public View curOperateV;
	private int resId;
	RelativeLayout addr_rela;
	RelativeLayout start_rela;
	TextView poi_title;
	TextView poi_addr;
	ImageView num_img;
	public int mode; //0 common; 1 delete & rename; 2 rename
	float x_pos = 0;
    final static int X_GAP = 75;
    public int cur_pos_non_common = 0;
    ViewPropertyAnimator animator;
    
	public GrpListAdapter(Context context, ArrayList<Group> listItems) {
		baseAct = (Base) context;
		listContainer = LayoutInflater.from(context);
		mListItems = listItems;
	}

	public void setList(ArrayList<Group> listItems){
		mListItems = listItems;
		notifyDataSetChanged();
	}
	

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mListItems.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return mListItems.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {        
        //v = super.getView(position, convertView, parent);
		ViewHolder vh;
		Group grp = (Group) getItem(position);
		
        if (convertView == null) {
        	vh = new ViewHolder();
        	convertView = LayoutInflater.from(baseAct).inflate(
					R.layout.grp_member_item, null);
        	vh.mixImgHead = (ImageView) convertView.findViewById(R.id.grp_item_head);
        	vh.grpName = (TextView) convertView.findViewById(R.id.grp_item_name);
        	vh.rename_e = (EditText) convertView.findViewById(R.id.grp_item_name_e);
//        	vh.delete = (TextView) convertView.findViewById(R.id.grp_item_del);
//        	vh.rename = (TextView) convertView.findViewById(R.id.grp_item_rn);
        	convertView.setTag(vh);        	
        }
        else{
        	vh = (ViewHolder) convertView.getTag();
        }
        if(grp.grpHead == null){
        	grp.grpHead = Group.setGroupHead(grp);
        }
        if(grp.grpHead != null){
        	vh.mixImgHead.setImageBitmap(grp.grpHead);
        }
        vh.grpName.setText(grp.name);        
        if(vh.rename_e != null)
        	if(Base.me_v.editMode && position == Base.me_v.editIdx){
        		vh.rename_e.setVisibility(View.VISIBLE);
        		vh.rename_e.setFocusable(true);
        		vh.rename_e.setClickable(true);
        	}
        	else{
        		vh.rename_e.setVisibility(View.INVISIBLE);
        	}
        	vh.rename_e.setText(grp.name);      
        	vh.rename_e.setOnKeyListener(new View.OnKeyListener(){

				@Override
				public boolean onKey(View v, int keyCode, KeyEvent event) {
					// TODO Auto-generated method stub
					if(keyCode == KeyEvent.KEYCODE_CLEAR){
						View childv = Base.me_v.group_v.getChildAt(Base.me_v.editIdx);
						childv.findViewById(R.id.grp_item_name).setVisibility(View.VISIBLE);
						childv.findViewById(R.id.grp_item_name_e).setVisibility(View.INVISIBLE);
						Base.me_v.setEditMode(false);
					}
					return false;
				}
        		
        	});
	        vh.rename_e.setOnEditorActionListener(new EditText.OnEditorActionListener(){
	
				@Override
				public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
					// TODO Auto-generated method stub
					if(event != null){
						if(event.getKeyCode() == KeyEvent.KEYCODE_ENTER){			
							Group grp = (Group) mListItems.get(Base.me_v.editIdx);
							if(!v.getText().equals(grp.name)){								
								JSONObject obj = new JSONObject();
								String url = Base.HTTP_GROUP_PATH+"/update";
								try {																	
									obj.put("oldGroupName", grp.name);
									obj.put("newGroupName", v.getText().toString().trim());
									obj.put("appid", "appid");
								} catch (JSONException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								} 				
								baseAct.httpQueueInstance.EnQueue(url, obj, 13);
							}
							View childv = Base.me_v.group_v.getChildAt(Base.me_v.editIdx);
							childv.findViewById(R.id.grp_item_name).setVisibility(View.VISIBLE);
							childv.findViewById(R.id.grp_item_name_e).setVisibility(View.INVISIBLE);
							Base.me_v.setEditMode(false);
						}
					
					}
					return false;
				}
	        	
	        });
		// 设置文字图片
		return convertView;
	}
	
	public class ViewHolder{
		ImageView mixImgHead;
		TextView grpName;
		TextView delete;
		TextView rename;
		EditText rename_e;
		//int posi;		
	}
}