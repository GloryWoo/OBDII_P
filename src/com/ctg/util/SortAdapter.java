package com.ctg.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.baidu.mapapi.map.offline.MKOLSearchRecord;
import com.baidu.mapapi.map.offline.MKOLUpdateElement;
import com.baidu.mapapi.map.offline.MKOfflineMap;
import com.baidu.mapapi.map.offline.MKOfflineMapListener;
import com.ctg.ui.Base;
import com.ctg.ui.CityDownActivity;
import com.ctg.ui.OBDApplication;
import com.ctg.ui.R;
import com.ctg.ui.TraceMapActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.sax.StartElementListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SectionIndexer;
import android.widget.TextView;
import android.widget.Toast;

public class SortAdapter extends BaseAdapter {
	private ArrayList<MKOLUpdateElement> downloadingCityLst = null;
	private Handler adapterHandler;
	private ListView listView;
	private MKOLUpdateElement update;

	private View mLastView;
	private int mLastPosition;
	private Context mContext;
	MKOfflineMap offlineMap;

	ArrayList<MKOLUpdateElement> updateArrlst;

	public SortAdapter(Context mContext,
			ArrayList<MKOLUpdateElement> downloadingCityLst,
			MKOfflineMap offlineMap, Handler adapterHandler) {
		this.mContext = mContext;
		this.adapterHandler = adapterHandler;
		this.downloadingCityLst = downloadingCityLst;
		this.offlineMap = offlineMap;

		// updateArrlst = offlineMap.getAllUpdateInfo();
		// this.setSections(list);
	}

	MKOLUpdateElement getUpdateEle(int cityId) {
		if (updateArrlst != null) {
			int len = updateArrlst.size();
			int i = 0;
			MKOLUpdateElement ele;
			for (i = 0; i < len; i++) {
				ele = updateArrlst.get(i);
				if (ele != null && ele.cityID == cityId)
					return ele;
			}
		}
		return null;

	}

	public int getCount() { // 都是默认的
		if (downloadingCityLst != null) {
			return downloadingCityLst.size();
		}
		return 0;
	}

	public Object getItem(int position) {
		return downloadingCityLst.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public String formatDataSize(int size) {
		String ret = "";
		if (size < (1024 * 1024)) {
			ret = String.format("%dK", size / 1024);
		} else {
			ret = String.format("%.1fM", size / (1024 * 1024.0));
		}
		return ret;
	}

	public void setList(ArrayList<MKOLUpdateElement> downloadingCityLst) {
		this.downloadingCityLst = downloadingCityLst;
	}

	public void setOfflineMap(MKOfflineMap offlineMap) {
		this.offlineMap = offlineMap;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHolder viewHolder = null;

		final int i = position;
		if (downloadingCityLst == null || downloadingCityLst.size() == 0) {
			return null;
		}
		final MKOLUpdateElement e = downloadingCityLst.get(position);
		if (convertView == null) {
			viewHolder = new ViewHolder();
			convertView = LayoutInflater.from(mContext).inflate(
					R.layout.down_manager_item, null);
			viewHolder.cityName = (TextView) convertView
					.findViewById(R.id.dcity_m_name);
			viewHolder.downStat = (TextView) convertView
					.findViewById(R.id.dcity_m_stat);
			viewHolder.downSize = (TextView) convertView
					.findViewById(R.id.dcity_m_stat_size);
			viewHolder.downIcon = (ImageView) convertView
					.findViewById(R.id.dcity_m_icon);
			viewHolder.header = (TextView) convertView
					.findViewById(R.id.dcity_item_header);
			viewHolder.btn_remove = (Button) convertView
					.findViewById(R.id.dcity_m_delete);
			viewHolder.btn_map = (Button) convertView
					.findViewById(R.id.dcity_m_map);
			viewHolder.btn_down = (Button) convertView
					.findViewById(R.id.dcity_m_update);
			viewHolder.progress = (ProgressBar) convertView
					.findViewById(R.id.dcity_m_progress);
			viewHolder.linearLayout = (LinearLayout) convertView
					.findViewById(R.id.dcity_m_layout);

			convertView.setTag(viewHolder);
		} else {

			viewHolder = (ViewHolder) convertView.getTag();
		}
		// viewHolder.linearLayout.setVisibility(View.GONE);

		switch (e.status) {
		case MKOLUpdateElement.FINISHED:
			viewHolder.downStat.setVisibility(View.VISIBLE);
			viewHolder.downStat.setText("已下载");
			viewHolder.downIcon.setImageResource(R.drawable.icon_download);
			viewHolder.progress.setVisibility(View.GONE);
			viewHolder.btn_down.setText("下载更新");

			viewHolder.btn_down.setTextColor(mContext.getResources().getColor(
					R.color.gray));
			viewHolder.btn_down.setClickable(false);

			break;
		case MKOLUpdateElement.DOWNLOADING:

			viewHolder.btn_down.setText("暂停");
			viewHolder.downStat.setVisibility(View.VISIBLE);
			viewHolder.progress.setVisibility(View.VISIBLE);
			if (e.ratio != 100) {
				viewHolder.downStat.setText("正在下载" + e.ratio + " %");
				viewHolder.downIcon.setImageResource(R.drawable.down_black);
			} else {
				viewHolder.downStat.setText("已下载");
			}

			break;
		case MKOLUpdateElement.WAITING:
			viewHolder.btn_down.setText("暂停");
			viewHolder.downStat.setVisibility(View.VISIBLE);
			viewHolder.downStat.setText("等待下载");
			break;
		case MKOLUpdateElement.SUSPENDED:
			// viewHolder.progress.setVisibility(View.VISIBLE);

			viewHolder.downStat.setVisibility(View.VISIBLE);
			viewHolder.downStat.setText("已暂停");

			break;
		case MKOLUpdateElement.UNDEFINED:
			// viewHolder.progress.setVisibility(View.VISIBLE);
			viewHolder.downStat.setVisibility(View.VISIBLE);
			viewHolder.downStat.setText("未定义错误");

			break;
		case MKOLUpdateElement.eOLDSIOError:
			// viewHolder.progress.setVisibility(View.VISIBLE);
			viewHolder.downStat.setVisibility(View.VISIBLE);
			viewHolder.downStat.setText("读写失败");
			break;
		case MKOLUpdateElement.eOLDSMd5Error:
			// viewHolder.progress.setVisibility(View.VISIBLE);
			viewHolder.downStat.setVisibility(View.VISIBLE);
			viewHolder.downStat.setText("校验异常");
			break;
		case MKOLUpdateElement.eOLDSMissData:
			// viewHolder.progress.setVisibility(View.VISIBLE);
			viewHolder.downStat.setVisibility(View.VISIBLE);
			viewHolder.downStat.setText("数据丢失");
			break;
		case MKOLUpdateElement.eOLDSNetError:
			// viewHolder.progress.setVisibility(View.VISIBLE);
			viewHolder.downStat.setVisibility(View.VISIBLE);
			viewHolder.downStat.setText("网络异常");

			break;
		case MKOLUpdateElement.eOLDSWifiError:
			// viewHolder.progress.setVisibility(View.VISIBLE);
			viewHolder.downStat.setVisibility(View.VISIBLE);
			viewHolder.downStat.setText("wifi网络异常");

			break;
		}

		viewHolder.cityName.setText(e.cityName);
		viewHolder.downSize.setText(formatDataSize(e.serversize));
		// viewHolder.downSize.setText(e.size + "");
		viewHolder.progress.setProgress(e.ratio);
		viewHolder.btn_map.setTag(e);
		viewHolder.btn_map.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				// Intent intent = new Intent(mContext, TraceMapActivity.class);
				Intent intent = new Intent(mContext, Base.class);
				intent.putExtra("x", e.geoPt.longitude);
				intent.putExtra("y", e.geoPt.latitude);
				intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				intent.setAction("OfflineMap");
				mContext.startActivity(intent);

			}
		});

		viewHolder.btn_down.setOnClickListener(new OnClickListener() {

			@SuppressLint("NewApi")
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				switch (offlineMap.getAllUpdateInfo().get(i).status) {
				case MKOLUpdateElement.FINISHED:

					if (offlineMap.getAllUpdateInfo().get(i).update) {
						((Button) v).setText("下载更新");
						((Button) v).setTextColor(mContext.getResources()
								.getColor(R.color.black));
					} else {
						Message msg = adapterHandler.obtainMessage();
						msg.what = CityDownActivity.FINISHED_ONE;
						msg.sendToTarget();
						((Button) v).setClickable(false);
						((Button) v).setText("下载更新");
						((Button) v).setTextColor(mContext.getResources()
								.getColor(R.color.gray));
					}
					refreshMyself();
					break;
				case MKOLUpdateElement.DOWNLOADING:
					((Button) v).setText("暂停");
					if (offlineMap.pause(e.cityID)) {
						refreshMyself();
						Message msg = adapterHandler.obtainMessage();
						msg.what = CityDownActivity.PAUSE_ONE;
						msg.sendToTarget();
						((Button) v).setText("下载");

					}

					break;
				case MKOLUpdateElement.WAITING:
					((Button) v).setText("暂停");
					if (offlineMap.pause(e.cityID)) {
						refreshMyself();
						Message msg = adapterHandler.obtainMessage();
						msg.what = CityDownActivity.PAUSE_ONE;
						msg.sendToTarget();
						((Button) v).setText("下载");

					}

					break;
				case MKOLUpdateElement.SUSPENDED:
					if (offlineMap.start(e.cityID)) {
						Message msg = adapterHandler.obtainMessage();
						msg.what = CityDownActivity.START_ONE;
						msg.sendToTarget();
						refreshMyself();
						((Button) v).setText("暂停");
					}
					break;
				case MKOLUpdateElement.UNDEFINED:
					if (offlineMap.start(e.cityID)) {
						Message msg = adapterHandler.obtainMessage();
						msg.what = CityDownActivity.START_ONE;
						msg.sendToTarget();
						refreshMyself();
						((Button) v).setText("暂停");
					}
					break;
				case MKOLUpdateElement.eOLDSIOError:
					if (offlineMap.start(e.cityID)) {
						Message msg = adapterHandler.obtainMessage();
						msg.what = CityDownActivity.START_ONE;
						msg.sendToTarget();
						refreshMyself();
						((Button) v).setText("暂停");
					}
					break;
				case MKOLUpdateElement.eOLDSMd5Error:
					if (offlineMap.start(e.cityID)) {
						Message msg = adapterHandler.obtainMessage();
						msg.what = CityDownActivity.START_ONE;
						msg.sendToTarget();
						refreshMyself();
						((Button) v).setText("暂停");
					}
					break;
				case MKOLUpdateElement.eOLDSMissData:
					if (offlineMap.start(e.cityID)) {
						Message msg = adapterHandler.obtainMessage();
						msg.what = CityDownActivity.START_ONE;
						msg.sendToTarget();
						refreshMyself();
						((Button) v).setText("暂停");
					}
					break;
				case MKOLUpdateElement.eOLDSNetError:
					if (offlineMap.start(e.cityID)) {
						Message msg = adapterHandler.obtainMessage();
						msg.what = CityDownActivity.START_ONE;
						msg.sendToTarget();
						refreshMyself();
						((Button) v).setText("暂停");
					}

					break;
				case MKOLUpdateElement.eOLDSWifiError:
					if (offlineMap.start(e.cityID)) {
						Message msg = adapterHandler.obtainMessage();
						msg.what = CityDownActivity.START_ONE;
						msg.sendToTarget();
						refreshMyself();
						((Button) v).setText("暂停");
					}

					break;
				}
			}
		});

		viewHolder.btn_remove.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				((View) v.getParent()).setVisibility(View.GONE);
				if (offlineMap.remove(e.cityID)) {
					setList(offlineMap.getAllUpdateInfo());					
					notifyDataSetChanged();
					Message msg = adapterHandler.obtainMessage();
					msg.what = CityDownActivity.REMOVE_ONE;
					msg.sendToTarget();

				}

			}
		});

		switch (e.status) {
		case MKOLUpdateElement.FINISHED:
			viewHolder.downStat.setVisibility(View.VISIBLE);
			viewHolder.downStat.setText("已下载");
			viewHolder.downIcon.setImageResource(R.drawable.down_gray);
			viewHolder.progress.setVisibility(View.GONE);
			viewHolder.btn_down.setClickable(false);
			break;
		case MKOLUpdateElement.DOWNLOADING:
			viewHolder.btn_down.setText("暂停");
			// viewHolder.progress.setVisibility(View.VISIBLE);
			viewHolder.downStat.setVisibility(View.VISIBLE);
			viewHolder.progress.setVisibility(View.VISIBLE);
			if (e.ratio != 100) {
				viewHolder.downStat.setText("正在下载 " + e.ratio + "%");
				viewHolder.downIcon.setImageResource(R.drawable.down_black);
			} else {
				viewHolder.downStat.setText("已下载");
			}

			break;
		case MKOLUpdateElement.WAITING:
			// viewHolder.progress.setVisibility(View.VISIBLE);
			viewHolder.downStat.setVisibility(View.VISIBLE);
			viewHolder.downStat.setText("等待下载");
			break;
		case MKOLUpdateElement.SUSPENDED:
			// viewHolder.progress.setVisibility(View.VISIBLE);
			viewHolder.btn_down.setText("下载");
			viewHolder.downStat.setVisibility(View.VISIBLE);
			viewHolder.downStat.setText("已暂停");

			break;
		case MKOLUpdateElement.UNDEFINED:
			// viewHolder.progress.setVisibility(View.VISIBLE);
			viewHolder.downStat.setVisibility(View.VISIBLE);
			viewHolder.downStat.setText("未定义错误");
			viewHolder.btn_down.setText("下载");
			break;
		case MKOLUpdateElement.eOLDSIOError:
			// viewHolder.progress.setVisibility(View.VISIBLE);
			viewHolder.downStat.setVisibility(View.VISIBLE);
			viewHolder.downStat.setText("读写失败");
			break;
		case MKOLUpdateElement.eOLDSMd5Error:
			// viewHolder.progress.setVisibility(View.VISIBLE);
			viewHolder.downStat.setVisibility(View.VISIBLE);
			viewHolder.downStat.setText("校验异常");
			break;
		case MKOLUpdateElement.eOLDSMissData:
			// viewHolder.progress.setVisibility(View.VISIBLE);
			viewHolder.downStat.setVisibility(View.VISIBLE);
			viewHolder.downStat.setText("数据丢失");
			break;
		case MKOLUpdateElement.eOLDSNetError:
			// viewHolder.progress.setVisibility(View.VISIBLE);
			viewHolder.downStat.setVisibility(View.VISIBLE);
			viewHolder.downStat.setText("网络异常");
			viewHolder.btn_down.setText("下载");
			break;
		case MKOLUpdateElement.eOLDSWifiError:
			// viewHolder.progress.setVisibility(View.VISIBLE);
			viewHolder.downStat.setVisibility(View.VISIBLE);
			viewHolder.downStat.setText("wifi网络异常");
			viewHolder.btn_down.setText("下载");
			break;
		}

		return convertView;
	}

	public class ViewHolder {
		public TextView cityName;
		public TextView downStat;
		public TextView downSize;
		public ImageView downIcon;

		public LinearLayout linearLayout;
		public ProgressBar progress;
		public Button btn_map, btn_down, btn_remove;
		public TextView header;

	}

	public int getNextSection(int section) {
		// for(int i=0,len=sections.size();i<len;i++)
		// {
		// if(sections.get(i).charAt(0)==section &&i+1<len)
		// return sections.get(i+1).charAt(0);
		// }
		return -1;
	}

	public void setListView(ListView listView) {
		this.listView = listView;
	}

	public ArrayList<MKOLUpdateElement> getDownloadingCityLst() {
		return this.downloadingCityLst;
	}

	public void changeImageVisable(View view, int position) {
		if (mLastView != null && mLastPosition != position) {
			ViewHolder holder = (ViewHolder) mLastView.getTag();
			switch (holder.linearLayout.getVisibility()) {
			case View.VISIBLE:
				holder.linearLayout.setVisibility(View.GONE);
				break;
			default:
				break;
			}
		}
		mLastPosition = position;
		mLastView = view;
		ViewHolder holder = (ViewHolder) view.getTag();
		switch (holder.linearLayout.getVisibility()) {
		case View.GONE:
			holder.linearLayout.setVisibility(View.VISIBLE);
			break;
		case View.VISIBLE:
			holder.linearLayout.setVisibility(View.GONE);
			break;
		}
	}

	public void refreshMyself() {
		downloadingCityLst = offlineMap.getAllUpdateInfo();
		setList(downloadingCityLst);

		notifyDataSetChanged();
	}

}
