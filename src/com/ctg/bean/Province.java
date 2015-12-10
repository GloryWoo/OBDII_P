package com.ctg.bean;

import java.util.List;

public class Province {
	private int province_id;
	private String province_name;	
	private String province_short_name;
	private List<City> citys;
	
	public Province( ) {		
	}
	
	public Province(int province_id, String province_name, String province_short_name, List<City> citys) {
		super();
		this.province_id = province_id;
		this.province_name = province_name;
		this.province_short_name = province_short_name;
		this.citys = citys;
	}

	public List<City> getCitys() {
		return citys;
	}

	public void setCitys(List<City> citys) {
		this.citys = citys;
	}

	public int getProvince_id() {
		return province_id;
	}

	public void setProvince_id(int province_id) {
		this.province_id = province_id;
	}

	public String getProvince_name() {
		return province_name;
	}

	public void setProvince_name(String province_name) {
		this.province_name = province_name;
	}

	public String getProvince_short_name() {
		return province_short_name;
	}

	public void setProvince_short_name(String province_short_name) {
		this.province_short_name = province_short_name;
	}


}
