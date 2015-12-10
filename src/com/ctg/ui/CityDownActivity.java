package com.ctg.ui;

import java.util.ArrayList;
import java.util.List;

import com.baidu.mapapi.map.offline.MKOLSearchRecord;
import com.baidu.mapapi.map.offline.MKOLUpdateElement;
import com.baidu.mapapi.map.offline.MKOfflineMap;
import com.baidu.mapapi.map.offline.MKOfflineMapListener;
import com.ctg.util.CharacterParser;
import com.ctg.util.SortAdapter;
import com.ctg.util.SortExpAdapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.Toast;

public class CityDownActivity extends Activity implements
		OnClickListener {
	public final static int PAUSE_ONE = 0; // 暂停一个下载
	public final static int REMOVE_ONE = 1;// 删除一个下载
	public final static int START_ONE = 2;// 继续一个下载
	public final static int FINISHED_ONE = 3;// 完成一个下载

	private TextView downMgr, downCitylst, manage_update_all, manage_down_all,
			manage_pause_all;
	private RelativeLayout down_bottom_layout;
	private ImageView btn_back, down_bottom_pasue_img, down_bottom_start_img, title_line1, title_line2;
	private ExpandableListView cityLstv;
	private EditText search_city;

	ListView downMgrLstv;
	SortAdapter dmgrLstAdp;
	SortExpAdapter dcityLstAdp;

	private ViewPager viewPager;
	private View downView, manageView;
	ArrayList<View> views;
	private CharacterParser characterParser;

	public ArrayList<MKOLUpdateElement> downloadingCityLst;// 正在下载的城市的集合
	public ArrayList<MKOLSearchRecord> dcityArrLst;
	public ArrayList<MKOLSearchRecord> curCityLst;
	public ArrayList<MKOLSearchRecord> hotCityLst;
	public ArrayList<MKOLSearchRecord> availOfflineCityLst;

	private List<MKOLSearchRecord> filterDateList;// 搜索过滤后的集合
	public static MKOfflineMap offlineMap;
	MKOfflineMapListener offlineListen;

	public String curCity;
	private int clickCityId;

	private Handler adapterHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what) {
			case PAUSE_ONE:

				if (isPauseOrRemoveOrFinishedLastOne()) {
					changePauseAllToDisable();
				}
				changeDownAllToAble();
				dcityLstAdp.refreshMyself();
				break;
			case REMOVE_ONE:
				if (offlineMap.getAllUpdateInfo() == null) {
					down_bottom_layout.setVisibility(View.GONE);
					dcityLstAdp.refreshMyself();
					break;
				}
				// 暂停或删除的是最后一个下载的item 则要把全部暂停禁用
				if (isPauseOrRemoveOrFinishedLastOne()) {
					changePauseAllToDisable();
				}
				// 开始或删除的是最后一个下载的item 则要把全部下载禁用
				if (isStartOrRemoveOrFinishedLastOne()) {
					changeDownAllToDisable();
				}
				dcityLstAdp.refreshMyself();

				break;
			case START_ONE:
				if (isStartOrRemoveOrFinishedLastOne()) {
					changeDownAllToDisable();
				}
				changePauseAllToAble();
				dcityLstAdp.refreshMyself();

				break;
			case FINISHED_ONE:
				if (isPauseOrRemoveOrFinishedLastOne()) {
					changePauseAllToDisable();
				}
				if (isStartOrRemoveOrFinishedLastOne()) {
					changeDownAllToDisable();
				}
				dmgrLstAdp.refreshMyself();
				dcityLstAdp.refreshMyself();
				break;
			}

		}

	};
		
	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onCreate(arg0);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		

		if( getConnectedType(CityDownActivity.this) == -1){
			Toast.makeText(CityDownActivity.this, "当前网络不可用，请连接网络后再试", 1).show();
		}else{
			
		}
		setContentView(R.layout.down_whole);
		curCity = getIntent().getStringExtra("curCity");
		initView();
		initListener();
	}

	public String getCurCity() {
		return curCity;
	}

	private void initView() {

		// 实例化汉字转拼音类
		characterParser = CharacterParser.getInstance();

		// 外层页面
		btn_back = (ImageView) findViewById(R.id.down_w_cancel_imgv);
		title_line1 = (ImageView) findViewById(R.id.city_line1);
		title_line2 = (ImageView) findViewById(R.id.city_line2);
		downMgr = (TextView) findViewById(R.id.down_mngr);
		downCitylst = (TextView) findViewById(R.id.down_citylist);
		viewPager = (ViewPager) findViewById(R.id.down_viewpager);
		// 加载城市列表和下载管理view
		downView = LayoutInflater.from(CityDownActivity.this).inflate(
				R.layout.down_city_list, viewPager, false);
		manageView = LayoutInflater.from(CityDownActivity.this).inflate(
				R.layout.down_manager_list, viewPager, false);
		// 城市列表的内容
		cityLstv = (ExpandableListView) downView
				.findViewById(R.id.dcityex_listview);
		search_city = (EditText) downView.findViewById(R.id.down_city_search_e);

		if(offlineMap == null){
			offlineMap = new MKOfflineMap();			
		}
		offlineListen = new OfflineListener();
		offlineMap.init(offlineListen);		
		offlineMap.importOfflineData(true);
		downloadingCityLst = offlineMap.getAllUpdateInfo();

		if (downloadingCityLst == null) {
			downloadingCityLst = new ArrayList<MKOLUpdateElement>();
		}

		dcityArrLst = new ArrayList<MKOLSearchRecord>();
		if(curCity == null || curCity.equals(""))
			return;
		if (offlineMap != null) {
			curCityLst = offlineMap.searchCity(curCity);
			hotCityLst = offlineMap.getHotCityList();
			availOfflineCityLst = offlineMap.getOfflineCityList();
		}

		dcityArrLst.addAll(curCityLst);
		dcityArrLst.addAll(hotCityLst);
		dcityArrLst.addAll(availOfflineCityLst);
		dcityLstAdp = new SortExpAdapter(CityDownActivity.this, dcityArrLst,
				offlineMap, downloadingCityLst);

		dcityLstAdp.setHotCurAvailCity(curCityLst, hotCityLst,
				availOfflineCityLst);

		cityLstv.setAdapter(dcityLstAdp);
		cityLstv.setGroupIndicator(null);// 去掉expandablelistview左边的默认箭头图标

		// 下载管理的内容
		downMgrLstv = (ListView) manageView.findViewById(R.id.dcity_listview);
		down_bottom_layout = (RelativeLayout) manageView
				.findViewById(R.id.dcity_bottom_layout);// 底部layout
		down_bottom_pasue_img = (ImageView) manageView
				.findViewById(R.id.dcity_pauseall_img);
		down_bottom_start_img = (ImageView) manageView
				.findViewById(R.id.dcity_downall_img);

		manage_update_all = (TextView) manageView
				.findViewById(R.id.dcity_updateall_text);
		manage_down_all = (TextView) manageView
				.findViewById(R.id.dcity_downall_text);
		manage_pause_all = (TextView) manageView
				.findViewById(R.id.dcity_pauseall_text);

		if (offlineMap.getAllUpdateInfo() == null) {
			down_bottom_layout.setVisibility(View.GONE);
		} else {
			down_bottom_layout.setVisibility(View.VISIBLE);
			// 遍历所有的有状态的地图 如果有没在下载的就设置全部下载可见
			for (int i = 0; i < offlineMap.getAllUpdateInfo().size(); i++) {
				if (offlineMap.getAllUpdateInfo().get(i).status != MKOLUpdateElement.DOWNLOADING
						&& offlineMap.getAllUpdateInfo().get(i).status != MKOLUpdateElement.WAITING
						&& offlineMap.getAllUpdateInfo().get(i).status != MKOLUpdateElement.FINISHED) {
					changeDownAllToAble();
					break;
				} else {
					changeDownAllToDisable();
				}
			}
			// 遍历所有的有状态的地图 如果有在下载的就设置全部暂停可见
			for (int i = 0; i < offlineMap.getAllUpdateInfo().size(); i++) {
				if (offlineMap.getAllUpdateInfo().get(i).status == MKOLUpdateElement.DOWNLOADING
						|| offlineMap.getAllUpdateInfo().get(i).status == MKOLUpdateElement.WAITING) {
					changePauseAllToAble();
					break;
				} else {
					changePauseAllToDisable();
				}
			}

		}

		dmgrLstAdp = new SortAdapter(CityDownActivity.this, downloadingCityLst,
				offlineMap, adapterHandler);
		downMgrLstv.setAdapter(dmgrLstAdp);

		views = new ArrayList<View>();
		views.add(manageView);
		views.add(downView);

		PagerAdapter mPagerAdapter = new PagerAdapter() {

			@Override
			public boolean isViewFromObject(View arg0, Object arg1) {
				return arg0 == arg1;
			}

			@Override
			public int getCount() {
				return views.size();
			}

			@Override
			public void destroyItem(View container, int position, Object object) {
				((ViewPager) container).removeView(views.get(position));
			}

			@Override
			public Object instantiateItem(View container, int position) {
				((ViewPager) container).addView(views.get(position));
				return views.get(position);
			}
		};

		viewPager.setAdapter(mPagerAdapter);

		viewPager.setCurrentItem(0);
		topBarFocusChanged(0);
	}

	private void initListener() {
		btn_back.setOnClickListener(this);
		downMgr.setOnClickListener(this);
		downCitylst.setOnClickListener(this);

		manage_down_all.setOnClickListener(this);
		manage_pause_all.setOnClickListener(this);
		manage_update_all.setOnClickListener(this);

		cityLstv.setOnChildClickListener(new OnChildClickListener() {
			@Override
			public boolean onChildClick(ExpandableListView parent, View v,
					int groupPosition, int childPosition, long id) {
				int cityId = dcityArrLst.get(groupPosition).childCities
						.get(childPosition).cityID;
				if (offlineMap.getUpdateInfo(cityId) == null) {
					offlineMap.start(cityId);
					downloadingCityLst = offlineMap.getAllUpdateInfo();
					down_bottom_layout.setVisibility(View.VISIBLE);
					changePauseAllToAble();
					dmgrLstAdp.refreshMyself();
					dcityLstAdp.refreshMyself();
					return true;
				} else {
					viewPager.setCurrentItem(0);
					changeFragment(0);
					return true;
				}

			}
		});

		cityLstv.setOnGroupClickListener(new OnGroupClickListener() {

			@Override
			public boolean onGroupClick(ExpandableListView parent, View v,
					int groupPosition, long id) {
				// TODO Auto-generated method stub
				if (dcityArrLst.get(groupPosition).cityType != 1) {// isEmpty没有
					clickCityId = dcityArrLst.get(groupPosition).cityID;
					if (offlineMap.getUpdateInfo(clickCityId) == null) {
						// fragmentCallBack.fun1(cityId);
						// downloadingCityLst.add(offlineMap.getUpdateInfo(cityId));

						offlineMap.start(clickCityId);
						downloadingCityLst = offlineMap.getAllUpdateInfo();
						dmgrLstAdp.refreshMyself();

						down_bottom_layout.setVisibility(View.VISIBLE);
						changePauseAllToAble();
						// updateCityView(cityLstv, v,
						// offlineMap.getUpdateInfo(clickCityId));

						dcityLstAdp.refreshMyself();

						return true;
					} else {
						viewPager.setCurrentItem(0);
						changeFragment(0);
						return true;
					}
				}
				return false;

			}
		});

		downMgrLstv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				LinearLayout layout = (LinearLayout) view
						.findViewById(R.id.dcity_m_layout);
				layout.setVisibility(View.GONE);
				dmgrLstAdp.changeImageVisable(view, position);
			}
		});
		// 设置城市列表搜索监听
		search_city.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// TODO Auto-generated method stub
				// 当输入框里面的值为空，更新为原来的列表，否则为过滤数据列表

				filterData(s.toString());
				dcityLstAdp.refreshMyself();

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
				dcityLstAdp.notifyDataSetChanged();
			}

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub

			}
		});

		viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

			@Override
			public void onPageSelected(int arg0) {
				// TODO Auto-generated method stub
				// 当滑动时，顶部的imageView是通过animation缓慢的滑动
				switch (arg0) {
				case 0:
					topBarFocusChanged(0);
					break;
				case 1:
					topBarFocusChanged(1);
					break;
				}

			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				// TODO Auto-generated method stub

			}
		});

	}

	/**
	 * 根据输入框中的值来过滤数据并更新ListView
	 * 
	 * @param filterStr
	 */
	private void filterData(String filterStr) {
		// filterDateList = new ArrayList<MKOLSearchRecord>();

		if (TextUtils.isEmpty(filterStr)) {
			dcityArrLst.clear();
			dcityLstAdp.refreshMyself();
			dcityArrLst.addAll(curCityLst);
			dcityArrLst.addAll(hotCityLst);
			dcityArrLst.addAll(availOfflineCityLst);
		} else {
			dcityArrLst.clear();
			for (MKOLSearchRecord cityRecord : availOfflineCityLst) {
				if (cityRecord.cityType == 1) {
					for (MKOLSearchRecord cityRecord1 : cityRecord.childCities) {
						String name = cityRecord1.cityName;
						if (name.toUpperCase().indexOf(
								filterStr.toString().toUpperCase()) != -1
								|| characterParser
										.getSelling(name)
										.toUpperCase()
										.startsWith(
												filterStr.toString()
														.toUpperCase())) {
							dcityArrLst.add(cityRecord1);
						}
					}
				} else {
					String name = cityRecord.cityName;
					if (name.toUpperCase().indexOf(
							filterStr.toString().toUpperCase()) != -1
							|| characterParser
									.getSelling(name)
									.toUpperCase()
									.startsWith(
											filterStr.toString().toUpperCase())) {
						dcityArrLst.add(cityRecord);
					}

				}
			}
		}
	}

	void topBarFocusChanged(int focus) {
		if (focus == 0) {
			title_line1.setVisibility(View.VISIBLE);
			title_line2.setVisibility(View.GONE);
//			downMgr.setTextColor(getResources().getColor(R.color.white));
//			downMgr.setBackgroundResource(R.drawable.shape_bottom_f);
//			downCitylst.setTextColor(getResources().getColor(R.color.black));
//			downCitylst.setBackgroundResource(R.drawable.shape_bottom);
		} else {
			title_line1.setVisibility(View.GONE);
			title_line2.setVisibility(View.VISIBLE);
//			downMgr.setTextColor(getResources().getColor(R.color.black));
//			downMgr.setBackgroundResource(R.drawable.shape_bottom);
//			downCitylst.setTextColor(getResources().getColor(R.color.white));
//			downCitylst.setBackgroundResource(R.drawable.shape_bottom_f);
		}
	}

	public void changeFragment(int i) {
		viewPager.setCurrentItem(i, false);
		topBarFocusChanged(i);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.down_w_cancel_imgv:
			this.finish();
			break;
		case R.id.down_mngr:
			changeFragment(0);
			break;
		case R.id.down_citylist:
			changeFragment(1);
			break;
		case R.id.dcity_updateall_text:

			break;
		case R.id.dcity_downall_text:
			int j = -1;
			for (int i = 0; i < offlineMap.getAllUpdateInfo().size(); i++) {
				j = offlineMap.getAllUpdateInfo().get(i).status;
				if (j != MKOLUpdateElement.DOWNLOADING
						&& j != MKOLUpdateElement.FINISHED
						&& j != MKOLUpdateElement.WAITING) {
					offlineMap
							.start(offlineMap.getAllUpdateInfo().get(i).cityID);
				}

			}
			changeDownAllToDisable();
			changePauseAllToAble();

			dmgrLstAdp.setList(offlineMap.getAllUpdateInfo());
			dcityLstAdp.setList(offlineMap.getAllUpdateInfo());

			dmgrLstAdp.notifyDataSetChanged();
			dcityLstAdp.notifyDataSetChanged();
			break;
		case R.id.dcity_pauseall_text:
			int k = -1;
			for (int i = 0; i < offlineMap.getAllUpdateInfo().size(); i++) {
				k = offlineMap.getAllUpdateInfo().get(i).status;
				if (k != MKOLUpdateElement.SUSPENDED
						&& k != MKOLUpdateElement.FINISHED) {
					offlineMap
							.pause(offlineMap.getAllUpdateInfo().get(i).cityID);
				}

			}
			changeDownAllToAble();
			changePauseAllToDisable();

			dmgrLstAdp.refreshMyself();
			dcityLstAdp.refreshMyself();

			break;
		default:
			break;
		}
	}

	/**
	 * 全部下载变为不可点击
	 */
	private void changeDownAllToDisable() {
		down_bottom_start_img.setImageResource(R.drawable.down_gray);
		manage_down_all.setTextColor(getResources().getColor(R.color.gray));
		manage_down_all.setClickable(false);

	}

	/**
	 * 全部下载变为可点击
	 */
	private void changeDownAllToAble() {
		down_bottom_start_img.setImageResource(R.drawable.down_black);
		manage_down_all.setTextColor(getResources().getColor(R.color.black));
		manage_down_all.setClickable(true);

		down_bottom_pasue_img.setImageResource(R.drawable.pause_nor);
		manage_pause_all.setTextColor(getResources().getColor(R.color.black));
		manage_pause_all.setClickable(true);
	}

	/**
	 * 全部暂停变为不可点击
	 */
	private void changePauseAllToDisable() {
		down_bottom_pasue_img.setImageResource(R.drawable.pause_dis);
		manage_pause_all.setTextColor(getResources().getColor(R.color.gray));
		manage_pause_all.setClickable(false);
	}

	/**
	 * 全部暂停变为可点击
	 */
	private void changePauseAllToAble() {

		down_bottom_pasue_img.setImageResource(R.drawable.pause_nor);
		manage_pause_all.setTextColor(getResources().getColor(R.color.black));
		manage_pause_all.setClickable(true);
	}

	/**
	 * 判断暂停或删除的城市是否是最后一个正在或等待下载的城市
	 */
	private boolean isPauseOrRemoveOrFinishedLastOne() {
		for (int i = 0; i < offlineMap.getAllUpdateInfo().size(); i++) {
			if (offlineMap.getAllUpdateInfo().get(i).status == MKOLUpdateElement.DOWNLOADING
					|| offlineMap.getAllUpdateInfo().get(i).status == MKOLUpdateElement.WAITING) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 判断开始或删除的城市是否是最后一个暂停下载的城市
	 */
	private boolean isStartOrRemoveOrFinishedLastOne() {

		for (int i = 0; i < offlineMap.getAllUpdateInfo().size(); i++) {
			if (offlineMap.getAllUpdateInfo().get(i).status == MKOLUpdateElement.SUSPENDED
					|| offlineMap.getAllUpdateInfo().get(i).status == MKOLUpdateElement.eOLDSIOError
					|| offlineMap.getAllUpdateInfo().get(i).status == MKOLUpdateElement.eOLDSMd5Error
					|| offlineMap.getAllUpdateInfo().get(i).status == MKOLUpdateElement.eOLDSMissData
					|| offlineMap.getAllUpdateInfo().get(i).status == MKOLUpdateElement.eOLDSWifiError) {
				return false;
			}

		}

		return true;
	}

	class OfflineListener implements MKOfflineMapListener {

		@Override
		public void onGetOfflineMapState(int type, int state) {
			// TODO Auto-generated method stub
			switch (type) {
			case MKOfflineMap.TYPE_DOWNLOAD_UPDATE:

				MKOLUpdateElement update = offlineMap.getUpdateInfo(state);
				// 离线地图下载更新 此时state为在安装的离线地图的城市ID
				if (update != null) {

					for (int i = 0; i < downloadingCityLst.size(); i++) {
						if (downloadingCityLst.get(i).cityID == state) {//
							if (update.status == MKOLUpdateElement.FINISHED) {
								dmgrLstAdp.refreshMyself();
								dcityLstAdp.refreshMyself();

							} else {
								updateDownView(downMgrLstv, i, update);// 如果已经在下载就局部刷新
							}

							// dcityLstAdp.setList(offlineMap.getAllUpdateInfo());
							// dcityLstAdp.notifyDataSetChanged();
							//break;
						} else {
							// downloadingCityLst =
							// offlineMap.getAllUpdateInfo();
//							dmgrLstAdp.setList(offlineMap.getAllUpdateInfo());
//							dmgrLstAdp.setOfflineMap(offlineMap);
//							// dcityLstAdp.setList(downloadingCityLst);
//
//							dmgrLstAdp.notifyDataSetChanged();
//
//							break;
						}
					}

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

	// 局部刷新
	public void updateDownView(ListView listView, int itemIndex,
			MKOLUpdateElement e) {
		// 得到第一个可显示控件的位置，
		int visiblePosition = listView.getFirstVisiblePosition();
		// 只有当要更新的view在可见的位置时才更新，不可见时，跳过不更新
		if (itemIndex - visiblePosition >= 0) {
			// 得到要更新的item的view
			View view = downMgrLstv.getChildAt(itemIndex - visiblePosition);
			// 从view中取得holder
			if(view == null)
				return;
			SortAdapter.ViewHolder holder = (SortAdapter.ViewHolder) view
					.getTag();

			holder.progress = (ProgressBar) view
					.findViewById(R.id.dcity_m_progress);
			holder.downSize = (TextView) view
					.findViewById(R.id.dcity_m_stat_size);
			holder.downStat = (TextView) view.findViewById(R.id.dcity_m_stat);
			holder.downSize = (TextView) view
					.findViewById(R.id.dcity_m_stat_size);
			holder.downIcon = (ImageView) view.findViewById(R.id.dcity_m_icon);
			holder.header = (TextView) view
					.findViewById(R.id.dcity_item_header);

			holder.progress = (ProgressBar) view
					.findViewById(R.id.dcity_m_progress);

			holder.downSize.setText(dmgrLstAdp.formatDataSize(e.size));
			holder.progress.setProgress(e.ratio);
			if(e.ratio == 100){
				adapterHandler.obtainMessage(FINISHED_ONE).sendToTarget();
			}
			switch (e.status) {
			case MKOLUpdateElement.FINISHED:
				holder.downStat.setVisibility(View.VISIBLE);
				holder.downStat.setText("已下载");
				holder.downIcon.setImageResource(R.drawable.down_gray);
				holder.progress.setVisibility(View.GONE);
				break;
			case MKOLUpdateElement.DOWNLOADING:

				// holder.progress.setVisibility(View.VISIBLE);
				holder.downStat.setVisibility(View.VISIBLE);
				holder.progress.setVisibility(View.VISIBLE);
				if (e.ratio != 100) {
					holder.downStat.setText("正在下载" + e.ratio + " %");
					holder.downIcon.setImageResource(R.drawable.down_black);
				} else {
					holder.downStat.setText("已下载");
				}

				break;
			case MKOLUpdateElement.WAITING:
				// holder.progress.setVisibility(View.VISIBLE);
				holder.downStat.setVisibility(View.VISIBLE);
				holder.downStat.setText("等待下载");
				break;
			case MKOLUpdateElement.SUSPENDED:
				// holder.progress.setVisibility(View.VISIBLE);

				holder.downStat.setVisibility(View.VISIBLE);
				holder.downStat.setText("已暂停");

				break;
			case MKOLUpdateElement.UNDEFINED:
				// holder.progress.setVisibility(View.VISIBLE);
				holder.downStat.setVisibility(View.VISIBLE);
				holder.downStat.setText("未定义错误");

				break;
			case MKOLUpdateElement.eOLDSIOError:
				// holder.progress.setVisibility(View.VISIBLE);
				holder.downStat.setVisibility(View.VISIBLE);
				holder.downStat.setText("读写失败");
				break;
			case MKOLUpdateElement.eOLDSMd5Error:
				// holder.progress.setVisibility(View.VISIBLE);
				holder.downStat.setVisibility(View.VISIBLE);
				holder.downStat.setText("校验异常");
				break;
			case MKOLUpdateElement.eOLDSMissData:
				// holder.progress.setVisibility(View.VISIBLE);
				holder.downStat.setVisibility(View.VISIBLE);
				holder.downStat.setText("数据丢失");
				break;
			case MKOLUpdateElement.eOLDSNetError:
				// holder.progress.setVisibility(View.VISIBLE);
				holder.downStat.setVisibility(View.VISIBLE);
				holder.downStat.setText("网络异常");

				break;
			case MKOLUpdateElement.eOLDSWifiError:
				// holder.progress.setVisibility(View.VISIBLE);
				holder.downStat.setVisibility(View.VISIBLE);
				holder.downStat.setText("wifi网络异常");

				break;
			}
		}
	}

	// 局部刷新城市列表
	public void updateCityView(ExpandableListView listView, View view,
			MKOLUpdateElement e) {
		// 得到第一个可显示控件的位置，
		int visiblePosition = listView.getFirstVisiblePosition();
		// 只有当要更新的view在可见的位置时才更新，不可见时，跳过不更新
		// if (itemIndex - visiblePosition >= 0) {
		// // 得到要更新的item的view
		// View view = cityLstv.getChildAt(itemIndex - visiblePosition);
		// // 从view中取得holder
		SortExpAdapter.ViewHolder holder = (SortExpAdapter.ViewHolder) view
				.getTag();

		holder.cityName = (TextView) view.findViewById(R.id.dcity_name);
		holder.downStat = (TextView) view.findViewById(R.id.dcity_stat);
		holder.downSize = (TextView) view.findViewById(R.id.dcity_size);
		holder.downIcon = (ImageView) view.findViewById(R.id.dcity_icon);

		switch (e.status) {
		case MKOLUpdateElement.FINISHED:
			holder.downStat.setVisibility(View.VISIBLE);
			holder.downStat.setText("已下载");
			holder.downIcon.setImageResource(R.drawable.down_gray);
			break;
		case MKOLUpdateElement.DOWNLOADING:

			holder.downStat.setText("正在下载");
			holder.downStat.setVisibility(View.VISIBLE);

			break;
		case MKOLUpdateElement.WAITING:
			// holder.progress.setVisibility(View.VISIBLE);
			holder.downStat.setVisibility(View.VISIBLE);
			holder.downStat.setText("等待下载");
			break;
		case MKOLUpdateElement.SUSPENDED:

			holder.downStat.setVisibility(View.VISIBLE);
			holder.downStat.setText("已暂停");

			break;
		case MKOLUpdateElement.UNDEFINED:
			// holder.progress.setVisibility(View.VISIBLE);
			holder.downStat.setVisibility(View.VISIBLE);
			holder.downStat.setText("未定义错误");

			break;
		case MKOLUpdateElement.eOLDSIOError:
			// holder.progress.setVisibility(View.VISIBLE);
			holder.downStat.setVisibility(View.VISIBLE);
			holder.downStat.setText("读写失败");
			break;
		case MKOLUpdateElement.eOLDSMd5Error:
			// holder.progress.setVisibility(View.VISIBLE);
			holder.downStat.setVisibility(View.VISIBLE);
			holder.downStat.setText("校验异常");
			break;
		case MKOLUpdateElement.eOLDSMissData:
			// holder.progress.setVisibility(View.VISIBLE);
			holder.downStat.setVisibility(View.VISIBLE);
			holder.downStat.setText("数据丢失");
			break;
		case MKOLUpdateElement.eOLDSNetError:
			// holder.progress.setVisibility(View.VISIBLE);
			holder.downStat.setVisibility(View.VISIBLE);
			holder.downStat.setText("网络异常");

			break;
		case MKOLUpdateElement.eOLDSWifiError:
			// holder.progress.setVisibility(View.VISIBLE);
			holder.downStat.setVisibility(View.VISIBLE);
			holder.downStat.setText("wifi网络异常");

			break;
		}

	}

	public int getConnectedType(Context context) {
		if (context != null) {
			ConnectivityManager mConnectivityManager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo mNetworkInfo = mConnectivityManager
					.getActiveNetworkInfo();
			if (mNetworkInfo != null && mNetworkInfo.isAvailable()) {
				return mNetworkInfo.getType();
			}else{
				return -1;
			}
		}
		return -1;
	}

}
