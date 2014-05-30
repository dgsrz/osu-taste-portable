package com.dgsrz.osuTaste.services;

import com.un4seen.bass.BASS;

/**
 * Created by: dgsrz
 * Date: 2014-01-31 13:57
 */
public class StreamProvider {

    private int stream;

    public StreamProvider(String fileName) {
        BASS.BASS_StreamFree(stream);
        stream = BASS.BASS_StreamCreateFile(fileName, 0, 0, 0);
    }

    public boolean play() {
        return BASS.BASS_ChannelPlay(stream, true);
    }

    public float getVolume() {
        Float volume = 1.0f;
        BASS.BASS_ChannelGetAttribute(stream, BASS.BASS_ATTRIB_VOL, volume);
        return volume;
    }

    public void setVolume(float volume) {
        BASS.BASS_ChannelSetAttribute(stream, BASS.BASS_ATTRIB_VOL, volume);
    }

    public long getChannelLength() {
        return BASS.BASS_ChannelGetLength(stream, BASS.BASS_POS_BYTE);
    }

    public long getChannelPosition() {
        return BASS.BASS_ChannelGetPosition(stream, BASS.BASS_POS_BYTE);
    }

    public double Bytes2Second(long position) {
        return BASS.BASS_ChannelBytes2Seconds(stream, position);
    }

}
