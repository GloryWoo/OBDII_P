package com.harman.ctg.monitor.models;

import android.graphics.Bitmap;

import java.util.Date;

/**
 * Created by XiaXu on 2015-7-20.
 */
public class VideoItem {
    public String url;
    public Bitmap thumbnail;
    public Date date;
    public boolean locked;

    public VideoItem() {
        //dummy constructor
    }

    public VideoItem(String url, Bitmap thumbnail, Date date, boolean locked) {
        this.url = url;
        this.thumbnail = thumbnail;
        this.date = date;
        this.locked = locked;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setThumbnail(Bitmap thumbnail) {
        this.thumbnail = thumbnail;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public String getUrl() {
        return this.url;
    }

    public Bitmap getThumbnail() {
        return this.thumbnail;
    }

    public Date getDate() {
        return this.date;
    }

    public boolean getLocked() {
        return this.locked;
    }
}
