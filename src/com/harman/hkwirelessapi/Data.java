package com.harman.hkwirelessapi;

import android.os.Handler;

/**
 * Created by lee on 15/5/5.
 */
public class Data {
    private static boolean playbackState = false;
    private static int timeElapsed = 0;

    static final int HANDLER_ERR_MSG = 1;
    public static Handler handler = null;

    public static boolean getPlaybackState() {
        return playbackState;
    }

    public static void setPlaybackState(boolean playbackState) {
        Data.playbackState = playbackState;
    }

    public static int getTimeElapsed() {
        return timeElapsed;
    }

    public static void settimeElapsed(int timeElapsed) {
        Data.timeElapsed = timeElapsed;
    }
}
