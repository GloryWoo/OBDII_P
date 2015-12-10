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

public class GrpListMemberSelDlg extends Dialog { 
	private static final String TAG = "CreateGrpDlg";
    Button cancelButton;		
	private Base baseAct;

	private static int default_width = 160; //
	private static int default_height = 120;//

	public GrpListMemberSelDlg(Context context, int layout, int style) {
		this(context, default_width, default_height, layout, style, null);
		
	}
	
	protected void onDestroy(){
	}
	
	public GrpListMemberSelDlg(Context context, int width, int height, int layout, int style, String content) {
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

	}
	


}
