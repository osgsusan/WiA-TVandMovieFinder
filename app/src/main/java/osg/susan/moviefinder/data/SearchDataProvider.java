package osg.susan.moviefinder.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

/**
 * Created by susanosgood on 4/4/15.
 */
public class SearchDataProvider extends ContentProvider {

    static final int SEARCH = 100;
    static final int SEARCH_WITH_IMDBID = 101;

    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private static final SQLiteQueryBuilder sSearchQueryBuilder;

    private DatabaseHelper mDatabaseHelper;

    static {
        sSearchQueryBuilder = new SQLiteQueryBuilder();
        sSearchQueryBuilder.setTables(SearchDataContract.SearchEntry.TABLE_NAME);
    }

    private static final String sImdbIdSelection =
            SearchDataContract.SearchEntry.TABLE_NAME + "." +
                    SearchDataContract.SearchEntry.COLUMN_IMDB_ID + " = ? ";

    private Cursor getSearchByImdbId(Uri uri, String[] projection, String sortOrder) {
        String imdbIdCurrent = SearchDataContract.SearchEntry.getImdbIdFromUri(uri);

        String[] selectionArgs;
        String selection;

        if (imdbIdCurrent != null && !imdbIdCurrent.isEmpty()) {
            selection = sImdbIdSelection;
            selectionArgs = new String[]{imdbIdCurrent};
        } else {
            selectionArgs = new String[0];
            selection = null;
        }

        return sSearchQueryBuilder.query(mDatabaseHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    /*
        This UriMatcher will match each URI to the SEARCH, and SEARCH_WITH_IMDBID,
        integer constants defined above.
        Test by uncommenting the testUriMatcher test within TestUriMatcher.
     */
    static UriMatcher buildUriMatcher() {
        // 1) The code passed into the constructor represents the code to return for the root
        // URI.  It's common to use NO_MATCH as the code for this case. Add the constructor below.
        final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        // 2) Use the addURI function to match each of the types.  Use the constants from
        // SearchDataContract to help define the types to the UriMatcher.
        uriMatcher.addURI(SearchDataContract.CONTENT_AUTHORITY,
                SearchDataContract.PATH_SEARCH, SEARCH);
        uriMatcher.addURI(SearchDataContract.CONTENT_AUTHORITY,
                SearchDataContract.PATH_SEARCH + "/*", SEARCH_WITH_IMDBID);

        return uriMatcher;
    }

    @Override
    public boolean onCreate() {
        mDatabaseHelper = new DatabaseHelper(getContext());
        return true;
    }

    /*
        Test by uncommenting testGetType in TestProvider.

     */
    @Override
    public String getType(Uri uri) {

        // Use the Uri Matcher to determine what kind of URI this is.
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case SEARCH_WITH_IMDBID:
                return SearchDataContract.SearchEntry.CONTENT_TYPE;
            case SEARCH:
                return SearchDataContract.SearchEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // given a URI, determine what kind of request it is,
        // and query the database accordingly.
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            // "search/*"
            case SEARCH_WITH_IMDBID: {
                retCursor = getSearchByImdbId(uri, projection, sortOrder);
                break;
            }
            // "search"
            case SEARCH: {
                retCursor = mDatabaseHelper.getReadableDatabase().query(
                        SearchDataContract.SearchEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case SEARCH: {
                long _id = db.insert(SearchDataContract.SearchEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = SearchDataContract.SearchEntry.buildSearchUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int numRowsDeleted;

        // To remove all rows and get a count, pass "1" as the whereClause
        if (selection == null) selection = "1";

        // Use the uriMatcher to match the SEARCH URI's handled.
        // If it doesn't match these, throw an UnsupportedOperationException.
        switch (match) {
            case SEARCH: {
                numRowsDeleted = db.delete(SearchDataContract.SearchEntry.TABLE_NAME,
                        selection, selectionArgs);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        // A null value deletes all rows. Listeners notified (using the content resolver)
        // if the rowsDeleted != 0 or the selection is null.
        if (numRowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return numRowsDeleted;
    }

    @Override
    public int update(
            Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        // Return the number of rows impacted by the update.
        final SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int numRowsUpdated;

        // Use the uriMatcher to match the SEARCH URI's handled.
        // If it doesn't match these, throw an UnsupportedOperationException.
        switch (match) {
            case SEARCH: {
                numRowsUpdated = db.update(
                        SearchDataContract.SearchEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (numRowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return numRowsUpdated;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case SEARCH:
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(SearchDataContract.SearchEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }
}