package com.ctg.trace;

import android.content.Context;
import android.database.Cursor;

import com.baidu.mapapi.model.LatLng;
import com.ctg.util.Preference;

import java.io.File;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by lee on 12/19/2014.
 */
public class TraceListInstance {

    static private DownloadTrace downloadTrace=null;
    static private ImportTraceToDatabase importTraceToDatabase=null;
    static private TraceDatabase traceDatabase=null;
    static private long curRecTraceId=0;
    static private float curAvgSpeed=0;
    static private int curAvgRot=0;
    static private float curAvgTemp=0;
    static private int curPtNum=0;
    static private int curDis=0;
    static private GPS prevGPS=null;
    static private boolean bFirstPos=true;
    private final double EARTH_RADIUS = 6378137.0;

    private static class SingletonTraceListInstance {
        private static final TraceListInstance instance = new TraceListInstance();
    }

    public static TraceListInstance getInstance() {
        return SingletonTraceListInstance.instance;
    }

    private TraceListInstance() {
        prevGPS = new GPS();
        downloadTrace = new DownloadTrace();
        importTraceToDatabase = new ImportTraceToDatabase();
        traceDatabase = new TraceDatabase();
        traceDatabase.open();
    }

    public void downloadTrace(Context cxt, Date updateTime, boolean trackable){
        Date latestUpdateTime = traceDatabase.getDbLatestUpdate();
//    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

//			try {
//				latestUpdateTime = sdf.parse("2016-01-01 00:00:00");
//			} catch (ParseException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
		downloadTrace.Download(cxt, latestUpdateTime, updateTime, trackable);
    }

    public void saveTraceToDB(){
        importTraceToDatabase.importTrace(downloadTrace.traceList, downloadTrace.pointList, downloadTrace.eventList);
    }

    public List<StatisticsData> getTraceList(Context cxt){
        List<StatisticsData> traceList = new ArrayList<StatisticsData>();

        Cursor cur = traceDatabase.getAllTrace();
        if (cur.getCount() == 0)
            return traceList;

        int id = cur.getColumnIndex(TraceDatabase.KEY_TRACE_ID);
        int start = cur.getColumnIndex(TraceDatabase.TRACE_START);
        int end = cur.getColumnIndex(TraceDatabase.TRACE_END);
        int speed = cur.getColumnIndex(TraceDatabase.TRACE_SPEED);
        int rot = cur.getColumnIndex(TraceDatabase.TRACE_ROT);
        int temp = cur.getColumnIndex(TraceDatabase.TRACE_TEMP);
        int dis = cur.getColumnIndex(TraceDatabase.TRACE_DIS);
        int loc = cur.getColumnIndex(TraceDatabase.TRACE_LOC);
        int idle = cur.getColumnIndex(TraceDatabase.TRACE_IDLE);
        int nyte = cur.getColumnIndex(TraceDatabase.TRACE_NYTE);
        int spdy = cur.getColumnIndex(TraceDatabase.TRACE_SPDY);
        int pk = cur.getColumnIndex(TraceDatabase.TRACE_PK);
        int ti60 = cur.getColumnIndex(TraceDatabase.TRACE_60);
        int ti90 = cur.getColumnIndex(TraceDatabase.TRACE_90);
        int ti120 = cur.getColumnIndex(TraceDatabase.TRACE_120);
        int ti120p = cur.getColumnIndex(TraceDatabase.TRACE_120P);
        int totalFuel = cur.getColumnIndex(TraceDatabase.TRACE_TOTAL_FUEL);
        int idleFuel = cur.getColumnIndex(TraceDatabase.TRACE_IDLE_FUEL);
        int avgMPG = cur.getColumnIndex(TraceDatabase.TRACE_AVG_MPG);
        int bat1 = cur.getColumnIndex(TraceDatabase.TRACE_BAT_VOL1);
        int bat2 = cur.getColumnIndex(TraceDatabase.TRACE_BAT_VOL2);
        
        for(cur.moveToFirst();!cur.isAfterLast();cur.moveToNext()) {

            StatisticsData statisticsData = new StatisticsData();
            statisticsData.setSid(cur.getLong(id));

            statisticsData.setAverageSpeed(cur.getInt(speed));
            statisticsData.setAverageRotate(cur.getInt(rot));
            statisticsData.setAverageTemp(cur.getInt(temp));

            Date date = DateUtil.DateStrToDate(cur.getString(start));
            statisticsData.setStartTime(new Timestamp(date.getTime()));
            date = DateUtil.DateStrToDate(cur.getString(end));
            statisticsData.setEndTime(new Timestamp(date.getTime()));
            
            
            statisticsData.setDistance(cur.getInt(dis));
            statisticsData.setTraceLocation(cur.getString(loc));
            statisticsData.setIdleTime(cur.getLong(idle));
            statisticsData.setNightTime(cur.getLong(nyte));
            statisticsData.setSpeedingTime(cur.getLong(spdy));;
            statisticsData.setRushTime(cur.getLong(pk));
            statisticsData.set0to60(cur.getLong(ti60));
            statisticsData.set60to90(cur.getLong(ti90));
            statisticsData.set90to120(cur.getLong(ti120));
            statisticsData.set120above(cur.getLong(ti120p));
            statisticsData.setTotal_fuel(cur.getFloat(totalFuel));
            statisticsData.setIdle_fuel(cur.getFloat(idleFuel));
            statisticsData.setAverage_mpg(cur.getFloat(avgMPG));
            statisticsData.setBattery_volt_1(cur.getFloat(bat1));
            statisticsData.setBattery_volt_2(cur.getFloat(bat2));

            traceList.add(statisticsData);
        }
        return traceList;
    }

    public List<GPS> getTracePtList(long id){
        List<GPS> ptList = new ArrayList<GPS>();
        Cursor cur = traceDatabase.getPointById(id);
        if (cur.getCount() ==0 )
            return ptList;

        int iLat = cur.getColumnIndex(TraceDatabase.PT_LAT);
        int iLon = cur.getColumnIndex(TraceDatabase.PT_LON);
        int iTime = cur.getColumnIndex(TraceDatabase.PT_TIME);
//        int iSpeed = cur.getColumnIndex(TraceDatabase.PT_SPEED);
//        int iRot = cur.getColumnIndex(TraceDatabase.PT_ROT);
//        int iTemp = cur.getColumnIndex(TraceDatabase.PT_TEMP);
//        int iAcc = cur.getColumnIndex(TraceDatabase.PT_ACC);


        for(cur.moveToFirst();!cur.isAfterLast();cur.moveToNext()) {

            GPS gps = new GPS();
            gps.setLat(cur.getDouble(iLat));
            gps.setLon(cur.getDouble(iLon));

//            gps.setSpeed(cur.getFloat(iSpeed));
//            gps.setRotate(cur.getFloat(iRot));
//            gps.setTemperature(cur.getFloat(iTemp));
//            gps.setAccelerateSpeed(cur.getFloat(iAcc));

            Date date = DateUtil.DateStrToDate(cur.getString(iTime));
            gps.setCreatetime(new Timestamp(date.getTime()));

            ptList.add(gps);
        }
        return ptList;
    }
    
    public List<DrivingEvent> getTraceEventList(long id, int type){
    	List<DrivingEvent> eList = new ArrayList<DrivingEvent>();
    	
    	Cursor cur = traceDatabase.getEventsById(id, type);
        if (cur.getCount() ==0 )
            return eList;
        
        int iType = cur.getColumnIndex(TraceDatabase.EVENT_TYPE);
        int iStart = cur.getColumnIndex(TraceDatabase.EVENT_START);
        int iEnd = cur.getColumnIndex(TraceDatabase.EVENT_END);
        int iLat = cur.getColumnIndex(TraceDatabase.EVENT_LOC_LAT);
        int iLon = cur.getColumnIndex(TraceDatabase.EVENT_LOC_LON);
        
        for(cur.moveToFirst();!cur.isAfterLast();cur.moveToNext()){
        	DrivingEvent de = new DrivingEvent();
        	
        	de.setTripid(Long.toString(id));
        	de.setM_loc_lat(cur.getDouble(iLat));
        	de.setM_loc_lon(cur.getDouble(iLon));
        	
        	eList.add(de);
        }
        
        return eList;
        
    }

    public void addDownloadListener(DownloadDelegate listener){
        downloadTrace.downloadDelegate = listener;
    }

    public void addImportListener(ImportToDBDelegate listener){
        importTraceToDatabase.importDelegate = listener;
    }

    public void addNewTrace(Date date){
        curRecTraceId = date.getTime()/1000;
        // no need to save trace data in DB!
        // traceDatabase.updateDbInfo();
        // traceDatabase.insertTrace(curRecTraceId, date, date, 0, 0, 0, 0, "test location", 0l, 0l, 0l, 0l, 0l, 0l, 0l, 0l);
        // bFirstPos=true;
    }

    public void addNewPtToTrace(GPS gps){
        traceDatabase.insertPoint(curRecTraceId, gps.getCreatetime(), gps.getLat(), gps.getLon(), gps.getSpeed(), gps.getRotate(), gps.getTemperature(), gps.getAccelerateSpeed());
        curPtNum++;
    }

    public long getCurRecTraceId(){
        return curRecTraceId;
    }
    
    public void setCurRecTraceId(long tid){
    	curRecTraceId = tid;
    }

    public void updateTraceInfo(GPS gps){
        curAvgSpeed += (gps.getSpeed()-curAvgSpeed)/curPtNum;
        curAvgRot += (gps.getRotate()-curAvgRot)/curPtNum;
        curAvgTemp += (gps.getTemperature()-curAvgTemp)/curPtNum;
        if (!bFirstPos){
            curDis += (int)getDis(gps, prevGPS);
        }
        bFirstPos=false;
        traceDatabase.updateTrace(curRecTraceId, gps.getCreatetime(), curAvgSpeed, curAvgRot, curAvgTemp, curDis);
        prevGPS = gps;
    }
    
    public void updateTraceInfo(long tid, String loc){
    	traceDatabase.updateTrace(tid, loc);
    }

    public void changeDatabase(){
        traceDatabase.close();
        traceDatabase.deleteDb();
        traceDatabase.open();
    }

    private double getDis(GPS A, GPS B){
        double radLat1 = (A.getLat() * Math.PI / 180.0);
        double radLat2 = (B.getLat() * Math.PI / 180.0);
        double a = radLat1 - radLat2;
        double b = (A.getLon() - B.getLon()) * Math.PI / 180.0;
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2)
                + Math.cos(radLat1) * Math.cos(radLat2)
                * Math.pow(Math.sin(b / 2), 2)));
        s = s * EARTH_RADIUS;
        s = Math.round(s * 10000) / 10000;
        return s;
    }




}
