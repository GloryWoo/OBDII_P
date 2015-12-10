package com.ctg.netmusic;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import com.ctg.netmusic.RadioLstAdapter.ICheckPlayImgState;
import com.ctg.ui.Base;
import com.ctg.ui.R;
import com.ctg.util.HorizontalListView;
import com.ximalaya.ting.android.opensdk.constants.DTransferConstants;
import com.ximalaya.ting.android.opensdk.constants.DTransferConstants.WeekDay;
import com.ximalaya.ting.android.opensdk.datatrasfer.CommonRequest;
import com.ximalaya.ting.android.opensdk.datatrasfer.IDataCallBack;
import com.ximalaya.ting.android.opensdk.httputil.Config;
import com.ximalaya.ting.android.opensdk.model.live.provinces.Province;
import com.ximalaya.ting.android.opensdk.model.live.provinces.ProvinceList;
import com.ximalaya.ting.android.opensdk.model.live.radio.Radio;
import com.ximalaya.ting.android.opensdk.model.live.radio.RadioList;
import com.ximalaya.ting.android.opensdk.model.live.schedule.ScheduleList;
import com.ximalaya.ting.android.opensdk.player.XmPlayerManager;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class XimalayaFM extends Fragment implements View.OnClickListener, ICheckPlayImgState{
	private static String TAG = "XimalayaFM";
	private static String secret_key = "bed66c2de16219d358ffb3f4ed577408";
	public static final int GET_PROVINCE_LIST_READY = 0x1000;
	public static final int GET_RADIO_LIST_READY = 0x1001;
	public static final int GET_SCHEDULE_LIST_READY = 0x1002;
	HashMap<Integer, RadioList> radioLstHash;
	HashMap<Integer, ScheduleList> scheduleHash;
	ProvinceList provLst;
	Context mContext;
	ImageView back_img;
	TextView country_labl, province_labl, web_labl;
	HorizontalListView province_lv;
	ListView radio_lv;
	ProvLstAdapter provinceAdpt;
	RadioLstAdapter radioAdpt;
	FragOnDestroy fragDestroy;
	RadioList radioLst;
	Province province;
	int lstType = 1; //1 country 2 province 3 web
	int lastFocusProvinceId = 0;
	int focusProvinceId;
	int lastFocusRadioId;
	int focusRadioId = -1;
	Radio curRadio;
	Radio lastRadio;
	public XmPlayerManager xmPlayer;
	//int provinceCode;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = getActivity();
		CommonRequest.getInstanse().init(mContext, secret_key);
		setHttpConfig();
		radioLstHash = new HashMap<Integer, RadioList>();
		scheduleHash = new HashMap<Integer, ScheduleList>();
		Map<String, String> map = new HashMap<String, String>();
		map.put(DTransferConstants.RADIOTYPE, ""+1);
		xmPlayer = XmPlayerManager.getInstance(mContext);
		xmPlayer.init();
		CommonRequest.getRadios(map, new IDataCallBack<RadioList>()
		{
			@Override 
			public void onSuccess(RadioList object)
			{
				radioLstHash.put(1, object);
				radioLst = object;
				handler.obtainMessage(GET_RADIO_LIST_READY).sendToTarget();
			}
			@Override
			public void onError(int code, String message)
			{
				Log.d(TAG, message);
			}
		});
		map = new HashMap<String, String>();
		CommonRequest.getProvinces(map, new IDataCallBack<ProvinceList>()
		{
			@Override public void onSuccess(ProvinceList object)
			{
				provLst = object;
				handler.obtainMessage(GET_PROVINCE_LIST_READY).sendToTarget();
			}
			@Override
			public void onError(int code, String message)
			{
				Log.d(TAG, message);
			}
		});

		//map.put(DTransferConstants.PROVINCECODE, 310000);
		//map.put(DTransferConstants.PAGE, mPageNum);
	}

	public void setOnDestroy(FragOnDestroy destroy){
		fragDestroy = destroy;
	}
	
	Handler handler = new Handler(){
		public void handleMessage(Message msg) {
			if(!Base.isBaseActive)
				return;
			switch(msg.what){
				case GET_PROVINCE_LIST_READY:
					provinceAdpt.setProvLst(provLst);
					break;
				case GET_RADIO_LIST_READY:
					radioAdpt.setList(radioLst);
					break;
				case GET_SCHEDULE_LIST_READY:
					break;
				default:
					break;
			}	
		}
	};
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.ximalaya_list, container, false);
		back_img = (ImageView) view.findViewById(R.id.fm_back_img);
		country_labl = (TextView) view.findViewById(R.id.fm_state);
		province_labl = (TextView) view.findViewById(R.id.fm_province);
		web_labl = (TextView) view.findViewById(R.id.fm_internet);
		back_img.setOnClickListener(this);
		country_labl.setOnClickListener(this);
		province_labl.setOnClickListener(this);
		web_labl.setOnClickListener(this);
		province_lv = (HorizontalListView) view.findViewById(R.id.province_city);		
		radio_lv = (ListView) view.findViewById(R.id.fm_radio_id);
		radio_lv.setDivider(Base.gray_line_draw);
		
		provinceAdpt = new ProvLstAdapter(mContext, provLst); 
		province_lv.setAdapter(provinceAdpt);
		radioAdpt = new RadioLstAdapter(mContext, radioLst);
		radioAdpt.setCheckPlayStat(this);
		radio_lv.setAdapter(radioAdpt);
		
		province_lv.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				if(focusProvinceId == position)
					return;
				ImageView focus_i = (ImageView) view.findViewById(R.id.xima_prov_line);				
				focus_i.setVisibility(View.VISIBLE);
				focusProvinceId = position;
				
				View lastView = parent.getChildAt(lastFocusProvinceId);
				if(lastView != null){
					ImageView lastfocus_i = (ImageView) lastView.findViewById(R.id.xima_prov_line);
					lastfocus_i.setVisibility(View.INVISIBLE);
				}
				
				provinceAdpt.setFocusId(position);
				lastFocusProvinceId = focusProvinceId;
				
				province = provLst.getProvinceList().get(position);
				radioLst = radioLstHash.get(province.getProvinceCode());
				if(radioLst != null){
					radioAdpt.setList(radioLst);                
				}
				else{
					Map<String, String> map = new HashMap<String, String>();
					map.put(DTransferConstants.RADIOTYPE, ""+2);
					map.put(DTransferConstants.PROVINCECODE, ""+province.getProvinceCode());
					CommonRequest.getRadios(map, new IDataCallBack<RadioList>()
					{
						@Override 
						public void onSuccess(RadioList object)
						{
							radioLst = object;
							radioLstHash.put((int) province.getProvinceCode(), object);
							handler.obtainMessage(GET_RADIO_LIST_READY).sendToTarget();
						}
						@Override
						public void onError(int code, String message)
						{
						}
					});
				}
			}
			
		});

		radio_lv.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				
				focusRadioId = position;
				radioAdpt.setFocusId(position);
				if(lastFocusRadioId != -1){
					View lastView = parent.getChildAt(lastFocusRadioId);
					if(lastView != null){
						ImageView lastfocus_i = (ImageView) lastView.findViewById(R.id.play);
						lastfocus_i.setImageResource(R.drawable.icon_download_stop);
					}
				}
				ImageView focus_i = (ImageView) view.findViewById(R.id.play);				
				curRadio = radioLst.getRadios().get(position);
			
				//boolean playOrPause = false;
				if(lastRadio != null && lastRadio.equals(curRadio)){
					if(xmPlayer.isPlaying()){
						
						xmPlayer.pause();
						focus_i.setImageResource(R.drawable.icon_download_stop);
						if(fragDestroy != null)
							fragDestroy.onPauseRadio();
					}
					else{						
						xmPlayer.play();
						focus_i.setImageResource(R.drawable.icon_download_paly);
					}
				}else{					
					if(xmPlayer.isPlaying()){
						xmPlayer.stop();
					}
					xmPlayer.playRadio(curRadio);
					focus_i.setImageResource(R.drawable.icon_download_paly);
				}
				Calendar cal = Calendar.getInstance();
				int day = cal.get(Calendar.DAY_OF_WEEK);
				if(fragDestroy != null){
					fragDestroy.onFragDestroy();
					if(lastRadio == null || !lastRadio.equals(curRadio)){
						fragDestroy.onPlayRadio(curRadio);
						ScheduleList list = scheduleHash.get(curRadio.getDataId());
						if(list == null){
							Map<String, String> map = new HashMap<String, String>();
							map.put(DTransferConstants.RADIOID, ""+curRadio.getDataId());
							map.put(DTransferConstants.WEEKDAY, ""+(day-1));
							final int curId = (int) curRadio.getDataId();
							CommonRequest.getSchedules(map, new IDataCallBack<ScheduleList>()
							{
								@Override public void onSuccess(ScheduleList object)
								{
									scheduleHash.put(curId, object);
									fragDestroy.getScheduleReady(object);
									//handler.obtainMessage(GET_SCHEDULE_LIST_READY).sendToTarget();
								}
								@Override
								public void onError(int code, String message)
								{
								}
							});
						}
						else{
							fragDestroy.getScheduleReady(list);
						}
					}
				}
				lastRadio = curRadio;
				lastFocusRadioId = focusRadioId;
		
			}
			
		});
		return view;

	}

	@Override
	public void onResume() {
		super.onResume();
		radio_lv.setAdapter(radioAdpt);
//		if(province_lv.getVisibility() == View.VISIBLE)
//			province_lv.setAdapter(provinceAdpt);
	}

	public void onPause(){
		super.onPause();
		radio_lv.setAdapter(null);
//		if(province_lv.getVisibility() == View.VISIBLE)
//			province_lv.setAdapter(null);
	}
	
	public void onDestroy() {		
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()){
		case R.id.fm_back_img:
			if(fragDestroy != null)
				fragDestroy.onFragDestroy();
			break;
		case R.id.fm_state:
			if(lstType == 1)
				return;
			lstType = 1;
			radioLst = radioLstHash.get(1);
			if(radioAdpt != null && radioLst != null){
				radioAdpt.setList(radioLst);
			}
			province_lv.setVisibility(View.GONE);
			break;
		case R.id.fm_province:
			if(lstType == 2)
				return;
			lstType = 2;
			//radioLst = radioLstHash.get(1);
			if(provLst == null)
				return;
			province = provLst.getProvinceList().get(focusProvinceId);
			radioLst = radioLstHash.get(province.getProvinceCode());
			if(radioLst != null){				
				radioAdpt.setList(radioLst);
			}
			else{
				Map<String, String> map = new HashMap<String, String>();
				map.put(DTransferConstants.RADIOTYPE, ""+2);
				map.put(DTransferConstants.PROVINCECODE, ""+province.getProvinceCode());
				xmPlayer = XmPlayerManager.getInstance(mContext);
				xmPlayer.init();
				CommonRequest.getRadios(map, new IDataCallBack<RadioList>()
				{
					@Override 
					public void onSuccess(RadioList object)
					{
						radioLstHash.put((int) province.getProvinceCode(), object);
						radioLst = object;
						handler.obtainMessage(GET_RADIO_LIST_READY).sendToTarget();
					}
					@Override
					public void onError(int code, String message)
					{
						Log.d(TAG, message);
					}
				});
			}
			province_lv.setVisibility(View.VISIBLE);
			break;
		case R.id.fm_internet: 
			if(lstType == 3)
				return;
			radioLst = radioLstHash.get(3);
			if(radioLst != null){
				radioAdpt.setList(radioLst);
			}
			else{
				Map<String, String> map = new HashMap<String, String>();
				map.put(DTransferConstants.RADIOTYPE, ""+3);
				CommonRequest.getRadios(map, new IDataCallBack<RadioList>()
				{
					@Override 
					public void onSuccess(RadioList object)
					{
						radioLstHash.put(3, object);
						radioLst = object;
						handler.obtainMessage(GET_RADIO_LIST_READY).sendToTarget();
					}
					@Override
					public void onError(int code, String message)
					{
						Log.d(TAG, message);
					}
				});
			}
			lstType = 3;
			province_lv.setVisibility(View.GONE);
			break;
			default:break;
		}
	}
	
	public void setHttpConfig(){
		Config config = new Config(); 
		config.useProxy = false; //若想使用代理，必须配置此项为true，否则代理配置被忽略
//		config.proxyHost = "192.168.3.1";
//		config.proxyPort = 1080;
		config.connectionTimeOut = 3000;
		config.readTimeOut = 3000;
		CommonRequest.getInstanse().setHttpConfig(config);
	}

	@Override
	public boolean checkImgPlayOrnot(Radio curRadio) {
		// TODO Auto-generated method stub
		if(lastRadio != null && lastRadio.equals(curRadio))
			return true;
		return false;
	}


}
