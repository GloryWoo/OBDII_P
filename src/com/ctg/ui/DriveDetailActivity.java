package com.ctg.ui;

import java.text.DecimalFormat;
import java.util.ArrayList;
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
import com.ctg.bean.NonValueFormatter;
import com.ctg.crash.LogRecord;
import com.ctg.group.Group;
import com.ctg.group.Member;
import com.ctg.net.GetDriveBehavior;
import com.ctg.net.HttpQueue;
import com.ctg.shareUserInfo.ShareUserTrace;
import com.ctg.trace.DateUtil;
import com.ctg.trace.DrivingEvent;
import com.ctg.trace.GPS;
import com.ctg.trace.StatisticsData;
import com.ctg.trace.TraceDataSource;
import com.ctg.util.Util;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.Legend.LegendPosition;
import com.github.mikephil.charting.data.DataSet;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.PercentFormatter;
import com.github.mikephil.charting.utils.ValueFormatter;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class DriveDetailActivity extends Activity implements OnClickListener {
	final private String TAG = "DriveDetailActivity ";
	private static final double FUEL_RATE_RMB = 6.5;
	private MapView mMapView;
	private BaiduMap mBaiduMap;
	private ArrayList<GPS> lsTracePt;
	private TraceDataSource trackView = null;
	private Long traceId;
	private LatLng start = null;
	private LatLng end = null;
	private int id;
	private boolean isHabVisiable = false;// is the habit layout is visiable
	private boolean isEffVisiable = false;// is the efficiency layout is
											// visiable
	private String time;
	private ArrayList<StatisticsData> lsTracePt_temp = null;
	private List<Group> lsGroup;
	private List<String> userName;
	private List<Member> user;
	private StatisticsData cur_trace = null;

	private ImageView btn_back;
	public PieChart mChart;
	public TextView btn_share, title, z_txt, s_txt, k_txt, j_txt, d_txt,n_txt, y_txt, c_txt, 
	g_txt, ds_txt, qy_txt, dsy_txt, jy_txt, zy_txt, jl_txt, q_txt, last_txt;
	public TextView jizhuan_cnt, sbiandao_cnt, kuaidao_cnt, jisha_cnt,
			diaotou_cnt, nixiang_cnt, yejian_cnt, chaosu_cnt, gaofeng_cnt,
			daisu_cnt, qiyou_cnt, daisuyou_cnt, junyou_cnt, zongyouzi_cnt, julie_cnt, qiting_cnt;
	public TextView safe_score, economy_score, comfy_score;
	private TextView ex11, ex12, ex13, ex14, ex15, ex16, ex17, ex18, ex21, ex22, ex23, ex24, 
						ex25, ex26, ex31, ex32, exfooter;
	private LinearLayout habit_param, efficiency_param, safe_layout,
			fiance_layout, confort_layout, linear_safe, linear_economic, linear_comfort;
			//detail_layout1, detail_layout2,detail_layout3, detail_layout4, detail_layout5, detail_layout6;

	private RelativeLayout habit_click, efficiency_click, z_layout, c_layout,
			y_layout, g_layout, s_layout, k_layout, j_layout, d_layout,
			n_layout, ds_layout, qy_layout, dsy_layout, jy_layout, zy_layout, jl_layout, q_layout, last_layout;
	ImageView z_img, c_img, y_img, g_img, s_img, k_img, 
			  j_img, d_img, n_img, ds_img, qy_img, dsy_img, jy_img, zy_img, jl_img, q_img, last_img;
	private boolean isSameClick;
	private Typeface tf;
	ImageView safe_line, economic_line, comfort_line;

//	private long nyte, speed, peak, idle;
	private int overallScore = 100, safeScore = 100, economyScore = 100, comfortScore = 100;
	private int hardAccTimes = 0;
	private boolean hasOBD = false, hasSensor = false;
	
	
//	private GetDriveBehavior getDriveBehavior;
	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		// 在使用SDK各组件之前初始化context信息，传入ApplicationContext
		super.onCreate(arg0);
		SDKInitializer.initialize(getApplicationContext());
		requestWindowFeature(Window.FEATURE_NO_TITLE);// 去掉标题栏
		setContentView(R.layout.drive_detail);

		initData();
		initView();
		initListener();
		Base.OBDApp.driveDetail = this;
	}

	@SuppressWarnings("unchecked")
	private void initData() {
		// TODO Auto-generated method stub
		traceId = getIntent().getLongExtra("traceId", -1);
		time = getIntent().getStringExtra("time");
		id = getIntent().getIntExtra("id", -1);
		lsTracePt_temp = (ArrayList<StatisticsData>) getIntent().getSerializableExtra("list");
		cur_trace = (StatisticsData)getIntent().getSerializableExtra("statistics");
//		nyte = getIntent().getLongExtra("nyte", -1);
//		speed = getIntent().getLongExtra("spdy", -1);
//		peak = getIntent().getLongExtra("peak", -1);
//		idle = getIntent().getLongExtra("idle", -1);
		
		trackView = new TraceDataSource(this);
		
		safeScore = calculateSafetyScore(cur_trace);
		economyScore = calculateEconomyScore(cur_trace);
		comfortScore = calculateComfortScore(cur_trace);
		overallScore = (safeScore + economyScore + comfortScore) / 3;	
		
		hardAccTimes = trackView.getEventListData(traceId, GetDriveBehavior.EVENT_TYPE_HARD_ACCEL).size();
//		speedyTurnTimes = trackView.getEventListData(traceId, GetDriveBehavior.EVENT_TYPE_SPEEDY_TURN).size();
		hasOBD = isOBDConnected(cur_trace);
//		getDriveBehavior = new GetDriveBehavior();
		
	}

	private void initView() {
		// TODO Auto-generated method stub
		mMapView = (MapView) findViewById(R.id.drive_detail_map);
		title = (TextView) findViewById(R.id.habit_title);
		
		z_txt = (TextView) findViewById(R.id.drive_detail_z_txt);
		s_txt = (TextView) findViewById(R.id.drive_detail_s_txt);
		k_txt = (TextView) findViewById(R.id.drive_detail_k_txt);
		j_txt = (TextView) findViewById(R.id.drive_detail_j_txt);
		d_txt = (TextView) findViewById(R.id.drive_detail_d_txt);
		n_txt = (TextView) findViewById(R.id.drive_detail_n_txt);
		y_txt = (TextView) findViewById(R.id.drive_detail_y_txt);
		c_txt = (TextView) findViewById(R.id.drive_detail_c_txt);
		g_txt = (TextView) findViewById(R.id.drive_detail_g_txt);
		ds_txt = (TextView) findViewById(R.id.drive_detail_ds_txt);
		qy_txt = (TextView) findViewById(R.id.drive_detail_qy_txt);
		dsy_txt = (TextView) findViewById(R.id.drive_detail_dsy_txt);
		jy_txt = (TextView) findViewById(R.id.drive_detail_jy_txt);	
		zy_txt = (TextView) findViewById(R.id.drive_detail_zy_txt);	
		jl_txt = (TextView) findViewById(R.id.drive_detail_jl_txt);
		q_txt = (TextView) findViewById(R.id.drive_detail_q_txt);
		
		ex11 = (TextView) findViewById(R.id.exclaim11);
		ex12 = (TextView) findViewById(R.id.exclaim12);
		ex13 = (TextView) findViewById(R.id.exclaim13);
		ex14 = (TextView) findViewById(R.id.exclaim14);
		ex15 = (TextView) findViewById(R.id.exclaim15);
		ex16 = (TextView) findViewById(R.id.exclaim16);
		ex17 = (TextView) findViewById(R.id.exclaim17);
		ex18 = (TextView) findViewById(R.id.exclaim18);
		ex21 = (TextView) findViewById(R.id.exclaim21);
		ex22 = (TextView) findViewById(R.id.exclaim22);
		ex23 = (TextView) findViewById(R.id.exclaim23);
		ex24 = (TextView) findViewById(R.id.exclaim24);
		ex25 = (TextView) findViewById(R.id.exclaim25);
		ex26 = (TextView) findViewById(R.id.exclaim26);
		ex31 = (TextView) findViewById(R.id.exclaim31);
		ex32 = (TextView) findViewById(R.id.exclaim32);
		exfooter = (TextView) findViewById(R.id.drive_detail_exclaim_notice);
		
		safe_score = (TextView) findViewById(R.id.drive_detail_habit_safe_score);		
		economy_score = (TextView) findViewById(R.id.drive_detail_habit_ecnmy_score);		
		comfy_score = (TextView) findViewById(R.id.drive_detail_habit_comfy_score);
		
		jisha_cnt = (TextView) findViewById(R.id.drive_detail_j_content);		
		yejian_cnt = (TextView) findViewById(R.id.drive_detail_y_content);
		chaosu_cnt = (TextView) findViewById(R.id.drive_detail_c_content);
		jizhuan_cnt = (TextView) findViewById(R.id.drive_detail_z_content);
		sbiandao_cnt = (TextView) findViewById(R.id.drive_detail_s_content);
		kuaidao_cnt = (TextView) findViewById(R.id.drive_detail_k_content);
		diaotou_cnt = (TextView) findViewById(R.id.drive_detail_d_content);
		nixiang_cnt = (TextView) findViewById(R.id.drive_detail_n_content);
		
		qiyou_cnt = (TextView) findViewById(R.id.drive_detail_qy_content);
		daisuyou_cnt = (TextView) findViewById(R.id.drive_detail_dsy_content);
		junyou_cnt = (TextView) findViewById(R.id.drive_detail_jy_content);
		zongyouzi_cnt = (TextView) findViewById(R.id.drive_detail_zy_content);
		gaofeng_cnt = (TextView) findViewById(R.id.drive_detail_g_content);
		daisu_cnt = (TextView) findViewById(R.id.drive_detail_ds_content);
		
		julie_cnt = (TextView) findViewById(R.id.drive_detail_jl_content);
		qiting_cnt = (TextView) findViewById(R.id.drive_detail_q_content);
		
		if(!hasOBD) {
			safe_score.setText("-- 分");
			economy_score.setText("-- 分");
			comfy_score.setText("-- 分");
			
			jisha_cnt.setText("-- 次");
			ex11.setVisibility(View.VISIBLE);			
			
			yejian_cnt.setText( "-- 分钟");
			ex12.setVisibility(View.VISIBLE);
			
			chaosu_cnt.setText("-- 分钟");
			ex13.setVisibility(View.VISIBLE);
			
			jizhuan_cnt.setText( "-- 次");
			ex14.setVisibility(View.VISIBLE);
			
			sbiandao_cnt.setText( "-- 次");
			ex15.setVisibility(View.VISIBLE);
			
			kuaidao_cnt.setText( "-- 次");
			ex16.setVisibility(View.VISIBLE);
			
			diaotou_cnt.setText( "-- 次");
			ex17.setVisibility(View.VISIBLE);
			
			nixiang_cnt.setText( "-- 次");
			ex18.setVisibility(View.VISIBLE);
			
			qiyou_cnt.setText("-- L");
			ex21.setVisibility(View.VISIBLE);
			
			daisuyou_cnt.setText("-- L");
			ex22.setVisibility(View.VISIBLE);
			
			junyou_cnt.setText("-- L/百公里");
			ex23.setVisibility(View.VISIBLE);
			
			zongyouzi_cnt.setText("-- 元");
			ex24.setVisibility(View.VISIBLE);
			
			gaofeng_cnt.setText("-- 分钟");
			ex25.setVisibility(View.VISIBLE);
			
			daisu_cnt.setText("-- 分钟");
			ex26.setVisibility(View.VISIBLE);
			
			julie_cnt.setText("-- 次");
			ex31.setVisibility(View.VISIBLE);
			
			qiting_cnt.setText("-- 次");
			ex32.setVisibility(View.VISIBLE);
		}
		else {
			safe_score.setText(safeScore + "分");
			economy_score.setText(economyScore + "分");
			comfy_score.setText(comfortScore + "分");
			
			jisha_cnt.setText(hardAccTimes + "次");
			yejian_cnt.setText(DateUtil.TimeInterval2Str(cur_trace.getNightTime()));
			chaosu_cnt.setText(DateUtil.TimeInterval2Str(cur_trace.getSpeedingTime()));
			
			DecimalFormat df=new DecimalFormat("#.##");
			qiyou_cnt.setText(df.format(cur_trace.getTotal_fuel()) + "L");
			daisuyou_cnt.setText(df.format(cur_trace.getIdle_fuel()) + "L");
			junyou_cnt.setText(cur_trace.getAverage_mpg() + "L/100km");
			zongyouzi_cnt.setText(df.format(cur_trace.getTotal_fuel() * FUEL_RATE_RMB) + "元");
			gaofeng_cnt.setText(DateUtil.TimeInterval2Str(cur_trace.getRushTime()));
			daisu_cnt.setText(DateUtil.TimeInterval2Str(cur_trace.getIdleTime()));
			
			qiting_cnt.setText(hardAccTimes + "次");
			
//			jizhuan_cnt.setText(speedyTurnTimes + "次");
			if(!hasSensor){
				jizhuan_cnt.setText( "-- 次");
				ex14.setVisibility(View.VISIBLE);
				
				sbiandao_cnt.setText( "-- 次");
				ex15.setVisibility(View.VISIBLE);
				
				kuaidao_cnt.setText( "-- 次");
				ex16.setVisibility(View.VISIBLE);
				
				diaotou_cnt.setText( "-- 次");
				ex17.setVisibility(View.VISIBLE);
				
				nixiang_cnt.setText( "-- 次");
				ex18.setVisibility(View.VISIBLE);
				
				julie_cnt.setText("-- 次");
				ex31.setVisibility(View.VISIBLE);
				
				exfooter.setText("表示需要优车宝传感器模块");
			}
		}				
		
//		yejian_cnt.setText(DateUtil.TimeInterval2Str(nyte));		
//		chaosu_cnt.setText(DateUtil.TimeInterval2Str(speed));		
//		gaofeng_cnt.setText(DateUtil.TimeInterval2Str(peak));
//		daisu_cnt.setText(DateUtil.TimeInterval2Str(idle));
		
		z_layout = (RelativeLayout) findViewById(R.id.drive_detail_z_layout);
		s_layout = (RelativeLayout) findViewById(R.id.drive_detail_s_layout);
		k_layout = (RelativeLayout) findViewById(R.id.drive_detail_k_layout);
		j_layout = (RelativeLayout) findViewById(R.id.drive_detail_j_layout);
		d_layout = (RelativeLayout) findViewById(R.id.drive_detail_d_layout);
		n_layout = (RelativeLayout) findViewById(R.id.drive_detail_n_layout);
		y_layout = (RelativeLayout) findViewById(R.id.drive_detail_y_layout);
		c_layout = (RelativeLayout) findViewById(R.id.drive_detail_c_layout);		
		g_layout = (RelativeLayout) findViewById(R.id.drive_detail_g_layout);
		ds_layout = (RelativeLayout) findViewById(R.id.drive_detail_ds_layout);
		qy_layout = (RelativeLayout) findViewById(R.id.drive_detail_qy_layout);
		dsy_layout = (RelativeLayout) findViewById(R.id.drive_detail_dsy_layout);
		jy_layout = (RelativeLayout) findViewById(R.id.drive_detail_jy_layout);
		zy_layout = (RelativeLayout) findViewById(R.id.drive_detail_zy_layout);
		jl_layout = (RelativeLayout) findViewById(R.id.drive_detail_jl_layout);
		q_layout = (RelativeLayout) findViewById(R.id.drive_detail_q_layout);		

		z_img = (ImageView) findViewById(R.id.drive_detail_z_image);
		s_img = (ImageView) findViewById(R.id.drive_detail_s_image);
		k_img = (ImageView) findViewById(R.id.drive_detail_k_image);
		j_img = (ImageView) findViewById(R.id.drive_detail_j_image);
		d_img = (ImageView) findViewById(R.id.drive_detail_d_image);
		n_img = (ImageView) findViewById(R.id.drive_detail_n_image);
		y_img = (ImageView) findViewById(R.id.drive_detail_y_image);
		c_img = (ImageView) findViewById(R.id.drive_detail_c_image);		
		g_img = (ImageView) findViewById(R.id.drive_detail_g_image);
		ds_img = (ImageView) findViewById(R.id.drive_detail_ds_image);
		qy_img = (ImageView) findViewById(R.id.drive_detail_qy_image);
		dsy_img = (ImageView) findViewById(R.id.drive_detail_dsy_image);
		jy_img = (ImageView) findViewById(R.id.drive_detail_jy_image);
		zy_img = (ImageView) findViewById(R.id.drive_detail_zy_image);
		jl_img = (ImageView) findViewById(R.id.drive_detail_jl_image);
		q_img = (ImageView) findViewById(R.id.drive_detail_q_image);		
		
		btn_back = (ImageView) findViewById(R.id.habit_back);
		// btn_share = (TextView) findViewById(R.id.drive_detail_share);
		habit_click = (RelativeLayout) findViewById(R.id.drive_detail_habit_layout);
//		detail_layout1 = (LinearLayout) findViewById(R.id.detail_layout1);
//		detail_layout2 = (LinearLayout) findViewById(R.id.detail_layout2);
//		detail_layout3 = (LinearLayout) findViewById(R.id.detail_layout3);
//		detail_layout4 = (LinearLayout) findViewById(R.id.detail_layout4);
//		detail_layout5 = (LinearLayout) findViewById(R.id.detail_layout5);
//		detail_layout6 = (LinearLayout) findViewById(R.id.detail_layout6);
		linear_safe = (LinearLayout) findViewById(R.id.layout_safe);
		linear_economic = (LinearLayout) findViewById(R.id.layout_economic);
		linear_comfort = (LinearLayout) findViewById(R.id.layout_comfort);
		
		safe_layout = (LinearLayout) findViewById(R.id.detail_safe);
		fiance_layout = (LinearLayout) findViewById(R.id.detail_economic);
		confort_layout = (LinearLayout) findViewById(R.id.detail_comfort);
		mChart = (PieChart) findViewById(R.id.drive_detail_score);

		safe_line = (ImageView) findViewById(R.id.safe_line);
		economic_line = (ImageView) findViewById(R.id.economic_line);
		comfort_line = (ImageView) findViewById(R.id.comfort_line);
		if(hasOBD)
			initPieChart(Integer.toString((safeScore + economyScore + comfortScore) / 3));
		else
			initPieChart("--");
//		getDriveBehavior.downloadDriveBehavior(DriveDetailActivity.this, 
//												DateUtil.DateToDateStr(cur_trace.getStartTime()), 
//												DateUtil.DateToDateStr(cur_trace.getEndTime()));
		
		mBaiduMap = mMapView.getMap();
		drawMap(trackView.getPtListData(lsTracePt_temp.get(id).getSid()));
	}

	private void initPieChart(String score) {
		mChart.setDescription("");
//		mChart.setHoleRadius(30f);
//		mChart.setTransparentCircleRadius(0f);
		mChart.setCenterTextSize(18f);
		// mChart.setDrawXValues(true);
		mChart.setUsePercentValues(false);
		mChart.setDrawHoleEnabled(true);
		mChart.setHoleColorTransparent(true);

		mChart.setTransparentCircleColor(Color.WHITE);

		mChart.setHoleRadius(90f);
//		mChart.setTransparentCircleRadius(53f);

		mChart.setDrawCenterText(true);
		mChart.setCenterTextColor(Color.WHITE);
		mChart.setRotationAngle(0);
		// enable rotation of the chart by touch
		mChart.setRotationEnabled(false);
		mChart.setDrawHoleEnabled(true);
		mChart.setTouchEnabled(false);
		
		Legend legend = mChart.getLegend();
		legend.setEnabled(false);
		
		// mChart.animateY(1500, Easing.EasingOption.EaseInOutQuad);
		// mChart.spin(2000, 0, 360);
		// mChart.setBackground(background);
		ArrayList<Entry> yVals = new ArrayList<Entry>();
		List<String> xVals = new ArrayList<String>();

		yVals.add(new Entry((float) 100, 0));
		xVals.add("");
//		yVals.add(new Entry((float) 13, 1));
//		xVals.add("");

		PieDataSet dataSet = new PieDataSet(yVals, "");

		ArrayList<Integer> colors = new ArrayList<Integer>();

		for (int c : ColorTemplate.JOYFUL_COLORS)
			colors.add(c);

		colors.add(ColorTemplate.getHoloBlue());

		dataSet.setColors(colors);

		ArrayList<DataSet> dataSets = new ArrayList<DataSet>();
		dataSets.add(dataSet);
		PieData data = new PieData(xVals, dataSet);

		data.setValueFormatter(new NonValueFormatter());
		data.setValueTextSize(11f);
		data.setValueTextColor(Color.WHITE);
		data.setValueTypeface(tf);
		mChart.setData(data);
		
		mChart.setCenterText("总评分：" + score + "分"); 
	}

	private void initListener() {
		btn_back.setOnClickListener(this);
		// btn_share.setOnClickListener(this);
		habit_click.setOnClickListener(this);
		// efficiency_click.setOnClickListener(this);
		safe_layout.setOnClickListener(this);
		fiance_layout.setOnClickListener(this);
		confort_layout.setOnClickListener(this);

		z_layout.setOnClickListener(this);
		s_layout.setOnClickListener(this);
		k_layout.setOnClickListener(this);
		j_layout.setOnClickListener(this);
		d_layout.setOnClickListener(this);
		n_layout.setOnClickListener(this);
		y_layout.setOnClickListener(this);
		c_layout.setOnClickListener(this);
		g_layout.setOnClickListener(this);
		ds_layout.setOnClickListener(this);
		qy_layout.setOnClickListener(this);
		dsy_layout.setOnClickListener(this);
		jy_layout.setOnClickListener(this);
		zy_layout.setOnClickListener(this);
		jl_layout.setOnClickListener(this);
		q_layout.setOnClickListener(this);

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
				Intent intent = new Intent(DriveDetailActivity.this,
						TraceMapActivity.class);
				intent.putExtra("id", id);
				intent.putExtra("list", lsTracePt_temp);
				// intent.putExtra("trace", lsUserTrace.get(position));
				DriveDetailActivity.this.startActivity(intent);

			}

		});

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
			start = new LatLng(lsPt.get(0).getLat(), lsPt.get(0)
					.getLon());
			end = new LatLng(lsPt.get(lsPt.size() - 1).getLat(),
					lsPt.get(lsPt.size() - 1).getLon());
		}

		for (int i = 0; i < lsPt.size(); i++) {
			pts.add(new LatLng(lsPt.get(i).getLat(), lsPt.get(i)
					.getLon()));
		}

		if(pts.size() > 1){
			OverlayOptions polylineOption = new PolylineOptions().points(pts).width(6).color(0xffff00ff);
			mBaiduMap.addOverlay(polylineOption);
		}
//		delList.clear();
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
	
	public void restoreLastImg(){
		if(last_img == null)
			return;
		switch(last_img.getId()){
		case R.id.drive_detail_z_image:
			z_img.setBackgroundResource(R.drawable.icon_wheel);
			break;
		case R.id.drive_detail_s_image:
			s_img.setBackgroundResource(R.drawable.icon_s);
			break;			
		case R.id.drive_detail_k_image:
			k_img.setBackgroundResource(R.drawable.icon_step);
			break;
		case R.id.drive_detail_j_image:
			j_img.setBackgroundResource(R.drawable.icon_impatient);
			break;			
		case R.id.drive_detail_d_image:
			d_img.setBackgroundResource(R.drawable.icon_turn_around);
			break;			
		case R.id.drive_detail_n_image:
			n_img.setBackgroundResource(R.drawable.icon_inverse);
			break;			
		case R.id.drive_detail_y_image:
			y_img.setBackgroundResource(R.drawable.icon_night);
			break;			
		case R.id.drive_detail_c_image:
			c_img.setBackgroundResource(R.drawable.icon_exceed);			
			break;			
		case R.id.drive_detail_g_image:
			g_img.setBackgroundResource(R.drawable.icon_peak_travel);
			break;			
		case R.id.drive_detail_ds_image:
			ds_img.setBackgroundResource(R.drawable.kong);
			break;
		case R.id.drive_detail_qy_image:
			qy_img.setBackgroundResource(R.drawable.quan);
			break;
		case R.id.drive_detail_dsy_image:
			dsy_img.setBackgroundResource(R.drawable.icon_idling);
			break;
		case R.id.drive_detail_jy_image:
			jy_img.setBackgroundResource(R.drawable.jun);
			break;	
		case R.id.drive_detail_zy_image:
			zy_img.setBackgroundResource(R.drawable.gas_fee);
			break;			
		case R.id.drive_detail_jl_image:
			jl_img.setBackgroundResource(R.drawable.icon_bump);
			break;
		case R.id.drive_detail_q_image:
			q_img.setBackgroundResource(R.drawable.icon_fierce);
			break;
		}
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
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (mMapView != null) {
			mMapView.onDestroy();
		}
		Base.OBDApp.driveDetail = null;
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
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.habit_back:
			this.finish();
			break;
//		case R.id.drive_detail_habit_layout:
//			changeLayoutVisibility(v.getId(), isHabVisiable);
//			break;
		case R.id.detail_safe:
			LogRecord.SaveLogInfo2File(Base.OperateInfo, TAG+"safe serial click");
			linear_safe.setVisibility(View.VISIBLE);
			linear_economic.setVisibility(View.INVISIBLE);
			linear_comfort.setVisibility(View.INVISIBLE);
			
			safe_line.setVisibility(View.VISIBLE);
			economic_line.setVisibility(View.GONE);
			comfort_line.setVisibility(View.GONE);
			break;
		case R.id.detail_economic:
			LogRecord.SaveLogInfo2File(Base.OperateInfo, TAG+"finance serial click");
			linear_safe.setVisibility(View.INVISIBLE);
			linear_economic.setVisibility(View.VISIBLE);
			linear_comfort.setVisibility(View.INVISIBLE);

			
			safe_line.setVisibility(View.GONE);
			economic_line.setVisibility(View.VISIBLE);
			comfort_line.setVisibility(View.GONE);
			break;
		case R.id.detail_comfort:
			LogRecord.SaveLogInfo2File(Base.OperateInfo, TAG+"comfort serial click");
			linear_safe.setVisibility(View.INVISIBLE);
			linear_economic.setVisibility(View.INVISIBLE);
			linear_comfort.setVisibility(View.VISIBLE);

			
			safe_line.setVisibility(View.GONE);
			economic_line.setVisibility(View.GONE);
			comfort_line.setVisibility(View.VISIBLE);
			break;

		case R.id.drive_detail_z_layout:
			LogRecord.SaveLogInfo2File(Base.OperateInfo, TAG+"drift emergency click");
			// resetDetailStatus();
			if (last_txt != null && last_layout != null) {
				if (last_txt == s_txt) {
					return;
				} else {
					last_txt.setTextColor(Color.BLACK);
					last_layout.setBackgroundResource(0);
					restoreLastImg();
				}
			}
			z_txt.setTextColor(Color.WHITE);
			z_layout.setBackgroundResource(R.drawable.detail_button);
			z_img.setBackgroundResource(R.drawable.icon_wheel_active);
			last_img = z_img;
			last_txt = z_txt;
			last_layout = z_layout;
			break;
		case R.id.drive_detail_s_layout:
			LogRecord.SaveLogInfo2File(Base.OperateInfo, TAG+"S shape lane modify click");
			if (last_txt != null && last_layout != null) {
				if (last_txt == s_txt) {
					return;
				} else {
					last_txt.setTextColor(Color.BLACK);
					last_layout.setBackgroundResource(0);
					restoreLastImg();
				}
			}
			s_txt.setTextColor(Color.WHITE);
			s_layout.setBackgroundResource(R.drawable.detail_button);
			s_img.setBackgroundResource(R.drawable.icon_s_active);
			last_img = s_img;
			last_txt = s_txt;
			last_layout = s_layout;
			break;
		case R.id.drive_detail_k_layout:
			LogRecord.SaveLogInfo2File(Base.OperateInfo, TAG+"drive on lane line click");
			if (last_txt != null && last_layout != null) {
				if (last_txt == k_txt) {
					return;
				} else {
					last_txt.setTextColor(Color.BLACK);
					last_layout.setBackgroundResource(0);
					restoreLastImg();
				}
			}
			k_txt.setTextColor(Color.WHITE);
			k_layout.setBackgroundResource(R.drawable.detail_button);
			k_img.setBackgroundResource(R.drawable.icon_step_active);
			last_img = k_img;
			last_txt = k_txt;
			last_layout = k_layout;
			break;
		case R.id.drive_detail_j_layout:
			LogRecord.SaveLogInfo2File(Base.OperateInfo, TAG+"brake&fuel hard click");
			if (last_txt != null && last_layout != null) {
				if (last_txt == j_txt) {
					return;
				} else {
					last_txt.setTextColor(Color.BLACK);
					last_layout.setBackgroundResource(0);
					restoreLastImg();
				}
			}
			j_txt.setTextColor(Color.WHITE);
			j_layout.setBackgroundResource(R.drawable.detail_button);
			j_img.setBackgroundResource(R.drawable.icon_impatient_active);
			last_img = j_img;
			last_txt = j_txt;
			last_layout = j_layout;
			break;
		case R.id.drive_detail_d_layout:
			LogRecord.SaveLogInfo2File(Base.OperateInfo, TAG+"dangerous turn around click");
			if (last_txt != null && last_layout != null) {
				if (last_txt == d_txt) {
					return;
				} else {
					last_txt.setTextColor(Color.BLACK);
					last_layout.setBackgroundResource(0);
					restoreLastImg();
				}
			}
			d_txt.setTextColor(Color.WHITE);
			d_layout.setBackgroundResource(R.drawable.detail_button);
			d_img.setBackgroundResource(R.drawable.icon_turn_around_active);
			last_img = d_img;
			last_txt = d_txt;
			last_layout = d_layout;
			break;
		case R.id.drive_detail_n_layout:
			LogRecord.SaveLogInfo2File(Base.OperateInfo, TAG+"reverse drive click");
			if (last_txt != null && last_layout != null) {
				if (last_txt == n_txt) {
					return;
				} else {
					last_txt.setTextColor(Color.BLACK);
					last_layout.setBackgroundResource(0);
					restoreLastImg();
				}
			}
			n_txt.setTextColor(Color.WHITE);
			n_layout.setBackgroundResource(R.drawable.detail_button);
			n_img.setBackgroundResource(R.drawable.icon_inverse_active);
			last_img = n_img;
			last_txt = n_txt;
			last_layout = n_layout;
			break;
		case R.id.drive_detail_y_layout:
			LogRecord.SaveLogInfo2File(Base.OperateInfo, TAG+"night drive click");
			if (last_txt != null && last_layout != null) {
				if (last_txt == y_txt) {
					return;
				} else {
					last_txt.setTextColor(Color.BLACK);
					last_layout.setBackgroundResource(0);
					restoreLastImg();
				}
			}
			y_txt.setTextColor(Color.WHITE);
			y_layout.setBackgroundResource(R.drawable.detail_button);
			y_img.setBackgroundResource(R.drawable.icon_night_avtive);
			last_img = y_img;
			last_txt = y_txt;
			last_layout = y_layout;
			break;
		case R.id.drive_detail_c_layout:
			LogRecord.SaveLogInfo2File(Base.OperateInfo, TAG+"over speed click");
			if (last_txt != null && last_layout != null) {
				if (last_txt == c_txt) {
					return;
				} else {
					last_txt.setTextColor(Color.BLACK);
					last_layout.setBackgroundResource(0);
					restoreLastImg();
				}
			}
			c_txt.setTextColor(Color.WHITE);
			c_layout.setBackgroundResource(R.drawable.detail_button);
			c_img.setBackgroundResource(R.drawable.icon_exceed_active);
			last_img = c_img;
			last_txt = c_txt;
			last_layout = c_layout;
			break;
		case R.id.drive_detail_g_layout:
			LogRecord.SaveLogInfo2File(Base.OperateInfo, TAG+"summit drive click");
			if (last_txt != null && last_layout != null) {
				if (last_txt == g_txt) {
					return;
				} else {
					last_txt.setTextColor(Color.BLACK);
					last_layout.setBackgroundResource(0);
					restoreLastImg();
				}
			}
			g_txt.setTextColor(Color.WHITE);
			g_layout.setBackgroundResource(R.drawable.detail_button);
			g_img.setBackgroundResource(R.drawable.icon_peak_travel_active);
			last_img = g_img;
			last_txt = g_txt;
			last_layout = g_layout;
			break;
		case R.id.drive_detail_ds_layout:
			LogRecord.SaveLogInfo2File(Base.OperateInfo, TAG+"idle time click");
			if (last_txt != null && last_layout != null) {
				if (last_txt == ds_txt) {
					return;
				} else {
					last_txt.setTextColor(Color.BLACK);
					last_layout.setBackgroundResource(0);
					restoreLastImg();
				}
			}
			ds_txt.setTextColor(Color.WHITE);
			ds_layout.setBackgroundResource(R.drawable.detail_button);
			ds_img.setBackgroundResource(R.drawable.kong_active);
			last_img = ds_img;
			last_txt = ds_txt;
			last_layout = ds_layout;
			break;
		case R.id.drive_detail_qy_layout:
			LogRecord.SaveLogInfo2File(Base.OperateInfo, TAG+"one routine gas consume click");
			if (last_txt != null && last_layout != null) {
				if (last_txt == qy_txt) {
					return;
				} else {
					last_txt.setTextColor(Color.BLACK);
					last_layout.setBackgroundResource(0);
					restoreLastImg();
				}
			}
			qy_txt.setTextColor(Color.WHITE);
			qy_layout.setBackgroundResource(R.drawable.detail_button);
			qy_img.setBackgroundResource(R.drawable.quan_active);
			last_img = qy_img;
			last_txt = qy_txt;
			last_layout = qy_layout;
			break;
		case R.id.drive_detail_dsy_layout:
			LogRecord.SaveLogInfo2File(Base.OperateInfo, TAG+"idle gas consume click");
			if (last_txt != null && last_layout != null) {
				if (last_txt == dsy_txt) {
					return;
				} else {
					last_txt.setTextColor(Color.BLACK);
					last_layout.setBackgroundResource(0);
					restoreLastImg();
				}
			}
			dsy_txt.setTextColor(Color.WHITE);
			dsy_layout.setBackgroundResource(R.drawable.detail_button);
			dsy_img.setBackgroundResource(R.drawable.icon_idling_active);
			last_img = dsy_img;
			last_txt = dsy_txt;
			last_layout = dsy_layout;
			break;
		case R.id.drive_detail_jy_layout:
			LogRecord.SaveLogInfo2File(Base.OperateInfo, TAG+"average gas consume click");
			if (last_txt != null && last_layout != null) {
				if (last_txt == jy_txt) {
					return;
				} else {
					last_txt.setTextColor(Color.BLACK);
					last_layout.setBackgroundResource(0);
					restoreLastImg();
				}
			}
			jy_txt.setTextColor(Color.WHITE);
			jy_layout.setBackgroundResource(R.drawable.detail_button);
			jy_img.setBackgroundResource(R.drawable.jun_active);
			last_img = jy_img;
			last_txt = jy_txt;
			last_layout = jy_layout;
			break;	
		case R.id.drive_detail_zy_layout:
			LogRecord.SaveLogInfo2File(Base.OperateInfo, TAG+"gas fee click");
			if (last_txt != null && last_layout != null) {
				if (last_txt == zy_txt) {
					return;
				} else {
					last_txt.setTextColor(Color.BLACK);
					last_layout.setBackgroundResource(0);
					restoreLastImg();
				}
			}
			zy_txt.setTextColor(Color.WHITE);
			zy_layout.setBackgroundResource(R.drawable.detail_button);
			zy_img.setBackgroundResource(R.drawable.gas_fee_active);
			last_img = zy_img;
			last_txt = zy_txt;
			last_layout = zy_layout;
			break;			
		case R.id.drive_detail_jl_layout:
			LogRecord.SaveLogInfo2File(Base.OperateInfo, TAG+"pump click");
			if (last_txt != null && last_layout != null) {
				if (last_txt == jl_txt) {
					return;
				} else {
					last_txt.setTextColor(Color.BLACK);
					last_layout.setBackgroundResource(0);
					restoreLastImg();
				}
			}
			jl_txt.setTextColor(Color.WHITE);
			jl_layout.setBackgroundResource(R.drawable.detail_button);
			jl_img.setBackgroundResource(R.drawable.icon_bump_active);
			last_img = jl_img;
			last_txt = jl_txt;
			last_layout = jl_layout;
			break;
		case R.id.drive_detail_q_layout:
			LogRecord.SaveLogInfo2File(Base.OperateInfo, TAG+"start stop fiercely click");
			if (last_txt != null && last_layout != null) {
				if (last_txt == q_txt) {
					return;
				} else {
					last_txt.setTextColor(Color.BLACK);
					last_layout.setBackgroundResource(0);
					restoreLastImg();
				}
			}
			q_txt.setTextColor(Color.WHITE);
			q_layout.setBackgroundResource(R.drawable.detail_button);
			q_img.setBackgroundResource(R.drawable.icon_fierce_active);
			last_img = q_img;
			last_txt = q_txt;
			last_layout = q_layout;
			break;
		}
	}

	private void resetDetailStatus() {
		z_txt.setTextColor(Color.BLACK);
		s_txt.setTextColor(Color.BLACK);
		k_txt.setTextColor(Color.BLACK);
		j_txt.setTextColor(Color.BLACK);
		d_txt.setTextColor(Color.BLACK);
		n_txt.setTextColor(Color.BLACK);
		y_txt.setTextColor(Color.BLACK);
		c_txt.setTextColor(Color.BLACK);
		g_txt.setTextColor(Color.BLACK);
		ds_txt.setTextColor(Color.BLACK);
		jl_txt.setTextColor(Color.BLACK);
		q_txt.setTextColor(Color.BLACK);
		z_layout.setBackgroundResource(0);
		c_layout.setBackgroundResource(0);
		q_layout.setBackgroundResource(0);
		ds_layout.setBackgroundResource(0);
		jl_layout.setBackgroundResource(0);
		y_layout.setBackgroundResource(0);
		g_layout.setBackgroundResource(0);
		s_layout.setBackgroundResource(0);
		k_layout.setBackgroundResource(0);
		j_layout.setBackgroundResource(0);
		d_layout.setBackgroundResource(0);
		n_layout.setBackgroundResource(0);
	}

	protected void changeLayoutVisibility(int id, boolean isVisiable) {
		// TODO Auto-generated method stub
		switch (id) {
		case R.id.drive_detail_habit_layout:
			habit_param.setVisibility(isVisiable == true ? View.GONE
					: View.VISIBLE);
			isHabVisiable = !isHabVisiable;
			break;
		// case R.id.drive_detail_efficiency_layout:
		// efficiency_param.setVisibility(isVisiable == true ? View.GONE
		// : View.VISIBLE);
		// isEffVisiable = !isEffVisiable;
		}
	}
	
	private boolean isOBDConnected(StatisticsData tripData){
		return !(tripData.getTotal_fuel() == 0f && tripData.getBattery_volt_2() == 0f);
	}
	
	private int calculateSafetyScore(StatisticsData tripData){
		long tid = tripData.getSid();
		int score = 100;
		
		List<DrivingEvent> elist1 = trackView.getEventListData(tid, GetDriveBehavior.EVENT_TYPE_HARD_ACCEL);
		List<DrivingEvent> elist2 = trackView.getEventListData(tid, GetDriveBehavior.EVENT_TYPE_SPEEDY_TURN);
		List<DrivingEvent> elist3 = trackView.getEventListData(tid, GetDriveBehavior.EVENT_ILLEGAL_U_TURN);
 		
		if(elist1.size() != 0) score -= 10;
		if(elist2.size() != 0) score -= 10;
		if(elist3.size() != 0) score -= 10;
		
		if(tripData.getNightTime() > (1000l * 60l * 60l)) score -= 10;	// greater than 1 hour
		if(tripData.getSpeedingTime() > (1000l * 60l * 5l)) score -= 10;	// greater than 5 min
		
		return score;
	}
	
	private int calculateEconomyScore(StatisticsData tripData){
		int score = 100;
		
		if(tripData.getRushTime() > (1000l * 60l * 10l)) score -= 15;
		if(tripData.getAverage_mpg() > 10f) score -= 15;
		if(tripData.getIdleTime() > (1000l * 60l * 10l)) score -= 15;
		
		return score;
	}
	
	private int calculateComfortScore(StatisticsData tripData){
		int score = 100;
		long tid = tripData.getSid();
		
		List<DrivingEvent> elist1 = trackView.getEventListData(tid, GetDriveBehavior.EVENT_TYPE_BUMPY_ROAD);
		List<DrivingEvent> elist2 = trackView.getEventListData(tid, GetDriveBehavior.EVENT_TYPE_HARD_ACCEL);
		
		if(elist1.size() != 0) score -= 20;
		if(elist2.size() != 0) score -= 20;
		
		return score;		
	}
	

}