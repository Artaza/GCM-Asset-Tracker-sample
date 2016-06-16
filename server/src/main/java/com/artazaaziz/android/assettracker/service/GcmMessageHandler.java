package com.artazaaziz.android.assettracker.service;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import com.artazaaziz.android.assettracker.model.Data;
import com.artazaaziz.android.assettracker.util.Preferences;
import com.google.android.gms.gcm.GcmListenerService;
import com.google.gson.Gson;

/**
 * Created by Artaza on 16-06-16.
 */
public class GcmMessageHandler extends GcmListenerService {
    Data data = new Data();
    private Gson gson = new Gson();

    //Message received from one of the assets via GCM
    @Override
    public void onMessageReceived(String from, Bundle data) {
        //Loop through all trackable assets
        for (String asset : Preferences.TOPICS) {
            //check which asset(topic) is sending us the message
            if (from.startsWith("/topics/" + asset)) {
                //Since we sent json data now lets de-serialize it, the json formatting was lost we
                // add the necessary parts to make it a valid JSON string
                String json = "{\"Waypoints\":" + data.getString("Waypoints") + "}";

                this.data = gson.fromJson(json, Data.class);

                Bundle b = new Bundle();
                // Parcelable is ~10x better than serialized
                b.putParcelable("WaypointsData", this.data);
                Intent intent = new Intent(Preferences.INTENT_FILTER);
                intent.putExtra(asset, b);

                //broadcast this message to the subscribers
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

            }
        }
    }
}