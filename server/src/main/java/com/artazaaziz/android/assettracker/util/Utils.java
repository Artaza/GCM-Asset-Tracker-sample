package com.artazaaziz.android.assettracker.util;

import android.os.Handler;
import android.os.SystemClock;
import android.view.animation.BounceInterpolator;
import android.view.animation.Interpolator;

import com.google.android.gms.maps.model.Marker;


/**
* From https://guides.codepath.com/android/Google-Maps-API-v2-Usage#falling-pin-animation
*
*/
public class Utils {
    public static void dropPinEffect(final Marker marker) {
        // Handler allows us to repeat a code block after a specified delay
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        final long duration = 100;

        // Use the bounce interpolator
        final Interpolator interpolator =
                new BounceInterpolator();

        // Animate marker with a bounce updating its position every 15ms
        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                // Calculate t for bounce based on elapsed time
                float t = Math.max(
                        1 - interpolator.getInterpolation((float) elapsed
                                / duration), 0);
                // Set the anchor
                marker.setAnchor(0.5f, 1.0f + 14 * t);

                if (t > 0.0) {
                    // Post this event again 15ms from now.
                    handler.postDelayed(this, 15);
                } else { // done elapsing, show window
                    marker.showInfoWindow();
                }
            }
        });
    }
}
