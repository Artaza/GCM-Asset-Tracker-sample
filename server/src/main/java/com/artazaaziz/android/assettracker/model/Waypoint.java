package com.artazaaziz.android.assettracker.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Artaza on 16-06-16.
 */
public class Waypoint {

    @SerializedName("latitude")
    @Expose
    private String latitude;

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    @SerializedName("longitude")
    @Expose

    private String longitude;
}
