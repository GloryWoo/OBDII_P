package com.harman.ctg.monitor.models;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.RealmClass;

/**
 * Created by XiaXu on 2015-7-16.
 */
@RealmClass
public class FileModel extends RealmObject {
    private String subdir;
    private String filename;
    private Date date;
    private boolean locked;

    public FileModel() {
        // just for constructor
    }

    public FileModel(String filename, boolean locked) {// just for Eventbus
        this.filename = filename;
        this.locked = locked;
    }

    public String getSubdir() {
        return subdir;
    }

    public void setSubdir(String subdir) {
        this.subdir = subdir;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public boolean getLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }
}