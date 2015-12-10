package com.ctg.util;

import java.io.Serializable;

public class MyGeoFenceCont implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public String name;
	public double lon;
	public double lat;
	public String address;
	public int radius;
	public int eta;
	public String dura;
	public String desc;
	public boolean inFence;
	public boolean visible;
	
	public MyGeoFenceCont(String fencename, String addr, double longi, double lati, int rad, String duration){
		name = fencename;
		address = addr;
		lon = longi;
		lat = lati;
		radius = rad;
		dura = duration;
	}


	public MyGeoFenceCont() {
		// TODO Auto-generated constructor stub
	}
}
