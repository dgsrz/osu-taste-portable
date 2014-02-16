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

import android.util.Log;
import com.un4seen.bass.*;

/**
 * @author dgsrz(dgsrz@vip.qq.com)
 */
public class SampleFile {

    private int mChannel;
    private int mSample = 0;

    public SampleFile(String fileName) {
        BASS.BASS_SampleFree(mSample);
        if (fileName != null && !fileName.equals("")) {
            // 最大通道数64，优先淘汰最后播放
            mSample = BASS.BASS_SampleLoad(fileName, 0, 0, 64, BASS.BASS_SAMPLE_OVER_POS);
        }
        if (mSample == 0) {
            Log.e("SampleLoad", "ErrorCode: " + BASS.BASS_ErrorGetCode());
        }
    }

    public boolean play() {
        mChannel = BASS.BASS_SampleGetChannel(mSample, false);
        if (mSample != 0) {
            BASS.BASS_ChannelPlay(mChannel, false);
        }
        return true;
    }

    /**
     * 完全释放播放器资源，等待GC回收内存
     */
    public void release() {
        BASS.BASS_StreamFree(mChannel);
        mChannel = 0;
    }

    public float getVolume() {
        Float volume = 1.0f;
        BASS.BASS_ChannelGetAttribute(mChannel, BASS.BASS_ATTRIB_VOL, volume);
        return volume;
    }

    public void setVolume(float volume) {
        BASS.BASS_ChannelSetAttribute(mChannel, BASS.BASS_ATTRIB_VOL, volume);
    }



}
