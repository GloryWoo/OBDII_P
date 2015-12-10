package com.ctg.group;

import java.io.Externalizable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.model.LatLng;
import com.ctg.net.HttpQueue;
import com.ctg.ui.Base;
import com.ctg.ui.R;
import com.ctg.util.Preference;
import com.ctg.util.Util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;


public class Member implements Externalizable, Runnable{

	private static final long serialVersionUID = 3647233284813657927L;
	private static final int GET_HEAD_BITMAP_SUCCESS = 0x1000;
	private static final int GET_HEAD_BITMAP_FAIL = 0x2000;
	private static final String MEMBER_ENTITY = "member entity";
	String userpw = null;
	public String name = null;
	//String imageName = null;
	String imagePath = null;
	boolean selected = false;
	String ip = null;
	String extentproperty = null;
	public String nickName;
	public int isOnline = 0;
	public String groupName;
	public boolean isGroupCreator;
	public boolean shield;
	public String headImgName;
	public String localHeadImgName;
	public boolean checked = false;
	public Bitmap headBitmap;
	
	public int shareModeListPos;
	public LatLng latlon;
	public long posTime;
	public boolean isInSharePosMode;
	public Overlay marker;
	public ChatMsgEntity chatMsg;
//	public boolean isChangeState;//if share mode changed 
	//public boolean isSelected;
	public static final int colorCount = 8;
	//							 blue		yellow		  red	      green         orange      purple       indigo    pink
	public static int color[] = {0xff01d4fb, 0xffffc731, 0xfff00029, 0xff0ad100, 0xffffa500, 0xff800080, 0xff4b0082, 0xffffc0cb};
	int a = R.color.orange;
	public static int locmk[] = {R.drawable.icon_geographic_blu, R.drawable.icon_geographic_yellow, 
								 R.drawable.icon_geographic_red, R.drawable.icon_geographic_green};
	private static Queue<Member> taskQue  = new LinkedList<Member>();
	
	private static Thread runner;

	public boolean fenceActive;
	public int fenceRadius;
	@Override
	public void readExternal(ObjectInput input) throws IOException,
			ClassNotFoundException {
		// TODO Auto-generated method stub
		name = (String)input.readObject();
		nickName = (String)input.readObject();
		headImgName = (String)input.readObject();
		localHeadImgName = (String)input.readObject();
		isOnline = input.readInt();		
	}


	@Override
	public void writeExternal(ObjectOutput output) throws IOException {
		// TODO Auto-generated method stub
		 output.writeObject(name);
		 output.writeObject(nickName);
		 output.writeObject(headImgName);
		 output.writeObject(localHeadImgName);
		 output.writeInt(isOnline);
	}
	
	@Override
	public synchronized void run() {
		// TODO Auto-generated method stub
		while(!taskQue.isEmpty()){
			Member curMember = taskQue.poll();
			curMember.getHttpBitmap();
		}
		runner = null;
	}
	

	public synchronized void startHttp() {
		if (runner == null) {
			runner = new Thread(this);
			runner.start();
		}
	}
	
	public void destroy(){
		if(headBitmap != null){
			headBitmap.recycle();
			headBitmap = null;
		}
			
	}
	
	public Member(String userName) //just use to compare
	{
		name = userName;
	}
	
	public Member(String userName, int isOnline, boolean creat) //base
	{
		name = userName;
		this.isOnline = isOnline;
		isGroupCreator = creat;
		chatMsg = new ChatMsgEntity(true);
	}
	
	public Member(String userName, int isOnline, boolean creat, String head) 
	{
		this(userName, isOnline, creat);
		headImgName = head;
		getHeadBitmapLocal();
		if (headImgName != null && (localHeadImgName == null || headImgName.compareTo(localHeadImgName) > 0))
		{			
			//new Thread(this).start();
			taskQue.add(this);
			startHttp();
		}
	}

	public Member(String userName, String nick, int online, String head) 
	{
		this(userName, online, false, head);
		nickName = nick;
	}
	
	public Member(String userName, String nick, int online) 
	{
		this(userName, online, false);
		nickName = nick;
	}
	
	public static int indexOfByName(ArrayList<Member> list, String name){
		int idx = 0;
		
		for(Member member : list){
			if(member.name.equals(name))
				return idx;
			idx++;
		}
		
		return -1;
	}
	
	public int getIsOnline() {
		return isOnline;
	}

	public void setIsOnline(int isOnline) {
		this.isOnline = isOnline;
	}

	public String getPawword() {
		return userpw;
	}

	public void setPassowrd(String pw) {
		this.userpw = pw;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getImagePath() {
		return imagePath;
	}

	public void setImagePath(String imagePath) {
		this.imagePath = imagePath;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	public String getIP() {
		return this.ip;
	}

	public void setIP(String ip) {
		this.ip = ip;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Member) {
			Member t = (Member) obj;
			return (this.getName().equals(t.getName()));
		}
		return super.equals(obj);
	}	
	
	public static Bitmap getHeadBitmapUser(String namestr){
		String path = Base.getSDPath()+"/OBDII/"+namestr;
		File dir = new File(path);
		FileInputStream ins;
		if(dir.isDirectory()){
			File[] fl = dir.listFiles();
			for(File f : fl){
				String fname = f.getName();
				if(fname.length() == 21 && fname.startsWith("20"))//check if head img
				{
					
					try {
						ins = new FileInputStream(f);
						return BitmapFactory.decodeStream(ins);
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}					
					return null;
				}
			}
		}
		return null;
	}
	

	
	public boolean getHeadBitmapLocal(){
		String path = Base.getSDPath()+"/OBDII/"+name;
		File dir = new File(path);
		FileInputStream ins;
		if(dir.isDirectory()){
			File[] fl = dir.listFiles();
			for(File f : fl){
				String fname = f.getName();
				if(fname.length() == 21 && fname.startsWith("20"))//check if head img
				{
					localHeadImgName = fname;
					try {
						ins = new FileInputStream(f);
						headBitmap =  BitmapFactory.decodeStream(ins);
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}					
					return true;
				}
			}
		}
		return false;
	}
	
	public static Handler getHeadBitmapHandler = new Handler(){
		public void handleMessage(Message msg) {
			Member  member = null;
			Bundle bund = msg.getData();
			switch(msg.what){
			case GET_HEAD_BITMAP_SUCCESS:
				member = (Member) bund.get(MEMBER_ENTITY);
				if(Base.me_v.listAdapt != null)
					Base.me_v.listAdapt.notifyDataSetChanged();
				if(Base.baidu_v.isGrpShareMode){
					if(Base.baidu_v.curMember != null && Base.baidu_v.curMember.equals(member)
					|| Base.baidu_v.curGrp != null && Base.baidu_v.curGrp.memberList.contains(member)){
						Base.baidu_v.honAdapter.notifyDataSetChanged();							
						if(Base.me_v.grpDetailDlg != null){
							Base.me_v.grpDetailDlg.adapter.notifyDataSetChanged();	
						}
					}						
					
				}
				if(Base.loginUser.equals(member.name) && Base.headbitmap==null){
					Base.headbitmap = member.headBitmap;					
					Base.myBitmap = Util.getRoundedCornerImageColorTriangle(Base.headbitmap, 50*Base.mDensityInt, 50*Base.mDensityInt, 0xff01d4fb);	
				}
				break;
			}
		}
	};
	
	public boolean getHttpBitmap() {
		URL myFileUrl = null;

		FileOutputStream out;
		byte[] buf = new byte[1024];
		int rLen, wLen;

		String path = Base.getSDPath() + "/OBDII/" + name;
		File f = new File(path);
		if (!f.isDirectory()) {
			f.mkdir();
		}


		String url = Base.NEW_HTTP_ROOT_PATH + "/account/image/" + headImgName;
		String sessionid = Preference.getInstance(Base.OBDApp.getApplicationContext()).getSessionId();
		try {
			myFileUrl = new URL(url);
			HttpURLConnection conn = (HttpURLConnection) myFileUrl
					.openConnection();
			conn.setConnectTimeout(1000);
			conn.setReadTimeout(5000);
			conn.setRequestProperty("X-token", sessionid);
			conn.connect();
			
			InputStream is = conn.getInputStream();
			out = new FileOutputStream(new File(path, headImgName));
			while ((rLen = is.read(buf, 0, 1024)) > 0) {
				out.write(buf, 0, rLen);
			}
			is.close();
			out.close();
			headBitmap = BitmapFactory.decodeFile(path+"/"+headImgName);
			if(localHeadImgName != null){
				String cur_path = Base.getSDPath()+"/OBDII/"+name;
				File oldFile = new File(cur_path, localHeadImgName);
				oldFile.delete();
			}
				
			localHeadImgName = headImgName;
			if(getHeadBitmapHandler != null){
				Bundle bund = new Bundle();
				bund.putSerializable(MEMBER_ENTITY, this);
				Message msg = new Message();
				msg.what = GET_HEAD_BITMAP_SUCCESS;
				msg.setData(bund);
				getHeadBitmapHandler.sendMessage(msg);
			}
			// bitmap.setDensity(DisplayMetrics.DENSITY_HIGH);

		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}





//	@Override
//	public void run() {
//		// TODO Auto-generated method stub
//		getHttpBitmap();
//	}
		
}
