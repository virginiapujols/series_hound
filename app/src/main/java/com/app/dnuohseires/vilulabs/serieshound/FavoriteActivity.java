package com.app.dnuohseires.vilulabs.serieshound;

import android.annotation.TargetApi;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;


public class FavoriteActivity extends ActionBarActivity implements FavoriteListFragment.Callback {
    private final String LOG_TAG = FavoriteActivity.class.getSimpleName();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);
//        if (savedInstanceState == null) {
//            getSupportFragmentManager().beginTransaction()
//                    .add(R.id.container, new FavoriteListFragment())
//                    .commit();
//        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        FavoriteListFragment fragment = (FavoriteListFragment)getSupportFragmentManager().findFragmentById(R.id.container);
        if ( null != fragment ) {
            fragment.onRestartLoader();
        }
    }

    @Override
    public void onItemSelected(Uri contentUri) {
        Intent intent = new Intent(this, DetailActivity.class).setData(contentUri);
        startActivity(intent);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public Intent getParentActivityIntent() {
        return super.getParentActivityIntent().addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    }

}
