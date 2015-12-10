package com.ctg.ui;

import java.util.ArrayList;

import com.ctg.util.MyPagerAdapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

public class SplashActivity extends Activity implements View.OnClickListener{
	
    private ViewPager viewPager;  
    private ArrayList<View> pageViews;  
    private ImageView imageView;  
    private ImageView[] imageViews;  
    // 
    private LinearLayout main;  
    //  
    private ViewGroup group;  
    // 
    private static final String SHAREDPREFERENCES_NAME = "first_pref"; 
    private boolean isFirstin = false;
    private String version;
    //
    private final static int DEFAULT_COUNT = 4;
    private int splash_count = 0;
    private final static String TAG = "splash";
    private boolean D = true;
    public OBDApplication OBDApp;
    private int history_dot_count = 0;
    Handler mhandler;
	Message msg;	
	private Context mcontext;
	private boolean homekey = false;
	public int mWidth;
	public int mHeight;
	Register registDlg;
	Login loginDlg;
	Forgetpw forgetpassword;
	View login_v;
	View regist_v;
	View enter_v;
	
	final public static int GET_LOGO_SUCCESS = 0x201;
	
	final public static int GET_LOGO_FAIL = 0x202;
	
	public static String logo_dir = Base.getSDPath() + "/OBDII/logo";
	
	public static String logo_path; 
	
	static String imageURL;
	static boolean logoDown = false;
    @Override  
    protected void onCreate(Bundle savedInstanceState)  
    {  
        // TODO Auto-generated method stub   
        super.onCreate(savedInstanceState); 
		if(D) Log.e(TAG, "splash onCreate");
        requestWindowFeature(Window.FEATURE_NO_TITLE);  
        LayoutInflater inflater = getLayoutInflater();  
        pageViews = new ArrayList<View>();  
        boolean fisttime = FirstJudge();
        View find;
		if(OBDApp == null)
			OBDApp = (OBDApplication) getApplication();
		OBDApp.setSplashContext(this);
		mhandler = OBDApp.getHandler();
		mcontext = OBDApp.baseAct;
		

		
		WindowManager wm = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
		 
	    mWidth = wm.getDefaultDisplay().getWidth();
	    mHeight = wm.getDefaultDisplay().getHeight();
	    
    	splash_count = DEFAULT_COUNT;
    	OBDApp.setSplash(0); 

        if(fisttime)
        {

        }
        else{
        	setContentView(R.layout.splash1);
        	login_v = findViewById(R.id.login_tv);
        	regist_v = findViewById(R.id.regist_tv);
        	enter_v = findViewById(R.id.enter_l);
        	login_v.setOnClickListener(this);
        	regist_v.setOnClickListener(this);
        	enter_v.setOnClickListener(this);        	
        	splash_count = 1;
        	OBDApp.setSplash(1);
    		return;
        }    

        
        main = (LinearLayout) inflater.inflate(R.layout.activity_splash, null);  
        group = (ViewGroup) main.findViewById(R.id.viewGroup);  
        viewPager = (ViewPager) main.findViewById(R.id.guidePages); 

        pageViews.add(inflater.inflate(R.layout.splash_one, null));
        pageViews.add(inflater.inflate(R.layout.splash_two, null));  
        pageViews.add(inflater.inflate(R.layout.splash_three, null)); 
        pageViews.add(inflater.inflate(R.layout.splash_four, null)); 
		
		find = inflater.inflate(R.layout.splash1, null); 
    	login_v = find.findViewById(R.id.login_tv);
    	regist_v = find.findViewById(R.id.regist_tv);
    	enter_v = find.findViewById(R.id.enter_l);
    	login_v.setOnClickListener(this);
    	regist_v.setOnClickListener(this);
    	enter_v.setOnClickListener(this);
    	pageViews.add(find);
    	viewPager.setAdapter(new MyPagerAdapter(pageViews));
        //pageViews.add(inflater.inflate(R.layout.item05, null));   
           
        imageViews = new ImageView[pageViews.size()-1];  
        viewPager.setOnPageChangeListener(new OnPageChangeListener(){

			@Override
			public void onPageScrollStateChanged(int arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onPageSelected(int arg0) {
				// TODO Auto-generated method stub
				if(arg0 < pageViews.size()-1){
					group.setVisibility(View.VISIBLE);
					for(int i = 0; i < pageViews.size()-1; i++){
						if(i != arg0)
							imageViews[i].setBackgroundResource(R.drawable.ic_text_dot0);
					}
					imageViews[arg0].setBackgroundResource(R.drawable.ic_text_dot);
				}
				else
				{
					group.setVisibility(View.GONE);
				}
			}
        	
        });
        for (int i = 0; i < pageViews.size()-1; i++)  
        {  
            imageView = new ImageView(SplashActivity.this);  
            imageView.setLayoutParams(new LayoutParams(30*Base.mDensityInt, 30*Base.mDensityInt));  
            //imageView.setPadding(40*Base.mDensityInt, 0, 40*Base.mDensityInt, 0);              
            imageViews[i] = imageView;  
            if(i == 0)
            	imageView.setBackgroundResource(R.drawable.ic_text_dot);
            else
            	imageView.setBackgroundResource(R.drawable.ic_text_dot0);
            group.addView(imageView);  
            
        } 

        
	    setContentView(main);  
  

        //setFirstTime();
      
    }  
    
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {  
        // TODO Auto-generated method stub   
    	 if (resultCode != RESULT_OK) {  
             return;  
         } else {  
             switch (requestCode) {  
             case Base.IMAGE_REQUEST_CODE:  
            	 if(registDlg != null)
            		 registDlg.resizeImage(data.getData());  
                 break;  
             case Base.CAMERA_REQUEST_CODE:  
            	 if(registDlg != null){
	                 if (Base.getSDPath()!=null) {  
	                	 registDlg.resizeImage(Register.getImageUri());  
	                 } else {  
	                     Toast.makeText(SplashActivity.this, "未找到存储卡，无法存储照片！",  
	                             Toast.LENGTH_LONG).show();  
	                 }  
            	 }
                 break;  
   
             case Base.RESIZE_REQUEST_CODE: 
            	 if(registDlg != null){
	                 if (data != null) {  
	                	 registDlg.showResizeImage(data);  
	                 }  
            	 }
                 break;  
             }  
         }  
        super.onActivityResult(requestCode, resultCode, data);  
    } 
    
    public boolean FirstJudge(){
    	SharedPreferences preference = getSharedPreferences(SHAREDPREFERENCES_NAME,MODE_PRIVATE);
    	Editor editor = preference.edit();
    	isFirstin = preference.getBoolean("isfirstin", true);
    	version = preference.getString("version", "1.0");
    	String currentversion = getVersion();
    	int ret = currentversion.compareToIgnoreCase(version);
    	if(homekey){
    		homekey = false;
    		ret = 1;
    	}
    	if(ret == 0){
        	editor.putBoolean("isfirstin", false);
        	editor.commit();    		
    	}
    	else if(ret > 0){
    		editor.putString("version",currentversion);
        	editor.putBoolean("isfirstin", true);
        	editor.commit();
    	}
    	else{
        	editor.putBoolean("isfirstin", false);
        	editor.commit();
    	}
    	isFirstin = preference.getBoolean("isfirstin", true);
    	if(isFirstin){
        	editor.putBoolean("isfirstin", false);
        	editor.commit();
    		return true;
    	}
    	else{
    		return false;
    	}    			
    }
    
    public void setFirstTime(){
    	SharedPreferences preference = getSharedPreferences(SHAREDPREFERENCES_NAME,MODE_PRIVATE);
    	Editor editor = preference.edit();
    	editor.putBoolean("isfirstin", false);
    	editor.commit();
    }
    
	public String getVersion() {
		try {
			PackageManager manager = this.getPackageManager();
			PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
			String version = info.versionName;
			return version;
		} catch (Exception e) {
			e.printStackTrace();
			return "1.0";
		}
	}
	
	protected void onResume(){
		if(D) Log.e(TAG, "splash onResume");
		super.onResume();
	}
	
	protected void onPause(){
		super.onPause();
		if(D) Log.e(TAG, "splash onPause"); 
		//JPushInterface.onPause(Base.this);		
	}
    
	protected void onDestroy(){
		OBDApp.setSplashContext(null);
		if(D) Log.e(TAG, "splash onDestory");
		super.onDestroy();
	}
	
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		switch(keyCode) {
			case KeyEvent.KEYCODE_BACK:
				//exitAlertDialog.show();
				((Activity) mcontext).finish();
				//return true;
				break;
			case KeyEvent.KEYCODE_HOME:
				homekey = true;
				break;
			case KeyEvent.KEYCODE_MENU:
				break;
			default:
				break;
		}
		return super.onKeyDown(keyCode, event);				
	}
  
   
    
	public void setLoginDialog(){
		if(loginDlg != null)
			OBDApp.setLoginDialog(loginDlg);		
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()){
		case R.id.login_tv:
			loginDlg = new Login(SplashActivity.this, mWidth, mHeight-Base.getStatusBarHeight(), R.layout.login, R.style.Theme_dialog);
			loginDlg.show();
    		OBDApp.setSplash(2);
    		setLoginDialog();  
			break;
		case R.id.regist_tv:
			if(registDlg == null)
				registDlg = new Register(SplashActivity.this, mWidth, mHeight-Base.getStatusBarHeight(), R.layout.register, R.style.Theme_dialog);
			registDlg.show();
    		OBDApp.setSplash(2);			
			break;
		case R.id.enter_l:
			SplashActivity.this.finish();
			break;
		default:break;	
		}
	}	
}
