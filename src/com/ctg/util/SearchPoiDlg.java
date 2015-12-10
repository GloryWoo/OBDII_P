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
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

import com.baidu.mapapi.search.geocode.GeoCodeOption;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.sug.SuggestionSearchOption;
import com.ctg.ui.Base;
import com.ctg.ui.R;

public class SearchPoiDlg extends Dialog implements DialogInterface.OnCancelListener{ 
	private static final String TAG = "CustomDialog";
//    Button cancelButton;		
	public Base baseAct;
	public EditText poi_edit;
	public ListView searchLst_v;
	Button searchBtn;
	ImageView back_v;
	ImageView keybd_v;
	boolean poiEditTextFromSuggest;
	
	public MyAdapter poiLstAdp;
	public ArrayList<String> poilistItem;
	
	private static int default_width = 160; //
	private static int default_height = 120;//

	
	private View.OnClickListener cancelListen = new View.OnClickListener(){

		@Override
		public void onClick(View v)
		{
			SearchPoiDlg.this.cancel();			
		}
		
	};
	public SearchPoiDlg(Context context, int layout, int style) {
		this(context, default_width, default_height, layout, style);		
	}
	
	public void onCancel(DialogInterface dialog) {
		Base.baidu_v.searchDlg = null;
	}	
	
	public SearchPoiDlg(Context context, int width, int height, int layout, int style, String searchText)
	{
		this(context, width, height, layout, style);
		if(searchText != null)
			poi_edit.setText(searchText);
	}
	
	public SearchPoiDlg(Context context, int width, int height, int layout, int style) {
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
		
		back_v = (ImageView) findViewById(R.id.search_back_icon);
		back_v.setOnClickListener(cancelListen);
		poi_edit = (EditText) findViewById(R.id.search_s_text);
		searchBtn = (Button) findViewById(R.id.search_s_btn);
		searchLst_v = (ListView) findViewById(R.id.search_poi_list);
		poi_edit.addTextChangedListener(search_watcher);
		
		
		searchBtn.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if(Base.CheckNetwork(baseAct)){
					String searchText = poi_edit.getText().toString();
					if(searchText != null && !searchText.equals(""))
						Base.baidu_v.mPoiSearch.searchInCity((new PoiCitySearchOption())  
							    .city(Base.baidu_v.mCity)  
							    .keyword(searchText) 
							    .pageNum(0));
					Base.baidu_v.poiName = searchText;
					return;
				}
				else{
					Toast.makeText(baseAct, "没有网络连接，无法搜索地址", Toast.LENGTH_SHORT).show();
				}
			}
			
		});
		poilistItem = new ArrayList<String>();
		poiLstAdp = new MyAdapter(baseAct,poilistItem,R.layout.search_poi_item);
		searchLst_v.setAdapter(poiLstAdp);
		searchLst_v.setDivider(Base.gray_line_draw);
		searchLst_v.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				if(Base.CheckNetwork(baseAct)){
					TextView addr = (TextView) arg1.findViewById(R.id.search_poi_text);
					String addStr = addr.getText().toString();
					poiEditTextFromSuggest = true;
					poi_edit.setText(addStr);					
					Base.baidu_v.mPoiSearch.searchInCity((new PoiCitySearchOption()) 
						    .city(Base.baidu_v.mCity)  
						    .keyword(addStr)  
						    .pageNum(0)
						    );
					Base.baidu_v.poiName = addStr;
				}
				else{
					Toast.makeText(baseAct, "没有网络连接，无法搜索地址", Toast.LENGTH_SHORT).show();
				}
			}
		});
		

	}	
	
	private TextWatcher search_watcher = new TextWatcher() {
	    
	    @Override
	    public void onTextChanged(CharSequence s, int start, int before, int count) {
	        // TODO Auto-generated method stub
	    	if(poiEditTextFromSuggest){
	    		poiEditTextFromSuggest = false;
	    		return;
	    	}
	    	String searchText = poi_edit.getText().toString();
			if(searchText != null && !searchText.equals("") && Base.CheckNetwork(baseAct)){
				Base.baidu_v.mSuggestionSearch.requestSuggestion((new SuggestionSearchOption())  
					    .keyword(searchText)  
					    .city(Base.baidu_v.mCity));
			}
	    }
	    
	    @Override
	    public void beforeTextChanged(CharSequence s, int start, int count,
	            int after) {
	        // TODO Auto-generated method stub
	        
	    }

		@Override
		public void afterTextChanged(Editable s) {
			// TODO Auto-generated method stub
			
		}

	};
	private float getDensity(Context context) {
		Resources resources = context.getResources();
		DisplayMetrics dm = resources.getDisplayMetrics();
		return dm.density;
	}

}
