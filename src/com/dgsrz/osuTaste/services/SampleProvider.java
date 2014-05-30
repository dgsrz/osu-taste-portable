package com.dgsrz.osuTaste.services;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;
import com.un4seen.bass.BASS;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * Created by: dgsrz
 * Date: 2014-02-01 19:41
 */
public class SampleProvider {

    private int channel;
    private int sample = 0;

    public SampleProvider(Context context, String fileName) {
        BASS.BASS_SampleFree(sample);
        AssetManager assetManager = context.getAssets();
        ByteBuffer buf = null;
        try {
            InputStream is = assetManager.open(fileName);
            buf = ByteBuffer.allocateDirect(is.available());
            int b;
            while ((b = is.read()) != -1) {
                buf.put((byte)b);
            }
        } catch (IOException ioe) {
        }

        sample = BASS.BASS_SampleLoad(buf, 0, buf.capacity(), 64, BASS.BASS_SAMPLE_OVER_POS);
        if (sample == 0) {
            Log.e("SAMPLE load failed", "Code: " + BASS.BASS_ErrorGetCode());
        }
    }

    public boolean play() {
        channel = BASS.BASS_SampleGetChannel(sample, false);
        if (sample != 0) {
            BASS.BASS_ChannelPlay(channel, false);
        }
        return true;
    }

    public float getVolume() {
        Float volume = 1.0f;
        BASS.BASS_ChannelGetAttribute(channel, BASS.BASS_ATTRIB_VOL, volume);
        return volume;
    }

    public void setVolume(float volume) {
        BASS.BASS_ChannelSetAttribute(channel, BASS.BASS_ATTRIB_VOL, volume);
    }

}
