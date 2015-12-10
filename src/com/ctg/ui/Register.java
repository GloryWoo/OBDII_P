package com.ctg.ui;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.ImageRequest;
import com.ctg.crash.LogRecord;
import com.ctg.group.Member;
import com.ctg.net.CacheManager;
import com.ctg.net.HttpQueue;
//import com.ctg.net.HttpThread;
import com.ctg.net.IHttpCallback;
import com.ctg.util.Preference;
import com.ctg.util.Util;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class Register extends Dialog implements View.OnClickListener, Runnable, DialogInterface.OnCancelListener, DialogInterface.OnKeyListener{
	
	private static final String SHAREDPREFERENCES_NAME1 = "Bcallnum";
	private static final String SHAREDPREFERENCES_NAME2 = "Ecallnum";

    public static final String IMAGE_FILE_NAME = "header.jpg"; 
    
    View first_page;
    View second_page;
	EditText user;
	EditText psw;
	EditText psw_cfm;
	EditText licence;
	EditText b_call;
	EditText e_call;
	EditText tel_cfm_code;
	EditText nick_name;
	View previous_step;
	View next_step;
	ImageView headicon_cam;
//	ImageView headicon_sel;	
//	ImageView headicon_default;
	public boolean setHeadIcon;
	Bitmap headBitmap;
	Spinner  maker;
	Spinner  type;
	DatePicker dtPick;
	View registerBtn;
	TextView confirmBtn;//获取验证码按钮
	Context mContext;
	
	private ArrayAdapter<String> adapter_maker;
	private ArrayAdapter<String> adapter_type;
	private ArrayList<String> array_m;
	private ArrayList<ArrayList<String>> array_t;
	int i_car_maker;
	int j_car_type;
	//HttpThread httpConnect;  
	String url;
	//File head_img_f;
	String tel_cfm_url = Base.HTTP_ROOT_PATH + "/getRegisterToken";
	String head_img_name;
	Map<String, String> phoneNum = null;
	
	String sessionid;
	ProgressDialog dlg;
	String str_user;
	String str_nickname;
	String str_pwd;
	String str_pwd_cfm;
	String str_tel_cfn_code;
	String str_bcall;
	String str_ecall;
	String str_licence;
	String str_maker;
	String str_type;
	String str_date;
	Message msg;
//	SplashActivity splashAct;
	Dialog choosePicSourceDlg;
	//private JSONObject param;
	boolean isNextPage;
	Timer myWaitCodeTm;
	int confirmTmCount = 0;
	 
    protected boolean initSpinnerArray(){
    	boolean ret = false;
    	InputStream in_s = null;    	   	
        Document doc = null;
        try {
        	in_s = mContext.getAssets().open("car_type.xml");
        	doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(in_s);
        	NodeList nodeList = doc.getElementsByTagName("maker");
        	String nodeVal = null;
	        String nodeVal1 = null;
	        Element ele;
	        Node node;
	        ArrayList<String> array_item;
	        array_m = new ArrayList<String>();
	        array_t = new ArrayList<ArrayList<String>>();
	        for (int i = 0; i < nodeList.getLength(); i++) {
	        	array_item = new ArrayList<String>();
	            ele = (Element) nodeList.item(i);  	            
	        	node = ele.getFirstChild();
	        	nodeVal = node.getNodeValue();
	        	if(nodeVal.endsWith("\n"))
	        		nodeVal = nodeVal.substring(0, nodeVal.length()-1);
	        	array_m.add(nodeVal);
	        	while(node != null){
	        		if(node.hasChildNodes()){
	        			nodeVal1 = node.getFirstChild().getNodeValue();
	        			array_item.add(nodeVal1);
	        		}
	        		node = node.getNextSibling();
	        	}
	        	array_t.add(array_item);
	        }
        	
        } catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			try {
				in_s.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}  
        
        return ret;
    }
	public Register(Context context, int width, int height, int layout, int style) {
		this(context, width, height, layout, style, null);
		
	}
	
	public Register(Context context, int width, int height, int layout, int style, String content) {
		super(context, style);
		boolean result = false;
		
		mContext = context;
		setContentView(layout);
		Window window = getWindow();
		WindowManager.LayoutParams params = window.getAttributes();

		params.width = (int) (width);
		params.height = (int) (height);
		params.gravity = Gravity.TOP;
		//params.verticalMargin = 2.0F;
		window.setAttributes(params);
		first_page = findViewById(R.id.regist_first);
		second_page = findViewById(R.id.regist_second);
		
		maker = (Spinner) findViewById(R.id.maker);

		result = initSpinnerArray();
		//ArrayAdapter
		adapter_maker = new ArrayAdapter<String>(mContext, android.R.layout.simple_spinner_item, array_m);		 
		//
		adapter_maker.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);		 
		//
		maker.setAdapter(adapter_maker);		 
		//Spinner 
		maker.setOnItemSelectedListener(new SpinnerSelectedListener());
	
		maker.setVisibility(View.VISIBLE);		 
		
		type = (Spinner) findViewById(R.id.type);
		adapter_type = new ArrayAdapter<String>(mContext, android.R.layout.simple_spinner_item, array_t.get(0));		 
		adapter_type.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);		 
		type.setAdapter(adapter_type);	
		//adapter_type.notifyDataSetChanged();
		type.setOnItemSelectedListener(new SpinnerSelectedListener1());
		type.setVisibility(View.VISIBLE);	
		
		user = (EditText)findViewById(R.id.username);
		nick_name = (EditText)findViewById(R.id.nick_name);
		psw = (EditText)findViewById(R.id.pswd);
		psw_cfm = (EditText)findViewById(R.id.pswd_cfm);
		licence = (EditText)findViewById(R.id.licence);
		tel_cfm_code = (EditText)findViewById(R.id.tel_cfm_code);
		headicon_cam = (ImageView) findViewById(R.id.headicon_camera);
//		headicon_sel = (ImageView) findViewById(R.id.headicon_sel_img);
//		headicon_default = (ImageView) findViewById(R.id.headicon_default);
		dtPick = (DatePicker) findViewById(R.id.buy_date_picker);
		b_call = (EditText)findViewById(R.id.base_call);
		e_call = (EditText)findViewById(R.id.emergency_call);
		previous_step = findViewById(R.id.previous_step);
		next_step = findViewById(R.id.next_step);
		
		confirmBtn = (TextView) findViewById(R.id.con_code);
		registerBtn = findViewById(R.id.register_btn);
		setOnKeyListener(this);
		android.widget.DatePicker.OnDateChangedListener odcl=new android.widget.DatePicker.OnDateChangedListener(){  
	    	public void onDateChanged(DatePicker view, int year,int monthOfYear, int dayOfMonth) {  
 
	        }  
	    };  
	    //ͨCalendar  
        Calendar calendar=Calendar.getInstance(TimeZone.getDefault());  
        int year=calendar.get(Calendar.YEAR);  
        int month=calendar.get(Calendar.MONTH);  
        int day=calendar.get(Calendar.DAY_OF_MONTH);  
        //
        dtPick.init(year, month, day, odcl);  
	        
        initListener();
		
		
	}

	private void initListener(){

		confirmBtn.setOnClickListener(this);
		registerBtn.setOnClickListener(this);
		headicon_cam.setOnClickListener(this);
//		headicon_sel.setOnClickListener(this);
		previous_step.setOnClickListener(this);
		next_step.setOnClickListener(this);
	}
	
	public Handler mHandler = new Handler() {  
        @Override  
        public void handleMessage(Message msg) {  
            switch (msg.what) {  
            case 1:  				
				confirmBtn.setTextColor(Color.LTGRAY);
				confirmBtn.setText(confirmTmCount + "秒后再获取");
                break;  
            case 2:  
            	confirmBtn.setTextColor(0xff0087cb);
				confirmBtn.setText("获取验证码");
				if(myWaitCodeTm != null){
					myWaitCodeTm.cancel();
					myWaitCodeTm = null;
				}
            	break;
            }  

        }  
    };  
    
	protected void getConfirmCode() {
		// TODO Auto-generated method stub
		if(confirmTmCount != 0)
			return;
		phoneNum = new HashMap<String, String>();
		phoneNum.put("user", user.getText().toString());
		CacheManager.getJson(mContext, tel_cfm_url, new IHttpCallback() {
			
			@Override
			public void handle(int retCode, Object response) {
				// TODO Auto-generated method stub
				if(retCode == 200){
					Toast.makeText(mContext, "获取验证码成功", Toast.LENGTH_SHORT).show();
				}
				else if(retCode == 409){
					Toast.makeText(mContext, "抱歉，用户名已存在", Toast.LENGTH_SHORT).show();
				}
				else if(retCode == 500){
					Toast.makeText(mContext, "抱歉，用户名必须为手机号", Toast.LENGTH_SHORT).show();
				}
				confirmTmCount = 0;
				confirmBtn.setTextColor(0xff0087cb);
				confirmBtn.setText("获取验证码");	
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
					mHandler.sendEmptyMessage(1);
				}
				else{
					mHandler.sendEmptyMessage(2);					
				}				
			}
		}, 0, 1000);		
	}

	class SpinnerSelectedListener implements OnItemSelectedListener{
	    public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
	    	if(i_car_maker == arg2)
	    		return;
	    	i_car_maker = arg2;	  
	    	//type.removeAllViews();
	    	adapter_type = new ArrayAdapter<String>(mContext, android.R.layout.simple_spinner_item, array_t.get(arg2));		 
	    	adapter_type.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//	    	adapter_type.addAll(array_t.get(i_car_maker));
//	    	adapter_type.notifyDataSetChanged();
	    	type.setAdapter(adapter_type);	
	    }

	    public void onNothingSelected(AdapterView<?> arg0) {

	    }

	}
	
	class SpinnerSelectedListener1 implements OnItemSelectedListener{
	    public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
	       j_car_type = arg2;
	    }

	    public void onNothingSelected(AdapterView<?> arg0) {

	    }

	}
	
	boolean checkFirstPageInfo(){
		str_user = user.getText().toString();
		str_pwd = psw.getText().toString();
		str_pwd_cfm = psw_cfm.getText().toString();
		str_tel_cfn_code = tel_cfm_code.getText().toString();
		str_licence = licence.getText().toString();
		str_maker = array_m.get(i_car_maker);
		str_type = array_t.get(i_car_maker).get(j_car_type);
		str_date = ""+dtPick.getYear()+"-"+(dtPick.getMonth()+1)+"-"+dtPick.getDayOfMonth();
		str_bcall = b_call.getText().toString();
		str_ecall = e_call.getText().toString();
		str_nickname = nick_name.getText().toString();
		
		if(!setHeadIcon){
			Toast.makeText(mContext, "没有设置头像", Toast.LENGTH_SHORT).show();
			return false;
		}
		if(str_user == null || !isMobileNO(str_user))
		{
			Toast.makeText(mContext, mContext.getResources().getString(R.string.error_username), Toast.LENGTH_SHORT).show();
			return false;
		}
		if(str_pwd == null || str_pwd.equals("") || str_pwd_cfm == null || !str_pwd_cfm.equals(str_pwd)){
			String noticeStr = "";
			if(str_pwd.equals(""))
				noticeStr = "没有输入密码";
			else if(str_pwd_cfm.equals(""))
				noticeStr = "没有输入确认密码";
			else
				noticeStr = mContext.getResources().getString(R.string.pwd_differ);
			Toast.makeText(mContext, noticeStr, Toast.LENGTH_SHORT).show();
			return false;
		}
		
		if(str_tel_cfn_code == null || !(str_tel_cfn_code.trim().length() > 0)){
			Toast.makeText(mContext, mContext.getResources().getString(R.string.cfm_code_loss), Toast.LENGTH_SHORT).show();
			return false;
		}
		
//		if(str_bcall == null){
//			Toast.makeText(mContext, "BCall" + mContext.getResources().getString(R.string.phone_num), Toast.LENGTH_SHORT).show();
//			return false;
//		}
//		if(str_ecall == null){
//			Toast.makeText(mContext, "ECall" + mContext.getResources().getString(R.string.phone_num), Toast.LENGTH_SHORT).show();
//			return false;
//		}
		return true;
	}
	
	boolean checkRegisterInfo(){
		boolean ret = false;

		Map<String, String> param = new HashMap<String, String>();

		//save b-call e-call number:

		
		
		param.put("user", str_user);
		if(str_nickname != null)
			param.put("alias", str_nickname);
		param.put("regtoken", str_tel_cfn_code);
		param.put("password", str_pwd);			
		param.put("brand", str_maker);
		param.put("model", str_type);
		if(str_licence != null)
			param.put("plate", str_licence);
		param.put("purchase_date", str_date);
		

		url = Base.NEW_HTTP_ROOT_PATH + "/register";		
		
//		try {
//			Base.OBDApp.httpConnect = new HttpThread(mContext, handler, url, new JSONObject(param.toString()), 1);
//		} catch (JSONException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		Base.OBDApp.httpConnect.startHttp();
		
		CacheManager.getJson(mContext, url, new IHttpCallback() {
			
			@Override
			public void handle(int retCode, Object response) {
				// TODO Auto-generated method stub
				if(retCode == 200){
					Base.loginUser = str_user;
					if(setHeadIcon){
						SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
						head_img_name = sdf.format(new Date());	
						head_img_name += ".png";
						if(Base.getSDPath() != null){
							String path = Base.getSDPath()+"/OBDII/"+str_user;
							File f = new File(path);
							if(!f.isDirectory()){
								f.mkdir();
							}
							File head_img_f = new File(path, head_img_name);
							BufferedOutputStream bos;
							try {
								bos = new BufferedOutputStream(new FileOutputStream(head_img_f));
								headBitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
								bos.flush();
								bos.close();
							} catch (FileNotFoundException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
															
						}

						url = Base.HTTP_ROOT_PATH + "/account/uploadImage";
						//head_img_f = new File(Environment.getExternalStorageDirectory(),IMAGE_FILE_NAME);
						new Thread(Register.this).start();
						Base.headbitmap = Member.getHeadBitmapUser(Base.loginUser);
						if(Base.headbitmap != null)
							Base.myBitmap = Util.getRoundedCornerImageColorTriangle(Base.headbitmap, 50*Base.mDensityInt, 50*Base.mDensityInt, 0xff01d4fb);
						
						Base.baidu_v.loginStateChange(true);
					}
					
					msg = new Message();
					msg.what = Base.REGISTER;
					((OBDApplication)(mContext.getApplicationContext())).getHandler().sendMessage(msg);
					Register.this.cancel();
					((Activity)mContext).finish();
				}
				else if(retCode == 409){				
					Toast.makeText(mContext, "抱歉，用户名已存在", Toast.LENGTH_SHORT).show();									
				}
				else if(retCode == 500){
					Toast.makeText(mContext, "抱歉，用户名必须为手机号", Toast.LENGTH_SHORT).show();
				}
				else{
					Toast.makeText(mContext, mContext.getResources().getString(R.string.register_title)+mContext.getResources().getString(R.string.failed), Toast.LENGTH_SHORT).show();
				}
				
			}
		}, param);
		return ret;
	}
	
	private boolean isMobileNO(String mobiles){
		Pattern p = Pattern.compile("^((13[0-9])|(15[^4,\\D])|(18[0,5-9]))\\d{8}$");
		Matcher m = p.matcher(mobiles);
		return m.matches();
	}
	
	
	    
//	Handler handler = new Handler() {
//		public void handleMessage(Message message) {
//			switch (message.what) {
//				case HttpThread.REGISTER_STATUES:
//					if (dlg != null && dlg.isShowing()) {
//						dlg.dismiss();
//						dlg = null;
//					}
//					if (message.getData().getInt(HttpThread.KEY_TRANSFER_STATUS) == HttpThread.CONNECT_SUCCEED) {
//						sessionid = message.getData().getString(HttpThread.SESSIONID);
//						Preference.getInstance(mContext.getApplicationContext()).setSessionId(sessionid);
//
//						Toast.makeText(mContext, mContext.getResources().getString(R.string.register_title)+mContext.getResources().getString(R.string.success), Toast.LENGTH_SHORT).show();
//						Preference.getInstance(mContext.getApplicationContext()).setSessionId(
//								sessionid);
//						Preference.getInstance(mContext.getApplicationContext()).setUser(str_user);
//						Preference.getInstance(mContext.getApplicationContext()).setNickname(str_nickname);
//						Preference.getInstance(mContext.getApplicationContext()).setUserPasswd(str_pwd);
//						Preference.getInstance(mContext.getApplicationContext()).setLicence(str_licence);
//						Preference.getInstance(mContext.getApplicationContext()).setCarMaker(str_maker);
//						Preference.getInstance(mContext.getApplicationContext()).setCarType(str_type);						
//						Preference.getInstance(mContext.getApplicationContext()).setPurchaseDate(str_date);
//						Preference.getInstance(mContext.getApplicationContext()).setBcall(str_bcall);
//						Preference.getInstance(mContext.getApplicationContext()).setEcall(str_ecall);
//						Preference.getInstance(mContext).setLoginStat(true);
////						((Base)mContext).setting_s.setSettingLoginState();
//						
////						Intent intent = new Intent(mContext, Base.class);
////						intent.setFlags(intent.getFlags() | Intent. FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
////						startActivity(intent);
////						Register.this.finish();
//						//loginScc();
//						Base.loginUser = str_user;
//						if(setHeadIcon){
//							SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
//							head_img_name = sdf.format(new Date());	
//							head_img_name += ".png";
//							if(Base.getSDPath() != null){
//								String path = Base.getSDPath()+"/OBDII/"+str_user;
//								File f = new File(path);
//								if(!f.isDirectory()){
//									f.mkdir();
//								}
//								File head_img_f = new File(path, head_img_name);
//								BufferedOutputStream bos;
//								try {
//									bos = new BufferedOutputStream(new FileOutputStream(head_img_f));
//									headBitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
//									bos.flush();
//									bos.close();
//								} catch (FileNotFoundException e) {
//									// TODO Auto-generated catch block
//									e.printStackTrace();
//								} catch (IOException e) {
//									// TODO Auto-generated catch block
//									e.printStackTrace();
//								}
//																
//							}
//
//							url = Base.HTTP_ROOT_PATH + "/account/uploadImage";
//							//head_img_f = new File(Environment.getExternalStorageDirectory(),IMAGE_FILE_NAME);
//							new Thread(Register.this).start();
//							Base.headbitmap = Member.getHeadBitmapUser(Base.loginUser);
//							if(Base.headbitmap != null)
//								Base.myBitmap = Util.getRoundedCornerImageColorTriangle(Base.headbitmap, 50*Base.mDensityInt, 50*Base.mDensityInt, 0xff01d4fb);
//							
//							Base.baidu_v.loginStateChange(true);
//						}
//						
//						msg = new Message();
//						msg.what = Base.REGISTER;
//						((OBDApplication)(mContext.getApplicationContext())).getHandler().sendMessage(msg);
//						Register.this.cancel();
//						((Activity)mContext).finish();
////						if(((OBDApplication)(mContext.getApplicationContext())).getSplashContext() != null)
////							((Activity) ((OBDApplication)mContext.getApplicationContext()).getSplashContext()).finish();
//					}
//					if (message.getData().getInt(HttpThread.KEY_TRANSFER_STATUS) == HttpThread.CONNECT_FAILED) {
//						if(message.getData().getInt(HttpThread.KEY_RET_CODE) == 409){
//							//Toast.makeText(mContext, mContext.getResources().getString(R.string.user_exist), Toast.LENGTH_SHORT).show();
//							Toast.makeText(mContext, "抱歉，用户名已存在", Toast.LENGTH_SHORT).show();
//						}
//						else if(message.getData().getInt(HttpThread.KEY_RET_CODE) == 500){
//							Toast.makeText(mContext, "抱歉，用户名必须为手机号", Toast.LENGTH_SHORT).show();
//						}
//						else{
//							Toast.makeText(mContext, mContext.getResources().getString(R.string.register_title)+mContext.getResources().getString(R.string.failed), Toast.LENGTH_SHORT).show();
//						}
//						//loginfailed();
//					}
//					if (((OBDApplication)(mContext.getApplicationContext())).httpConnect != null) {
//						((OBDApplication)(mContext.getApplicationContext())).httpConnect.stopThread();
//						((OBDApplication)(mContext.getApplicationContext())).httpConnect = null;		
//					}
//					break;
//				
//
//				
//				default:break;
//			}
//			
//		}
//	};



	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
//		 case R.id.headicon_sel_img:
//			 Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
//			 galleryIntent.addCategory(Intent.CATEGORY_OPENABLE);
//			 galleryIntent.setType("image/*");
//			 splashAct.startActivityForResult(galleryIntent, IMAGE_REQUEST_CODE);
//			 break;
		case R.id.previous_step:
			first_page.setVisibility(View.VISIBLE);
			second_page.setVisibility(View.INVISIBLE);
			isNextPage = false;
			break;
			
		case R.id.next_step:
			if(checkFirstPageInfo()){
				first_page.setVisibility(View.INVISIBLE);
				second_page.setVisibility(View.VISIBLE);
				isNextPage = true;
			}
			break;
		case R.id.headicon_camera:
//			if (Base.getSDPath() != null) {
//				Intent cameraIntent = new Intent(
//						"android.media.action.IMAGE_CAPTURE");
//				cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, getImageUri());
//				cameraIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);
//				splashAct.startActivityForResult(cameraIntent,
//						CAMERA_REQUEST_CODE);
//			} else {
//				Toast.makeText(v.getContext(), "请插入sd卡", Toast.LENGTH_LONG)
//						.show();
//			}
			choosePicSourceDlg = new Dialog(mContext, R.style.Theme_dialog);
			choosePicSourceDlg.setContentView(R.layout.bottom_dlg3);
			
			Window dialogWindow = choosePicSourceDlg.getWindow();
	        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
	        lp.height = Base.mHeight/4;
	        lp.width = Base.mWidth;
	        lp.gravity = Gravity.BOTTOM;
	        dialogWindow.setGravity(Gravity.BOTTOM);
	        dialogWindow.setAttributes(lp);
	       
	        View.OnClickListener dlgClick = new View.OnClickListener(){

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					switch(v.getId()){
					case R.id.textvw1:					
						choosePicSourceDlg.cancel();
						 Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
						 galleryIntent.addCategory(Intent.CATEGORY_OPENABLE);
						 galleryIntent.setType("image/*");
						 ((Activity)mContext).startActivityForResult(galleryIntent, Base.IMAGE_REQUEST_CODE);						
						break;
					case R.id.textvw2:
						choosePicSourceDlg.cancel();
						if (Base.getSDPath() != null) {
						Intent cameraIntent = new Intent(
								"android.media.action.IMAGE_CAPTURE");
						cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, getImageUri());
						cameraIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);
						((Activity)mContext).startActivityForResult(cameraIntent,
								Base.CAMERA_REQUEST_CODE);
						} else {
							Toast.makeText(v.getContext(), "请插入sd卡", Toast.LENGTH_LONG)
									.show();
						}						
						break;
					case R.id.textvw3:
						choosePicSourceDlg.cancel();
						break;						
					default:
						break;
					}
				}
	        	
	        };
	        TextView tv1 = ((TextView)choosePicSourceDlg.findViewById(R.id.textvw1));
	        tv1.setText("从相册选择");
	        tv1.setOnClickListener(dlgClick);
	        TextView tv2 = ((TextView)choosePicSourceDlg.findViewById(R.id.textvw2));
	        tv2.setText("拍照");
	        tv2.setOnClickListener(dlgClick);
	        TextView tv3 = ((TextView)choosePicSourceDlg.findViewById(R.id.textvw3));
//	        tv3.setVisibility(View.VISIBLE);
	        tv3.setText("取消");
	        tv3.setOnClickListener(dlgClick);
	        
	        choosePicSourceDlg.setCanceledOnTouchOutside(true);
	        choosePicSourceDlg.show();	
			break;
		case R.id.con_code:
			getConfirmCode();
			break;
		case R.id.register_btn:
			checkRegisterInfo();
			break;
			
		default:
			break;
		}

	}

	public void resizeImage(Uri uri) {
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(uri, "image/*");
		intent.putExtra("crop", "true");
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		intent.putExtra("outputX", 150);
		intent.putExtra("outputY", 150);
		intent.putExtra("return-data", true);
		((Activity)mContext).startActivityForResult(intent, Base.RESIZE_REQUEST_CODE);
	}

	public void showResizeImage(Intent data) {
		Bundle extras = data.getExtras();
		if (extras != null) {
			headBitmap = (Bitmap) extras.getParcelable("data");
			//Drawable drawable = new BitmapDrawable(photo);
			// mImageHeader.setImageDrawable(drawable);
			headicon_cam.setImageBitmap(headBitmap);			 
			setHeadIcon = true;
		}
	}

	public static Uri getImageUri() {
		return Uri.fromFile(new File(Environment.getExternalStorageDirectory(),
				IMAGE_FILE_NAME));
	}
	
	public InputStream Bitmap2InputStream(Bitmap bm, int quality) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bm.compress(Bitmap.CompressFormat.PNG, quality, baos);
		InputStream is = new ByteArrayInputStream(baos.toByteArray());
		return is;
	}

	public String uploadHeadIcon(String RequestURL) {
		final int TIME_OUT = 10 * 1000; // Upload timeout
		final String CHARSET = "utf-8"; // set encode format
		String result = "";
		String BOUNDARY = UUID.randomUUID().toString();
		String PREFIX = "--", LINE_END = "\r\n";
		String CONTENT_TYPE = "multipart/form-data";

		String version = Base.OBDApp.getVersion();
		String sessionid = Preference.getInstance(
				mContext.getApplicationContext()).getSessionId();

		
		try {
			URL url = new URL(RequestURL);
			// URL url = new URL(testurltemp);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setReadTimeout(TIME_OUT);
			conn.setConnectTimeout(TIME_OUT);
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.setUseCaches(false);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Charset", CHARSET); // set encode format
			conn.setRequestProperty("connection", "keep-alive");
			conn.setRequestProperty("Content-Type", CONTENT_TYPE + ";boundary="
					+ BOUNDARY);
			conn.addRequestProperty("X-token", sessionid);
			conn.addRequestProperty("X-API-version", version);
			conn.addRequestProperty("Content-Size",Integer.toString(headBitmap.getByteCount()));
			if (headicon_cam != null) {
				/**
				 * If file is null, then transfer file and prepare to upload it;
				 */
				DataOutputStream dos = new DataOutputStream(
						conn.getOutputStream());
				StringBuffer sb = new StringBuffer();
				sb.append(PREFIX);
				sb.append(BOUNDARY);
				sb.append(LINE_END);

				sb.append("Content-Disposition: form-data; name=\"file\"; filename=\""
						+ head_img_name + "\"" + LINE_END);
				sb.append("Content-Type: application/octet-stream; charset="
						+ CHARSET + LINE_END);
				sb.append(LINE_END);

				dos.write(sb.toString().getBytes());
				InputStream is = Bitmap2InputStream(headBitmap, 100);
				byte[] bytes = new byte[1024];
				int len = 0;
				while ((len = is.read(bytes)) != -1) {
					dos.write(bytes, 0, len);
				}
				is.close();
				dos.write(LINE_END.getBytes());
				byte[] end_data = (PREFIX + BOUNDARY + PREFIX + LINE_END)
						.getBytes();
				dos.write(end_data);
				dos.flush();
				dos.close();
				int res = conn.getResponseCode();

				if (res == 200) {
					result = "OK";
				} else {
				}
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		if(uploadHeadIcon(url).equals("OK")){
			setHeadIcon = false;
			Base.headbitmap = Member.getHeadBitmapUser(Base.loginUser);
			if(Base.headbitmap != null)
				Base.myBitmap = Util.getRoundedCornerImageColorTriangle(Base.headbitmap, 50*Base.mDensityInt, 50*Base.mDensityInt, 0xff01d4fb);
//			Base.OBDApp.mLocationClient.start();
		}
	}
	@Override
	public void onCancel(DialogInterface dialog) {
		// TODO Auto-generated method stub
//		splashAct.registDlg = null;
		Activity activity = (Activity)mContext;
		if(activity instanceof Base){
			((Base)activity).registDlg = null;
		}
		else if(activity instanceof SplashActivity){
			((SplashActivity)activity).registDlg = null;
		}
	}
	
	@Override
	public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP){
			if(isNextPage){
				first_page.setVisibility(View.VISIBLE);
				second_page.setVisibility(View.INVISIBLE);
				isNextPage = false;
				return true;
			}
			else{
				InputMethodManager imm = (InputMethodManager)mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
				if(imm.isActive())
					;
				else{
					Register.this.hide();
					return true;
				}
			}
				
		}		
		return false;
	}
}
