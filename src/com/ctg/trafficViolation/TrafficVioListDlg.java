package com.ctg.trafficViolation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.ctg.crash.LogRecord;
import com.ctg.ui.Base;
import com.ctg.ui.R;
import com.ctg.util.DTCsDetailDialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class TrafficVioListDlg extends Dialog implements DialogInterface.OnCancelListener, View.OnClickListener{
	private static final String TAG = "TrafficVioListDlg";
    Button cancelButton;		
	private Base mContext;

	TextView title;
	ListView list_v;
	ImageView list_back;
	ArrayList<Map<String, Object>> listItem;
	SimpleAdapter listItemAdapter;
	String mJSONStr;
	private static int default_width = 720; //
	private static int default_height = 1280;//
	ImageView exceedspeed;
	ImageView vioStop;
	ImageView vioLine;
	
	protected void onDestroy(){
	}
	
	public TrafficVioListDlg(Context context, int layout, int style) {
		this(context, default_width, default_height, layout, style, null);
		
	}
	
	
	public TrafficVioListDlg(Context context, int width, int height, int layout, int style, String content) {
		super(context, style);
		//set content
		setContentView(layout);
		
		//mac_address_init();
		//set window params
		Window window = getWindow();
		WindowManager.LayoutParams params = window.getAttributes();
		//set width,height by density and gravity
		float density = 1;//getDensity(context);	
		params.width = (int) (width*density);
		params.height = (int) (height*density);
		params.gravity = Gravity.TOP;
		//params.verticalMargin = 2.0F;
		window.setAttributes(params);
		list_v = (ListView) findViewById(R.id.wz_list);
		title = (TextView) findViewById(R.id.wz_list_title_cont);
		exceedspeed = (ImageView) findViewById(R.id.chaosu_img);
		vioStop = (ImageView) findViewById(R.id.weiting_img);
		vioLine = (ImageView) findViewById(R.id.xianxing_img);
		mContext = (Base)context;
		if(mContext.car_v.wzQueryDlg != null)
			title.setText(mContext.queryLicence);
		list_v.setDivider(Base.gray_line_draw);
		list_back = (ImageView)findViewById(R.id.wz_list_back);
		list_back.setOnClickListener(new android.view.View.OnClickListener(){
			public void onClick(View v) {
				TrafficVioListDlg.this.dismiss();
				mContext.car_v.wzListDlg = null;
			}
		});
		mJSONStr = content;
		initListView();

	}
	
	public void initListView(){
		listItem = new ArrayList<Map<String, Object>>();  
		
		JSONObject jsObj;
		try {
			//jsObj = new JSONObject(mJSONStr);		
			//JSONArray jsArr = new JSONArray(jsObj.getString("historys"));
			JSONArray jsArr = new JSONArray(mJSONStr);
			int wzIdx, wzCount = jsArr.length();
			for(wzIdx = 0; wzIdx < wzCount; wzIdx++){			
				JSONObject jsObjWz = jsArr.getJSONObject(wzIdx);			
				Map<String, Object> map = new HashMap<String, Object>();
//				 "addr": "龙山店立交桥-测速卡口",
//			        "date": "2014-08-23 16:43:00",
//			        "handled": "N",
//			        "money": "50",
//			        "reason": "其他车高速公路外超速50%以上不足70%",
//			        "fen": "6"
				map.put("content", jsObjWz.getString("reason"));  
				map.put("address", jsObjWz.getString("addr")); 
				String proc_stat = jsObjWz.getString("handled");
				if(proc_stat.equals("N"))
					map.put("proc_stat",  mContext.getResources().getString(R.string.wz_proc_n));
				else
					map.put("proc_stat",  mContext.getResources().getString(R.string.wz_proc_y));
				String date = jsObjWz.getString("date");
				//map.put("date", date.split(" ")[0]);
				map.put("date", date);
				//map.put("time", date);
				map.put("fen", ""+jsObjWz.getString("fen"));
				map.put("money", ""+jsObjWz.getString("money"));
				map.put("cityname", jsObjWz.getString("city"));
				listItem.add(map);
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		listItemAdapter = new SimpleAdapter(mContext,listItem,// 
	            R.layout.trafficvio_list_item,
	            new String[] {"content", "address", "proc_stat", "date"},   
	            new int[] {R.id.wz_item_content,R.id.wz_item_address,R.id.wz_item_proc_stat,R.id.wz_item_time}  
	        ); 
		list_v.setAdapter(listItemAdapter); 
		list_v.setOnItemClickListener(new OnItemClickListener() {  			  
            @Override  
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {  
            	
            	LogRecord.SaveLogInfo2File(Base.OperateInfo, TAG+"item click");
            	Map<String, Object> mapItm = listItem.get(arg2);
            
            	mapItm.put("licence", mContext.queryLicence);            	
//    			mContext.setting_s.wzDetailDlg = new WeiZhDetailDlg(mContext, mContext.mWidth, mContext.mHeight, R.layout.weizhang_detail, R.style.Theme_dialog, mapItm);
//    			mContext.setting_s.wzDetailDlg.show();	
            	Intent i = new Intent(mContext, TrafficVioDetailActivity.class);  //自定义打开的界面            
	        	i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);  	        	
	        	mContext.OBDApp.wzMapItem = mapItm;	        	        	
	        	mContext.startActivity(i);    

            }  
		});
		listItemAdapter.notifyDataSetChanged();
	}

	@Override
	public void onCancel(DialogInterface dialog) {
		// TODO Auto-generated method stub
		mContext.car_v.wzListDlg = null;
	}
	
	void setFocusImage(int imgId){
		switch(imgId){
		case R.id.chaosu_img:
		
			break;
		case R.id.weiting_img:
			break;
		case R.id.xianxing_img:
			break;
			
			default:break;
		}
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		setFocusImage(v.getId());
	}

}
