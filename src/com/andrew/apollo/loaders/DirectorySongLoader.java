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
import android.provider.BaseColumns;
import com.andrew.apollo.model.Song;
import com.andrew.apollo.provider.DirectoryStore;
import com.andrew.apollo.provider.OsuFileStore;
import com.andrew.apollo.utils.Lists;

import java.util.ArrayList;
import java.util.List;

/**
 * Used to query {@link android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI} and return
 * the Song for a particular album.
 *
 * @author Andrew Neal (andrewdneal@gmail.com)
 */
public class DirectorySongLoader extends WrappedAsyncTaskLoader<List<Song>> {

    /**
     * The result
     */
    private final ArrayList<Song> mSongList = Lists.newArrayList();

    /**
     * The {@link android.database.Cursor} used to run the query.
     */
    private Cursor mCursor;

    /**
     * The Id of the album the songs belong to.
     */
    private final Integer mDirectoryId;

    /**
     * Constructor of <code>AlbumSongHandler</code>
     *
     * @param context The {@link android.content.Context} to use.
     * @param dirId The Id of the album the songs belong to.
     */
    public DirectorySongLoader(final Context context, final Integer dirId) {
        super(context);
        mDirectoryId = dirId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Song> loadInBackground() {
        // Create the Cursor
        mCursor = makeDirectorySongCursor(getContext(), mDirectoryId);
        // Gather the data
        if (mCursor != null && mCursor.moveToFirst()) {
            do {
                // Copy the song Id
                final long id = mCursor.getLong(0);

                // Copy the file name
                final String fileName = mCursor.getString(1);

                // Copy the song name
                final String songName = mCursor.getString(2);

                // Copy the artist name
                final String artist = mCursor.getString(3);

                // Copy the dir Path
                final String dirPath = mCursor.getString(4);

                // Copy the diffName
                final String diffName = mCursor.getString(5);

                // Create a new song
                final Song song = new Song(id, dirPath, fileName, songName, diffName, artist);

                // Add everything up
                mSongList.add(song);
            } while (mCursor.moveToNext());
        }
        // Close the cursor
        if (mCursor != null) {
            mCursor.close();
            mCursor = null;
        }
        return mSongList;
    }

    /**
     * @param context The {@link android.content.Context} to use.
     * @param dirId The Id of the album the songs belong to.
     * @return The {@link android.database.Cursor} used to run the query.
     */
    public static final Cursor makeDirectorySongCursor(final Context context, final Integer dirId) {
        // Match the songs up with the artist
        String dirName = DirectoryStore.getDirectoryName(context, dirId);

        final StringBuilder selection = new StringBuilder();
        selection.append(OsuFileStore.OsuFileColumns.FILENAME + " != ''");
        selection.append(" AND " + OsuFileStore.OsuFileColumns.DIRNAME + "='" + dirName + "'");
        return OsuFileStore
                .getInstance(context)
                .getReadableDatabase()
                .query(OsuFileStore.OsuFileColumns.NAME, new String[] {
                         /* 0 */
                        OsuFileStore.OsuFileColumns.ID + " as _id",
                         /* 1 */
                        OsuFileStore.OsuFileColumns.FILENAME,
                         /* 2 */
                        OsuFileStore.OsuFileColumns.SONGNAME,
                         /* 3 */
                        OsuFileStore.OsuFileColumns.ARTISTNAME,
                         /* 4 */
                        OsuFileStore.OsuFileColumns.DIRNAME,
                         /* 5 */
                        OsuFileStore.OsuFileColumns.DIFFNAME
                }, selection.toString(), null, null, null, null);
    }

}
