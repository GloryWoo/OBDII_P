package com.ctg.util;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class MySurfaceView extends SurfaceView implements SurfaceHolder.Callback{


	private MyThread myThread; 
	private SurfaceHolder holder;
	public static int Height = 380;
	public static int Width = 360;
	public Context mContext;
	protected int y_scale = 1;
	protected int mType = 0; //1 rpm, 2 km/h, 3 ℃
	final int count = 320;
//	protected List<Integer> yValLst;
//	protected int listCnt;
	protected int drawCnt = 0;
	long dataCnt;
	int curDataI;
	int lastDataI;
	
	//test
	public boolean testThreadFlag;
	
	public MySurfaceView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public MySurfaceView(Context context, AttributeSet attrs) {
	    super(context, attrs);
	    // TODO Auto-generated constructor stub
	    holder = this.getHolder();
	    holder.addCallback(this);
	    mContext = context;
	    //将surfaceView背景变为透明
	    setZOrderOnTop(true);
	    getHolder().setFormat(PixelFormat.TRANSLUCENT);
//	    yValLst = new ArrayList<Integer>();
	}
	
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// TODO Auto-generated method stub
	    myThread = new MyThread(holder);

        myThread.isRun = true;
        myThread.start();   
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		System.out.println("surfaceDestroyed");
        myThread.isRun = false;
        myThread = null;
        
        testThreadFlag = false;
	}
	
	public void setYScale(int scale){
		y_scale = scale;
	}
	
	public void setType(int type){
		mType = type;
	}
	
	public void setYValue(int val){
		lastDataI = curDataI;
		curDataI = val;
		dataCnt++;
	}
	
	class MyThread extends Thread
    {
        private SurfaceHolder holder;
        public boolean isRun ;
        public  MyThread(SurfaceHolder holder)
        {
            this.holder =holder; 
            isRun = true;
        }
        @Override
        public void run() {
        	
        	float density = getResources().getDisplayMetrics().density;
//            int[] dData = new int[count];
            float x = 0;
            int X = 0;//真x，实际画在图上的x
            int oldX = X;//真oldx,实际画在图上的oldx,上一个X的坐标
            float[] y= new float[count];//上一个纵轴坐标
//            float y0 = (46*Height)/56f;//纵轴0点位置
            int[] dFlag = new int[count];
            int i = 0, j = 0;
            


            Paint pt = new Paint();
            Paint p_gray = new Paint();
            
            p_gray.setColor(Color.DKGRAY);
            p_gray.setStrokeWidth(2);
//            p_gray.setAlpha(60);
            pt.setTextSize(16f*density);
            pt.setColor(Color.WHITE);
            pt.setStrokeWidth(4);
            Paint pt_U = new Paint(pt);
            pt_U.setAlpha(80);
            int padding = 8;
//            Canvas canvs = holder.lockCanvas(new Rect(0, 0, MySurfaceView.Width, MySurfaceView.Height));
//            holder.unlockCanvasAndPost(canvs);
            int moveX = (int) (Width/100f*density);
            int gap = (int)(40*density);
            int gap_scale = 33;
            int gap_scaleY = 34;
            float curData;
            float lastData;
            x = (int)(Width*density)-gap;
            X = (int) x;
            oldX = X-moveX;
            while(isRun) {
                Canvas c = null;
                Paint p = new Paint();
                //得到数据
//                for(i = 0; i<listCnt; i++) {
//                    //dData[i] = MainActivity.dummyData[i];
//                	curData = yValLst.get(i)*100/y_scale;
//                	if(curData < 101){
//                		dData[i] = curData;
//                		y[i] = ((100 - dData[i])*41f*Height)/(40f*56f)+9*Height/56f;
//                	}
//                }

                try {
                    synchronized (holder) {

                    	
                        //c = holder.lockCanvas(new Rect(gap, 0, (int)(MySurfaceView.Width*density)-gap, (int)(MySurfaceView.Height*density)-gap));//(new Rect((int)oldX, (int) ((MySurfaceView.Height-42)*density), (int)X+3, MySurfaceView.Height));  
                        //c.drawColor(Color.TRANSPARENT,PorterDuff.Mode.CLEAR);
                        //c.translate(moveX, 0);
                        
                        //holder.unlockCanvasAndPost(c); 
//                        if(x >= MySurfaceView.Width*density-gap){
//                            // 清除画布
//                            //clear();                              	
//                            c = holder.lockCanvas(new Rect(gap, 0, (int)(MySurfaceView.Width*density)-gap, (int)(MySurfaceView.Height*density)-gap));//(new Rect((int)oldX, (int) ((MySurfaceView.Height-42)*density), (int)X+3, MySurfaceView.Height));  
//                            c.drawColor(Color.TRANSPARENT,PorterDuff.Mode.CLEAR);                            
//                            x = 40*density;
//                            X = (int) x;
//                            oldX = X;
//                            //c.translate(x*10, 0);
//                            holder.unlockCanvasAndPost(c);
//                            
//                        }
//                        else 
                        {
                            c = holder.lockCanvas(new Rect(gap, 0, (int) (Width*density)+4, (int)(Height*density)-gap));//(new Rect((int)oldX, (int) ((MySurfaceView.Height-42)*density), (int)X+3, MySurfaceView.Height));
                                                                                                                   
                            c.drawColor(Color.TRANSPARENT,PorterDuff.Mode.CLEAR);// 清除画布
                           
                            
                            
                            //unit
                            if(mType == 1){
                            	c.drawText("X1000 rpm", 35*density, 25*density, pt_U);
                            }
                            else if(mType == 2){
                            	c.drawText("X20 km/h", 35*density, 25*density, pt_U);
                            }
                            else if(mType == 3){
                            	c.drawText("X10%", 35*density, 25*density, pt_U);
                            }
                            else if(mType == 4){
                            	c.drawText("X20 ℃", 35*density, 25*density, pt_U);
                            }
                            //x 
                            c.drawLine(padding*density+10, (Height-40)*density, (Width-padding)*density, (Height-40)*density, pt);
                            //y
                            c.drawLine(29*density, padding*density, 29*density, (Height-padding)*density, pt);
                            for(i = 0; i < 9; i++){
                            	//x
                            	c.drawLine((Width-gap_scale*(i+1))*density-7, (Height-40)*density, (Width-gap_scale*(i+1))*density-7, (Height-36)*density, pt);
                            	c.drawText(Integer.toString(i+1), (Width-gap_scale*(i+1)-8)*density, (Height-23)*density, pt);
                            	//y
                            	c.drawLine(25*density, gap_scaleY*(i+1)*density+4, 29*density, gap_scaleY*(i+1)*density+4, pt);                            	    
                            	c.drawLine(31*density, gap_scaleY*(i+1)*density+4, (Width-39)*density, gap_scaleY*(i+1)*density+4, p_gray);
                            	c.drawText(Integer.toString(9-i), 15*density, (gap_scaleY*(i+1)+6)*density, pt);
                            }
                            
                                             
//                            c.drawBitmap(memBm, clipRect, pastaRect, null);
                            
                            p.setColor(Color.GREEN);
                            //根据标志设置不同颜色
//                            if(dIeFlag[j] == 0)
//                                p.setColor(Color.GREEN);
//                            else
//                                p.setColor(Color.YELLOW);
                            
                            p.setStrokeWidth(3);
                            p.setAntiAlias(true);
//                            if(j == 0) {                            	
//                                j++;                            
//                            }
                            
                            for(i = j-1; i > 1; i--){
                            	if(oldX-(j-i)*moveX > 32*density)
                            		c.drawLine(oldX-(j-i)*moveX, y[i-1], X-(j-i)*moveX, y[i], p);
                            	else{
                            		drawCnt = j-i+1;
                            		break;
                            	}
                            }
                            //第一个点

//                            //如果y=0则不画多边形而画直线

//                            //如果y不等于0则画多边形填色
                            if(dataCnt > 1){
                            	p.setStyle(Paint.Style.FILL);
                            	
                        		curData = curDataI;                        		
                        		curData /= y_scale/10;                        	
                        		//c.drawLine(oldX-moveX, y[j-1], X-moveX, y[j-1], p);
                        		//y[j-1] = (Height*density-gap-4)*(100-lastData)/100;
                        		
                        		y[j] = (Height*density-gap-4)*(100-curData)/100;
                        		if(j > 0)
                        			c.drawLine(oldX, y[j-1], X, y[j], p);                                                                	                                
                                if(j < count-1)
                                	j++; 
                                else if(count > drawCnt && drawCnt > 30){                                	
                                	float temp[] = new float[count];
                                	System.arraycopy(y, count-drawCnt-1, temp, 0, drawCnt);
                                	j = drawCnt;
                                	y = temp;
                                	
                                }
                            }	                            
//                            else{
//                            	curData = 0;
//                            	lastData = 0;
//                                c.drawLine(oldX,(Height-42)*density, X, (Height-42)*density, p);
//                            }
                            
//                            oldX = X;
                            
//                            if(j == listCnt) j = 0;
                            
                            //需要按照坐标画点，若不省略点，则一共需要画：8秒/20毫秒=400个点
                            //首先算出精确的x坐标，是浮点数，然后四舍五入成整数画在画布上，保证点数正确
//                            x = x + MySurfaceView.Width/100f*density;
//                            X = Math.round(x);
                            //c.translate(x*100, 0);
                            
                            holder.unlockCanvasAndPost(c);
//                            MySurfaceView.this.draw(memC);
                        }                        
                        Thread.sleep(100);
                    }
                } catch (Exception e) {
                    // TODO: handle exception
                    e.printStackTrace();
                }
                
            }
        }
    }

}
