package com.dgsrz.osuTaste.model;

import android.text.TextUtils;

/**
 * @author dgsrz (dgsrz@vip.qq.com)
 */
public class Beatmap {

    public int mId;

    public String mFileName;

    public String mDirectory;

    public String mTitle;

    public String mDifficulty;

    public String mArtist;

    public String mMapper;

    public Beatmap(final int id, final String fileName, final String directory, final String title,
                   final String difficulty, final String artist, final String mapper) {
        mId = id;
        mFileName = fileName;
        mDirectory = directory;
        mTitle = title;
        mDifficulty = difficulty;
        mArtist = artist;
        mMapper = mapper;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (mFileName == null ? 0 : mFileName.hashCode());
        result = prime * result + (mDirectory == null ? 0 : mDirectory.hashCode());
        result = prime * result + (mTitle == null ? 0 : mTitle.hashCode());
        result = prime * result + (mDifficulty == null ? 0 : mDifficulty.hashCode());
        result = prime * result + (mArtist == null ? 0 : mArtist.hashCode());
        result = prime * result + (mMapper == null ? 0 : mMapper.hashCode());
        result = prime * result + mId;
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
        final Beatmap other = (Beatmap)obj;
        if (mId != other.mId) {
            return false;
        }
        if (!TextUtils.equals(mFileName, other.mFileName)) {
            return false;
        }
        if (!TextUtils.equals(mDirectory, other.mDirectory)) {
            return false;
        }
        if (!TextUtils.equals(mTitle, other.mTitle)) {
            return false;
        }
        if (!TextUtils.equals(mDifficulty, other.mDifficulty)) {
            return false;
        }
        if (!TextUtils.equals(mArtist, other.mArtist)) {
            return false;
        }
        if (!TextUtils.equals(mMapper, other.mMapper)) {
            return false;
        }
        return true;
    }

}
