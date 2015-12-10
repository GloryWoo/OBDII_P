package com.harman.hkwirelessapi;






import com.harman.hkwirelessapi.Util.MusicFormat;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class MusicPlayerService extends Service {
	public static final String action = "broadcast.action.play_time";
	private HKWirelessUtil hkwireless = HKWirelessUtil.getInstance();
	private PcmCodecUtil pcmCodec = PcmCodecUtil.getInstance();	
	private String path;
	
	private Thread mThread = null;
	private Handler mHandler = null;

	private static final int PCMCODEC_PLAY = 1;	
	private static final int PCMCODEC_PAUSE = 2;
	private static final int PCMCODEC_RESUME = 3;
	private static final int PCMCODEC_STOP = 4;
	private static final int PCMCODEC_SNDREC_RECORD = 5;
	private static final int PCMCODEC_SNDREC_PLAY = 6;
	private static final int PCMCODEC_SNDREC_STOP = 7;
	
	private static final String URL_FLAG = "url:";
	private static final String LAST_URL_FLAG = "lastUrl:";
	
	//ExtAudioRecorder extRecorder = null;
	HKAudioRecorder hkRecorder = null;

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public void wirelessInit(){
		hkwireless.registerHKWirelessControllerListener(new HKWirelessListener(){

			@Override
			public void onPlayEnded() {
				// TODO Auto-generated method stub
				Util.getInstance().setMusicTimeElapse(0);
				Log.i("HKWirelessListener","onPlayEnded");
//				if (musicPlayerFragment != null) {
//					Message msg = new Message();
//					msg.what = musicPlayerFragment.CMD_NEXT;
//					musicPlayerFragment.handler.sendMessage(msg);
//				}
//				
//				if (soundRecorderFragment != null) {
//					Message msg = new Message();
//					msg.what = soundRecorderFragment.CMD_STOP_PLAYING;
//					soundRecorderFragment.handler.sendMessage(msg);
//				}
			}

			@Override
			public void onPlaybackStateChanged(int arg0) {
				// TODO Auto-generated method stub
//				if (arg0 == HKPlayerState.EPlayerState_Stop.ordinal())
//					Util.getInstance().setMusicTimeElapse(0);
			}

			@Override
			public void onPlaybackTimeChanged(int arg0) {
				// TODO Auto-generated method stub
				Util.getInstance().setMusicTimeElapse(arg0);
			}

			@Override
			public void onVolumeLevelChanged(long deviceId, int deviceVolume,
					int avgVolume) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onDeviceStateUpdated(long deviceId, int reason) {
				// TODO Auto-generated method stub
//				Util.getInstance().updateDeviceInfor(deviceId);
//				if (deviceListFragment != null) {
//					deviceListFragment.handler.sendMessage(new Message());
//				}
//				if (!Util.getInstance().hasDeviceConnected()) {
//					if (musicPlayerFragment != null) {
//						Message msg = new Message();
//						msg.what = musicPlayerFragment.CMD_STOP;
//						musicPlayerFragment.handler.sendMessage(msg);
//					}
//					
//					if (soundRecorderFragment != null) {
//						Message msg = new Message();
//						msg.what = soundRecorderFragment.CMD_STOP_PLAYING;
//						soundRecorderFragment.handler.sendMessage(msg);
//					}
//				}
			}

			@Override
			public void onErrorOccurred(int errorCode, String errorMesg) {
				// TODO Auto-generated method stub
				Log.i("HKWirelessListener","hkwErrorOccurred@arg0="+errorCode+",arg1="+errorMesg);
			}
		});
		
		if (!hkwireless.isInitialized()) {
			hkwireless.initializeHKWirelessController();
			if (hkwireless.isInitialized()) {
				//Toast.makeText(this, "Wireless controller init success", 1000).show();
			} else {
				//Toast.makeText(this, "Wireless controller init fail", 1000).show();
			}
		}
	}
	
	@Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        //recorder = ExtAudioRecorder.getInstanse(false);
        mThread = new PcmCodecThread();
        mThread.start();
//        wirelessInit();
    }
	
	@Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        mThread.stop();
    }

	
	public String geExtfileName(String path) {
		int lastDiv = path.lastIndexOf(".");
		return path.substring(lastDiv, path.length());
	}
	
	public MusicFormat getMusicFormat(String path) {
		String extName = geExtfileName(path);
		return Util.supportMusicFormat.get(extName);
	}

	@Override  
	public int onStartCommand(Intent intent, int flags, int startId) {
		int cmd = 0;
		if (intent != null)
		{
			cmd = intent.getIntExtra(Util.MSG_TYPE_MUSIC, 0);
			if(cmd == Util.MSG_PCM_PLAY) {
				final String url = intent.getStringExtra(Util.MSG_URL_MUSIC);
				Message msg = new Message();
				Bundle bundle = new Bundle();
				bundle.putString(URL_FLAG, url);
				bundle.putString(LAST_URL_FLAG, path);
				msg.what = PCMCODEC_PLAY;
				msg.setData(bundle);
				mHandler.sendMessage(msg);
			} else if(cmd == Util.MSG_PCM_PAUSE) {
				Message msg = new Message();
				msg.what = PCMCODEC_PAUSE;
				mHandler.sendMessage(msg);
				//stop();				
			} 
			else if(cmd == Util.MSG_PCM_RESUME) {
				Message msg = new Message();
				msg.what = PCMCODEC_RESUME;
				mHandler.sendMessage(msg);
				//stop();
			} else if(cmd == Util.MSG_PCM_STOP) {
				Message msg = new Message();
				msg.what = PCMCODEC_STOP;
				mHandler.sendMessage(msg);
				//stop();
			} else if (cmd == Util.MSG_PCM_SNDREC_RECORD) {
				Message msg = new Message();
				msg.what = PCMCODEC_SNDREC_RECORD;
				final String url = intent.getStringExtra(Util.MSG_URL_MUSIC);
				Bundle bundle = new Bundle();
				bundle.putString(URL_FLAG, url);
				msg.setData(bundle);
				mHandler.sendMessage(msg);
			} else if (cmd == Util.MSG_PCM_SNDREC_STOP) {
				Message msg = new Message();
				msg.what = PCMCODEC_SNDREC_STOP;
				mHandler.sendMessage(msg);
			} else if (cmd == Util.MSG_PCM_SNDREC_PLAY) {
				final String url = intent.getStringExtra(Util.MSG_URL_MUSIC);
				Message msg = new Message();
				Bundle bundle = new Bundle();
				bundle.putString(URL_FLAG, url);
				msg.what = PCMCODEC_SNDREC_PLAY;
				msg.setData(bundle);
				mHandler.sendMessage(msg);
			}
		}
		return super.onStartCommand(intent, flags, startId);
	}
	
	class PcmCodecThread extends Thread {
        @Override
        public void run() {
            Looper.prepare();
            mHandler = new Handler(){
                @SuppressWarnings("static-access")
				public void handleMessage (Message msg) {
                	Bundle bundle = msg.getData();
                    switch(msg.what) {
                    case PCMCODEC_PLAY:
                    	String url = bundle.getString(URL_FLAG);
                    	String oldUrl = bundle.getString(LAST_URL_FLAG);
                    	if (oldUrl != null && !oldUrl.equalsIgnoreCase(url)) {
        					Util.getInstance().setMusicTimeElapse(0);
        				}
                    	if (oldUrl != null)
                    		stopMusic();
                    	
                    	setCurrentPath(url);
                    	playMusic(url, Util.getInstance().getMusicTimeElapse());
                        break;
                    case PCMCODEC_STOP:
                    	stopMusic();
                    	break;
                    case PCMCODEC_PAUSE:
                    	pauseMusic();
                    	break;
                    case PCMCODEC_RESUME:
                    	resumeMusic();
                    	break;
                    case PCMCODEC_SNDREC_RECORD:
                    	String path = bundle.getString(URL_FLAG);
                    	//extRecorder = ExtAudioRecorder.getInstanse();
                    	//extRecorder.recordChat(path);
                    	hkRecorder = new HKAudioRecorder(path);
                    	hkRecorder.startRecord();
                    	break;
                    case PCMCODEC_SNDREC_STOP:
                    	//extRecorder.stopRecord();
                    	hkRecorder.stopRecord();
                    	break;
                    case PCMCODEC_SNDREC_PLAY:
                    	String sndUrl = bundle.getString(URL_FLAG);
                    	playSound(sndUrl);
                    	break;
                    default:
                    	break;
                    }
                }
            };
            Looper.loop();
        }
    }
	
	private void setCurrentPath(String url)
	{
		path = url;
	}
	
	private void playMusic(String url, int time) {
		MusicFormat formate = getMusicFormat(url);

		switch (formate) {
		case MUSIC_TYPE_MP3:
		case MUSIC_TYPE_AAC:
		case MUSIC_TYPE_FLAC:
		case MUSIC_TYPE_M4A:
		case MUSIC_TYPE_OGG:
		case MUSIC_TYPE_WAV:
			pcmCodec.play(url, time);
			break;
		default:
			break;
		}
	}
	
	private void resumeMusic() {
		pcmCodec.resume();
	}
	
	private void pauseMusic() {
		pcmCodec.pause();
	}
	
	private void stopMusic(){
		pcmCodec.stop();
	}
	
	private void playSound(String url) {
		pcmCodec.playWAV(url);
	}
}