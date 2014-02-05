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
 * HitObject描述
 *
 * @author dgsrz(dgsrz@vip.qq.com)
 */
public class HitObject {

    private int time;
    private int sound;
    private float volume = 0.8f;
    private int soundType = 0;
    private int customSound = 0;

    public HitObject() {
    }

    public int getTime() {
        return time;
    }

    public int getSound() {
        return sound;
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

    public void setTime(int time) {
        this.time = time;
    }

    public void setTime(String time) {
        this.time = Integer.parseInt(time);
    }

    public void setSound(int sound) {
        this.sound = sound;
    }

    public void setSound(String sound) {
        this.sound = Integer.parseInt(sound) | 1;
    }

    public void setVolume(float volume) {
        this.volume = volume;
    }

    public void setSoundType(int soundType) {
        this.soundType = soundType;
    }

    public void setCustomSound(int customSound) {
        this.customSound = customSound;
    }
}
