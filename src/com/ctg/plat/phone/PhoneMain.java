package com.ctg.plat.phone;

import com.ctg.ui.Base;
import com.ctg.ui.OBDApplication;
import com.ctg.ui.R;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class PhoneMain implements OnClickListener{

	Context mContext;
	public static String dialFragTag = "Dial_Frag";
	public static String contactFragTag = "Contact_Frag";
	public static String recentFragTag = "Recent_Frag";
	public static int focusFragIdx = -1;
	FragmentManager fm;
	Dial dialFrg;
	Contact contactFrg;
	Recent rcntFrg;
	ImageView dial_img;
	ImageView pb_img;
	ImageView recent_img;
	public LinearLayout linear;
	boolean inited;
	
	public PhoneMain(Context context) {
		mContext = context;
		fm = ((Activity) context).getFragmentManager();
		//setContentView(R.layout.activity_phonebook);
		linear = (LinearLayout) View.inflate(context, R.layout.activity_phonebook, null);
		View dial_l = (View)linear.findViewById(R.id.dial);
		View pb_l = (View)linear.findViewById(R.id.pb);
		View rcnt_l = (View)linear.findViewById(R.id.recent);
		dial_img = (ImageView)linear.findViewById(R.id.dial_img);
		pb_img = (ImageView)linear.findViewById(R.id.pb_img);
		recent_img = (ImageView)linear.findViewById(R.id.recent_img);
	
		//dialFrg = new Dial();
		dialFrg = new Dial();
		contactFrg = new Contact();
		rcntFrg = new Recent();
		
		dial_l.setOnClickListener(this);
		pb_l.setOnClickListener(this);
		rcnt_l.setOnClickListener(this);

//		initPhoneMain();
//		linear.post(new Runnable(){
//			@Override
//			public void run() {
//				// TODO Auto-generated method stub					
//				FragmentTransaction act = fm.beginTransaction();
//				act.replace(R.id.phone_fragment, dialFrg);
//				act.commit();
//			}
//			
//		});				
	}
	
	public void initPhoneMain(){
		if(!inited){
			inited = true;
			FragmentTransaction act = fm.beginTransaction();
			act.add(R.id.phone_fragment, dialFrg);
			act.commit();
		}
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		FragmentTransaction act = fm.beginTransaction();
		switch(v.getId()){
		case R.id.dial:
			focusFragIdx = 0;
			act.replace(R.id.phone_fragment, dialFrg);
			dial_img.setImageResource(R.drawable.dial_key_active);
			pb_img.setImageResource(R.drawable.address_list);
			recent_img.setImageResource(R.drawable.time);

			break;
		case R.id.pb:
			focusFragIdx = 1;
			act.replace(R.id.phone_fragment, contactFrg);
			dial_img.setImageResource(R.drawable.dial_key);
			pb_img.setImageResource(R.drawable.address_list_active);
			recent_img.setImageResource(R.drawable.time);
			break;
		case R.id.recent:
			focusFragIdx = 2;
			act.replace(R.id.phone_fragment, rcntFrg);
			dial_img.setImageResource(R.drawable.dial_key);
			pb_img.setImageResource(R.drawable.address_list);
			recent_img.setImageResource(R.drawable.time_active);
			break;
		}
		act.commit();
	}
}
