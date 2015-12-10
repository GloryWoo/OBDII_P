package com.ctg.util;

import java.util.List;
import java.util.Map;

import com.ctg.ui.Base;
import com.ctg.ui.R;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MyAdapter extends BaseAdapter {
	private Context mContext;

	private List<String> mListItems;

	private LayoutInflater listContainer;
	
	private TextView mTextV;
	private int resId;
	

	public MyAdapter(Context context, List<String> listItems, int resource) {
		mContext = context;
		listContainer = LayoutInflater.from(context);
		mListItems = listItems;
		resId = resource;
	}

	public void setList(List<String> listItems){
		mListItems = listItems;
		this.notifyDataSetChanged();
	}
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mListItems.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return mListItems.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}


	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final int selectID = position;
		TextView text_v = null;
		ImageView keybd_v;
		if (convertView == null) {
			convertView = listContainer.inflate(resId, null);
			// 获得控件对象
			text_v = (TextView) convertView.findViewById(R.id.search_poi_text);			
			// 设置空间集到convertView
			convertView.setTag(text_v);

		} else {
			text_v = (TextView) convertView.getTag();
		}
		text_v.setText(mListItems.get(position));
		keybd_v = (ImageView) convertView.findViewById(R.id.search_poi_keyboard);
		keybd_v.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Base baseAct = (Base) mContext;
				RelativeLayout parent = (RelativeLayout) v.getParent();
				TextView addr = (TextView) parent.findViewById(R.id.search_poi_text);
				if(Base.baidu_v.searchDlg != null)
					Base.baidu_v.searchDlg.poi_edit.setText(addr.getText().toString());
			}
			
		});
		// 设置文字图片
		return convertView;
	}

}