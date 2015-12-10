package com.ctg.plat.phone;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.ctg.ui.Base;
import com.ctg.ui.R;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.CallLog;
import android.provider.CallLog.Calls;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class Recent extends Fragment{
	
	private View CallLogView;
	// 通话记录的列表
	private ListView m_calllogslist;
	// 通话记录列表的适配器
	private CallLogsCursorAdapter m_calllogsadapter;
	// 加载器监听器
	private CallLogsLoaderListener m_CallLogsCallback = new CallLogsLoaderListener();
	// 通话记录内容观察者
	private CallLogsContentObserver CallLogsCO = new CallLogsContentObserver(new Handler());;

	Cursor loadCursor;
	
	class CallLogsContentObserver extends ContentObserver{

	    public CallLogsContentObserver(Handler handler) {
	        super(handler);
	        // TODO Auto-generated constructor stub
	    }

	    @Override
	    public void onChange(boolean selfChange) {
	        Log.i("huahua", "通话记录数据库发生了变化");
//	      Utils.getCallLogs();
	    }

	}
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//LayoutInflater inflater = getActivity().getLayoutInflater();
		// 通话记录列表
		getActivity().getContentResolver().registerContentObserver(
				CallLog.Calls.CONTENT_URI, false, CallLogsCO);
		getActivity().getLoaderManager()
				.initLoader(1, null, m_CallLogsCallback);	
//		m_calllogsadapter = new CallLogsCursorAdapter(getActivity(), null);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		CallLogView = inflater.inflate(R.layout.fragment_call_log, null);
		// ViewGroup p = (ViewGroup) CallLogView.getParent();
		// if (p != null) {
		// p.removeAllViewsInLayout();
		// }
		
		m_calllogslist = (ListView) CallLogView
				.findViewById(R.id.calllogs_list);
		
		ColorDrawable draw = new ColorDrawable(Color.parseColor("#30FFFFFF"));
		m_calllogslist.setDivider(draw);
		m_calllogslist.setDividerHeight(1);
		//getActivity().getLoaderManager().getLoader(1).forceLoad();
		m_calllogslist.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				Cursor cursor = loadCursor;
				int i = position;
				while(i > 0 && cursor != null){
					i--;
					cursor.moveToNext();
				}
				String number = cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER));
				Intent intent = new Intent(Intent.ACTION_CALL , Uri.parse("tel:" +  number));
				getActivity().startActivity(intent);
				Base.OBDApp.callStat = 2;
			}
			
		});
		return CallLogView;
	}
	
	@Override
	public void onDestroyView(){		
		super.onDestroyView();
	}

	@Override
	public void onDestroy() {
		//m_calllogslist.setAdapter(null);
		super.onDestroy();		
	}

	@Override
	public void onPause() {
		super.onPause();
		m_calllogslist.setAdapter(null);
	}
	
    @Override
	public void onResume() {  
        super.onResume();  
		if(m_calllogsadapter != null)
			m_calllogslist.setAdapter(m_calllogsadapter);
      
    }    
    
	@Override
	public void onStop() {
		super.onStop();
	}

	// 加载器的监听器
	private class CallLogsLoaderListener implements
			LoaderManager.LoaderCallbacks<Cursor> {
		@Override
		public Loader<Cursor> onCreateLoader(int id, Bundle args) {
			return new CursorLoader(getActivity(), CallLog.Calls.CONTENT_URI,
					null, null, null, CallLog.Calls.DEFAULT_SORT_ORDER);
		}

		@Override
		public void onLoadFinished(Loader<Cursor> arg0, Cursor arg1) {			
			//Cursor cursor = m_calllogsadapter.swapCursor(arg1);
			loadCursor = arg1;
			loadCursor.moveToFirst();
			m_calllogsadapter = new CallLogsCursorAdapter(getActivity(), loadCursor);
			m_calllogslist.setAdapter(m_calllogsadapter);
			//m_calllogsadapter.notifyDataSetInvalidated();
		}

		@Override
		public void onLoaderReset(Loader<Cursor> arg0) {
			m_calllogsadapter.swapCursor(null);
		}
	}

	private class CallLogsCursorAdapter extends CursorAdapter {
		private Context context;

		public CallLogsCursorAdapter(Context context, Cursor c) {
			super(context, c);
			this.context = context;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			return super.getView(position, convertView, parent);
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			if (cursor == null) {
				return;
			}
			// 通话记录的姓名
			TextView name = (TextView) view.findViewById(R.id.name);
			name.setText(cursor.getString(cursor
					.getColumnIndex(CallLog.Calls.CACHED_NAME)));
			if (cursor.getString(cursor
					.getColumnIndex(CallLog.Calls.CACHED_NAME)) == null) {
				name.setText("未知号码");
			} else {
				name.setText(cursor.getString(cursor
						.getColumnIndex(CallLog.Calls.CACHED_NAME)));
			}
			// 通话记录的电话状态
			ImageView callType = (ImageView) view.findViewById(R.id.dialImage);
			if (cursor.getInt(cursor.getColumnIndex(CallLog.Calls.TYPE)) == CallLog.Calls.INCOMING_TYPE) {
				callType.setBackgroundResource(android.R.drawable.sym_call_incoming);
			} else if (cursor.getInt(cursor.getColumnIndex(CallLog.Calls.TYPE)) == CallLog.Calls.OUTGOING_TYPE) {
				callType.setBackgroundResource(android.R.drawable.sym_call_outgoing);
			} else if (cursor.getInt(cursor.getColumnIndex(CallLog.Calls.TYPE)) == CallLog.Calls.MISSED_TYPE) {
				callType.setBackgroundResource(android.R.drawable.sym_call_missed);
			}else if(cursor.getInt(cursor.getColumnIndex(Calls.TYPE)) == 5){
				callType.setImageResource(android.R.drawable.sym_call_outgoing);
			}
			// 通话记录的号码
			TextView number = (TextView) view.findViewById(R.id.number);
			number.setText(cursor.getString(cursor
					.getColumnIndex(CallLog.Calls.NUMBER)));
			// 通话记录的日期
			TextView date = (TextView) view.findViewById(R.id.dialDate);
			Date date2 = new Date(Long.parseLong(cursor.getString(cursor
					.getColumnIndex(CallLog.Calls.DATE))));
			SimpleDateFormat sfd = new SimpleDateFormat("yyyy/MM/dd\n"
					+ "HH:mm:ss");
			String time = sfd.format(date2);
			date.setText(time);
			ImageView dialBtn = (ImageView) view.findViewById(R.id.image);
			dialBtn.setTag(cursor.getString(cursor
					.getColumnIndex(CallLog.Calls.NUMBER)));
			dialBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View arg0) {
					Intent intent = new Intent(Intent.ACTION_CALL, Uri
							.parse("tel://" + (String) arg0.getTag()));
					startActivity(intent);
				}
			});
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			return LayoutInflater.from(context).inflate(
					R.layout.calllog_list_item, parent, false);
		}
	}


}