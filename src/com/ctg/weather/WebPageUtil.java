package com.ctg.weather;  
  
import java.io.InputStream;  
import java.net.Socket;  
import java.nio.charset.Charset;  
//import java.util.logging.Logger;  
import java.util.regex.Matcher;  
import java.util.regex.Pattern;  

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;  
/** 
 * 一个简单的类，用来获取网页内容 
 *  
 * 
 */  
public class WebPageUtil{  
  
    /** 
     * 调试 
     */  
    private static boolean debug = false;  
      
    /** 
     * 消息头，包含http资源或者http服务器的一些属性，例如:<BR/> 
     * HTTP/1.1 200 OK<BR/> 
     * Server: nginx/0.7.68<BR/> 
     * Date: Tue, 22 Jan 2013 10:55:21 GMT<BR/> 
     * Content-Type: image/jpeg<BR/> 
     * Content-Length: 6372<BR/> 
     * Last-Modified: Sun, 29 Apr 2012 07:29:01 GMT<BR/> 
     * Connection: close<BR/> 
     * Expires: Mon, 18 Nov 2013 10:55:21 GMT<BR/> 
     * Cache-Control: max-age=25920000<BR/> 
     */  
    private String msgHeader;  
    /** 
     * 消息头的字符缓存 
     */  
    private StringBuffer msgHeaderBuffer = new StringBuffer();  
  
    /** 
     * 网页内容编码 
     */  
    private Charset charset;  
  
    /** 
     * 缓存大小(单位：字节) 
     */  
    private int buffer_size = 4096;  
      
    /** 
     * 网页内容 
     */  
    private byte[] bytes = new byte[0];  
    
    String mUrl = null;
      
    private Handler mHanlder;
//    /**  
//     * 一个调用WebUtil的例子  
//     *   
//     * @param args  
//     */  
//    public static void main(String[] args) {  
//        try {  
//            String url = "http://m.weathercn.com/common/province.jsp";  
//            WebPageUtil webPageUtil = new WebPageUtil().processUrl(url);  
//            System.out.println("=======Header :=======\r\n"+webPageUtil.getMsgHeader());  
//            System.out.println("=======Content:=======\r\n"+webPageUtil.getWebContent());  
//              
//        } catch (Exception e) {  
//            e.printStackTrace();  
//        }  
//    }  
    /*public WebPageUtil(String url, Handler myhand){
    	mUrl = url;
    	mHanlder = myhand;
    }*/
    /** 
     * 使用Socket请求（获取）一个网页。<br/> 
     * 例如:<br/> 
     * processUrl("http://www.baidu.com/")会获取百度首页；<br/> 
     *  
     * @param url 
     *            这个网页或者网页内容的地址 
     * @throws Exception 
     */  
    public WebPageUtil processUrl(String url) throws Exception {  
        url = formatUrl(url);  
  
        // 设置要连接的服务器地址  
        Socket socket = new Socket(getHost(url), getPort(url));  
        socket.setSoTimeout(3000);  
  
        // 构造请求，详情请参考HTTP协议(RFC2616)  
        String request = String.format("GET %s HTTP/1.0\r\n", getSubUrl(url));  
        request += String.format("HOST: %s \r\n\r\n", getHost(url));  
  
        if(debug) {  
            System.out.println("request:\r\n"+request);  
        }  
  
        // 发送请求  
        socket.getOutputStream().write(request.getBytes());  
  
        // 设置缓存，最好跟系统的socket接收缓存一样  
        this.buffer_size = socket.getReceiveBufferSize();  
        byte[] bytesBuffer = new byte[buffer_size];// 缓存InputStream的原始数据  
        char[] charsBuffer = new char[buffer_size];// 缓存InputStream的字符数据  
  
        // 来自服务器响应(InputStream)  
        InputStream is = socket.getInputStream();  
  
        // 局部变量，读取到的内容长度（字节）  
        int bytesLength = 0;  
        // 局部变量，判断消息头是否读取完毕  
        boolean headerComplete = false;  
  
        // 从InputStream中读取网页的内容，如果读取到的内容  
        // 长度为-1，则读取完毕  
        while ((bytesLength = is.read(bytesBuffer, 0, buffer_size)) != -1) {  
            if (headerComplete) {  
                SaveBytes(bytesBuffer, 0, bytesLength);  
            } else {  
                int bufferLength = msgHeaderBuffer.length();  
                msgHeaderBuffer.append(  
                        bytes2chars(bytesBuffer, charsBuffer, bytesLength), 0,  
                        bytesLength);  
                int msgEndIndex = msgHeaderBuffer.indexOf("\r\n\r\n");  
                if (msgEndIndex != -1) {  
                    headerComplete = true;  
                    msgHeader = "Url: " + url + "\r\n"  
                            + msgHeaderBuffer.substring(0, msgEndIndex);  
                    int temp = msgEndIndex - bufferLength + 4;  
                    SaveBytes(bytesBuffer, temp, bytesLength - temp);  
                }  
            }  
        }  
          
        socket.close();  
  
        //获取网页编码   
        this.getCharset();  
          
        return this;  
    }  
  
    
	private synchronized void sendMsg(int what) {
		if (mHanlder != null) {
			Message msg = mHanlder.obtainMessage();
			msg.what = what;
			Bundle bb = new Bundle();
			switch (what) {
			case WeatherInfo.MSG_WEATHER_DATA_READY:
				//bb.putParcelableArrayList(Weather.MSG_WEATHER_DATA_READY,
				//		null);
				bb.putByteArray(WeatherInfo.WEATHER_DATA_READY_STR, bytes);					
				break;


			}
			msg.setData(bb);
			mHanlder.sendMessage(msg);
		}
	}
    /** 
     * 根据网址获取服务器端口。<br/> 
     * http 端口为80<br/> 
     * https端口为443 
     * @param url 
     * @return 
     */  
    public static int getPort(String url) {  
        int port = 0;  
        if (url.startsWith("https://")) {  
            port = 443;  
        } else if (url.startsWith("http://")) {  
            port = 80;  
        }  
          
        if(debug) {  
            System.out.println("port: "+port);  
        }  
          
        return port;  
    }  
  
    /** 
     * 根据网址，获取服务器地址<br/> 
     * 例如：<br/> 
     * http://m.weathercn.com/common/province.jsp<p> 
     * 返回：<br/> 
     * m.weathercn.com 
     * @param url 网址 
     * @return 
     */  
    public static String getHost(String url) {  
        String host = "";  
        Matcher mat = Pattern.compile("(?<=https?://).+?(?=/)").matcher(url);  
        if(mat.find()) {  
            host = mat.group();  
        }  
          
        if(debug) {  
            System.out.println("host: "+host);  
        }  
          
        return host;  
    }  
      
    /** 
     * 根据网址，获取网页路径 
     * 例如：<br/> 
     * http://m.weathercn.com/common/province.jsp<p> 
     * 返回：<br/> 
     * /common/province.jsp 
     * @param url 
     * @return 如果没有获取到网页路径，返回""; 
     */  
    public static String getSubUrl(String url) {  
        String subUrl = "";  
        Matcher mat = Pattern.compile("https?://.+?(?=/)").matcher(url);  
        if(mat.find()) {  
            subUrl = url.substring(mat.group().length());  
        }  
          
        if(debug) {  
            System.out.println("subUrl: "+subUrl);  
        }  
          
        return subUrl;  
    }  
  
    /** 
     * 在某些网址上加个"/"<br/> 
     * 例如：<br/> 
     * http://www.baidu.com<br/> 
     * 返回：<br/> 
     * http://www.baidu.com/<p> 
     * 例如：<br/> 
     * http://www.baidu.com/xxxx<br/> 
     * 返回：(没有加"/")<br/> 
     * http://www.baidu.com/xxxx<br/> 
     * @param url 
     * @return 
     */  
    public static String formatUrl(String url) {  
        Matcher mat = Pattern.compile("https?://[^/]+").matcher(url);  
        if (mat.find() && mat.group().equals(url)) {  
            return url + "/";  
        } else {  
            return url;  
        }  
    }  
      
    /** 
     * 把从输入流中读取到的数据保存到bytes数组中， 
     * 每次都创建一个新的byte[]来存储原来bytes[]数组中的数据和 
     * 新读取到的b中的数据。 
     * @param b 存储内容的byte[] 
     * @param start 内容的起始位置，从0开始 
     * @param length 内容的长度 
     * @throws Exception 
     */  
    private void SaveBytes(byte[] b, int start, int length) throws Exception {  
        //do some check  
        if(start<0 || length<0) {  
            throw new Exception("start/length is incorrect.");  
        }  
        //新建一个byte数组  
        byte[] newBytes = new byte[bytes.length+length];  
        System.arraycopy(bytes, 0, newBytes, 0, bytes.length);  
        System.arraycopy(b, start, newBytes, bytes.length, length);  
          
        bytes = newBytes;  
  
    }  
  
    /** 
     * 将字节数据转换成字符数据 
     * @param srcBytes 
     * @param dstChars 
     * @param length 
     * @return 
     */  
    private char[] bytes2chars(byte[] srcBytes, char[] dstChars, int length) {  
        for (int i = 0; i < length; i++) {  
            dstChars[i] = (char) srcBytes[i];  
        }  
  
        return dstChars;  
    }  
  
    /** 
     * 获取网页资源（文件）的消息头，里面包含了服务器和资源的一些属性 
     */  
    public String getMsgHeader() {  
        return msgHeader;  
    }  
  
    /** 
     * 获取网页或网页资源的编码，如果在消息头里面没有找到编码，那么就 
     * 返回系统默认编码。 
     * @return 
     */  
    public Charset getCharset() {  
        String header = this.msgHeader.toUpperCase();  
        Matcher mat = Pattern.compile("CHARSET=.+").matcher(header);  
        if(mat.find()) {  
            this.charset = Charset.forName(mat.group().split("=")[1]);  
        }else{  
            this.charset = Charset.defaultCharset();  
        }          
        return charset;  
    }  
      
    /** 
     * 获取网页内容 
     * @return 
     */  
    public String getWebContent() {  
        return new String(bytes, charset);  
    }  
    

}  









