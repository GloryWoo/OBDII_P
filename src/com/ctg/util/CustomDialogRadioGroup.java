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
import android.util.Log;
import android.view.Gravity;
import android.view.View;

import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnLayoutChangeListener;

import android.widget.ArrayAdapter;
import android.widget.Button;

import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

import android.widget.Toast;


public class CustomDialogRadioGroup extends Dialog { 
	private static final String TAG = "CustomDialogRadioGroup";
    Button cancelButton;
	private BluetoothAdapter mBtAdapter;
	private ArrayAdapter<String> mNewDevicesArrayAdapter;
	private RadioGroup group;
	List<String> lstDevices = new ArrayList<String>();
	private static Boolean hasDevices;
	
	private Base mContext;
	BluetoothSet mBluetoothSet;
	
	private static int default_width = 160; //
	private static int default_height = 120;//
	Toast myToast;
	private int rdo_btn_id;
	
	//boolean BTAdapterThdRun = false;
	static int count = 0;
	
	DialogInterface.OnCancelListener onCancel = new DialogInterface.OnCancelListener(){
		public void onCancel(DialogInterface arg0) {
			Manual_BT_OnDestroy();		      
		}
	};
	
	Thread enableBTAdapterThd = new Thread(){
		public void run(){			
			while(mBtAdapter != null && mBtAdapter.getState() != BluetoothAdapter.STATE_ON){				
				try {
					Log.d("manual BT connect", "run: bluetooth status is not STATE ON!");
					sleep(50L);
					if(count++ == 1000){
						count = 0;
						break;
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			Log.d("manual BT connect", "count:"+count);
			if(mBtAdapter != null)
				mBtAdapter.startDiscovery();
			//BTAdapterThdRun = false;
		}
	};
	
	private android.widget.Button.OnClickListener cancelListen = new android.widget.Button.OnClickListener(){

		@Override
		public void onClick(View v)
		{
			CustomDialogRadioGroup.this.cancel();			
		}
		
	};
	public CustomDialogRadioGroup(Context context, int layout, int style) {
		this(context, default_width, default_height, layout, style);
		
	}
	
//	protected void onDestroy(){
//		Manual_BT_OnDestroy();
//	}
	
	public CustomDialogRadioGroup(Context context, int width, int height, int layout, int style) {
		super(context, style);
		//set content
		setContentView(layout);
		
		//mac_address_init();
		//set window params
		Window window = getWindow();
		WindowManager.LayoutParams params = window.getAttributes();
		//set width,height by density and gravity
		float density = getDensity(context);
		params.width = (int) (width*density);
		params.height = (int) (height*density);
		params.gravity = Gravity.TOP;
		//params.verticalMargin = 2.0F;
		window.setAttributes(params);
		mContext = (Base)context;
		mBtAdapter = BluetoothAdapter.getDefaultAdapter();
		listDevice();
		setOnCancelListener(onCancel);
	}
	
	private float getDensity(Context context) {
		Resources resources = context.getResources();
		DisplayMetrics dm = resources.getDisplayMetrics();
		return dm.density;
	}
	
	public void Manual_BT_OnDestroy(){
		if (mBtAdapter != null)
		{
			mBtAdapter.cancelDiscovery();
		}

		// Unregister broadcast listeners
		mContext.unregisterReceiver(mReceiver);
		//mContext.unregisterReceiver(mReceiverAuto);
	}
	
	void listDevice(){
		cancelButton = (Button) findViewById(R.id.button_cancel);
		cancelButton.setOnClickListener((android.widget.Button.OnClickListener) cancelListen);
		
		mNewDevicesArrayAdapter = new ArrayAdapter<String>(mContext,
				R.layout.device_name,lstDevices);
		
		// Find and set up the ListView for newly discovered devices
		//ListView newDevicesListView = (ListView) findViewById(R.id.new_devices);
		group = (RadioGroup) findViewById(R.id.device_group);
		group.setOnCheckedChangeListener(myGroupListen);
		//newDevicesListView.setAdapter(mNewDevicesArrayAdapter);
		//newDevicesListView.setOnItemClickListener(mDeviceClickListener);
	
		// Register for broadcasts when a device is discovered
		IntentFilter found_filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		mContext.registerReceiver(mReceiver, found_filter);
	
		// Register for broadcasts when discovery has finished
		IntentFilter discovery_filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		mContext.registerReceiver(mReceiver, discovery_filter);
		
		// Get the local Bluetooth adapter		
		if (mBtAdapter != null){
			doDiscovery();

		rdo_btn_id = 0x100;
			//mContext.tabHost.setCurrentTab(3);
		}
	}
	
	private void doDiscovery()
	{
		//if (D) Log.d(TAG, "doDiscovery()");

		// Indicate scanning in the title
		//setProgressBarIndeterminateVisibility(true);

		// If we're already discovering, stop it
		if (mBtAdapter.isDiscovering())
		{
			mBtAdapter.cancelDiscovery();
		}
		hasDevices = false;
		if(!mBtAdapter.isEnabled())
			mBtAdapter.enable();
		// Request discover from BluetoothAdapter 
		new Thread(enableBTAdapterThd).start();
		myToast = Toast.makeText(mContext, R.string.searching_bt, Toast.LENGTH_LONG);
		myToast.show();
		    
	}

	private OnCheckedChangeListener myGroupListen = new OnCheckedChangeListener() {    
	    
        @Override    
            public void onCheckedChanged(RadioGroup group, int checkedId) {    
            // ���ID�ж�ѡ��İ�ť    
                RadioButton btn = (RadioButton) findViewById(checkedId); 
                if (hasDevices){
    				String info = btn.getText().toString();
    				String address = lstDevices.get(checkedId-0x100);//info.substring(info.length() - 17);
    	
    				// Create the result Intent and include the MAC address
    				//Intent intent = new Intent();
    				//intent.putExtra(EXTRA_DEVICE_ADDRESS, address);

    				//mBluetoothSet = new BluetoothSet(mContext, bt_conn_state);
                    //mBluetoothSet.ConnectDevices(address); 
    				mContext.setting_s.obdDevice = info.split("\n")[1];
    				mContext.setting_s.mac_addr_str = address;
//					if(mContext.localbinder != null && mContext.serviceConn)
//						mContext.localbinder.setObdName(mContext.setting_s.obdDevice);
    				Base.OBDApp.setOBDName(mContext.setting_s.obdDevice);
    				myToast.cancel();
    				CustomDialogRadioGroup.this.cancel();
    				mContext.setting_s.bluetooth_connect();                
    				mContext.setting_s.mac_address_save();
                    //CustomDialog.this.cancel();
    				// Set result and finish this Activity
    			}
            }    
     };  

     private OnLayoutChangeListener layout_group_btn = new OnLayoutChangeListener(){

 		@Override
 		public void onLayoutChange(View v, int left, int top, int right,
 				int bottom, int oldLeft, int oldTop, int oldRight,
 				int oldBottom) {
 			// TODO Auto-generated method stub
 			int horizon_offset = 0;
 			int vtc_offset = 0;
 			RadioButton btn = (RadioButton)v;
 			
 			//((GroupView)v).
 			
 			//horizon_offset = 120;
 			//v.setLeft(left+horizon_offset);
 			v.setRight(group.getWidth());
 			
 			
 		}
     	
     };
	
	// The BroadcastReceiver that listens for discovered devices and
	// changes the title when discovery is finished
	private final BroadcastReceiver mReceiver = new BroadcastReceiver()
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			if(intent == null){
				Log.d(TAG, "intent null");
				return;
			}
			String action = intent.getAction();
			// When discovery finds a device
			if (BluetoothDevice.ACTION_FOUND.equals(action))
			{
				// Get the BluetoothDevice object from the Intent
				BluetoothDevice device = (BluetoothDevice)intent
						.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				String tempString;
				if(device.getBondState() == BluetoothDevice.BOND_NONE){
					tempString = "Status: UnPaired\n";
				}
				else {
					tempString = "Status: Paired\n";
				}
			
				//����豸				
				tempString += device.getName();// + "\n"+ device.getAddress();
				//��ֹ�ظ����
				if (lstDevices.indexOf(tempString) == -1){
					lstDevices.add(device.getAddress());
					RadioButton rdo_btn = (RadioButton) View.inflate(mContext, R.layout.device_radiobtn, null);
					rdo_btn.setId(rdo_btn_id);
					rdo_btn.setText(tempString);					
					rdo_btn.addOnLayoutChangeListener(layout_group_btn);
					rdo_btn_id++;
					group.addView(rdo_btn);
					//mNewDevicesArrayAdapter.notifyDataSetChanged();
				}
				/*if(device.getAddress().equalsIgnoreCase(mContext.setting_s.mac_addr_str) || device.getName().contains("OBD") || device.getName().contains("obd")){
					mBtAdapter.cancelDiscovery();
					//mpDialog.cancel();
					mContext.setting_s.obdDevice = device.getName();
					if(mContext.localbinder != null && mContext.serviceConn)
						mContext.localbinder.setObdName(mContext.setting_s.obdDevice);
					mContext.setting_s.bluetooth_connect(); 
    				myToast.cancel();
    				CustomDialogRadioGroup.this.cancel();
					//mContext.bt_c.removeAllViews();				
					//mContext.bt_c.addView(relat_lay1);
					//bt_conn_state = (TextView) ((ViewGroup) ((ViewGroup) relat_lay1.getChildAt(0)).getChildAt(1)).getChildAt(0);
					//scanButton = (Button) relat_lay1.getChildAt(1);
					//scanButton.setOnClickListener(scanListener);

				}*/
				//mNewDevicesArrayAdapter.add(device.getName() + "\n"
				//		+ device.getAddress());
				hasDevices = true;
				
			}
			else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action))
			{
				// When discovery is finished, change the Activity title
				//setProgressBarIndeterminateVisibility(false);
				
				/*if (mNewDevicesArrayAdapter.getCount() == 0)
				{
					String noDevices = context.getResources().getText(
							R.string.none_found).toString();
					mNewDevicesArrayAdapter.add(noDevices);
					hasDevices = false;					
				}*/
				//mpDialog.cancel();
			}
		}
	};


	

	
}
