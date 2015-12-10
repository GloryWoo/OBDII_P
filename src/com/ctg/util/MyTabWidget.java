package com.ctg.util;

import com.ctg.ui.Base;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TabWidget;

public class MyTabWidget extends TabWidget{
	public static String TAG = "MyTabWidget";
	Base mContext;
	MyTabWidget curContext;
	public MyTabWidget(Context context) {		
		super(context);
		mContext = (Base) context;
	}

	public MyTabWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = (Base) context;
		curContext = this;
		// TODO Auto-generated constructor stub
		// TODO Auto-generated constructor stub			
	}


	public boolean onTouchEvent (MotionEvent ev){
		if(mContext.dtc_s != null )// && mContext.dtc_s.dtcQueueThdRun
			return false;
		else
			return super.onTouchEvent(ev);		
	}	
	
}
