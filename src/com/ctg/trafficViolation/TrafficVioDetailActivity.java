package com.ctg.trafficViolation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.lbsapi.auth.LBSAuthManagerListener;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMap.OnMapClickListener;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeOption;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.mapapi.utils.DistanceUtil;
import com.baidu.navisdk.BNaviEngineManager.NaviEngineInitListener;
import com.baidu.navisdk.BaiduNaviManager;
import com.baidu.navisdk.util.verify.BNKeyVerifyListener;
import com.ctg.TrafficViolationPt.ViolationList;
import com.ctg.TrafficViolationPt.ViolationPt;
import com.ctg.TrafficViolationPt.ViolationPtDataSource;
import com.ctg.TrafficViolationPt.ViolationPtDataSourceDelegate;
import com.ctg.crash.LogRecord;
import com.ctg.ui.Base;
import com.ctg.ui.OBDApplication;
import com.ctg.ui.R;
import com.ctg.util.Util;

public class TrafficVioDetailActivity extends Activity implements
		OnGetGeoCoderResultListener, ViolationPtDataSourceDelegate,
		OnMapClickListener {
	private static final String TAG = "TrafficVioDetailDlg";
	private TrafficVioDetailActivity mContext;
	Map<String, Object> mMapItem;

	private static int default_width = 720; //
	private static int default_height = 1280;//
	private int hPx, wPx;
	private float mScreenWidth, mScreenHeight;// 地图控件的宽高像素
	ImageView mBackImg;
	TextView mTitle;
	TextView mAddr;
	TextView mCont;
	TextView mTime;
	TextView mStat;
	TextView mFine;
	TextView mPoint;
	TextView mPersonCnt;

	GeoCoder mSearch;
	BaiduMap mBaiduMap;
	MapView mMapView;

	boolean mIsEngineInitSuccess;

	ViolationPtDataSource vio;
	LatLng pos;
	float zoom;
	boolean bLoad;
	ImageView chaosu_imgv;
	ImageView weiting_imgv;
	ImageView xianxing_imgv;
	ImageView clear_imgv;
	int curtype[];
	ViolationList lastLs;
	LatLng vioLatLng;
	
	public void onCreate(Bundle saveInstance) {
		super.onCreate(saveInstance);

		// 在使用SDK各组件之前初始化context信息，传入ApplicationContext
		SDKInitializer.initialize(getApplicationContext());

		setContentView(R.layout.trafficvio_detail);

		// 初始化导航引擎
		// BaiduNaviManager.getInstance().initEngine(this, Base.getSDPath(),
		// mNaviEngineInitListener, Base.ACCESS_KEY, mKeyVerifyListener);
//		BaiduNaviManager.getInstance().initEngine(
//				TrafficVioDetailActivity.this, Base.getSDPath(),
//				mNaviEngineInitListener, new LBSAuthManagerListener() {
//					@Override
//					public void onAuthResult(int status, String msg) {
//						String str = null;
//						if (0 == status) {
//							str = "key校验成功!";
//						} else {
//							str = "key校验失败, " + msg;
//						}
//					}
//				});

		mContext = this;
		initMap();
		initView();
		initListener();

		// curtype = new int[3];
	}

	private void initMap() {
		mMapItem = ((com.ctg.ui.OBDApplication) getApplication()).wzMapItem;
		// 获取地图控件引用
		mMapView = (MapView) findViewById(R.id.wz_bmapView);

		getScreenSize();

		mBaiduMap = mMapView.getMap();
		// 初始化搜索模块，注册事件监听
		mSearch = GeoCoder.newInstance();
		mSearch.setOnGetGeoCodeResultListener(this);
		
		mSearch.geocode(new GeoCodeOption().city(
				(String) mMapItem.get("cityname")).address(
				(String) mMapItem.get("address")));
	}

	/**
	 * 获取一个手机的宽高相当于多少个百度地图比例尺，从而获得地图控件能够显示的范围，假设一个像素点是0.1毫米
	 */
	private void getScreenSize() {
		DisplayMetrics dm = new DisplayMetrics();
		mContext.getWindowManager().getDefaultDisplay().getMetrics(dm);
		hPx = Util.dip2px(mContext, 310);
		wPx = dm.widthPixels;
		mScreenWidth = (float) (wPx * 0.1 / 10);
		mScreenHeight = (float) (hPx * 0.1 / 10);
	}

	private void initView() {
		mBackImg = (ImageView) findViewById(R.id.wz_detail_back);

		mTitle = (TextView) findViewById(R.id.wz_detail_title_cont);
		mAddr = (TextView) findViewById(R.id.wz_addr);
		mCont = (TextView) findViewById(R.id.wz_content);
		mTime = (TextView) findViewById(R.id.wz_time);
		mStat = (TextView) findViewById(R.id.wz_process_status);
		mFine = (TextView) findViewById(R.id.wz_fine_amount);
		mPoint = (TextView) findViewById(R.id.point_loss);
		mPersonCnt = (TextView) findViewById(R.id.wz_person_count);
		loadData();

		mBaiduMap.setOnMapStatusChangeListener(MapStatelistener);
		mBaiduMap.setMapStatus(MapStatusUpdateFactory.zoomTo(15f));
		vio = new ViolationPtDataSource(mContext);
		vio.violationPtDataSourceDelegate = this;
		// vio.openDataSource();
		vio.updateDataSource();
		chaosu_imgv = (ImageView) findViewById(R.id.chaosu_img);
		weiting_imgv = (ImageView) findViewById(R.id.weiting_img);
		xianxing_imgv = (ImageView) findViewById(R.id.xianxing_img);
		clear_imgv = (ImageView) findViewById(R.id.clear_img);
	}

	private void initListener() {
		mBackImg.setOnClickListener(new android.view.View.OnClickListener() {
			public void onClick(View v) {
				TrafficVioDetailActivity.this.finish();
			}
		});

		curtype = new int[1];
		curtype[0] = -1;
		View.OnClickListener onclick = new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (pos == null)
					return;
				
				if(vioLatLng == null)	// null result from geocoding
					vioLatLng = pos;
					
				if (v == chaosu_imgv) {
					// curtype = new int[1];
					LogRecord.SaveLogInfo2File(Base.OperateInfo, TAG+"over speed click");
					curtype[0] = 0;
					mBaiduMap.clear();
					mBaiduMap.addOverlay(new MarkerOptions().position(vioLatLng)
							.icon(BitmapDescriptorFactory
							.fromResource(R.drawable.icon_home_navigation_active)));
					OBDApplication.getThreadPool().execute(runnable);
					chaosu_imgv.setImageResource(R.drawable.icon_overspeed_active);
					weiting_imgv.setImageResource(R.drawable.icon_illegally_parked);
					xianxing_imgv.setImageResource(R.drawable.icon_limit_line);
				} else if (v == weiting_imgv) {
					// curtype = new int[1];
					LogRecord.SaveLogInfo2File(Base.OperateInfo, TAG+"forbidden stop click");
					curtype[0] = 1;
					mBaiduMap.clear();
					mBaiduMap.addOverlay(new MarkerOptions().position(vioLatLng)
							.icon(BitmapDescriptorFactory
							.fromResource(R.drawable.icon_home_navigation_active)));
					OBDApplication.getThreadPool().execute(runnable);
					chaosu_imgv.setImageResource(R.drawable.icon_overspeed);
					weiting_imgv.setImageResource(R.drawable.icon_illegally_parked_active);
					xianxing_imgv.setImageResource(R.drawable.icon_limit_line);
					// Thread mThread = new Thread(runnable);
					// mThread.start();
				} else if (v == xianxing_imgv) {
					// curtype = new int[1];
					LogRecord.SaveLogInfo2File(Base.OperateInfo, TAG+"forbidden drive direction click");
					curtype[0] = 2;
					mBaiduMap.clear();
					mBaiduMap.addOverlay(new MarkerOptions().position(vioLatLng)
							.icon(BitmapDescriptorFactory
							.fromResource(R.drawable.icon_home_navigation_active)));
					OBDApplication.getThreadPool().execute(runnable);
					chaosu_imgv.setImageResource(R.drawable.icon_overspeed);
					weiting_imgv.setImageResource(R.drawable.icon_illegally_parked);
					xianxing_imgv.setImageResource(R.drawable.icon_limit_line_active);
				} else if (v == clear_imgv) {
					curtype[0] = -1;
					mBaiduMap.clear();
				}
				// mSearch.geocode(new GeoCodeOption().city(
				// (String) mMapItem.get("cityname")).address(
				// (String) mMapItem.get("address")));

			}

		};
		chaosu_imgv.setOnClickListener(onclick);
		weiting_imgv.setOnClickListener(onclick);
		xianxing_imgv.setOnClickListener(onclick);
//		clear_imgv.setOnClickListener(onclick);
	}

	public void drawOverlayPt(List<ViolationPt> listPt, float scale) {
		// List<ViolationPt> listPt = ls.getMlsPt();
		if (listPt == null || listPt.size() == 0) {
			return;
		}
		
		int type = ensureScale(scale);
		if( type < 8 ){
			draw(100, 1000 / 1, listPt);
		}else{
			draw(60, 0, listPt);
		}


	}

	private void draw(int limit, double distance, List<ViolationPt> listPt) {

		int count = 0;
		ViolationPt lastPt = null;
		double mDistance = 0.0;
		int resId[] = { R.drawable.icon_site_overspeed, R.drawable.icon_site_illegally_parked, R.drawable.icon_site_limit_line };
		BitmapDescriptor bitmap = BitmapDescriptorFactory
				.fromResource(resId[curtype[0]]);

		while (count < limit && count < listPt.size()) {
			ViolationPt pt = listPt.get(count);
			if (lastPt != null) {
				mDistance = DistanceUtil
						.getDistance(pt.getPt(), lastPt.getPt());
			} else {
				mDistance = Double.MAX_VALUE;
			}

			if (pt != null && mDistance > distance) {
				OverlayOptions option = new MarkerOptions()
						.position(pt.getPt()).icon(bitmap);

				mBaiduMap.addOverlay(option);

			} else {
				break;
			}
			lastPt = pt;
			count++;
		}
	}

	private int ensureScale(float scale) {
		if (scale < 4 && scale >= 3)// 1000km
			return 3;
		else if (scale < 5 && scale >= 4)// 500km
			return 4;
		else if (scale < 6 && scale >= 5)// 2500km
			return 5;
		else if (scale < 7 && scale >= 6)// 1000km
			return 6;
		else if (scale < 8 && scale >= 7)// 500km
			return 7;
		else if (scale < 9 && scale >= 8)// 500km
			return 8;
		else if (scale < 10 && scale >= 9)// 250km
			return 9;
		else if (scale < 11 && scale >= 10)// 100km
			return 10;
		else if (scale < 12 && scale >= 11)// 50km
			return 11;
		else if (scale < 13 && scale >= 12)// 25km
			return 12;
		else if (scale < 14 && scale >= 13)// 10km
			return 13;
		else if (scale < 15 && scale >= 14)// 5km
			return 14;
		else if (scale < 16 && scale >= 15)// 2.5km
			return 15;
		else if (scale < 17 && scale >= 16)// 2.5km
			return 16;
		else if (scale < 18 && scale >= 17)// 1km
			return 17;
		else if (scale < 19 && scale >= 18)// 0.5km
			return 18;
		else if (scale == 19)// 0.2km
			return 19;
		else
			return 0;
	}

	BaiduMap.OnMapStatusChangeListener MapStatelistener = new BaiduMap.OnMapStatusChangeListener() {
		public void onMapStatusChangeStart(MapStatus status) {
			pos = status.target;
			zoom = status.zoom;
			// if (curtype[0] == -1)
			// return;
			// if (bLoad) {
			// bLoad = false;
			// OBDApplication.getThreadPool().execute(runnable);
			// }
		}

		public void onMapStatusChange(MapStatus status) {
			pos = status.target;
			zoom = status.zoom;
		}

		public void onMapStatusChangeFinish(MapStatus status) {
			pos = status.target;
			zoom = status.zoom;
			// 地图缩放级别 3~19
			if (curtype[0] == -1)
				return;
			if (bLoad) {
				bLoad = false;
				OBDApplication.getThreadPool().execute(runnable);
			}
		}
	};

	Runnable runnable = new Runnable() {
		@Override
		public void run() {
			// MapStatus status = mBaiduMap.getMapStatus();
			if(!vio.openDataSource())
				return;
			final ViolationList ls = vio.getlistVioByCoord(pos.longitude,
					pos.latitude, zoom, curtype, mScreenWidth, mScreenHeight);
			if (ls != null) {
				if (lastLs != null) {
					drawOverlayPt(findPt(lastLs, ls), zoom);
					bLoad = true;
				} else {
					drawOverlayPt(ls.getMlsPt(), zoom);
					bLoad = true;
					lastLs = ls;
				}
			}
		}

	};

	private List<ViolationPt> findPt(ViolationList lastPt, ViolationList ls) {
		// 判断上个集合的点在不在这次查找的点集中 如果在的话 优先画这些点。数据库操作是不是会方便点？
		List<ViolationPt> last = lastPt.getMlsPt();
		List<ViolationPt> current = ls.getMlsPt();
		for (int i = 0; i < current.size(); i++) {
			for (int j = 0; j < last.size(); j++) {
				if (current.get(i).equals(last.get(j))) {
					// newPts.add(current.get(i));
					current.get(i).setFrequency(Integer.MAX_VALUE);
					break;
				}
			}

		}
		return current;

	}

	@Override
	protected void onPause() {
		/**
		 * MapView的生命周期与Activity同步，当activity挂起时需调用MapView.onPause()
		 */
		mMapView.onPause();
		super.onPause();
	}

	@Override
	protected void onResume() {
		/**
		 * MapView的生命周期与Activity同步，当activity恢复时需调用MapView.onResume()
		 */
		mMapView.onResume();
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		/**
		 * MapView的生命周期与Activity同步，当activity销毁时需调用MapView.destroy()
		 */
		mMapView.onDestroy();
		super.onDestroy();
	}

	public void loadData() {
		mTitle.setText((String) mMapItem.get("licence"));
		mAddr.setText((String) mMapItem.get("address"));
		mCont.setText((String) mMapItem.get("content"));
		mTime.setText((String) mMapItem.get("date"));
		// mTime.setText((String) mMapItem.get("time"));
		mStat.setText((String) mMapItem.get("proc_stat"));
		mFine.setText(mMapItem.get("money")
				+ mContext.getResources().getString(R.string.wz_rmb));
		mPoint.setText(mMapItem.get("fen")
				+ mContext.getResources().getString(R.string.wz_point));
		mPersonCnt.setText("1");

	}

	@Override
	public void onGetGeoCodeResult(GeoCodeResult result) {
		if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR 
				|| result.getLocation() == null) {
			Toast.makeText(mContext, "抱歉，未能找到违章地点", Toast.LENGTH_SHORT).show();
//			mSearch.geocode(new GeoCodeOption().city(
//					(String) mMapItem.get("cityname")).address(
//					(String) mMapItem.get("address")));
			return;
		}
		// mBaiduMap.clear();
		vioLatLng = result.getLocation();
		Log.v(TAG, result.getLocation().toString());
		mBaiduMap.addOverlay(new MarkerOptions().position(result.getLocation()).icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_home_navigation_active)));
		mBaiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(result
				.getLocation()));
		String strInfo = String.format("纬度：%f 经度：%f",
				result.getLocation().latitude, result.getLocation().longitude);
		// Toast.makeText(mContext, strInfo, Toast.LENGTH_LONG).show();

		// addCustomElementsDemo(result.getLocation().latitude,
		// result.getLocation().longitude);
	}

	@Override
	public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
		if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
			Toast.makeText(mContext, "抱歉，未能找到结果", Toast.LENGTH_SHORT).show();

			return;
		}
		// mBaiduMap.clear();
		vioLatLng = result.getLocation();
		mBaiduMap.addOverlay(new MarkerOptions().position(vioLatLng)
				.icon(BitmapDescriptorFactory
						.fromResource(R.drawable.icon_home_navigation_active)));
		mBaiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(vioLatLng));
		// Toast.makeText(mContext, result.getAddress(),
		// Toast.LENGTH_LONG).show();

	}

	private NaviEngineInitListener mNaviEngineInitListener = new NaviEngineInitListener() {
		public void engineInitSuccess() {
			mIsEngineInitSuccess = true;
		}

		public void engineInitStart() {
		}

		public void engineInitFail() {
		}
	};

	private BNKeyVerifyListener mKeyVerifyListener = new BNKeyVerifyListener() {

		@Override
		public void onVerifySucc() {
			// TODO Auto-generated method stub
			// Toast.makeText(mContext, "key校验成功", Toast.LENGTH_LONG).show();
		}

		@Override
		public void onVerifyFailed(int arg0, String arg1) {
			// TODO Auto-generated method stub
			// Toast.makeText(mContext, "key校验失败", Toast.LENGTH_LONG).show();
		}
	};

	@Override
	public void updateResult(int ret) {
		// TODO Auto-generated method stub
		if (ViolationPtDataSource.RET_UPDATE_COMPETE == ret)
			vio.openDataSource();// open datasource

	}

	@Override
	public void onMapClick(LatLng arg0) {
		// TODO Auto-generated method stub
		Log.v("position", arg0.latitude + ", " + arg0.longitude);
	}

	@Override
	public boolean onMapPoiClick(MapPoi arg0) {
		// TODO Auto-generated method stub

		return false;
	}
}
