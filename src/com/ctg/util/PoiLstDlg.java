package com.ctg.util;


import java.util.ArrayList;
import java.util.Map;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.baidu.mapapi.search.geocode.GeoCodeOption;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.sug.SuggestionSearchOption;
import com.ctg.interf.OnBorderListener;
import com.ctg.ui.Base;
import com.ctg.ui.R;

public class PoiLstDlg extends Dialog implements DialogInterface.OnCancelListener, OnBorderListener{ 
	private static final String TAG = "CustomDialog";
//    Button cancelButton;		
	public Base baseAct;

	public ListView poiLst_v;
	public LinearLayout poiLinear;
	public PoiScrollView poiScroll;
	ImageView expand_v;
	public RelativeLayout dragdown_rela;
	public RelativeLayout dragup_rela;
	public ProgressBar dragdown_progress;
	public ProgressBar dragup_progress;
	public TextView dragdown_text;
	public TextView dragup_text;
	
	public PoiAdapter poiLstAdp;
//	public SimpleAdapter simpleAdp;
	public ArrayList<Map<String, Object>> poilistItem;
	
	private static int default_width = 160; //
	private static int default_height = 120;//
	public int curPage;
	public int totalPage;
	public int pageCapacity;

	
	private View.OnClickListener expandListen = new View.OnClickListener(){

		@Override
		public void onClick(View v)
		{
						
		}
		
	};
	public PoiLstDlg(Context context, int layout, int style) {
		this(context, default_width, default_height, layout, style);		
	}
	
	public void onCancel(DialogInterface dialog) {
		Base.baidu_v.poiLstDlg = null;
		Base.baidu_v.isSelectAddrMode = 0; 
	}	
	

	
	public PoiLstDlg(Context context, int width, int height, int layout, int style) {
		super(context, style);
		//set content
		setContentView(layout);
		
		//mac_address_init();
		//set window params
		Window window = getWindow();
		WindowManager.LayoutParams params = window.getAttributes();
		//set width,height by density and gravity
//		float density = getDensity(context);	
		params.width = width;//(int) (width*density);
		params.height = height;//(int) (height*density);
		params.gravity = Gravity.CENTER;
		//params.verticalMargin = 2.0F;
		window.setAttributes(params);
		baseAct = (Base)context;
		
		expand_v = (ImageView) findViewById(R.id.expand_icon);
		if(expand_v != null)
			expand_v.setOnClickListener(expandListen);

		poiLst_v = (ListView) findViewById(R.id.poi_list);
		poiLinear = (LinearLayout) findViewById(R.id.poi_linear);
		poiScroll = (PoiScrollView) findViewById(R.id.poi_scroll);
		if(poiScroll != null)
			poiScroll.setOnBorderListener(this);
		dragdown_text = (TextView)findViewById(R.id.drag_down_text);
		dragup_text = (TextView)findViewById(R.id.drag_up_text);
		dragdown_rela = (RelativeLayout) findViewById(R.id.dragdown_rela);
		dragup_rela = (RelativeLayout) findViewById(R.id.dragup_rela);
		dragdown_progress = (ProgressBar) findViewById(R.id.dragdown_progress);
		dragup_progress = (ProgressBar) findViewById(R.id.dragup_progress);
		
		poilistItem = new ArrayList<Map<String, Object>>();
		poiLstAdp = new PoiAdapter(baseAct,poilistItem, 
	        R.layout.poi_item,
	        new String[] {"image", "name", "address", "distance"},   
	        new int[] {R.id.poi_num,R.id.poi_title,R.id.poi_addr, R.id.poi_distance});
		poiLst_v.setAdapter(poiLstAdp);	
		poiLst_v.setDivider(null);		
		setOnCancelListener(this);
//		simpleAdp = new SimpleAdapter(baseAct,poilistItem,// 
//	            R.layout.poi_item,
//	            new String[] {"image", "name", "address"},   
//	            new int[] {R.id.poi_num,R.id.poi_title,R.id.poi_addr}  );		
//		poiLst_v.setAdapter(simpleAdp);	
	}

	@Override
	public void onBottom() {
		// TODO Auto-generated method stub
		if(Base.baidu_v.mPoiSearchCurPage < Base.baidu_v.mPoiSearchTotalPageNum-1){
			Base.baidu_v.mPoiSearch.searchInCity((new PoiCitySearchOption()) 
				    .city(Base.baidu_v.mCity)  
				    .keyword(Base.baidu_v.poiName)
				    .pageNum(Base.baidu_v.mPoiSearchCurPage+1)
				    );
    		dragup_rela.setVisibility(View.VISIBLE);
    		dragup_progress.setVisibility(View.VISIBLE); 
    		dragup_text.setText(R.string.dragup_notice1);
		}
	}

	@Override
	public void onTop() {
		// TODO Auto-generated method stub
		if(Base.baidu_v.mPoiSearchCurPage > 0){
			Base.baidu_v.mPoiSearch.searchInCity((new PoiCitySearchOption()) 
				    .city(Base.baidu_v.mCity)  
				    .keyword(Base.baidu_v.poiName)  
				    .pageNum(Base.baidu_v.mPoiSearchCurPage-1)
				    );
			if(Base.baidu_v.mPoiSearchCurPage == 0)
				dragdown_rela.setVisibility(View.GONE);
			else
				dragdown_rela.setVisibility(View.VISIBLE);
    		dragdown_progress.setVisibility(View.VISIBLE); 
    		dragdown_text.setText(R.string.dragdown_notice1);
		}
	}	
	

//	private float getDensity(Context context) {
//		Resources resources = context.getResources();
//		DisplayMetrics dm = resources.getDisplayMetrics();
//		return dm.density;
//	}

}
