package com.artazaaziz.android.assettracker.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;

import com.artazaaziz.android.assettracker.R;
import com.artazaaziz.android.assettracker.model.Data;
import com.artazaaziz.android.assettracker.service.RegistrationIntentService;
import com.artazaaziz.android.assettracker.ui.anim.LatLngInterpolator;
import com.artazaaziz.android.assettracker.ui.anim.MarkerAnimation;
import com.artazaaziz.android.assettracker.util.Preferences;
import com.artazaaziz.android.assettracker.util.Utils;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMapLoadedCallback {

    BroadcastReceiver mReceiver;
    String[] topics = Preferences.TOPICS;
    Polyline polyline;
    float[] colorHSV;
    private LatLng[] latLngArray = new LatLng[]{};
    private List<LatLng> latLngList = new ArrayList<>();
    private MapView mapView;
    private GoogleMap mGoogleMap;
    private ArrayList<Marker> markers = new ArrayList<>();
    private HashMap<String, Integer> colors = new HashMap<>();
    private HashMap<String, LatLng> lastMarkerPositions = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startService(new Intent(this, RegistrationIntentService.class));

        mapView = ((MapView) findViewById(R.id.maps));
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMap.getUiSettings().setMapToolbarEnabled(true);
        googleMap.setIndoorEnabled(false);
        googleMap.getUiSettings().setIndoorLevelPickerEnabled(false);
        googleMap.setMyLocationEnabled(true);
        googleMap.setBuildingsEnabled(true);
        googleMap.setOnMapLoadedCallback(this);
        mGoogleMap = googleMap;

        //place an invisible marker for each of the assets we need to track
        for (String asset : topics) {
            float hue = (float) Math.random() * 360;
            markers.add(mGoogleMap.addMarker(new MarkerOptions()
                    .position(new LatLng(0, 0))
                    .visible(false)
                    .icon(BitmapDescriptorFactory.defaultMarker(hue))
                    .title(asset)));

            //hue, saturation, value for polyline
            colorHSV = new float[]{hue, 1, 0.65f};
            colors.put(asset,Color.HSVToColor(125, colorHSV));
        }
    }

    @Override
    public void onMapLoaded() {
        mGoogleMap.animateCamera(CameraUpdateFactory
                .newLatLngZoom(new LatLng(25.088475048737795, 55.14925092458725), 13));

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        mapView.onResume();
        mReceiver = new BroadcastReceiver() {
            //message has been received my our GcmMessageHandler class and is being broadcasted
            @Override
            public void onReceive(Context context, Intent intent) {
                for (int i = 0, topicsLength = topics.length; i < topicsLength; i++) {

                    final String asset = topics[i];

                    //Get the data in the gcm message
                    if (intent.hasExtra(asset)) {
                        Bundle b = intent.getBundleExtra(asset);
                        Data d = (Data) b.get("WaypointsData");
                        final Marker marker = markers.get(i);

                        //convert our Waypoint list to LatLng type to use with Google Maps API
                        if (d != null)
                            latLngList = d.convertAllWaypointsToLatLng();

                        //set the last position we got from a previous message
                        LatLng lastPos = lastMarkerPositions.get(asset);

                        //if we have a last position form previous message then add it into current
                        //list to include in current animation; this reduces jumpiness from one
                        //message's data to another caused by network lag or delay
                        if (lastPos != null) {
                            latLngList.add(0, lastPos);
                        }else{
                            marker.setPosition(latLngList.get(0));
                            marker.setVisible(true);
                            Utils.dropPinEffect(marker);
                        }

                        //set last position
                        lastMarkerPositions.put(asset, latLngList.get(latLngList.size() - 1));

                        //remove previous polyline
                        if (polyline != null) {
                            polyline.remove();
                        }

                        new Handler().post(new Runnable() {
                            @Override
                            public void run() {

                                latLngArray = latLngList.toArray(new LatLng[latLngList.size()]);

                                //animate the asset's marker
                                MarkerAnimation.animateMarker(marker,
                                        new LatLngInterpolator.Linear(),
                                        latLngArray);

                                //draw the polyline displaying the route of the marker
                                polyline = mGoogleMap.addPolyline(new PolylineOptions()
                                        .add(latLngArray)
                                        .color(colors.get(asset)));

                            }
                        });
                    }
                }
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, new IntentFilter(Preferences.INTENT_FILTER));
        super.onResume();
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
        mapView.onPause();
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
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
