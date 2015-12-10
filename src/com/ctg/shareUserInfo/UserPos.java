package com.ctg.shareUserInfo;

import java.util.Iterator;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.CoordinateConverter;
import com.ctg.trace.DateUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by lee on 15/1/6.
 */
public class UserPos {
	public double lat;
	public double lon;
	public String name;
	public String groupName;
    public long time;
    public int count;
    public String action;
    
    public UserPos(JSONObject json) {
        try {
        	action = json.getString("action");
            setGroupName(json.getString("groupName"));
            setName(json.getString("fromUser"));
            count = json.getInt("count");
            JSONObject obj_enti = json.getJSONObject("messages");
            setLat(obj_enti.getDouble("lat"));
            setLon(obj_enti.getDouble("lon"));            
            ConvertPtToBaidu();
        }catch (JSONException e){
            e.printStackTrace();
        }

    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void ConvertPtToBaidu(){
        LatLng source = new LatLng(lat, lon);
        CoordinateConverter converter  = new CoordinateConverter();
        converter.from(CoordinateConverter.CoordType.GPS);
        converter.coord(source);
        lat = converter.convert().latitude;
        lon = converter.convert().longitude;
    }
}
