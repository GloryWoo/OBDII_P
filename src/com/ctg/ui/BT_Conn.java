package com.ctg.ui;

import java.util.ArrayList;
import java.util.List;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ctg.bluetooth.BluetoothService;
import com.ctg.bluetooth.BluetoothSet;
import com.ctg.crash.LogRecord;

public class BT_Conn extends Thread{
	//public static final String MAC_ADDRESS_SUB = "/OBDII/mac_address";  
	//public String MAC_ADDRESS;
	private static final String TAG = "BT_Conn";
	public static final String NAME = "BT_Conn_Thread";
	public RelativeLayout relat_lay;
	public RelativeLayout relat_lay1;
	Button scanButton;
	Base mContext;
	private static final String TAG_LIST = "DeviceListView";
	private static final boolean D = true;
	final public static int OBD_DEVICE_FOUND = 0x200;
	final public static int OBD_DEVICE_NOT_FOUND = 0x201;
	final public static int BT_DEVICE_IN_DISCOVERY = 0x202;
	final private static long SECOND = 1000l;

	// Return Intent extra
	//public static String EXTRA_DEVICE_ADDRESS = "device_address";

	// Member fields
	public BluetoothAdapter mBtAdapter;
	private ArrayAdapter<String> mNewDevicesArrayAdapter;
	List<String> lstDevices = new ArrayList<String>();
	private static Boolean hasDevices;
	private ProgressDialog mpDialog = null;
	
	private BluetoothSet mBluetoothSet = null;
	TextView bt_conn_state = null;
	private boolean bt_connected;
	private static int counter = 0;
	//private String mac_addr_str = null;
	private boolean isExit = false;	// whether this thread should stop or not
	
	public static void setCounter(int num){
		counter = num;
	}
	
	private OnClickListener scanListener = new OnClickListener()
	{
		public void onClick(View v)
		{
//			mContext.bt_c.removeAllViews();
			lstDevices.clear();
			listDevice();
			//doDiscovery();
			//v.setVisibility(View.GONE);
		}
	};
	
	public BT_Conn(Context cont){
		//relat_lay = (RelativeLayout) View.inflate(cont, R.layout.activity_device_list, null);
		//relat_lay1 = (RelativeLayout) View.inflate(cont, R.layout.bt_conn, null);
		mContext = (Base) cont;	
		this.setName(NAME);
		BT_Init();
		//new Thread(this).start();
		// Initialize array adapters. One for already paired devices and
		// one for newly discovered devices								
	}
	
	public void BT_Init(){								
		mBtAdapter = BluetoothAdapter.getDefaultAdapter();
		
		// Register for broadcasts when a device is discovered
		IntentFilter found_filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		mContext.registerReceiver(mReceiver, found_filter);
	
		// Register for broadcasts when discovery has finished
		IntentFilter discovery_filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		mContext.registerReceiver(mReceiver, discovery_filter);	
		
		if(mBtAdapter == null)
			return;
		//mac_address_init();
//		if(!mBtAdapter.isEnabled()){
//			Base.OBDApp.ifBtOpenInitial = false;
//			mBtAdapter.enable();					
//			//listDevice();	
//		}
//		else
//			Base.OBDApp.ifBtOpenInitial = true;
	}
	
	public void Auto_BT_OnDestroy(){
		if (mBtAdapter != null)
		{
			mBtAdapter.cancelDiscovery();
		}

		// Unregister broadcast listeners		
		mContext.unregisterReceiver(mReceiver);
		mContext.setting_s.bt_s = null;
		
		this.cancel();
		//mContext.unregisterReceiver(mReceiverAuto);
	}
	
	void listDevice(){
		//mContext.bt_c.removeAllViews();
		//mContext.bt_c.addView(relat_lay);
		//scanButton = (Button) relat_lay.getChildAt(1);
		//scanButton.setOnClickListener(scanListener);
		
		/*mNewDevicesArrayAdapter = new ArrayAdapter<String>(mContext,
				R.layout.device_name,lstDevices);
		
		// Find and set up the ListView for newly discovered devices
		ListView newDevicesListView = (ListView) relat_lay.getChildAt(0);
		newDevicesListView.setAdapter(mNewDevicesArrayAdapter);
		newDevicesListView.setOnItemClickListener(mDeviceClickListener);*/	
		//mac_address_init();

		if (D) Log.d(TAG, "listDevice() register intent!");
		
		// Get the local Bluetooth adapter		
		if (mBtAdapter != null){
			doDiscovery();
			mContext.setting_s.handler.obtainMessage(BT_DEVICE_IN_DISCOVERY).sendToTarget();
			//mContext.tabHost.setCurrentTab(3);
		}
	}
	
	private void doDiscovery()
	{
		if (D) Log.d(TAG, "doDiscovery()");

		// Indicate scanning in the title
		//setProgressBarIndeterminateVisibility(true);

		// If we're already discovering, stop it
		if (mBtAdapter.isDiscovering())
		{
			mBtAdapter.cancelDiscovery();
		}
		hasDevices = false;
		//mBtAdapter.enable();
		// Request discover from BluetoothAdapter 
		mBtAdapter.startDiscovery();
		/*mpDialog = new ProgressDialog(mContext); 
		
        mpDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);//���÷��ΪԲ�ν����  
        mpDialog.setTitle("Remind");//���ñ���  
        mpDialog.setMessage("Scaning the bluetooth devices...");  
        mpDialog.setIndeterminate(false);//���ý�����Ƿ�Ϊ����ȷ  
        mpDialog.setCancelable(true);//���ý�����Ƿ���԰��˻ؼ�ȡ��  
        mpDialog.setButton("Stop", new DialogInterface.OnClickListener(){  

            @Override  
            public void onClick(DialogInterface dialog, int which) {  
                dialog.cancel();  
                if (mBtAdapter.isDiscovering()){
                	mBtAdapter.cancelDiscovery();
                }
            }  
        });  
        
        mpDialog.show(); */    
	}
	// The BroadcastReceiver that listens for discovered devices and
	// changes the title when discovery is finished
	private final BroadcastReceiver mReceiver = new BroadcastReceiver()
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			String action = intent.getAction();
			// When discovery finds a device
			if (BluetoothDevice.ACTION_FOUND.equals(action))
			{
				// Get the BluetoothDevice object from the Intent
				BluetoothDevice device = (BluetoothDevice)intent
						.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				if(device == null)
					return;
				String tempString;
				if(device.getBondState() == BluetoothDevice.BOND_NONE){
					tempString = "Status: UnPaired\n";
				}
				else {
					tempString = "Status: Paired\n";
				}
			
				//			
				tempString += device.getName() + "\n"
						+ device.getAddress();
				//
				if (lstDevices.indexOf(tempString) == -1){
					lstDevices.add(tempString);
					//mNewDevicesArrayAdapter.notifyDataSetChanged();
				}
				String str1 = device.getAddress();
				String str2 = mContext.setting_s.mac_addr_str;
				boolean stringEqual = false;
				int stat = BluetoothAdapter.getDefaultAdapter().getState();
				if(D) Log.d(TAG, "blue state =" + stat);
				if(str1 != null && str2 != null)
					stringEqual = str1.equals(str2);
				//|| device.getName().contains("Moto")|| device.getName().contains("小米")
				if(stringEqual 
				|| device.getName() != null && (device.getName().contains("OBD") || device.getName().contains("obd")||device.getName().contains("iEST327") 
				|| device.getName().contains("JBL") || device.getName().contains("iEST527"))){
					hasDevices = true;
//				if(device.getName() != null && device.getName().equals("HISH-P007")){	
					mBtAdapter.cancelDiscovery();
					//mpDialog.cancel();
					if(D) Log.d(TAG, "bluetooth connect start!");
					if(!stringEqual){
						mContext.setting_s.mac_addr_str = str1;
						mContext.setting_s.mac_address_save();
					}
					mContext.setting_s.obdDevice = device.getName();
//					if(mContext.localbinder != null && mContext.serviceConn)
//						mContext.localbinder.setObdName(mContext.setting_s.obdDevice);
					mContext.OBDApp.setOBDName(mContext.setting_s.obdDevice);
					mContext.setting_s.bluetooth_connect(); 
					if(D) Log.d(TAG, "bluetooth connect end!");
				}				
//				hasDevices = true;
				
			}
			else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action))
			{				
				if(lstDevices.size() == 0)
				{
					hasDevices = false;
				}
				if(!hasDevices)
					mContext.setting_s.handler.obtainMessage(OBD_DEVICE_NOT_FOUND).sendToTarget();
			}
		}
	};
			
	public void run(){
		while(!isExit){			
			if(mContext.localbinder != null 
					&& mContext.localbinder.getBTstate() != BluetoothService.STATE_CONNECTED){	// we are going to discover OBD devices
				if(mBtAdapter.getState() != BluetoothAdapter.STATE_ON){
					if (D) Log.d(TAG, "run: bluetooth status is not STATE ON!");
				}
				else {
					listDevice();
				}					
			}
			
			try {
				sleep(25*SECOND);	// auto discovery every 10 seconds
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		}
		
//        int i = 0;
//		while(mBtAdapter.getState() != BluetoothAdapter.STATE_ON && i < 100){//100
//			try {
//				if (D) Log.d(TAG, "run: bluetooth status is not STATE ON!");
//				sleep(100L);
//				i++;
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//		if (D) Log.d(TAG, " List BT device!");
//		if(i == 100)//100
//			return;
//		listDevice();

		if (D) Log.d(TAG, "exit auto discovery!");
	}
	
	public void cancel() {		
		isExit = true;
	}
	
}