package com.app.dnuohseires.vilulabs.serieshound;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.app.dnuohseires.vilulabs.serieshound.data.SMDBContract;
import com.app.dnuohseires.vilulabs.serieshound.service.RequestType;
import com.app.dnuohseires.vilulabs.serieshound.service.SMService;

/**
 * Created by virginia on 6/4/15.
 */
public class ResultListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    public static final String LOG_TAG = ResultListFragment.class.getSimpleName();
    private static final int LOADER = 0;
    private static final String SELECTED_KEY = "selected_position";
    private ResultListAdapter mAdapter;
    private ListView mListView;
    private int mPosition = ListView.INVALID_POSITION;
    private boolean mIsTwoPain;

    // Specify the columns we need.
    public static final String[] SMCONTENT_COLUMNS = {
            SMDBContract.SMContentEntry.TABLE_NAME + "." + SMDBContract.SMContentEntry._ID,
            SMDBContract.SMContentEntry.COLUMN_AIR_DATE,
            SMDBContract.SMContentEntry.COLUMN_TMDB_ID,
            SMDBContract.SMContentEntry.COLUMN_TITLE,
            SMDBContract.SMContentEntry.COLUMN_POSTER,
            SMDBContract.SMContentEntry.COLUMN_POPULARITY,
    };

    // These indices are tied to SMCONTENT_COLUMNS.  If SMCONTENT_COLUMNS changes, these
    // must change.
    static final int COL_CONTENT_ID = 0;
    static final int COL_AIR_DATE   = 1;
    static final int COL_TMDB_ID    = 2;
    static final int COL_TITLE      = 3;
    static final int COL_POSTER     = 4;
    static final int COL_POPULARITY = 5;


    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        public void onItemSelected(Uri uri);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        Log.i(LOG_TAG, "onCreateView");

        // The Adapter will take data from a source and
        // use it to populate the ListView it's attached to.
        mAdapter = new ResultListAdapter(getActivity(), null, 0);
        mAdapter.setIsTwoPain(mIsTwoPain);

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // Get a reference to the ListView, and attach this adapter to it.
        mListView = (ListView) rootView.findViewById(R.id.listview_sm_content);
        mListView.setAdapter(mAdapter);
        // We'll call our MainActivity
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                // CursorAdapter returns a cursor at the correct position for getItem(), or null
                // if it cannot seek to that position.
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                if (cursor != null) {
                    ((Callback) getActivity())
                            .onItemSelected(SMDBContract.
                                    SMContentDetailEntry
                                    .buildSMContentId(
                                            SMDBContract.CATEGORY_SERIE,
                                            cursor.getLong(COL_TMDB_ID)));
                }
                mPosition = position;
            }
        });

        // If there's instance state, mine it for useful information.
        // The end-goal here is that the user never knows that turning their device sideways
        // does crazy lifecycle related things.  It should feel like some stuff stretched out,
        // or magically appeared to take advantage of room, but data or place in the app was never
        // actually *lost*.
        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)) {
            // The listview probably hasn't even been populated yet.  Actually perform the
            // swapout in onLoadFinished.
            mPosition = savedInstanceState.getInt(SELECTED_KEY);
        }

        return rootView;
    }

    // since we read the location when we create the loader, all we need to do is restart things
    void onResultChanged() {
        updateSMContent();
        getLoaderManager().restartLoader(LOADER, null, this);
    }

    public boolean isTwoPain() {
        return mIsTwoPain;
    }

    public void setIsTwoPain(boolean mIsTwoPain) {

        this.mIsTwoPain = mIsTwoPain;
        mAdapter.setIsTwoPain(mIsTwoPain);
    }

    private void updateSMContent() {

        Intent intent = new Intent(getActivity(), SMService.class);
        intent.putExtra(SMService.REQUEST_TYPE, RequestType.POPULAR_TV.ordinal());

        getActivity().startService(intent);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // When tablets rotate, the currently selected list item needs to be saved.
        // When no item is selected, mPosition will be set to Listview.INVALID_POSITION,
        if (mPosition != ListView.INVALID_POSITION) {
            outState.putInt(SELECTED_KEY, mPosition);
        }
        super.onSaveInstanceState(outState);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // This is called when a new Loader needs to be created.  This
        // fragment only uses one loader, so we don't care about checking the id.

        Uri resultSearchURI = SMDBContract.SMContentEntry.buildSMContentCategory(SMDBContract.CATEGORY_SERIE);

        return new CursorLoader(getActivity(),
                resultSearchURI,
                SMCONTENT_COLUMNS,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
        if (mPosition != ListView.INVALID_POSITION) {
            // If we don't need to restart the loader,
            // and there's a desired position to restore to, do so now.
            mListView.smoothScrollToPosition(mPosition);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

}
