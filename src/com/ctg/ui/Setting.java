package com.ctg.ui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.SimpleAdapter;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.ctg.bluetooth.EST527.BluetoothService;
import com.ctg.bluetooth.EST527.BluetoothSet;
import com.ctg.crash.LogRecord;
import com.ctg.group.Group;
import com.ctg.net.HttpQueue;
//import com.ctg.net.HttpThread;
import com.ctg.net.UploadGPS;
import com.ctg.obdii.OBDcmd;
import com.ctg.sensor.OBDSensor;
import com.ctg.service.CarDataService;
import com.ctg.trafficViolation.TrafficVioListDlg;
import com.ctg.trafficViolation.TrafficVioQueryDlg;
import com.ctg.util.*;

import android.view.View.OnClickListener;
import butterknife.Bind;
import butterknife.ButterKnife;

public class Setting implements View.OnClickListener{
	public static final String MAC_ADDRESS_SUB = "/OBDII/mac_address"; 
	public static final String VOLTAGE_ADDRESS = "/OBDII/voltage";
	private static final String TAG = "Setting";
	private static final boolean D = true;
	public String MAC_ADDRESS;
	public String mac_addr_str = null;	
	//public BluetoothSet mBluetoothSet;
	public OBDcmd mOBDcmd;
	public OBDApplication OBDApp;
    public LinearLayout linearLayout;  
    public ScrollView scrollView;
    private Base baseAct;
    Resources baseRes;
    //private ImageButton obdStat;
    //private ImageButton blueEnable;
    private View btSearch;
    private TextView clearDtc;  
    View back_v;
    View connect_rela;
    View clear_rela;
    View account_rela;
    View help_rela;
    
    private View helpBtn;
    public TextView bt_conn_stat;
    public TextView volVal;
    private TextView dtcsNum;
    private TextView version;
	private TextView conn_vol;//voltage
	private TextView logoutBtn;
	private TextView accountBtn;
	private Button startmapBtn;
	private Button wzBtn;
	public ImageView gpsSwt;
	public ImageView sensorSwt;
	
	public ImageView driveRecord;
	public boolean sensorEnable = false;
	public BT_Conn bt_s;

	public CustomDialog helpDialog;
	public CustomDialogRadioGroup mpRadioDialog;
	
	// public WeiZhDetailDlg wzDetailDlg;
	// tableRow;
	public String obdDevice;
	OBDSensor obdSensor = null;
	int loop_time = 0;
	TestOBDThread mythread; 
	private int mBtStat = -1;
	ProgressDialog dlg;
	//HttpThread httpConnect;
	String version_str;
	private static boolean haveGotVol = false;
//	boolean recordSwitch;
//	Login loginDlg;
	
	class TestOBDThread extends Thread{
		public synchronized void run() {
			
			while(true){
				try {
					sleep(500L);
					mOBDcmd.enginerpm = loop_time*1000;
					mOBDcmd.vehiclespeed = loop_time*20;
					mOBDcmd.enginetemp = loop_time*20;
					loop_time = (loop_time+1)%9;
					//mHandler.obtainMessage(OBDcmd.READY).sendToTarget();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
	}
	

	
	public Setting(Context context, int width, int height, int layout, int style)
	{
	}
	
	public Setting(Context cont) {
		//linearLayout = (LinearLayout) View.inflate(cont, R.layout.setting, null);				
		scrollView = (ScrollView) View.inflate(cont, R.layout.setting, null);
//		setContentView(scrollView);
		baseAct = (Base) cont;
		baseRes = baseAct.getResources();
//		setTitle(R.string.setting);		
//		Window window = getWindow();
//		WindowManager.LayoutParams params = window.getAttributes();
//		params.width =  baseAct.mWidth;
//		params.height =  baseAct.mHeight;
//		params.gravity = Gravity.TOP;
//		//params.verticalMargin = 2.0F;
//		window.setAttributes(params);
		// TODO Auto-generated constructor stub
 
//        baseAct.setting_c.addView(scrollView, DTCs_Scroll.params);
        linearLayout = (LinearLayout) scrollView.findViewById(R.id.setting);
        //obdStat = (ImageButton)((ViewGroup) linearLayout.getChildAt(0)).getChildAt(1);
        //blueEnable = (ImageButton)((ViewGroup) linearLayout.getChildAt(1)).getChildAt(1);  
        btSearch = linearLayout.findViewById(R.id.conn_more); 
        bt_conn_stat = (TextView)linearLayout.findViewById(R.id.conn_stat);
        //dtcsNum = (TextView)((ViewGroup) linearLayout.getChildAt(1)).getChildAt(1);  
        clearDtc = (TextView) linearLayout.findViewById(R.id.clear_btn);
        //volVal = (TextView)linearLayout.findViewById(R.id.voltage_val); 
        helpBtn = linearLayout.findViewById(R.id.help_rela_sub);        
        version = (TextView)linearLayout.findViewById(R.id.text_version); 
        accountBtn = (TextView)linearLayout.findViewById(R.id.account_btn);
        logoutBtn = (Button)linearLayout.findViewById(R.id.logout);
        startmapBtn = (Button)linearLayout.findViewById(R.id.startmap);
		gpsSwt = (ImageView) linearLayout.findViewById(R.id.gps_switch);
		sensorSwt = (ImageView) linearLayout.findViewById(R.id.sensor_switch);
		conn_vol = (TextView) linearLayout.findViewById(R.id.conn_vol);
		wzBtn = (Button) linearLayout.findViewById(R.id.weizhang_btn);
		back_v = linearLayout.findViewById(R.id.setting_back);
	    connect_rela = linearLayout.findViewById(R.id.conn_more_rela);
	    clear_rela = linearLayout.findViewById(R.id.clear_rela);
	    account_rela = linearLayout.findViewById(R.id.account_rela);
	    help_rela = linearLayout.findViewById(R.id.help_rela);
	    driveRecord = (ImageView) linearLayout.findViewById(R.id.drive_record_i);
	    connect_rela.setOnClickListener(this);
	    clear_rela.setOnClickListener(this);
	    account_rela.setOnClickListener(this);
	    help_rela.setOnClickListener(this);
	    logoutBtn.setOnClickListener(this);
	    back_v.setOnClickListener(this);
	    driveRecord.setOnClickListener(this);
		mac_address_init();
        
        //version_str = Preference.getInstance(baseAct.getApplicationContext()).getVersion();
        version_str = baseAct.OBDApp.getVersion();
        version.setText(version_str);

        
        boolean trackEnable = Preference.getInstance(baseAct.getApplicationContext()).getGpsMonitor();
       
        if(trackEnable){
        	gpsSwt.setImageResource(R.drawable.icon_radio_enable);
        }
        else{
        	gpsSwt.setImageResource(R.drawable.icon_radio_disable);
        }

  
        gpsSwt.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
//		        boolean gpsEnable = baseAct.locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER); 
				boolean trackEnable = Preference.getInstance(baseAct.getApplicationContext()).getGpsMonitor();
		        if (!trackEnable){// turn on gpsSwt
		        	LogRecord.SaveLogInfo2File(Base.OperateInfo, TAG+"track enable");
	        		Preference.getInstance(baseAct.getApplicationContext()).setGpsMonitor(true);
	        		gpsSwt.setImageResource(R.drawable.icon_radio_enable);				    
				}
				else{	// turn off gpsSwt
					LogRecord.SaveLogInfo2File(Base.OperateInfo, TAG+"track disable");
					Preference.getInstance(baseAct.getApplicationContext()).setGpsMonitor(false);
					gpsSwt.setImageResource(R.drawable.icon_radio_disable);
					if(baseAct.localbinder != null)
						baseAct.localbinder.setGPSUpdateState(CarDataService.GPS_UPLOAD_START);	// to cause to make a new trace
				}		
			}

        });

		

		sensorSwt.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				// TODO Auto-generated method stub
				sensorEnable = !sensorEnable;
				if( CarDataService.obdSensor == null ){
					CarDataService.obdSensor = new OBDSensor(baseAct);
				}
				if (sensorEnable) {
//					if( mBtStat == 3 ){
					LogRecord.SaveLogInfo2File(Base.OperateInfo, TAG+"sensor enable");
					sensorSwt.setImageResource(R.drawable.icon_radio_enable);
//					CarDataService.obdSensor.initData();
					CarDataService.obdSensor.registSensor();					
					Base.OBDApp.sensorState = true;//start write sensor data
//					}else{
//						Toast.makeText(baseAct, "OBD未连接，请先连接OBD", 1).show();
//					}
				} else {
					LogRecord.SaveLogInfo2File(Base.OperateInfo, TAG+"sensor disable");
					sensorSwt.setImageResource(R.drawable.icon_radio_disable);					
					CarDataService.obdSensor.unRegistSensor();
					Base.OBDApp.sensorState = false;//stop write sensor data and tell service upload data if wifi enable
				}				
			}
		});



        setSettingLoginState();

        if(D) Log.d(TAG, "new BT_Conn");
        bt_s = new BT_Conn(cont);	// register broadcast receiver
	}
	
	public void setSettingLoginState(){
		if(accountBtn == null || logoutBtn == null)
			return;
        if(Preference.getInstance(baseAct.getApplicationContext()).getLoginStat()){
        	accountBtn.setText(R.string.login_yes);
//        	logoutBtn.setVisibility(View.VISIBLE);
        	logoutBtn.setBackground(baseRes.getDrawable(R.drawable.shape_red));
        	logoutBtn.setText("退出登录");
			accountBtn.setTextColor(baseRes.getColor(R.color.black));
        }
        else{
        	accountBtn.setText(R.string.login_title);
//        	logoutBtn.setVisibility(View.INVISIBLE);
        	logoutBtn.setBackground(baseRes.getDrawable(R.drawable.shape_green));
        	logoutBtn.setText("登录");
			accountBtn.setTextColor(baseRes.getColor(R.color.gray));
        }
	}
	
	Handler handler = new Handler() {
		public void handleMessage(Message message) {
			String sessionid = null;
			switch (message.what) {
//				case HttpThread.LOGOUT_STATUES:
//					if (message.getData().getInt(HttpThread.KEY_TRANSFER_STATUS) == HttpThread.CONNECT_SUCCEED) {
//						sessionid = message.getData().getString(HttpThread.SESSIONID);						
//						//Toast.makeText(baseAct, baseAct.getResources().getString(R.string.logout_title)+baseAct.getResources().getString(R.string.success), Toast.LENGTH_SHORT).show();
//						//loginScc();
//						Preference.getInstance(baseAct.getApplicationContext()).setAutoConnect(false);
//						//Base.OBDApp.login_stat = false;
//						Preference.getInstance(baseAct.getApplicationContext()).setLoginStat(false);
//						accountBtn.setText(R.string.login_title);
//						logoutBtn.setVisibility(View.INVISIBLE);
//					}
//
//					if (Base.OBDApp.httpConnect != null) {
//						Base.OBDApp.httpConnect.stopThread();
//						Base.OBDApp.httpConnect = null;			
//					}
//					break;
				case BT_Conn.BT_DEVICE_IN_DISCOVERY:
					bt_conn_stat.setText(R.string.searching_bt);
					break;
				
				case BT_Conn.OBD_DEVICE_NOT_FOUND:
					bt_conn_stat.setText(R.string.none_found);
					break;
					
				default:break;
			}
		}
	};
	
	private boolean logOut(){
		Preference.getInstance(baseAct.getApplicationContext()).setLoginStat(false);
		setSettingLoginState();
		return true;
	}
	
	public boolean saveBatteryVol(String vol){
		File file = new File(VOLTAGE_ADDRESS); 
		//String vol = (String) volVal.getText();
		byte vol_byte[] = new byte[100];
		int len = 0;
		boolean ret;
		SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");     
		String date = sDateFormat.format(new java.util.Date());				
		String date_vol = date+" "+vol+"\n";
		
		try {				
			RandomAccessFile randomFile = new RandomAccessFile(file, "rw");  
            //文件长度，字节数  
            long fileLength = randomFile.length();  
            // 将写文件指针移到文件尾。  
             randomFile.seek(fileLength);               
             randomFile.writeBytes(date_vol);  
             randomFile.close();  
         } catch (IOException e) {  
             e.printStackTrace();  
             return false;
         }  		
		 return true;
	}
	
	public boolean initBatteryBol(){
		File file = new File(VOLTAGE_ADDRESS); 
		//String vol = (String) volVal.getText();
		byte vol_byte[] = new byte[50];
		int len = 0;
		boolean ret;
		String date_vol = null;
		char ch = '\0';
		int readCnt = 0;
		int idx = 0;
		String strArr[];
//		SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");     
//		String date = sDateFormat.format(new java.util.Date());				
//		String date_vol = date+" "+vol+"\n";
		if(!file.exists())
			return false;
		
		try {				
			RandomAccessFile randomFile = new RandomAccessFile(file, "r");  
            // 文件长度，字节数   
            long fileLength = randomFile.length();  
            // 将写文件指针移到文件尾。 
            if(fileLength == 0){
            	randomFile.close();            
            	return false;
            }
            if(fileLength >= 25){
            	randomFile.seek(fileLength-25);            
            	randomFile.read(vol_byte, 0, 50);
            	date_vol = new String(vol_byte);
            	idx = date_vol.indexOf('\n');
            	date_vol.substring(idx+1);
            }
            else{
            	randomFile.read(vol_byte, 0, 50);
            	date_vol = new String(vol_byte);            	
            }            
            randomFile.close();  
            date_vol.replace("\n", "");
            
            strArr = date_vol.split(" ");
//            if(strArr != null && strArr.length == 3)
//            	volVal.setText(strArr[2]);
            //ch = randomFile.readChar()
            //randomFile.writeBytes(date_vol);  
            
         } catch (IOException e) {  
             e.printStackTrace();  
             return false;
         }  		
		 return true;
		 
		
	}

	
	public int getBtStat(){
		return mBtStat;
	}
	
	//upload_stat: 0 not upload, 1 upload car start, 2 upload car shutdown
	public void setVoltage(String vol, int upload_stat){//12.8V
		if(baseAct.serviceConn){
//			volVal.setText(vol);
			String volDigit = "";
			float volF;
			long volExp;
			if(vol.contains("v") || vol.contains("V"))
				volDigit = vol.substring(0, vol.length()-1);
			else
				volDigit = vol;
			volF = Float.parseFloat(volDigit);
			if(volF < 13.01){
				volExp = Preference.getInstance(baseAct.getApplicationContext()).getVolExp();
				if(volExp != 0){
					double volExpD = Double.longBitsToDouble(volExp);		
					double expTime =  (volExpD*(volF-10.5));
					long expTimeL = (long) expTime;
					int day = 0, hr = 0;
					String notice = baseAct.getResources().getString(R.string.bat_expire_time);
					String expTimeStr = "";
					
					if((day = (int) (expTimeL/86400)) > 0){
						expTimeStr = expTimeStr + day + baseAct.getResources().getString(R.string.bat_day);
						expTimeL %= 86400;
					}
					if((hr = (int) (expTimeL/3600)) > 0){
						expTimeStr = expTimeStr + hr + baseAct.getResources().getString(R.string.bat_hour);					
					}
					if(!expTimeStr.equals("")){
						int rank = (int) ((volF-10.5)*6/(13-10.5));
//						if(day > 0)
//							baseAct.tips_s.setBatteryInfo(Base.BAT_NORMAL, rank, notice+expTimeStr);
//						else
//							baseAct.tips_s.setBatteryInfo(Base.BAT_LOW, rank, notice+expTimeStr);
					}
				}
				if(upload_stat != 0)
					baseAct.uploadVoltage(volDigit);
			}
			
			baseAct.curVoltage = Float.parseFloat(volDigit);
		}
		else
			saveBatteryVol(vol);
	}
	
	public void setBtStat(int btStat){
		if(baseAct == null || baseAct.serviceConn == false){
			//Log.d(TAG, "BT connection baseAct.serviceConn == false");
			return;
		}
		Log.d(TAG, "BTConnect setBtStat:" + btStat);
		mBtStat = btStat;
		if(btStat == 0 || btStat == 1){
			bt_conn_stat.setText(R.string.title_not_connected);
//			if(baseAct.panel_s != null)
//				baseAct.panel_s.setPanelReadData("/", "/", "/","/");
		}
		else if(btStat == 2){
			bt_conn_stat.setText(R.string.title_connecting);
//			if(baseAct.panel_s != null)
//				baseAct.panel_s.setPanelReadData("/", "/", "/","/");
		}
		else if(btStat == 3){
			String conn_state_str = baseAct.getResources().getString(R.string.title_connected_to);							
			conn_state_str += obdDevice;
			bt_conn_stat.setText(conn_state_str);
			BT_Conn.setCounter(0);
		}
//		if(baseAct.dtc_s != null && baseAct.dtc_s.curCount == 0){	
//			if(btStat != 3)
//				Toast.makeText(baseAct, R.string.dtc_no_bt, Toast.LENGTH_SHORT).show();			
//			else
//				Toast.makeText(baseAct, R.string.no_dtc_notice, Toast.LENGTH_SHORT).show();
//			
//		}
	}
	
	public boolean mac_address_save(){
		File file = new File(MAC_ADDRESS);  
		byte mac_byte[] = new byte[100];
		int len = 0;
		boolean ret;
		
		try {
//			ret = file.createNewFile();
//			if(ret){
			FileOutputStream outs = new FileOutputStream(file);
			if(mac_addr_str == null){
				outs.close();			
				return false;
			}
			mac_byte = mac_addr_str.getBytes();
			outs.write(mac_byte);
			outs.close();
//			}
//			else
//				return false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return true;
	}
	
	public void mac_address_init(){
		
		MAC_ADDRESS = Base.getSDPath() + MAC_ADDRESS_SUB;
		File file = new File(MAC_ADDRESS);  
		byte mac_byte[] = new byte[100];
		int len = 0;
		
		
		if(!file.exists()){
			return;
		} 
		
		try {
			FileInputStream ins = new FileInputStream(file);
			if((len = ins.available()) > 0){
				ins.read(mac_byte, 0, len);	
				mac_addr_str = new String(mac_byte, 0, len);				
			}
			ins.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		
	}
	
	
	public void bluetooth_connect(){
		if (!CarDataService.iEST527) {
			if (CarDataService.mOBDcmd == null)
				return;
			if (CarDataService.mOBDcmd.mBluetoothSet == null) {
				CarDataService.mOBDcmd.mBluetoothSet = new com.ctg.bluetooth.BluetoothSet(
						CarDataService.mOBDcmd.memHandler);
			}
			CarDataService.mOBDcmd.mBluetoothSet.ConnectDevices(mac_addr_str);
		}
		else {
			if(baseAct.localbinder == null){
				return;
			}
			
			BluetoothSet bts = baseAct.localbinder.getBluetoothSet();			
			if(!bts.isSupported()){
				Toast.makeText(baseAct, "Bluetooth SPP Not Supported!",
						Toast.LENGTH_SHORT).show();
				return;
			}
			else if(!bts.isRegistered()){
				Toast.makeText(baseAct, "Bluetooth Service Not Registered!",
						Toast.LENGTH_SHORT).show();
				return;
			}
//			else if(!bts.isConnected()){
//				Toast.makeText(baseAct, "Bluetooth Service Not Connected!",
//						Toast.LENGTH_SHORT).show();
//				return;
//			}
			bts.ConnectDevices(mac_addr_str);
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()){
		case R.id.conn_more_rela:
			LogRecord.SaveLogInfo2File(Base.OperateInfo, TAG+"bt connection click");
			btSearch.findViewById(R.id.conn_more_img).setBackground(baseRes.getDrawable(R.drawable.arrow_r_gray));   					
//			if( mBtStat != BluetoothService.STATE_CONNECTED ){
//				if (!bt_s.mBtAdapter.isEnabled()) {
//					bt_s.mBtAdapter.enable();
//				}
//				
//				if (baseAct.setting_s.bt_s != null) {
//					new Thread(baseAct.setting_s.bt_s).start();
//					Log.d(TAG, "BTConnect auto connect");
//				}
//			}			
			break;
		case R.id.drive_record_i:
			if(baseAct.monitor == null)
				return;
			if(!baseAct.monitoring) {
				baseAct.monitor.config(6, 10);
				baseAct.monitor.start();
				baseAct.monitoring = true;
				driveRecord.setImageResource(R.drawable.icon_radio_enable);
				Toast.makeText(baseAct, "Start recording ...", Toast.LENGTH_SHORT).show();
			}
			else {
				baseAct.monitor.stop();
				baseAct.monitoring = false;
				driveRecord.setImageResource(R.drawable.icon_radio_disable);
				Toast.makeText(baseAct, "Stop recording ...", Toast.LENGTH_SHORT).show();
			}
			break;
			
		case R.id.clear_rela:
			LogRecord.SaveLogInfo2File(Base.OperateInfo, TAG+"clear DTC click");
			if(baseAct.localbinder != null){
				baseAct.localbinder.clearDTCs();							
			}
			baseAct.clear_dtc();	
			break;
			
		case R.id.account_rela:
			LogRecord.SaveLogInfo2File(Base.OperateInfo, TAG+"account click");
			if(Preference.getInstance(baseAct.getApplicationContext()).getLoginStat()){					
				helpDialog = new CustomDialog(baseAct, 320, 320, R.layout.account_info, R.style.Theme_dialog3);
				ListView list_v = (ListView) helpDialog.findViewById(R.id.account_info);
				list_v.setDivider(baseAct.gray_line_draw);
				SimpleAdapter adapter;
				ArrayList<Map<String, Object>> listItem = new ArrayList<Map<String, Object>>();
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("type", baseAct.getResources().getString(R.string.user_account));
				map.put("cont", Preference.getInstance(baseAct).getUser());
				listItem.add(map);
				map = new HashMap<String, Object>();
				map.put("type", baseAct.getResources().getString(R.string.car_plate));
				map.put("cont", Preference.getInstance(baseAct).getLicence());
				listItem.add(map);
				map = new HashMap<String, Object>();
				map.put("type", baseAct.getResources().getString(R.string.car_maker));
				map.put("cont", Preference.getInstance(baseAct).getCarMaker());
				listItem.add(map);
				map = new HashMap<String, Object>();
				map.put("type", baseAct.getResources().getString(R.string.car_type));
				map.put("cont", Preference.getInstance(baseAct).getCarType());
				listItem.add(map);
				map = new HashMap<String, Object>();
				map.put("type", baseAct.getResources().getString(R.string.purchase_date));
				map.put("cont", Preference.getInstance(baseAct).getPurchaseDate());					
				listItem.add(map);
				
				adapter = new SimpleAdapter(baseAct,listItem,// 
			            R.layout.account_item,
			            new String[] {"type", "cont"},   
			            new int[] {R.id.account_type,R.id.account_content}  
			        ); 
				list_v.setAdapter(adapter); 
				
				View editView = helpDialog.findViewById(R.id.account_edit);
				editView.setOnClickListener(this);
				helpDialog.show();
				
			}
			else{
				baseAct.loginDlg = new Login(baseAct, Base.mWidth, Base.mHeight, R.layout.login, R.style.Theme_dialog);
				baseAct.loginDlg.show();
			}			
			break;
		case R.id.help_rela:
			LogRecord.SaveLogInfo2File(Base.OperateInfo, TAG+"help click");
			helpDialog = new CustomDialog(baseAct, 320, 320, R.layout.help_content, R.style.Theme_dialog3);
			helpDialog.show();
			break;					
		case R.id.setting_back:
//			Setting.this.hide();
			break;
		case R.id.logout:
			LogRecord.SaveLogInfo2File(Base.OperateInfo, TAG+"login out click");
    		if(Preference.getInstance(baseAct.getApplicationContext()).getLoginStat()){
    			CarDataService.saveFenceData();
    			logOut();
    			Base.baidu_v.loginStateChange(false);
    		}
    		else{
    			baseAct.loginDlg = new Login(baseAct, baseAct.mWidth, baseAct.mHeight, R.layout.login, R.style.Theme_dialog);
    			baseAct.loginDlg.show();
    		}			
			break;
			
		case R.id.account_edit:
			baseAct.editAccountDlg = new EditAccount(baseAct, Base.mWidth, Base.mHeight, R.layout.edit_account_info, R.style.Theme_dialog);			
			baseAct.editAccountDlg.show();
			break;
		}
	}
}
