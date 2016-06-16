package com.artazaaziz.android.assettracker.service;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.artazaaziz.android.assettracker.R;
import com.artazaaziz.android.assettracker.util.Preferences;
import com.google.android.gms.gcm.GcmPubSub;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import java.io.IOException;

/**
 * Created by Artaza on 16-06-16.
 */
public class RegistrationIntentService extends IntentService {


    private static final String TAG = "RegIntentService";

    public RegistrationIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        // Make a call to Instance API
        InstanceID instanceID = InstanceID.getInstance(this);
        String senderId = getResources().getString(R.string.gcm_defaultSenderId);
        try {
            // request token that will be used by the server to send push notifications
            String token = instanceID.getToken(senderId, GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
            Log.d(TAG, "GCM Registration Token: " + token);

            // Subscribe to topic channels
            subscribeTopics(token);

            sharedPreferences.edit().putString(Preferences.GCM_TOKEN, token).apply();
            sharedPreferences.edit().putBoolean(Preferences.SENT_TOKEN_TO_SERVER, true).apply();

        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "Failed to complete token refresh", e);
            // If an exception happens while fetching the new token or updating our registration data
            // on a third-party server, this ensures that we'll attempt the update at a later time.
            sharedPreferences.edit().putBoolean(Preferences.SENT_TOKEN_TO_SERVER, false).apply();
        }
    }

    private void subscribeTopics(String token) throws IOException {
        GcmPubSub pubSub = GcmPubSub.getInstance(this);
        for (String topic : Preferences.TOPICS) {
            pubSub.subscribe(token, "/topics/" + topic, null);
        }
    }
}

