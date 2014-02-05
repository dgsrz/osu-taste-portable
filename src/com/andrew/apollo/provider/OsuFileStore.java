/*
 * Copyright (C) 2012 Andrew Neal Licensed under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package com.andrew.apollo.provider;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.andrew.apollo.model.Song;

/**
 * This class is used to to create the database used to store osu file info.
 *
 * @author Andrew Neal (andrewdneal@gmail.com)
 * @author dgsrz (dgsrz@vip.qq.com)
 */
public class OsuFileStore extends SQLiteOpenHelper {

    /* Version constant to increment when the database should be rebuilt */
    private static final int VERSION = 1;

    /* Name of database file */
    public static final String DATABASENAME = "osu.db";

    private static OsuFileStore sInstance = null;

    /**
     * Constructor of <code>OsuFileStore</code>
     *
     * @param context The {@link android.content.Context} to use
     */
    public OsuFileStore(final Context context) {
        super(context, DATABASENAME, null, VERSION);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(final SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + OsuFileColumns.NAME + " ("
                + OsuFileColumns.ID + " INTEGER PRIMARY KEY," + OsuFileColumns.DIRNAME
                + " TEXT NOT NULL," + OsuFileColumns.FILENAME + " TEXT NOT NULL,"
                + OsuFileColumns.SONGNAME + " TEXT NOT NULL," + OsuFileColumns.ARTISTNAME
                + " TEXT NOT NULL," + OsuFileColumns.DIFFNAME + " TEXT NOT NULL);");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + OsuFileColumns.NAME);
        onCreate(db);
    }

    /**
     * @param context The {@link android.content.Context} to use
     * @return A new instance of this class.
     */
    public static final synchronized OsuFileStore getInstance(final Context context) {
        if (sInstance == null) {
            sInstance = new OsuFileStore(context.getApplicationContext());
        }
        return sInstance;
    }

    /**
     * Used to store some osu files in the database.
     * 
     * @param songs The songs collection.
     */
    public void addSongCollection(final Song songs[]) {
        final SQLiteDatabase database = getWritableDatabase();

        database.beginTransaction();

        for (Song song : songs) {
            if (song.mDirPath == null || song.mFileName == null || song.mSongName == null
                    || song.mDiffName == null || song.mArtistName == null) {
                continue;
            }
            final ContentValues values = new ContentValues(5);
            values.put(OsuFileColumns.DIRNAME, song.mDirPath);
            values.put(OsuFileColumns.FILENAME, song.mFileName);
            values.put(OsuFileColumns.SONGNAME, song.mSongName);
            values.put(OsuFileColumns.DIFFNAME, song.mDiffName);
            values.put(OsuFileColumns.ARTISTNAME, song.mArtistName);

            database.insert(OsuFileColumns.NAME, null, values);
        }
        database.setTransactionSuccessful();
        database.endTransaction();
    }

    /**
     * Clear the database.
     */
    public void deleteDatabase() {
        final SQLiteDatabase database = getReadableDatabase();
        database.delete(OsuFileColumns.NAME, null, null);
    }

    /**
     * @param songId The file Id to remove.
     */
    public void removeItem(final long songId) {
        final SQLiteDatabase database = getReadableDatabase();
        database.delete(OsuFileColumns.NAME, OsuFileColumns.ID + " = ?", new String[] {
            String.valueOf(songId)
        });

    }

    public interface OsuFileColumns {

        /* Table name */
        public static final String NAME = "localfiles";

        /* Osu file IDs column */
        public static final String ID = "id";

        /* Directory's name column */
        public static final String DIRNAME = "dirname";

        /* Osu file name column */
        public static final String FILENAME = "filename";

        /* Osu file song name column */
        public static final String SONGNAME = "songname";

        /* Osu file difficulty name column */
        public static final String DIFFNAME = "diffname";

        /* Artist name column */
        public static final String ARTISTNAME = "artistname";
    }
}
