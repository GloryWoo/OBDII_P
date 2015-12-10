package com.ctg.util;


import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ctg.ui.Base;
import com.ctg.ui.R;

public class AddFenceDlg extends Dialog implements DialogInterface.OnCancelListener{ 
	private static final String TAG = "CustomDialog";
    Button cancelButton;		
	public Base baseAct;
	EditText fenceName;
	EditText duration;
	EditText radiusE;
	TextView latText_v;
	TextView lonText_v;
	public TextView addr_v;
	public String addr;
	Button addFenceBtn;
	
	private static int default_width = 160; //
	private static int default_height = 120;//

	
	private android.widget.Button.OnClickListener cancelListen = new android.widget.Button.OnClickListener(){

		@Override
		public void onClick(View v)
		{
			AddFenceDlg.this.cancel();			
		}
		
	};
	public AddFenceDlg(Context context, int layout, int style) {
		this(context, default_width, default_height, layout, style, 0, 0);		
	}
	
	public void onCancel(DialogInterface dialog) {
		baseAct.baidu_v.addFenceDlg = null;
	}	
	
	public AddFenceDlg(Context context, int width, int height, int layout, int style, double lat, double lon) {
		super(context, style);
		//set content
		setContentView(layout);
		
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
		baseAct = (Base)context;
		
		latText_v = (TextView) findViewById(R.id.fence_lat_cont);
		lonText_v = (TextView) findViewById(R.id.fence_lon_cont);
		addr_v = (TextView) findViewById(R.id.fence_addr_cont);
		fenceName = (EditText) findViewById(R.id.add_fence_edit);
		duration = (EditText) findViewById(R.id.fence_duration_e);
		radiusE = (EditText) findViewById(R.id.fence_radius_edit);
		duration.setInputType(EditorInfo.TYPE_CLASS_PHONE);
		
		latText_v.setText(Double.toString(lat));
		lonText_v.setText(Double.toString(lon));
		addFenceBtn = (Button) findViewById(R.id.fence_add_btn);
		addFenceBtn.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				String name = fenceName.getText().toString();
				String dura = duration.getText().toString();
				if(name != null && !name.equals("") 
				&& dura != null && !dura.equals("")){
					//baseAct.baidu_v.addFenceAction(name, dura);
					String radStr = radiusE.getText().toString();
					int rad = 0;
					if(radStr != null)
						rad = Integer.parseInt(radStr);
					Base.baidu_v.addMyFenceAction(name, AddFenceDlg.this.addr, dura, rad);					
					AddFenceDlg.this.cancel();
				}
				else{
					Toast.makeText(baseAct, "请正确填写围栏名称和持续时间", Toast.LENGTH_SHORT).show();
					AddFenceDlg.this.cancel();
				}
			}
			
		});
		
	}
	
	private float getDensity(Context context) {
		Resources resources = context.getResources();
		DisplayMetrics dm = resources.getDisplayMetrics();
		return dm.density;
	}

}
