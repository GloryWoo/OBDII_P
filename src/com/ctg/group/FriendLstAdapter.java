package com.ctg.group;

import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ctg.ui.Base;
import com.ctg.ui.R;
import com.ctg.util.Util;

public class FriendLstAdapter extends BaseAdapter {
	private Base baseAct;

	private List<Member> mListItems;

	private LayoutInflater listContainer;
	
	
	private int resId;
	RelativeLayout addr_rela;
	RelativeLayout start_rela;
	TextView poi_title;
	TextView poi_addr;
	ImageView num_img;
	public boolean checkMode; 
	public HashMap<Integer, Boolean> isSelected; 
    //Bitmap headbitmp;
	
	public FriendLstAdapter(Context context, List<Member> listItems) {
		baseAct = (Base) context;
		listContainer = LayoutInflater.from(context);
		mListItems = listItems;
		isSelected = new HashMap<Integer, Boolean>();
		for(int i = 0; i < listItems.size(); i++)
			isSelected.put(i, false);
	}

	public void setList(List<Member> listItems){
		mListItems = listItems;
		isSelected.clear();
		for(int i = 0; i < listItems.size(); i++)
			isSelected.put(i, false);
		notifyDataSetChanged();	
	}
	
	public void EnterCheckMode(){
		checkMode = true;
		notifyDataSetChanged();	
		if(Base.me_v != null){
			Base.me_v.selLinear.setVisibility(View.VISIBLE);
			Base.me_v.operLinear.setVisibility(View.VISIBLE);
		}
	}
	
	public void ExitCheckMode(){
		checkMode = false;
		notifyDataSetChanged();	
		if(Base.me_v != null){
			Base.me_v.selLinear.setVisibility(View.INVISIBLE);
			Base.me_v.operLinear.setVisibility(View.INVISIBLE);
		}
		int len = isSelected.size();
		for(int i = 0; i < len; i++)
			isSelected.put(i, false);
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
			Member member = (Member) getItem(position);

	        if (convertView == null) {
	        	vh = new ViewHolder();
	        	convertView = LayoutInflater.from(baseAct).inflate(
						R.layout.grp_member_item, null);
	        	vh.grpName = (TextView) convertView.findViewById(R.id.grp_item_name);
	        	vh.checkbox = (CheckBox) convertView.findViewById(R.id.grp_item_select);
	        	vh.headv = (ImageView) convertView.findViewById(R.id.grp_item_head);
	        	convertView.setTag(vh);        	
	        }
	        else{
	        	vh = (ViewHolder) convertView.getTag();
	        }
	        vh.grpName.setText(member.name);     
	        if(member.headBitmap != null){
	        	Bitmap bitProc = Util.getRoundedCornerImage(member.headBitmap);
	        	vh.headv.setImageBitmap(bitProc);
	        }
	      
	        if(checkMode){
	        	vh.checkbox.setVisibility(View.VISIBLE);
	        	vh.checkbox.setChecked(isSelected.get(position));
	        }
	        else
	        	vh.checkbox.setVisibility(View.INVISIBLE);
			// 设置文字图片
			return convertView;
		}
		
		public class ViewHolder{
			TextView grpName;
			TextView delete;
			CheckBox checkbox;
			ImageView headv;
			//int posi;		
		}
	
}