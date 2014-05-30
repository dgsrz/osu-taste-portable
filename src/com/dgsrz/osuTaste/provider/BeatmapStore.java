package com.dgsrz.osuTaste.provider;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.dgsrz.osuTaste.model.Beatmap;

import java.util.List;

/**
 * @author dgsrz (dgsrz@vip.qq.com)
 */
public class BeatmapStore extends SQLiteOpenHelper {

    private static final int VERSION = 1;

    public static final String DBNAME = "osu.db";

    private static BeatmapStore sInstance = null;

    public BeatmapStore(final Context context) {
        super(context, DBNAME, null, VERSION);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + BeatmapColumns.NAME + "("
                + BeatmapColumns.ID + " INTEGER PRIMARY KEY,"
                + BeatmapColumns.FILENAME + " TEXT NOT NULL,"
                + BeatmapColumns.DIRECTORY + " TEXT NOT NULL,"
                + BeatmapColumns.TITLE + " TEXT NOT NULL,"
                + BeatmapColumns.DIFFICULTY + " TEXT NOT NULL,"
                + BeatmapColumns.ARTIST + " TEXT NOT NULL,"
                + BeatmapColumns.MAPPER + " TEXT NOT NULL);");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onUpgrade(final SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + BeatmapColumns.NAME);
        onCreate(db);
    }

    public static final synchronized BeatmapStore getInstance(final Context context) {
        if (sInstance == null) {
            sInstance = new BeatmapStore(context.getApplicationContext());
        }
        return sInstance;
    }

    public void addBeatmapCollection(final List<Beatmap> beatmaps) {
        final SQLiteDatabase database = getWritableDatabase();

        database.beginTransaction();

        for (Beatmap beatmap : beatmaps) {
            if (beatmap.mFileName == null || beatmap.mDirectory == null || beatmap.mTitle == null
                    || beatmap.mDifficulty == null || beatmap.mArtist == null || beatmap.mMapper == null) {
                continue;
            }
            final ContentValues values = new ContentValues();
            values.put(BeatmapColumns.FILENAME, beatmap.mFileName);
            values.put(BeatmapColumns.DIRECTORY, beatmap.mDirectory);
            values.put(BeatmapColumns.TITLE, beatmap.mTitle);
            values.put(BeatmapColumns.DIFFICULTY, beatmap.mDifficulty);
            values.put(BeatmapColumns.ARTIST, beatmap.mArtist);
            values.put(BeatmapColumns.MAPPER, beatmap.mMapper);
            database.insert(BeatmapColumns.NAME, null, values);
        }

        database.setTransactionSuccessful();
        database.endTransaction();
    }

    public void deleteDatabase() {
        final SQLiteDatabase database = getWritableDatabase();
        database.delete(BeatmapColumns.NAME, null, null);
    }

    public void removeItem(final long songId) {
        final SQLiteDatabase database = getWritableDatabase();
        database.delete(BeatmapColumns.NAME, BeatmapColumns.ID + " = ?",
                new String[] { String.valueOf(songId) });
    }

    public void removeItems(final List<String> conditions) {
        final SQLiteDatabase database = getWritableDatabase();
        database.beginTransaction();

        for (String arg : conditions) {
            database.delete(BeatmapColumns.NAME, BeatmapColumns.DIRECTORY + " = ?",
                    new String[] { String.valueOf(arg) });
        }

        database.setTransactionSuccessful();
        database.endTransaction();
    }

    public interface BeatmapColumns {
        public static final String ID = "id";
        public static final String NAME = "beatmaps";
        public static final String FILENAME = "filename";
        public static final String DIRECTORY = "directory";
        public static final String TITLE = "title";
        public static final String DIFFICULTY = "difficulty";
        public static final String ARTIST = "artist";
        public static final String MAPPER = "mapper";
    }
}
