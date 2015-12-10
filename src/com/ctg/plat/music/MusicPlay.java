package com.ctg.plat.music;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import butterknife.Bind;
import butterknife.ButterKnife;

import com.ctg.netmusic.FragOnDestroy;
import com.ctg.netmusic.XimalayaFM;
import com.ctg.ui.Base;
import com.ctg.ui.R;
import com.ctg.util.MyAdapter;
import com.ctg.util.MyPagerAdapter;
import com.ctg.util.MyViewPager;
import com.harman.ctg.monitor.models.VideoItem;
import com.harman.hkwirelessapi.PcmCodecUtil;
import com.harman.hkwirelessapi.Util;
import com.harman.hkwirelessapi.Util.MusicFormat;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.nostra13.universalimageloader.utils.StorageUtils;
import com.ximalaya.ting.android.opensdk.constants.DTransferConstants;
import com.ximalaya.ting.android.opensdk.constants.DTransferConstants.WeekDay;
import com.ximalaya.ting.android.opensdk.datatrasfer.CommonRequest;
import com.ximalaya.ting.android.opensdk.datatrasfer.IDataCallBack;
import com.ximalaya.ting.android.opensdk.model.album.Announcer;
import com.ximalaya.ting.android.opensdk.model.live.program.Program;
import com.ximalaya.ting.android.opensdk.model.live.program.ProgramList;
import com.ximalaya.ting.android.opensdk.model.live.radio.Radio;
import com.ximalaya.ting.android.opensdk.model.live.radio.RadioList;
import com.ximalaya.ting.android.opensdk.model.live.schedule.LiveAnnouncer;
import com.ximalaya.ting.android.opensdk.model.live.schedule.Schedule;
import com.ximalaya.ting.android.opensdk.model.live.schedule.ScheduleList;
import com.ximalaya.ting.android.opensdk.player.XmPlayerManager;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnTimedTextListener;
import android.media.MediaPlayer.TrackInfo;
import android.media.TimedText;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FilenameFilter;
import java.net.URLDecoder;
import java.nio.charset.Charset;


public class MusicPlay implements View.OnClickListener, Runnable, FragOnDestroy{
	Context mContext;
	public static final int PLAY_TIME = 0x1000;
	public LinearLayout music_linear;
	LinearLayout local_l, xima_l;
	MyProgressBar progress;
	MyViewPager vpager;
	ImageView local_imgv;
	ImageView list_i;
	ImageView prev_i;
	ImageView play_i;
	ImageView next_i;
	ImageView loop_i;
	ImageView music_cover;
	TextView song_name_t;
	TextView song_column_t;
	TextView singer_t;
	TextView duration_t;
	TextView elapse_t;
	private SimpleDateFormat sdf;
//	ArrayList<View> listViews;
	FrameLayout mFrm;
	LinearLayout mplay_linear;
	LinearLayout plist_linear;
	TextView listTitle;
	int sourceType;
	int subLayerIdx;
	ImageView plist_back;
	ListView plistv;
//	SimpleAdapter musicAdapter;
	MusicArrayAdapter musicAdapter;
	FMArrayAdapter fmAdapter;
//	ArrayList<Map<String, String>> music_list;
	
	FragmentManager frgMngr; 
	XimalayaFM ximaFrg;
	boolean isShow;
	int curType;// music source : internet, FM, local
//	int curLayer;// music play layer: 0 media player, 1 list...
    //播放对象
    private MediaPlayer myMediaPlayer;
    //播放列表
    //private List<String> myMusicList=new ArrayList<String>();
    //当前播放歌曲的索引
    
    //音乐的路径
    private static final String MUSIC_PATH = Base.getSDPath()+"/Music/";
    private static final String MUSIC_PLAY_STAT_PATH = Base.getSDPath()+"/OBDII/musicplay.cfg";
    private List<Mp3Info> mp3Infos = new ArrayList<Mp3Info>(); 
    long lastOperationTime;
	private int playState = 0; //0 stop, 1 play, 2 pause;
	Thread playPosThd;
	boolean playPosThdRun;
	int curSecond;
	int playRandomOrLoop;//0 random 1 loop 2 loop one
	int curMainId;
//	int curFocusItmId;
	int currentListItem=0;
	int curFmProgramId = 0;
	ImageLoader imageLoader;
	DisplayImageOptions options;
	
	Radio curRadio;
	
//    int listPosition;
    
	public static class Mp3Info implements Serializable{
		long id;
		String url;
		String title;
		public String album;
		public int albumId;
		public String artist;
		public int duration;
		public int playTime;
		public int playState;
		public int playType;//playRandomOrLoop
		Mp3Info() {
		}
		
		void setId(long id) {
			this.id = id;
		}
		long getId() {
			return id;
		}

		void setUrl(String url) {
			this.url = url;
		}
		String getUrl() {
			return url;
		}

		void setTitle(String title) {
			this.title = title;
		}
		String getTitle() {
			return title;
		}
		
		public boolean equals(Object other){
			return url.equals(((Mp3Info)other).url);
		}
	}
	
    public class MusicFilter implements FilenameFilter{

        @Override
        public boolean accept(File dir, String filename) {
            // TODO Auto-generated method stub
            return (filename.endsWith(".mp3"));
        }

         
    }
    
    class MusicArrayAdapter extends ArrayAdapter<Mp3Info> {
    	
    	int foucusId = -1;
        public MusicArrayAdapter(Context context, List<Mp3Info> mp3Infos) {
            super(context, R.layout.music_item, mp3Infos);
            
        }

        public void setFocus(int focus){
        	if(foucusId == focus)
        		return;
        	else{
        		foucusId = focus;
        		notifyDataSetChanged();
        	}
        }
        
        @Override
        public View getView(int position, View view, ViewGroup parent) {
            final MusicVH holder;
            Mp3Info info = getItem(position);
            
			if(!Base.isBaseActive)
				return null;
            if(view == null) {
                // inflate the GridView item layout
                LayoutInflater inflater = LayoutInflater.from(getContext());
                view = inflater.inflate(R.layout.music_item, parent, false);

                // initialize the view holder
                holder = new MusicVH(view);
                view.setTag(holder);
            } else {
                // recycle the already inflated view
                holder = (MusicVH) view.getTag();
            }
            holder.indexTv.setText(""+position);
            try {
				holder.songNmTv.setText(URLDecoder.decode(info.getTitle(), "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            if(position == foucusId){
            	view.setBackgroundColor(0x33888888);
            }
            else
            	view.setBackground(null);
            return view;
        }

        class MusicVH {
            @Bind(R.id.music_itm_idx) TextView indexTv;
            @Bind(R.id.song_name) TextView songNmTv;


            public MusicVH(View view) {
                ButterKnife.bind(this, view);
            }
        }
    }
    
    class FMArrayAdapter extends ArrayAdapter<Schedule> {
        
    	public List<Schedule> plst;
    	public int maxId;
    	public final SimpleDateFormat sdf_hm = new SimpleDateFormat("HH:mm");
    	int foucusId = -1;
    	
        public FMArrayAdapter(Context context, List<Schedule> plst) {        
            super(context, R.layout.fm_program_item, plst);
            this.plst = plst;
        }

        public void setFocus(int focus){
        	if(foucusId == focus)
        		return;
        	else{
        		foucusId = focus;
        		notifyDataSetChanged();
        	}
        }
        
        @Override
        public View getView(int position, View view, ViewGroup parent) {
            final FMVH holder;
            Schedule info = getItem(position);
            Program grogram = info.getRelatedProgram();
            long curTime = System.currentTimeMillis();
            Date curDate = new Date(curTime);
            String timeStr = sdf_hm.format(curDate);
            List<LiveAnnouncer> anlst = grogram.getAnnouncerList();
            String endTime;
            String startTime;
            String announcer = "";
			if(!Base.isBaseActive)
				return null;
            if(view == null) {
                // inflate the GridView item layout
                LayoutInflater inflater = LayoutInflater.from(getContext());
                view = inflater.inflate(R.layout.fm_program_item, parent, false);

                // initialize the view holder
                holder = new FMVH(view);
                view.setTag(holder);
            } else {
                // recycle the already inflated view
                holder = (FMVH) view.getTag();
            }
            
            holder.programNmT.setText(grogram.getProgramName());   
            for(LiveAnnouncer liveAn :anlst){
            	announcer += liveAn.getNickName() + " ";
            }
            holder.broadcasterT.setText(announcer);
            startTime = info.getStartTime();
            endTime = info.getEndTime();
            holder.timeGapT.setText(startTime+"~"+endTime);
            if(timeStr.compareTo(endTime) > 0 && !endTime.startsWith("00")){
            	holder.programNmT.setTextColor(0xffffffff);
            	holder.broadcasterT.setTextColor(0xffcccccc);
            	holder.timeGapT.setTextColor(0xffffffff);
            	holder.fmPlayStatT.setText("回看");
            	holder.fmPlayStatT.setTextColor(0xffcccccc);
            	holder.fmPlayStatT.setBackground(null);
            	//view.setClickable(true);
            }
            else if(timeStr.compareTo(startTime) >= 0){
            	holder.programNmT.setTextColor(0xffffffff);
            	holder.broadcasterT.setTextColor(0xffcccccc);
            	holder.timeGapT.setTextColor(0xffffffff);
            	holder.fmPlayStatT.setText("直播");
            	holder.fmPlayStatT.setTextColor(0xffffffff);
            	holder.fmPlayStatT.setBackground(mContext.getResources().getDrawable(R.drawable.shape_live_font));
            	//view.setClickable(true);
            	maxId = position;
            }
            else{
            	holder.programNmT.setTextColor(0x80cccccc);
            	holder.broadcasterT.setTextColor(0x80cccccc);
            	holder.timeGapT.setTextColor(0x80cccccc);
            	holder.fmPlayStatT.setBackground(null);
            	holder.fmPlayStatT.setText("");
            	//view.setClickable(false);
            }
            if(position == foucusId){
            	view.setBackgroundColor(0x33888888);
            }
            else
            	view.setBackground(null);
            return view;
        }

        class FMVH {
            @Bind(R.id.program_nm) TextView programNmT;
            @Bind(R.id.broadcaster) TextView broadcasterT;
            @Bind(R.id.time_gap) TextView timeGapT;
            @Bind(R.id.fm_play_stat) TextView fmPlayStatT;

            public FMVH(View view) {
                ButterKnife.bind(this, view);
            }
        }
    }
    
	public MusicPlay(Context context){
		mContext = context;
		music_linear = (LinearLayout)View.inflate(mContext, R.layout.music, null);
		
		//vpager = (MyViewPager) music_linear.findViewById(R.id.vpager);
		//listViews = new ArrayList<View>();
		//vpager.setAdapter(new MyPagerAdapter(listViews));
		//vpager.setCurrentItem(0);
		sourceType = 0;
		subLayerIdx = 0;
		mFrm = (FrameLayout)music_linear.findViewById(R.id.music_frm);
//		mplay_linear = (LinearLayout) View.inflate(mContext,  R.layout.music_play, null);
//		plist_linear = (LinearLayout) View.inflate(mContext,  R.layout.music_list, null);
//		mFrm.addView(mplay_linear);
//		mFrm.addView(plist_linear);
		mplay_linear = (LinearLayout) music_linear.findViewById(R.id.music_play_l);
		plist_linear = (LinearLayout) music_linear.findViewById(R.id.music_list_l);
//		plist_linear.setVisibility(View.INVISIBLE);
		local_l = (LinearLayout) music_linear.findViewById(R.id.localmusic);
		xima_l = (LinearLayout) music_linear.findViewById(R.id.xima_radio);
		local_imgv = (ImageView) music_linear.findViewById(R.id.localmusic_img);
		local_l.setOnClickListener(this);
		xima_l.setOnClickListener(this);
		list_i = (ImageView) mplay_linear.findViewById(R.id.music_list);
		prev_i = (ImageView) mplay_linear.findViewById(R.id.music_prev);
		play_i = (ImageView) mplay_linear.findViewById(R.id.music_play_pause);
		next_i = (ImageView) mplay_linear.findViewById(R.id.music_next);
		loop_i = (ImageView) mplay_linear.findViewById(R.id.music_loop);
		music_cover = (ImageView)mplay_linear.findViewById(R.id.iv_music_id);
		song_name_t = (TextView) mplay_linear.findViewById(R.id.song_name);
		song_column_t = (TextView) mplay_linear.findViewById(R.id.song_column);
		duration_t = (TextView) mplay_linear.findViewById(R.id.duration_tv);
		elapse_t = (TextView) mplay_linear.findViewById(R.id.elapse_tv);
		singer_t = (TextView) mplay_linear.findViewById(R.id.singer);
		list_i.setOnClickListener(this);
		prev_i.setOnClickListener(this);
		play_i.setOnClickListener(this);
		next_i.setOnClickListener(this);
		list_i.setOnClickListener(this);
		loop_i.setOnClickListener(this);
		progress = (MyProgressBar) mplay_linear.findViewById(R.id.music_progressbar);
		
		plist_back = (ImageView)plist_linear.findViewById(R.id.back_img);
		plistv = (ListView) plist_linear.findViewById(R.id.music_lv);
		listTitle = (TextView) plist_linear.findViewById(R.id.music_list_tv);
		plist_back.setOnClickListener(this);
		curType = 0;

		sdf = new SimpleDateFormat("mm:ss");
//		initMediaPlayer();
		
		currentListItem = 0;
		initMp3Infos(mContext);
		initMusicList();
		initMusicPlayerService();
		Base.OBDApp.setPlayTimeHander(playTimeHandler);
//		initPlayMp3Info();
		playPosThd = new Thread(this);
		frgMngr = ((Activity) context).getFragmentManager();
		imageLoader = ImageLoader.getInstance();
		options = new DisplayImageOptions.Builder()
		.showImageOnLoading(R.drawable.recomend_image) // 设置图片下载期间显示的图片
		.showImageForEmptyUri(R.drawable.recomend_image) // 设置图片Uri为空或是错误的时候显示的图片
		.showImageOnFail(R.drawable.reload_message) // 设置图片加载或解码过程中发生错误显示的图片
		.cacheInMemory(true) // 设置下载的图片是否缓存在内存中
		.cacheOnDisc(true) // 设置下载的图片是否缓存在SD卡中
		.displayer(new RoundedBitmapDisplayer(20)) // 设置成圆角图片
		.build();

		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
				context).defaultDisplayImageOptions(options).build();
		imageLoader.init(config);
		
		//playMusic(MUSIC_PATH+music_list.get(currentListItem).get("name"), false);
	}

//	void initMediaPlayer(){
//		myMediaPlayer = new MediaPlayer();
//		
//		myMediaPlayer.setOnTimedTextListener(new OnTimedTextListener(){
//
//			@Override
//			public void onTimedText(MediaPlayer mp, TimedText text) {
//				// TODO Auto-generated method stub
//				elapse_t.setText(text.getText());
//			}
//			
//		});
//		myMediaPlayer.setOnInfoListener(new OnInfoListener(){
//
//			@Override
//			public boolean onInfo(MediaPlayer mp, int what, int extra) {
//				// TODO Auto-generated method stub
//				return false;
//			}
//			
//		});
//		myMediaPlayer.setOnPreparedListener(new OnPreparedListener(){
//
//			@Override
//			public void onPrepared(MediaPlayer mp) {
//				// TODO Auto-generated method stub
//				
//			}
//			
//		});
//		myMediaPlayer.setOnBufferingUpdateListener(new OnBufferingUpdateListener(){
//
//			@Override
//			public void onBufferingUpdate(MediaPlayer mp, int percent) {
//				// TODO Auto-generated method stub
//				progress.setProgress(percent);
//			}
//			
//		});
//		
//	}
	
	OnItemClickListener musicListItemClick = new OnItemClickListener(){

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			// TODO Auto-generated method stub
			if(curMainId == 0){
				currentListItem = arg2;
				//playMusic(MUSIC_PATH+music_list.get(currentListItem).get("name"), true);
				playMusic();
				musicAdapter.setFocus(arg2);
				//mFrm.removeViews(1, mFrm.getChildCount()-1);
			}
			else{
				if(arg2 > fmAdapter.maxId)
					return;
				else if(arg2 == fmAdapter.maxId){
					if(ximaFrg.xmPlayer.isPlaying()){
						ximaFrg.xmPlayer.stop();
					}
					ximaFrg.xmPlayer.playRadio(curRadio);
				}
				else
					ximaFrg.xmPlayer.playSchedule(fmAdapter.plst, arg2);
				curFmProgramId = arg2;
				fmAdapter.setFocus(arg2);
				playProgram(fmAdapter.plst.get(arg2).getRelatedProgram());
				play_i.setImageResource(R.drawable.ib_music_pause_default);
			}
			plist_linear.setVisibility(View.INVISIBLE);
			
		}
	};
	
	void initMusicList(){
		//fetchLocalMusic();
//		musicAdapter = new SimpleAdapter(mContext,music_list,
//	            R.layout.music_item,
//	            new String[] {"index", "name"},   
//	            new int[] {R.id.music_itm_idx,R.id.song_name}  
//	        );
		musicAdapter = new MusicArrayAdapter(mContext, mp3Infos);
		plistv.setAdapter(musicAdapter);
		plistv.setOnItemClickListener(musicListItemClick);
	
	}
	

	
	void playProgram(Program grogram){
		
		song_name_t.setText(grogram.getProgramName());
		List<LiveAnnouncer> anlst = grogram.getAnnouncerList();		
		String announcer = "";
        for(LiveAnnouncer liveAn :anlst){
        	announcer += liveAn.getNickName() + " ";
        }
        song_column_t.setText(announcer);

        Schedule info = fmAdapter.plst.get(curFmProgramId);
        String startTime = info.getStartTime();
        String endTime = info.getEndTime();
        singer_t.setText(startTime + " ~ " + endTime);

	}
	
	
	
	void initPlayMp3Info(){
		Mp3Info info = null;
		int index = -1;
		try {
			FileInputStream fr = new FileInputStream(MUSIC_PLAY_STAT_PATH);
			ObjectInputStream objr= new ObjectInputStream(fr);
			info = (Mp3Info) objr.readObject();
			objr.close();
			fr.close();
			
			index = mp3Infos.indexOf(info);
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(index != -1){
			currentListItem = index;
			playRandomOrLoop = info.playType;			
			playState = info.playState;
			curSecond = info.playTime;
			if(playRandomOrLoop == 0){
				loop_i.setImageResource(R.drawable.ib_mode_rand_default);				
			}
			else if(playRandomOrLoop == 1){
				loop_i.setImageResource(R.drawable.ib_music_loop_all_default);
			}
			else{
				loop_i.setImageResource(R.drawable.ib_music_loop_one_default);	
			}
			if(playState == 0 || playState == 2){
				play_i.setImageResource(R.drawable.ib_music_play_default);				
			}
			else if(playState == 1){
				play_i.setImageResource(R.drawable.ib_music_pause_default);
			}
			String songName = info.getTitle();
			String decodeName = songName;
			try {
				decodeName = URLDecoder.decode(songName, "gbk");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			song_name_t.setText(decodeName);
			song_column_t.setText(info.album);
			singer_t.setText(info.artist);
			duration_t.setText(sdf.format(new Date(info.duration)));
			progress.setProgress((int)(curSecond*1000*100/info.duration));
			elapse_t.setText(sdf.format(new Date(curSecond*1000)));
		}
	}
	
	void savePlayMp3Info(){
		Mp3Info info = mp3Infos.get(currentListItem);
		info.playState = playState;
		info.playTime = curSecond;
		info.playType = playRandomOrLoop;
		try {
			FileOutputStream fw = new FileOutputStream(MUSIC_PLAY_STAT_PATH);
			ObjectOutputStream objw= new ObjectOutputStream(fw);
			objw.writeObject(info);
			objw.close();
			fw.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void onDestroy(){
		//savePlayMp3Info();
		stopMusic();
	}
	
	public boolean onSystemBack(){
		if(plist_linear.getVisibility() == View.VISIBLE){
//			mFrm.removeViews(1, mFrm.getChildCount()-1);
//			curLayer = 0;
			plist_linear.setVisibility(View.INVISIBLE);
			return true;
		}
		if(curMainId == 1 && isShow){
			FragmentTransaction act = frgMngr.beginTransaction(); 
			act.hide(ximaFrg);		
			isShow = false;
			act.commit();	
			return true;
		}
		plistv.setAdapter(null);	
		
		return false;		
	}
	
	public void onPause(){
		if(plistv != null)
			plistv.setAdapter(null);
	}
	
	public void onResume(){
		if(plistv != null && musicAdapter != null)
			plistv.setAdapter(musicAdapter);
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		FragmentTransaction act = null;
		switch(v.getId()){
		case R.id.localmusic:
			if(curMainId == 0)
				return;
			else{
				play_i.setImageResource(R.drawable.ib_music_play_default);
				music_cover.setImageResource(R.drawable.iv_music_pic);
				song_name_t.setText("歌名");
				song_column_t.setText("专辑");
				singer_t.setText("歌手");
				listTitle.setText("音乐列表");
			}
			if(ximaFrg != null && ximaFrg.xmPlayer.isPlaying()){
				ximaFrg.xmPlayer.stop();
			}

			plist_linear.setVisibility(View.INVISIBLE);
			curMainId = 0;
			loop_i.setBackground(null);
			//plistv.setAdapter(musicAdapter);
			if(ximaFrg != null){
				act = frgMngr.beginTransaction(); 
//				act.remove(ximaFrg);
				act.hide(ximaFrg);		
				isShow = false;
				act.commit();						
//				ximaFrg = null;
			}
			break;
			
		case R.id.xima_radio:
			if(curMainId == 0){
				stopMusic();		
				play_i.setImageResource(R.drawable.ib_music_play_default);
				progress.setProgress(0);
				elapse_t.setText("");
				duration_t.setText("");
				song_name_t.setText("节目名称");
				song_column_t.setText("主播");
				singer_t.setText("时间段");
				loop_i.setBackgroundColor(0x77cccccc);
				listTitle.setText("电台节目列表");
			}
			else if(isShow)
				return;

			plist_linear.setVisibility(View.INVISIBLE);
			curMainId = 1;
			act = frgMngr.beginTransaction(); 
			if(ximaFrg == null){
				ximaFrg = new XimalayaFM();
				ximaFrg.setOnDestroy(this);				
				act.add(R.id.music_frm, ximaFrg);				
			}
			else{
				act.show(ximaFrg);
			}
			isShow = true;
			act.commit();
			break;
		case R.id.music_list:
//			curLayer = 1;
			plist_linear.setVisibility(View.VISIBLE);
			if(curMainId == 0){
				plistv.setAdapter(musicAdapter);
			}
			else{
				plistv.setAdapter(fmAdapter);
			}
			break;
		case R.id.music_prev:
			if(curMainId == 0){
				prevMusic();
			}
			else if(curFmProgramId != 0 && fmAdapter != null)
			{
				ximaFrg.xmPlayer.playSchedule(fmAdapter.plst, --curFmProgramId);			
				playProgram(fmAdapter.plst.get(curFmProgramId).getRelatedProgram());
				fmAdapter.setFocus(curFmProgramId);
			}
			
				
			break;
		case R.id.music_next:
			if(curMainId == 0){
				nextMusic();
			}
			else if(fmAdapter != null && curFmProgramId != fmAdapter.maxId)
			{
				if(curFmProgramId == fmAdapter.maxId-1){
					++curFmProgramId;
					if(ximaFrg.xmPlayer.isPlaying()){
						ximaFrg.xmPlayer.stop();
					}
					ximaFrg.xmPlayer.playRadio(curRadio);
				}
				else
					ximaFrg.xmPlayer.playSchedule(fmAdapter.plst, ++curFmProgramId);			
				playProgram(fmAdapter.plst.get(curFmProgramId).getRelatedProgram());
				fmAdapter.setFocus(curFmProgramId);
			}
			break;			
		case R.id.music_play_pause:	
			if(curMainId == 0){
				if(playState == 0)
					playMusic();
				else if(playState == 1)
					pauseMusic();
				else if(playState == 2)
					resumeMusic();
			}
			else{
				if(ximaFrg.xmPlayer.isPlaying()){
					ximaFrg.xmPlayer.pause();
					play_i.setImageResource(R.drawable.ib_music_play_default);
				}
				else{
					ximaFrg.xmPlayer.play();
					play_i.setImageResource(R.drawable.ib_music_pause_default);
				}
			}		
//			PcmCodecUtil.getInstance().play(MUSIC_PATH+music_list.get(currentListItem).get("name"), 0);
//			if(myMediaPlayer.isPlaying()){
//				play_i.setImageResource(R.drawable.ib_music_play_default);
//				myMediaPlayer.pause();
//			}else{
//				play_i.setImageResource(R.drawable.ib_music_pause_default);
//				myMediaPlayer.start();
//			}			
			break;

		case R.id.music_loop:
			if(curMainId != 0)
				return;
			playRandomOrLoop = (playRandomOrLoop+1)%3;
			if(playRandomOrLoop == 0){
				loop_i.setImageResource(R.drawable.ib_mode_rand_default);				
			}
			else if(playRandomOrLoop == 1){
				loop_i.setImageResource(R.drawable.ib_music_loop_all_default);
			}
			else{
				loop_i.setImageResource(R.drawable.ib_music_loop_one_default);	
			}
			break;
		
		//play list
		case R.id.back_img:
//			if(curLayer == 1){
//				mFrm.removeViews(1, mFrm.getChildCount()-1);
//				curLayer = 0;
//			}
			if(plist_linear.getVisibility() == View.VISIBLE){
				plist_linear.setVisibility(View.INVISIBLE);
			}			
			break;
			
		}
	}
	
	//绑定音乐
    void fetchLocalMusic(){
//    	int i = 0;
//    	Map mapItm;
//
//    	music_list = new ArrayList<Map<String, String>>();
//        File home=new File(MUSIC_PATH);
//        if(home.listFiles(new MusicFilter()).length>0){
//            for(File file:home.listFiles(new MusicFilter())){
//                //myMusicList.add(file.getName());
//            	mapItm = new HashMap<String, String>();
//            	mapItm.put("index", ""+i);
//            	mapItm.put("name", file.getName());
//            	music_list.add(mapItm);
//                i++;
//            }
//            //ArrayAdapter<String> musicList=new ArrayAdapter<String>(mContext, 0, myMusicList);
//            //setListAdapter(musicList);
//        }
    }
    
    Handler playTimeHandler = new Handler(){
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			Bundle bundle;
			int dura;
			switch(msg.what){
				case PLAY_TIME:
					Mp3Info mp3Info = mp3Infos.get(currentListItem);
//					bundle = msg.getData();
//					playtm = bundle.getLong("play_time");
					dura = mp3Info.duration;
					progress.setProgress((int)(curSecond*1000*100/dura));					
					elapse_t.setText(sdf.format(new Date(curSecond*1000)));
					if(curSecond*1000 >= dura){
						nextMusic();
					}
//					duration_t.setText(sdf.format(new Date(dura)));
					break;
			}
		}
    };
    
	@Override
	public void run() {
		// TODO Auto-generated method stub
		playPosThdRun = true;
		while(true){
			try {
				Thread.sleep(1000);
				if(playState == 1){
					curSecond++;
					playTimeHandler.obtainMessage(PLAY_TIME).sendToTarget();
				}				
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				break;
			}
		}
	} 
	
	private void initMusicPlayerService() {
		Intent intent = new Intent();
		intent.putExtra(Util.MSG_TYPE_MUSIC, Util.MSG_PCM_INIT);
		intent.setAction(Util.MUSICPLAYER); 
		intent.setPackage(mContext.getPackageName());
		mContext.startService(intent);
	}
	
	private void playMusic() {	
		

		Mp3Info mp3Info = mp3Infos.get(currentListItem);
		String songName = mp3Info.getTitle();
		String decodeName = songName;
		try {
			decodeName = URLDecoder.decode(songName, "gbk");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		song_name_t.setText(decodeName);
		song_column_t.setText(mp3Info.album);
		singer_t.setText(mp3Info.artist);
		duration_t.setText(sdf.format(new Date(mp3Info.duration)));
		progress.setProgress(0);
		elapse_t.setText(sdf.format(new Date(0)));
		playState = 1;
		play_i.setImageResource(R.drawable.ib_music_pause_default);
		Drawable cover = getAlbumArt(mContext, mp3Info.albumId);
		if(cover != null)
			music_cover.setImageDrawable(cover);
		else
			music_cover.setImageResource(R.drawable.iv_music_pic);
		long curTime = System.currentTimeMillis();
		if(curTime - lastOperationTime < 1000)
			return;
		lastOperationTime = curTime;
		Intent intent = new Intent();
		intent.putExtra(Util.MSG_URL_MUSIC, mp3Info.getUrl());
		intent.putExtra(Util.MSG_TYPE_MUSIC, Util.MSG_PCM_PLAY);
		intent.setAction(Util.MUSICPLAYER); 
		intent.setPackage(mContext.getPackageName());
		mContext.startService(intent);
		if(playPosThdRun){
			playPosThd.interrupt();
		}
		playPosThd = new Thread(this);
		playPosThd.start();
		curSecond = 0;
	}
	
	private void pauseMusic() {
		Mp3Info mp3Info = mp3Infos.get(currentListItem);
		playState = 2;
		play_i.setImageResource(R.drawable.ib_music_play_default);
		Intent intent = new Intent();
		intent.putExtra(Util.MSG_URL_MUSIC, mp3Info.getUrl());
		intent.putExtra(Util.MSG_TYPE_MUSIC, Util.MSG_PCM_PAUSE);
		intent.setAction(Util.MUSICPLAYER);
		intent.setPackage(mContext.getPackageName());
		mContext.startService(intent);
	}
	
	private void resumeMusic() {
		Mp3Info mp3Info = mp3Infos.get(currentListItem);
		playState = 1;
		play_i.setImageResource(R.drawable.ib_music_pause_default);
		Intent intent = new Intent();
		intent.putExtra(Util.MSG_URL_MUSIC, mp3Info.getUrl());
		intent.putExtra(Util.MSG_TYPE_MUSIC, Util.MSG_PCM_RESUME);
		intent.setAction(Util.MUSICPLAYER);
		intent.setPackage(mContext.getPackageName());
		mContext.startService(intent);
	}
	
	private void stopMusic() {

		Intent intent = new Intent();
		intent.putExtra(Util.MSG_TYPE_MUSIC, Util.MSG_PCM_STOP);
		intent.setAction(Util.MUSICPLAYER);
		intent.setPackage(mContext.getPackageName());
		mContext.startService(intent);
	}
	
	private void nextMusic() {
		int num = mp3Infos.size();
		if(playRandomOrLoop == 0){
			int current = 0;
			do{
				Random rand = new Random(); 
				int i = rand.nextInt(100);//100内的随机数
				current = (currentListItem+i)%num;
			}while(currentListItem == current);
			currentListItem = current;
		}
		else if(playRandomOrLoop == 1){
			if (++currentListItem >= num)
				currentListItem = 0;
		}
		musicAdapter.setFocus(currentListItem);
		Mp3Info mp3Info = mp3Infos.get(currentListItem);
		//tvMusicName.setText(mp3Info.getTitle());
		//if (canPause)
			playMusic();
	}
	
	private void prevMusic() {
		int num = mp3Infos.size();
		if(playRandomOrLoop == 0){
			int current = 0;
			do{
				Random rand = new Random(); 
				int i = rand.nextInt(100);//100内的随机数
				current = (currentListItem+i)%num;
			}while(currentListItem == current);
			currentListItem = current;
		}
		else if(playRandomOrLoop == 1){
			if (--currentListItem < 0)
				currentListItem = num-1;
		}
		musicAdapter.setFocus(currentListItem);
		Mp3Info mp3Info = mp3Infos.get(currentListItem);
		//tvMusicName.setText(mp3Info.getTitle());
		//if (canPause)
			playMusic();
	}

	public static Drawable getAlbumArt(Context context, int album_id) {
		String mUriAlbums = "content://media/external/audio/albums";
		String[] projection = new String[] { "album_art" };
		Cursor cur = context.getContentResolver().query(Uri.parse(mUriAlbums + "/" + Integer.toString(album_id)),
		projection, null, null, null);
		String album_art = null;
		if (cur.getCount() > 0 && cur.getColumnCount() > 0) {
			cur.moveToNext();
			album_art = cur.getString(0);
		}
		cur.close();
		cur = null;
		if (album_art == null || album_art.equals("")) {
//			Bitmap bm = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ablum_deflaut);
//			BitmapDrawable bmpDraw = new BitmapDrawable(bm);
			return null;
		}
		Bitmap bm = BitmapFactory.decodeFile(album_art);
		BitmapDrawable bmpDraw = new BitmapDrawable(bm);
		return bmpDraw;
	}
	
	public void initMp3Infos(Context context) {
		mp3Infos.clear();
		Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
				null, null, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
		if (cursor == null)
			return;
		
		
//    	music_list = new ArrayList<Map<String, String>>();
//    	Map mapItm;
        //ArrayAdapter<String> musicList=new ArrayAdapter<String>(mContext, 0, myMusicList);
        //setListAdapter(musicList);
        
        
		for (int i = 0, j = 0; i < cursor.getCount(); i++) {
			cursor.moveToNext(); 
			Mp3Info mp3Info = new Mp3Info();
			long id = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
			String title = cursor.getString((cursor.getColumnIndex(MediaStore.Audio.Media.TITLE))); 
			String url = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
			String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
			String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
			int isMusic = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.IS_MUSIC));
			int dura = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
			int album_id = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));
			
			if (isMusic != 0 && isMp3Type(url)) {
				j++;
				mp3Info.setId(id);
				mp3Info.setTitle(title);
				mp3Info.setUrl(url);
				mp3Info.album = album;
				mp3Info.albumId = album_id;
				mp3Info.artist = artist;
				mp3Info.duration = dura;
				mp3Infos.add(mp3Info);				
//				mapItm = new HashMap<String, String>();
//            	mapItm.put("index", ""+j);
//            	try {
//					mapItm.put("name", URLDecoder.decode(title, "UTF-8"));
//				} catch (UnsupportedEncodingException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}            	
//            	music_list.add(mapItm);
            	
			} 
		}
	}

	public boolean isMp3Type(String url) {
		int lastIndex = url.lastIndexOf(".");
		String suffix = url.substring(lastIndex, url.length());
		//for (Entry<String, MusicFormat> entry: Util.supportMusicFormat.entrySet()) {
			if (suffix.equalsIgnoreCase(".mp3")) {//
				return true;
			}
		//}
		return false;
	}
	

	public String getMp3fileName(Mp3Info info) {
		String url = info.getUrl();
		int lastDiv = url.lastIndexOf("/");
		return url.substring(lastDiv+1, url.length());
	}

	@Override
	public void onFragDestroy() {
		// TODO Auto-generated method stub
		FragmentTransaction act = frgMngr.beginTransaction(); 
		act.hide(ximaFrg);
		isShow = false;
		act.commit();
	}

	@Override
	public void onPlayRadio(Radio radio) {
		// TODO Auto-generated method stub
		ScheduleList scheduleLst;
		
		imageLoader.displayImage(radio.getCoverUrlSmall(), music_cover);
		song_name_t.setText(radio.getProgramName());
//		singer_t.setText(radio.getRadioDesc());
//		song_column_t.setText(radio.getRadioName());
		play_i.setImageResource(R.drawable.ib_music_pause_default);
		curRadio = radio;
//		next_i.setImageResource()
	}
	
	@Override
	public void onPauseRadio() {
		// TODO Auto-generated method stub
		play_i.setImageResource(R.drawable.ib_music_play_default);
	}
	
	@Override
	public void getScheduleReady(ScheduleList list) {
		// TODO Auto-generated method stub
		fmAdapter = new FMArrayAdapter(mContext, list.getmScheduleList());
		fmAdapter.setFocus(fmAdapter.maxId);
        String startTime;
        String endTime;
        long curTime = System.currentTimeMillis();
        Date curDate = new Date(curTime);
        String timeStr = fmAdapter.sdf_hm.format(curDate);
		List<Schedule> scheLst = list.getmScheduleList();
		int index = 0;
		for(Schedule info : scheLst){
			startTime = info.getStartTime();
			endTime = info.getEndTime();
			if(timeStr.compareTo(endTime) < 0 && timeStr.compareTo(startTime) >= 0){
				List<LiveAnnouncer> anlst = info.getRelatedProgram().getAnnouncerList();		
				String announcer = "";
		        for(LiveAnnouncer liveAn :anlst){
		        	announcer += liveAn.getNickName() + " ";
		        }
		        song_column_t.setText(announcer);
		        singer_t.setText(startTime + " ~ " + endTime);
		        curFmProgramId = index;
		        break;
            }			
			index++;
        
		}
		
	}     
}
