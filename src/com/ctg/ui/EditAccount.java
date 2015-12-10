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

public class EditAccount extends Dialog implements View.OnClickListener, Runnable, DialogInterface.OnCancelListener, DialogInterface.OnKeyListener{ 
    public static final String IMAGE_FILE_NAME = "header.jpg"; 
    
	EditText user;
	EditText licence;
	EditText b_call;
	EditText e_call;
	EditText nick_name;
	ImageView headicon_cam;
	public boolean setHeadIcon;
	Bitmap headBitmap;
	Spinner  maker;
	Spinner  type;
	DatePicker dtPick;
	View modifyBtn;
	Base baseAct;
	
	private ArrayAdapter<String> adapter_maker;
	private ArrayAdapter<String> adapter_type;
	private ArrayList<String> array_m;
	private ArrayList<ArrayList<String>> array_t;
	int i_car_maker;
	int j_car_type;
	String url;
	String tel_cfm_url = Base.HTTP_ROOT_PATH + "/getRegisterToken";
	String head_img_name;
	Map<String, String> phoneNum = null;
	
	String sessionid;
	ProgressDialog dlg;
	String str_user;
	String str_nickname;
	String str_bcall;
	String str_ecall;
	String str_licence;
	String str_maker;
	String str_type;
	String str_date;

	String cur_str_licence;
	String cur_str_maker;
	String cur_str_type;
	String cur_str_date;
	String cur_str_bcall;
	String cur_str_ecall;
	String cur_str_nickname;
	Message msg;
	Dialog choosePicSourceDlg;
	Timer myWaitCodeTm;
	int confirmTmCount = 0;
	File head_img_f;
	
    protected boolean initSpinnerArray(){
    	boolean ret = false;
    	InputStream in_s = null;    	   	
        Document doc = null;
        int j = 0;
        int j_pivot = 0;
        try {
        	in_s = baseAct.getAssets().open("car_type.xml");
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
	        	j = 0;
	        	while(node != null){
	        		if(node.hasChildNodes()){
	        			nodeVal1 = node.getFirstChild().getNodeValue();
	        			array_item.add(nodeVal1);
	        			if(((String)nodeVal1).equals(str_type))
	        				j_pivot = j;
	        			j++;
	        		}
	        		node = node.getNextSibling();
	        		
	        		
	        	}
	        	if(nodeVal.equals(str_maker)){
	        		i_car_maker = i;
	        		j_car_type = j_pivot;
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
	public EditAccount(Context context, int width, int height, int layout, int style) {
		this(context, width, height, layout, style, null);
		
	}
	
	public EditAccount(Context context, int width, int height, int layout, int style, String content) {
		super(context, style);
		boolean result = false;
		
		baseAct = (Base) context;
		setContentView(layout);
		Window window = getWindow();
		WindowManager.LayoutParams params = window.getAttributes();

		params.width = (int) (width);
		params.height = (int) (height);
		params.gravity = Gravity.TOP;
		//params.verticalMargin = 2.0F;
		window.setAttributes(params);

		str_user = Base.loginUser;
		str_nickname = Base.nickname;
		str_licence = Preference.getInstance(baseAct.getApplicationContext()).getLicence();
		str_maker = Preference.getInstance(baseAct.getApplicationContext()).getCarMaker();
		str_type = Preference.getInstance(baseAct.getApplicationContext()).getCarType();						
		str_date = Preference.getInstance(baseAct.getApplicationContext()).getPurchaseDate();
		str_bcall = Preference.getInstance(baseAct.getApplicationContext()).getBcall();
		str_ecall = Preference.getInstance(baseAct.getApplicationContext()).getEcall();					

		maker = (Spinner) findViewById(R.id.maker_editacc);

		result = initSpinnerArray();
		//ArrayAdapter
		adapter_maker = new ArrayAdapter<String>(baseAct, android.R.layout.simple_spinner_item, array_m);		 
		//
		adapter_maker.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);		 
		//
		maker.setAdapter(adapter_maker);		 
		//Spinner 
		maker.setOnItemSelectedListener(new SpinnerSelectedListener());	 
		maker.setSelection(i_car_maker);
		type = (Spinner) findViewById(R.id.type_editacc);
		adapter_type = new ArrayAdapter<String>(baseAct, android.R.layout.simple_spinner_item, array_t.get(0));		 
		adapter_type.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);		 
		type.setAdapter(adapter_type);	
		//adapter_type.notifyDataSetChanged();
		type.setOnItemSelectedListener(new SpinnerSelectedListener1());
		type.setSelection(j_car_type);
		
		user = (EditText)findViewById(R.id.username_editacc);
		nick_name = (EditText)findViewById(R.id.nick_name_editacc);

		licence = (EditText)findViewById(R.id.licence_editacc);
		headicon_cam = (ImageView) findViewById(R.id.headicon_camera_editacc);
		if(Base.headbitmap != null)
			headicon_cam.setImageBitmap(Base.headbitmap);
		dtPick = (DatePicker) findViewById(R.id.buy_date_picker_editacc);
		b_call = (EditText)findViewById(R.id.bcall_editacc);
		e_call = (EditText)findViewById(R.id.ecall_editacc);
		user.setText(Base.loginUser);
		nick_name.setText(Base.nickname);
		b_call.setText(str_bcall);
		e_call.setText(str_bcall);
		licence.setText(str_licence);
		b_call.setText(str_bcall);	
		modifyBtn = findViewById(R.id.modity_btn_editacc);
		modifyBtn.setOnClickListener(this);
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

		modifyBtn.setOnClickListener(this);
		headicon_cam.setOnClickListener(this);
	}

	class SpinnerSelectedListener implements OnItemSelectedListener{
	    public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
	    	if(i_car_maker == arg2)
	    		return;
	    	i_car_maker = arg2;	  
	    	adapter_type = new ArrayAdapter<String>(baseAct, android.R.layout.simple_spinner_item, array_t.get(arg2));		 
	    	adapter_type.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    	type.setAdapter(adapter_type);	
	    	type.setSelection(0);
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
	
	boolean checkAccountInfo(){
		boolean ret = false;

		str_user = user.getText().toString();

		cur_str_licence = licence.getText().toString();
		cur_str_maker = array_m.get(i_car_maker);
		cur_str_type = array_t.get(i_car_maker).get(j_car_type);
		cur_str_date = ""+dtPick.getYear()+"-"+(dtPick.getMonth()+1)+"-"+dtPick.getDayOfMonth();
		cur_str_bcall = b_call.getText().toString();
		cur_str_ecall = e_call.getText().toString();
		cur_str_nickname = nick_name.getText().toString();
		

		
		if((cur_str_licence.equals("") || cur_str_licence.equals(str_licence))
		 &&(cur_str_maker.equals("") || cur_str_licence.equals(str_maker))
		 &&(cur_str_type.equals("") || cur_str_licence.equals(str_type))
		 &&(cur_str_date.equals("") || cur_str_licence.equals(str_date))
		 &&(cur_str_bcall.equals("") || cur_str_licence.equals(str_bcall))
		 &&(cur_str_ecall.equals("") || cur_str_licence.equals(str_ecall))
		 &&(cur_str_nickname.equals("") || cur_str_licence.equals(str_nickname)))
		{
			Toast.makeText(baseAct, "没有任何修改,无需上传服务器", Toast.LENGTH_SHORT).show();
			return false;
		}

		url = Base.NEW_HTTP_ROOT_PATH + "/updateUserInfo";				
		Map<String, String> paraMap = new HashMap<String, String>();
		paraMap.put("user", str_user);
		
		paraMap.put("alias", cur_str_nickname);
		paraMap.put("brand", cur_str_maker);
		paraMap.put("model", cur_str_type);
		
		paraMap.put("plate", cur_str_licence);
		paraMap.put("purchase_date", cur_str_date);
		
		CacheManager.getJson(baseAct, url, new IHttpCallback() {
			
			@Override
			public void handle(int retCode, Object response) {
				// TODO Auto-generated method stub
				if(retCode == 200){
					Preference.getInstance(baseAct.getApplicationContext()).setBcall(cur_str_bcall);
					Preference.getInstance(baseAct.getApplicationContext()).setEcall(cur_str_ecall);
//					Preference.getInstance(baseAct.getApplicationContext()).setUser(str_user);
					Preference.getInstance(baseAct.getApplicationContext()).setNickname(cur_str_nickname);
					Preference.getInstance(baseAct.getApplicationContext()).setLicence(cur_str_licence);
					Preference.getInstance(baseAct.getApplicationContext()).setCarMaker(cur_str_maker);
					Preference.getInstance(baseAct.getApplicationContext()).setCarType(cur_str_type);						
					Preference.getInstance(baseAct.getApplicationContext()).setPurchaseDate(cur_str_date);
					Preference.getInstance(baseAct.getApplicationContext()).setBcall(cur_str_bcall);
					Preference.getInstance(baseAct.getApplicationContext()).setEcall(cur_str_ecall);					
					Toast.makeText(baseAct, "修改用户信息成功", Toast.LENGTH_SHORT).show();
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
						new Thread(EditAccount.this).start();
						Base.headbitmap = Member.getHeadBitmapUser(Base.loginUser);
						if(Base.headbitmap != null)
							Base.myBitmap = Util.getRoundedCornerImageColorTriangle(Base.headbitmap, 50*Base.mDensityInt, 50*Base.mDensityInt, 0xff01d4fb);			
					}
				}
				else
					Toast.makeText(baseAct, "修改用户信息失败", Toast.LENGTH_LONG).show();
			}
		}, paraMap);
		return ret;
	}
	
	private boolean isMobileNO(String mobiles){
		Pattern p = Pattern.compile("^((13[0-9])|(15[^4,\\D])|(18[0,5-9]))\\d{8}$");
		Matcher m = p.matcher(mobiles);
		return m.matches();
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.headicon_camera_editacc:
			choosePicSourceDlg = new Dialog(baseAct, R.style.Theme_dialog);
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
						 ((Activity)baseAct).startActivityForResult(galleryIntent, Base.IMAGE_REQUEST_CODE);						
						break;
					case R.id.textvw2:
						choosePicSourceDlg.cancel();
						if (Base.getSDPath() != null) {
						Intent cameraIntent = new Intent(
								"android.media.action.IMAGE_CAPTURE");
						cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, getImageUri());
						cameraIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);
						((Activity)baseAct).startActivityForResult(cameraIntent,
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
	        tv3.setText("取消");
	        tv3.setOnClickListener(dlgClick);
	        
	        choosePicSourceDlg.setCanceledOnTouchOutside(true);
	        choosePicSourceDlg.show();	
			break;

		case R.id.modity_btn_editacc:
			checkAccountInfo();
			cancel();
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
		((Activity)baseAct).startActivityForResult(intent, Base.RESIZE_REQUEST_CODE);
	}

	public void showResizeImage(Intent data) {
		Bundle extras = data.getExtras();
		if (extras != null) {
			headBitmap = (Bitmap) extras.getParcelable("data");
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
				baseAct.getApplicationContext()).getSessionId();

		
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
	
	Handler handler = new Handler(){
		public void handleMessage(Message msg) {
			switch(msg.what){
				case 1:
					Toast.makeText(baseAct, "修改头像成功", Toast.LENGTH_SHORT).show();					
					break;
				case 2:
					Toast.makeText(baseAct, "修改头像失败", Toast.LENGTH_SHORT).show();
					break;
			}
		}
	};
	@Override
	public void run() {
		// TODO Auto-generated method stub
		if(uploadHeadIcon(url).equals("OK")){
			handler.obtainMessage(1).sendToTarget();
			setHeadIcon = false;
			Base.headbitmap = Member.getHeadBitmapUser(Base.loginUser);
			if(Base.headbitmap != null)
				Base.myBitmap = Util.getRoundedCornerImageColorTriangle(Base.headbitmap, 50*Base.mDensityInt, 50*Base.mDensityInt, 0xff01d4fb);
//			Base.OBDApp.mLocationClient.start();
		}
		else
			handler.obtainMessage(2).sendToTarget();
	}
	@Override
	public void onCancel(DialogInterface dialog) {
		// TODO Auto-generated method stub
		baseAct.editAccountDlg = null;
	}
	
	@Override
	public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP){
			InputMethodManager imm = (InputMethodManager)baseAct.getSystemService(Context.INPUT_METHOD_SERVICE);
			if(imm.isActive())
				;
			else{
				EditAccount.this.hide();
				return true;
			}
		}
		return false;
	}
}
