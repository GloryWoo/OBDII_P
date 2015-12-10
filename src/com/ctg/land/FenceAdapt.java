package com.ctg.land;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.baidu.mapapi.map.MyLocationData;
import com.ctg.service.CarDataService;
import com.ctg.ui.Base;
import com.ctg.ui.R;
import com.ctg.util.MyGeoFenceCont;

public class FenceAdapt extends BaseAdapter {
	private Base baseAct;

	private List<MyGeoFenceCont> mListItems;

	private LayoutInflater listContainer;
	
	
	private int resId;
	RelativeLayout addr_rela;
	RelativeLayout start_rela;
	TextView poi_title;
	TextView poi_addr;
	ImageView num_img;
	public boolean checkMode; 
	//public HashMap<Integer, Boolean> isSelected; 
    //Bitmap headbitmp;
	
	public FenceAdapt(Context context, List<MyGeoFenceCont> listItems) {
		baseAct = (Base) context;
		listContainer = LayoutInflater.from(context);
		mListItems = listItems;
		//isSelected = new HashMap<Integer, Boolean>();
	}
	
	public void setList(){
		mListItems = CarDataService.fenceList;
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
		MyGeoFenceCont fence = (MyGeoFenceCont) getItem(position);

        if (convertView == null) {
        	vh = new ViewHolder();
        	if(Base.OBDApp.landScapeMode == 1)
	        	convertView = LayoutInflater.from(baseAct).inflate(
						R.layout.gfence_item, null);
        	else
	        	convertView = LayoutInflater.from(baseAct).inflate(
						R.layout.gfence_item_p, null);        	

        	vh.fenceName = (TextView) convertView.findViewById(R.id.gfence_name);
        	vh.fenceAddr = (TextView) convertView.findViewById(R.id.gfence_addr);
        	vh.fenceImgv = (ImageView) convertView.findViewById(R.id.gfence_head);
        	vh.detail = convertView.findViewById(R.id.gfence_detail);
        	vh.detail.setTag(fence);
        	convertView.setTag(vh);        	
        }
        else{
        	vh = (ViewHolder) convertView.getTag();
        }
        convertView.setOnTouchListener(new OnTouchListener(){

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				if(event.getAction() == MotionEvent.ACTION_DOWN){
					
				}
				else if(event.getAction() == MotionEvent.ACTION_MOVE)
				{
					
				}
				else if(event.getAction() == MotionEvent.ACTION_UP)
				{
					
				}
				return false;
			}
			
		});
        vh.fenceName.setText(fence.name);     
        vh.fenceAddr.setText(fence.address);

      
		return convertView;
	}
	
	public class ViewHolder{
		TextView fenceName;
		TextView fenceAddr;
		View detail;
		ImageView fenceImgv;
		//int posi;		
	}
	
}