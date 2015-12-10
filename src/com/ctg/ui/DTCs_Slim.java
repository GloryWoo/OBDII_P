package com.ctg.ui;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;

import com.ctg.util.CustomDialog;
import com.ctg.util.DTCsDetailDialog;

public class DTCs_Slim {
	final static String[] dtc_code = {"C0129", "P0369", "P0720", "P1426", "P1504"};	
	Button dtc_btn[];
	Base mContext;
	Button clearDtcs;
	int scr_width;
	int scr_height;
	
	private android.widget.Button.OnClickListener btnListen = new android.widget.Button.OnClickListener(){

		@Override
		public void onClick(View v)
		{
//			String xmlPath = dtc_code[v.getId()];
//			DTCsDetailDialog dialog = new DTCsDetailDialog(mContext, 320, 520, R.layout.dtcs_detail, R.style.Theme_dialog, xmlPath);
//			dialog.show();					
		}
		
	};
	
	private OnClickListener clearBtnListener = new OnClickListener()
	{
		public void onClick(View v)
		{		
			for(int i = 0; i < 5; i++){
				dtc_btn[i].setVisibility(View.GONE);
			}
		}
	};
	
	public DTCs_Slim(Context cont) {
		
		//ImageButton tmpBtn;
		mContext = (Base) cont; 
		
		WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);

		scr_width = wm.getDefaultDisplay().getWidth();
		scr_height = wm.getDefaultDisplay().getHeight();
		/*tableLayout = (TableLayout) View.inflate(cont, R.layout.dtcs_whole, null);
        
		// TODO Auto-generated constructor stub
        addRow(); 
        
        mContext.dtc_c.addView(tableLayout);
        
        clearDtcs = new Button(mContext);
        clearDtcs.setText(R.string.clear_obd_dtcs);*/
		LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
        //param.width = 500;
        //param.height = 120;
        
        //param.topMargin = 960;
        //param.leftMargin = 260;
        dtc_btn = new Button[5];
        ImageView gap_v;	
        param.width = LayoutParams.MATCH_PARENT;
    	param.height = 28;	
        for(int i = 0; i < 5; i++){
        	dtc_btn[i] = (Button)View.inflate(mContext, R.layout.dtc_button, null);
        	if(i > 0){
        		//param.addRule(RelativeLayout.BELOW, i-1);         		
        		//dtc_btn[i].setLayoutParams(param);
        	}
        	gap_v = new ImageView(mContext);	                	 
            gap_v.setBackgroundColor(Color.TRANSPARENT);
            gap_v.setLayoutParams(param);	
        	mContext.dtc_c.addView(gap_v);
        	mContext.dtc_c.addView(dtc_btn[i], 500, 120);         	
        	dtc_btn[i].setText(dtc_code[i]);
        	dtc_btn[i].setId(i);
        	dtc_btn[i].setOnClickListener(btnListen);
        	
        }
    	gap_v = new ImageView(mContext);	                	 
        gap_v.setBackgroundColor(Color.TRANSPARENT);
        //param.height = 75;
        //param.height = scr_height-mContext.tabHost.getTabWidget().getHeight() - (25+120)*5;
        gap_v.setLayoutParams(param);	
    	mContext.dtc_c.addView(gap_v);
        clearDtcs = new Button(mContext);
        clearDtcs.setText(R.string.clear_obd_dtcs);
        //RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, -2);
        //param.width = 180;
        //param.height = 120;
        //param.topMargin = 960;
        //param.leftMargin = 260;
        mContext.dtc_c.addView(clearDtcs, 180, 120);
        clearDtcs.setOnClickListener(clearBtnListener);
        //mContext.dtc_c.addView(clearDtcs, param);
        //clearDtcs.setOnClickListener(clearBtnListener);

	}
}
