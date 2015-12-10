package com.ctg.util;

import android.content.Context;

import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class MyViewPager extends ViewPager {  
  
    private boolean scrollable = false;  
  
    public MyViewPager(Context context) {  
        super(context);  
    }  
  
    public MyViewPager(Context context, AttributeSet attrs) {  
        super(context, attrs);  
    }  
  
    public void setScrollable(boolean enable) {  
        scrollable = enable;  
    }  
  
    @Override  
    public boolean onInterceptTouchEvent(MotionEvent event) {  
        if (scrollable) {  
            return super.onInterceptTouchEvent(event);  
        } else {  
            return false;  
        }  
    }  
    
    public boolean onTouchEvent(MotionEvent event) {
        if (scrollable) {
            return super.onTouchEvent(event);
        }
        return false;
    }
    
    public boolean onTouch(View v, MotionEvent event)
    {
        // TODO Auto-generated method stub
      
        return false;
    }  
}  