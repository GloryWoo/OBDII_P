package com.ctg.netmusic;

import com.ctg.group.GrpListAdapter.ViewHolder;
import com.ctg.ui.Base;
import com.ctg.ui.R;
import com.ximalaya.ting.android.opensdk.model.live.provinces.Province;
import com.ximalaya.ting.android.opensdk.model.live.provinces.ProvinceList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public class ProvLstAdapter extends BaseAdapter{
	Context mContext;
	ProvinceList mProvLst;
	int focusId;
	Province province;
	public ProvLstAdapter(Context context, ProvinceList provList){
		mContext = context;
		mProvLst = provList;
	}

	public void setProvLst(ProvinceList provList){
		mProvLst = provList;
		this.notifyDataSetChanged();
	}
	@Override
	public int getCount() {
		if(mProvLst != null)
			return mProvLst.getProvinceList().size();
		else
			return 0;
	}

	@Override
	public Object getItem(int position) {
		if(mProvLst != null)
			return mProvLst.getProvinceList().get(position);
		else
			return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	public void setFocusId(int focus){
		focusId = focus;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHolder vh = null;
		if(!Base.isBaseActive)
			return null;
		if (convertView == null) {
			convertView = View.inflate(mContext, R.layout.city_item, null);	
			vh = new ViewHolder();
			vh.cityNmTv = (TextView) convertView.findViewById(R.id.xima_prov_item);
			vh.focusImg = (ImageView) convertView.findViewById(R.id.xima_prov_line);
			convertView.setTag(vh);
        }
        else{
        	vh = (ViewHolder) convertView.getTag();
        }
		province = mProvLst.getProvinceList().get(position);
		vh.cityNmTv.setText(province.getProvinceName());	
		if(position == focusId){
			vh.focusImg.setVisibility(View.VISIBLE);
		}
		else
			vh.focusImg.setVisibility(View.INVISIBLE);
		return convertView;
	}

	public class ViewHolder{
		TextView cityNmTv;
		ImageView focusImg;
		
	}
}
