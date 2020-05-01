package fr.crt.dc.ngn.soundroid.utility;

import android.annotation.SuppressLint;

import java.util.Locale;

/**
 * Created by CRETE JONATHAN on 28/04/2020.
 */
public class Utility {
    /**
     *
     * Convert time to minutes/seconds
     * @param duration time
     * @return time with minutes/seconds format
     */
    public static String convertDuration(long duration){

        long minutes = (duration / 1000 ) / 60;
        long seconds = (duration / 1000 ) % 60;

        // met en forme -> EX : 3:03
        return String.format(Locale.getDefault(),"%d:%02d", minutes, seconds);

    }
}
