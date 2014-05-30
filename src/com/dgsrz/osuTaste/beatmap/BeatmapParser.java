package com.dgsrz.osuTaste.beatmap;

import android.util.Log;
import com.dgsrz.osuTaste.exception.BeatmapParsingException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by: dgsrz
 * Date: 2014-01-31 15:40
 */
public class BeatmapParser {

    private int version = 0;
    private String fileName = "";
    public String audioFileName = "";
    private ArrayList<HitObject> hitObjects;
    private ArrayList<TimingPoint> timingPointses;
    private HashMap<String, String> metaData;
    private float sliderMultiplier = 1.0f;
    private int hpDrainRate = 0;
    private int circleSize = 0;
    private int overallDifficulty = 0;
    private int approachRate = 0;
    private int audioOffset = 0;
    private String soundType = "normal";
    private String background = "";
    // Temporary not implement
    /**
		private bool isTaikoMode = false;
		private int bpmLowerBound = 60000;
		private int bpmUpperBound = 0;
		private double previewTime = 0.0f;
     */
    private StringBuffer key = null;
    private StringBuffer value = null;

    public BeatmapParser() {
        hitObjects = new ArrayList<HitObject>();
        timingPointses = new ArrayList<TimingPoint>();
        metaData = new HashMap<String, String>();
    }

    public BeatmapParser(String fileName) {
        this();
        this.fileName = fileName;
        try {
            loadBeatmap();
        } catch (IOException e) {
            Log.e("BeatmapLoad IO Exception", e.getMessage());
        } catch (BeatmapParsingException be) {
            Log.e("BeatmapParsing Exception", be.getMessage());
        }
    }

    public void dispose() {
        hitObjects.clear();
        timingPointses.clear();
        metaData.clear();
        hitObjects = null;
        timingPointses = null;
        metaData = null;
    }

    public void loadBeatmap() throws IOException, BeatmapParsingException {
        FileReader fileReader = new FileReader(fileName);
        BufferedReader buff = new BufferedReader(fileReader);

        String header = buff.readLine().trim();
        Pattern pattern = Pattern.compile("osu file format v(\\d+)");
        Matcher matcher = pattern.matcher(header);
        if (!matcher.find()) {
            throw new BeatmapParsingException("Incompatible beatmap version.");
        }
        version = Integer.parseInt(matcher.group(1));

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
            timingPointses.add(tp);
        }
    }

    private void parseDifficulty(BufferedReader buff) throws IOException {
        String line;
        while ((line = buff.readLine()) != null) {
            line = line.trim();
            if (line.equals("")) return;

            splitKeyValue(line);

            if (key.toString().equals("SliderMultiplier")) {
                sliderMultiplier = Float.parseFloat(value.toString());
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
                    background = matcher.group(0);
                    Log.i("BeatmapParse", background);
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

            while (pos < timingPointses.size() - 1
                    && timingPointses.get(pos + 1).getBeginTime() <= time) {
                ++pos;  // Set timing attribute for each hit object
            }

            int soundType = Integer.parseInt(val[3]);
            hitObjects.addAll(createHitObjectCollection(soundType, timingPointses.get(pos), val));
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

    private void splitKeyValue(String str) {
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

    public String getStoryBoard() {
        return background;
    }

}
