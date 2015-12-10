/**
 * Copyright 2014 Jeroen Mols
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Modified by xiangmin.xu, for dvr usage
 */

package com.jmolsmobile.landscapevideocapture;

import android.os.Environment;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class VideoFile {

	private static final String	DIRECTORY_SEPARATOR	= "/";
	private static final String	DATE_FORMAT			= "yyyyMMdd_HHmmss";
	private static final String	DEFAULT_PREFIX		= "video_";
	private static final String	DEFAULT_EXTENSION	= ".mp4";

	private final String        mSubDir;
	private final String		mFilename;
	private Date				mDate = null;
	private boolean             mLocked = false;

	public VideoFile(String filename) {
		this.mSubDir = null;
		this.mFilename = filename;
	}

	public VideoFile(String filename, Date date) {
		this.mSubDir = null;
		this.mFilename = filename;
		this.mDate = date;
	}

    public VideoFile(String subdir, String filename) {
        this.mSubDir = subdir;
        this.mFilename = filename;
    }

	public VideoFile(String subdir, String filename, Date date, boolean locked) {
		this.mSubDir = subdir;
		this.mFilename = filename;
		this.mDate = date;
		this.mLocked = locked;
	}

	public String getFullPath() {
		return getFile().getAbsolutePath();
	}

	private File getFile() {
		final String filename = generateFilename();
		if (filename.contains(DIRECTORY_SEPARATOR)) return new File(filename);

		final File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
        if (isValidSubDir()) {
            final File subdir = new File(dir, mSubDir);
            subdir.mkdirs();
            return new File(subdir, generateFilename());
        }
        else {
            dir.mkdirs();
            return new File(dir, generateFilename());
        }
	}

	private String generateFilename() {
		if (isValidFilename()) return mFilename;

		final String dateStamp = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).format(getDate());
		return DEFAULT_PREFIX + dateStamp + DEFAULT_EXTENSION;
	}

	private boolean isValidFilename() {
		if (mFilename == null) return false;
		if (mFilename.isEmpty()) return false;

		return true;
	}

    private boolean isValidSubDir() {
        if (mSubDir == null) return false;
		if (mSubDir.isEmpty()) return false;

        return true;
    }

    public Date getDate() {
        if (mDate == null) {
            mDate = new Date();
        }
        return mDate;
    }

    public void setData(Date date) {
		mDate = date;
	}

    public boolean getLocked() {
		return mLocked;
	}

    public void setLocked(boolean locked) {
		mLocked = locked;
	}

    public String getSubDirectory() { return mSubDir; }

    public String getFilename() { return mFilename; }
}
