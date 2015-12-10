package com.ctg.bean;

public class GPSData {
	private String currentTime;
	private double lat;
	private double lon;
	private float carSpeed;
	private int rotate;
	private float waterTemp;
	
	public GPSData( ) {		
	}
	
	public GPSData( String currentTime, double lat, double lon ) {
		super();
		this.currentTime = currentTime;
		this.lat = lat;
		this.lon = lon;
		
	}
	
	public GPSData(String currentTime, double lat, double lon, float carSpeed,
			int rotate, float waterTemp) {
		super();
		this.currentTime = currentTime;
		this.lat = lat;
		this.lon = lon;
		this.carSpeed = carSpeed;
		this.rotate = rotate;
		this.waterTemp = waterTemp;
	}

	public int getRotate() {
		return rotate;
	}

	public void setRotate(int rotate) {
		this.rotate = rotate;
	}

	

	public String getCurrentTime() {
		return currentTime;
	}

	public void setCurrentTime(String currentTime) {
		this.currentTime = currentTime;
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

	public float getCarSpeed() {
		return carSpeed;
	}

	public void setCarSpeed(float carSpeed) {
		this.carSpeed = carSpeed;
	}

	public float getWaterTemp() {
		return waterTemp;
	}

	public void setWaterTemp(float waterTemp) {
		this.waterTemp = waterTemp;
	}
}
