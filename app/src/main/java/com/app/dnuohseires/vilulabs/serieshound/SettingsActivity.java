package com.app.dnuohseires.vilulabs.serieshound;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.app.dnuohseires.vilulabs.serieshound.data.SMDBContract;

public class SettingsActivity extends PreferenceActivity
        implements Preference.OnPreferenceChangeListener {

    public static final String[] CONTENT_COLUMNS = {
            SMDBContract.SMContentEntry.COLUMN_TMDB_ID,
            SMDBContract.SMContentEntry.COLUMN_TITLE,
            SMDBContract.SMContentEpisodeEntry.COLUMN_SEASON_ID,
            SMDBContract.SMContentEpisodeEntry.COLUMN_EPISODE_ID,
            SMDBContract.SMContentEpisodeEntry.TABLE_NAME + "." + SMDBContract.SMContentEpisodeEntry.COLUMN_AIR_DATE,
    };

    // These indices are tied to SMCONTENT_COLUMNS.  If SMCONTENT_COLUMNS changes, these
    // must change.
    static final int COL_TMDB_ID            = 0;
    static final int COL_TITLE              = 1;
    static final int COL_SEASON_ID          = 2;
    static final int COL_EPISODE_ID         = 3;
    static final int COL_EPISODE_AIR_DATE   = 4;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Add 'general' preferences, defined in the XML file
        addPreferencesFromResource(R.xml.pref_general);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        Preference preference = findPreference(this.getString(R.string.pref_enable_notifications_key));
        preference.setOnPreferenceChangeListener(this);

    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String stringValue = newValue.toString();
        preference.setSummary(stringValue);
        Log.d("Setting: Notifications", Boolean.valueOf(stringValue) ? "Enabled" : "Disabled");

        Context context = this;

        String favoritesIds = Utility.getFavoritesIds(context);
        if (favoritesIds.isEmpty() || favoritesIds.equals("-1"))
            return true;

        long todayMillis = Utility.getTodayInMillis();

        for (String tmdbId : favoritesIds.split(",")) {
            Uri uri = SMDBContract.SMContentEpisodeEntry.buildSMContentId(Long.valueOf(tmdbId), 0);
            Cursor episodeCursor = context.getContentResolver().query(
                    uri,
                    CONTENT_COLUMNS,
                    null,
                    null,
                    null);

            //For-each episode, schedule/cancel notification
            while (episodeCursor.moveToNext()) {
                String title = episodeCursor.getString(COL_TITLE);
                long episodeAirDateMillis = episodeCursor.getLong(COL_EPISODE_AIR_DATE);
                int seasonNumber = episodeCursor.getInt(COL_SEASON_ID);
                int episodeNumber = episodeCursor.getInt(COL_EPISODE_ID);

                long millisecondsFromNow = episodeAirDateMillis - todayMillis;
                if (millisecondsFromNow >= 0) { //Time to fire notification from Now
                    // Define the text of the notification.
                    String contentText = String.format(context.getString(R.string.format_notification),
                            seasonNumber,
                            episodeNumber);

                    NotificationCompat.Builder notification =
                            new NotificationCompat.Builder(context)
                                    .setColor(context.getResources().getColor(R.color.series_hound_pink))
                                    .setSmallIcon(R.mipmap.ic_launcher)
                                    .setContentTitle(title)
                                    .setContentText(contentText);

                    if (Boolean.valueOf(stringValue)) {
                        Utility.scheduleNotification(context, notification.build(), millisecondsFromNow, Integer.valueOf(tmdbId));
                    } else {
                        Utility.cancelNotification(context, notification.build(), Integer.valueOf(tmdbId));
                    }
                }
            }

            episodeCursor.close();
        }
        return true;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public Intent getParentActivityIntent() {
        return super.getParentActivityIntent().addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    }

}