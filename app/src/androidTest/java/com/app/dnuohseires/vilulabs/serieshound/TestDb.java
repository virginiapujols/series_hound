package com.app.dnuohseires.vilulabs.serieshound;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import com.app.dnuohseires.vilulabs.serieshound.data.SMDBContract;
import com.app.dnuohseires.vilulabs.serieshound.data.SMDBDBHelper;

import java.io.Console;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by Virginia on 5/30/2015.
 */
public class TestDb extends AndroidTestCase {

    public static final String LOG_TAG = TestDb.class.getSimpleName();

    // Since we want each test to start with a clean slate
    void deleteTheDatabase() {
        mContext.deleteDatabase(SMDBDBHelper.DATABASE_NAME);
    }

    /*
        This function gets called before each test is executed to delete the database.  This makes
        sure that we always have a clean test.
     */
    public void setUp() {
        deleteTheDatabase();
    }
    public void testCreateDb() throws Throwable {
        // build a HashSet of all of the table names we wish to look for
        // Note that there will be another table in the DB that stores the
        // Android metadata (db version information)
        final HashSet<String> tableNameHashSet = new HashSet<String>();
        tableNameHashSet.add(SMDBContract.GenreEntry.TABLE_NAME);
        tableNameHashSet.add(SMDBContract.SMContentEntry.TABLE_NAME);
        tableNameHashSet.add(SMDBContract.SMContentGenre.TABLE_NAME);

        mContext.deleteDatabase(SMDBDBHelper.DATABASE_NAME);
        SQLiteDatabase db = new SMDBDBHelper( this.mContext).getWritableDatabase();

        assertEquals(true, db.isOpen());

        // have we created the tables we want?
        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);

        assertTrue("Error: This means that the database has not been created correctly",
                c.moveToFirst());

        // verify that the tables have been created
        do {
            tableNameHashSet.remove(c.getString(0));
        } while( c.moveToNext() );

        // if this fails, it means that your database doesn't contain both the location entry
        // and weather entry tables
        assertTrue("Error: Your database was created without both the location entry and weather entry tables",
                tableNameHashSet.isEmpty());

        // now, do our tables contain the correct columns?
        c = db.rawQuery("PRAGMA table_info(" + SMDBContract.GenreEntry.TABLE_NAME + ")",
                null);

        assertTrue("Error: This means that we were unable to query the database for table information.",
                c.moveToFirst());

        // Build a HashSet of all of the column names we want to look for
        final HashSet<String> locationColumnHashSet = new HashSet<String>();
        locationColumnHashSet.add(SMDBContract.GenreEntry._ID);
        locationColumnHashSet.add(SMDBContract.GenreEntry.COLUMN_TMDB_ID);
        locationColumnHashSet.add(SMDBContract.GenreEntry.COLUMN_CATEGORY_KEY);
        locationColumnHashSet.add(SMDBContract.GenreEntry.COLUMN_DESCRIPTION);

        int columnNameIndex = c.getColumnIndex("name");
        do {
            String columnName = c.getString(columnNameIndex);
            locationColumnHashSet.remove(columnName);
        } while(c.moveToNext());

        // if this fails, it means that your database doesn't contain all of the required location
        // entry columns
        assertTrue("Error: The database doesn't contain all of the required location entry columns",
                locationColumnHashSet.isEmpty());
        db.close();
    }

    /*
        Students:  Here is where you will build code to test that we can insert and query the
        location database.  We've done a lot of work for you.  You'll want to look in TestUtilities
        where you can uncomment out the "createNorthPoleLocationValues" function.  You can
        also make use of the ValidateCurrentRecord function from within TestUtilities.
    */
    public void testGenreTable() {
        // First step: Get reference to writable database
        // If there's an error in those massive SQL table creation Strings,
        // errors will be thrown here when you try to get a writable database.
        SMDBDBHelper dbHelper = new SMDBDBHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Second Step: Create ContentValues of what you want to insert
        // (you can use the createNorthPoleLocationValues if you wish)
        ContentValues testValues = new ContentValues();
        testValues.put(SMDBContract.GenreEntry.COLUMN_TMDB_ID, 10759);
        testValues.put(SMDBContract.GenreEntry.COLUMN_CATEGORY_KEY, SMDBContract.CATEGORY_MOVIE);
        testValues.put(SMDBContract.GenreEntry.COLUMN_DESCRIPTION, "Romance");

        // Third Step: Insert ContentValues into database and get a row ID back
        long genreRowId;
        genreRowId = db.insert(SMDBContract.GenreEntry.TABLE_NAME, null, testValues);

        // Verify we got a row back.
        assertTrue(genreRowId != -1);

        // Data's inserted.  IN THEORY.  Now pull some out to stare at it and verify it made
        // the round trip.

        // Fourth Step: Query the database and receive a Cursor back
        // A cursor is your primary interface to the query results.
        Cursor cursor = db.query(
                SMDBContract.GenreEntry.TABLE_NAME,  // Table to Query
                null, // all columns
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null // sort order
        );

        // Move the cursor to a valid database row and check to see if we got any records back
        // from the query
        assertTrue( "Error: No Records returned from location query", cursor.moveToFirst() );

        // Fifth Step: Validate data in resulting Cursor with the original ContentValues
        // (you can use the validateCurrentRecord function in TestUtilities to validate the
        // query if you like)
        validateCurrentRecord("Error: Location Query Validation Failed", cursor, testValues);

        // Move the cursor to demonstrate that there is only one record in the database
        assertFalse( "Error: More than one record returned from location query",
                cursor.moveToNext() );

        // Sixth Step: Close Cursor and Database
        cursor.close();
        db.close();
    }

    /*
        Students:  Here is where you will build code to test that we can insert and query the
        database.  We've done a lot of work for you.  You'll want to look in TestUtilities
        where you can use the "createWeatherValues" function.  You can
        also make use of the validateCurrentRecord function from within TestUtilities.
     */
    public void testSMContentTable() {
        // First step: Get reference to writable database
        // If there's an error in those massive SQL table creation Strings,
        // errors will be thrown here when you try to get a writable database.
        SMDBDBHelper dbHelper = new SMDBDBHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Second Step (Weather): Create weather values
        ContentValues testValues = new ContentValues();
        testValues.put(SMDBContract.SMContentEntry.COLUMN_CATEGORY_KEY, SMDBContract.CATEGORY_MOVIE);
        testValues.put(SMDBContract.SMContentEntry.COLUMN_AIR_DATE, 1419033600L); // December 20th, 2014
        testValues.put(SMDBContract.SMContentEntry.COLUMN_TITLE, "Fight Club");
        testValues.put(SMDBContract.SMContentEntry.COLUMN_TMDB_ID, 550);
        testValues.put(SMDBContract.SMContentEntry.COLUMN_POPULARITY, 2.5030);
        testValues.put(SMDBContract.SMContentEntry.COLUMN_POSTER, "/2lECpi35Hnbpa4y46JX0aY3AWTy.jpg");


        // Third Step (Weather): Insert ContentValues into database and get a row ID back
        long smContentRowId = db.insert(SMDBContract.SMContentEntry.TABLE_NAME, null, testValues);
        assertTrue(smContentRowId != -1);

        // Fourth Step: Query the database and receive a Cursor back
        // A cursor is your primary interface to the query results.
        Cursor testCursor = db.query(
                SMDBContract.SMContentEntry.TABLE_NAME,  // Table to Query
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null  // sort order
        );

        // Move the cursor to the first valid database row and check to see if we have any rows
        assertTrue( "Error: No Records returned from location query", testCursor.moveToFirst() );

        // Fifth Step: Validate the location Query
        validateCurrentRecord("testInsertReadDb SMDBContract.SMContentEntry failed to validate",
                testCursor, testValues);

        // Move the cursor to demonstrate that there is only one record in the database
        assertFalse( "Error: More than one record returned from weather query",
                testCursor.moveToNext() );

        // Sixth Step: Close cursor and database
        testCursor.close();
        dbHelper.close();
    }


    public void testInsertContentGenre() {
        String content_genres="50,10,15,11";

        SMDBDBHelper dbHelper = new SMDBDBHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        for(String i:content_genres.split(",")){
            ContentValues testValues = new ContentValues();
            testValues.put(SMDBContract.SMContentGenre.COLUMN_TMDB_GENRE_KEY, i);
            testValues.put(SMDBContract.SMContentGenre.COLUMN_CONTENT_KEY, 1);//1= test

            // Third Step: Insert ContentValues into database and get a row ID back
            long genreRowId;
            genreRowId = db.insert(SMDBContract.SMContentGenre.TABLE_NAME, null, testValues);

            // Verify we got a row back.
            assertTrue(genreRowId != -1);

            // Data's inserted.  IN THEORY.  Now pull some out to stare at it and verify it made
            // the round trip.

            // Fourth Step: Query the database and receive a Cursor back
            // A cursor is your primary interface to the query results.
            Cursor cursor = db.query(
                    SMDBContract.SMContentGenre.TABLE_NAME,  // Table to Query
                    null, // all columns
                    null, // Columns for the "where" clause
                    null, // Values for the "where" clause
                    null, // columns to group by
                    null, // columns to filter by row groups
                    null // sort order
            );

            // Move the cursor to a valid database row and check to see if we got any records back
            // from the query
            assertTrue("Error: No Records returned from location query", cursor.moveToFirst());

            System.out.println("DESCRIPTION:"+cursor.getCount());



            // Sixth Step: Close Cursor and Database
            cursor.close();

            //;
        }

        db.close();



    }

    static void validateCurrentRecord(String error, Cursor valueCursor, ContentValues expectedValues) {
        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();
        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int idx = valueCursor.getColumnIndex(columnName);
            assertFalse("Column '" + columnName + "' not found. " + error, idx == -1);
            String expectedValue = entry.getValue().toString();
            assertEquals("Value '" + entry.getValue().toString() +
                    "' did not match the expected value '" +
                    expectedValue + "'. " + error, expectedValue, valueCursor.getString(idx));
        }
    }

}