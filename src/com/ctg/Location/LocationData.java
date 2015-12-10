package com.ctg.Location;

import android.os.Parcel;
import android.os.Parcelable;

public class LocationData implements Parcelable{
	private double latitude;
	private double longitude;
	private String city;
	private String poiName;
	
	
	public LocationData() {
		super();
	}
	public LocationData(double latitude, double longitude,
			String city, String poiName) {
		super();
		this.latitude = latitude;
		this.longitude = longitude;
		this.city = city;
		this.poiName = poiName;
	}
	
	public double getLatitude() {
		return latitude;
	}
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}
	public double getLongitude() {
		return longitude;
	}
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public String getPoiName() {
		return poiName;
	}
	public void setPoiName(String poiName) {
		this.poiName = poiName;
	}
	public static Parcelable.Creator<LocationData> getCreator() {
		return CREATOR;
	}
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
		 // 写数据进行保存	    
		dest.writeDouble(latitude);
		dest.writeDouble(longitude);	
		dest.writeString(city);
		dest.writeString(poiName);	
	}
	
	// 用来创建自定义的Parcelable的对象
    public static final Parcelable.Creator<LocationData> CREATOR
            = new Parcelable.Creator<LocationData>() {
        public LocationData createFromParcel(Parcel in) {
            return new LocationData(in);
        }

        public LocationData[] newArray(int size) {
            return new LocationData[size];
        }
    };
    
    // 读数据进行恢复 读出与写入数据顺与一致
    private LocationData(Parcel in) {
    	latitude = in.readDouble();
    	longitude = in.readDouble();
    	city = in.readString();
    	poiName = in.readString();
    }
	

}
