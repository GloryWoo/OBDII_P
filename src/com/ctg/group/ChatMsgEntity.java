
package com.ctg.group;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.baidu.mapapi.model.LatLng;

public class ChatMsgEntity implements Serializable{

	public static int CHAT_MSG_TEXT = 100;
	public static int CHAT_MSG_LOCATE = 101;
	public static int CHAT_MSG_TRACK = 102;
	
	public static int CHAT_MSG_TO_USER = 201;
	public static int CHAT_MSG_TO_GROUP = 202;
	
	public String name;

    public String date;

    public String text;

    public String groupName;
    public String toUserName;
    
    public int msgType;
        
    public int toType;
    public int lOrR;
    
    public LatLng latlon_loc;
    public boolean isFirst;
    public ArrayList<LatLng> latlon_track;
    public ArrayList<String> usrsList;
    public int msgCount;
    public int loadState;//0 needn't load, 1 not load 2 loaded
    public boolean isProcessed;
    
    public ChatMsgEntity(String name, String groupName,
    		String date, String text, int lOrR, int msgType) {
//        super();
        this.name = name;
        this.date = date;
        this.text = text;
        this.msgType = msgType;
        this.lOrR = lOrR;
        this.groupName = groupName;
        toType = CHAT_MSG_TO_GROUP;
    }
    
    public boolean equals(Object obj) {
		if (obj instanceof ChatMsgEntity) {
			ChatMsgEntity chat = (ChatMsgEntity) obj;
			return (this.name.equals(chat.name));
		}
		return super.equals(obj);
    	
    }
    
	public ChatMsgEntity(String name, String groupName, String date,
			String text, int lOrR, int msgType, LatLng latlon) {
		// super();
		this(name, groupName, date, text, lOrR, msgType);
		latlon_loc = latlon;
		toType = CHAT_MSG_TO_GROUP;
	}

	public ChatMsgEntity(String name, ArrayList<String> lst, String date,
			String text, int lOrR, int msgType, LatLng latlon) {
		// super();
		this(name, "", date, text, lOrR, msgType);
		usrsList = lst;
		latlon_loc = latlon;
		toType = CHAT_MSG_TO_GROUP;
	}
	
	public ChatMsgEntity(String name, String groupName, String date,
			String text, int lOrR, int msgType, ArrayList<LatLng> latlon_lst) {
		// super();
		this(name, groupName,date, text, lOrR, msgType);
		latlon_track = latlon_lst;
		toType = CHAT_MSG_TO_GROUP;
	}

	public ChatMsgEntity(String name, String groupName, String date,
			String text, int lOrR, int msgType, LatLng latlon, int toType) {
		// super();
		this(name, groupName, date, text, lOrR, msgType);
		latlon_loc = latlon;
		this.toType = toType;
	}

	public ChatMsgEntity(String name, String groupName, String date,
			String text, int lOrR, int msgType, ArrayList<LatLng> latlon_lst,
			int toType) {
		// super();
		this(name, groupName, date, text, lOrR, msgType);
		latlon_track = latlon_lst;
		this.toType = toType;
	}

	public ChatMsgEntity() {
		// TODO Auto-generated constructor stub		
	}
	public ChatMsgEntity(boolean creatTrackList) {
		// TODO Auto-generated constructor stub	
		if(creatTrackList)
			latlon_track = new ArrayList<LatLng>();
	}
}
