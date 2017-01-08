package com.opiumfive.vkphotosearch;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements
        OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    private GoogleMap mMap;
    private LocationManager mLocationManager;
    private Circle mCircle;
    private LatLng currentLL;
    private int currentRadius = 5000;
    private int[] rads = {10,100,800,5000,6000,50000};

    private void toggleRadius() {
        int i = 0;
        while (rads[i++] != currentRadius) {}
        i--;
        if (i > 0) {
            currentRadius = rads[i - 1];
        } else {
            currentRadius = rads[rads.length - 1];
        }

        if (mCircle != null) {
            mCircle.setRadius((double) currentRadius);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        }

        findViewById(R.id.myLocation).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestPermis();
            }
        });

        findViewById(R.id.chooseRadius).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleRadius();
            }
        });

        findViewById(R.id.ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                returnCoo(currentLL.latitude, currentLL.longitude);
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapLongClickListener(this);

        SPmanager prefs = SPmanager.getInstance();
        LatLng preset;
        if (prefs.getLastLat() == 0.0f) {
            preset = new LatLng(55.75, 37.61);
        } else {
            preset = new LatLng(prefs.getLastLat(), prefs.getLastLong());
        }
        currentLL = preset;
        mMap.clear();
        if (mCircle != null) {
            mCircle.remove();
        }
        CameraUpdate zoom = CameraUpdateFactory.zoomTo(10);
        mCircle = mMap.addCircle(new CircleOptions()
                    .center(preset)
                    .clickable(true)
                    .radius(currentRadius)
                    .strokeColor(Color.parseColor("#3b5f87")));
        mMap.addMarker(new MarkerOptions().position(preset));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(preset));
        mMap.animateCamera(zoom);
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        mMap.clear();
        if (mCircle != null) {
            mCircle.remove();
        }
        CameraUpdate zoom = CameraUpdateFactory.zoomTo(10);
        mMap.addMarker(new MarkerOptions().position(latLng).title(""));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        currentLL = latLng;
        mMap.animateCamera(zoom);
        Circle circle = mMap.addCircle(new CircleOptions()
                .center(latLng)
                .clickable(true)
                .radius(currentRadius)
                .strokeColor(Color.parseColor("#3b5f87")));
    }

    public void returnCoo(double lat, double longi) {
        LatLng latLng = new LatLng(lat, longi);
        Intent intent = new Intent();
        mMap.addMarker(new MarkerOptions().position(latLng).title(""));
        intent.putExtra("lat", latLng.latitude);
        intent.putExtra("long", latLng.longitude);
        intent.putExtra("rad", currentRadius);
        setResult(RESULT_OK, intent);
        finish();
    }

    protected void onStart() {
        super.onStart();
    }

    protected void onStop() {

        super.onStop();
    }

    public void requestPermis() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        } else {
            getLoc();
        }
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull final String[] permissions, @NonNull final int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted.
                getLoc();
            } else {
                // User refused to grant permission.
                Toast.makeText(getApplicationContext(), "Вы не предоставили разрешения", Toast.LENGTH_SHORT ).show();
            }
        }
    }

    public void getLoc() {
        try {
            Location last = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (last != null) {
                mMap.clear();
                if (mCircle != null) {
                    mCircle.remove();
                }
                CameraUpdate zoom = CameraUpdateFactory.zoomTo(10);
                LatLng latLng = new LatLng(last.getLatitude(), last.getLongitude());
                currentLL = latLng;
                mMap.addMarker(new MarkerOptions().position(latLng).title(""));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                mMap.animateCamera(zoom);
                Circle circle = mMap.addCircle(new CircleOptions()
                        .center(latLng)
                        .clickable(true)
                        .radius(currentRadius)
                        .strokeColor(Color.parseColor("#3b5f87")));
            }
        } catch (SecurityException e) {}
        long minTime = 1;
        float minDistance = 9000.0f;
        try {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, minDistance, mLocationListener);
        } catch (SecurityException e) {}
    }

    private final LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(final Location location) {
            mMap.clear();
            if (mCircle != null) {
                mCircle.remove();
            }
            CameraUpdate zoom = CameraUpdateFactory.zoomTo(10);
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            currentLL = latLng;
            mMap.addMarker(new MarkerOptions().position(latLng).title(""));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(zoom);
            Circle circle = mMap.addCircle(new CircleOptions()
                    .center(latLng)
                    .clickable(true)
                    .radius(currentRadius)
                    .strokeColor(Color.parseColor("#3b5f87")));
            try {
                mLocationManager.removeUpdates(mLocationListener);
            } catch (SecurityException ex) {}
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }
    };

}
