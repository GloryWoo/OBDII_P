package com.ctg.TrafficViolationPt;

import com.ctg.crash.LogRecord;
import com.ctg.ui.Base;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

/**
 * Created by ChaLi on 12/26/2014.
 */
public class ViolationPtDatabase {

    static final String KEY_ROWID = "id";
    static final String KEY_LON = "lon";
    static final String KEY_LAT = "lat";
    static final String VIOLATION_TYPE = "violationtype";
    static final String FREQUENCY = "frequency";
    static final String TAG = "DBAdapter";

    static final String DATABASE_TABLE = "ticket";
    static final int DATABASE_VERSION = 1;

    static int dbVersion = 0;

    static final String DATABASE_CREATE =
            "create table ticket(id integer primary key autoincrement,lon real,lat real," +
                    "violationtype integer,frequency integer);";
    final Context context;
    SQLiteDatabase db;

    public ViolationPtDatabase(Context cxt)
    {
        this.context = cxt;
    }

    public void upgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.wtf(TAG, "Upgrading database from version " + oldVersion + "to " + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS contacts");
    }

    //open the database
    public ViolationPtDatabase open() throws SQLException
    {
        String dbPath = Environment.getExternalStorageDirectory().getPath() + "/OBDII/Violation/Violation.db";
       // String dbPath = context.getFilesDir().getPath() + "/Violation/Violation.db";
        db = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READWRITE);
        return this;
    }
    //close the database
    public void close()
    {
        db.close();
    }

    //insert a contact into the database
    public long insert(double lon, double lat, int type, String frequency)
    {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_LON, lon);
        initialValues.put(KEY_LAT, lat);
        initialValues.put(VIOLATION_TYPE, type);
        initialValues.put(FREQUENCY, frequency);
        return db.insert(DATABASE_TABLE, null, initialValues);
    }
    //delete a particular contact
    public boolean delete(long rowId)
    {
        return db.delete(DATABASE_TABLE, KEY_ROWID + "=" +rowId, null) > 0;
    }
    public void drop()
    {
        db.execSQL("DROP TABLE IF EXISTS contact");
    }
    //retreves all the contacts
    public Cursor getAll()
    {
        return db.query(DATABASE_TABLE, null, null, null, null, null, null);
    }
    //retreves a particular contact
    public Cursor get(long rowId) throws SQLException
    {
        Cursor mCursor =
                db.query(DATABASE_TABLE, null, KEY_ROWID + "=" + rowId, null, null, null, null, null);
        if (mCursor != null)
            mCursor.moveToFirst();
        return mCursor;
    }
    public Cursor getByCoor(final double lon,final double lat, final float scale, final int[] type,float mScreenWidth,float mScreenHeight) {
        CalcRangeByZoom calcZoom = new CalcRangeByZoom(scale, mScreenWidth, mScreenHeight);
        double lonRange = calcZoom.getLonRange(mScreenHeight);
        double latRange = calcZoom.getLatRange(mScreenWidth);
        double lon1 = lon + lonRange;
        double lat1  = lat + latRange;
        double lon2 = lon - lonRange;
        double lat2  = lat - latRange;
        String sType = " and violationtype in ( ";
        for(int i = 0; i < type.length; i++){
            sType += type[i];
            if(i+1!=type.length)
                sType += ",";
        }
        sType += " )";
        String selRange = "lon between " + lon2 + " and " + lon1 +
                " and lat between " + lat2 + " and "+ lat1 +  sType;
        String orderby = "frequency desc";
        Log.v(TAG, selRange);
        LogRecord.SaveLogInfo2File(Base.WeathInfo, selRange);
        Cursor mCursor = db.query(DATABASE_TABLE, null,
                selRange,
                null,
                null, null, orderby, null);
        if (mCursor != null)
            mCursor.moveToFirst();
        return mCursor;
    }
   
    //updates a contact
    public boolean update(long rowId,double lon, double lat, int type, String frequency)
    {
        ContentValues args = new ContentValues();
        args.put(KEY_LON, lon);
        args.put(KEY_LAT, lat);
        args.put(VIOLATION_TYPE, type);
        args.put(FREQUENCY, frequency);
        return db.update(DATABASE_TABLE, args, KEY_ROWID + "=" +rowId, null) > 0;
    }


    class CalcRangeByZoom {
        private float mZoom, mScreenWidth, mScreenHeight;
        public CalcRangeByZoom(float zoom,float mScreenWidth,float mScreenHeight){
        	this.mZoom = zoom;
            this.mScreenWidth = mScreenWidth;
            this.mScreenHeight = mScreenHeight;
        }
//        public double getLatRange(){
//            if(mZoom < 4 && mZoom >= 3)//20000km
//                return 180.2776;
//            else if(mZoom< 5 && mZoom >= 4)//10000km
//                return 90.1388;
//            else if(mZoom < 6 && mZoom >= 5)//5000km
//                return 45.0694;
//            else if(mZoom < 7 && mZoom >= 6)//2000km
//                return 18.02776;
//            else if(mZoom < 8 && mZoom >= 7)//1000km
//                return 9.01388;
//            else if(mZoom < 9 && mZoom >= 8)//500km
//                return 4.507;
//            else if(mZoom < 10 && mZoom >= 9)//250km
//                return 2.2534;
//            else if(mZoom < 11 && mZoom >= 10)//200km
//                return 1.803;
//            else if(mZoom < 12 && mZoom >= 11)//100km
//                return 0.901388;
//            else if(mZoom < 13 && mZoom >= 12)//50km
//                return 0.45069;
//            else if(mZoom < 14 && mZoom >= 13)//20km
//                return 0.1802776;
//            else if(mZoom < 15 && mZoom >= 14)//10km
//                return 0.0901388;
//            else if(mZoom < 16 && mZoom >= 15)//5km
//                return 0.045069;
//            else if(mZoom < 17 && mZoom >= 16)//2km
//                return 0.0180278;
//            else if(mZoom < 18 && mZoom >= 17)//1km
//                return 0.009014;
//            else if(mZoom < 19 && mZoom >= 18)//0.5km
//                return 0.0045;
//            else if(mZoom == 19)//0.2km
//                return 0.0018028;
//            else return 0.0;
//        }
//        public double getLonRange(){
//            if(mZoom < 4 && mZoom >= 3)//20000km
//                return 234.53;
//            else if(mZoom< 5 && mZoom >= 4)//1000km
//                return 117.266;
//            else if(mZoom < 6 && mZoom >= 5)//5000km
//                return 58.633;
//            else if(mZoom < 7 && mZoom >= 6)//2000km
//                return 23.453;
//            else if(mZoom < 8 && mZoom >= 7)//1000km
//                return 11.7266;
//            else if(mZoom < 9 && mZoom >= 8)//500km
//                return 5.8633;
//            else if(mZoom < 10 && mZoom >= 9)//250km
//                return 2.93165;
//            else if(mZoom < 11 && mZoom >= 10)//200km
//                return 2.34533;
//            else if(mZoom < 12 && mZoom >= 11)//100km
//                return 1.17266;
//            else if(mZoom < 13 && mZoom >= 12)//50km
//                return 0.58633;
//            else if(mZoom < 14 && mZoom >= 13)//20km
//                return 0.23453;
//            else if(mZoom < 15 && mZoom >= 14)//10km
//                return 0.117266;
//            else if(mZoom < 16 && mZoom >= 15)//5km
//                return 0.058633;
//            else if(mZoom < 17 && mZoom >= 16)//2km
//                return 0.02345325;
//            else if(mZoom < 18 && mZoom >= 17)//1km
//                return 0.0117266;
//            else if(mZoom < 19 && mZoom >= 18)//0.5km
//                return 0.00586331;
//            else if(mZoom == 19)//0.2km
//                return 0.00234533;
//            else return 0.0;
//        }

        public double getLatRange(float mScreenWidth){
            if(mZoom < 4 && mZoom >= 3)//10000km
                return 2000*mScreenWidth/110.94;
            else if(mZoom< 5 && mZoom >= 4)//5000km
                return 1000*mScreenWidth/110.94;
            else if(mZoom < 6 && mZoom >= 5)//2500km
                return 500*mScreenWidth/110.94;
            else if(mZoom < 7 && mZoom >= 6)//1000km
                return 200*mScreenWidth/110.94;
            else if(mZoom < 8 && mZoom >= 7)//500km
                return 100*mScreenWidth/110.94;
            else if(mZoom < 9 && mZoom >= 8)//25km
                return 50*mScreenWidth/110.94;
            else if(mZoom < 10 && mZoom >= 9)//125km
                return 25*mScreenWidth/110.94;
            else if(mZoom < 11 && mZoom >= 10)//100km
                return 20*mScreenWidth/110.94;
            else if(mZoom < 12 && mZoom >= 11)//50km
                return 10*mScreenWidth/110.94;
            else if(mZoom < 13 && mZoom >= 12)//25km
                return 5*mScreenWidth/110.94;
            else if(mZoom < 14 && mZoom >= 13)//10km
                return 2*mScreenWidth/110.94;
            else if(mZoom < 15 && mZoom >= 14)//5km
                return 1*mScreenWidth/110.94;
            else if(mZoom < 16 && mZoom >= 15)//2.5km
                return 0.5*mScreenWidth/110.94;
            else if(mZoom < 17 && mZoom >= 16)//1km
                return 0.2*mScreenWidth/110.94;
            else if(mZoom < 18 && mZoom >= 17)//0.5km
                return 0.1*mScreenWidth/110.94;
            else if(mZoom < 19 && mZoom >= 18)//0.25km
                return 0.05*mScreenWidth/110.94;
            else if(mZoom == 19)//0.125km
                return 0.02*mScreenWidth/110.94;
            else return 0.0;
        }
        public double getLonRange(float mScreenHeight){
            if(mZoom < 4 && mZoom >= 3)//10000km
                return 2000*mScreenHeight/85.7;
            else if(mZoom< 5 && mZoom >= 4)//5000km
                return 1000*mScreenHeight/85.7;
            else if(mZoom < 6 && mZoom >= 5)//2500km
                return 500*mScreenHeight/85.75;
            else if(mZoom < 7 && mZoom >= 6)//1000km
                return 200*mScreenHeight/85.7;
            else if(mZoom < 8 && mZoom >= 7)//500km
                return 100*mScreenHeight/85.7;
            else if(mZoom < 9 && mZoom >= 8)//250km
                return 50*mScreenHeight/85.7;
            else if(mZoom < 10 && mZoom >= 9)//125km
                return 25*mScreenHeight/85.7;
            else if(mZoom < 11 && mZoom >= 10)//100km
                return 20*mScreenHeight/85.7;
            else if(mZoom < 12 && mZoom >= 11)//50km
                return 10*mScreenHeight/85.7;
            else if(mZoom < 13 && mZoom >= 12)//25km
                return 5*mScreenHeight/85.7;
            else if(mZoom < 14 && mZoom >= 13)//10km
                return 2*mScreenHeight/85.7;
            else if(mZoom < 15 && mZoom >= 14)//5km
                return 1*mScreenHeight/85.7;
            else if(mZoom < 16 && mZoom >= 15)//2.5km
                return 0.5*mScreenHeight/85.7;
            else if(mZoom < 17 && mZoom >= 16)//1km
                return 0.2*mScreenHeight/85.7;
            else if(mZoom < 18 && mZoom >= 17)//0.5km
                return 0.1*mScreenHeight/85.7;
            else if(mZoom < 19 && mZoom >= 18)//0.25km
                return 0.05*mScreenHeight/85.7;
            else if(mZoom == 19)//0.125km
                return 0.02*mScreenHeight/85.7;
            else return 0.0;
        }
    }
}
