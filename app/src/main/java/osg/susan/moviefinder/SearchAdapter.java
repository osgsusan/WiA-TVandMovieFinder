package osg.susan.moviefinder;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import osg.susan.moviefinder.data.SearchDataContract;

/**
 * Created by susanosgood on 4/3/15.
 */
public class SearchAdapter extends CursorAdapter {

    /**
     * Cache of the children views for a search list item.
     */
    public static class ViewHolder {
        //public final TextView imdbIdView;
        public final TextView titleView;
        public final TextView yearView;
        public final TextView typeView;

        public ViewHolder(View view) {
            //imdbIdView = (TextView) view.findViewById(R.id.list_item_imdbId_textview);
            titleView = (TextView) view.findViewById(R.id.list_item_title_textview);
            yearView = (TextView) view.findViewById(R.id.list_item_year_textview);
            typeView = (TextView) view.findViewById(R.id.list_item_type_textview);
        }
    }

    public SearchAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    /*
        Views are reused as needed.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        int layoutId = -1;
        layoutId = R.layout.search_list_item;

        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }

    /*
        Fill-in the views with the contents of the cursor.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        ViewHolder viewHolder = (ViewHolder) view.getTag();

        // Read imdbId from cursor
        String imdbId = cursor.getString(SearchDataContract.SearchEntry.COLUMN_INDEX_IMDB_ID);

        //viewHolder.imdbIdView.setText(imdbId);

        // Read title from cursor
        String title = cursor.getString(SearchDataContract.SearchEntry.COLUMN_INDEX_TITLE);
        viewHolder.titleView.setText(title);

        // Read year from cursor
        String year = cursor.getString(SearchDataContract.SearchEntry.COLUMN_INDEX_YEAR);
        viewHolder.yearView.setText(year);

        // Read type from cursor
        String type = cursor.getString(SearchDataContract.SearchEntry.COLUMN_INDEX_TYPE);
        viewHolder.typeView.setText(type);

    }
}
