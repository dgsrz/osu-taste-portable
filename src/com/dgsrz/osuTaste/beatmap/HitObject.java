package com.dgsrz.osuTaste.beatmap;

/**
 * Created by: dgsrz
 * Date: 2014-01-31 15:41
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
