package com.ctg.weather;  
  
import java.io.File;  
import java.io.InputStream;
import java.util.ArrayList;  
import java.util.Calendar;  
import java.util.HashMap;
import java.util.Hashtable;  
import java.util.List;  
import java.util.Map;
import java.util.regex.Matcher;  
import java.util.regex.Pattern;  
  
import javax.xml.parsers.DocumentBuilderFactory;  
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
  

import org.json.JSONObject;
import org.w3c.dom.Document;  
import org.w3c.dom.Element;  
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;  

import com.ctg.ui.Base;

import android.content.Context;
  
import java.io.BufferedReader; 

import java.io.InputStreamReader; 

import java.net.URL; 
import java.net.URLConnection; 


/** 
 * 中国气象频道手机版("http://m.weathercn.com")获取天气信息， 因为手机版网页内容小，访问速度快。 
 *  
 *  
 *  
 */  
public class WeatherUtil {  
  
    /** 
     * 省份页面（省） 
     */  
    public static final String PROVINCE_URL = "http://m.weathercn.com/common/province.jsp";  
    /** 
     * 城市页面（市）<br/> 
     * pid=%s中%s表示城市编号 例：http://m.weathercn.com/common/dis.do?pid=010101 
     */  
    public static final String DISTRICT_URL = "http://m.weathercn.com/common/dis.do?pid=%s";  
    /** 
     * 县区页面（区）<br/> 
     * did=%s中%s表示县区编号<br/> 
     * pid=%s中%s表示城市编号<br/> 
     * 例：http://m.weathercn.com/common/cout.do?did=01010101&pid=010101 
     */  
    public static final String COUNTY_URL = "http://m.weathercn.com/common/cout.do?did=%s&pid=%s";  
    /** 
     * 7天天气预报页面<br/> 
     * cid=%s中%s表示区编号<br/> 
     * 例：http://m.weathercn.com/common/7d.do?cid=0101010110 
     */  
    public static final String REPORT7_URL = "http://m.weathercn.com/common/7d.do?cid=%s";
    
    
    public static final String REPORTPM25_URL = "http://www.pm25x.com/city/%s.htm";
    /*今日详情*/
    public static final String REPORT_DETAIL_URL = "http://m.weathercn.com/jinri.do?cid=%s";
    /** 
     * 生活指数页面<br/> 
     * cid=%s中%s表示区编号 
     */  
    public static final String REPORT_MORE_URL = "http://m.weathercn.com/common/zslb.do?cid=%s";  
  

    /** 
     * 保存城市编码信息的xml文档<br/> 
     * 保存了具体的区县和区县所对应的编码，例如<br/> 
     * <county><br/> 
     * <name>北京</name><br/> 
     * <code>0101010110</code><br/> 
     * </county> 
     */  
    public static final String XML_FILE = com.ctg.ui.Base.getSDPath()+"/OBDII/weathercn.xml";  
  
    private List<WeatherReport> weatherReportList = new ArrayList<WeatherReport>();  
  
    private ArrayList<String> weatherIndex = new ArrayList<String>();
    
    private Map<String, String> weatherIndexMp = new HashMap<String, String>();
    
    private WeatherDetail weatherDetail = null;
    
    private String cityId;
    
    Context mContext;
    /** 
     * 启动的时候，首先检查weathercn.xml是否存在，如果不存在的话，重新从m.weathercn.com获取， 
     * 只有第一次的时候会获取。 
    */
//    static {  
//        try {  
//            prepareXML();  
//        } catch (Exception e) {  
//            e.printStackTrace();  
//        }  
//    }  

    public WeatherUtil(Context context){
    	mContext = context;
    }
 
    /** 
     * 返回指定城市，指定日期的天气预报。<br/> 
     *  
     * @param city 
     *            城市，如"北京"。 
     * @param cal 
     *            日期。 
     * @return 如果城市正确，并且日期在7天以内，那么返回天气信息。否则返回null。 
     */  
	public WeatherReport getWeatherReport(String city, Calendar cal) {  
        String dateStr = cal.get(Calendar.MONTH) + "月"  
                + cal.get(Calendar.DAY_OF_MONTH) + "日";  
        return getWeatherReport(city, dateStr);  
    }  
  
    /** 
     * 返回指定城市，指定日期的天气预报。<br/> 
     *  
     * @param city 
     *            城市，如"北京"。 
     * @param date 
     *            日期，格式为"1月20日"。 
     * @return 如果城市正确，并且日期在7天以内，那么返回天气信息。否则返回null。 
     */  
    public WeatherReport getWeatherReport(String city, String date) {  
        for (WeatherReport report : getWeatherReports(city)) {  
            if (report.getDate().equals(date)) {  
                return report;  
            }  
        }  
  
        return null;  
    }  
  
    /*返回各种天气指数*/
    public ArrayList<String> getWeatherIndex2() throws Exception{
    	URLConnection connectionData; 
        StringBuilder sb; 
        BufferedReader br;// 读取data数据流 
        JSONObject jsonData; 
        JSONObject info; 
    	URL url = new URL("http://m.weather.com.cn/data/" + cityId + ".html"); 
        connectionData = url.openConnection(); 
        connectionData.setConnectTimeout(1000); 
        
        br = new BufferedReader(new InputStreamReader( 
                connectionData.getInputStream(), "UTF-8")); 
        sb = new StringBuilder(); 
        String line = null; 
        while ((line = br.readLine()) != null) 
            sb.append(line); 
        
        String datas = sb.toString();   
            
        jsonData = new JSONObject(datas);//fromObject(datas); 
      //  System.out.println(jsonData.toString());  
        info = jsonData.getJSONObject("weatherinfo"); 
        String index;// 今天的穿衣指数 
        String index_uv ;// 紫外指数 
        String index_tr ;// 旅游指数 
        String index_co ;// 舒适指数 
        String index_cl ;// 晨练指数 
        String index_xc;//洗车指数 
        String index_d;//天气详细穿衣指数
        index = info.getString("index").toString(); 
        index_uv = info.getString("index_uv").toString(); 
        index_tr = info.getString("index_tr").toString(); 
        index_co= info.getString("index_co").toString(); 
        index_cl = info.getString("index_cl").toString(); 
        index_xc = info.getString("index_xc").toString(); 
        index_d =  info.getString("index_d").toString(); 
        weatherIndex.add(index);
        weatherIndex.add(index_uv);
        weatherIndex.add(index_tr);
        weatherIndex.add(index_co);
        weatherIndex.add(index_cl);
        weatherIndex.add(index_xc);
        weatherIndex.add(index_d);
        
		return weatherIndex;
    	
    }
    
    public WeatherDetail getTodayDetail(){    	
    	String myDetailStr;
    	String myIconStr;
    	String htmlStr;

    	List<String> left;
    	List<String> right;
    	List<String> iconStrLst;
    	String iconStr;
    	weatherDetail = new WeatherDetail();
    	try {
    		htmlStr = new WebPageUtil().processUrl(String.format(REPORT_DETAIL_URL, cityId))  
			        .getWebContent();
    		myDetailStr = getMatcher(htmlStr, "(?<=class=\"skxiang\">)[\\s\\S]{500}?");//(?=</div>)

    		left = getAllMathers(myDetailStr, "(?<=class=\"a\")[\\s\\S]+?(?=</p>)");
    		right = getAllMathers(myDetailStr, "(?<=class=\"b\")[\\s\\S]+?(?=</p>)");
    		
    		
			weatherDetail.humidity = left.get(0).split("&nbsp;")[1];
			weatherDetail.windspeed = left.get(1).split("&nbsp;")[1];
			weatherDetail.pressure = left.get(2).split("&nbsp;")[1];
			
			weatherDetail.visibility = right.get(0).split("&nbsp;")[1];
			weatherDetail.sunrise = right.get(1).split("&nbsp;")[1];
			weatherDetail.sunset = right.get(2).split("&nbsp;")[1];
    
			myIconStr = getMatcher(htmlStr, "(?<=class=\"skicon\">)[\\s\\S]{100}?");
			iconStrLst = getAllMathers(myIconStr, "(?<=img src=\")[\\s\\S]+?(?=\")");
			iconStr = iconStrLst.get(0);
			weatherDetail.icon = iconStr;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

    	
    	return weatherDetail;
    }
    
    public Map<String, String> getWeatherIndex(){
    	String cont = null;
    	
    	try {
			cont = new WebPageUtil().processUrl(String.format(REPORT_MORE_URL, cityId))  
			        .getWebContent();
			
            List<String> indexStrList = getAllMathers(cont,  
                    "(?<=class=\"c\"><span class=\"d\">)[\\s\\S]+?(?=</div)");
            String typeReg = "[\\s\\S]+(?=</span)";
            String contReg = "(?<=</span>)[\\s\\S]+";
            Matcher matType;
            Matcher matCont;
            String typeStr, contStr;
            for (String indexStr : indexStrList) {
            	matType = Pattern.compile(typeReg).matcher(indexStr);
            	matCont = Pattern.compile(contReg).matcher(indexStr);
            	if(matType.find() && matCont.find()){
            		typeStr = matType.group();
            		contStr = matCont.group();
            		typeStr = typeStr.replace("[", "").replace("]", "");
            		weatherIndexMp.put(typeStr, contStr);
            		
            	}
            		
            }
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	return weatherIndexMp;
    	
    }
    /** 
     * 返回指定城市的天气预报（7天内） 
     *  
     * @param city 
     * @return 返回指定城市的天气预报（7天内），如果指定的城市错误，返回空的list，list.size()=0 
     */  
    public List<WeatherReport> getWeatherReports(String city) {  
        List<WeatherReport> list = new ArrayList<WeatherReport>();  
        try {  
  
            String weatherPage = getWeatherReportPage(city);  
  
            List<String> reportStrList = getAllMathers(weatherPage,  
                    "(?<=class=\"b\">)[\\s\\S]+?<br>[\\s\\S]+?(?=</)");  
            for (String reportStr : reportStrList) {  
                String weather = reportStr.trim().replaceAll(" ", "")  
                        .replaceAll("<br>\r\n\r\n", "\r\n")  
                        .replaceAll("<br>", "");  
  
                String[] str = weather.split("\r\n");  
                if (str.length > 5) {  
                    WeatherReport report = new WeatherReport();  
  
                    int i = 0;  
                    String dateStr = str[i++].trim();  
  
                    report.setCity(city);  
                    report.setDate(getMatcher(dateStr, ".+(?=\\()"));  
                    report.setWeekDay(getMatcher(dateStr, "(?<=\\().+?(?=\\))"));  
                    report.setDayOrNight(str[i++].trim());  
                    report.setWeather(str[i++].trim());  
                    report.setTemperature(str[i++].trim());  
                    report.setWindDir(str[i++].trim());  
                    report.setWind(str[i++].trim());  
  
                    list.add(report);  
                    if (str.length > 10) {  
                        report = new WeatherReport();  
                        report.setCity(city);  
                        report.setDate(getMatcher(dateStr, ".+(?=\\()"));  
                        report.setWeekDay(getMatcher(dateStr,  
                                "(?<=\\().+?(?=\\))"));  
                        report.setDayOrNight(str[i++].trim());  
                        report.setWeather(str[i++].trim());  
                        report.setTemperature(str[i++].trim());  
                        report.setWindDir(str[i++].trim());  
                        report.setWind(str[i++].trim());  
                        list.add(report);  
                    }  
                }  
            }  
  
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
  
        this.weatherReportList = list;  
        return this.weatherReportList;  
  
    }  
  
    public String[] getPM25Report(String cityPY){
    	String value;
    	String text;
    	String level;
    	 try {      		  
             String pm25Page = getPM25ReportPage(cityPY);
             if(pm25Page != null){
            	 value = getMatcher(pm25Page,  
                         "(?<=div class=\"aqivalue\">)[\\s\\S]+?(?=</)"); 
            	 text = getMatcher(pm25Page,  
                         "(?<=div class=\"aqileveltext\">)[\\s\\S]+?(?=</)"); 
            	 level = getMatcher(pm25Page,  
                         "(?<=div class=\"aqitext\">)[\\s\\S]+?(?=</span)"); 
            	 if(level != null)
            		 level = getMatcher(level, "(?<=<span>)[\\s\\S]+"); 
            	 return new String[]{value, text, level};
             }
    	 }catch (Exception e) {  
             e.printStackTrace();  
         }  
		return null;
    	
    }
    /** 
     * 返回字符串中第一个符合regex的字符串，如果没有符合的，返回空字符串 
     *  
     * @param str 
     *            字符串 
     * @param regex 
     *            正则表达式 
     * @return 
     */  
    public static String getMatcher(String str, String regex) {  
        Matcher mat = Pattern.compile(regex).matcher(str);  
        if (mat.find()) {  
            return mat.group();  
        } else {  
            return "";  
        }  
    }  
  
    /** 
     * 返回字符串str中所有符合regex的子字符串。 
     *  
     * @param str 
     * @param regex 
     * @return 
     */  
    public static List<String> getAllMathers(String str, String regex) {  
        List<String> strList = new ArrayList<String>();  
        Matcher mat = Pattern.compile(regex).matcher(str);  
        while (mat.find()) {  
            strList.add(mat.group());  
        }  
        return strList;  
    }  
  
    /** 
     * 从m.weathercn.com获取城市(区域county)和城市所对应的编号(区域编号cid)。<br/> 
     * 并保存到xml文件"weathercn.xml"。如果已经存在weathercn.xml文件，那么不会再次获取。 
     *  
     * @throws Exception 
     */  
    private static void prepareXML() throws Exception {  
        /** 
         * 如果xml文件已经存在，不用再次获取。 
         */  
        File file = new File(XML_FILE);  
        if (file.exists()) {  
            // 提示xml文件位置，不需要可以注释掉。  
            System.out.println("在下面的路径中找到XML文件 " + file.getCanonicalPath());  
            return;  
        }  
    	InputStream ins = Base.OBDApp.getAssets().open("weathercn.xml");
    	    	
    	
    	if(ins != null)
    		return;
        // 用DOM创建XML文档  
        Document doc = DocumentBuilderFactory.newInstance()  
                .newDocumentBuilder().newDocument();        
        // 创建XML文档root element  
        
        Element root = doc.createElement("root");  
        doc.appendChild(root);  
  
        // 省province  
        //  
        WebPageUtil webPageUtil = new WebPageUtil().processUrl(PROVINCE_URL);         
        String provincePage = webPageUtil.getWebContent();  
        Hashtable<String, String> provinceTable = parseProvincePage(provincePage);
        //Element provEle = doc.createElement("province"); 
        //Element distEle = doc.createElement("district");
        //Element countryEle = doc.createElement("country");
        //Node textNode = null;
        for (String province : provinceTable.keySet()) {  
            // 进度提示，不需要可以注释掉  
            System.out.println(String.format("正在获取%s的城市信息...", province));  
            Element eleProvince = doc.createElement("province");                       
            eleProvince.setAttribute("pid", provinceTable.get(province)); 
            Node nodeProv = doc.createTextNode(province); 
            eleProvince.appendChild(nodeProv);
            root.appendChild(eleProvince);  
  
            String districtPage = new WebPageUtil().processUrl(  
                    String.format(DISTRICT_URL, provinceTable.get(province)))  
                    .getWebContent();  
  
            Hashtable<String, String> districtTable = parseDistrictPage(districtPage);  
            for (String district : districtTable.keySet()) {  
                Element eleDistrict = doc.createElement("district");                
                eleDistrict.setAttribute("did", districtTable.get(district));
                Node nodeDist = doc.createTextNode(district);
                eleDistrict.appendChild(nodeDist);
                eleProvince.appendChild(eleDistrict);  
  
                // long time = System.currentTimeMillis();  
                String countyPage = new WebPageUtil().processUrl(  
                        String.format(COUNTY_URL, districtTable.get(district),  
                                provinceTable.get(province))).getWebContent();  
                Hashtable<String, String> countyTable = parseCountyPage(countyPage);  
                for (String county : countyTable.keySet()) {  
                    Element eleCounty = doc.createElement("country");  
                    eleCounty.setAttribute("cid", countyTable.get(county)); 
                    Node nodeCont = doc.createTextNode(county);
                    eleCounty.appendChild(nodeCont);
                    eleDistrict.appendChild(eleCounty);  
                    // System.out.println(String.format("%s->%s->%s %s",  
                    // province, district, county,  
                    // System.currentTimeMillis()-time));  
  
                }  
            }  
            // 进度提示，不需要可以注释掉  
            System.out.println(String.format("已成功获取%s的城市信息", province));  
        }  
  
        // 将获取到的信息保存到xml文件中  
        TransformerFactory tFactory = TransformerFactory.newInstance();  
        Transformer transformer = tFactory.newTransformer();  
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");  
        transformer.setOutputProperty(  
                "{http://xml.apache.org/xslt}indent-amount", "2");  
        DOMSource source = new DOMSource(doc);  
        StreamResult result = new StreamResult(file);  
        transformer.transform(source, result);  
        System.out.println("XML文件已经被成功创建 " + file.getCanonicalPath());  
  
    }  
  
    /** 
     * 从m.weathercn.com的市页面中获取区县信息。<br/> 
     * 例如：从成都市页面中获取新津，双流等区县的编号。 
     *  
     * @param page 
     * @return 
     */  
    public static Hashtable<String, String> parseCountyPage(String page) {  
  
        return getKeyValues(page,  
                "<a href=\"index.do\\?cid=?.+?&pid=.+?class=\"c\">.+?</a>",  
                "(?<=>).+?(?=</a>)", "(?<=cid=)[0-9]+");  
    }  
  
    /** 
     * 从m.weathercn.com的省页面中获取省市信息。<br/> 
     * 例如：从四川省的页面获取成都，绵羊等市的编号。 
     *  
     * @param page 
     * @return 
     */  
    public static Hashtable<String, String> parseDistrictPage(String page) {  
  
        return getKeyValues(page, "<a href=\"cout.do\\?.+?class=\"c\">.+?</a>",  
                "(?<=>).+?(?=</a>)", "(?<=did=)[0-9]+");  
    }  
  
    /** 
     * 从m.weathercn.com的国内页面中获取省市信息。<br/> 
     * 例如：从国内页面获取四川省，山东省，北京市等的编号。 
     *  
     * @param page 
     * @return 
     */  
    public static Hashtable<String, String> parseProvincePage(String page) {  
  
        return getKeyValues(page, "<a href=\"dis.do?.+?class=\"c\">.+?</a>",  
                "(?<=>).+?(?=</a>)", "(?<=pid=)[0-9]+");  
    }  
  
    /** 
     * 从页面里面获取所需要的信息。 
     *  
     * @param webPage 
     *            网页 
     * @param tagRegex 
     *            正则表达式，用以获取包含key和value的内容，保存在字符串tag中 
     * @param keyRegex 
     *            正则表达式，用以从tag获取key的值 
     * @param valueRegex 
     *            正则表达式，用以从tag获取value的值 
     * @return 返回网页中所有匹配的key和value，如果没有，返回一个空的table，table.size()=0 
     */  
    public static Hashtable<String, String> getKeyValues(String webPage,  
            String tagRegex, String keyRegex, String valueRegex) {  
        Hashtable<String, String> table = new Hashtable<String, String>();  
  
        for (String tag : getAllMathers(webPage, tagRegex)) {  
            table.put(getMatcher(tag, keyRegex), getMatcher(tag, valueRegex));  
        }  
  
        return table;  
    }  
  
    /** 
     * 获取指定城市或者区域的天气预报页面。 
     *  
     * @param city 
     *            城市 
     * @return 返回天气预报页面的源代码。错误或者城市不正确等则返回空字符串。 
     * @throws Exception 
     */  
    public String getWeatherReportPage(String city) throws Exception { 
//    	File file = new File(XML_FILE);
//        Document doc = DocumentBuilderFactory.newInstance()  
//                .newDocumentBuilder().parse(file);  
    	InputStream in_s = mContext.getAssets().open("weathercn.xml");
        Document doc = DocumentBuilderFactory.newInstance()  
                .newDocumentBuilder().parse(in_s);  
        NodeList nodeList = doc.getElementsByTagName("district");
        String nodeVal = null;
        for (int i = 0; i < nodeList.getLength(); i++) {  
            Element ele = (Element) nodeList.item(i);  
            /*if (!ele.getAttribute("cid").equals("")) {  
                return new WebPageUtil().processUrl(  
                        String.format(REPORT7_URL, ele.getAttribute("cid")))  
                        .getWebContent();  
            }  */
   
        	 Node node = ele.getFirstChild();
        	 nodeVal = node.getNodeValue();
             if(city.startsWith(nodeVal)){ 
            	cityId = ele.getAttribute("did");
            	in_s.close();
             	return new WebPageUtil().processUrl(  
                         String.format(REPORT7_URL, ele.getAttribute("did")))  
                         .getWebContent();
             }
              
           
        }   
        in_s.close();
        return "";  
    }  
    
    /*返回pm2.5数值*/
    public String getPM25ReportPage(String city) throws Exception{
    	return new WebPageUtil().processUrl(String.format(REPORTPM25_URL, city)).getWebContent();
    }
    
}  