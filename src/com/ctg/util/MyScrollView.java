package com.ctg.util;



import com.ctg.ui.Base;
import com.ctg.ui.DTCs_Scroll;


import android.content.Context;

import android.util.AttributeSet;
import android.view.MotionEvent;

import android.widget.HorizontalScrollView;

import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;



public class MyScrollView extends HorizontalScrollView {
    final static String[] dtc_code = {"C0129", "P0369", "P0720", "P1426", "P1504"};
    float pos_x_start = 0;
    float pos_x_end = 0;
    public static final int default_x = (int) (640*Base.scale);
    //int curId = 0;
    Base mContext;
    ScrollView detailScroll;
    LinearLayout scrollLinear;
    RelativeLayout curAbsView;
    public static long milliTime;
    public boolean isStart = false;
    public MyScrollView(Context context) {
        super(context);
        mContext = (Base) context;

        // TODO Auto-generated constructor stub
    }

    public MyScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = (Base) context;
        // TODO Auto-generated constructor stub
    }

	/*@Override
    public boolean arrowScroll(int direction) {
        switch (direction) {
            case FOCUS_LEFT:    //向←滚动
                 scrollBy(-320, 0);  //这里滚动的位置根据需要来调整
                 return true;
            case FOCUS_RIGHT:     //向→滚动
                 scrollBy(320, 0);
                 return true;
        }
        return false;
    }*/

    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        onScrollStart(oldl, oldt);
        super.onScrollChanged(l, t, oldl, oldt);
    }

    private void onScrollFinished() {
    }

    private void onScrollStart(int oldx, int oldy) {
        if (!isStart) {
            isStart = true;
            new ScrollListener(oldx, oldy).start();
        }
    }

    class ScrollListener extends Thread {
        int oldX, oldY;

        public ScrollListener(int oldx, int oldy) {
            this.oldX = oldx;
            this.oldY = oldy;
        }

        public void run() {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            while (isStart) {
                int newX = getScrollX();
                int newY = getScrollY();
                if (newX == oldX && newY == oldY) {
                    onScrollFinished();
                    isStart = false;
                } else {
                    oldX = newX;
                    oldY = newY;
                }
            }
        }
    }

//	public boolean onInterceptTouchEvent(MotionEvent ev){
//		return false;
//		
//	}

    public boolean onTouchEvent (MotionEvent ev){
        int length = mContext.dtc_s.dtcScrollLinear.getChildCount();


        if(ev.getAction() == MotionEvent.ACTION_DOWN){
            pos_x_start = ev.getX();
        }
        else if(ev.getAction() == MotionEvent.ACTION_UP){
            int cur = mContext.dtc_s.curId, next;
            if(mContext.dtc_s.dtcScrollLinear.getChildCount() < 2)// || mContext.dtc_s.dtcQueueThdRun
                return true;
            curAbsView = (RelativeLayout) mContext.dtc_s.dtcScrollLinear.getChildAt(cur);
            pos_x_end = ev.getX();
            if(pos_x_start != 0 && Math.abs(pos_x_end-pos_x_start) > 50 && !isStart){
                if(System.currentTimeMillis() - milliTime <= 700){
                    int iMilli = 0;
                    iMilli++;
                    return true;
                }
                milliTime = System.currentTimeMillis();
                if(pos_x_end < pos_x_start){
                    if(cur == length-1){
                        scrollTo(0, (int)0);
                        next = 0;
                    }
                    else{
                        scrollTo((int)curAbsView.getX()+default_x, (int)0);
                        next = cur+1;
                    }
                }
                else{
                    if(cur == 0){
                        next = length-1;
                        scrollTo((int) mContext.dtc_s.dtcScrollLinear.getChildAt(next).getX(), (int)0);
                    }
                    else{
                        scrollTo((int)curAbsView.getX()-default_x, (int)0);
                        next = cur-1;
                    }
                }
                mContext.dtc_s.resetLinearDot(cur, next);
                mContext.dtc_s.curId = next;
                mContext.dtc_s.unLoadDTCsDetail(cur);
                mContext.dtc_s.loadDTCsDetail();
            }
            pos_x_start = 0;
            pos_x_end = 0;
        }
        return true;
    }
}