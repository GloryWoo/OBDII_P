package com.ctg.group;

import android.content.Context;
import android.graphics.Bitmap;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;

import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import com.baidu.mapapi.map.BaiduMap.SnapshotReadyCallback;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.ctg.ui.Base;
import com.ctg.ui.R;
/**
 * 
 ******************************************
 * @author 
 * @文件名称	:  ChatMsgAdapter.java
 * @创建时间	: 2013-1-27 下午02:33:16
 * @文件描述	: 消息数据填充起
 ******************************************
 */
public class ChatMsgAdapter extends BaseAdapter implements Runnable{

	public static interface IMsgViewType {
		int IMVT_COM_MSG = 0;
		int IMVT_TO_MSG = 1;
	}
	public static final int LOAD_MAPVIEW_READY = 100;
	
	LinkedList<Integer> mMapImgLoadQue;
	//MapView mMapV;
	private List<ChatMsgEntity> coll;
	ListView listview;
	private LayoutInflater mInflater;
	private Context context;
	Base baseAct;
	private static Thread runner;
	private static int curProcIdx;
	int lastSelect = -1;
	
	public ChatMsgAdapter(Context context, ListView lstv, List<ChatMsgEntity> coll) {
		this.coll = coll;
		mInflater = LayoutInflater.from(context);
		this.context = context;
		baseAct = (Base) context;
		listview = lstv;
		mMapImgLoadQue = new LinkedList<Integer>();
		//mMapV = (MapView) View.inflate(context, R.layout.mapview, null);//new MapView(context);
		//mMapV.setLayoutParams(new LayoutParams(320*baseAct.mDensityInt,240*baseAct.mDensityInt));		
	}

	public int getCount() {
		return coll.size();
	}

	public Object getItem(int position) {
		return coll.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public int getItemViewType(int position) {
		ChatMsgEntity entity = coll.get(position);

		if (entity.lOrR == 0) {
			return IMsgViewType.IMVT_COM_MSG;
		} else {
			return IMsgViewType.IMVT_TO_MSG;
		}

	}

	public int getViewTypeCount() {
		return 2;
	}

	
	public class SortComparator implements Comparator {  
		@Override
		public int compare(Object arg0, Object arg1) {
			// TODO Auto-generated method stub
			int l = (Integer) arg0;
			int r = (Integer) arg1;
			
			return (l-r);
		}  
	}
	public View getView(int position, View convertView, ViewGroup parent) {

		final ChatMsgEntity entity = coll.get(position);

		ViewHolder viewHolder = null;
		if (convertView == null) {
			if (getItemViewType(position)==IMsgViewType.IMVT_COM_MSG) {
				convertView = mInflater.inflate(
						R.layout.chat_item_left, null);
			} else {
				convertView = mInflater.inflate(
						R.layout.chat_item_right, null);
			}

			viewHolder = new ViewHolder();
//			viewHolder.rela = (RelativeLayout) convertView
//					.findViewById(R.id.mp_rela);
			viewHolder.tvSendTime = (TextView) convertView
					.findViewById(R.id.tv_sendtime);
			viewHolder.tvContent = (TextView) convertView
					.findViewById(R.id.tv_chatcontent);			
//			viewHolder.mapImg = (ImageView) convertView
//					.findViewById(R.id.mp_loc_track);
			viewHolder.mapview = (MapView) convertView
					.findViewById(R.id.chat_mapView);
			viewHolder.idx = position;
//			viewHolder.proBar = (ProgressBar) convertView.findViewById(R.id.mp_load_progress);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		if(viewHolder.mapview != null)
			viewHolder.mapview.setVisibility(View.GONE);
		viewHolder.tvContent.setTag(position);
		viewHolder.tvSendTime.setText(entity.date);
		if(entity.msgType == ChatMsgEntity.CHAT_MSG_TEXT){
			viewHolder.tvContent.setText(entity.text);
		}
		else if(entity.msgType == ChatMsgEntity.CHAT_MSG_LOCATE){
			if(entity.lOrR == 0){
				viewHolder.tvContent.setText(entity.name+"发布了位置："+
						"(" + entity.latlon_loc.latitude + "," + entity.latlon_loc.longitude+")");
//				if(!mMapImgLoadQue.contains(position)&& entity.loadState != 2){
//					mMapImgLoadQue.add(position);
//					Collections.sort(mMapImgLoadQue,new SortComparator()); 
//					viewHolder.rela.setVisibility(View.VISIBLE);	
//					viewHolder.mapview.setVisibility(View.VISIBLE);
//					BitmapDescriptor bitmap = BitmapDescriptorFactory
//							.fromResource(R.drawable.icon_marka);
//					OverlayOptions option = new MarkerOptions().position(entity.latlon_loc)
//							.icon(bitmap);
//		
//					viewHolder.mapview.getMap().addOverlay(option);
//					final ViewHolder localViewHolder = viewHolder;
//					viewHolder.mapview.getMap().snapshot(new SnapshotReadyCallback(){
//						@Override
//						public void onSnapshotReady(Bitmap arg0) {
//							// TODO Auto-generated method stub							
////							View itemV = listview.getChildAt(curProcIdx);
////							ViewHolder viewHolder = (ViewHolder) itemV.getTag();																							
//							localViewHolder.mapview.getMap().clear();
//							localViewHolder.mapview.setVisibility(View.GONE);
//							localViewHolder.mapImg.setImageBitmap(arg0);
//							localViewHolder.mapImg.setVisibility(View.VISIBLE);
//							//localViewHolder.proBar.setVisibility(View.GONE);
//							entity.loadState = 2;
//						}				
//					});
//					//startThread();
//				}
			}
			else{
				String editStr = "我发布了位置：" + baseAct.baidu_v.curPoiName;
				editStr += "(" + Base.baidu_v.mCurLatitude +"," + Base.baidu_v.mCurLongitude + ")";
				viewHolder.tvContent.setText(editStr);
			}			
		}
		else if(entity.msgType == ChatMsgEntity.CHAT_MSG_TRACK){
			if(entity.lOrR == 0){
				viewHolder.tvContent.setText(entity.name+"发布了轨迹：");
//				if(!mMapImgLoadQue.contains(position) && entity.loadState != 2){
//					mMapImgLoadQue.add(position);
////					Collections.sort(mMapImgLoadQue,new SortComparator()); 
//					//startThread();
//				}
			}
			else{
				String editStr = "我发布了轨迹：" + baseAct.baidu_v.curPoiName;
				editStr += "(" + Base.baidu_v.mCurLatitude +"," + Base.baidu_v.mCurLongitude + ")";
				viewHolder.tvContent.setText(editStr);
			}
		}

		return convertView;
	}

	public class ViewHolder {
		public RelativeLayout rela;
		public TextView tvSendTime;
		public TextView tvContent;
		public ImageView mapImg;
		public ProgressBar proBar;
		public MapView mapview;
		public int idx;
//		public int msgType;
//		public int lOrR;
	}
	
	public synchronized void startThread() {
		if (runner == null) {
			runner = new Thread(this);
			runner.start();
		}
	}

	public void stopHttp() {
		if (runner != null) {
			runner.stop();
			runner = null;
		}
	}

	public synchronized void stopThread() {
		if (runner != null) {
			Thread moribund = runner;
			runner = null;
			moribund.interrupt();
		}
	}
	
	public Handler msgHandler = new Handler(){
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub			
			switch(msg.what){
				case LOAD_MAPVIEW_READY:
					View itemV = listview.getChildAt(curProcIdx);
					ViewHolder viewHolder = (ViewHolder) itemV.getTag();				
					viewHolder.mapview.getMap().snapshot(new SnapshotReadyCallback(){
						@Override
						public void onSnapshotReady(Bitmap arg0) {
							// TODO Auto-generated method stub							
							View itemV = listview.getChildAt(curProcIdx);
							ViewHolder viewHolder = (ViewHolder) itemV.getTag();																							
							viewHolder.mapview.getMap().clear();
							viewHolder.mapview.setVisibility(View.GONE);
							viewHolder.mapImg.setImageBitmap(arg0);
							viewHolder.mapImg.setVisibility(View.VISIBLE);
							viewHolder.proBar.setVisibility(View.GONE);
							coll.get(curProcIdx).loadState = 2;
							
							
							if(runner != null){
								runner.notify();
							}
						}				
					});
					break;
				default:
					break;
			}
		}
	};
	
	@Override
	public synchronized void run() {
		// TODO Auto-generated method stub
		int idx = 0;
		
		while(!mMapImgLoadQue.isEmpty()){
			curProcIdx = mMapImgLoadQue.pollLast();
			ChatMsgEntity entity = coll.get(curProcIdx);
			
			View itemV = listview.getChildAt(curProcIdx);
			ViewHolder viewHolder = (ViewHolder) itemV.getTag();
			//MapView mMapV = (MapView) itemV.findViewById(R.id.chat_mapView);
			viewHolder.mapview.setVisibility(View.VISIBLE);
			if(entity.msgType == ChatMsgEntity.CHAT_MSG_LOCATE){
				BitmapDescriptor bitmap = BitmapDescriptorFactory
						.fromResource(R.drawable.icon_marka);
	
				OverlayOptions option = new MarkerOptions().position(entity.latlon_loc)
						.icon(bitmap);
	
				viewHolder.mapview.getMap().addOverlay(option);
			}
			else if(entity.msgType == ChatMsgEntity.CHAT_MSG_TRACK){
				OverlayOptions polylineOption = new PolylineOptions().points(
						entity.latlon_track).color(0xFF000000);
				viewHolder.mapview.getMap().addOverlay(polylineOption);
			}
			msgHandler.obtainMessage(LOAD_MAPVIEW_READY).sendToTarget();
			
			try {
				wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		runner = null;
		
	}


}
