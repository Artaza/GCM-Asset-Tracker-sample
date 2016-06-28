package com.artazaaziz.android.client.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Artaza on 16-06-16.
 */
public class Data {

    @SerializedName("Waypoints")
    @Expose
    private List<Waypoint> Waypoints = new ArrayList<>();

    public List<Waypoint> getWaypoints() {
        return Waypoints;
    }

    public void setWaypoints(List<Waypoint> waypoints) {
        Waypoints = waypoints;
    }
}