package com.ctg.TrafficViolationPt;

import android.content.Context;
import android.os.Environment;

import com.ctg.trace.DownloadDelegate;
import com.ctg.trace.ZipUtil;
import com.ctg.util.Preference;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ChaLi on 12/26/2014.
 */
public class DownloadViolationFile {
    static final public int MSG_DOWNLOAD_COMPLETE = 0;
    static final public int MSG_DOWNLOAD_FAIL = 1;
    static final public int MSG_DOWNLOAD_NO_MORE = 2;
    private String savePtah = Environment.getExternalStorageDirectory().getPath()+"/OBDII/Violation";
    private String mVersion;
    private Thread mThread;
    private Context context;

    public DownloadViolationPtDelegate downloadDelegate=null;

    public DownloadViolationFile(Context ctx){
        context = ctx;
    }

    public void Download(String version){
        mVersion = version;
        Thread thread = new Thread(runnable);
        thread.start();
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            getFile();
        }
    };

    private void getFile(){

        String version = ((com.ctg.ui.OBDApplication)context.getApplicationContext()).getVersion();
        String sessionidExist = Preference.getInstance(context.getApplicationContext()).getSessionId();

        HttpClient httpClient = new DefaultHttpClient();
        String url="http://192.168.1.51:8080/obd/services/ticket/getTicketPoint";
        HttpPost httpPost = new HttpPost(url);

        httpPost.setHeader("X-API-version", String.format("%s", version));
        httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
        httpPost.setHeader("X-token", sessionidExist);

        HttpResponse response=null;
        HttpEntity entity=null;
        InputStream input=null;
        List<NameValuePair> params=new ArrayList<NameValuePair>();
        try {
            params.add(new BasicNameValuePair("version", mVersion));
            httpPost.setEntity(new UrlEncodedFormEntity(params));
            response = httpClient.execute(httpPost);

            if(response.getStatusLine().getStatusCode()==200){
                entity=response.getEntity();
                input=entity.getContent();

                File file = new File(Environment.getExternalStorageDirectory().getPath()+"/OBDII");
                
                if( !file.exists() ){
                	file.mkdir();
                }
                File zipFile = new File(file +"/Violation.zip");
                if( !file.exists() ){
                	file.createNewFile();
                }
                FileOutputStream fout = new FileOutputStream(zipFile);
                byte bytes[] = new byte[1024];
                int j = 0;
                while( (j = input.read(bytes))!=-1){
                    fout.write(bytes, 0, j);
                }
                fout.close();

                //unzip
                unzipFile();

                //update data complete
                downloadDelegate.downloadComplete(MSG_DOWNLOAD_COMPLETE);
            }
            else if(response.getStatusLine().getStatusCode()==500){
                System.out.println("500");
                downloadDelegate.downloadComplete(MSG_DOWNLOAD_NO_MORE);
            }
            else if(response.getStatusLine().getStatusCode()==404){
                System.out.println("404");
                downloadDelegate.downloadComplete(MSG_DOWNLOAD_FAIL);
            }
            else {
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void unzipFile(){
        try{
            File[] files =new File(savePtah).listFiles();
            File destDir = new File(savePtah);
            if (!destDir.exists()) {
                destDir.mkdirs();
            }

            //ZipUtil.unzip(context.getFilesDir().getPath() + "/Violation.zip", context.getFilesDir() + "/Violation");
              ZipUtil.unzip(Environment.getExternalStorageDirectory().getPath()+"/OBDII" + "/Violation.zip", savePtah+"/");
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }


}
