package com.ctg.ui;

import com.ctg.crash.LogRecord;
import com.ctg.trafficViolation.TrafficVioListDlg;
import com.ctg.trafficViolation.TrafficVioQueryDlg;
import com.ctg.util.CustomDialog;
import com.ctg.util.FullScreenDialog;
import com.ctg.util.PanelDlg;
import com.ctg.util.Preference;
import com.harman.ctg.monitor.GalleryActivity;
import com.harman.ctg.monitor.fragments.MonitorFragment;


import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class Car extends LinearLayout{
	Base baseAct;
	LinearLayout line_v;
//	RelativeLayout carbody_rela;
	final private String TAG = "Car ";
	RelativeLayout weizh_rela;
	RelativeLayout drivehabit_rela;
	RelativeLayout dtc_rela;
	RelativeLayout bcall_rela;
	RelativeLayout ecall_rela;
	RelativeLayout reversectrl_rela;
	RelativeLayout gas_rela;
	ImageView weizh_i;
	ImageView dvhbt_i;
	ImageView dtc_i;
	ImageView bcall_i;
	ImageView ecall_i;
	ImageView rvsctrl_i;
	ImageView gas_i;
//	Drawable normalbackdraw;
//	Drawable backdraw;
	RelativeLayout behavior_rela;
	TextView behavior_switch;
	ImageView carBack;
	public TrafficVioQueryDlg wzQueryDlg;
	public TrafficVioListDlg wzListDlg;
	public RelativeLayout lastRelaFocus;
	public PanelDlg panelDlg;
	public static boolean startRecordBehavior=false;
	private MonitorFragment monitor=null;
	public Car(Context context){
		super(context);
		
		baseAct = (Base)context;
		line_v = (LinearLayout) View.inflate(context, R.layout.car, this);
		weizh_rela = (RelativeLayout) findViewById(R.id.weizh_rela);
		dtc_rela = (RelativeLayout) findViewById(R.id.dtc_rela);
//		carbody_rela = (RelativeLayout) findViewById(R.id.carbody_rela);
		gas_rela = (RelativeLayout) findViewById(R.id.gas_consume);
		reversectrl_rela = (RelativeLayout) findViewById(R.id.carcontrol_rela);
		drivehabit_rela = (RelativeLayout) findViewById(R.id.drive_habit_rela);	
		bcall_rela = (RelativeLayout) findViewById(R.id.bcall_rela);	
		ecall_rela = (RelativeLayout) findViewById(R.id.ecall_rela);
		weizh_rela.setOnClickListener(relaClickListener);
		dtc_rela.setOnClickListener(relaClickListener);
//		carbody_rela.setOnClickListener(relaClickListener);
		bcall_rela.setOnClickListener(relaClickListener);
		ecall_rela.setOnClickListener(relaClickListener);
		gas_rela.setOnClickListener(relaClickListener);
		reversectrl_rela.setOnClickListener(relaClickListener);
		drivehabit_rela.setOnClickListener(relaClickListener);
		carBack = (ImageView) line_v.findViewById(R.id.car_back);
		carBack.setOnClickListener(relaClickListener);
		
		weizh_i = (ImageView) line_v.findViewById(R.id.weizh_icon);
		dvhbt_i = (ImageView) line_v.findViewById(R.id.drive_habit_icon);
		dtc_i = (ImageView) line_v.findViewById(R.id.dtc_icon);		
		bcall_i = (ImageView) line_v.findViewById(R.id.bcall_icon);
		ecall_i = (ImageView) line_v.findViewById(R.id.ecall_icon);
		rvsctrl_i = (ImageView) line_v.findViewById(R.id.carcontrol_icon);
		behavior_rela = (RelativeLayout) findViewById(R.id.behavior_switch);
		behavior_switch = (TextView) line_v.findViewById(R.id.behavior_switch_text);
		behavior_rela.setOnClickListener(relaClickListener);
//		backdraw = baseAct.getResources().getDrawable(R.drawable.shape_car_frame_f);
//		normalbackdraw = baseAct.getResources().getDrawable(R.drawable.shape_car_frame);
		lastRelaFocus = null;
	}
	
	boolean onBackKeyDown(){
		boolean ret = false;
		
		if(wzQueryDlg != null && wzQueryDlg.isShowing())
		{
			wzQueryDlg.hide();
			ret = true;
		}
		if(wzListDlg != null && wzListDlg.isShowing()){
			wzListDlg.hide();
			ret = true;
		}
		if(panelDlg != null && panelDlg.isShowing()){
			panelDlg.hide();
			ret = true;
		}
		if(baseAct.fullScreenDlg != null && baseAct.fullScreenDlg.isShowing()){
			baseAct.fullScreenDlg.hide();	
			ret = true;
		}
		if(baseAct.setting_s != null && baseAct.settingDlg.isShowing()){				
			if(baseAct.setting_s.helpDialog != null && baseAct.setting_s.helpDialog.isShowing())
				baseAct.setting_s.helpDialog.hide();
			baseAct.settingDlg.hide();
			ret = true;			
		}
		return ret;
	}
	
	View.OnClickListener relaClickListener = new View.OnClickListener(){

		@Override
		public void onClick(View v) {
			String callnumber;
			Intent intent;
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.behavior_switch:
				startRecordBehavior = !startRecordBehavior;

				//example for GalleryActivity @xxm
				Intent gallery  = new Intent(baseAct, GalleryActivity.class);
				baseAct.startActivity(gallery);
				break;
			
			case R.id.weizh_rela:
				LogRecord.SaveLogInfo2File(Base.OperateInfo, TAG+"traffic vio click");
				if(baseAct.login_state_check()){
					wzQueryDlg = new TrafficVioQueryDlg(baseAct, Base.mWidth, Base.mHeight, R.layout.trafficvio_query, R.style.Theme_dialog);
					wzQueryDlg.show();
				}
//				if(lastRelaFocus != null)
//					lastRelaFocus.setBackground(normalbackdraw);
//				weizh_rela.setBackground(backdraw);
//				lastRelaFocus = weizh_rela;				
				break;
				
			case R.id.drive_habit_rela:
				LogRecord.SaveLogInfo2File(Base.OperateInfo, TAG+"drive habit");
				if(baseAct.login_state_check()){
					Intent drive_intent  = new Intent(baseAct, DrivehabitActivity.class);
					baseAct.startActivity(drive_intent);
				}
//				if(lastRelaFocus != null)
//					lastRelaFocus.setBackground(normalbackdraw);
//				drivehabit_rela.setBackground(backdraw);
//				lastRelaFocus = drivehabit_rela;				
				//baseAct.finish();
				break;	
				
			case R.id.dtc_rela:
				LogRecord.SaveLogInfo2File(Base.OperateInfo, TAG+"dtc click");
				if(baseAct.login_state_check()){
					if(baseAct.fullScreenDlg == null){
						baseAct.fullScreenDlg = new FullScreenDialog(baseAct, Base.mWidth, Base.realHeight,R.style.Theme_dialog);					
					}
					//baseAct.dtc_l = new DTCs_List(baseAct);								
					baseAct.fullScreenDlg.show();
				}
//				if(lastRelaFocus != null)
//					lastRelaFocus.setBackground(normalbackdraw);
//				dtc_rela.setBackground(backdraw);
//				lastRelaFocus = dtc_rela;
				break;
				
//			case R.id.carbody_rela:
//				if(baseAct.fullScreenDlg1 == null){
//					baseAct.fullScreenDlg1 = new FullScreenDialog(baseAct, baseAct.mWidth, baseAct.mHeight, R.style.Theme_dialog);
//					baseAct.fullScreenDlg1.setContentView(baseAct.carbody_rela);
//				}
//				//baseAct.initBodyChek();				
//				baseAct.fullScreenDlg1.show();
//				break;
				
			case R.id.bcall_rela:
				LogRecord.SaveLogInfo2File(Base.OperateInfo, TAG+"bcall click");
				if(baseAct.login_state_check()){
					callnumber = Preference.getInstance(baseAct.getApplicationContext()).getBcall();
			    	intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + callnumber));
			    	baseAct.startActivity(intent);
				}
//				if(lastRelaFocus != null)
//					lastRelaFocus.setBackground(normalbackdraw);
//				bcall_rela.setBackground(backdraw);
//				lastRelaFocus = bcall_rela;
				break;
				
			case R.id.ecall_rela:
				LogRecord.SaveLogInfo2File(Base.OperateInfo, TAG+"ecall click");
				if(baseAct.login_state_check()){
					callnumber = Preference.getInstance(baseAct.getApplicationContext()).getEcall();
			    	intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + callnumber));
			    	baseAct.startActivity(intent);
				}
//				if(lastRelaFocus != null)
//					lastRelaFocus.setBackground(normalbackdraw);
//				ecall_rela.setBackground(backdraw);
//				lastRelaFocus = ecall_rela;
				break;
			case R.id.gas_consume:
				if(baseAct.login_state_check()){
					panelDlg = new PanelDlg(baseAct, Base.mWidth, Base.mHeight, R.layout.panel, R.style.Theme_dialog);
					panelDlg.show();
				}
				break;
				
			case R.id.carcontrol_rela:		
				LogRecord.SaveLogInfo2File(Base.OperateInfo, TAG+"setting click");
				if(baseAct.setting_s == null)
					baseAct.setting_s = new Setting(baseAct);
				if(baseAct.settingDlg == null)
					baseAct.settingDlg = new CustomDialog(baseAct, Base.mWidth, Base.mHeight, baseAct.setting_s.scrollView, R.style.Theme_dialog);
				
				baseAct.settingDlg.setOnCancelListener(new OnCancelListener(){

					@Override
					public void onCancel(DialogInterface dialog) {
						// TODO Auto-generated method stub
						baseAct.settingDlg = null;
					}
					
				});
				baseAct.settingDlg.show();
				//					baseAct.setting_s = new Setting(baseAct, R.style.Theme_dialog);				
//				baseAct.setting_s.show();
				
//				if(lastRelaFocus != null)
//					lastRelaFocus.setBackground(normalbackdraw);
//				reversectrl_rela.setBackground(backdraw);
//				lastRelaFocus = reversectrl_rela;
				break;

			case R.id.car_back:
				baseAct.setVpagerItem0();				
				break;
			default:
				break;
			}
		}
		
	};
	
	public Car(Context context, AttributeSet attrs){
		super(context, attrs);
	}
}
