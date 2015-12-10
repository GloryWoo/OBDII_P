package com.ctg.util;


import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.ctg.bluetooth.BluetoothSet;
import com.ctg.group.Group;
import com.ctg.group.Member;
import com.ctg.net.HttpQueue;
import com.ctg.ui.Base;
import com.ctg.ui.R;

import android.app.Dialog;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;

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
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class GrpDelDlg extends Dialog implements DialogInterface.OnCancelListener{ 
	private static final String TAG = "CreateGrpDlg";
    Button cancelButton;		
	private Base baseAct;
	public GrpLstSelAdapter grpLstAdapter;
	public GrpLstSelAdapter grpMemLstAdapter;
	public ArrayList<?> list;
//	public ArrayList<Member> memlist;
	public ListView groupLstV;
	TextView titlev;
	private Button bt_selectall;  
    private Button bt_cancel;  
    private Button bt_deselectall;  
    private int checkNum; // 记录选中的条目数量  
//    private TextView tv_show;// 用于显示选中的条目数量  
    Button commite_btn;
    int mOption;//0 删除群 ; 1 添加自己到该群; 2 选择用户; 4 选择群被用户加入
	private static int default_width = 160; //
	private static int default_height = 120;//

	public GrpDelDlg(Context context, int layout, int style) {
		this(context, default_width, default_height, layout, style, 0, null);
		
	}
	
	@Override
	public void onCancel(DialogInterface dialog) {
		// TODO Auto-generated method stub
//		if(dialog == baseAct.selGroupDlg)
//			baseAct.selGroupDlg = null;
//		else if(dialog == baseAct.delGroupDlg)
//			baseAct.delGroupDlg = null;
//		else if(dialog == baseAct.selMemDlg)
//			baseAct.selMemDlg = null;
//		else if(dialog == baseAct.selGroupJoinDlg)
//			baseAct.selGroupJoinDlg = null;
	}  
	


	public GrpDelDlg(Context context, int width, int height, int layout, int style, int option, ArrayList<?> lst) {
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
		titlev = (TextView)findViewById(R.id.grp_sel_title_t);
		if(option == 1){
			titlev.setText("加入群组");
		}
		else if(option == 2){
			if(baseAct.searchUserMode == 0)
				titlev.setText("添加成员");
			else
				titlev.setText("添加好友");						
		}
		
		groupLstV = (ListView) findViewById(R.id.group_sel_del_lst);
		
		bt_selectall = (Button) findViewById(R.id.bt_selectall);  
        bt_cancel = (Button) findViewById(R.id.bt_cancleselectall);  
        bt_deselectall = (Button) findViewById(R.id.bt_deselectall);  
//        tv_show = (TextView) findViewById(R.id.tv);  
//        list = new ArrayList<Group>();  
        list = lst;
        grpLstAdapter = new GrpLstSelAdapter(list, baseAct);
        groupLstV.setAdapter(grpLstAdapter);       
        groupLstV.setDivider(baseAct.gray_line_draw);
        commite_btn = (Button) findViewById(R.id.result_btn);
        
        mOption = option;
        if(mOption == 4){
        	findViewById(R.id.line).setVisibility(View.GONE);
        	findViewById(R.id.tv).setVisibility(View.GONE);
        	commite_btn.setVisibility(View.GONE);
        }
		// 全选按钮的回调接口  
        bt_selectall.setOnClickListener(new View.OnClickListener() {  
            @Override  
            public void onClick(View v) {  
                // 遍历list的长度，将MyAdapter中的map值全部设为true  
                for (int i = 0; i < list.size(); i++) {  
                	grpLstAdapter.getIsSelected().put(i, true);  
                }  
                // 数量设为list的长度  
                checkNum = list.size();  
                // 刷新listview和TextView的显示  
                dataChanged();  
            }  
        });  
  
        // 反选按钮的回调接口  
        bt_cancel.setOnClickListener(new View.OnClickListener() {  
            @Override  
            public void onClick(View v) {  
                // 遍历list的长度，将已选的设为未选，未选的设为已选  
                for (int i = 0; i < list.size(); i++) {  
                    if (grpLstAdapter.getIsSelected().get(i)) {  
                    	grpLstAdapter.getIsSelected().put(i, false);  
                        checkNum--;// 数量减1  
                    }  
                }  
                // 刷新listview和TextView的显示  
                dataChanged();  
            }  
        });  
  
        // 取消按钮的回调接口  
        bt_deselectall.setOnClickListener(new View.OnClickListener() {  
            @Override  
            public void onClick(View v) {  
                // 遍历list的长度，将已选的按钮设为未选  
                for (int i = 0; i < list.size(); i++) {  
                    if (grpLstAdapter.getIsSelected().get(i)) {  
                    	grpLstAdapter.getIsSelected().put(i, false);  
                        checkNum--;  
                    } else {  
                    	grpLstAdapter.getIsSelected().put(i, true);  
                        checkNum++;  
                    }  
                }  
                // 刷新listview和TextView的显示  
                dataChanged();  
            }  
        });  
  
        // 绑定listView的监听器  
        groupLstV.setOnItemClickListener(new OnItemClickListener() {  
            @Override  
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,  
                    long arg3) {  
                // 取得ViewHolder对象，这样就省去了通过层层的findViewById去实例化我们需要的cb实例的步骤  
            	GrpLstSelAdapter.ViewHolder holder = (GrpLstSelAdapter.ViewHolder) arg1.getTag(); 
            	
            	if(mOption == 4){
            		Group grp = HttpQueue.grpCreatorLst.get(arg2);
            		JSONArray array = new JSONArray();
            		for(Member mem : HttpQueue.grpSelMemberLst)
            		{
            			if(!grp.memberList.contains(mem)){
            				array.put(mem.name);
            			}
            		}
            		if(array.length() > 0){
            			String url = Base.HTTP_GROUP_PATH+"/inviteUsersToGroup";
            			JSONObject obj = new JSONObject();
						try {									
							obj.put("appid", "appid");
							obj.put("groupName", grp.name);
							obj.put("users", array);
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}					
						baseAct.httpQueueInstance.EnQueue(url, obj, 19);
            		}
            		else{
            			Toast.makeText(baseAct, "用户已经在该群", Toast.LENGTH_SHORT).show();
            		}
            		GrpDelDlg.this.cancel();
            		return;
            	}
                // 改变CheckBox的状态  
                holder.cb.toggle();  
                // 将CheckBox的选中状况记录下来  
                grpLstAdapter.getIsSelected().put(arg2, holder.cb.isChecked());  
                // 调整选定条目  
                if (holder.cb.isChecked() == true) {  
                    checkNum++;  
                } else {  
                    checkNum--;  
                }  
                // 用TextView显示  
//                tv_show.setText("已选中" + checkNum + "项"); 
                commite_btn.setText("确定("+checkNum+")");
            }  
        }); 
        
        
        commite_btn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				JSONObject obj;
				String url;
				
				if(checkNum > 0){
//					ArrayList<String> tmplist = new ArrayList<String>();
					HashMap<Integer, Boolean> selected = grpLstAdapter.getIsSelected();
					int len = list.size();
					if(mOption == 2){
						if(HttpQueue.grpSelMemberLst == null)
							HttpQueue.grpSelMemberLst = new ArrayList<Member>();
						else
							HttpQueue.grpSelMemberLst.clear();
					}
					for(int i = 0; i < len; i++){
						if(selected.get(i)){
							Group grp = null;							
							if(mOption == 0){
								grp = (Group) list.get(i);
								obj = new JSONObject();
								url = Base.HTTP_GROUP_PATH+"/delete";
								try {																	
									obj.put("groupName", grp.name);
									obj.put("appid", "appid");
								} catch (JSONException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								} 				
								baseAct.httpQueueInstance.EnQueue(url, obj, 12);
							}
							else if(mOption == 1){
								grp = (Group) list.get(i);
								obj = new JSONObject();
								url = Base.HTTP_GROUP_PATH+"/addUserToGroup";
								
								try {									
									obj.put("appid", "appid");
									obj.put("groupName", grp.name);
									obj.put("listMember", true);
								} catch (JSONException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}					
								baseAct.httpQueueInstance.EnQueue(url, obj, 15);
							}
							else if(mOption == 2){
								Member mem = HttpQueue.grpSearchMemberLst.get(i);
								HttpQueue.grpSelMemberLst.add(mem);
								
							}	
				
//							tmplist.add(list.get(i));
						}
					}
					if(mOption == 2){
						if(HttpQueue.grpSelMemberLst != null){
							int memSelLen = HttpQueue.grpSelMemberLst.size();
							if(memSelLen > 0){
	
								if(baseAct.searchUserMode == 0){//add group member

//									if(HttpQueue.grpCreatorLst != null && HttpQueue.grpCreatorLst.size() > 0){
//										baseAct.selGroupJoinDlg = new GrpDelDlg(baseAct, 320*baseAct.mDensityInt, 540*baseAct.mDensityInt, R.layout.group_list, R.style.Theme_dialog, 4, HttpQueue.grpCreatorLst);
//										baseAct.selGroupJoinDlg.show();
//									}
//									else{
//										Toast.makeText(baseAct, "你还没有创建任何群组", Toast.LENGTH_SHORT).show();
//									}
									Group grp = Base.me_v.getCurrentGrp();
									if(grp == null)
										return;
				            		JSONArray array = new JSONArray();
				            		for(Member mem : HttpQueue.grpSelMemberLst)
				            		{
				            			if(!grp.memberList.contains(mem)){
				            				array.put(mem.name);
				            			}
				            		}
				            		if(array.length() > 0){
				            			url = Base.HTTP_GROUP_PATH+"/inviteUsersToGroup";
				            			obj = new JSONObject();
										try {									
											obj.put("appid", "appid");
											obj.put("groupName", grp.name);
											obj.put("users", array);
										} catch (JSONException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}					
										baseAct.httpQueueInstance.EnQueue(url, obj, 19);
				            		}
				            		else{
				            			Toast.makeText(baseAct, "该群已有的用户不能重复添加", Toast.LENGTH_SHORT).show();
				            		}
				            		GrpDelDlg.this.cancel();
				            		return;									
								}
								else if(baseAct.searchUserMode == 1)//add friend
								{
									obj = new JSONObject();
									JSONArray jsonArr = new JSONArray();
									url = Base.HTTP_FRIEND_PATH+"/inviteFriend";
									for(Member member : HttpQueue.grpSelMemberLst){
										jsonArr.put(member.name);
									}
									try {									
										obj.put("users", jsonArr);
									} catch (JSONException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}					
									baseAct.httpQueueInstance.EnQueue(url, obj, 50);
								}
								else if(baseAct.searchUserMode == 2){//add to certain group
									Group grp = HttpQueue.grpResLst.get(baseAct.me_v.editIdx);
				            		JSONArray array = new JSONArray();
				            		for(Member mem : HttpQueue.grpSelMemberLst)
				            		{
				            			if(!grp.memberList.contains(mem)){
				            				array.put(mem.name);
				            			}
				            		}
				            		if(array.length() > 0){
				            			url = Base.HTTP_GROUP_PATH+"/inviteUsersToGroup";
				            			obj = new JSONObject();
										try {									
											obj.put("appid", "appid");
											obj.put("groupName", grp.name);
											obj.put("users", array);
										} catch (JSONException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}					
										baseAct.httpQueueInstance.EnQueue(url, obj, 19);
				            		}
				            		else{
				            			Toast.makeText(baseAct, "用户已经在该群", Toast.LENGTH_SHORT).show();
				            		}
				            		GrpDelDlg.this.cancel();
								}
								else if(baseAct.searchUserMode == 3){//add user to temp group
									if(Base.me_v != null && Base.me_v.frndGridDlg!= null){
										for(Member mem : HttpQueue.grpSelMemberLst){					
											int lastIdx = 0;;
											if(!Base.me_v.frndGridDlg.adapter.gMemberList.contains(mem)){
												lastIdx = Base.me_v.frndGridDlg.adapter.gMemberList.size()-1;												
												Base.me_v.frndGridDlg.adapter.gMemberList.add(lastIdx, mem);
											}
										}
										Base.me_v.frndGridDlg.adapter.notifyDataSetChanged();
									}
								}
							}
						}
						else{
							Toast.makeText(baseAct, "没有选择任何用户", Toast.LENGTH_SHORT).show();
						}
					}
					GrpDelDlg.this.cancel();
				}
				else
					Toast.makeText(baseAct, "没有选择任何一项", Toast.LENGTH_SHORT).show();
			}  
        	
        });
	}
	
	// 初始化数据  
    private void initDate() {  
//        for (int i = 0; i < 15; i++) {  
//            list.add("data" + " " + i);  
//        }  
        
    }  
    // 刷新listview和TextView的显示  
    private void dataChanged() {  
        // 通知listView刷新  
    	grpLstAdapter.notifyDataSetChanged();  
        // TextView显示最新的选中数目  
//        tv_show.setText("已选中" + checkNum + "项");
    	commite_btn.setText("确定("+checkNum+")");
    } 

    
    public class GrpLstSelAdapter extends BaseAdapter {  
        // 填充数据的list  
        private ArrayList<?> list;  
        // 用来控制CheckBox的选中状况  
        private HashMap<Integer, Boolean> isSelected;  
        // 上下文  
        private Context context;  
        // 用来导入布局  
        private LayoutInflater inflater = null;  
      
        // 构造器  
        public GrpLstSelAdapter(ArrayList<?> list, Context context) {  
            this.context = context;  
            this.list = list;  
            inflater = LayoutInflater.from(context);  
            isSelected = new HashMap<Integer, Boolean>();  
            // 初始化数据  
            initData();  
        }  
      
        // 初始化isSelected的数据  
        private void initData() {  
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
            ViewHolder holder = null;  
            if (convertView == null) {  
                // 获得ViewHolder对象  
                holder = new ViewHolder();  
                // 导入布局并赋值给convertview  
                convertView = inflater.inflate(R.layout.group_del_item, null);  
                holder.tv = (TextView) convertView.findViewById(R.id.group_del_item_name);  
                holder.cb = (CheckBox) convertView.findViewById(R.id.group_del_item_select);  
                // 为view设置标签  
                convertView.setTag(holder);  
            } else {  
                // 取出holder  
                holder = (ViewHolder) convertView.getTag();  
            }  
            // 设置list中TextView的显示  
            if(mOption == 0 || mOption == 1){
	            Group grp = (Group) list.get(position);
	            holder.tv.setText(grp.name);  
	            holder.cb.setChecked(getIsSelected().get(position));  
            }
            else if(mOption == 2){
            	Member  mem = (Member) list.get(position);
            	holder.tv.setText(mem.name); 
            	holder.cb.setChecked(getIsSelected().get(position));  
            }
            else if(mOption == 4){
            	holder.cb.setVisibility(View.GONE);
            	Group grp = (Group) list.get(position);
	            holder.tv.setText(grp.name);  
            }
            // 根据isSelected来设置checkbox的选中状况  
            
            return convertView;  
        }  
      
        public HashMap<Integer, Boolean> getIsSelected() {  
            return isSelected;  
        }  
      
        public void setIsSelected(HashMap<Integer, Boolean> isSelected) {  
        	this.isSelected = isSelected;  
        }  
      
        public class ViewHolder {  
            TextView tv;  
            CheckBox cb;  
        }  
    }

}
