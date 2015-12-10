/******************************************************/
/* Copyright (C) 2014 The Android Project             */
/* All rights is reserved by Harman CTG Shanghai      */
/* First Version is delivered  by Zhiming.hu          */
/* Data: 2014-02-25                                   */
/* Change history:                                    */
/* Modifier:                                          */
/* Data:                                              */
/******************************************************/

package com.ctg.obdii;


import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Semaphore;


import android.content.Context;


import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;


import com.ctg.bluetooth.BluetoothService;
import com.ctg.bluetooth.BluetoothSet;
import com.ctg.crash.CrashHandler;
import com.ctg.crash.LogRecord;
import com.ctg.service.CarDataService;
import com.ctg.ui.Base;
import com.ctg.ui.OBDApplication;



public class OBDcmd{
	public static int test_dtc_send;
	
//	public OBDApplication OBDapp;
	public Context mContext;
	
	private static boolean D = false;
	private static final String TAG = "OBDcmd";
	private static int updatestatus = 0;
	public ReadThread mreadthread = null;
	public static volatile boolean readstate = true;
	private volatile int softtoken = 0;
	//invoked by high level activity:
	public  DTCindex mDTCindex;
	//-------------------------------
	private int mdtcnum =  0;
	private DTCindex.DTCCodeType dtccodetype;
	
	//send meesage to high level activity:
	private Handler mHandler;
	private static Handler tempHandler = null;
	private SendReceCycle sendrecethread;
	private TimeoutThread timeoutthread;
	public static final int READY = 10; 
	public static final int STATECHANGE = 20;
	public static final int DTCEXIST = 30;
	public static final int DTCNOEXIST = 31;
	public static final int VOLTVALUE = 40;
	public static final int SHUTDOWN = 50;
	public static final int VSUPPORT = 60;
	public static final int TEST = 100;
	//public static final int DTCEXIST = 30;
	private static int CONNECTSTATE = 0;
	// STATE_NONE       :  CONNECTSTATE = 0     we're doing nothing
    // STATE_LISTEN     :  CONNECTSTATE = 1;    now listening for incoming connections
    // STATE_CONNECTING :  CONNECTSTATE = 2;    now initiating an outgoing connection
    // STATE_CONNECTED  :  CONNECTSTATE = 3;    connection is established
	
	//Used by high level activity;
	public  String obddtcexplain = "";
	public  String obddtcsolution = "";
	public  static final int DTCnum = 10 ;
	
	
	//Use semaphore to synchnize the send and receive thread:
	public static final Semaphore sendsem = new Semaphore(1);
	public static final Semaphore readsem = new Semaphore(1);
	
	//-------------------------------
	public final int ENGINERPM = 1;
	public final int SPEED     = 2;
	public final int THROTTLE  = 4;
	public final int LOAD      = 8;
	public final int TEMPRATURE= 16;
	public final int FUEL      = 32;
	public final int DTCS      = 64;
	public final int DTCNUM    = 128;
	//initialize status is used for monitoring init command status;
	public static volatile int initstatus = 0;
	//Define vehicle info:
	public enum VehicleInfo{
		EngineRPM,
		VehicleSpeed,
		ThrottlePosition,
		EngineLoad,
		CoolantTemprature,
		FuelConsumption,
		DiagnosticStatus,
		DiagnosticCode,
		ReadFuel
	}
	private VehicleInfo vehiclecmd = OBDcmd.VehicleInfo.EngineRPM ;
	//Define EST327 initializing process command:
	public enum InitCmd{
		ResetAll,
		WarmStart,
		EchoOff,
		LineFeeds,
		HeadsOn,
		VerionInfo,
		DisplayDriver,
		ReadVolt,
		SetProtocol,
		CurrentProtocol,
		SupportCmd,
		ClearDTC		
	}
	private InitCmd initcmd;
	//response ID of OBD service number
	public final static String RES_SERVICE01 = "41";
	public final static String RES_SERVICE02 = "42";
	public final static String RES_SERVICE03 = "43";
	public final static String RES_SERVICE04 = "44";
	public final static String RES_SERVICE05 = "45";
	public final static String RES_SERVICE06 = "46";
	public final static String RES_SERVICE07 = "47";
	public final static String RES_SERVICE08 = "48";
	public final static String RES_SERVICE09 = "49";	
	//OBD command status:
	public final static int RESPONSE_CONT = 200;  
	public final static int RESPONSE_DATA = 201;
	public final static int RESPONSE_OK   = 202;
	public final static int RES_EXCEP     = 203;
	//OBD sub command---control command:
	public final static int RES_VOLT = 210;  
	public final static int RES_PVER = 211;
	public final static int RES_HVER = 212;	
	//OBD sub command---data command:
	public final static int RES_RPM = 220;
	public final static int RES_VSS = 221;
	public final static int RES_TP  = 223;	
	public final static int RES_LOD = 224;  
	public final static int RES_ECT = 225;
	public final static int RES_MPG = 226;	
	public final static int RES_DTC = 227;
	public final static int RES_DTCs =228;
	public final static int RES_FUEL =229;
	
	//OBD initializing control command number:	
	public final static int RSTA = 100;  //reset all
	public final static int WRST = 101;  //warm start
	public final static int ECHO = 102;  //echo off
	public final static int LFDO = 103;  //linefeeds off 	
	public final static int HDON = 104;  //heads on
	public final static int VINF = 105;  //version information
	public final static int DISP = 106;  //display driver
	public final static int VOLT = 107;  //read voltage
	public final static int SETP = 108;  //set protocol automatically
	public final static int CURP = 109;  //describe current protocol
	public final static int SUPP = 110;  //OBD support command	
	public final static int CLRDTC = 111;  //clear OBD DTC
	
	
	
	//OBD send control command content:
	public static String SendRSTA = "ATZ\r\n";
	public static String SendWRST = "ATWS\r\n";
	public static String SendECHO = "ATE0\r\n";
	public static String SendLFDO = "ATL0\r\n";
	public static String SendHDON = "ATH0\r\n";	
	public static String SendVINF = "ATI\r\n";
	public static String SendDISP = "AT@1\r\n";	
	public static String SendVOLT = "ATRV\r\n";
	public static String SendSETP = "ATSPA6\r\n";//"ATSP0\r\n";	
	public static String SendCURP = "ATDP\r\n";
	public static String SendSUPP = "01 00\r\n";	
	public static String NOCMD    = "";		
	
	//OBD read vehicle parameters command number:
	public final static int RPM = 0x0C;    //engine speed
	public final static int VSS = 0x0D;    //vehicle speed
	public final static int TP  = 0x11;    //Throttle position
	public final static int LOD = 0x04;    //engine load
	public final static int ECT = 0x05;    //cool oil temperature
	public final static int MPG = 0x10;    //Instantaneous fuel consumption
	public final static int DTC = 0x01;    //read DTC status
	public final static int DTCs =0x43;   //read detail DTC number
	public final static int FUELS =0x2F;   //read fuel;
//	private final static int AVM = 6;        //average fuel consumption
	
	//OBD read vehicle data command content:
	private static String ReadRPM = "01 0C\r\n";
	private static String ReadVSS = "01 0D\r\n";
	private static String ReadTP  = "01 11\r\n";
	private static String ReadLOD = "01 04\r\n";
	private static String ReadECT = "01 05\r\n";	
	private static String ReadMPG = "01 10\r\n"; //use (MAF/14 = instantaneous fuel consumption)
	private static String ReadMPG1 = "01 0B\r\n";
	private static String ReadFUELS = "01 2F\r\n";
//	private static String ReadAVM = "";		
	//OBD read Diagnostic Trouble Code status command:
	private static String ReadDTC = "01 01\r\n";	
	//OBD read detail Diagnostic Trouble Code command:
	private static String ReadDTCs = "03\r\n";		
	//OBD clear DTC command:
	private static String CLRDTCs = "04\r\n";	
	//OBD response data from OBDII interface in string format:
	public static String strRPM = "";
	public static String strVSS = "";
	public static String strTP  = "";
	public static String strLOD = "";
	public static String strECT = "";
	public static String strMPG = "";
	public static String strDTC = "";
	public static String strDTCs = "";
	public static String strFUEL = "";
	
	//OBD data is parsed by using readable format:
	//read by high level activity:
	public static int enginerpm        = 0;    
	public static int vehiclespeed     = 0;
	public static int throttleposition = 0;
	public static int engineload       = 0;
	public static int enginetemp       = 0;
	public static double fuelconsump   = 0;
	public static int dtcstatus        = 0;
	public static int percentfuel       = 0;
	public static String olddtccode    = "";
	public static String dtccode       = "";  
	public static String dtchistory    = "";
	//------------------------------------------
	public static String protocolver   = "";
	public static String obdvoltage    = "";
	public static String hardwarever   = "";
	public int resToken = 0;
	private static boolean dtccupdateflag = false;
	private static boolean dtccleanflag = false;
	private boolean getdtcstatus = false;
	private volatile boolean turnoffvolt = false;
	

	
	//OBD command structure:
	public static String servicenr  = "";	
	public static String pidnr      = "";	
	public static String cmdcontent = "";	
	public static String obdcommand = "";	
	
	////invoked by high level activity:
	public BluetoothSet mBluetoothSet = null;
	//---------------------------------------
	//private Context mContext = null;

	public static Queue<String> cmdqueue = new LinkedList<String>();
	private static boolean queuestatus = false;
	public  String DTCArray[] = new String[DTCnum];
	public  String DTCexplain[] = new String[DTCnum];
	public  String DTCsolution[] = new String[DTCnum];
	public  String DTCHistory[] = new String[DTCnum];


	
	
	//Create bluetoothset object and prepare to start bluetoothservice to connect device
	//dataHandler is for sending response data to meesage queue of high level application;
	public OBDcmd(Handler datahandler){
        //mContext = context;
		if(datahandler != null)
			mHandler = datahandler;
        //step 1:
        //New bluetoothset and bluetoothservice
		if(mBluetoothSet != null)
			mBluetoothSet = null;
        mBluetoothSet = new BluetoothSet(memHandler); 
        if(D) Log.d(TAG, "new bluetoothset");
        //step 2:
        //open bluetooth service and monitor connecting state
        mBluetoothSet.openBluetooth();
        if(D) Log.d(TAG, "openDeviceList");
        
        
        //step 3:        
        mDTCindex  = new DTCindex();
        if(D) Log.d(TAG, "new DTCindex");
        
//        OBDapp = new OBDApplication();
//        if(OBDapp != null)
//        	mContext = OBDapp.getApplicationContext();
        
        dtccode = "";
        dtchistory = "";
        
        //start to read thread to request data 
        //and monitor state bit to decide if send ready signal to high level activity;
        // monitor if bt is connected;
        // do OBD initialize command sequence;
        //
        // monitor if response data is ready;
        // monitor if dtc code exists;
        // read dtc explain and solution;
        readstate = true;
        if(mreadthread != null)
        	mreadthread = null;
        mreadthread   = new ReadThread();
        if(D) Log.d(TAG, "new ReadThread");
        mreadthread.start();   
        tempHandler = memHandler;
	}
	
	public void OBDStop(){
		if(mreadthread != null)
			mreadthread.cancel();
		mreadthread = null;
	}
	
	/**************Send OBD command*********************/
	/**************Send OBD control command*************/
	/**************Send OBD data read command***********/
	/**************Parse/Get OBD response data**********/
	/***************************************************/
	//wirte and update obd command:	
	public static void appendOBDcmd(String OBDcmd, int sendtoken){
		queuestatus = true;
		String cmdstr = OBDcmd + "-" + String.valueOf(sendtoken);
		if(cmdqueue != null){
			cmdqueue.offer(cmdstr);
			queuestatus = false;
			if(D) Log.d(TAG, "cmdqueue if false");
		}
		else
			if(D) Log.d(TAG, "cmdqueue doesn't exist!");
	}
	//Monitor command queue status: other thread can't operate queue when this thread appends element into queue;
	public static boolean getQueueStatus(){
		return queuestatus;
	}
	
	public void InitOBDSequence(int token){
		//if(D) Log.d(TAG, "init obd sequenc\n");
		if(mBluetoothSet == null)
			return;
		if((mBluetoothSet.isConnected())&&(!mBluetoothSet.getIsBusy()&&(initstatus == 0))){
			if(D) Log.d(TAG, "init obd sequenc  start\n");
			switch(token)
			{
			case 0:
				break;
			case 1:
				initcmd = OBDcmd.InitCmd.ResetAll;
				sendOBDControlCmd(initcmd, token);
				break;
			case 2:
				initcmd = OBDcmd.InitCmd.WarmStart;
				sendOBDControlCmd(initcmd, token);
				break;
			case 3:
				initcmd = OBDcmd.InitCmd.EchoOff;
				sendOBDControlCmd(initcmd, token);
				break;				     
			case 4:
				initcmd = OBDcmd.InitCmd.LineFeeds;
				sendOBDControlCmd(initcmd, token);
				break;
			case 5:
				initcmd = OBDcmd.InitCmd.HeadsOn;
				sendOBDControlCmd(initcmd, token);
				break;
			case 6:
				initcmd = OBDcmd.InitCmd.SetProtocol;
				sendOBDControlCmd(initcmd, token);
				break;
			case 7:	
				vehiclecmd = OBDcmd.VehicleInfo.EngineRPM;
				sendOBDDataCmd(vehiclecmd, token);	
			case 8:
				initcmd = OBDcmd.InitCmd.CurrentProtocol;
				sendOBDControlCmd(initcmd, token);
				break;
			case 9:
				initcmd = OBDcmd.InitCmd.SupportCmd;
				sendOBDControlCmd(initcmd, token);
				break;
			case 10:
				initcmd = OBDcmd.InitCmd.CurrentProtocol;
				sendOBDControlCmd(initcmd, token);
				break;
			case 11:
				initcmd = OBDcmd.InitCmd.ReadVolt;
				sendOBDControlCmd(initcmd, token);
				break;				
			case 12:
				initstatus = 1;
				break;				
			default:
				break;			
			}
			setsofttoken();
		}
	}
		
	
	
	public void TestInitOBDSequence(int token){
		if(mBluetoothSet == null)
			return;
		//if((mBluetoothSet.isConnected())&&(!mBluetoothSet.getIsBusy()&&(initstatus == 0)))
		if((mBluetoothSet.isConnected())&&(initstatus == 0)){
			if(D) Log.d(TAG, "init obd sequenc  start\n");
			switch(token)
			{
			case 0:
//				initcmd = OBDcmd.InitCmd.ResetAll;
//				sendOBDControlCmd(initcmd, token);
//				globalcmd = RSTA;
				break;
			case 1:
				//OBDApp.setglobalcmd(WRST);
				initcmd = OBDcmd.InitCmd.WarmStart;
				sendOBDControlCmd(initcmd, WRST);
				break;
			case 2:
				//OBDApp.setglobalcmd(ECHO);
				initcmd = OBDcmd.InitCmd.EchoOff;
				sendOBDControlCmd(initcmd, ECHO);
				break;				     
			case 3:
				//OBDApp.setglobalcmd(LFDO);
				initcmd = OBDcmd.InitCmd.LineFeeds;
				sendOBDControlCmd(initcmd, LFDO);
				break;
			case 4:
				//OBDApp.setglobalcmd(HDON);
				initcmd = OBDcmd.InitCmd.HeadsOn;
				sendOBDControlCmd(initcmd, HDON);
				break;
			case 5:
				//OBDApp.setglobalcmd(SETP);
				initcmd = OBDcmd.InitCmd.SetProtocol;
				sendOBDControlCmd(initcmd, SETP);
				break;
			case 6:	
				//OBDApp.setglobalcmd(RES_RPM);
				vehiclecmd = OBDcmd.VehicleInfo.EngineRPM;
				sendOBDDataCmd(vehiclecmd, RES_RPM);	
			case 7:
				//OBDApp.setglobalcmd(CURP);
				initcmd = OBDcmd.InitCmd.CurrentProtocol;
				sendOBDControlCmd(initcmd, CURP);
				break;
			case 8:
				//OBDApp.setglobalcmd(SUPP);
				initcmd = OBDcmd.InitCmd.SupportCmd;
				sendOBDControlCmd(initcmd, SUPP);
				break;
			case 9:
				//OBDApp.setglobalcmd(VOLT);
				initcmd = OBDcmd.InitCmd.ReadVolt;
				sendOBDControlCmd(initcmd, VOLT);
				break;				
			case 10:
				//OBDApp.setglobalcmd(RES_VSS);
				vehiclecmd = OBDcmd.VehicleInfo.VehicleSpeed;
				sendOBDDataCmd(vehiclecmd, RES_VSS);	
				break;	
			case 11:				
				initstatus = 1;
				break;
			default:
				//OBDApp.setglobalcmd(-1);; 
				break;
			}
			setsofttoken();
		}				
	}
	
	public void ClearDTcs(){
		if(mBluetoothSet == null)
			return;		
		if((mBluetoothSet.isConnected())&&(!mBluetoothSet.getIsBusy()&&(initstatus == 1))){
			initcmd = OBDcmd.InitCmd.ClearDTC;
			setsofttoken();
			sendOBDControlCmd(initcmd, softtoken);
			//if(D) 
				Log.d(TAG, "clear DTCs");
		}
	}
	
	public void Readvolt(){
		if(mBluetoothSet == null)
			return;		
		if((mBluetoothSet.isConnected())&&(initstatus == 1)){
			initcmd = OBDcmd.InitCmd.ReadVolt;
			setsofttoken();
			sendOBDControlCmd(initcmd, VOLT);
			//if(D) 
				Log.d(TAG, "Read Volt");
		}
	}
	
	public void DirectReadvolt(){
		if(mBluetoothSet == null)
			return;		
		initcmd = OBDcmd.InitCmd.ReadVolt;
		setsofttoken();
		sendOBDControlCmd(initcmd, VOLT);
	}
	
	
	public void Readdata(){	
		 if(mBluetoothSet == null)
			 return;
		 if((mBluetoothSet.isConnected()))
		 {
			 switch(vehiclecmd){
			    //read rpm:
			 	case EngineRPM:
			 		//OBDApp.setglobalcmd(RES_RPM);
			 		//setsofttoken();
			 		//softtoken = softtoken + 1;
			 		sendOBDDataCmd(vehiclecmd, RES_RPM);
			 		vehiclecmd = OBDcmd.VehicleInfo.VehicleSpeed; 
			 		break;
			 	case VehicleSpeed:
			 		//OBDApp.setglobalcmd(RES_VSS);
			 		//read speed:
			 		//setsofttoken();
			 		//softtoken = softtoken + 1;
			 		sendOBDDataCmd(vehiclecmd, RES_VSS);
			 		vehiclecmd = OBDcmd.VehicleInfo.CoolantTemprature; 
			 		break;
			 	case CoolantTemprature:
			 		//OBDApp.setglobalcmd(RES_TP);
			 		//setsofttoken();
			 		//softtoken = softtoken + 1;
			 		sendOBDDataCmd(vehiclecmd, RES_TP);
			 		vehiclecmd = OBDcmd.VehicleInfo.DiagnosticStatus;
			 		break;
			 	case DiagnosticStatus:
			 		//OBDApp.setglobalcmd(RES_DTC);
			 		//setsofttoken();
			 		//softtoken = softtoken + 1;
			 		sendOBDDataCmd(vehiclecmd, RES_DTC);
			 		vehiclecmd = OBDcmd.VehicleInfo.DiagnosticCode;
			 		break;
			 	case DiagnosticCode:
			 		//OBDApp.setglobalcmd(RES_DTCs);
			 		//setsofttoken();
			 		//softtoken = softtoken + 1;
			 		sendOBDDataCmd(vehiclecmd, RES_DTCs);
			 		vehiclecmd = OBDcmd.VehicleInfo.ReadFuel;	
			 		break;
			 	case ReadFuel:
		 			sendOBDDataCmd(vehiclecmd, RES_FUEL);
		 			vehiclecmd = OBDcmd.VehicleInfo.EngineRPM;	
		 			break;			 	
			 	default:
			 		//OBDApp.setglobalcmd(0);  
			 		break;
		 }
		}
	}
	
	public synchronized void setsofttoken(){
		softtoken = softtoken + 1;
	}
	
	//Initialize sequence:
	public void InitOBDCmd(int sendtoken){
		//ResetAll:
		appendOBDcmd(SendRSTA,sendtoken);
		sendtoken++ ;
		//Warmreset:
		appendOBDcmd(SendWRST,sendtoken);
		sendtoken++ ;
		//shut off echo off:
		appendOBDcmd(SendECHO,sendtoken);
		sendtoken++ ;
		//shut off return and space 
		appendOBDcmd(SendLFDO,sendtoken);
		sendtoken++ ;
		//shut off message heads 
		appendOBDcmd(SendHDON,sendtoken);
		sendtoken++ ;		
		//set protocol 
		appendOBDcmd(SendSETP,sendtoken);
		sendtoken++ ;
		//get protocol 
		appendOBDcmd(SendCURP,sendtoken);
		sendtoken++ ;
		//get support PID
		appendOBDcmd(SendSUPP,sendtoken);
		sendtoken++ ;		
		//get protocol 
		appendOBDcmd(SendCURP,sendtoken);
		sendtoken++ ;
//		appendOBDcmd(ReadRPM,sendtoken);
	}
	
	
    //get real-time vehicle info by reading vehicle info:	
	public void readOBDData(VehicleInfo vehicleinfo,int sendtoken){
	    switch(vehicleinfo)	{
	    //get engine RPM
	    case EngineRPM:
	    	appendOBDcmd(ReadRPM,sendtoken);
	    	break;
	    //get vehicle speed
	    case VehicleSpeed:
	    	appendOBDcmd(ReadVSS,sendtoken);
	    	break;	
	    //get throttle position
	    case ThrottlePosition:
	    	appendOBDcmd(ReadVSS,sendtoken);
	    	break;
	    //get engine load
	    case EngineLoad:
	    	appendOBDcmd(ReadLOD,sendtoken);
	    	break;
	    //get coolant temperature
	    case CoolantTemprature:
	    	appendOBDcmd(ReadECT,sendtoken);
	    	break;
	    //get fuel consumption
	    case FuelConsumption:
	    	appendOBDcmd(ReadMPG1,sendtoken);
	    	break;
	    //get diagnostic status
	    case DiagnosticStatus:
	    	appendOBDcmd(ReadDTC,sendtoken);
	    	break;
	    //get diagnostic code
	    case DiagnosticCode:
	    	appendOBDcmd(ReadDTCs,sendtoken);
	    	mBluetoothSet.sendMessage(ReadDTCs,sendtoken);
	    	break;	    
	    default:
	    	appendOBDcmd(NOCMD,sendtoken);
	    	break;
	    }		
	}

    //get real-time vehicle group info:	
	public void readGroupInfo(int sendtoken){
	    appendOBDcmd(ReadRPM,sendtoken);
	    if(D) Log.d(TAG, "read rpm");
	    sendtoken++ ;
	    //appendOBDcmd(ReadVSS,sendtoken);
	    //if(D) Log.d(TAG, "read rpm");
	    //++ ;
	    appendOBDcmd(ReadVSS,sendtoken);
	    if(D) Log.d(TAG, "read vss");
	    sendtoken++ ;
	    appendOBDcmd(ReadLOD,sendtoken);
	    if(D) Log.d(TAG, "read load");
	    sendtoken++ ;
	    appendOBDcmd(ReadECT,sendtoken);
	    if(D) Log.d(TAG, "read ect");
	    sendtoken++ ;
	    appendOBDcmd(ReadMPG1,sendtoken);
	    if(D) Log.d(TAG, "read mpg");
	    sendtoken++ ;
	    appendOBDcmd(ReadDTC,sendtoken);
	    if(D) Log.d(TAG, "read dtc");
	    sendtoken++ ;
	    appendOBDcmd(ReadDTCs,sendtoken); 
	    if(D) Log.d(TAG, "read dtcs");
	    sendtoken++ ;
	}	
	
	//set update flags from by setting specified bit and test if all bits are updated;
	public void updateInfoFlag(VehicleInfo vehicleinfo){
		switch(vehicleinfo){
			case EngineRPM:
				updatestatus = updatestatus |  ENGINERPM;
				break;
			case VehicleSpeed:
				updatestatus = updatestatus |  SPEED;
				break; 
			case ThrottlePosition:
				updatestatus = updatestatus |  THROTTLE;
				break; 
			case EngineLoad:
				updatestatus = updatestatus |  LOAD;
				break; 
			case CoolantTemprature:
				updatestatus = updatestatus |  TEMPRATURE;
				break; 
			case FuelConsumption:
				updatestatus = updatestatus |  FUEL;
				break; 
			case DiagnosticStatus:
				updatestatus = updatestatus |  DTCS;
				break; 
			case DiagnosticCode:
				updatestatus = updatestatus |  DTCNUM;
				break;     
			default:
				break;		
		}
		//return testUpdateFlag();
	}
	
	//clear all update bits and prepare for next update cycle;
	public void clearUpdateFlag(){
		updatestatus = 0;
	}
	
	//test if update bits are updated whole.
	public boolean testUpdateFlag(){
		if(updatestatus == 19)
			return true;
		else
			return false;		
	}
		
	//Send OBD control command:
	public void sendOBDControlCmd(InitCmd initcmd, int sendtoken){
		if(mBluetoothSet == null)
			return;
		//boolean ret = false;
		try {
			sendsem.acquire();
			Log.e(TAG,"sem acquire");
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			switch(initcmd)	{
			case ResetAll:
				mBluetoothSet.sendMessage(SendRSTA,sendtoken);
				break;
			case WarmStart:
				mBluetoothSet.sendMessage(SendWRST,sendtoken);
				break;	 
			case EchoOff:
				mBluetoothSet.sendMessage(SendECHO,sendtoken);
				break;	
			case LineFeeds:
				mBluetoothSet.sendMessage(SendLFDO,sendtoken);
				break;		    	
			case HeadsOn:
				mBluetoothSet.sendMessage(SendHDON,sendtoken);
				break;	
			case VerionInfo:
				mBluetoothSet.sendMessage(SendVINF,sendtoken);
				break;	
			case DisplayDriver:
				mBluetoothSet.sendMessage(SendDISP,sendtoken);
				break;	
			case ReadVolt:
				mBluetoothSet.sendMessage(SendVOLT,sendtoken);
				break;		    	
			case SetProtocol:
				mBluetoothSet.sendMessage(SendSETP,sendtoken);
				break;	
			case CurrentProtocol:
				mBluetoothSet.sendMessage(SendCURP,sendtoken);
				break;		
			case SupportCmd:
				mBluetoothSet.sendMessage(SendSUPP,sendtoken);
				break;
			case ClearDTC:
				mBluetoothSet.sendMessage(CLRDTCs,sendtoken);
				break;			    	
			default:
				mBluetoothSet.sendMessage(NOCMD,sendtoken);
				sendsem.release();
				break;
			}
	}
	//Send OBD data command:	
	public void sendOBDDataCmd(VehicleInfo vehicleinfo,int sendtoken){
		if(mBluetoothSet == null)
			return;
		//boolean ret = false;
		//ret = sendsem.tryAcquire();
		try {
			sendsem.acquire();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//if(ret){
		    switch(vehicleinfo)	{
		    case EngineRPM:
		    	mBluetoothSet.sendMessage(ReadRPM,sendtoken);
		    	break;
		    case VehicleSpeed:
		    	mBluetoothSet.sendMessage(ReadVSS,sendtoken);
		    	break;	 
		    case ThrottlePosition:
		    	mBluetoothSet.sendMessage(ReadTP,sendtoken);
		    	break;
		    case EngineLoad:
		    	mBluetoothSet.sendMessage(ReadLOD,sendtoken);
		    	break;
		    case CoolantTemprature:
		    	mBluetoothSet.sendMessage(ReadECT,sendtoken);
		    	break;
		    case FuelConsumption:
		    	mBluetoothSet.sendMessage(ReadMPG1,sendtoken);
		    	break;
		    case DiagnosticStatus:
		    	mBluetoothSet.sendMessage(ReadDTC,sendtoken);
		    	break;
		    case DiagnosticCode:
		    	mBluetoothSet.sendMessage(ReadDTCs,sendtoken);
		    	break;
		    case ReadFuel:
		    	mBluetoothSet.sendMessage(ReadFUELS,sendtoken);
		    	break;
		    default:
		    	mBluetoothSet.sendMessage(NOCMD,sendtoken);
		    	sendsem.release();
		    	break;
		    }
		//}
	}
	
	//This function is for extending OBD command besides defined OBD command in this file;
	//Generating OBD command automatically by service number and pid
	public String GenerateOBDcmd(String sernr, String pid){
		obdcommand = "";
		servicenr = sernr;
		if(servicenr == null || pidnr == null )
			return "null";
		if(!servicenr.equalsIgnoreCase("") && (Integer.valueOf(servicenr)>0) && (Integer.valueOf(servicenr)<9))
			obdcommand = servicenr;
		else
			if(D) Log.d(TAG, "Service id of OBD command is error!");
		pidnr = pid;
		if(!pidnr.equalsIgnoreCase("") && (Integer.valueOf(pidnr)>0))
			obdcommand = obdcommand + pidnr;
		else
			if(D) Log.d(TAG, " Pid of OBD command is null!");		
		obdcommand = obdcommand +"\r\n";
		return obdcommand;					
	}
	
		
	//Parse RPM data 
	//OBD response data in the format: 41 0C XX XX
	//XX XX are hex, every bit represents 1/4rpm 
	public void parseRPM(String RPM){
		String strArray[] = null;
		int valuerpm = 700;
	    strRPM = "";
	    enginerpm = 0;
	    if(RPM == null)
	    	return;
	    strRPM = RPM;
        strArray = strRPM.split(" ");
        synchronized(this){
        	resToken = Integer.valueOf(strArray[0]);	
        }
        String thirdbyterpm = strArray[3];
        String fourthbyterpm = strArray[4];
        String combinestring = thirdbyterpm + fourthbyterpm;
        if(combinestring != null){
        	valuerpm = Integer.parseInt(combinestring, 16);
        	enginerpm = (int)(valuerpm/4);
        }
	}
	//Get RPM data
	public int getRPM(){
		return enginerpm;
	}
	
	//Parse VSS data 
	//OBD response data in the format: 41 0D XX 
	//XX are hex, every bit represents 1km/h	
	public void parseVSS(String VSS){
		String strArray[] = null;
		String thirdbytevss = null;
	    strVSS = "";
	    vehiclespeed = 0;
	    if(VSS == null)
	    	return;
	    strVSS = VSS;		
        strArray = strVSS.split(" ");
        synchronized(this){
        resToken = Integer.valueOf(strArray[0]);
        }        
        thirdbytevss = strArray[3];
        if(thirdbytevss != null){
        	vehiclespeed = Integer.parseInt(thirdbytevss, 16);	
        }
	}
	//Get VSS data
	public int getVSS(){
		return vehiclespeed;
	}	
	
	//Parse TP data 
	//OBD response data in the format: 41 11 XX 
	//XX are hex, every bit represents 100/255%	
	public void parseTP(String TP){
		String strArray[] = null;
		String thirdbytetp = null;
	    strTP = "";
	    throttleposition = 0;
	    if(TP == null)
	    	return;	    
	    strTP = TP;		
        strArray = strTP.split(" ");
        synchronized(this){
        resToken = Integer.valueOf(strArray[0]);
        }        
        thirdbytetp = strArray[3];
        if(thirdbytetp != null){
        	throttleposition =(int)Integer.parseInt(thirdbytetp, 16)*100/255;	
        }
	}
	//Get TP data
	public int getTP(){
		return throttleposition;
	}		
	
	//Parse LOD data 
	//OBD response data in the format: 41 04 XX 
	//XX are hex, every bit represents 100/255%	
	public void parseLOD(String LOD){
		String strArray[] = null; 
		String thirdbyteload = null;
	    strLOD = "";
	    engineload = 0;
	    if(LOD == null)
	    	return;	 
	    strLOD = LOD;		
        strArray = strLOD.split(" ");
        synchronized(this){
        resToken = Integer.valueOf(strArray[0]);
        }        
        thirdbyteload = strArray[3];
        if(thirdbyteload != null){
        	engineload = (int)Integer.parseInt(thirdbyteload, 16)*100/255;	
        }
	}
	//Get LOD data
	public int getLOD(){
		return engineload;
	}	
	
	//Parse ECT data 
	//OBD response data in the format: 41 05 XX 
	//XX are hex, every bit represents 1 degree with -40 degree offset
	//temperature range is form -40 celsiur degree to 215 celsiur degree;
	public void parseECT(String ECT){
		String strArray[] = null;
		String thirdbytetemp = null;
	    strECT = "";
	    enginetemp = 0;
	    if(ECT == null)
	    	return;	 
	    strECT = ECT;		
        strArray = strECT.split(" ");
        synchronized(this){
        resToken = Integer.valueOf(strArray[0]);
        }        
        thirdbytetemp = strArray[3];
        if(thirdbytetemp != null){
        	enginetemp = Integer.parseInt(thirdbytetemp, 16)-40;
        }
	}
	//Get ECT data
	public int getECT(){
		return enginetemp;
	}		
	
	
	
	//Parse MPG data 
	//OBD response data in the format: 41 10 XX XX 
	//XX XX are hex, every bit represents 0.01g/s = 0.01*3.785L/s
	//Max range means 655.35gallon/s
	public void parseMPG(String MPG){
		String strArray[] = null;
		String combinempg = null;
	    strMPG = "";
	    fuelconsump = 0;
	    if(MPG == null)
	    	return;
	    strMPG = MPG;		
        strArray = strMPG.split(" ");
        synchronized(this){
        resToken = Integer.valueOf(strArray[0]);
        }
        String thirdbytempg = strArray[3];
        String fourthbytempg = strArray[4];
        combinempg = thirdbytempg + fourthbytempg;
        if(combinempg != null){
        	fuelconsump = (double)Integer.parseInt(combinempg, 16)/100;	
        }
	}
	
    //For another version to parse fuel consumption:
	public void parseMPG1(String MPG){
		String strArray[] = null;
		String thirdbytempg = null;
	    strMPG = "";
	    fuelconsump = 0;
	    if(MPG == null)
	    	return;	    
	    strMPG = MPG;		
        strArray = strMPG.split(" ");
        synchronized(this){
        resToken = Integer.valueOf(strArray[0]);
        }
        thirdbytempg = strArray[3];
        if(thirdbytempg != null){
        	fuelconsump = (double)Integer.parseInt(thirdbytempg, 16)/14;
        }
	}
	//Get MPG data
	public double getMPG(){
		return fuelconsump;
	}
	
	//Parse DTC status data 
	//OBD response data in the format: 41 01 XX .. .. .. 
	//XX are hex, XX is larger than 0x80, it means that at least one DTCs existing
	//the number of DTC is 0x80&0xXX;
	public void parseDTC(String DTC){
		String strArray[] = null;
		String thirdbytedtc = null;
		int temp = 0;
	    strDTC = "";
	    dtcstatus = 0;
	    if(DTC == null)
	    	return;	    
	    strDTC = DTC;		
        strArray = strDTC.split(" ");
        synchronized(this){
        resToken = Integer.valueOf(strArray[0]);
        }
        thirdbytedtc = strArray[3];
        if(thirdbytedtc != null){
        	temp = Integer.parseInt(thirdbytedtc, 16);
        //add for debugging:
        //temp = 131;
        //make the DTC code exists;
        	if(temp <=128){
        		dtcstatus = temp - 128;
        	}
        }
	}
	//Get DTC status data
	public synchronized int getDTC(){
		return dtcstatus;
	}	
	
	//Parse DTC code data 
	//OBD response data in the format: 43 XX XX XX XX XX XX or 43 XX
	//XX are hex, every two XX represents a DTC code;
	//For every DTC code, the first XX byte represents the different fault type;
	public void parseDTCs(){
//		String strArray[] = null;
	    strDTCs = "";
	    dtccode = "";
	    //strDTCs = DTCs;
	    String temp = "";
	    if(strDTCs != null && !strDTCs.equals("")){
	    	String strArray[] = strDTCs.split(" ");
	    	synchronized(this)
	    	{
	    		resToken = Integer.valueOf(strArray[0]);
	    	}
	    	if(strArray.length <= 2)
	    	{
	    		if(D) Log.d(TAG, "no DTC code");           
	    		return;
	    	}
	    	String secondbytedtcs  = strArray[2];
	    	String thirdbytedtcs   = strArray[3];
	    	String dtctype = dtctypetemp(secondbytedtcs);
	    	if(!dtctype.equalsIgnoreCase("ERROR"))
	    	{
	    		temp = dtctype + thirdbytedtcs;
	    	}
	    	else
	    		Log.e(TAG, "DTC code type error!");
	    	if(strArray.length > 4 && strArray.length < 7){
	    		String fourthbytedtcs  = strArray[4];
	    		String fifthbytedtcs   = strArray[5];
	    		dtctype = dtctypetemp(fourthbytedtcs);
	    		if(!dtctype.equalsIgnoreCase("ERROR"))
	    		{
	    			temp = temp + " " + dtctype + fifthbytedtcs;
	    		}
	    		else
	    			Log.e(TAG, "DTC code type error!"); 
	    	}
	    	if(strArray.length > 6 && strArray.length < 9){
	    		String sixthbytedtcs   = strArray[6];
	    		String seventhbytedtcs = strArray[7]; 
	    		dtctype = dtctypetemp(sixthbytedtcs);
	    		if(!dtctype.equalsIgnoreCase("ERROR"))
	    		{
	    			temp = temp + " " + dtctype + seventhbytedtcs;
	    		}
	    		else
	    			Log.e(TAG, "DTC code type error!");  
	    	}
	    	if(strArray.length >= 9){
	    		Log.e(TAG, "DTC code length error!");  
	    	}
	    }
	    setDtccode(temp);
	    compareDTCs();
	}
	
	public synchronized void setDtccode(String code){
		if(code == null){
			dtccode = "";
			return;
		}
		dtccode = code;
		//add for debugging
	    //dtccode = /*"P0369" + " " +"P1504" +* " " +*/"C0129" + " " +"P0720" ;
	}
	
	public synchronized void setStrDTCs(String code){
		if(code == null){
			dtccode = "";
			return;			
		}
		strDTCs = code;
	}	

	//Judge if DTC code is needed to be updated; 
	public boolean compareDTCs(){
		int ret = 0, i = 0;
		boolean bret = false;
		if(dtccode == null || dtchistory == null)
			return false;
		bret = dtccode.contentEquals("");
		if(bret){
			ret = 0;
			return false;
		}
		ret = dtccode.compareToIgnoreCase(dtchistory);
		if(D) Log.d(TAG, "dtccode current is " + dtccode); 
		if(D) Log.d(TAG, "dtccode history is " + dtchistory); 
		if(ret != 0){
			Log.d(TAG, "DTC code is updated!");  
			while(i < DTCHistory.length && DTCHistory[i] != null){
				i++;
			}
			if(i <= DTCnum){
				if(dtchistory.equalsIgnoreCase(""))
					dtchistory = dtccode;	
			}
			DTCHistory[i] = dtchistory;
			dtchistory = dtccode;
			appendDTCcodes(dtccode);
			setupdateFlag(true);
			return true;
		}
		else{
			Log.d(TAG, "DTC code is not needed to be updated!");  
			setupdateFlag(false);
			return false;
		}	
	}
	
	//This function is used to get the status of the dtcqueue;
	public synchronized boolean getUpdateFlag(){
		return dtccupdateflag;
	}
	
	public synchronized void setupdateFlag(boolean flag){
		Log.d(TAG, "dtccupdateflag is" + flag);  
		dtccupdateflag = flag;
	}
	
	
	//This function will append DTC code into dtc queue;
	//and append DTC explaination into summary queue;
	public void appendDTCcodes(String DTCcode){
		if(DTCcode == null){
			Log.e(TAG, "AddDtc appendDTCcodes return 1");
			return;
		}
			
		boolean ret = true;
		String ReceiveBuffer = DTCcode;
		String dtcpath;
		//int i = 0 , dtcnum = 0, dtccodenume = 0;
		//String dtcexplain = "";
		//DTCindex.DTCCodeType mdtctype;
		//String strArray[] = ReceiveBuffer.split(" ");
		/*
		String strArray[] = new String[5];
		if(ReceiveBuffer.length() == 5)
		{
			strArray[0] = ReceiveBuffer.substring(0, 4);
			
			dtcnum = 1;
		}
		else if(ReceiveBuffer.length() == 11)
		{
			strArray[0] = ReceiveBuffer.substring(0, 4);
			strArray[1] = ReceiveBuffer.substring(6, 11);
			dtcnum = 2;
		}
		else if(ReceiveBuffer.length() == 17)
		{
			strArray[0] = ReceiveBuffer.substring(0, 4);
			strArray[1] = ReceiveBuffer.substring(6, 11);
			strArray[1] = ReceiveBuffer.substring(12, 17);
			dtcnum = 3;
		}
		else{
			dtcnum = 0;
			if(D) Log.d(TAG, "DTC more than three");
		}
		*/	

		if(mDTCindex == null){
			Log.e(TAG, "AddDtc appendDTCcodes return 2");
			return;
		}
		dtcpath = mDTCindex.makeDirectory();
		ret = mDTCindex.readDTCsFromHashMap(ReceiveBuffer);
		if(!ret)
			ret = mDTCindex.readMutipleDtcsXML(dtcpath,ReceiveBuffer);//readMutipleDtcs
		if(!ret)
			if(D) Log.e(TAG, "Read DTC error!"); 
		//String strDTCs[] = ReceiveBuffer.split(" ");
		//String strExplain[] = dtcexplain.split("+");
		//if(dtcqueue == null || summaryqueue == null)
		//	return;
		
		//for(i = 0; i < dtcnum; i++){
		//for(i = 0; i < strDTCs.length; i++){
			//dtcqueue.offer(strDTCs[i]);
			//mdtctype = mDTCindex.JudgeType(strArray[i]);
			//dtccodenume = mDTCindex.ParseDTC(strArray[i]);
			//dtcpath = mDTCindex.makeDirectory();
			//dtcsummary = mDTCindex.readexcel(dtcpath,0,dtccodenume,false,mdtctype);
			//summaryqueue.offer(strExplain[i]);
			//Log.d(TAG, "append DTC code is" + strDTCs[i]); 
			//if(D) Log.d(TAG, "DTC summary is" + dtcsummary); 
		//}		
	}
	
	//Fetch dtc code and dtc summary from queue;
    //The below two function must be used at the same time;	
	
	
	public synchronized void setcleanFlags(boolean flag){
		dtccleanflag = flag;
	}
	
	public void cleanDTCs(){
		if(dtccleanflag)
			dtccode = "";
	}
	
	
	//Select detail DTC code type according to the first DTC byte;
	public String dtctypetemp(String dtcfirstbyte){
		String dtctypetemp = "";
		int num = Integer.parseInt(dtcfirstbyte.substring(0, 1), 16);
		switch(num){
		case 0:
			dtctypetemp = "P0";
			break;
		case 1:
			dtctypetemp = "P1";
			break;			
		case 2:
			dtctypetemp = "P2";
			break;	
		case 3:
			dtctypetemp = "P3";
			break;	
		case 4:
			dtctypetemp = "C0";
			break;				
		case 5:
			dtctypetemp = "C1";
			break;				
		case 6:
			dtctypetemp = "C2";
			break;				
		case 7:
			dtctypetemp = "C3";
			break;	
		case 8:
			dtctypetemp = "B0";
			break;				
		case 9:
			dtctypetemp = "B1";
			break;				
		case 10:
			dtctypetemp = "B2";
			break;	
		case 11:
			dtctypetemp = "B3";
			break;				
		case 12:
			dtctypetemp = "U0";
			break;				
		case 13:
			dtctypetemp = "U1";
			break;				
		case 14:
			dtctypetemp = "U2";
			break;				
		case 15:
			dtctypetemp = "U3";
			break;				
		default:
			dtctypetemp = "ERROR";
			break;			
		}
		dtctypetemp = dtctypetemp + dtcfirstbyte.substring(1, 2);
		return dtctypetemp;
	}	
	//Get DTCs code data
	public String getDTCs(){
		return dtccode;
	}
	
	//Simulator specified DTCs;
	public String generateDTC(String type,String fristbyte, String secondbyte){
		String simulatordtc = type + fristbyte + secondbyte + ";";
		return simulatordtc;
	}
	
	//Parse OBD protocol version
	public void parsePVER(String PVER){
		String strArray[] = null;
		if(PVER == null)
			return;
        strArray = PVER.split(" ");
        if(strArray[0] == null)
        	return;
        synchronized(this){
        resToken = Integer.valueOf(strArray[0]);
        }
        int i = 0;
        protocolver = "";
        for(i = 1;i < strArray.length; i++)
        	protocolver = protocolver + " " + strArray[i];	
	}
	//Get OBD protocol version
	public String getPVER(){
		return protocolver;
	}
	
	public void parseFUEL(String FUEL){
		String strArray[] = null;
		String thirdbytetemp = null;
		strFUEL = "";
	    if(FUEL == null)
	    	return;	 
	    strFUEL = FUEL;	
	    if(strFUEL.contains(" ")){
	    	strArray = strFUEL.split(" ");
	    }
        synchronized(this){
        resToken = Integer.valueOf(strArray[0]);
        }  
        if(strArray.length >= 4){
	        thirdbytetemp = strArray[3];
	        if(thirdbytetemp != null){
	        	//percentfuel = Integer.parseInt(thirdbytetemp, 16);
	        	percentfuel = Integer.parseInt(thirdbytetemp, 10);
	        }
	        if(thirdbytetemp == null){
	        	percentfuel = Integer.parseInt("0", 10);
	        }
        }
	}
	
	//Parse battery voltage
	public void parseVLOT(String VOLT){
		String strArray[] = null;
		if(VOLT == null)
			return;
        strArray = VOLT.split(" ");
        //synchronized(this){
        //resToken = Integer.valueOf(strArray[0]);
        //}		
        obdvoltage = strArray[1];
        obdvoltage = obdvoltage + "V";
	}
	//Get battery voltage
	public String getVOLT(){
		return obdvoltage;
	}
	
	//Parse OBD hardware version
	public void parseHVER(String HVER){
		String strArray[] = null;
		if(HVER == null)
			return;
        strArray = HVER.split(" ");
        synchronized(this){
        resToken = Integer.valueOf(strArray[0]);
        }			
        hardwarever = strArray[1] + " " + strArray[2];
	}
	//Get OBD hardware version
	public String getHVER(){
		return hardwarever;
	}
	
	//Read real-time vehicle data interface by synchronize way:
	//These interfaces are invoked by activity;
	public static synchronized int readRPM(){
		return enginerpm;
	}
	
	public static synchronized int readVSS(){
		return vehiclespeed;
	}
	
	public static synchronized int readTP(){
		return throttleposition;
	}
	
	public static synchronized int readLoad(){
		return engineload;
	}
	
	public static synchronized int readTemp(){
		return enginetemp;
	}
	
	public static synchronized int readDTCStatus(){
		return dtcstatus;
	}
	
	public static synchronized String readDTCs(){
		return dtccode;
	}
	
	public void updateDTCs(){
		
	}
	
	//OBD message handler
	//This handler should be defined by high level service;
	//And high level service should set its handler into datahandler of bluetoothSet construct function;
	/*
	private final Handler mHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			Bundle bundle = msg.getData();
			//String strArray[] = null;
			switch (msg.what) {
			case RESPONSE_OK:
					Toast.makeText(mContext,"response ok", Toast.LENGTH_SHORT).show();
					break;
			case RESPONSE_DATA:
				break;
			case RESPONSE_CONT:
				break;	
			case RES_RPM:
				String sRPM = bundle.getString("sRES_RPM");
				parseRPM(sRPM);
				break;
			case RES_VSS:
			    String sVSS = bundle.getString("sRES_VSS");
			    parseVSS(sVSS);
			    break;				
			case RES_TP:
			    String sTP = bundle.getString("sRES_TP");
			    parseTP(sTP);
			    break;	
			case RES_LOD:
			    String sLOD = bundle.getString("sRES_LOD");
			    parseLOD(sLOD);
			    break;
			case RES_ECT:
			    String sECT = bundle.getString("sRES_ECT");
			    parseECT(sECT);
			    break;				
			case RES_MPG:
			    String sMPG = bundle.getString("sRES_MPG");
			    parseMPG(sMPG);
			    break;
			case RES_DTC:
			    String sDTC = bundle.getString("sRES_DTC");
			    parseDTC(sDTC);
			    break;
			case RES_DTCs:
			    String sDTCs = bundle.getString("sRES_DTCs");
		        parseDTCs(sDTCs);
			    break;
			case RES_PVER:    
			    String sPVER = bundle.getString("sRES_PVER");
		        parsePVER(sPVER);
			    break;	
			case RES_VOLT:
			    String sVOLT = bundle.getString("sRES_VOLT");
			    parseVLOT(sVOLT);
			    break;
			case RES_HVER:
			    String sHVER = bundle.getString("sRES_HVER");
		        parseHVER(sHVER);
			    break;							    
			default:
				Log.d(TAG, "DTC response message without right type!");   
				break;
			}
		}		
	};*/
	

	//Get obd control response data from outputstream and try to recognize which type it belongs to;
	public static void getCmdData(String data, int token, Handler datahandler){
		Handler cmdHandler = tempHandler;//datahandler;
		if(cmdHandler == null)
			return;
		String strToken = String.valueOf(token);
		if(data == null || data.equalsIgnoreCase("")){
			if(D) Log.d(TAG, "CMD being parsed is null!");  
			return;
		}
		if(!data.contains("\r"))
			return;
		String ReceiveBuffer = data;
		String sResponseType = null;
		//System.out.println("===Parse Control cmd====");
		//System.out.println(ReceiveBuffer);
		String strArray[] = ReceiveBuffer.split("\r");		
		//System.out.println(strArray[0]);
		
		Message msg = null;
		if(ReceiveBuffer.equalsIgnoreCase("OK")){
			msg = cmdHandler.obtainMessage(RESPONSE_CONT);
			sResponseType = "NODATA";												
		}						
		else if(ReceiveBuffer.startsWith("EST") || ReceiveBuffer.contains("1.5") ){
			msg = cmdHandler.obtainMessage(RES_HVER);
			sResponseType = "sRES_HVER";												
		}
		else if(ReceiveBuffer.contains("V") || ReceiveBuffer.contains(".")){	
			int result = strArray[0].length();
			if(result > 3 ){
				msg = cmdHandler.obtainMessage(RES_VOLT);
				sResponseType = "sRES_VOLT";
			}
		}	
		else if(ReceiveBuffer.contains("ISO")){
			msg = cmdHandler.obtainMessage(RES_PVER);
			sResponseType = "sRES_PVER";				
		}
		else if(ReceiveBuffer.contains("AUTO")){
			msg = cmdHandler.obtainMessage(RESPONSE_CONT);
			sResponseType = "NODATA";												
		}
		else 
		{
			msg = cmdHandler.obtainMessage(RES_EXCEP);
			sResponseType = "NODATA";	
			Log.d(TAG, "OBD control cmd is not in range!");  
		}
		
		if(sResponseType == null || strArray[0] == null)
			return;
	    //Pack these data into a bundle;
		ReceiveBuffer = strToken + " " + strArray[0];
		Bundle bundle = new Bundle();
		bundle.putString(sResponseType, ReceiveBuffer);
		msg.setData(bundle);
		cmdHandler.sendMessage(msg);
	}	
	
	

	//Get vehicle data from outputstream and try to recognize which type it belongs to;
	public static void getCarData(String data, int token, Handler datahandler){
		Handler cardataHandler = tempHandler;//datahandler;
		if(cardataHandler == null)
			return;
		String strToken = String.valueOf(token);
		if(data.equalsIgnoreCase("") || data == null){
			if(D) Log.d(TAG, "Data being parsed is null!");  
			return;
		}
		if(data.length() < 3 || data.charAt(2) != ' ' || (!data.contains(" ")))
		{
			Log.d(TAG, "Data is illegal");
			return;
		}
		String ReceiveBuffer = data;
		String sResponseType = null;
		String strArray[] = ReceiveBuffer.split("\r");
		//System.out.println("===Parse vehicle data====");
		//System.out.println(strArray[0]);
		String byteArray[] = strArray[0].split(" ");

		Message msg = null;
		if(byteArray[0] != null){
			if(!byteArray[0].matches("[0-9a-fA-f]+"))
				return;
		}
		/*if(byteArray[1] != null){
			if(!byteArray[1].matches("[0-9a-fA-f]+"))
				return;
		}*/
		int secondbyte = Integer.parseInt(byteArray[1], 16);		
		if(byteArray[0].equalsIgnoreCase(RES_SERVICE01)){
			switch(secondbyte){
			case DTC:
				msg = cardataHandler.obtainMessage(RES_DTC);   
				sResponseType = "sRES_DTC";
				break;							
			case RPM:
				msg = cardataHandler.obtainMessage(RES_RPM);   
				sResponseType = "sRES_RPM";
				break;
			case VSS:
				msg = cardataHandler.obtainMessage(RES_VSS);
				sResponseType = "sRES_VSS";
				break;
			case TP:
				msg = cardataHandler.obtainMessage(RES_TP);
				sResponseType = "sRES_TP";
				break;
			case LOD:
				msg = cardataHandler.obtainMessage(RES_LOD);
				sResponseType = "sRES_LOD";
				break;
			case ECT:
				msg = cardataHandler.obtainMessage(RES_ECT);
				sResponseType = "sRES_ECT";
				break;
			case MPG:
				msg = cardataHandler.obtainMessage(RES_MPG);
				sResponseType = "sRES_MPG";
				break;
			case FUELS:
				msg = cardataHandler.obtainMessage(RES_FUEL);
				sResponseType = "sRES_FUELS";				
				break;				
			default:
				msg = cardataHandler.obtainMessage(RESPONSE_OK);
				sResponseType = "sNODATA";
				break;
			}				
		}
		else if(byteArray[0].equalsIgnoreCase(RES_SERVICE02)){
			switch(secondbyte){
			case 0:
				msg = cardataHandler.obtainMessage(RESPONSE_DATA);
				sResponseType = "NODATA";
				break;
			default:
				break;
			}				
		}
		else if(byteArray[0].equalsIgnoreCase(RES_SERVICE03)){
			msg = cardataHandler.obtainMessage(RES_DTCs);
			sResponseType = "sDTCs";
		}
		else if(byteArray[0].equalsIgnoreCase(RES_SERVICE04)){
			msg = cardataHandler.obtainMessage(RESPONSE_CONT);
			sResponseType = "NODATA";
			if(D) Log.d(TAG, "All DTC code will be cleared");  
		}
		else
		{
			msg = cardataHandler.obtainMessage(RES_EXCEP);
			sResponseType = "NODATA";	
			if(D) Log.d(TAG, "OBD control cmd is not in range!");   
		}
			
		if(sResponseType == null || strArray[0] == null)
			return;
	    //Pack these data into a bundle;
		ReceiveBuffer = strToken + " " + strArray[0];
		Bundle bundle = new Bundle();
		bundle.putString(sResponseType, ReceiveBuffer);
		msg.setData(bundle);
		cardataHandler.sendMessage(msg);

	}
	
		
	//message handler for dealing with obd response data:
	//Notice: this handler will be implemented by high level service or activity;
	//OBD message handler
	//This handler should be defined by high level service;
	//And high level service should set its handler into datahandler of bluetoothSet construct function;
	
	public final Handler memHandler = new Handler(){
		int dtccodestatus = 0;
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			Bundle bundle = msg.getData();
			VehicleInfo vehicleinfo;
			//String strArray[] = null;
			switch (msg.what) {
			case OBDcmd.RESPONSE_OK:
//					Toast.makeText(this,"response ok", Toast.LENGTH_SHORT).show();
					break;
			case OBDcmd.RESPONSE_DATA:
				break;
			case OBDcmd.RESPONSE_CONT:
				break;	
			case OBDcmd.RES_RPM:
				String sRPM = bundle.getString("sRES_RPM");
				parseRPM(sRPM);
				vehicleinfo = VehicleInfo.EngineRPM;
				updateInfoFlag(vehicleinfo);
			    //initstatus = 2;
				//if(D) Log.d(TAG, "get rpm");
				break;
			case OBDcmd.RES_VSS:
			    String sVSS = bundle.getString("sRES_VSS");
			    parseVSS(sVSS);
				vehicleinfo = VehicleInfo.VehicleSpeed;
				updateInfoFlag(vehicleinfo);
				initstatus = 1;
				//if(D) Log.d(TAG, "get vss");
			    break;				
			case OBDcmd.RES_TP:
			    String sTP = bundle.getString("sRES_TP");
			    parseTP(sTP);
				vehicleinfo = VehicleInfo.ThrottlePosition;
				//updateInfoFlag(vehicleinfo);
				if(D) Log.d(TAG, "get tp");
			    break;	
			case OBDcmd.RES_LOD:
			    String sLOD = bundle.getString("sRES_LOD");			    
			    parseLOD(sLOD);
				vehicleinfo = VehicleInfo.EngineLoad;
				//updateInfoFlag(vehicleinfo);
				if(D) Log.d(TAG, "get load");
			    break;
			case OBDcmd.RES_ECT:
			    String sECT = bundle.getString("sRES_ECT");
			    parseECT(sECT);
				vehicleinfo = VehicleInfo.CoolantTemprature;
				updateInfoFlag(vehicleinfo);	
				if(D) Log.d(TAG, "get ect");
			    break;				
			case OBDcmd.RES_MPG:
			    String sMPG = bundle.getString("sRES_MPG");
			    parseMPG1(sMPG);
				vehicleinfo = VehicleInfo.FuelConsumption;
				//updateInfoFlag(vehicleinfo);
				if(D) Log.d(TAG, "get mpg");
 			    break;
			case OBDcmd.RES_DTC:
			    String sDTC = bundle.getString("sRES_DTC");
			    parseDTC(sDTC);
				//vehicleinfo = VehicleInfo.DiagnosticStatus;
				//updateInfoFlag(vehicleinfo);
				//if(D) Log.d(TAG, "get dtc");
			    break;
			case OBDcmd.RES_DTCs:
			    String sDTCs = bundle.getString("sRES_DTCs");
			    dtccodestatus = readDTCStatus();
			    if(dtccodestatus >= 0){
			    	//getdtcstatus = true;
			    	setStrDTCs(sDTCs);
			    	//wuzhr test
			    	//new Thread(new GetDTCs()).start();
		    		parseDTCs();		    				    		
		    		if(mHandler != null)
		    			mHandler.obtainMessage(DTCEXIST).sendToTarget();
			    	//parseDTCs();
		    		dtccodestatus = 0;
			    }
			    else{
		    		if(mHandler != null)
		    			mHandler.obtainMessage(DTCNOEXIST).sendToTarget();			    	
			    }
				vehicleinfo = VehicleInfo.DiagnosticCode;
				//updateInfoFlag(vehicleinfo);
				if(D) Log.d(TAG, "parse dtcs+");
				//System.out.print(updatestatus);
			    break;
			case OBDcmd.RES_FUEL:
			    String sFUELS = bundle.getString("sRES_FUELS");
			    parseFUEL(sFUELS);
				vehicleinfo = VehicleInfo.ReadFuel;
			    break;			    
			case OBDcmd.RES_PVER:    
			    String sPVER = bundle.getString("sRES_PVER");
			    parsePVER(sPVER);
			    break;	
			case OBDcmd.RES_VOLT:
			    String sVOLT = bundle.getString("sRES_VOLT");
			    parseVLOT(sVOLT);
	    		if(mHandler != null)
	    			mHandler.obtainMessage(VOLTVALUE).sendToTarget();
			    break;
			case OBDcmd.RES_HVER:
			    String sHVER = bundle.getString("sRES_HVER");
			    parseHVER(sHVER);
			    break;	
			case OBDcmd.RES_EXCEP:
				break;
			default:
				break;
			}
		}		
	};
	
    /**
	 * This thread monitors the bluetooth service connecting state 
	 * and do the initialization and read data cycle;
     */
    class GetDTCs extends Thread{    	
	    public synchronized void run() {
	    	
	    	
	    	try {
	    		parseDTCs();
	    		sleep(1000);
	    		Log.d(TAG, "parse DTCs");
	    		if(mHandler != null)
	    			mHandler.obtainMessage(DTCEXIST).sendToTarget();
	    		//getdtcstatus = false;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
	    	
	   
	    }
    }
	
	
	
    /**
	 * This thread monitors the bluetooth service connecting state 
	 * and do the initialization and read data cycle;
     */
    public class ReadThread extends Thread {
        int firsttime = 0;
        int connectstate = 0;
        int connecttemp = 0;
        int tokencount = 0;
        boolean readonce = true;
        //int test = 0;
        public ReadThread() {
            	if(D) Log.d(TAG, "Read thread cycle starts\n");
            	initstatus = 0;
 //           	connecttemp = mBluetoothSet.ConnectionState(); 
            }
        

        public void run() {
                while(mBluetoothSet != null && readstate){
    		    //send BT connection state to high level activity:
    		    //CONNECTSTATE = mBluetoothSet.ConnectionState();       	
    			//Message msg = mHandler.obtainMessage(CONNECTSTATE);
    			//mHandler.sendMessage(msg);        	
        	    //test if OBD is ready for being read, if yes, start to read cycle in every 0.5s;
                	//if(D) Log.d(TAG, "read thread start running now!\n"); 
                	//mHandler.obtainMessage(STATECHANGE, connecttemp, connectstate).sendToTarget();                	
                	if(test_dtc_send == 0 ){                		
                		//DtcService.context.loadDtcIntoList = true;
                		//appendDTCcodes("P1504 P0720 C0129");
                		//DtcService.context.loadDtcIntoList = false;
                		test_dtc_send = 1;
               			try {
    						sleep(1000);
    					} catch (InterruptedException e) {
    						// TODO Auto-generated catch blocks
    						e.printStackTrace();
    					}
               			//LogRecord.UploadLogFiles(Base.ycblog,BluetoothService.testURL);
               			//LogRecord.UploadLogFiles(CrashHandler.crashDirectory,BluetoothService.testURL);
                		//mHandler.obtainMessage(DTCNOEXIST).sendToTarget();
               			//mHandler.obtainMessage(VSUPPORT).sendToTarget();
               			//percentfuel = 30;
               			//enginerpm = 40;
               			//enginetemp = 50;
                    	//mHandler.obtainMessage(READY).sendToTarget();

//						appendDTCcodes("C0129");
//						mHandler.obtainMessage(DTCEXIST).sendToTarget();

                	} 
                	
                	//percentfuel = 30;
                	//mHandler.obtainMessage(READY).sendToTarget();

                	
    		        if(connectstate != connecttemp){
            			//send message to high level application/activity to notify that connecting state is changed;
            			try {
    						sleep(50);
    					} catch (InterruptedException e) {
    						// TODO Auto-generated catch blocks
    						e.printStackTrace();
    					}
            			if(mHandler != null)
            				mHandler.obtainMessage(STATECHANGE, connecttemp, connectstate).sendToTarget();
						connectstate = connecttemp;   
						if(D) Log.d(TAG, "BTconnection current is \n" + connecttemp);
						if(connectstate == BluetoothService.STATE_NONE || connectstate == BluetoothService.STATE_LISTEN){
							firsttime = 0;
							tokencount = 0;
							initstatus = 0;
							softtoken = 0;
							sendsem.release();
						}
						if(connectstate == BluetoothService.STATE_CONNECTED){
	               			timeoutthread = new TimeoutThread(0,50);
	               			sendrecethread = new SendReceCycle(0,25);
						}
						//parseDTCs("");
						//add for debugging:
//						if(connectstate == 1){
//							appendDTCcodes("P1504 P0720 C0129");
//							mHandler.obtainMessage(DTCEXIST).sendToTarget();
//						}
						//if(D) 
						//	Log.d(TAG, "append P1504 + P0720\n");
						/*
						setupdateFlag(true);
						Message msg1 = mHandler.obtainMessage(READY);
						mHandler.sendMessage(msg1);
						if(D) Log.d(TAG, "append P1504 + P0720 + C0129 \n");
						connectstate = connecttemp; */
    		        }
            		if(firsttime > 0){
            			//readGroupInfo(softtoken);
            			if(connectstate == BluetoothService.STATE_CONNECTED){
            				sendrecethread.cancel();
                			try {
        						sleep(20);
        					} catch (InterruptedException e) {
        						// TODO Auto-generated catch blocks
        						e.printStackTrace();
        					}
            				sendrecethread = new SendReceCycle(0,10);           				
            			}
            			Readdata();
            			//mHandler.obtainMessage(VSUPPORT).sendToTarget();
            			//CarDataService.dataFlag ++;
            			//if(CarDataService.dataFlag > 40){
            				//mHandler.obtainMessage(SHUTDOWN).sendToTarget();
            				//CarDataService.dataFlag = 0;
            				//Log.e(TAG, "---read group info\n");
            				//DirectReadvolt();
            			
            			if(D) Log.d(TAG, "read group info\n");
            		} 
            		if(testUpdateFlag()){
            			//send message to high level application/activity to notify that data is ready for being read;
            			if(enginerpm > 0){
            				turnoffvolt = false;
            				readonce = false;
            			}
            			if(mHandler != null)
            				mHandler.obtainMessage(READY).sendToTarget();
            			if(enginerpm == 0){
            				new ReadlastVolt(true,15).start();
            				readonce = true;
            			}
            			clearUpdateFlag();
            			if(D) 
            				Log.d(TAG, "update flag\n");
            			//mHandler.obtainMessage(TEST).sendToTarget(); 
            			           			
            		}
            		if(turnoffvolt && readonce){
            			readonce = false;
        				DirectReadvolt();
        				mHandler.obtainMessage(SHUTDOWN).sendToTarget();            			
            		}
            		//test if bluetooth connection is established, if yes, do intialization sequences.
            		if(mBluetoothSet != null && mBluetoothSet.isExist()){
            			if((firsttime == 0)&&(mBluetoothSet.isConnected())){
            				//firsttime = 1;
            				if(initstatus == 0){
            					if(D) Log.d(TAG, "set init status\n");
            					
            					TestInitOBDSequence(softtoken);
            					System.out.println("====softtoken===\n");
            					System.out.println(softtoken);
            					tokencount++;
            					if(tokencount == 20 || tokencount == 40 || tokencount == 60)
            						softtoken = 0;
//           					if(tokencount > 120){
//            						mHandler.obtainMessage(VSUPPORT).sendToTarget();
//            						//tokencount = 0;
//            					}
            				}
            				if(initstatus == 1){
            					firsttime = 1;
            					try {
            						sleep(800);
            					} catch (InterruptedException e) {
    							// TODO Auto-generated catch block
            						e.printStackTrace();
            					} 
            					if(D) Log.d(TAG, "first time is 1\n");
            					timeoutthread.cancel();
            				}
            			}
            		}
            	            		
            		if(mBluetoothSet != null && mBluetoothSet.isExist()){
            			if(!mBluetoothSet.isConnected()){
            				firsttime = 0;
            				softtoken = 0;
            				initstatus = 0;
            				tokencount = 0;
            				mBluetoothSet.setIsBusy(false);
            				//Message msg1= new Message();
            				//msg1.what = READY;
    						//mHandler.sendMessage(msg1);
                			//try {
    						//	sleep(2000);
    						//} catch (InterruptedException e) {
    							// TODO Auto-generated catch block
    						//	e.printStackTrace();
    						//}
            			}
            		}
            		if(mBluetoothSet != null && mBluetoothSet.isExist()){
            			connecttemp = mBluetoothSet.ConnectionState();
            		}
            		
                }
        }
        
        public void cancel(){
        	if(mreadthread != null)
        		readstate = false;
        }

    }
    
    
    
    
   //Timeout thread to counter the initialization sequence;
    private class TimeoutThread extends Thread{	
    	int timeval = 0;
    	
    	public TimeoutThread(int initset, int timeout){
    		initstatus = initset;
    		timeval = timeout;
    	}
    	
    	@Override
    	public void run() {
    		// Auto-generated method stub
    		int i = 0;
    		while(initstatus == 0){
    			try{					   				
    				sleep(1000L);
    				i++;
    				if (i == timeval){
    					mHandler.obtainMessage(VSUPPORT).sendToTarget();
    				}
    			} catch (InterruptedException e) {
    				e.printStackTrace();
    			}				
    		}			
    	}
    	
    	public void cancel(){
    		initstatus = 1;
    	}
    } 
  
	int timeval = 0;
	int startcounter = 0;
	
    //Timeout thread to counter the time from sending data to receiving data;
    private class SendReceCycle extends Thread{	

    	
    	public SendReceCycle(int initset, int timeout){
    		startcounter = initset;
    		timeval = timeout;
    	}
    	
    	@Override
    	public void run() {
    		// Auto-generated method stub
    		while(startcounter < timeval){
    			try{					   				
    				sleep(500L);
    				startcounter ++;
    				
    			} catch (InterruptedException e) {
    				e.printStackTrace();
    			}				
    		}
    		sendsem.release();
    	}
    	
    	public void cancel(){
    		startcounter = timeval + 10;
    	}
    } 
    
    
    //Timeout thread to counter the time from sending data to receiving data;
    private class ReadlastVolt extends Thread{	
    		boolean teststatus;
    	
    	public ReadlastVolt(boolean status, int timeout){
    		teststatus = status;
    		timeval = timeout;
    	}
    	
    	@Override
    	public void run() {
    		// Auto-generated method stub
    		while(teststatus){
    			try{					   				
    				sleep(1000L);
    			} catch (InterruptedException e) {
    				e.printStackTrace();
    			}				
    		}
    		turnoffvolt = true;
    	}
    	
    	public void cancel(){
    		teststatus = false;
    	}
    }  
    
    
}



	
	
	
	
	
	

