package com.ctg.dbtrace;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by lee on 15/1/13.
 */
public class DateUtil {
	public static SimpleDateFormat sdf_up = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
    static public String DateToDateStr(Date date){
        SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sDateFormat.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
        return sDateFormat.format(date);
    }

    static public Date DateStrToDate(String DateStr){
        Date date=null;
        String pattern = "yyyy-MM-dd HH:mm:ss";
        SimpleDateFormat sDateFormat = new SimpleDateFormat(pattern);
        sDateFormat.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
        try {
            date = sDateFormat.parse(DateStr);
        }catch (Exception e){
            e.printStackTrace();
        }
        return date;
    }
    
    static public long StringToLong(String str){
    	
        String regex = "[^0-9]";  
        Pattern pattern = Pattern.compile(regex);  
        Matcher matcher = pattern.matcher(str);  
        String s = matcher.replaceAll(""); 
        long ret = Long.parseLong(s);
    	return ret;
    }
    
    static public String TimeInterval2Str(long ti){
    	long seconds = ti/1000;	// to sec
    	
    	long hours = seconds / 3600;
		long minutes = seconds % 3600 / 60;
		
		if(hours != 0l)
			return hours + " 小时 " + minutes + " 分钟 ";
		else 
			return minutes + " 分钟 ";
		
    }
    
	public static Timestamp StringToDate(String str) {
		Timestamp ts = new Timestamp(System.currentTimeMillis());
		try {
			ts = Timestamp.valueOf(str);
			// System.out.println("StringToDate:----------"+ts);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ts;
	}
}
