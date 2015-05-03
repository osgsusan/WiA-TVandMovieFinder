package osg.susan.moviefinder.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import osg.susan.moviefinder.data.SearchDataContract.SearchEntry;

/**
 * Created by susanosgood on 4/4/15.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 3;

    static final String DATABASE_NAME = "searchdata.db";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        createSearchDataTable(sqLiteDatabase);
    }

    private void createSearchDataTable(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_SEARCH_TABLE = "CREATE TABLE " + SearchEntry.TABLE_NAME + " (" +
                SearchEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +

                SearchEntry.COLUMN_IMDB_ID + " INTEGER NOT NULL, " +
                SearchEntry.COLUMN_TITLE + " TEXT NOT NULL, " +
                SearchEntry.COLUMN_YEAR + ", " +
                SearchEntry.COLUMN_TYPE + ", " +
                SearchEntry.COLUMN_RATED + ", " +
                SearchEntry.COLUMN_GENRE + ", " +
                SearchEntry.COLUMN_RUNTIME + ", " +
                SearchEntry.COLUMN_IMDB_RATING + " , " +
                SearchEntry.COLUMN_AWARDS + ", " +
                SearchEntry.COLUMN_ACTORS + ", " +
                SearchEntry.COLUMN_DIRECTOR + ", " +
                SearchEntry.COLUMN_WRITER + ", " +
                SearchEntry.COLUMN_LANGUAGE + ", " +
                SearchEntry.COLUMN_COUNTRY + ", " +
                SearchEntry.COLUMN_PLOT + ", " +

                // To assure the application have just one search entry per day
                // per location, it's created a UNIQUE constraint with REPLACE strategy
                " UNIQUE (" + SearchEntry.COLUMN_IMDB_ID + ") ON CONFLICT REPLACE);";

        sqLiteDatabase.execSQL(SQL_CREATE_SEARCH_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        // Note that this only fires if you change the version number for your database.
        // It does NOT depend on the version number for your application.
        // If you want to update the schema without wiping data, commenting out the next 2 lines
        // should be your top priority before modifying this method.
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + SearchEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
