package com.ctg.land;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ctg.group.Group;
import com.ctg.group.Member;
import com.ctg.net.HttpQueue;
import com.ctg.ui.Base;
import com.ctg.ui.R;
import com.ctg.util.ChineseCharToEn;
import com.ctg.util.Util;

public class FrndGrpAdapt extends BaseAdapter {
	protected Base baseAct;

	public List<Member> frndLst;
	public List<Group> grpLst;
	protected LayoutInflater listContainer;
	
	

	public int mergeLen;
	public int frndLen;
	public int grpLen;
	public ListView myLv;

	public FrndGrpAdapt(Context context, List<Member> list1, List<Group> list2) {
		baseAct = (Base) context;
		listContainer = LayoutInflater.from(context);
		frndLst = list1;
		grpLst = list2;
		if(frndLst != null)
			frndLen = frndLst.size();
		else
			frndLst = new ArrayList<Member>();
		if(grpLst != null)
			grpLen = grpLst.size();
		else 
			grpLst = new ArrayList<Group>();
		mergeLen = frndLen + grpLen;

	}

	public void setFrndGrpLst(String searchText){
		if(searchText == null || searchText.equals("")){
			frndLst = HttpQueue.friendLst;
			grpLst = HttpQueue.grpResLst;
			this.notifyDataSetChanged();
		}
		else{
			frndLst = new ArrayList<Member>();
			for(Member member : HttpQueue.friendLst){
				if(member.name.contains(searchText)){
					frndLst.add(member);
				}
			}
			grpLst = new ArrayList<Group>();
			for(Group grp : HttpQueue.grpResLst){
				if(grp.name.contains(searchText))
					grpLst.add(grp);
			}
			this.notifyDataSetChanged();
		}
		if(frndLst != null)
			frndLen = frndLst.size();
		else
			frndLst = new ArrayList<Member>();
		if(grpLst != null)
			grpLen = grpLst.size();
		else 
			grpLst = new ArrayList<Group>();
			
	}
	
	public FrndGrpAdapt(Context context, ListView lv, List<Member> list1, List<Group> list2) {
		this(context, list1, list2);
		myLv = lv;
	}
	
	public void setList(List<Member> list1, List<Group> list2){
		if(list1 != null){
			frndLst = list1;
		}
		if(list2 != null){
			grpLst = list2;
		}
		if(frndLst != null)
			frndLen = frndLst.size();
		if(grpLst != null)
			grpLen = grpLst.size();
		mergeLen = frndLen + grpLen;
		notifyDataSetChanged();
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		if(frndLst != null)
			frndLen = frndLst.size();
		if(grpLst != null)
			grpLen = grpLst.size();
		mergeLen = frndLen + grpLen;
		return mergeLen;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		if(position < frndLst.size())
			return frndLst.get(position);
		else
			return grpLst.get(position - frndLst.size());
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
		Member member = null;
		Group group = null;

		
        if (convertView == null) {
        	vh = new ViewHolder();
        	if(Base.OBDApp.landScapeMode == 1)
	        	convertView = LayoutInflater.from(baseAct).inflate(
						R.layout.frnd_grp_item, null);
        	else{	     
	        	convertView = LayoutInflater.from(baseAct).inflate(
						R.layout.frnd_grp_item_p, null);      		
        	}
        	vh.grpName = (TextView) convertView.findViewById(R.id.grp_item_name_l);	        	
        	vh.headv = (ImageView) convertView.findViewById(R.id.grp_item_head_l);
        	vh.divide = convertView.findViewById(R.id.frnd_grp_item_div_l);
        	convertView.setTag(vh);        	
        }
        else{
        	vh = (ViewHolder) convertView.getTag();
        }
        if(position < frndLst.size()){
        	member = frndLst.get(position);
        	vh.grpName.setText(member.name);
        	if(member.headBitmap != null){
        		Bitmap bitProc = Util.getRoundedCornerImage(member.headBitmap);
        		vh.headv.setImageBitmap(bitProc);        		
        	}

        }
        else{
        	group = grpLst.get(position - frndLst.size());
        	vh.grpName.setText(group.name);
            if(group.grpHead == null){
            	group.grpHead = Group.setGroupHead(group);
            }
            if(group.grpHead != null){
            	Bitmap bitProc = Util.getRoundedCornerImage(group.grpHead);
            	vh.headv.setImageBitmap(bitProc);            	
            }

        }  

//        if(position == mergeLen-1 && Base.OBDApp.landScapeMode == 1){
//        	vh.divide.setVisibility(View.INVISIBLE);
//        }

		// 设置文字图片
		return convertView;
	}
		
	public class ViewHolder{
		public TextView grpName;
		public ImageView headv;
		public View divide;
		public LinearLayout extra;
		public TextView childRN;
		public TextView childDel;
		//int posi;		
	}
	
}