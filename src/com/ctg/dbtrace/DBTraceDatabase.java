package com.ctg.dbtrace;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;



import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by lee on 15/1/8.
 */
public class DBTraceDatabase {

    static final String KEY_DB_VER = "db_version";
    static final String DB_TIME = "db_time";

    static final String KEY_TRACE_ID = "trace_id";
    static final String TRACE_START = "trace_startTime";
    static final String TRACE_END = "trace_endTime";
    static final String TRACE_SPEED = "trace_avgSpeed";
//    static final String TRACE_ROT = "trace_avgRot";
//    static final String TRACE_TEMP = "trace_avgTemp";
    static final String TRACE_DIS = "trace_distance";
    static final String TRACE_LOC = "trace_location";
    static final String TRACE_ST_LAT = "trip_start_lat";
    static final String TRACE_ST_LON = "trip_start_lon";
    static final String TRACE_EN_LAT = "trip_end_lat";
    static final String TRACE_EN_LON = "trip_end_lon"; 
    static final String TRACE_PER_SCORE = "safe_score";
    static final String TRACE_AVER_SCORE = "safe_score_total"; 
    
//    static final String TRACE_IDLE = "idle_time";
//    static final String TRACE_NYTE = "night_time";
//    static final String TRACE_SPDY = "speedy_time";
//    static final String TRACE_PK = "peak_time";
//    static final String TRACE_60 = "time_60";
//    static final String TRACE_90 = "time_60_90";
//    static final String TRACE_120 = "time_90_120";
//    static final String TRACE_120P = "time_120";
//    static final String TRACE_TOTAL_FUEL = "total_fuel";
//    static final String TRACE_IDLE_FUEL = "idle_fuel";
//    static final String TRACE_AVG_MPG = "average_mpg";
//    static final String TRACE_BAT_VOL1 = "battery_volt1";
//    static final String TRACE_BAT_VOL2 = "battery_volt2";

//    static final String KEY_PT_ID = "point_id";
//    static final String PT_TIME = "point_time";
//    static final String PT_LAT = "point_lat";
//    static final String PT_LON = "point_lon";
//    static final String PT_SPEED = "point_speed";
//    static final String PT_ROT = "point_rot";
//    static final String PT_TEMP = "point_temp";
//    static final String PT_ACC = "point_acc";
//    static final String PT_BAT = "point_battery";
//    static final String PT_THROTTLE = "point_throttle";
//    static final String PT_ENGINE_LOAD = "point_engine_load";
//    static final String PT_MPG_INST = "point_mpg_inst";
//    static final String PT_MPG_AGGRE = "point_mpg_aggregate";
//    static final String PT_FUEL_LEVEL = "point_fuel_level";
    static final String KEY_PT_TRACE = "trace_id";
//    static final String PT_DTCs = "point_dtcs";
//    static final String PT_IDLE = "point_idle";
//    static final String PT_OBD_CONN = "point_obd_conn"; 
    
    static final String KEY_EVENT_TRACE = "trace_id";
    static final String EVENT_TYPE = "event_type";
    static final String EVENT_START = "event_startTime";
    static final String EVENT_END = "event_endTime";
    static final String EVENT_LOC_LAT = "event_lat";
    static final String EVENT_LOC_LON = "event_lon";

    static final String DB_DATABASE_NAME = "db_trace.db";

    static final String DB_TABLE = "db_table";
    static final String TRACE_TABLE = "trace_table";
    static final String POINT_TABLE = "point_table";
    static final String EVENT_TABLE = "event_table";


    static final String DB_TABLE_CREATE =
            "create table db_table(db_version integer primary key autoincrement, db_time text);";

    static final String TRACE_TABLE_CREATE =
            "create table trace_table(trace_id integer primary key autoincrement,"+
                    "trace_startTime text, trace_endTime text, trace_avgSpeed float,trace_distance float, " + 
            		"trip_start_lon double, trip_start_lat double," + 
            		"trip_end_lon double, trip_end_lat double," +
                    "safe_score int, safe_score_total int);";

//    static final String POINT_TABLE_CREATE =
//            "create table point_table(point_id integer primary key autoincrement,trace_id INT8 REFERENCES trace_table(trace_id)," +
//                    "point_lat double, point_lon double, point_time text);";
    
    static final String EVENT_TABLE_CREATE = 
    		"create table event_table(event_id integer primary key autoincrement,trace_id int REFERENCES trace_table(trace_id)," + 
    				"event_type int, event_startTime text, event_endTime text, event_lon double, event_lat double);";

    SQLiteDatabase db;

    public void open()
    {
        String dbPath = Environment.getExternalStorageDirectory().getPath() + "/OBDII/" + DB_DATABASE_NAME;
//        String dbPath = context.getFilesDir().getPath() + "/" + DB_DATABASE_NAME;
        File file = new File(dbPath);
        if (!file.exists()){
            db = SQLiteDatabase.openOrCreateDatabase(dbPath, null);
            db.execSQL(DB_TABLE_CREATE);
            db.execSQL(TRACE_TABLE_CREATE);
//            db.execSQL(POINT_TABLE_CREATE);
            db.execSQL(EVENT_TABLE_CREATE);

            int ver = 0;
            Date time = null;
            try {
                time = DateUtil.DateStrToDate("2000-01-01 00:00:00");
            }catch (Exception e){
                e.printStackTrace();
            }
            insertDbInfor(ver, time);
        }
        else{
            db = SQLiteDatabase.openOrCreateDatabase(file.getPath(), null);
        }

    }

    public void close()
    {
        db.close();
    }

    public boolean deleteDb() {
        try {
            String dbPath = Environment.getExternalStorageDirectory().getPath() + "/" + DB_DATABASE_NAME;
            File f = new File(dbPath);
            return f.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public void insertDbInfor(int ver, Date time){
        String dateStr = DateUtil.DateToDateStr(time);
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_DB_VER, ver);
        initialValues.put(DB_TIME, dateStr);
        db.insert(DB_TABLE, null, initialValues);
    }

    public void updateDbInfo(){
        String dateStr = getLatestTraceDate();

        long dbVersion = getDbVersion();
        dbVersion++;

        ContentValues updateValues = new ContentValues();
        updateValues.put(KEY_DB_VER, dbVersion);
        updateValues.put(DB_TIME, dateStr);
        db.update(DB_TABLE, updateValues, null, null);
    }

    public void insertTrace(long traceId, Date startTime, Date endTime, float avgSpeed, float dis, double stLat, double stLon, 
    		double enLat, double enLon, int score, int averAcore)
	{
		String dateStart = DateUtil.DateToDateStr(startTime);
		String dateEnd = DateUtil.DateToDateStr(endTime);
		
		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_TRACE_ID, traceId);
		initialValues.put(TRACE_START, dateStart);
		initialValues.put(TRACE_END, dateEnd);
		initialValues.put(TRACE_SPEED, avgSpeed);		
		initialValues.put(TRACE_DIS, dis);
		initialValues.put(TRACE_ST_LAT, stLat);
		initialValues.put(TRACE_ST_LON, stLon);
		initialValues.put(TRACE_EN_LAT, enLat);		
		initialValues.put(TRACE_EN_LON, enLon);
		initialValues.put(TRACE_PER_SCORE, score);		
		initialValues.put(TRACE_AVER_SCORE, averAcore);		
		db.insert(TRACE_TABLE, null, initialValues);
	}
    
    public void insertTrace(long traceId, Date startTime, Date endTime, float avgSpeed, int avgRot, float avgTemp, int dis, 
    						String trace_loc, long idle_time, long night_time, long speedy_time, long peak_time, 
    						long time60, long time90, long time120, long time120plus, float total_fuel, float idle_fuel, 
    						float avg_mpg, float bat_v1, float bat_v2)
    {
        String dateStart = DateUtil.DateToDateStr(startTime);
        String dateEnd = DateUtil.DateToDateStr(endTime);

        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_TRACE_ID, traceId);
        initialValues.put(TRACE_START, dateStart);
        initialValues.put(TRACE_END, dateEnd);
        initialValues.put(TRACE_SPEED, avgSpeed);

        initialValues.put(TRACE_DIS, dis);
        initialValues.put(TRACE_LOC, trace_loc);
      
        
        db.insert(TRACE_TABLE, null, initialValues);
    }

    public void updateTrace(long traceId, Date endTime, float avgSpeed, int avgRot, float avgTemp, int dis){
        String dateStr = DateUtil.DateToDateStr(endTime);

        ContentValues updateValues = new ContentValues();
        updateValues.put(TRACE_END, dateStr);
        updateValues.put(TRACE_SPEED, avgSpeed);
        updateValues.put(TRACE_DIS, dis);
        db.update(TRACE_TABLE, updateValues, KEY_TRACE_ID + " = " + traceId, null);
    }
    
    public void updateTrace(long traceId, String loc){
    	ContentValues updateValues = new ContentValues();
    	updateValues.put(TRACE_LOC, loc);
    	db.update(TRACE_TABLE, updateValues, KEY_TRACE_ID + " = " + traceId, null);
    }

    public void insertBeginTransaction(){
        db.beginTransaction();
    }
    
    public void setTransactionSuccessful(){
    	db.setTransactionSuccessful();
    }

    public void insertEndTransaction(){
//        db.setTransactionSuccessful();
        db.endTransaction();
    }

  
    public void insertEvent(long traceId, int type, Date start, Date end, double lat, double lon){
    	ContentValues cv = new ContentValues();
    	
    	cv.put(KEY_EVENT_TRACE, traceId);
    	cv.put(EVENT_TYPE, type);
    	cv.put(EVENT_START, DateUtil.DateToDateStr(start));
    	cv.put(EVENT_END, DateUtil.DateToDateStr(end));
    	cv.put(EVENT_LOC_LAT, lat);
    	cv.put(EVENT_LOC_LON, lon);
    	
    	db.insert(EVENT_TABLE, null, cv);    	
    }

    public long getDbVersion(){
        long dbVersion=0;
        String sql = "Select * FROM " + DB_TABLE;
        Cursor cursor = db.rawQuery(sql, null);
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            int version = cursor.getColumnIndex(KEY_DB_VER);
            dbVersion = cursor.getLong(version);
        }

        return dbVersion;
    }

    public Date getDbLatestUpdate(){
        Date date = DateUtil.DateStrToDate("2000-01-01 00:00:00");
        String sql = "Select * FROM " + DB_TABLE;
        Cursor cursor = db.rawQuery(sql, null);
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            int time = cursor.getColumnIndex(DB_TIME);
            String sDate = cursor.getString(time);
            date = DateUtil.DateStrToDate(sDate);
        }

        return date;
    }

    public Cursor getEventsById(long id, int type){
//        String sql = "Select * FROM " + EVENT_TABLE + " event " + " JOIN " + TRACE_TABLE + " trace " + " ON " +
//                		"trace.trace_id = " + id + " AND " + " event.trace_id = " + id + 
//                		" AND" +  " event.event_type = " + type + ";";
        String sql = "Select * FROM " + EVENT_TABLE + " where trace_id = " + id + 
        		" AND event_type = " + type + ";";        
        Cursor cursor = db.rawQuery(sql, null);
        if (cursor != null)
            cursor.moveToFirst();
        return cursor;
    }

    public Cursor getTraceById(long rowId) throws SQLException
    {
        Cursor cursor = db.query(TRACE_TABLE, null, KEY_TRACE_ID + "=" + rowId, null, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();
        return cursor;
    }

    private String getLatestTraceDate(){
        String dateStr = "2000-01-01 00:00:00";
        Cursor cursor = db.query(TRACE_TABLE, null, null, null, null, null, TRACE_END + " desc", null);
        if (cursor.getCount() > 0){
            cursor.moveToFirst();
            int time = cursor.getColumnIndex(DBTraceDatabase.TRACE_END);
            dateStr = cursor.getString(time);
        }
        return dateStr;
    }

    public Cursor getAllTrace()
    {
        Cursor cursor = db.query(TRACE_TABLE, null, null, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();
        return cursor;
    }

    
    
    public int getScoreWeight(int max, int min){
        String sql = "SELECT COUNT(safe_score) FROM " + TRACE_TABLE + " where safe_score >= " + min 
        		+ " AND safe_score <= " + max;
        Cursor cursor = db.rawQuery(sql, null);
        if (cursor != null)        	
            cursor.moveToFirst();
        return cursor.getInt(0);
    }
    
    public int getCountOfTrace(){
        String sql = "SELECT COUNT(*) FROM " + TRACE_TABLE;
        Cursor cursor = db.rawQuery(sql, null);
        
        if (cursor == null)        	
            return 0;
        cursor.moveToFirst();
        int idx = cursor.getColumnIndex("COUNT(*)");

        return  cursor.getInt(idx);
    }
    
    
    public int getCountOfEvent(){
        String sql = "SELECT COUNT(*) FROM " + EVENT_TABLE;
        Cursor cursor = db.rawQuery(sql, null);
        if (cursor == null)        	
            return 0;
        cursor.moveToFirst();
        return  cursor.getInt(0);
    }
    
    //获得某一个月平均分
    public int getMonthScore(int year, int month){
    	Date stdt = new Date(), endt = new Date();
    	stdt.setTime(0);
    	stdt.setYear(year);
    	stdt.setMonth(month);
    	stdt.setHours(0);
    	endt.setTime(0);
    	endt.setHours(0);
    	if(month == 11){
    		stdt.setYear(year+1);
    		stdt.setMonth(0);
    	}
    	else
    	{
    		endt.setYear(year);
    		endt.setMonth(month+1);
    	}
    	
    	String stStr, enStr;
    	stStr = DateUtil.sdf_up.format(stdt);
    	enStr = DateUtil.sdf_up.format(endt);
        String sql = "SELECT AVG(safe_score) FROM " + TRACE_TABLE + " where trace_startTime >= \"" + stStr + "\" "
        		+ " AND trace_startTime < \"" + enStr + "\"";
        Cursor cursor = db.rawQuery(sql, null);
        if (cursor == null)        	
        	return 0;
        cursor.moveToFirst();
        
    	return cursor.getInt(0);
    }
    
    public int getDateScore(int year, int month, int date){
    	Date stdt = new Date(), endt;
    	stdt.setTime(0);
    	stdt.setYear(year);
    	stdt.setMonth(month);
    	stdt.setDate(date);
    	GregorianCalendar gc=new GregorianCalendar(); 
    	gc.setTime(stdt);
    	gc.add(5, 1);//加一天
    	endt = gc.getTime();
    	String stStr, enStr;
    	stStr = DateUtil.sdf_up.format(stdt);
    	enStr = DateUtil.sdf_up.format(endt);
        String sql = "SELECT AVG(safe_score) FROM " + TRACE_TABLE + " where trace_startTime >= \"" + stStr + "\""
        		+ " AND trace_startTime < \"" + enStr + "\"";
        Cursor cursor = db.rawQuery(sql, null);
        if (cursor == null)        	
        	return 0;
        cursor.moveToFirst();

    	return cursor.getInt(0);
    }
    
    
    public Cursor getRecentTrace(){
        String sql = "Select Max(trace_endTime) AS trace_id,trace_startTime,trace_endTime,trace_avgSpeed,trace_distance,trip_start_lon,trip_start_lat,trip_end_lon,trip_end_lat,safe_score,safe_score_total FROM " + TRACE_TABLE;
        Cursor cursor = db.rawQuery(sql, null);
        if (cursor != null)
            cursor.moveToFirst();
        return cursor;
    }
}
