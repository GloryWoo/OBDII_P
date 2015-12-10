package com.ctg.land;

import java.util.List;

import com.ctg.group.Group;
import com.ctg.group.GrpListAdapter.ViewHolder;
import com.ctg.group.Member;
import com.ctg.ui.Base;
import com.ctg.ui.R;
import com.ctg.util.Util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class GrpMapExpAdapter extends BaseExpandableListAdapter{

	Base baseAct;
	List<Group> grpLst;
	static Bitmap defaultHeadBit = null;	
	public int grpIdx;
	public int memIdx;
	ExpandableListView explv;
	public GrpMapExpAdapter(Context context, ExpandableListView lv, List<Group> lst){
		baseAct = (Base)context;
		grpLst = lst;
		grpIdx = 0;
		explv = lv;
		if(defaultHeadBit == null){
			defaultHeadBit = BitmapFactory.decodeResource(baseAct.getResources(), R.drawable.ic_launcher_df);
		}
		
	}
	
	@Override
	public int getGroupCount() {
		// TODO Auto-generated method stub		
		return grpLst.size();
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		// TODO Auto-generated method stub
		
		return grpLst.get(groupPosition).memberList.size();
	}

	@Override
	public Object getGroup(int groupPosition) {
		// TODO Auto-generated method stub
		return grpLst.get(groupPosition);
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		Group grp = (Group) getGroup(groupPosition);
		Member member = grp.memberList.get(childPosition);
		return member;
	}

	@Override
	public long getGroupId(int groupPosition) {
		// TODO Auto-generated method stub
		return groupPosition;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		return (groupPosition<<8) + childPosition;
	}

	@Override
	public boolean hasStableIds() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHolder vh;
		Group grp = (Group) getGroup(groupPosition);

		grpIdx = groupPosition;
        if (convertView == null) {
        	vh = new ViewHolder();
        	convertView = LayoutInflater.from(baseAct).inflate(
					R.layout.grp_map_item, null);
        	vh.head = (ImageView)convertView.findViewById(R.id.grp_map_item_header);
        	vh.name = (TextView) convertView.findViewById(R.id.grp_map_item_text);
        	vh.check = (CheckBox) convertView.findViewById(R.id.grp_map_item_check);
        	vh.rela = (RelativeLayout) convertView.findViewById(R.id.grp_map_rela);
        	vh.type = 0;
        	
        	convertView.setTag(vh);  
        	
        }
        else{
        	vh = (ViewHolder) convertView.getTag();
        }
        vh.check.setChecked(grp.checked);
        vh.check.setTag(groupPosition);
        vh.check.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				int grpPos = (Integer) v.getTag();
				int count = getChildrenCount(grpPos);
				int i = 0;
				Group grp = (Group) getGroup(grpPos);

				View childv;
				Member mem = null;
				grp.checked = !grp.checked;
				for(i = 0; i < count; i++){
					mem = grp.memberList.get(i);
					mem.checked = grp.checked;					
				}
				
				if(grp.checked){
					for(Group grpItm : grpLst){
						if(grpItm.equals(grp))
							continue;
						else if(grpItm.checked)
							grpItm.checked = false;    						
					}
				}
				//GrpMapExpAdapter.this.notifyDataSetInvalidated();
				GrpMapExpAdapter.this.notifyDataSetChanged();
			}
        	
        });

        
        if(grp.grpHead != null){
        	vh.head.setImageBitmap(grp.grpHead);
        }
        vh.name.setText(grp.name); 
		return convertView;
	}
	
	public int getExpChildViewPos(int groupPosition, int childPosition){
		int pos = 0;
		Group grp = null;
		for(int i = 0; i < groupPosition; i++){
			grp = grpLst.get(i);
			pos += grp.memberList.size();
		}
		pos += childPosition;
		return pos;
		//return explv.getExpandableListPosition(pos);
		
	}
	
	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHolder vh;
		Group grp = (Group) getGroup(groupPosition);
		Member member = grp.GetMemberList().get(childPosition);
		grpIdx = groupPosition;
		memIdx = childPosition;
        if (convertView == null) {
        	vh = new ViewHolder();
        	convertView = LayoutInflater.from(baseAct).inflate(
					R.layout.grp_map_child_item, null);
        	vh.head = (ImageView)convertView.findViewById(R.id.grp_map_mem_item_header);
        	vh.name = (TextView) convertView.findViewById(R.id.grp_map_mem_item_text);
        	vh.check = (CheckBox) convertView.findViewById(R.id.grp_map_mem_item_check);
        	
        	vh.type = 1;
        	convertView.setTag(vh);  
        	
        }
        else{
        	vh = (ViewHolder) convertView.getTag();
        }
        vh.name.setText(member.name);        
        vh.check.setChecked(grpLst.get(groupPosition).checked && member.checked);
        vh.check.setTag((groupPosition<<8) + childPosition);
        vh.check.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				// TODO Auto-generated method stub
				int pos = (Integer) buttonView.getTag();
				int grpPos = pos>>8;
				int childPos = pos&0xff;
				Group grp = (Group) getGroup(grpPos);
				Member mem = null;						
				mem = grp.memberList.get(childPos);
				mem.checked = isChecked;									
			}
        	
        });
        Bitmap bitProc;
        if(member.headBitmap != null){
        	bitProc = Util.getRoundedCornerImage(member.headBitmap);
        }
        else{
        	bitProc = Util.getRoundedCornerImage(defaultHeadBit);
        }
	    if(member.getIsOnline() == 0)
	    	bitProc = Util.setColorGrey(bitProc);
	    vh.head.setImageBitmap(bitProc);
		return convertView;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		return false;
	}

	class ViewHolder{
		ImageView head;
		TextView name;
		CheckBox check;
		RelativeLayout rela;
		int type;// 0 group 1 member
		
	}
}
