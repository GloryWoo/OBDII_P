package com.ctg.widget;

import com.ctg.ui.Base;
import com.ctg.ui.R;
import com.ctg.util.CustomDialog;
import com.ctg.util.MySurfaceView;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;

import android.widget.ImageView;


public class Gas extends ImageView{

	Paint paint;
	public float gas_volumn = 0;
	public int gasChart = 0;
	float scale;
	public MySurfaceView surfaceGasVolume;
	public Context mContext;
	CustomDialog chartDialog;
//	int gasConsume;// gas consume per 100km
	public Gas(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	public Gas(Context context, AttributeSet attrs) {
		super(context, attrs);
		scale = context.getResources().getDisplayMetrics().density/2;
		mContext = context;
		setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
//				int height = Base.OBDApp.landScapeMode != 0 ? 360 : 420;
//				chartDialog = new CustomDialog(mContext, 360, height, R.layout.surface, R.style.Theme_dialog);				
//				chartDialog.show();
//				surfaceGasVolume = (MySurfaceView) chartDialog.findViewById(R.id.myView);
//				surfaceGasVolume.setYScale(10);
//				surfaceGasVolume.setType(3);				
			}
			
		});
		
		setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				if(event.getAction() == MotionEvent.ACTION_DOWN){
					//v.setBackgroundResource(R.drawable.focusdial);
					v.postDelayed(new Runnable() {  
		    		    @Override  
		    		    public void run() {  
		    		    	Gas.this.setBackgroundResource(R.drawable.defaultdial);
		    		    }   
		    		}, 300);
				}
				return false;
			}
		});				
	}


	public void setGasVolumn(float volumn){
		gas_volumn = volumn;
		postInvalidate();	
	}

	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		float  y=255*scale; 
		float  offset_pos = 230f;
	    int count = 36; //
	    
		paint = new Paint(); //
        paint.setColor(Color.WHITE);    
        paint.setStrokeJoin(Paint.Join.ROUND);    
        paint.setStrokeCap(Paint.Cap.ROUND);    
        paint.setStrokeWidth(4); 
        paint.setTextSize(36f*scale);
	    paint.setAntiAlias(true);    
	    paint.setStyle(Style.FILL_AND_STROKE);    
	    canvas.translate(canvas.getWidth()/2, canvas.getHeight()/2); 
	    canvas.save();
	    Paint tmpPaint = new Paint(paint); //  
	    tmpPaint.setStrokeWidth(1); 
	    for(int i = 0; i < 31; i++){    
	        if(i%2 == 0){
	        	if(i == 30)
	        		paint.setColor(Color.RED);
	            canvas.drawLine(0f, y, 0f, y+12f*scale, paint);    
	            //canvas.drawText(String.valueOf(i/2), -6f, y-6f, tmpPaint);    
	          
	        }else{    
	            canvas.drawLine(0f, y, 0f, y+5f*scale, tmpPaint);    
	        }    	        
	        canvas.rotate(360/count,0f,0f); //   
	    }  
	    paint.setColor(Color.WHITE); 
	    canvas.restore();
	    canvas.save();
	    double deg;
	    float x_pos, y_pos;
	    for(int i = 0; i < 16; i++){    	            	             
            if(i == 0)
            	canvas.drawText(String.valueOf(i*2), -10f*scale, y-6f*scale, tmpPaint);	            	
            else if(i < 9){
            	deg = (270-20*i)/180f*Math.PI;
            	x_pos =   (float)(offset_pos*Math.cos(deg));
            	y_pos = (float)(offset_pos*Math.sin(deg));
            	y_pos = -y_pos;
            	canvas.drawText(String.valueOf(i*2), (-12f+x_pos)*scale, (10f+y_pos)*scale, tmpPaint);
            }
            else if(i == 9){
            	canvas.drawText(String.valueOf(i*2), -20f*scale, (10f-offset_pos)*scale, tmpPaint);
            }
            else{
            	deg = (270-20*i)/180f*Math.PI;
            	x_pos =   (float)(offset_pos*Math.cos(deg));
            	y_pos = (float)(offset_pos*Math.sin(deg));
            	y_pos = -y_pos;
            	canvas.drawText(String.valueOf(i*2), (-24f+x_pos)*scale, (10f+y_pos)*scale, tmpPaint);
            }
	        
	    }  
	    canvas.restore();
	    canvas.save();
//	    Paint tmpPaint1 = new Paint(paint); 	    
	    //canvas.drawLine(0f, 0f, 0, y-3f, tmpPaint1);
	    RectF outerRect = new RectF(-4f*scale, 6f*scale, 4f*scale, y-12f*scale);
	    paint.setStyle(Style.FILL);
//	    tmpPaint1.setColor(Color.BLACK);
//		    canvas.drawCircle(0, 0, 4f*scale, tmpPaint1);
	    canvas.rotate(gas_volumn*10,0f,0f);
//	    tmpPaint1.setColor(Color.WHITE);
	    paint.setColor(Color.WHITE);
	    canvas.drawCircle(0, 0, 12f, paint);
	    canvas.drawRoundRect(outerRect, 0, 0, paint);
	    canvas.restore();
	}   	
	
}
