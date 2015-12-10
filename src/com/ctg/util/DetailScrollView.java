package com.ctg.util;



import com.ctg.ui.Base;
import com.ctg.ui.R;

import android.content.Context;

import android.util.AttributeSet;
import android.view.MotionEvent;

import android.widget.LinearLayout;
import android.widget.ScrollView;



public class DetailScrollView extends ScrollView{
	float pos_x_start = 0;
	float pos_x_end = 0;
	int default_x = 640;
	//int curId = 0;
	Base mContext;
	MyScrollView hScroll;	
	ScrollView detailScroll;
	LinearLayout scrollLinear;
	LinearLayout curAbsView;
	
	public DetailScrollView(Context context) {
		super(context);
		mContext = (Base) context;		
		// TODO Auto-generated constructor stub
	}
	
	public DetailScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = (Base) context;	
		hScroll = (MyScrollView)mContext.findViewById(R.id.dtcs_scroll);
		curAbsView = (LinearLayout)hScroll.findViewById(R.id.dtcs_scroll_linear);
		
		// TODO Auto-generated constructor stub
	}

	/*@Override
    public boolean arrowScroll(int direction) {
        switch (direction) {
            case FOCUS_LEFT:    //
                 scrollBy(-320, 0);  //
                 return true;
            case FOCUS_RIGHT:     //
                 scrollBy(320, 0);
                 return true;
        }
        return false;
    }*/
	
	public boolean onTouchEvent (MotionEvent ev){
//		int length = mContext.dtc_s.dtcScrollLinear.getChildCount();
		int i;
		if(ev.getAction() == MotionEvent.ACTION_DOWN){
//			mContext.dtc_s.dtcScroll.onTouchEvent(ev);
			i = ev.getAction();			
		}
		else if(ev.getAction() == MotionEvent.ACTION_UP){			
			i = ev.getAction();
		}
//		mContext.dtc_s.dtcScroll.onTouchEvent(ev);
		return super.onTouchEvent(ev);	
		
	}	
	
	/*public void loadDtcsDetail(String xmlPath){
        scrollLinear = (LinearLayout)this.findViewById(R.id.dtcs_dtlay);
        prepareDtcsDetail(xmlPath, scrollLinear);
		
	}*/

}