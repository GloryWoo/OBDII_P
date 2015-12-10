package com.ctg.trace;

import java.io.Serializable;
import java.sql.Timestamp;

public class StatisticsData implements Serializable {
	private static final long serialVersionUID = -1700870998548314002L;
	private long Sid;
	private String userID;
	private Timestamp startTime;
	private Timestamp endTime;
	private float averageSpeed;
	private int averageRotate;
	private float averageTemp;
	private int distance;

	// extra trace statistics data added by Weiran
	private String traceLocation = null;

	private long m_time_idle = 0l;
	private long m_time_night = 0l;
	private long m_time_speeding = 0l;
	private long m_time_rush_hour = 0l;

	// for pie chart
	private long m_time_0_to_60 = 0l;
	private long m_time_60_to_90 = 0l;
	private long m_time_90_to_120 = 0l;
	private long m_time_120_above = 0l;
	
	private float total_fuel;
	private float idle_fuel;
	private float average_mpg;
	private float battery_volt_1;
	private float battery_volt_2;
	
	public StatisticsData() {
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
		this.traceLocation = traceLocation;
		this.m_time_idle = m_time_idle;
		this.m_time_night = m_time_night;
		this.m_time_speeding = m_time_speeding;
		this.m_time_rush_hour = m_time_rush_hour;
		this.m_time_0_to_60 = m_time_0_to_60;
		this.m_time_60_to_90 = m_time_60_to_90;
		this.m_time_90_to_120 = m_time_90_to_120;
		this.m_time_120_above = m_time_120_above;
		this.total_fuel = total_fuel;
		this.idle_fuel = idle_fuel;
		this.average_mpg = average_mpg;
		this.battery_volt_1 = battery_volt_1;
		this.battery_volt_2 = battery_volt_2;
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

		this.traceLocation = traceLocation;
		this.m_time_idle = idle;
		this.m_time_night = night;
		this.m_time_speeding = spd;
		this.m_time_rush_hour = rush;

		this.m_time_0_to_60 = ti_60;
		this.m_time_60_to_90 = ti_6090;
		this.m_time_90_to_120 = ti_90120;
		this.m_time_120_above = ti_120;
	}

	public Long getSid() {
		return Sid;
	}

	public void setSid(Long sid) {
		Sid = sid;
	}

	public int getDistance() {
		return distance;
	}

	public void setDistance(int distance) {
		this.distance = distance;
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

	public long get0to60() {
		return this.m_time_0_to_60;
	}

	public void set0to60(long ti) {
		this.m_time_0_to_60 = ti;
	}

	public long get60to90() {
		return this.m_time_60_to_90;
	}

	public void set60to90(long ti) {
		this.m_time_60_to_90 = ti;
	}

	public long get90to120() {
		return this.m_time_90_to_120;
	}

	public void set90to120(long ti) {
		this.m_time_90_to_120 = ti;
	}

	public long get120above() {
		return this.m_time_120_above;
	}

	public void set120above(long ti) {
		this.m_time_120_above = ti;
	}

	public long getIdleTime() {
		return m_time_idle;
	}

	public void setIdleTime(long time) {
		this.m_time_idle = time;
	}

	public long getNightTime() {
		return m_time_night;
	}

	public void setNightTime(long time) {
		this.m_time_night = time;
	}

	public long getSpeedingTime() {
		return m_time_speeding;
	}

	public void setSpeedingTime(long time) {
		this.m_time_speeding = time;
	}

	public long getRushTime() {
		return m_time_rush_hour;
	}

	public void setRushTime(long time) {
		this.m_time_rush_hour = time;
	}

	public String getTraceLocation() {
		return traceLocation;
	}

	public void setTraceLocation(String traceLocation) {
		this.traceLocation = traceLocation;
	}

	public long getM_time_idle() {
		return m_time_idle;
	}

	public void setM_time_idle(long m_time_idle) {
		this.m_time_idle = m_time_idle;
	}

	public long getM_time_night() {
		return m_time_night;
	}

	public void setM_time_night(long m_time_night) {
		this.m_time_night = m_time_night;
	}

	public long getM_time_speeding() {
		return m_time_speeding;
	}

	public void setM_time_speeding(long m_time_speeding) {
		this.m_time_speeding = m_time_speeding;
	}

	public long getM_time_rush_hour() {
		return m_time_rush_hour;
	}

	public void setM_time_rush_hour(long m_time_rush_hour) {
		this.m_time_rush_hour = m_time_rush_hour;
	}

	public long getM_time_0_to_60() {
		return m_time_0_to_60;
	}

	public void setM_time_0_to_60(long m_time_0_to_60) {
		this.m_time_0_to_60 = m_time_0_to_60;
	}

	public long getM_time_60_to_90() {
		return m_time_60_to_90;
	}

	public void setM_time_60_to_90(long m_time_60_to_90) {
		this.m_time_60_to_90 = m_time_60_to_90;
	}

	public long getM_time_90_to_120() {
		return m_time_90_to_120;
	}

	public void setM_time_90_to_120(long m_time_90_to_120) {
		this.m_time_90_to_120 = m_time_90_to_120;
	}

	public long getM_time_120_above() {
		return m_time_120_above;
	}

	public void setM_time_120_above(long m_time_120_above) {
		this.m_time_120_above = m_time_120_above;
	}

	public float getTotal_fuel() {
		return total_fuel;
	}

	public void setTotal_fuel(float total_fuel) {
		this.total_fuel = total_fuel;
	}

	public float getIdle_fuel() {
		return idle_fuel;
	}

	public void setIdle_fuel(float idle_fuel) {
		this.idle_fuel = idle_fuel;
	}

	public float getAverage_mpg() {
		return average_mpg;
	}

	public void setAverage_mpg(float average_mpg) {
		this.average_mpg = average_mpg;
	}

	public float getBattery_volt_1() {
		return battery_volt_1;
	}

	public void setBattery_volt_1(float battery_volt_1) {
		this.battery_volt_1 = battery_volt_1;
	}

	public float getBattery_volt_2() {
		return battery_volt_2;
	}

	public void setBattery_volt_2(float battery_volt_2) {
		this.battery_volt_2 = battery_volt_2;
	}

	public void setSid(long sid) {
		Sid = sid;
	}

	public String toString() {
		return Sid + ":" + userID + ":" + startTime + ":" + endTime + ":"
				+ averageSpeed + ":" + averageRotate + ":" + averageTemp;
	}

}
