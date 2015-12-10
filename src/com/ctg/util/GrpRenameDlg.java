package com.ctg.util;


import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.ctg.bluetooth.BluetoothSet;
import com.ctg.group.Group;
import com.ctg.net.HttpQueue;
import com.ctg.ui.Base;
import com.ctg.ui.R;
import com.ctg.util.GrpDelDlg.GrpLstSelAdapter;
import com.ctg.util.GrpDelDlg.GrpLstSelAdapter.ViewHolder;

import android.app.Dialog;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;

import android.util.DisplayMetrics;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class GrpRenameDlg extends Dialog { 
	private static final String TAG = "CreateGrpDlg";
    Button cancelButton;		
	private Base baseAct;

	private static int default_width = 160; //
	private static int default_height = 120;//

	LinearLayout linear;
    private TextView tv_show;// 用于显示选中的条目数量  
    Button commite_btn;
    public ListView groupLstV;
	public GrpLstEditAdapter grpLstAdapter;
	public ArrayList<Group> list;
	String usrName;
	
	public GrpRenameDlg(Context context, int layout, int style) {
		this(context, default_width, default_height, layout, style, null);
		
	}
	
	protected void onDestroy(){
	}
	
	public GrpRenameDlg(Context context, int width, int height, int layout, int style, String content) {
		super(context, style);
		//set content
		setContentView(layout);
		
		//mac_address_init();
		//set window params
		Window window = getWindow();
		WindowManager.LayoutParams params = window.getAttributes();
		//set width,height by density and gravity
		
		params.width = (int) width;
		params.height = (int) height;
		params.gravity = Gravity.TOP;
		//params.verticalMargin = 2.0F;
		window.setAttributes(params);
		baseAct = (Base)context;
		tv_show = (TextView) findViewById(R.id.tv);
		linear = (LinearLayout) findViewById(R.id.line);
		tv_show.setVisibility(View.GONE);
		linear.setVisibility(View.GONE);
		commite_btn = (Button) findViewById(R.id.result_btn);
		usrName = Preference.getInstance(baseAct).getUser();
		list = Group.filterGroupList(usrName, HttpQueue.grpResLst);
//		list = HttpQueue.grpResLst;  
		groupLstV = (ListView) findViewById(R.id.group_sel_del_lst);
        grpLstAdapter = new GrpLstEditAdapter(list, baseAct);
        groupLstV.setDivider(null);
        groupLstV.setAdapter(grpLstAdapter);
        
        groupLstV.setOnItemClickListener(new OnItemClickListener() {  
            @Override  
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,  
                    long arg3) {  
                // 取得ViewHolder对象，这样就省去了通过层层的findViewById去实例化我们需要的cb实例的步骤  
            	EditText edit = (EditText)arg1.getTag();  
                // 改变CheckBox的状态  
                 
            }  
        }); 
        
        commite_btn = (Button) findViewById(R.id.result_btn);
        commite_btn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				int len = list.size();
				boolean ret = false;
				
				JSONObject obj = new JSONObject();
				String url = Base.HTTP_GROUP_PATH+"/update";

				
				for(int i = 0; i < len; i++){	
					RelativeLayout rela = (RelativeLayout) groupLstV.getChildAt(i);
					EditText edit = (EditText) rela.findViewById(R.id.group_del_item_name);
					Group grp = list.get(i);;
					String text;
					if(edit != null && edit.getText() != null){
						text = edit.getText().toString();
						if(text != null && !text.equals(grp.name)){
							obj = new JSONObject();
							url = Base.HTTP_GROUP_PATH+"/update";
							try {
								obj.put("oldGroupName", grp.name);
								obj.put("newGroupName", text);
								obj.put("appid", "appid");
							} catch (JSONException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}					
							baseAct.httpQueueInstance.EnQueue(url, obj, 13);
							ret = true;
						}
					}
				}
				if(ret){
					GrpRenameDlg.this.cancel();
				}
				else
					Toast.makeText(baseAct, "没有选择删除任何群组", Toast.LENGTH_SHORT).show();
			}  
        	
        });
	}
	

	
	public class GrpLstEditAdapter extends BaseAdapter {
		// 填充数据的list
		private ArrayList<Group> list;
		// 用来控制CheckBox的选中状况
		private HashMap<Integer, Boolean> isSelected;
		// 上下文
		private Context context;
		// 用来导入布局
		private LayoutInflater inflater = null;

		// 构造器
		public GrpLstEditAdapter(ArrayList<Group> list, Context context) {
			this.context = context;
			this.list = list;
			inflater = LayoutInflater.from(context);
			isSelected = new HashMap<Integer, Boolean>();
			// 初始化数据
			initDate();
		}

		// 初始化isSelected的数据
		private void initDate() {
			for (int i = 0; i < list.size(); i++) {
				getIsSelected().put(i, false);
			}
		}

		@Override
		public int getCount() {
			return list.size();
		}

		@Override
		public Object getItem(int position) {
			return list.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			EditText edit = null;
			if (convertView == null) {
				// 获得ViewHolder对象

				// 导入布局并赋值给convertview
				convertView = inflater
						.inflate(R.layout.group_rename_item, null);
				edit = (EditText) convertView
						.findViewById(R.id.group_del_item_name);
				// 为view设置标签
				convertView.setTag(edit);
			} else {
				// 取出holder
				edit = (EditText) convertView.getTag();
			}
			// 设置list中TextView的显示
			Group grp = list.get(position);
			edit.setText(grp.name);
			// 根据isSelected来设置checkbox的选中状况
			return convertView;
		}

		public HashMap<Integer, Boolean> getIsSelected() {
			return isSelected;
		}

		public void setIsSelected(HashMap<Integer, Boolean> isSelected) {
			this.isSelected = isSelected;
		}

	}

}
