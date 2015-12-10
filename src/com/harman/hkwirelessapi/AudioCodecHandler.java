package com.harman.hkwirelessapi;

/**
 * Created by lee on 15/4/27.
 */

public class AudioCodecHandler {

    private PcmCodec m_pcm = new PcmCodec();

    void PcmCodecAPI(){
    }

    public boolean playCAF(String url, String songName, boolean resumeFlag){
        int startTime = 0;
        if (resumeFlag){
            startTime = Data.getTimeElapsed();
        }
        return m_pcm.play(url, startTime);
    }
    public boolean playCAFFromCertainTime(String url, String songName, int startTime){ return m_pcm.play(url, startTime);}
    public void pause(){
        m_pcm.pause();
    }
    public void stop(){
        m_pcm.stop();
    }
    public long getDuation(){
        return m_pcm.getDuation();
    }

    public boolean playWAV(String url){
        return m_pcm.playWAV(url);
    }

    public boolean isPlaying(){
        return m_pcm.IsPlaying();
    }
    public HKPlayerState getPlayerState(){
        return HKPlayerState.EPlayerState_Init;
    }

    public void setVolumeAll(int volume){
        m_pcm.SetVolumeAll(volume);
    }

    public void setVolumeDevice(long deviceId, int volume){
        m_pcm.SetDeviceVolume(deviceId, volume);
    }

    public int getVolume(){
        return m_pcm.GetVolume();
    }

    public int getDeviceVolume(long deviceId){
        return m_pcm.GetDeviceVolume(deviceId);
    }

    public int getMaximumVolumeLevel(){
        return m_pcm.GetMaximumVolumeLevel();
    }
}
