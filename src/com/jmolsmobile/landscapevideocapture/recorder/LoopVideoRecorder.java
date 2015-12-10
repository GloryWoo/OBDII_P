package com.jmolsmobile.landscapevideocapture.recorder;

import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.view.Surface;
import android.view.SurfaceHolder;

import com.jmolsmobile.landscapevideocapture.CLog;
import com.jmolsmobile.landscapevideocapture.VideoFile;
import com.jmolsmobile.landscapevideocapture.camera.CameraWrapper;
import com.jmolsmobile.landscapevideocapture.camera.OpenCameraException;
import com.jmolsmobile.landscapevideocapture.camera.PrepareCameraException;
import com.jmolsmobile.landscapevideocapture.camera.RecordingSize;
import com.jmolsmobile.landscapevideocapture.configuration.CaptureConfiguration;
import com.jmolsmobile.landscapevideocapture.preview.CapturePreview;
import com.jmolsmobile.landscapevideocapture.preview.CapturePreviewInterface;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by XiaXu on 2015-7-14.
 */
public class LoopVideoRecorder implements MediaRecorder.OnInfoListener, CapturePreviewInterface {

    private CameraWrapper mCameraWrapper;
    private final Surface mPreviewSurface;
    private CapturePreview mVideoCapturePreview = null;

    private final CaptureConfiguration mCaptureConfiguration;
    private ArrayList<VideoFile> mVideoFileList;
    private int mFileIndex;
    private VideoFile mVideoFile;

    private MediaRecorder mRecorder;
    private boolean mRecording = false;
    private final VideoRecorderInterface mRecorderInterface;

    private LoopVideoRecorderListener mListener = null;
    public interface LoopVideoRecorderListener {
        void onLoopRecordingStopped(ArrayList<VideoFile> videoFileList, int fileIndex);
    }

    public LoopVideoRecorder(VideoRecorderInterface recorderInterface, CaptureConfiguration captureConfiguration, ArrayList<VideoFile> videoFileList, int fileIndex,
                         CameraWrapper cameraWrapper, SurfaceHolder previewHolder) {
        mCaptureConfiguration = captureConfiguration;
        mRecorderInterface = recorderInterface;
        mVideoFileList = videoFileList;
        mFileIndex = fileIndex;
        mVideoFile = mVideoFileList.get(mFileIndex);
        mCameraWrapper = cameraWrapper;
        mPreviewSurface = previewHolder.getSurface();

        initializeCameraAndPreview(previewHolder);
    }

    public void setLoopVideoRecorderListener(LoopVideoRecorderListener listener) {
        this.mListener = listener;
    }

    protected void initializeCameraAndPreview(SurfaceHolder previewHolder) {
        try {
            mCameraWrapper.openCamera();
        } catch (final OpenCameraException e) {
            e.printStackTrace();
            mRecorderInterface.onRecordingFailed(e.getMessage());
            return;
        }

        mVideoCapturePreview = new CapturePreview(this, mCameraWrapper, previewHolder);
    }

    public void toggleRecording() {
        if (isRecording()) {
            stopRecording(null);
        } else {
            startRecording();
        }
    }

    protected void startRecording() {
        mRecording = false;

        if (!initRecorder()) return;
        if (!prepareRecorder()) return;
        if (!startRecorder()) return;

        mVideoFile.setData(new Date());
        mVideoFileList.set(mFileIndex, mVideoFile);

        mRecording = true;
        mRecorderInterface.onRecordingStarted();
        CLog.d(CLog.RECORDER, "Successfully started recording - outputfile: " + mVideoFile.getFullPath());
    }

    public void stopRecording(String message) {
        if (!isRecording()) return;

        if(getMediaRecorder() != null) {
            try {
                getMediaRecorder().setOnErrorListener(null);
                getMediaRecorder().setPreviewDisplay(null);
                getMediaRecorder().stop();
                if (message == null) {
                    mRecorderInterface.onRecordingSuccess();
                }
                CLog.d(CLog.RECORDER, "Successfully stopped recording - outputfile: " + mVideoFile.getFullPath());
            } catch (final RuntimeException e) {
                CLog.d(CLog.RECORDER, "Failed to stop recording");
            }

            mRecording = false;
            mRecorderInterface.onRecordingStopped(mVideoFile.getFullPath());

            if ((message == null) & (mListener != null)) {
                mListener.onLoopRecordingStopped(mVideoFileList, mFileIndex);
            }
        }
    }

    private boolean initRecorder() {
        try {
            mCameraWrapper.prepareCameraForRecording();
        } catch (final PrepareCameraException e) {
            e.printStackTrace();
            mRecorderInterface.onRecordingFailed("Unable to record video");
            CLog.e(CLog.RECORDER, "Failed to initialize recorder - " + e.toString());
            return false;
        }

        if (getMediaRecorder() == null) {
            mRecorder = new MediaRecorder();
            mRecorder.setOnErrorListener(null);
        } else {
            getMediaRecorder().reset();
        }

        configureMediaRecorder(getMediaRecorder(), mCameraWrapper.getCamera());

        CLog.d(CLog.RECORDER, "MediaRecorder successfully initialized");
        return true;
    }

    @SuppressWarnings("deprecation")
    protected void configureMediaRecorder(final MediaRecorder recorder, android.hardware.Camera camera) throws IllegalStateException, IllegalArgumentException {
        recorder.setCamera(camera);
        recorder.setAudioSource(mCaptureConfiguration.getAudioSource());
        recorder.setVideoSource(mCaptureConfiguration.getVideoSource());

        CamcorderProfile baseProfile = mCameraWrapper.getBaseRecordingProfile();
        baseProfile.fileFormat = mCaptureConfiguration.getOutputFormat();
        baseProfile.duration = mCaptureConfiguration.getMaxCaptureDuration();

        RecordingSize size = mCameraWrapper.getSupportedRecordingSize(mCaptureConfiguration.getVideoWidth(), mCaptureConfiguration.getVideoHeight());
        baseProfile.videoFrameWidth = size.width;
        baseProfile.videoFrameHeight = size.height;
        baseProfile.videoBitRate = mCaptureConfiguration.getVideoBitrate();

        baseProfile.audioCodec = mCaptureConfiguration.getAudioEncoder();
        baseProfile.videoCodec = mCaptureConfiguration.getVideoEncoder();

        recorder.setProfile(baseProfile);
        recorder.setOutputFile(mVideoFile.getFullPath());

        if(mCaptureConfiguration.getMaxCaptureFileSize() != CaptureConfiguration.NO_FILESIZE_LIMIT) {
            try {
                recorder.setMaxFileSize(mCaptureConfiguration.getMaxCaptureFileSize());
            } catch (IllegalArgumentException e) {
                CLog.e(CLog.RECORDER, "Failed to set max filesize - illegal argument: " + mCaptureConfiguration.getMaxCaptureFileSize());
            } catch (RuntimeException e2) {
                CLog.e(CLog.RECORDER, "Failed to set max filesize - runtime exception");
            }
        }

        // added by xxm, for loop recording mode
        if(mCaptureConfiguration.getMaxCaptureDuration() != CaptureConfiguration.NO_DURATION_LIMIT) {
            try {
                recorder.setMaxDuration(mCaptureConfiguration.getMaxCaptureDuration());
            } catch (IllegalArgumentException e) {
                CLog.e(CLog.RECORDER, "Failed to set max duration - illegal argument: " + mCaptureConfiguration.getMaxCaptureDuration());
            } catch (RuntimeException e2) {
                CLog.e(CLog.RECORDER, "Failed to set max duration - runtime exception");
            }
        }

        recorder.setOnInfoListener(this);
    }

    private boolean prepareRecorder() {
        try {
            getMediaRecorder().prepare();
            CLog.d(CLog.RECORDER, "MediaRecorder successfully prepared");
            return true;
        } catch (final IllegalStateException e) {
            e.printStackTrace();
            CLog.e(CLog.RECORDER, "MediaRecorder preparation failed - " + e.toString());
            return false;
        } catch (final IOException e) {
            e.printStackTrace();
            CLog.e(CLog.RECORDER, "MediaRecorder preparation failed - " + e.toString());
            return false;
        }
    }

    private boolean startRecorder() {
        try {
            getMediaRecorder().start();
            CLog.d(CLog.RECORDER, "MediaRecorder successfully started");
            return true;
        } catch (final IllegalStateException e) {
            e.printStackTrace();
            CLog.e(CLog.RECORDER, "MediaRecorder start failed - " + e.toString());
            return false;
        } catch (final RuntimeException e2) {
            e2.printStackTrace();
            CLog.e(CLog.RECORDER, "MediaRecorder start failed - " + e2.toString());
            mRecorderInterface.onRecordingFailed("Unable to record video with given settings");
            return false;
        }
    }

    protected boolean isRecording() {
        return mRecording;
    }

    protected MediaRecorder getMediaRecorder() {
        return mRecorder;
    }

    public void lockVideoFile() {
        mVideoFile.setLocked(true);
        mVideoFileList.set(mFileIndex, mVideoFile);
    }

    private void setNextVideoFile() {
        int sz = mVideoFileList.size();
        for(int n=1; n<=sz; n++) {
            int fileIndex = (mFileIndex + n) % sz;
            if(!mVideoFileList.get(fileIndex).getLocked()) {
                mFileIndex = fileIndex;
                mVideoFile = mVideoFileList.get(fileIndex);
                break;
            }
        }
    }

    private void releaseRecorderResources() {
        MediaRecorder recorder = getMediaRecorder();
        if (recorder != null) {
            recorder.release();
            recorder = null;
        }
    }

    public void releaseAllResources() {
        if (mVideoCapturePreview != null) {
            mVideoCapturePreview.releasePreviewResources();
        }
        if (mCameraWrapper != null) {
            mCameraWrapper.releaseCamera();
            mCameraWrapper = null;
        }
        releaseRecorderResources();
        CLog.d(CLog.RECORDER, "Released all resources");
    }

    @Override
    public void onCapturePreviewFailed() {
        mRecorderInterface.onRecordingFailed("Unable to show camera preview");
    }

    @Override
    public void onInfo(MediaRecorder mr, int what, int extra) {
        switch (what) {
            case MediaRecorder.MEDIA_RECORDER_INFO_UNKNOWN:
                // NOP
                break;
            case MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED:
                CLog.d(CLog.RECORDER, "MediaRecorder max duration reached");
                stopRecording("Capture looping - Max duration reached");

                // loop recording
                setNextVideoFile();
                startRecording();
                break;
            case MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED:
                CLog.d(CLog.RECORDER, "MediaRecorder max filesize reached");
                stopRecording("Capture looping - Max file size reached");

                // loop recording
                setNextVideoFile();
                startRecording();
                break;
            default:
                break;
        }
    }

}

