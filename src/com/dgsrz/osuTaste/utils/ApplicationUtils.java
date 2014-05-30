package com.dgsrz.osuTaste.utils;

import android.content.Context;
import android.os.Environment;
import com.dgsrz.osuTaste.model.Beatmap;
import com.dgsrz.osuTaste.provider.BeatmapStore;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author dgsrz (dgsrz@vip.qq.com)
 */
public class ApplicationUtils {

    private static String storagePath = Environment.getExternalStorageDirectory().getAbsoluteFile() + "/osu!droid/Songs";

    public static void syncBeatmaps(Context context) {
        File[] directories = (new File(storagePath)).listFiles();
        List<Beatmap> beatmaps = new ArrayList<Beatmap>();
        List<String> pendingRemove = new ArrayList<String>();
        if (directories != null) {
            for (File directory : directories) {
                if (directory.isDirectory()) {
                    File[] files = directory.listFiles();
                    for (File file : files) {
                        if (file.isDirectory()) continue;
                        String fileName = file.getName();
                        if (fileName.substring(fileName.indexOf(".") + 1).equals("osu")) {
                            pendingRemove.add(directory.getAbsolutePath());
                            beatmaps.add(new Beatmap(
                                    -1,
                                    fileName,
                                    directory.getAbsolutePath(),
                                    fileName,
                                    "Insane",
                                    "Mine",
                                    "Mine"
                            ));
                        }
                    }
                }
            }
        }
        BeatmapStore beatmapStore = BeatmapStore.getInstance(context);
        beatmapStore.removeItems(pendingRemove);
        beatmapStore.addBeatmapCollection(beatmaps);
    }

}
