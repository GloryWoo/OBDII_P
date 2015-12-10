package com.ctg.util;

import java.util.ArrayList;

import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.model.LatLng;
import com.ctg.group.Group;
import com.ctg.group.Member;
import com.ctg.net.HttpQueue;
import com.ctg.ui.Base;
import com.ctg.ui.R;  

import android.content.Context;  
import android.graphics.Bitmap;  
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;  
import android.media.ThumbnailUtils;  
import android.view.LayoutInflater;  
import android.view.View;  
import android.view.View.OnClickListener;
import android.view.ViewGroup;  
import android.widget.BaseAdapter;  
import android.widget.ImageView;  
import android.widget.TextView;  
  
public class HorizontalListViewAdapter extends BaseAdapter{  
//    private int[] mIconIDs;  
//    private String[] mTitles;  
	public Base baseAct;
	public ArrayList<Member> gMemberList; 
    private LayoutInflater mInflater;  
    Bitmap iconBitmap;  
    private int selectIndex = -1;  
	int friendLen;
	int grpLen;
	Group grp;
    public HorizontalListViewAdapter(Context context){  
    	baseAct = (Base)context;  
//        this.mIconIDs = ids;  
//        this.mTitles = titles;  
    	gMemberList = new ArrayList<Member>();
		friendLen = HttpQueue.friendLst.size();
		grpLen = HttpQueue.grpResLst.size();
        mInflater=(LayoutInflater)baseAct.getLayoutInflater();//LayoutInflater.from(baseAct);  
		if(Base.friendOrGrpIdx < friendLen){
			gMemberList.add(HttpQueue.friendLst.get(Base.friendOrGrpIdx));
		}
		else{
			grp = HttpQueue.grpResLst.get(Base.friendOrGrpIdx-friendLen);
			gMemberList.addAll(grp.memberList);
		}
    }  
    
    public void refreshGrpList(){
		grp = HttpQueue.grpResLst.get(Base.friendOrGrpIdx-HttpQueue.friendLst.size());
		gMemberList.clear();
		gMemberList.addAll(grp.memberList);
		this.notifyDataSetChanged();
    }
    @Override  
    public int getCount() {  
        return gMemberList.size(); 
    }  
    @Override  
    public Object getItem(int position) {  
        return position;  
    }  
  
    @Override  
    public long getItemId(int position) {  
        return position;  
    }  
  
    @Override  
    public View getView(int position, View convertView, ViewGroup parent) {  
  
        ViewHolder holder; 
        Bitmap bitProc = null;
        Member member = gMemberList.get(position);
        if(convertView==null){  
            holder = new ViewHolder();  
            convertView = mInflater.inflate(R.layout.image_view, null);  
            holder.mImage=(ImageView)convertView.findViewById(R.id.headimg);  
//            holder.mTitle=(TextView)convertView.findViewById(R.id.text_list_item);  
            convertView.setTag(holder);                      
        }else{  
            holder=(ViewHolder)convertView.getTag();  
        }  
		if(member.name.equals(Base.loginUser) && Base.headbitmap != null){
			bitProc = Util.getRoundedCornerImageColor(Base.headbitmap, Member.color[position%Member.colorCount]);
			holder.mImage.setImageBitmap(bitProc);
		}
		else if (member.headBitmap != null) {			
			if(member.isInSharePosMode)
				bitProc = Util.getRoundedCornerImageColor(member.headBitmap, Member.color[position%Member.colorCount]);
			else
				bitProc = Util.getRoundedCornerGrayImage(member.headBitmap);//Util.getRoundedCornerImageColor(member.headBitmap, Member.color[position%Member.colorCount]);
		
			holder.mImage.setImageBitmap(bitProc);
		}
		else {
			Bitmap bmp = BitmapFactory.decodeResource(baseAct.getResources(), R.drawable.ic_launcher_df);
			if(member.isInSharePosMode)
				bitProc = Util.getRoundedCornerImageColor(bmp, Member.color[position%Member.colorCount]);
			else
				bitProc = Util.getRoundedCornerGrayImage(bmp);//Util.getRoundedCornerImageColor(member.headBitmap, Member.color[position%Member.colorCount]);
			holder.mImage.setImageBitmap(bitProc);						
		}    
		holder.mImage.setTag(member);    
		holder.mImage.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Member myMember = (Member) v.getTag();
				if(myMember.isInSharePosMode || myMember.name.equals(Base.loginUser)){
					if(myMember.name.equals(Base.loginUser)){
						myMember.latlon = new LatLng(Base.baidu_v.mCurLatitude, Base.baidu_v.mCurLongitude);
					}
					Base.baidu_v.mBaiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(myMember.latlon));
				}
				if(Base.friendOrGrpIdx <  HttpQueue.friendLst.size()){
					Base.baidu_v.curGrp = null;
				}
				else{
					Base.baidu_v.curGrp = HttpQueue.grpResLst.get(Base.friendOrGrpIdx-friendLen);
				}
				Base.baidu_v.curMember = myMember;
				if(!myMember.name.equals(Base.loginUser))
					Base.baidu_v.pickupChoiceCheck();
				
			}
			
		});
        member.shareModeListPos = position;
	
		return convertView;
    }  
  
    private static class ViewHolder {  
        private TextView mTitle ;  
        private ImageView mImage;  
    }  
    private Bitmap getPropThumnail(int id){  
        Drawable d = baseAct.getResources().getDrawable(id);  
        Bitmap b = BitmapUtil.drawableToBitmap(d);  
//      Bitmap bb = BitmapUtil.getRoundedCornerBitmap(b, 100);  
        int w = baseAct.getResources().getDimensionPixelOffset(R.dimen.thumnail_default_width);  
        int h = baseAct.getResources().getDimensionPixelSize(R.dimen.thumnail_default_height);  
          
        Bitmap thumBitmap = ThumbnailUtils.extractThumbnail(b, w, h);  
          
        return thumBitmap;  
    }  
    public void setSelectIndex(int i){  
        selectIndex = i;  
    }  
}