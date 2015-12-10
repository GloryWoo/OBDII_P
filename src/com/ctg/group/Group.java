package com.ctg.group;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.ctg.net.HttpQueue;
import com.ctg.ui.Base;
import com.ctg.ui.R;
import com.ctg.ui.Base.MyBitmapEntity;
import com.example.combinebitmap.BitmapUtil;

import android.graphics.Bitmap;
import android.media.ThumbnailUtils;

public class Group implements Serializable {
	public String creator;
	public String name;
	public int groupID = 0;
	public String groupDsc = null;
	public ArrayList<Member> memberList = null;
	public Bitmap grpHead;
	public boolean checked;
	public boolean shield;
	public Group(int groupID, String groupDsc, ArrayList<Member> memberList) {
		super();
		this.groupID = groupID;
		this.groupDsc = groupDsc;
		if (memberList != null) {
			this.memberList = new ArrayList<Member>();
			this.memberList.addAll(memberList);
		}
	}

	public Group(int grpIdx, String creat, String groupName, String desc, ArrayList<Member> memLst){
		groupID = grpIdx;
		creator = creat;
		name = groupName;
		groupDsc = desc;
		memberList = memLst;
	}
	
	public Group(String groupName){
		name = groupName;
	}
	
	public Group(String groupName, String creatName){
		name = groupName;
		creator = creatName;
	}
	
	public int GetGroupID() {
		return this.groupID;
	}

	public void SetGroupID(int groupID) {
		this.groupID = groupID;
	}

	public String GetGroupDsc() {
		return this.groupDsc;
	}

	public void SetGroupDsc(String groupDsc) {
		this.groupDsc = groupDsc;
	}

	public ArrayList<Member> GetMemberList() {
		return this.memberList;
	}

	public void SetMemberList(ArrayList<Member> memberList) {
		if (null == this.memberList)
			this.memberList = new ArrayList<Member>();
		else
			this.memberList.clear();
		this.memberList.addAll(memberList);
	}

	public static int indexOfByName(ArrayList<Group> list, String name){
		int idx = 0;
		
		for(Group grp : list){
			if(grp.name.equals(name))
				return idx;
			idx++;
		}
		
		return -1;
	}
	
	public static ArrayList<Group> filterGroupList(String name, ArrayList<Group> list){
		ArrayList<Group> newLst = new ArrayList<Group>();
		for(Group grp : list){
			if(grp.creator.equals(name))
				newLst.add(grp);
		}
		return newLst;
	}
	
	public void delMemberFromGroup(Member member){
		String url = Base.HTTP_GROUP_PATH+"/quitFromGroup";
		JSONObject obj = new JSONObject();
		JSONArray jsArr = new JSONArray();
		try {																	
			obj.put("groupName", this.name);
			obj.put("appid", "appid");
			jsArr.put(member.name);
			obj.put("userName", jsArr.toString());						
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 				
		Base.OBDApp.baseAct.httpQueueInstance.EnQueue(url, obj, 16);
		
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Group) {
			Group t = (Group) obj;
			return (name.equals(t.name));
		}
		return super.equals(obj);
	}

	public static Bitmap setGroupHead(Group grp){
		if(grp.memberList == null || grp.memberList.size() == 0)
			return null;
		List<MyBitmapEntity> mEntityList;
		int i = 0;
		int len = grp.memberList.size();
		len = len < 4 ? len : 4;
		
		Bitmap[] mBitmaps = new Bitmap[len];
		mEntityList = Base.getBitmapEntitys(len);
		Member mem;
		for(i = 0; i < len; i++){
			mem = grp.memberList.get(i);
			if(mem.headBitmap != null){				
				mBitmaps[i] = ThumbnailUtils.extractThumbnail(mem.headBitmap, (int) mEntityList
						.get(i).width, (int) mEntityList.get(i).height);				
			}			
			else{
				if(mem.name.equals(Base.loginUser) && Base.headbitmap != null){
					mBitmaps[i] = ThumbnailUtils.extractThumbnail(Base.headbitmap, (int) mEntityList
							.get(i).width, (int) mEntityList.get(i).height);
				}
				else
					mBitmaps[i] = ThumbnailUtils.extractThumbnail(BitmapUtil.getScaleBitmap(
							Base.OBDApp.getResources(), R.drawable.ic_launcher_df), (int) mEntityList
							.get(i).width, (int) mEntityList.get(i).height);
			}
			
		}
		return BitmapUtil.getCombineBitmaps(mEntityList,mBitmaps);			
	}
	
}
