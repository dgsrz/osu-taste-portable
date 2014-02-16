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
import android.util.Log;
import com.un4seen.bass.*;

/**
 * @author dgsrz(dgsrz@vip.qq.com)
 */
public class MusicFile {

    private final static String TAG = "MusicPlayer";
    private final static boolean D = true;

    private String mFileName;
    private int mStream;

    public MusicFile(final String fileName) {
        BASS.BASS_StreamFree(mStream);
        mFileName = fileName;
        mStream = BASS.BASS_StreamCreateFile(mFileName, 0, 0, 0);
    }

    public void play() {
        if (mStream != 0) {
            BASS.BASS_ChannelPlay(mStream, false);
        }
    }

    public void stop() {
        if (isPlaying()) {
            BASS.BASS_ChannelStop(mStream);
        }
    }

    public void pause() {
        if (isPlaying()) {
            BASS.BASS_ChannelPause(mStream);
        }
    }

    public void seek(int position) {
        if (isPlaying())
        {
            double sec = position / 1000.0;
            BASS.BASS_ChannelSetPosition(mStream, BASS.BASS_ChannelSeconds2Bytes(mStream, sec), BASS.BASS_POS_BYTE);
        }
    }

    /**
     * 完全释放播放器资源，等待GC回收内存
     */
    public void release() {
        stop();
        BASS.BASS_StreamFree(mStream);
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

    public boolean isChannelEndPosition() {
        return BASS.BASS_ChannelIsActive(mStream) == BASS.BASS_ACTIVE_STOPPED;
    }

}
