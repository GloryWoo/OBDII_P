package com.ctg.trafficViolation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.ctg.bean.City;
import com.ctg.bean.CarData;
import com.ctg.bean.Province;
import com.ctg.crash.LogRecord;
import com.ctg.net.CacheManager;
import com.ctg.net.HttpQueue;
import com.ctg.net.IHttpCallback;
import com.ctg.trafficViolation.TrafficVioSearchCityAdapter.OnCitySelected;
import com.ctg.ui.Base;
import com.ctg.ui.OBDApplication;
import com.ctg.ui.R;
import com.ctg.util.Preference;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.Selection;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class TrafficVioQueryDlg extends Dialog implements
		DialogInterface.OnCancelListener {
	private static final String TAG = "TrafficVioQueryDlg";
	Button cancelButton;
	private Base baseAct;
	InputMethodManager inputMethodManager;
	public static final int QUERY_READY = 100;
	private static int default_width = 720; //
	private static int default_height = 1280;//

	public static final String QUERY_RESULT = "query_result";

	// Button dftBtn1;
	// Button dftBtn2;
	// Button clearBtn;
	public EditText licenceNo;
	public EditText classNo;
	public EditText engineNo;
	public EditText registNo;
	Button queryBtn;

	String dftLicenceNo;
	String dftClassNo;
	String dftEngineNo;
	String dftLicenceCity;
	String dftRegistNo;
	int engineNum = 0;
	int classNum = 0;
	int registNum = 0;
	// private TextView textView;
	private TextView engineText;
	private TextView classText;
	private TextView registText;
	private TextView licence_city;

	FrameLayout wz_frame;
	ProgressBar progress;
	View traffic_back;
	

	
	private HttpQueue httpQueue;
	public String queryURL = Base.NEW_HTTP_ROOT_PATH + "/ticket/getTicketInfo";

	private android.widget.Button.OnClickListener cancelListen = new android.widget.Button.OnClickListener() {

		@Override
		public void onClick(View v) {
			TrafficVioQueryDlg.this.cancel();
		}

	};

	public TrafficVioQueryDlg(Context context, int layout, int style) {
		this(context, default_width, default_height, layout, style);

	}

	public TrafficVioQueryDlg(Context context, int width, int height,
			int layout, int style) {
		super(context, style);
		// set content
		setContentView(layout);

		// mac_address_init();
		// set window params
		Window window = getWindow();
		WindowManager.LayoutParams params = window.getAttributes();
		// set width,height by density and gravity
		float density = 1;// getDensity(context);
		params.width = (int) (width * density);
		params.height = (int) (height * density);
		params.gravity = Gravity.TOP;
		// params.verticalMargin = 2.0F;
		window.setAttributes(params);
		baseAct = (Base) context;
		// dftBtn1 = (Button) findViewById(R.id.default_query1);
		// dftBtn2 = (Button) findViewById(R.id.default_query2);
		// clearBtn = (Button) findViewById(R.id.default_clear);

		initView();
		getLastQuery();

		initListener();
		inputMethodManager = (InputMethodManager) baseAct
				.getSystemService(Context.INPUT_METHOD_SERVICE);
	}
	
	@Override
	public void dismiss() {
		// TODO Auto-generated method stub
		baseAct.unregisterReceiver(mReceiver);
		super.dismiss();
	}

	/**
	 * 默认填写好上次成功查询的数据
	 */
	protected void getLastQuery() {
//		Preference.getInstance(baseAct).setLicenceNo("");
		dftLicenceCity = Preference.getInstance(baseAct).getLicenceCity();
		dftLicenceNo = Preference.getInstance(baseAct).getLicenceNo();
		dftClassNo = Preference.getInstance(baseAct).getClassNo();
		dftEngineNo = Preference.getInstance(baseAct).getEngineNo();		
		dftRegistNo = Preference.getInstance(baseAct).getRegistNo();
		classNum = Preference.getInstance(baseAct).getClassNum();
		engineNum = Preference.getInstance(baseAct).getEngineNum();		
		registNum = Preference.getInstance(baseAct).getRegistNum();
		
//		if ("".equals(dftLicenceNo)) {
//			licenceNo.setVisibility(View.INVISIBLE);
//		} else 
		{
			licenceNo.setVisibility(View.VISIBLE);
			licenceNo.setText(dftLicenceNo);
		}

		if ("".equals(dftClassNo)) {
			classText.setVisibility(View.GONE);
			classNo.setVisibility(View.GONE);
		} else {
			classText.setVisibility(View.VISIBLE);
			classNo.setVisibility(View.VISIBLE);
			classNo.setText(dftClassNo);
			if(classNum == 100)
				classNo.setHint("请输入全部的车架号");
			else
				classNo.setHint("请输入车架号后"+classNum+"位");
		}

//		if ("".equals(dftEngineNo)) {
//			engineText.setVisibility(View.GONE);
//			engineNo.setVisibility(View.GONE);
//		} else 
		{
			engineText.setVisibility(View.VISIBLE);
			engineNo.setVisibility(View.VISIBLE);
			engineNo.setText(dftEngineNo);
			if(engineNum == 100)
				engineNo.setHint("请输入全部的发动机号");
			else
				engineNo.setHint("请输入发动机号后"+engineNum+"位");			
		}

		if ("".equals(dftRegistNo)) {
			registText.setVisibility(View.GONE);
			registNo.setVisibility(View.GONE);
		} else {
			registText.setVisibility(View.VISIBLE);
			registNo.setVisibility(View.VISIBLE);
			registNo.setText(dftRegistNo);
			if(registNum == 100)
				registNo.setHint("请输入全部的登记证书号");
			else
				registNo.setHint("请输入登记证书号后"+registNum+"位");
		}

		if ("".equals(dftLicenceCity)) {
			licence_city.setVisibility(View.GONE);
		} else {
			licence_city.setVisibility(View.VISIBLE);
			licence_city.setText(dftLicenceCity);
		}

	}

	protected void initView() {
		licenceNo = (EditText) findViewById(R.id.licence_cont);
		classNo = (EditText) findViewById(R.id.class_cont);
		engineNo = (EditText) findViewById(R.id.engine_cont);
		registNo = (EditText) findViewById(R.id.regist_cont);
		queryBtn = (Button) findViewById(R.id.query_press);

		// //lzy
		// textView = (TextView) findViewById(R.id.licence_city);
		engineText = (TextView) findViewById(R.id.engine_no);
		classText = (TextView) findViewById(R.id.class_no);
		registText = (TextView) findViewById(R.id.regist_no);
		licence_city = (TextView) findViewById(R.id.licence_city);

		wz_frame = (FrameLayout) findViewById(R.id.wz_frame);
		progress = (ProgressBar) findViewById(R.id.wz_progressbar);
		traffic_back = findViewById(R.id.traffic_back);
		progress.setVisibility(View.INVISIBLE);
	}

	protected void initListener() {
		queryBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (licence_city.getText().toString() == null
						|| licenceNo.getText().toString() == null
						|| licenceNo.getText().toString().length() == 0) {
					Toast.makeText(
							baseAct,
							baseAct.getResources().getString(
									R.string.wz_input_empty),
							Toast.LENGTH_SHORT).show();
					return;
				}
				LogRecord.SaveLogInfo2File(Base.OperateInfo, TAG+"query btn click");
				Map<String, String> query = new HashMap<String, String>();
				query.put("PlateNo", licence_city.getText().toString()
						+ licenceNo.getText().toString());
				query.put("Vin", classNo.getText().toString());
				query.put("EngineNo", engineNo.getText().toString());



				QueryData queryData = new QueryData(licence_city.getText()
						.toString() + licenceNo.getText().toString(), classNo
						.getText().toString(), engineNo.getText().toString(),
						registNo.getText().toString());
				JSONObject jsonQuery = queryDataToJson(queryData);
				httpQueue = HttpQueue.getInstance(baseAct);
				httpQueue.EnQueue(queryURL, jsonQuery, 32, queryHandler);

				// baseAct.cheshouye = new CheShouYe(licence_city.getText()
				// .toString() + licence.getText().toString(), classNo.getText()
				// .toString(), "", "", "02", queryHandler);
				// new Thread(baseAct.cheshouye).start();
//				wz_frame.setAlpha(0.6f);
				progress.setVisibility(View.VISIBLE);
				
				inputMethodManager.hideSoftInputFromWindow(TrafficVioQueryDlg.this
						.getCurrentFocus().getWindowToken(),
						WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
//				if(inputMethodManager.isActive())
//					inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_NOT_ALWAYS);
			}
		});
		// 检索车牌开头省市
		licence_city.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						Intent intent = new Intent(baseAct,
								TrafficVioSearchCity.class);
						baseAct.startActivity(intent);
					}
				});
		traffic_back.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				TrafficVioQueryDlg.this.cancel();
			}
			
		});
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		// 注册广播接收者 更新选择好城市后的界面
		IntentFilter filter = new IntentFilter("com.search_city.SUCCESS");
		baseAct.registerReceiver(mReceiver, filter);
		super.onStart();

	}

	BroadcastReceiver mReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			// 刷新dialog界面
			Bundle bundle = intent.getExtras();
			String cityHead = bundle.getString("cityHead");
			String cityName = bundle.getString("cityName");
			String abbr = bundle.getString("abbr");
			int engine = bundle.getInt("engine");
			int classa = bundle.getInt("classa");
			int regist = bundle.getInt("regist");
			int engineno = bundle.getInt("engineno");
			int classno = bundle.getInt("classno");
			int registno = bundle.getInt("registno");
			licenceNo.setText("");
			licence_city.setText(cityHead);

			if (engine == 0) {
				engineNum = 0;
				engineNo.setVisibility(View.GONE);
				engineText.setVisibility(View.GONE);
			} else {
				if (engineno == 0) {
					engineNum = 100;
					engineNo.setText("");
					engineNo.setVisibility(View.VISIBLE);
					engineText.setVisibility(View.VISIBLE);
					engineNo.setHint("请输入全部的发动机号");
				} else {
					engineNum = engineno;
					engineNo.setText("");
					engineNo.setVisibility(View.VISIBLE);
					engineText.setVisibility(View.VISIBLE);
					engineNo.setHint("请输入发动机号后" + engineno + "位");
				}
			}

			if (classa == 0) {
				classNum = 0;
				classNo.setVisibility(View.GONE);
				classText.setVisibility(View.GONE);
			} else {
				if (classno == 0) {
					classNum = 100;
					classNo.setText("");
					classNo.setVisibility(View.VISIBLE);
					classText.setVisibility(View.VISIBLE);
					classNo.setHint("请输入全部的车架号");
				} else {
					classNum = classno;
					classNo.setText("");
					classNo.setVisibility(View.VISIBLE);
					classText.setVisibility(View.VISIBLE);
					classNo.setHint("请输入车架号后" + classno + "位");
				}
			}

			if (regist == 0) {
				registNum = 0;
				registNo.setVisibility(View.GONE);
				registText.setVisibility(View.GONE);
			} else {
				if (registno == 0) {
					registNum = 100;
					registNo.setText("");
					registNo.setVisibility(View.VISIBLE);
					registNo.setVisibility(View.VISIBLE);
					registNo.setHint("请输入全部的登记证书号");
				} else {
					registNum = registno;
					registNo.setText("");
					registNo.setVisibility(View.VISIBLE);
					registNo.setVisibility(View.VISIBLE);
					registNo.setHint("请输入登记证书号后" + registno + "位");
				}
			}

		}
	};

	public final Handler queryHandler = new Handler() {
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			String reason = null;
			String data = null;
			String ret = null;
			JSONObject jsonObj = null;
			switch (msg.what) {
			case QUERY_READY:
				String retVal = msg.obj.toString();
				Log.v("0", retVal);
				try {
					jsonObj = new JSONObject(retVal);
					ret = jsonObj.getString("resultcode");
					Log.v("1", ret);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Log.v("querydata", retVal);
				// JSONObject jsObj = new JSONObject(retVal);
				// String retResult = jsObj.getString("status");
				if ("200".equalsIgnoreCase(ret)) {
//					if (Base.car_v.wzQueryDlg.licenceNo.getText()
//							.toString().trim().length() > 0) {
//					}
					baseAct.queryLicence = licence_city.getText().toString() + licenceNo.getText().toString();
					try {
						if(jsonObj.has("data")){
							data = jsonObj.getString("data");
							Base.car_v.wzListDlg = new TrafficVioListDlg(baseAct,
									Base.mWidth, Base.mHeight,
									R.layout.trafficvio_list, R.style.Theme_dialog,
									data);

							Base.car_v.wzListDlg.show();
						}
						else{
							data = "恭喜您！没有查到违章信息。";
							Toast.makeText(baseAct, data, Toast.LENGTH_SHORT).show();
						}
						Log.v("2", data);

					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					String licence = licenceNo.getText().toString();
					String classNo = TrafficVioQueryDlg.this.classNo.getText().toString();
					String engineNo = TrafficVioQueryDlg.this.engineNo.getText().toString();
					String registNo = TrafficVioQueryDlg.this.registNo.getText().toString();
					String licence_city = TrafficVioQueryDlg.this.licence_city.getText().toString();

					
					Preference.getInstance(baseAct.getApplicationContext())
							.setLicenceNo(licence);
					Preference.getInstance(baseAct.getApplicationContext())
							.setClassNo(classNo);
					Preference.getInstance(baseAct.getApplicationContext())
							.setEngineNo(engineNo);
					Preference.getInstance(baseAct.getApplicationContext())
							.setRegistNo(registNo);
					Preference.getInstance(baseAct.getApplicationContext())
							.setLicenceCity(licence_city);
					Preference.getInstance(baseAct.getApplicationContext())
						.setEngineNum(engineNum);
					Preference.getInstance(baseAct.getApplicationContext())
						.setClassNum(classNum);
					Preference.getInstance(baseAct.getApplicationContext())
						.setRegistNum(registNum);					
					// Preference.getInstance(
					// baseAct.getApplicationContext())
					// .setClassNo1(cla);
//					TrafficVioQueryDlg.this.dismiss();
					progress.setVisibility(View.GONE);
//					wz_frame.setAlpha(1.0f);

					Log.v("bro", "广播解除注册---非oncancel");
				} else {
					try {
                        if(jsonObj==null){
                        	Toast.makeText(baseAct, "结果为null", 0).show();
                        }else{
                        	reason = jsonObj.getString("reason");
                        	Toast.makeText(baseAct, reason, 0).show();
                        	inputMethodManager.hideSoftInputFromWindow(TrafficVioQueryDlg.this
            						.getCurrentFocus().getWindowToken(),
            						WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
                        }
						
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					Toast.makeText(baseAct, reason, Toast.LENGTH_SHORT).show();
					progress.setVisibility(View.GONE);
					wz_frame.setAlpha(1.0f);
				}

				// TrafficVioQueryDlg.this.dismiss();
				// baseAct.car_v.wzQueryDlg = null;
				break;
			default:
				break;
			}
		}
	};

	@Override
	public void onCancel(DialogInterface dialog) {
		// TODO Auto-generated method stub
		Base.car_v.wzQueryDlg = null;
	}

	public JSONObject queryDataToJson(QueryData queryData) {

		JSONObject jsonObj = new JSONObject();
		try {
			jsonObj.put("plateNo", queryData.getPlateNo());
			jsonObj.put("vin", queryData.getVin());
			jsonObj.put("engineNo", queryData.getEngineNo());

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return jsonObj;
	}

	private class QueryData {
		String plateNo;// 车牌号
		String vin;// 车架号
		String engineNo;// 引擎号
		String registNo;// 引擎号

		public String getRegistNo() {
			return registNo;
		}

		public void setRegistNo(String registNo) {
			this.registNo = registNo;
		}

		public String getPlateNo() {
			return plateNo;
		}

		public void setPlateNo(String plateNo) {
			this.plateNo = plateNo;
		}

		public String getVin() {
			return vin;
		}

		public void setVin(String vin) {
			this.vin = vin;
		}

		public String getEngineNo() {
			return engineNo;
		}

		public void setEngineNo(String engineNo) {
			this.engineNo = engineNo;
		}

		public QueryData(String plateNo, String vin, String engineNo,
				String registNo) {
			super();
			this.plateNo = plateNo;
			this.vin = vin;
			this.engineNo = engineNo;
			this.registNo = registNo;
		}

	}

}
