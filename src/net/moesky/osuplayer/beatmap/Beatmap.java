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

    private int version = 0;
    private String fileName = "";
    private String audioFileName = "";
    private ArrayList<HitObject> hitObjects;
    private ArrayList<TimingPoint> timingPoints;
    private float sliderMultiplier = 1.0f;
    private int hpDrainRate = 0;
    private int circleSize = 0;
    private int overallDifficulty = 0;
    private int approachRate = 0;
    private int audioOffset = 0;
    private String soundType = "normal";
    private String background = "";
    private HashMap<String, String> metaData;
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
        hitObjects = new ArrayList<HitObject>();
        timingPoints = new ArrayList<TimingPoint>();
        metaData = new HashMap<String, String>();
    }

    public Beatmap(String fileName) {
        this();
        this.fileName = fileName;
        try {
            LoadBeatmap();
        } catch (IOException e) {
            Log.e("BeatmapIO", e.getMessage());
        }
    }

    public void LoadBeatmap() throws IOException {
        FileReader fileReader = new FileReader(fileName);
        BufferedReader buff = new BufferedReader(fileReader);

        String header = buff.readLine().trim();
        Pattern pattern = Pattern.compile("osu file format v(\\d+)");
        Matcher matcher = pattern.matcher(header);
        if (!matcher.find()) {
            Log.e("BeatmapParsing", "Incompatible beatmap version.");
        }
        version = Integer.parseInt(matcher.group(1));

        String line;
        while ((line = buff.readLine()) != null) {
            pattern = Pattern.compile("\\[(\\w+)]");
            matcher = pattern.matcher(line.trim());

            if (matcher.find()) {
                String title = matcher.group(1);
                if (title.equals("General")) {
                    ParseGeneral(buff);
                } else if (title.equals("Metadata")) {
                    // TODO: Metadata display on player
                } else if (title.equals("Difficulty")) {
                    ParseDifficulty(buff);
                } else if (title.equals("Events")) {
                    // TODO: Background, StoryBoard, etc.
                } else if (title.equals("TimingPoints")) {
                    ParseTimingPoints(buff);
                } else if (title.equals("HitObjects")) {
                    ParseHitObject(buff);
                }
            }
        }
        buff.close();
    }

    private void ParseGeneral(BufferedReader buff) throws IOException {
        String line;
        while ((line = buff.readLine()) != null) {
            line = line.trim();
            if (line.equals("")) return;

            SplitKeyValue(line);

            if (key.toString().equals("AudioFilename")) {
                audioFileName = value.toString();
            } else if (key.toString().equals("AudioLeadIn")) {
                audioOffset = Integer.parseInt(value.toString());
            } else if (key.toString().equals("PreviewTime")) {
                // TODO: Start play on yellow line position
            } else if (key.toString().equals("Mode")) {
                // TODO: Support for taiko mode
            } else if (key.toString().equals("SampleSet")) {
                soundType = value.toString().toLowerCase();
            }
        }
    }

    private void ParseTimingPoints(BufferedReader buff) throws IOException {
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
            timingPoints.add(tp);
        }
    }

    private void ParseDifficulty(BufferedReader buff) throws IOException {
        String line;
        while ((line = buff.readLine()) != null) {
            line = line.trim();
            if (line.equals("")) return;

            SplitKeyValue(line);

            if (key.toString().equals("SliderMultiplier")) {
                sliderMultiplier = Float.parseFloat(value.toString());
            }
        }
    }

    private void ParseHitObject(BufferedReader buff) throws IOException {
        String line;
        String val[];
        int pos = 0;
        while ((line = buff.readLine()) != null) {
            line = line.trim();
            if (line.equals("")) return;

            val = line.split(",");
            int time = Integer.parseInt(val[2]);

            while (pos < timingPoints.size() - 1
                    && timingPoints.get(pos + 1).getBeginTime() <= time) {
                ++pos;  // Set timing attribute for each hit object
            }

            int soundType = Integer.parseInt(val[3]);
            hitObjects.addAll(createHitObjectCollection(soundType, timingPoints.get(pos), val));
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
            float sliderTime = timingPoint.getBeatTime() * (sliderLength / sliderMultiplier) / 100;
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

    private void SplitKeyValue(String str) {
        int index = str.indexOf(':');
        key = new StringBuffer(str.substring(0, index).trim());
        value = new StringBuffer(str.substring(index + 1, str.length()).trim());
    }

    public int getAudioOffset() {
        return audioOffset;
    }

    public ArrayList<HitObject> getHitObjects() {
        return hitObjects;
    }

    public String getSoundType() {
        return soundType;
    }

    public String getAudioFileName() {
        return audioFileName;
    }

}
