package com.ctg.util;

import java.util.ArrayList;
import java.util.List;

import com.baidu.mapapi.map.offline.MKOLSearchRecord;
import com.baidu.mapapi.map.offline.MKOLUpdateElement;
import com.baidu.mapapi.map.offline.MKOfflineMap;
import com.baidu.mapapi.map.offline.MKOfflineMapListener;
import com.ctg.ui.Base;
import com.ctg.ui.R;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SectionIndexer;
import android.widget.TextView;
import android.widget.Toast;

public class SortExpAdapter extends BaseExpandableListAdapter {

	private List<MKOLSearchRecord> list = null; // 这里是你listview里面要用到的内容的list
												// 可以自定义
	private List<MKOLSearchRecord> hotCityLst = null;
	private List<MKOLSearchRecord> curCityLst = null;
	private List<MKOLSearchRecord> availOfflineCityLst = null;
	private List<MKOLUpdateElement> downloadingCityLst = null;
	private Context mContext;
	MKOfflineMap offlineMap;

	ArrayList<MKOLUpdateElement> updateArrlst;

	public void setList(List<MKOLUpdateElement> downloadingCityLst) {
		this.downloadingCityLst = downloadingCityLst;
	}

	public void setCityList(List<MKOLSearchRecord> cityLst) {
		this.list = cityLst;
	}

	public void setMap(MKOfflineMap offlineMap) {
		this.offlineMap = offlineMap;
	}

	// private ArrayList<String> sections=new ArrayList<String>();
	// //这里是分组的名字也是在外面自定义，然后传进来就是，这和普通的Listview是一样的
	public SortExpAdapter(Context mContext, List<MKOLSearchRecord> list,
			MKOfflineMap offlineMap, List<MKOLUpdateElement> downloadingCityLst) {
		this.mContext = mContext;
		this.list = list;

		this.downloadingCityLst = downloadingCityLst;
		this.offlineMap = offlineMap;

		// this.setSections(list);
	}

	public void setHotCurAvailCity(List<MKOLSearchRecord> curCityLst,
			List<MKOLSearchRecord> hotCityLst,
			List<MKOLSearchRecord> availOfflineCityLst) {
		this.hotCityLst = hotCityLst;
		this.curCityLst = curCityLst;
		this.availOfflineCityLst = availOfflineCityLst;
	}

	MKOLUpdateElement getUpdateEle(int cityId) {
		downloadingCityLst = offlineMap.getAllUpdateInfo();
		if (downloadingCityLst != null) {
			int len = downloadingCityLst.size();
			int i = 0;
			MKOLUpdateElement ele;
			for (i = 0; i < len; i++) {
				ele = downloadingCityLst.get(i);
				if (ele != null && ele.cityID == cityId)
					return ele;
			}
		}
		return null;

	}

	public int getCount() { // 都是默认的
		return this.list.size();
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

	public class ViewHolder {
		public TextView cityName;
		public TextView downStat;
		public TextView downSize;
		public ImageView downIcon;

		TextView header;

	}

	/**
	 * author lzy 根据position 返回城市ID和所属集合
	 * 
	 * @param position
	 * @return
	 */
	public String getSectionForPosition(int position) {
		if (list.get(position).cityID == curCityLst.get(0).cityID) {
			return list.get(position).cityID + "/当前城市";
		} else if (list.get(position).cityID == hotCityLst.get(0).cityID) {
			return list.get(position).cityID + "/热门城市";
		} else if (list.get(position).cityID == availOfflineCityLst.get(0).cityID) {
			return list.get(position).cityID + "/所有城市";
		}
		return null;
	}

	/**
	 * author lzy 根据城市ID和所属列表的字符串 找到第一次出现该城市时候的position从而添加header标题
	 * 
	 * @param section
	 * @return
	 */
	public int getPositionForSection(String section) {
		for (int i = 0; i < getCount(); i++) {
			if (list.get(i).cityID == Integer.parseInt(section.substring(0,
					section.indexOf("/")))) {
				return i;
			}
		}
		return -1;

	}

	/**
	 * 提取英文的首字母，非英文字母用#代替。
	 * 
	 * @param str
	 * @return
	 */
	private String getAlpha(String str) {
		String sortStr = str.trim().substring(0, 1).toUpperCase();
		// 正则表达式，判断首字母是否是英文字母
		if (sortStr.matches("[A-Z]")) {
			return sortStr;
		} else {
			return "#";
		}
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		return list.get(groupPosition).childCities.get(childPosition);
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		return childPosition;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHolder viewHolder = null;
		final MKOLSearchRecord record = list.get(groupPosition).childCities
				.get(childPosition);
		MKOLUpdateElement update = getUpdateEle(record.cityID);
		if (convertView == null) {
			viewHolder = new ViewHolder();
			convertView = LayoutInflater.from(mContext).inflate(
					R.layout.down_city_item, null);
			viewHolder.cityName = (TextView) convertView
					.findViewById(R.id.dcity_name);
			viewHolder.downStat = (TextView) convertView
					.findViewById(R.id.dcity_stat);
			viewHolder.downSize = (TextView) convertView
					.findViewById(R.id.dcity_size);
			viewHolder.downIcon = (ImageView) convertView
					.findViewById(R.id.dcity_icon);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		// 根据position获取分类的首字母的Char ascii值
		// int section = getSectionForPosition(groupPosition);
		viewHolder.cityName

		.setText(record.cityName);
		viewHolder.cityName.setPadding(15, 0, 0, 0);

		viewHolder.downSize.setText(formatDataSize(record.size));
		if (update == null) {
			viewHolder.downStat.setVisibility(View.INVISIBLE);
			viewHolder.downIcon.setImageResource(R.drawable.down_black);
		} else {
			switch (update.status) {
			case MKOLUpdateElement.FINISHED:
				viewHolder.downStat.setVisibility(View.VISIBLE);
				viewHolder.downStat.setText("已下载");
				viewHolder.downIcon.setImageResource(R.drawable.down_gray);

				break;
			case MKOLUpdateElement.DOWNLOADING:

				viewHolder.downStat.setVisibility(View.VISIBLE);

				viewHolder.downStat.setText("正在下载");
				viewHolder.downIcon.setImageResource(R.drawable.down_black);

				break;
			case MKOLUpdateElement.WAITING:
				viewHolder.downIcon.setImageResource(R.drawable.down_black);
				viewHolder.downStat.setVisibility(View.VISIBLE);
				viewHolder.downStat.setText("等待下载");
				break;
			case MKOLUpdateElement.SUSPENDED:
				viewHolder.downIcon.setImageResource(R.drawable.down_black);

				viewHolder.downStat.setVisibility(View.VISIBLE);
				viewHolder.downStat.setText("已暂停");

				break;
			case MKOLUpdateElement.UNDEFINED:
				viewHolder.downIcon.setImageResource(R.drawable.down_black);
				viewHolder.downStat.setVisibility(View.VISIBLE);
				viewHolder.downStat.setText("未定义错误");

				break;
			case MKOLUpdateElement.eOLDSIOError:
				viewHolder.downIcon.setImageResource(R.drawable.down_black);
				viewHolder.downStat.setVisibility(View.VISIBLE);
				viewHolder.downStat.setText("读写失败");
				break;
			case MKOLUpdateElement.eOLDSMd5Error:
				viewHolder.downIcon.setImageResource(R.drawable.down_black);
				viewHolder.downStat.setVisibility(View.VISIBLE);
				viewHolder.downStat.setText("校验异常");
				break;
			case MKOLUpdateElement.eOLDSMissData:
				viewHolder.downIcon.setImageResource(R.drawable.down_black);
				viewHolder.downStat.setVisibility(View.VISIBLE);
				viewHolder.downStat.setText("数据丢失");
				break;
			case MKOLUpdateElement.eOLDSNetError:
				viewHolder.downIcon.setImageResource(R.drawable.down_black);
				viewHolder.downStat.setVisibility(View.VISIBLE);
				viewHolder.downStat.setText("网络异常");

				break;
			case MKOLUpdateElement.eOLDSWifiError:
				viewHolder.downIcon.setImageResource(R.drawable.down_black);
				viewHolder.downStat.setVisibility(View.VISIBLE);
				viewHolder.downStat.setText("wifi网络异常");

				break;
			}
		}
		return convertView;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		// TODO Auto-generated method stub
		return list.get(groupPosition) == null ? 0
				: list.get(groupPosition).cityType == 1 ? list
						.get(groupPosition).childCities.size() : 0;
	}

	@Override
	public Object getGroup(int groupPosition) {
		// TODO Auto-generated method stub
		return list.get(groupPosition);
	}

	@Override
	public int getGroupCount() {
		// TODO Auto-generated method stub
		return list.size();
	}

	@Override
	public long getGroupId(int groupPosition) {
		// TODO Auto-generated method stub
		return groupPosition;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHolder viewHolder = null;
		final MKOLSearchRecord record = (MKOLSearchRecord) list
				.get(groupPosition);
		MKOLUpdateElement update = getUpdateEle(record.cityID);
		if (convertView == null) {
			viewHolder = new ViewHolder();
			convertView = LayoutInflater.from(mContext).inflate(
					R.layout.down_city_item, null);
			viewHolder.cityName = (TextView) convertView
					.findViewById(R.id.dcity_name);
			viewHolder.downStat = (TextView) convertView
					.findViewById(R.id.dcity_stat);
			viewHolder.downSize = (TextView) convertView
					.findViewById(R.id.dcity_size);
			viewHolder.downIcon = (ImageView) convertView
					.findViewById(R.id.dcity_icon);
			viewHolder.header = (TextView) convertView
					.findViewById(R.id.dcity_item_header);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		viewHolder.header.setVisibility(View.GONE);

		// 根据position获取分类的首字母的Char ascii值
		String section = null;
		if (curCityLst != null && hotCityLst != null) {
			section = getSectionForPosition(groupPosition);
		}

		if (section != null && getPositionForSection(section) == groupPosition) {
			viewHolder.header.setVisibility(View.VISIBLE);
			viewHolder.header.setText(section.substring(section
					.lastIndexOf("/") + 1));
		}

		viewHolder.cityName.setText(record.cityName);
		viewHolder.downSize.setText(formatDataSize(record.size));

		if( record.cityType == 1){//省份
			viewHolder.downStat.setVisibility(View.INVISIBLE);
			viewHolder.downIcon.setImageResource(R.drawable.arrow_d_b);
		}else if (update == null) {
			viewHolder.downStat.setVisibility(View.INVISIBLE);
			viewHolder.downIcon.setImageResource(R.drawable.down_black);

		} else {
			switch (update.status) {
			case MKOLUpdateElement.FINISHED:
				viewHolder.downStat.setVisibility(View.VISIBLE);
				viewHolder.downStat.setText("已下载");
				viewHolder.downIcon.setImageResource(R.drawable.down_gray);

				break;
			case MKOLUpdateElement.DOWNLOADING:

				viewHolder.downStat.setVisibility(View.VISIBLE);

				viewHolder.downStat.setText("正在下载");
				viewHolder.downIcon.setImageResource(R.drawable.down_black);

				break;
			case MKOLUpdateElement.WAITING:
				viewHolder.downIcon.setImageResource(R.drawable.down_black);
				viewHolder.downStat.setVisibility(View.VISIBLE);
				viewHolder.downStat.setText("等待下载");
				break;
			case MKOLUpdateElement.SUSPENDED:
				viewHolder.downIcon.setImageResource(R.drawable.down_black);

				viewHolder.downStat.setVisibility(View.VISIBLE);
				viewHolder.downStat.setText("已暂停");

				break;
			case MKOLUpdateElement.UNDEFINED:
				viewHolder.downIcon.setImageResource(R.drawable.down_black);
				viewHolder.downStat.setVisibility(View.VISIBLE);
				viewHolder.downStat.setText("未定义错误");

				break;
			case MKOLUpdateElement.eOLDSIOError:
				viewHolder.downIcon.setImageResource(R.drawable.down_black);
				viewHolder.downStat.setVisibility(View.VISIBLE);
				viewHolder.downStat.setText("读写失败");
				break;
			case MKOLUpdateElement.eOLDSMd5Error:
				viewHolder.downIcon.setImageResource(R.drawable.down_black);
				viewHolder.downStat.setVisibility(View.VISIBLE);
				viewHolder.downStat.setText("校验异常");
				break;
			case MKOLUpdateElement.eOLDSMissData:
				viewHolder.downIcon.setImageResource(R.drawable.down_black);
				viewHolder.downStat.setVisibility(View.VISIBLE);
				viewHolder.downStat.setText("数据丢失");
				break;
			case MKOLUpdateElement.eOLDSNetError:
				viewHolder.downIcon.setImageResource(R.drawable.down_black);
				viewHolder.downStat.setVisibility(View.VISIBLE);
				viewHolder.downStat.setText("网络异常");

				break;
			case MKOLUpdateElement.eOLDSWifiError:
				viewHolder.downIcon.setImageResource(R.drawable.down_black);
				viewHolder.downStat.setVisibility(View.VISIBLE);
				viewHolder.downStat.setText("wifi网络异常");

				break;
			}
		}

		return convertView;
	}

	public void refreshMyself() {
		downloadingCityLst = offlineMap.getAllUpdateInfo();
		setList(downloadingCityLst);

		notifyDataSetChanged();
	}

	@Override
	public boolean hasStableIds() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		return true;
	}

}
