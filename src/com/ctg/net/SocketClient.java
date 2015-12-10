package com.ctg.net;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class SocketClient {
	public static boolean upGpsSocketRun = false;
	public static int uploadTimes = 0;
	byte sendByte[];
	SendThread sendTh; 
	
    class SendThread extends Thread{  
        private Socket socket;  
        public SendThread(Socket socket) {  
            this.socket = socket; 
            sendByte = new byte[24];
        }  
        @Override  
        public void run() {  
            while(upGpsSocketRun && uploadTimes < 1000){  
                try {  
                	wait();
                    //getSendBytes();              
                    DataOutputStream dw = new DataOutputStream(socket.getOutputStream());  
                    dw.write(sendByte, 0, 24);  
                    dw.flush();  
                    uploadTimes++;
                } catch (Exception e) {  
                    e.printStackTrace();  
                }                  
            }  
        }  
    }  
    
    public boolean setSendBytes(long time, double longi, double lati){
		System.arraycopy(time, 0, sendByte, 0, 8);
		System.arraycopy(longi, 0, sendByte, 8, 8);
		System.arraycopy(lati, 0, sendByte, 16, 8);        
        return true;  
    }  
    
    public void start() throws UnknownHostException, IOException{  
        Socket socket = new Socket("http://192.168.1.142/obd/services/GPS",8080);  
        sendTh = new SendThread(socket);          
        sendTh.start();
    }  
    
    public void notifyThread(){
    	sendTh.notify();
    }
}
