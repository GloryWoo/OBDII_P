package com.ctg.weather;  
  
/** 
 * 天气预报类，天气预报的基本属性。 
 * 
 * 
 */  
public class WeatherReport {  
  
    /** 
     * 城市（区县） 
     */  
    private String city;  
    /** 
     * 日期 
     */  
    private String date;  
    /** 
     * 星期几 
     */  
    private String weekDay;  
    /** 
     * 天气 
     */  
    private String weather;  
    /** 
     * 温度 
     */  
    private String temperature;  
    /** 
     * 风向 
     */  
    private String windDir;  
    /** 
     * 风力 
     */  
    private String wind;  
      
    /** 
     * 白天还是晚上 
     */  
    private String dayOrNight;  
  

    /** 
     * 获取天气预报的城市。 
     * @return 
     */  
    public String getCity() {  
        return city;  
    }  
  
    /** 
     * 设置天气预报的城市。 
     * @param city 
     */  
    public void setCity(String city) {  
        this.city = city;  
    }  
  
    /** 
     * 获取天气预报的日期，格式为"1月28日" 
     * @return 
     */  
    public String getDate() {  
        return date;  
    }  
  
    /** 
     * 设置天气预报的日期，格式为"1月28日" 
     * @param date 
     */  
    public void setDate(String date) {  
        this.date = date;  
    }  
  
    /** 
     * 获取天气预报的星期 
     * @return 
     */  
    public String getWeekDay() {  
        return weekDay;  
    }  
  
    /** 
     * 设置天气预报的星期 
     * @param weekDay 
     */  
    public void setWeekDay(String weekDay) {  
        this.weekDay = weekDay;  
    }  
  
    /** 
     * 获取天气 
     * @return 
     */  
    public String getWeather() {  
        return weather;  
    }  
  
    /** 
     * 设置天气 
     * @param weather 
     */  
    public void setWeather(String weather) {  
        this.weather = weather;  
    }  
  
    /** 
     * 获取温度 
     * @return 
     */  
    public String getTemperature() {  
        return temperature;  
    }  
  
    /** 
     * 设置温度 
     * @param temperature 
     */  
    public void setTemperature(String temperature) {  
        this.temperature = temperature;  
    }  
  
    /** 
     * 获取风向 
     * @return 
     */  
    public String getWindDir() {  
        return windDir;  
    }  
  
    /** 
     * 设置风向 
     * @param windDir 
     */  
    public void setWindDir(String windDir) {  
        this.windDir = windDir;  
    }  
  
    /** 
     * 获取风力 
     * @return 
     */  
    public String getWind() {  
        return wind;  
    }  
  
    /** 
     * 设置风力 
     * @param wind 
     */  
    public void setWind(String wind) {  
        this.wind = wind;  
    }  
  
    /** 
     * 获取天气预报是白天还是晚上 
     * @return 
     */  
    public String getDayOrNight() {  
        return dayOrNight;  
    }  
  
    /** 
     * 设置天气预报是白天还是晚上 
     * @param dayOrNight 
     */  
    public void setDayOrNight(String dayOrNight) {  
        this.dayOrNight = dayOrNight;  
    }  
  
    /** 
     * 天气预报的字符串 
     */  
    public String toString() {  
        return "WeatherReport [city=" + city + ", date=" + date + ", weekDay="  
                + weekDay + ", weather=" + weather + ", temperature="  
                + temperature + ", windDir=" + windDir + ", wind=" + wind  
                + ", dayOrNight=" + dayOrNight + "]";  
    }  
  
}  