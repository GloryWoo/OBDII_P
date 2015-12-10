package com.harman.hkwirelessapi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.util.Log;


public class Util {
	
	public static final int MSG_PCM_INIT = 0;
	public static final int MSG_PCM_PLAY = 1;
	public static final int MSG_PCM_PAUSE = 2;
	public static final int MSG_PCM_RESUME = 3;
	public static final int MSG_PCM_STOP = 4;
	public static final int MSG_PCM_SNDREC_PLAY = 5;
	public static final int MSG_PCM_SNDREC_RECORD = 6;
	public static final int MSG_PCM_SNDREC_STOP =7;
	
	public static final String MSG_TYPE_MUSIC = "msg";
	public static final String MSG_URL_MUSIC = "url";
	public static final String MSG_FORMAT_MUSIC = "format";
	
	public static final String MUSICPLAYER = "com.pcm.codec.MusicPlayer";

	public enum MusicFormat {
		MUSIC_TYPE_MP3,
		MUSIC_TYPE_WAV,
		MUSIC_TYPE_AAC,
		MUSIC_TYPE_FLAC,
		MUSIC_TYPE_M4A,
		MUSIC_TYPE_OGG
	}
	public static final Map<String,MusicFormat> supportMusicFormat = new HashMap<String,MusicFormat>();
	static {
		supportMusicFormat.put(".mp3", MusicFormat.MUSIC_TYPE_MP3);
		supportMusicFormat.put(".wav", MusicFormat.MUSIC_TYPE_WAV);
		supportMusicFormat.put(".aac", MusicFormat.MUSIC_TYPE_AAC);
		supportMusicFormat.put(".flac", MusicFormat.MUSIC_TYPE_FLAC);
		supportMusicFormat.put(".m4a", MusicFormat.MUSIC_TYPE_M4A);
		supportMusicFormat.put(".ogg", MusicFormat.MUSIC_TYPE_OGG);
	};
	
	public class DeviceData {
		public DeviceObj deviceObj;
		public Boolean status;
	}
	
	private List<DeviceData> devices = new ArrayList<DeviceData>();
	private static Util instance = new Util();
	private static int musicTimeElapse = 0;
	
	private static boolean isInit = false;
	
	private Util() {
	}
	
	public static Util getInstance() {
		return instance;
	}			
	
	public Boolean hasDeviceConnected() {
		synchronized (this) {
			int i;

			for (i=0; i<devices.size(); i++) {
				if (devices.get(i).status)
					return true;
			}
			return false;
		}
	}

	public List<DeviceData> getDevices() {
		synchronized (this) {  
			return devices;
		}
	}
	
	/*public ArrayList<Boolean> getDevicesStatus() {
		synchronized (this) {  
			return checkedItem;
		}
	}

	public void setDeviceStatus(int position, boolean status) {
		synchronized (this) {
			checkedItem.set(position, status);
		}
	}*/

	public boolean getDeviceStatus(int position) {
		synchronized (this) {
			return devices.get(position).status;
		}
	}
	
	public void setMusicTimeElapse(int time) {
		musicTimeElapse = time;
	}
	public int getMusicTimeElapse() {
		return musicTimeElapse;
	}
}
