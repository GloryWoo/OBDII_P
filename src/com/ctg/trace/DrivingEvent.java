package com.ctg.trace;

import java.io.Serializable;
import java.sql.Timestamp;


public class DrivingEvent implements Serializable {
	private static final long serialVersionUID = 1L;
	private int eventid;
	private String tripid;
	private int m_event_type;
	private Timestamp m_start_time;
	private Timestamp m_end_time;
	private double m_loc_lon;
	private double m_loc_lat;
	private String username;

	public DrivingEvent() {
	}
    
	
	public DrivingEvent(int eventid, String tripid, int m_event_type,
			Timestamp m_start_time, Timestamp m_end_time, double m_loc_lon,
			double m_loc_lat, String username) {
		super();
		this.eventid = eventid;
		this.tripid = tripid;
		this.m_event_type = m_event_type;
		this.m_start_time = m_start_time;
		this.m_end_time = m_end_time;
		this.m_loc_lon = m_loc_lon;
		this.m_loc_lat = m_loc_lat;
		this.username = username;
	}
	
	public DrivingEvent(String tripid, int m_event_type,
			Timestamp m_start_time, Timestamp m_end_time, double m_loc_lon,
			double m_loc_lat, String username) {
		super();
		this.tripid = tripid;
		this.m_event_type = m_event_type;
		this.m_start_time = m_start_time;
		this.m_end_time = m_end_time;
		this.m_loc_lon = m_loc_lon;
		this.m_loc_lat = m_loc_lat;
		this.username = username;
	}

	public DrivingEvent(int type, String start, String end, double lon,
			double lat) {
		this.m_event_type = type;
		//System.out.print("start:---"+start);
		this.m_start_time = DateUtil.StringToDate(start);
		this.m_end_time = DateUtil.StringToDate(end);
		this.m_loc_lon = lon;
		this.m_loc_lat = lat;
	}
	

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public int getM_event_type() {
		return m_event_type;
	}

	public void setM_event_type(int m_event_type) {
		this.m_event_type = m_event_type;
	}

	public Timestamp getM_start_time() {
		return m_start_time;
	}

	public void setM_start_time(Timestamp m_start_time) {
		this.m_start_time = m_start_time;
	}

	public Timestamp getM_end_time() {
		return m_end_time;
	}

	public void setM_end_time(Timestamp m_end_time) {
		this.m_end_time = m_end_time;
	}

	public double getM_loc_lon() {
		return m_loc_lon;
	}

	public void setM_loc_lon(double m_loc_lon) {
		this.m_loc_lon = m_loc_lon;
	}

	public double getM_loc_lat() {
		return m_loc_lat;
	}

	public void setM_loc_lat(double m_loc_lat) {
		this.m_loc_lat = m_loc_lat;
	}

	public int getEventid() {
		return eventid;
	}

	public void setEventid(int eventid) {
		this.eventid = eventid;
	}

	public String getTripid() {
		return tripid;
	}

	public void setTripid(String tripid) {
		this.tripid = tripid;
	}

}
