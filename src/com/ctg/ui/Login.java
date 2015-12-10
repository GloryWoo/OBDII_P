package com.ctg.ui;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Bitmap;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.ctg.crash.LogRecord;
import com.ctg.group.Member;
import com.ctg.net.CacheManager;
//import com.ctg.net.HttpThread;
import com.ctg.net.IHttpCallback;
import com.ctg.service.CarDataService;
import com.ctg.util.Preference;
import com.ctg.util.Util;

public class Login extends Dialog{

	static final String TAG = "Login";

	public static final String KEY_SERVER_IP = "ip";

	static final String KEY_SERVER_PORT = "port";

	public static final int TRANSFER_STATUS = 1;	
	
	private EditText passwd;

	public EditText emailaddress;
	View enter_l;
	TextWatcher usrTextWatch;
	TextWatcher pswdTextWatch;
	
	private String phoneNO;


	ProgressDialog dlg;

	private String sessionid;
	private String plate;
	private String brand;
	private String model;
	private String purchase;
	
	private String passwds;

	private String logpath;

	PopupWindow popWindow;

	private int mWidth;

	private int mHeight;

	//private HttpThread httpConnect;
	
	private boolean autologin;
	
	private boolean saveUserPwd;
	
	private boolean saveUsr;
	
	private final int WC = ViewGroup.LayoutParams.WRAP_CONTENT;

	private final int FP = ViewGroup.LayoutParams.FILL_PARENT;

//	final public static int GET_LOGO_SUCCESS = 0x201;
//	
//	final public static int GET_LOGO_FAIL = 0x202;
//	
//	public static String logo_dir = Base.getSDPath() + "/OBDII/logo";
//	
//	public static String logo_path; 
	
	ImageView logoImage;
//	ImageView autologinview;
//	ImageView saveUserPwdView;
	View loginBtn;
	View forgetPswd;
	View registerBtn;
	Context mContext;
	
	Bitmap logoBm;
	boolean logoDown = false;
	
	public OBDApplication OBDapp;
	Forgetpw forgetpassword;

	private float getDensity(Context context) {
		Resources resources = context.getResources();
		DisplayMetrics dm = resources.getDisplayMetrics();
		return dm.density;
	}
	
	
	public Login(Context context, int width, int height, int layout, int style) {
		this(context, width, height, layout, style, null);		
		
	}
	
	public Login(Context context, int width, int height, int layout, int style, String content) {
		super(context, style);
		
		setContentView(layout);
		mContext = context;
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
		
		autologin = Preference.getInstance(mContext)
				.getAutoConnect();
		
		saveUsr = Preference.getInstance(mContext)
				.getSaveUser();
		saveUserPwd = Preference.getInstance(mContext)
				.getSaveUserPwd();
		

		loginBtn = findViewById(R.id.login_login_tv);
		forgetPswd = findViewById(R.id.login_forgetpswd);
		registerBtn = findViewById(R.id.no_account_need_reg);
		emailaddress = (EditText) findViewById(R.id.username);
		passwd = (EditText) findViewById(R.id.passwd);
		enter_l = findViewById(R.id.enter_l);
		
		logoImage = (ImageView) findViewById(R.id.login_logo);  

		if (saveUsr) {
			emailaddress.setText(Preference.getInstance(
					mContext.getApplicationContext()).getUser());
			CharSequence text = emailaddress.getText();
			if (text instanceof Spannable) {
				Spannable spanText = (Spannable) text;
				Selection.setSelection(spanText, text.length());
			}
		}
		if(saveUserPwd){
			passwd.setText(Preference.getInstance(mContext.getApplicationContext()).getUserPasswd());
			CharSequence textPwd = emailaddress.getText();
			if (textPwd instanceof Spannable) {
				Spannable spanText = (Spannable) textPwd;
				Selection.setSelection(spanText, textPwd.length());
			}
		}

		loginBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				checkUser();

			}
		});
		
		registerBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
//				SplashActivity splash = (SplashActivity)mContext;
				Activity activity = (Activity)mContext;
				if(activity instanceof Base){
					if(((Base)activity).registDlg == null)
						((Base)activity).registDlg = new Register(mContext, mWidth, mHeight, R.layout.register, R.style.Theme_dialog);
					((Base)activity).registDlg.show();
				}
				else if(activity instanceof SplashActivity){
					if(((SplashActivity)activity).registDlg == null)
						((SplashActivity)activity).registDlg = new Register(mContext, mWidth, mHeight, R.layout.register, R.style.Theme_dialog);
					((SplashActivity)activity).registDlg.show();
				}
//	    		Base.OBDApp.setSplash(2);
//	    		Login.this.cancel();

			}
		});
		
		forgetPswd.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
            	forgetpassword = new Forgetpw(mContext, mWidth, mHeight, R.layout.resetpw, R.style.Theme_dialog, emailaddress.getText().toString());
    			forgetpassword.show();
    			setForgetDialog();
			}
		});
		enter_l.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Login.this.cancel();
			}
		});
		usrTextWatch = new TextWatcher() {

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
				if(saveUsr && Preference.getInstance(mContext).getUser().length()-1 == emailaddress.getText().length()){
					saveUsr = false;
					Preference.getInstance(mContext).setSaveUser(saveUsr);
					emailaddress.removeTextChangedListener(usrTextWatch);
					emailaddress.setText("");
				}
			}           

        };
//        TextView textview = (TextView) findViewById(R.id.forgetpassword);
//        textview.setOnClickListener(new View.OnClickListener()
//        {                	
//            @Override  
//            public void onClick(View v)  
//            {  
//            	forgetpassword = new Forgetpw(mContext, mWidth, mHeight, R.layout.resetpw, R.style.Theme_dialog, emailaddress.getText().toString());
//    			forgetpassword.show();
//    			setForgetDialog();
//                //finish(); 
//                //SplashActivity.this.finish(); 
//            }                 	
//        });
        
        if(saveUsr)
        	emailaddress.addTextChangedListener(usrTextWatch);  
		
		pswdTextWatch = new TextWatcher() {

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
				if(saveUserPwd && Preference.getInstance(mContext).getUserPasswd().length()-1 == passwd.getText().length()){
					saveUserPwd = false;
					Preference.getInstance(mContext).setSaveUserPwd(saveUserPwd);
					passwd.removeTextChangedListener(pswdTextWatch);
					passwd.setText("");
				}
			}           

        };
        if(saveUserPwd)
        	passwd.addTextChangedListener(pswdTextWatch); 
		

		setOnCancelListener(new OnCancelListener(){

			@Override
			public void onCancel(DialogInterface dialog) {
				// TODO Auto-generated method stub				
				if(logoBm != null && !logoBm.isRecycled()){ 
			        // 回收并且置为null
					logoBm.recycle(); 
					logoBm = null; 
				} 
			}
			
		});
	}
	
	

	
	protected void loginScc() {
		Toast.makeText(mContext, mContext.getResources().getString(R.string.login_title)+mContext.getResources().getString(R.string.success), Toast.LENGTH_SHORT).show();

	
		Preference.getInstance(mContext.getApplicationContext()).setSessionId(
				sessionid);
		Preference.getInstance(mContext.getApplicationContext()).setUser(phoneNO);
		Preference.getInstance(mContext.getApplicationContext()).setUserPasswd(passwds);
		Preference.getInstance(mContext.getApplicationContext()).setLicence(plate);
		Preference.getInstance(mContext.getApplicationContext()).setCarMaker(brand);
		Preference.getInstance(mContext.getApplicationContext()).setCarType(model);						
		Preference.getInstance(mContext.getApplicationContext()).setPurchaseDate(purchase);

		saveUsr = true;
		Preference.getInstance(mContext).setSaveUser(saveUsr);
		saveUserPwd = true;
		Preference.getInstance(mContext).setSaveUserPwd(saveUserPwd);
		passwd.addTextChangedListener(pswdTextWatch);
    	emailaddress.addTextChangedListener(usrTextWatch); 
//		Intent intent = new Intent(mContext, Base.class);
//		intent.setFlags(intent.getFlags() | Intent. FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
//		mContext.startActivity(intent);
		cancel();
		Message msg = new Message();
		msg.what = Base.LOGIN;
		Base.OBDApp.getHandler().sendMessage(msg);
		Base.loginUser = phoneNO;
    	if(Base.OBDApp.baseAct != null){
    		if(Base.OBDApp.baseAct.headusername != null)
    			Base.OBDApp.baseAct.headusername.setText(phoneNO);
    		//Base.OBDApp.baseAct.localbinder.bindInitFenceData();
    		CarDataService.initFenceData();
    	}
		Base.headbitmap = Member.getHeadBitmapUser(Base.loginUser);
		if(Base.headbitmap != null)
			Base.myBitmap = Util.getRoundedCornerImageColorTriangle(Base.headbitmap, 50*Base.mDensityInt, 50*Base.mDensityInt, 0xff01d4fb);
		Base.baidu_v.loginStateChange(true);
//		Base.OBDApp.mLocationClient.start();
		//		((SplashActivity)mContext).finish();
		if(Base.OBDApp.getSplashContext() != null)
			((Activity) Base.OBDApp.getSplashContext()).finish();

	}
	


	protected void loginfailed() {
		Toast.makeText(mContext, mContext.getResources().getString(R.string.login_title)+mContext.getResources().getString(R.string.failed), Toast.LENGTH_SHORT).show();
	}

	private void checkUser() {		
		phoneNO = emailaddress.getText().toString();
		passwds = passwd.getText().toString();
		if(phoneNO == null || phoneNO.equals("") || passwds == null || passwds.equals("")){
			//dlg = ProgressDialog.show(this, getResources().getString(R.string.input_error), getResources().getString(R.string.username_no_null));
			Toast.makeText(mContext, mContext.getResources().getString(R.string.username_no_null), Toast.LENGTH_SHORT).show();

			return;
		}
		else if(!phoneNO.matches("^[1][3-8]+\\d{9}") && !phoneNO.matches("^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$"))
		{
			//dlg = ProgressDialog.show(this, getResources().getString(R.string.input_error), getResources().getString(R.string.error_username));			
			Toast.makeText(mContext, mContext.getResources().getString(R.string.error_username), Toast.LENGTH_SHORT).show();

			return;
		}
		String url = Base.HTTP_ROOT_PATH + "/login?account=" + phoneNO + "&password=" + passwds;
	
//		if (Base.OBDApp.httpConnect != null) {
//			Base.OBDApp.httpConnect.stopThread();
//			Base.OBDApp.httpConnect = null;			
//		}
//		Base.OBDApp.httpConnect = new HttpThread(mContext, handler, url, null, 2);
//		Base.OBDApp.httpConnect.startHttp();
		
		
	
		
		CacheManager.getJson(mContext, url,  new IHttpCallback() {
			
			@Override
			public void handle(int retCode, Object response) {
				// TODO Auto-generated method stub		
				
				if (dlg != null && dlg.isShowing()) {
					dlg.dismiss();
					dlg = null;
				}
				try{
					if(retCode == 200){
						JSONObject jsonObject = new JSONObject(response.toString());
	
						sessionid = jsonObject.getString("token");
						plate = jsonObject.getString("plate");
						brand = jsonObject.getString("brand");
						model = jsonObject.getString("model");
						purchase = jsonObject.getString("purchase_date");						
						Preference.getInstance(mContext).setLoginStat(true);
	//					mContext.setting_s.setSettingLoginState();
						LogRecord.SetSessionid(sessionid);
						loginScc();
						
						
						if(Base.OBDApp.baseAct != null){
							String usr = Preference.getInstance(mContext).getUser();
							String url = Base.HTTP_GROUP_PATH+"/findGroups?appID=appid&memberName="+usr+"&listMember=1";
							Base.OBDApp.baseAct.httpQueueInstance.EnQueue(url, null, 142);
							
							url = Base.HTTP_FRIEND_PATH+"/getFriends";
							Base.OBDApp.baseAct.httpQueueInstance.EnQueue(url, null, 52);
//							Base.OBDApp.baseAct.getGroupAndFriendList();
						}
					}
					else{
						loginfailed();
					}
					
				}
				catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}, null);
		
		dlg = ProgressDialog.show(mContext, mContext.getResources().getString(R.string.login_title), mContext.getResources().getString(R.string.login_content));
		dlg.setOnKeyListener(new OnKeyListener() {
			
			@Override
			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
				// TODO Auto-generated method stub
				if (keyCode == KeyEvent.KEYCODE_BACK) {
					if (dlg != null && dlg.isShowing()) {
						dlg.dismiss();
						dlg = null;
//						if (Base.OBDApp.httpConnect != null) {
//							Base.OBDApp.httpConnect.stopThread();
//							Base.OBDApp.httpConnect = null;
//						}
						return false;
					}
				}
				return false;
			}
		});
	}

	private boolean checkUrl(String ipadds) {
		String tmpstring = ipadds.substring(ipadds.lastIndexOf("/") + 1);
		Pattern p = Pattern.compile("(2[5][0-5]|2[0-4]\\d|1\\d{2}|\\d{1,2})"
				+ "\\.(25[0-5]|2[0-4]\\d|1\\d{2}|\\d{1,2})"
				+ "\\.(25[0-5]|2[0-4]\\d|1\\d{2}|\\d{1,2})"
				+ "\\.(25[0-5]|2[0-4]\\d|1\\d{2}|\\d{1,2})");
		if (tmpstring != null) {
			Matcher m = p.matcher(tmpstring);
			if (m.matches()) {
				return true;
			} else {
				return false;
			}
		}
		return false;
	}

	@Override
	public void onStart() {
		super.onStart();
		
	}


	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			cancel();
		}

		return false;
	}

	
	public void setForgetDialog(){
		OBDapp = (OBDApplication)(mContext.getApplicationContext());
		if(forgetpassword != null)
			OBDapp.setForgetpwDialog(forgetpassword);		
	}







}