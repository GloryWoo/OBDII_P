package com.ctg.util;

import java.util.Date;

import com.baidu.mapapi.model.LatLng;

import android.content.Context;
import android.content.SharedPreferences;

public class Preference {
		// For debugging
		private static final String TAG = "Preference";

		// Preferences name
		public static final String pp_PREFERENCE = "pp_preference";

		public final static String KEY_LOGINSTAT = "longin_stat";
		public final static String KEY_SESSIONID = "session_id";
		public final static String KEY_DB_SESSIONID = "db_session_id";
		public final static String KEY_USER = "user";
		public final static String KEY_NICKNAME = "nickname";
		public final static String KEY_USERPASSWD = "userpasswd";		
		public final static String LICENCE = "licence_plate";
		public final static String CAR_MAKER = "car_maker";
		public final static String CAR_TYPE = "car_type";
		public final static String KEY_LOCALE = "set_locale";
		public final static String AUTO_LOGIN = "auto_login";
		public final static String USER_PWD = "user_password";
		public final static String SAVE_USER = "save_user";
		public final static String SAVE_USER_PWD = "save_user_password";
		public final static String BCALL_NUM = "bcall_num";
		public final static String ECALL_NUM = "ecall_num";
		public final static String VERSION = "version";
		public final static String VOLTAGE = "voltage";
		public final static String PURCHASE_DATE = "purchase_date";
		public final static String GPS_MONITOR = "gps_monitor";
		public final static String HOME_KEY_LOCATION = "home_key_location";
		public final static String VOL_EXPIRE = "vol_expire";

		public final static String LICENCE_NO = "lincence_no";
		public final static String CLASS_NO = "class_no";
		public final static String ENGINE_NO = "engine_no";
		public final static String REGIST_NO = "regist_no";
		
		
		public final static String CLASS_NUM = "class_num";
		public final static String ENGINE_NUM = "engine_num";
		public final static String REGIST_NUM = "regist_num";
		
		public final static String LICIENCE_CITY = "licence_city";	
		
		public final static String LAST_LATITUDE = "last_latitude";
		public final static String LAST_LONGITUDE = "last_longitude";

        public final static String KEY_SHARE_POS = "share_pos";
        
        public final static String KEY_POINT_LAT = "key_point_lat";
        public final static String KEY_POINT_LON = "key_point_lon";
        
        public final static String KEY_NAVI_POINT_LAT_H = "home_navi_poi_lat";
        public final static String KEY_NAVI_POINT_LON_H = "home_navi_poi_lon";
        public final static String KEY_NAVI_POINT_ADDR_H = "home_navi_poi_addr";
        
        public final static String KEY_NAVI_POINT_LAT_C = "cmpny_navi_poi_lat";
        public final static String KEY_NAVI_POINT_LON_C = "cmpny_navi_poi_lon";
        public final static String KEY_NAVI_POINT_ADDR_C = "cmpny_navi_poi_addr";
        
        public final static String KEY_LAST_TRACE_ID = "last_trace_id";
        public final static String KEY_LAST_QUIT_TIME = "last_app_quit_time";
		/** Instance */
		private static Preference m_Instance;

		/** Context application */
		private Context m_Context;


		private Preference(Context context) {
			this.m_Context = context;
		}

		public synchronized static Preference getInstance(Context context) {
			if (m_Instance == null) {

				m_Instance = new Preference(context);
			}

			return m_Instance;
		}

		public String getLocale() {
			SharedPreferences preferences = m_Context.getSharedPreferences(
					pp_PREFERENCE, Context.MODE_PRIVATE);
			return preferences.getString(KEY_LOCALE, "zh");

		}
		
		public boolean getLoginStat(){
			SharedPreferences preferences = m_Context.getSharedPreferences(
					pp_PREFERENCE, Context.MODE_PRIVATE);
			return preferences.getBoolean(KEY_LOGINSTAT, false);
		}
		
		public void setLoginStat(boolean stat) {
			SharedPreferences preferences = m_Context.getSharedPreferences(
					pp_PREFERENCE, Context.MODE_PRIVATE);
			SharedPreferences.Editor settingEditor = preferences.edit();
			settingEditor.putBoolean(KEY_LOGINSTAT, stat);
			settingEditor.commit();
		}
		
		public String getSessionId(){
			SharedPreferences preferences = m_Context.getSharedPreferences(
					pp_PREFERENCE, Context.MODE_PRIVATE);
			return preferences.getString(KEY_SESSIONID, "1234567890");
		}

		public void setSessionId(String sessionid){
			SharedPreferences preferences = m_Context.getSharedPreferences(
					pp_PREFERENCE, Context.MODE_PRIVATE);
			SharedPreferences.Editor settingEditor = preferences.edit();
			settingEditor.putString(KEY_SESSIONID, sessionid);
			settingEditor.commit();
		}

		public String getDBSessionId(){
			SharedPreferences preferences = m_Context.getSharedPreferences(
					pp_PREFERENCE, Context.MODE_PRIVATE);
			return preferences.getString(KEY_DB_SESSIONID, "1234567890");
		}

		public void setDBSessionId(String sessionid){
			SharedPreferences preferences = m_Context.getSharedPreferences(
					pp_PREFERENCE, Context.MODE_PRIVATE);
			SharedPreferences.Editor settingEditor = preferences.edit();
			settingEditor.putString(KEY_DB_SESSIONID, sessionid);
			settingEditor.commit();
		}
		
		public void setUser(String user){
			SharedPreferences preferences = m_Context.getSharedPreferences(
					pp_PREFERENCE, Context.MODE_PRIVATE);
			SharedPreferences.Editor settingEditor = preferences.edit();
			settingEditor.putString(KEY_USER, user);
			settingEditor.commit();
		}

		public String getUser(){
			SharedPreferences preferences = m_Context.getSharedPreferences(
					pp_PREFERENCE, Context.MODE_PRIVATE);
			return preferences.getString(KEY_USER, "");
		}
		
		public void setNickname(String nick){
			SharedPreferences preferences = m_Context.getSharedPreferences(
					pp_PREFERENCE, Context.MODE_PRIVATE);
			SharedPreferences.Editor settingEditor = preferences.edit();
			settingEditor.putString(KEY_NICKNAME, nick);
			settingEditor.commit();
		}

		public String getNickname(){
			SharedPreferences preferences = m_Context.getSharedPreferences(
					pp_PREFERENCE, Context.MODE_PRIVATE);
			return preferences.getString(KEY_NICKNAME, "");
		}
		
		public void setUserPasswd(String passwd){
			SharedPreferences preferences = m_Context.getSharedPreferences(
					pp_PREFERENCE, Context.MODE_PRIVATE);
			SharedPreferences.Editor settingEditor = preferences.edit();
			settingEditor.putString(KEY_USERPASSWD, passwd);
			settingEditor.commit();
		}

		public String getUserPasswd(){
			SharedPreferences preferences = m_Context.getSharedPreferences(
					pp_PREFERENCE, Context.MODE_PRIVATE);
			return preferences.getString(KEY_USERPASSWD, "Harman1234");
		}
		
		public void setLicence(String licence){
			SharedPreferences preferences = m_Context.getSharedPreferences(
					pp_PREFERENCE, Context.MODE_PRIVATE);
			SharedPreferences.Editor settingEditor = preferences.edit();
			settingEditor.putString(LICENCE, licence);
			settingEditor.commit();
		}

		public String getLicence(){
			SharedPreferences preferences = m_Context.getSharedPreferences(
					pp_PREFERENCE, Context.MODE_PRIVATE);
			return preferences.getString(LICENCE, "沪A80237");
		}
		
		public void setCarMaker(String maker){
			SharedPreferences preferences = m_Context.getSharedPreferences(
					pp_PREFERENCE, Context.MODE_PRIVATE);
			SharedPreferences.Editor settingEditor = preferences.edit();
			settingEditor.putString(CAR_MAKER, maker);
			settingEditor.commit();
		}

		public String getCarMaker(){
			SharedPreferences preferences = m_Context.getSharedPreferences(
					pp_PREFERENCE, Context.MODE_PRIVATE);
			return preferences.getString(CAR_MAKER, m_Context.getResources().getString(com.ctg.ui.R.string.def_car_maker));
		}
		
		public void setCarType(String type){
			SharedPreferences preferences = m_Context.getSharedPreferences(
					pp_PREFERENCE, Context.MODE_PRIVATE);
			SharedPreferences.Editor settingEditor = preferences.edit();
			settingEditor.putString(CAR_TYPE, type);
			settingEditor.commit();
		}

		public String getCarType(){
			SharedPreferences preferences = m_Context.getSharedPreferences(
					pp_PREFERENCE, Context.MODE_PRIVATE);
			return preferences.getString(CAR_TYPE, m_Context.getResources().getString(com.ctg.ui.R.string.def_car_type));
		}

		public void setAutoConnect(boolean auto){
			SharedPreferences preferences = m_Context.getSharedPreferences(
					pp_PREFERENCE, Context.MODE_PRIVATE);
			SharedPreferences.Editor settingEditor = preferences.edit();
			settingEditor.putBoolean(AUTO_LOGIN, auto);
			settingEditor.commit();
		}

		public boolean getAutoConnect(){
			SharedPreferences preferences = m_Context.getSharedPreferences(
					pp_PREFERENCE, Context.MODE_PRIVATE);
			return preferences.getBoolean(AUTO_LOGIN, false);
		}
		
		public void setSaveUser(boolean save){
			SharedPreferences preferences = m_Context.getSharedPreferences(
					pp_PREFERENCE, Context.MODE_PRIVATE);
			SharedPreferences.Editor settingEditor = preferences.edit();
			settingEditor.putBoolean(SAVE_USER, save);			
			settingEditor.commit();
		}

		public boolean getSaveUser(){
			SharedPreferences preferences = m_Context.getSharedPreferences(
					pp_PREFERENCE, Context.MODE_PRIVATE);
			return preferences.getBoolean(SAVE_USER, false);
		}
		
		public void setSaveUserPwd(boolean save){
			SharedPreferences preferences = m_Context.getSharedPreferences(
					pp_PREFERENCE, Context.MODE_PRIVATE);
			SharedPreferences.Editor settingEditor = preferences.edit();
			settingEditor.putBoolean(SAVE_USER_PWD, save);
			settingEditor.commit();
		}

		public boolean getSaveUserPwd(){
			SharedPreferences preferences = m_Context.getSharedPreferences(
					pp_PREFERENCE, Context.MODE_PRIVATE);
			return preferences.getBoolean(SAVE_USER_PWD, false);
		}
		
		
		public void setBcall(String bcall){
			SharedPreferences preferences = m_Context.getSharedPreferences(
					pp_PREFERENCE, Context.MODE_PRIVATE);
			SharedPreferences.Editor settingEditor = preferences.edit();
			settingEditor.putString(BCALL_NUM, bcall);
			settingEditor.commit();
		}

		public String getBcall(){
			SharedPreferences preferences = m_Context.getSharedPreferences(
					pp_PREFERENCE, Context.MODE_PRIVATE);
			return preferences.getString(BCALL_NUM, "10086");
		}
		
		public void setEcall(String ecall){
			SharedPreferences preferences = m_Context.getSharedPreferences(
					pp_PREFERENCE, Context.MODE_PRIVATE);
			SharedPreferences.Editor settingEditor = preferences.edit();
			settingEditor.putString(ECALL_NUM, ecall);
			settingEditor.commit();
		}

		public String getEcall(){
			SharedPreferences preferences = m_Context.getSharedPreferences(
					pp_PREFERENCE, Context.MODE_PRIVATE);
			return preferences.getString(ECALL_NUM, "10086");
		}
		
		
//		public void setVersion(String ver){
//			SharedPreferences preferences = m_Context.getSharedPreferences(
//					pp_PREFERENCE, Context.MODE_PRIVATE);
//			SharedPreferences.Editor settingEditor = preferences.edit();
//			settingEditor.putString(VERSION, ver);
//			settingEditor.commit();
//		}
//
//		public String getVersion(){
//			SharedPreferences preferences = m_Context.getSharedPreferences(
//					pp_PREFERENCE, Context.MODE_PRIVATE);
//			return preferences.getString(VERSION, "1.01");
//		}
		
		public void setVoltage(String vol){
			SharedPreferences preferences = m_Context.getSharedPreferences(
					pp_PREFERENCE, Context.MODE_PRIVATE);
			SharedPreferences.Editor settingEditor = preferences.edit();
			settingEditor.putString(VOLTAGE, vol);
			settingEditor.commit();
		}

		public String getVoltage(){
			SharedPreferences preferences = m_Context.getSharedPreferences(
					pp_PREFERENCE, Context.MODE_PRIVATE);
			return preferences.getString(VOLTAGE, "13");
		}
		
		public void setPurchaseDate(String date){
			SharedPreferences preferences = m_Context.getSharedPreferences(
					pp_PREFERENCE, Context.MODE_PRIVATE);
			SharedPreferences.Editor settingEditor = preferences.edit();
			settingEditor.putString(PURCHASE_DATE, date);
			settingEditor.commit();
		}

		public String getPurchaseDate(){
			SharedPreferences preferences = m_Context.getSharedPreferences(
					pp_PREFERENCE, Context.MODE_PRIVATE);
			return preferences.getString(PURCHASE_DATE, "2014-7-22");
		}
		
		public void setGpsMonitor(boolean enable){
			SharedPreferences preferences = m_Context.getSharedPreferences(
					pp_PREFERENCE, Context.MODE_PRIVATE);
			SharedPreferences.Editor settingEditor = preferences.edit();
			settingEditor.putBoolean(GPS_MONITOR, enable);
			settingEditor.commit();
		}

		public boolean getGpsMonitor(){
			SharedPreferences preferences = m_Context.getSharedPreferences(
					pp_PREFERENCE, Context.MODE_PRIVATE);
			return preferences.getBoolean(GPS_MONITOR, false);
		}
		
		public void setHomeLoc(int loc){
			SharedPreferences preferences = m_Context.getSharedPreferences(
					pp_PREFERENCE, Context.MODE_PRIVATE);
			SharedPreferences.Editor settingEditor = preferences.edit();
			settingEditor.putInt(HOME_KEY_LOCATION, loc);
			settingEditor.commit();
		}

		public int getHomeLoc(){
			SharedPreferences preferences = m_Context.getSharedPreferences(
					pp_PREFERENCE, Context.MODE_PRIVATE);
			return preferences.getInt(HOME_KEY_LOCATION, 0);	
		}
		
		public void setVolExp(long vol){
			SharedPreferences preferences = m_Context.getSharedPreferences(
					pp_PREFERENCE, Context.MODE_PRIVATE);
			SharedPreferences.Editor settingEditor = preferences.edit();
			settingEditor.putLong(VOL_EXPIRE, vol);
			settingEditor.commit();
		}

		public long getVolExp(){
			SharedPreferences preferences = m_Context.getSharedPreferences(
					pp_PREFERENCE, Context.MODE_PRIVATE);
			return preferences.getLong(VOL_EXPIRE, 0);	
		}
		
		public void setRegistNum(int registNum){
			SharedPreferences preferences = m_Context.getSharedPreferences(
					pp_PREFERENCE, Context.MODE_PRIVATE);
			SharedPreferences.Editor settingEditor = preferences.edit();
			settingEditor.putInt(REGIST_NUM, registNum);
			settingEditor.commit();
		}

		public int getRegistNum(){
			SharedPreferences preferences = m_Context.getSharedPreferences(
					pp_PREFERENCE, Context.MODE_PRIVATE);
			return preferences.getInt(REGIST_NUM, 100);	
		}
		
		public void setClassNum(int classNum){
			SharedPreferences preferences = m_Context.getSharedPreferences(
					pp_PREFERENCE, Context.MODE_PRIVATE);
			SharedPreferences.Editor settingEditor = preferences.edit();
			settingEditor.putInt(CLASS_NUM, classNum);
			settingEditor.commit();
		}

		public int getClassNum(){
			SharedPreferences preferences = m_Context.getSharedPreferences(
					pp_PREFERENCE, Context.MODE_PRIVATE);
			return preferences.getInt(CLASS_NUM, 100);	
		}
		
		public void setEngineNum(int engineNum){
			SharedPreferences preferences = m_Context.getSharedPreferences(
					pp_PREFERENCE, Context.MODE_PRIVATE);
			SharedPreferences.Editor settingEditor = preferences.edit();
			settingEditor.putInt(ENGINE_NUM, engineNum);
			settingEditor.commit();
		}

		public int getEngineNum(){
			SharedPreferences preferences = m_Context.getSharedPreferences(
					pp_PREFERENCE, Context.MODE_PRIVATE);
			return preferences.getInt(ENGINE_NUM, 100);	
		}
		
		public void setLicenceNo(String lic){
			SharedPreferences preferences = m_Context.getSharedPreferences(
					pp_PREFERENCE, Context.MODE_PRIVATE);
			SharedPreferences.Editor settingEditor = preferences.edit();
			settingEditor.putString(LICENCE_NO, lic);
			settingEditor.commit();
		}

		public String getLicenceNo(){
			SharedPreferences preferences = m_Context.getSharedPreferences(
					pp_PREFERENCE, Context.MODE_PRIVATE);
			return preferences.getString(LICENCE_NO, "");	
		}
		
		public void setRegistNo(String registNo){
			SharedPreferences preferences = m_Context.getSharedPreferences(
					pp_PREFERENCE, Context.MODE_PRIVATE);
			SharedPreferences.Editor settingEditor = preferences.edit();
			settingEditor.putString(REGIST_NO, registNo);
			settingEditor.commit();
		}

		public String getRegistNo(){
			SharedPreferences preferences = m_Context.getSharedPreferences(
					pp_PREFERENCE, Context.MODE_PRIVATE);
			return preferences.getString(REGIST_NO, "");	
		}
		
		public void setClassNo(String lic){
			SharedPreferences preferences = m_Context.getSharedPreferences(
					pp_PREFERENCE, Context.MODE_PRIVATE);
			SharedPreferences.Editor settingEditor = preferences.edit();
			settingEditor.putString(CLASS_NO, lic);
			settingEditor.commit();
		}

		public String getClassNo(){
			SharedPreferences preferences = m_Context.getSharedPreferences(
					pp_PREFERENCE, Context.MODE_PRIVATE);
			return preferences.getString(CLASS_NO, "");	
		}
		
		public void setEngineNo(String class_no){
			SharedPreferences preferences = m_Context.getSharedPreferences(
					pp_PREFERENCE, Context.MODE_PRIVATE);
			SharedPreferences.Editor settingEditor = preferences.edit();
			settingEditor.putString(ENGINE_NO, class_no);
			settingEditor.commit();
		}

		public String getEngineNo(){
			SharedPreferences preferences = m_Context.getSharedPreferences(
					pp_PREFERENCE, Context.MODE_PRIVATE);
			return preferences.getString(ENGINE_NO, "");	
		}
		
		public void setLicenceCity(String class_no){
			SharedPreferences preferences = m_Context.getSharedPreferences(
					pp_PREFERENCE, Context.MODE_PRIVATE);
			SharedPreferences.Editor settingEditor = preferences.edit();
			settingEditor.putString(LICIENCE_CITY, class_no);
			settingEditor.commit();
		}

		public String getLicenceCity(){
			SharedPreferences preferences = m_Context.getSharedPreferences(
					pp_PREFERENCE, Context.MODE_PRIVATE);
			return preferences.getString(LICIENCE_CITY, "沪");	
		}
		
		public void setLastLat(double lat){
			SharedPreferences preferences = m_Context.getSharedPreferences(
					pp_PREFERENCE, Context.MODE_PRIVATE);
			SharedPreferences.Editor settingEditor = preferences.edit();
			settingEditor.putLong(LAST_LATITUDE, Double.doubleToLongBits(lat));
			settingEditor.commit();
		}

		public double getLastLat(){
			SharedPreferences preferences = m_Context.getSharedPreferences(
					pp_PREFERENCE, Context.MODE_PRIVATE);
			long latL = preferences.getLong(LAST_LATITUDE, 0);
			return Double.longBitsToDouble(latL);	
		}
		
		public void setLastLon(double lon){
			SharedPreferences preferences = m_Context.getSharedPreferences(
					pp_PREFERENCE, Context.MODE_PRIVATE);
			SharedPreferences.Editor settingEditor = preferences.edit();
			settingEditor.putLong(LAST_LONGITUDE, Double.doubleToLongBits(lon));
			settingEditor.commit();
		}

		public double getLastLon(){
			SharedPreferences preferences = m_Context.getSharedPreferences(
					pp_PREFERENCE, Context.MODE_PRIVATE);
			long lonL = preferences.getLong(LAST_LONGITUDE, 0);
			return Double.longBitsToDouble(lonL);	
		}

        public void setSharePos(boolean bSharePos){
            SharedPreferences preferences = m_Context.getSharedPreferences(
                    pp_PREFERENCE, Context.MODE_PRIVATE);
            SharedPreferences.Editor settingEditor = preferences.edit();
            settingEditor.putBoolean(KEY_SHARE_POS, bSharePos);
            settingEditor.commit();
        }

        public boolean getSharePos(){
            SharedPreferences preferences = m_Context.getSharedPreferences(
                    pp_PREFERENCE, Context.MODE_PRIVATE);
            return preferences.getBoolean(KEY_SHARE_POS, false);
        }
        
        public void setNaviPointHome(NavitPoint pt){
            SharedPreferences preferences = m_Context.getSharedPreferences(
                    pp_PREFERENCE, Context.MODE_PRIVATE);
            SharedPreferences.Editor settingEditor = preferences.edit();
            settingEditor.putLong(KEY_NAVI_POINT_LAT_H, Double.doubleToLongBits(pt.lat));
            settingEditor.putLong(KEY_NAVI_POINT_LON_H, Double.doubleToLongBits(pt.lon));
            settingEditor.putString(KEY_NAVI_POINT_ADDR_H, pt.addr);  
            settingEditor.commit();
        }
		
        public NavitPoint getNaviPointHome(){
        	double lat, lon;
        	String addr;
            SharedPreferences preferences = m_Context.getSharedPreferences(
                    pp_PREFERENCE, Context.MODE_PRIVATE);
            lat = Double.longBitsToDouble(preferences.getLong(KEY_NAVI_POINT_LAT_H, 0L));
            lon = Double.longBitsToDouble(preferences.getLong(KEY_NAVI_POINT_LON_H, 0L));
            addr = preferences.getString(KEY_NAVI_POINT_ADDR_H, "");
        	return new NavitPoint(lat, lon, addr);
        }
        
        public void setNaviPointCmpy(NavitPoint pt){
            SharedPreferences preferences = m_Context.getSharedPreferences(
                    pp_PREFERENCE, Context.MODE_PRIVATE);
            SharedPreferences.Editor settingEditor = preferences.edit();
            settingEditor.putLong(KEY_NAVI_POINT_LAT_C, Double.doubleToLongBits(pt.lat));
            settingEditor.putLong(KEY_NAVI_POINT_LON_C, Double.doubleToLongBits(pt.lon));
            settingEditor.putString(KEY_NAVI_POINT_ADDR_C, pt.addr);  
            settingEditor.commit();
        }
		
        public NavitPoint getNaviPointCmpy(){
        	double lat, lon;
        	String addr;
            SharedPreferences preferences = m_Context.getSharedPreferences(
                    pp_PREFERENCE, Context.MODE_PRIVATE);
            lat = Double.longBitsToDouble(preferences.getLong(KEY_NAVI_POINT_LAT_C, 0L));
            lon = Double.longBitsToDouble(preferences.getLong(KEY_NAVI_POINT_LON_C, 0L));
            addr = preferences.getString(KEY_NAVI_POINT_ADDR_C, "");
        	return new NavitPoint(lat, lon, addr);
        }
        
        public void setPointLatLng(LatLng latlon){
            SharedPreferences preferences = m_Context.getSharedPreferences(
                    pp_PREFERENCE, Context.MODE_PRIVATE);
            SharedPreferences.Editor settingEditor = preferences.edit();
            settingEditor.putLong(KEY_POINT_LAT, Double.doubleToLongBits(latlon.latitude));
            settingEditor.putLong(KEY_POINT_LON, Double.doubleToLongBits(latlon.longitude));
            settingEditor.commit();
        }
		
        public LatLng getPointLatLng(){
        	double lat, lon;
        	String addr;
            SharedPreferences preferences = m_Context.getSharedPreferences(
                    pp_PREFERENCE, Context.MODE_PRIVATE);
            lat = Double.longBitsToDouble(preferences.getLong(KEY_POINT_LAT, 0L));
            lon = Double.longBitsToDouble(preferences.getLong(KEY_POINT_LON, 0L));            
        	return new LatLng(lat, lon);
        }
        
        public void setTraceID(long tid){
        	SharedPreferences preferences = m_Context.getSharedPreferences(
                    pp_PREFERENCE, Context.MODE_PRIVATE);
        	SharedPreferences.Editor settingEditor = preferences.edit();
        	settingEditor.putLong(KEY_LAST_TRACE_ID, tid);
        	settingEditor.commit();
        }
        
        public long getTraceID(){
            SharedPreferences preferences = m_Context.getSharedPreferences(
                    pp_PREFERENCE, Context.MODE_PRIVATE);
            return preferences.getLong(KEY_LAST_TRACE_ID, 0l);
        }
        
        public void setLastQuitTime(){
        	SharedPreferences preferences = m_Context.getSharedPreferences(pp_PREFERENCE, Context.MODE_PRIVATE);
        	SharedPreferences.Editor settingEditor = preferences.edit();
        	settingEditor.putLong(KEY_LAST_QUIT_TIME, new Date(System.currentTimeMillis()).getTime());
        	settingEditor.commit();
        }
        
        public long getLastQuitTime(){
            SharedPreferences preferences = m_Context.getSharedPreferences(
                    pp_PREFERENCE, Context.MODE_PRIVATE);
            return preferences.getLong(KEY_LAST_QUIT_TIME, 0l);
        }
}
