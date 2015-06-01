package osg.susan.moviefinder.service;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import osg.susan.moviefinder.data.SearchDataContract;

/**
 * Created by susanosgood on 4/8/15.
 */
public class SearchDataService extends IntentService {

    public static final String IMDBID_QUERY_EXTRA = "imdbqe";

    private static final String LOG_TAG = SearchDataService.class.getSimpleName();
    private static final String RESULTS_NOT_FOUND = "Search results not found.";

    private Vector<ContentValues> cVVector;

    public SearchDataService() {
        super("Search");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String imdbQuery = intent.getStringExtra(IMDBID_QUERY_EXTRA);
        List<String> searchResultList = new ArrayList<>();

        // delete old data
        this.getContentResolver().delete(SearchDataContract.SearchEntry.CONTENT_URI, null, null);

        // Get the list of imdbIds, then loop through and get the rest of the data.
        searchResultList = doOmdbSearch(imdbQuery);

        if (searchResultList != null && !searchResultList.isEmpty()) {
            // get the rest of the data
            for (String imdbId : searchResultList) {
                doImdbIdSearch(imdbId);

                if (cVVector != null) {
                    // add to database
                    if (cVVector.size() > 0) {
                        ContentValues[] cvArray = new ContentValues[cVVector.size()];
                        cVVector.toArray(cvArray);
                        this.getContentResolver().bulkInsert(
                                SearchDataContract.SearchEntry.CONTENT_URI, cvArray);
                    }
                    Log.d(LOG_TAG, "SearchData Service Complete. " + cVVector.size() + " Inserted");
                }
            }
        } else {
            if (cVVector != null) {
                // add fake title to database as message
                ContentValues searchValues = new ContentValues();
                searchValues.put(SearchDataContract.SearchEntry.COLUMN_IMDB_ID, "");
                searchValues.put(SearchDataContract.SearchEntry.COLUMN_TITLE, RESULTS_NOT_FOUND);
                this.getContentResolver().insert(
                        SearchDataContract.SearchEntry.CONTENT_URI, searchValues);
                Log.d(LOG_TAG, "SearchData Service Complete. " + cVVector.size() + " Inserted");
            }
        }
    }

    private List<String> doOmdbSearch(String searchStr) {
        // Get the list of imdbIds (API returns 10),
        // then loop through and get the rest of the data.
        List<String> searchResultList = new ArrayList<>();

        if (searchStr != null && !searchStr.isEmpty()) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String searchJsonStr = null;
            String format = "json";

            try {
                //http://www.omdbapi.com/?s=social&r=json
                final String SEARCH_BASE_URL = "http://www.omdbapi.com/?";
                final String SEARCH_PARAM = "s";
                final String FORMAT_PARAM = "r";

                Uri builtUri = Uri.parse(SEARCH_BASE_URL).buildUpon()
                        .appendQueryParameter(SEARCH_PARAM, searchStr)
                        .appendQueryParameter(FORMAT_PARAM, format)
                        .build();

                URL url = new URL(builtUri.toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                searchJsonStr = buffer.toString();
                searchResultList = getSearchDataFromJson(searchJsonStr);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // Unsuccessful data fetch
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

        return searchResultList;
    }

    private List<String> getSearchDataFromJson(String searchJsonStr) throws JSONException {
        /*JSONObject obj = new JSONObject(" .... ");
        String pageName = obj.getJSONObject("pageInfo").getString("pageName");

        JSONArray arr = obj.getJSONArray("posts");
        for (int i = 0; i < arr.length(); i++)
        {
            String post_id = arr.getJSONObject(i).getString("post_id");
            ......
        }*/
        // Search result information. Each search result info is an element of the "list" array.
        final String OMDB_RESULT_LIST = "Search";
        final String OMDB_IMDBID = "imdbID";

        List<String> imdbIdList = new ArrayList<>();
        if (searchJsonStr != null && !searchJsonStr.isEmpty()) {
            JSONObject searchJson = new JSONObject(searchJsonStr);
            try {
                JSONArray resultsArray = searchJson.getJSONArray(OMDB_RESULT_LIST);

                // Insert the new search information into the database
                cVVector = new Vector<>(resultsArray.length());

                for (int i = 0; i < resultsArray.length(); i++) {
                    // Get the JSON object representing the result
                    // Add the imdbId from each search result to the arraylist
                    JSONObject result = resultsArray.getJSONObject(i);
                    imdbIdList.add(result.getString(OMDB_IMDBID));
                }
            } catch (JSONException e) {
                // handle no imdblist, getJsonObject, check for response = false;
                if (searchJsonStr.contains("Response") &&
                        "False".equalsIgnoreCase(searchJson.getString("Response"))) {
                    cVVector = new Vector<>(1);

                } else {
                    e.printStackTrace();
                }
            }
        }
        return imdbIdList;
    }

    private void doImdbIdSearch(String imdbId) {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String searchJsonStr = null;
        String plot = "full";
        String format = "json";
        try {
            //http://www.omdbapi.com/?i=tt1285016&plot=full&r=json
            final String SEARCH_BASE_URL = "http://www.omdbapi.com/?";
            final String ID_PARAM = "i";
            final String PLOT_PARAM = "plot";
            final String FORMAT_PARAM = "r";

            Uri builtUri = Uri.parse(SEARCH_BASE_URL).buildUpon()
                    .appendQueryParameter(ID_PARAM, imdbId)
                    .appendQueryParameter(PLOT_PARAM, plot)
                    .appendQueryParameter(FORMAT_PARAM, format)
                    .build();

            URL url = new URL(builtUri.toString());

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
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return;
            }
            searchJsonStr = buffer.toString();
            getImdbIdDataFromJson(searchJsonStr);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            // Unsuccessful data fetch
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
        return;
    }

    private void getImdbIdDataFromJson(String imdbIdJsonStr)
            throws JSONException {

        final String OMDB_IMDBID = "imdbID";
        final String OMDB_TITLE = "Title";
        final String OMDB_YEAR = "Year";
        final String OMDB_TYPE = "Type";
        final String OMDBD_RATED = "Rated";
        final String OMDBD_RUNTIME = "Runtime";
        final String OMDBD_GENRE = "Genre";
        final String OMDBD_IMDB_RATING = "imdbRating";
        final String OMDBD_AWARDS = "Awards";
        final String OMDBD_ACTORS = "Actors";
        final String OMDBD_DIRECTOR = "Director";
        final String OMDBD_WRITER = "Writer";
        final String OMDBD_LANGUAGE = "Language";
        final String OMDBD_COUNTRY = "Country";
        final String OMDBD_PLOT = "Plot";

        try {
            String imdbId;
            String title;
            String year;
            String type;
            String rated = "";
            String genre = "";
            String runtime = "";
            String imdbRating = "";
            String awards = "";
            String actors = "";
            String director = "";
            String writer = "";
            String language = "";
            String country = "";
            String plot = "";

            // Get the JSON object representing the result
            JSONObject result = new JSONObject(imdbIdJsonStr);
            //System.out.println("json detail: " + result.toString());

            imdbId = result.getString(OMDB_IMDBID);
            title = result.getString(OMDB_TITLE);
            year = result.getString(OMDB_YEAR);
            type = result.getString(OMDB_TYPE);
            rated = result.getString(OMDBD_RATED);
            genre = result.getString(OMDBD_GENRE);
            runtime = result.getString(OMDBD_RUNTIME);
            imdbRating = result.getString(OMDBD_IMDB_RATING);
            awards = result.getString(OMDBD_AWARDS);
            actors = result.getString(OMDBD_ACTORS);
            director = result.getString(OMDBD_DIRECTOR);
            writer = result.getString(OMDBD_WRITER);
            language = result.getString(OMDBD_LANGUAGE);
            country = result.getString(OMDBD_COUNTRY);
            plot = result.getString(OMDBD_PLOT);

            ContentValues searchValues = new ContentValues();

            searchValues.put(SearchDataContract.SearchEntry.COLUMN_IMDB_ID, imdbId);
            searchValues.put(SearchDataContract.SearchEntry.COLUMN_TITLE, title);
            searchValues.put(SearchDataContract.SearchEntry.COLUMN_YEAR, year);
            searchValues.put(SearchDataContract.SearchEntry.COLUMN_TYPE, type);
            searchValues.put(SearchDataContract.SearchEntry.COLUMN_RATED, rated);
            searchValues.put(SearchDataContract.SearchEntry.COLUMN_GENRE, genre);
            searchValues.put(SearchDataContract.SearchEntry.COLUMN_RUNTIME, runtime);
            searchValues.put(SearchDataContract.SearchEntry.COLUMN_IMDB_RATING, imdbRating);
            searchValues.put(SearchDataContract.SearchEntry.COLUMN_AWARDS, awards);
            searchValues.put(SearchDataContract.SearchEntry.COLUMN_ACTORS, actors);
            searchValues.put(SearchDataContract.SearchEntry.COLUMN_DIRECTOR, director);
            searchValues.put(SearchDataContract.SearchEntry.COLUMN_WRITER, writer);
            searchValues.put(SearchDataContract.SearchEntry.COLUMN_LANGUAGE, language);
            searchValues.put(SearchDataContract.SearchEntry.COLUMN_COUNTRY, country);
            searchValues.put(SearchDataContract.SearchEntry.COLUMN_PLOT, plot);

            cVVector.add(searchValues);

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }
}
