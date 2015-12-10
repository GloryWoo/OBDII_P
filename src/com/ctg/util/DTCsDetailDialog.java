package com.ctg.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.ctg.ui.Base;
import com.ctg.ui.R;

import android.app.Dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import android.util.AttributeSet;
import android.util.DisplayMetrics;

import android.view.Gravity;
import android.view.View;

import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;

import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import android.widget.TextView;


public class DTCsDetailDialog  extends Dialog {
	public Base mContext;
	public LinearLayout dtcs_layout;
	public ArrayList<ImageView> bmLst;
	ScrollView dtcScroll;
	TextView code_v;
	
	public DTCsDetailDialog(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public DTCsDetailDialog(Context context, int theme) {
		super(context, theme);
		// TODO Auto-generated constructor stub
	}
	public DTCsDetailDialog(Context context, int width, int height, 
			int layout, int style, String xmlPath, String intepret) {
		super(context, style);
		//set content
		mContext = (Base)context;
		setContentView(layout);
		dtcs_layout = (LinearLayout)View.inflate(mContext, R.layout.linear, null);;
		
		//mac_address_init();
		//set window params
		Window window = getWindow();
		WindowManager.LayoutParams params = window.getAttributes();
		//set width,height by density and gravity
		float density = getDensity(context);
		params.width = (int) (width*density);
		params.height = (int) (height*density);
		params.gravity = Gravity.TOP;
		//params.verticalMargin = 2.0F;
		window.setAttributes(params);
		

//		String fullpath = Base.getSDPath()+"/OBDII/dtcs/xml/" + xmlPath + ".xml";
		bmLst = new ArrayList<ImageView>();
		dtcScroll = (ScrollView) findViewById(R.id.dtc_item_scroll);
		dtcScroll.addView(dtcs_layout);
		loadDtcsDetail(xmlPath, dtcs_layout, intepret);

		setOnCancelListener(new OnCancelListener(){

			@Override
			public void onCancel(DialogInterface dialog) {
				// TODO Auto-generated method stub
				releaseBMList();
			}
			
		});
	}

	
	private float getDensity(Context context) {
		Resources resources = context.getResources();
		DisplayMetrics dm = resources.getDisplayMetrics();
		return dm.density;
	}	
	
	public void loadDtcsDetail(String xmlPath, LinearLayout linear, String intepret){
		TextView slt_txt;
		ImageView img_v;
		
		InputStream abpath = null;
        Document doc = null;
        
		code_v = (TextView)findViewById(R.id.dtc_item_code);
		String code;
		int idx = xmlPath.lastIndexOf("/");
		code = xmlPath.substring(idx+1, xmlPath.length()-4);
		String dtcPre = mContext.getResources().getString(R.string.dtc_title) + "\n";
		code_v.setText(dtcPre+code);
        
		try {
			
	        slt_txt = (TextView) View.inflate(mContext, R.layout.dtc_text, null);
			slt_txt.setText(intepret);
			slt_txt.setTextColor(0xfffacb3d);
			linear.addView(slt_txt);
			
			abpath = mContext.getAssets().open(xmlPath);
			if(abpath == null)
				return;
			doc = DocumentBuilderFactory.newInstance()  
			        .newDocumentBuilder().parse(abpath);
			
	        NodeList nodeList = doc.getElementsByTagName("step");
	        
	        String nodeVal = null;
	        Element ele;
	        Node node;
	        BitmapFactory.Options opts = new BitmapFactory.Options();  
	        opts.inSampleSize = 1;
     
	        int resId;
	        

	        for (int i = 0; i < nodeList.getLength(); i++) {  
	            ele = (Element) nodeList.item(i);  
	        	node = ele.getFirstChild();
	        	while(node != null){

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
		        				bmLst.add(img_v);				        			
			        			linear.addView(img_v, (int)(600*Base.scale), (int)(460*Base.scale));
		        			}
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
	
	void releaseBMList(){
		//Bitmap bm = null;
		ImageView v;
		if(bmLst == null)
			return;
		int len = bmLst.size();
		int i = 0;
		Drawable d;
		while(i < len){
//				bm = bmLst.get(i);
//				if(bm != null && !bm.isRecycled())
//					bm.recycle();
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
