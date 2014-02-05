/*
 * Copyright (C) 2012 Andrew Neal Licensed under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package com.andrew.apollo.model;

import android.text.TextUtils;

/**
 * A class that represents an album.
 * 
 * @author Andrew Neal (andrewdneal@gmail.com)
 */
public class Directory {

    /**
     * The unique Id of the directory
     */
    public int mDirectoryId;

    /**
     * The path of the directory
     * 注：此为文件夹名字而非完整路径
     * 完整路径需要加上设置中的监视文件夹地址
     */
    public String mDirPath;

    /**
     * The album artist
     */
    public String mArtistName;

    /**
     * The number of songs in the directory
     */
    public int mSongNumber;

    /**
     * Last modified
     */
    public long mLastModified;

    /**
     * Constructor of <code>Directory</code>
     *
     * @param directoryId The Id of the directory
     * @param dirPath The path of the directory
     * @param artistName The album artist
     * @param songNumber The number of songs in the directory
     */
    public Directory(final int directoryId, final String dirPath, final String artistName,
                     final int songNumber, final long lastModified) {
        super();
        mDirectoryId = directoryId;
        mDirPath = dirPath;
        mArtistName = artistName;
        mSongNumber = songNumber;
        mLastModified = lastModified;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) mDirectoryId;
        result = prime * result + (mDirPath == null ? 0 : mDirPath.hashCode());
        result = prime * result + (mArtistName == null ? 0 : mArtistName.hashCode());
        result = prime * result + mSongNumber;
        result = prime * result + (int) mLastModified;
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Directory other = (Directory)obj;
        return mDirectoryId == other.mDirectoryId
                && TextUtils.equals(mDirPath, other.mDirPath)
                && TextUtils.equals(mArtistName, other.mArtistName)
                && mSongNumber == other.mSongNumber
                && mLastModified == other.mLastModified;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return mDirPath;
    }

}
