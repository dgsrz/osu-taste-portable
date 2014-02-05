package com.andrew.apollo.utils;

import android.content.Context;
import com.andrew.apollo.model.Directory;
import com.andrew.apollo.model.Song;
import com.andrew.apollo.provider.DirectoryStore;
import com.andrew.apollo.provider.OsuFileStore;

/**
 * 调试工具类，正式版本中不应该引用此类
 *
 * @author dgsrz (dgsrz@vip.qq.com)
 */
public final class DebugUtils {

    private static String folders = "/sdcard/osu!Player";
    private static String files[] = { "guilty", "dash", "test", "fate", "ga", "vid" };

    public static void GenerateSomeJunkFolders(final Context context) {
        Directory directories[] = new Directory[1];
        Song songs[] = new Song[6];

        Directory directory = new Directory(0
                , folders, "dgsrz", 0, System.currentTimeMillis());
        directories[0] = directory;
        DirectoryStore.getInstance(context).addDirCollection(directories);

        int id = 0;
        for (String str : files) {
            Song song = new Song(0, folders, str + ".osu", str, "Insane", "dgsrz");
            songs[id] = song;
            id++;
        }
        OsuFileStore.getInstance(context).addSongCollection(songs);
    }

}
