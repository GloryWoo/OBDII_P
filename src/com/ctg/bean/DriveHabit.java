package com.ctg.bean;

public class DriveHabit{
	private String where;
	private String foot;
	private String xxx;
	private String gas;
	private String time;
	private double[] points;
	//private Bitmap map;
	
	
	
	public String getWhere() {
		return where;
	}
	public double[] getPoints() {
		return points;
	}
	public void setPoints(double[] points) {
		this.points = points;
	}
	public DriveHabit(String where, String foot, String xxx, String gas,
			String time, double[] points) {
		super();
		this.where = where;
		this.foot = foot;
		this.xxx = xxx;
		this.gas = gas;
		this.time = time;
		this.points = points;
	}
	public void setWhere(String where) {
		this.where = where;
	}
	public String getFoot() {
		return foot;
	}
	public void setFoot(String foot) {
		this.foot = foot;
	}
	public String getXxx() {
		return xxx;
	}
	public void setXxx(String xxx) {
		this.xxx = xxx;
	}
	public String getGas() {
		return gas;
	}
	public void setGas(String gas) {
		this.gas = gas;
	}
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	
}
