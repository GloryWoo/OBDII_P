package com.ctg.trace;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.CoordinateConverter;
import com.ctg.util.Preference;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
public class TraceDataSource implements DownloadDelegate, ImportToDBDelegate{
    static final public int RET_UPDATE_COMPETE = 0;
    static final public int RET_UPDATE_FAIL = 1;
    static final public int RET_UPDATE_ALL = 2;
    static final public int RET_UPDATE_NO_MORE = 3;

    public TraceDataSourceDelegate traceDataSourceDelegate=null;
    private Context context;

    public TraceDataSource(Context cxt){
        context = cxt;

        TraceListInstance traceList = TraceListInstance.getInstance();
        traceList.addDownloadListener(this);
        traceList.addImportListener(this);
    }

    public boolean updateTrace(boolean trackable){
        Date curDate = new Date(System.currentTimeMillis());
        TraceListInstance traceList = TraceListInstance.getInstance();
        traceList.downloadTrace(context, curDate, trackable);
        return true;
    }

    public List<StatisticsData> getListData() {
        TraceListInstance singletonClass = TraceListInstance.getInstance();
        return singletonClass.getTraceList(context);
    }

    public List<GPS> getPtListData(long id){
        TraceListInstance singletonClass = TraceListInstance.getInstance();
        return singletonClass.getTracePtList(id);
    }
    
    public List<DrivingEvent> getEventListData(long id, int type){
    	TraceListInstance singletonClass = TraceListInstance.getInstance();
    	return singletonClass.getTraceEventList(id, type);
    }

    public void newTrace(){
        Date curDate = new Date(System.currentTimeMillis());
        TraceListInstance singletonClass = TraceListInstance.getInstance();
        singletonClass.addNewTrace(curDate);
    }
    
    public void setTraceID(long tid){
    	TraceListInstance singletonClass = TraceListInstance.getInstance();
        singletonClass.setCurRecTraceId(tid);
    }

    public void addTracePt(GPS gps){
        TraceListInstance singletonClass = TraceListInstance.getInstance();
        singletonClass.addNewPtToTrace(gps);
        singletonClass.updateTraceInfo(gps);
    }
    
    public void addTraceLocation(long tid, String location){
    	TraceListInstance singletonClass = TraceListInstance.getInstance();
    	singletonClass.updateTraceInfo(tid, location);
    }

    public void changeUser(){
        TraceListInstance singletonClass = TraceListInstance.getInstance();
        singletonClass.changeDatabase();
    }

    public long getCurTraceId(){
        TraceListInstance singletonClass = TraceListInstance.getInstance();
        return singletonClass.getCurRecTraceId();
    }

    public void downloadComplete(int ret){
        if (ret == DownloadTrace.MSG_DOWNLOAD_COMPLETE){
            TraceListInstance singletonClass = TraceListInstance.getInstance();
            singletonClass.saveTraceToDB();
        }
        else if(ret == DownloadTrace.MSG_DOWNLOAD_FAIL){
            if (traceDataSourceDelegate!=null)
                traceDataSourceDelegate.updateResult(RET_UPDATE_FAIL);
        }
        else if(ret == DownloadTrace.MSG_DOWNLOAD_NO_MORE){
            if (traceDataSourceDelegate!=null)
                traceDataSourceDelegate.updateResult(RET_UPDATE_ALL);
        }
    }

    public void importComplete(int ret){
        if(traceDataSourceDelegate == null)return;
    	
    	if (ret == ImportTraceToDatabase.MSG_IMPORT_COMPLETE){
            //update complete
            traceDataSourceDelegate.updateResult(RET_UPDATE_COMPETE);            
        }
        else if(ret == ImportTraceToDatabase.MSG_IMPORT_NO_MORE){
        	traceDataSourceDelegate.updateResult(RET_UPDATE_NO_MORE);
        }
        else if(ret == ImportTraceToDatabase.MSG_IMPORT_FAILED){
        	traceDataSourceDelegate.updateResult(RET_UPDATE_FAIL);
        }
        
    }

}