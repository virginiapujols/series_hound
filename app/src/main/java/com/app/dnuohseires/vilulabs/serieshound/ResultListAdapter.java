package com.app.dnuohseires.vilulabs.serieshound;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

/**
 * Created by virginia on 6/4/15.
 */
public class ResultListAdapter extends CursorAdapter {

    private final int VIEW_TYPE_IMAGE = 0;
    private final int VIEW_TYPE_TWO_PAIN = 1;
    private final int VIEW_TYPE_GRID = 2;

    private boolean mIsTwoPain;
    private boolean mIsFavoriteView;
    private Context mContext;
    private Cursor mCursor;

    public ResultListAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        mContext = context;
        mCursor = c;
    }

    public boolean isTwoPain() {
        return mIsTwoPain;
    }

    public void setIsTwoPain(boolean mIsTwoPain) {
        this.mIsTwoPain = mIsTwoPain;
    }

    public boolean isFavoriteView() {
        return mIsFavoriteView;
    }

    public void setIsFavoriteView(boolean mIsFavoriteView) {
        this.mIsFavoriteView = mIsFavoriteView;
    }

    public Cursor getCursor() {
        return mCursor;
    }

    public void setCursor(Cursor mCursor) {
        this.mCursor = mCursor;
    }

    public static class ViewHolder {
        public final ImageView iconView;
        public final TextView dateView;
        public final TextView titleView;
        public final TextView voteAvgView;

        public ViewHolder(View view) {
            iconView = (ImageView) view.findViewById(R.id.list_item_icon);
            dateView = (TextView) view.findViewById(R.id.list_item_air_date_textView);
            titleView = (TextView) view.findViewById(R.id.list_item_title_textView);
            voteAvgView = (TextView) view.findViewById(R.id.list_item_vote_avg_textView);
        }
    }

    @Override
    public int getViewTypeCount() {
        return 3;
    }

    @Override
    public int getItemViewType(int position) {
        if (this.isTwoPain())
            return VIEW_TYPE_TWO_PAIN;
        else if (this.isFavoriteView())
            return VIEW_TYPE_GRID;
        else
            return VIEW_TYPE_IMAGE;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        int viewType = getItemViewType(cursor.getPosition());
        int layoutId;
        if (viewType == VIEW_TYPE_GRID)
            layoutId = R.layout.grid_item;
        else if (viewType == VIEW_TYPE_TWO_PAIN)
            layoutId = R.layout.list_item_two_pain;
        else
            layoutId = R.layout.list_item;

        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        ViewHolder viewHolder = (ViewHolder) view.getTag();
        if (viewHolder.iconView != null) {
            Picasso.with(context)
                    .load(cursor.getString(ResultListFragment.COL_POSTER))
                    .fit()
                    .centerInside()
                    .into(viewHolder.iconView);
        }

        if (viewHolder.titleView != null)
            viewHolder.titleView.setText(cursor.getString(ResultListFragment.COL_TITLE));
        if (viewHolder.dateView != null)
            viewHolder.dateView.setText(cursor.getString(ResultListFragment.COL_AIR_DATE));
        if (viewHolder.voteAvgView != null)
            viewHolder.voteAvgView.setText(cursor.getString(ResultListFragment.COL_POPULARITY));
    }
}
