package com.major.dustinclient;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;

/**
 * Created by Faiz on 05-05-2016.
 */
public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        ActivityCompat.OnRequestPermissionsResultCallback {

    private final static String TAG = "MainActivity";
    protected static final int REQUEST_CHECK_SETTINGS = 0x1;
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2;
    protected final static String LOCATION_KEY = "location-key";
    private static final int REQUEST_LOCATION = 0;
    private static String[] PERMISSION_LOCATION = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

    private GoogleApiClient mGoogleApiClient;
    protected LocationRequest mLocationRequest;
    protected Location mCurrentLocation;

    protected View parentLayout;
    protected TextView mLatitude, mLongitude;

    protected String mLatitudeLabel;
    protected String mLongitudeLabel;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        parentLayout = findViewById(R.id.parent_layout);
        mLatitude = (TextView) findViewById(R.id.latitude);
        mLongitude = (TextView) findViewById(R.id.longitude);

        mLatitudeLabel = getResources().getString(R.string.latitude_label);
        mLongitudeLabel = getResources().getString(R.string.longitude_label);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);

        final PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient,
                        builder.build());

        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {
                final Status status = locationSettingsResult.getStatus();
                final LocationSettingsStates locationSettingsStates = locationSettingsResult.getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        if (savedInstanceState != null) {
                            if (savedInstanceState.keySet().contains(LOCATION_KEY)) {
                                mCurrentLocation = savedInstanceState.getParcelable(LOCATION_KEY);
                            }
                            updateUI();
                        }
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        try {
                            status.startResolutionForResult(MainActivity.this, REQUEST_CHECK_SETTINGS);

                        } catch (IntentSender.SendIntentException e) {

                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        break;
                }
            }
        });
    }

    private void updateUI() {
        if (mCurrentLocation != null) {
            mLatitude.setText(String.format("%s: %f", mLatitudeLabel, mCurrentLocation.getLatitude()));
            mLongitude.setText(String.format("%s: %f", mLongitudeLabel, mCurrentLocation.getLongitude()));
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putParcelable(LOCATION_KEY, mCurrentLocation);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        final LocationSettingsStates states = LocationSettingsStates.fromIntent(data);
        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        break;
                    case Activity.RESULT_CANCELED:
                        break;
                    default:
                        break;
                }
                break;
        }
    }

    protected void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestLocationPermission();
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected()) {
            startLocationUpdates();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected()) {
            stopLocationUpdates();
        }
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG, "Connected to GoogleApiClient");
        if (mCurrentLocation == null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestLocationPermission();
                return;
            }
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            updateUI();
        }
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        updateUI();
    }

    private void requestLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            Snackbar.make(parentLayout, R.string.permission_locations_rationale,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ActivityCompat.requestPermissions(MainActivity.this, PERMISSION_LOCATION,
                                    REQUEST_LOCATION);
                        }
                    })
                    .show();
        } else {
            ActivityCompat.requestPermissions(this, PERMISSION_LOCATION, REQUEST_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_LOCATION) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "Location permission has now been granted.");
                Snackbar.make(parentLayout, R.string.permission_available_location,
                        Snackbar.LENGTH_SHORT).show();
            } else {
                Log.i(TAG, "CAMERA permission was NOT granted.");
                Snackbar.make(parentLayout, R.string.permissions_not_granted,
                        Snackbar.LENGTH_SHORT).show();

            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
