package com.dgsrz.osuTaste.beatmap;

/**
 * Created by: dgsrz
 * Date: 2014-01-31 15:44
 */
public class InheritedTimingPoint extends TimingPoint {

    private TimingPoint parent;

    public InheritedTimingPoint() { }

    public InheritedTimingPoint(TimingPoint timingPoint) {
        this.parent = timingPoint;
        setInherited(true);
    }

    @Override
    public float getBeatTime() {
        return parent.getBeatTime() * getMultiplier();
    }

    @Override
    public float getMultiplier() {
        return -(super.getBeatTime() / 100);
    }

}
