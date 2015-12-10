package com.ctg.ui;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;


import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;

import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import android.view.ViewGroup.LayoutParams;

import android.widget.Button;

import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import android.widget.RelativeLayout;
import android.widget.ScrollView;

import android.widget.TextView;


import com.ctg.util.DTCsDetailDialog;
import com.ctg.util.DetailScrollView;
import com.ctg.util.MyScrollView;
import com.ctg.util.Util;

public class DTCs_Scroll {
	private static String TAG = "DTCs_Scroll";
	final static int LOAD_DETAIL_READY = 0x100;
	final static int INIT_LOAD_DETAIL_READY = 0x101;
	final static int NO_NEED_LOAD = 0x110;
	final static int DTC_QUEUE_SENT_ITEM = 0x200;
	final static String DTC_CODE_TITLE = "code+title"; 
	final static String[] dtc_code = {"C0129", "P0369", "P0720", "P1426", "P1504"};	
	Button dtc_btn[];
	Base mContext;
	Button clearDtcs;
	//int scr_width;
	//int scr_height;
	public RelativeLayout relativeLay;
	public MyScrollView dtcScroll;
	
	public LinearLayout dtcScrollLinear;
	LinearLayout linearDot;
	LinearLayout tempLinear;
	//public List<LinearLayout> abstrctViewLst;
	//static public LinearLayout s_curAbsView;
	List<String> codeLst;	
	List<String> abstractLst;
	//LoadDTCsDetailThread loadDtcThread;
	String fullpath;
	public volatile static boolean loadDtcThreadRun = false;
	
	public volatile static boolean needLoad = false;
	float pos_x_start = 0;
	float pos_x_end = 0;
	int default_x = 320;
	public int curId = -1;
	public int loadId = 0;
	public int curCount = 0;
	
	public volatile static Queue<String> dtcQueue;
	public ArrayList<ImageView> bmLst;
	DtcQueueProcessThread dtcProcessThd;
	public boolean dtcQueueThdRun;
	
	private static final int WC = ViewGroup.LayoutParams.WRAP_CONTENT;
	private static final int FP = ViewGroup.LayoutParams.MATCH_PARENT;
	public static LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(FP, FP);
	private static String mDtcCodeTitle;
	private static Thread waitForTopThd;
	private static ArrayList<String> dtcArr;
	//final Semaphore dtcProcessSemp;
	//final Semaphore uiSemp;
//	final Semaphore loadQueueSemp;
//	private android.widget.Button.OnClickListener btnListen = new android.widget.Button.OnClickListener(){
//
//		@Override
//		public void onClick(View v)
//		{
//			String xmlPath = dtc_code[curId];
//			DTCsDetailDialog dialog = new DTCsDetailDialog(mContext, 320, 520, R.layout.dtcs_detail, R.style.Theme_dialog, xmlPath);
//			dialog.show();					
//		}
//		
//	};
	
	private View.OnTouchListener abstractOnTouch = new View.OnTouchListener(){

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			// TODO Auto-generated method stub
			int length = dtcScrollLinear.getChildCount();//abstrctViewLst.size();
			
			if(length == 1)
				return false;
			if(event.getAction() == MotionEvent.ACTION_DOWN){
				pos_x_start = event.getX();	
			}
			else if(event.getAction() == MotionEvent.ACTION_MOVE){
				int cur = v.getId(), next;
				pos_x_end = event.getX();
				if(pos_x_start != 0 && Math.abs(pos_x_end-pos_x_start) > 30){
					if(pos_x_end > pos_x_start){
						if(v == dtcScrollLinear.getChildAt(length-1)){
							dtcScroll.scrollTo((int)v.getX()-default_x, (int)v.getY());
							next = 0;
						}
						else{
							dtcScroll.scrollTo((int)v.getX()+default_x, (int)v.getY());
							next = cur+1;
						}
					}
					else{
						if(v == dtcScrollLinear.getChildAt(0)){
							dtcScroll.scrollTo((int)v.getX()+default_x, (int)v.getY());
							next = length-1;
						}
						else{
							dtcScroll.scrollTo((int)v.getX()-default_x, (int)v.getY());
							next = cur-1;
						}
					}
					resetLinearDot(cur, next);
					curId = next;
				}
				pos_x_start = 0;
				pos_x_end = 0;
			}
			return true;
		}
		
	};
	
	public void DTCs_Destroy(){
		tempLinear = null;
		codeLst = null;
		abstractLst = null;
		dtcQueue = null;
		releaseBMList();
		bmLst = null;
	}
	
	public DTCs_Scroll(Context cont) {
		
		//ImageButton tmpBtn;
		mContext = (Base) cont; 
		
		//WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);

		//scr_width = wm.getDefaultDisplay().getWidth();
		//scr_height = wm.getDefaultDisplay().getHeight();
		/*tableLayout = (TableLayout) View.inflate(cont, R.layout.dtcs_whole, null);
        
		// TODO Auto-generated constructor stub
        addRow(); 
        
        mContext.dtc_c.addView(tableLayout);
        
        clearDtcs = new Button(mContext);
        clearDtcs.setText(R.string.clear_obd_dtcs);*/  
		relativeLay = (RelativeLayout) View.inflate(mContext, R.layout.dtcs_scroll_none, null);
		mContext.dtc_c.addView(relativeLay, params);		

	}
	
	public void initDtcSroll(){
		dtcQueue = new LinkedList<String>();
		dtcProcessThd = new DtcQueueProcessThread();
		bmLst = new ArrayList<ImageView>();
		relativeLay = (RelativeLayout) View.inflate(mContext, R.layout.dtcs_scroll, null);
		mContext.dtc_c.removeAllViews();
		mContext.dtc_c.addView(relativeLay);
		dtcScroll = (MyScrollView) relativeLay.findViewById(R.id.dtcs_scroll);		
		//dtcScroll.setOnDragListener(dtcScrollDrag);
		dtcScrollLinear = (LinearLayout) dtcScroll.findViewById(R.id.dtcs_scroll_linear);
		//dtcScrollLinear.setLayoutParams(params)
		linearDot = (LinearLayout) relativeLay.findViewById(R.id.several_dot);
		
		//abstrctViewLst = new ArrayList<LinearLayout>();
		codeLst = new ArrayList<String>();
		abstractLst = new ArrayList<String>();
		//for test
		//for(int i = 0; i < 5; i++){
		//	addDtcAbstract(dtc_code[i], dtc_code[i]+" abstracted content "+i);
		//}
		//curCode = dtc_code[0];
		//curAbstract = dtc_code[0]+" abstracted content "+0;
		
		//loadDTCsDetail();
		//linearDot.getChildAt(0).setBackgroundResource(R.drawable.ic_text_dot);
		//new Thread(dtcProcessThd).start();
		
	}
	
	public void clear_dtc(){
		if(curCount != 0){
			DTCs_Destroy();
			curCount = 0;
			mContext.dtc_c.removeAllViews();
			relativeLay = (RelativeLayout) View.inflate(mContext, R.layout.dtcs_scroll_none, null);
			mContext.dtc_c.addView(relativeLay);
			Toast.makeText(mContext, R.string.dtc_cleared, Toast.LENGTH_SHORT).show();
		}
	}
	
	public void threadAddDtcAbstractImplement(String dtcCodeTitle){
		int idx = dtcCodeTitle.indexOf("+");
		int len = dtcCodeTitle.length();
		String dtcCode = dtcCodeTitle.substring(0, idx);
		String dtcTitle = dtcCodeTitle.substring(idx+1, len);
		
		if(curCount == 0)
			initDtcSroll();
		
		int i = 0;
		
		int lenLst = codeLst.size();
		boolean existDtc = false;
		boolean existDtcDetail = false;
		if(lenLst != 0)
			for(i = 0; i < lenLst; i++){
				String item = codeLst.get(i);
				if(item.equals(dtcCode)){
					existDtc = true;
					break;
				}
				
			}
		if(curId == -1){
			existDtcDetail = addDtcAbstract(dtcCode, dtcTitle);
			linearDot.getChildAt(curId).setBackgroundResource(R.drawable.ic_text_dot);
		}
		else if(Base.OBDApp.getFocusId() == curId){
			if(!existDtc){
				existDtcDetail = addDtcAbstract(dtcCode, dtcTitle);
				curId = Base.OBDApp.getFocusId();				
			}			
			return;
		}
		else if(existDtc){
			if(curId == i)
				return;
			
			resetLinearDot(curId, i);
			unLoadDTCsDetail(curId);
			curId = i;
//			dtcScroll.smoothScrollTo(dtcScroll.default_x*curId, 0);													
//			mContext.dtc_s.loadDTCsDetail();
//			return;
			for(String dtcItem : dtc_code){
				if(dtcItem.equals(codeLst.get(curId))){
					existDtcDetail = true;
					break;
				}				
			}
		}
		else{
			unLoadDTCsDetail(curId);
			ImageView dotImg = (ImageView) linearDot.getChildAt(curId);
			if(dotImg != null)
				dotImg.setBackgroundResource(R.drawable.ic_text_dot0);
			existDtcDetail = addDtcAbstract(dtcCode, dtcTitle);
			linearDot.getChildAt(curId).setBackgroundResource(R.drawable.ic_text_dot);
		}
		if(existDtcDetail){	//existDtc
//			needLoad = false;
//			while(loadQueueSemp.hasQueuedThreads())
//			{
//				try {
//					Thread.sleep(50);
//				} catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
			if(loadDtcThreadRun)
				return;
			dtcCodeTitle += "+" + curId;
			if(getDtcQueue().isEmpty()){//dtcProcessThd may stop, so new it
				getDtcQueue().add(dtcCodeTitle);
				//dtcProcessSemp.notify();
				dtcQueueThdRun = true;
				new Thread(dtcProcessThd).start();
				
			}
			else{
				getDtcQueue().clear();
				//dtcProcessSemp.notify();
//				try {
//					Thread.sleep(100L);
//				} catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
				getDtcQueue().add(dtcCodeTitle);
				//new Thread(dtcProcessThd).start();
			}
//			loadId = curId;
//			if(loadQueueSemp.hasQueuedThreads())
//				loadQueueSemp.release();
//			dtcQueue.notify();
			
		}
		else{
			Thread myThread = new DtcScollToThread();
			new Thread(myThread).start();
		}
	}
	public void threadAddDtcAbstract(String dtcCodeTitle){

		Log.e(TAG, "threadAddDtcAbstract enter");
		if(mContext == null || mContext.serviceConn == false){
			return;
		}
		if(!Base.isForeground(mContext) || mContext.vPager.getCurrentItem() != 0){
			if(dtcArr == null)
				dtcArr = new ArrayList<String>();
			dtcArr.add(dtcCodeTitle);
			if(waitForTopThd == null){
				waitForTopThd = new Thread(threadAddDtcAbstractRunnable);
				waitForTopThd.start();
			}			
			return;
		}
		threadAddDtcAbstractImplement(dtcCodeTitle);

		return;
//		Log.e(TAG, "threadAddDtcAbstract enter");
//		if(mContext == null || mContext.serviceConn == false){
//			return;
//		}
//		if(!Base.isForeground(mContext)){
//			if(dtcArr == null)
//				dtcArr = new ArrayList<String>();
//			dtcArr.add(dtcCodeTitle);
//			if(waitForTopThd == null){
//				waitForTopThd = new Thread(threadAddDtcAbstractRunnable);
//				waitForTopThd.start();
//			}			
//			return;
//		}
//		threadAddDtcAbstractImplement(dtcCodeTitle);


	}
	

	private Runnable threadAddDtcAbstractRunnable = new Runnable() {

		@Override
		public void run() {									
			// TODO Auto-generated method stub
			while(!Base.isForeground(mContext)
					|| mContext.vPager.getCurrentItem() != 0){
				try {
					Thread.sleep(500L);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if(dtcArr == null || dtcArr.size() == 0)
				return;
			
			mContext.runOnUiThread(new Runnable() {
				public void run() {
					String dtcTitle = "";
					int len = dtcArr.size();
					for(int i = 0; i < len; i++){
						dtcTitle = dtcArr.get(i);
						threadAddDtcAbstractImplement(dtcTitle);
					}
					dtcArr.clear();
					dtcArr = null;
					waitForTopThd = null;
				}
			});
		} 
		
	};
	
	
	public boolean addDtcAbstract(String code, String abstrct){
//		if(curCount == 0)
//			initDtcSroll();
//		else
//			for(String itmStr: codeLst){
//				if(itmStr.equals(code))
//					return false;
//			}
		//curCode = code;
		//curAbstract = abstrct;
		
		codeLst.add(code);
		abstractLst.add(abstrct);
		RelativeLayout abstrctView = (RelativeLayout) View.inflate(mContext, R.layout.dtcs_abstract, null);
		//abstrctViewLst.add(abstrctView);
		TextView code_v;
		TextView cont_v;
		ScrollView scroll_v;
		Button btn;
		ImageView dot_v;
		boolean existDtcDetail = false;
		abstrctView.setId(curCount);
		curId = curCount;		
		
		code_v = (TextView)abstrctView.findViewById(R.id.dtc_abs_code);
		scroll_v = (ScrollView)abstrctView.findViewById(R.id.dtc_abs_scroll);
		//cont_v = (TextView)abstrctView.findViewById(R.id.dtc_abs_title);
		//btn = (Button)abstrctView.findViewById(R.id.dtc_abs_btn);
		//btn.setOnClickListener(btnListen);
		
		code_v.setText(mContext.getResources().getString(R.string.dtc_title)+"\n"+code);
		//cont_v.setText(abstrct);
		
		dtcScrollLinear.addView(abstrctView, params);
		
		dot_v = (ImageView)View.inflate(mContext, R.layout.dot, null);
		//abstrctView.setOnDragListener(abstrctViewDrag);
		//abstrctView.setOnTouchListener(abstractOnTouch);
		dot_v.setId(curCount);
		linearDot.addView(dot_v, (int)(56*Base.scale), (int)(60*Base.scale));
		curCount++;				
				
		for(String dtcItem : dtc_code){
			if(dtcItem.equals(codeLst.get(curId))){
				existDtcDetail = true;
				break;
			}				
		}
		DetailScrollView detailScrollView = (DetailScrollView)abstrctView.findViewById(R.id.dtc_abs_scroll);
		if(!existDtcDetail){
			TextView slt_txt = (TextView) View.inflate(mContext, R.layout.dtc_text, null);
			slt_txt.setTextColor(0xfffacb3d);
			slt_txt.setText(abstractLst.get(curId));			
			detailScrollView.addView(slt_txt);		
		}
		else{    	
		}
		//dtcScroll.smoothScrollTo(dtcScroll.default_x*curId, 0);
			//loadDTCsDetail();
		//linearDot.getChildAt(curId).setBackgroundResource(R.drawable.ic_text_dot);
		 
//		if(curId > 0){
//			boolean lastExistDtc = false;
//			for(String dtcItem : dtc_code){
//				if(dtcItem.equals(codeLst.get(curId-1))){
//					lastExistDtc = true;
//					break;
//				}				
//			}
//			if(lastExistDtc)
//				unLoadDTCsDetail(curId-1);
//			linearDot.getChildAt(curId-1).setBackgroundResource(R.drawable.ic_text_dot0);
//		}
		return existDtcDetail;
		//if(curCount>1){
		//	linearDot.getChildAt(curCount-1).setBackgroundResource(R.drawable.ic_text_dot0);
		//}
//		return false;
	}
	

	public void unLoadDTCsDetail(int id){
		boolean existDtc = false;
		RelativeLayout curLinear = (RelativeLayout) dtcScrollLinear.getChildAt(id);
		if(curLinear == null)
			return;
		DetailScrollView detailScrollView = (DetailScrollView)curLinear.findViewById(R.id.dtc_abs_scroll);
		//LinearLayout detailScroll = (LinearLayout)detailScrollView.findViewById(R.id.dtcs_dtlay);
		for(String dtcItem : dtc_code){
			if(dtcItem.equals(codeLst.get(id))){
				existDtc = true;
				break;
			}				
		}
		if(existDtc){
			detailScrollView.removeAllViews();
			releaseBMList();
		}

	}
	
	public void loadDTCsDetail(){	
		boolean existDtc = false;
		RelativeLayout curLinear = (RelativeLayout) dtcScrollLinear.getChildAt(curId);
		DetailScrollView detailScrollView = (DetailScrollView)curLinear.findViewById(R.id.dtc_abs_scroll);
		//LinearLayout detailScroll = (LinearLayout)detailScrollView.findViewById(R.id.dtcs_dtlay);
		for(String dtcItem : dtc_code){
			if(dtcItem.equals(codeLst.get(curId))){
				existDtc = true;
				break;
			}				
		}
		if(!existDtc){
			return;
		}		
		RelativeLayout progress = (RelativeLayout) View.inflate(mContext, R.layout.progressbar, null);
		detailScrollView.removeAllViews();
		detailScrollView.addView(progress);
		progress.setY(240*Base.scale);
		//s_curAbsView = abstrctViewLst.get(curId);
					
		//fullpath = Base.getSDPath()+"/OBDII/dtcs/xml/" + codeLst.get(curId) + ".xml"; 
		fullpath =  "xml/"+codeLst.get(curId) + ".xml";
		if(!loadDtcThreadRun){
			loadDtcThreadRun = true;
			//loadId = curId;
			needLoad = true;
			new LoadDTCsDetailThread().start();
		}
		else{
//			needLoad = false;			
//			while(loadDtcThreadRun){
//				try {
//					Thread.sleep(300L);
//				} catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
//			needLoad = true;
//			new LoadDTCsDetailThread().start();
		}	
		
		
			
			
	}
	
	public void resetLinearDot(int curId, int nextId){
		ImageView cur_v, next_v;
		
		cur_v = (ImageView) linearDot.getChildAt(curId);
		next_v = (ImageView) linearDot.getChildAt(nextId);
		if(cur_v != null)
			cur_v.setBackgroundResource(R.drawable.ic_text_dot0);
		if(next_v != null)
			next_v.setBackgroundResource(R.drawable.ic_text_dot);
	}
	
	 private byte[] InputStreamToByte(InputStream is) throws IOException {
        ByteArrayOutputStream bytestream = new ByteArrayOutputStream();
        int ch;
        while ((ch = is.read()) != -1) {
            bytestream.write(ch);
        }
        byte imgdata[] = bytestream.toByteArray();
        bytestream.close();
        return imgdata;
    }
	 
	public void prepareDtcsDetail(String xmlPath , LinearLayout linear){
		TextView slt_txt;
		ImageView img_v;
		String path = null;
		InputStream abpath = null;
		try {
			abpath = mContext.getAssets().open(xmlPath);
//			path = new String(InputStreamToByte(abpath1));
//			File file = new File(path);    	
	        Document doc = null;
			doc = DocumentBuilderFactory.newInstance()  
			        .newDocumentBuilder().parse(abpath);
			
	        NodeList nodeList = doc.getElementsByTagName("step");
	        
	        String nodeVal = null;
	        Element ele;
	        Node node;
	        String localNm;
	        String imageFold = Base.getSDPath()+"/OBDII/dtcs/pic/";
	        String imagePath;
	        File img_file;
	        Bitmap bm;
	        BitmapFactory.Options opts = new BitmapFactory.Options();  
	        opts.inSampleSize = 1;
	        AttributeSet attr;
	        ImageView gap_v;	        
	        LayoutParams para;	     
	        int resId;
	        
	        slt_txt = (TextView) View.inflate(mContext, R.layout.dtc_text, null);
			slt_txt.setText(abstractLst.get(curId));
			slt_txt.setTextColor(0xfffacb3d);
			linear.addView(slt_txt);
	        for (int i = 0; i < nodeList.getLength(); i++) {  
	            ele = (Element) nodeList.item(i);  
	        	node = ele.getFirstChild();
	        	while(node != null){
	        		/*localNm = node.getLocalName();
	        		if(localNm == null){
	        			node = node.getNextSibling();
	        			continue;
	        		}*/	
	        		String name = node.getNodeName();
	        		short type = node.getNodeType();
	        		
	        		if(node.hasChildNodes()){
	        			nodeVal = node.getFirstChild().getNodeValue();
	        			if(nodeVal.startsWith("C") || nodeVal.startsWith("P")){
	        				String littleVal = nodeVal.toLowerCase();
	        				littleVal = littleVal.replace("-", "_");
		        			resId = Util.getImage(littleVal);
		        			if(resId != 0){
			        			img_v = (ImageView) View.inflate(mContext, R.layout.dtc_image, null);
			        			img_v.setImageResource(resId);	
			        			
	//		        			imagePath = imageFold + nodeVal + ".jpg";			        			
	//	        				bm = BitmapFactory.decodeFile(imagePath, opts);	
	//	        				if(bm == null){
	//	        					Log.e(TAG, "bitmap decode failed");
	//	        					return;
	//	        				}
		        				bmLst.add(img_v);
	//		        			img_v.setImageBitmap(bm);				        			
			        			linear.addView(img_v, (int)(600*Base.scale), (int)(460*Base.scale));
		        			}
//		        	        gap_v = new ImageView(mContext);	        
//		        	        para = new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
//		        	        para.width = LayoutParams.MATCH_PARENT;
//		        	        para.height = 1;	 
//		        	        gap_v.setBackgroundColor(Color.WHITE);
//		        	        gap_v.setLayoutParams(para);	
//		        	        linear.addView(gap_v);
	        			}
	        			else if(nodeVal.matches("[0-9].+")){
	        				slt_txt = (TextView) View.inflate(mContext, R.layout.dtc_text, null);
		        			slt_txt.setText(nodeVal);
		        			linear.addView(slt_txt);
	        			}
	        		}
	        		node = node.getNextSibling();
	        	}
	        	
	        }
	        abpath.close();
	       
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
	}	
		
    class LoadDTCsDetailThread extends Thread{
    	
	    public synchronized void run() {
	    	//LinearLayout curLinear = abstrctViewLst.get(curId);
	    	//DetailScrollView detailScrollView = (DetailScrollView)s_curAbsView.findViewById(R.id.dtc_abs_scroll);
			//LinearLayout detailScroll = (LinearLayout)detailScrollView.findViewById(R.id.dtcs_dtlay);
	    	while(dtcScroll.isStart){
	    		try {
					Thread.sleep(30L);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	    	}

	    	mContext.runOnUiThread(new Runnable() {
				public void run() {
//			    	if(mContext == null || !mContext.serviceConn)
//						return;
			    	if(needLoad){
				    	if(tempLinear != null){
				    		tempLinear.removeAllViews();
				    		releaseBMList();
				    	}
				    	else
				    		tempLinear = (LinearLayout)View.inflate(mContext, R.layout.linear, null);
				    	prepareDtcsDetail(fullpath, tempLinear);
			    	}
//			    	if(needLoad)
//			    		mLoadDtcsHandler.obtainMessage(LOAD_DETAIL_READY).sendToTarget();
//			    	loadDtcThreadRun = false;
				}
	    	});

	    	if(needLoad)
	    		mLoadDtcsHandler.obtainMessage(LOAD_DETAIL_READY).sendToTarget();
	    	loadDtcThreadRun = false;
	    	//detailScroll.removeAllViews();
			//detailScrollView.loadDtcsDetail(fullpath);
	    }
   }
    
    public Handler mLoadDtcsHandler = new Handler(){
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub			
			if(msg.what == LOAD_DETAIL_READY){
				//if(loadDtcThreadCount > 0)
				//	loadDtcThreadCount--;
				//LinearLayout curLinear = abstrctViewLst.get(curId);
				//dtcQueue.poll();
//				if(loadDtcThreadRun)
//					loadDtcThreadRun = false;
//				if(!needLoad)
//					return;
				//if(loadId != curId)
				//	return;
		    	DetailScrollView detailScrollView = (DetailScrollView)dtcScrollLinear.getChildAt(curId).findViewById(R.id.dtc_abs_scroll);
		    	detailScrollView.removeAllViews();		    	
		    	detailScrollView.addView(tempLinear);
	    		if(mContext.localbinder != null)
		    		mContext.localbinder.clearSingleNoti(codeLst.get(curId));
//		    	needLoad = false;
			}
			else if(msg.what == INIT_LOAD_DETAIL_READY){
				Log.d(TAG, "INIT_LOAD_DETAIL_READY loadId="+loadId+"  curId="+curId);
				if(loadId != curId)
					return;
		    	DetailScrollView detailScrollView = (DetailScrollView)dtcScrollLinear.getChildAt(curId).findViewById(R.id.dtc_abs_scroll);
		    	detailScrollView.removeAllViews();		    	
		    	detailScrollView.addView(tempLinear);
		    	
//		    	if(mContext.tabHost.getCurrentTab() != 0){
//		    		mContext.tabHost.setCurrentTab(0);
//		    		//dtcScroll.smoothScrollTo(dtcScroll.default_x*curId, 0);
//		    		dtcScroll.post(new Runnable() {  
//		    		    @Override  
//		    		    public void run() {  
//		    		    	//dtcScroll.smoothScrollTo(dtcScroll.default_x*curId, 0);
//		    		    	dtcScroll.setScrollX(dtcScroll.default_x*curId);
//		    		    	if(mContext.localbinder != null)
//		    		    		mContext.localbinder.clearSingleNoti(codeLst.get(curId));
//		    		    }   
//		    		});
//		    	}
//		    	else{
//		    		dtcScroll.setScrollX(dtcScroll.default_x*curId);
//		    		if(mContext.localbinder != null)
//    		    		mContext.localbinder.clearSingleNoti(codeLst.get(curId));
//		    	}
		    		//dtcScroll.smoothScrollTo(dtcScroll.default_x*curId, 0);
//		    	else{
//					Thread myThread = new DtcScollToThread();
//					new Thread(myThread).start();
//		    	}
			}
			else if(msg.what == NO_NEED_LOAD){
				dtcScroll.scrollTo(dtcScroll.default_x*curId, 0);	
			}
			else if(msg.what == DTC_QUEUE_SENT_ITEM){
//				Bundle bund = msg.getData();
//				String dtcCodeTitle = bund.getString(DTC_CODE_TITLE);				
//				int idx = dtcCodeTitle.indexOf("+");
//				int len = dtcCodeTitle.length();
//				String dtcCode = dtcCodeTitle.substring(0, idx);
//				String dtcTitle = dtcCodeTitle.substring(idx+1, len);		
//				addDtcAbstract(dtcCode, dtcTitle);					
//				if(!loadDtcThreadRun){				
//					loadDTCsDetail();
//				}
			}
		}

	};  
	
	public synchronized Queue<String> getDtcQueue(){
		return dtcQueue;
	}
	
	class DtcQueueProcessThread extends Thread{
		public synchronized void run() {
			mContext.runOnUiThread(new Runnable() {
				public void run() {
					while(!getDtcQueue().isEmpty()){//!getDtcQueue().isEmpty()				
						if(mContext == null || !mContext.serviceConn)
							return;
						//dtcQueueThdRun = true;
						String dtcCodeTitle = getDtcQueue().poll();
						int idx1 = dtcCodeTitle.indexOf("+");
						int idx2;
						//int len = dtcCodeTitle.length();
						String curloadIdStr;
						int curLoadId;
						
						String dtcCode = dtcCodeTitle.substring(0, idx1);
						String remain = dtcCodeTitle.substring(idx1+1);
						idx2 = remain.indexOf("+");
						curloadIdStr = remain.substring(idx2+1);
						curLoadId = Integer.valueOf(curloadIdStr);
						Log.d(TAG, "DtcQueueProcessThread curLoadId="+curLoadId+"  curId="+curId);
						if(curLoadId == curId){						
							//fullpath = Base.getSDPath()+"/OBDII/dtcs/xml/" + dtcCode + ".xml";
							fullpath =  "xml/"+dtcCode + ".xml";
					    	if(tempLinear != null){
					    		tempLinear.removeAllViews();
					    		releaseBMList();
					    	}
					    	else
					    		tempLinear = (LinearLayout)View.inflate(mContext, R.layout.linear, null);
					    	prepareDtcsDetail(fullpath, tempLinear);
						}
	//					try {
	//						sleep(200L);
	//					} catch (InterruptedException e) {
	//						// TODO Auto-generated catch block
	//						e.printStackTrace();
	//					}
						if(mContext == null || !mContext.serviceConn)
							return;
						if(curLoadId == curId){
							loadId = curId;
							//mLoadDtcsHandler.obtainMessage(INIT_LOAD_DETAIL_READY).sendToTarget();
							DetailScrollView detailScrollView = (DetailScrollView)dtcScrollLinear.getChildAt(curId).findViewById(R.id.dtc_abs_scroll);
					    	detailScrollView.removeAllViews();		    	
					    	detailScrollView.addView(tempLinear);
					    	
//					    	if(mContext.tabHost.getCurrentTab() != 0){
//					    		mContext.tabHost.setCurrentTab(0);
//					    		//dtcScroll.smoothScrollTo(dtcScroll.default_x*curId, 0);
//					    		dtcScroll.post(new Runnable() {  
//					    		    @Override  
//					    		    public void run() {  
//					    		    	//dtcScroll.smoothScrollTo(dtcScroll.default_x*curId, 0);
//					    		    	dtcScroll.setScrollX(dtcScroll.default_x*curId);
//					    		    	if(mContext.localbinder != null)
//					    		    		mContext.localbinder.clearSingleNoti(codeLst.get(curId));
//					    		    }   
//					    		});
//					    	}
//					    	else{
//					    		dtcScroll.setScrollX(dtcScroll.default_x*curId);
//					    		if(mContext.localbinder != null)
//			    		    		mContext.localbinder.clearSingleNoti(codeLst.get(curId));
//					    	}
						}					
					}
					mContext.dtc_c.postDelayed(new Runnable() {  
		    		    @Override  
		    		    public void run() {  
		    		    	//dtcScroll.smoothScrollTo(dtcScroll.default_x*curId, 0);
		    		    	dtcQueueThdRun = false;
		    		    }   
		    		}, 900);
				}
			});
			

			//dtcQueueThdRun = false;
		}
	}
	
	class DtcScollToThread extends Thread{
		public synchronized void run() {
			if(mContext == null || !mContext.serviceConn)
				return;
			//if(loadId == curId)
			mLoadDtcsHandler.obtainMessage(NO_NEED_LOAD).sendToTarget();
		}
	}
	
	void releaseBMList(){
		//Bitmap bm = null;
		ImageView v;
		if(bmLst == null)
			return;
		int len = bmLst.size();
		int i = 0;
		Drawable d;
		while(i < len){
//			bm = bmLst.get(i);
//			if(bm != null && !bm.isRecycled())
//				bm.recycle();
			v = bmLst.get(i);
			d=((ImageView)v).getDrawable();
            if(d != null && d instanceof BitmapDrawable)
            {                        
                Bitmap bmp=((BitmapDrawable)d).getBitmap();
                bmp.recycle();
                bmp=null;
            }
            ((ImageView)v).setImageBitmap(null);
            if(d!=null){
                d.setCallback(null);
            }
			i++;
		}
		bmLst.clear();
		System.gc();
	}
}
