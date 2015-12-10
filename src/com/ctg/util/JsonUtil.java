package com.ctg.util;

import org.json.JSONException;
import org.json.JSONObject;

import com.ctg.bean.CarData;
import com.ctg.service.CarDataService;

public class JsonUtil {
    public static JSONObject GPSdataToJson(CarData updateData, long traceId){
 
    	JSONObject jsonObj = new JSONObject();   	
    	try {
    		if(!updateData.getHasOBDData()){
    			jsonObj.put("time", updateData.getCurrentTime());
    			jsonObj.put("latitude", updateData.getLat());
    	    	jsonObj.put("longitude", updateData.getLon());
    	    	jsonObj.put("speed", updateData.getVSS());
    	    	
    	    	jsonObj.put("hasOBD", false);
                jsonObj.put("traceId", traceId);
    		}
    		else {
    			jsonObj.put("time", updateData.getCurrentTime());
    			jsonObj.put("latitude", updateData.getLat());
    	    	jsonObj.put("longitude", updateData.getLon());
    	    	jsonObj.put("battery", updateData.getBat());
    	    	jsonObj.put("rotate", updateData.getRPM());
    	    	jsonObj.put("speed", updateData.getVSS());    	    	
    	    	jsonObj.put("throttle", updateData.getThrotPos());
    	    	jsonObj.put("load", updateData.getEngineLoad());
    	    	jsonObj.put("temperature", updateData.getTemp());
    	    	jsonObj.put("MPG", updateData.getMPG());
    	    	jsonObj.put("Average_MPG", updateData.getAvg_mpg());
    	    	jsonObj.put("fuel", updateData.getRemainingFuel());
    	    	jsonObj.put("Diagnosis", updateData.getDTC());
    	    	jsonObj.put("idle", updateData.isIdle());
    	    	
    	    	jsonObj.put("hasOBD", true);    	    	
                jsonObj.put("traceId", traceId);
    		}
    		


		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	return jsonObj;
    }
    
}