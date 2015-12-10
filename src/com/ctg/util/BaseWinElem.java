package com.ctg.util;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class BaseWinElem extends LinearLayout{

	
	public BaseWinElem(Context context) {
		super(context);				
		// TODO Auto-generated constructor stub
	}
	
	public BaseWinElem(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}
	
	protected void onDraw(Canvas canvas){
        super.onDraw(canvas);
        /*Paint paint = new Paint();
        float cx = 0, cy = 0;
        
        cx = getX()+60;
        cy = getY()+60;
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawCircle(cx, cy, 100, paint);*/
        
	}
	public void setImage(int imgId){
		getImage().setImageResource(imgId);
	}
	
	public void setText(String content){
		getText().setText(content);
	}
	
	public ImageView getImage(){		
		return (ImageView)getChildAt(0);
	}
	
	public TextView getText(){
		return (TextView)getChildAt(1);
	}
}
