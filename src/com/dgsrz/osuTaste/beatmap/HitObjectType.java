package com.dgsrz.osuTaste.beatmap;

/**
 * Created by: dgsrz
 * Date: 2014-01-31 16:02
 */
public enum HitObjectType {

    Normal(1), Slider(2), Spinner(8);

    private int value;

    private HitObjectType(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }
}
