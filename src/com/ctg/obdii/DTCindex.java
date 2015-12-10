/******************************************************/
/* Copyright (C) 2014 The Android Project             */
/* All rights is reserved by Harman CTG Shanghai      */
/* First Version is delivered  by Zhiming.hu          */
/* Data: 2014-02-25                                   */
/* Change history:                                    */
/* Modifier:                                          */
/* Data:                                              */
/******************************************************/

package com.ctg.obdii;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.Queue;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.ctg.service.CarDataService;



import android.os.Environment;
import android.util.Log;
//import jxl.Cell;    
//   
//import jxl.Sheet;    
//import jxl.Workbook; 
//import jxl.read.biff.BiffException;
//import jxl.write.Label;
//import jxl.write.WritableSheet;
//import jxl.write.WritableWorkbook;
//import jxl.write.WriteException;
//import jxl.write.biff.RowsExceededException;


public class DTCindex {
	
	public enum DTCCodeType{
		Ptype,
		Btype,
		Ctype,
		Utype,
		Other
	}
	public static Queue<String> dtcqueue = new LinkedList<String>();
	public static Queue<String> summaryqueue = new LinkedList<String>();
	public static Queue<String> dtcqueue_history = new LinkedList<String>();
	public static Queue<String> summaryqueue_history = new LinkedList<String>();	
	public DTCCodeType dtctype;
    private static boolean D = false;
    //True means excel 2007, false means excel 2003;
    private final boolean version = false;
	private final static String DTCTAG = "DTC table index tag";
	private static String SDcardpath;	
	private static String filepath_2007 = "DTC/OBD2_DTCTable.xlsx";
	private static String filepath_2003 = "DTC/OBD2_DTCTable.xls";
	private static String filedirectory;
	//This variable for private;
	private static String cellcontent = "";
	//This variable for high level service;
	public static String dtcexplain = "";
	public static String dtcproposal = "";
	
	//This part is needed to be updated:++++
	private static String filepath_pic = "DTC_picture";
	private static String full_picpath = "";
	
	public int stepnum = 10;
	public int picnum  = 10;
	public String stepArray[] = new String[stepnum];
	public String picArray[][] = new String[stepnum][picnum];
	public byte picBitmap[][] = new byte[stepnum][picnum];
	//=========================================================
	
	public DTCindex(){
	}
	
	//This part is needed to be updated:++++
	public void DisplayPicture(String Dtccode, String DTCsolution){
		String sdpath = getSDPath();
		String dtcsolutemp = DTCsolution;
		String fullcode = Dtccode.substring(1, 5);
		String picindex = "";
		String picpath = "";
		String strArray[] = DTCsolution.split(" ");
		int i = 0, j = 0;
		for(i = 0; i< strArray.length; i++ ){
			picArray[i][0] = strArray[i];
			picBitmap[i][0] = 1;
			if(!strArray[i].equals("")){
				if(strArray[i].contains("图")){
					String solutiontemp[] = strArray[i].split("图");
					for(j = 0; j < solutiontemp.length ; j++){
						picindex = solutiontemp[j].substring(1, 2);
						picindex = fullcode + "-" + picindex;
						picpath = sdpath + filepath_pic + picindex;
						picArray[i][j+1] = picpath;
						picBitmap[i][j+1] = 1;
					}
				}
									
			}
		}		
	}
	
	public void CLRbitmap(){
		int i = 0, j = 0;
		for(j = 0;  j < stepnum; j ++){
			for(i = 0; i < picnum; i ++){
				picBitmap[i][j] = 0;
			}
		}
	}
	//============================================
	
	//Judge the DTC code type:
	public DTCCodeType JudgeType(String dtccode){
		DTCCodeType dtccodetype;
		if(dtccode.contains("P"))
			dtccodetype = DTCCodeType.Ptype;
		else if(dtccode.contains("B"))
			dtccodetype = DTCCodeType.Btype;
		else if(dtccode.contains("C"))
			dtccodetype = DTCCodeType.Ctype;
		else if(dtccode.contains("U"))
			dtccodetype = DTCCodeType.Utype;
		else 
			dtccodetype = DTCCodeType.Other;
		return dtccodetype;		
	}
	

	//Get OBD DTC index data from searching result;
	public int ParseDTC(String dtccode){
		System.out.println("DTC code: PXXXX:");
		System.out.print(dtccode);		
		String strdtc = dtccode.substring(1, 5);
		int tempdtcnum = Integer.parseInt(strdtc);
		System.out.println("DTC code: integer--XXXX:");
		System.out.print(tempdtcnum);	
		return tempdtcnum;
	}	
	
	
	//Get external SD card directory:
	public String getSDPath(){
		File sdDir = null;
		boolean sdCardExist = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);   //test if sd exist;
	    if(!sdCardExist)   
	    {  
	    	 return ""; 		
	    } 
	    else
	    {
	         sdDir = Environment.getExternalStorageDirectory();   //get root directory
	         SDcardpath = sdDir.toString();
	         if(D) Log.d(DTCTAG, "SD card path is :" + SDcardpath );
	 	     return SDcardpath; 
	    } 
	}
	
	//Make file directory:
	public String makeDirectory(){
		String rootDir = getSDPath();
		if(rootDir == null)
			return "";
	    if(version)
			filedirectory = rootDir + "/" + filepath_2007;
		else 
			filedirectory = rootDir + "/" + filepath_2003; 
	    if(D) Log.d(DTCTAG, "SD card path is :" + filedirectory );
		return filedirectory;				
	}
	
		
	//Read specified excel table and get content in the specified cell:
//	public String readexcel(String dtcpath, int dtccolumn, int dtcrows, boolean solution, DTCCodeType dtccodetype){
//	    try{
//	    	File pathDir = null;
//	    	InputStream in = null;
//	    	String defaultpath = makeDirectory();
//	    	Workbook wbread = null;
//	    	//can't get sdcard directory or without sdcard;
//	    	if(defaultpath.contentEquals(filedirectory))
//	    	{	    		
//	    		//obtain files and open excel;
//	    		pathDir = new File(dtcpath);
//	    		if(pathDir.exists()){
//	    			in = new FileInputStream(pathDir);
//	    			wbread =  Workbook.getWorkbook(in);
//	    		}
//	    		else
//	    			pathDir.createNewFile();
//	    	}
//	    	else
//	    	{
//	    		pathDir = new File(defaultpath);
//	    		if(pathDir.exists()){
//	    			in = new FileInputStream(pathDir);
//	    			wbread =  Workbook.getWorkbook(in);
//	    		}
//	    		else
//	    			pathDir.createNewFile();	    		
//	    	}
//	    	Sheet sheet = null;
//	    	switch(dtccodetype)
//	    	{
//	    		case Ptype:	    		
//	    			sheet = wbread.getSheet(0); 
//	    			break;
//	    		case Btype:
//	    			sheet = wbread.getSheet(1); 
//	    			break;
//	    		case Ctype:
//	    			sheet = wbread.getSheet(2); 
//	    			break;	    			
//	    		case Utype:
//	    			sheet = wbread.getSheet(3); 
//	    			break;	
//	    		case Other:
//	    			sheet = wbread.getSheet(4); 
//	    			break;	    			
//	    	}
//	    	if(D) Log.d(DTCTAG, "Reading DTC index table is opened!"); 
//	    	//get the specified cells in "rows" row, in "column" column;
//	    	if(solution){
//	    		dtcrows = dtcrows -1;
//	    		Cell cell = sheet.getCell(dtccolumn+1, dtcrows);
//	    		cellcontent = cell.getContents();
//	    		dtcexplain = cellcontent;
//        		System.out.println("DTC solution: sheet content from cell:");
//        		System.out.print(cellcontent);	    		
//	    	}
//	    	else{
//	    		dtcrows = dtcrows -1;
//	    		Cell cell = sheet.getCell(dtccolumn, dtcrows);
//	    		cellcontent = cell.getContents();
//	    		dtcexplain = cellcontent;
//        		System.out.println("DTC explain: sheet content from cell:");
//        		System.out.print(cellcontent);	        		
//	    	}
//	    	
//	    	wbread.close();
//	    	//return dtcexplain;
//	    	if(D) Log.d(DTCTAG, "Reading DTC index table is closed!"); 
//	    }
//	    catch (BiffException e){
//	    	if(D) Log.e(DTCTAG, "Biffexception error.");	
//	    }
//		catch (IOException e){
//			if(D) Log.e(DTCTAG,"IOException error.");	
//		}
//	    return dtcexplain;
//	}
//	
	public boolean readDTCsFromHashMap(String dtccode){
		String strArray[] = dtccode.split(" ");
		String summary = null;
		int i;
		
    	dtcqueue.clear();
		summaryqueue.clear();
		dtcqueue_history.clear();
		summaryqueue_history.clear();
		for(i = 0; i < strArray.length; i++){						
			summary = CarDataService.dtcSummaryMap.get(strArray[i]);
			if(summary == null){
		    	dtcqueue.clear();
				summaryqueue.clear();
				dtcqueue_history.clear();
				summaryqueue_history.clear();
				return false;
			}
			else{
				dtcqueue.offer(strArray[i]);
				summaryqueue.offer(summary);
				dtcqueue_history.offer(strArray[i]);
				summaryqueue_history.offer(summary);
			}
		}
		return true;
	}
	
	public boolean readMutipleDtcsXML(String dtcpath,String dtccode){
		try{
			String strret[] = null;
	    	int i = 0, j, dtccodenume = 0;
	    	String TemDtcs = dtccode;
	    	File pathDir = null;
	    	String xmlPath = getSDPath() + "/DTC/OBD2_DTCTable.xml";
	    	File file = new File(xmlPath);
	        Document doc = DocumentBuilderFactory.newInstance()  
			        .newDocumentBuilder().parse(file);
			NodeList nodeList = null;
	        String nodeVal = null;
	    	dtcqueue.clear();
			summaryqueue.clear();
			dtcqueue_history.clear();
			summaryqueue_history.clear();
	    	String strArray[] = TemDtcs.split(" ");
	    	for(i = 0; i < strArray.length; i++){
				nodeList = doc.getElementsByTagName(strArray[i]);		        		         
	            Element ele = (Element) nodeList.item(0);  
	        	Node node = ele.getFirstChild();
	        	nodeVal = node.getNodeValue();
				dtcqueue.offer(strArray[i]);
				summaryqueue.offer(nodeVal);
				Log.e(DTCTAG, "AddDtc dtcqueue dtccode="+strArray[i]);
				Log.e(DTCTAG, "AddDtc summaryqueue dtcsummary="+nodeVal);
				dtcqueue_history.offer(strArray[i]);
				summaryqueue_history.offer(nodeVal);
	    	}
		}
		catch (SAXException e) {
			// TODO Auto-generated catch block
			Log.e(DTCTAG, "AddDtc SAXException");
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.e(DTCTAG, "AddDtc IOException");
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			Log.e(DTCTAG, "AddDtc ParserConfigurationException");
			e.printStackTrace();
		}  
		return true;
	}
	
	//Read specified excel table and get content in the specified cell:
//	public boolean readMutipleDtcs(String dtcpath,String dtccode){
//	    try{
//	    	String strret[] = null;
//	    	int i = 0, dtccodenume = 0;
//	    	String TemDtcs = dtccode;
//	    	DTCCodeType mdtctype;
//	    	File pathDir = null;
//	    	InputStream in = null;
//	    	String defaultpath = makeDirectory();
//	    	Workbook wbread = null;
//	    	//can't get sdcard directory or without sdcard;
//	    	if(defaultpath.contentEquals(filedirectory))
//	    	{	    		
//	    		//obtain files and open excel;
//	    		pathDir = new File(dtcpath);
//	    		if(pathDir.exists()){
//	    			in = new FileInputStream(pathDir);
//	    			wbread =  Workbook.getWorkbook(in);
//	    		}
//	    		else
//	    			pathDir.createNewFile();
//	    	}
//	    	else
//	    	{
//	    		pathDir = new File(defaultpath);
//	    		if(pathDir.exists()){
//	    			in = new FileInputStream(pathDir);
//	    			wbread =  Workbook.getWorkbook(in);
//	    		}
//	    		else
//	    			pathDir.createNewFile();	    		
//	    	}
//	    	Sheet sheet = null;
//	    	if(TemDtcs == null)
//	    		return false;
//			dtcqueue.clear();
//			summaryqueue.clear();
//			dtcqueue_history.clear();
//			summaryqueue_history.clear();
//	    	String strArray[] = TemDtcs.split(" ");
//	    	for(i = 0; i < strArray.length; i++){
//	    		mdtctype = JudgeType(strArray[i]);
//	    		dtccodenume = ParseDTC(strArray[i]);	    		
//	    		switch(mdtctype)
//	    		{
//	    			case Ptype:	    		
//	    				sheet = wbread.getSheet(0); 
//	    				break;
//	    			case Btype:
//	    				sheet = wbread.getSheet(1); 
//	    				break;
//	    			case Ctype:
//	    				sheet = wbread.getSheet(2); 
//	    				break;	    			
//	    			case Utype:
//	    				sheet = wbread.getSheet(3); 
//	    				break;	
//	    			case Other:
//	    				sheet = wbread.getSheet(4); 
//	    				break;	    			
//	    		}
//	    		if(D) Log.d(DTCTAG, "Reading DTC index table is opened!"); 
//	    		//get the specified cells in "rows" row, in "column" column;
//	    		dtccodenume = dtccodenume -1;
//	    		Cell cell = sheet.getCell(0, dtccodenume);
//	    		cellcontent = cell.getContents();
//				dtcqueue.offer(strArray[i]);
//				summaryqueue.offer(cellcontent);
//				dtcqueue_history.offer(strArray[i]);
//				summaryqueue_history.offer(cellcontent);			
//	    		//System.out.println("DTC solution: sheet content from cell:");
//	    		System.out.print(cellcontent);
//	    	}	    		
//	    	wbread.close();
//	    	//return dtcexplain;
//	    	if(D) Log.d(DTCTAG, "Reading DTC index table is closed!"); 
//	    }
//	    catch (BiffException e){
//	    	if(D) Log.e(DTCTAG, "Biffexception error.");	
//	    }
//		catch (IOException e){
//			if(D) Log.e(DTCTAG,"IOException error.");	
//		}
//	    //return dtcexplain;
//	    return true;
//	}
	
	
	
	//Write specified content into the specified cell:
//	public void writeexcel(String path, int column, int rows, String updatecontent, DTCCodeType dtccodetype){
//		try{
//	    	File pathDir = null;
//	    	InputStream in = null;
//	    	String defaultpath = makeDirectory();
//	    	Workbook wbwirte = null;
//	    	//can't get sdcard directory or without sdcard;
//	    	if(defaultpath.contentEquals("/" + filedirectory))
//	    	{	    		
//	    		//obtain files and open excel;
//	    		pathDir = new File(path);
//	    		if(pathDir.exists()){
//	    			in = new FileInputStream(pathDir);
//	    			wbwirte =  Workbook.getWorkbook(in);
//	    		}
//	    		else
//	    			pathDir.createNewFile();
//	    	}
//	    	else
//	    	{   
//    			pathDir = new File(defaultpath);
//	    		if(pathDir.exists()){	    			
//	    			in = new FileInputStream(pathDir);
//	    			wbwirte =  Workbook.getWorkbook(in);
//	    		}
//	    		else
//	    			pathDir.createNewFile();	    		
//	    	}			    	
//	    	//Create a copy of original file;
//	    	if(in != null)
//	    	{
//	    		WritableWorkbook wbook = Workbook.createWorkbook(pathDir, wbwirte);
//	    		WritableSheet wsheet = null;
//	    		switch(dtccodetype)
//	    		{
//	    			case Ptype:	    				
//	    				wsheet = wbook.getSheet(0);
//	    				break;
//	    			case Btype:	    				
//	    				wsheet = wbook.getSheet(1);
//	    				break;	    				
//	    			case Ctype:	    				
//	    				wsheet = wbook.getSheet(2);
//	    				break;
//	    			case Utype:	    				
//	    				wsheet = wbook.getSheet(3);
//	    				break;	    				
//	    			case Other:	    				
//	    				wsheet = wbook.getSheet(4);
//	    				break;	    				
//	    		}
//	    		wsheet.addCell(new Label(column, rows ,updatecontent));
//	    		wbook.write();
//	    		wbook.close();
//	    		wbwirte.close();
//	    		in.close();
//	    	}
//			if(D) Log.d(DTCTAG, "Writing DTC index table is closed!"); 
//		}
//		catch (RowsExceededException e) {
//			if(D) Log.e(DTCTAG,"RowsExceededException error.");	
//		}
//        catch (IOException e){
//        	if(D) Log.e(DTCTAG,"IOException error.");	
//        }
//        catch (WriteException e){
//        	if(D) Log.e(DTCTAG,"WriteException error.");	
//        }
//	    catch (BiffException e){
//	    	if(D) Log.e(DTCTAG, "Biffexception error.");	
//	    }		
//	}
	
	//invoke by outside function;
	public String getDTCexplain(){
		return dtcexplain;
	}
	
	public String getDTCsolution(){
		return dtcproposal;
	}
	
	//queue operation for DTCs
	public static String fetchDTCcodes(){
		String dtccode = "";
		if(dtcqueue != null && !dtcqueue.isEmpty()){
			dtccode = dtcqueue.poll();			
		}
		return dtccode;		
	}
	
	public static String fetchSummary(){
		String dtcsummary = "";
		if(summaryqueue != null && !summaryqueue.isEmpty()){
			dtcsummary = summaryqueue.poll();
		}
		return dtcsummary;
	}
	
	/*public static String appendDTCcodes(){
		String dtccode = "";
		if(dtcqueue_history != null && !dtcqueue_history.isEmpty()){
			dtccode = dtcqueue_history.poll();
		}
		return dtccode;		
	}
	
	public static String appendSummary(){
		String dtcsummary = "";
		if(summaryqueue_history != null && !summaryqueue_history.isEmpty()){
			dtcsummary = summaryqueue_history.poll();
		}
		return dtcsummary;
	}*/
	
	
	//This is for testing:
//	public void createxcel() throws WriteException 
//	{
//		try{
//			File root = Environment.getExternalStorageDirectory(); 
//			String rootdir = root.toString() + "/test.xls";
//			WritableWorkbook wb = Workbook.createWorkbook(new File(rootdir));
//			WritableSheet sheet = wb.createSheet("sheet0", 0);
//			Label lb = new Label(0 , 0, "绗竴鏍煎唴瀹�);
//			sheet.addCell(lb);
//			wb.write();
//			wb.close();					
//		}
//        catch (IOException e){
//        	if(D) Log.e(DTCTAG,"IOException error.");	
//        }
//		
//	}	
			
}
