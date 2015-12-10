package com.ctg.TrafficViolationPt;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.os.Environment;

import com.baidu.mapapi.model.LatLng;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ChaLi on 12/26/2014.
 */
public class ImportViolationData {

    Context context;
    private ViolationPtDatabase mDbVio= null;

    private ViolationList mListVio = new ViolationList();
//    private List<Integer> mLoadVio = new ArrayList<Integer>();


    public ImportViolationData(Context cxt) {
        context = cxt;
  //      copyFile(Environment.getExternalStorageDirectory().getPath() + "/" + "obd.db", context.getFilesDir() + "/" + "obd.bd");
        mDbVio= new ViolationPtDatabase(context) ;
    }

    public boolean openDB(){
        try
        {
            mDbVio.open();
        }
        catch(SQLException e)
        {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean closeDB(){
        try
        {
            mDbVio.close();
        }
        catch(SQLException e)
        {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public ViolationList getListByCoor(final double lon,final double lat, final float scale, final int[] violationType,float mScreenWidth,float mScreenHeight){
        Cursor cur = mDbVio.getByCoor(lon, lat, scale, violationType, mScreenWidth, mScreenHeight);

        int iLon = cur.getColumnIndex("lon");
        int iLat = cur.getColumnIndex("lat");
        int iId = cur.getColumnIndex("id");
        int iType = cur.getColumnIndex("violationtype");
        int iTrequency = cur.getColumnIndex("frequency");

        mListVio.clearPtList();

        for(cur.moveToFirst();!cur.isAfterLast();cur.moveToNext())
        {
            int id = cur.getInt(iId);
            Integer tmpID = new Integer(id);
  //          if(mLoadVio.indexOf(tmpID) == -1){
                double dLon = cur.getDouble(iLon);
                double dLat = cur.getDouble(iLat);
                int type = cur.getInt(iType);
                int frequency = cur.getInt(iTrequency);
                ViolationPt pt = new ViolationPt(dLat, dLon);
                pt.setId(id);
                pt.setType(type);
                pt.setFrequency(frequency);
                mListVio.addPt(pt);
 //               mLoadVio.add(tmpID);
  //          }
        }
        return mListVio;
    }

    private void openDataSource(){
        mDbVio.open();
    }

    public void copyFile(String oldPath, String newPath) {
        try {
            int bytesum = 0;
            int byteread = 0;
            File oldfile = new File(oldPath);
            if (oldfile.exists()) {
                InputStream inStream = new FileInputStream(oldPath);
                FileOutputStream fs = new FileOutputStream(newPath);
                byte[] buffer = new byte[1444];
                int length;
                while ( (byteread = inStream.read(buffer)) != -1) {
                    bytesum += byteread;
                    System.out.println(bytesum);
                    fs.write(buffer, 0, byteread);
                }
                inStream.close();
            }
        }
        catch (Exception e) {
            System.out.println("copy error");
            e.printStackTrace();

        }
    }
}
