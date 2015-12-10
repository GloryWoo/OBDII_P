package com.harman.hkwirelessapi;

import android.media.AudioFormat;
import android.media.AudioTrack;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.AudioTrack.OnPlaybackPositionUpdateListener;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Surface;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.Semaphore;

import com.ctg.plat.music.MusicPlay;
import com.ctg.ui.Base;
import com.harman.play.AudioParam;
import com.harman.play.AudioPlayer;

/**
 * Created by Administrator on 2015/4/11.
 */
public class PcmCodec {

    public final String LOG_TAG = "PcmCodec";
    private static int TIMEOUT_US = 2000;
    MediaCodec codec=null;
    public MediaExtractor extractor = null;
    MediaCodec.BufferInfo info = null;
    Boolean sawInputEOS = false;
    Boolean sawOutputEOS = false;

    MediaFormat format;
    ByteBuffer[] codecInputBuffers;
    ByteBuffer[] codecOutputBuffers;

    long m_PresentationTimeUs = 0;
    public long m_duration = 0;
    boolean isRun = false;
    private PlayThread m_Player = null;
    boolean m_threadEnd = true;
    AudioPlayer audioPlay;
    Semaphore pauseResumeRemp;
   
    Handler mHandler = new Handler()
	{

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch(msg.what)
			{
			case AudioPlayer.STATE_MSG_ID:
				//showState((Integer)msg.obj);
				break;
			}
		}
		
		
	};
	
	public AudioParam getAudioParam()
	{
		AudioParam audioParam = new AudioParam();
    	audioParam.mFrequency = 44100;
    	audioParam.mChannel = AudioFormat.CHANNEL_CONFIGURATION_STEREO;
    	audioParam.mSampBit = AudioFormat.ENCODING_PCM_16BIT;
    	
    	return audioParam;
	}
	
	
    public PcmCodec(){
    	audioPlay = new AudioPlayer(mHandler);
   	 /*
         * 获得PCM音频数据参数
         */
    	AudioParam audioParam = getAudioParam();
    	audioPlay.setAudioParam(audioParam);
    	pauseResumeRemp = new Semaphore(1);
    	
    }

    public long getDuation(){
        return m_duration;
    }

    private boolean configAudioPlayer(String url){
        Log.d(LOG_TAG, "configAudioPlayer start");

        while(!m_threadEnd){
            try{
                Log.d(LOG_TAG, "waiting thread exit :" + isRun);
                Thread.sleep(500);
                /*
                i++;
                if (i > 10){
                    m_Player.interruptThread();
                    m_threadEnd = true;
                    i = 0;
                    deinit();
                    End();
                    Log.d(LOG_TAG, "thread interruptThread");
                }
                */
            }catch (InterruptedException e){
                e.printStackTrace();
            }
        }

        if (extractor == null) {
			extractor = new MediaExtractor();
        }else{
            extractor.release();
            extractor = null;
            extractor = new MediaExtractor();
        }
        try {
			extractor.setDataSource(url);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

        int numTracks = extractor.getTrackCount();
        try {
            format = extractor.getTrackFormat(0);
        }catch (RuntimeException e){
            e.printStackTrace();
            return false;
        }
        
        String mime = format.getString(MediaFormat.KEY_MIME);
        int sampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE);
        m_duration = format.getLong(MediaFormat.KEY_DURATION);
        int channelCnt = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT);
//        int inBitrate = sampleRate * channelCnt * 16;  // bit/sec

        Log.d(LOG_TAG, "===========================");
        Log.d(LOG_TAG, "url " + url);
        Log.d(LOG_TAG, "mime type : " + mime);
        Log.d(LOG_TAG, "numTracks : " + numTracks);
        Log.d(LOG_TAG, "sample rate : " + sampleRate);
        Log.d(LOG_TAG, "channelCnt : " + channelCnt);
        Log.d(LOG_TAG, "duration : " + m_duration);
        Log.d(LOG_TAG, "===========================");

        if (sampleRate < 44100){
            Log.e(LOG_TAG, "sample rate : " + sampleRate + "< 44100");
            formatNotSupportMsg();
            extractor.release();
            extractor = null;
            Log.e(LOG_TAG, "sample rate : " + sampleRate + "< 44100");
            return false;
        }

        codec = MediaCodec.createDecoderByType(mime);

        codec.configure(format, null, null, MediaCodec.CRYPTO_MODE_UNENCRYPTED);
        codec.start();

        codecInputBuffers = codec.getInputBuffers();
        codecOutputBuffers = codec.getOutputBuffers();
        
        extractor.selectTrack(0);
        
        if (info == null){
            info = new MediaCodec.BufferInfo();
        }

        String fName = url.trim();
        String fileName = fName.substring(fName.lastIndexOf("/") + 1);
        String songName = fileName.substring(0, fileName.indexOf("."));
        SetAudioFile(url, songName, m_duration, 16, sampleRate, channelCnt);

        Log.d(LOG_TAG, "configAudioPlayer end");
        return true;
    }

    public boolean play(String url, int startTime){
        Log.d(LOG_TAG, "play start :" + url);

        if (!fileIsExists(url)){
            Log.e(LOG_TAG, "file not exist");
            return false;
        }

        String fName = url.trim();
        String extName = fName.substring(fName.indexOf(".") + 1);
        
        if (!(extName.equalsIgnoreCase("mp3") || extName.equalsIgnoreCase("wav") || 
        		extName.equalsIgnoreCase("flac") || extName.equalsIgnoreCase("acc") ||
        		extName.equalsIgnoreCase("m4a") || extName.equalsIgnoreCase("ogg")))
        {
                formatNotSupportMsg();
                return false;
        }

        if (isRun){
//            if (startTime == 0) {
//                stop();
//            }else{
//                pause();
//            }
            stop();
        }

        boolean ret = configAudioPlayer(url);
        if (!ret){
            Log.e(LOG_TAG, "configAudioPlayer fail");
            return false;
        }

        isRun = true;
        Data.setPlaybackState(true);

        if(startTime!=0){
            playAtTime(startTime * 1000000);
//            if (!PlayAtTime(startTime)){
//                return false;
//            }
        }else{
//            if (!Play()){
//                return false;
//            }
        }

        if (m_Player == null) {
            m_Player = new PlayThread();
        }
        new Thread(m_Player).start();
        
        Log.d(LOG_TAG, "play end");
        return true;
    }

    public void pause(){
        Log.d(LOG_TAG, "pause");        
        audioPlay.mAudioTrack.pause();
        try {
			pauseResumeRemp.acquire();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        //Pause();
    }

    public void resume(){
    	audioPlay.mAudioTrack.play();
    	pauseResumeRemp.release();
    }
    
    public void stop(){
        Log.d(LOG_TAG, "stop");
        if(m_Player == null)
        	return;
        isRun = false;
        if(pauseResumeRemp.availablePermits() == 0)
        	pauseResumeRemp.release();
        m_Player.interruptThread();
        audioPlay.mAudioTrack.stop();
        //Stop();
    }

    public void playAtTime(long startTime){
        Log.d(LOG_TAG, "playAtTime :" + startTime / 1000000);

        extractor.seekTo(startTime, MediaExtractor.SEEK_TO_CLOSEST_SYNC);    
        
        sawInputEOS = false;
        sawOutputEOS = false;
    }

    public void deinit(){
        codec.stop();
        codec.release();
        extractor.release();
        codec = null;
        extractor = null;
        info = null;
        sawInputEOS = false;
        sawOutputEOS = false;
    }

    class PlayThread implements Runnable, AudioTrack.OnPlaybackPositionUpdateListener{

        public synchronized void resumeThread() throws InterruptedException {
            notify();
        }

        public synchronized void waitingThread() throws InterruptedException {
            wait();
        }

        public synchronized void interruptThread(){
            Thread.interrupted();
        }

        
        @Override
        public void run() {

            Log.d(LOG_TAG, "m_Player Thread");
            m_threadEnd = false;
            isRun = true;
            
            audioPlay.prepare();

            audioPlay.mAudioTrack.play();	          
            audioPlay.mAudioTrack.setPlaybackPositionUpdateListener(this);
            audioPlay.mAudioTrack.setNotificationMarkerPosition(1000);
            int i = 0;
            if(pauseResumeRemp.availablePermits() == 0)
            	pauseResumeRemp.release();
            while (!Thread.interrupted()) {

                if (!isRun || !Data.getPlaybackState()){
                    Log.d(LOG_TAG, "Thread stop");
                    break;
                }
                try {
					pauseResumeRemp.acquire();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
                int rate = audioPlay.mAudioTrack.getPlaybackRate();
                int pos = audioPlay.mAudioTrack.getPlaybackHeadPosition();
                int sample = audioPlay.mAudioTrack.getSampleRate();
                int period = audioPlay.mAudioTrack.getPositionNotificationPeriod();
                input();
                boolean ret = output();
//                if(Base.OBDApp.playTimeHandler != null && i++ == 20){
//                	Message msg = new Message();
//                	Bundle bundle = new Bundle();
//                	msg.what = MusicPlay.PLAY_TIME;
//                	bundle.putLong("play_time", extractor.getSampleTime());
//                	bundle.putLong("duration", m_duration);
//                	msg.setData(bundle);
//                	Base.OBDApp.playTimeHandler.sendMessage(msg);
//                	i = 0;
//                }
                if (!ret){
                    isRun = false;
                    break;
                }

                // All decoded frames have been rendered, we can stop playing now
                if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    Log.d(LOG_TAG, "OutputBuffer BUFFER_FLAG_END_OF_STREAM");
                    isRun = false;
                    break;
                }
                pauseResumeRemp.release();
                
            }
            deinit();
            Log.d(LOG_TAG, "thread end");
            //End();
            m_threadEnd = true;
        }

		@Override
		public void onMarkerReached(AudioTrack track) {
			// TODO Auto-generated method stub
			Log.d(LOG_TAG, "audioPlay.mAudioTrack on marker reached");
			
		}

		@Override
		public void onPeriodicNotification(AudioTrack track) {
			// TODO Auto-generated method stub
			Log.d(LOG_TAG, "audioPlay.mAudioTrack on periodic notification");
			
		}
    }


    private int input() {
        int inputBufIndex = codec.dequeueInputBuffer(TIMEOUT_US);
        if (inputBufIndex >= 0) {
            ByteBuffer dstBuf = codecInputBuffers[inputBufIndex];
            int sampleSize = extractor.readSampleData(dstBuf, 0);
            if (sampleSize < 0) {
                Log.d(LOG_TAG, "Saw input end of stream!");
                sawInputEOS = true;
                sampleSize = 0;
            } else {
                m_PresentationTimeUs = extractor.getSampleTime();
//                Log.i(LOG_TAG, "presentationTimeUs " + m_PresentationTimeUs);
            }

            codec.queueInputBuffer(inputBufIndex,
                    0, //offset
                    sampleSize,
                    m_PresentationTimeUs,
                    sawInputEOS ? MediaCodec.BUFFER_FLAG_END_OF_STREAM : 0);//
            if (!sawInputEOS) {
                extractor.advance();
            }else{
                Log.d(LOG_TAG, "sawInputEOS");
            }
        }else{
            Log.d(LOG_TAG, "inputBufIndex = " + inputBufIndex);
        }
        return inputBufIndex;
    }

    private boolean output()
    {
        final int res = codec.dequeueOutputBuffer(info, TIMEOUT_US);
        if (res >= 0) {
            int outputBufIndex = res;
            ByteBuffer buf = codecOutputBuffers[outputBufIndex];

            final byte[] chunk = new byte[info.size];
            buf.get(chunk); // Read the buffer all at once
            buf.clear(); // ** MUST DO!!! OTHERWISE THE NEXT TIME YOU GET THIS SAME BUFFER BAD THINGS WILL HAPPEN

            if (chunk.length > 0) {
                //send buffer
                //if (!SampleBuffer(chunk))
                {
                	audioPlay.setDataSource(chunk); 
                	//audioPlay.prepare();
                	//audioPlay.play();
                	audioPlay.mAudioTrack.write(chunk, 0, info.size);
//                    return false;                      	
                }
            }
            codec.releaseOutputBuffer(outputBufIndex, false /* render */);

            if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                sawOutputEOS = true;
            }
        } else if (res == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
            codecOutputBuffers = codec.getOutputBuffers();
            Log.d(LOG_TAG, "INFO_OUTPUT_BUFFERS_CHANGED");
        } else if (res == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
            final MediaFormat oformat = codec.getOutputFormat();
            Log.d(LOG_TAG, "Output format has changed to " + oformat);
        }
        else{
            Log.d(LOG_TAG, "ret = " + res);
        }
        return true;
    }


    public boolean playWAV(String url){
        if (!fileIsExists(url)){
            Log.e(LOG_TAG, "file not exist");
            return false;
        }

        String fName = url.trim();
        String extName = fName.substring(fName.lastIndexOf(".")+1);

        if (!extName.equalsIgnoreCase("wav"))
        {
                formatNotSupportMsg();
                return false;
        }

        MediaExtractor extractorWav = new MediaExtractor();
        try {
			extractorWav.setDataSource(url);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        MediaFormat formatWav = null;
        int numTracks = extractorWav.getTrackCount();
        if (numTracks > 0){
            formatWav = extractorWav.getTrackFormat(0);

//            String mime = formatWav.getString(MediaFormat.KEY_MIME);
            int sampleRate = formatWav.getInteger(MediaFormat.KEY_SAMPLE_RATE);
//            int channelCnt = formatWav.getInteger(MediaFormat.KEY_CHANNEL_COUNT);
            m_duration = formatWav.getLong(MediaFormat.KEY_DURATION);
            if (sampleRate < 44100) {
                formatNotSupportMsg();
                return false;
            }

            return PlayWav(url);
        }else{
            return false;
        }
    }

    private boolean fileIsExists(String url){
        try{
            File f=new File(url);
            if(!f.exists()){
                return false;
            }
        }catch (Exception e) {
            return false;
        }
        return true;
    }

    private void formatNotSupportMsg(){
        if (Data.handler != null){
            Message msg = new Message();
            msg.what = Data.HANDLER_ERR_MSG;
            msg.arg1 = HKErrorCode.ERROR_MEDIA_UNSUPPORTED.ordinal();
            Bundle bundle = new Bundle();
            bundle.putString("errorMesg","media format is not supported !");
            msg.setData(bundle);
            Data.handler.sendMessage(msg);
        }
    }


    public native void SetAudioFile(String url, String name, long duration, int bit, int rate, int channel);
    public native boolean Play();
    public native boolean PlayAtTime(long startTime);
    public native boolean SampleBuffer(byte[] buf);
    public native void Pause();
    public native void Stop();
    public native void End();
    public native boolean PlayWav(String url);

    public native void SetDeviceVolume  (long deviceId, int volume);
    public native void SetVolumeAll(int volume);
    public native int GetVolume();
    public native int GetDeviceVolume(long deviceId);
    public native int GetMaximumVolumeLevel();

    public native boolean IsPlaying();
    public native int GetPlayerState();
}
