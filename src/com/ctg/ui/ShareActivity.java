package com.ctg.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.baidu.mapapi.map.offline.MKOLSearchRecord;
import com.ctg.group.Group;
import com.ctg.group.Member;
import com.ctg.net.HttpQueue;
import com.ctg.shareUserInfo.ShareUserTrace;
import com.ctg.util.CharacterParser;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class ShareActivity extends Activity implements OnClickListener {
	private Button btn_back;
	private TextView choose_all, choose_back, choose_confirm;
	private EditText user_search;
	private RelativeLayout choose_layout;
	private ListView listView;
	private ShareAdapter adapter;
	private List<Group> lsGroup;
	private List<Group> lsGroup_temp = new ArrayList<Group>();// 用于检索时候存放检索结果;
	private List<String> userName = new ArrayList<String>();;
	private List<Member> user;
	private List<Member> user_temp = new ArrayList<Member>();// 用于检索时候存放检索结果
	private Map<Integer, Boolean> stateMap = new HashMap<Integer, Boolean>();
	private int type = 0;
	private long traceId;
	private String groupName;
	ViewHolder viewHolder = null;
	private ShareUserTrace shareUserTrace = new ShareUserTrace(this);
	private CharacterParser characterParser;
	private final static int TYPE_GROUP = 0;
	private final static int TYPE_USER = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);// 去掉标题栏
		setContentView(R.layout.share_list);

		initData();
		initView();
		initListener();
	}

	private void initData() {
		// TODO Auto-generated method stub
		traceId = getIntent().getLongExtra("traceId", -1);
		// 实例化汉字转拼音类
		characterParser = CharacterParser.getInstance();
		lsGroup = HttpQueue.grpResLst;// 获取群组信息
		lsGroup_temp.addAll(lsGroup);

	}

	private void initView() {
		// TODO Auto-generated method stub

		listView = (ListView) findViewById(R.id.share_list);
		choose_all = (TextView) findViewById(R.id.choose_bar_all);
		choose_back = (TextView) findViewById(R.id.choose_bar_back);
		choose_confirm = (TextView) findViewById(R.id.share_confirm);
		user_search = (EditText) findViewById(R.id.share_user_search);
		btn_back = (Button) findViewById(R.id.habit_back);
		choose_layout = (RelativeLayout) findViewById(R.id.choose_bar);

		adapter = new ShareAdapter();
		listView.setAdapter(adapter);

	}

	private void initSelState(boolean state) {
		if (user_temp == null || user_temp.size() == 0)
			return;
		for (int i = 0; i < user_temp.size(); i++) {
			stateMap.put(i, state);
		}
	}

	private boolean isAllTrue() {
		for (int i = 0; i < user_temp.size(); i++) {
			if (!stateMap.get(i)) {
				return false;
			}
		}
		return true;
	}

	private void initListener() {
		btn_back.setOnClickListener(this);
		choose_all.setOnClickListener(this);
		choose_back.setOnClickListener(this);
		choose_confirm.setOnClickListener(this);

		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				if (type == TYPE_GROUP) {
					user = lsGroup.get(position).GetMemberList();
					// user_temp = user;
					user_temp.addAll(user);
					type = TYPE_USER;
					initSelState(false);
					adapter.notifyDataSetChanged();
					choose_confirm.setVisibility(View.VISIBLE);
					choose_layout.setVisibility(View.VISIBLE);
					choose_all.setText("全   选");
					groupName = lsGroup.get(position).name;
				} else if (type == TYPE_USER) {
					if (stateMap.get(position) == false) {
						stateMap.put(position, true);
						if (isAllTrue()) {
							choose_all.setText("全不选");
						}
					} else {
						stateMap.put(position, false);
						choose_all.setText("全  选");
					}
					adapter.notifyDataSetChanged();
				}
			}
		});

		user_search.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// TODO Auto-generated method stub
				filterData(s.toString(), type);
				adapter.notifyDataSetChanged();
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
				adapter.notifyDataSetChanged();
			}

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub

			}
		});
	}

	/**
	 * 根据输入框中的值来过滤数据并更新ListView
	 * 
	 * @param filterStr
	 */
	private void filterData(String filterStr, int type) {
		// filterDateList = new ArrayList<MKOLSearchRecord>();

		if (type == TYPE_GROUP) {
			if (TextUtils.isEmpty(filterStr)) {
				lsGroup_temp.clear();
				lsGroup_temp.addAll(lsGroup);
				if (isAllTrue()) {
					choose_all.setText("全不选");
				} else {

					choose_all.setText("全  选");
				}
				adapter.notifyDataSetChanged();
			} else {
				lsGroup_temp.clear();
				for (Group gro : lsGroup) {

					String name = gro.name;
					if (name.toUpperCase().indexOf(
							filterStr.toString().toUpperCase()) != -1
							|| characterParser
									.getSelling(name)
									.toUpperCase()
									.startsWith(
											filterStr.toString().toUpperCase())) {
						lsGroup_temp.add(gro);
					}
				}
			}
		} else {
			if (TextUtils.isEmpty(filterStr)) {
				user_temp.clear();
				user_temp.addAll(user);
				if (isAllTrue()) {
					choose_all.setText("全不选");
				} else {

					choose_all.setText("全  选");
				}
				adapter.notifyDataSetChanged();
			} else {
				user_temp.clear();
				for (Member mem : user) {

					String name = mem.name;
					if (name.toUpperCase().indexOf(
							filterStr.toString().toUpperCase()) != -1
							|| characterParser
									.getSelling(name)
									.toUpperCase()
									.startsWith(
											filterStr.toString().toUpperCase())) {
						user_temp.add(mem);
					}
				}
			}
		}

	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.habit_back:
			this.finish();
			break;
		case R.id.choose_bar_all:
			if (isAllTrue()) {
				initSelState(false);
				choose_all.setText("全   选");
				adapter.notifyDataSetChanged();
			} else {
				initSelState(true);
				choose_all.setText("全不选");
				adapter.notifyDataSetChanged();
			}

			break;
		case R.id.choose_bar_back:
			type = TYPE_GROUP;
			choose_confirm.setVisibility(View.GONE);
			choose_layout.setVisibility(View.GONE);
			adapter.notifyDataSetChanged();
			user_search.setText("");
			break;
		case R.id.share_confirm:

			for (int i = 0; i < stateMap.size(); i++) {
				if (stateMap.get(i) == true) {
					userName.add(user.get(i).name);
				}
			}
			shareUserTrace.shareTrace(traceId, groupName, userName);
			ShareActivity.this.finish();
			break;

		}
	}

	private class ShareAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			if (type == TYPE_GROUP) {
				return lsGroup_temp != null && lsGroup_temp.size() > 0 ? lsGroup_temp
						.size() : 0;
			} else {
				return user_temp != null && user_temp.size() > 0 ? user_temp
						.size() : 0;
			}

		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub

			switch (type) {
			case TYPE_GROUP:
				if (convertView == null) {
					convertView = LayoutInflater.from(ShareActivity.this)
							.inflate(R.layout.choose_user_item, null);
					viewHolder = new ViewHolder();
					viewHolder.groupName = (TextView) convertView
							.findViewById(R.id.choose_user_name);
					viewHolder.selImage = (ImageView) convertView
							.findViewById(R.id.choose_user_img);

					convertView.setTag(viewHolder);
				} else {
					viewHolder = (ViewHolder) convertView.getTag();
				}
				viewHolder.selImage.setVisibility(View.GONE);
				viewHolder.groupName.setText(lsGroup.get(position).name);
				break;
			case TYPE_USER:
				if (convertView == null) {
					convertView = LayoutInflater.from(ShareActivity.this)
							.inflate(R.layout.choose_user_item, null);
					viewHolder = new ViewHolder();
					viewHolder.groupName = (TextView) convertView
							.findViewById(R.id.choose_user_name);
					viewHolder.selImage = (ImageView) convertView
							.findViewById(R.id.choose_user_img);
					convertView.setTag(viewHolder);
				} else {
				}
				viewHolder = (ViewHolder) convertView.getTag();
				viewHolder.selImage.setVisibility(View.VISIBLE);
				if (stateMap.size() > 0) {
					if (stateMap.get(position) == false) {
						viewHolder.selImage
								.setImageResource(R.drawable.select_false);
					} else {
						viewHolder.selImage
								.setImageResource(R.drawable.select_true);
					}
				}
				viewHolder.groupName.setText(user_temp.get(position).name);

				break;
			}

			return convertView;
		}

	}

	public class ViewHolder {
		public TextView groupName;
		public ImageView groupImage, userImage, selImage;
	}

}
