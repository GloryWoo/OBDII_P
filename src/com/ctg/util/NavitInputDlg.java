package com.ctg.util;


import java.util.ArrayList;
import java.util.Map;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

import com.baidu.mapapi.search.geocode.GeoCodeOption;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.sug.SuggestionSearchOption;
import com.baidu.navisdk.BNaviPoint;
import com.ctg.crash.LogRecord;
import com.ctg.land.Cascade;
import com.ctg.ui.Base;
import com.ctg.ui.CityDownActivity;
import com.ctg.ui.R;


public class NavitInputDlg extends Dialog implements DialogInterface.OnCancelListener, View.OnClickListener{ 
	private static final String TAG = "NavitInputDlg";
//    Button cancelButton;		
	public Base baseAct;
//	EditText poi_edit;
	public ListView searchLst_v;
	Button searchBtn;
	View go_home_linear;
	View go_company_linear;
	TextView input_dest;
	TextView go_home;
	TextView go_company;
	ImageView back_v;
	ImageView edit_home;
	ImageView edit_company;
	View offLineV;
	View fenceV;
	public NavitPoint nvPtH;
	public NavitPoint nvPtC;
	AlertDialog note_dialog;
	TextView cityName;
	
	public ArrayList<String> poilistItem;
	
	private static int default_width = 160; //
	private static int default_height = 120;//

	
	private View.OnClickListener cancelListen = new View.OnClickListener(){

		@Override
		public void onClick(View v)
		{
			NavitInputDlg.this.cancel();			
		}
		
	};
	public NavitInputDlg(Context context, int layout, int style) {
		this(context, default_width, default_height, layout, style);		
	}
	
	public void onCancel(DialogInterface dialog) {
		Base.baidu_v.searchDlg = null;
	}	
	
	public NavitInputDlg(Context context, int width, int height, int layout, int style) {
		super(context, style);
		//set content
		setContentView(layout);
		
		//mac_address_init();
		//set window params
		Window window = getWindow();
		WindowManager.LayoutParams params = window.getAttributes();
		//set width,height by density and gravity
//		float density = getDensity(context);	
		params.width = width;
		params.height = height;
		params.gravity = Gravity.TOP;
		//params.verticalMargin = 2.0F;
		window.setAttributes(params);
		baseAct = (Base)context;
		
		back_v = (ImageView) findViewById(R.id.navit_back);
		back_v.setOnClickListener(cancelListen);
		go_home_linear = findViewById(R.id.go_home);
		go_company_linear = findViewById(R.id.go_company);
		if(!Preference.getInstance(Base.OBDApp).getLoginStat()){
			go_home_linear.setVisibility(View.GONE);
			go_company_linear.setVisibility(View.GONE);
		}
		input_dest = (TextView) findViewById(R.id.input_dest);
		go_home = (TextView) findViewById(R.id.go_home_t);
		go_company = (TextView) findViewById(R.id.go_company_t);
		edit_home = (ImageView) findViewById(R.id.home_edit_i);
		edit_company = (ImageView) findViewById(R.id.company_edit_i);
		offLineV = findViewById(R.id.offline_map);
		fenceV = findViewById(R.id.fence);
		
		cityName = (TextView) findViewById(R.id.city_name);
//		cityName.setText(Base.baidu_v.mCity);
				
		input_dest.setOnClickListener(this);
		go_home.setOnClickListener(this);
		go_company.setOnClickListener(this);
		edit_home.setOnClickListener(this);
		edit_company.setOnClickListener(this);
		offLineV.setOnClickListener(this);
		fenceV.setOnClickListener(this);
		
//		nvPtH = Preference.getInstance(context).getNaviPointHome();
//		nvPtC = Preference.getInstance(context).getNaviPointCmpy();
		
	}
	
	void createAlertDlg(final int type){
		String msg = "";
		if(type == 0){
			msg = "没有设置家地址，是否去设置？";
		}
		else{
			msg = "没有设置公司地址，是否去设置？";
		}
		note_dialog = new AlertDialog.Builder(baseAct).create();
		note_dialog.setMessage(msg);
		note_dialog.setButton(DialogInterface.BUTTON_NEGATIVE, baseAct.getResources().getString(R.string.str_return),
			new DialogInterface.OnClickListener(){

				@Override
				public void onClick(DialogInterface dialog,
						int which) {
					// TODO Auto-generated method stub
					note_dialog.cancel();
				}
	
		});
		note_dialog.setButton(DialogInterface.BUTTON_POSITIVE, baseAct.getResources().getString(R.string.string_confirm),
			new DialogInterface.OnClickListener(){

				@Override
				public void onClick(DialogInterface dialog,
						int which) {
					// TODO Auto-generated method stub
					if(type == 0)
						Base.baidu_v.isSelectAddrMode = 1;
					else
						Base.baidu_v.isSelectAddrMode = 2;
					note_dialog.cancel();
		        	Base.baidu_v.searchDlg = new SearchPoiDlg(baseAct, Base.mWidth,
		                    Base.mHeight, R.layout.search_poi,
		                    R.style.Theme_dialog);
		        	Base.baidu_v.searchDlg.show();
				}
	
		});
		note_dialog.show();	
			
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()){
		case R.id.input_dest:            
			LogRecord.SaveLogInfo2File(Base.OperateInfo, TAG + "input dest");
        	Base.baidu_v.searchDlg = new SearchPoiDlg(baseAct, Base.mWidth,
                    Base.mHeight, R.layout.search_poi,
                    R.style.Theme_dialog);
        	Base.baidu_v.searchDlg.show();
            InputMethodManager imm = (InputMethodManager) baseAct.getSystemService(Context.INPUT_METHOD_SERVICE); 
            // 接受软键盘输入的编辑文本或其它视图 
            imm.showSoftInput(Base.baidu_v.searchDlg.poi_edit,InputMethodManager.SHOW_FORCED); 
//        	NavitInputDlg.this.cancel();
			break;

		case R.id.go_home_t:
			LogRecord.SaveLogInfo2File(Base.OperateInfo, TAG + "go home");
			nvPtH = Preference.getInstance(baseAct).getNaviPointHome();
			if(!nvPtH.addr.equals("") && Math.abs(Base.baidu_v.mCurLongitude) > 0.1){
		        BNaviPoint startPoint = new BNaviPoint(Base.baidu_v.mCurLongitude,Base.baidu_v.mCurLatitude,
		        		Base.baidu_v.curPoiName, BNaviPoint.CoordinateType.BD09_MC);//WGS84
		        BNaviPoint endPoint = new BNaviPoint(nvPtH.lon,nvPtH.lat,
		        		nvPtH.addr, BNaviPoint.CoordinateType.BD09_MC);
		        
		        Cascade.startNavit(baseAct, startPoint, endPoint);
			}
			else{
				createAlertDlg(0);			
			}
			break;
		case R.id.go_company_t:
			LogRecord.SaveLogInfo2File(Base.OperateInfo, TAG + "go company");
			nvPtC = Preference.getInstance(baseAct).getNaviPointCmpy();
			if(!nvPtC.addr.equals("")&& Math.abs(Base.baidu_v.mCurLongitude) > 0.1){
		        BNaviPoint startPoint = new BNaviPoint(Base.baidu_v.mCurLongitude,Base.baidu_v.mCurLatitude,
		        		Base.baidu_v.curPoiName, BNaviPoint.CoordinateType.BD09_MC);//WGS84
		        BNaviPoint endPoint = new BNaviPoint(nvPtC.lon,nvPtC.lat,
		        		nvPtC.addr, BNaviPoint.CoordinateType.BD09_MC);
		        
		        Cascade.startNavit(baseAct, startPoint, endPoint);
			}
			else{
				createAlertDlg(1);
			}
			break;			
		case R.id.home_edit_i:
			LogRecord.SaveLogInfo2File(Base.OperateInfo, TAG + "edit home");
			nvPtH = Preference.getInstance(baseAct).getNaviPointHome();
			String searchTextH = "";
			if(nvPtH != null)
				searchTextH = nvPtH.addr;
        	Base.baidu_v.searchDlg = new SearchPoiDlg(baseAct, Base.mWidth,
                    Base.mHeight, R.layout.search_poi,
                    R.style.Theme_dialog, searchTextH);
        	Base.baidu_v.searchDlg.show();
        	Base.baidu_v.isSelectAddrMode = 1;
			break;
		case R.id.company_edit_i:
			LogRecord.SaveLogInfo2File(Base.OperateInfo, TAG + "edit company");
			nvPtC = Preference.getInstance(baseAct).getNaviPointCmpy();
			String searchTextC = "";
			if(nvPtC != null)
				searchTextC = nvPtC.addr;
        	Base.baidu_v.searchDlg = new SearchPoiDlg(baseAct, Base.mWidth,
                    Base.mHeight, R.layout.search_poi,
                    R.style.Theme_dialog, searchTextC);
        	Base.baidu_v.searchDlg.show();
        	Base.baidu_v.isSelectAddrMode = 2;
			break;
		case R.id.offline_map:
			LogRecord.SaveLogInfo2File(Base.OperateInfo, TAG + "offline map");
			Intent intent_cityDown = new Intent(baseAct,
					CityDownActivity.class);
			intent_cityDown
					.putExtra("curCity", Base.baidu_v.mCity);
			baseAct.startActivity(intent_cityDown);
//			NavitInputDlg.this.cancel();
			break;
		case R.id.fence:
//			NavitInputDlg.this.cancel();
			LogRecord.SaveLogInfo2File(Base.OperateInfo, TAG + "fence");
			if(baseAct.login_state_check()){
				baseAct.fenceDlg = new GeoFenceDlg(baseAct, Base.mWidth,
	                    Base.mHeight, R.layout.geo_fence,
	                    R.style.Theme_dialog);
				baseAct.fenceDlg.show();
			}
			break;			
		default:break;
		}
		
	}
	
	

}
















