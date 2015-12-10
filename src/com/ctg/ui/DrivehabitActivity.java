package com.ctg.ui;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMap.OnMapClickListener;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.mapapi.utils.DistanceUtil;
import com.ctg.crash.LogRecord;
import com.ctg.trace.GPS;
import com.ctg.trace.StatisticsData;
import com.ctg.trace.TraceDataSource;
import com.ctg.trace.TraceDataSourceDelegate;
import com.ctg.util.DriveHabitAdapter;
import com.ctg.util.Preference;
import com.ctg.widget.XListView;
import com.ctg.widget.XListView.IXListViewListener;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.Legend.LegendPosition;
import com.github.mikephil.charting.data.DataSet;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.PercentFormatter;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

public class DrivehabitActivity extends Activity implements IXListViewListener,
		OnGetGeoCoderResultListener, TraceDataSourceDelegate, OnClickListener, XListView.OnScrollXListView {
	final private String TAG = "DrivehabitActivity ";
	public BaiduMap mBaiduMap;
	public MapView mMapView;
	ImageView imageView, habit_arrow;
	public PieChart piechart;
	public LinearLayout mapLayout;
	private int lastPosition = -1;
	private int position;// 点击地图跳转的地图position
	private Long traceId;
	private GeoCoder mSearch;
	private int geo_req_counter = 0;

	boolean mIsEngineInitSuccess;
	private ImageView btn_back;
	Button btn_yes, btn_ago, btn_today;
	private ImageView scroll_line1, scroll_line2, scroll_line3;
	private XListView xListView; // 下拉刷新 上拉加载更多
	private DriveHabitAdapter adapter;
	private Handler mHandler;

	private List<StatisticsData> lsUserTrace = null;
	private ArrayList<StatisticsData> queryTrace = null;// 存放查询到的数据
	private TraceDataSource trackView = null;

	static final String DATABASE_NAME = "trace.db";
	static final String TRACE_TABLE = "trace_table";
	static final String POINT_TABLE = "point_table";
	static final int INIT_BDMAP_LISTENER = 4;

	private LatLng start = null;
	private LatLng end = null;
	private Typeface tf;
	ColorTemplate mCt;
	int curTab;
	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			// super.handleMessage(msg);
			switch (msg.what) {
			case TraceDataSource.RET_UPDATE_COMPETE:
				lsUserTrace = trackView.getListData();
//				queryTrace = getTodayDateList();
//				adapter.setLlist(queryTrace);
//				adapter.notifyDataSetChanged();
//				changeTabState(3);
//				xListView.stopRefresh();// 完成
//				Toast.makeText(DrivehabitActivity.this, "有新的行程哦", Toast.LENGTH_SHORT).show();							
				
				for(int i = 0; i<lsUserTrace.size(); i++){
					if(lsUserTrace.get(i).getTraceLocation().equals("")){
						long sid = lsUserTrace.get(i).getSid();
						GPS start = trackView.getPtListData(sid).get(0);
						geo_req_counter++;
						mSearch.reverseGeoCode(new ReverseGeoCodeOption().location(new LatLng(start.getLat(), start.getLon())));
					}
				}
				break;
			case TraceDataSource.RET_UPDATE_ALL:
				break;
			case TraceDataSource.RET_UPDATE_FAIL:
				Toast.makeText(DrivehabitActivity.this, "行程下载失败", Toast.LENGTH_SHORT).show();
				xListView.stopRefresh();// 完成
				break;
			case TraceDataSource.RET_UPDATE_NO_MORE:
				xListView.stopRefresh();// 完成
				Toast.makeText(DrivehabitActivity.this, "没发现新的行程哦", Toast.LENGTH_SHORT).show();
//				mSearch.reverseGeoCode(new ReverseGeoCodeOption().location(new LatLng(31.174225, 121.395815)));
				break;
			case INIT_BDMAP_LISTENER:// handle click event on map
//				createBDMap(currentView);
//				drawMap(trackView.getPtListData(queryTrace.get(position-1)
//						.getSid()));
				break;
			}

		}

	};

	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		// 在使用SDK各组件之前初始化context信息，传入ApplicationContext
		super.onCreate(arg0);
		SDKInitializer.initialize(getApplicationContext());
		requestWindowFeature(Window.FEATURE_NO_TITLE);// 去掉标题栏
		setContentView(R.layout.drive_habit);
		trackView = new TraceDataSource(this);
		trackView.traceDataSourceDelegate = this;

		Base.OBDApp.driveHabit = this;
		initData();
		getTodayDateList();
		initView();
		initListener();
		mCt = new ColorTemplate();
		tf = Typeface.createFromAsset(this.getAssets(),
				"OpenSans-Regular.ttf");
		curTab = 3;
	}

	private void initData() {
		// TODO Auto-generated method stub
		// trackView.updateTrace();
		lsUserTrace = trackView.getListData();
		queryTrace = new ArrayList<StatisticsData>();

		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
	}

	/**
	 * query trace according to startTime and endTime
	 * 
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	public ArrayList<StatisticsData> queryTrace(Date startTime, Date endTime) {
		queryTrace.clear();	
		int len = lsUserTrace.size();
		for (int i = len-1; i >= 0 ; i--) {
			if (lsUserTrace.get(i).getStartTime().after(startTime)
					&& lsUserTrace.get(i).getEndTime().before(endTime)) {
				queryTrace.add(lsUserTrace.get(i));

			}
		}
		// trackView.getPtListData(queryTrace.get)
		return queryTrace;
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		
		super.onStop();
	}

	private void initView() {
		// TODO Auto-generated method stub

		btn_back = (ImageView) findViewById(R.id.habit_back);
		btn_today = (Button) findViewById(R.id.habit_today);
		btn_ago = (Button) findViewById(R.id.habit_ago);
		btn_yes = (Button) findViewById(R.id.habit_yes);
		scroll_line1 = (ImageView) findViewById(R.id.habit_scroll_line1);
		scroll_line2 = (ImageView) findViewById(R.id.habit_scroll_line2);
		scroll_line3 = (ImageView) findViewById(R.id.habit_scroll_line3);
		habit_arrow = (ImageView) findViewById(R.id.habit_arrow);

		
		mHandler = new Handler();
		xListView = (XListView) findViewById(R.id.habit_xlistview);

		xListView.setPullRefreshEnable(true);// 设置下拉刷新
		xListView.setXListViewListener(this);// 设置监听事件，重写两个方法
		xListView.setPullLoadEnable(false);// 设置上拉刷新
		xListView.setOnScrollXListView(this);
		adapter = new DriveHabitAdapter(this, queryTrace, trackView, null);
		xListView.setAdapter(adapter);
		xListView.setDivider(null);
		mapLayout = (LinearLayout) View.inflate(this, R.layout.map_chart, null);
		mMapView = 	(MapView) mapLayout.findViewById(R.id.drvhabit_mapView);
		mBaiduMap = mMapView.getMap();
		piechart = (PieChart) mapLayout.findViewById(R.id.drive_chart);
		initPieChart();
		
		if(queryTrace == null || queryTrace.size() == 0){
			if(!Base.OBDApp.driveHabitDragDownNotice){
				Toast.makeText(this, "下拉屏幕可以下载行程哦", Toast.LENGTH_SHORT).show();
				Base.OBDApp.driveHabitDragDownNotice = true;
			}
		}

	}



	void initPieChart(){
		piechart.setDescription("");
		piechart.setHoleRadius(30f);
		piechart.setTransparentCircleRadius(0f);
		piechart.setCenterTextSize(18f);
		// piechart.setDrawXValues(true);
		piechart.setUsePercentValues(true);
		piechart.setDrawHoleEnabled(true);
		piechart.setHoleColorTransparent(true);

		piechart.setTransparentCircleColor(Color.WHITE);

		piechart.setHoleRadius(50f);
		piechart.setTransparentCircleRadius(53f);

		piechart.setDrawCenterText(true);
		// piechart.setCenterText("MPAndroidChart\nby Philipp Jahoda");
		piechart.setRotationAngle(0);
		// enable rotation of the chart by touch
		piechart.setRotationEnabled(true);
		// piechart.setDrawHoleEnabled(false);
		piechart.setTouchEnabled(false);
		// piechart.animateY(1500, Easing.EasingOption.EaseInOutQuad);
		// mChart.spin(2000, 0, 360);
	}
	private void initListener() {
		btn_back.setOnClickListener(this);
		btn_yes.setOnClickListener(this);
		btn_ago.setOnClickListener(this);
		btn_today.setOnClickListener(this);
		mBaiduMap.setOnMapClickListener(new OnMapClickListener() {

			@Override
			public boolean onMapPoiClick(MapPoi arg0) {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public void onMapClick(LatLng arg0) {
				// TODO Auto-generated method stub		
				LogRecord.SaveLogInfo2File(Base.OperateInfo, TAG+"map click");
				traceId = queryTrace.get(position-1).getSid();
				Intent intent = new Intent(DrivehabitActivity.this,
						DriveDetailActivity.class);
				intent.putExtra("id", position-1);
				intent.putExtra("traceId", traceId);
//				intent.putExtra("time", queryTrace.get(position-1).getStartTime());
				intent.putExtra("list", queryTrace);
				intent.putExtra("statistics", queryTrace.get(position-1));
//				intent.putExtra("nyte", queryTrace.get(position-1).getNightTime());
//				intent.putExtra("spdy", queryTrace.get(position-1).getSpeedingTime());
//				intent.putExtra("peak", queryTrace.get(position-1).getRushTime());
//				intent.putExtra("idle", queryTrace.get(position-1).getIdleTime());
//				intent.putExtra("obd", isOBDConnected(queryTrace.get(position-1)));
//				int s1 = calculateSafetyScore(queryTrace.get(position-1));
//				int s2 = calculateEconomyScore(queryTrace.get(position-1));
//				int s3 = calculateComfortScore(queryTrace.get(position-1));
//				intent.putExtra("safety", s1);
//				intent.putExtra("economy", s2);
//				intent.putExtra("comfort", s3);
//				intent.putExtra("overall", (s1 + s2 + s3) / 3);
				
				// intent.putExtra("trace", lsUserTrace.get(position));
//			    GetDriveBehavior getDriveBehavior = new GetDriveBehavior();
//			    getDriveBehavior.downloadDriveBehavior(DrivehabitActivity.this, "2015-03-20 13:43:59.324", "2015-03-20 13:54:05.888");
				
				DrivehabitActivity.this.startActivity(intent);
				lastPosition = -1;
				LinearLayout existContainer = (LinearLayout) mapLayout.getParent();
				existContainer.removeView(mapLayout);	
				ImageView lastArrow = (ImageView)existContainer.findViewById(R.id.habit_arrow);
				lastArrow.setImageResource(R.drawable.arrow_down_gray);
				mMapView.onPause();
				System.gc();
//				DrivehabitActivity.this.finish();

			}

		});
		xListView.setOnItemClickListener(new OnItemClickListener() {

			// keep only one map when click event happended
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				
				StatisticsData trace = queryTrace.get(position-1);
				ImageView curArrow = null, lastArrow = null;
				curArrow = (ImageView)view.findViewById(R.id.habit_arrow);
				
				LogRecord.SaveLogInfo2File(Base.OperateInfo, TAG+"item click");
				if(lastPosition == -1){					
					lastPosition = position;
					((LinearLayout)view).addView(mapLayout);
					mMapView.onResume();
					drawMap(trackView.getPtListData(queryTrace.get(position-1).getSid()));
					drawPieChart(trace.get0to60(), trace.get60to90(), trace.get90to120(), trace.get120above());
					curArrow.setImageResource(R.drawable.arrow_up_gray);
				}
				else if(position == lastPosition){
					lastPosition = -1;
					((LinearLayout)view).removeView(mapLayout);					
					mMapView.onPause();
					curArrow.setImageResource(R.drawable.arrow_down_gray);
				}
				else{//click another one while maplayout visible
					lastPosition = position;
					LinearLayout existContainer = (LinearLayout) mapLayout.getParent();
					existContainer.removeView(mapLayout);
					((LinearLayout)view).addView(mapLayout);					
					mMapView.onResume();
					drawMap(trackView.getPtListData(queryTrace.get(position-1).getSid()));
					drawPieChart(trace.get0to60(), trace.get60to90(), trace.get90to120(), trace.get120above());
					curArrow.setImageResource(R.drawable.arrow_up_gray);
					lastArrow = (ImageView)existContainer.findViewById(R.id.habit_arrow);
					lastArrow.setImageResource(R.drawable.arrow_down_gray);
				}
				DrivehabitActivity.this.position = position;
				// TODO Auto-generated method stub

			}

		});
		
		// reverse geocode listener
		mSearch = GeoCoder.newInstance();
		mSearch.setOnGetGeoCodeResultListener(this);
	}
	
	private void drawPieChart(long ti60, long ti90, long ti120, long ti120p){
		ArrayList<Entry> yVals = new ArrayList<Entry>();
		List<String> xVals = new ArrayList<String>();
		String [] labels = {"0-60 km/h", "60-90 km/h", "90-120 km/h", "> 120 km/h"};
		
		int non_zeros = 0;
		long [] non_zero_ti = new long[4];
		if(ti60 != 0){
			non_zero_ti[non_zeros] = ti60;
			non_zeros++;
		}
		
		if(ti90 != 0){
			non_zero_ti[non_zeros] = ti90;
			non_zeros++;
		}
		
		if(ti120 != 0){
			non_zero_ti[non_zeros] = ti120;
			non_zeros++;
		}
		
		if(ti120p != 0){
			non_zero_ti[non_zeros] = ti120;
			non_zeros++;
		}
		
		if(non_zeros == 0){
			piechart.setCenterTextSize(10f);
			piechart.setCenterTextColor(Color.BLACK);
			piechart.setCenterText("囧...没有有效的车速信息。");
		}
		else{
			long sum = ti60 + ti90 + ti120 + ti120p;
			
			for(int i=0; i<non_zeros; i++){
				yVals.add(new Entry((float) non_zero_ti[i] / sum, 0));
				xVals.add(labels[i]);
			}
			
			PieDataSet dataSet = new PieDataSet(yVals, "");
			
			ArrayList<Integer> colors = new ArrayList<Integer>();

			int[] colors1 = {Color.rgb(75, 171, 226), Color.rgb(242, 181, 36), Color.rgb(81, 195, 78), Color.rgb(249, 121, 87)};		
			
			for (int i=0; i<non_zeros; i++){
				colors.add(colors1[i]);
			}				

			colors.add(ColorTemplate.getHoloBlue());

			dataSet.setColors(colors);

			ArrayList<DataSet> dataSets = new ArrayList<DataSet>();
			dataSets.add(dataSet);
			PieData data = new PieData(xVals, dataSet);

			data.setValueFormatter(new PercentFormatter());
			data.setValueTextSize(11f);
			data.setValueTextColor(Color.BLACK);
			data.setValueTypeface(tf);
			piechart.setData(data);
			
			Legend l = piechart.getLegend();
			l.setPosition(LegendPosition.BELOW_CHART_CENTER);
		}
	}

	public void drawMap(List<GPS> lsTracePt) {
		mBaiduMap.clear();
		List<LatLng> pts = new ArrayList<LatLng>();
//		List<LatLng> delList = new ArrayList<LatLng>();
		List<GPS> lsPt = new ArrayList<GPS>();
		start = null;
		end = null;

		lsPt = lsTracePt;
		if (lsPt == null) {
			Log.v("lsPt", "kong");
		} else if (lsPt.size() > 0) {
			start = new LatLng(lsPt.get(0).getLat(), lsPt.get(0).getLon());
			end = new LatLng(lsPt.get(lsPt.size() - 1).getLat(), lsPt.get(lsPt.size() - 1).getLon());
		}

		for (int i = 0; i < lsPt.size(); i++) {
			pts.add(new LatLng(lsPt.get(i).getLat(), lsPt.get(i).getLon()));
		}

		if(pts.size() > 1){
			OverlayOptions polylineOption = new PolylineOptions().points(pts).width(6).color(0xffff00ff);
			mBaiduMap.addOverlay(polylineOption);
		}
		
		//delList.clear();
		mBaiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(start));
		mBaiduMap.setMapStatus(MapStatusUpdateFactory.zoomTo(15f));
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
//			Message msg = handler.obtainMessage();
//			msg.sendToTarget();
		}

	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (mMapView != null) {
			mMapView.onDestroy();
//			Message msg = handler.obtainMessage();
//			msg.sendToTarget();
		}
		Base.OBDApp.driveHabit = null;
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		if (mMapView != null) {
			mMapView.onPause();
			// Message msg = handler.obtainMessage();
			// msg.sendToTarget();
		}
	}

	private ArrayList<StatisticsData> getTodayDateList() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 HH时mm分");

		Calendar today = Calendar.getInstance(); // 今天
		Calendar current = Calendar.getInstance();

		today.set(Calendar.YEAR, current.get(Calendar.YEAR));
		today.set(Calendar.MONTH, current.get(Calendar.MONTH));
		today.set(Calendar.DAY_OF_MONTH, current.get(Calendar.DAY_OF_MONTH));
		// Calendar.HOUR——12小时制的小时数 Calendar.HOUR_OF_DAY——24小时制的小时数
		today.set(Calendar.HOUR_OF_DAY, 0);
		today.set(Calendar.MINUTE, 0);
		today.set(Calendar.SECOND, 0);

		Date startTime = today.getTime();
		Date endTime = new Date();
		Log.v("time", sdf.format(startTime) + "--" + sdf.format(endTime));

		return queryTrace(startTime, endTime);
	}

	private ArrayList<StatisticsData> getYesDateList() {
		new SimpleDateFormat("yyyy年MM月dd日 HH时mm分");

		Calendar today = Calendar.getInstance(); // 今天
		Calendar yesterday = Calendar.getInstance(); // 昨天
		Calendar current = Calendar.getInstance();

		today.set(Calendar.YEAR, current.get(Calendar.YEAR));
		today.set(Calendar.MONTH, current.get(Calendar.MONTH));
		today.set(Calendar.DAY_OF_MONTH, current.get(Calendar.DAY_OF_MONTH));
		// Calendar.HOUR——12小时制的小时数 Calendar.HOUR_OF_DAY——24小时制的小时数
		today.set(Calendar.HOUR_OF_DAY, 0);
		today.set(Calendar.MINUTE, 0);
		today.set(Calendar.SECOND, 0);

		yesterday.set(Calendar.YEAR, current.get(Calendar.YEAR));
		yesterday.set(Calendar.MONTH, current.get(Calendar.MONTH));
		yesterday.set(Calendar.DAY_OF_MONTH,
				current.get(Calendar.DAY_OF_MONTH) - 1);
		yesterday.set(Calendar.HOUR_OF_DAY, 0);
		yesterday.set(Calendar.MINUTE, 0);
		yesterday.set(Calendar.SECOND, 0);

		Date startTime = yesterday.getTime();
		Date endTime = today.getTime();
		Log.v("time", startTime.toString() + "--" + endTime.toString());
		return queryTrace(startTime, endTime);

	}

	private ArrayList<StatisticsData> getLongAgoDateList() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		// Calendar yesterday = Calendar.getInstance(); // 昨天
		Calendar beforeYesterday = Calendar.getInstance(); // 前天
		Calendar current = Calendar.getInstance();

		beforeYesterday.set(Calendar.YEAR, current.get(Calendar.YEAR));
		beforeYesterday.set(Calendar.MONTH, current.get(Calendar.MONTH));
		beforeYesterday.set(Calendar.DAY_OF_MONTH, current.get(Calendar.DAY_OF_MONTH) - 1);
		beforeYesterday.set(Calendar.HOUR_OF_DAY, 0);
		beforeYesterday.set(Calendar.MINUTE, 0);
		beforeYesterday.set(Calendar.SECOND, 0);

		Date startTime = null;
		try {

			startTime = sdf.parse("2000-01-01 00:00:00");
//			endTime = sdf.parse("2015-06-09 00:00:00");
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		};
		Log.v("time", sdf.format(startTime) + "--" + sdf.format(beforeYesterday.getTime()));

		return queryTrace(startTime, beforeYesterday.getTime());
	}
	
	private ArrayList<StatisticsData> getAllTimeList(){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date startTime = null, endTime = null;
		try {

			startTime = sdf.parse("2000-01-01 00:00:00");
			endTime = new Date();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		};
		
		return queryTrace(startTime, endTime);		
	}

	void closeIfMapChartExist(){
		lastPosition = -1;				
		mMapView.onPause();
		if(mapLayout != null){
			LinearLayout existContainer = (LinearLayout) mapLayout.getParent();
			if(existContainer != null){
				existContainer.removeView(mapLayout);
				ImageView lastArrow = (ImageView)existContainer.findViewById(R.id.habit_arrow);
				if(lastArrow != null)
					lastArrow.setImageResource(R.drawable.arrow_down_gray);
			}
		}
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.habit_back:
			this.finish();
			break;
		case R.id.habit_ago:
			if(curTab == 1)
				return;
			closeIfMapChartExist();
			queryTrace.clear();
			queryTrace = getLongAgoDateList();
			adapter.setLlist(queryTrace);
			adapter.notifyDataSetChanged();
			changeTabState(1);
//			Toast.makeText(DrivehabitActivity.this, "更早", 1).show();
			break;
		case R.id.habit_yes:
			if(curTab == 2)
				return;
			closeIfMapChartExist();
			queryTrace.clear();
			queryTrace = getYesDateList();
			adapter.setLlist(queryTrace);
			adapter.notifyDataSetChanged();
			changeTabState(2);
//			Toast.makeText(DrivehabitActivity.this, "昨天", 1).show();
			break;
		case R.id.habit_today:
			if(curTab == 3)
				return;
			closeIfMapChartExist();
			queryTrace.clear();
			queryTrace = getTodayDateList();
			adapter.setLlist(queryTrace);
			adapter.notifyDataSetChanged();
			changeTabState(3);
//			Toast.makeText(DrivehabitActivity.this, "今天", 1).show();
			break;
		}
	}

	private void changeTabState(int i) {
		curTab = i;
		switch (i) {
		case 1:
			scroll_line1.setVisibility(View.VISIBLE);
			scroll_line2.setVisibility(View.INVISIBLE);
			scroll_line3.setVisibility(View.INVISIBLE);
			break;
		case 2:
			scroll_line1.setVisibility(View.INVISIBLE);
			scroll_line2.setVisibility(View.VISIBLE);
			scroll_line3.setVisibility(View.INVISIBLE);
			break;
		case 3:
			scroll_line1.setVisibility(View.INVISIBLE);
			scroll_line2.setVisibility(View.INVISIBLE);
			scroll_line3.setVisibility(View.VISIBLE);
			break;
		default:
			break;
		}
	}
	
	private void stopRefresh(){
		queryTrace = getTodayDateList();
		adapter.setLlist(queryTrace);
		adapter.notifyDataSetChanged();
		changeTabState(3);
		xListView.stopRefresh();// 完成
		Toast.makeText(DrivehabitActivity.this, "有新的行程哦", Toast.LENGTH_SHORT).show();
	}

	

	@Override
	public void updateResult(int ret) {
		// TODO Auto-generated method stub
		if(Base.OBDApp.driveHabit == null)
			return;
		if (ret == TraceDataSource.RET_UPDATE_COMPETE) {			
			handler.obtainMessage(TraceDataSource.RET_UPDATE_COMPETE).sendToTarget();
			Log.v("update", "success");
		} 
		else if(ret == TraceDataSource.RET_UPDATE_NO_MORE){
			handler.obtainMessage(TraceDataSource.RET_UPDATE_NO_MORE).sendToTarget();
			Log.v("update", "success");
		}		
		else {
			handler.obtainMessage(TraceDataSource.RET_UPDATE_FAIL).sendToTarget();
			Log.v("update", "error");
		}
	}

	@Override
	public void onGetGeoCodeResult(GeoCodeResult arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
		geo_req_counter--;
		
		if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
			return;
		}		
		
		for(int i = 0; i<lsUserTrace.size(); i++){
			if(lsUserTrace.get(i).getTraceLocation().equals("")){
				long sid = lsUserTrace.get(i).getSid();
				GPS start = trackView.getPtListData(sid).get(0);
				double dist = DistanceUtil.getDistance(result.getLocation(), new LatLng(start.getLat(), start.getLon()));
				if(dist < 100)	{ // reverse geocode resolution assumed < 100m???
					String add_street = result.getAddressDetail().street;
					lsUserTrace.get(i).setTraceLocation(add_street);
					trackView.addTraceLocation(sid, add_street);	// update Database
					break;
				}
			}
		}
		
		if(geo_req_counter == 0){
			for(int i = 0; i<lsUserTrace.size(); i++){
				if(lsUserTrace.get(i).getTraceLocation().equals("")){
					long sid = lsUserTrace.get(i).getSid();
					lsUserTrace.get(i).setTraceLocation("未知地址");
					trackView.addTraceLocation(sid, "未知地址");	// update Database
				}
			}
			stopRefresh();
		}
	}

	@Override
	public void onRefresh() {
		// TODO Auto-generated method stub
		Toast.makeText(DrivehabitActivity.this, "下拉刷新", 0).show();
		Preference.getInstance(Base.OBDApp.getApplicationContext()).getGpsMonitor();
		Base baseAct = Base.OBDApp.baseAct;
//		if(!trackEnable && baseAct.localbinder != null){
//			baseAct.localbinder.setGPSUpdateState(CarDataService.GPS_UPLOAD_START);	// to cause to make a new trace
//		}		
		boolean isOBDConnected = false;
		if(baseAct.localbinder != null && baseAct.localbinder.getBluetoothSet() != null){
			isOBDConnected = baseAct.localbinder.getBluetoothSet().isConnected();
		}
		trackView.updateTrace(isOBDConnected);	// download trace from server
//		mHandler.postDelayed(new Runnable() {
//			@Override
//			public void run() {
//				// if( lsUserTrace != null && lsUserTrace.size() != 0){
//				// lsUserTrace.add(0, lsUserTrace.get(0));
//				// adapter.notifyDataSetChanged();
//				// xListView.stopRefresh();// 完成
//				// }
//				// Toast.makeText(DrivehabitActivity.this, "没有新内容", 1).show();
//				
//				if (trackView.updateTrace()) {
//					queryTrace = getLongAgoDateList();
//					adapter.setLlist(queryTrace);	// getview();请教吴征荣
//					xListView.setAdapter(adapter);
////					adapter.notifyDataSetChanged();
//					changeTabState(1);
//					xListView.stopRefresh();// 完成
//					Toast.makeText(DrivehabitActivity.this, "成功", 1).show();
//				} else {
//					Toast.makeText(DrivehabitActivity.this, "失败", 1).show();
//					xListView.stopRefresh();// 完成
//				}
//
//			}
//		}, 2000);
	}

	@Override
	public void onLoadMore() {
		// TODO Auto-generated method stub
		Toast.makeText(DrivehabitActivity.this, "加载更多", 0).show();
		mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				// if (lsUserTrace != null && lsUserTrace.size() != 0) {
				// lsUserTrace.add(lsUserTrace.get(0));
				// adapter.setLlist(lsUserTrace);
				// adapter.notifyDataSetChanged();
				// xListView.stopLoadMore();// 完成
				// Toast.makeText(DrivehabitActivity.this, "为了测试重复加载", 1)
				// .show();
				// } else {
				Toast.makeText(DrivehabitActivity.this, "没有更多内容", 1).show();
				xListView.stopLoadMore();// 完成
				// }

			}
		}, 2000);
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		// TODO Auto-generated method stub
		if(lastPosition != -1 && (lastPosition<firstVisibleItem || lastPosition >= firstVisibleItem+visibleItemCount)){
			lastPosition = -1;
			LinearLayout existContainer = (LinearLayout) mapLayout.getParent();
			existContainer.removeView(mapLayout);				
			mMapView.onPause();
			ImageView lastArrow = (ImageView)existContainer.findViewById(R.id.habit_arrow);
			lastArrow.setImageResource(R.drawable.arrow_down_gray);
		}
	}

}