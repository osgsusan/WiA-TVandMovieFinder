package osg.susan.moviefinder.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by susanosgood on 4/4/15.
 * <p/>
 * Defines table and column names for the weather database.
 */
public class SearchDataContract {

    // The "Content authority" is a name for the entire content provider, similar to the
    // relationship between a domain name and its website.  A convenient string to use for the
    // content authority is the package name for the app, which is guaranteed to be unique on the
    // device.
    public static final String CONTENT_AUTHORITY = "osg.susan.moviefinder";

    // Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
    // the content provider.
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    // Possible paths (appended to base content URI for possible URI's)
    // For instance, content://com.example.android.sunshine.app/weather/ is a valid path.
    public static final String PATH_SEARCH = "search";

    /* Inner class that defines the table contents of the search table */
    public static final class SearchEntry implements BaseColumns {

        public static final String TABLE_NAME = "search";
        public static final String COLUMN_IMDB_ID = "imdb_id";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_YEAR = "year";
        public static final String COLUMN_TYPE = "type";
        public static final String COLUMN_RATED = "rated";
        public static final String COLUMN_GENRE = "genre";
        public static final String COLUMN_RUNTIME = "runtime";
        public static final String COLUMN_IMDB_RATING = "imdb_rating";
        public static final String COLUMN_AWARDS = "awards";
        public static final String COLUMN_ACTORS = "actors";
        public static final String COLUMN_DIRECTOR = "director";
        public static final String COLUMN_WRITER = "writer";
        public static final String COLUMN_LANGUAGE = "language";
        public static final String COLUMN_COUNTRY = "country";
        public static final String COLUMN_PLOT = "plot";

        public static final int COLUMN_INDEX_IMDB_ID = 1;
        public static final int COLUMN_INDEX_TITLE = 2;
        public static final int COLUMN_INDEX_YEAR = 3;
        public static final int COLUMN_INDEX_TYPE = 4;
        public static final int COLUMN_INDEX_RATED = 5;
        public static final int COLUMN_INDEX_GENRE = 6;
        public static final int COLUMN_INDEX_RUNTIME = 7;
        public static final int COLUMN_INDEX_IMDB_RATING = 8;
        public static final int COLUMN_INDEX_AWARDS = 9;
        public static final int COLUMN_INDEX_ACTORS = 10;
        public static final int COLUMN_INDEX_DIRECTOR = 11;
        public static final int COLUMN_INDEX_WRITER = 12;
        public static final int COLUMN_INDEX_LANGUAGE = 13;
        public static final int COLUMN_INDEX_COUNTRY = 14;
        public static final int COLUMN_INDEX_PLOT = 15;

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_SEARCH).build();


        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" +
                CONTENT_AUTHORITY + "/" + PATH_SEARCH;

        public static Uri buildSearchUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildSearchDataUri() {
            return CONTENT_URI.buildUpon().build();
        }

        public static Uri buildImdbIdDataUri(String imdbId) {
            return CONTENT_URI.buildUpon().appendPath(imdbId).build();
        }

        public static String getImdbIdFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }
    }
}
