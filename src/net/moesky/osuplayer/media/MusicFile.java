/*
 * Copyright (C) 2014 dgsrz Licensed under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package net.moesky.osuplayer.media;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import com.un4seen.bass.*;

/**
 * @author dgsrz(dgsrz@vip.qq.com)
 */
public class MusicFile {

    /* Handler执行周期，数值越小越接近原始osu听感，同时更消耗系统资源 */
    private final static int INTERVAL = 5;

    /* 取消与HitSoundEffect同步 */
    private final static int EVENT_CANCEL_SYNC = 1;

    private String mFileName;
    private int mStream;
    private Handler mHandler;

    public MusicFile(final String fileName, final Looper looper) {
        BASS.BASS_StreamFree(mStream);
        mFileName = fileName;
        mStream = BASS.BASS_StreamCreateFile(mFileName, 0, 0, 0);
        mHandler = new Handler(looper);
    }

    private Runnable refresh = new Runnable() {
        @Override
        public void run() {
            mOnUpdateListener.onUpdate();
            mHandler.postDelayed(refresh, MusicFile.INTERVAL);
        }
    };

    public void play() {
        if (mStream != 0) {
            BASS.BASS_ChannelPlay(mStream, true);
            mHandler.postDelayed(refresh, MusicFile.INTERVAL);
        }
    }

    public void stop() {
        if (isPlaying()) {
            BASS.BASS_ChannelStop(mStream);
            mHandler.sendEmptyMessage(EVENT_CANCEL_SYNC);
        }
    }

    public void pause() {
        if (isPlaying()) {
            BASS.BASS_ChannelPause(mStream);
            mHandler.sendEmptyMessage(EVENT_CANCEL_SYNC);
        }
    }

    /**
     * 完全释放播放器资源，等待GC回收内存
     */
    public void release() {
        stop();
        BASS.BASS_StreamFree(mStream);
        setOnUpdateListener(null);
        mStream = 0;
        mFileName = null;
    }

    public float getVolume() {
        Float volume = 1.0f;
        BASS.BASS_ChannelGetAttribute(mStream, BASS.BASS_ATTRIB_VOL, volume);
        return volume;
    }

    public String getFileName() {
        return mFileName;
    }

    public boolean isPlaying() {
        return BASS.BASS_ChannelIsActive(mStream) == BASS.BASS_ACTIVE_PLAYING;
    }

    public void setVolume(float volume) {
        BASS.BASS_ChannelSetAttribute(mStream, BASS.BASS_ATTRIB_VOL, volume);
    }

    public long getChannelLength() {
        return BASS.BASS_ChannelGetLength(mStream, BASS.BASS_POS_BYTE);
    }

    public long getChannelPosition() {
        return BASS.BASS_ChannelGetPosition(mStream, BASS.BASS_POS_BYTE);
    }

    public double Bytes2Second(long position) {
        return BASS.BASS_ChannelBytes2Seconds(mStream, position);
    }

    public interface onUpdateListener {
        void onUpdate();
    }

    public void setOnUpdateListener(onUpdateListener listener) {
        mOnUpdateListener = listener;
    }

    private onUpdateListener mOnUpdateListener;

}
