package com.ctg.bean;

public class City{
	private String city_name;
	private String city_code;
	private String abbr;
	private int engine;//�Ƿ���Ҫ�������0,����Ҫ 1,��Ҫ
	private int  classa;//�Ƿ���Ҫ���ܺ�0,����Ҫ 1,��Ҫ
	private int regist;//�Ƿ���Ҫ�Ǽ�֤��0,����Ҫ 1,��Ҫ
	private int engineno;//��Ҫ������ź�λ 0Ϊȫ��
	private int  classno;//��Ҫ���ܺź�λ 0Ϊȫ��
	private int registno;//��Ҫ�Ǽ�֤���λ 0Ϊȫ��	
	private String car_head;	// e.g., ��B
	
	public City( ) {		
	}

	public String getCity_name() {
		return city_name;
	}

	public void setCity_name(String city_name) {
		this.city_name = city_name;
	}

	public String getCity_code() {
		return city_code;
	}

	public void setCity_code(String city_code) {
		this.city_code = city_code;
	}

	public String getAbbr() {
		return abbr;
	}

	public void setAbbr(String abbr) {
		this.abbr = abbr;
	}

	public int getEngine() {
		return engine;
	}

	public void setEngine(int engine) {
		this.engine = engine;
	}

	public int getClassa() {
		return classa;
	}

	public void setClassa(int classa) {
		this.classa = classa;
	}

	public int getRegist() {
		return regist;
	}

	public void setRegist(int regist) {
		this.regist = regist;
	}

	public int getEngineno() {
		return engineno;
	}

	public void setEngineno(int engineno) {
		this.engineno = engineno;
	}

	public int getClassno() {
		return classno;
	}

	public void setClassno(int classno) {
		this.classno = classno;
	}

	public int getRegistno() {
		return registno;
	}

	public void setRegistno(int registno) {
		this.registno = registno;
	}

	public City(String city_name, String city_code, String abbr, int engine,
			int classa, int regist, int engineno, int classno, int registno) {
		super();
		this.city_name = city_name;
		this.city_code = city_code;
		this.abbr = abbr;
		this.engine = engine;
		this.classa = classa;
		this.regist = regist;
		this.engineno = engineno;
		this.classno = classno;
		this.registno = registno;
	}

	public String getCarHead() {
		return car_head;
	}

	public void setCarHead(String car_head) {
		this.car_head = car_head;
	}
	

}
