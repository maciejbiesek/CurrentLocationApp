package com.example.maciej.bootcampmap;

import android.content.IntentSender;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;


public class MapActivity extends ActionBarActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, OnMapReadyCallback {

    private GoogleApiClient mGoogleApiClient;
    private GoogleMap googleMap;
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private Location userLocation;
    private Double userLatitude;
    private Double userLongitude;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }


    @Override
    public void onConnected(Bundle bundle) {
        Log.i("debug", "Location services connected.");
        userLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        userLatitude = userLocation.getLatitude();
        userLongitude = userLocation.getLongitude();
        String address = getAddress();
        plotMarker(address);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i("debug", "Location services suspended. Please reconnect.");

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {
            Log.i("debug", "Location services connection failed with code " + connectionResult.getErrorCode());
        }
    }

    @Override
    public void onMapReady(GoogleMap map) {
        Log.i("debug", "Map ready.");
        googleMap = map;
        googleMap.getUiSettings().setMapToolbarEnabled(false);
    }

    @Override
    public void onStop() {
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected()) {
            userLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            userLatitude = userLocation.getLatitude();
            userLongitude = userLocation.getLongitude();
        }
    }

    private String getAddress() {
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());

        addresses = null;
        try {
            addresses = geocoder.getFromLocation(userLatitude, userLongitude, 1);
        } catch (IOException e) {
            return userLatitude + ", " + userLongitude;
        }

        String address = addresses.get(0).getAddressLine(0);
        String city = addresses.get(0).getLocality();
        return address + ", " + city;
    }

    private void plotMarker(String address) {
        LatLng currentLocation = new LatLng(userLatitude, userLongitude);
        googleMap.addMarker(new MarkerOptions().position(currentLocation).title(address).snippet("JESTEŚ TUTAJ"));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 11));
    }
}
