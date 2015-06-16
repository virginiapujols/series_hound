package com.app.dnuohseires.vilulabs.serieshound;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by iosdeveloper on 6/9/15.
 */
public class Utility {

    public static final String PREFS_FAVORITES = "MyFavorites";

    public static boolean isFavoriteNotificationActive (Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String displayNotificationsKey = context.getString(R.string.pref_enable_notifications_key);
        boolean displayNotifications = prefs.getBoolean(displayNotificationsKey,
                Boolean.parseBoolean(context.getString(R.string.pref_enable_notifications_default)));
        return displayNotifications;
    }

    public static String getFavoritesIds(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        String favoritesIds = sharedPref.getString(PREFS_FAVORITES, "-1");
        return favoritesIds;
    }

    public static boolean isFavorite(Context context, String id) {
        if (id == null)
            return false;

        String favoriteIds = Utility.getFavoritesIds(context);

        if (favoriteIds.contains(id))
            return true;

        return false;
    }

    public static void addToFavorites(Context context, String id) {
        if (id == null)
            return;

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        String favoriteIds = Utility.getFavoritesIds(context);
        if (favoriteIds.equals("-1") || favoriteIds.isEmpty()) {
            favoriteIds = id;
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(PREFS_FAVORITES, id);
            editor.apply();
        } else if (!favoriteIds.contains(id)) {
            favoriteIds = favoriteIds + "," + id;
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(PREFS_FAVORITES, favoriteIds);
            editor.apply();
        }

        Log.d("Favorites: ", favoriteIds);
        Toast.makeText(context, context.getString(R.string.added_to_favorites), Toast.LENGTH_LONG).show();
    }

    public static void removeFromFavorites(Context context, String id) {
        if (id == null)
            return;

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        String favoriteIds = Utility.getFavoritesIds(context);
        favoriteIds = favoriteIds.replace(id + ",", "");
        favoriteIds = favoriteIds.replace(id, "");

        if (favoriteIds.isEmpty())
            favoriteIds = "-1";

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(PREFS_FAVORITES, favoriteIds);
        editor.apply();
        Log.d("Favorites: ", favoriteIds);
        Toast.makeText(context, context.getString(R.string.delete_from_favorites), Toast.LENGTH_LONG).show();
    }

    public static void scheduleNotification(Context context, Notification notification, long notificationDateMillis, int id) {
        Intent notificationIntent = new Intent(context, NotificationReceiver.class);
        notificationIntent.putExtra(NotificationReceiver.NOTIFICATION_ID, id);
        notificationIntent.putExtra(NotificationReceiver.NOTIFICATION, notification);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, notificationDateMillis, pendingIntent);
        Log.d("NOTIFICATION", "Scheduled Notification:" + id + " millis:" + notificationDateMillis);
    }

    public static void cancelNotification(Context context, Notification notification, int id) {

        Intent notificationIntent = new Intent(context, NotificationReceiver.class);
        notificationIntent.putExtra(NotificationReceiver.NOTIFICATION_ID, id);
        notificationIntent.putExtra(NotificationReceiver.NOTIFICATION, notification);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
        Log.d("NOTIFICATION", "Cancelled Notification:" + id);
    }

    public static class NotificationReceiver extends BroadcastReceiver {

        public static String NOTIFICATION_ID = "notification_id";
        public static String NOTIFICATION = "notification";

        public void onReceive(Context context, Intent intent) {

            NotificationManager notificationManager =
                    (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);

            Notification notification = intent.getParcelableExtra(NOTIFICATION);
            int id = intent.getIntExtra(NOTIFICATION_ID, 0);
            notificationManager.notify(id, notification);

        }
    }


    public static long getTodayInMillis () {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        Date today = new Date();
        String todayFormatted = simpleDateFormat.format(today);
        long todayMillis = 0;
        try {
            todayMillis = simpleDateFormat.parse(todayFormatted).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return todayMillis;
    }

}