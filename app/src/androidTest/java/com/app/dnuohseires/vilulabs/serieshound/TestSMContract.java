package com.app.dnuohseires.vilulabs.serieshound;

import android.net.Uri;
import android.test.AndroidTestCase;

import com.app.dnuohseires.vilulabs.serieshound.data.SMDBContract;

/**
 * Created by Virginia on 5/31/2015.
 */
public class TestSMContract extends AndroidTestCase {
    // intentionally includes a slash to make sure Uri is getting quoted correctly
    //private static final String TEST_WEATHER_LOCATION = "/North Pole";
   // private static final long TEST_WEATHER_DATE = 1419033600L;  // December 20th, 2014

    /*
        Students: Uncomment this out to test your weather location function.
     */
    public void testBuildGenre() {
        Uri genreUri = SMDBContract.GenreEntry.buildGenreUri(1);
        assertNotNull("Error: Null Uri returned.  You must fill-in buildGenreUri in " +
                        "SMDBContract.",
                genreUri);

       // assertEquals("Error: Weather location not properly appended to the end of the Uri",
         //       TEST_WEATHER_LOCATION, genreUri.getLastPathSegment());
        System.out.println("DESCRIPTION URL OOOHH LA LA: "+genreUri.toString());

       /* assertEquals("Error: Weather location Uri doesn't match our expected result",
                genreUri.toString(),
                "content://com.example.android.sunshine.app/weather/%2FNorth%20Pole");*/
    }

    public void testBuildSContent() {
        Uri smContentUri = SMDBContract.SMContentEntry.buildContentUri(1);
        assertNotNull("Error: Null Uri returned.  You must fill-in buildGenreUri in " +
                        "SMDBContract.",
                smContentUri);

        System.out.println("SMCONTENT URL OOOHH LA LA: "+smContentUri.toString());

    }

}
