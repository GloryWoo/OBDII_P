package com.harman.ctg.monitor.fragments;

import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.ctg.ui.R;
import com.harman.ctg.monitor.models.FileModel;
import com.jmolsmobile.landscapevideocapture.VideoFile;
import com.jmolsmobile.landscapevideocapture.camera.CameraWrapper;
import com.jmolsmobile.landscapevideocapture.configuration.CaptureConfiguration;
import com.jmolsmobile.landscapevideocapture.configuration.PredefinedCaptureConfigurations;
import com.jmolsmobile.landscapevideocapture.recorder.LoopVideoRecorder;
import com.jmolsmobile.landscapevideocapture.recorder.VideoRecorderInterface;

import java.util.ArrayList;
import java.util.Date;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by XiaXu on 2015-8-7.
 */
public class MonitorFragment extends Fragment implements VideoRecorderInterface, LoopVideoRecorder.LoopVideoRecorderListener, SensorEventListener {
    @Bind(R.id.vr_monitortureview)
    FrameLayout bk_preview;

    private static final String ARG_SECTION_NUMBER  = "section_number";

    public static final String	DEFAULT_PREFIX		= "video_";
    public static final String	DEFAULT_EXTENSION	= ".mp4";
    private static final int DEFAULT_MAXFILENUMS    = 6;
    private static final int DEFAULT_MAXDURATION    = 5*60;

    private static final int RECORDING_NOTIFICATION_ID  = 1001;

    private static final String	SAVE_VIDEOFILE_NUMS		= "com.harman.ctg.roadstyle.filenums";
    private static final String SAVE_MAX_FILEDURATION   = "com.harman.ctg.roadstyle.maxfileduration";
    private static final String	SAVE_VIDEOFILE_INDEX	= "com.harman.ctg.roadstyle.fileindex";

    private int                     fileNums = DEFAULT_MAXFILENUMS;
    private int                     maxFileDuration = DEFAULT_MAXDURATION;
    private int                     fileIndex = 0;
    private CaptureConfiguration    captureConfiguration;
    private Realm                   realm;

    private boolean                 monitoring = false;
    private ArrayList<VideoFile>    videoFileList = new ArrayList<VideoFile>();
    private LoopVideoRecorder       loopVideoRecorder = null;

    private NotificationCompat.Builder builder;
    private SharedPreferences          pref;
    private static Context             context;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment MonitorFragment.
     */
    public static MonitorFragment newInstance(Context ctx, int sectionNumber) {
        MonitorFragment fragment = new MonitorFragment();
        context = ctx;
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public MonitorFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        realm = Realm.getInstance(context.getApplicationContext());

        ((SensorManager) context.getSystemService(Context.SENSOR_SERVICE)).registerListener(this,
                ((SensorManager) context.getSystemService(Context.SENSOR_SERVICE)).getDefaultSensor(Sensor.TYPE_PROXIMITY),
                SensorManager.SENSOR_DELAY_NORMAL);

        builder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_stat_av_videocam)
                .setContentTitle("Video Recording...")
                .setContentText("Car Monitor")
                .setOngoing(true);

        pref = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.monitor_monitor, container, false);
        ButterKnife.bind(this, view);

        fileNums        = pref.getInt(SAVE_VIDEOFILE_NUMS, DEFAULT_MAXFILENUMS);
        maxFileDuration = pref.getInt(SAVE_MAX_FILEDURATION, DEFAULT_MAXDURATION);
        fileIndex       = pref.getInt(SAVE_VIDEOFILE_INDEX, 0);

        SurfaceView preview = new SurfaceView(context.getApplicationContext());
        bk_preview.addView(preview);

        return view;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(isVisibleToUser) {
            bk_preview.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

        if (loopVideoRecorder != null) {
            loopVideoRecorder.stopRecording(null);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        realm.close();

        ((SensorManager) context.getSystemService(Context.SENSOR_SERVICE)).unregisterListener(this);
    }

    private void initializeLoopRecorder() {
        // insert new file mode to realm
        String appName = getResources().getString(R.string.monitor_name);
        int sz = realm.allObjects(FileModel.class).size();
        if(sz != fileNums) {
            realm.beginTransaction();
            realm.allObjects(FileModel.class).clear();
            realm.commitTransaction();

            realm.beginTransaction();
            for(int n=0; n<fileNums; n++) {
                FileModel f = realm.createObject(FileModel.class);
                f.setSubdir(appName);
                f.setFilename(DEFAULT_PREFIX + String.valueOf(n) + DEFAULT_EXTENSION);
                f.setDate(new Date());
                f.setLocked(false);
            }
            realm.commitTransaction();

            //update
            if(sz > fileNums) {
                fileIndex = 0;

                SharedPreferences.Editor edit = pref.edit();
                edit.putInt(SAVE_VIDEOFILE_INDEX, fileIndex);
                edit.commit();
            }
        }
        realm.refresh();

        // restrive
        videoFileList.clear();
        for (FileModel f : realm.allObjects(FileModel.class)) {
            videoFileList.add(new VideoFile(f.getSubdir(), f.getFilename(), f.getDate(), f.getLocked()));
        }

        captureConfiguration =  createCaptureConfiguration();
    }

    private CaptureConfiguration createCaptureConfiguration() {
        final PredefinedCaptureConfigurations.CaptureResolution resolution = getResolution(1); //{ "1080p", "720p", "480p" };
        final PredefinedCaptureConfigurations.CaptureQuality quality = getQuality(2); //{ "high", "medium", "low" };
        int fileDuration = maxFileDuration;  //CaptureConfiguration.NO_DURATION_LIMIT;
        int fileSize = CaptureConfiguration.NO_FILESIZE_LIMIT;
        return new CaptureConfiguration(resolution, quality, fileDuration, fileSize);
    }

    private PredefinedCaptureConfigurations.CaptureQuality getQuality(int position) {
        final PredefinedCaptureConfigurations.CaptureQuality[] quality = new PredefinedCaptureConfigurations.CaptureQuality[] { PredefinedCaptureConfigurations.CaptureQuality.HIGH, PredefinedCaptureConfigurations.CaptureQuality.MEDIUM,
                PredefinedCaptureConfigurations.CaptureQuality.LOW };
        return quality[position];
    }

    private PredefinedCaptureConfigurations.CaptureResolution getResolution(int position) {
        final PredefinedCaptureConfigurations.CaptureResolution[] resolution = new PredefinedCaptureConfigurations.CaptureResolution[] { PredefinedCaptureConfigurations.CaptureResolution.RES_1080P,
                PredefinedCaptureConfigurations.CaptureResolution.RES_720P, PredefinedCaptureConfigurations.CaptureResolution.RES_480P };
        return resolution[position];
    }

    private void startLoopRecorder() {
        if(!monitoring) {
            initializeLoopRecorder();

            if (loopVideoRecorder == null) {
                bk_preview.removeAllViews();
                SurfaceView preview = new SurfaceView(context.getApplicationContext());
                bk_preview.addView(preview);
                loopVideoRecorder = new LoopVideoRecorder(this, captureConfiguration, videoFileList, fileIndex, new CameraWrapper(), preview.getHolder());
                loopVideoRecorder.setLoopVideoRecorderListener(this);
            }

            // start recroding auto
            Handler handler = new Handler();
            final Runnable r = new Runnable() {
                public void run() {
                    loopVideoRecorder.toggleRecording();  // start
                    monitoring = true;
                }
            };
            handler.postDelayed(r, 500);
        }
    }

    @Override
    public void onRecordingStopped(String file) {
        //no-op
    }

    @Override
    public void onRecordingStarted() {
        NotificationManager notifier = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        notifier.notify(RECORDING_NOTIFICATION_ID, builder.build());
    }

    @Override
    public void onRecordingSuccess() {
        //no-op
    }

    @Override
    public void onRecordingFailed(String message) {
        //no-op
    }

    @Override
    public void onLoopRecordingStopped(ArrayList<VideoFile> videoFileList, int fileIndex) {
        videoFileList.clear();
        for (VideoFile f : videoFileList) {
            videoFileList.add(new VideoFile(f.getSubDirectory(), f.getFilename(), f.getDate(), f.getLocked()));
        }

        realm.beginTransaction();
        for (VideoFile f : videoFileList) {
            RealmResults<FileModel> r = realm.where(FileModel.class).equalTo("filename", f.getFilename()).findAll();
            r.first().setDate(f.getDate());
            r.first().setLocked(f.getLocked());
        }
        realm.commitTransaction();

        this.fileIndex = (fileIndex + 1) % fileNums;
        SharedPreferences.Editor edit = pref.edit();
        edit.putInt(SAVE_VIDEOFILE_INDEX, fileIndex);
        edit.commit();

        if(loopVideoRecorder != null) {
            loopVideoRecorder.releaseAllResources();
            loopVideoRecorder = null;

            if(bk_preview != null) {
                bk_preview.removeAllViews();
            }
        }

        ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).cancel(RECORDING_NOTIFICATION_ID);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.values[0] == 0) { //near
            if (loopVideoRecorder != null) {
                Toast.makeText(context.getApplicationContext(), "Lock the video file.", Toast.LENGTH_LONG).show();
                loopVideoRecorder.lockVideoFile();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //no-op
    }

    ///////////////////////////////////////////////////////////////////////////////
    // interfaces for caller
    public void config(int fileNums, int maxFileDuration) {
        this.fileNums = fileNums;
        this.maxFileDuration = maxFileDuration;

        SharedPreferences.Editor edit = pref.edit();
        edit.putInt(SAVE_VIDEOFILE_NUMS, fileNums);
        edit.putInt(SAVE_MAX_FILEDURATION, maxFileDuration);
        edit.commit();
    }

    public void start() {
        startLoopRecorder();
    }

    public void stop() {
        if ((loopVideoRecorder != null) && (monitoring)) {
            loopVideoRecorder.stopRecording(null);
            monitoring = false;
        }
    }
}
