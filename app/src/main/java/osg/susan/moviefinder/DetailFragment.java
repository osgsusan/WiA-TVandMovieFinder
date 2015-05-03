package osg.susan.moviefinder;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import osg.susan.moviefinder.data.SearchDataContract;

/**
 * Created by susanosgood on 4/8/15.
 */
public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = DetailFragment.class.getSimpleName();
    static final String DETAIL_URI = "URI";
    private static final String WIA_SHARE_HASHTAG = " #WIA?WhatsItAbout";

    private ShareActionProvider mShareActionProvider;
    private String shareString;
    private Uri mUri;

    private static final int DETAIL_LOADER = 0;

    private static final String[] DETAIL_COLUMNS = {
            SearchDataContract.SearchEntry.TABLE_NAME + "." + SearchDataContract.SearchEntry._ID,
            SearchDataContract.SearchEntry.COLUMN_IMDB_ID,
            SearchDataContract.SearchEntry.COLUMN_TITLE,
            SearchDataContract.SearchEntry.COLUMN_YEAR,
            SearchDataContract.SearchEntry.COLUMN_TYPE,
            SearchDataContract.SearchEntry.COLUMN_RATED,
            SearchDataContract.SearchEntry.COLUMN_GENRE,
            SearchDataContract.SearchEntry.COLUMN_RUNTIME,
            SearchDataContract.SearchEntry.COLUMN_IMDB_RATING,
            SearchDataContract.SearchEntry.COLUMN_AWARDS,
            SearchDataContract.SearchEntry.COLUMN_ACTORS,
            SearchDataContract.SearchEntry.COLUMN_DIRECTOR,
            SearchDataContract.SearchEntry.COLUMN_WRITER,
            SearchDataContract.SearchEntry.COLUMN_LANGUAGE,
            SearchDataContract.SearchEntry.COLUMN_COUNTRY,
            SearchDataContract.SearchEntry.COLUMN_PLOT
    };

    private TextView textviewImdbId;
    private TextView textviewTitle;
    private TextView textviewYear;
    private TextView textviewType;
    private TextView textviewRated;
    private TextView textviewGenre;
    private TextView textviewRuntime;
    private TextView textviewImdbRating;
    private TextView textviewAwards;
    private TextView textviewActors;
    private TextView textviewDirector;
    private TextView textviewWriter;
    private TextView textviewLanguage;
    private TextView textviewCountry;
    private TextView textviewPlot;

    public DetailFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Bundle arguments = getArguments();
        if (arguments != null) {
            mUri = arguments.getParcelable(DetailFragment.DETAIL_URI);
        }

        View rootView = inflater.inflate(R.layout.fragment_search_detail, container, false);
        textviewImdbId = ((TextView) rootView.findViewById(R.id.detail_imdbId_textView));
        textviewTitle = ((TextView) rootView.findViewById(R.id.detail_title_textView));
        textviewYear = ((TextView) rootView.findViewById(R.id.detail_year_textView));
        textviewGenre = ((TextView) rootView.findViewById(R.id.detail_genre_textView));
        textviewImdbRating = ((TextView) rootView.findViewById(R.id.detail_imdbId_rating_textView));
        textviewAwards = ((TextView) rootView.findViewById(R.id.detail_awards_textView));
        textviewActors = ((TextView) rootView.findViewById(R.id.detail_actors_textView));
        textviewDirector = ((TextView) rootView.findViewById(R.id.detail_director_textView));
        textviewWriter = ((TextView) rootView.findViewById(R.id.detail_writer_textView));
        textviewLanguage = ((TextView) rootView.findViewById(R.id.detail_language_textView));
        textviewCountry = ((TextView) rootView.findViewById(R.id.detail_country_textView));
        textviewPlot = ((TextView) rootView.findViewById(R.id.detail_plot_textView));

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.detailfragment, menu);

        // Retrieve the share menu item
        MenuItem menuItem = menu.findItem(R.id.action_share);

        // Get the provider and hold onto it to set/change the share intent.
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        // If onLoadFinished happens before this, we can go ahead and set the share intent now.
        if (shareString != null) {
            mShareActionProvider.setShareIntent(createShareWiaIntent());
        }
    }

    private Intent createShareWiaIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareString + WIA_SHARE_HASHTAG);
        return shareIntent;
    }

    void onDataQuery() {
        Uri uri = mUri;
        if (null != uri) {
            String imdbId = SearchDataContract.SearchEntry.getImdbIdFromUri(uri);
            Uri updatedUri = SearchDataContract.SearchEntry.buildImdbIdDataUri(imdbId);
            mUri = updatedUri;
            getLoaderManager().restartLoader(DETAIL_LOADER, null, this);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if ( null != mUri ) {
            // Now create and return a CursorLoader that will take care of
            // creating a Cursor for the data being displayed.
            return new CursorLoader(
                    getActivity(),
                    mUri,
                    DETAIL_COLUMNS,
                    null,
                    null,
                    null
            );
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null && data.moveToFirst()) {
            // Read data from cursor and update the view

            String imdbId = data.getString(SearchDataContract.SearchEntry.COLUMN_INDEX_IMDB_ID);
            // url http://www.imdb.com/title/tt0858436/
            textviewImdbId.setText("IMDb: http://www.imdb.com/title/" + imdbId);

            String title = data.getString(SearchDataContract.SearchEntry.COLUMN_INDEX_TITLE);
            textviewTitle.setText(title);

            // 2007- | SERIES | TV-14 | 120 mins
            String year = data.getString(SearchDataContract.SearchEntry.COLUMN_INDEX_YEAR);
            year = (year != null ? year : "");

            String type = data.getString(SearchDataContract.SearchEntry.COLUMN_INDEX_TYPE);
            type = (type != null ? (type.equalsIgnoreCase("series") ? "TV series" : type) : "");

            String rated = data.getString(SearchDataContract.SearchEntry.COLUMN_INDEX_RATED);
            rated = (rated != null ? rated : "");

            String runtime = data.getString(SearchDataContract.SearchEntry.COLUMN_INDEX_RUNTIME);
            runtime = (runtime != null ? runtime : "");

            String headerInfo = year + " | " + type + " | " + rated + " | " + runtime;

            textviewYear.setText(headerInfo);

            String genre = data.getString(SearchDataContract.SearchEntry.COLUMN_INDEX_GENRE);
            textviewGenre.setText(genre);

            String imdbRating = data.getString(SearchDataContract.SearchEntry.COLUMN_INDEX_IMDB_RATING);
            textviewImdbRating.setText("IMDb score: " + imdbRating + "/10");

            String awards = data.getString(SearchDataContract.SearchEntry.COLUMN_INDEX_AWARDS);
            textviewAwards.setText("awards: " + awards);

            String actors = data.getString(SearchDataContract.SearchEntry.COLUMN_INDEX_ACTORS);
            textviewActors.setText("actors: " + actors);

            String director = data.getString(SearchDataContract.SearchEntry.COLUMN_INDEX_DIRECTOR);
            textviewDirector.setText("director: " + director);

            String writer = data.getString(SearchDataContract.SearchEntry.COLUMN_INDEX_WRITER);
            textviewWriter.setText("writer: " + writer);

            String country = data.getString(SearchDataContract.SearchEntry.COLUMN_INDEX_COUNTRY);
            textviewCountry.setText("country: " + country);

            String language = data.getString(SearchDataContract.SearchEntry.COLUMN_INDEX_LANGUAGE);
            textviewLanguage.setText("languages: " + language);

            String plot = data.getString(SearchDataContract.SearchEntry.COLUMN_INDEX_PLOT);
            textviewPlot.setText(plot);

            // For the share intent
            shareString = String.format("%s %s %s%s", "Check out the", type, title, "!");

            // If onCreateOptionsMenu has already happened, update the share intent now.
            if (mShareActionProvider != null) {
                mShareActionProvider.setShareIntent(createShareWiaIntent());
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) { }
 }
