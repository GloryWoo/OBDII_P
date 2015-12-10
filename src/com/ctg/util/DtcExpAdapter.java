package com.ctg.util;

import java.util.ArrayList;
import java.util.List;

import com.baidu.mapapi.map.offline.MKOLSearchRecord;
import com.baidu.mapapi.map.offline.MKOLUpdateElement;
import com.baidu.mapapi.map.offline.MKOfflineMap;
import com.baidu.mapapi.map.offline.MKOfflineMapListener;
import com.ctg.ui.Base;
import com.ctg.ui.R;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SectionIndexer;
import android.widget.TextView;
import android.widget.Toast;

public class DtcExpAdapter extends BaseExpandableListAdapter {

	public class GItemCont{
		String name;
		int status; //0 query; 1 got result OK; 2 got result warning
		ArrayList<CItemCont> cList;
		
		public GItemCont(){
			cList = new ArrayList<CItemCont>();
		}
		
		public GItemCont(String nameStr){
			name = nameStr;
			cList = new ArrayList<CItemCont>();
		}
	}
	
	public class CItemCont{
		String typeName; //故障码 ; other
		String code;
		String cont;		
	}
	
	public ArrayList<GItemCont> gList;
	private Context mContext;

	public static String grpListName[] = {"车身系统", "动力系统", "底盘系统", "通讯系统"};
	
	public void initGList(){
		if(gList != null)
			return;
		gList = new ArrayList<GItemCont>();
		GItemCont gItm;
		for(String str : grpListName){
			gItm = new GItemCont(str);
			gList.add(gItm);
		}
	}

	public void resetGList(){
		if(Base.dtcLst == null)
			return;
		for(int i = 0; i < 4; i++)
			gList.get(i).cList = Base.dtcLst[i];
		notifyDataSetChanged();
	}
	// private ArrayList<String> sections=new ArrayList<String>();
	// //这里是分组的名字也是在外面自定义，然后传进来就是，这和普通的Listview是一样的
	public DtcExpAdapter(Context context) {

		mContext = context;
		initGList();
		// this.setSections(list);
	}


	@Override
	public Object getChild(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		return childPosition;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub		
		GItemCont gItem = gList.get(groupPosition);
		if(gItem.cList == null)
			return convertView;
		CItemCont cItem = gItem.cList.get(childPosition);
		CViewHolder cvh = null;
		if(convertView == null){
			convertView = View.inflate(mContext, R.layout.dtc_list_item_c, null);
			cvh = new CViewHolder();
			cvh.typeName = (TextView) convertView.findViewById(R.id.c_dtc_type);
			cvh.code = (TextView) convertView.findViewById(R.id.c_dtc_code);
			cvh.cont = (TextView) convertView.findViewById(R.id.c_dtc_cont);
			convertView.setTag(cvh);
		}
		else{
			cvh = (CViewHolder) convertView.getTag();
		}
		cvh.typeName.setText(cItem.typeName);
		if(cItem.typeName.equals("故障码")){
			cvh.code.setVisibility(View.VISIBLE);
			cvh.code.setText(cItem.code);
		}
		else
			cvh.code.setVisibility(View.INVISIBLE);
		cvh.cont.setText(cItem.cont);
		return convertView;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		// TODO Auto-generated method stub
		GItemCont gItem = gList.get(groupPosition);
		if(gItem.cList == null)
			return 0;
		return gItem.cList.size();
	}

	@Override
	public Object getGroup(int groupPosition) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getGroupCount() {
		// TODO Auto-generated method stub
		return gList.size();
	}

	@Override
	public long getGroupId(int groupPosition) {
		// TODO Auto-generated method stub
		return groupPosition;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		GViewHolder vh = null;
		GItemCont gItem = gList.get(groupPosition);
		if(convertView == null){
			convertView = View.inflate(mContext, R.layout.dtc_list_item_g, null);
			vh = new GViewHolder();
			vh.name = (TextView) convertView.findViewById(R.id.dtc_title_g);
			vh.progress = (ProgressBar) convertView.findViewById(R.id.dtc_progress);
			vh.status = (ImageView) convertView.findViewById(R.id.car_part_status);									
			convertView.setTag(vh);
			
		}
		else{
			vh = (GViewHolder) convertView.getTag();
		}
		vh.name.setText(gItem.name);
		if(gItem.status == 0){
			vh.progress.setVisibility(View.VISIBLE);
			vh.status.setVisibility(View.INVISIBLE);
		}
		else{
			vh.progress.setVisibility(View.INVISIBLE);
			vh.status.setVisibility(View.VISIBLE);
			if(gItem.status == 1){
				vh.status.setImageResource(R.drawable.toggle);
			}
			else if(gItem.status == 2){
				vh.status.setImageResource(R.drawable.warn);
			}
				
		}
		return convertView;
	}
	
	public class CViewHolder {
		TextView typeName;
		TextView code;
		TextView cont;
	}

	public class GViewHolder {
		TextView name;
		ImageView status;
		ProgressBar progress;
	}


	@Override
	public boolean hasStableIds() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		return true;
	}

}
