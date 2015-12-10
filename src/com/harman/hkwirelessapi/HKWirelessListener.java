package com.harman.hkwirelessapi;

/**
 * Created by Administrator on 2015/4/11.
 */
public interface HKWirelessListener {
    public void onDeviceStateUpdated(long deviceId, int reason);
    public void onPlaybackStateChanged(int playState);
    public void onVolumeLevelChanged(long deviceId, int deviceVolume, int avgVolume);
    public void onPlayEnded();
    public void onPlaybackTimeChanged(int timeElapsed);
    public void onErrorOccurred(int errorCode, String errorMesg);
}
