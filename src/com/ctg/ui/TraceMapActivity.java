package com.ctg.ui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.map.offline.MKOfflineMap;
import com.baidu.mapapi.map.offline.MKOfflineMapListener;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.CoordinateConverter;
import com.ctg.trace.GPS;
import com.ctg.trace.StatisticsData;
import com.ctg.trace.TraceDataSource;
import com.ctg.trace.TraceDataSourceDelegate;
import com.ctg.util.DriveHabitAdapter;
import com.ctg.util.Util;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

public class TraceMapActivity extends Activity implements MKOfflineMapListener {
	private MapView mMapView;
	private BaiduMap mBaiduMap;
	private MKOfflineMap mOffline;
	private ArrayList<GPS> lsTracePt;
	private TraceDataSource trackView = null;
	private LatLng start = null;
	private LatLng end = null;
	private int id;
	private ArrayList<StatisticsData> lsTracePt_temp = null;


	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.trace_map);

		initView();
		judgeComeFrom();

	}

	@SuppressWarnings("unchecked")
	private void judgeComeFrom() {
		
			id = getIntent().getIntExtra("id", -1);
			lsTracePt_temp = (ArrayList<StatisticsData>) getIntent()
					.getSerializableExtra("list");

			trackView = new TraceDataSource(this);
			drawMap(trackView.getPtListData(lsTracePt_temp.get(id).getSid()));
	
	}

	protected void initOfflineMap() {
		// 初始化离线地图
		mOffline = new MKOfflineMap();
		mOffline.init(TraceMapActivity.this);
		mOffline.importOfflineData();
	}

	protected void initView() {
		// TODO Auto-generated method stub

		mMapView = (MapView) findViewById(R.id.trace_mapView);
		mBaiduMap = mMapView.getMap();

	}

	public void drawMap(List<GPS> lsTracePt) {
		List<LatLng> pts = new ArrayList<LatLng>();
//		List<LatLng> delList = new ArrayList<LatLng>();
		List<GPS> lsPt = new ArrayList<GPS>();
		start = null;
		end = null;

		lsPt = lsTracePt;
		if (lsPt == null) {
			Log.v("lsPt", "kong");
		} else if (lsPt.size() > 0) {
			start = new LatLng(lsPt.get(0).getLat(), lsPt.get(0)
					.getLon());
			end = new LatLng(lsPt.get(lsPt.size() - 1).getLat(),
					lsPt.get(lsPt.size() - 1).getLon());
		}

		for (int i = 0; i < lsPt.size(); i++) {
			pts.add(new LatLng(lsPt.get(i).getLat(), lsPt.get(i)
					.getLon()));
		}

		// delList = pts;

//		for (int i = 0; i < pts.size() - 1; i++) {
//			delList.add(pts.get(i));
//			delList.add(pts.get(i + 1));
//			delList.clear();
//		}
		OverlayOptions polylineOption = new PolylineOptions().points(pts).width(10).color(0xffff00ff);
		mBaiduMap.addOverlay(polylineOption);
		mBaiduMap.setMapStatus(MapStatusUpdateFactory.zoomTo(16f));
		mBaiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(start));
		if (start != null) {
			drawStartEnd(start, true);
		}
		if (end != null) {
			drawStartEnd(end, false);
		}
	}

	public void drawStartEnd(LatLng point, boolean startOrEnd) {
		
		BitmapDescriptor bitmap;
		if(startOrEnd)
			bitmap = BitmapDescriptorFactory.fromResource(R.drawable.icon_start);
		else
			bitmap = BitmapDescriptorFactory.fromResource(R.drawable.icon_end);

		OverlayOptions option = new MarkerOptions().position(point)
				.icon(bitmap);

		mBaiduMap.addOverlay(option);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if (mMapView != null) {
			mMapView.onResume();
		}
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		if (mMapView != null) {
			mMapView.onPause();
		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (mMapView != null) {
			mMapView.onDestroy();
		}
	}

	@Override
	public void onGetOfflineMapState(int arg0, int arg1) {
		// TODO Auto-generated method stub

	}
}