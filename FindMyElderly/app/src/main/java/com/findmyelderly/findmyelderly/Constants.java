package com.findmyelderly.findmyelderly;

import com.google.android.gms.maps.model.LatLng;

import java.util.HashMap;

/**
 * Created by Developer on 2/3/2017.
 */

public final class Constants {
    private Constants(){

    }


    public static final String PACKAGE_NAME = "com.geofence.developer.geofence";

    public static final String SHARED_PREFERENCES_NAME = PACKAGE_NAME + ".SHARED_PREFERENCES_NAME";

    public static final String GEOFENCES_ADDED_KEY = PACKAGE_NAME + ".GEOFENCES_ADDED_KEY";

    /**
     * Used to set an expiration time for a geofence. After this amount of time Location Services
     * stops tracking the geofence.
     */
    public static final long GEOFENCE_EXPIRATION_IN_HOURS = 48;

    /**
     * For this sample, geofences expire after 48 hours.
     */
    public static final long GEOFENCE_EXPIRATION_IN_MILLISECONDS =
            GEOFENCE_EXPIRATION_IN_HOURS * 60 * 60 * 1000;

    public static final float GEOFENCE_RADIUS_IN_METERS = 500; // 1 mile, 1.6 km

    /**
     * Map for storing information about airports in the San Francisco bay area.
     */
   /* public static final HashMap<String, LatLng> BAY_AREA_LANDMARKS = new HashMap<String, LatLng>();
    static {

        // Test
        BAY_AREA_LANDMARKS.put("Udacity Studio", new LatLng(22.3751414, 114.1115287));
    }*/
    public static final HashMap<String,LatLng> POPAYAN_LANDMARKS=new HashMap<String, LatLng>();
    static {
        POPAYAN_LANDMARKS.put("",new LatLng(22.316362, 114.180320));


    }

}

