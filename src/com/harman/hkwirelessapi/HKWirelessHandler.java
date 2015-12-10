package com.harman.hkwirelessapi;

/**
 * Created by lee on 15/4/27.
 */
public class HKWirelessHandler {

    private HKWirelessController m_wireless = new HKWirelessController();
    private HKWirelessListener m_listener = null;

    public int HKW_INIT_SUCCESS = 0;
    public int HKW_INIT_FAILURE_LICENSE_INVALID = -1;

    void HKWirelessAPI(){
    }

    public int initializeHKWirelessController(String key){
        return m_wireless.InitHKWireless(key);
    }

    public boolean isInitialized(){
        return m_wireless.IsInitialized();
    }

    public void registerHKWirelessControllerListener(HKWirelessListener listener){
        m_wireless.registerHKWirelessControllerListener(listener);
    }

    public void refreshDeviceInfoOnce(){
        m_wireless.RefreshDeviceInfoOnce();
    }

    public void startRefreshDeviceInfo(){
        m_wireless.StartRefreshDeviceInfo();
    }

    public void stopRefreshDeviceInfo(){
        m_wireless.StopRefreshDeviceInfo();
    }

    public boolean addDeviceToSession(long id){
        return m_wireless.AddDeviceToSession(id);
    }
    public boolean removeDeviceFromSession(long deviceid){
        return m_wireless.RemoveDeviceFromSession(deviceid);
    }


    public int getGroupCount(){
        return m_wireless.GetGroupCount();
    }

    public int getDeviceCountInGroupIndex(int groupIndex){
        return m_wireless.GetDeviceCountInGroupIndex(groupIndex);
    }

    public int getDeviceCount(){
        return m_wireless.GetDeviceCount();
    }

    public DeviceObj getDeviceInfoFromTable(int groupIndex, int deviceIndex){
        return m_wireless.GetDeviceInfoFromTable(groupIndex, deviceIndex);
    }

    public DeviceObj getDeviceInfoByIndex(int deviceIndex){
        return m_wireless.GetDeviceInfoByIndex(deviceIndex);
    }

    public GroupObj findDeviceGroupWithDeviceId(long deviceId){
        return m_wireless.FindGroupWithDeviceId(deviceId);
    }

    public DeviceObj findDeviceFromList(long deviceId){
        return m_wireless.FindDeviceFromList(deviceId);
    }

    public boolean isDeviceActive(long deviceId){
        return m_wireless.IsDeviceActive(deviceId);
    }

    public void removeDeviceFromGroup(long groupId, long deviceId){
        m_wireless.RemoveDeviceFromGroup(groupId, deviceId);
    }

    public GroupObj getDeviceGroupByIndex(int groupIndex){
        return m_wireless.GetGroupByIndex(groupIndex);
    }

    public GroupObj getDeviceGroupById(long groupId){
        return m_wireless.GetGroupById(groupId);
    }

    public String getDeviceGroupNameByIndex(int groupIndex){
        return m_wireless.GetGroupNameByIndex(groupIndex);
    }

    public long getDeviceGroupIdByIndex(int groupIndex){
        return m_wireless.GetGroupIdByIndex(groupIndex);
    }

    public void setDeviceName(long deviceId, String deviceName){
        m_wireless.SetDeviceName(deviceId, deviceName);
    }

    public void setDeviceGroupName(long deviceId, String groupName){
        m_wireless.SetDeviceGroupName(deviceId, groupName);
    }

    public void setDeviceRole(long deviceId, int role){
        m_wireless.SetDeviceRole(deviceId, role);
    }

    public int getActiveDeviceCount(){
        return m_wireless.GetActiveDeviceCount();
    }

    public int getActiveGroupCount(){
        return m_wireless.GetActiveGroupCount();
    }

    public void refreshDeviceWiFiSignal(long deviceId){
        m_wireless.RefreshDeviceWiFiSignal(deviceId);
    }

    public HKWifiSingalStrength getWifiSignalStrengthType(int wifiSignal){
        HKWifiSingalStrength type =  HKWifiSingalStrength.values()[m_wireless.GetWifiSignalStrengthType(wifiSignal)];
        return type;
    }
}
