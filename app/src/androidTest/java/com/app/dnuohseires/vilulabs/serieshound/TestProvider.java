/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.app.dnuohseires.vilulabs.serieshound;

import android.content.ComponentName;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import com.app.dnuohseires.vilulabs.serieshound.data.SMDBContract;
import com.app.dnuohseires.vilulabs.serieshound.data.SMDBDBHelper;
import com.app.dnuohseires.vilulabs.serieshound.data.SMDBProvider;

import java.util.Map;
import java.util.Set;

/*
    Note: This is not a complete set of tests of the Sunshine ContentProvider, but it does test
    that at least the basic functionality has been implemented correctly.

    Students: Uncomment the tests in this class as you implement the functionality in your
    ContentProvider to make sure that you've implemented things reasonably correctly.
 */
public class TestProvider extends AndroidTestCase {

    public static final String LOG_TAG = TestProvider.class.getSimpleName();

    /*
       This helper function deletes all records from both database tables using the ContentProvider.
       It also queries the ContentProvider to make sure that the database has been successfully
       deleted, so it cannot be used until the Query and Delete functions have been written
       in the ContentProvider.

       Students: Replace the calls to deleteAllRecordsFromDB with this one after you have written
       the delete functionality in the ContentProvider.
     */
    public void deleteAllRecordsFromProvider() {
        mContext.getContentResolver().delete(
                SMDBContract.SMContentEntry.SMCONTENT_URI,
                null,
                null
        );
        mContext.getContentResolver().delete(
                SMDBContract.GenreEntry.CONTENT_URI,
                null,
                null
        );

        Cursor cursor = mContext.getContentResolver().query(
                SMDBContract.SMContentEntry.SMCONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals("Error: Records not deleted from Weather table during delete", 0, cursor.getCount());
        cursor.close();

        cursor = mContext.getContentResolver().query(
                SMDBContract.GenreEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals("Error: Records not deleted from Location table during delete", 0, cursor.getCount());
        cursor.close();
    }

    /*
        Student: Refactor this function to use the deleteAllRecordsFromProvider functionality once
        you have implemented delete functionality there.
     */
    public void deleteAllRecords() {
        deleteAllRecordsFromProvider();
    }

    // Since we want each test to start with a clean slate, run deleteAllRecords
    // in setUp (called by the test runner before each test).
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        deleteAllRecords();
    }

    /*
        This test checks to make sure that the content provider is registered correctly.
        Students: Uncomment this test to make sure you've correctly registered the WeatherProvider.
     */
    public void testProviderRegistry() {
        PackageManager pm = mContext.getPackageManager();

        // We define the component name based on the package name from the context and the
        // WeatherProvider class.
        ComponentName componentName = new ComponentName(mContext.getPackageName(),
                SMDBProvider.class.getName());
        try {
            // Fetch the provider info using the component name from the PackageManager
            // This throws an exception if the provider isn't registered.
            ProviderInfo providerInfo = pm.getProviderInfo(componentName, 0);

            // Make sure that the registered authority matches the authority from the Contract.
            assertEquals("Error: WeatherProvider registered with authority: " + providerInfo.authority +
                    " instead of authority: " + SMDBContract.CONTENT_AUTHORITY,
                    providerInfo.authority, SMDBContract.CONTENT_AUTHORITY);
        } catch (PackageManager.NameNotFoundException e) {
            // I guess the provider isn't registered correctly.
            assertTrue("Error: WeatherProvider not registered at " + mContext.getPackageName(),
                    false);
        }
    }

    /*
            This test doesn't touch the database.  It verifies that the ContentProvider returns
            the correct type for each type of URI that it can handle.
            Students: Uncomment this test to verify that your implementation of GetType is
            functioning correctly.
         */
    public void testGetType() {
        // content://com.example.android.sunshine.app/weather/
        String type = mContext.getContentResolver().getType(SMDBContract.SMContentEntry.SMCONTENT_URI);
        // vnd.android.cursor.dir/com.example.android.sunshine.app/weather
        assertEquals("Error: the SMDBContract.SMContentEntry CONTENT_URI should return SMDBContract.SMContentEntry.CONTENT_TYPE",
                SMDBContract.SMContentEntry.CONTENT_TYPE, type);


        // content://com.example.android.sunshine.app/weather/94074
        type = mContext.getContentResolver().getType(SMDBContract.SMContentEntry.buildSMContentCategory("movie"));
        // vnd.android.cursor.dir/com.example.android.sunshine.app/weather


        assertEquals("Error: the SMDBContract.SMContentEntry CONTENT_URI with location should return SMDBContract.SMContentEntry.CONTENT_TYPE",
                SMDBContract.SMContentEntry.CONTENT_TYPE, type);

        // content://com.example.android.sunshine.app/weather/94074/20140612
        type = mContext.getContentResolver().getType(SMDBContract.SMContentEntry.buildSMContentMultiple("movie", "550"));
        // vnd.android.cursor.item/com.example.android.sunshine.app/weather/1419120000

        assertEquals("Error: the SMDBContract.SMContentEntry CONTENT_URI with location and date should return SMDBContract.SMContentEntry.CONTENT_ITEM_TYPE",
                SMDBContract.SMContentEntry.CONTENT_ITEM_TYPE, type);

        // content://com.example.android.sunshine.app/location/
        type = mContext.getContentResolver().getType(SMDBContract.GenreEntry.CONTENT_URI);
        // vnd.android.cursor.dir/com.example.android.sunshine.app/location
        assertEquals("Error: the SMDBContract.GenreEntry CONTENT_URI should return SMDBContract.GenreEntry.CONTENT_TYPE",
                SMDBContract.GenreEntry.CONTENT_TYPE, type);
    }

    public void testBasicSMContentQuery() {
        // insert our test records into the database
        SMDBDBHelper dbHelper = new SMDBDBHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues testValues = new ContentValues();
        testValues.put(SMDBContract.SMContentEntry.COLUMN_CATEGORY_KEY, SMDBContract.CATEGORY_MOVIE);
        testValues.put(SMDBContract.SMContentEntry.COLUMN_AIR_DATE, 1419033600L); // December 20th, 2014
        testValues.put(SMDBContract.SMContentEntry.COLUMN_TITLE, "Fight Club");
        testValues.put(SMDBContract.SMContentEntry.COLUMN_TMDB_ID, 550);
        testValues.put(SMDBContract.SMContentEntry.COLUMN_POPULARITY, 2.5030);
        testValues.put(SMDBContract.SMContentEntry.COLUMN_POSTER, "/2lECpi35Hnbpa4y46JX0aY3AWTy.jpg");
        long smContentRowId = db.insert(SMDBContract.SMContentEntry.TABLE_NAME, null, testValues);

        assertTrue("Unable to Insert WeatherEntry into the Database", smContentRowId != -1);

        db.close();

        // Test the basic content provider query
        Cursor weatherCursor = mContext.getContentResolver().query(
                SMDBContract.SMContentEntry.SMCONTENT_URI,
                null,
                null,
                null,
                null
        );

        // Make sure we get the correct cursor out of the database
        validateCursor("testBasicWeatherQuery", weatherCursor, testValues);
    }

    static void validateCursor(String error, Cursor valueCursor, ContentValues expectedValues) {
        assertTrue("Empty cursor returned. " + error, valueCursor.moveToFirst());
        validateCurrentRecord(error, valueCursor, expectedValues);
        valueCursor.close();
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
