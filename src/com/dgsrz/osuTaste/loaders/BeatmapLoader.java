package com.dgsrz.osuTaste.loaders;

import android.content.Context;
import android.database.Cursor;
import com.dgsrz.osuTaste.model.Beatmap;
import com.dgsrz.osuTaste.provider.BeatmapStore;
import com.dgsrz.osuTaste.provider.BeatmapStore.BeatmapColumns;
import com.dgsrz.osuTaste.utils.Lists;

import java.util.ArrayList;
import java.util.List;

/**
 * @author dgsrz (dgsrz@vip.qq.com)
 */
public class BeatmapLoader extends WrappedAsyncTaskLoader<List<Beatmap>> {

    private final ArrayList<Beatmap> mBeatmapList = Lists.newArrayList();

    private Cursor mCursor;

    public BeatmapLoader(final Context context) {
        super(context);
    }

    @Override
    public List<Beatmap> loadInBackground() {
        mCursor = makeBeatmapCursor(getContext());
        if (mCursor != null && mCursor.moveToFirst()) {
            do {
                final int id = mCursor.getInt(0);
                final String fileName = mCursor.getString(1);
                final String directory = mCursor.getString(2);
                final String title = mCursor.getString(3);
                final String difficulty = mCursor.getString(4);
                final String artist = mCursor.getString(5);
                final String mapper = mCursor.getString(6);
                Beatmap beatmap = new Beatmap(
                        id,
                        fileName,
                        directory,
                        title,
                        difficulty,
                        artist,
                        mapper
                );
                mBeatmapList.add(beatmap);
            } while (mCursor.moveToNext());
        }
        if (mCursor != null) {
            mCursor.close();
            mCursor = null;
        }
        return mBeatmapList;
    }

    public static final Cursor makeBeatmapCursor(final Context context) {
        return BeatmapStore
                .getInstance(context)
                .getReadableDatabase()
                .query(BeatmapColumns.NAME,
                        new String[] {
                                BeatmapColumns.ID + " as _id",
                                BeatmapColumns.FILENAME,
                                BeatmapColumns.DIRECTORY,
                                BeatmapColumns.TITLE,
                                BeatmapColumns.DIFFICULTY,
                                BeatmapColumns.ARTIST,
                                BeatmapColumns.MAPPER
                        }, null, null, null, null, null);
    }
}
