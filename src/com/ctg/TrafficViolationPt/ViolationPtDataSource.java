package com.ctg.TrafficViolationPt;

import android.content.Context;
import android.os.Environment;

import com.ctg.trace.DownloadTrace;
import com.ctg.trace.TraceDataSourceDelegate;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by ChaLi on 12/26/2014.
 */
public class ViolationPtDataSource implements DownloadViolationPtDelegate{
    static final public int RET_UPDATE_COMPETE = 0;
    static final public int RET_UPDATE_FAIL = 1;
    static final public int RET_UPDATE_ALL = 0;

    public ViolationPtDataSourceDelegate violationPtDataSourceDelegate=null;

    Context context;
    private ViolationList mlistVio;
    private ImportViolationData mImportVio = null;
    private boolean mbAvailable;
    private DownloadViolationFile downloadFile=null;
    private double mVersionDB=0.0;

    public ViolationPtDataSource(Context cxt) {
        context = cxt;
        mImportVio = new ImportViolationData(context);
        downloadFile = new DownloadViolationFile(context);
        downloadFile.downloadDelegate = this;
        mVersionDB = getDBVersion();
    }

    public void updateDataSource(){
        downloadFile.Download(Double.toString(mVersionDB));
    }

    public boolean openDataSource(){
        return mImportVio.openDB();
    }

    public boolean closeDataSource(){
        return mImportVio.openDB();
    }

    public final ViolationList getlistVioByCoord(final double centerLon,final double centerLat,final float scale, final int[] violationType,float mScreenWidth,float mScreenHeight) {
        mlistVio = mImportVio.getListByCoor(centerLon, centerLat, scale, violationType, mScreenWidth, mScreenHeight);
        return mlistVio;
    }

    public void downloadComplete(int ret){
        if (ret == downloadFile.MSG_DOWNLOAD_COMPLETE){

            String verDB = getFileList();
            if(!verDB.isEmpty()){
                mVersionDB = Double.parseDouble(verDB);
            }
            if (violationPtDataSourceDelegate!=null)
                violationPtDataSourceDelegate.updateResult(RET_UPDATE_COMPETE);
        }
        else if(ret == downloadFile.MSG_DOWNLOAD_FAIL){
            if (violationPtDataSourceDelegate!=null)
                violationPtDataSourceDelegate.updateResult(RET_UPDATE_FAIL);
        }
        else if(ret == downloadFile.MSG_DOWNLOAD_NO_MORE){
            if (violationPtDataSourceDelegate!=null)
                violationPtDataSourceDelegate.updateResult(RET_UPDATE_ALL);
        }
    }

    private double getDBVersion(){
        File file =new File(Environment.getExternalStorageDirectory().getPath()+"/OBDII" + "/Violation/Version.txt");
        if(file == null)
            return 0.0;

        if(!file.exists()||file.isDirectory())
            return 0.0;

        StringBuffer sb=new StringBuffer();

        try {
            FileInputStream fis= new FileInputStream(file);
            byte[] buf = new byte[1024];
            if((fis.read(buf))!=-1){
                sb.append(new String(buf));
            }
        }catch (FileNotFoundException e){
        }catch (IOException e){
        }

        return Double.parseDouble(sb.toString());
    }

    private String getFileList(){
        File[] files =new File(Environment.getExternalStorageDirectory().getPath()+"/OBDII" + "/Violation").listFiles();

        if(files == null)
            return "";

        for (int i =0; i < files.length; i++)
        {
            File f = files[i];
            if (f.isFile())
            {
                String version = f.getName().substring(0, f.getName().length()-3);//0.1.db

                if(version.equals("0.1")){
                    File dbFile = new File(Environment.getExternalStorageDirectory().getPath()+"/OBDII" + "/Violation/Violation.db");
                    f.renameTo(dbFile);
                }
                else{
                    //add data to db
                }

                try {
                    File file=new File(Environment.getExternalStorageDirectory().getPath()+"/OBDII" + "/Violation/Version.txt");
                    if(!file.exists())
                        file.createNewFile();
                    FileOutputStream out=new FileOutputStream(file,true);
                    out.write(version.getBytes("utf-8"));
                    out.close();
                }catch (IOException e){
                }

                return version;
            }
        }
        return "";
    }
}
