package com.ctg.util;


import java.util.ArrayList;
import java.util.List;

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

import android.util.DisplayMetrics;

import android.view.Gravity;
import android.view.View;

import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class CustomDialog extends Dialog{ 
	private static final String TAG = "CustomDialog";
    Button cancelButton;		
	private Base mContext;

	
	private static int default_width = 160; //
	private static int default_height = 120;//

	
	public CustomDialog(Context context, int layout, int style) {
		this(context, default_width, default_height, layout, style);
		
	}
	
	protected void onDestroy(){
	}
	
	public CustomDialog(Context context, int width, int height, int x, int y, int layout, int style){
		super(context, style);
		setContentView(layout);
		

		Window window = getWindow();
		WindowManager.LayoutParams params = window.getAttributes();
		//set width,height by density and gravity	
		params.width = width;
		params.height = height;
		params.gravity = Gravity.TOP;
		params.x = x;		
		params.y = y;
		//params.verticalMargin = 2.0F;
		window.setAttributes(params);
		mContext = (Base)context;
	}
	
	public CustomDialog(Context context, int width, int height, View layout, int style) {
		super(context, style);
		//set content
		setContentView(layout);
		
		//mac_address_init();
		//set window params
		Window window = getWindow();
		WindowManager.LayoutParams params = window.getAttributes();
		//set width,height by density and gravity
		float density = getDensity(context);	
		if(width > 0)
			params.width = (int) (density*width);
		else 
			params.width = width;
		if(height > 0)
			params.height = (int) (density*height);
		else
			params.height = height;
		params.gravity = Gravity.CENTER;		
		//params.verticalMargin = 2.0F;
		window.setAttributes(params);
		mContext = (Base)context;
		setCancelable(true);
	}
	
	public CustomDialog(Context context, int width, int height, int layout, int style) {
		this(context, width, height, View.inflate(context, layout, null), style);
	}
	
	private float getDensity(Context context) {
		Resources resources = context.getResources();
		DisplayMetrics dm = resources.getDisplayMetrics();
		return dm.density;
	}


}
