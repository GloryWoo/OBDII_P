package com.ctg.land;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.ctg.group.Group;
import com.ctg.group.Member;
import com.ctg.ui.R;
import com.ctg.util.GrpDelDlg.GrpLstSelAdapter.ViewHolder;

   public class GrpMapExamAdapter extends BaseAdapter {  
        // 填充数据的list  
    private HashMap<String, Integer> mHash;  
    // 上下文  
    private Context context;  
    // 用来导入布局  
    private LayoutInflater inflater = null;  
  
    class ColorItem{
    	String username;
    	int color;
    	
    	public ColorItem(String key, int value) {
			// TODO Auto-generated constructor stub
    		username = key;
    		color = value;
		}
    }
    // 构造器  
    public GrpMapExamAdapter(HashMap<String, Integer> hash, Context context) {  
        this.context = context;  
        this.mHash = hash;  
        inflater = LayoutInflater.from(context);   
    }    
  
    
    
    @Override  
    public int getCount() {  
        return mHash.size();  
    }  
  
    @Override  
    public Object getItem(int position) {  
    	int i = 0;
    	for(Entry<String, Integer> entry :  mHash.entrySet()){
    		if(i == position)
    			return new ColorItem(entry.getKey(), entry.getValue());
    	}
        return null;  
    }  
  
    @Override  
    public long getItemId(int position) {  
        return position;  
    }  
  
    @Override  
    public View getView(int position, View convertView, ViewGroup parent) {  
        ViewHolder holder = null;  
        if (convertView == null) {  
            // 获得ViewHolder对象  
            holder = new ViewHolder();  
            // 导入布局并赋值给convertview  
            convertView = inflater.inflate(R.layout.grp_mem_dot, null);  
            holder.tv = (TextView) convertView.findViewById(R.id.grp_mem_dot_name);  
            holder.img = (ImageView) convertView.findViewById(R.id.grp_mem_dot_img);  
            // 为view设置标签  
            convertView.setTag(holder);  
        } else {  
            // 取出holder  
            holder = (ViewHolder) convertView.getTag();  
        }  
        ColorItem item = (ColorItem) getItem(position);
        // 设置list中TextView的显示  
        if(item != null){
        	holder.tv.setText(item.username);
        	holder.img.setBackgroundColor(item.color);
        }
        // 根据isSelected来设置checkbox的选中状况  
        
        return convertView;  
    }  
  
    public class ViewHolder {  
        TextView tv;  
        ImageView img;  
    }  
}