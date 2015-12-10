package com.ctg.trace;

import java.io.Serializable;
import java.sql.Timestamp;

public class GPS implements Serializable{
	private static final long serialVersionUID = 5192304351812106964L;
	private transient String gpsID;
	//private transient PGpoint point;
	
	private String userID;
	private Timestamp createtime;
	private double lat;
	private double lon;
	private float  speed;
	private float rotate;
	private float temperature;
	private float accelerateSpeed;
	private float battery_volt;
	private float throttle_pos;
	private float engine_load;
	private float mpg_inst;
	private float mpg_aggr;
	private float fuel_level;
	private float diagnosis_codes;
	private boolean is_idle;
	private boolean obd_conn;
	
	
	/**
	 * Default constructor
	 */
	public GPS(){}
	
	public GPS(String userID, Timestamp createtime, double lat,
			double lon, float speed, float rotate, float temperature,
			float accelerateSpeed, float battery_volt, float throttle_pos,
			float engine_load, float mpg_inst, float mpg_aggr,
			float fuel_level, float diagnosis_codes, boolean is_idle,
			boolean obd_conn) {
		super();
		this.userID = userID;
		this.createtime = createtime;
		this.lat = lat;
		this.lon = lon;
		this.speed = speed;
		this.rotate = rotate;
		this.temperature = temperature;
		this.accelerateSpeed = accelerateSpeed;
		this.battery_volt = battery_volt;
		this.throttle_pos = throttle_pos;
		this.engine_load = engine_load;
		this.mpg_inst = mpg_inst;
		this.mpg_aggr = mpg_aggr;
		this.fuel_level = fuel_level;
		this.diagnosis_codes = diagnosis_codes;
		this.is_idle = is_idle;
		this.obd_conn = obd_conn;
	}

	/**
	 * Constructor with Longitude and Latitude
	 * @param deviceID
	 * @param lon
	 * @param lat
	 * @param timestamp
	 */
	public GPS(String userID,double lon, double lat,Timestamp timestamp,
			  float speed,float acceleratespeed){
		this.userID = userID;
		this.lon = lon;
		this.lat = lat;
		//this.point = new PGpoint(lon,lat);
		this.createtime = timestamp;
		this.speed=speed;
		this.accelerateSpeed=acceleratespeed;
	}
	
	public GPS(String userID, Timestamp createtime, double lat, double lon) {
		super();
		this.userID = userID;
		this.createtime = createtime;
		this.lat = lat;
		this.lon = lon;
	}
	//getter and setter
	public String getGpsID() {
		return gpsID;
	}
	public void setGpsID(String gpsID) {
		this.gpsID = gpsID;
	}
	public String getUserID() {
		return userID;
	}
	public void setUserID(String userID) {
		this.userID = userID;
	}
//	public PGpoint getPoint() {
//		if(point==null){
//			this.point = new PGpoint(this.lon,this.lat);
//		}
//		return point;
//	}
//	public void setPoint(PGpoint point) {
//		this.point = point;
//	}
	public Timestamp getCreatetime() {
		return createtime;
	}
	public void setCreatetime(Timestamp timestamp) {
		this.createtime = timestamp;
	}
	public double getLat() {
		return lat;
	}
	public void setLat(double lat) {
		this.lat = lat;
	}
	public double getLon() {
		return lon;
	}
	public void setLon(double lon) {
		this.lon = lon;
	}
	public float getAccelerateSpeed() {
		return accelerateSpeed;
	}
	public void setAccelerateSpeed(float accelerateSpeed) {
		this.accelerateSpeed = accelerateSpeed;
	}
	public float getSpeed() {
		return speed;
	}
	public void setSpeed(float speed) {
		this.speed = speed;
	}
	public float getRotate() {
		return rotate;
	}
	public void setRotate(float rotate) {
		this.rotate = rotate;
	}
	public float getTemperature() {
		return temperature;
	}
	public void setTemperature(float temperature) {
		this.temperature = temperature;
	}
	
	public float getBattery_volt() {
		return battery_volt;
	}
	public void setBattery_volt(float battery_volt) {
		this.battery_volt = battery_volt;
	}
	public float getThrottle_pos() {
		return throttle_pos;
	}
	public void setThrottle_pos(float throttle_pos) {
		this.throttle_pos = throttle_pos;
	}
	public float getEngine_load() {
		return engine_load;
	}
	public void setEngine_load(float engine_load) {
		this.engine_load = engine_load;
	}
	public float getMpg_inst() {
		return mpg_inst;
	}
	public void setMpg_inst(float mpg_inst) {
		this.mpg_inst = mpg_inst;
	}
	public float getMpg_aggr() {
		return mpg_aggr;
	}
	public void setMpg_aggr(float mpg_aggr) {
		this.mpg_aggr = mpg_aggr;
	}
	public float getFuel_level() {
		return fuel_level;
	}
	public void setFuel_level(float fuel_level) {
		this.fuel_level = fuel_level;
	}
	public float getDiagnosis_codes() {
		return diagnosis_codes;
	}
	public void setDiagnosis_codes(float diagnosis_codes) {
		this.diagnosis_codes = diagnosis_codes;
	}
	public boolean isIdle() {
		return is_idle;
	}
	public void setIdle(boolean is_idle) {
		this.is_idle = is_idle;
	}
	public boolean isObd_conn() {
		return obd_conn;
	}
	public void setObd_conn(boolean obd_conn) {
		this.obd_conn = obd_conn;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	public String toString() {
        return "lat=" + lat + ", lon=" + lon+",createtime="+createtime;
   }
	
}
