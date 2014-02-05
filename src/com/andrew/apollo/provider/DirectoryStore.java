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
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.andrew.apollo.model.Directory;

/**
 * This class is used to to create the database used to store directories.
 *
 * @author Andrew Neal (andrewdneal@gmail.com)
 * @author dgsrz (dgsrz@vip.qq.com)
 */
public class DirectoryStore extends SQLiteOpenHelper {

    /* Version constant to increment when the database should be rebuilt */
    private static final int VERSION = 1;

    /* Name of database file */
    public static final String DATABASENAME = "osu_dir.db";

    private static DirectoryStore sInstance = null;

    /**
     * Constructor of <code>OsuFileStore</code>
     *
     * @param context The {@link android.content.Context} to use
     */
    public DirectoryStore(final Context context) {
        super(context, DATABASENAME, null, VERSION);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(final SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + DirectoryColumns.NAME + " ("
                + DirectoryColumns.ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + DirectoryColumns.DIRNAME
                + " TEXT NOT NULL," + DirectoryColumns.ARTISTNAME + " TEXT NOT NULL,"
                + DirectoryColumns.SONGCOUNT + " TEXT NOT NULL,"
                + DirectoryColumns.LASTMODIFIED + " LONG NOT NULL);");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + DirectoryColumns.NAME);
        onCreate(db);
    }

    /**
     * @param context The {@link android.content.Context} to use
     * @return A new instance of this class.
     */
    public static final synchronized DirectoryStore getInstance(final Context context) {
        if (sInstance == null) {
            sInstance = new DirectoryStore(context.getApplicationContext());
        }
        return sInstance;
    }

    /**
     * Used to store some directories in the database.
     * 
     * @param dirs The directory collection.
     */
    public void addDirCollection(final Directory dirs[]) {
        final SQLiteDatabase database = getWritableDatabase();

        database.beginTransaction();

        for (Directory dir : dirs) {
            if (dir.mDirPath == null || dir.mArtistName == null) {
                continue;
            }
            final ContentValues values = new ContentValues(4);
            values.put(DirectoryColumns.DIRNAME, dir.mDirPath);
            values.put(DirectoryColumns.ARTISTNAME, dir.mArtistName);
            values.put(DirectoryColumns.SONGCOUNT, dir.mSongNumber);
            values.put(DirectoryColumns.LASTMODIFIED, dir.mLastModified);

            database.insert(DirectoryColumns.NAME, null, values);
        }
        database.setTransactionSuccessful();
        database.endTransaction();
    }

    /**
     * Clear the database.
     */
    public void deleteDatabase() {
        final SQLiteDatabase database = getReadableDatabase();
        database.delete(DirectoryColumns.NAME, null, null);
    }

    /**
     * @param dirId The directory Id to remove.
     */
    public void removeItem(final long dirId) {
        final SQLiteDatabase database = getReadableDatabase();
        database.delete(DirectoryColumns.NAME, DirectoryColumns.ID + " = ?", new String[] {
            String.valueOf(dirId)
        });
    }

    public static final String getDirectoryName(final Context context, final Integer dirId) {
        final StringBuilder selection = new StringBuilder();
        selection.append(DirectoryColumns.ID + "=" + dirId);
        Cursor cursor = DirectoryStore
                .getInstance(context)
                .getReadableDatabase()
                .query(DirectoryColumns.NAME,
                        new String[] {
                                DirectoryColumns.DIRNAME,
                        }, selection.toString(), null, null, null, null);
        String dirName = "";
        if (cursor != null && cursor.moveToFirst()) {
            dirName = cursor.getString(0);
            cursor.close();
            cursor = null;
        }
        return dirName;
    }

    public interface DirectoryColumns {

        /* Table name */
        public static final String NAME = "localdir";

        /* Directory IDs column */
        public static final String ID = "id";

        /* Directory's name column */
        public static final String DIRNAME = "dirname";

        /* Artist name column */
        public static final String ARTISTNAME = "artistname";

        /* Total songs in directory */
        public static final String SONGCOUNT = "songcount";

        /* Last modified time */
        public static final String LASTMODIFIED = "lastmod";
    }
}
