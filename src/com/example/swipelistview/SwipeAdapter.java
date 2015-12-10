package com.example.swipelistview;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.ctg.group.Group;
import com.ctg.group.Member;
import com.ctg.land.FrndGrpAdapt;
import com.ctg.land.FrndGrpAdapt.ViewHolder;
import com.ctg.ui.Base;
import com.ctg.ui.R;
import com.ctg.util.GrpCreateDlg;
import com.ctg.util.Util;


public class SwipeAdapter extends BaseAdapter {
	protected Base baseAct;

	protected List<Member> frndLst;
	protected List<Group> grpLst;
	protected LayoutInflater listContainer;
	
	

	public int mergeLen;
	public int frndLen;
	public int grpLen;
//	int curPosition;
	
	public SwipeAdapter(Context context){
		baseAct = (Base) context;
	}
	public SwipeAdapter(Context context, List<Member> list1, List<Group> list2) {
		baseAct = (Base) context;
		listContainer = LayoutInflater.from(context);
		frndLst = list1;
		grpLst = list2;
		if(frndLst != null)
			frndLen = frndLst.size();
		if(grpLst != null)
			grpLen = grpLst.size();
		mergeLen = frndLen + grpLen;
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
		return mergeLen;
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return arg0;
	}

	@Override
	public View getView(int position, View contentView, ViewGroup arg2) {
		ViewHolder vh;
		Member member = null;
		Group group = null;

		if(contentView==null){
        	vh = new ViewHolder();
			View view01 = LayoutInflater.from(baseAct).inflate(R.layout.frnd_grp_item_p, null);
			View view02 = LayoutInflater.from(baseAct).inflate(R.layout.frnd_grp_item_extra, null);
			contentView = new SwipeItemLayout(view01, view02, null, null);

        	vh.extra = (LinearLayout) contentView.findViewById(R.id.invisiblepart);
        	vh.childRN = (TextView) vh.extra.findViewById(R.id.grp_frnd_rename);
        	vh.childDel = (TextView) vh.extra.findViewById(R.id.grp_frnd_del);      		        	
        	vh.grpName = (TextView) contentView.findViewById(R.id.grp_item_name_l);	        	
        	vh.headv = (ImageView) contentView.findViewById(R.id.grp_item_head_l);
        	vh.divide = contentView.findViewById(R.id.frnd_grp_item_div_l);
        	contentView.setTag(vh);        	
        }
        else{
        	vh = (ViewHolder) contentView.getTag();
        }
		vh.childRN.setTag(position);
		vh.childDel.setTag(position);
    	if(position < frndLst.size()){
    		vh.childRN.setVisibility(View.GONE);
    	}
    	else{
    		group = grpLst.get(position - frndLst.size());
            if(group.creator.equals(Base.loginUser)){
            }
            else{
            	vh.childRN.setVisibility(View.GONE);
            }
    	}  
		vh.childRN.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				int curPosition = (Integer) v.getTag();
				SwipeItemLayout swipeItem = (SwipeItemLayout) v.getParent().getParent();
				swipeItem.closeMenu();
				Group group = grpLst.get(curPosition - frndLst.size());
				Base.me_v.createGroupDlg = new GrpCreateDlg(baseAct, 320*Base.mDensityInt, 320*Base.mDensityInt, R.layout.group_create, R.style.Theme_dialog, 1, group.name);
				Base.me_v.createGroupDlg.show();
			}
			
		});
		vh.childDel.setOnClickListener(new OnClickListener(){

			
			@Override
			public void onClick(View v) {
				Member member = null;
				Group group = null;
				int curPosition = (Integer) v.getTag();
				SwipeItemLayout swipeItem = (SwipeItemLayout) v.getParent().getParent();
				swipeItem.closeMenu();
				// TODO Auto-generated method stub
				if (curPosition < frndLst.size()) {
					member = frndLst.get(curPosition);
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
					group = grpLst.get(curPosition - frndLst.size());
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
			}

		});
        if(position < frndLst.size()){
        	member = frndLst.get(position);
        	vh.grpName.setText(member.name);
        	if(member.headBitmap != null){
        		Bitmap bitProc = Util.getRoundedCornerImage(member.headBitmap);
        		vh.headv.setImageBitmap(bitProc);        		
        	}
        	else{
        		Bitmap bitProc = Util.getRoundedCornerImage(Base.dftHeadBitmap);
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
            else{
        		Bitmap bitProc = Util.getRoundedCornerImage(Base.dftHeadBitmap);
        		vh.headv.setImageBitmap(bitProc);
            }

        }  

//        if(position == mergeLen-1 && Base.OBDApp.landScapeMode == 1){
//        	vh.divide.setVisibility(View.INVISIBLE);
//        }

		
		return contentView;
	
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
