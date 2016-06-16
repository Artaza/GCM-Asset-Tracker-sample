package com.artazaaziz.android.assettracker.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Artaza on 16-06-16.
 */
public class Data implements Parcelable {

    @SerializedName("Waypoints")
    @Expose
    public List<Waypoint> Waypoints = new ArrayList<>();
    private List<LatLng> latLngList = new ArrayList<>();

    public List<LatLng> convertAllWaypointsToLatLng() {
        if (this.Waypoints != null) {
            for (Waypoint w : this.Waypoints) {
                latLngList.add(new LatLng(Double.valueOf(w.latitude), Double.valueOf(w.longitude)));
            }
        }
        return latLngList;
    }


    public static final Creator<Data> CREATOR = new Creator<Data>() {
        @Override
        public Data createFromParcel(Parcel in) {
            return new Data(in);
        }

        @Override
        public Data[] newArray(int size) {
            return new Data[size];
        }
    };

    public Data() {
        //empty
    }

    protected Data(Parcel in) {
        if (in.readByte() == 0x01) {
            Waypoints = new ArrayList<>();
            in.readList(Waypoints, Waypoint.class.getClassLoader());
        } else {
            Waypoints = null;
        }
        if (in.readByte() == 0x01) {
            latLngList = new ArrayList<LatLng>();
            in.readList(latLngList, LatLng.class.getClassLoader());
        } else {
            latLngList = null;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (Waypoints == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(Waypoints);
        }
        if (latLngList == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(latLngList);
        }
    }
}
