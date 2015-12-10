package com.ctg.util;


import java.util.Random;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.ctg.ui.Base;
import com.ctg.ui.R;
import com.ctg.widget.Gas;
import com.ctg.widget.GasBar;

public class PanelDlg extends Dialog implements Runnable, DialogInterface.OnCancelListener{ 
	private static final String TAG = "CustomDialog";		
	private Base baseAct;
	Gas gas;
	GasBar gasbar;
	TextView gas_textv;
	TextView gas_unit;
	View backBtn;
	String gas_str;
	int refreshCnt;
	float gas_c;
	float gas_l;
	float scale;
	
	private static int default_width = 160; //
	private static int default_height = 120;//

	boolean testRun;
	
	public PanelDlg(Context context, int layout, int style) {
		this(context, default_width, default_height, layout, style);
		
	}
	
	protected void onDestroy(){
	}
	
	public PanelDlg(Context context, int width, int height, int layout, int style) {
		super(context, style);
		//set content
		setContentView(layout);
		
		//mac_address_init();
		//set window params
		Window window = getWindow();
		WindowManager.LayoutParams params = window.getAttributes();
		//set width,height by density and gravity
		params.width = width;
		params.height = height;
		params.gravity = Gravity.TOP;		
		window.setAttributes(params);
		baseAct = (Base)context;
		setCancelable(true);
		gas_textv = (TextView) findViewById(R.id.gas_text);
		gas_unit = (TextView) findViewById(R.id.gas_unit);
		gas = (Gas) findViewById(R.id.gas);
		gasbar = (GasBar) findViewById(R.id.gas_bar);
		scale = context.getResources().getDisplayMetrics().density/2;
		setOnCancelListener(this);
		backBtn = findViewById(R.id.panel_back);
		backBtn.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				PanelDlg.this.cancel();
			}			
		});
		
		//testRun = true;
		//new Thread(new MyThread()).start();
		
	}	
	
	public void TextLayout(){
		int start = 0, end = 4;
		Spannable word;
	
        
        gas_str =  gas_textv.getText().toString();
        int LIdx = gas_str.indexOf("L");
		word = new SpannableString(gas_str);  
		start = 0;
		end = 5;		
        word.setSpan(new AbsoluteSizeSpan(40), start, end,   
                Spannable.SPAN_INCLUSIVE_INCLUSIVE);  
        word.setSpan(new ForegroundColorSpan(0xff000000), start, end,   
                Spannable.SPAN_INCLUSIVE_INCLUSIVE); 
        start = 5;
        end = LIdx;        
        word.setSpan(new AbsoluteSizeSpan(45), start, end,   
                 Spannable.SPAN_INCLUSIVE_INCLUSIVE);  
        word.setSpan(new ForegroundColorSpan(Color.RED), start, end,   
                Spannable.SPAN_INCLUSIVE_INCLUSIVE);  
        start = LIdx; 
        end = gas_str.length();
        word.setSpan(new AbsoluteSizeSpan(40), start, end,   
                Spannable.SPAN_INCLUSIVE_INCLUSIVE);  
        word.setSpan(new ForegroundColorSpan(Color.BLACK), start, end,   
               Spannable.SPAN_INCLUSIVE_INCLUSIVE);          
        gas_textv.setText(word);  
         
	}
	
	public boolean setPanelReadData(String text, float fuel){		
		String gas_round;
//		String fuel_str = "";
		//String strArray[] = null;
		
		
		if(baseAct == null || baseAct.serviceConn == false){
			return false;
		}
		
		if(fuel < 0)
			fuel = 0;
		else if(fuel > 30)
			fuel = 30f;
		
//		fuel_str += fuel; 	
	

		//Log.d(TAG, "rpm="+rpm+" vss"+vss+" temp"+temp);
//		gas_round = baseAct.getString(R.string.gas_consume) +"\n\n"+fuel +"L/百公里";			
		
		
//		if(fuel_str.matches("[0-9].+"))
//			gas_c = Float.parseFloat(fuel);
//		else
//			gas_c = 0f;
		if(text.contains("L/h"))
			gas_unit.setText("怠速每小时油耗:");
		else
			gas_unit.setText("百公里油耗:");
		gas_c = fuel;
		gas_l =	gas.gas_volumn;
		
		gas_textv.setText(text);//.substring(0, text.indexOf("L")+1)
//		if(gas_c > -0.00000001 && gas_c < 0.00000001)
//		{
//			gas_textv.setText("/");
//		}
//		else{
//			int dotIdx = fuel_str.indexOf(".");
//			if(dotIdx != -1 && dotIdx+2 < fuel_str.length()-1){
//				fuel_str = fuel_str.substring(0, dotIdx+3);
//			}
//			gas_textv.setText(text);
//		}
		//TextLayout();

		


		
		if(true){//rpm_n != 0 || vss_n != 0 || degree_n != 0
			new Thread(this).start();
			refreshCnt = 0;
		}
		return true;
	}
	

	
	void setGasVolumn(float volumn){
		gas.gas_volumn = volumn;
		gas.postInvalidate();
	
		
		
	}
	
	
	static final int MSG_REFRESH = 0x100;
	float fuelTest = 0;
	Handler myHandler = new Handler(){
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			
			switch(msg.what){
			case MSG_REFRESH:
				setPanelReadData(fuelTest+"L/100km",fuelTest);
				break;
			}
		}
	};
	
		
	public synchronized void run() {
		while(refreshCnt < 10){
			refreshCnt++;
			gas.setGasVolumn(gas_l+(gas_c-gas_l)*refreshCnt/10);
			gasbar.setGasVolum(gas_l+(gas_c-gas_l)*refreshCnt/10);
			try {
				Thread.sleep(80L);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void onCancel(DialogInterface dialog) {
		// TODO Auto-generated method stub
		testRun = false;
		Base.car_v.panelDlg = null;
	};
	
	//this is test code
	class MyThread extends Thread{
		public synchronized void run() {
			float i = 0f;
			while(testRun){
				try {
					i= (float) Math.random();
					i *= 30;					
					fuelTest = i;
					myHandler.obtainMessage(MSG_REFRESH).sendToTarget();
					sleep(1500L);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		}
	}	
}
