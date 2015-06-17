package com.app.dnuohseires.vilulabs.serieshound.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Virginia on 5/30/2015.
 */
public class SMDBContract {
    public static final String CATEGORY_MOVIE = "movie";
    public static final String CATEGORY_SERIE = "tv";

    // The "Content authority" is a name for the entire content provider, similar to the
    // relationship between a domain name and its website.  A convenient string to use for the
    // content authority is the package name for the app, which is guaranteed to be unique on the
    // device.
    public static final String CONTENT_AUTHORITY = "com.app.dnuohseires.vilulabs.serieshound";

    // Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
    // the content provider.
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    // Possible paths (appended to base content URI for possible URI's)
    // For instance, content://com.example.android.sunshine.app/weather/ is a valid path for
    // looking at weather data. content://com.example.android.sunshine.app/givemeroot/ will fail,
    // as the ContentProvider hasn't been given any information on what to do with "givemeroot".
    // At least, let's hope not.  Don't be that dev, reader.  Don't be that dev.
    public static final String PATH_GENRE = "genre";
    public static final String PATH_SMDBCONTENT = "smdb_content";
    public static final String PATH_SMDBCONTENT_DETAIL = "smdb_content_detail";
    public static final String PATH_SMDBCONTENT_EPISODE = "smdb_content_episode";

    /* Inner class that defines the table contents of the content (series & movies or SM) table */
    public static final class SMContentEntry implements BaseColumns {

        public static final Uri SMCONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_SMDBCONTENT).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_SMDBCONTENT;

        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_SMDBCONTENT;


        public static final String TABLE_NAME = "smdb_content";

        public static final String COLUMN_CATEGORY_KEY = "category_id";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_AIR_DATE = "air_date";
        public static final String COLUMN_TMDB_ID = "tmdb_id";
        public static final String COLUMN_POSTER = "poster";
        public static final String COLUMN_BACKDROP = "backdrop";
        public static final String COLUMN_POPULARITY = "popularity";

        public static Uri buildContentUri(long id) {
            return ContentUris.withAppendedId(SMCONTENT_URI, id);
        }

        public static Uri buildSMContentCategory(String category) {
            return SMCONTENT_URI.buildUpon().appendPath(category).build();
        }

        public static Uri buildSMContentMultiple(String category,String multi_id) {
            return SMCONTENT_URI.buildUpon().appendPath(category).appendPath(multi_id).build();
        }

        public static String getCategoryFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }
        public static String getContentIdFromUri(Uri uri) {
            return uri.getPathSegments().get(2);
        }

    }

    /* Inner class that defines the table contents of the content detail table */
    public static final class SMContentDetailEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_SMDBCONTENT_DETAIL).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_SMDBCONTENT_DETAIL;

        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_SMDBCONTENT_DETAIL;


        public static final String TABLE_NAME               = "smdb_content_detail";
        public static final String COLUMN_CONTENT_ID        = "content_id";
        public static final String COLUMN_SUMMARY           = "summary";
        public static final String COLUMN_SEASON_COUNT      = "season_count";
        public static final String COLUMN_EPISODES_COUNT    = "episode_count";
        public static final String COLUMN_NETWORK           = "network";
        public static final String COLUMN_STATUS            = "status";


        public static Uri buildSMContentId(String category,long id) {
            return CONTENT_URI.buildUpon().appendPath(category).appendPath(Long.toString(id)).build();
        }

    }

    /* Inner class that defines the table contents of the content episodes table */
    public static final class SMContentEpisodeEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_SMDBCONTENT_EPISODE).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_SMDBCONTENT_EPISODE;

        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_SMDBCONTENT_EPISODE;


        public static final String TABLE_NAME               = "smdb_content_episode";
        public static final String COLUMN_CONTENT_DETAIL_ID = "content_detail_id";
        public static final String COLUMN_SEASON_ID         = "season_id";
        public static final String COLUMN_EPISODE_ID        = "episode_id";
        public static final String COLUMN_NAME              = "name";
        public static final String COLUMN_OVERVIEW          = "overview";
        public static final String COLUMN_AIR_DATE          = "air_date";

        public static Uri buildContentUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildSMContentId(long detail_id ,long episode_id) {
            return CONTENT_URI.buildUpon().appendPath(Long.toString(detail_id)).appendPath(Long.toString(episode_id)).build();
        }
        public static String getContentIdFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }
        public static String getSeasonIdFromUri(Uri uri) {
            return uri.getPathSegments().get(2);
        }

    }

}
