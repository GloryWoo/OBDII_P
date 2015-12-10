package com.harman.hkwirelessapi;

import android.util.Log;



public class HKWirelessUtil {
    private HKWirelessController hkwireless = new HKWirelessController();
    
    private static HKWirelessUtil instance = new HKWirelessUtil();
    
    private static final String KEY = "2FA8-2FD6-C27D-47E8-A256-D011-3751-2BD6";
    
    private HKWirelessUtil() {

    }
    
    public static HKWirelessUtil getInstance() {
    	return instance;
    }
    
    public void initializeHKWirelessController() {
    	hkwireless.InitHKWireless(KEY);
    }
    
    public boolean isInitialized() {
    	return hkwireless.IsInitialized();
    }
    
    public void registerHKWirelessControllerListener(HKWirelessListener listener){
    	hkwireless.registerHKWirelessControllerListener(listener);
    }

    public void refreshDeviceInfoOnce(){
    	hkwireless.RefreshDeviceInfoOnce();
    }

    public void startRefreshDeviceInfo(){
    	hkwireless.StartRefreshDeviceInfo();
    }

    public void stopRefreshDeviceInfo(){
    	hkwireless.StopRefreshDeviceInfo();
    }

    public boolean addDeviceToSession(long id){
        return hkwireless.AddDeviceToSession(id);
    }

    public boolean removeDeviceFromSession(long deviceid){
        return hkwireless.RemoveDeviceFromSession(deviceid);
    }

    public int getGroupCount(){
        return hkwireless.GetGroupCount();
    }

    public long getDeviceCountInGroupIndex(int groupIndex){
        return hkwireless.GetGroupIdByIndex(groupIndex);
    }

    public int getDeviceCount(){
        return hkwireless.GetDeviceCount();
    }

    public DeviceObj getDeviceInfoFromTable(int groupIndex, int deviceIndex){
        return hkwireless.GetDeviceInfoFromTable(groupIndex, deviceIndex);
    }

    public DeviceObj getDeviceInfoByIndex(int deviceIndex){
        return hkwireless.GetDeviceInfoByIndex(deviceIndex);
    }

    public GroupObj findDeviceGroupWithDeviceId(long deviceId){
        return hkwireless.FindGroupWithDeviceId(deviceId);
    }

    public DeviceObj findDeviceFromList(long deviceId){
        return hkwireless.FindDeviceFromList(deviceId);
    }

    public boolean isDeviceActive(long deviceId){
        return hkwireless.IsDeviceActive(deviceId);
    }

    public void removeDeviceFromGroup(int groupId, long deviceId){
    	hkwireless.RemoveDeviceFromGroup(groupId, deviceId);
    }

    public GroupObj getDeviceGroupByIndex(int groupIndex){
        return hkwireless.GetGroupByIndex(groupIndex);
    }

    public GroupObj getDeviceGroupById(int groupId){
        return hkwireless.GetGroupById(groupId);
    }

    public String getDeviceGroupNameByIndex(int groupIndex){
        return hkwireless.GetGroupNameByIndex(groupIndex);
    }

    public long getDeviceGroupIdByIndex(int groupIndex){
        return hkwireless.GetGroupIdByIndex(groupIndex);
    }

    public void setDeviceName(long deviceId, String deviceName){
    	hkwireless.SetDeviceName(deviceId, deviceName);
    }

    public void setDeiceGroupName(int groupId, String groupName){
    	hkwireless.SetDeviceGroupName(groupId, groupName);
    }

    public void setDeviceRole(long deviceId, int role){
    	hkwireless.SetDeviceRole(deviceId, role);
    }

    public int getActiveDeviceCount(){
        return hkwireless.GetActiveDeviceCount();
    }

    public int getActiveGroupCount(){
        return hkwireless.GetActiveGroupCount();
    }

    public void refreshDeviceWiFiSignal(long deviceId){
    	hkwireless.RefreshDeviceWiFiSignal(deviceId);
    }

    /*public HKWifiSingalStrength getWifiSignalStrengthType(int wifiSignal){
        HKWifiSingalStrength type =  HKWifiSingalStrength.values()[m_wireless.GetWifiSignalStrengthType(wifiSignal)];
        return type;
    }*/
    
}
