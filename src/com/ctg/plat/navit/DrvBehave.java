package com.ctg.plat.navit;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.sug.SuggestionSearchOption;
import com.baidu.mapapi.utils.CoordinateConverter;
import com.baidu.mapapi.utils.CoordinateConverter.CoordType;
import com.baidu.navisdk.BNaviPoint;
import com.baidu.navisdk.BaiduNaviManager;
import com.baidu.navisdk.comapi.routeplan.RoutePlanParams;
import com.ctg.bean.NonValueFormatter;
import com.ctg.crash.LogRecord;
import com.ctg.dbtrace.DrivingEvent;
import com.ctg.dbtrace.StatisticsData;
import com.ctg.dbtrace.TraceDataSource;
import com.ctg.dbtrace.TraceDataSourceDelegate;
import com.ctg.dbtrace.TraceListInstance;
import com.ctg.group.Group;
import com.ctg.group.Member;
import com.ctg.land.FrndGrpAdapt;
import com.ctg.net.CacheManager;
import com.ctg.net.HttpQueue;
import com.ctg.net.IHttpCallback;
import com.ctg.sensor.UpLoadSensor;
import com.ctg.service.CarDataService;
import com.ctg.ui.BNavigatorActivity;
import com.ctg.ui.BaiduMapView;
import com.ctg.ui.Base;
import com.ctg.ui.OBDApplication;
import com.ctg.ui.R;
import com.ctg.util.GrpSearchDlg;
import com.ctg.util.MyBDLocation;
import com.ctg.util.MyPagerAdapter;
import com.ctg.util.MyPagerAdapter1;
import com.ctg.util.MyViewPager;
import com.ctg.util.NavitPoint;
import com.ctg.util.Preference;
import com.ctg.util.SearchPoiDlg;
import com.ctg.util.Util;
import com.ctg.weather.WeatherReport;
import com.example.swipelistview.SwipeAdapter;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.components.Legend.LegendForm;
import com.github.mikephil.charting.data.DataSet;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

class DrvBehave implements View.OnClickListener, TraceDataSourceDelegate{
	
	public static int UPDATE_VIEW_MSG = 1000;
	Context mContext;
	Base baseAct;
	public FrameLayout frame;
	TextView score_tv;
	TextView gold_tv;
	TextView statistic_tv;
	PieChart pieChart[];
	TextView statisBtn;
	LinearLayout detail_itm_sz[];//safety economic comfort item

	RelativeLayout detail_rela_sz[];
	ImageView img_sz[];
	TextView tv_sz[];
	TextView tv_v_sz[];
	ImageView detailItmLine[];
	TextView score_t;
	
	int focusColumn, focusItem = -1;
	public static int imgIdArr[][] = {{R.drawable.icon_exceed, R.drawable.brk_font, R.drawable.icon_turn_around},
					  {R.drawable.gas_font,R.drawable.quan,      R.drawable.jun},
					  {R.drawable.icon_bump,  R.drawable.icon_fierce}};
	public static int imgIdActArr[][] = {{R.drawable.icon_exceed_active, R.drawable.brk_font_active, R.drawable.icon_turn_around_active},
						 {R.drawable.gas_font_active,R.drawable.quan_active,      R.drawable.jun_active},
						 {R.drawable.icon_bump_active,  R.drawable.icon_fierce_active}};
	public static int itemStrArr[][] = {{R.string.exceed_spd, R.string.brk_fierce, R.string.turn_around},
						{R.string.gas_fierce,   R.string.whole_fuel,       R.string.average_fuel},
						{R.string.pump,        R.string.go_stop}};
	
	public static String tv_v_content_sz[][] = {{"次", "次", "次"},
								{"次", "L", "L"},
								{"次", "次", "次"}};
	
	public static int itemVal[][] = new int[3][3];
	//second page
//	boolean statisPageShow;
	LinearLayout statisPage;
	ImageView backArrow;
	PieChart scorePie;
	TextView scoreTotal_t;
	TextView goldTotal_t;
	View perMonth;
	View perDay;
	ImageView perMonthUnderL;
	ImageView perDayUnderL;
	LineChart mChart;
	TextView startStopSwitch;
	public TraceDataSource   traceData;
	UpLoadSensor upSensor;
	boolean startSwitch;
	public static boolean initDbData;	
	public static TraceListInstance traceList = TraceListInstance.getInstance();
	public static StatisticsData curTrace;
	public static ArrayList<DrivingEvent> overSpdLs;
	public static ArrayList<DrivingEvent> brkLs;
	public static ArrayList<DrivingEvent> turnLs;
	public static ArrayList<DrivingEvent> gasLs;
//	public static int overSpdCnt;
//	public static int brkCnt;
//	public static int turnCnt;
//	public static int gasCnt;
	public static int lay_score[];
	public static int lay_m[];
	public static int lay_d[];
	Overlay routineOverlay[];
	public class DataContent{
		int score;
		int gold;
		int rank;
		int safety_s;
		int economic_s;
		int comfort_s;
		int turn_fierce;
		int ride_drv;
		int turn_round;
		int idle_fuel;
		int whole_fuel;
		int aver_fuel;
		int pump;
		int go_stop_fierce;
	
	}
	public DataContent dataContent;

	public static void initDBData(){
		Date dt = new Date(System.currentTimeMillis());
		curTrace = traceList.getCurTrace();
		overSpdLs = traceList.getTraceEventList(curTrace.getTraceId(), 10);
		brkLs = traceList.getTraceEventList(curTrace.getTraceId(), 7);
		turnLs = traceList.getTraceEventList(curTrace.getTraceId(), 3);
		gasLs = traceList.getTraceEventList(curTrace.getTraceId(), 6);
		itemVal[0][0] = overSpdLs.size();
		itemVal[0][1] = brkLs.size();
		itemVal[0][2] = turnLs.size();
		itemVal[1][0] = gasLs.size();
		lay_score = traceList.getScoreWeight();
		lay_m = traceList.getMonthLayout(dt);
		lay_d = traceList.getDateLayout(dt);
		
	}
	
	@Override
	public void updateResult(int ret) {
		// TODO Auto-generated method stub
		if(ret == TraceDataSource.RET_UPDATE_COMPETE || ret == TraceDataSource.RET_UPDATE_NO_MORE){
			initDBData();
			msgUpdateViewHandler.obtainMessage(UPDATE_VIEW_MSG).sendToTarget();
		}
	}
	
	public void initDataView(){
		// TODO Auto-generated method stub
		initPieChart();
		initStatisticPieChart();
		initLineChart();
		//drawLastRoutine();
		score_tv.setText(""+curTrace.scoreTheTrip);
		scoreTotal_t.setText(""+curTrace.scoreAllAver);								
	}
	
	Handler msgUpdateViewHandler = new Handler(){
		public void handleMessage(Message msg) {
			if(msg.what == UPDATE_VIEW_MSG){
				initDataView();
			}
		}
	};
	
	public DrvBehave(Context context){
//		super(context);
		baseAct = (Base)context;
		mContext = context;
		frame = (FrameLayout) View.inflate(context, R.layout.sub_db, null);
		//this.setBackgroundColor(0);
//		addView(frame, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
//				LinearLayout.LayoutParams.MATCH_PARENT));
		
		
		
		statisBtn = (TextView) frame.findViewById(R.id.statis_btn);
		score_tv = (TextView) frame.findViewById(R.id.db_score);
		gold_tv = (TextView) frame.findViewById(R.id.db_gold);
		
		statisBtn.setOnClickListener(this);
		detail_itm_sz = new LinearLayout[3];
		detail_itm_sz[0] = (LinearLayout) frame.findViewById(R.id.detail_safe);
		detail_itm_sz[1] = (LinearLayout) frame.findViewById(R.id.detail_economic);
		detail_itm_sz[2] = (LinearLayout) frame.findViewById(R.id.detail_comfort);
		
		detail_rela_sz = new RelativeLayout[3];
		detail_rela_sz[0] = (RelativeLayout) frame.findViewById(R.id.drive_detail_rela_1);
		detail_rela_sz[1] = (RelativeLayout) frame.findViewById(R.id.drive_detail_rela_2);
		detail_rela_sz[2] = (RelativeLayout) frame.findViewById(R.id.drive_detail_rela_3);
		
		img_sz = new ImageView[3];
		img_sz[0] = (ImageView) frame.findViewById(R.id.drive_detail_img_1);
		img_sz[1] = (ImageView) frame.findViewById(R.id.drive_detail_img_2);
		img_sz[2] = (ImageView) frame.findViewById(R.id.drive_detail_img_3);
		
		tv_sz = new TextView[3];
		tv_sz[0] = (TextView) frame.findViewById(R.id.drive_detail_t_1);
		tv_sz[1] = (TextView) frame.findViewById(R.id.drive_detail_t_2);
		tv_sz[2] = (TextView) frame.findViewById(R.id.drive_detail_t_3);
		
		tv_v_sz = new TextView[3];
		tv_v_sz[0] = (TextView) frame.findViewById(R.id.drive_detail_v_t_1);
		tv_v_sz[1] = (TextView) frame.findViewById(R.id.drive_detail_v_t_2);
		tv_v_sz[2] = (TextView) frame.findViewById(R.id.drive_detail_v_t_3);
		tv_v_sz[0].setText(itemVal[0][0] + tv_v_content_sz[0][0]);
		tv_v_sz[1].setText(itemVal[0][1] + tv_v_content_sz[0][1]);
		tv_v_sz[2].setText(itemVal[0][2] + tv_v_content_sz[0][2]);
		detailItmLine = new ImageView[3];
		detailItmLine[0] = (ImageView) frame.findViewById(R.id.safety_line);
		detailItmLine[1] = (ImageView) frame.findViewById(R.id.economic_line);
		detailItmLine[2] = (ImageView) frame.findViewById(R.id.comfort_line);
		
		startStopSwitch = (TextView) frame.findViewById(R.id.start_stop_routine);
		startStopSwitch.setOnClickListener(this);
		for(int i = 0; i < 3; i++){
			detail_itm_sz[i].setOnClickListener(this);
			detail_rela_sz[i].setOnClickListener(this);
		}	
		
		dataContent = new DataContent();
		pieChart = new PieChart[3];
		pieChart[0] = (PieChart) frame.findViewById(R.id.db_chart_safety);
		pieChart[1] = (PieChart) frame.findViewById(R.id.db_chart_economic);
		pieChart[2] = (PieChart) frame.findViewById(R.id.db_chart_comfort);
		
		
		dataContent.safety_s = 89;
		dataContent.economic_s = 75;
		dataContent.comfort_s = 74;
		
		
		
		//second page
		statisPage = (LinearLayout) frame.findViewById(R.id.db_page2);
		backArrow = (ImageView) frame.findViewById(R.id.stat_back);
		scorePie = (PieChart) frame.findViewById(R.id.stat_score_pie);
		scoreTotal_t = (TextView) frame.findViewById(R.id.stat_score_val);
		
		goldTotal_t = (TextView) frame.findViewById(R.id.stat_gold_val);
		
		perMonth = frame.findViewById(R.id.per_month_l);
		perDay = frame.findViewById(R.id.per_day_l);
		
		
		perMonthUnderL = (ImageView) frame.findViewById(R.id.per_month_line);
		perDayUnderL = (ImageView) frame.findViewById(R.id.per_day_line);
		mChart = (LineChart) frame.findViewById(R.id.stat_hist_line_chrt);
		backArrow.setOnClickListener(this);
		perMonth.setOnClickListener(this);
		perDay.setOnClickListener(this);
		
//		initPieChart();
//		initStatisticPieChart();
//		initLineChart();
//		score_tv.setText(""+curTrace.scoreTheTrip);
//		scoreTotal_t.setText(""+curTrace.scoreAllAver);
		
		traceData = new TraceDataSource(mContext);
		traceData.traceDataSourceDelegate = this;		
		getSessionId();
	}

	
	void getSessionId(){
		
		
		String url = Base.DB_BEHAVIOR_SERVER + "/login?account=" + Base.loginUser + "&password=" + "1234";

		CacheManager.getJson(mContext, url, new IHttpCallback() {					
			@Override
			public void handle(int retCode, Object response) {
				// TODO Auto-generated method stub
				Log.d("GrpMap", "retCode:" + retCode);
				String resp = response.toString();
				JSONObject jsonObject;
				try {
					jsonObject = new JSONObject(resp);				
					if(jsonObject.has("token")){
						String sessionId = jsonObject.getString("token");
						Preference.getInstance(mContext.getApplicationContext()).setDBSessionId(sessionId);
						upSensor = new UpLoadSensor(mContext, sessionId);
						Toast.makeText(mContext, "DB-Server 登录成功", Toast.LENGTH_SHORT).show();
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				traceData.updateTrace(false);
			}
		}, null);
	}
	private void initPieChart() {
		
		ArrayList<Entry> yVals;
		List<String> xVals;
		PieDataSet dataSet;
		ArrayList<Integer> colors;
		PieData data = null;
		
		for(int i = 0; i < 3; i++){
			
			pieChart[i].setDescription("");
	//		mChart.setHoleRadius(30f);
	//		mChart.setTransparentCircleRadius(0f);
			pieChart[i].setCenterTextSize(18f);
			
			// mChart.setDrawXValues(true);
			pieChart[i].setUsePercentValues(false);
			pieChart[i].setDrawHoleEnabled(true);
			pieChart[i].setHoleColorTransparent(false);
	
			//pieChart[i].setTransparentCircleColor(Color.WHITE);
			
			
	//		mChart.setTransparentCircleRadius(53f);
	
			pieChart[i].setDrawCenterText(true);
			pieChart[i].setCenterTextColor(Color.WHITE);
			
			// enable rotation of the chart by touch
			pieChart[i].setRotationEnabled(false);
			pieChart[i].setDrawHoleEnabled(true);
			pieChart[i].setTouchEnabled(false);
			
			Legend legend = pieChart[i].getLegend();
			legend.setEnabled(false);
			
			// mChart.animateY(1500, Easing.EasingOption.EaseInOutQuad);
			// mChart.spin(2000, 0, 360);
			// mChart.setBackground(background);
	
	//		data.setValueTypeface(tf);
			
			
			pieChart[i].setCenterText(""); 
			
			switch(i){
			case 0:
				yVals = new ArrayList<Entry>();
				xVals = new ArrayList<String>();

				yVals.add(new Entry((float) 83, 0));
				xVals.add(""+lay_score[0]);
				yVals.add(new Entry((float) 17, 1));
				xVals.add("");


				dataSet = new PieDataSet(yVals, "");

				colors = new ArrayList<Integer>();
//				for (int c : ColorTemplate.JOYFUL_COLORS)
//					colors.add(c);
//				colors.add(Color.rgb(255, 0, 0));//ColorTemplate.getHoloBlue()
				colors.add(Color.rgb(255, 90, 121));
				colors.add(Color.rgb(236, 236, 236));				
				dataSet.setColors(colors);
//				dataSet.setColor(Color.rgb(255, 90, 121));
				data = new PieData(xVals, dataSet);
				
				pieChart[i].setHoleRadius(72f);
				//pieChart[i].setRotationAngle(0);				
				break;
			case 1:
				yVals = new ArrayList<Entry>();
				xVals = new ArrayList<String>();

				yVals.add(new Entry((float) 80, 0));
				xVals.add(""+lay_score[1]);
				yVals.add(new Entry((float) 20, 1));
				xVals.add("");
				//yVals.add(new Entry((float) 13, 1));
				//xVals.add("");

				dataSet = new PieDataSet(yVals, "");

				colors = new ArrayList<Integer>();
//				for (int c : ColorTemplate.COLORFUL_COLORS)
//					colors.add(c);
//				colors.add(Color.rgb(0, 255, 0));				
				colors.add(Color.rgb(0, 255, 90));
				colors.add(Color.rgb(221, 221, 221));
				dataSet.setColors(colors);				
//				dataSet.setColor(Color.rgb(221, 221, 221));				
//				dataSet.setColor(Color.rgb(0, 255, 90));				
				data = new PieData(xVals, dataSet);
				
				pieChart[i].setHoleRadius(60f);
				//pieChart[i].setRotationAngle(-180*360/100);
				break;
			case 2:
				yVals = new ArrayList<Entry>();
				xVals = new ArrayList<String>();

				yVals.add(new Entry((float) 85, 0));
				xVals.add(""+lay_score[2]);
				yVals.add(new Entry((float) 15, 1));
				xVals.add("");
				//yVals.add(new Entry((float) 13, 1));
				//xVals.add("");

				dataSet = new PieDataSet(yVals, "");

				colors = new ArrayList<Integer>();
//				for (int c : ColorTemplate.VORDIPLOM_COLORS)
//					colors.add(c);
//				colors.add(Color.rgb(255, 255, 0));
				colors.add(Color.rgb(255, 210, 0));
				colors.add(Color.rgb(209, 209, 209));				
				dataSet.setColors(colors);				
//				dataSet.setColor(Color.rgb(255, 210, 0));
				data = new PieData(xVals, dataSet);
				
				pieChart[i].setHoleRadius(43f);
				//pieChart[i].setRotationAngle(-180*360/100);
				break;
			}
			data.setValueFormatter(new NonValueFormatter());
			data.setValueTextSize(11f);
			data.setValueTextColor(Color.WHITE);
			pieChart[i].setData(data);
		}
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()){
		case R.id.stat_back:
			statisPage.setVisibility(View.INVISIBLE);
			break;
		case R.id.detail_safe:
			focusColumn = 0;
			setTypeAndImage();
			detail_rela_sz[2].setVisibility(View.VISIBLE);
			break;
		case R.id.detail_economic:
			focusColumn = 1;
			setTypeAndImage();
			detail_rela_sz[2].setVisibility(View.VISIBLE);
			break;
		case R.id.detail_comfort: 
			focusColumn = 2;
			setTypeAndImage();
			detail_rela_sz[2].setVisibility(View.INVISIBLE);
			break;
		case R.id.drive_detail_rela_1:
			focusItem = focusColumn*3;
			setTypeContentActive();
			break;
		case R.id.drive_detail_rela_2:
			focusItem = focusColumn*3+1;
			setTypeContentActive();
			break;
		case R.id.drive_detail_rela_3:
			focusItem = focusColumn*3+2;
			setTypeContentActive();
			break;	
		case R.id.statis_btn:
			statisPage.setVisibility(View.VISIBLE);
			break;
		case R.id.per_month_l:
			setData(0);
			perMonthUnderL.setVisibility(View.VISIBLE);
			perDayUnderL.setVisibility(View.INVISIBLE);
			break;
		case R.id.per_day_l:
			setData(1);
			perMonthUnderL.setVisibility(View.INVISIBLE);
			perDayUnderL.setVisibility(View.VISIBLE);
			break;
		case R.id.start_stop_routine:
			if(startSwitch){
				baseAct.obdSensor.stopLocate();
				startSwitch = false;
				startStopSwitch.setText("开始行程");
			}
			else{
				if(!baseAct.obdSensor.startLocate()){
					return;
				}
				startSwitch = true;
				startStopSwitch.setText("结束行程");
			}
			break;
		}
	}
	
	void setTypeAndImage(){
		for(int i = 0; i < 3; i++){
			if(!(focusColumn == 2 && i == 2)){
				img_sz[i].setImageResource(imgIdArr[focusColumn][i]);
				tv_sz[i].setText(itemStrArr[focusColumn][i]);
				tv_v_sz[i].setText(tv_v_content_sz[focusColumn][i]);
				
				if(i == focusItem%3 && focusItem/3 == focusColumn){
					detail_rela_sz[i].setBackgroundResource(R.drawable.detail_button);
					img_sz[i].setImageResource(imgIdActArr[focusColumn][i]);
					tv_sz[i].setTextColor(mContext.getResources().getColor(R.color.white));
					tv_v_sz[i].setTextColor(mContext.getResources().getColor(R.color.white));
				}
				else{
					detail_rela_sz[i].setBackground(null);
					img_sz[i].setImageResource(imgIdArr[focusColumn][i]);
					tv_sz[i].setTextColor(mContext.getResources().getColor(R.color.black));
					tv_v_sz[i].setTextColor(mContext.getResources().getColor(R.color.black));
				}				
			}
			
			tv_v_sz[i].setText(itemVal[focusColumn][i] + tv_v_content_sz[focusColumn][i]);
			
			if(i == focusColumn)
				detailItmLine[i].setVisibility(View.VISIBLE);
			else
				detailItmLine[i].setVisibility(View.INVISIBLE);
		}
	}
	
	void setTypeContentActive(){
		for(int i = 0; i < 3; i++){		
			if(focusColumn == 2 && i == 2)
				break;
			if(i == focusItem%3 && focusItem/3 == focusColumn){
				detail_rela_sz[i].setBackgroundResource(R.drawable.detail_button);
				img_sz[i].setImageResource(imgIdActArr[focusColumn][i]);
				tv_sz[i].setTextColor(mContext.getResources().getColor(R.color.white));
				tv_v_sz[i].setTextColor(mContext.getResources().getColor(R.color.white));
			}
			else{
				detail_rela_sz[i].setBackground(null);
				img_sz[i].setImageResource(imgIdArr[focusColumn][i]);
				tv_sz[i].setTextColor(mContext.getResources().getColor(R.color.black));
				tv_v_sz[i].setTextColor(mContext.getResources().getColor(R.color.black));
			}
			
			tv_v_sz[i].setText(itemVal[focusColumn][i] + tv_v_content_sz[focusColumn][i]);
		}
	}
	
	public void clearDrawRoutine(){
		if(routineOverlay == null)
			return;
		for(int i = 0; i < routineOverlay.length; i++){
			if(routineOverlay[i] != null)
				routineOverlay[i].remove();
		}
		routineOverlay = null;
	}
	
	public void drawLastRoutine(){
		
		if(curTrace == null)
			return;
		int markCnt = 2 + overSpdLs.size() + brkLs.size() + turnLs.size() + gasLs.size();
		int markIdx = 0;
		int i = 0;
		LatLng startPt, endPt, latlng;
		DrivingEvent drvEvt;
		routineOverlay = new Overlay[markCnt];
		CoordinateConverter converter = new CoordinateConverter();
		converter.from(CoordType.GPS); 
		converter.coord(new LatLng(curTrace.startLat, curTrace.startLon));
		startPt = converter.convert();
		if(startPt.longitude < 60 || startPt.longitude > 180)
			return;
		Base.baidu_v.mBaiduMap.setMapStatus(MapStatusUpdateFactory
				.newLatLng(startPt));
		routineOverlay[markIdx++] = Base.baidu_v.mBaiduMap.addOverlay(new MarkerOptions().position(startPt)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.st_mk)));
		converter.coord(new LatLng(curTrace.endLat, curTrace.endLon));
		endPt = converter.convert();
		routineOverlay[markIdx++] = Base.baidu_v.mBaiduMap.addOverlay(new MarkerOptions().position(endPt)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.en_mk)));
		for(i = 0; i < overSpdLs.size(); i++){
			drvEvt = overSpdLs.get(i);
			converter.coord(new LatLng(drvEvt.m_loc_lat, drvEvt.m_loc_lon));
			latlng = converter.convert();
			routineOverlay[markIdx++] = Base.baidu_v.mBaiduMap.addOverlay(new MarkerOptions().position(endPt)
	                .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_site_exceed)));
		}
		for(i = 0; i < brkLs.size(); i++){
			drvEvt = overSpdLs.get(i);
			converter.coord(new LatLng(drvEvt.m_loc_lat, drvEvt.m_loc_lon));
			latlng = converter.convert();
			routineOverlay[markIdx++] = Base.baidu_v.mBaiduMap.addOverlay(new MarkerOptions().position(endPt)
	                .icon(BitmapDescriptorFactory.fromResource(R.drawable.brk_mk)));
		}
		for(i = 0; i < turnLs.size(); i++){
			drvEvt = overSpdLs.get(i);
			converter.coord(new LatLng(drvEvt.m_loc_lat, drvEvt.m_loc_lon));
			latlng = converter.convert();
			routineOverlay[markIdx++] = Base.baidu_v.mBaiduMap.addOverlay(new MarkerOptions().position(endPt)
	                .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_site_turn_around)));
		}
		for(i = 0; i < gasLs.size(); i++){
			drvEvt = overSpdLs.get(i);
			converter.coord(new LatLng(drvEvt.m_loc_lat, drvEvt.m_loc_lon));
			latlng = converter.convert();
			routineOverlay[markIdx++] = Base.baidu_v.mBaiduMap.addOverlay(new MarkerOptions().position(endPt)
	                .icon(BitmapDescriptorFactory.fromResource(R.drawable.gas_mk)));
		}

	}
	
	
	
	public boolean exitDrvBhv(){	
		if(statisPage.getVisibility() == View.VISIBLE){
			statisPage.setVisibility(View.INVISIBLE);
			return true;
		}
		clearDrawRoutine();
		return false;
	}
	
	//second page
	private void initStatisticPieChart() {
		
		ArrayList<Entry> yVals;
		List<String> xVals;
		PieDataSet dataSet;
		ArrayList<Integer> colors;
		PieData data = null;
				
			
		scorePie.setDescription("");
	//		mChart.setHoleRadius(30f);
	//		mChart.setTransparentCircleRadius(0f);
		scorePie.setCenterTextSize(18f);
			
			// mChart.setDrawXValues(true);
		scorePie.setUsePercentValues(false);
		scorePie.setDrawHoleEnabled(true);
		scorePie.setHoleColorTransparent(true);
	
		//pieChart[i].setTransparentCircleColor(Color.WHITE);
			
			
	//		mChart.setTransparentCircleRadius(53f);
	
		scorePie.setDrawCenterText(true);
		scorePie.setCenterTextColor(Color.WHITE);
			
		// enable rotation of the chart by touch
		scorePie.setRotationEnabled(false);
		scorePie.setDrawHoleEnabled(true);
		scorePie.setTouchEnabled(false);
			
		Legend legend = scorePie.getLegend();
		legend.setEnabled(false);
		
		// mChart.animateY(1500, Easing.EasingOption.EaseInOutQuad);
		// mChart.spin(2000, 0, 360);
		// mChart.setBackground(background);

//		data.setValueTypeface(tf);
			
			
		scorePie.setCenterText(""); 
			
			
		yVals = new ArrayList<Entry>();
		xVals = new ArrayList<String>();

		yVals.add(new Entry((float) lay_score[0], 0));
		xVals.add(""+lay_score[0]);
		yVals.add(new Entry((float) lay_score[1], 1));
		xVals.add(""+lay_score[1]);
		yVals.add(new Entry((float) lay_score[2], 2));
		xVals.add(""+lay_score[2]);
		yVals.add(new Entry((float) lay_score[3], 3));
		xVals.add(""+lay_score[3]);

		dataSet = new PieDataSet(yVals, "");

		colors = new ArrayList<Integer>();
//				for (int c : ColorTemplate.JOYFUL_COLORS)
//					colors.add(c);
//				colors.add(Color.rgb(255, 0, 0));//ColorTemplate.getHoloBlue()
		colors.add(Color.parseColor("#42DF88"));
		colors.add(Color.parseColor("#00D8FF"));
		colors.add(Color.parseColor("#FFDC51"));
		colors.add(Color.parseColor("#FF4C4C"));		
		dataSet.setColors(colors);
//				dataSet.setColor(Color.rgb(255, 90, 121));
		data = new PieData(xVals, dataSet);
		
		scorePie.setHoleRadius(56f);
		//pieChart[i].setRotationAngle(0);				
			
		data.setValueFormatter(new NonValueFormatter());
		data.setValueTextSize(11f);
		data.setValueTextColor(Color.WHITE);
		scorePie.setData(data);
		
	}

	
    void initLineChart(){
//      LimitLine llXAxis = new LimitLine(10f, "Index 10");
//      llXAxis.setLineWidth(4f);
//      llXAxis.enableDashedLine(10f, 10f, 0f);
//      llXAxis.setLabelPosition(LimitLabelPosition.POS_RIGHT);
//      llXAxis.setTextSize(10f);
//      llXAxis.setTextColor(Color.WHITE);
      XAxis xAxis = mChart.getXAxis();
      xAxis.enableGridDashedLine(10f, 10f, 0f);
      xAxis.setTextColor(Color.WHITE);
      //xAxis.setValueFormatter(new MyCustomXAxisValueFormatter());
      //xAxis.addLimitLine(llXAxis); // add x-axis limit line

      Typeface tf = Typeface.createFromAsset(mContext.getAssets(), "OpenSans-Regular.ttf");

//      LimitLine ll1 = new LimitLine(130f, "Upper Limit");
//      ll1.setLineWidth(4f);
//      ll1.enableDashedLine(10f, 10f, 0f);
//      ll1.setLabelPosition(LimitLabelPosition.POS_LEFT);
//      ll1.setTextSize(10f);
//      //ll1.setTypeface(tf);
//
//      LimitLine ll2 = new LimitLine(-30f, "Lower Limit");
//      ll2.setLineWidth(4f);
//      ll2.enableDashedLine(10f, 10f, 0f);
//      ll2.setLabelPosition(LimitLabelPosition.POS_RIGHT);
//      ll2.setTextSize(10f);
//      //ll2.setTypeface(tf);

      YAxis leftAxis = mChart.getAxisLeft();
      leftAxis.removeAllLimitLines(); // reset all limit lines to avoid overlapping lines      
//      leftAxis.addLimitLine(ll1);
//      leftAxis.addLimitLine(ll2);
      leftAxis.setAxisMaxValue(120);
      leftAxis.setAxisMinValue(-20);
      leftAxis.setStartAtZero(false);
      leftAxis.setTextColor(Color.WHITE);
      //leftAxis.setYOffset(20f);
      leftAxis.enableGridDashedLine(10f, 10f, 0f);

      // limit lines are drawn behind data (and not on top)
//      leftAxis.setDrawLimitLinesBehindData(true);

      mChart.getAxisRight().setEnabled(false);
 //     mChart.setDescription("2015/10");
      //mChart.getViewPortHandler().setMaximumScaleY(2f);
      //mChart.getViewPortHandler().setMaximumScaleX(2f);

      // add data
      setData(0);

//      mChart.setVisibleXRange(20);
//      mChart.setVisibleYRange(20f, AxisDependency.LEFT);
//      mChart.centerViewTo(20, 50, AxisDependency.LEFT);

      mChart.animateX(2500, Easing.EasingOption.EaseInOutQuart);
//      mChart.invalidate();

      // get the legend (only possible after setting data)
      Legend l = mChart.getLegend();

      // modify the legend ...
      // l.setPosition(LegendPosition.LEFT_OF_CHART);
      l.setForm(LegendForm.LINE);
  }
  
  private void setData(int perMonthOrDate) {

      ArrayList<String> xVals = new ArrayList<String>();
      int count = 0;
      int lay_data[]=null;
      int x_max = 0;
	  Calendar cal = Calendar.getInstance(); 
	  cal.setTime(new Date(System.currentTimeMillis()));
      if(perMonthOrDate == 0){
    	  lay_data = lay_m;
    	  x_max = 12;
    	  mChart.setDescription(cal.get(Calendar.YEAR)+"");
      }
      else{
    	  x_max = cal.getActualMaximum(Calendar.DATE);//当前月有几天
    	  lay_data = lay_d;
    	  mChart.setDescription(cal.get(Calendar.YEAR)+"/"+(cal.get(Calendar.MONTH)+1));
      }
      
      count = lay_data.length;
      
      for (int i = 0; i < x_max; i++) {
          xVals.add((i+1) + "");
      }

      ArrayList<Entry> yVals = new ArrayList<Entry>();

      for (int i = 0; i < count; i++) {

          //float mult = (range + 1);
          //float val = (float) (Math.random() * mult) + 3;// + (float)
          // ((mult *
          // 0.1) / 10);
          yVals.add(new Entry(lay_data[i], i));
      }

      // create a dataset and give it a type
      LineDataSet set1 = new LineDataSet(yVals, "");
      // set1.setFillAlpha(110);
      // set1.setFillColor(Color.RED);

      // set the line to be drawn like this "- - - - - -"
//      set1.enableDashedLine(10f, 5f, 0f);
      //set1.enableDashedHighlightLine(10f, 5f, 0f);
      set1.setColor(Color.BLACK);
      set1.setCircleColor(Color.BLACK);
      set1.setLineWidth(1f);
      set1.setCircleSize(3f);
      set1.setDrawCircleHole(true);
      set1.setValueTextSize(9f);
      set1.setFillAlpha(65);
      set1.setFillColor(Color.BLACK);        
//      set1.setDrawFilled(true);
      // set1.setShader(new LinearGradient(0, 0, 0, mChart.getHeight(),
      // Color.BLACK, Color.WHITE, Shader.TileMode.MIRROR));

      ArrayList<LineDataSet> dataSets = new ArrayList<LineDataSet>();
      dataSets.add(set1); // add the datasets

      // create a data object with the datasets
      LineData data = new LineData(xVals, dataSets);

      // set data
      mChart.setData(data); 
      mChart.invalidate();
  }


	
}