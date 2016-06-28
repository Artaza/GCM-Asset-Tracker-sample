package com.artazaaziz.android.client.ui;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.artazaaziz.android.client.R;
import com.artazaaziz.android.client.model.GcmMessage;
import com.artazaaziz.android.client.model.Waypoint;
import com.artazaaziz.android.client.service.RegistrationIntentService;
import com.artazaaziz.android.client.util.Preferences;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.gson.Gson;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMapLoadedCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    final Handler handler = new Handler();
    private final OkHttpClient client = new OkHttpClient();
    BroadcastReceiver broadcastReceiver;
    Request request;
    Location previousLocation = new Location(LOCATION_SERVICE);
    private MapView mapView;
    private GoogleMap googleMap;
    private long UPDATE_INTERVAL = 1000;
    private long FASTEST_INTERVAL = 1000;
    private GoogleApiClient googleApiClient;
    private GcmMessage gcmMessage = new GcmMessage();
    private Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Register with GCM, init Pub Sub
        startService(new Intent(this, RegistrationIntentService.class));

        // initialize Fused Location Api
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();


        mapView = ((MapView) findViewById(R.id.maps));
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        // Set the topic to send to
        gcmMessage.setTo("/topics/" + Preferences.TO);
        setTitle(Preferences.TO);
    }


    protected void startLocationUpdates() {
        // Create the location request
        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_INTERVAL);

        // Request location updates -> invokes onLocationChanged()
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);

        //Start polling your location updates at the specified time interval
        pollLocationTransmission(Preferences.LOCATION_UPDATES_INTERVAL);
    }


    private void pollLocationTransmission(final long locationUpdatesInterval) {
        handler.post(new Runnable() {
            public void run() {
                if (gcmMessage.getData().getWaypoints().size() > 0) {

                    //prepare request to send gcm message using ok-http
                    request = new Request.Builder()
                            .url(getString(R.string.server_url))
                            .header("Authorization", "key=" + getString(R.string.api_key))
                            .addHeader("Content-Type", "application/json")
                            .post(RequestBody.create(MediaType.parse("application/json"),
                                    gson.toJson(gcmMessage)))
                            .build();

                    //execute the request
                    client.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Request request, IOException e) {}
                        @Override
                        public void onResponse(final Response response) throws IOException {}
                    });

                    //clear the current ArrayList
                    gcmMessage.getData().getWaypoints().clear();
                }
                //Re-queue update every "locationUpdatesInterval" milliseconds
                handler.postDelayed(this, locationUpdatesInterval);
            }
        });
    }


    @Override
    public void onConnected(Bundle bundle) {
        // connected
        // Get last known recent location.
        Location currentLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        if (currentLocation != null) {
            onLocationChanged(currentLocation);
        }
        // Begin polling for new location updates.
        startLocationUpdates();
    }

    @Override
    public void onLocationChanged(Location location) {
        // New location has now been determined
        if (previousLocation.getLatitude() != location.getLatitude() &&
                previousLocation.getLongitude() != location.getLongitude()) {
            gcmMessage.getData().getWaypoints().add(new Waypoint(location.getLatitude(), location.getLongitude()));
            previousLocation = location;
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMap.getUiSettings().setMapToolbarEnabled(true);
        googleMap.setIndoorEnabled(false);
        googleMap.getUiSettings().setIndoorLevelPickerEnabled(false);
        googleMap.setMyLocationEnabled(true);
        googleMap.setBuildingsEnabled(true);
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setZoomGesturesEnabled(true);
        googleMap.setOnMapLoadedCallback(this);
        this.googleMap = googleMap;
    }

    @Override
    public void onMapLoaded() {
       // googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(25.088475048737795, 55.14925092458725), 1));
    }

    @Override
    public void onConnectionSuspended(int i) {
        // not connected anymore
        if (i == CAUSE_SERVICE_DISCONNECTED) {
            Toast.makeText(this, "Disconnected. Please re-connect.", Toast.LENGTH_SHORT).show();
        } else if (i == CAUSE_NETWORK_LOST) {
            Toast.makeText(this, "Network lost. Please re-connect.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // tried to connect but failed
    }


    @Override
    protected void onStart() {
        super.onStart();
        googleApiClient.connect();
    }

    @Override
    protected void onResume() {
        mapView.onResume();
        if (googleApiClient.isConnected()) {
            startLocationUpdates();
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        if (googleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
            googleApiClient.disconnect();
        }
        mapView.onPause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        stopService(new Intent(this, RegistrationIntentService.class));
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

}
