package com.ctg.ui;


import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;
import org.json.JSONObject;

import com.ctg.net.CacheManager;
//import com.ctg.net.HttpThread;
import com.ctg.net.IHttpCallback;
import com.ctg.util.Preference;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
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

public class Forgetpw extends Dialog {
	

	static final String TAG = "Forgetpw";

	public static final String KEY_SERVER_IP = "ip";

	static final String KEY_SERVER_PORT = "port";

	public static final int TRANSFER_STATUS = 1;	
	public Login logind;
	private EditText passwd;

	private EditText phoneNO;

	TextWatcher usrTextWatch;
	TextWatcher pswdTextWatch;
	
	private String str_phone;


	ProgressDialog dlg;

	TextView psw_text;
	TextView psw_cfm_text;
	private EditText psw; 
	private EditText psw_cfm;
	private EditText token; 
	
	String tel_cfm_url = Base.HTTP_ROOT_PATH + "/account/resetPassword";
	Map<String, String> phoneNum = null;
	PopupWindow popWindow;

	private int mWidth;

	private int mHeight;

	//private HttpThread httpConnect;
	
	private boolean autologin;
	
	private boolean saveUserPwd;
	
	private boolean saveUsr;
	
	private final int WC = ViewGroup.LayoutParams.WRAP_CONTENT;

	private final int FP = ViewGroup.LayoutParams.FILL_PARENT;

	
	final public static int F = 0x106;
	
	public static String logo_dir = Base.getSDPath() + "/OBDII/logo";
	
	public static String logo_path; 
	
//	public ResetPassword resetpassword;
	public OBDApplication OBDapp;
	
	ImageView logoImage;
//	ImageView autologinview;
//	ImageView saveUserPwdView;
	TextView resetpasswd;
	TextView get_confrim_code;
	Context mContext;
	static String imageURL;
	Bitmap logoBm;
	boolean logoDown = false;
	Timer myWaitCodeTm;
	int confirmTmCount = 0;
	
	Handler handler = new Handler() {
		public void handleMessage(Message message) {
//			if (((OBDApplication)(mContext.getApplicationContext())).httpConnect != null) {
//				((OBDApplication)(mContext.getApplicationContext())).httpConnect.stopThread();
//				((OBDApplication)(mContext.getApplicationContext())).httpConnect = null;			
//			}
			Bundle bb = message.getData();
			switch (message.what) {
	            case 1:  				
	            	get_confrim_code.setTextColor(Color.LTGRAY);
	            	get_confrim_code.setText(confirmTmCount + "秒后再获取");
	                break;  
	            case 2:  
	            	get_confrim_code.setTextColor(0xff0087cb);
	            	get_confrim_code.setText("获取验证码");
					if(myWaitCodeTm != null){
						myWaitCodeTm.cancel();
						myWaitCodeTm = null;
					}
	        	break;
//				case HttpThread.RESET_PW:	
//					int ret = bb.getInt(HttpThread.KEY_RESET_PW, 0);
//					Toast.makeText(mContext, "成功", 1).show();
//					if (ret == 1){
//						logind = OBDapp.getLoginDialog();						
//						if(logind != null){
//							logind.show();							
//						}
//						if (dlg != null && dlg.isShowing()) {
//							dlg.dismiss();
//							dlg = null;
//						}
//						Forgetpw.this.cancel();
//						
//					}					
//					else{
//						resetfailed();
//					}	
//					break;
				default:
					break;
			}

		}
	};
	
	private float getDensity(Context context) {
		Resources resources = context.getResources();
		DisplayMetrics dm = resources.getDisplayMetrics();
		return dm.density;
	}	
	
	public Forgetpw(Context context, int width, int height, int layout, int style) {
		this(context, width, height, layout, style, null);				
	}
		
	public Forgetpw(Context context, int width, int height, int layout, int style, String content) {
		super(context, style);
			// TODO Auto-generated constructor stub
		setContentView(layout);
		mContext = context;
		OBDapp = (OBDApplication)(mContext.getApplicationContext());
		WindowManager wm = (WindowManager) getContext()
                .getSystemService(Context.WINDOW_SERVICE);

	     
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
		

		resetpasswd = (TextView) findViewById(R.id.resetpasswd);
		psw_text = (TextView)findViewById(R.id.passwd_txt);
		psw_cfm_text = (TextView)findViewById(R.id.passwd_cfm_txt);
		get_confrim_code = (TextView) findViewById(R.id.get_confrim_code);
		psw = (EditText)findViewById(R.id.passwd);
		psw_cfm = (EditText)findViewById(R.id.passwd_cfm);
		token = (EditText)findViewById(R.id.confrim_code);
		phoneNO = (EditText) findViewById(R.id.reset_username);
		//passwd = (EditText) findViewById(R.id.passwd);
		
//		if(content != null)
			//phoneNO.setText(content);
//		logoImage = (ImageView) findViewById(R.id.login_logo);  
//		logoBm = BitmapFactory.decodeFile(SplashActivity.logo_path);
//		if(logoBm != null)
//			logoImage.setImageBitmap(logoBm);
//		else
//			logoImage.setImageResource(R.drawable.logo_u);

		if (saveUsr) {
			phoneNO.setText(Preference.getInstance(
					mContext.getApplicationContext()).getUser());
			CharSequence text = phoneNO.getText();
			if (text instanceof Spannable) {
				Spannable spanText = (Spannable) text;
				Selection.setSelection(spanText, text.length());
			}
		}
		if(saveUserPwd){
			passwd.setText(Preference.getInstance(mContext.getApplicationContext()).getUserPasswd());
			CharSequence textPwd = phoneNO.getText();
			if (textPwd instanceof Spannable) {
				Spannable spanText = (Spannable) textPwd;
				Selection.setSelection(spanText, textPwd.length());
			}
		}
		initListener();
		
	}
	
	protected void initListener() {
		get_confrim_code.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				phoneNum = new HashMap<String, String>();
				str_phone = phoneNO.getText().toString();
				if(str_phone != null && !str_phone.trim().equals("")){
					phoneNum.put("user", str_phone);
				}				
				CacheManager.getJson(mContext, tel_cfm_url, new IHttpCallback() {
					
					@Override
					public void handle(int retCode, Object response) {
						// TODO Auto-generated method stub
						Toast.makeText(mContext, "验证码获取成功", 1).show();
						confirmTmCount = 0;
						get_confrim_code.setTextColor(0xff0087cb);
						get_confrim_code.setText("获取验证码");	
						if(myWaitCodeTm != null){
							myWaitCodeTm.cancel();
							myWaitCodeTm = null;
						}
					}
				}, phoneNum);
				if(myWaitCodeTm == null)
					myWaitCodeTm = new Timer();
				confirmTmCount = 60;
				myWaitCodeTm.schedule(new TimerTask() {
					@Override
					public void run() {
						if(confirmTmCount > 0){
							confirmTmCount--;
							handler.sendEmptyMessage(1);
						}
						else{
							handler.sendEmptyMessage(2);					
						}				
					}
				}, 0, 1000);	
			}
		});

		resetpasswd.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				checkUser();
			}
		});
		
		token.addTextChangedListener(tokenTextWatch);
		
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
	}

	protected void findPWDfailed() {
		Toast.makeText(mContext, mContext.getResources().getString(R.string.findpw_title)+mContext.getResources().getString(R.string.failed), Toast.LENGTH_SHORT).show();
		if (dlg != null && dlg.isShowing()) {
			dlg.dismiss();
			dlg = null;
		}
	}
	
	private void checkUser() {	
		Map<String, String> param = new HashMap<String, String>();
		str_phone = phoneNO.getText().toString();
		String password = psw.getText().toString();
		String password_cfm = psw_cfm.getText().toString();
		String tokencontent = token.getText().toString();
		if(str_phone == null || str_phone.trim().equals("")){
			//dlg = ProgressDialog.show(this, getResources().getString(R.string.input_error), getResources().getString(R.string.username_no_null));
			Toast.makeText(mContext, mContext.getResources().getString(R.string.username_no_null), Toast.LENGTH_SHORT).show();
			return;
		}
		else if(!str_phone.matches("^[1][3-8]+\\d{9}"))
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
	
		
		param.put("user", str_phone);
		param.put("newPassword", password);			
		param.put("updateToken", tokencontent);
		
	
	
//		((OBDApplication)(mContext.getApplicationContext())).httpConnect = new HttpThread(mContext, handler, url, param, 8);
//		((OBDApplication)(mContext.getApplicationContext())).httpConnect.startHttp();
		
		CacheManager.getJson(mContext, url,  new IHttpCallback() {
			
			@Override
			public void handle(int retCode, Object response) {
				// TODO Auto-generated method stub		
				if(retCode == 200){
					Toast.makeText(mContext, "更改密码成功", Toast.LENGTH_SHORT).show();
					
					logind = OBDapp.getLoginDialog();						
					if(logind != null){
						logind.show();							
					}
					if (dlg != null && dlg.isShowing()) {
						dlg.dismiss();
						dlg = null;
					}
					Forgetpw.this.cancel();																						
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
	
	protected void resetfailed() {
		Toast.makeText(mContext, mContext.getResources().getString(R.string.findpw_title)+mContext.getResources().getString(R.string.failed), Toast.LENGTH_SHORT).show();
		if (dlg != null && dlg.isShowing()) {
			dlg.dismiss();
			dlg = null;
		}
		cancel();
	}
//	public void setResetpwDialog(){		
//		if(resetpassword != null)
//			OBDapp.setResetpwDialog(resetpassword);		
//	}	
	
	
}
