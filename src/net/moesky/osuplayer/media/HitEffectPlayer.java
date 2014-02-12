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

import android.os.Environment;
import android.util.Log;
import net.moesky.osuplayer.beatmap.Beatmap;

/**
 * 音效播放器类
 * 用于提供皮肤音效、地图自定义音效的回放
 *
 * @author dgsrz (dgsrz@vip.qq.com)
 */
public class HitEffectPlayer implements MusicFile.onUpdateListener {

    private static String TAG = "HitEffectPlayer";
    private static boolean D = true;

    private Beatmap mBeatmap;
    private MusicFile mAudioPlayer;
    private SampleFile mSoftSamples[];
    private SampleFile mNormalSamples[];
    private int mPos = 0;

    public HitEffectPlayer(MusicFile audioPlayer, Beatmap beatmap) {
        mBeatmap = beatmap;
        mAudioPlayer = audioPlayer;
        audioPlayer.setOnUpdateListener(this);
        initHitEffects();
    }

    /**
     * 初始化地图音效
     */
    public void initHitEffects() {
        if (D) Log.i(TAG, "Initializing hit effects...");
        String storagePath = Environment.getExternalStorageDirectory().getAbsoluteFile() + "/osu!player";
        mSoftSamples = new SampleFile[4];
        mSoftSamples[0] = new SampleFile(storagePath + "/skins/soft-hitnormal.wav");
        mSoftSamples[1] = new SampleFile(storagePath + "/skins/soft-hitwhistle.wav");
        mSoftSamples[2] = new SampleFile(storagePath + "/skins/soft-hitfinish.wav");
        mSoftSamples[3] = new SampleFile(storagePath + "/skins/soft-hitclap.wav");
        mNormalSamples = new SampleFile[4];
        mNormalSamples[0] = new SampleFile(storagePath + "/skins/normal-hitnormal.wav");
        mNormalSamples[1] = new SampleFile(storagePath + "/skins/normal-hitwhistle.wav");
        mNormalSamples[2] = new SampleFile(storagePath + "/skins/normal-hitfinish.wav");
        mNormalSamples[3] = new SampleFile(storagePath + "/skins/normal-hitclap.wav");
        mPos = 0;
    }

    /**
     * 清理音效播放器占用的资源
     */
    public void release() {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onUpdate() {
        // do play hit effects
        long length = mAudioPlayer.getChannelLength();
        long position = mAudioPlayer.getChannelPosition();
        double total = mAudioPlayer.Bytes2Second(length);
        double elapsed = mAudioPlayer.Bytes2Second(position);

        while (mPos < mBeatmap.getHitObjects().size()
                && (elapsed * 1000 >= mBeatmap.getHitObjects().get(mPos).getTime()
                + mBeatmap.getAudioOffset() / 1000.0)) {
            if (mPos + 1 < mBeatmap.getHitObjects().size()
                    && (elapsed * 1000 >= mBeatmap.getHitObjects().get(mPos + 1).getTime()
                    + mBeatmap.getAudioOffset() / 1000.0)) {
                ++mPos;
                continue;
            }

            for (int i = 0; i < 4; i++) {
                if ((mBeatmap.getHitObjects().get(mPos).getSound() & (1 << i)) != 0) {
                    if (mBeatmap.getHitObjects().get(mPos).getSoundType() == 1) {
                        mNormalSamples[i].play();
                        mNormalSamples[i].setVolume(mBeatmap.getHitObjects().get(mPos).getVolume());
                    } else if (mBeatmap.getHitObjects().get(mPos).getSound() == 2) {
                        mSoftSamples[i].play();
                        mSoftSamples[i].setVolume(mBeatmap.getHitObjects().get(mPos).getVolume());
                    } else {
                        if (mBeatmap.getSoundType().equals("normal")) {
                            mNormalSamples[i].play();
                            mNormalSamples[i].setVolume(mBeatmap.getHitObjects().get(mPos).getVolume());
                        } else {
                            mSoftSamples[i].play();
                            mSoftSamples[i].setVolume(mBeatmap.getHitObjects().get(mPos).getVolume());
                        }
                    }
                }
            }

            ++mPos;
        }
    }

    public void reset() {
        mPos = 0;
    }
}
