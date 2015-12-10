package com.ctg.util;

import java.io.Serializable;

public class MyBDLocation implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public double lon;
	public double lat;
	public String address;
	public String name;
	
	public MyBDLocation(double lo, double la, String addr){
		address = addr;
		lon = lo;
		lat = la;
	}
	
	public MyBDLocation(){
		
	}
}
