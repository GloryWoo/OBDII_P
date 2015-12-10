package com.ctg.util;

import java.util.ArrayList;
import java.util.List;

import com.baidu.mapapi.map.offline.MKOLSearchRecord;
import com.baidu.mapapi.map.offline.MKOLUpdateElement;
import com.baidu.mapapi.map.offline.MKOfflineMap;
import com.baidu.mapapi.map.offline.MKOfflineMapListener;
import com.ctg.bluetooth.BluetoothSet;
import com.ctg.ui.Base;
import com.ctg.ui.R;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ExpandableListView.OnGroupExpandListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.ctg.ui.R;

@SuppressLint("ResourceAsColor")
public class CityDownDlg extends Dialog implements
		DialogInterface.OnCancelListener {
	private static final String TAG = "CityDownDlg";
	Button cancelButton;
	private Base baseAct;
	MyViewPager vPager;
	TextView downMgr;
	TextView downCitylst;
	LinearLayout downMgrLinear;
	LinearLayout downCitylstLinear;
	ArrayList<View> listViews;
	ListView downMgrLstv;
	ExpandableListView cityLstv;
	RelativeLayout dcityUpdateRela;
	RelativeLayout updateAllRela;
	RelativeLayout downAllRela;
	RelativeLayout pauseAllRela;
	ImageView dcityUpdateBtn;
	ImageView cancelImgV;
	private static int default_width = 160; //
	private static int default_height = 120;//

	public MKOfflineMap offlineMap;
	SortExpAdapter dcityLstAdp;
	SortAdapter dmgrLstAdp;
	public ArrayList<MKOLUpdateElement> downloadingCityLst;// 正在下载的城市的集合
	public ArrayList<MKOLUpdateElement> downloadedCityLst;// 下载完毕的城市的集合
	public ArrayList<MKOLSearchRecord> dcityArrLst;
	public ArrayList<MKOLSearchRecord> curCityLst;
	public ArrayList<MKOLSearchRecord> hotCityLst;
	public ArrayList<MKOLSearchRecord> availOfflineCityLst;

	// ////////////////////////lzy
	private boolean isLayoutVisiable = false;
	private PopupWindow popupWindow;
	private ProgressBar progress;
	private TextView pop_map, pop_down_stop, pop_remove;
	private static ArrayList<Integer> downloading_cityIds = new ArrayList<Integer>();
	private boolean isDownloading = true;
	
	
	public void setProgressBar(ProgressBar progress){
		this.progress = progress;
	}

	@Override
	public void onCancel(DialogInterface dialog) {
		// TODO Auto-generated method stub
		baseAct.cityDownDlg = null;
	}

	public CityDownDlg(Context context, int layout, int style) {
		this(context, default_width, default_height, layout, style);

	}

	protected void onDestroy() {
	}

	public CityDownDlg(Context context, int width, int height, int layout,
			int style) {
		super(context, style);
		// set content
		setContentView(layout);

		// mac_address_init();
		// set window params
		Window window = getWindow();
		WindowManager.LayoutParams params = window.getAttributes();
		// set width,height by density and gravity
		float density = getDensity(context);
		params.width = width;// (int) (width*density);
		params.height = height;// (int) (height*density);
		params.gravity = Gravity.TOP;
		// params.verticalMargin = 2.0F;
		window.setAttributes(params);
		baseAct = (Base) context;
		vPager = (MyViewPager) findViewById(R.id.down_viewpager);
		vPager.setOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				topBarFocusChanged(position);
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
			}
		});
		downMgr = (TextView) findViewById(R.id.down_mngr);// 下载管理
		downCitylst = (TextView) findViewById(R.id.down_citylist);// 城市列表
		View.OnClickListener top_click = new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				switch (v.getId()) {
				case R.id.down_mngr:
					vPager.setCurrentItem(0, false);
					break;

				case R.id.down_citylist:
					vPager.setCurrentItem(1, false);
					break;

				default:
					break;
				}

			}
		};
		cancelImgV = (ImageView) findViewById(R.id.down_w_cancel_imgv);
		cancelImgV.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				CityDownDlg.this.cancel();
			}
		});
		downMgr.setOnClickListener(top_click);
		downCitylst.setOnClickListener(top_click);
		downMgrLinear = (LinearLayout) View.inflate(baseAct,
				R.layout.down_manager_list, null);
		downCitylstLinear = (LinearLayout) View.inflate(baseAct,
				R.layout.down_city_list, null);
		downMgrLstv = (ListView) downMgrLinear
				.findViewById(R.id.dcity_listview);
		cityLstv = (ExpandableListView) downCitylstLinear
				.findViewById(R.id.dcityex_listview);
//		dcityUpdateRela = (RelativeLayout) findViewById(R.id.dcity_m_update_cancel_rela);
//		dcityUpdateBtn = (ImageView) downMgrLinear
//				.findViewById(R.id.dcity_m_update_cancel_btn);
		updateAllRela = (RelativeLayout) downMgrLinear
				.findViewById(R.id.dcity_updateall_rela);
		downAllRela = (RelativeLayout) downMgrLinear
				.findViewById(R.id.dcity_downall_rela);
		pauseAllRela = (RelativeLayout) downMgrLinear
				.findViewById(R.id.dcity_pauseall_rela);
		dcityUpdateBtn.setOnClickListener(relativeClick);
		updateAllRela.setOnClickListener(relativeClick);
		downAllRela.setOnClickListener(relativeClick);
		pauseAllRela.setOnClickListener(relativeClick);

		listViews = new ArrayList<View>();
		listViews.add(downMgrLinear);
		listViews.add(downCitylstLinear);

		vPager.setAdapter(new MyPagerAdapter(listViews));

		downMgrLstv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub

			}

		});

		downMgrLstv.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub

			}
		});

		offlineMap = new MKOfflineMap();
		offlineMap.init(offlineListen);
		int num = offlineMap.importOfflineData(true);

		// downloadingCityLst = offlineMap.getAllUpdateInfo();
		if (downloadingCityLst == null) {
			downloadingCityLst = new ArrayList<MKOLUpdateElement>();
		}

		dcityArrLst = new ArrayList<MKOLSearchRecord>();
		curCityLst = offlineMap.searchCity(baseAct.baidu_v.mCity);
		hotCityLst = offlineMap.getHotCityList();
		availOfflineCityLst = offlineMap.getOfflineCityList();
		dcityArrLst.addAll(curCityLst);
		dcityArrLst.addAll(hotCityLst);
		dcityArrLst.addAll(availOfflineCityLst);
//		dcityLstAdp = new SortExpAdapter(baseAct, dcityArrLst, curCityLst,
//				hotCityLst, availOfflineCityLst, offlineMap, downloadingCityLst);
		cityLstv.setAdapter(dcityLstAdp);

		cityLstv.setGroupIndicator(null);// 去掉expandablelistview左边的默认箭头图标
		// cityLstv.setHeaderView(getLayoutInflater().inflate(R.layout.down_city_header,
		// cityLstv, false));

		// // 下载管理列表的listview

		downloadingCityLst = offlineMap.getAllUpdateInfo();
//		dmgrLstAdp = new SortAdapter(baseAct, downloadingCityLst,
//				downloadedCityLst, offlineMap);
		dmgrLstAdp.setListView(downMgrLstv);
		downMgrLstv.setAdapter(dmgrLstAdp);	

		downMgrLstv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				int[] arrayOfInt = new int[2];

				view.getLocationOnScreen(arrayOfInt);
		        int x = arrayOfInt[0];
		        int y = arrayOfInt[1];
				showPop(view, x, y+50, position);
				// LinearLayout layout = (LinearLayout) view
				// .findViewById(R.id.dcity_m_layout);
				// layout.setVisibility(isLayoutVisiable == true ? View.GONE
				// : View.VISIBLE);
			}
		});

		cityLstv.setOnChildClickListener(new OnChildClickListener() {
			@Override
			public boolean onChildClick(ExpandableListView parent, View v,
					int groupPosition, int childPosition, long id) {
				int cityId = dcityArrLst.get(groupPosition).childCities
						.get(childPosition).cityID;
				if (offlineMap.getUpdateInfo(cityId) == null){
					offlineMap.start(cityId);
				    downloading_cityIds.add(cityId);
				}
				return true;
			}
		});

		cityLstv.setOnGroupClickListener(new OnGroupClickListener() {

			@Override
			public boolean onGroupClick(ExpandableListView parent, View v,
					int groupPosition, long id) {
				// TODO Auto-generated method stub
				if (dcityArrLst.get(groupPosition).cityType != 1) {// isEmpty没有
					int cityId = dcityArrLst.get(groupPosition).cityID;
					if (offlineMap.getUpdateInfo(cityId) == null) {
						offlineMap.start(cityId);
						downloading_cityIds.add(cityId);
						// cityLstv.setAdapter(dcityLstAdp);
						// dcityLstAdp.notifyDataSetChanged();
						// downloadingCityLst.clear();
						// downloadingCityLst.addAll(offlineMap.getAllUpdateInfo());
						// downloadingCityLst.add(offlineMap.getUpdateInfo(cityId));
						// dmgrLstAdp.notifyDataSetChanged();
						// downMgrLstv.setAdapter(dmgrLstAdp);
						return true;
					} else {
						

						return true;
					}
				}
				return false;

			}
		});

	}

	/*
	 * public void start(int cityId) {
	 * 
	 * offlineMap.start(cityId); clickLocalMapListButton(null);
	 * Toast.makeText(baseAct, "开始下载离线地图. cityid: " + cityId,
	 * Toast.LENGTH_SHORT).show(); updateView(); }
	 */

	

	public void showPop(View parent, int x, int y, final int position) {

		// ����popwindow��ʾλ��
		popupWindow.showAtLocation(parent, 0, x, y);
		// ��ȡpopwindow����
		popupWindow.setFocusable(true);
		// ����popwindow�������������򣬱�رա�
		popupWindow.setOutsideTouchable(true);
		popupWindow.update();
		if (popupWindow.isShowing()) {

		}
		pop_map.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

			}
		});
		pop_down_stop.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (isDownloading) {
					offlineMap.pause(downloading_cityIds.get(position));
					isDownloading = false;
					// notifyDataSetChanged();
				} else {
					offlineMap.start(downloading_cityIds.get(position));
					isDownloading = true;
					// notifyDataSetChanged();
				}
			}
		});
		pop_remove.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				downloading_cityIds.remove(downloading_cityIds.get(position));
				offlineMap.remove(downloading_cityIds.get(position));
				downloadingCityLst = offlineMap.getAllUpdateInfo();
				dmgrLstAdp.setList(downloadingCityLst);
//				Toast.makeText(baseAct, "删除离线地图. cityid: " + e.cityID,
//						Toast.LENGTH_SHORT).show();
				dmgrLstAdp.notifyDataSetChanged();
			}
		});
	}

	class OfflineListener implements MKOfflineMapListener {

		@Override
		public void onGetOfflineMapState(int type, int state) {
			// TODO Auto-generated method stub
			switch (type) {
			case MKOfflineMap.TYPE_DOWNLOAD_UPDATE:
				Log.v("lzyyyyyyyyyyyyyyyy", "111111111111111111111");
				MKOLUpdateElement update = offlineMap.getUpdateInfo(state);
				// 离线地图下载更新 此时state为在安装的离线地图的城市ID
				if (update != null) {

					Log.v("lzyyyyyyyyyyyyyyyy", "222222222222222222");
					downloadingCityLst = offlineMap.getAllUpdateInfo();

					dmgrLstAdp.setList(downloadingCityLst);
					dmgrLstAdp.setOfflineMap(offlineMap);
					dcityLstAdp.setList(downloadingCityLst);

					dmgrLstAdp.notifyDataSetChanged();
					dcityLstAdp.notifyDataSetChanged();

					try {
						Thread.sleep(200);
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					// downMgrLstv.setAdapter(dmgrLstAdp);
					// cityLstv.setAdapter(dcityLstAdp);
				}
				break;

			case MKOfflineMap.TYPE_NEW_OFFLINE:
				// 有新离线地图安装
				// 有新离线地图安装 此时state为新安装的离线地图的数目
				Log.d("OfflineDemo",
						String.format("add offlinemap num:%d", state));
				break;

			case MKOfflineMap.TYPE_VER_UPDATE:
				// 版本更新提示
				MKOLUpdateElement e = offlineMap.getUpdateInfo(state);
				break;

			}
		}

	};

	public OfflineListener offlineListen = new OfflineListener();

	public boolean onKeyDown(int keyCode, KeyEvent event) {

		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			CityDownDlg.this.cancel();
			break;
		}
		return false;
	}

	View.OnClickListener relativeClick = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {

//			case R.id.dcity_m_update_cancel_rela:
//				break;

			case R.id.dcity_updateall_rela:
				break;

			case R.id.dcity_downall_rela:
				break;

			case R.id.dcity_pauseall_rela:
				break;

			default:
				break;
			}
		}
	};

	void topBarFocusChanged(int focus) {
		if (focus == 0) {
			downMgr.setTextColor(R.color.white);
			downMgr.setBackgroundResource(R.drawable.shape_bottom_f);
			downCitylst.setTextColor(R.color.black);
			downCitylst.setBackgroundResource(R.drawable.shape_bottom);
		} else {
			downMgr.setTextColor(R.color.black);
			downMgr.setBackgroundResource(R.drawable.shape_bottom);
			downCitylst.setTextColor(R.color.white);
			downCitylst.setBackgroundResource(R.drawable.shape_bottom_f);
		}
	}

	private float getDensity(Context context) {
		Resources resources = context.getResources();
		DisplayMetrics dm = resources.getDisplayMetrics();
		return dm.density;
	}

}
