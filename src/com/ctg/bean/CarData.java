package com.ctg.bean;

public class CarData {
	private String currentTime;
	private double lat;
	private double lon;
	private float bat;
	private int rpm;
	private float vss;
	private float throtPos;
	private float engineLoad;
	private float temp;
	private float mpg;
	private float avg_mpg;
	private float fli;
	private int dtc;
	
	private boolean isIdle;
	private boolean hasOBDData;
	
	// 未连接OBD数据
	public CarData( String currentTime, double lat, double lon ) {
		super();
		this.setCurrentTime(currentTime);
		this.setLat(lat);
		this.setLon(lon);
	}
	
	// iEST327
	public CarData( String currentTime, double lat, double lon, float vss, int rpm, float temp) {
		super();
		this.setCurrentTime(currentTime);
		this.setLat(lat);
		this.setLon(lon);
		this.setVSS(vss);
		this.setRPM(rpm);
		this.setTemp(temp);
	}
	
	// iEST527
	public CarData(String currentTime, double lat, double lon, float bat, int rpm, float vss, float throt, float load,
					float temp, float mpg, float avg_mpg, float fli, int dtc) {
		super();
		this.setCurrentTime(currentTime);
		this.setLat(lat);
		this.setLon(lon);
		this.setBat(bat);
		this.setVSS(vss);
		this.setRPM(rpm);
		this.setThrotPos(throt);
		this.setEngineLoad(load);
		this.setTemp(temp);
		this.setMPG(mpg);
		this.setAvg_mpg(avg_mpg);
		this.setRemainingFuel(fli);
		this.setDTC(dtc);		
	}

	public void setVSS(float vss) {
		this.vss = vss;
	}

	public boolean isIdle() {
		return isIdle;
	}

	public void setIdle(boolean isIdle) {
		this.isIdle = isIdle;
	}

	public boolean getHasOBDData() {
		return hasOBDData;
	}

	public void setHasOBDData(boolean hasOBDData) {
		this.hasOBDData = hasOBDData;
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

	public float getBat() {
		return bat;
	}

	public void setBat(float bat) {
		this.bat = bat;
	}

	public int getRPM() {
		return rpm;
	}

	public void setRPM(int rpm) {
		this.rpm = rpm;
	}

	public float getVSS() {
		return vss;
	}

	public float getThrotPos() {
		return throtPos;
	}

	public void setThrotPos(float throtPos) {
		this.throtPos = throtPos;
	}

	public float getEngineLoad() {
		return engineLoad;
	}

	public void setEngineLoad(float engineLoad) {
		this.engineLoad = engineLoad;
	}

	public float getTemp() {
		return temp;
	}

	public void setTemp(float temp) {
		this.temp = temp;
	}

	public float getMPG() {
		return mpg;
	}

	public void setMPG(float mpg) {
		this.mpg = mpg;
	}

	public float getAvg_mpg() {
		return avg_mpg;
	}

	public void setAvg_mpg(float avg_mpg) {
		this.avg_mpg = avg_mpg;
	}

	public float getRemainingFuel() {
		return fli;
	}

	public void setRemainingFuel(float fli) {
		this.fli = fli;
	}

	public int getDTC() {
		return dtc;
	}

	public void setDTC(int dtc) {
		this.dtc = dtc;
	}



}
