package com.app.dnuohseires.vilulabs.serieshound;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.dnuohseires.vilulabs.serieshound.data.SMDBContract;
import com.app.dnuohseires.vilulabs.serieshound.service.RequestType;
import com.app.dnuohseires.vilulabs.serieshound.service.SMService;
import com.squareup.picasso.Picasso;

public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    static final String DETAIL_URI = "URI";
    private static final String SHARE_HASHTAG = " #SeriesHound";
    private final String LOG_TAG = DetailFragment.class.getSimpleName();
    private static final int DETAIL_LOADER = 0;
    public String tmdb_id;
    public String title;
    public String season_number;

    private ShareActionProvider mShareActionProvider;
    private Uri mUri;
    private String sharedText;

    // Specify the columns we need.
    public static final String[] SMCONTENT_COLUMNS = {
            SMDBContract.SMContentDetailEntry.TABLE_NAME + "." + SMDBContract.SMContentDetailEntry._ID,
            SMDBContract.SMContentEntry.TABLE_NAME + "." +SMDBContract.SMContentEntry.COLUMN_AIR_DATE,
            SMDBContract.SMContentEntry.TABLE_NAME + "." +SMDBContract.SMContentEntry.COLUMN_TITLE,
            SMDBContract.SMContentEntry.TABLE_NAME + "." +SMDBContract.SMContentEntry.COLUMN_POSTER,
            SMDBContract.SMContentEntry.TABLE_NAME + "." +SMDBContract.SMContentEntry.COLUMN_BACKDROP,
            SMDBContract.SMContentEntry.TABLE_NAME + "." +SMDBContract.SMContentEntry.COLUMN_POPULARITY,
            SMDBContract.SMContentDetailEntry.COLUMN_NETWORK,
            SMDBContract.SMContentDetailEntry.COLUMN_SUMMARY,
            SMDBContract.SMContentDetailEntry.COLUMN_STATUS,
            SMDBContract.SMContentDetailEntry.COLUMN_SEASON_COUNT,
            SMDBContract.SMContentDetailEntry.COLUMN_EPISODES_COUNT,
            SMDBContract.SMContentDetailEntry.COLUMN_CONTENT_ID,
    };

    // These indices are tied to SMCONTENT_COLUMNS.  If SMCONTENT_COLUMNS changes, these
    // must change.
    static final int COL_ID = 0;
    static final int COL_AIR_DATE   = 1;
    static final int COL_TITLE      = 2;
    static final int COL_POSTER     = 3;
    static final int COL_BACKDROP   = 4;
    static final int COL_POPULARITY = 5;
    static final int COL_NETWORK    = 6;
    static final int COL_SUMMARY    = 7;
    static final int COL_STATUS     = 8;
    static final int COL_SEASONS    = 9;
    static final int COL_EPISODES   = 10;
    static final int COL_CONTENT_ID = 11;

    public static final String[] SMCONTENT_EPISODE_COLUMNS = {
            SMDBContract.SMContentEpisodeEntry.COLUMN_EPISODE_ID,
            SMDBContract.SMContentEpisodeEntry.TABLE_NAME + "." + SMDBContract.SMContentEpisodeEntry.COLUMN_AIR_DATE,
    };

    static final int COL_EPISODE_ID = 0;
    static final int COL_EPISODE_AIR_DATE   = 1;

    private ImageView mIconView;
    private ImageView mIconWideView;
    private TextView mTitleView;
    private TextView mNetworkView;
    private TextView mSeasonsView;
    private TextView mEpisodesView;
    private TextView mVotesView;
    private TextView mSummaryView;


    public DetailFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {


        Bundle arguments = getArguments();
        if (arguments != null) {
            mUri = arguments.getParcelable(DetailFragment.DETAIL_URI);
        }

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        //find views
        mIconView = (ImageView) rootView.findViewById(R.id.detail_item_icon);
        mIconWideView = (ImageView) rootView.findViewById(R.id.detail_item_wide_icon);
        mTitleView = (TextView) rootView.findViewById(R.id.detail_item_title_textView);
        mNetworkView = (TextView) rootView.findViewById(R.id.detail_item_network_textView);
        mSeasonsView = (TextView) rootView.findViewById(R.id.detail_item_seasons_textView);
        mEpisodesView = (TextView) rootView.findViewById(R.id.detail_item_episodes_textView);
        mVotesView = (TextView) rootView.findViewById(R.id.detail_item_votes_textView);
        mSummaryView = (TextView) rootView.findViewById(R.id.detail_item_summary_textView);

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_detailfragment, menu);

        // Retrieve the share menu item
        MenuItem menuItem = menu.findItem(R.id.action_share);

        // Get the provider and hold onto it to set/change the share intent.
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        // If onLoadFinished happens before this, we can go ahead and set the share intent now.
        if (sharedText != null) {
            mShareActionProvider.setShareIntent(createShareForecastIntent());
        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem menuItem = menu.findItem(R.id.action_fav);
        if (tmdb_id != null && Utility.isFavorite(getActivity(), tmdb_id)) {
            menuItem.setIcon(R.drawable.ic_action_favorite_active);
        }

        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_fav) {
            if (Utility.isFavorite(getActivity(), tmdb_id)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(R.string.delete_from_favorites)
                        .setMessage(R.string.delete_from_favorites_confirm_msj)
                        .setPositiveButton(R.string.YES, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Utility.removeFromFavorites(getActivity(), tmdb_id);
                                getActivity().supportInvalidateOptionsMenu();
                                scheduleEpisodesAction(Integer.valueOf(tmdb_id), Integer.valueOf(season_number), 1);

                            }
                        })
                        .setNegativeButton(R.string.NO, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) { }
                        });

                AlertDialog dialog = builder.create();
                dialog.show();
            } else {
                Utility.addToFavorites(getActivity(), tmdb_id);
                getActivity().supportInvalidateOptionsMenu();
                scheduleEpisodesAction(Integer.valueOf(tmdb_id), Integer.valueOf(season_number), 0);
            }
        }
        return super.onOptionsItemSelected(item);
    }

    void onItemChanged(long id) {
        Uri uri = mUri;
        Log.d(LOG_TAG, "uri: " + Long.toString(id));
        if (null != uri) {
            Uri updatedUri = SMDBContract.SMContentDetailEntry
                    .buildSMContentId(SMDBContract.CATEGORY_SERIE, id);
            mUri = updatedUri;
            getLoaderManager().restartLoader(DETAIL_LOADER, null, this);
        }
    }

    private void updateLastSeasonEpisodes (int tmdbId, int seasonId) {
        Uri uri = SMDBContract.SMContentEpisodeEntry.buildSMContentId(tmdbId, seasonId);
        Cursor cursor = getActivity().getContentResolver().query(
                uri,
                null,
                null,
                null,
                null);

        if (!cursor.moveToFirst()) { // All episodes are old
            //Delete current list and update from API
            getActivity().getContentResolver().delete(
                    uri,
                    SMDBContract.SMContentEpisodeEntry.COLUMN_CONTENT_DETAIL_ID + " = ? AND "
                            + SMDBContract.SMContentEpisodeEntry.COLUMN_SEASON_ID + " = ? ",
                    new String[] {tmdb_id, season_number}
            );

            Intent intent = new Intent(getActivity(), SMService.class);
            intent.putExtra(SMService.REQUEST_TYPE, RequestType.TV_SERIES_DETAIL_SEASON.ordinal());
            intent.putExtra(SMService.TMDB_ID, Integer.valueOf(tmdbId));
            intent.putExtra(SMService.SEASON_NUMBER, Integer.valueOf(seasonId));
            getActivity().startService(intent);
        }
    }

    // action: 0-schedule, 1-cancel
    private void scheduleEpisodesAction(int tmdbId, int seasonNumber, int action) {

        if (Utility.isFavoriteNotificationActive(getActivity())) {
            long todayMillis = Utility.getTodayInMillis();

            //List of episodes
            Uri uri = SMDBContract.SMContentEpisodeEntry.buildSMContentId(tmdbId, seasonNumber);
            Cursor episodeCursor = getActivity().getContentResolver().query(
                    uri,
                    SMCONTENT_EPISODE_COLUMNS,
                    null,
                    null,
                    null);

            //For-each episode, schedule/cancel notification
            while (episodeCursor.moveToNext()) {

                long episodeAirDateMillis = episodeCursor.getLong(COL_EPISODE_AIR_DATE);
                int episodeNumber = episodeCursor.getInt(COL_EPISODE_ID);

                long millisecondsFromNow = episodeAirDateMillis - todayMillis;
                if (millisecondsFromNow >= 0) { //Time to fire notification from Now
                    // Define the text of the notification.
                    String contentText = String.format(getActivity().getString(R.string.format_notification),
                            seasonNumber,
                            episodeNumber);

                    NotificationCompat.Builder notification =
                            new NotificationCompat.Builder(getActivity())
                                    .setColor(getActivity().getResources().getColor(R.color.series_hound_pink))
                                    .setSmallIcon(R.mipmap.ic_launcher)
                                    .setContentTitle(title)
                                    .setContentText(contentText);

                    if (action == 0) {
                        Utility.scheduleNotification(getActivity(), notification.build(), millisecondsFromNow, tmdbId);
                    } else {
                        Utility.cancelNotification(getActivity(), notification.build(), tmdbId);
                    }
                }
            }

            episodeCursor.close();
        }
    }

    private Intent createShareForecastIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, sharedText + "\n" + SHARE_HASHTAG);
        return shareIntent;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if ( null != mUri ) {
            // Now create and return a CursorLoader that will take care of
            // creating a Cursor for the data being displayed.
            return new CursorLoader(
                    getActivity(),
                    mUri,
                    SMCONTENT_COLUMNS,
                    null,
                    null,
                    null
            );
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        Log.d(LOG_TAG, "onLoadFinished");
        if (cursor != null && cursor.moveToFirst()) {

            if (mIconView != null) {
                Picasso.with(getActivity())
                        .load(cursor.getString(COL_POSTER))
                        .fit()
                        .centerInside()
                        .into(mIconView);
            }
            if (mIconWideView != null) {
                if (mIconWideView.getWidth() != 0 || mIconWideView.getHeight() != 0) {
                    //Log.d("Image size", "width:" + mIconWideView.getWidth() + " height:" + mIconWideView.getHeight());
                    Picasso.with(getActivity())
                            .load(cursor.getString(COL_BACKDROP))
                            .resize(mIconWideView.getWidth(), mIconWideView.getHeight())
                            .centerCrop()
                            .into(mIconWideView);
                } else {
                    Picasso.with(getActivity())
                            .load(cursor.getString(COL_BACKDROP))
                            .fit()
                            .centerInside()
                            .into(mIconWideView);
                }
            }

            tmdb_id = cursor.getString(COL_CONTENT_ID);
            title = cursor.getString(COL_TITLE);
            season_number = cursor.getString(COL_SEASONS);

            mTitleView.setText(title);
            mNetworkView.setText(cursor.getString(COL_NETWORK));
            mSeasonsView.setText(season_number + " " + getActivity().getString(R.string.seasons));
            mEpisodesView.setText(cursor.getString(COL_EPISODES) + " " + getActivity().getString(R.string.episodes));
            mVotesView.setText(cursor.getString(COL_POPULARITY));
            mSummaryView.setText(cursor.getString(COL_SUMMARY));

            // for the share intent
            sharedText = String.format(getActivity().getString(R.string.format_share),
                    cursor.getString(COL_TITLE),
                    season_number,
                    cursor.getString(COL_POPULARITY));

            // If onCreateOptionsMenu has already happened, we need to update the share intent now.
            if (mShareActionProvider != null) {
                mShareActionProvider.setShareIntent(createShareForecastIntent());
            }

            getActivity().supportInvalidateOptionsMenu();
            updateLastSeasonEpisodes(Integer.valueOf(tmdb_id), Integer.valueOf(season_number));

        } else {
            String contentId = SMDBContract.SMContentEntry.getContentIdFromUri(mUri);
            Intent intent = new Intent(getActivity(), SMService.class);
            intent.putExtra(SMService.REQUEST_TYPE, RequestType.TV_SERIES_DETAIL.ordinal());
            intent.putExtra(SMService.TMDB_ID, Integer.valueOf(contentId));
            getActivity().startService(intent);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) { }
}
