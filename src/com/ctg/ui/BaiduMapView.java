package com.ctg.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ZoomControls;

import com.baidu.lbsapi.auth.LBSAuthManagerListener;
import com.baidu.location.BDGeofence;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.BDLocationStatusCodes;
import com.baidu.location.GeofenceClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.CircleOptions;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.map.Stroke;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.mapapi.search.sug.OnGetSuggestionResultListener;
import com.baidu.mapapi.search.sug.SuggestionResult;
import com.baidu.mapapi.search.sug.SuggestionSearch;
import com.baidu.mapapi.utils.DistanceUtil;
import com.baidu.navisdk.BNaviEngineManager.NaviEngineInitListener;
import com.baidu.navisdk.BNaviPoint;
import com.baidu.navisdk.BaiduNaviManager;
import com.baidu.navisdk.util.verify.BNKeyVerifyListener;
import com.ctg.crash.LogRecord;
import com.ctg.group.ChatMsgEntity;
import com.ctg.group.Group;
import com.ctg.group.GrpDetailDlg;
import com.ctg.group.Member;
import com.ctg.land.Cascade;
import com.ctg.net.CacheManager;
import com.ctg.net.HttpQueue;
import com.ctg.net.IHttpCallback;
import com.ctg.service.CarDataService;
import com.ctg.shareUserInfo.UserPos;
import com.ctg.shareUserInfo.UserTrace;
import com.ctg.util.AddFenceDlg;
import com.ctg.util.CustomDialog;
import com.ctg.util.GetPinyin;
import com.ctg.util.HorizontalListView;
import com.ctg.util.HorizontalListViewAdapter;
import com.ctg.util.MyGeoFenceCont;
import com.ctg.util.PoiLstDlg;
import com.ctg.util.Preference;
import com.ctg.util.SearchPoiDlg;
import com.ctg.util.Util;
import com.ctg.weather.WeatherInfo;


public class BaiduMapView extends LinearLayout implements
		OnGetGeoCoderResultListener, Runnable, View.OnClickListener{
	/** SDK验证密钥 */
	private final static String ACCESS_KEY = "C5hXmzBUptLOYKVhHF5ifeCc";//"18UzTFgyGvrzKRHqQau3DjXe"; zhewu's id
	private static final String CATEGORY_SDK_DEMO = "android.intent.category.BAIDUNAVISDK_DEMO";
	final private String TAG = "BaiduMapView";
	public final static int BAIDU_CLIENT_LOC_START = 0x1000;
	public final static int Refresh_Share_Mode = 0x1001;
	
	public final static String Refresh_Type = "refresh_type";//0x1-- refresh icon; 0x10 refresh pos; 0x11 refresh both
	public final static int Refresh_Gap = 30000;
	public GeoCoder mSearch = null;
	public BaiduMap mBaiduMap = null;
	boolean hasLocated = false;
	public MapView mMapView = null;
	ZoomControls zoomControls;
	public FrameLayout mFrame;
	public LinearLayout mLinear;
	public Overlay myHeadMarker;
	Overlay selectPoiMarker;
	Overlay gfenceMarker;
	Overlay pickupGfenceMarker;
//	public Bitmap myBitmap;
	Base baseAct;
	// location
//	LocationClient mLocationClient;
	TextView LocationResult;
	
	// geofence
	public GeofenceClient mGeofenceClient;
	private AddGeofenceListener listener;
	private Geofence fence;

	private boolean mIsEngineInitSuccess = false;
//	public boolean geofenceMode;

	public LocationManager locationManager;
	public OBDLocationListener mLocationListener;
	public PoiSearch mPoiSearch;
	public int mPoiSearchTotalPageNum;
	public int mPoiSearchCurPage;
	// public MKSearch mkSearch;
	public String mCity;
	public String pureCity; 
	public String mCityPY;
	public String curPoiName;
	public double mCurLatitude = 0.0, mCurLongitude = 0.0;
	LatLng mCurlatlng;
	public String mCurAddress;
	public double mClickLatitude, mClickLongitude;
	public AddFenceDlg addFenceDlg;
	public SearchPoiDlg searchDlg;
	public PoiLstDlg poiLstDlg;
	public String poiName;
	public SuggestionSearch mSuggestionSearch;

	public LinearLayout top_bar_l;
	public RelativeLayout search_rela;
	public ImageView  search_icon;
	public EditText search_edit;
//	public ArrayList<MyGeoFenceCont> fenceList;
	public MyGeoFenceThread myGeofenceThd;
	public boolean myGeoFenceThdRun;
	AlertDialog fenceNoticeDlg;
	public boolean runInstantUploadGPS;
	public boolean isFirstStart;
//	public Semaphore semp;
	
//	Thread myThread;
	double x;
	double y;
	public BDLocation bdlocation;
	ListView grp_member_exam;
	ImageView grp_img;
	
	public int gfenceState; //0 nothing; 1 get location; 2 edit ; 3 browse
	public int isSelectAddrMode;//0 navit; 1 set home addr; 2 set company addr; 3 set gfence addr; 4 select point
	public TextView fenceStateTv;
	public boolean isGrpShareMode;
	
	Timer myShareModeTimer;
	public View grp_share_v;
	public View grp_share_back;
	public View grp_share_member_detail;
	public TextView share_num_tv;
	public HorizontalListView honLv;
	public HorizontalListViewAdapter honAdapter;
	
	LinearLayout gfence_l;
	TextView gfence_title;
	TextView gfence_del;
	EditText gfence_name;	
	boolean gfence_name_from_poi;
	public EditText gfence_addr;
	View gfence_addr_search;
	EditText gfence_dura;
	EditText gfence_radius_def;
	TextView radius_500;
	TextView radius_700;
	TextView radius_800;
	TextView radius_1000;
	TextView lastClickRadius;
	View gfence_save;
	View gfence_back;
	public MyGeoFenceCont myGfence;
	int gFenceRadius;
	public int gfenceIdx;
	public Member curMember;
	public Group curGrp;
	Bitmap dftbmp;
	public Dialog exitShareModeDlg;
	public Dialog enterShareModeDlg;
	public CustomDialog pickupChoiceDlg;
	public CustomDialog receivePickupDlg;
	public CustomDialog pickupReplyDlg;
	public CustomDialog pickupDurationDlg;
	public boolean needFailMsg;
		
	public int pickupMode;//0 nothing 1 active 2 passive
	public int inviteType;//0 be picked up,  1 pick up
	
	public String receiveGrpNm;
	public String receiveInvitor = "";
	public int receiveInviteType;
	public double receiveLat;
	public double recevielng;
	
	public String replyGrpNm;
	public String replyUser = "";
	public int replyResult;
	public int replyType;
	public double replyLat;
	public double replylng;	
	public InfoWindow mInfoWindow;
	public InfoWindow gfenceInfoWin;
	EditText pickup_radius_def;
	TextView pickup_radius_500;
	TextView pickup_radius_1000;
	TextView pickup_lastClickRadius;
	TextView pickup_fenceSave;
	TextView pickup_fenceDel;
	
	View jumpToCenter;
	public boolean ifSearchAddrShow;
	public RelativeLayout searchAddrRela;
	public TextView searchAddrCont;
	public TextView searchAddrDist;
	public View searchAddrBtn;
	LatLng searchAddrLatlng;
	
	public BaiduMapView(Context context){
		this(context, -1, -1);
	}
	
	public BaiduMapView(Context context, AttributeSet attrs){
		this(context, -1, -1);		
	}	
	
	public BaiduMapView(Context context, double x, double y) {
		super(context);
		this.x = x;
		this.y = y;

		baseAct = (Base) context;
		dftbmp = BitmapFactory.decodeResource(baseAct.getResources(),R.drawable.geo_wht);
		mCity = baseAct.getResources().getString(R.string.default_city_shanghai);
		Base.myCity = mCity;
//		if(baseAct.headbitmap != null)
//			myBitmap = Util.getRoundedCornerImageColor(baseAct.headbitmap, 50*Base.mDensityInt, 50*Base.mDensityInt, 0xe700ffff);	
		//
		SDKInitializer.initialize(Base.OBDApp);
		// 初始化导航引擎
//		BaiduNaviManager.getInstance().initEngine((Activity) baseAct,
//                Base.getSDPath(), mNaviEngineInitListener, ACCESS_KEY,
//                mKeyVerifyListener);
        BaiduNaviManager.getInstance().initEngine((Activity)baseAct, Base.getSDPath(),
                mNaviEngineInitListener, new LBSAuthManagerListener() {
                    @Override
                    public void onAuthResult(int status, String msg) {
                        String str = null;
                        if (0 == status) {
                            str = "key校验成功!";
                        } else {
                            str = "key校验失败, " + msg;
                        }
                    }
                });
       
    	mFrame = (FrameLayout) View.inflate(context, R.layout.baidumap, null);
//		LayoutParams params = new LayoutParams(Base.mWidth, Base.mHeight);
//		this.setLayoutParams(params);
      
        addView(mFrame);
        searchAddrRela = (RelativeLayout) mFrame.findViewById(R.id.search_addr_rela);
        searchAddrCont = (TextView)mFrame.findViewById(R.id.search_addr_cont);
        searchAddrDist = (TextView)mFrame.findViewById(R.id.search_addr_distance);
        searchAddrBtn = (TextView)mFrame.findViewById(R.id.search_addr_btn);
        if(searchAddrBtn != null)
        	searchAddrBtn.setOnClickListener(this);
        top_bar_l = (LinearLayout) mFrame.findViewById(R.id.topbar);
		search_rela = (RelativeLayout) mFrame.findViewById(R.id.mapview_search_rela);
		search_icon = (ImageView) mFrame.findViewById(R.id.mapview_search_icon);
		search_edit = (EditText) mFrame.findViewById(R.id.mapview_search_edit);
		if(search_edit != null)
			search_edit.setOnEditorActionListener(new EditText.OnEditorActionListener(){
				
				@Override
				public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
					// TODO Auto-generated method stub
					if(actionId==EditorInfo.IME_ACTION_DONE) {        
					// 点击了键盘完成或Enter按钮，发现有些手机不支持，所以注释掉了      
		                searchDlg = new SearchPoiDlg(baseAct, Base.mWidth,
		                        Base.mHeight, R.layout.search_poi,
		                        R.style.Theme_dialog);
		                searchDlg.show();
		                isSelectAddrMode = 4;
					}
					return false;
				}
	        	
	        });
		search_rela.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                
                searchDlg = new SearchPoiDlg(baseAct, Base.mWidth,
                        Base.mHeight, R.layout.search_poi,
                        R.style.Theme_dialog);
                searchDlg.show();
                isSelectAddrMode = 4;
                InputMethodManager imm = (InputMethodManager) baseAct.getSystemService(Context.INPUT_METHOD_SERVICE); 
	            // 接受软键盘输入的编辑文本或其它视图 
	            imm.showSoftInput(searchDlg.poi_edit,InputMethodManager.SHOW_FORCED); 
            }

        });
		
		mMapView = (MapView) mFrame.findViewById(R.id.bmapView);
		
		mMapView.showZoomControls(true);
		zoomControls = (ZoomControls) mMapView.getChildAt(2);
        //mMapView.removeViewAt(2);		        
        jumpToCenter = findViewById(R.id.jump_to_center);
        if(Base.OBDApp.landScapeMode == 0)
        	zoomControls.setPadding(0, 0, 0, 40*Base.mDensityInt);
//        else
//        	zoomControls.setPadding(0, 0, 60*Base.mDensityInt, 60*Base.mDensityInt);
        if(jumpToCenter != null)
        	jumpToCenter.setOnClickListener(this);
		grp_share_v = findViewById(R.id.grp_share);
		grp_share_back = findViewById(R.id.grp_back);
		grp_share_member_detail = findViewById(R.id.home_group);
		share_num_tv = (TextView) findViewById(R.id.share_num);
		honLv = (HorizontalListView) findViewById(R.id.grp_head_examp);
		if(grp_share_back != null)
			grp_share_back.setOnClickListener(this);
		if(grp_share_member_detail != null)
			grp_share_member_detail.setOnClickListener(this);

		if(mMapView != null){
			mBaiduMap = mMapView.getMap();
			mBaiduMap.setMapStatus(MapStatusUpdateFactory.zoomTo(15f));


			BaiduMapOnClick mapClick = new BaiduMapOnClick(); 
			mBaiduMap.setOnMarkerClickListener(mapClick);
			mBaiduMap.setOnMapClickListener(mapClick);
		}
			// 初始化搜索模块，注册事件监听
		mSearch = GeoCoder.newInstance();
		mSearch.setOnGetGeoCodeResultListener(this);

		// location
//		mLocationClient = Base.OBDApp.mLocationClient;
		// LocationResult = (TextView)findViewById(R.id.text_loc);
		Base.OBDApp.mLocationResult = LocationResult;

		// geofence
		mGeofenceClient = Base.OBDApp.mGeofenceClient;
		fence = new Geofence();
		mGeofenceClient.registerGeofenceTriggerListener(fence);
		listener = new AddGeofenceListener();

		init();
//		initFenceData();
		mPoiSearch = PoiSearch.newInstance();
		mPoiSearch.setOnGetPoiSearchResultListener(poiListener);
		mSuggestionSearch = SuggestionSearch.newInstance();
		mSuggestionSearch.setOnGetSuggestionResultListener(suggestionListener);

		myGeofenceThd = new MyGeoFenceThread();
		if(Base.OBDApp.landScapeMode > 0){//横屏
			//grp_img = (ImageView) mFrame.findViewById(R.id.mapview_grp_icon);	
			//grp_member_exam = (ListView) mFrame.findViewById(R.id.grp_lv_exam);	
			//grp_img.setOnClickListener(this);
		}
		else{
//			fenceStateTv = (TextView) findViewById(R.id.gfence_add_notice);
//			fenceStateTv.setVisibility(View.INVISIBLE);
		}
		
		if(Base.OBDApp.landScapeMode == 1)
			return;
		
		initGeofenceView();

	}
	
	void initGeofenceView(){
		gfence_l = (LinearLayout) findViewById(R.id.gfence_linear);
		gfence_title = (TextView) findViewById(R.id.gfence_title);
		gfence_del = (TextView) findViewById(R.id.gfence_del);
		gfence_back = findViewById(R.id.gfence_back);
		gfence_save = findViewById(R.id.gfence_save);
		gfence_name = (EditText) findViewById(R.id.gfence_name);
		gfence_addr = (EditText) findViewById(R.id.gfence_addr);
		gfence_dura = (EditText) findViewById(R.id.gfence_dura);
		gfence_radius_def = (EditText) findViewById(R.id.gfence_radius_def);
		radius_500 = (TextView) findViewById(R.id.gfence_radi_500);
		radius_700 = (TextView) findViewById(R.id.gfence_radi_700);
		radius_800 = (TextView) findViewById(R.id.gfence_radi_800);
		radius_1000 = (TextView) findViewById(R.id.gfence_radi_1000);
		gfence_addr_search = findViewById(R.id.gfence_addr_search);
		gfence_back.setOnClickListener(this);
		gfence_save.setOnClickListener(this);
		radius_500.setOnClickListener(this);
		radius_700.setOnClickListener(this);
		radius_800.setOnClickListener(this);
		radius_1000.setOnClickListener(this);
		gfence_radius_def.setOnClickListener(this);
		gfence_addr_search.setOnClickListener(this);
		gfence_del.setOnClickListener(this);
		gfence_addr.setOnEditorActionListener(new TextView.OnEditorActionListener(){

			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				// TODO Auto-generated method stub
				if(event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER){
					Base.hideSoftKeyboard((Activity)baseAct);
					String searchText = gfence_addr.getText().toString();
					Base.baidu_v.poiName = searchText;
					if(searchText != null && !searchText.equals("")){
						Base.baidu_v.mPoiSearch.searchInCity((new PoiCitySearchOption())  
							    .city(Base.baidu_v.mCity)  
							    .keyword(searchText)  
							    .pageNum(0));
						isSelectAddrMode = 3;
					}
					return true;
				}
				return false;
			}});	
		gfence_radius_def.addTextChangedListener(new TextWatcher(){

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// TODO Auto-generated method stub
				if(s.toString() != null && !s.toString().equals("") && gfenceState != 0 && Math.abs(myGfence.lat) > 0.000001){
					gFenceRadius = Integer.parseInt(s.toString());		
					addGeofenceMarker();
				}
				if(lastClickRadius != null){
					lastClickRadius.setBackgroundColor(0xffffffff);
					lastClickRadius.setTextColor(0xff000000);
				}
			}

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				
			}
			
		});
		gfence_radius_def.setOnEditorActionListener(new TextView.OnEditorActionListener(){

			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				// TODO Auto-generated method stub
				if(event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER ||
						event.getKeyCode() == KeyEvent.KEYCODE_BACK)){
					String radiusText = gfence_radius_def.getText().toString();
					if(radiusText != null && !radiusText.equals("")){
						myGfence.radius = Integer.parseInt(radiusText);
						if(gfenceState == 1){
							addGeofenceMarker();
						}
					}
				}
				if(lastClickRadius != null){
					lastClickRadius.setBackgroundColor(0xffffffff);
					lastClickRadius.setTextColor(0xff000000);
				}
				return false;
			}});
		
	}
	
//	void initFenceData(){
//		String obdii_path = Base.getSDPath() +"/OBDII";
//		String fence_path = obdii_path + "/geofence";
//		fenceList = new ArrayList<MyGeoFenceCont>();
//		try {
//			FileInputStream fence_in = new FileInputStream(fence_path);
//			MyGeoFenceCont fence = null;
//			if(fence_in != null){
//				ObjectInputStream obj_in = new ObjectInputStream(fence_in);												
//				while((fence = (MyGeoFenceCont) obj_in.readObject()) != null){
//					fenceList.add(fence);
////					addCustomElementsDemo(fence.lat, fence.lon,fence.radius);
//				}
//				fence_in.close();
//			}		
//		}
//		catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (ClassNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//	}
	
	public void searchAddrDisplay(boolean show, String name, LatLng latlng){
		ifSearchAddrShow = show;
		if(show){
			mBaiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(latlng));
			selectPoiMarker =  mBaiduMap.addOverlay(new MarkerOptions().position(latlng)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_geographic_red_l)));			
			if(Base.OBDApp.landScapeMode == 0){
				//baseAct.id_bottombar.setVisibility(View.GONE);				
		        zoomControls.setPadding(0, 0, 0, 55*Base.mDensityInt);
		        jumpToCenter.setY(jumpToCenter.getY()-20*Base.mDensityInt);
			}
	        searchAddrRela.setVisibility(View.VISIBLE);
	        searchAddrCont.setText(name);
	        LatLng curLatlng = new LatLng(mCurLatitude, mCurLongitude);
	        double dist_d = (float) DistanceUtil.getDistance(curLatlng, latlng);
	        String dist_str = "";
	        if(dist_d > 1000)
	        	dist_str = String.format("%.2fkm", dist_d/1000);
	        else
	        	dist_str = String.format("%dm", Math.round(dist_d));
	        searchAddrDist.setText(dist_str);
	        searchAddrLatlng = latlng;
		}
		else{
			if(selectPoiMarker != null){
				selectPoiMarker.remove();
				selectPoiMarker = null;
			}
			if(Base.OBDApp.landScapeMode == 0){
				//baseAct.id_bottombar.setVisibility(View.VISIBLE);				
		        zoomControls.setPadding(0, 0, 0, 40*Base.mDensityInt);
		        jumpToCenter.setY(jumpToCenter.getY()+20*Base.mDensityInt);
			}
			searchAddrRela.setVisibility(View.INVISIBLE);
		}
	}
		
	public void onDestroy(){
//		saveFenceData();
		if(mCurLatitude> 0.000001 || mCurLatitude < 0.000001){
			Preference.getInstance(baseAct.getApplicationContext())
				.setPointLatLng(new LatLng(mCurLatitude, mCurLongitude));
		}
	}
	
	public void enterFenceAddMode(){		
		gfenceState = 1;
		myGfence = new MyGeoFenceCont();
		myGfence.radius = 500;
		myGfence.lat = mCurLatitude;
		myGfence.lon = mCurLongitude;
		gfence_l.setVisibility(View.VISIBLE);
		gfence_del.setVisibility(View.GONE);
		gfence_title.setText("添加围栏");
		gfence_addr.setFocusable(true);
		gfence_addr.setText(mCurAddress);
		gfence_name.setText("");
		gfence_dura.setText("");
		radius_500.setBackground(baseAct.gfencebackdraw);
		radius_500.setTextColor(0xffffffff);
//		mBaiduMap.clear();
		lastClickRadius = radius_500;
		mBaiduMap.setMapStatus(MapStatusUpdateFactory.newLatLngZoom(new LatLng(mCurLatitude, mCurLongitude), 15));
		//addCustomElementsDemo(mCurLatitude, mCurLongitude, 500);
		gFenceRadius = 500;
		addGeofenceMarker();
		//baseAct.id_bottombar.setVisibility(View.GONE);	
		zoomControls.setPadding(0, 0, 0, 0);
//		jumpToCenter.setPadding(0, 0, 0, 0);
//        jumpToCenter.layout(jumpToCenter.getLeft(), jumpToCenter.getTop()+20*Base.mDensityInt,
//        		jumpToCenter.getRight(), jumpToCenter.getBottom()+20*Base.mDensityInt);
		jumpToCenter.setY(jumpToCenter.getY()+40*Base.mDensityInt);					
	}

	public void exitFenceAddMode(){
		if(gfenceState == 0)
			return;
		if(lastClickRadius != null){
			lastClickRadius.setBackgroundColor(0xffffffff);
			lastClickRadius.setTextColor(0xff000000);
		}
		lastClickRadius = null;
		gfenceState = 0;
		mBaiduMap.clear();
		gfence_l.setVisibility(View.INVISIBLE);
		baseAct.id_bottombar.setVisibility(View.VISIBLE);
		zoomControls.setPadding(0, 0, 0, 40*Base.mDensityInt);
		jumpToCenter.setY(jumpToCenter.getY()-40*Base.mDensityInt);
//		baseAct.navitDlg = new NavitInputDlg(baseAct, Base.mWidth,
//                Base.mHeight, R.layout.sub_navit_p,
//                R.style.Theme_dialog);
		baseAct.navitDlg.show();	
		baseAct.fenceDlg.show();
		baseAct.fenceDlg.fenceAdapt.setList();
//		mLocationClient.start();
	}
	
	public void enterFenceModeL(int pos){
		myGfence = CarDataService.fenceList.get(pos);
		LinearLayout gfenceInfoLinear = (LinearLayout) View.inflate(baseAct, R.layout.gfence_infowin, null);
		TextView nameT = (TextView) gfenceInfoLinear.findViewById(R.id.gfence_title);
		TextView addrT = (TextView) gfenceInfoLinear.findViewById(R.id.gfence_addr);
		TextView radiusT = (TextView) gfenceInfoLinear.findViewById(R.id.gfence_radi_500);
		View backImg = gfenceInfoLinear.findViewById(R.id.gfence_back);
		backImg.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				exitFenceModeL();
			}
		});
		
		nameT.setText(myGfence.name);
		addrT.setText(myGfence.address);
		radiusT.setText(""+myGfence.radius);
		gfenceState = 2;
		gFenceRadius = myGfence.radius;
		mBaiduMap.setMapStatus(MapStatusUpdateFactory.newLatLngZoom(new LatLng(myGfence.lat+0.0075, myGfence.lon), 15));
		gfenceInfoWin =  new InfoWindow(gfenceInfoLinear, new LatLng(myGfence.lat, myGfence.lon), -65*Base.mDensityInt);
		mBaiduMap.showInfoWindow(gfenceInfoWin);	
		addGeofenceMarker();
	}
	
	public void exitFenceModeL(){
		if(gfenceMarker != null)
			gfenceMarker.remove();
		mBaiduMap.hideInfoWindow();
		gfenceState = 0;
	}
	
	public void enterFenceEditMode(int pos){
		gfenceIdx = pos;
		gfenceState = 2;
		gfence_l.setVisibility(View.VISIBLE);
		gfence_del.setVisibility(View.VISIBLE);
		gfence_title.setText("设置围栏");
//		gfence_addr.setFocusable(false);
		lastClickRadius = null;
		myGfence = CarDataService.fenceList.get(pos);
		mBaiduMap.setMapStatus(MapStatusUpdateFactory.newLatLngZoom(new LatLng(myGfence.lat, myGfence.lon), 15));
		myGfence.visible = true;
		//addCustomElementsDemo(myGfence.lat, myGfence.lon,myGfence.radius);		
		gfence_name.setText(myGfence.name);
		gfence_addr.setText(myGfence.address);
		gfence_dura.setText(myGfence.dura);
		zoomControls.setPadding(0, 0, 0, 0);
        jumpToCenter.setY(jumpToCenter.getY()+40*Base.mDensityInt);
        if(myGfence.radius == 500){
			radius_500.setBackground(baseAct.gfencebackdraw);
			radius_500.setTextColor(0xffffffff);
			lastClickRadius = radius_500;
			gfence_radius_def.setText("");
		}
		else if(myGfence.radius == 700){
			radius_700.setBackground(baseAct.gfencebackdraw);
			radius_700.setTextColor(0xffffffff);
			lastClickRadius = radius_700;
			gfence_radius_def.setText("");
		}
		else if(myGfence.radius == 800){
			radius_800.setBackground(baseAct.gfencebackdraw);
			radius_800.setTextColor(0xffffffff);
			lastClickRadius = radius_800;
			gfence_radius_def.setText("");
		}
		else if(myGfence.radius == 1000){
			radius_1000.setBackground(baseAct.gfencebackdraw);
			radius_1000.setTextColor(0xffffffff);
			lastClickRadius = radius_1000;
			gfence_radius_def.setText("");
		}
		else{
			gfence_radius_def.setText(""+myGfence.radius);
		} 
        gFenceRadius = myGfence.radius;
        addGeofenceMarker();
		baseAct.id_bottombar.setVisibility(View.GONE);
	}

	public void exitFenceEditMode(){
		if(gfenceState == 0)
			return;
		if(lastClickRadius != null){
			lastClickRadius.setBackgroundColor(0xffffffff);
			lastClickRadius.setTextColor(0xff000000);
		}
		lastClickRadius = null;
		gfenceState = 0;
		gfence_l.setVisibility(View.INVISIBLE);
		baseAct.id_bottombar.setVisibility(View.VISIBLE);
//		baseAct.navitDlg = new NavitInputDlg(baseAct, Base.mWidth,
//                Base.mHeight, R.layout.sub_navit_p,
//                R.style.Theme_dialog);
		mBaiduMap.clear();	
		baseAct.navitDlg.show();
		baseAct.fenceDlg.show();
		baseAct.fenceDlg.fenceAdapt.setList();		
//		mLocationClient.start();
		zoomControls.setPadding(0, 0, 0, 40*Base.mDensityInt);
        jumpToCenter.setY(jumpToCenter.getY()-40*Base.mDensityInt);

	}
	
	public void getFencePoint(String destname, String destAddr, LatLng latlng){
//		if(gfence_name.getText().toString() == null || gfence_name.getText().toString().equals(""))
//		{	
//			gfence_name.setText(destname);
//			myGfence.name = destname;
//		}
		gfence_addr.setText(destAddr);
		myGfence.address = destAddr;
		myGfence.lat = latlng.latitude;
		myGfence.lon = latlng.longitude;
//		float curZoom = mBaiduMap.getMapStatus().zoom;
		mBaiduMap.setMapStatus(MapStatusUpdateFactory.newLatLngZoom(latlng, 15));
//		mBaiduMap.setMapStatus(MapStatusUpdateFactory.zoomTo(15f));
		OverlayOptions ooCircle = null;
//		gfenceState = 1;
		if(myGfence.radius == 0){
			ooCircle = new CircleOptions().fillColor(0x400087cb)
					.center(latlng).stroke(new Stroke(4, 0xee0087cb))
					.radius(500);
		}
		else{
			ooCircle = new CircleOptions().fillColor(0x400087cb)
					.center(latlng).stroke(new Stroke(4, 0xee0087cb))
					.radius(myGfence.radius);
		}
//		mBaiduMap.clear();
//		gfenceMarker = mBaiduMap.addOverlay(ooCircle);
		addGeofenceMarker();
	}
	
	public void addLocationMark(ChatMsgEntity msgEnti, Member member){
		int idx = honAdapter.gMemberList.indexOf(new Member(msgEnti.name));
//		Bitmap bmp = Util.getColoredImage(dftbmp, Member.color[idx%Member.colorCount]);	
//		Bitmap bmpLoc = BitmapFactory.decodeResource(baseAct.getResources(), R.drawable.drivehabit_area_img);
//		Bitmap bmp = Util.getIntegretedBitmap(member.headBitmap, 80, 80, bmpLoc);
		//ThumbnailUtils.extractThumbnail(member.headBitmap, 120, 120);
		Bitmap bmp = Util.getRoundedCornerImageColor(member.headBitmap, 120, 120, Member.color[idx%Member.colorCount]);	

		LatLng latlng = new LatLng(msgEnti.latlon_loc.latitude,msgEnti.latlon_loc.longitude);

		mBaiduMap.addOverlay(new MarkerOptions().position(latlng)
				.icon(BitmapDescriptorFactory.fromBitmap(bmp)));//fromBitmap(bmp)
		if(!bmp.isRecycled())
			bmp.recycle();
		mBaiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(msgEnti.latlon_loc));
		
	}
	

	Handler mShareModeHandler = new Handler(){
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			if(Base.OBDApp == null || Base.OBDApp.getActivityBack() != Base.APP_RUN_FOREGROUND){
				return;
			}
			Bundle bb = null;
			int refreshVal = 0;
			Bitmap bmp = null;
			boolean neadRecycle = false;
			switch(msg.what){
			case BAIDU_CLIENT_LOC_START:
//				if(mLocationClient != null)
//					mLocationClient.start();
				break;
			case Refresh_Share_Mode:
				bb = msg.getData();
				refreshVal = bb.getInt(Refresh_Type);					
				if((refreshVal & 0x10) != 0){
//					mBaiduMap.clear();
					for(Member member : honAdapter.gMemberList){
						if(member.marker != null)
							member.marker.remove();
						if(member.isInSharePosMode || member.name.equals(Base.loginUser)){
							if(member.name.equals(Base.loginUser)){
								
//								bmp = Util.getRoundedCornerImageColor(Base.headbitmap, 50*Base.mDensityInt, 50*Base.mDensityInt, 0xff01d4fb);
								if(Base.headbitmap != null){
									bmp = Base.myBitmap;
									member.latlon = new LatLng(mCurLatitude, mCurLongitude);
									neadRecycle = false;
								}
								
							}
							else{
								if(pickupMode == 1 && member.name.equals(replyUser)
								|| pickupMode == 2 && member.name.equals(receiveInvitor)){
									bmp = Util.getRoundedCornerImageColorTriangleExclamation(member.headBitmap, 50*Base.mDensityInt, 50*Base.mDensityInt, Member.color[member.shareModeListPos]);
									if(member.fenceActive)
										addGeofenceMarker(member);
								}
								else
									bmp = Util.getRoundedCornerImageColorTriangle(member.headBitmap, 50*Base.mDensityInt, 50*Base.mDensityInt, Member.color[member.shareModeListPos]);																
								neadRecycle = true;
							}
							if(bmp != null && member.latlon != null){
								MarkerOptions overLay = new MarkerOptions().position(member.latlon)
										.icon(BitmapDescriptorFactory.fromBitmap(bmp));	
								member.marker =  mBaiduMap.addOverlay(overLay);//fromBitmap(bmp)
								if(neadRecycle && !bmp.isRecycled())
									bmp.recycle();
								
							}
							
						}
						else
							member.marker = null;
						
					}
				}
				if((refreshVal & 0x01) != 0){
					if(honAdapter != null){
						honAdapter.notifyDataSetChanged();
					}
				}
				break;
			}
		}
	};
	@Override
	public synchronized void run() {
//		semp = new Semaphore(1);		
//		// TODO Auto-generated method stub
//		while(runInstantUploadGPS){			
//			try {
//				semp.acquire();
//				Thread.sleep(3000);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			
////			if(mLocationClient != null)
////				mLocationClient.start();
//			mShareModeHandler.obtainMessage(BAIDU_CLIENT_LOC_START).sendToTarget();
//		}
	}
	
	public void enterGpsShareMode(){
		isGrpShareMode = true;
		needFailMsg = true;
		grp_share_v.setVisibility(View.VISIBLE);
		search_rela.setVisibility(View.INVISIBLE);
		if(Base.OBDApp.landScapeMode == 0){
			baseAct.vPager.setCurrentItem(0, false);
			baseAct.id_bottombar.setVisibility(View.GONE);
			zoomControls.setPadding(0, 0, 0, 0);
	        jumpToCenter.setY(jumpToCenter.getY()+40*Base.mDensityInt);
		}
		if(Base.friendOrGrpIdx < HttpQueue.friendLst.size()){			
			curMember = HttpQueue.friendLst.get(Base.friendOrGrpIdx);
			curGrp = null;
			share_num_tv.setText("好友"+curMember.name);
		}
		else{
			curGrp = HttpQueue.grpResLst.get(Base.friendOrGrpIdx-HttpQueue.friendLst.size());			
			share_num_tv.setText("群组"+curGrp.name+"("+curGrp.memberList.size() +")人");
//			curMember = null;
		}
		honAdapter = new HorizontalListViewAdapter(baseAct);
		honLv.setAdapter(honAdapter);	
		mBaiduMap.clear();
//		startUploadGPS();
//		myShareModeTimer = new Timer();
//		myShareModeTimer.schedule(new TimerTask() {
//			@Override
//			public void run() {
//				// TODO Auto-generated method stub
//				long curTime = System.currentTimeMillis();
//				int refreshVal = 0;
//				
//				if(Base.friendOrGrpIdx >= HttpQueue.friendLst.size()){//this is group, need refresh myself
//					refreshVal = 0x10;
//				}
//				for(Member member : honAdapter.gMemberList){
//					if(member.isInSharePosMode){
//						if(member.posTime != 0 && Math.abs(curTime - member.posTime) > Refresh_Gap){
//							member.isInSharePosMode = false;							
//						}
//						refreshVal |= 0x11;
//					}
//					else{
//						if(member.posTime != 0 && Math.abs(curTime - member.posTime) <= Refresh_Gap){
//							member.isInSharePosMode = true;
//							refreshVal = 0x11;
//						}												
//					}
//					
//				}
//				if(refreshVal > 0){
//					Message msg = mShareModeHandler.obtainMessage(Refresh_Share_Mode);
//					Bundle bb = new Bundle();
//					bb.putInt(Refresh_Type, refreshVal);
//					msg.setData(bb);
//					mShareModeHandler.sendMessage(msg);
//				}
//
//			}
//			}, 0, 2000);				
	}
	
	public void exitGpsShareMode(){
		isGrpShareMode = false;
		pickupMode = 0;
		needFailMsg = false;
		
		grp_share_v.setVisibility(View.INVISIBLE);		
		search_rela.setVisibility(View.VISIBLE);
		if(Base.OBDApp.landScapeMode == 0){
			baseAct.vPager.setCurrentItem(1, false);
			baseAct.id_bottombar.setVisibility(View.INVISIBLE);
			zoomControls.setPadding(0, 0, 0, 40*Base.mDensityInt);
			jumpToCenter.setY(jumpToCenter.getY()-40*Base.mDensityInt);
		}
		stopUploadGPS();
		Base.friendOrGrpIdx = -1;
		mBaiduMap.clear();
		if(myShareModeTimer != null)
			myShareModeTimer.cancel();		
//		mLocationClient.start();
	}
	
	public boolean onTouchEvent(MotionEvent event) {
		// if(event.getAction() == MotionEvent.ACTION_MOVE)
		// return true;
		return false;
	}

	public boolean onInterceptTouchEvent(MotionEvent event) {
		// super.onInterceptTouchEvent(event);
		// mMapView.onTouchEvent(event);

		return false;

	}

	OnGetSuggestionResultListener suggestionListener = new OnGetSuggestionResultListener() {
		public void onGetSuggestionResult(SuggestionResult res) {
			if (res == null || res.getAllSuggestions() == null) {
				return;
				// 未找到相关结果
			}
			if (searchDlg != null) {
				searchDlg.poilistItem.clear();
				for (SuggestionResult.SuggestionInfo info : res
						.getAllSuggestions()) {
					searchDlg.poilistItem.add(info.key);
				}
				searchDlg.poiLstAdp.setList(searchDlg.poilistItem);
				// searchDlg.initSearchLst();
			}
			// 获取在线建议检索结果
		}
	};

	public Comparator<PoiInfo> poiComparator = new Comparator<PoiInfo>(){

		@Override
		public int compare(PoiInfo left, PoiInfo right) {
			// TODO Auto-generated method stub
			double d1 = DistanceUtil.getDistance(mCurlatlng, left.location);
			double d2 = DistanceUtil.getDistance(mCurlatlng, right.location);
			if(d1 < d2)
				return -1;
			else if(d1 > d2)
				return 1;
			else
				return 0;
		}
		
	};
	
	OnGetPoiSearchResultListener poiListener = new OnGetPoiSearchResultListener() {
		public void onGetPoiResult(PoiResult result) {
			// 获取POI检索结果
			if (searchDlg != null) {
				searchDlg.cancel();
			}
			if (result == null || result.getAllPoi() == null) {
				return;
			}
			int idx = 0;

			boolean isRefresh = false;
			if (poiLstDlg == null) {
				int height = baseAct.realHeight;
//				if(isSelectAddrMode == 0){
//					height = height*2/3;
//				}
				if(Base.OBDApp.landScapeMode == 0)
					poiLstDlg = new PoiLstDlg(baseAct, Base.mWidth, height,
							R.layout.poi_list, R.style.Theme_dialog);
				else
					poiLstDlg = new PoiLstDlg(baseAct, Base.mWidth*2/3, Base.mHeight,
							R.layout.poi_list, R.style.Theme_dialog);
			} else {
				poiLstDlg.poilistItem.clear();
				isRefresh = true;
			}

			mPoiSearchCurPage = result.getCurrentPageNum();
			mPoiSearchTotalPageNum = result.getTotalPageNum();
			if(poiLstDlg.dragdown_rela != null){
				if (result.getCurrentPageNum() > 0) {
					// View drag_down_v = View.inflate(baseAct,
					// R.layout.poi_dragdown, null);
					// poiLstDlg.poiScroll.addView(drag_down_v);
					poiLstDlg.dragdown_rela.setVisibility(View.VISIBLE);
					poiLstDlg.dragdown_progress.setVisibility(View.INVISIBLE);
					poiLstDlg.dragdown_text.setText(R.string.dragdown_notice);
				} else {
					poiLstDlg.dragdown_rela.setVisibility(View.GONE);
				}
			}
			Collections.sort(result.getAllPoi(), poiComparator);	
			double dist = 0.0;
			String dist_str = "";
			for (PoiInfo info : result.getAllPoi()) {				
				Map<String, Object> poi_map = new HashMap<String, Object>();
				poi_map.put("image", R.drawable.search_poi_01 + idx);
				poi_map.put("name", info.name);
				poi_map.put("address", info.address);
				poi_map.put("latlon", info.location);
				dist = DistanceUtil.getDistance(mCurlatlng, info.location);				
				if(dist > 1000)
					dist_str = String.format("%.1fkm", dist/1000);
				else
					dist_str = String.format("%dm", Math.round(dist));
				poi_map.put("distance", dist_str);
				poiLstDlg.poilistItem.add(poi_map);
				// poi_map.put("image", R.drawable.search_poi_01+idx);
				// poi_map.put("name", info.name);
				// poi_map.put("address", info.address);
				// poiLstDlg.poilistItem.add(poi_map);
				idx++;
			}
			// poiLstDlg.poiLstAdp.notifyDataSetChanged();

			if(poiLstDlg.dragdown_rela != null){
				if (result.getCurrentPageNum() < result.getTotalPageNum() - 1) {
					// View drag_up_v = View.inflate(baseAct, R.layout.poi_dragup,
					// null);
					// poiLstDlg.poiScroll.addView(drag_up_v);
					poiLstDlg.dragup_rela.setVisibility(View.VISIBLE);
					poiLstDlg.dragup_progress.setVisibility(View.INVISIBLE);
					poiLstDlg.dragup_text.setText(R.string.dragup_notice);
				} else {
					poiLstDlg.dragup_rela.setVisibility(View.INVISIBLE);
				}
			}
			if (isRefresh)
				poiLstDlg.poiLstAdp.setList(poiLstDlg.poilistItem);
				//poiLstDlg.poiLstAdp.notifyDataSetChanged();

			poiLstDlg.show();

		}

		public void onGetPoiDetailResult(PoiDetailResult result) {
			// 获取Place详情页检索结果
		}
	};

	private NaviEngineInitListener mNaviEngineInitListener = new NaviEngineInitListener() {
		public void engineInitSuccess() {
			mIsEngineInitSuccess = true;
		}

		public void engineInitStart() {
		}

		public void engineInitFail() {
			mIsEngineInitSuccess = false;
		}
	};

	private BNKeyVerifyListener mKeyVerifyListener = new BNKeyVerifyListener() {
		int i = 0;
		@Override
		public void onVerifySucc() {
			// TODO Auto-generated method stub
			// Toast.makeText(BaiduMapView.this, "key校验成功",
			// Toast.LENGTH_SHORT).show();
			i = 1;
		}

		@Override
		public void onVerifyFailed(int arg0, String arg1) {
			// TODO Auto-generated method stub
			// Toast.makeText(BaiduMapView.this, "key校验失败",
			// Toast.LENGTH_SHORT).show();
			i = -1;
		}
	};

	public Handler baiduLocationHander = new Handler(){
		public void handleMessage(Message msg) {
			switch(msg.what){
			case 1:
				Bundle bb = msg.getData();
				double lat = bb.getDouble("latitude");
				double lng = bb.getDouble("longitude");
				String city = bb.getString("city");
				String address = bb.getString("address");
				LatLng latlng = new LatLng(lat, lng);
				mCurlatlng = latlng;
				mCurLatitude = lat;
				mCurLongitude = lng;
				mCurAddress = address;
				if(runInstantUploadGPS){
					
					String dataStr = Util.getDate();
					String grpName = "";
					String url = Base.HTTP_GROUP_PATH+"/pushMessagesToGroupUsers";
					ChatMsgEntity curMsg = null;
					if(curGrp != null){
						grpName = curGrp.name;
					
						curMsg = new ChatMsgEntity(Base.loginUser, 
								grpName, dataStr, "",  1, ChatMsgEntity.CHAT_MSG_LOCATE,
								latlng);	
					}
					else //if(curMember != null)
					{
						ArrayList<String> lst = new ArrayList<String>();
						lst.add(curMember.name);
						curMsg = new ChatMsgEntity(Base.loginUser, 
								lst, dataStr, "",  1, ChatMsgEntity.CHAT_MSG_LOCATE,
								latlng);
					}
//						baseAct.httpQueueInstance.EnQueue(url, null, 20, curMsg);
					Map<String, String> postData = new HashMap<String, String>();

					
					try {
						postData.put("appID", "appid");
						postData.put("groupName", grpName);					
						
						JSONArray jsonarray = new JSONArray();
						if(curMsg.groupName != null && !curMsg.groupName.equals("")){
							int idx = Group.indexOfByName(HttpQueue.grpResLst, curMsg.groupName);
							Group grp = HttpQueue.grpResLst.get(idx);
							for(Member member:grp.memberList){
								if(!member.name.equals(Base.loginUser))
									jsonarray.put(member.name);
							}
							postData.put("users", jsonarray.toString());
						}
						else if(curMsg.usrsList != null && curMsg.usrsList.size()!= 0){
							for(String memberStr:curMsg.usrsList){
								if(!memberStr.equals(Base.loginUser))
									jsonarray.put(memberStr);
							}
							postData.put("users", jsonarray.toString());
						}
						
						JSONObject obj_enti = new JSONObject();
						obj_enti.put("from", curMsg.name);
						if(curMsg.groupName != null && !curMsg.groupName.equals(""))
							obj_enti.put("group", curMsg.groupName);
						obj_enti.put("users", jsonarray);													
						if (curMsg.msgType == ChatMsgEntity.CHAT_MSG_TEXT) {
							obj_enti.put("type", ChatMsgEntity.CHAT_MSG_TEXT);
							obj_enti.put("text", curMsg.text);
						} else if (curMsg.msgType == ChatMsgEntity.CHAT_MSG_LOCATE) {
							obj_enti.put("type", ChatMsgEntity.CHAT_MSG_LOCATE);
							obj_enti.put("lat", curMsg.latlon_loc.latitude);
							obj_enti.put("lon", curMsg.latlon_loc.longitude);
							
						} else if (curMsg.msgType == ChatMsgEntity.CHAT_MSG_TRACK) {
							obj_enti.put("type", ChatMsgEntity.CHAT_MSG_TRACK);
							JSONArray jsonArr = new JSONArray();
							for (LatLng latlon : curMsg.latlon_track) {
								jsonArr.put(latlon.latitude);
								jsonArr.put(latlon.longitude);
							}
							obj_enti.put("latlon_lst", jsonArr);

						}
						postData.put("messages", obj_enti.toString());
						
						CacheManager.getJson(baseAct, url, new IHttpCallback() {
							
							@Override
							public void handle(int retCode, Object response) {
								// TODO Auto-generated method stub		
								int ret = retCode;
//								if(semp.availablePermits() == 0)
//									semp.release();
							}
						}, postData);						
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}	
					return;
				}
				else{
					//Base.OBDApp.mBDLoc = location;
//					if(location.getCity()!= null)
					mCity = city;
					curPoiName = address;				
	
					pureCity = mCity;
					Base.myCity = mCity;
					if(pureCity != null){
						if(pureCity != null && pureCity.endsWith(baseAct.getResources().getString(R.string.city))){
							pureCity = pureCity.substring(0, pureCity.length()-1);
						}
														
						String loadCity = WeatherInfo.last_city_init();
						
						if(loadCity != null && loadCity.equals(pureCity)){
							mCityPY = WeatherInfo.last_city_py_init();
						}
						else{
							WeatherInfo.last_city_save(pureCity);
							mCityPY = GetPinyin.getPingYin(pureCity);
							WeatherInfo.last_city_py_save(mCityPY);
						}
						//baseAct.myWeatherInfo = new WeatherInfo(baseAct);
					}
					
					
					if(!ifSearchAddrShow){
						if(!hasLocated){
							hasLocated = true;
							mBaiduMap.setMapStatus(MapStatusUpdateFactory
									.newLatLng(latlng));
						}
						//mBaiduMap.clear();
						if(myHeadMarker != null)
							myHeadMarker.remove();
						if(Base.inDrvBhvStat)
							return;
						if(Base.myBitmap != null && Preference.getInstance(baseAct).getLoginStat()){						
							myHeadMarker = mBaiduMap.addOverlay(new MarkerOptions().position(latlng)
			                        .icon(BitmapDescriptorFactory.fromBitmap(Base.myBitmap)));	//fromBitmap(myBitmap)
						}
						else{
							myHeadMarker = mBaiduMap.addOverlay(new MarkerOptions().position(latlng)
			                        .icon(BitmapDescriptorFactory.fromBitmap(Base.dftMyBitmap)));	
						}
					}
				}
				break;
			}
		}
	};
	
	public class OBDLocationListener implements BDLocationListener {

		@Override
		public void onReceiveLocation(BDLocation location) {
			// Receive Location
			double lat, lon;
			bdlocation = location;			
			lat = location.getLatitude();
			lon = location.getLongitude();
			mCurLatitude = lat;
			mCurLongitude = lon;
			mCurAddress = location.getAddrStr();
			LatLng latLng = new LatLng(
					lat, lon);

			if(Base.OBDApp.getActivityBack() != Base.APP_RUN_FOREGROUND 
					|| !Base.isBaseActive)
				return;


			}		

	}

	public void enterExitGfence(boolean enter, String name){
		String content = "";
		if(enter){
			content = "进入电子围栏：";
		}
		else{
			content = "退出电子围栏：";
		}
		content += name;
		if(fenceNoticeDlg == null){
			fenceNoticeDlg = new AlertDialog.Builder(baseAct)
		 	.setTitle("电子围栏提醒")
		 	.setMessage(content)
		 	.setPositiveButton("确定", 
	 			new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();						
					}
			 	})
			.create();
			fenceNoticeDlg.setOnCancelListener(new OnCancelListener(){

				@Override
				public void onCancel(DialogInterface dialog) {
					// TODO Auto-generated method stub
					fenceNoticeDlg = null;
				}
				
			});
		}
		else{
			fenceNoticeDlg.setMessage(content);
		}
		fenceNoticeDlg.show();
	 	
		
	}
	
	public void loginStateChange(boolean login){
		if(myHeadMarker != null)
			myHeadMarker.remove();
		if(Base.myBitmap != null && login){						
			myHeadMarker =  mBaiduMap.addOverlay(new MarkerOptions().position(new LatLng(mCurLatitude, mCurLongitude))
                    .icon(BitmapDescriptorFactory.fromBitmap(Base.myBitmap)));	//fromBitmap(myBitmap)
		}
		else{
			myHeadMarker =  mBaiduMap.addOverlay(new MarkerOptions().position(new LatLng(mCurLatitude, mCurLongitude))
                    .icon(BitmapDescriptorFactory.fromBitmap(Base.dftMyBitmap)));	
		}
	}

	void init() {
		mLocationListener = new OBDLocationListener();
//		myThread = new Thread(this);
//		mLocationClient.registerLocationListener(mLocationListener);
//		InitLocation();		
//		mLocationClient.start();

	}

	// mLatitude = Preference.getInstance(Base.OBDApp).getLastLat();
	// mLongitude = Preference.getInstance(Base.OBDApp).getLastLon();
	// com.baidu.mapapi.model.LatLng latLng = new
	// com.baidu.mapapi.model.LatLng(mClickLatitude, mClickLongitude);

	public void addMyFenceAction(String name, String addr, String dura, int radius) {
		MyGeoFenceCont cont = new MyGeoFenceCont(name, addr, mClickLongitude,
				mClickLatitude, radius, dura);

		addCustomElementsDemo(mClickLatitude, mClickLongitude, radius);
		if (CarDataService.fenceList == null) {
			CarDataService.fenceList = new ArrayList<MyGeoFenceCont>();
//			myGeofenceThd.start();
//			myGeoFenceThdRun = true;
		}
		CarDataService.fenceList.add(cont);
		

	}

	public void addFenceAction(String name, String dura) {
		BDGeofence fence = new BDGeofence.Builder()
				.setGeofenceId(name)
				.setCircularRegion(mClickLongitude, mClickLatitude,
						BDGeofence.RADIUS_TYPE_SMALL)
				.setExpirationDruation(1000L * Integer.parseInt(dura))
				.setCoordType(BDGeofence.COORD_TYPE_BD09LL).build();
		mGeofenceClient.setInterval(3600);
		mGeofenceClient.addBDGeofence(fence, listener);
		// Toast.makeText(baseAct, "围栏" + name + "添加失败",
		// Toast.LENGTH_SHORT).show();
	}

	public class BaiduMapOnClick implements BaiduMap.OnMapClickListener, BaiduMap.OnMarkerClickListener {

		@Override
		public void onMapClick(LatLng arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public boolean onMapPoiClick(MapPoi arg0) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean onMarkerClick(Marker arg0) {
			// TODO Auto-generated method stub
			if(isGrpShareMode){
				for(Member member : honAdapter.gMemberList){
					if(member.marker != null && member.marker.equals(arg0)
							&& !member.name.equals(Base.loginUser)){
						curMember = member;
						pickupChoiceCheck();
						mBaiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(curMember.latlon));
						break;
					}
				}
			}
			return true;
		}

	};

	@Override
	public void onGetGeoCodeResult(GeoCodeResult result) {
		if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
			// Toast.makeText(BaiduMapView.this, "抱歉，未能找到结果",
			// Toast.LENGTH_SHORT)
			// .show();
			return;
		}
		// mBaiduMap.clear();
		// mBaiduMap.addOverlay(new
		// MarkerOptions().position(result.getLocation())
		// .icon(BitmapDescriptorFactory
		// .fromResource(R.drawable.icon_marka)));
		// mBaiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(result
		// .getLocation()));
		String strInfo = String.format("纬度：%f 经度：%f",
				result.getLocation().latitude, result.getLocation().longitude);		
		// Toast.makeText(BaiduMapView.this, strInfo,
		// Toast.LENGTH_SHORT).show();

		// addCustomElementsDemo(result.getLocation().latitude,
		// result.getLocation().longitude);
	}

	@Override
	public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
		if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
			// Toast.makeText(BaiduMapView.this, "抱歉，未能找到结果",
			// Toast.LENGTH_SHORT)
			// .show();
			return;
		}
		// mBaiduMap.clear();
		// mBaiduMap.addOverlay(new
		// MarkerOptions().position(result.getLocation())
		// .icon(BitmapDescriptorFactory
		// .fromResource(R.drawable.icon_marka)));

		// mBaiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(result
		// .getLocation()));

		// Toast.makeText(BaiduMapView.this, result.getAddress(),
		// Toast.LENGTH_SHORT).show();
//		if (addFenceDlg != null){
//			addFenceDlg.addr = result.getAddress();
//			addFenceDlg.addr_v.setText(result.getAddress());
//		}
		mCurAddress = result.getAddress();

	}

	public void addCustomElementsDemo(double lat, double lon, int radius) {
		LatLng llCircle = new LatLng(lat, lon);
		OverlayOptions ooCircle = new CircleOptions().fillColor(0x400087cb)
				.center(llCircle).stroke(new Stroke(4, 0xee0087cb))
				.radius(radius);
		mBaiduMap.clear();
		mBaiduMap.addOverlay(ooCircle);
//		mBaiduMap.addOverlay(ooCircle1);
	}

	private void InitLocation() {
		LocationClientOption option = new LocationClientOption();
		option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);// 设置定位模式
		option.setCoorType("bd09ll");// 返回的定位结果是百度经纬度，默认值gcj02
		int span = 1000;
		option.setScanSpan(span);// 设置发起定位请求的间隔时间为5000ms
		option.setIsNeedAddress(true);
//		mLocationClient.setLocOption(option);

	}

	public void removeFence() {
		List<String> fences = new ArrayList<String>();
		fences.add("fence1");
		mGeofenceClient.removeBDGeofences(fences, new RemoveFenceListener());
	}

	public class AddGeofenceListener implements
			GeofenceClient.OnAddBDGeofencesResultListener {

		@Override
		public void onAddBDGeofencesResult(int statusCode, String geofenceId) {
			try {
				if (statusCode == BDLocationStatusCodes.SUCCESS) {
					Toast.makeText(baseAct, "围栏" + geofenceId + "添加成功",
							Toast.LENGTH_SHORT).show();
					if (mGeofenceClient != null) {
						mGeofenceClient.start();
					}
//					mBaiduMap.clear();
					addCustomElementsDemo(mClickLatitude, mClickLongitude,1000);
				} else {
					Toast.makeText(baseAct, "围栏" + geofenceId + "添加失败",
							Toast.LENGTH_SHORT).show();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public class Geofence implements GeofenceClient.OnGeofenceTriggerListener {
		@Override
		public void onGeofenceTrigger(String arg0) {
			// 进入围栏
			// Toast.makeText(BaiduMapView.this, "进入围栏",
			// Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onGeofenceExit(String arg0) {
			// 退出围栏
			// Toast.makeText(BaiduMapView.this, "退出围栏",
			// Toast.LENGTH_SHORT).show();
		}
	}

	public class RemoveFenceListener implements
			GeofenceClient.OnRemoveBDGeofencesResultListener {
		@Override
		public void onRemoveBDGeofencesByRequestIdsResult(int statusCode,
				String[] geofenceRequestIds) {
			// 删除围栏
			// Toast.makeText(BaiduMapView.this, "删除围栏",
			// Toast.LENGTH_SHORT).show();
		}
	}

	class MyGeoFenceThread extends Thread {
		public synchronized void run() {
			while (myGeoFenceThdRun) {
//				mLocationClient.start();
				try {
					sleep(6000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	public void drawSharePosColor(UserPos pos, int color) {
		LatLng latLng = new LatLng(pos.getLat(), pos.getLon());
//		BitmapDescriptor bitmap = BitmapDescriptorFactory
//				.fromResource(R.drawable.icon_marka);
		Bitmap bitm = BitmapFactory.decodeResource(baseAct.getResources(), R.drawable.where4);
		bitm = Util.setBitmapColor(bitm, color);
		BitmapDescriptor bitmap = BitmapDescriptorFactory.fromBitmap(bitm);
		OverlayOptions option = new MarkerOptions().position(latLng)
				.icon(bitmap).title(pos.getName());
		
		mBaiduMap.addOverlay(option);
	}
	
	public void drawSharePos(UserPos pos) {
		LatLng latLng = new LatLng(pos.getLat(), pos.getLon());
		BitmapDescriptor bitmap = BitmapDescriptorFactory
				.fromResource(R.drawable.icon_marka);
		
		OverlayOptions option = new MarkerOptions().position(latLng)
				.icon(bitmap).title(pos.getName());
		
		mBaiduMap.addOverlay(option);
	}
	
	public void drawShareTraceColor(UserTrace trace, int color) {		
		List<LatLng> pts = null;
		List<LatLng> delList = new ArrayList<LatLng>();
		LatLng start = null;
		LatLng end = null;

		pts = trace.getLsPos();
		if (pts == null) {
			Log.v("lsPt", "kong");
		} else if (pts.size() > 0) {
			start = pts.get(0);
			end = pts.get(pts.size() - 1);
		}		

		for (int i = 0; i < pts.size() - 1; i++) {
			delList.add(pts.get(i));
			delList.add(pts.get(i + 1));
		}
		OverlayOptions polylineOption = new PolylineOptions()
				.points(delList).width(10).color(color);
		Overlay  overylay = mBaiduMap.addOverlay(polylineOption);		
		delList.clear();
		
		mBaiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(start));
		if (start != null) {
			drawStartEnd(start);
		}
		if (end != null) {
			drawStartEnd(end);
		}
    }
	
	public void drawShareTrace(UserTrace trace) {		
		List<LatLng> pts = null;
		List<LatLng> delList = new ArrayList<LatLng>();
		LatLng start = null;
		LatLng end = null;

		pts = trace.getLsPos();
		if (pts == null) {
			Log.v("lsPt", "kong");
		} else if (pts.size() > 0) {
			start = pts.get(0);
			end = pts.get(pts.size() - 1);
		}		

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
	
	public void stopUploadGPS(){
		runInstantUploadGPS = false;
//		semp.release();
//		mLocationClient.stop();
	}
	
	public void startUploadGPS(){
		
//		Thread myThread = new Thread(this);
		if(!runInstantUploadGPS){
			runInstantUploadGPS = true;
//			new Thread(this).start();
		}
//		mLocationClient.start();
	}

	public boolean onBackKeyEnter(){
		if(isGrpShareMode){
			if(Base.OBDApp.landScapeMode == 0)
				exitShareModeCheck();
			else
				exitGpsShareMode();	
			return true;
		}
		if(gfenceState == 1){
			exitFenceAddMode();
			return true;
		}
		else if(gfenceState == 2){
			if(Base.OBDApp.landScapeMode == 0)
				exitFenceEditMode();
			else
				exitFenceModeL();
			return true;
		}
		else if(gfenceState == 3)
		{
			if(gfenceMarker != null)
				gfenceMarker.remove();
			gfenceState = 0;
			return true;
		}
		if(ifSearchAddrShow){
			searchAddrDisplay(false, null, null);
			return true;
		}
		return false;
			
	}
	
	void createInfoWindow(){		
		LinearLayout infoWinLinear = (LinearLayout) View.inflate(baseAct, R.layout.pickup_geofence, null);
		pickup_radius_500 = (TextView) infoWinLinear.findViewById(R.id.pickup_geofence_radi_500);
		pickup_radius_1000 = (TextView) infoWinLinear.findViewById(R.id.pickup_geofence_radi_1000);
		pickup_radius_def = (EditText) infoWinLinear.findViewById(R.id.pickup_geofence_radius_def);
		pickup_fenceSave = (TextView) infoWinLinear.findViewById(R.id.pickup_geofence_save);
		pickup_fenceDel = (TextView) infoWinLinear.findViewById(R.id.pickup_geofence_del);
		if(!curMember.fenceActive){
			pickup_fenceDel.setText("取 消");
			curMember.fenceRadius = 500;
        	pickup_radius_500.setBackground(baseAct.gfencebackdraw);
        	pickup_radius_500.setTextColor(0xffffffff);
        	pickup_lastClickRadius = pickup_radius_500;
        	pickup_radius_def.setText("");
		}
		else{
			if(curMember.fenceRadius != 0){
				if(curMember.fenceRadius == 500){
		        	pickup_radius_500.setBackground(baseAct.gfencebackdraw);
		        	pickup_radius_500.setTextColor(0xffffffff);
		        	pickup_lastClickRadius = pickup_radius_500;
		        	pickup_radius_def.setText("");
				}
				else if(curMember.fenceRadius == 1000){
		        	pickup_radius_1000.setBackground(baseAct.gfencebackdraw);
		        	pickup_radius_1000.setTextColor(0xffffffff);
		        	pickup_lastClickRadius = pickup_radius_1000;
		        	pickup_radius_def.setText("");
				}
				else{
					pickup_radius_def.setText(""+curMember.fenceRadius);
				} 
			}
		}
		pickup_radius_500.setOnClickListener(this);
		pickup_radius_1000.setOnClickListener(this);
		pickup_radius_def.setOnClickListener(this);
		pickup_fenceSave.setOnClickListener(this);
		pickup_fenceDel.setOnClickListener(this);
		pickup_radius_def.addTextChangedListener(new TextWatcher(){

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// TODO Auto-generated method stub
				if(s.toString() != null && !s.toString().equals("")){
					curMember.fenceRadius = Integer.parseInt(s.toString());		
					addGeofenceMarker(curMember);
				}
				if(pickup_lastClickRadius != null){
					pickup_lastClickRadius.setBackgroundColor(0xffffffff);
					pickup_lastClickRadius.setTextColor(0xff000000);
				}
			}

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				
			}
			
		});
		pickup_radius_def.setOnEditorActionListener(new TextView.OnEditorActionListener(){

			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				// TODO Auto-generated method stub
				if(event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER ||
						event.getKeyCode() == KeyEvent.KEYCODE_BACK)){
					String radiusText = pickup_radius_def.getText().toString();
					if(radiusText != null && !radiusText.equals("")){
						curMember.fenceRadius = Integer.parseInt(radiusText);
						addGeofenceMarker(curMember);
					}
				}
				if(pickup_lastClickRadius != null){
					pickup_lastClickRadius.setBackgroundColor(0xffffffff);
					pickup_lastClickRadius.setTextColor(0xff000000);
				}
				return false;
			}});
		addGeofenceMarker(curMember);
		mInfoWindow =  new InfoWindow(infoWinLinear, curMember.latlon, -66*Base.mDensityInt);
		mBaiduMap.showInfoWindow(mInfoWindow);		
	}
	
	public void pickupChoiceCheck(){
		if(pickupMode != 0 && curMember.isInSharePosMode){
			if(pickupMode == 1 && curMember.name.equals(replyUser) || 
			   pickupMode == 2 && curMember.name.equals(receiveInvitor)){				
				if(curMember.name.equals(replyUser) || curMember.name.equals(receiveInvitor)){
					if(mInfoWindow != null){
						//mBaiduMap.hideInfoWindow();
						//mInfoWindow = null;
					}
					else{
						if(!curMember.fenceActive){
							String content = "";
							if(inviteType == 0){
								//Toast.makeText(baseAct, "TA正在来接你", Toast.LENGTH_SHORT).show();
								content = curMember.name + "正在来接你";
							}
							else{	
								//Toast.makeText(baseAct, "你正在去接TA", Toast.LENGTH_SHORT).show();
								content = "你正在去接" + curMember.name;
							}
							content += "\n,是否给TA设置地理围栏?";
							pickupDurationDlg  = new CustomDialog(baseAct, 300, 120, R.layout.pickup_duration, R.style.Theme_dialog3);
							TextView title = (TextView) pickupDurationDlg.findViewById(R.id.pickup_dura_title);					
							TextView confirm = (TextView) pickupDurationDlg.findViewById(R.id.pickup_dura_confirm);
							TextView cancel = (TextView) pickupDurationDlg.findViewById(R.id.pickup_dura_cancel);
							title.setText(content);
							confirm.setOnClickListener(this);
							cancel.setOnClickListener(this);
							pickupDurationDlg.show();
							
						}
						else{
							createInfoWindow();
						}
					}
				}
				else{
					Toast.makeText(baseAct, "你正在去接别人，请先退出后再接TA", Toast.LENGTH_SHORT).show();
				}
			}
			return;
		}
		
		if(pickupChoiceDlg != null)
			return;
		pickupChoiceDlg = new CustomDialog(baseAct, 300, 120, R.layout.pickup_choice, R.style.Theme_dialog3);
		TextView invite = (TextView) pickupChoiceDlg.findViewById(R.id.pickup_choice_invite);
		TextView acquire = (TextView) pickupChoiceDlg.findViewById(R.id.pickup_choice_acquire);
		TextView cancel = (TextView) pickupChoiceDlg.findViewById(R.id.pickup_choice_cancel);
		invite.setOnClickListener(this);
		acquire.setOnClickListener(this);
		cancel.setOnClickListener(this);
		pickupChoiceDlg.setOnCancelListener(new Dialog.OnCancelListener(){
			@Override
			public void onCancel(DialogInterface dialog) {
				// TODO Auto-generated method stub
				pickupChoiceDlg = null;
			}
		});			
		pickupChoiceDlg.show();
		
	}
	
	public void receivePickupInviteCheck(){
		if(receivePickupDlg != null)
			return;
		receivePickupDlg = new CustomDialog(baseAct, 300, 120, R.layout.pickup_receive, R.style.Theme_dialog3);
		TextView title = (TextView) receivePickupDlg.findViewById(R.id.pickup_title);
		TextView accept = (TextView) receivePickupDlg.findViewById(R.id.pickup_accept);
		TextView reject = (TextView) receivePickupDlg.findViewById(R.id.pickup_reject);
		TextView ignore = (TextView) receivePickupDlg.findViewById(R.id.pickup_ignore);
		String titleCont = receiveInvitor;
		if(receiveInviteType == 0)
			titleCont += "\n请求你去接TA";
		else
			titleCont += "\n请求来接你";
		receivePickupDlg.setOnCancelListener(new Dialog.OnCancelListener(){
			@Override
			public void onCancel(DialogInterface dialog) {
				// TODO Auto-generated method stub
				receivePickupDlg = null;
			}
		});
		title.setText(titleCont);
		accept.setOnClickListener(this);
		reject.setOnClickListener(this);
		ignore.setOnClickListener(this);
		receivePickupDlg.show();
		
	}
	
	public void receivePickupReply(){
		if(pickupReplyDlg != null)
			return;
		pickupReplyDlg  = new CustomDialog(baseAct, 300, 120, R.layout.pickup_reply, R.style.Theme_dialog3);
		TextView replyTitle =  (TextView)pickupReplyDlg.findViewById(R.id.pickup_reply_title);
		View btn = pickupReplyDlg.findViewById(R.id.pickup_reply_btn);
		View cancel = pickupReplyDlg.findViewById(R.id.pickup_navit_cancel);
		String titleCont = replyUser;
		
		if(replyResult == 1){
			if(inviteType == 1){
				titleCont += "\n接受你的邀请,\n是否根据TA的位置启动导航";		
				cancel.setVisibility(View.VISIBLE);
				pickupMode = 1;
			}
			else{
				titleCont += "\n接受你的邀请";		
				pickupMode = 2;
			}			
		}
		else{
			titleCont += "\n拒绝你的邀请";
		}
		replyTitle.setText(titleCont);
		
		btn.setOnClickListener(this);
		cancel.setOnClickListener(this);
		pickupReplyDlg.show();
		pickupReplyDlg.setOnCancelListener(new Dialog.OnCancelListener(){
			@Override
			public void onCancel(DialogInterface dialog) {
				// TODO Auto-generated method stub
				pickupReplyDlg = null;
			}
		});	
	}
	
	public void enterShareModeCheck(){
		enterShareModeDlg = new Dialog(baseAct, R.style.Theme_dialog);
		enterShareModeDlg.setContentView(R.layout.bottom_dlg);
		
		Window dialogWindow = enterShareModeDlg.getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.height = Base.mHeight/5;
        lp.width = Base.mWidth;
        lp.gravity = Gravity.BOTTOM;
        dialogWindow.setGravity(Gravity.BOTTOM);
        dialogWindow.setAttributes(lp);
       
        View.OnClickListener dlgClick = new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				switch(v.getId()){
				case R.id.textvw1:
					Group grp = null;
					
					Base.baidu_v.enterGpsShareMode();
					//Base.me_v.chatMsgLst.get(Base.friendOrGrpIdx).clear();						
					enterShareModeDlg.cancel();
					break;
				case R.id.textvw2:
					enterShareModeDlg.cancel();
					break;
				default:
					break;
				}
			}
        	
        };
        TextView tv1 = ((TextView)enterShareModeDlg.findViewById(R.id.textvw1));
        tv1.setText("进入实时分享位置");
        tv1.setOnClickListener(dlgClick);
        TextView tv2 = ((TextView)enterShareModeDlg.findViewById(R.id.textvw2));
        tv2.setText("取消");
        tv2.setOnClickListener(dlgClick);
        
        enterShareModeDlg.setCanceledOnTouchOutside(true);
        enterShareModeDlg.show();
	}
	
	public void exitShareModeCheck(){
		if(mInfoWindow != null){
			mBaiduMap.hideInfoWindow();
			mInfoWindow = null;
			return;
		}
		exitShareModeDlg = new Dialog(baseAct, R.style.Theme_dialog);
		exitShareModeDlg.setContentView(R.layout.bottom_dlg);
		
		Window dialogWindow = exitShareModeDlg.getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.height = Base.mHeight/5;
        lp.width = Base.mWidth;
        lp.gravity = Gravity.BOTTOM;
        dialogWindow.setGravity(Gravity.BOTTOM);
        dialogWindow.setAttributes(lp);
       
        View.OnClickListener dlgClick = new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				switch(v.getId()){
				case R.id.textvw1:
					exitGpsShareMode();						
					exitShareModeDlg.cancel();
					break;
				case R.id.textvw2:
					exitShareModeDlg.cancel();
					break;
				default:
					break;
				}
			}
        	
        };
        TextView tv1 = ((TextView)exitShareModeDlg.findViewById(R.id.textvw1));
        if(pickupMode != 0)
        	tv1.setText("正处于接人模式，是否退出");
        else	
        	tv1.setText("退出实时分享位置");
        tv1.setOnClickListener(dlgClick);
        TextView tv2 = ((TextView)exitShareModeDlg.findViewById(R.id.textvw2));
        tv2.setText("取消");
        tv2.setOnClickListener(dlgClick);
//        exitShareModeDlg.findViewById(R.id.textvw3).setVisibility(View.INVISIBLE);
        
        exitShareModeDlg.setCanceledOnTouchOutside(true);
        exitShareModeDlg.show();	
	}

	public void addGeofenceMarker(Member member){
		OverlayOptions ooCircle = new CircleOptions().fillColor(0x400087cb)
				.center(member.latlon).stroke(new Stroke(4, 0xee0087cb))
				.radius(member.fenceRadius);

		if(pickupGfenceMarker != null)
			pickupGfenceMarker.remove();
		pickupGfenceMarker =  mBaiduMap.addOverlay(ooCircle);				
	}
	
	public void addGeofenceMarker(){

		if(gfenceState != 0 && Math.abs(myGfence.lat) > 0.000001){
			OverlayOptions ooCircle = new CircleOptions().fillColor(0x400087cb)
					.center(new LatLng(myGfence.lat, myGfence.lon)).stroke(new Stroke(4, 0xee0087cb))
					.radius(gFenceRadius);
			//mBaiduMap.clear();
			if(gfenceMarker != null)
				gfenceMarker.remove();
			gfenceMarker =  mBaiduMap.addOverlay(ooCircle);
		
		}
	}
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		String url = null;
		Map<String, String> postData = null;
		switch(v.getId()){
//			case R.id.grp_lv_exam:
//				break;
		case R.id.grp_back:
//			exitGprShareMode();
			if(Base.OBDApp.landScapeMode == 0)
				exitShareModeCheck();
			else
				exitGpsShareMode();	
			break;
		case R.id.home_group:
			LogRecord.SaveLogInfo2File(Base.OperateInfo, TAG+"group detail click");
			Base.me_v.grpDetailDlg = new GrpDetailDlg(baseAct);
			Base.me_v.grpDetailDlg.show();
			break;
		
		//geofence
		case R.id.pickup_geofence_radi_500:	
        	pickup_radius_def.setText("");
			curMember.fenceRadius = 500;
			addGeofenceMarker(curMember);
			if(pickup_lastClickRadius != null){
				pickup_lastClickRadius.setBackgroundColor(0xffffffff);
				pickup_lastClickRadius.setTextColor(0xff000000);
			}
			pickup_lastClickRadius = pickup_radius_500;
			pickup_radius_500.setBackground(baseAct.gfencebackdraw);
			pickup_radius_500.setTextColor(0xffffffff);
        	break;        	
		case R.id.pickup_geofence_radi_1000:	
        	pickup_radius_def.setText("");
			curMember.fenceRadius = 1000;
			addGeofenceMarker(curMember);
			if(pickup_lastClickRadius != null){
				pickup_lastClickRadius.setBackgroundColor(0xffffffff);
				pickup_lastClickRadius.setTextColor(0xff000000);
			}
			pickup_lastClickRadius = pickup_radius_1000;
			pickup_radius_1000.setBackground(baseAct.gfencebackdraw);
			pickup_radius_1000.setTextColor(0xffffffff);
        	break;  
		case R.id.pickup_geofence_radius_def:
			if(pickup_lastClickRadius != null){
				pickup_lastClickRadius.setBackgroundColor(0xffffffff);
				pickup_lastClickRadius.setTextColor(0xff000000);
			}
			pickup_lastClickRadius = null;
			break;
		case R.id.pickup_geofence_save:
			curMember.fenceActive = true;
			mBaiduMap.hideInfoWindow();
			mInfoWindow = null;
			break;
		case R.id.pickup_geofence_del:
			if(pickupGfenceMarker != null)
				pickupGfenceMarker.remove();
			curMember.fenceActive = false;
			mBaiduMap.hideInfoWindow();
			mInfoWindow = null;
			break;
		case R.id.gfence_radi_500:
			gfence_radius_def.setText("");
			gFenceRadius = 500;
			addGeofenceMarker();
			if(lastClickRadius != null){
				lastClickRadius.setBackgroundColor(0xffffffff);
				lastClickRadius.setTextColor(0xff000000);
			}
			lastClickRadius = radius_500;
			radius_500.setBackground(baseAct.gfencebackdraw);
			radius_500.setTextColor(0xffffffff);
			break;
		case R.id.gfence_radi_700:
			gfence_radius_def.setText("");
			gFenceRadius = 700;
			addGeofenceMarker();
			if(lastClickRadius != null){
				lastClickRadius.setBackgroundColor(0xffffffff);
				lastClickRadius.setTextColor(0xff000000);
			}
			lastClickRadius = radius_700;
			radius_700.setBackground(baseAct.gfencebackdraw);
			radius_700.setTextColor(0xffffffff);
			break;
		case R.id.gfence_radi_800:
			gfence_radius_def.setText("");
			gFenceRadius = 800;
			addGeofenceMarker();
			if(lastClickRadius != null){
				lastClickRadius.setBackgroundColor(0xffffffff);
				lastClickRadius.setTextColor(0xff000000);
			}
			lastClickRadius = radius_800;
			radius_800.setBackground(baseAct.gfencebackdraw);
			radius_800.setTextColor(0xffffffff);	
			break;
		case R.id.gfence_radi_1000:
			gfence_radius_def.setText("");
			gFenceRadius = 1000;	
			addGeofenceMarker();
			if(lastClickRadius != null){
				lastClickRadius.setBackgroundColor(0xffffffff);
				lastClickRadius.setTextColor(0xff000000);
			}
			lastClickRadius = radius_1000;
			radius_1000.setBackground(baseAct.gfencebackdraw);
			radius_1000.setTextColor(0xffffffff);
			break;
		case R.id.gfence_radius_def:
			if(lastClickRadius != null){
				lastClickRadius.setBackgroundColor(0xffffffff);
				lastClickRadius.setTextColor(0xff000000);
			}
			lastClickRadius = null;
			break;			
		case R.id.gfence_back:
			exitFenceAddMode();
			break;
		case R.id.gfence_save:
			myGfence.radius = gFenceRadius;
			myGfence.name = gfence_name.getText().toString();
			myGfence.dura = gfence_dura.getText().toString();
			myGfence.address = gfence_addr.getText().toString();			
			if(gfenceState == 1 && !myGfence.name.equals("") && !myGfence.address.equals(""))
			{	
				CarDataService.fenceList.add(myGfence);
				exitFenceAddMode();
				mBaiduMap.clear();
				Toast.makeText(baseAct, "添加成功",Toast.LENGTH_SHORT).show();
			}
			else if(gfenceState == 2 && !myGfence.name.equals("") && !myGfence.address.equals("")){
				CarDataService.fenceList.remove(gfenceIdx);
				CarDataService.fenceList.add(gfenceIdx,myGfence);
				exitFenceEditMode(); 
				mBaiduMap.clear();
				Toast.makeText(baseAct, "修改成功",Toast.LENGTH_SHORT).show();
			}
			else
				Toast.makeText(baseAct, "地理围栏参数输入不全",Toast.LENGTH_SHORT).show();
			
//			CarDataService.fenceList = fenceList;
			break;
		case R.id.gfence_addr_search:
			String searchText = gfence_addr.getText().toString();
			Base.baidu_v.poiName = searchText;
			if(searchText != null && !searchText.equals("")){
				Base.baidu_v.mPoiSearch.searchInCity((new PoiCitySearchOption())  
					    .city(Base.baidu_v.mCity)  
					    .keyword(searchText)  
					    .pageNum(0));
				isSelectAddrMode = 3;
			}
			break;
		case R.id.gfence_del:
			mBaiduMap.clear();
			CarDataService.fenceList.remove(gfenceIdx);
//			CarDataService.fenceList = fenceList;
			exitFenceEditMode();
			Toast.makeText(baseAct, "删除成功",Toast.LENGTH_SHORT).show();
			break;
	
			
		//pickup
		case R.id.pickup_choice_invite:
			postData = new HashMap<String, String>();
			url = Base.HTTP_GROUP_PATH+"/carPool";
			postData.put("appID", "appid");
			postData.put("type", "1");
			inviteType = 1;
			if(curGrp != null)
				postData.put("groupName", curGrp.name);
			postData.put("users", curMember.name);
			postData.put("lat", Double.toString(mCurLatitude));
			postData.put("lon", Double.toString(mCurLongitude));
			CacheManager.getJson(baseAct, url, new IHttpCallback() {				
				@Override
				public void handle(int retCode, Object response) {
					// TODO Auto-generated method stub
					int ret = retCode;
				}
			}, postData);
			pickupChoiceDlg.cancel();
			break;
		case R.id.pickup_choice_acquire:
			postData = new HashMap<String, String>();
			url = Base.HTTP_GROUP_PATH+"/carPool";
			postData.put("appID", "appid");
			postData.put("type", "0");
			inviteType = 0;
			if(curGrp != null)
				postData.put("groupName", curGrp.name);
			postData.put("users", curMember.name);
			postData.put("lat", Double.toString(mCurLatitude));
			postData.put("lon", Double.toString(mCurLongitude));
			CacheManager.getJson(baseAct, url, new IHttpCallback() {				
				@Override
				public void handle(int retCode, Object response) {
					// TODO Auto-generated method stub
					int ret = retCode;
				}
			}, postData);		
			pickupChoiceDlg.cancel();
			break;
		case R.id.pickup_choice_cancel:
			pickupChoiceDlg.cancel();
			break;
		case R.id.pickup_accept:
			postData = new HashMap<String, String>();
			url = Base.HTTP_GROUP_PATH+"/replyCarPool";
			postData.put("appID", "appid");
			postData.put("type", ""+receiveInviteType);
			postData.put("reply", "1");
			if(receiveGrpNm != null)
				postData.put("groupName", receiveGrpNm);
			postData.put("users", receiveInvitor);
			postData.put("lat", Double.toString(mCurLatitude));
			postData.put("lon", Double.toString(mCurLongitude));
			CacheManager.getJson(baseAct, url, new IHttpCallback() {				
				@Override
				public void handle(int retCode, Object response) {
					// TODO Auto-generated method stub
					if(retCode == 200){
						int idx = 0;
						if(receiveGrpNm == null){
							idx = Member.indexOfByName(HttpQueue.friendLst, receiveInvitor);
							Base.friendOrGrpIdx = idx;
						}
						else{
							idx = Group.indexOfByName(HttpQueue.grpResLst, receiveGrpNm);
							if(idx != -1)
								Base.friendOrGrpIdx = idx+HttpQueue.friendLst.size();
						}

						if(!isGrpShareMode)
							enterGpsShareMode();
						else{
							isGrpShareMode = true;
							if(Base.friendOrGrpIdx < HttpQueue.friendLst.size()){			
								curMember = HttpQueue.friendLst.get(Base.friendOrGrpIdx);
								curGrp = null;
								share_num_tv.setText("好友"+curMember.name);
							}
							else{
								curGrp = HttpQueue.grpResLst.get(Base.friendOrGrpIdx-HttpQueue.friendLst.size());			
								int memberIdx = Member.indexOfByName(curGrp.memberList, receiveInvitor);
								curMember = curGrp.memberList.get(memberIdx);
								share_num_tv.setText("群组"+curGrp.name+"("+curGrp.memberList.size() +")人");
								curMember = null;
							}
							honAdapter = new HorizontalListViewAdapter(baseAct);
							honLv.setAdapter(honAdapter);	
							mBaiduMap.clear();
						}			
						if(receiveInviteType == 0){
							pickupMode = 1;							
							BNaviPoint startPoint = new BNaviPoint(Base.baidu_v.mCurLongitude,Base.baidu_v.mCurLatitude,
						        		Base.baidu_v.curPoiName, BNaviPoint.CoordinateType.BD09_MC);//WGS84
							BNaviPoint endPoint = new BNaviPoint(recevielng,receiveLat,
									 	"", BNaviPoint.CoordinateType.BD09_MC);
							Cascade.startNavit(baseAct, startPoint, endPoint);
						}
						else
							pickupMode = 2;
						Toast.makeText(Base.OBDApp, "你已接受TA的接人邀请", Toast.LENGTH_SHORT).show();
					}
					else{
						Toast.makeText(Base.OBDApp, "发送接受信息失败", Toast.LENGTH_SHORT).show();						
					}
				}
			}, postData);			
			receivePickupDlg.cancel();
			break;
		case R.id.pickup_reject:
			postData = new HashMap<String, String>();
			url = Base.HTTP_GROUP_PATH+"/replyCarPool";
			postData.put("appID", "appid");
			postData.put("type", ""+receiveInviteType);
			postData.put("reply", "0");
			if(receiveGrpNm != null)
				postData.put("groupName", receiveGrpNm);
			postData.put("users", receiveInvitor);
			postData.put("lat", Double.toString(mCurLatitude));
			postData.put("lon", Double.toString(mCurLongitude));
			CacheManager.getJson(baseAct, url, new IHttpCallback() {				
				@Override
				public void handle(int retCode, Object response) {
					// TODO Auto-generated method stub
					if(retCode == 200){
						Toast.makeText(Base.OBDApp, "你已拒绝TA的接人邀请", Toast.LENGTH_SHORT).show();	
					}
					else{
						Toast.makeText(Base.OBDApp, "发送拒绝信息失败", Toast.LENGTH_SHORT).show();						
					}
				}
			}, postData);				
			receivePickupDlg.cancel();
			break;
		case R.id.pickup_ignore:
			receivePickupDlg.cancel();
			break;
		case R.id.pickup_reply_btn:
			pickupReplyDlg.cancel();
	        BNaviPoint startPoint = new BNaviPoint(Base.baidu_v.mCurLongitude,Base.baidu_v.mCurLatitude,
	        		Base.baidu_v.curPoiName, BNaviPoint.CoordinateType.BD09_MC);//WGS84
	        BNaviPoint endPoint = null;
	        if(pickupMode == 1){	
	        	if(inviteType == 1){	        		
	        		endPoint = new BNaviPoint(replylng,replyLat,
	    	        		"", BNaviPoint.CoordinateType.BD09_MC);	        	
	        	}		
		        Cascade.startNavit(baseAct, startPoint, endPoint);
			}
			break;
		case R.id.pickup_navit_cancel:
			pickupReplyDlg.cancel();
			break;
		case R.id.pickup_dura_confirm:
			pickupDurationDlg.cancel();
			createInfoWindow();
			break;
		case R.id.pickup_dura_cancel:
			pickupDurationDlg.cancel();
			break;
		case R.id.jump_to_center:
			if(mCurLatitude > 0.000001 || mCurLatitude < -0.000001)
				mBaiduMap.setMapStatus(MapStatusUpdateFactory
						.newLatLng(new LatLng(mCurLatitude, mCurLongitude)));
			break;
		case R.id.search_addr_btn:	
	        BNaviPoint start = new BNaviPoint(Base.baidu_v.mCurLongitude,Base.baidu_v.mCurLatitude,
	        		Base.baidu_v.curPoiName, BNaviPoint.CoordinateType.BD09_MC);//WGS84
	        BNaviPoint end = new BNaviPoint(searchAddrLatlng.longitude,searchAddrLatlng.latitude,
	        		searchAddrCont.getText().toString(), BNaviPoint.CoordinateType.BD09_MC);
			Cascade.startNavit(baseAct, start, end);
			searchAddrDisplay(false, null, null);
			break;
		}
	}	
}
