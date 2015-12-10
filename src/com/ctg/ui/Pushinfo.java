package com.ctg.ui;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ctg.crash.LogRecord;
//import com.ctg.net.HttpThread;
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

public class Pushinfo extends Dialog {
	


	static final String TAG = "Login";

	public static final String KEY_SERVER_IP = "ip";

	static final String KEY_SERVER_PORT = "port";

	public static final int TRANSFER_STATUS = 1;	
	
	private EditText passwd;

	public EditText emailaddress;

	TextWatcher usrTextWatch;
	TextWatcher pswdTextWatch;
	
	private String email;


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

	final public static int GET_LOGO_SUCCESS = 0x201;
	
	final public static int GET_LOGO_FAIL = 0x202;
	
	public static String logo_dir = Base.getSDPath() + "/OBDII/logo";
	
	public static String logo_path; 
	
	ImageView logoImage;
//	ImageView autologinview;
//	ImageView saveUserPwdView;
	Button loginBtn;
	Button registerBtn;
	Context mContext;
	static String imageURL;
	static Bitmap logoBm;
	boolean logoDown = false;
	
	public OBDApplication OBDapp;
	
	
	Handler handler = new Handler() {
		public void handleMessage(Message message) {
			
//			if(Base.OBDApp == null || Base.OBDApp.getActivityBack() != Base.APP_RUN_FOREGROUND){
//				return;
//			}
//			if (((OBDApplication)(mContext.getApplicationContext())).httpConnect != null) {
//				((OBDApplication)(mContext.getApplicationContext())).httpConnect.stopThread();
//				((OBDApplication)(mContext.getApplicationContext())).httpConnect = null;			
//			}
			Bundle bb = message.getData();
			switch (message.what) {

//				case HttpThread.LOGIN_STATUES:				
//					if (dlg != null && dlg.isShowing()) {
//						dlg.dismiss();
//						dlg = null;
//					}
//					if (message.getData().getInt(HttpThread.KEY_TRANSFER_STATUS) == HttpThread.CONNECT_SUCCEED) {
//						sessionid = bb.getString(HttpThread.SESSIONID);
//						plate = bb.getString(HttpThread.PLATE);
//						brand = bb.getString(HttpThread.BRAND);
//						model = bb.getString(HttpThread.MODEL);
//						purchase = bb.getString(HttpThread.PURCHASE);
//						Preference.getInstance(mContext).setLoginStat(true);
////						mContext.setting_s.setSettingLoginState();
//						LogRecord.SetSessionid(sessionid);
//						loginScc();
//	
//					}
//					if (message.getData().getInt(HttpThread.KEY_TRANSFER_STATUS) == HttpThread.CONNECT_FAILED) {
//						loginfailed();
//					}
//					break;
					
				case GET_LOGO_SUCCESS:
					if(logoImage != null){
						Drawable d = logoImage.getDrawable();
			            if(d != null && d instanceof BitmapDrawable)
			            {                        
			                Bitmap bmp=((BitmapDrawable)d).getBitmap();
			                bmp.recycle();
			                bmp=null;
			            }
			            logoImage.setImageBitmap(null);
					}
					logoImage.setImageBitmap(logoBm);
//					BitmapFactory.Options ops = new BitmapFactory.Options();
//					ops.inScaled = true;
//					Bitmap bm1 = BitmapFactory.decodeFile(logo_path);
//					Bitmap bm = Bitmap.createScaledBitmap(bm1, 285, 117, false);
//					bm1 = null;
//					bm1.recycle();
//					logoImage.setImageBitmap(bm);					
					break;
				case GET_LOGO_FAIL:
					if(logoImage != null){
						Drawable d = logoImage.getDrawable();
			            if(d != null && d instanceof BitmapDrawable)
			            {                        
			                Bitmap bmp=((BitmapDrawable)d).getBitmap();
			                bmp.recycle();
			                bmp=null;
			            }
			            logoImage.setImageBitmap(null);
					}
					logoImage.setImageResource(R.drawable.logo);
//					Bitmap bm = BitmapFactory.decodeFile(logo_path);
//					logoImage.setImageBitmap(bm);
					break;	
					
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
	
	public Pushinfo(Context context, int width, int height, int layout, int style, String title, String content){
		//super(context);
		this(context, width, height, layout, style, null, title,content);	
		// TODO Auto-generated constructor stub
	}
	
	
	public Pushinfo(Context context, int width, int height, int layout, int style, String content,String pushtitle,String pushcontent) {
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
		logoImage = (ImageView) findViewById(R.id.login_logo);  

		setNetLogo();
		//handler.sendEmptyMessage(GET_LOGO_FAIL);

		TextView ptitle = (TextView) findViewById(R.id.pushtitle);
		TextView pcontent = (TextView) findViewById(R.id.pushcontent);
		ptitle.setText(pushtitle);
		pcontent.setText(pushcontent);

	}
	
	
	public void setNetLogo(){
		File dir = new File(logo_dir);
		if(!dir.exists()){
			dir.mkdir();
		}
		//Bitmap bm = getHttpBitmap(Base.HTTP_ROOT_PATH+"/services/logo/logo_u.png");	
		//Bitmap bm = getHttpBitmap("http://unionad.vanclimg.com/union/ad_images/200x200_20130826_164651.jpg");
		
//		if(bm != null)
//			logoImage.setImageBitmap(bm);
//		imageURL = "http://unionad.vanclimg.com/union/ad_images/200x200_20130826_164651.jpg";
		//imageURL = "http://pic1a.nipic.com/2008-11-19/2008111921951949_2.jpg";
		imageURL = Base.HTTP_ROOT_PATH+"/services/logo/logo_u.png";
		String logoFileName = imageURL.substring(imageURL.lastIndexOf("/")+1);
		logo_path = logo_dir + "/" + logoFileName;
		
		new Thread(mdownApkRunnable).start();
	}
	
	private Runnable mdownApkRunnable = new Runnable(){
		public void run() {
			
			logoDown = getHttpBitmap(imageURL);
			
			if(logoDown){
				handler.sendEmptyMessage(GET_LOGO_SUCCESS);
			}
			else{
				handler.sendEmptyMessage(GET_LOGO_FAIL);
			}
		}
	};
	
	public static boolean getHttpBitmap(String url){
	     URL myFileUrl = null;
//	     Bitmap bitmap = null;
//	     BitmapFactory.Options ops = new BitmapFactory.Options();
//	     ops.inJustDecodeBounds = true;
	     
	     FileOutputStream out;
	     byte[] buf = new byte[1024];
	     int rLen, wLen;
	     try {
			out = new FileOutputStream(logo_path);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return false;
		}
	     
	     
	     try{
	    	 myFileUrl = new URL(url);
	     }catch(MalformedURLException e){
	    	 e.printStackTrace();
	    	 return false;
	     }
	     try {          	    
	          HttpURLConnection conn = (HttpURLConnection) myFileUrl.openConnection();
	          conn.setConnectTimeout(1000);
	          conn.setReadTimeout(5000);
	          conn.connect();
	          InputStream is = conn.getInputStream();  
//	          while((rLen = is.read(buf, 0, 1024)) > 0){
//	        	  out.write(buf, 0, rLen);
//	          }
	          logoBm = BitmapFactory.decodeStream(is);
//	          bitmap.setDensity(DisplayMetrics.DENSITY_HIGH);
	          is.close();
	          out.close();
	     }catch (IOException e) {
	          e.printStackTrace();
	          return false;
	     }
	     return true;
	}
	
	protected void loginScc() {
		Toast.makeText(mContext, mContext.getResources().getString(R.string.login_title)+mContext.getResources().getString(R.string.success), Toast.LENGTH_SHORT).show();

	
		Preference.getInstance(mContext.getApplicationContext()).setSessionId(
				sessionid);
		Preference.getInstance(mContext.getApplicationContext()).setUser(email);
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
		((OBDApplication)(mContext.getApplicationContext())).getHandler().sendMessage(msg);
//		((SplashActivity)mContext).finish();
		if(((OBDApplication)(mContext.getApplicationContext())).getSplashContext() != null)
			((Activity) ((OBDApplication)mContext.getApplicationContext()).getSplashContext()).finish();

	}
	


	protected void loginfailed() {
		Toast.makeText(mContext, mContext.getResources().getString(R.string.login_title)+mContext.getResources().getString(R.string.failed), Toast.LENGTH_SHORT).show();

	}

	private void checkUser() {		
		email = emailaddress.getText().toString();
		passwds = passwd.getText().toString();
		if(email == null || email.equals("") || passwds == null || passwds.equals("")){
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
		String url = Base.HTTP_ROOT_PATH + "/login?account=" + email + "&password=" + passwds;


	
//		if (((OBDApplication)(mContext.getApplicationContext())).httpConnect != null) {
//			((OBDApplication)(mContext.getApplicationContext())).httpConnect.stopThread();
//			((OBDApplication)(mContext.getApplicationContext())).httpConnect = null;			
//		}
//		((OBDApplication)(mContext.getApplicationContext())).httpConnect = new HttpThread(mContext, handler, url, null, 2);
//		((OBDApplication)(mContext.getApplicationContext())).httpConnect.startHttp();
		
		
		dlg = ProgressDialog.show(mContext, mContext.getResources().getString(R.string.login_title), mContext.getResources().getString(R.string.login_content));
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



}
