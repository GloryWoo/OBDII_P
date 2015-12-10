package com.ctg.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMap.OnMapClickListener;
import com.baidu.mapapi.map.BaiduMap.SnapshotReadyCallback;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.CoordinateConverter;
import com.baidu.navisdk.BNaviEngineManager.NaviEngineInitListener;
import com.baidu.navisdk.util.verify.BNKeyVerifyListener;
import com.ctg.trace.DateUtil;
import com.ctg.trace.GPS;
import com.ctg.trace.StatisticsData;
import com.ctg.trace.TraceDataSource;
import com.ctg.ui.DriveDetailActivity;
import com.ctg.ui.OBDApplication;
import com.ctg.ui.R;
import com.ctg.ui.TraceMapActivity;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.Legend.LegendPosition;
import com.github.mikephil.charting.data.ChartData;
import com.github.mikephil.charting.data.DataSet;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.PercentFormatter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class DriveHabitAdapter extends BaseAdapter {
	private Context mContext;
	private ArrayList<StatisticsData> lsUserTrace;
	private List<GPS> lsTracePt;
	private List<List<GPS>> lsTrace = new ArrayList<List<GPS>>();
	private List listDatas;
	private TraceDataSource trackView;
	private BaiduMap mBaiduMap;
	// private MapView mMapView;
	private LatLng start = null;
	private LatLng end = null;
	private Typeface tf;
	ColorTemplate mCt;

	// private final static String ACCESS_KEY = "18UzTFgyGvrzKRHqQau3DjXe";

	public DriveHabitAdapter(Context mContext,
			ArrayList<StatisticsData> queryTrace, TraceDataSource trackView,
			List listDatas) {
		this.mContext = mContext;
		this.lsUserTrace = queryTrace;
		// this.lsTracePt = lsTracePt;
		this.trackView = trackView;
		// filterPt();
		SDKInitializer.initialize(mContext.getApplicationContext());
		// mBaiduMap = mMapView.getMap();

		mCt = new ColorTemplate();
		tf = OBDApplication.mTf;//Typeface.createFromAsset(mContext.getAssets(),"OpenSans-Regular.ttf");
	}

	public int getCount() { // 都是默认的
		return lsUserTrace.size();
		// return habitList.size();
	}

	public Object getItem(int position) {
		return lsUserTrace.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHolder viewHolder = null;
		if (convertView == null) {
			viewHolder = new ViewHolder();
			convertView = LayoutInflater.from(mContext).inflate(
					R.layout.drive_habit_item, null);
//			viewHolder.linearLayout = (LinearLayout) convertView
//					.findViewById(R.id.habit_map_layout);
			viewHolder.caption = (TextView)convertView.findViewById(R.id.habit_end_time);
			viewHolder.where = (TextView)convertView.findViewById(R.id.habit_area);			
			 viewHolder.distance = (TextView) convertView
			 .findViewById(R.id.habit_distance_txt);
			 viewHolder.dura = (TextView) convertView
					 .findViewById(R.id.habit_time_txt);
			 viewHolder.speed = (TextView) convertView
			 .findViewById(R.id.habit_speed_txt);
			 viewHolder.idle_time = (TextView) convertView
					 .findViewById(R.id.habit_lazytime_txt);

			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		StatisticsData item = (StatisticsData) getItem(position);
		
		viewHolder.caption.setText(DateUtil.DateToDateStr(item.getStartTime()));	// get only month and date
		viewHolder.where.setText(item.getTraceLocation());
		viewHolder.distance.setText(Integer.toString(item.getDistance()));
		viewHolder.dura.setText(DateUtil.TimeInterval2Str(item.getTripDuration()));
		viewHolder.speed.setText(Float.toString(item.getAverageSpeed()));
		viewHolder.idle_time.setText(DateUtil.TimeInterval2Str(item.getIdleTime()));
		
		return convertView;
	}

	public void drawMap(List<GPS> lsTracePt) {
		mBaiduMap.clear();
		List<LatLng> pts = new ArrayList<LatLng>();
		List<LatLng> delList = new ArrayList<LatLng>();
		List<GPS> lsPt = new ArrayList<GPS>();
		start = null;
		end = null;

		lsPt = lsTracePt;
		if (lsPt == null) {
			Log.v("lsPt", "kong");
		} else if (lsPt.size() > 0) {
			start = Util.ConvertPt(new LatLng(lsPt.get(0).getLat(), lsPt.get(0)
					.getLon()));
			end = Util.ConvertPt(new LatLng(lsPt.get(lsPt.size() - 1).getLat(),
					lsPt.get(lsPt.size() - 1).getLon()));
		}

		for (int i = 0; i < lsPt.size(); i++) {
			pts.add(Util.ConvertPt(new LatLng(lsPt.get(i).getLat(), lsPt.get(i)
					.getLon())));
		}

		// delList = pts;

		for (int i = 0; i < pts.size() - 1; i++) {
			delList.add(pts.get(i));
			delList.add(pts.get(i + 1));
			OverlayOptions polylineOption = new PolylineOptions()
					.points(delList).width(10).color(0xffff00ff);
			mBaiduMap.addOverlay(polylineOption);
			delList.clear();
		}
		mBaiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(start));
		if (start != null) {
			drawStartEnd(start);
		}
		if (end != null) {
			drawStartEnd(end);
		}

	}

	public void drawStartEnd(LatLng point) {
		BitmapDescriptor bitmap = BitmapDescriptorFactory
				.fromResource(R.drawable.icon_marka);

		OverlayOptions option = new MarkerOptions().position(point)
				.icon(bitmap);

		mBaiduMap.addOverlay(option);
	}

	class ViewHolder {
		TextView speed, distance, start_time, caption, gas, rank, where, dura, idle_time;
		MapView mMapView;
		PieChart mChart;
		LinearLayout linearLayout;
		// PieChart pieChart;

	}

	public void setLlist(ArrayList<StatisticsData> lsUserTrace) {
		// TODO Auto-generated method stub
		this.lsUserTrace = lsUserTrace;
	}

}