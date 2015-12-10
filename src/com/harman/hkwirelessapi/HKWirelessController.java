package com.harman.hkwirelessapi;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * Created by Administrator on 2015/4/11.
 */
public class HKWirelessController {

    public final String LOG_TAG = "HKWirelessController";
    private HKWirelessListener m_listener = null;

    public HKWirelessController(){
        Data.handler = new Handler(){
            public void handleMessage (Message msg)
            {
                switch(msg.what)
                {
                    case Data.HANDLER_ERR_MSG:
                        String str1 = msg.getData().getString("errorMesg");
                        Log.d(LOG_TAG, "The handler thread id = " + Thread.currentThread().getId() + " " + str1 + " " + msg.arg1);
                        break;
                }
            }
        };
    }

    public void registerHKWirelessControllerListener(HKWirelessListener listener){
        m_listener = listener;
    }

    public void callbackDeviceStateUpdated(long deviceId, int reason){
        Log.d(LOG_TAG, "callbackDeviceStateUpdated");
        if (m_listener != null) {
            m_listener.onDeviceStateUpdated(deviceId, reason);
        }
    }
    public void callbackPlaybackStateChanged(int playState){
        Log.d(LOG_TAG, "callbackPlaybackStateChanged : " + playState);
        if (m_listener != null) {
            m_listener.onPlaybackStateChanged(playState);
        }
        if (playState == HKPlayerState.EPlayerState_Stop.ordinal()){
            Data.setPlaybackState(false);
        }
    }
    public void callbackVolumeLevelChanged(long deviceId, int deviceVolume, int avgVolume){
        Log.d(LOG_TAG, "callbackVolumeLevelChanged : " + deviceVolume);
        if (m_listener != null) {
            m_listener.onVolumeLevelChanged(deviceId, deviceVolume, avgVolume);
        }
    }
    public void callbackPlayEnded(){
        Log.d(LOG_TAG, "callbackPlayEnded");
        if (m_listener != null) {
            m_listener.onPlayEnded();
        }
    }
    public void callbackPlaybackTimeChanged(int timeElapsed){
        Log.d(LOG_TAG, "callbackPlaybackTimeChanged :" + timeElapsed);
        Data.settimeElapsed(timeElapsed);
        if (m_listener != null) {
            m_listener.onPlaybackTimeChanged(timeElapsed);
        }
    }
    public void callbackErrorOccurred(int errorCode, String errorMesg){
        Log.d(LOG_TAG, "callbackErrorOccurred");
        if (m_listener != null) {
            m_listener.onErrorOccurred(errorCode, errorMesg);
        }
    }

    public native int InitHKWireless(String key);
    public native boolean IsInitialized();

    public native void RefreshDeviceInfoOnce();
    public native void StartRefreshDeviceInfo();
    public native void StopRefreshDeviceInfo();

    public native boolean AddDeviceToSession (long id);
    public native boolean RemoveDeviceFromSession (long id);

    public native int GetGroupCount ();
    public native int GetDeviceCountInGroupIndex (int groupIndex);
    public native int GetDeviceCount ();

    public native DeviceObj GetDeviceInfoFromTable (int groupIndex, int deviceIndex);
    public native DeviceObj GetDeviceInfoByIndex (int deviceIndex);
    public native DeviceObj FindDeviceFromList (long deviceId);

    public native GroupObj FindGroupWithDeviceId (long id);
    public native GroupObj GetGroupByIndex (int groupIndex);
    public native GroupObj GetGroupById (long groupId);

    public native boolean IsDeviceActive (long id);
    public native void RemoveDeviceFromGroup (long group, long id);

    public native String GetGroupNameByIndex (int groupIndex);
    public native long GetGroupIdByIndex (int groupIndex);
    public native void SetDeviceName (long deviceId, String deviceName);
    public native void SetDeviceGroupName (long deviceId, String groupName);
    public native void SetDeviceRole (long deviceId, int role);

    public native int GetActiveDeviceCount ();
    public native int GetActiveGroupCount ();
    public native void RefreshDeviceWiFiSignal (long deviceId);
    public native int GetWifiSignalStrengthType (int wifiSignal);

    static {
        System.loadLibrary("HKWirelessHD");
    }
}
