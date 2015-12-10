package com.ctg.group;


import com.ctg.ui.Base;
import com.ctg.ui.R;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;

public class GrpSharePosDlg extends Dialog implements DialogInterface.OnCancelListener, View.OnClickListener { 
	private static final String TAG = "GrpSharePosDlg";
    Button cancelButton;		
	private Base baseAct;
	View back;
	View memberDetail;
	
	
	private static int default_width = 160; //
	private static int default_height = 120;//
	FrameLayout frame;
	
	public GrpSharePosDlg(Context context, int layout, int style) {
		this(context, default_width, default_height, layout, style);
		
	}
	
	protected void onDestroy(){
	}
	
	public GrpSharePosDlg(Context context){
		super(context);
		frame = (FrameLayout) View.inflate(context, R.layout.grp_share_pos, null);
		setContentView(frame);
		
		baseAct = (Base)context;
		Window window = getWindow();
		WindowManager.LayoutParams params = window.getAttributes();
		//set width,height by density and gravity	
		params.width = Base.mWidth;
		params.height = Base.mHeight;
		params.gravity = Gravity.TOP;
		params.x = 0;		
		params.y = 0;
		//params.verticalMargin = 2.0F;
		window.setAttributes(params);
//		Base.baidu_v = new BaiduMapView(context);
//		frame.addView(Base.baidu_v);
		Base.baidu_v.search_rela.setVisibility(View.INVISIBLE);
		back = frame.findViewById(R.id.grp_back);
		memberDetail = frame.findViewById(R.id.home_group);
		back.setOnClickListener(this);
		memberDetail.setOnClickListener(this);
	}
	
	public GrpSharePosDlg(Context context, int width, int height, int layout, int style) {
		super(context, style);
		//set content
		setContentView(layout);
		
		//mac_address_init();
		//set window params
		Window window = getWindow();
		WindowManager.LayoutParams params = window.getAttributes();
		//set width,height by density and gravity

		if(width > 0)
			params.width = (int) (width);
		else 
			params.width = width;
		if(height > 0)
			params.height = (int) (height);
		else
			params.height = height;
		params.gravity = Gravity.TOP;		
		//params.verticalMargin = 2.0F;
		window.setAttributes(params);
		baseAct = (Base)context;

	}

	@Override
	public void onCancel(DialogInterface dialog) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()){
		case R.id.grp_back:
			GrpSharePosDlg.this.cancel();
			break;
		case R.id.home_group:
			Base.me_v.grpDetailDlg = new GrpDetailDlg(baseAct);
			Base.me_v.grpDetailDlg.show();
			break;
		}
	}
	


}
