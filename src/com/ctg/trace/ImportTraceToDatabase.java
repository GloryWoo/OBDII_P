package com.ctg.trace;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.ctg.crash.LogRecord;
import com.ctg.ui.Base;

/**
 * Created by lee on 15/1/8.
 */
public class ImportTraceToDatabase {

    static final public int MSG_IMPORT_COMPLETE = 0;
    static final public int MSG_IMPORT_NO_MORE = 1;
    static final public int MSG_IMPORT_FAILED = 2;

    private List<StatisticsData> traceList;
    private List<List<GPS>> pointList;
    private Map<Long, HashMap<Integer, ArrayList<DrivingEvent>>> eventList;
    private TraceDatabase traceDatabase=null;
    public ImportToDBDelegate importDelegate=null;

    public ImportTraceToDatabase(){
    }

    public void importTrace(List<StatisticsData> lsTrace, List<List<GPS>> lsPoint, 
    						Map<Long, HashMap<Integer, ArrayList<DrivingEvent>>> lsEvent) {    	
    	traceList = lsTrace;
        pointList = lsPoint;
        eventList = lsEvent;

        traceDatabase = new TraceDatabase();

        Thread thread = new Thread(runnable);
        thread.start();
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            saveDataToDB();
        }
    };

    private void saveDataToDB(){

        long traceId;
        double lat;
        double lon;
        Date time;
        int traceIdx = 0;
        traceDatabase.open();

        if(traceList.size() == 0 && importDelegate != null){
        	importDelegate.importComplete(MSG_IMPORT_NO_MORE);
        	traceDatabase.close();
        	return;
        }
        
        traceDatabase.insertBeginTransaction();
        try{
            for(StatisticsData trace:traceList) {
                traceId = trace.getSid();
                traceDatabase.insertTrace(traceId, trace.getStartTime(), trace.getEndTime(), trace.getAverageSpeed(), trace.getAverageRotate(), trace.getAverageTemp(), trace.getDistance(),
                							trace.getTraceLocation(), trace.getIdleTime(), trace.getNightTime(), trace.getSpeedingTime(), trace.getRushTime(),
                							trace.get0to60(), trace.get60to90(), trace.get90to120(), trace.get120above(), trace.getTotal_fuel(), trace.getIdle_fuel(),
                							trace.getAverage_mpg(), trace.getBattery_volt_1(), trace.getBattery_volt_2());

                List<GPS> lsGps = pointList.get(traceIdx);
//                traceDatabase.insertBeginTransaction();
                for(GPS gps:lsGps){
                    time = gps.getCreatetime();
                    lat = gps.getLat();
                    lon = gps.getLon();
                    traceDatabase.insertPoint(traceId, time, lat, lon);
                }
//                traceDatabase.insertEndTransaction();
                traceIdx++;
            }
            
            if (eventList != null && eventList.size() != 0) {
    			for (Entry<Long, HashMap<Integer, ArrayList<DrivingEvent>>> entry : eventList.entrySet()) {
    				HashMap<Integer, ArrayList<DrivingEvent>> tempmap2 = entry.getValue();
    				for (Entry<Integer, ArrayList<DrivingEvent>> entry2 : tempmap2.entrySet()) {
    					ArrayList<DrivingEvent> deList = entry2.getValue();
    					for (int i = 0; i < deList.size(); i++) {
    						DrivingEvent de = deList.get(i);
    						long tid = Long.parseLong(de.getTripid());
    						int type = de.getM_event_type();
    						Date start = de.getM_start_time();
    						Date end = de.getM_end_time();
    						double lat_e = de.getM_loc_lat();
    						double lon_e = de.getM_loc_lon();
    						traceDatabase.insertEvent(tid, type, start,end, lat_e, lon_e);
    					}
    				}
    			}
    		}
    		traceDatabase.updateDbInfo();
    		traceDatabase.setTransactionSuccessful();	// commit
            if(importDelegate!=null)importDelegate.importComplete(MSG_IMPORT_COMPLETE);
        } catch(Exception e) {
            if(importDelegate!=null)
                importDelegate.importComplete(MSG_IMPORT_FAILED);        	
        	LogRecord.SaveLogInfo2File(Base.WeathInfo, "[ImportTraceToDatabase]:" + e.toString());
        } finally {
        	// end the transaction
        	traceDatabase.insertEndTransaction();
        } 
		
        traceDatabase.close();
    }
}
