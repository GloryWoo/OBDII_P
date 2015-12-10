package com.ctg.net;

import java.io.File;  
import java.io.FileOutputStream;  
import java.io.IOException;  
import java.io.InputStream;  
import java.net.HttpURLConnection;  
import java.net.MalformedURLException;  
import java.net.URL;  
import java.net.URLEncoder;
  
  
import android.app.AlertDialog;  
import android.app.Dialog;  
import android.app.AlertDialog.Builder;  
import android.content.Context;  
import android.content.DialogInterface;  
import android.content.Intent;  
import android.content.DialogInterface.OnClickListener;  
import android.net.Uri;  
import android.os.Handler;  
import android.os.Message;  
import android.util.Log;
import android.view.LayoutInflater;  
import android.view.View;  
import android.widget.ProgressBar;  
import android.widget.Toast;
  
import com.ctg.ui.Base;
import com.ctg.ui.R;
import com.ctg.util.Preference;

public class UpdateManager {  
  
	public static final String TAG = "UpdateManager";
    private Context mContext;  
      
    //提示语  
    //private String updateMsg = "服务器有新的软件版本，是否需要更新？";  
      
    //返回的安装包url  
    private String apkUrl = "http://116.236.202.130:8081/apps/优车宝.apk";  
      
    private String logoUrl;
    
    private Dialog noticeDialog;  
      
    private Dialog downloadDialog;  
     /* 下载包安装路径 */  
    private static final String savePath = Base.getSDPath()+"/OBDII/";  
      
    public static final String saveFileName = savePath + "OBDII.apk";  
     
    public static final String logoFileName = savePath + "logo_u.png";
    /* 进度条与通知ui刷新的handler和msg常量 */  
    private ProgressBar mProgress;  
  
      
    private static final int DOWN_UPDATE = 1;  
      
    private static final int DOWN_OVER = 2;  
      
    private static final int DOWN_FAIL = 3;  
    
    private static final int DOWN_OVER_FAKE = 4;
    
    private int progress;  
      
    private Thread downLoadThread;  
      
    private Thread downLogoThread;  
    
    private boolean interceptFlag = false;  
      
    private int option = 0;
    
    private Handler downLogoHandler;
    
    private Handler mHandler = new Handler(){  
        public void handleMessage(Message msg) { 
			if(Base.OBDApp == null || Base.OBDApp.getActivityBack() != Base.APP_RUN_FOREGROUND){
				return;
			}
            switch (msg.what) {  
            case DOWN_UPDATE:  
                mProgress.setProgress(progress);  
                break;  
            case DOWN_OVER:                    
                //installApk();
                //downloadDialog.cancel();
            	showNoticeDialog();
                break;  
            case DOWN_FAIL:
            	if(downloadDialog != null)
            		downloadDialog.cancel();
        		Toast.makeText(mContext, R.string.update_failed, Toast.LENGTH_SHORT).show();
        		break;
            case DOWN_OVER_FAKE:
            	if(downloadDialog != null)
            		downloadDialog.dismiss();
            	installApk();
            	break;
            default:  
                break;  
            }  
        };  
    };  
      
    public UpdateManager(Context context, String url) {  
        this.mContext = context;  
        apkUrl = url;
    }  
      
    //外部接口让主Activity调用  
    public void checkUpdateInfo(){  
        showNoticeDialog();  
    }  
    
    public int checkVersion(String serverVersion) {
        int ret = 0;
        String clientVersion = "1";

        //clientVersion = mActivity.getResources().getString(R.string.clientVersion);

        Log.d(TAG, "clientVersion=" + clientVersion);
        Log.d(TAG, "serverVersion=" + serverVersion);

        try {
            String[] clietVer = clientVersion.split("\\.");
            String[] serverVer = serverVersion.split("\\.");
            //            Log.d(TAG, "clientVersion=" + Arrays.toString(clietVer));
            //            Log.d(TAG, "serverVersion=" + serverVer.toString());
            if (clietVer.length == serverVer.length) {
                for (int i = 0; i < clietVer.length; i++) {
                    ret = Integer.valueOf(clietVer[i]).compareTo(Integer.valueOf(serverVer[i]));
                    if (ret != 0) {
                        break;
                    }
                }
            } else {
                Log.d(TAG, "Illeage version definition!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "Illeage version definition!");
        }

        return ret;
    }
    
      
    private void showNoticeDialog(){  
    	String updateMsg = mContext.getResources().getString(R.string.update_msg);
    	updateMsg += "\n" + ((Base)mContext).updateDesc;
        AlertDialog.Builder builder = new Builder(mContext);  
        builder.setTitle(R.string.update_version);  
        builder.setMessage(updateMsg);  
        builder.setPositiveButton(R.string.update, new OnClickListener() {           
            @Override  
            public void onClick(DialogInterface dialog, int which) {  
                dialog.dismiss();  
                //showDownloadDialog();     
                //installApk();
                option = 2;
                progress = 0;
                downLoadThread = new Thread(mdownApkRunnable);  
                downLoadThread.start(); 
                AlertDialog.Builder builder = new Builder(mContext);  
                builder.setTitle(R.string.update_version);  
                  
                final LayoutInflater inflater = LayoutInflater.from(mContext);  
                View v = inflater.inflate(R.layout.progress, null);  
                mProgress = (ProgressBar)v.findViewById(R.id.progress);  
                 
                builder.setView(v);  
                builder.setNegativeButton(R.string.string_cancel, new OnClickListener() {   
                    @Override  
                    public void onClick(DialogInterface dialog, int which) {  
                        dialog.dismiss();  
                        interceptFlag = true;  
                    }  
                });  
                downloadDialog = builder.create();  
                downloadDialog.show(); 
            }  
        });  
        builder.setNegativeButton(R.string.update_next, new OnClickListener() {             
            @Override  
            public void onClick(DialogInterface dialog, int which) {  
                dialog.dismiss();                 
            }  
        });  
        noticeDialog = builder.create();  
        if(noticeDialog != null)
        	noticeDialog.show();  
    }  
      
    public void setDownloadInterupt(){
    	interceptFlag = true;
    }
    
    private void showDownloadDialog(){  
        AlertDialog.Builder builder = new Builder(mContext);  
        builder.setTitle(R.string.update_version);  
          
        final LayoutInflater inflater = LayoutInflater.from(mContext);  
        View v = inflater.inflate(R.layout.progress, null);  
        mProgress = (ProgressBar)v.findViewById(R.id.progress);  
         
        builder.setView(v);  
        builder.setNegativeButton(R.string.string_cancel, new OnClickListener() {   
            @Override  
            public void onClick(DialogInterface dialog, int which) {  
                dialog.dismiss();  
                interceptFlag = true;  
            }  
        });  
        downloadDialog = builder.create();  
        downloadDialog.show();  
          
        downloadApk();  
    }  
      
	private Runnable mdownApkRunnable = new Runnable() {  
    	boolean ret = false;
        @Override  
        public void run() {  
        	if(option == 1){
	            try {  
//	            	URL url = new URL(URLEncoder.encode(apkUrl, "UTF-8"));
	            	URL url = new URL(apkUrl);
	                HttpURLConnection conn = (HttpURLConnection)url.openConnection();  
	                conn.connect();  
	                //int length = conn.getContentLength();  
	                InputStream is = conn.getInputStream();  
	                  
	                File file = new File(savePath);  
	                if(!file.exists()){  
	                    file.mkdir();  
	                }  
	                String apkFile = saveFileName;  
	                File ApkFile = new File(apkFile);  
	                if(!ApkFile.exists()){
	                    ApkFile.createNewFile();
	                }
	                FileOutputStream fos = new FileOutputStream(ApkFile);  
	                  
	                int count = 0;  
	                byte buf[] = new byte[1024];  
	                  
	                do{                   
	                    int numread = is.read(buf);  
	                    count += numread;  
	                    progress =(int)(((float)count / Base.OBDApp.downApkLen) * 100);  
	                    //更新进度  
	                    //mHandler.sendEmptyMessage(DOWN_UPDATE);  
	                    if(numread <= 0){      
	                        //下载完成通知安装  
	                        mHandler.sendEmptyMessage(DOWN_OVER);  
	                        break;  
	                    }  
	                    fos.write(buf,0,numread);  
	        			if(Base.OBDApp == null || Base.OBDApp.getActivityBack() != Base.APP_RUN_FOREGROUND){
	    	                fos.close();  
	    	                is.close();  
	    	                ret = false;
	        				return;
	        			}
	                }while(!interceptFlag);//点击取消就停止下载.  
	                  
	                fos.close();  
	                is.close();  
	                ret = true;
	            } catch (MalformedURLException e) {  
	                e.printStackTrace(); 
	                ret = false;               
	            } catch(IOException e){  
	                e.printStackTrace();
	                ret = false;
	            }finally{
	            	if(!ret){
//	            		mHandler.sendEmptyMessage(DOWN_FAIL);	            		
	            	}
	            }
        	}
        	else if(option == 2){ 
        		while(progress != 100){
	        		progress += 5;
	        		mHandler.sendEmptyMessage(DOWN_UPDATE);
	        		
	        		if(progress == 100){
	        			mHandler.sendEmptyMessage(DOWN_OVER_FAKE);	
	        		}
	        		try {
						Thread.sleep(200L);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
        		}
        	}  
        }  
    };  
     /** 
     * 下载apk 
     * @param url 
     */  
      
    public void downloadApk(){  
    	option = 1;
        downLoadThread = new Thread(mdownApkRunnable);  
        downLoadThread.start();  
    }  
     /** 
     * 安装apk 
     * @param url 
     */  
    private void installApk(){  
        File apkfile = new File(saveFileName);  
        if (!apkfile.exists()) {  
            return;  
        }      
        Intent i = new Intent(Intent.ACTION_VIEW);  
        i.setDataAndType(Uri.parse("file://" + apkfile.toString()), "application/vnd.android.package-archive");   
        mContext.startActivity(i);  
        //?
        //Preference.getInstance(mContext.getApplicationContext()).setVersion(((Base)mContext).mVersion);
      
    }  
}  
