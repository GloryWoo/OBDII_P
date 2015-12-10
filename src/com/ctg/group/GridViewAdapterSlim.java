package com.ctg.group;

import java.util.ArrayList;
import java.util.HashMap;

import com.ctg.net.HttpQueue;
import com.ctg.ui.Base;
import com.ctg.ui.R;
import com.ctg.util.GrpSearchDlg;
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
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class GridViewAdapterSlim extends BaseAdapter {

	// Declare variables
	public Base baseAct;
	public ArrayList<Member> gMemberList;

	public boolean mWithCheck;
	public HashMap<Integer, Boolean> isSelected;  
//	Group group;
//	int mGridType;
	public boolean addIconExist;
//	int friendLen;
//	int grpLen;
	Group mGrp;
	Member mMember;
	
	public GridViewAdapterSlim(Activity a, boolean withCheck) {
		baseAct = (Base) a;
		gMemberList = new ArrayList<Member>();
//		gMemberList.addAll(memberList);
		mWithCheck = withCheck;
		
		isSelected = new HashMap<Integer, Boolean>();
//		mGridType = gridType;
//		group = HttpQueue.grpResLst.get(baseAct.me_v.editIdx);	
//		friendLen = HttpQueue.friendLst.size();
//		grpLen = HttpQueue.grpResLst.size();
		if(Base.friendOrGrpIdx < HttpQueue.friendLst.size()){
			mMember = HttpQueue.friendLst.get(Base.friendOrGrpIdx);
			mGrp = null;
			gMemberList.add(HttpQueue.friendLst.get(Base.friendOrGrpIdx));
		}
		else{
			mGrp = HttpQueue.grpResLst.get(Base.friendOrGrpIdx-HttpQueue.friendLst.size());
			mMember = null;
			gMemberList.addAll(mGrp.memberList);
		}
//		if(mGridType != 0 || Base.loginUser.equals(group.creator)){
//			Member addOne = new Member("addOne", 0, false);
//			gMemberList.add(addOne);
//			addIconExist = true;
//		}
		int i = 0;
		for (Member member : gMemberList) { 
			if(member.shield)
				isSelected.put(i, true);
			else
				isSelected.put(i, false);  
			i++;
        } 
		
	}

	public void setWithCheck(){
		mWithCheck = true;
		notifyDataSetChanged();
		Base.me_v.grpDetailDlg.delAndQuit.setText("删除完成");
	}
	
	public void quitCheck(){
		mWithCheck = false;
		notifyDataSetChanged();
		Base.me_v.grpDetailDlg.delAndQuit.setText("删除并退出");
	}
	
    public void refreshGrpList(){
		mGrp = HttpQueue.grpResLst.get(Base.friendOrGrpIdx-HttpQueue.friendLst.size());
		gMemberList.clear();
		gMemberList.addAll(mGrp.memberList);
		this.notifyDataSetChanged();
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
		if(Base.friendOrGrpIdx < HttpQueue.friendLst.size() || mWithCheck)
			return gMemberList.size();
		else if(mGrp.creator.equals(Base.loginUser))
			return gMemberList.size()+2;
		else
			return gMemberList.size()+1;		
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
		 public View delMark;
		 public int mPos;
	 }

	 
	 @Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		 ViewHolder holder = null;
		 Log.i("GridViewAdapter getView", String.valueOf(position));
		 RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(0, 0); 
		 
		 params.topMargin = 10*Base.mDensityInt;
		 if (convertView == null) {
//			 LayoutInflater vi = (LayoutInflater)baseAct.getSystemService(
//					 Context.LAYOUT_INFLATER_SERVICE);
			 convertView = View.inflate(baseAct, R.layout.grp_grid_detail_item, null);

			 holder = new ViewHolder();
			 holder.image = (ImageView) convertView.findViewById(R.id.grid_item_image);
			 holder.name = (TextView) convertView.findViewById(R.id.name_g);
			 holder.delMark = convertView.findViewById(R.id.del_mark);
			 holder.mPos = position;
//			 if(mWithCheck){
//				 holder.delMark.setVisibility(View.VISIBLE);
//			 }

			 convertView.setTag(holder);
		 }
		 else {
			 holder = (ViewHolder) convertView.getTag();
		 }

		 if(position < gMemberList.size()){
			 Member member = gMemberList.get(position);
	
			 holder.name.setVisibility(View.VISIBLE);
			 holder.name.setText(member.getName());
			 if(member.headBitmap != null)
			 {
				 //Bitmap bmp = BitmapFactory.decodeFile(member.getImagePath());	
				 //holder.image.setImageBitmap(member.headBitmap);
	        	 Bitmap bitProc = Util.getRoundedCornerImage(member.headBitmap);
//				 if(member.getIsOnline() == 0)
//					 bitProc = Util.setColorGrey(bitProc);
	        	 holder.image.setImageBitmap(bitProc);
			 }
			 else if(member.name.equals(Base.loginUser) && Base.headbitmap != null){
					Bitmap bitProc = Util.getRoundedCornerImage(Base.headbitmap);
					holder.image.setImageBitmap(bitProc);
			 }
			 else
			 {
				 Bitmap bmp = BitmapFactory.decodeResource(baseAct.getResources(), R.drawable.ic_launcher_df);
//				 if(member.getIsOnline() == 0)
//					 bmp = Util.setColorGrey(bmp);
				 
				 holder.image.setImageBitmap(bmp);
			 }
			 if(mWithCheck && !member.getName().equals(Base.loginUser)){
				 holder.delMark.setVisibility(View.VISIBLE);
				 holder.delMark.setOnClickListener(new View.OnClickListener(){

						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							setWithCheck();
						}
						 
				 });
				 convertView.setOnClickListener(new View.OnClickListener(){

						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							if(mWithCheck){
								ViewHolder thisVh = (ViewHolder) v.getTag();
								int idx = thisVh.mPos;
								Group grp = HttpQueue.grpResLst.get(Base.friendOrGrpIdx-HttpQueue.friendLst.size());
								if(idx < grp.memberList.size()){
									Member mem = grp.memberList.get(idx);
									grp.delMemberFromGroup(mem);	
								}
							}
						}
						 
				 });
			 }
			 else
				 holder.delMark.setVisibility(View.INVISIBLE);
			 
			 //holder.image.setBackgroundResource(R.drawable.shape_img);
		 }
		 else if(Base.friendOrGrpIdx >= HttpQueue.friendLst.size() && position == gMemberList.size() && !mWithCheck){
			 holder.name.setVisibility(View.INVISIBLE);
			 holder.delMark.setVisibility(View.INVISIBLE);
			 holder.image.setImageResource(R.drawable.icon_add);
			 //holder.image.layout(holder.image.getLeft(), holder.image.getTop()+10*Base.mDensityInt, holder.image.getBottom()+10*Base.mDensityInt, holder.image.getRight());
//			 holder.image.setLayoutParams(params);
			 holder.image.setOnClickListener(new View.OnClickListener(){

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					Base.me_v.searchGroupDlg = new GrpSearchDlg(baseAct, 300*Base.mDensityInt, 320*Base.mDensityInt, R.layout.grp_user_search, R.style.Theme_dialog, true);
					Base.me_v.searchGroupDlg.show();
				}
				 
			 });

		 }
		 else if(Base.friendOrGrpIdx >= HttpQueue.friendLst.size() && position == gMemberList.size()+1 && !mWithCheck){
			 holder.name.setVisibility(View.INVISIBLE);
			 holder.delMark.setVisibility(View.INVISIBLE);
			 holder.image.setImageResource(R.drawable.icon_putoff);	
			 //holder.image.layout(holder.image.getLeft(), holder.image.getTop()+10*Base.mDensityInt, holder.image.getBottom()+10*Base.mDensityInt, holder.image.getRight());
			 holder.image.setOnClickListener(new View.OnClickListener(){

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					setWithCheck();
				}
				 
			 });
		 }

		 
		 return convertView;
	 }
}