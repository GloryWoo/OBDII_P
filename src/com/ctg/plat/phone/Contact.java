package com.ctg.plat.phone;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import android.app.Fragment;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.ctg.plat.phone.MyLetterListView.OnTouchingLetterChangedListener;
import com.ctg.ui.Base;
import com.ctg.ui.R;

public class Contact extends Fragment implements View.OnClickListener{

	private BaseAdapter adapter;  
    private ListView personList;
    private TextView overlay;
    private MyLetterListView letterListView;
    private AsyncQueryHandler asyncQuery;  
    private static final String NAME = "name", NUMBER = "number", SORT_KEY = "sort_key";
    private HashMap<String, Integer> alphaIndexer;//��Ŵ��ڵĺ���ƴ������ĸ����֮��Ӧ���б�λ��
    private String[] sections;//��Ŵ��ڵĺ���ƴ������ĸ
    private Handler handler;
    private OverlayThread overlayThread;
    List<ContentValues> cvlist;  
    
    @Override 
    public void onCreate(Bundle savedInstanceState)  
    {  
        super.onCreate(savedInstanceState);         
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {  

    	View view = inflater.inflate(R.layout.fragment_contact, container, false);
    	
        
        personList = (ListView) view.findViewById(R.id.list_view);
        letterListView = (MyLetterListView) view.findViewById(R.id.MyLetterListView01);
        letterListView.setOnTouchingLetterChangedListener(new LetterListViewListener());
        personList.setDivider(null);
        asyncQuery = new MyAsyncQueryHandler(getActivity().getContentResolver());
        alphaIndexer = new HashMap<String, Integer>();
        handler = new Handler();
        overlayThread = new OverlayThread();
        initOverlay();
        cvlist = new ArrayList<ContentValues>(); 
        Uri uri = Uri.parse("content://com.android.contacts/data/phones");  
        String[] projection = { "_id", "display_name", "data1", "sort_key" };  
        asyncQuery.startQuery(0, null, uri, projection, null, null,  
                "sort_key COLLATE LOCALIZED asc");
        
        //fix listview height
//        ViewGroup.LayoutParams params = personList.getLayoutParams(); 
//        params.height = view.getHeight(); //
//        personList.setLayoutParams(params); 
		return view;
    }  
  
    @Override
	public void onResume() {  
        super.onResume();  

    }  
  
    public void onDestroy(){
    	super.onDestroy();
    	personList.setAdapter(null);
    }
    //��ѯ��ϵ��
    private class MyAsyncQueryHandler extends AsyncQueryHandler {  
  
        public MyAsyncQueryHandler(ContentResolver cr) {  
            super(cr);  
  
        }  
  
        @Override  
        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {  
            if (cursor != null && cursor.getCount() > 0) {  
            	cvlist = new ArrayList<ContentValues>();  
                cursor.moveToFirst();  
                for (int i = 0; i < cursor.getCount(); i++) {
                    ContentValues cv = new ContentValues();  
                    cursor.moveToPosition(i);  
                    String name = cursor.getString(1);  
                    String number = cursor.getString(2);  
                    String sortKey = cursor.getString(3);
                    if (number.startsWith("+86")) {  
                        cv.put(NAME, name);  
                        cv.put(NUMBER, number.substring(3));  //ȥ��+86
                        cv.put(SORT_KEY, sortKey);  
                    } else {  
                        cv.put(NAME, name);  
                        cv.put(NUMBER, number);  
                        cv.put(SORT_KEY, sortKey);  
                    }  
                    cvlist.add(cv);  
                }  
                if (cvlist.size() > 0) {  
                    setAdapter(cvlist);  
                }  
            }  
        }  
  
    }  
  
    private void setAdapter(List<ContentValues> list) {
    	adapter = new ListAdapter(getActivity(), list);
        personList.setAdapter(adapter);  
        personList.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				ContentValues cv = cvlist.get(position);
				String number = (String) cv.get(NUMBER);
				Intent intent = new Intent(Intent.ACTION_CALL , Uri.parse("tel:" +  number));
				getActivity().startActivity(intent);
				Base.OBDApp.callStat = 2;
			}
        	
        });
    }
    
    private class ListAdapter extends BaseAdapter {
    	 private LayoutInflater inflater;  
         private List<ContentValues> list;
    	
    	public ListAdapter(Context context, List<ContentValues> list) {
    		this.inflater = LayoutInflater.from(context);
    		this.list = list;
    		alphaIndexer = new HashMap<String, Integer>();
    		sections = new String[list.size()];
    		
    		for (int i = 0; i < list.size(); i++) {
    			//��ǰ����ƴ������ĸ
    			String currentStr = getAlpha(list.get(i).getAsString(SORT_KEY));
    			//��һ������ƴ������ĸ�����������Ϊ�� ��
                String previewStr = (i - 1) >= 0 ? getAlpha(list.get(i - 1).getAsString(SORT_KEY)) : " ";
                if (!previewStr.equals(currentStr)) {
                	String name = getAlpha(list.get(i).getAsString(SORT_KEY));
                	alphaIndexer.put(name, i);  
                	sections[i] = name; 
                }
            }
    	}
    	
		@Override
		public int getCount() {
			return list.size();
		}

		@Override
		public Object getItem(int position) {
			return list.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if(!Base.isBaseActive)
				return null;
			if (convertView == null) {  
                convertView = inflater.inflate(R.layout.list_item, null);
                holder = new ViewHolder();  
                holder.alpha_l = (LinearLayout) convertView.findViewById(R.id.alpha_linear);
                holder.alpha = (TextView) convertView.findViewById(R.id.alpha);  
                holder.name = (TextView) convertView.findViewById(R.id.name);  
                holder.number = (TextView) convertView.findViewById(R.id.number);  
                convertView.setTag(holder);  
            } else {  
                holder = (ViewHolder) convertView.getTag();  
            }  
            ContentValues cv = list.get(position);  
            holder.name.setText(cv.getAsString(NAME));
            holder.number.setText(cv.getAsString(NUMBER));
            String currentStr = getAlpha(list.get(position).getAsString(SORT_KEY));
            String previewStr = (position - 1) >= 0 ? getAlpha(list.get(position - 1).getAsString(SORT_KEY)) : " ";
            if(holder.alpha_l != null){
	            if (!previewStr.equals(currentStr)) {  
	                holder.alpha_l.setVisibility(View.VISIBLE);
	                holder.alpha.setText(currentStr);
	            } else {  
	                holder.alpha_l.setVisibility(View.GONE);
	            }  
            }
            return convertView;  
		}
		
		private class ViewHolder {
			LinearLayout alpha_l;
			TextView alpha;  
            TextView name;  
            TextView number;
		}
    	
    }
    
    //��ʼ������ƴ������ĸ������ʾ��
    private void initOverlay() {
    	LayoutInflater inflater = LayoutInflater.from(getActivity());
    	overlay = (TextView) inflater.inflate(R.layout.overlay, null);
    	overlay.setVisibility(View.INVISIBLE);
		WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,
				WindowManager.LayoutParams.TYPE_APPLICATION,
				WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
						| WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
				PixelFormat.TRANSLUCENT);
		WindowManager windowManager = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
		windowManager.addView(overlay, lp);
    }
    
    private class LetterListViewListener implements OnTouchingLetterChangedListener{

		@Override
		public void onTouchingLetterChanged(final String s) {
			if(alphaIndexer.get(s) != null) {
				int position = alphaIndexer.get(s);
				personList.setSelection(position);
				overlay.setText(sections[position]);
				overlay.setVisibility(View.VISIBLE);
				handler.removeCallbacks(overlayThread);
				//�ӳ�һ���ִ�У���overlayΪ���ɼ�
				handler.postDelayed(overlayThread, 1500);
			} 
		}
    	
    }
    
    //����overlay���ɼ�
    private class OverlayThread implements Runnable {

		@Override
		public void run() {
			overlay.setVisibility(View.GONE);
		}
    	
    }
    
    
	//��ú���ƴ������ĸ
    private String getAlpha(String str) {  
        if (str == null) {  
            return "#";  
        }  
  
        if (str.trim().length() == 0) {  
            return "#";  
        }  
  
        char c = str.trim().substring(0, 1).charAt(0);  
        // ������ʽ���ж�����ĸ�Ƿ���Ӣ����ĸ  
        Pattern pattern = Pattern.compile("^[A-Za-z]+$");  
        if (pattern.matcher(c + "").matches()) {  
            return (c + "").toUpperCase();  
        } else {  
            return "#";  
        }  
    }

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
//		Intent intent = new Intent(Intent.ACTION_CALL , Uri.parse("tel:" +  ViewHolder..getText()));
//        startActivity(intent);
	}  
}
