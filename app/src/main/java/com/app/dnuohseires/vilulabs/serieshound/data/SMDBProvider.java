package com.app.dnuohseires.vilulabs.serieshound.data;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import com.app.dnuohseires.vilulabs.serieshound.Utility;

/**
 * Created by Virginia on 5/31/2015.
 */
public class SMDBProvider extends ContentProvider {
    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private SMDBDBHelper mOpenHelper;

    static final int GENRE = 100;

    static final int SMCONTENT = 200;
    static final int SMCONTENT_CATEGORY = 201;
    static final int SMCONTENT_CONTENT_MULTIPLE_ID = 204;


    static final int SMCONTENT_DETAIL = 300;
    static final int SMCONTENT_DETAIL_WITH_ID = 301;

    static final int SMCONTENT_CONTENT_EPISODE = 400;
    static final int SMCONTENT_CONTENT_EPISODE_ID_SEASON = 401;

    private static final SQLiteQueryBuilder sContentDetailQueryBuilder;
    static{
        sContentDetailQueryBuilder = new SQLiteQueryBuilder();
        sContentDetailQueryBuilder.setTables(
                SMDBContract.SMContentDetailEntry.TABLE_NAME
                        + " LEFT JOIN " + SMDBContract.SMContentEntry.TABLE_NAME
                        + " ON " + SMDBContract.SMContentDetailEntry.TABLE_NAME + "." + SMDBContract.SMContentDetailEntry.COLUMN_CONTENT_ID
                        + " = " + SMDBContract.SMContentEntry.TABLE_NAME + "." + SMDBContract.SMContentEntry.COLUMN_TMDB_ID
        );
    }

    private static final SQLiteQueryBuilder sContentEpisodeQueryBuilder;
    static {
        sContentEpisodeQueryBuilder = new SQLiteQueryBuilder();
        sContentEpisodeQueryBuilder.setTables(
                SMDBContract.SMContentEpisodeEntry.TABLE_NAME
                        + " JOIN " + SMDBContract.SMContentEntry.TABLE_NAME
                        + " ON " + SMDBContract.SMContentEpisodeEntry.TABLE_NAME + "." + SMDBContract.SMContentEpisodeEntry.COLUMN_CONTENT_DETAIL_ID
                        + " = " + SMDBContract.SMContentEntry.TABLE_NAME + "." + SMDBContract.SMContentEntry.COLUMN_TMDB_ID
        );
    }

    private static final String sContentCategorySelection =
            SMDBContract.SMContentEntry.TABLE_NAME+
                    "." + SMDBContract.SMContentEntry.COLUMN_CATEGORY_KEY + " = ? ";

    private static final String sContentTMDBIdSelection =
            SMDBContract.SMContentEntry.TABLE_NAME +
                    "." + SMDBContract.SMContentEntry.COLUMN_TMDB_ID + " = ? ";

    private static final String sContentFutureSeasonEpisodesSelection =
            SMDBContract.SMContentEpisodeEntry.COLUMN_CONTENT_DETAIL_ID + " = ? AND "
                    + SMDBContract.SMContentEpisodeEntry.COLUMN_SEASON_ID + " = ? AND "
                    + SMDBContract.SMContentEpisodeEntry.TABLE_NAME + "." + SMDBContract.SMContentEpisodeEntry.COLUMN_AIR_DATE + " >= ? ";

    private static final String sContentFutureEpisodesSelection =
            SMDBContract.SMContentEpisodeEntry.COLUMN_CONTENT_DETAIL_ID + " = ? AND "
                    + SMDBContract.SMContentEpisodeEntry.TABLE_NAME + "." + SMDBContract.SMContentEpisodeEntry.COLUMN_AIR_DATE + " >= ? ";

    private Cursor getContentByCategory(Uri uri, String[] projection, String sortOrder) {
        String categoryKey = SMDBContract.SMContentEntry.getCategoryFromUri(uri);

        String[] selectionArgs;
        String selection;

        selection = sContentCategorySelection;
        selectionArgs = new String[]{categoryKey};

        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(SMDBContract.SMContentEntry.TABLE_NAME);
        queryBuilder.setDistinct(true);
        return queryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    private Cursor getContentByMultipleId(Uri uri, String[] projection, String sortOrder) {
        String idsKey = SMDBContract.SMContentEntry.getContentIdFromUri(uri);

        String selection = SMDBContract.SMContentEntry.TABLE_NAME +
                "." + SMDBContract.SMContentEntry.COLUMN_TMDB_ID + " IN ( " + makePlaceholders(idsKey.split(",").length) + " )";

        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(SMDBContract.SMContentEntry.TABLE_NAME);
        queryBuilder.setDistinct(true);
        return queryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                idsKey.split(","),
                null,
                null,
                sortOrder
        );
    }

    String makePlaceholders(int len) {
        if (len < 1) {
            // It will lead to an invalid query anyway ..
            throw new RuntimeException("No placeholders");
        } else {
            StringBuilder sb = new StringBuilder(len * 2 - 1);
            sb.append("?");
            for (int i = 1; i < len; i++) {
                sb.append(",?");
            }
            return sb.toString();
        }
    }

    private Cursor getContentDetailById(
            Uri uri, String[] projection, String sortOrder) {
        String contentId = SMDBContract.SMContentEntry.getContentIdFromUri(uri);

        return sContentDetailQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                sContentTMDBIdSelection,
                new String[]{contentId},
                null,
                null,
                sortOrder
        );
    }

    private Cursor getContentEpisodesByIdAndSeason(
            Uri uri, String[] projection, String sortOrder) {
        String contentId = SMDBContract.SMContentEpisodeEntry.getContentIdFromUri(uri);
        String seasonId = SMDBContract.SMContentEpisodeEntry.getSeasonIdFromUri(uri);
        String selection = sContentFutureSeasonEpisodesSelection;
        String[] selectionArgs = new String[]{contentId, seasonId, String.valueOf(Utility.getTodayInMillis())};
        if (Integer.valueOf(seasonId) == 0) {
            selection = sContentFutureEpisodesSelection;
            selectionArgs = new String[]{contentId, String.valueOf(Utility.getTodayInMillis())};
        }

        SQLiteQueryBuilder queryBuilder = sContentEpisodeQueryBuilder;
        queryBuilder.setDistinct(true);

        return queryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    static UriMatcher buildUriMatcher() {
        // I know what you're thinking.  Why create a UriMatcher when you can use regular
        // expressions instead?  Because you're not crazy, that's why.

        // All paths added to the UriMatcher have a corresponding code to return when a match is
        // found.  The code passed into the constructor represents the code to return for the root
        // URI.  It's common to use NO_MATCH as the code for this case.
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = SMDBContract.CONTENT_AUTHORITY;

        // For each type of URI you want to add, create a corresponding code.
        matcher.addURI(authority, SMDBContract.PATH_GENRE, GENRE);

        matcher.addURI(authority, SMDBContract.PATH_SMDBCONTENT, SMCONTENT);
        matcher.addURI(authority, SMDBContract.PATH_SMDBCONTENT + "/*", SMCONTENT_CATEGORY);
        matcher.addURI(authority, SMDBContract.PATH_SMDBCONTENT  + "/*/*", SMCONTENT_CONTENT_MULTIPLE_ID);

        matcher.addURI(authority, SMDBContract.PATH_SMDBCONTENT_DETAIL + "/*/#", SMCONTENT_DETAIL_WITH_ID);

        matcher.addURI(authority, SMDBContract.PATH_SMDBCONTENT_EPISODE, SMCONTENT_CONTENT_EPISODE);
        matcher.addURI(authority, SMDBContract.PATH_SMDBCONTENT_EPISODE + "/#/#", SMCONTENT_CONTENT_EPISODE_ID_SEASON);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new SMDBDBHelper(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {
        // Use the Uri Matcher to determine what kind of URI this is.
        final int match = sUriMatcher.match(uri);
        String type="";
        switch (match) {
            case GENRE:
                type=SMDBContract.GenreEntry.CONTENT_TYPE;
                break;
            case SMCONTENT:
                type= SMDBContract.SMContentEntry.CONTENT_TYPE;
                break;
            case SMCONTENT_CATEGORY:
                type= SMDBContract.SMContentEntry.CONTENT_TYPE;
                break;
            case SMCONTENT_DETAIL_WITH_ID:
                type= SMDBContract.SMContentDetailEntry.CONTENT_ITEM_TYPE;
                break;
            case SMCONTENT_CONTENT_EPISODE:
                type= SMDBContract.SMContentEpisodeEntry.CONTENT_TYPE;
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        return type;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            case SMCONTENT_CATEGORY:
                retCursor = getContentByCategory(uri, projection, sortOrder);
                break;

            case SMCONTENT_DETAIL_WITH_ID:
                retCursor = getContentDetailById(uri, projection, sortOrder);
                break;

            case SMCONTENT:
                retCursor = mOpenHelper.getReadableDatabase().query(
                        SMDBContract.SMContentEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            case SMCONTENT_CONTENT_MULTIPLE_ID:
                retCursor = getContentByMultipleId(uri,projection,sortOrder);
                break;
            case SMCONTENT_CONTENT_EPISODE_ID_SEASON:
                retCursor = getContentEpisodesByIdAndSeason(uri,projection,sortOrder);
                break;
            case GENRE:
                retCursor = mOpenHelper.getReadableDatabase().query(
                        SMDBContract.GenreEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case SMCONTENT: {
                long _id = db.insert(SMDBContract.SMContentEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = SMDBContract.SMContentEntry.buildContentUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case GENRE: {
                long _id = db.insert(SMDBContract.GenreEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = SMDBContract.GenreEntry.buildGenreUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case SMCONTENT_CONTENT_EPISODE:{
                long _id = db.insert(SMDBContract.SMContentEpisodeEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = SMDBContract.SMContentEpisodeEntry.buildContentUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;}
            case SMCONTENT_DETAIL_WITH_ID: {
                long _id = db.insert(SMDBContract.SMContentDetailEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = SMDBContract.SMContentDetailEntry
                            .buildSMContentId(SMDBContract.CATEGORY_SERIE,
                                    values.getAsInteger(SMDBContract.SMContentDetailEntry.COLUMN_CONTENT_ID));
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;
        // this makes delete all rows return the number of rows deleted
        if ( null == selection ) selection = "1";
        switch (match) {
            case SMCONTENT:
                rowsDeleted = db.delete(
                        SMDBContract.SMContentEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case GENRE:
                rowsDeleted = db.delete(
                        SMDBContract.GenreEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case SMCONTENT_CONTENT_EPISODE_ID_SEASON:
                rowsDeleted = db.delete(
                        SMDBContract.SMContentEpisodeEntry.TABLE_NAME, selection, selectionArgs);
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Because a null deletes all rows
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(
            Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match) {
            case SMCONTENT:
                rowsUpdated = db.update(SMDBContract.SMContentEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            case GENRE:
                rowsUpdated = db.update(SMDBContract.GenreEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int returnCount = 0;
        switch (match) {
            case GENRE:
                db.beginTransaction();
                returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(SMDBContract.GenreEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                break;
            case SMCONTENT_CONTENT_EPISODE:
                db.beginTransaction();
                returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(SMDBContract.SMContentEpisodeEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                break;
            case SMCONTENT:
                db.beginTransaction();
                returnCount = 0;
                try {
                    for (ContentValues value : values) {

                        /* Si ya existe, no agregar */
                        long _id = value.getAsLong(SMDBContract.SMContentEntry.COLUMN_TMDB_ID);
                        Cursor cursor = db.query(
                                SMDBContract.SMContentEntry.TABLE_NAME,  // Table to Query
                                null, // all columns
                                sContentTMDBIdSelection, // Columns for the "where" clause
                                new String[]{String.valueOf(_id)}, // Values for the "where" clause
                                null, // columns to group by
                                null, // columns to filter by row groups
                                null // sort order
                        );

                        if (cursor.getCount() > 0) // Me salto el insert
                            continue;

                        _id = db.insert(SMDBContract.SMContentEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }

                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                break;
            default:
                returnCount= super.bulkInsert(uri, values);
        }

        return returnCount;
    }

    // You do not need to call this method. This is a method specifically to assist the testing
    // framework in running smoothly. You can read more at:
    // http://developer.android.com/reference/android/content/ContentProvider.html#shutdown()
    @Override
    @TargetApi(11)
    public void shutdown() {
        mOpenHelper.close();
        super.shutdown();
    }
}
