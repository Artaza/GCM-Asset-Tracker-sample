package com.artazaaziz.android.client.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Artaza on 16-06-16.
 */
public class GcmMessage {

    @SerializedName("to")
    @Expose
    public String to;
    @SerializedName("data")
    @Expose
    public Data data = new Data();

}



