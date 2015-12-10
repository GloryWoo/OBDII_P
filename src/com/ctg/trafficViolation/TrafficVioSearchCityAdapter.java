package com.ctg.trafficViolation;

import java.util.ArrayList;
import java.util.List;

import com.baidu.mapapi.map.offline.MKOLSearchRecord;
import com.baidu.mapapi.map.offline.MKOLUpdateElement;
import com.baidu.mapapi.map.offline.MKOfflineMap;
import com.baidu.mapapi.map.offline.MKOfflineMapListener;
import com.ctg.bean.City;
import com.ctg.bean.Province;
import com.ctg.ui.Base;
import com.ctg.ui.R;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
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

public class TrafficVioSearchCityAdapter extends BaseAdapter {
	private Context mContext;
	private City city;
	private List<Province> provinces;
	private List<City> cities;
	private OnCitySelected onCitySelected;
	private OnProvinceSelected onProvinceSelected;

	private int type = 0;// 0代表显示省份列表 1代表显示城市列表

	public interface OnCitySelected {
		void onCitySelected(int engineno, int classno, String cityName);
	}

	public interface OnProvinceSelected {
		void onProvinceSelected(List<City> cities);
	}

	public void setOnCitySelected(OnCitySelected onCitySelected) {
		this.onCitySelected = onCitySelected;
	}

	public void setOnProvinceSelected(OnProvinceSelected onProvinceSelected) {
		this.onProvinceSelected = onProvinceSelected;
	}

	public void setList(List<Province> provinces, List<City> cities,
			int type) {
		this.provinces = provinces;
		this.cities = cities;
		this.type = type;
	}

	public TrafficVioSearchCityAdapter(Context mContext, List<Province> provinces,
			List<City> cities, int type) {
		this.mContext = mContext;
		this.provinces = provinces;
		this.cities = cities;
		this.type = type;
	}

	public int getCount() { // 都是默认的
		return provinces == null || provinces.size() == 0 ? cities.size()
				: provinces.size();
	}

	public Object getItem(int position) {
		return null;
	}

	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHolder viewHolder = null;

		if (convertView == null) {
			viewHolder = new ViewHolder();
			convertView = LayoutInflater.from(mContext).inflate(
					R.layout.trafficvio_search_city_item, null);			
			viewHolder.cityName = (TextView) convertView
					.findViewById(R.id.trafficvio_search_city_name);
			viewHolder.cityLayout = (LinearLayout) convertView
					.findViewById(R.id.trafficvio_search_city_layout);

			convertView.setTag(viewHolder);
		} else {

			viewHolder = (ViewHolder) convertView.getTag();
		}

		if (type == 0) {
			viewHolder.cityName.setText("-"
					+ provinces.get(position).getProvince_name());			
		} else if (type == 1) {
			viewHolder.cityName.setText(cities.get(position).getCarHead()+"-"+cities.get(position).getCity_name());			
		}

		return convertView;
	}

	public class ViewHolder {
		public TextView cityShortName;
		public TextView cityName;
		public LinearLayout cityLayout;
	}

}
