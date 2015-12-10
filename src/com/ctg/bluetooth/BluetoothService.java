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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import com.ctg.crash.LogRecord;
import com.ctg.obdii.OBDcmd;
import com.ctg.ui.Base;
import com.ctg.ui.OBDApplication;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;


/**
 * This class does all the work for setting up and managing Bluetooth
 * connections with other devices. It has a thread that listens for
 * incoming connections, a thread for connecting with a device, and a
 * thread for performing data transmissions when connected.
 */


public class BluetoothService {

    // Debugging
    private static final String TAG = "BluetoothService";
    private static final boolean D = false;
    
    public static final String testURL = "http://116.236.202.130:8080/obd/services/log";

    // Name for the SDP record when creating server socket
    private static final String NAME = "OBDDTC";
    
    //OBD command token;
    private volatile int localtoken = 0;

    // Unique UUID for this application    									 				
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    //##member fields
    private final BluetoothAdapter mAdapter;
    private final Handler mHandler;
    private final Handler dataHandler;
    private AcceptThread mAcceptThread;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;    
    private int mState;
    //##for receiving data
//    private String receiveBuffer;
    
    // Constants that indicate the current connection state
    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device
    
    private OBDApplication OBDApp;

    /**
     * Constructor. Prepares a new BluetoothChat session.
     * @param context  The UI Activity Context
     * @param handler  A Handler to send messages back to the UI Activity
     */
    public BluetoothService(Handler handler1, Handler handler2) {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = STATE_NONE;
        mHandler = handler1;
        dataHandler = handler2;
        if (D) Log.d(TAG, "oncreate BluetoothService");
    }

    /**
     * Set the current state of the chat connection
     * @param state  An integer defining the current connection state
     */
    private synchronized void setState(int state) {
        if (D) Log.d(TAG, "BluetoothService:setState() " + mState + " -> " + state);
        mState = state;

        // Give the new state to the Handler so the UI Activity can update
        if(mHandler != null)
        	mHandler.obtainMessage(BluetoothSet.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
    }

    /**
     * Return the current connection state. */
    public synchronized int getState() {
        return mState;
    }

    /**
     * Start the chat service. Specifically start AcceptThread to begin a
     * session in listening (server) mode. Called by the Activity onResume() */
    public synchronized void start() {
        if (D) Log.d(TAG, "BluetoothService:start");

        // Cancel any thread attempting to make a connection
        if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}

        // Start the thread to listen on a BluetoothServerSocket
        if (mAcceptThread == null) {
            mAcceptThread = new AcceptThread();
            mAcceptThread.start();
        }
        setState(STATE_LISTEN);
    }

    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     * @param device  The BluetoothDevice to connect
     */
    public synchronized void connect(BluetoothDevice device) {
        if (D) Log.d(TAG, "BluetoothService:connect to: " + device);

        // Cancel any thread attempting to make a connection
        if (mState == STATE_CONNECTING) {
            if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}

        // Start the thread to connect with the given device
        mConnectThread = new ConnectThread(device);
        mConnectThread.start();
        setState(STATE_CONNECTING);
    }

    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     * @param socket  The BluetoothSocket on which the connection was made
     * @param device  The BluetoothDevice that has been connected
     */
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
        if (D) Log.d(TAG, "BluetoothService:connected");

        // Cancel the thread that completed the connection
        if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}

        // Cancel the accept thread because we only want to connect to one device
        if (mAcceptThread != null) {mAcceptThread.cancel(); mAcceptThread = null;}

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();
        if (D) Log.d(TAG, "BluetoothService: connected thread start");

        // Send the name of the connected device back to the UI Activity
        if(mHandler != null){
        	Message msg = mHandler.obtainMessage(BluetoothSet.MESSAGE_DEVICE_NAME);
        	Bundle bundle = new Bundle();
        	bundle.putString(BluetoothSet.DEVICE_NAME, device.getName());
        	msg.setData(bundle);
        	mHandler.sendMessage(msg);

        	setState(STATE_CONNECTED);
        }
    }

    /**
     * Stop all threads
     */
    public synchronized void stop() {
        if (D) Log.d(TAG, "BluetoothService:stop");
        if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}
        if (mAcceptThread != null) {mAcceptThread.cancel(); mAcceptThread = null;}
        setState(STATE_NONE);
    }

    /**
     * Write to the ConnectedThread in an unsynchronized manner
     * @param out The bytes to write
     * @see ConnectedThread#write(byte[])
     */
    public void write(byte[] out, int token) {
        // Create temporary object
        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        // update and synchronize token of send message with token of receive message
        synchronized (this) {
            if (mState != STATE_CONNECTED) 
            	return;
            r = mConnectedThread;
            localtoken = token;
        }
        // Perform the write unsynchronized
        r.write(out, token);
    }

    /**
     * Indicate that the connection attempt failed and notify the UI Activity.
     */
    private void connectionFailed() {
        setState(STATE_LISTEN);

        // Send a failure message back to the Activity
       /* Message msg = mHandler.obtainMessage(BluetoothSet.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        //bundle.putString(TAG, "Unable to connect device");
        bundle.putString(BluetoothSet.TOAST, "Unable to connect device");
        msg.setData(bundle);
        mHandler.sendMessage(msg);*/
    }

    /**
     * Indicate that the connection was lost and notify the UI Activity.
     */
    private void connectionLost() {
        setState(STATE_LISTEN);

        // Send a failure message back to the Activity
        /*Message msg = mHandler.obtainMessage(BluetoothSet.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        //bundle.putString(TAG, "Device connection was lost");
        bundle.putString(BluetoothSet.TOAST, "Device connection was lost");
        msg.setData(bundle);
        mHandler.sendMessage(msg);*/
    }

    /**
     * This thread runs while listening for incoming connections. It behaves
     * like a server-side client. It runs until a connection is accepted
     * (or until cancelled).
     */
    private class AcceptThread extends Thread {
        // The local server socket
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread() {
            BluetoothServerSocket tmp = null;

            // Create a new listening server socket
            try {
            	if(mAdapter != null)
            		tmp = mAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);            	
            } catch (IOException e) {
                Log.e(TAG, "listen() failed", e);
            }
            mmServerSocket = tmp;
        }

        public void run() {
            if (D) Log.d(TAG, "BEGIN mAcceptThread" + this);
            setName("AcceptThread");
            BluetoothSocket socket = null;

            // Listen to the server socket if we're not connected
            while (mState != STATE_CONNECTED) {
                try {
                    // This is a blocking call and will only return on a
                    // successful connection or an exception
                	if(mmServerSocket == null)
                		return;
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    Log.e(TAG, "accept() failed", e);
                    break;
                }

                // If a connection was accepted
                if (socket != null) {
                    synchronized (BluetoothService.this) {
                        switch (mState) {
                        case STATE_LISTEN:
                        case STATE_CONNECTING:
                            // Situation normal. Start the connected thread.
                            connected(socket, socket.getRemoteDevice());
                            if (D) Log.d(TAG, "BluetoothService:mAcceptThread start connection");
                            break;
                        case STATE_NONE:
                        case STATE_CONNECTED:
                            // Either not ready or already connected. Terminate new socket.
                            try {
                            	if(socket != null)
                            		socket.close();
                            } catch (IOException e) {
                                Log.e(TAG, "Could not close unwanted socket", e);
                            }
                            break;
                        }
                    }
                }
            }
            if (D) Log.d(TAG, "BlluetoothService:END mAcceptThread");
        }

        public void cancel() {
            if (D) Log.d(TAG, "cancel " + this);
            try {
            	if(mmServerSocket != null)
            		mmServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of server failed", e);
            }
        }
    }


    /**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */
    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            mmDevice = device;
            BluetoothSocket tmp = null;

            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {
                //tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            	int sdk = Build.VERSION.SDK_INT;
            	if(sdk >= 10){
            		tmp = device.createInsecureRfcommSocketToServiceRecord(MY_UUID);
            		if (D) Log.d(TAG, "BTSPP : sdk version >10");
            	}else {
            		tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            		if (D) Log.d(TAG, "BTSPP : sdk version <10");
				}             	
            } catch (IOException e) {
                Log.e(TAG, "Connect Thread:create() tmp failed", e);
            }
            if(tmp == null){
            	if (D) Log.d(TAG, "Connect Thread: tmp and mmSocket is null");
            }           	
            mmSocket = tmp;
        }

        public void run() {
            if(D) Log.d(TAG, "BluetoothService:BEGIN mConnectThread");
            setName("ConnectThread");

            // Always cancel discovery because it will slow down a connection
            if(mAdapter != null)
            	mAdapter.cancelDiscovery();

            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
            	if(mmSocket == null){
            		if(D) Log.d(TAG, "mmSocket is null!");
            		return;
            	}
                mmSocket.connect();
            } catch (IOException e) {
                connectionFailed();
                // Close the socket
                try {
                	if(mmSocket != null)
                		mmSocket.close();
                } catch (IOException e2) {
                	LogRecord.SaveLogInfo2File(Base.BTlog,"unable to close() socket during connection failure");
                	//LogRecord.UploadLogFiles(Base.ycblog,BluetoothService.testURL);
                    //Log.e(TAG, "unable to close() socket during connection failure", e2);
                }
                // Start the service over to restart listening mode
                BluetoothService.this.start();  //modified by zhiming hu 5/14
                return;
            }

            // Reset the ConnectThread because we're done
            synchronized (BluetoothService.this) {
                mConnectThread = null;
            }

            // Start the connected thread
            if(mmSocket == null || mmDevice == null){
            	LogRecord.SaveLogInfo2File(Base.BTlog,"mmSocket or mmDevice is null!");
            	//LogRecord.UploadLogFiles(Base.ycblog,BluetoothService.testURL);
        		if(D) Log.d(TAG, "mmSocket or mmDevice is null!");
        		return;           	
            }           	
            connected(mmSocket, mmDevice);
        }

        public void cancel() {
            try {
            	if(mmSocket != null)
            		mmSocket.close();
            } catch (IOException e) {
            	LogRecord.SaveLogInfo2File(Base.BTlog, "close() of connect socket failed");
                //Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }

    /**
     * This thread runs during a connection with a remote device.
     * It handles all incoming and outgoing transmissions.
     */
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            if(D) Log.d(TAG, "BluetoothService:create ConnectedThread");
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();                
            } catch (IOException e) {
                Log.e(TAG, "temp sockets not created", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            if(D) Log.d(TAG, "BluetoothService:BEGIN mConnectedThread");
            byte[] buffer = new byte[300];
            int bytes;
            int index = 0;
            String voltagevalue = "";
            int logcount = 0;
            int cmdcode = 0;
            String temp = "",temps = "",tempall ="";
            char sequence='a';
            
            // Keep listening to the InputStream while connected
            String receiveBuffer ="";
            while (true) {
                try {   
                	while(mConnectedThread != null){                		
		                // Read from the InputStream 
		        		//add for debug;
                		sleep(50L);
                		int j = 0;
                		cmdcode = localtoken;
                		if(mmInStream == null)
                			break;
                	
                		bytes = mmInStream.read(buffer);
		      	        
		        		String tempString = new String(buffer,0,bytes);
		        		receiveBuffer += tempString;

		        		if(receiveBuffer == null)
		        			break;
		        		//================================================================
		        		//OBDcmd.readsem.
		        		switch(cmdcode){
		        		case OBDcmd.RES_RPM:
		        			if(receiveBuffer.contains(">")){
		        			//if( receiveBuffer.contains("41 0C")||receiveBuffer.contains("41")|| receiveBuffer.contains("\r\r>") || receiveBuffer.contains(">")){
		        				index = receiveBuffer.indexOf("4");
			        			if(index >= 0){
			        				temp = receiveBuffer.substring(index, receiveBuffer.length());
			        				if(temp.length() > 11){
			        					String str = temp.substring(0, 11);	
				        				OBDcmd.getCarData(str,localtoken,dataHandler);
			        				}
			        			}
			        			else{
			        				
			        			}
		        			    receiveBuffer = "";
		        			    OBDcmd.sendsem.release();
		        			}
		        			//OBDcmd.sendsem.release();
		        			break;
		        		case OBDcmd.RES_VSS:
		        			if(receiveBuffer.contains(">")){
		        			//if( receiveBuffer.contains("41 0D")||receiveBuffer.contains("41")|| receiveBuffer.contains("\r\r>") || receiveBuffer.contains(">")){
		        				index = receiveBuffer.indexOf("4");
			        			if(index >= 0){
			        				temp = receiveBuffer.substring(index, receiveBuffer.length());
			        				if(temp.length() > 8){
			        					String str = temp.substring(0, 8);	
				        				OBDcmd.getCarData(str,localtoken,dataHandler);
			        				}
			        			}
			        			else
			        			{
			        				
			        			}
		        			    receiveBuffer = "";
		        			    //OBDcmd.initstatus = 1;
		        			    OBDcmd.sendsem.release();
		        			}		        			
		        			break;	
		        		case OBDcmd.RES_TP:
		        			if(receiveBuffer.contains(">")){
		        			//if( receiveBuffer.contains("41 11")||receiveBuffer.contains("41")|| receiveBuffer.contains("\r\r>")|| receiveBuffer.contains(">")){
		        				index = receiveBuffer.indexOf("4");
			        			if(index >= 0){
			        				temp = receiveBuffer.substring(index, receiveBuffer.length());
			        				if(temp.length() > 8){
			        					String str = temp.substring(0, 8);	
				        				OBDcmd.getCarData(str,localtoken,dataHandler);
			        				}	
			        			}
			        			else{
			        				
			        			}
		        			    receiveBuffer = "";
		        			    OBDcmd.sendsem.release();
		        			}		        			
		        			break;
		        		case OBDcmd.RES_DTC:
		        			if(receiveBuffer.contains(">")){
		        			//if( receiveBuffer.contains("41 01")||receiveBuffer.contains("41") || receiveBuffer.contains("\r\r>")|| receiveBuffer.contains(">") ){
		        				index = receiveBuffer.indexOf("4");
		        				if(index >= 0){
		        					tempall = "";
		        					temp = receiveBuffer.substring(index, receiveBuffer.length());
		        					index = 0;
		        					temps = temp.substring(index, index+1);
		        					while((temps!= null && temps.matches("[0-9a-fA-f]+")&&(index < receiveBuffer.length()))|| temps.equals(" ")){
		        						tempall = tempall + temps;
		        						index ++;
		        						temps = temp.substring(index, index+1);
		        					}
		        					OBDcmd.getCarData(tempall,localtoken,dataHandler);
		        					
		        				}
		        				else// index = -1;
		        				{
		        					
		        				}				        		
		        			    receiveBuffer = "";
		        			    OBDcmd.sendsem.release();
		        			}
		        			//OBDcmd.sendsem.release();
		        			break;
		        		case OBDcmd.RES_DTCs:
		        			if(receiveBuffer.contains(">")){
		        			//if(receiveBuffer.contains("43")  || receiveBuffer.contains("\r\r>")|| receiveBuffer.contains(">")){
		        				index = receiveBuffer.indexOf("4");
		        				if(index >= 0){
		        					tempall = "";
		        					temp = receiveBuffer.substring(index, receiveBuffer.length());
		        					index = 0;
		        					temps = temp.substring(index, index+1);
		        					while((temps!= null && temps.matches("[0-9a-fA-f]+")&&(index < receiveBuffer.length()))|| temps.equals(" ")){
		        						tempall = tempall + temps;
		        						index ++;
		        						temps = temp.substring(index, index+1);
		        					}
		        					OBDcmd.getCarData(tempall,localtoken,dataHandler);
		        					
		        				}
		        				else// index = -1;
		        				{
		        					
		        				}	
		        					
		        			    receiveBuffer = "";
		        			    OBDcmd.sendsem.release();
		        			}
		        			//OBDcmd.sendsem.release();
		        			break;	
		        		case OBDcmd.RES_FUEL:
		        			if(receiveBuffer.contains(">")){        		
		        				index = receiveBuffer.indexOf("4");
			        			if(index >= 0 && receiveBuffer.length()>=8){
		        					tempall = "";
		        					temp = receiveBuffer.substring(index, receiveBuffer.length());
		        					index = 0;
		        					temps = temp.substring(index, index+1);
		        					while((temps!= null && temps.matches("[0-9a-fA-f]+")&&(index < receiveBuffer.length()))|| temps.equals(" ")){
		        						tempall = tempall + temps;
		        						index ++;
		        						temps = temp.substring(index, index+1);
		        					}
		        					OBDcmd.getCarData(tempall,localtoken,dataHandler);	
			        			}
			        			else{
			        				
			        			}
		        			    receiveBuffer = "";
		        			    OBDcmd.sendsem.release();
		        			}		        			
		        			break;
		        		case OBDcmd.RSTA:
		        			LogRecord.SaveLogInfo2File(Base.OBDinit,receiveBuffer);
		        			if(receiveBuffer.contains(">")){
		        				//receiveBuffer.contains("ATZ") || receiveBuffer.contains("OK") ){			        		
		        			    receiveBuffer = "";
		        			    OBDcmd.sendsem.release();
		        			    Log.e(TAG,"sem release");
		        			}		        			
		        			break;	
		        		case OBDcmd.WRST:
		        			LogRecord.SaveLogInfo2File(Base.OBDinit,receiveBuffer);
		        			if(receiveBuffer.contains(">")){
		        			//if(receiveBuffer.contains("ATWS") || receiveBuffer.contains("EST327") ){			        		
		        			    receiveBuffer = "";
		        			    OBDcmd.sendsem.release();
		        			    Log.e(TAG,"sem release");
		        			}		        			
		        			break;			        			
		        		case OBDcmd.ECHO:
		        			LogRecord.SaveLogInfo2File(Base.OBDinit,receiveBuffer);
		        			if(receiveBuffer.contains(">")){
		        			//if(receiveBuffer.contains("ATE0") || receiveBuffer.contains("OK") ){			        		
		        			    receiveBuffer = "";
		        			    OBDcmd.sendsem.release();
		        			    Log.e(TAG,"sem release");
		        			}		        			
		        			break;	
		        		case OBDcmd.LFDO:		        			
		        			LogRecord.SaveLogInfo2File(Base.OBDinit,receiveBuffer);
		        			if(receiveBuffer.contains(">")){
		        			//if(receiveBuffer.contains("ATL0") || receiveBuffer.contains("OK") ){			        		
		        			    receiveBuffer = "";
		        			    OBDcmd.sendsem.release();
		        			    Log.e(TAG,"sem release");
		        			}		        			
		        			break;	
		        		case OBDcmd.HDON:
		        			LogRecord.SaveLogInfo2File(Base.OBDinit,receiveBuffer);
		        			if(receiveBuffer.contains(">")){
		        			//if(receiveBuffer.contains("ATH1") || receiveBuffer.contains("OK") ){			        		
		        			    receiveBuffer = "";
		        			    OBDcmd.sendsem.release();
		        			    Log.e(TAG,"sem release");
		        			}		        			
		        			break;
		        		case OBDcmd.VINF:
		        			LogRecord.SaveLogInfo2File(Base.OBDinit,receiveBuffer);
		        			if(receiveBuffer.contains(">")){
		        			//if(receiveBuffer.contains("ATI") || receiveBuffer.contains("EST327") ){			        		
		        			    receiveBuffer = "";
		        			    OBDcmd.sendsem.release();
		        			    Log.e(TAG,"sem release");
		        			}		        			
		        			break;
		        		case OBDcmd.VOLT:
		        			LogRecord.SaveLogInfo2File(Base.OBDinit,receiveBuffer);
		        			if(receiveBuffer.contains(">")){
		        			//if(receiveBuffer.contains("ATRV") || receiveBuffer.contains("V") ){	
		        				index = receiveBuffer.indexOf(".");
		        				if(index > 1)
		        				{
		        					voltagevalue = receiveBuffer.substring((index-2), (index+2));
		        					voltagevalue = voltagevalue +"\r";
		        					OBDcmd.getCmdData(voltagevalue,localtoken,dataHandler);			        					
		        				}
		        				//Log.e(TAG, "OBDcmd voltage from BT is" + receiveBuffer);
		        				voltagevalue = "";
		        				receiveBuffer = "";
		        			    OBDcmd.sendsem.release();
		        			    Log.e(TAG,"sem release");
		        			}		        			
		        			break;
		        		case OBDcmd.SETP:
		        			LogRecord.SaveLogInfo2File(Base.OBDinit,receiveBuffer);
		        			if(receiveBuffer.contains(">")){
		        			//if(receiveBuffer.contains("ATSPA6") || receiveBuffer.contains("SEARCH") || receiveBuffer.contains("OK")){			        		
		        			    receiveBuffer = "";
		        			    OBDcmd.sendsem.release();
		        			    Log.e(TAG,"sem release");
		        			}		        			
		        			break;	
		        		case OBDcmd.CURP:
		        			LogRecord.SaveLogInfo2File(Base.OBDinit,receiveBuffer);
		        			if(receiveBuffer.contains(">")){
		        			//if(receiveBuffer.contains("ATDP") || receiveBuffer.contains("AUTO") || receiveBuffer.contains("ISO")  ){			        		
		        			    receiveBuffer = "";
		        			    OBDcmd.sendsem.release();
		        			    Log.e(TAG,"sem release");
		        			}		        			
		        			break;	
		        		case OBDcmd.SUPP:
		        			LogRecord.SaveLogInfo2File(Base.OBDinit,receiveBuffer);
		        			if(receiveBuffer.contains(">")){
		        			//if(receiveBuffer.contains("01 00") || receiveBuffer.contains("48") || receiveBuffer.contains("41") || receiveBuffer.contains("00")|| receiveBuffer.contains(">")){			        		
		        			    receiveBuffer = "";
		        			    //OBDcmd.initstatus = 1;
		        			    OBDcmd.sendsem.release();
		        			    Log.e(TAG,"sem release");
		        			}		        			
		        			break;			        			
		        		case 0:
		        			//OBDcmd.readsem.release();
		        			break;
		        		case -1:
		        			//init sequence is failed;
		        			break;
		        		default:
		        				break;
		        			
		        		}
		        		//================================================================
		        		/*
		        		if (receiveBuffer.startsWith("\r\r")||receiveBuffer.contains("NOT")||receiveBuffer.contains("STOPPED")||receiveBuffer.contains("SEARCH")){		        				
	        				BluetoothSet.setIsBusy(false); 
	        				LogRecord.SaveLogInfo2File(Base.OBDinit,receiveBuffer);
	        				receiveBuffer = "";
	        				logcount ++;
	        				if(logcount > 20){
	        					LogRecord.UploadLogFiles(Base.OBDinit,testURL);
	        					logcount = 0;
	        					LogRecord.SaveSysInfo2File(Base.OBDinit);
	        				}
		        		}
			        	if (receiveBuffer.endsWith("\r\r>")){
			        		if(receiveBuffer.contains("AT")){
			        			if(receiveBuffer.contains("ATRV")){
			        				LogRecord.SaveLogInfo2File(Base.OBDinit,receiveBuffer);
			        				index = receiveBuffer.indexOf(".");
			        				if(index > 4)
			        				{
			        					voltagevalue = receiveBuffer.substring((index-2), (index+2));
			        					voltagevalue =voltagevalue +"\r";
			        					OBDcmd.getCmdData(voltagevalue,localtoken,dataHandler);			        					
			        				}
			        				Log.e(TAG, "OBDcmd voltage from BT is" + receiveBuffer);
			        				voltagevalue = "";
			        			}
			        			else{
			        				String strArray[] = receiveBuffer.split("\r\r\r");
			        				LogRecord.SaveLogInfo2File(Base.OBDinit,receiveBuffer);
			        				if(strArray.length == 1){
			        					String strArray1[] = strArray[0].split("\r");
			        					if(receiveBuffer.contains("ISO"))
			        						OBDcmd.getCmdData(strArray1[2],localtoken,dataHandler); 
			        					else
			        						OBDcmd.getCmdData(strArray1[1],localtoken,dataHandler);
			        				}
			        				else
			        				{
			        					OBDcmd.getCmdData(strArray[1],localtoken,dataHandler);
			        				}
			        				if(D) 
			        					Log.d(TAG, "BluetoothService:control response is received by BT!");
			        			}
		        			    receiveBuffer = "";			        			
			        		}
			        		else if(receiveBuffer.contains("OK")){
			        			LogRecord.SaveLogInfo2File(Base.OBDinit,receiveBuffer);
			        			String strArray[] = receiveBuffer.split("\r");
			        			OBDcmd.getCmdData(strArray[0],localtoken,dataHandler);
		        				if(D) 
			        			Log.d(TAG, "BluetoothService:control response is received by BT-----!");
		        			    receiveBuffer = "";				        						        			
			        		}
			        		else if(receiveBuffer.contains("ISO")){
			        			LogRecord.SaveLogInfo2File(Base.OBDinit,receiveBuffer);
			        			String strArray[] = receiveBuffer.split("\r");
				        		OBDcmd.getCmdData(strArray[1],localtoken,dataHandler);
				        		receiveBuffer = "";
			        		}
			        		else if(receiveBuffer.contains(".") && receiveBuffer.contains("V"))
			        		{
			        			LogRecord.SaveLogInfo2File(Base.OBDinit,receiveBuffer);
			        			if(receiveBuffer.contains("1.5")){
			        				
			        			}
			        			else{
			        				index = receiveBuffer.indexOf(".");
			        				if(index > 1)
			        				{
			        					voltagevalue = receiveBuffer.substring((index-2), (index+2));
			        					voltagevalue = voltagevalue +"\r";
			        					OBDcmd.getCmdData(voltagevalue,localtoken,dataHandler);			        					
			        				}
			        				//Log.e(TAG, "OBDcmd voltage from BT is" + receiveBuffer);
			        				voltagevalue = "";
			        			}
			        			receiveBuffer = "";
			        		}
			        		else{			        		
			        			String strArray[] = receiveBuffer.split("\r");				        		
		        				OBDcmd.getCarData(strArray[1],localtoken,dataHandler);				        		
//			    				mHandler.obtainMessage(BluetoothSet.MESSAGE_READ, stemp.length(), localtoken, stemp.getBytes())
//			    	        		.sendToTarget();
		        				if(D) 
		        					Log.d(TAG, "BluetoothService:data response is received by BT!+++++");
		        			    receiveBuffer = "";
		        		}			        	
			        				        
		        		if (mmInStream.available() == 0) {
		        			//System.out.println("*****no byte can be read!");
		        			sleep(10L);
		        			BluetoothSet.setIsBusy(false); 
		        			break;	
		        		}
	        			int k = mmInStream.available();
	        			//System.out.println("*****available byte can be read!");
	        			//System.out.print(k);
                	} */
                  }
                } catch (IOException e) {
                    Log.e(TAG, "disconnected", e);
                    OBDcmd.sendsem.release();
                    connectionLost();
                    break;
                } catch (InterruptedException e) {
                	Log.e(TAG, "disconnected", e);
                	OBDcmd.sendsem.release();
                    connectionLost();
                    break;
				}
            }
        }
        
        /**
         * Write to the connected OutStream.
         * @param buffer  The bytes to write
         */
        public void write(byte[] buffer, int token) {
        	try {
        		if(mmOutStream != null)
        			mmOutStream.write(buffer);
	    		if(D) Log.d(TAG, "BluetoothService:data is wirtten normal!");         
        	}catch (IOException e) {
        			Log.e(TAG, "Exception during write", e);
        		}
        }

        public void cancel() {
            try {
            	if(mmSocket != null)
            		mmSocket.close();
            	OBDcmd.sendsem.release();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }
}