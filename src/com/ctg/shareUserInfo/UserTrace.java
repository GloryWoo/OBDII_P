package com.ctg.shareUserInfo;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.CoordinateConverter;
import com.ctg.trace.DateUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by lee on 15/1/22.
 */
public class UserTrace {

    private String fromUser;
    private String groupName;
    private Date startTime;
    private Date endTime;
    private float  averageSpeed;
    private int averageRotate;
    private float averageTemp;
    private int distance;
    private List<LatLng> lsPos = new ArrayList<LatLng>();

    public UserTrace(JSONObject json) {
        try {
            setGroupName(json.getString("groupName"));
            setFromUser(json.getString("fromUser"));
            JSONObject obj_enti = json.getJSONObject("messages");
            setStartTime(DateUtil.DateStrToDate(obj_enti.getString("startTime")));
            setEndTime(DateUtil.DateStrToDate(obj_enti.getString("endTime")));
            setAverageSpeed((float)obj_enti.getDouble("aveSpeed"));
            setAverageRotate(obj_enti.getInt("aveRotate"));
            setAverageTemp((float)obj_enti.getDouble("aveTemp"));
            setDistance(obj_enti.getInt("distance"));
            JSONArray jsonArray = obj_enti.getJSONArray("position");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONArray jo = jsonArray.getJSONArray(i);
                addLsPos(jo.getDouble(0), jo.getDouble(1));
            }
        }catch (JSONException e){
            e.printStackTrace();
        }

    }

    public List<LatLng> getLsPos() {
        return lsPos;
    }

    public void addLsPos(double lat, double lon) {
        this.lsPos.add(ConvertPtToBaidu(lat, lon));
    }

    public String getFromUser() {
        return fromUser;
    }

    public void setFromUser(String fromUser) {
        this.fromUser = fromUser;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public float getAverageSpeed() {
        return averageSpeed;
    }

    public void setAverageSpeed(float averageSpeed) {
        this.averageSpeed = averageSpeed;
    }

    public int getAverageRotate() {
        return averageRotate;
    }

    public void setAverageRotate(int averageRotate) {
        this.averageRotate = averageRotate;
    }

    public float getAverageTemp() {
        return averageTemp;
    }

    public void setAverageTemp(float averageTemp) {
        this.averageTemp = averageTemp;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    private LatLng ConvertPtToBaidu(double lat, double lon){
        LatLng source = new LatLng(lat, lon);
        CoordinateConverter converter  = new CoordinateConverter();
        converter.from(CoordinateConverter.CoordType.GPS);
        converter.coord(source);
        LatLng target = new LatLng(converter.convert().latitude, converter.convert().longitude);
        return target;
    }
}
