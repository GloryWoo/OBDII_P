package com.ctg.group;

import java.util.ArrayList;
import java.util.HashMap;

import com.ctg.net.HttpQueue;
import com.ctg.ui.Base;
import com.ctg.ui.R;
import com.ctg.util.Util;



import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

public class GridViewAdapter extends BaseAdapter {

	// Declare variables
	public Base baseAct;
	public ArrayList<Member> gMemberList;

	public boolean mWithCheck;
	public HashMap<Integer, Boolean> isSelected;  
	Group group;
	int mGridType;
	public boolean addIconExist;
	public GridViewAdapter(Activity a, int textViewResourceId, ArrayList<Member> memberList, boolean withCheck, int gridType) {
		baseAct = (Base) a;
		gMemberList = new ArrayList<Member>();
		gMemberList.addAll(memberList);
		mWithCheck = withCheck;
		
		isSelected = new HashMap<Integer, Boolean>();
		mGridType = gridType;
		group = HttpQueue.grpResLst.get(baseAct.me_v.editIdx);	
		if(mGridType != 0 || Base.loginUser.equals(group.creator)){
			Member addOne = new Member("addOne", 0, false);
			gMemberList.add(addOne);
			addIconExist = true;
		}
		int i = 0;
		for (Member member : gMemberList) { 
			if(member.shield)
				isSelected.put(i, true);
			else
				isSelected.put(i, false);  
			i++;
        } 
		
	}

	public ArrayList<Member> getAll()
	{
		return this.gMemberList;
	}
	
	public void set(int position, Member member)
	{
		this.gMemberList.set(position, member);
	}

	public boolean contains(Member member)
	{
		return gMemberList.contains(member);
	}
	
	public int indexOf(Member member)
	{
		return gMemberList.indexOf(member);
	}
	
	public void add(Member member)
	{
		this.gMemberList.add(member);
	}
	
	public void addAll(ArrayList<Member> memberList)
	{
		this.gMemberList.addAll(memberList);
	}
	
	public void remove(int position)
	{
		this.gMemberList.remove(position);
	}
	
	public void clear()
	{
		this.gMemberList.clear();
	}
	@Override
	public int getCount() {
		return gMemberList.size();
	}
	
	@Override
	public Object getItem(int position) {
		return position;
	}
	
	@Override
	public long getItemId(int position) {
		return position;
	}

	public class ViewHolder {
		 public ImageView image;
		 //TextView plateNumber;
		 public TextView name;
		 public CheckBox check;
	 }

	 
	 @Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		 ViewHolder holder = null;
		 Log.i("GridViewAdapter getView", String.valueOf(position));

		 if (convertView == null) {
			 LayoutInflater vi = (LayoutInflater)baseAct.getSystemService(
					 Context.LAYOUT_INFLATER_SERVICE);
			 convertView = vi.inflate(R.layout.group_list_member_info, null);

			 holder = new ViewHolder();
			 holder.image = (ImageView) convertView.findViewById(R.id.image);
			 holder.name = (TextView) convertView.findViewById(R.id.name_g);
			 holder.check = (CheckBox) convertView.findViewById(R.id.group_item_select);

			 if(mWithCheck){
				 holder.check.setVisibility(View.VISIBLE);
				 holder.check.setChecked(isSelected.get(position));
			 }

			 convertView.setTag(holder);
		 }
		 else {
			 holder = (ViewHolder) convertView.getTag();
		 }

		 if(position < gMemberList.size()-1 || !addIconExist){
			 Member member = gMemberList.get(position);
	
			 holder.name.setVisibility(View.VISIBLE);
			 holder.name.setText(member.getName());
			 if(member.headBitmap != null)
			 {
				 //Bitmap bmp = BitmapFactory.decodeFile(member.getImagePath());	
				 //holder.image.setImageBitmap(member.headBitmap);
	        	 Bitmap bitProc = Util.getRoundedCornerImage(member.headBitmap);
				 if(member.getIsOnline() == 0)
					 bitProc = Util.setColorGrey(bitProc);
	        	 holder.image.setImageBitmap(bitProc);
			 }
			 else
			 {
				 Bitmap bmp = BitmapFactory.decodeResource(baseAct.getResources(), R.drawable.ic_launcher_df);
				 if(member.getIsOnline() == 0)
					 bmp = Util.setColorGrey(bmp);
				 
				 holder.image.setImageBitmap(bmp);
			 }
			 //holder.image.setBackgroundResource(R.drawable.shape_img);
		 }
		 else{
			 holder.name.setVisibility(View.INVISIBLE);
			 holder.check.setVisibility(View.INVISIBLE);
			 holder.image.setImageResource(R.drawable.cursor_add);
		 }
		 return convertView;
	 }
}