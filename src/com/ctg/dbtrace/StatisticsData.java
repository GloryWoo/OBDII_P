package com.ctg.dbtrace;

import java.io.Serializable;
import java.sql.Timestamp;

public class StatisticsData implements Serializable {
	private static final long serialVersionUID = -1700870998548314002L;
//	private long Sid;
	long tripId;
	private String userID;
	private Timestamp startTime;
	private Timestamp endTime;
	private float averageSpeed;
	private int averageRotate;
	private float averageTemp;
	private float distance;

	public double startLat;
	public double startLon;
	public double endLat;
	public double endLon;
	public int scoreTheTrip;
	public int scoreAllAver;
	// extra trace statistics data added by Weiran

	
	public StatisticsData() {
	}
    

	public StatisticsData(long trip_id, String userID, Timestamp startTime,
			Timestamp endTime, double sLat, double sLon, double eLat, double eLon,
			float distance, float averageSpeed,  int score_single_trip, int score_all_average
		) {
		tripId = trip_id;
		this.userID = userID;
		this.startTime = startTime;
		this.endTime = endTime;
		startLat = sLat;
		startLon = sLon;
		endLat = eLat;
		endLon = eLon;		
		this.distance = distance;
		this.averageSpeed = averageSpeed;
		scoreTheTrip = score_single_trip;
		scoreAllAver = score_all_average;		
	}
	
	public StatisticsData( String userID, Timestamp startTime,
			Timestamp endTime, float averageSpeed, int averageRotate,
			float averageTemp, int distance, String traceLocation,
			long m_time_idle, long m_time_night, long m_time_speeding,
			long m_time_rush_hour, long m_time_0_to_60, long m_time_60_to_90,
			long m_time_90_to_120, long m_time_120_above, float total_fuel,
			float idle_fuel, float average_mpg, float battery_volt_1,
			float battery_volt_2) {
		super();
		this.userID = userID;
		this.startTime = startTime;
		this.endTime = endTime;
		this.averageSpeed = averageSpeed;
		this.averageRotate = averageRotate;
		this.averageTemp = averageTemp;
		this.distance = distance;

	}

	public StatisticsData(String userID, Timestamp startTime,
			Timestamp endTime, float averageSpeed, int averageRotate,
			float averageTemp, int distance, String traceLocation, long idle,
			long night, long spd, long rush, long ti_60, long ti_6090,
			long ti_90120, long ti_120) {
		super();
		this.userID = userID;
		this.startTime = startTime;
		this.endTime = endTime;
		this.averageSpeed = averageSpeed;
		this.averageRotate = averageRotate;
		this.averageTemp = averageTemp;
		this.distance = distance;


	}






//
//	public Long getSid() {
//		return Sid;
//	}
//
//	public void setSid(Long sid) {
//		Sid = sid;
//	}

	public float getDistance() {
		return distance;
	}

	public void setDistance(int distance) {
		this.distance = distance;
	}
	
	public long getTraceId() {
		return tripId;
	}

	public String getUserID() {
		return userID;
	}

	public void setUserID(String userID) {
		this.userID = userID;
	}

	public Timestamp getStartTime() {
		return startTime;
	}

	public void setStartTime(Timestamp startTime) {
		this.startTime = startTime;
	}

	public Timestamp getEndTime() {
		return endTime;
	}

	public void setEndTime(Timestamp endTime) {
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

	public long getTripDuration() {
		return endTime.getTime() - startTime.getTime();
	}



//	public void setSid(long sid) {
//		Sid = sid;
//	}
	public void setTraceId(long tid) {
		tripId = tid;
	}
	public String toString() {
		return tripId + ":" + userID + ":" + startTime + ":" + endTime + ":"
				+ averageSpeed + ":" + averageRotate + ":" + averageTemp;
	}

}
