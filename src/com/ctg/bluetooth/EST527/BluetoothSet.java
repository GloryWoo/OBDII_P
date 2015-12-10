package com.ctg.bluetooth.EST527;

import com.ctg.service.CarDataService;
import com.ctg.service.getDataThread;
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
    private static final String TAG = "BluetoothSet";
    private static final boolean D = true;
	
	//蓝牙状态定义
    private static final int STATE_ON = 12;				//已打开
    private static final int STATE_OFF = 10;			//已关闭
    private static final int STATE_TURING_ON = 11;		//正在打开
    private static final int STATE_TURING_OFF = 13;		//正在关闭
	
    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    
    public static final int SEND_TIMEOUT = 15; 
    
    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";
    
    // Name of the connected device
    private String mConnectedDeviceName = null;    
	private BluetoothService mBtService;
	private BluetoothAdapter mBluetoothAdapter = null;
//	private ProgressDialog mDialog = null;
	private Context mContext;
//	private TextView mTitle;
	
	private boolean isBusy;
	private int lastBTState = -1;
	
	public BluetoothSet(Context context){
		mContext = context;
//		mTitle = mTitView;
		isBusy = false;
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	}
	
	/**
	 * 判断是否支持本地蓝牙设备
	 * @return
	 */
	public Boolean isSupported(){
		if (mBluetoothAdapter == null)
			return false;		
		else		
			return true;
	}
	
	/**
	 * 判断蓝牙是否已连接，即STATE为STATE_CONNECTED
	 * @return
	 */
	public Boolean isConnected(){
		if(mBtService.getState() == BluetoothService.STATE_CONNECTED){
			return true;
		}
		else{
			return false;
		}
	}
	
	/**
	 * 判断蓝牙服务已注册
	 * @return
	 */
	public Boolean isRegistered(){
		if (mBtService != null)
			return true;
		else
			return false;					
	}
	
	/**
	 * 获取串口数据状态
	 * @return  isbusy    true:忙碌/ false:空闲
	 */
	public synchronized boolean getIsBusy(){
		return isBusy;
	}
	
	/**
	 * 设置串口数据状态
	 * @param status：false(空闲)/true(忙碌)
	 */
	public synchronized void setIsBusy(boolean status){
		this.isBusy = status;
	}
	
	
	/**
	 * 打开本地蓝牙设备
	 */
	public void openBluetooth(){
		if (!mBluetoothAdapter.isEnabled()){
						
//			mDialog = new ProgressDialog(mContext);
//			mDialog.setMessage("Opening the bluetooth...");
//			mDialog.setCancelable(false); 
//			
//			mDialog.show();
			Base.OBDApp.ifBtOpenInitial = false;
			//mBluetoothAdapter.enable();					   
			new OpenBluetoothThread().start();
		}
		else
		{
			Base.OBDApp.ifBtOpenInitial = true;	
			//注册服务
			registerService();
		}
	}
	
	/**
	 * 停止蓝牙服务
	 */
	public void stopBTService(){
		if (mBtService != null) mBtService.stop();
		if(D) Log.e(TAG, "--- ON STOP ---");
	}
	
	/**
	 * 启动蓝牙服务
	 */
	public void startBTService(){
		if(D) Log.e(TAG, "--- ON START ---");
		if (mBtService != null) {			
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mBtService.getState() == BluetoothService.STATE_NONE) {
              // Start the Bluetooth chat services
            	mBtService.start();
            }
        }
		else
		{
			Log.i(TAG, "Bluetooth service not register!");
		}
	}
	
	/**
	 * 向蓝牙窗口发送数据
	 * @param message:数据
	 */
	public int sendMessage(String message){
		// Check that we're actually connected before trying anything
        if (mBtService.getState() != BluetoothService.STATE_CONNECTED) {
            Toast.makeText(mContext, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return 0;
        }

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
        	//String outString = ShareFunctional.bytesToHexString(message.getBytes());
        	//wait for receive data over.
        	
        	byte[] send = message.getBytes();            
            mBtService.write(send);
            setIsBusy(true);
            
            //开启等待
        	TimeoutThread mTimeoutThread = new TimeoutThread();
        	mTimeoutThread.start();    	         
             
            return 1;
        }else{
        	return 0;
        }
	}
	
	/**
	 * 得到蓝牙状态
	 */
	public int getConnectionState(){
		if(mBtService != null)
			return mBtService.getState();
		else
			return 0;
	}
	
	/**
	 * 注册服务
	 */
	private void registerService(){
		if(D) Log.i(TAG, "registerService enter");
		if (mBtService == null){
			mBtService = new BluetoothService(mContext, mHandler);
		}
	}
	
	/**
	 * 定义的消息对象
	 */
	private final Handler mHandler = new Handler(){	
		
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			
			switch (msg.what) {		
			case MESSAGE_STATE_CHANGE:
				if(D) Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
				
				CarDataService cds = (CarDataService) mContext;
				if(cds.getLocalsetting() == null){
//					Toast.makeText(mContext, R.string.setting_none, Toast.LENGTH_SHORT).show();
					return;
				}				
                if(lastBTState == -1)lastBTState = msg.arg1;
                
                switch (msg.arg1) {
                case BluetoothService.STATE_CONNECTED:
                	cds.getLocalsetting().setBtStat(BluetoothService.STATE_CONNECTED);
                    break;
                case BluetoothService.STATE_CONNECTING:
                	cds.getLocalsetting().setBtStat(BluetoothService.STATE_CONNECTING);
                    break;
                case BluetoothService.STATE_LISTEN:
                	if(lastBTState == BluetoothService.STATE_CONNECTED){	// OBD connection is broken, let's start a new trace id
                		cds.GPSUploadState = CarDataService.GPS_UPLOAD_START;
                	}
                	cds.getLocalsetting().setBtStat(BluetoothService.STATE_LISTEN);
                	break;
                case BluetoothService.STATE_NONE:
                	cds.getLocalsetting().setBtStat(BluetoothService.STATE_NONE);
                    break;
                default:
                	break;
                }
                
                lastBTState =  msg.arg1;
                break;
            case MESSAGE_WRITE:
                byte[] writeBuf = (byte[]) msg.obj;
                // construct a string from the buffer
                String writeMessage = new String(writeBuf);
                
                break;
            case MESSAGE_READ:
                byte[] readBuf = (byte[]) msg.obj;
                // construct a string from the valid bytes in the buffer
                String readMessage = new String(readBuf, 0, msg.arg1);
                getDataThread.setCommData(readMessage);
                setIsBusy(false);                
                break;
            case MESSAGE_DEVICE_NAME:
                // save the connected device's name
            	mConnectedDeviceName =  msg.getData().getString(DEVICE_NAME);
                Toast.makeText(mContext, "Connected to "
                               + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                break;
            case MESSAGE_TOAST:
                Toast.makeText(mContext, msg.getData().getString(TOAST),
                               Toast.LENGTH_SHORT).show();
                break;
			case STATE_ON:
				registerService();	// in case the service is not registered initially
				break;
			case STATE_OFF:
				
				break;
			case STATE_TURING_ON:
				
				break;
			case STATE_TURING_OFF:
				
				break;
			case SEND_TIMEOUT:
				Toast.makeText(mContext, "Timeout!", Toast.LENGTH_SHORT).show();
				setIsBusy(false);
			default:
				break;
			}
		}		
	};	
	
	/**
	 * 连接蓝牙设备
	 */
	public void ConnectDevices(final String address){
		// Get the BLuetoothDevice object
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        
        Log.d("BTDevice", device.toString());
        // Attempt to connect to the device
        mBtService.connect(device);
	}
	
	/**
	 * 开启线程处理，打开蓝牙设备
	 *
	 */
	private class OpenBluetoothThread extends Thread{

		@Override
		public void run() {
			// TODO 自动生成的方法存根
			//直接打开蓝牙	
			while(mBluetoothAdapter.getState() != BluetoothAdapter.STATE_ON){
				try {
					sleep(50L);					
					Message msg = new Message();
					msg.what = mBluetoothAdapter.getState();
					mHandler.sendMessage(msg);
				} catch (InterruptedException e) {
					// TODO: handle exception
					e.printStackTrace();
//					mDialog.dismiss();
				}
			}
		}
	}
	
	/**
	 * 超时线程，发送串口指令之后，3秒内未得到返回数据，视为超时
	 * @author Jacy
	 *
	 */
	private class TimeoutThread extends Thread{	
		public boolean isStop;
		
		public TimeoutThread(){
			isStop = false;
		}
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			int i = 0;
			while(!isStop){
				try{					
					if (!getIsBusy()) break;
					
					sleep(20L);
					i++;
					if (i == 500){						
						if (getIsBusy()){							
							Message msg = mHandler.obtainMessage(SEND_TIMEOUT);
							mHandler.sendMessage(msg);
//							if(Mainactivity.mpDialog.isShowing())
//								Mainactivity.mpDialog.cancel();
							break;
						}		
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
					// TODO: handle exception
				}				
			}			
		}
		
		public void cancel(){
			isStop = true;
		}
	}

}
