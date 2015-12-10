package com.ctg.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.ImageView;


public class GasBar extends ImageView{

	Paint paint;
	public int gas_volumn = 0;
	float scale;
	public Context mContext;
	int width, height;
	float eachRectW;
	RectF rectf;
//	int gasConsume;// gas consume per 100km
	public GasBar(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	public GasBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		scale = context.getResources().getDisplayMetrics().density/2;
		mContext = context;
		rectf = new RectF();
	}

	public void setGasVolum(float vol)
	{
		gas_volumn = Math.round(vol);
		postInvalidate();
	}

	
	protected void onMeasure(int w, int h){
		super.onMeasure(w, h);
		width = this.getWidth();
		height = this.getHeight();
		eachRectW = width/30f;
//		rectf = new RectF(0, 0, eachRectW*0.9f, height);		
	}
	
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		paint = new Paint(); //           
        paint.setStrokeJoin(Paint.Join.ROUND);    
        paint.setStrokeCap(Paint.Cap.ROUND);    
	    paint.setAntiAlias(true);    
	    paint.setStyle(Style.FILL_AND_STROKE); 
	    canvas.save();
	    rectf.set(0, 2, eachRectW*0.9f, height-2);
	    for(int i = 0; i < gas_volumn; i++){    		    		
	    	if(i < 10)
	    		paint.setColor(Color.GREEN);
	    	else if(i < 20)
	    		paint.setColor(Color.YELLOW);
	    	else
	    		paint.setColor(Color.RED);
	    	canvas.drawRect(rectf, paint);	
	    	rectf.left += eachRectW;
	    	rectf.right = rectf.left+eachRectW*0.9f;
	    }
	    canvas.restore();
	}   	
	
}
