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

package com.andrew.apollo.loaders;

import android.content.Context;
import android.database.Cursor;
import com.andrew.apollo.model.Directory;
import com.andrew.apollo.provider.DirectoryStore;
import com.andrew.apollo.provider.DirectoryStore.DirectoryColumns;
import com.andrew.apollo.utils.Lists;

import java.util.ArrayList;
import java.util.List;

/**
 * Used to query {@link com.andrew.apollo.provider.OsuFileStore} and return
 * the osu files on local storage.
 *
 * @author Andrew Neal (andrewdneal@gmail.com)
 * @author dgsrz (dgsrz@vip.qq.com)
 */
public class DirectoryLoader extends WrappedAsyncTaskLoader<List<Directory>> {

    /**
     * The result
     */
    private final ArrayList<Directory> mDirectoryList = Lists.newArrayList();

    /**
     * The {@link android.database.Cursor} used to run the query.
     */
    private Cursor mCursor;

    /**
     * Constructor of <code>DirectoryLoader</code>
     *
     * @param context The {@link android.content.Context} to use
     */
    public DirectoryLoader(final Context context) {
        super(context);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Directory> loadInBackground() {
        // Create the Cursor
        mCursor = makeDirectoryCursor(getContext());
        // Gather the data
        if (mCursor != null && mCursor.moveToFirst()) {
            do {
                // Copy the album id
                final int id = mCursor.getInt(0);

                // Copy the album name
                final String dirPath = mCursor.getString(1);

                // Copy the artist name
                final String artist = mCursor.getString(2);

                // Copy the number of songs
                final int songCount = mCursor.getInt(3);

                // Copy the number of songs
                final long lastModified = mCursor.getLong(4);

                // Create a new album
                final Directory directory = new Directory(id, dirPath, artist, songCount, lastModified);

                // Add everything up
                mDirectoryList.add(directory);
            } while (mCursor.moveToNext());
        }
        // Close the cursor
        if (mCursor != null) {
            mCursor.close();
            mCursor = null;
        }
        return mDirectoryList;
    }

    /**
     * Creates the {@link android.database.Cursor} used to run the query.
     *
     * @param context The {@link android.content.Context} to use.
     * @return The {@link android.database.Cursor} used to run the album query.
     */
    public static final Cursor makeDirectoryCursor(final Context context) {
        return DirectoryStore
                .getInstance(context)
                .getReadableDatabase()
                .query(DirectoryColumns.NAME,
                        new String[] {
                                DirectoryColumns.ID + " as _id",
                                DirectoryColumns.DIRNAME,
                                DirectoryColumns.ARTISTNAME,
                                DirectoryColumns.SONGCOUNT,
                                DirectoryColumns.LASTMODIFIED
                        }, null, null, null, null, null);
        /*
        return context.getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                new String[] {
                        BaseColumns._ID,
                        AlbumColumns.ALBUM,
                        AlbumColumns.ARTIST,
                        AlbumColumns.NUMBER_OF_SONGS,
                        AlbumColumns.FIRST_YEAR
                }, null, null, PreferenceUtils.getInstance(context).getAlbumSortOrder());
                */
    }
}
