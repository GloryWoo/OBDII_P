package com.ctg.ui;

import com.ctg.util.DTCsDetailDialog;

import android.app.Activity;
import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;

import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class DTCs extends Activity {
	private static final String TAG = "DTCs";
    public TableLayout tableLayout;  
    private Button btnAdd;  
    private int colNum = 0;
    private Base mContext;
    private int rowNum = 0;
    private ImageButton btnClose;
    private ImageButton btnMore;
    Button clearDtcs;
    // tableRow; 
    final static String[] dtc_code = {"C0129", "P0369", "P0720", "P1426", "P1504"};
    
	public DTCs(Context cont) {
		tableLayout = (TableLayout) View.inflate(cont, R.layout.dtcs_whole, null);
        mContext = (Base) cont; 
		// TODO Auto-generated constructor stub
        addRow(); 
        mContext.dtc_c.addView(tableLayout);
        
        clearDtcs = new Button(mContext);
        clearDtcs.setText(R.string.clear_obd_dtcs);
        RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, -2);
        param.width = 180;
        param.height = 120;
        param.topMargin = 960;
        param.leftMargin = 260;
        mContext.dtc_c.addView(clearDtcs, param);
        clearDtcs.setOnClickListener(clearBtnListener);

	}
	private void addRow()  
    {          
        TableRow tableRow;
        View dtcView;
        int width;        
        TextView text_v;
        String text_str;
        TableRow.LayoutParams param = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, 1);
        param.setMargins(0, 0, 0, 0);
        param.width = 320;
        param.height = 180;       
        for(int i = 0; i < 3; i++){
        	tableRow = new TableRow(mContext);
        	for(int j = 0; j < 2; j++){
        		dtcView = View.inflate(mContext, R.layout.dtcs, null);         		        		
        		btnClose = (ImageButton) ((ViewGroup) ((ViewGroup) dtcView).getChildAt(0)).getChildAt(1);
        		btnMore = (ImageButton) ((ViewGroup) dtcView).getChildAt(1);
        		btnClose.setOnClickListener(closeListener);
        		//btnMore.setOnClickListener(unFoldListener);
        		dtcView.setOnTouchListener(dtcOnTouchListener);
        		dtcView.setId(i*2+j);
        		text_v = (TextView) ((ViewGroup) ((ViewGroup) dtcView).getChildAt(0)).getChildAt(0);
        		//text_str = (String) text_v.getText();
        		//text_str += (i*10 + j);
        		text_str = mContext.getString(R.string.dtc_title);
        		text_str += dtc_code[i*2+j];
        		text_v.setText(text_str);
        		param.leftMargin = 24; 
        		tableRow.addView(dtcView, param);
        		if(i == 2)
        			break;
        	} 
        	tableRow.setPadding(0, 30, 0, 0);        
        	tableLayout.addView(tableRow);        
        }

    } 
	private OnTouchListener dtcOnTouchListener = new OnTouchListener(){

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			// TODO Auto-generated method stub
			if(event.getAction() == MotionEvent.ACTION_DOWN){
				ImageButton btnClose = (ImageButton) ((ViewGroup) ((ViewGroup) v).getChildAt(0)).getChildAt(1);
				if(!(event.getX() >= btnClose.getX() && event.getX()<= btnClose.getX()+btnClose.getWidth()
				&& event.getY() >= btnClose.getY() && event.getY() <= btnClose.getY()+btnClose.getHeight())){
					View dtcView = (View) v;
					int v_id = dtcView.getId();
					String xmlPath = dtc_code[v_id];
//					DTCsDetailDialog dialog = new DTCsDetailDialog(mContext, 320, 520, R.layout.dtcs_detail, R.style.Theme_dialog, xmlPath);
//					dialog.show();			
				}
			}
			return false;
		}
		
	};
	
	private OnClickListener clearBtnListener = new OnClickListener()
	{
		public void onClick(View v)
		{						
			tableLayout.setVisibility(View.GONE);
		}
	};
	
	private OnClickListener closeListener = new OnClickListener()
	{
		public void onClick(View v)
		{			
			View dtcView = (View) v.getParent().getParent();
			dtcView.setVisibility(View.GONE);
			//tableLayout.removeView(dtcView);
			//mContext.dtc_c.removeAllViews();
			//mContext.dtc_c.addView(tableLayout);
		}
	};
	
	private OnClickListener unFoldListener = new OnClickListener()
	{
		public void onClick(View v)
		{
			View dtcView = (View) v.getParent();
			int v_id = dtcView.getId();
			String xmlPath = dtc_code[v_id];
//			DTCsDetailDialog dialog = new DTCsDetailDialog(mContext, 320, 520, R.layout.dtcs_detail, R.style.Theme_dialog, xmlPath);
//			dialog.show();			
		}
	};
	
	private OnClickListener foldListener = new OnClickListener()
	{
		public void onClick(View v)
		{

		}
	};
}
