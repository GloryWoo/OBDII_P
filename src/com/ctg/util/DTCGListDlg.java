package com.ctg.util;


import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.ctg.bluetooth.BluetoothSet;
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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import android.util.DisplayMetrics;

import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;

import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;


public class DTCGListDlg extends Dialog implements DialogInterface.OnCancelListener{ 
	private static final String TAG = "DTCGListDlg";
    Button cancelButton;		
	private Base baseAct;

	
	private static int default_width = 160; //
	private static int default_height = 120;//

	TextView title_v;
	TextView intepret_v;
	public LinearLayout line_v;
	ExpandableListView list_v;
//	TextView none_dtc;
//	RelativeLayout none_dtc_rela;
//	ArrayList<Map<String, Object>> listItem;
//	SimpleAdapter listItemAdapter;
	DtcExpAdapter expAdp;
	DTCsDetailDialog dtcDetail;
//	ScrollView scrollContainer;
//	ScrollView lastScrollView;
	LinearLayout lastLinear;
	ImageView updown;
	RelativeLayout updownRela;
	int lastPosition = -1;
	LinearLayout linearContainer;
//	public ArrayList<ImageView> bmLst;
	private static final int WC = ViewGroup.LayoutParams.WRAP_CONTENT;
	private static final int FP = ViewGroup.LayoutParams.MATCH_PARENT;
	public static LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(FP, FP);
	public RelativeLayout relativeLay;
	View dtc_back;
	boolean inited = false;
//	public DTCGListDlg(Context context, int layout, int style) {
//		this(context, default_width, default_height, style);
//		
//	}
	
	public void onCancel(DialogInterface dialog) {
		baseAct.fullScreenDlg = null;
	}
	
	public DTCGListDlg(Context context, int width, int height, int style) {
		super(context, style);
		//set content
		//setContentView(layout);
		baseAct = (Base)context;
		//mac_address_init();
		//set window params
//		setContentView(R.layout.dtcs_list);
		line_v = (LinearLayout) View.inflate(baseAct, R.layout.dtc_list_group, null);
		setContentView(line_v);
		

		list_v = (ExpandableListView) line_v.findViewById(R.id.dtc_listv_g);
		dtc_back = findViewById(R.id.dtc_back_g);
		dtc_back.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				DTCGListDlg.this.cancel();
			}
		});
		initDtcSroll();
		Window window = getWindow();
		WindowManager.LayoutParams params = window.getAttributes();
		//set width,height by density and gravity
		//float density = getDensity(context);	
		params.width = (int) (width);
		params.height = (int) (height);
		params.gravity = Gravity.TOP;
		//params.verticalMargin = 2.0F;
		window.setAttributes(params);
		
		setOnCancelListener(this);
	}


	
	public void initDtcSroll(){
		inited = true;
		list_v = (ExpandableListView) line_v.findViewById(R.id.dtc_listv);
		expAdp = new DtcExpAdapter(baseAct);
		
		list_v.setAdapter(expAdp);
		list_v.setDividerHeight(6*Base.mDensityInt);
		list_v.setOnItemClickListener(new OnItemClickListener() {  			  
            @Override  
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) { 
            			
            }  
		});
	}
	

}
