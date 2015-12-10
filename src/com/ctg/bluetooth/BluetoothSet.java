/******************************************************/
/* Copyright (C) 2014 The Android Project             */
/* All rights is reserved by Harman CTG Shanghai      */
/* First Version is delivered  by Zhiming.hu          */
/* Data: 2014-02-25                                   */
/* Change history:                                    */
/* Modifier:                                          */
/* Data:                                              */
/******************************************************/

package com.ctg.bluetooth;


import com.ctg.crash.LogRecord;
import com.ctg.obdii.OBDcmd;
import com.ctg.ui.Base;
import com.ctg.ui.R;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;


public class BluetoothSet {

	// Debugging
    private static String TAG = "BluetoothSet";
    private static boolean D = false;
	
	//Bluetooth service state
    private static final int STATE_ON = 12;				
    private static final int STATE_OFF = 10;			
    private static final int STATE_TURING_ON = 11;		
    private static final int STATE_TURING_OFF = 13;		
	
    // Message types sent from the BluetoothService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    
    public static final int SEND_TIMEOUT = 15; 
    
    // Key names received from the BluetoothService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";
    
    // Receive OBD data from EST327
    public static String returnData = "";
    
    // Name of the connected device
    public String mConnectedDeviceName = null;    
	private BluetoothService mBtService;
	private BluetoothAdapter mBluetoothAdapter = null;
	private SendOBDCmd mSendOBDCmd = null;
	//private ProgressDialog mDialog = null;
	private Context mContext;
	private final Handler dataHandler;
	
	private static boolean isBusy = false;
	
	public BluetoothSet(Handler datahandler)
	{
		//mContext = context;
		//mTitle = mTitView;
		dataHandler = datahandler;  //this handler is for poping up data the the high level application message queue;
		isBusy = false;
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if(D) Log.d(TAG, "Bluetooth set:onCreate bluetooth");
	}
	
	
	public Boolean isSupported(){
		if (mBluetoothAdapter == null)
			return false;		
		else		
			return true;
	}
	
	public Boolean isExist(){
		if(mBtService == null)
			return false;
		else
			return true;
	}
	

	public Boolean isConnected(){
		if( mBtService != null ){
			if(mBtService.getState() == BluetoothService.STATE_CONNECTED){
				return true;
			}
			else{
				return false;
			}
		}
		else
			return false;
	}
	
	public int ConnectionState(){
		if(mBtService != null)
			return mBtService.getState();
		else 
			return 0;
	}

	public Boolean isRegistered(){
		if (mBtService != null)
			return true;
		else
			return false;					
	}
	

	public synchronized static boolean getIsBusy(){
		return isBusy;
	}
	

	public synchronized static void setIsBusy(boolean status){
		isBusy = status;
	}
	
	

	public void openBluetooth(){
		if(mBluetoothAdapter == null)
			return;
		if (!mBluetoothAdapter.isEnabled()){
						
		    //mBluetoothAdapter.enable();	
		    count = 0;
			new OpenBluetoothThread().start();
			if(D) Log.e(TAG, "Bluetooth set:Open bluetooth thread");
		}
		else
		{
			registerService();
			if(D) Log.d(TAG, "Bluetooth set:register service");
		}
	}
	

	public void stopBTService(){
		if (mBtService != null) mBtService.stop();
		if(D) Log.d(TAG, "Bluetooth set:--- ON STOP ---");
	}
	

	public void startBTService(){
		//if(D) Log.d(TAG, "Bluetooth set:--- ON START ---");
		if (mBtService != null) {			
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mBtService.getState() == BluetoothService.STATE_NONE) {
              // Start the Bluetooth chat services
            	mBtService.start();
            }
        }
		else
		{
			if(D) Log.d(TAG, "Bluetooth set:can't start BT service!");
		}
	}
	

	public int sendMessage(String message, int token){
		if(mBtService == null) 
			return 0;
		// Check that we're actually connected before trying anything
        if (mBtService.getState() != BluetoothService.STATE_CONNECTED) {
            //Toast.makeText(mContext, "not_connected", Toast.LENGTH_SHORT).show();
            if(D) Log.d(TAG, "Bluetooth set:State is not STATE_CONNECTED!");
            return 0;
        }

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
        	//String outString = ShareFunctional.bytesToHexString(message.getBytes());
        	//wait for receive data over.
        	
        	byte[] send = message.getBytes(); 
        	//add for debug:
        	//System.out.println(message);
            mBtService.write(send,token);
            setIsBusy(true);
            

        	TimeoutThread mTimeoutThread = new TimeoutThread();
        	mTimeoutThread.start();    	         
        	if(D) Log.d(TAG, "Bluetooth set:Send messgae length is normal!");
            return 1;
        }else{
        	//Toast.makeText(mContext, "message lenthg", Toast.LENGTH_SHORT).show();
        	if(D) Log.d(TAG, "Bluetooth set:Send messgae length is zero!");
        	return 0;
        }
	}
	
    //####add for reading return data	
	public synchronized void setReturnData(String rData, int token){
		String localtoken = String.valueOf(token);		
		returnData = rData;
		returnData = returnData + " " + localtoken + "\r\n";
	}
	
	public synchronized String getReturnData(){
		return returnData;
	}	
	//####

	private void registerService(){
		if (mBtService == null){
			mBtService = new BluetoothService(mHandler, dataHandler);
			if(D) Log.d(TAG, "Bluetooth set:new bluetooth service!");
		}
	}
	

	private final Handler mHandler = new Handler(){			
		@Override
		public void handleMessage(Message msg) {
			// Auto-generated method stub
			switch (msg.what) {		
			case MESSAGE_STATE_CHANGE:
                if(D) Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                switch (msg.arg1) {
                case BluetoothService.STATE_CONNECTED:
                	if(D) Log.d(TAG, "Bluetooth set:Title connected!");
                    break;
                case BluetoothService.STATE_CONNECTING:
                	if(D) Log.d(TAG, "Bluetooth set:Title is connecting!");
                    break;
                case BluetoothService.STATE_LISTEN:
                case BluetoothService.STATE_NONE:
                	if(D) Log.d(TAG, "Bluetooth set:Title not connected!");
                    break;
                }
                break;
            case MESSAGE_DEVICE_NAME:
                // save the connected device's name
            	mConnectedDeviceName =  msg.getData().getString(DEVICE_NAME);
                break;
            case MESSAGE_TOAST:
            	//Modified on 2014-05-07
                //Toast.makeText(mContext, msg.getData().getString(TOAST),
                               //Toast.LENGTH_SHORT).show();
                break;
			case STATE_ON:				
				registerService();
				if(D) Log.d(TAG, "Bluetooth set:registerService on STATE_ON");
				break;
			case STATE_OFF:
				
				break;
			case STATE_TURING_ON:
				
				break;
			case STATE_TURING_OFF:
				
				break;
			case SEND_TIMEOUT:

			default:
				break;
			}
		}		
	};	
	

	public void ConnectDevices(final String address){
		if(address == null || mBluetoothAdapter == null){
			LogRecord.SaveLogInfo2File(Base.BTlog,"mac address is null!");
			//LogRecord.UploadLogFiles(Base.ycblog,BluetoothService.testURL);
        	if(D) Log.d(TAG, " mac address is null!");
        	return;
        }  			
		// Get the BLuetoothDevice object
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if(device == null){
        	LogRecord.SaveLogInfo2File(Base.BTlog,"BT device is null!");
        	//LogRecord.UploadLogFiles(Base.ycblog,BluetoothService.testURL);
        	if(D) Log.d(TAG, "BT device is null!");
        	return;
        }        	
        // Attempt to connect to the device
        if(mBtService == null){
        	if(D) Log.d(TAG, "mBTservice is not created and is null!");
        	if(BluetoothAdapter.getDefaultAdapter().getState() == STATE_ON)
        		registerService();
        	else
        		return;
        }
        //add for protecting evading invoking many times: by Hu Zhiming 2014-06-04
        if(mBtService.getState() == 2 || mBtService.getState() == 3)
        {
        	LogRecord.SaveLogInfo2File(Base.BTlog,"BT status is 2 or 3!");
        	//LogRecord.UploadLogFiles(Base.ycblog,BluetoothService.testURL);
        	return;
        }        
        //
        mBtService.connect(device);
        if(D) Log.d(TAG, "Bluetooth set:connect device after getting device address");
	}
	

	static int count;
	
	private class OpenBluetoothThread extends Thread{

		@Override
		public void run() {
			if(D) Log.i("THREAD", "Bluetooth set:open bluetooth thread");
			while(mBluetoothAdapter != null && mBluetoothAdapter.getState() != BluetoothAdapter.STATE_ON){
				try {
					sleep(50L);					
					Message msg = new Message();
					msg.what = mBluetoothAdapter.getState();
					if(mHandler != null)
						mHandler.sendMessage(msg);
				} catch (InterruptedException e) {
					// handle exception
					e.printStackTrace();
					//mDialog.dismiss();
				}
			}
		}
	}
	

	private class TimeoutThread extends Thread{	
		public boolean isStop;
		
		public TimeoutThread(){
			isStop = false;
		}
		
		@Override
		public void run() {
			// Auto-generated method stub
			int i = 0;
			while(!isStop){
				try{					
					if (!getIsBusy()) break;
					
					sleep(20L);
					i++;
					if (i == 100){						
						if (getIsBusy() && mHandler != null){							
							Message msg = mHandler.obtainMessage(SEND_TIMEOUT);
							mHandler.sendMessage(msg);
							break;
						}		
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
					//  handle exception
				}				
			}			
		}
		
		public void cancel(){
			Log.d(TAG, "cancel ");
			isStop = true;
		}
	}
	
	
	private class SendOBDCmd extends Thread{
		String obdcmd = "";
		int ret = 0;
		int softtoken = 0;
		public void run(){	
			while(true){   		
				while((!OBDcmd.cmdqueue.isEmpty()) && (!getIsBusy()) &&(!OBDcmd.getQueueStatus()))
				{ 
    				try {
    					sleep(1000);
    				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
    					e.printStackTrace();
    				}
				    if(D) Log.d(TAG, "sendobdcmd thread!");
					obdcmd = OBDcmd.cmdqueue.poll();
					String strArray[] = obdcmd.split("-");
	        		System.out.println("----OBDcmd ----");
	        		System.out.print(strArray[0]);
	        		System.out.println("----softtoken--");
	        		System.out.print(strArray[1]);
	        		softtoken = Integer.valueOf(strArray[1]);
	        		ret = sendMessage(strArray[0],softtoken);
	        		try {
						sleep(20L);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}			
			}
		}
	}	
}