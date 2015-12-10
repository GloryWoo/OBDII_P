package com.ctg.util;



import com.ctg.interf.OnBorderListener;
import com.ctg.ui.Base;
import com.ctg.ui.DTCs_Scroll;


import android.content.Context;

import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import android.widget.HorizontalScrollView;

import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;



public class PoiScrollView extends ScrollView{

	float pos_y_start = 0;
	float pos_y_end = 0;
	//int curId = 0;
	Base baseAct;
	Context mContext;
	public static long milliTime;
	public boolean isStart = false;
	
	private OnBorderListener onBorderListener;
//	private boolean touchUp;

	
	public PoiScrollView(Context context) {
		super(context);
//		baseAct = (Base) context;
		
		// TODO Auto-generated constructor stub
	}
	
	public PoiScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
//		baseAct = (Base) context;
		// TODO Auto-generated constructor stub
	}

	private void doOnBorderListener() {
		View contentView = getChildAt(0);
		
	    if (contentView != null && contentView.getMeasuredHeight() <= getScrollY() + getHeight()) {	
	    	if(onBorderListener != null)
	    		onBorderListener.onBottom();	        
	    } else if (getScrollY() == 0) {
	    	if(onBorderListener != null)
	    		onBorderListener.onTop();	        
	    }
		    
		
	}
	
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		onScrollStart(oldl, oldt);
		super.onScrollChanged(l, t, oldl, oldt);
//		 doOnBorderListener();
	}


	private void onScrollStart(int oldx, int oldy) {
		
	}

	
	
	public boolean onTouchEvent (MotionEvent ev){	
		super.onTouchEvent(ev);
		if(ev.getAction() == MotionEvent.ACTION_DOWN){
			pos_y_start = ev.getY();						
		}
		else if(ev.getAction() == MotionEvent.ACTION_UP){	
//			touchUp = true;
			milliTime = System.currentTimeMillis();
		}	
		else if(ev.getAction() == MotionEvent.ACTION_MOVE){
			View contentView = getChildAt(0);			
			if(System.currentTimeMillis() - milliTime > 500){
				if(getScrollY() == 0){
					if(onBorderListener != null)
			    		onBorderListener.onTop();	   
				}
				else if(contentView != null && contentView.getMeasuredHeight() <= getScrollY() + getHeight()){
					if(onBorderListener != null)
			    		onBorderListener.onBottom();
				}
				
			}
			
			
		}
		return true;
	}

	public void setOnBorderListener(OnBorderListener listener){
		onBorderListener = listener;
	}
	
	
	
}

