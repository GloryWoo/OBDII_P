package com.ctg.net;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.Response;
import com.ctg.ui.Base;
import com.ctg.ui.OBDApplication;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

public class GetDriveBehavior {
	public static final int EVENT_TYPE_LANE_CHANGE = 1;
	public static final int EVENT_TYPE_TURN = 2;
	public static final int EVENT_TYPE_SPEEDY_TURN = 3;
	public static final int EVENT_TYPE_ZIGZAG = 4;
	public static final int EVENT_TYPE_DOUBLE_LANE_CHANGE = 5;
	public static final int EVENT_TYPE_HARD_ACCEL = 6;
	public static final int EVENT_TYPE_HARD_BRAKE = 7;
	public static final int EVENT_TYPE_BUMPY_ROAD = 8;
	public static final int EVENT_ILLEGAL_U_TURN = 9;
	public static final int EVENT_OPPOSITE_DRIVING = 10;
	
	public static final int EVENT_TYPE_TOTAL = 10;
	
	private OBDApplication obdApplication;
	private String driveBehaviorURL = Base.HTTP_ROOT_PATH + "/services/getDriverBehavior";
	
	private AnalysisResult analysis_result;	
	
	public void downloadDriveBehavior(final Context mContext, String startTime, String endTime){
		Map<String, String> postData = new HashMap<String, String>();

		postData.put("startTime", startTime);
		postData.put("endTime", endTime);
		CacheManager.getJson(mContext, driveBehaviorURL, new IHttpCallback(){

			@Override
			public void handle(int retCode, Object response) {
				// TODO Auto-generated method stub
				String resultString = response.toString();
//				String resultString = (((Response<String>)response).result).toString();
				if(retCode == 200){
					analysis_result = parseResultString(resultString);
					analysis_result.calculateScores();
					if(Base.OBDApp.driveDetail != null){
						Base.OBDApp.driveDetail.jizhuan_cnt.setText(analysis_result.getEventLists().get(EVENT_TYPE_SPEEDY_TURN).size() + "次");
						Base.OBDApp.driveDetail.jisha_cnt.setText(analysis_result.getEventLists().get(EVENT_TYPE_HARD_ACCEL).size() + 
																	analysis_result.getEventLists().get(EVENT_TYPE_HARD_BRAKE).size() + "次");
						Base.OBDApp.driveDetail.sbiandao_cnt.setText(analysis_result.getEventLists().get(EVENT_TYPE_ZIGZAG).size() + "次");
						Base.OBDApp.driveDetail.kuaidao_cnt.setText(analysis_result.getEventLists().get(EVENT_TYPE_DOUBLE_LANE_CHANGE).size() + "次");
						Base.OBDApp.driveDetail.diaotou_cnt.setText(analysis_result.getEventLists().get(EVENT_ILLEGAL_U_TURN).size() + "次");
						Base.OBDApp.driveDetail.nixiang_cnt.setText(analysis_result.getEventLists().get(EVENT_OPPOSITE_DRIVING).size() + "次");
						Base.OBDApp.driveDetail.julie_cnt.setText(analysis_result.getEventLists().get(EVENT_TYPE_BUMPY_ROAD).size() + "次");
						Base.OBDApp.driveDetail.qiting_cnt.setText(analysis_result.getEventLists().get(EVENT_TYPE_HARD_ACCEL).size() + 
																		analysis_result.getEventLists().get(EVENT_TYPE_HARD_BRAKE).size() + "次");
						
						Base.OBDApp.driveDetail.safe_score.setText(analysis_result.getSafeScore() + "分");
						Base.OBDApp.driveDetail.economy_score.setText(analysis_result.getEconomyScore() + "分");
						Base.OBDApp.driveDetail.comfy_score.setText(analysis_result.getComfyScore() + "分");
						
						Base.OBDApp.driveDetail.mChart.setCenterText("总评分：" + analysis_result.getOverallScore() + "分"); 
					}
					// ((OBDApplication)((Activity)mContext).getApplication()).driveBehaviorData = resultString;
				}else{
					// emit an error msg, using a dlg?
				}
				
			}
			
		}, postData);
	}
	
	private AnalysisResult parseResultString(String result){
		AnalysisResult ar = new AnalysisResult();
		String temp_str;
//		HashMap<Integer, ArrayList<DrivingEvent>> analyzed_events = new HashMap<Integer, ArrayList<DrivingEvent>>();
		
		try {
			JSONObject ar_json = new JSONObject(result);			
			JSONArray event_arr;
			String str;				
			
			for (int i = 1; i <= EVENT_TYPE_TOTAL; i++) {
				if ((event_arr = ar_json.getJSONArray(String.valueOf(i))) != null) {
//					ArrayList<DrivingEvent> events_list = new ArrayList<DrivingEvent>();
					for (int j = 0; j < event_arr.length(); j++) {
						JSONObject event_json = (JSONObject) event_arr.get(j);
						DrivingEvent event = new DrivingEvent();

						if ((str = event_json.getString("m_start_time")) != null) {
							event.setStartTime(str);
						}

						if ((str = event_json.getString("m_end_time")) != null) {
							event.setEndTime(str);
						}

						if ((str = event_json.getString("m_loc_lon")) != null) {
							event.setLongitude(Double.valueOf(str));
						}

						if ((str = event_json.getString("m_loc_lat")) != null) {
							event.setLatitude(Double.valueOf(str));
						}
						ar.getEventLists().get(i).add(event);
//						events_list.add(event);
					}
//					analyzed_events.put(i, events_list);
				}
			}				
//			}
//			ar.setEventLists(analyzed_events);
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		return ar;
	}
	
	public AnalysisResult getAnalysisResult() { 
        return analysis_result; 
    }
	
	public class AnalysisResult {
		
		private HashMap<Integer, ArrayList<DrivingEvent>> m_analyzed_events = new HashMap<Integer, ArrayList<DrivingEvent>>();
		
		private int m_safe_score = 0;
		private int m_economy_score = 0;
		private int m_comfy_score = 0;
		
		private int m_overall_score = 0;
		
		public AnalysisResult() {
			// TODO Auto-generated constructor stub
			for (int i = 1; i <= EVENT_TYPE_TOTAL; i++){
				m_analyzed_events.put(i, new ArrayList<DrivingEvent>());
			}
		}	
		
		public int getSafeScore(){
			return this.m_safe_score;
		}
		
		public int getEconomyScore(){
			return this.m_economy_score;
		}	
		
		public int getComfyScore(){
			return this.m_comfy_score;
		}
		
		public int getOverallScore(){
			return this.m_overall_score;
		}
		
		public HashMap<Integer, ArrayList<DrivingEvent>> getEventLists(){
			return m_analyzed_events;
		}
		
		public void setEventLists(HashMap<Integer, ArrayList<DrivingEvent>> events_list){
			this.m_analyzed_events = events_list;
		}
		
		public List<DrivingEvent> getLaneChangeEvents(){
			if(m_analyzed_events.get(EVENT_TYPE_LANE_CHANGE) != null)
				return m_analyzed_events.get(EVENT_TYPE_LANE_CHANGE);
			return new ArrayList<DrivingEvent>();
		}
		
		public List<DrivingEvent> getTurnEvents(){
			if(m_analyzed_events.get(EVENT_TYPE_TURN) != null)
				return m_analyzed_events.get(EVENT_TYPE_TURN);
			return new ArrayList<DrivingEvent>();
		}
		
		public List<DrivingEvent> getSpeedyTurnEvents(){
			if(m_analyzed_events.get(EVENT_TYPE_SPEEDY_TURN) != null)
				return m_analyzed_events.get(EVENT_TYPE_SPEEDY_TURN);
			return new ArrayList<DrivingEvent>();
		}
		
		public List<DrivingEvent> getZigzagEvents(){
			if(m_analyzed_events.get(EVENT_TYPE_ZIGZAG) != null)
				return m_analyzed_events.get(EVENT_TYPE_ZIGZAG);
			return new ArrayList<DrivingEvent>();
		}
		
		public List<DrivingEvent> getDoubleLaneChangeEvents(){
			if(m_analyzed_events.get(EVENT_TYPE_DOUBLE_LANE_CHANGE) != null)
				return m_analyzed_events.get(EVENT_TYPE_DOUBLE_LANE_CHANGE);
			return new ArrayList<DrivingEvent>();
		}
		
		public List<DrivingEvent> getHardAccelEvents(){
			if(m_analyzed_events.get(EVENT_TYPE_HARD_ACCEL) != null)
				return m_analyzed_events.get(EVENT_TYPE_HARD_ACCEL);
			return new ArrayList<DrivingEvent>();
		}
		
		public List<DrivingEvent> getHardBrakeEvents(){
			if(m_analyzed_events.get(EVENT_TYPE_HARD_BRAKE) != null)
				return m_analyzed_events.get(EVENT_TYPE_HARD_BRAKE);
			return new ArrayList<DrivingEvent>();
		}
		
		public List<DrivingEvent> getBumpyEvents(){
			if(m_analyzed_events.get(EVENT_TYPE_BUMPY_ROAD) != null)
				return m_analyzed_events.get(EVENT_TYPE_BUMPY_ROAD);
			return new ArrayList<DrivingEvent>();
		}
		
		public List<DrivingEvent> getIllegalUTurnEvents(){
			if(m_analyzed_events.get(EVENT_ILLEGAL_U_TURN) != null)
				return m_analyzed_events.get(EVENT_ILLEGAL_U_TURN);
			return new ArrayList<DrivingEvent>();
		}
		
		public List<DrivingEvent> getOppoDrivingEvents(){
			if(m_analyzed_events.get(EVENT_OPPOSITE_DRIVING) != null)
				return m_analyzed_events.get(EVENT_OPPOSITE_DRIVING);
			return new ArrayList<DrivingEvent>();
		}
		
		private void calculateScores(){
			// for debug only
			m_safe_score = 85;
			m_economy_score = 78;
			m_comfy_score = 88;
			
			m_overall_score = (m_safe_score + m_economy_score + m_comfy_score) / 3;
		}
	}
	
	class DrivingEvent {	
		private int m_event_type;
		private Date m_start_time;
		private Date m_end_time;
		private double m_loc_lon;
		private double m_loc_lat;	
		
		public DrivingEvent() {

		}
		
		public int getEventType(){
			return this.m_event_type;
		}
		
		public Date getStartTime(){
			return this.m_start_time;
		}
		
		public void setStartTime(String start_time){
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
			try {
				this.m_start_time = sdf.parse(start_time);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		public Date getEndTime(){
			return this.m_end_time;
		}
		
		public void setEndTime(String end_time){
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
			try {
				this.m_end_time = sdf.parse(end_time);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		public double getLongitude(){
			return this.m_loc_lon;
		}
		
		public void setLongitude(double lon){
			this.m_loc_lon = lon;
		}
		
		public double getLatitude(){
			return this.m_loc_lat;
		}
		
		public void setLatitude(double lat){
			this.m_loc_lat = lat;
		}

	}

}
