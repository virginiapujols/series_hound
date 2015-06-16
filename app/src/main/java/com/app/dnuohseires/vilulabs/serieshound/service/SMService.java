package com.app.dnuohseires.vilulabs.serieshound.service;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.app.dnuohseires.vilulabs.serieshound.R;
import com.app.dnuohseires.vilulabs.serieshound.Utility;
import com.app.dnuohseires.vilulabs.serieshound.data.SMDBContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Vector;

/**
 * Created by virginia on 5/31/15.
 */
public class SMService extends IntentService {
    private final String LOG_TAG = SMService.class.getSimpleName();

    private Intent mIntent;
    private RequestType mType;

    public static final String REQUEST_TYPE = "request_type";
    public static final String TMDB_ID = "id";
    public static final String SEASON_NUMBER = "season_number";
    public static final String EPISODE_NUMBER = "episode_number";
    final String API_KEY_PARAM = "api_key";
    final String API_KEY = "d439e9799629fa2d2c55597377b5ecaa";
    final String BASE_PATH_IMG = "http://image.tmdb.org/t/p/original/";
    final String TMDB_LIST = "results";
    final String TMDB_POSTER = "poster_path";
    final String TMDB_BACKDROP = "backdrop_path";
    final String TMDB_NAME = "name";
    final String TMDB_FIRST_AIR_DATE = "first_air_date";
    final String TMDB_AIR_DATE = "air_date";
    final String TMDB_NETWORK = "networks";
    final String TMDB_STATUS = "status";
    final String TMDB_SUMMARY = "overview";
    final String TMDB_EPISODE_COUNT = "number_of_episodes";
    final String TMDB_SEASON_COUNT = "number_of_seasons";
    final String TMDB_VOTE_AV = "vote_average";
    final String TMDB_EPISODES = "episodes";


    /*
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public SMService() {
        super("SeriesHounds");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        mIntent = intent;

        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String jsonStr;

        try {
            // Construct the URL for the OpenWeatherMap query
            // http://api.themoviedb.org/3/tv/popular?api_key=#
            Uri builtUri = getUriByRequestType(intent);

            URL url = new URL(builtUri.toString());

            // Create the request, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line).append("\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.
                return;
            }

            jsonStr = buffer.toString();
            getTVDataFromJson(jsonStr);

        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            // If the code didn't successfully get the weather data, there's no point in attempting
            // to parse it.
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }
    }

    private void getTVDataFromJson(String jsonStr) throws JSONException {

        try {
            JSONObject baseJson = new JSONObject(jsonStr);

            if (mType == RequestType.TV_SERIES_DETAIL_SEASON) {
                Vector<ContentValues> cVVector = parseTVEpisodeData(baseJson);
                int countResult = -1;
                if (cVVector.size() > 0) {
                    ContentValues[] cvArray = new ContentValues[cVVector.size()];
                    cVVector.toArray(cvArray);
                    countResult = this.getContentResolver().bulkInsert(
                            SMDBContract.SMContentEpisodeEntry.CONTENT_URI,
                            cvArray);
                }

                Log.i(LOG_TAG, "TV episodes Complete. " + countResult + " Inserted");

            } else {
                if (!baseJson.has(TMDB_LIST)) { //It's a detail
                    ContentValues contentValues = parseTVDataDetail(baseJson);
                    this.getContentResolver().insert(
                            SMDBContract.SMContentDetailEntry
                                    .buildSMContentId(
                                            SMDBContract.CATEGORY_SERIE,
                                            contentValues.getAsInteger(SMDBContract.SMContentDetailEntry.COLUMN_CONTENT_ID)),
                            contentValues);

                } else { // it's a list
                    JSONArray seriesArray = baseJson.getJSONArray(TMDB_LIST);

                    // Insert the new item information into the database
                    Vector<ContentValues> cVVector = new Vector<ContentValues>(seriesArray.length());

                    for (int i = 0; i < seriesArray.length(); i++) {
                        JSONObject jsonItem = seriesArray.getJSONObject(i);
                        ContentValues contentValues = parseTVData(jsonItem);
                        cVVector.add(contentValues);
                    }

                    // add to database
                    int countResult = -1;
                    if (cVVector.size() > 0) {
                        ContentValues[] cvArray = new ContentValues[cVVector.size()];
                        cVVector.toArray(cvArray);
                        countResult = this.getContentResolver().bulkInsert(
                                SMDBContract.SMContentEntry.SMCONTENT_URI,
                                cvArray);
                    }

                    Log.i(LOG_TAG, "TV Series Complete. " + countResult + " Inserted");
                }
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }

    private Uri getUriByRequestType(Intent intent) {
        int requestValue = intent.getIntExtra(REQUEST_TYPE, 0);
        mType = RequestType.values()[requestValue];

        Uri builtUri;
        StringBuilder BASE_URL= new StringBuilder("http://api.themoviedb.org/3/tv");
        switch (mType) {
            case POPULAR_TV:
                builtUri = Uri.parse(BASE_URL.toString()).buildUpon()
                        .appendPath("popular")
                        .appendQueryParameter(API_KEY_PARAM, API_KEY)
                        .build();
                break;
            case TV_SERIES_ON_AIR:
                builtUri = Uri.parse(BASE_URL.toString()).buildUpon()
                        .appendPath("on_the_air")
                        .appendQueryParameter(API_KEY_PARAM, API_KEY)
                        .build();
                break;
            case TV_SERIES_DETAIL:
                builtUri = Uri.parse(BASE_URL.toString()).buildUpon()
                        .appendPath(String.valueOf(intent.getIntExtra(TMDB_ID, 0)))
                        .appendQueryParameter(API_KEY_PARAM, API_KEY)
                        .build();
                break;
            case TV_SERIES_DETAIL_SEASON:
                builtUri = Uri.parse(BASE_URL.toString()).buildUpon()
                       .appendPath(String.valueOf(intent.getIntExtra(TMDB_ID, 0)))
                        .appendPath("season")
                        .appendPath(String.valueOf(intent.getIntExtra(SEASON_NUMBER, 0)))
                        .appendQueryParameter(API_KEY_PARAM, API_KEY)
                        .build();

                break;
            default:
                builtUri = null;
        }

        Log.i(LOG_TAG, "Called API request: " + builtUri.toString());

        return builtUri;
    }

    private ContentValues parseTVData(JSONObject jsonItem) {
        int tvId;
        double rating;

        String name;
        String posterPath;
        String backdropPath;
        String airDate;

        ContentValues contentValues = null;
        try {
            tvId = jsonItem.getInt(TMDB_ID);

            rating = jsonItem.getDouble(TMDB_VOTE_AV);

            name = jsonItem.getString(TMDB_NAME);
            airDate = jsonItem.getString(TMDB_FIRST_AIR_DATE);

            posterPath   = BASE_PATH_IMG + jsonItem.getString(TMDB_POSTER);
            backdropPath = BASE_PATH_IMG + jsonItem.getString(TMDB_BACKDROP);

            contentValues = new ContentValues();
            contentValues.put(SMDBContract.SMContentEntry.COLUMN_CATEGORY_KEY, SMDBContract.CATEGORY_SERIE);
            contentValues.put(SMDBContract.SMContentEntry.COLUMN_AIR_DATE, airDate);
            contentValues.put(SMDBContract.SMContentEntry.COLUMN_TITLE, name);
            contentValues.put(SMDBContract.SMContentEntry.COLUMN_TMDB_ID, tvId);
            contentValues.put(SMDBContract.SMContentEntry.COLUMN_POPULARITY, rating);
            contentValues.put(SMDBContract.SMContentEntry.COLUMN_POSTER, posterPath);
            contentValues.put(SMDBContract.SMContentEntry.COLUMN_BACKDROP, backdropPath);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return contentValues;
    }

    private ContentValues parseTVDataDetail(JSONObject jsonItem) {
        ContentValues contentValues = null;
        try {
            String network = "";
            JSONArray networksArray = jsonItem.getJSONArray(TMDB_NETWORK);
            if (networksArray.length() >= 1) {
                network = networksArray.getJSONObject(0).getString(TMDB_NAME);
            }

            contentValues = new ContentValues();
            contentValues.put(SMDBContract.SMContentDetailEntry.COLUMN_CONTENT_ID, jsonItem.getInt(TMDB_ID));
            contentValues.put(SMDBContract.SMContentDetailEntry.COLUMN_NETWORK, network);
            contentValues.put(SMDBContract.SMContentDetailEntry.COLUMN_STATUS, jsonItem.getString(TMDB_STATUS));
            contentValues.put(SMDBContract.SMContentDetailEntry.COLUMN_SUMMARY, jsonItem.getString(TMDB_SUMMARY));

            if (jsonItem.getString(TMDB_EPISODE_COUNT)!="null")
                contentValues.put(SMDBContract.SMContentDetailEntry.COLUMN_EPISODES_COUNT, jsonItem.getInt(TMDB_EPISODE_COUNT));
            else
                contentValues.put(SMDBContract.SMContentDetailEntry.COLUMN_EPISODES_COUNT, 0);

            if(jsonItem.getString(TMDB_SEASON_COUNT)!="null")
                contentValues.put(SMDBContract.SMContentDetailEntry.COLUMN_SEASON_COUNT, jsonItem.getInt(TMDB_SEASON_COUNT));
            else
                contentValues.put(SMDBContract.SMContentDetailEntry.COLUMN_SEASON_COUNT, 0);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return contentValues;
    }

    private Vector<ContentValues> parseTVEpisodeData(JSONObject jsonItem) {
        int tvId;
        String name;
        long season_number;
        long episode_id;
        String overview;
        String sAirDate;
        Vector<ContentValues> cVVector = null;
        ContentValues contentValues = null;
        try {

            JSONArray episodesArray = jsonItem.getJSONArray(TMDB_EPISODES);

            // Insert the new item information into the database
            cVVector = new Vector<ContentValues>(episodesArray.length());
            tvId = mIntent.getIntExtra(TMDB_ID, 0);

            for(int i = 0; i < episodesArray.length(); i++) {
                JSONObject jsonEpisodeItem = episodesArray.getJSONObject(i);

                if (!jsonEpisodeItem.isNull(TMDB_AIR_DATE)) {
                    name = jsonItem.getString(TMDB_NAME);
                    overview = jsonItem.getString(TMDB_SUMMARY);
                    season_number = jsonEpisodeItem.getInt(SEASON_NUMBER);
                    episode_id = jsonEpisodeItem.getInt(EPISODE_NUMBER);
                    sAirDate= jsonEpisodeItem.getString(TMDB_AIR_DATE);
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
                    Date episodeAirDate = simpleDateFormat.parse(sAirDate);
                    long episodeAirDateMillis = episodeAirDate.getTime();

                    contentValues = new ContentValues();
                    contentValues.put(SMDBContract.SMContentEpisodeEntry.COLUMN_CONTENT_DETAIL_ID, tvId);
                    contentValues.put(SMDBContract.SMContentEpisodeEntry.COLUMN_NAME, name);
                    contentValues.put(SMDBContract.SMContentEpisodeEntry.COLUMN_OVERVIEW, overview);
                    contentValues.put(SMDBContract.SMContentEpisodeEntry.COLUMN_AIR_DATE, episodeAirDateMillis);
                    contentValues.put(SMDBContract.SMContentEpisodeEntry.COLUMN_EPISODE_ID, episode_id);
                    contentValues.put(SMDBContract.SMContentEpisodeEntry.COLUMN_SEASON_ID, season_number);

                    Log.d(LOG_TAG, "Episode date:" + new Date(episodeAirDateMillis));
                    cVVector.add(contentValues);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return cVVector;
    }

    private void notifyNextEpisodes(JSONObject baseJson) {
        int tmdbId = mIntent.getIntExtra(TMDB_ID, 0);

        try {

            JSONArray episodesArray = baseJson.getJSONArray(TMDB_EPISODES);
            for(int i = 0; i < episodesArray.length(); i++) {
                JSONObject jsonEpisodeItem = episodesArray.getJSONObject(i);

                int seasonNumber = jsonEpisodeItem.getInt(SEASON_NUMBER);
                int episodeNumber = jsonEpisodeItem.getInt(EPISODE_NUMBER);
                String episodeAirDateStr = jsonEpisodeItem.getString(TMDB_AIR_DATE);

               SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
                Date episodeAirDate = simpleDateFormat.parse(episodeAirDateStr);
                Date today = new Date();
                String todayFormatted = simpleDateFormat.format(today);

                long episodeAirDateMillis = episodeAirDate.getTime();
                long todayMillis = simpleDateFormat.parse(todayFormatted).getTime();
                long millisecondsFromNow = episodeAirDateMillis - todayMillis;

                if (episodeAirDateMillis >= todayMillis) {
                    Context context = this;
                    Uri uri = SMDBContract.SMContentDetailEntry.buildSMContentId(SMDBContract.CATEGORY_SERIE, tmdbId);

                    Cursor cursor = context.getContentResolver().query(uri,
                            new String[]{SMDBContract.SMContentEntry.COLUMN_TITLE},
                            null,
                            null,
                            null);

                    if (cursor.moveToFirst()) {
                        String name = cursor.getString(0);

                        // Define the text of the notification.
                        String contentText = String.format(context.getString(R.string.format_notification),
                                seasonNumber,
                                episodeNumber);

                        NotificationCompat.Builder mBuilder =
                                new NotificationCompat.Builder(context)
                                        .setColor(context.getResources().getColor(R.color.series_hound_pink))
                                        .setSmallIcon(R.mipmap.ic_launcher)
                                        .setContentTitle(name)
                                        .setContentText(contentText);

                        Utility.scheduleNotification(this, mBuilder.build(), millisecondsFromNow, tmdbId);

                    }
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }


}