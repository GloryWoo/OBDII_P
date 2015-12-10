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
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
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

import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.geocode.GeoCodeOption;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.sug.SuggestionSearchOption;
import com.baidu.navisdk.BNaviPoint;
import com.ctg.land.Cascade;
import com.ctg.land.FenceAdapt;
import com.ctg.service.CarDataService;
import com.ctg.ui.Base;
import com.ctg.ui.CityDownActivity;
import com.ctg.ui.R;


public class GeoFenceDlg extends Dialog implements DialogInterface.OnCancelListener, View.OnClickListener{ 
	private static final String TAG = "CustomDialog";
//    Button cancelButton;		
	public Base baseAct;
//	EditText poi_edit;
	public ListView fence_lv;	
	public ArrayList<String> fencelistItem;
	View geofence_add;
	View back_btn;
	public FenceAdapt fenceAdapt;
	
	private View.OnClickListener cancelListen = new View.OnClickListener(){

		@Override
		public void onClick(View v)
		{
			GeoFenceDlg.this.cancel();			
		}
		
	};

	public void onCancel(DialogInterface dialog) {
		Base.baidu_v.searchDlg = null;
	}	
	
	public GeoFenceDlg(Context context, int width, int height, int layout, int style) {
		super(context, style);
		//set content
		setContentView(layout);
		

		//set window params
		Window window = getWindow();
		WindowManager.LayoutParams params = window.getAttributes();

		params.width = width;
		params.height = height;
		params.gravity = Gravity.TOP;

		window.setAttributes(params);
		baseAct = (Base)context;
		geofence_add = findViewById(R.id.geofence_add_linear);
		fence_lv = (ListView) findViewById(R.id.geofence_lv);		
		back_btn = findViewById(R.id.fence_back);
		geofence_add.setOnClickListener(this);
		back_btn.setOnClickListener(this);
		if(CarDataService.fenceList != null){
			fenceAdapt = new FenceAdapt(context, CarDataService.fenceList);
			fence_lv.setAdapter(fenceAdapt);
		}
		fence_lv.setDivider(null);
		fence_lv.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
			
							
				MyGeoFenceCont myfence = CarDataService.fenceList.get(position);
//				if(baseAct.cascade != null)
//					baseAct.cascade.cas_l.setVisibility(View.INVISIBLE);
				if(Base.OBDApp.landScapeMode == 0){
					if(baseAct.navitDlg != null)
						baseAct.navitDlg.cancel();
					if(baseAct.fenceDlg != null)
						baseAct.fenceDlg.cancel();
				}


				Base.baidu_v.enterFenceEditMode(position);
			}
			
		});
		
	}
	

	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()){
		case R.id.geofence_add_linear:   
			
			Base.baidu_v.enterFenceAddMode();
			baseAct.navitDlg.hide();
			GeoFenceDlg.this.hide();
			break;

		case R.id.fence_back:
			GeoFenceDlg.this.cancel();
			break;
		default:break;
		}
		
	}
	
	

}
















