package com.ctg.netmusic;

import com.ximalaya.ting.android.opensdk.model.live.radio.Radio;
import com.ximalaya.ting.android.opensdk.model.live.schedule.ScheduleList;

public interface FragOnDestroy {
	void onFragDestroy();
	void onPlayRadio(Radio radio);
	void onPauseRadio();
	void getScheduleReady(ScheduleList list);
}
