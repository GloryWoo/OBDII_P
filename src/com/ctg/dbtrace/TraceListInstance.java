package com.ctg.dbtrace;

import android.content.Context;
import android.database.Cursor;

import com.ctg.util.Preference;

import java.io.File;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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
    static private DBTraceDatabase traceDatabase=null;
    static private long curRecTraceId=0;
    static private float curAvgSpeed=0;
    static private int curAvgRot=0;
    static private float curAvgTemp=0;
    static private int curPtNum=0;
    static private int curDis=0;
    static private GPS prevGPS=null;
    static private boolean bFirstPos=true;
    private final double EARTH_RADIUS = 6378137.0;
    
    public static int traceCnt;
    public static int evtCnt;
    

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
        traceDatabase = new DBTraceDatabase();
        traceDatabase.open();
        traceCnt = traceDatabase.getCountOfTrace();
        evtCnt = traceDatabase.getCountOfEvent();
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
        importTraceToDatabase.importTrace(downloadTrace.traceList, downloadTrace.eventList);
    }

    public StatisticsData getCurTrace(){
    	StatisticsData trace = new StatisticsData();
    	
    	Cursor cur = traceDatabase.getRecentTrace();
        if (cur.getCount() == 0)
            return trace;
        
        cur.moveToFirst();
        
        int id = cur.getColumnIndex(DBTraceDatabase.KEY_TRACE_ID);
        int start = cur.getColumnIndex(DBTraceDatabase.TRACE_START);
        int end = cur.getColumnIndex(DBTraceDatabase.TRACE_END);
        int speed = cur.getColumnIndex(DBTraceDatabase.TRACE_SPEED);
        int dis = cur.getColumnIndex(DBTraceDatabase.TRACE_DIS);
        int stLat = cur.getColumnIndex(DBTraceDatabase.TRACE_ST_LAT);
        int stLon = cur.getColumnIndex(DBTraceDatabase.TRACE_ST_LON);
        int enLat = cur.getColumnIndex(DBTraceDatabase.TRACE_EN_LAT);
        int enLon = cur.getColumnIndex(DBTraceDatabase.TRACE_EN_LON);
        int score = cur.getColumnIndex(DBTraceDatabase.TRACE_PER_SCORE);
        int scoreAvr = cur.getColumnIndex(DBTraceDatabase.TRACE_AVER_SCORE);
        
        if(cur.getString(id) == null)
        	return trace;

		trace.setTraceId(DateUtil.StringToLong(cur.getString(id)));


        trace.setAverageSpeed(cur.getInt(speed));


        Date date = DateUtil.DateStrToDate(cur.getString(start));
        trace.setStartTime(new Timestamp(date.getTime()));
        date = DateUtil.DateStrToDate(cur.getString(end));
        trace.setEndTime(new Timestamp(date.getTime()));
        trace.setDistance(cur.getInt(dis));
        
        trace.startLat = cur.getDouble(stLat);
        trace.startLon = cur.getDouble(stLon);
        trace.endLat = cur.getDouble(enLat);
        trace.endLon = cur.getDouble(enLon); 
        trace.scoreTheTrip = cur.getInt(score);
        trace.scoreAllAver = cur.getInt(scoreAvr);              
    	return trace;
    }
    
    public List<StatisticsData> getTraceList(Context cxt){
        List<StatisticsData> traceList = new ArrayList<StatisticsData>();

        Cursor cur = traceDatabase.getAllTrace();
        if (cur.getCount() == 0)
            return traceList;

        int id = cur.getColumnIndex(DBTraceDatabase.KEY_TRACE_ID);
        int start = cur.getColumnIndex(DBTraceDatabase.TRACE_START);
        int end = cur.getColumnIndex(DBTraceDatabase.TRACE_END);
        int speed = cur.getColumnIndex(DBTraceDatabase.TRACE_SPEED);
        int dis = cur.getColumnIndex(DBTraceDatabase.TRACE_DIS);
        int stLat = cur.getColumnIndex(DBTraceDatabase.TRACE_ST_LAT);
        int stLon = cur.getColumnIndex(DBTraceDatabase.TRACE_ST_LON);
        int enLat = cur.getColumnIndex(DBTraceDatabase.TRACE_EN_LAT);
        int enLon = cur.getColumnIndex(DBTraceDatabase.TRACE_EN_LON);
        int score = cur.getColumnIndex(DBTraceDatabase.TRACE_PER_SCORE);
        int scoreAvr = cur.getColumnIndex(DBTraceDatabase.TRACE_AVER_SCORE);        
        for(cur.moveToFirst();!cur.isAfterLast();cur.moveToNext()) {

            StatisticsData statisticsData = new StatisticsData();
            //statisticsData.setTraceId(cur.getLong(id));
           
            statisticsData.setTraceId(DateUtil.StringToLong(cur.getString(id)));
    		
            statisticsData.setAverageSpeed(cur.getInt(speed));


            Date date = DateUtil.DateStrToDate(cur.getString(start));
            statisticsData.setStartTime(new Timestamp(date.getTime()));
            date = DateUtil.DateStrToDate(cur.getString(end));
            statisticsData.setEndTime(new Timestamp(date.getTime()));
            statisticsData.setDistance(cur.getInt(dis));
            
            statisticsData.startLat = cur.getDouble(stLat);
            statisticsData.startLon = cur.getDouble(stLon);
            statisticsData.endLat = cur.getDouble(enLat);
            statisticsData.endLon = cur.getDouble(enLon); 
            statisticsData.scoreTheTrip = cur.getInt(score);
            statisticsData.scoreAllAver = cur.getInt(scoreAvr);              
            traceList.add(statisticsData);
        }
        return traceList;
    }

    
    public ArrayList<DrivingEvent> getTraceEventList(long id, int type){
    	ArrayList<DrivingEvent> eList = new ArrayList<DrivingEvent>();
    	
    	Cursor cur = traceDatabase.getEventsById(id, type);
        if (cur.getCount() ==0 )
            return eList;
        
        int iType = cur.getColumnIndex(DBTraceDatabase.EVENT_TYPE);
        int iStart = cur.getColumnIndex(DBTraceDatabase.EVENT_START);
        int iEnd = cur.getColumnIndex(DBTraceDatabase.EVENT_END);
        int iLat = cur.getColumnIndex(DBTraceDatabase.EVENT_LOC_LAT);
        int iLon = cur.getColumnIndex(DBTraceDatabase.EVENT_LOC_LON);
        
        for(cur.moveToFirst();!cur.isAfterLast();cur.moveToNext()){
        	DrivingEvent de = new DrivingEvent();
        	
        	de.setTripid(id);
        	de.setM_loc_lat(cur.getDouble(iLat));
        	de.setM_loc_lon(cur.getDouble(iLon));
        	
        	eList.add(de);
        }
        
        return eList;
        
    }

    public int[] getScoreWeight(){
    	int[] weights = new int[4];
    	int cnt0, cnt1, cnt2;
    	cnt0 = traceDatabase.getScoreWeight(100, 90);
    	weights[0] = cnt0*100/traceCnt;
    	cnt1 = traceDatabase.getScoreWeight(89, 80);
    	weights[1] = cnt1*100/traceCnt;
    	cnt2 = traceDatabase.getScoreWeight(79, 60);
    	weights[2] = cnt2*100/traceCnt;

    	weights[3] = 100-weights[0]-weights[1]-weights[2];
    	return weights;
    }
    
    public int[] getMonthLayout(Date dt){
    	int year = dt.getYear();
    	int month = dt.getMonth();
    	int layout[] = new int[month+1];
    	for(int i = 0; i < month+1; i++){
    		layout[i] = traceDatabase.getMonthScore(year, i);
    	}
    	//date.getMonth()
    	return layout;
    }
    
    public int[] getDateLayout(Date dt){
    	int year = dt.getYear();
    	int month = dt.getMonth();
    	int date = dt.getDate();
    	int layout[] = new int[date];
    	for(int i = 1; i < date+1; i++){
    		layout[i-1] = traceDatabase.getDateScore(year, month, i);
    	}
    	//date.getMonth()
    	return layout;
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
