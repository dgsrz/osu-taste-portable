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

import android.util.Log;

import java.util.*;
import java.util.regex.*;
import java.io.*;

/**
 * Beatmap载入相关的工具类
 *
 * @author dgsrz(dgsrz@vip.qq.com)
 */
public class Beatmap {

    private int mVersion = 0;
    private String mFileName = "";
    private String mAudioFileName = "";
    private ArrayList<HitObject> mHitObjects;
    private ArrayList<TimingPoint> mTimingPoints;
    private float mSliderMultiplier = 1.0f;
    private int mHPDrainRate = 0;
    private int mCircleSize = 0;
    private int mOverallDifficulty = 0;
    private int mApproachRate = 0;
    private int mAudioOffset = 0;
    private String mSoundType = "normal";
    private String mBackground = "";
    private HashMap<String, String> mMetaData;
    // Temporary not implement
    /**
		private bool isTaikoMode = false;
		private int bpmLowerBound = 60000;
		private int bpmUpperBound = 0;
		private double previewTime = 0.0f;
     */
    private StringBuffer key = null;
    private StringBuffer value = null;

    public Beatmap() {
        mHitObjects = new ArrayList<HitObject>();
        mTimingPoints = new ArrayList<TimingPoint>();
        mMetaData = new HashMap<String, String>();
    }

    public Beatmap(String fileName) {
        this();
        this.mFileName = fileName;
        try {
            loadBeatmap();
        } catch (IOException e) {
            Log.e("BeatmapIO", e.getMessage());
        }
    }

    public void release() {
        mFileName = null;
        mAudioFileName = null;
        mSoundType = null;
        mBackground = null;
        key = null;
        value = null;
        if (mHitObjects != null) {
            mHitObjects.clear();
            mHitObjects = null;
        }
        if (mTimingPoints != null) {
            mTimingPoints.clear();
            mTimingPoints = null;
        }
        if (mMetaData != null) {
            mMetaData.clear();
            mMetaData = null;
        }
    }

    public void loadBeatmap() throws IOException {
        FileReader fileReader = new FileReader(mFileName);
        BufferedReader buff = new BufferedReader(fileReader);

        String header = buff.readLine().trim();
        Pattern pattern = Pattern.compile("osu file format v(\\d+)");
        Matcher matcher = pattern.matcher(header);
        if (!matcher.find()) {
            Log.e("BeatmapParsing", "Incompatible beatmap version.");
        }
        mVersion = Integer.parseInt(matcher.group(1));

        String line;
        while ((line = buff.readLine()) != null) {
            pattern = Pattern.compile("\\[(\\w+)]");
            matcher = pattern.matcher(line.trim());

            if (matcher.find()) {
                String title = matcher.group(1);
                if (title.equals("General")) {
                    parseGeneral(buff);
                } else if (title.equals("Metadata")) {
                    // TODO: Metadata display on player
                } else if (title.equals("Difficulty")) {
                    parseDifficulty(buff);
                } else if (title.equals("Events")) {
                    parseEvent(buff);
                } else if (title.equals("TimingPoints")) {
                    parseTimingPoints(buff);
                } else if (title.equals("HitObjects")) {
                    parseHitObject(buff);
                }
            }
        }
        buff.close();
    }

    private void parseGeneral(BufferedReader buff) throws IOException {
        String line;
        while ((line = buff.readLine()) != null) {
            line = line.trim();
            if (line.equals("")) return;

            splitKeyValue(line);

            if (key.toString().equals("AudioFilename")) {
                mAudioFileName = value.toString();
            } else if (key.toString().equals("AudioLeadIn")) {
                mAudioOffset = Integer.parseInt(value.toString());
            } else if (key.toString().equals("PreviewTime")) {
                // TODO: Start play on yellow line position
            } else if (key.toString().equals("Mode")) {
                // TODO: Support for taiko mode
            } else if (key.toString().equals("SampleSet")) {
                mSoundType = value.toString().toLowerCase();
            }
        }
    }

    private void parseTimingPoints(BufferedReader buff) throws IOException {
        String line;
        String info[];
        TimingPoint prevTimingPoint = null;
        while ((line = buff.readLine()) != null) {
            line = line.trim();
            if (line.equals("")) return;

            info = line.split(",");
            TimingPoint tp;
            if (info.length > 6 && Integer.parseInt(info[6]) == 0) {
                tp = new InheritedTimingPoint(prevTimingPoint);
            } else {
                tp = new TimingPoint();
                prevTimingPoint = tp;
            }
            tp.setBeginTime(info[0]);
            tp.setBeatTime(info[1]);
            if (info.length > 3) {
                tp.setSoundType(info[3]);
            }
            if (info.length > 4) {
                tp.setCustomSound(info[4]);
            }
            if (info.length > 5) {
                tp.setVolume(info[5]);
            }
            mTimingPoints.add(tp);
        }
    }

    private void parseDifficulty(BufferedReader buff) throws IOException {
        String line;
        while ((line = buff.readLine()) != null) {
            line = line.trim();
            if (line.equals("")) return;

            splitKeyValue(line);

            if (key.toString().equals("SliderMultiplier")) {
                mSliderMultiplier = Float.parseFloat(value.toString());
            }
        }
    }

    private void parseEvent(BufferedReader buff) throws IOException {
        String line;
        String info[];
        while ((line = buff.readLine()) != null) {
            line = line.trim();
            if (line.equals("")) return;

            if (line.contains(",")) {
                info = line.split(",");
                Pattern pattern = Pattern.compile("[^\"]+\\.(jpg|png)");
                Matcher matcher = pattern.matcher(line);
                if (info[0].equals("0") && matcher.find()) {
                    mBackground = matcher.group(0);
                    Log.i("BeatmapParse", mBackground);
                }
            }
        }
    }

    private void parseHitObject(BufferedReader buff) throws IOException {
        String line;
        String val[];
        int pos = 0;
        while ((line = buff.readLine()) != null) {
            line = line.trim();
            if (line.equals("")) return;

            val = line.split(",");
            int time = Integer.parseInt(val[2]);

            while (pos < mTimingPoints.size() - 1
                    && mTimingPoints.get(pos + 1).getBeginTime() <= time) {
                ++pos;  // Set timing attribute for each hit object
            }

            int soundType = Integer.parseInt(val[3]);
            mHitObjects.addAll(createHitObjectCollection(soundType, mTimingPoints.get(pos), val));
        }
    }

    private ArrayList<HitObject> createHitObjectCollection(
            int objectType, TimingPoint timingPoint, String info[]) {
        ArrayList<HitObject> objects = new ArrayList<HitObject>();

        if ((objectType & HitObjectType.Normal.getValue()) > 0) {
            HitObject obj = new HitObject();
            obj.setTime(info[2]);
            obj.setSound(info[4]);
            obj.setVolume(timingPoint.getVolume());
            obj.setSoundType(timingPoint.getSoundType());
            obj.setCustomSound(timingPoint.getCustomSound());
            objects.add(obj);
        }

        if ((objectType & HitObjectType.Slider.getValue()) > 0) {
            int baseTime = Integer.parseInt(info[2]);
            float sliderLength = Float.parseFloat(info[7]);
            int sliderCount = Integer.parseInt(info[6]) + 1;
            float sliderTime = timingPoint.getBeatTime() * (sliderLength / mSliderMultiplier) / 100;
            String soundCollection[] = null;
            if (info.length > 8) {
                soundCollection = info[8].split("\\|");
            }
            for (int i = 0; i < sliderCount; ++i) {
                HitObject obj = new HitObject();
                obj.setTime((int) (baseTime + i * sliderTime));
                obj.setVolume(timingPoint.getVolume());
                obj.setSoundType(timingPoint.getSoundType());
                obj.setCustomSound(timingPoint.getCustomSound());
                if (info.length > 8) {
                    obj.setSound(soundCollection[i]);
                } else {
                    obj.setSound(1);
                }
                objects.add(obj);
            }
        }

        if ((objectType & HitObjectType.Spinner.getValue()) > 0) {
            HitObject obj = new HitObject();
            obj.setTime(info[5]);
            obj.setSound(info[4]);
            obj.setVolume(timingPoint.getVolume());
            obj.setSoundType(timingPoint.getSoundType());
            obj.setCustomSound(timingPoint.getCustomSound());
            objects.add(obj);
        }

        return objects;
    }

    private void splitKeyValue(String str) {
        int index = str.indexOf(':');
        key = new StringBuffer(str.substring(0, index).trim());
        value = new StringBuffer(str.substring(index + 1, str.length()).trim());
    }

    public int getAudioOffset() {
        return mAudioOffset;
    }

    public ArrayList<HitObject> getHitObjects() {
        return mHitObjects;
    }

    public String getSoundType() {
        return mSoundType;
    }

    public String getAudioFileName() {
        return mAudioFileName;
    }

    public String getStoryBoard() {
        return mBackground;
    }

}
