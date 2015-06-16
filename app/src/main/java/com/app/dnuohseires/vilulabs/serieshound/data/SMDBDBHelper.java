package com.app.dnuohseires.vilulabs.serieshound.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Virginia on 5/30/2015.
 */
public class SMDBDBHelper extends SQLiteOpenHelper {

    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 6;

    public static final String DATABASE_NAME = "seriesmovies.db";

    public SMDBDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        // Create a table to hold locations.  A location consists of the string supplied in the
        // location setting, the city name, and the latitude and longitude
        final String SQL_CREATE_GENRE_TABLE = "CREATE TABLE " + SMDBContract.GenreEntry.TABLE_NAME + " (" +
                SMDBContract.GenreEntry._ID + " INTEGER PRIMARY KEY," +
                SMDBContract.GenreEntry.COLUMN_TMDB_ID + " INTEGER UNIQUE NOT NULL, " +
                SMDBContract.GenreEntry.COLUMN_CATEGORY_KEY + " TEXT NOT NULL, " +
                SMDBContract.GenreEntry.COLUMN_DESCRIPTION + " TEXT NOT NULL" +
                " );";

        final String SQL_CREATE_SMCONTENT_TABLE = "CREATE TABLE " + SMDBContract.SMContentEntry.TABLE_NAME + " (" +
                // Why AutoIncrement here, and not above?
                // Unique keys will be auto-generated in either case.  But for weather
                // forecasting, it's reasonable to assume the user will want information
                // for a certain date and all dates *following*, so the forecast data
                // should be sorted accordingly.
                SMDBContract.SMContentEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +

                // the ID of the location entry associated with this weather data
                SMDBContract.SMContentEntry.COLUMN_CATEGORY_KEY + " TEXT NOT NULL, " +
                SMDBContract.SMContentEntry.COLUMN_AIR_DATE + " INTEGER NOT NULL, " +
                SMDBContract.SMContentEntry.COLUMN_TITLE + " TEXT NOT NULL, " +
                SMDBContract.SMContentEntry.COLUMN_TMDB_ID + " INTEGER NOT NULL," +

                SMDBContract.SMContentEntry.COLUMN_POPULARITY + " REAL NOT NULL, " +
                SMDBContract.SMContentEntry.COLUMN_POSTER + " TEXT NOT NULL," +
                SMDBContract.SMContentEntry.COLUMN_BACKDROP + " TEXT NOT NULL);";

        final String SQL_CREATE_SMCONTENT_DETAIL_TABLE = "CREATE TABLE " + SMDBContract.SMContentDetailEntry.TABLE_NAME + " (" +
                SMDBContract.SMContentDetailEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +

                // the ID of the location entry associated with this weather data
                SMDBContract.SMContentDetailEntry.COLUMN_CONTENT_ID + " INTEGER NOT NULL, " +
                SMDBContract.SMContentDetailEntry.COLUMN_SUMMARY + " TEXT NOT NULL, " +
                SMDBContract.SMContentDetailEntry.COLUMN_SEASON_COUNT + " INTEGER NOT NULL, " +
                SMDBContract.SMContentDetailEntry.COLUMN_EPISODES_COUNT + " INTEGER NOT NULL," +

                SMDBContract.SMContentDetailEntry.COLUMN_NETWORK + " TEXT NOT NULL, " +
                SMDBContract.SMContentDetailEntry.COLUMN_STATUS + " TEXT NOT NULL);";

        final String SQL_CREATE_SMCONTENT_EPISODE_TABLE = "CREATE TABLE " + SMDBContract.SMContentEpisodeEntry.TABLE_NAME + " (" +
                SMDBContract.SMContentEpisodeEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +

                // the ID of the location entry associated with this weather data
                SMDBContract.SMContentEpisodeEntry.COLUMN_CONTENT_DETAIL_ID + " INTEGER NOT NULL, " +
                SMDBContract.SMContentEpisodeEntry.COLUMN_OVERVIEW + " TEXT NOT NULL, " +
                SMDBContract.SMContentEpisodeEntry.COLUMN_SEASON_ID + " INTEGER NOT NULL, " +
                SMDBContract.SMContentEpisodeEntry.COLUMN_EPISODE_ID + " INTEGER NOT NULL," +

                SMDBContract.SMContentEpisodeEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                SMDBContract.SMContentEpisodeEntry.COLUMN_AIR_DATE + " INTEGER NOT NULL);";

        final String SQL_CREATE_CONTENT_GENRE_TABLE = "CREATE TABLE " + SMDBContract.SMContentGenre.TABLE_NAME + " (" +
                SMDBContract.SMContentGenre._ID + " INTEGER PRIMARY KEY," +
                SMDBContract.SMContentGenre.COLUMN_TMDB_GENRE_KEY+ " TEXT NOT NULL, " +
                SMDBContract.SMContentGenre.COLUMN_CONTENT_KEY + " TEXT NOT NULL" +
                " );";

        sqLiteDatabase.execSQL(SQL_CREATE_GENRE_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_SMCONTENT_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_SMCONTENT_DETAIL_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_SMCONTENT_EPISODE_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_CONTENT_GENRE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        // Note that this only fires if you change the version number for your database.
        // It does NOT depend on the version number for your application.
        // If you want to update the schema without wiping data, commenting out the next 2 lines
        // should be your top priority before modifying this method.
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + SMDBContract.GenreEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + SMDBContract.SMContentEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + SMDBContract.SMContentGenre.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + SMDBContract.SMContentDetailEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + SMDBContract.SMContentEpisodeEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
