package com.ctg.util;


import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.ctg.bluetooth.BluetoothSet;
import com.ctg.ui.Base;
import com.ctg.ui.R;

import android.app.Dialog;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import android.util.DisplayMetrics;

import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;

import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;


public class FullScreenDialog extends Dialog implements DialogInterface.OnCancelListener{ 
	private static final String TAG = "FullScreenDialog";
    Button cancelButton;		
	private Base baseAct;

	
	private static int default_width = 160; //
	private static int default_height = 120;//

	TextView title_v;
	TextView intepret_v;
	public RelativeLayout rela_v;
	ListView list_v;
//	TextView none_dtc;
	RelativeLayout none_dtc_rela;
//	ArrayList<Map<String, Object>> listItem;
	SimpleAdapter listItemAdapter;
	DTCsDetailDialog dtcDetail;
//	ScrollView scrollContainer;
//	ScrollView lastScrollView;
	LinearLayout lastLinear;
	ImageView updown;
	RelativeLayout updownRela;
	int lastPosition = -1;
	LinearLayout linearContainer;
	public ArrayList<ImageView> bmLst;
	private static final int WC = ViewGroup.LayoutParams.WRAP_CONTENT;
	private static final int FP = ViewGroup.LayoutParams.MATCH_PARENT;
	public static LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(FP, FP);
	public RelativeLayout relativeLay;
	View dtc_back;
	boolean inited = false;
//	public FullScreenDialog(Context context, int layout, int style) {
//		this(context, default_width, default_height, style);
//		
//	}
	
	public void onCancel(DialogInterface dialog) {
		baseAct.fullScreenDlg = null;
		releaseBMList();
	}
	
	public FullScreenDialog(Context context, int width, int height, int style) {
		super(context, style);
		//set content
		//setContentView(layout);
		baseAct = (Base)context;
		//mac_address_init();
		//set window params
//		setContentView(R.layout.dtcs_list);
		rela_v = (RelativeLayout) View.inflate(baseAct, R.layout.dtcs_list, null);
		setContentView(rela_v);
		
		none_dtc_rela = (RelativeLayout)rela_v.findViewById(R.id.dtc_none_rela);
		list_v = (ListView) rela_v.findViewById(R.id.dtc_listv);
		dtc_back = findViewById(R.id.dtc_back);
		dtc_back.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				FullScreenDialog.this.cancel();
			}
		});
		if(Base.dtcListItem != null && Base.dtcListItem.size() != 0){
			list_v.setVisibility(View.VISIBLE);
			none_dtc_rela.setVisibility(View.INVISIBLE);
			initDtcSroll();
		}
		else{
			list_v.setVisibility(View.INVISIBLE);
			none_dtc_rela.setVisibility(View.VISIBLE);
		}
		Window window = getWindow();
		WindowManager.LayoutParams params = window.getAttributes();
		//set width,height by density and gravity
		//float density = getDensity(context);	
		params.width = (int) (width);
		params.height = (int) (height);
		params.gravity = Gravity.TOP;
		//params.verticalMargin = 2.0F;
		window.setAttributes(params);
		
		setOnCancelListener(this);
	}

	public void setList(){
		if(!inited){
			initDtcSroll();
		}
		else{
			listItemAdapter = new SimpleAdapter(baseAct,Base.dtcListItem,// 
		            R.layout.dtc_list_item,
		            new String[] {"title", "intepret"},   
		            new int[] {R.id.dtc_title,R.id.dtc_intepret}  
		        ); 
			list_v.setAdapter(listItemAdapter); 
		}
	}
	
	public void initDtcSroll(){
		inited = true;
		none_dtc_rela = (RelativeLayout)rela_v.findViewById(R.id.dtc_none_rela);
		list_v = (ListView) rela_v.findViewById(R.id.dtc_listv);
		bmLst = new ArrayList<ImageView>();
		listItemAdapter = new SimpleAdapter(baseAct,Base.dtcListItem,// 
	            R.layout.dtc_list_item,
	            new String[] {"title", "intepret"},   
	            new int[] {R.id.dtc_title,R.id.dtc_intepret}  
	        ); 
		list_v.setAdapter(listItemAdapter);  	
		list_v.setOnScrollListener(new OnScrollListener(){

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				// TODO Auto-generated method stub
				if(lastPosition != -1 && (lastPosition<firstVisibleItem || lastPosition >= firstVisibleItem+visibleItemCount)){
					lastPosition = -1;
					if(lastLinear != null){
						ImageView img = (ImageView) ((View)lastLinear.getParent()).findViewById(R.id.arrow_updown);
						if(img != null){
							img.setImageResource(R.drawable.arrow_down_blck);
						}
						((ViewGroup) lastLinear.getParent()).removeView(lastLinear);
//						releaseBMList();
					}
				}
			}
			
		});
		list_v.setDividerHeight(6*Base.mDensityInt);
		list_v.setOnItemClickListener(new OnItemClickListener() {  			  
            @Override  
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) { 
            	updown = (ImageView) arg1.findViewById(R.id.arrow_updown);
            	Map mapItm = baseAct.dtcListItem.get(arg2);
            	String code = (String) mapItm.get("title");
            	String intepret = (String) mapItm.get("intepret");
            	int idxGap = code.indexOf(" ");
            	code = code.substring(idxGap+1);
            	String fullpath =  "xml/"+code + ".xml";
            	
            	if(lastPosition == -1){
	            	linearContainer = (LinearLayout) View.inflate(baseAct, R.layout.linear, null);
	            	loadDtcsDetail(fullpath, linearContainer,intepret);
					updown.setImageResource(R.drawable.arrow_up_blck);
	            	lastPosition = arg2;
	            	((ViewGroup) arg1).addView(linearContainer);
	            	lastLinear = linearContainer;
            	}
				else if(arg2 == lastPosition){										
					updown.setImageResource(R.drawable.arrow_down_blck);
					lastPosition = -1;
					((ViewGroup) lastLinear.getParent()).removeView(lastLinear);
//					releaseBMList();
				}
				else{//click another one while scrollview visible
					if(lastLinear != null){
						ImageView img = (ImageView) ((View)lastLinear.getParent()).findViewById(R.id.arrow_updown);
						if(img != null){
							img.setImageResource(R.drawable.arrow_down_blck);
						}
						((ViewGroup) lastLinear.getParent()).removeView(lastLinear);
//						releaseBMList();
					}
					linearContainer = (LinearLayout) View.inflate(baseAct, R.layout.linear, null);
					loadDtcsDetail(fullpath, linearContainer,intepret);
					updown.setImageResource(R.drawable.arrow_up_blck);
	            	((ViewGroup) arg1).addView(linearContainer);
	            	lastLinear = linearContainer;
					lastPosition = arg2;
				}				
            }  
		});
	}
	
	public void loadDtcsDetail(String xmlPath, LinearLayout linear, String intepret){
		TextView slt_txt;
		ImageView img_v;
		
		InputStream abpath = null;
        Document doc = null;
        
		try {
			
	        slt_txt = (TextView) View.inflate(baseAct, R.layout.dtc_text, null);
			slt_txt.setText(intepret);
			slt_txt.setTextColor(0xfffacb3d);
			linear.addView(slt_txt);
			
			abpath = baseAct.getAssets().open(xmlPath);
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
			        			img_v = (ImageView) View.inflate(baseAct, R.layout.dtc_image, null);
			        			img_v.setImageResource(resId);	
		        				bmLst.add(img_v);				        			
			        			linear.addView(img_v, (int)(600*Base.scale), (int)(460*Base.scale));
		        			}
	        			}
	        			else if(nodeVal.matches("[0-9].+")){
	        				slt_txt = (TextView) View.inflate(baseAct, R.layout.dtc_text, null);
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
