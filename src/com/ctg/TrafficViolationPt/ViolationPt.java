package com.ctg.TrafficViolationPt;

import com.baidu.mapapi.model.LatLng;

/**
 * Created by ChaLi on 12/26/2014.
 */
public class ViolationPt{
    private int id;
    private LatLng pt = null;
    private int type;
    private String text;
    private int frequency;
    

    public int getFrequency() {
		return frequency;
	}

	public void setFrequency(int frequency) {
		this.frequency = frequency;
	}

	public void setPt(LatLng pt) {
		this.pt = pt;
	}

	public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public ViolationPt(double lat, double lon){
        pt = new LatLng(lat, lon);
    }

    public LatLng getPt() {
        return pt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

	@Override
	public boolean equals(Object o) {
		// TODO Auto-generated method stub
		if( o instanceof ViolationPt ){
			return id == ((ViolationPt)o).id; 
		}else{
			return false;
		}
		
	}

	
}
