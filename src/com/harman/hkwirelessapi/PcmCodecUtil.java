package com.harman.hkwirelessapi;




public class PcmCodecUtil {

    private PcmCodec pcmCodec = new PcmCodec();
    
    private static PcmCodecUtil instance = new PcmCodecUtil();
    
    private PcmCodecUtil() {

    }
    
    public static PcmCodecUtil getInstance() {
    	return instance;
    }
    
    /*public void configCAFAudioPlayer(String url){
    	pcmCodec.configCAFAudioPlayer(url);
    }*/

    public void play(String url, int timeElapsed){
    	/*
    	if (timeElapsed != 0)
    		pcmCodec.playCAF(url, songName, true);
    	else
    		pcmCodec.playCAF(url, songName, false);
    		*/
    	pcmCodec.play(url, timeElapsed);
    }

    public void resume(){
    	pcmCodec.resume();
    }
    
    public void pause(){
    	pcmCodec.pause();
    }

    public void stop(){
    	pcmCodec.stop();
    }

    public void playWAV(String url){
    	pcmCodec.playWAV(url);
    }

    public boolean isPlaying(){
        return pcmCodec.IsPlaying();
    }

    public int getPlayerState(){
        return pcmCodec.GetPlayerState();
    }

    public void setVolumeAll(int volume){
    	pcmCodec.SetVolumeAll(volume);
    }

    public void setVolumeDevice(long deviceId, int volume){
    	pcmCodec.SetDeviceVolume(deviceId, volume);
    }

    public int getVolume(){
        return pcmCodec.GetVolume();
    }

    public int getDeviceVolume(long deviceId){
        return pcmCodec.GetDeviceVolume(deviceId);
    }

    public int getMaximumVolumeLevel(){
        return pcmCodec.GetMaximumVolumeLevel();
    }
}
