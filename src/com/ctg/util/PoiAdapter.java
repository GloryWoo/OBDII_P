package com.ctg.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class PoiAdapter extends SimpleAdapter {
	private Base baseAct;

	private List<Map<String, Object>> mListItems;

	private LayoutInflater listContainer;
	
	
	private int resId;
	RelativeLayout addr_rela;
	RelativeLayout start_rela;
	TextView start_text;
	TextView poi_title;
	TextView poi_addr;
	ImageView num_img;
	 

    
	public PoiAdapter(Context context, List<Map<String, Object>> listItems, int resource, String[] from, int[] to) {
		super(context, listItems, resource, from, to);
		baseAct = (Base) context;
		listContainer = LayoutInflater.from(context);
		mListItems = listItems;
		resId = resource;

	}

	public void setList(ArrayList<Map<String, Object>> list){
		mListItems = list;
		this.notifyDataSetChanged();
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
		final int selectID = position;
		Map poi_map;
        View v;  
        final int mPosition = position;  
        v = super.getView(position, convertView, parent);  

        start_rela = (RelativeLayout) v.findViewById(R.id.poi_start_rela);
        start_text = (TextView) v.findViewById(R.id.poi_start);
        if(Base.baidu_v.isSelectAddrMode != 0){
        	//TextView textv = (TextView) v.findViewById(R.id.poi_start);
        	start_text.setText("选定");
        }
		addr_rela = (RelativeLayout) v.findViewById(R.id.poi_text_rela);
//		addr_rela.setOnClickListener(new View.OnClickListener(){
//			@Override
//			public void onClick(View v) {
//				// TODO Auto-generated method stub
//				int i = 0;				
//			}
//			
//		});
		
		start_text = (TextView) v.findViewById(R.id.poi_start);
		start_text.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Map<String, Object> map = mListItems.get(mPosition);
				LatLng latlon = (LatLng) map.get("latlon");
				String destName = (String) map.get("name");
				String addr = (String) map.get("address");
				NavitPoint nvpt = new NavitPoint(latlon.latitude, latlon.longitude, destName);
				if(Base.baidu_v.isSelectAddrMode == 1){
					Preference.getInstance(baseAct).setNaviPointHome(nvpt);
				}
				else if(Base.baidu_v.isSelectAddrMode == 2){					
					Preference.getInstance(baseAct).setNaviPointCmpy(nvpt);
				}
				else if(Base.baidu_v.isSelectAddrMode == 3){
					if(Base.baidu_v.gfenceState != 0){
						Base.baidu_v.getFencePoint(destName, addr, latlon);
					}
				}
				else if(Base.baidu_v.isSelectAddrMode == 4){
					Base.baidu_v.searchAddrDisplay(true, destName, latlon);
				}
				else{
			        BNaviPoint startPoint = new BNaviPoint(Base.baidu_v.mCurLongitude,Base.baidu_v.mCurLatitude,
			        		Base.baidu_v.curPoiName, BNaviPoint.CoordinateType.BD09_MC);//WGS84
			        BNaviPoint endPoint = new BNaviPoint(latlon.longitude,latlon.latitude,
			        		destName, BNaviPoint.CoordinateType.BD09_MC);
					
					BaiduNaviManager.getInstance().launchNavigator(baseAct,
			                startPoint,
			                endPoint,
			                RoutePlanParams.NE_RoutePlan_Mode.ROUTE_PLAN_MOD_MIN_TIME, 		 //算路方式
			                true, 									   		 //真实导航
			                BaiduNaviManager.STRATEGY_FORCE_ONLINE_PRIORITY, //在离线策略
			                new BaiduNaviManager.OnStartNavigationListener() {				 //跳转监听
	
			                    @Override
			                    public void onJumpToNavigator(Bundle configParams) {
			                        Intent intent = new Intent(baseAct, BNavigatorActivity.class);
			                        intent.putExtras(configParams);
			                        baseAct.startActivity(intent);
			                    }
	
			                    @Override
			                    public void onJumpToDownloader() {
			                    }
			                });	
				}
				Base.baidu_v.poiLstDlg.cancel();
			}
			
		});
		// 设置文字图片
		return v;
	}
	
}