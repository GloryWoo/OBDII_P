//package com.ctg.group;
//
//
//import java.io.ByteArrayOutputStream;
//import java.io.IOException;
//import java.io.ObjectOutputStream;
//import java.util.ArrayList;
//import java.util.List;
//
//import org.json.JSONException;
//import org.json.JSONObject;
//
//import com.baidu.mapapi.map.BitmapDescriptor;
//import com.baidu.mapapi.map.BitmapDescriptorFactory;
//import com.baidu.mapapi.map.MapStatusUpdateFactory;
//import com.baidu.mapapi.map.MapView;
//import com.baidu.mapapi.map.MarkerOptions;
//import com.baidu.mapapi.map.OverlayOptions;
//import com.baidu.mapapi.map.PolylineOptions;
//import com.baidu.mapapi.model.LatLng;
//import com.ctg.bluetooth.BluetoothSet;
//import com.ctg.group.ChatMsgAdapter;
//import com.ctg.group.ChatMsgAdapter.ViewHolder;
//import com.ctg.group.ChatMsgEntity;
//import com.ctg.group.Group;
//import com.ctg.net.HttpQueue;
//import com.ctg.ui.Base;
//import com.ctg.ui.R;
//import com.ctg.util.Preference;
//import com.ctg.util.Util;
//
//import android.app.Dialog;
//
//import android.bluetooth.BluetoothAdapter;
//import android.bluetooth.BluetoothDevice;
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.DialogInterface;
//
//import android.content.Intent;
//import android.content.IntentFilter;
//import android.content.res.Resources;
//
//import android.text.Editable;
//import android.util.DisplayMetrics;
//
//import android.view.Gravity;
//import android.view.KeyEvent;
//import android.view.View;
//
//import android.view.Window;
//import android.view.WindowManager;
//import android.widget.AdapterView;
//import android.widget.AdapterView.OnItemSelectedListener;
//import android.widget.ArrayAdapter;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.ImageButton;
//import android.widget.LinearLayout;
//import android.widget.ListView;
//import android.widget.TextView;
//import android.widget.Toast;
//import android.widget.AdapterView.OnItemClickListener;
//
//public class ChatDlg extends Dialog implements DialogInterface.OnCancelListener{ 
//	private static final String TAG = "CustomDialog";
//	
//	private static int default_width = 160; //
//	private static int default_height = 120;//
//	
//    Button cancelButton;		
//	private Base baseAct;
//	int groupIdx;
//	Group curGroup;
//	ImageButton locBtn;
//	ImageButton trackBtn;
//	Button sendBtn;
//	EditText sendEdit;
//	public ChatMsgEntity curMsg;
//	public ListView myListv;
//	public ChatMsgAdapter myAdapter;
//	public ArrayList<ChatMsgEntity> myList;
//	//int lastSelect = -1;
//	View lastView;
//	
//	public void onCancel(DialogInterface dialog) {
//		// TODO Auto-generated method stub
//		myList.clear();	
//		Base.me_v.vGroupList.chagDlg = null;
//	}  
//	
//	
//	public ChatDlg(Context context, int layout, int style) {
//		this(context, default_width, default_height, layout, style, null);
//		
//	}
//	
////	public boolean onKeyDown(int keyCode, KeyEvent event){
////		switch(keyCode) {
////			case KeyEvent.KEYCODE_BACK:	
////				ChatDlg.this.cancel();
////				break;
////			default:break;
////		}
////		return false;
////	}
//	
//	public ChatDlg(Context context, int width, int height, int layout, int style, ArrayList<ChatMsgEntity> list) {
//		super(context, style);
//		//set content
//		setContentView(layout);
//		
//		//mac_address_init();
//		//set window params
//		Window window = getWindow();
//		WindowManager.LayoutParams params = window.getAttributes();
//		//set width,height by density and gravity
//		params.width = (int) width;
//		params.height = (int) height;
//		params.gravity = Gravity.TOP;
//		//params.verticalMargin = 2.0F;
//		window.setAttributes(params);
//		baseAct = (Base)context;
//		groupIdx = baseAct.me_v.vGroupList.curGroupPos;
//		curGroup = HttpQueue.grpResLst.get(groupIdx);
//		setTitle(curGroup.name);
//		locBtn = (ImageButton) findViewById(R.id.btn_position);
//		trackBtn = (ImageButton) findViewById(R.id.btn_track);
//		sendBtn = (Button) findViewById(R.id.btn_send);
//		sendEdit = (EditText)findViewById(R.id.et_sendmessage);
//		locBtn.setOnClickListener(myBtnClick);
//		trackBtn.setOnClickListener(myBtnClick);
//		sendBtn.setOnClickListener(myBtnClick);
//		if(list != null)
//			myList = list;
//		else
//			myList = new ArrayList<ChatMsgEntity>();
//		myListv = (ListView) findViewById(R.id.chat_listv);
//		myAdapter = new ChatMsgAdapter(baseAct, myListv, myList);
//		myListv.setAdapter(myAdapter);
//		setOnCancelListener(this);
//		myListv.setOnItemClickListener(new OnItemClickListener(){
//
//			@Override
//			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
//					long arg3) {
//				// TODO Auto-generated method stub
//				ChatMsgEntity entity = myList.get(arg2);
//				if(entity.lOrR == 1)
//					return;
//				//LinearLayout linear = (LinearLayout) myListv.getChildAt(arg2);
//				MapView mapv = (MapView) arg1.findViewById(R.id.chat_mapView);
//				if(lastView != null && lastView != arg1){
//					//LinearLayout lastlinear = (LinearLayout) myListv.getChildAt(lastSelect);
//					MapView mapvl = (MapView) lastView.findViewById(R.id.chat_mapView);
//					if(mapvl != null){
//						mapvl.setVisibility(View.GONE);
//					}
//				}
//				if(entity.msgType != ChatMsgEntity.CHAT_MSG_TEXT && entity.lOrR == 0){
//					if(mapv != null){
//						if(mapv.getVisibility() == View.GONE){
//							mapv.setVisibility(View.VISIBLE);					
//							if(entity.msgType == ChatMsgEntity.CHAT_MSG_LOCATE){
//								mapv.getMap().clear();
//								//mapv.getMap().setMapType(arg0);
//								BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(R.drawable.icon_marka);
//								OverlayOptions option = new MarkerOptions().position(entity.latlon_loc)
//										.icon(bitmap);
//								mapv.getMap().setMapStatus(MapStatusUpdateFactory
//										.newLatLng(entity.latlon_loc));
//								mapv.getMap().addOverlay(option);						
//							}
//							else if(entity.msgType == ChatMsgEntity.CHAT_MSG_TRACK){
//								mapv.getMap().clear();
//								OverlayOptions polylineOption = new PolylineOptions().points(
//										entity.latlon_track).color(0xFF000000);
//								mapv.getMap().setMapStatus(MapStatusUpdateFactory
//								.newLatLng(entity.latlon_track.get(0)));
//								mapv.getMap().addOverlay(polylineOption);
//							}
//						}
//						else{
//							mapv.setVisibility(View.GONE);
//						}
//							
//					}
//				}
//				//lastSelect = arg2;
//				lastView = arg1;
//			}
//		});
//		
//	}
//	
//	View.OnClickListener myBtnClick = new View.OnClickListener(){
//
//		@Override
//		public void onClick(View v) {
//			// TODO Auto-generated method stub
//			String dataStr = Util.getDate();
//			String loginUser = Preference.getInstance(getContext()).getUser();
//			String textStr;
//			String editStr;
//			JSONObject obj = new JSONObject();
//			JSONObject obj_enti = new JSONObject();	
//			String url = Base.HTTP_GROUP_PATH+"/pushMessagesToGroupUsers";
//			if(v.getId() == locBtn.getId()){
//				editStr = baseAct.getResources().getString(R.string.chat_my_location);
//				editStr += baseAct.baidu_v.curPoiName;
//				editStr += "(" + baseAct.baidu_v.mCurLatitude +"," + baseAct.baidu_v.mCurLongitude + ")";
//				//sendEdit.setText(editStr);
//				curMsg = new ChatMsgEntity(loginUser, curGroup.name,dataStr, "",  1, ChatMsgEntity.CHAT_MSG_LOCATE,
//						new LatLng(baseAct.baidu_v.mCurLatitude, baseAct.baidu_v.mCurLongitude));										 						
//				baseAct.httpQueueInstance.EnQueue(url, null, 20, curMsg);
//			}
//			else if(v.getId() == trackBtn.getId()){
//				editStr = baseAct.getResources().getString(R.string.chat_my_track);
//				
//				//test
//				ArrayList<LatLng> list = new ArrayList<LatLng>();
//				list.add(new LatLng(23.0, 126.0));
//				list.add(new LatLng(23.1, 126.1));
//				list.add(new LatLng(23.2, 126.2));
//				
//				curMsg = new ChatMsgEntity(loginUser, curGroup.name, dataStr, "",  1, ChatMsgEntity.CHAT_MSG_TRACK,
//						list);										
//				baseAct.httpQueueInstance.EnQueue(url, null, 20, curMsg);
//			}
//			else if(v.getId() == sendBtn.getId()){
//				Editable edita = sendEdit.getText();
//				if(edita != null && edita.toString() != null && !edita.toString().equals("")){
//					editStr = edita.toString();
//					curMsg = new ChatMsgEntity(loginUser, curGroup.name, dataStr, editStr,  1, ChatMsgEntity.CHAT_MSG_TEXT);	
//					baseAct.httpQueueInstance.EnQueue(url, null, 20, curMsg);
//				}
//			}
//		}
//		
//	};
//
//}
