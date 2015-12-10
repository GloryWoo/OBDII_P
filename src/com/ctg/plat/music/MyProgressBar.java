package com.ctg.plat.music;

import com.ctg.ui.Base;
import com.ctg.ui.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.ProgressBar;

public class MyProgressBar extends ProgressBar {  
    private String text_progress;  
    private Paint mPaint;//画笔  
    int mProgress;
    //Bitmap dotbmap;
    Context mContext;
    int mWidth;
    int mHeight;
    
    public MyProgressBar(Context context) {  
        super(context);  
        mContext = context;
        initPaint();  
    }  
    public MyProgressBar(Context context, AttributeSet attrs) {  
        super(context, attrs);
        mContext = context;
        initPaint();  
    }  
    public MyProgressBar(Context context, AttributeSet attrs, int defStyle) {  
        super(context, attrs, defStyle); 
        mContext = context;
        initPaint();  
    }  
      
    protected void onMeasure(int width, int height){
    	super.onMeasure(width, height);
    	mWidth = width;
    	mHeight = height;
    }
    
    @Override  
    public synchronized void setProgress(int progress) {  
        super.setProgress(progress);  
        mProgress = progress;
       
        //setTextProgress(progress);  
    }  
    @Override  
    protected synchronized void onDraw(Canvas canvas) {  
        // TODO Auto-generated method stub  
        super.onDraw(canvas);  
        RectF rect=new RectF(0,0,mWidth*mProgress/100,mHeight);  
//        this.mPaint.getTextBounds(this.text_progress, 0, this.text_progress.length(), rect);  
//        int x = (getWidth() / 2) - rect.centerX();  
//        int y = (getHeight() / 2) - rect.centerY();  
        //canvas.drawBitmap(dotbmap, getWidth()*i_progress*Base.mDensity/100, 0, mPaint);
        //canvas.drawText(this.text_progress, x, y, this.mPaint);         
        //canvas.drawRoundRect(rect, 0.3f, 0.3f, mPaint);
    }  
    /** 
     *  
     *description: 初始化画笔 
     *Create by lll on 2013-8-13 下午1:41:49 
     */  
    private void initPaint(){  
        this.mPaint=new Paint();  
        this.mPaint.setAntiAlias(true);  
        this.mPaint.setColor(R.color.lightblue);        
        //dotbmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.iv_progressbar);
    }  
    private void setTextProgress(int progress){   
//        int i = (int) ((progress * 1.0f / this.getMax()) * 100);  
//        this.text_progress = String.valueOf(i) + "%";  
    }  
  
  
  
}  