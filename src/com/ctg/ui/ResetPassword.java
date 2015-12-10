package com.ctg.ui;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

import com.ctg.crash.LogRecord;
import com.ctg.net.CacheManager;
//import com.ctg.net.HttpThread;
import com.ctg.net.IHttpCallback;
import com.ctg.util.Preference;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnKeyListener;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.Selection;
import android.text.Spannable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

public class ResetPassword extends Dialog {

	static final String TAG = "ResetPassword";

	public static final String KEY_SERVER_IP = "ip";

	static final String KEY_SERVER_PORT = "port";

	public static final int TRANSFER_STATUS = 1;	
	
	
	ProgressDialog dlg;
	
	TextView psw_text;
	TextView psw_cfm_text;
	private EditText emailaddress;
	
	private EditText psw; 
	private EditText psw_cfm;
	private EditText token; 
	
	private String email;

	PopupWindow popWindow;

	private int mWidth;

	private int mHeight;

	//private HttpThread httpConnect;
	
	

	public OBDApplication OBDapp;
	public Login logind;
	public Forgetpw forgetpw;
	
	ImageView logoImage;
	Button resetBtn;
	Context mContext;
	static String imageURL;
	Bitmap logoBm;
	boolean logoDown = false;
	String useraccount = "";
	
//	Handler handler = new Handler() {
//		public void handleMessage(Message message) {
//			if (((OBDApplication)(mContext.getApplicationContext())).httpConnect != null) {
//				((OBDApplication)(mContext.getApplicationContext())).httpConnect.stopThread();
//				((OBDApplication)(mContext.getApplicationContext())).httpConnect = null;			
//			}
//			Bundle bb = message.getData();
//			switch (message.what) {
//
//				case HttpThread.RESET_PW:	
//					int ret = bb.getInt(HttpThread.KEY_RESET_PW, 0);
//					if (ret == 1){
//						logind = OBDapp.getLoginDialog();
//						forgetpw = OBDapp.getForgetpwDialog();
//						if(forgetpw != null)
//							forgetpw.dismiss();
//						if(logind != null){
//							logind.show();
//							if(logind.emailaddress != null){
//								logind.emailaddress.setText(useraccount);
//							}
//						}
//						if (dlg != null && dlg.isShowing()) {
//							dlg.dismiss();
//							dlg = null;
//						}
//						ResetPassword.this.cancel();
//						
//					}					
//					else{
//						resetfailed();
//					}	
//					break;
//				default:
//					break;
//			}
//
//		}
//	};
	
	TextWatcher tokenTextWatch = new TextWatcher() {

		@Override
		public void afterTextChanged(Editable s) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			// TODO Auto-generated method stub
			if(token.getText().toString().length() != 0){
				psw.setEnabled(true);
				psw_cfm.setEnabled(true);
				psw_text.setEnabled(true);
				psw_cfm_text.setEnabled(true);
			}
			else{
				psw.setEnabled(false);
				psw_cfm.setEnabled(false);
				psw_text.setEnabled(false);
				psw_cfm_text.setEnabled(false);
			}
		}           

    };
    
	private float getDensity(Context context) {
		Resources resources = context.getResources();
		DisplayMetrics dm = resources.getDisplayMetrics();
		return dm.density;
	}
	
/*	public ResetPassword(Context context, int width, int height, int layout, int style, String email) {
		this(context, width, height, layout, style, email);				
	}*/


		
	public ResetPassword(Context context, int width, int height, int layout, int style, String content) {
		super(context, style);
			// TODO Auto-generated constructor stub
		setContentView(layout);
		mContext = context;
		OBDapp = (OBDApplication) context.getApplicationContext();
		WindowManager wm = (WindowManager) getContext()
                .getSystemService(Context.WINDOW_SERVICE);

		useraccount = content; 
		Window window = getWindow();
		WindowManager.LayoutParams params = window.getAttributes();
		//set width,height by density and gravity
		//float density = context.getDensity(context);
		float density = getDensity(context);
		params.width = (int) (width);
		params.height = (int) (height);
		params.gravity = Gravity.TOP;
		//params.verticalMargin = 2.0F;
		window.setAttributes(params);
		mWidth = params.width;
		mHeight = params.height;
		
		resetBtn = (Button) findViewById(R.id.resetpasswd);
		//registerBtn = (Button) findViewById(R.id.register);
		emailaddress = (EditText) findViewById(R.id.username);
		//passwd = (EditText) findViewById(R.id.passwd);
		emailaddress.setText(content);
		
		psw = (EditText)findViewById(R.id.passwd);
		psw_cfm = (EditText)findViewById(R.id.passwd_cfm);
		token = (EditText)findViewById(R.id.confrim_code);
		
		psw_text = (TextView)findViewById(R.id.passwd_txt);
		psw_cfm_text = (TextView)findViewById(R.id.passwd_cfm_txt);
		logoImage = (ImageView) findViewById(R.id.login_logo);  
		logoBm = BitmapFactory.decodeFile(SplashActivity.logo_path);
		if(logoBm != null)
			logoImage.setImageBitmap(logoBm);
		else
			logoImage.setImageResource(R.drawable.logo);
		
		setOnCancelListener(new OnCancelListener(){
			@Override
			public void onCancel(DialogInterface dialog) {				
				if(logoBm != null && !logoBm.isRecycled()){ 
			        // 回收并且置为null
					logoBm.recycle(); 
					logoBm = null; 
				} 
			}
			
		});

		resetBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				checkUser();
			}
		});
		
		token.addTextChangedListener(tokenTextWatch);
	}


    
	protected void resetfailed() {
		Toast.makeText(mContext, mContext.getResources().getString(R.string.findpw_title)+mContext.getResources().getString(R.string.failed), Toast.LENGTH_SHORT).show();
		if (dlg != null && dlg.isShowing()) {
			dlg.dismiss();
			dlg = null;
		}
		cancel();
	}
	

	private void checkUser() {	
		Map<String, String> param = new HashMap<String, String>();
		email = emailaddress.getText().toString();
		String password = psw.getText().toString();
		String password_cfm = psw_cfm.getText().toString();
		String tokencontent = token.getText().toString();
		//passwds = passwd.getText().toString();
		if(email == null || email.equals("")){
			//dlg = ProgressDialog.show(this, getResources().getString(R.string.input_error), getResources().getString(R.string.username_no_null));
			Toast.makeText(mContext, mContext.getResources().getString(R.string.username_no_null), Toast.LENGTH_SHORT).show();
			return;
		}
		else if(!email.matches("^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$"))
		{
			//dlg = ProgressDialog.show(this, getResources().getString(R.string.input_error), getResources().getString(R.string.error_username));			
			Toast.makeText(mContext, mContext.getResources().getString(R.string.error_username), Toast.LENGTH_SHORT).show();

			return;
		}
		if(password == null || password_cfm == null || !password.equals(password_cfm)){
			//dlg = ProgressDialog.show(this, getResources().getString(R.string.register_title), getResources().getString(R.string.pwd_differ));
			Toast.makeText(mContext, mContext.getResources().getString(R.string.pwd_differ), Toast.LENGTH_SHORT).show();

			return;
		}
		
		String url = Base.HTTP_ROOT_PATH + "/account/updatePassword";
		useraccount = email;
		
		param.put("findpwmail", email);
		param.put("password", password);			
		param.put("token", tokencontent);
		
	
//		if (((OBDApplication)(mContext.getApplicationContext())).httpConnect != null) {
//			((OBDApplication)(mContext.getApplicationContext())).httpConnect.stopThread();
//			((OBDApplication)(mContext.getApplicationContext())).httpConnect = null;			
//		}
//		((OBDApplication)(mContext.getApplicationContext())).httpConnect = new HttpThread(mContext, handler, url, param, 8);
//		((OBDApplication)(mContext.getApplicationContext())).httpConnect.startHttp();
		
		CacheManager.getJson(mContext, url, new IHttpCallback() {
			
			@Override
			public void handle(int retCode, Object response) {
				// TODO Auto-generated method stub
				if(retCode == 200){
					logind = OBDapp.getLoginDialog();
					forgetpw = OBDapp.getForgetpwDialog();
					if(forgetpw != null)
						forgetpw.dismiss();
					if(logind != null){
						logind.show();
						if(logind.emailaddress != null){
							logind.emailaddress.setText(useraccount);
						}
					}
					if (dlg != null && dlg.isShowing()) {
						dlg.dismiss();
						dlg = null;
					}
					ResetPassword.this.cancel();
				}
				else{
					resetfailed();
				}
			}
		}, param);
		
		dlg = ProgressDialog.show(mContext, mContext.getResources().getString(R.string.findpw_title), mContext.getResources().getString(R.string.findpw_content));
		dlg.setOnKeyListener(new OnKeyListener() {
			
			@Override
			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
				// TODO Auto-generated method stub
				if (keyCode == KeyEvent.KEYCODE_BACK) {
					if (dlg != null && dlg.isShowing()) {
						dlg.dismiss();
						dlg = null;
//						if (((OBDApplication)(mContext.getApplicationContext())).httpConnect != null) {
//							((OBDApplication)(mContext.getApplicationContext())).httpConnect.stopThread();
//							((OBDApplication)(mContext.getApplicationContext())).httpConnect = null;
//						}
						return false;
					}
				}
				return false;
			}
		});
	}

	@Override
	public void onStart() {
		super.onStart();
		
	}


	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			cancel();
//			System.exit(0);
		}
		//for test
//		else if(keyCode == KeyEvent.KEYCODE_MENU){
//			Intent intent = new Intent(this, Base.class);
//			intent.setFlags(intent.getFlags() | Intent. FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
//			startActivity(intent);
//			finish();
//		}
		return false;
	}	
	
	
	public void JumpToLogin(){
		
	}
	

}
