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
    public List<Waypoint> Waypoints = new ArrayList<>();
}