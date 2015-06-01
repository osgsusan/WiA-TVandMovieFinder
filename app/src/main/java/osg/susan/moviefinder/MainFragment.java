package osg.susan.moviefinder;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import osg.susan.moviefinder.data.SearchDataContract;
import osg.susan.moviefinder.service.SearchDataService;

/**
 * Created by susanosgood on 4/8/15.
 */
public class MainFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String SELECTED_KEY = "selected_position";
    private static final String INTERNET_NOT_CONNECTED = "Internet connection is not available";
    private static final int SEARCH_LOADER = 0;

    // For the list view, show only a small subset of the stored data.
    // Specify the columns we need.
    private static final String[] SEARCH_COLUMNS = {
            SearchDataContract.SearchEntry.TABLE_NAME + "." + SearchDataContract.SearchEntry._ID,
            SearchDataContract.SearchEntry.COLUMN_IMDB_ID,
            SearchDataContract.SearchEntry.COLUMN_TITLE,
            SearchDataContract.SearchEntry.COLUMN_YEAR,
            SearchDataContract.SearchEntry.COLUMN_TYPE
    };

    private SearchAdapter mSearchAdapter;
    private ListView mListView;
    private int mPosition = ListView.INVALID_POSITION;


    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        public void onItemSelected(Uri itemUri);
    }

    public MainFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // The SearchAdapter will take data from a source and
        // use it to populate the ListView it's attached to.
        mSearchAdapter = new SearchAdapter(getActivity(), null, 0);

        View rootView = inflater.inflate(R.layout.fragment_search_list, container, false);

        // Get a reference to the ListView, and attach this adapter to it.
        mListView = (ListView) rootView.findViewById(R.id.search_listview);
        mListView.setAdapter(mSearchAdapter);
        // We'll call our MainActivity
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                // CursorAdapter returns a cursor at the correct position for getItem(), or null
                // if it cannot seek to that position.
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                boolean hasDetail =
                        !cursor.getString(SearchDataContract.SearchEntry.COLUMN_INDEX_IMDB_ID).isEmpty();
                if (cursor != null && hasDetail) {
                    ((Callback) getActivity())
                            .onItemSelected(SearchDataContract.SearchEntry.buildImdbIdDataUri(
                                    cursor.getString(SearchDataContract.SearchEntry.COLUMN_INDEX_IMDB_ID)
                            ));
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

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(SEARCH_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);

    }

    /** Called when the user touches the button */
    public void sendMessage(View view) {
        // Do something in response to button click

        // first check for Internet connectivity
        ConnectivityManager cm =
                (ConnectivityManager)getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        if (isConnected) {
            Intent intent = new Intent(getActivity(), SearchDataService.class);
            EditText editText = (EditText) getActivity().findViewById(R.id.edit_message);
            String message = editText.getText().toString();
            intent.putExtra(SearchDataService.IMDBID_QUERY_EXTRA, message);
            getActivity().startService(intent);
            getLoaderManager().restartLoader(SEARCH_LOADER, null, this);
        } else {
            Toast.makeText(getActivity(), INTERNET_NOT_CONNECTED, Toast.LENGTH_LONG).show();
        }

    }

    // query the data
    /*public void onDataQuery( ) {
        updateData();
        getLoaderManager().restartLoader(SEARCH_LOADER, null, this);
    }

    public void updateData() {
        Intent intent = new Intent(getActivity(), SearchDataService.class);
        intent.putExtra(SearchDataService.IMDBID_QUERY_EXTRA, "social");
        getActivity().startService(intent);
    }

    public void clearData() {
        Intent intent = new Intent(getActivity(), SearchDataService.class);
        intent.putExtra(SearchDataService.IMDBID_QUERY_EXTRA, "");
        getActivity().startService(intent);
    }*/

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // When tablets rotate, the currently selected list item needs to be saved.
        // When no item is selected, mPosition will be set to Listview.INVALID_POSITION,
        // so check for that before storing.
        if (mPosition != ListView.INVALID_POSITION) {
            outState.putInt(SELECTED_KEY, mPosition);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // This is called when a new Loader needs to be created.  This
        // fragment only uses one loader, so we don't care about checking the id.

        // To only show current and future dates, filter the query to return weather only for
        // dates after or including today.

        // Sort order:  Ascending, by date.
        //String sortOrder = SearchDataContract.SearchEntry.COLUMN_TITLE + " ASC";

        Uri searchDataUri = SearchDataContract.SearchEntry.buildSearchDataUri();

        return new CursorLoader(getActivity(),
                searchDataUri,
                SEARCH_COLUMNS,
                null,
                null,
                null);//sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mSearchAdapter.swapCursor(data);
        if (mPosition != ListView.INVALID_POSITION) {
            // If we don't need to restart the loader, and there's a desired position to restore
            // to, do so now.
            mListView.smoothScrollToPosition(mPosition);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mSearchAdapter.swapCursor(null);
    }


}
