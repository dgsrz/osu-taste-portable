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

package net.moesky.osuplayer.beatmap;

/**
 * @author dgsrz(dgsrz@vip.qq.com)
 */
public class TimingPoint {

    private int beginTime;
    private float beatTime;
    private boolean inherited = false;
    private float volume = 0.8f;
    private int soundType = 0; // 0-Normal, 1-Soft, 2-Drum
    private int customSound = 0;
    private boolean kiasTime = false;

    public int getBeginTime() {
        return beginTime;
    }

    public float getBeatTime() {
        return beatTime;
    }

    public boolean isInherited() {
        return inherited;
    }

    public float getVolume() {
        return volume;
    }

    public int getSoundType() {
        return soundType;
    }

    public int getCustomSound() {
        return customSound;
    }

    public boolean isKiasTime() {
        return kiasTime;
    }

    public float getMultiplier() {
        return 1.0f;
    }

    public void setBeginTime(String beginTime) {
        this.beginTime = (int) Float.parseFloat(beginTime);
    }

    public void setBeatTime(String beatTime) {
        this.beatTime = Float.parseFloat(beatTime);
    }

    public void setInherited(boolean inherited) {
        this.inherited = inherited;
    }

    public void setVolume(String volume) {
        this.volume = Float.parseFloat(volume) / 100.0f;
    }

    public void setSoundType(String soundType) {
        this.soundType = Integer.parseInt(soundType);
    }

    public void setCustomSound(String customSound) {
        this.customSound = Integer.parseInt(customSound);
    }

    public void setKiasTime(boolean kiasTime) {
        this.kiasTime = kiasTime;
    }

}
