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
import android.widget.SeekBar;
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
        OnMapReadyCallback, GoogleMap.OnMapLongClickListener, GoogleMap.OnMapClickListener {

    private SeekBar mSeekBar;
    private GoogleMap mMap;
    private LocationManager mLocationManager;
    private Circle mCircle;
    private LatLng currentLL;
    private int currentRadius = 5000;
    private int[] rads = {10,100,800,5000,6000,50000};

    private void toggleRadius(float position) {
        if (mCircle != null) {
            mCircle.setRadius((double) 10f + (50000 - 10)/100 * position);
        }
    }


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

        mSeekBar = (SeekBar) findViewById(R.id.seekBar);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                toggleRadius(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapLongClickListener(this);
        mMap.setOnMapClickListener(this);

        SPmanager prefs = SPmanager.getInstance();
        LatLng preset;
        if (prefs.getLastLat() == 0.0f) {
            preset = new LatLng(55.75, 37.61);
        } else {
            preset = new LatLng(prefs.getLastLat(), prefs.getLastLong());
        }
        moveToNewPoint(preset);
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        //moveToNewPoint(latLng);
    }

    @Override
    public void onMapClick(LatLng latLng) {
        moveToNewPoint(latLng);
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
                getLoc();
            } else {
                Toast.makeText(getApplicationContext(), R.string.no_grant, Toast.LENGTH_SHORT ).show();
            }
        }
    }

    private void moveToNewPoint(Object loc) {
        mMap.clear();
        if (mCircle != null) {
            mCircle.remove();
        }
        CameraUpdate zoom = CameraUpdateFactory.zoomTo(10);
        LatLng latLng = null;
        if (loc instanceof Location) {
            Location tempLocation = (Location) loc;
            latLng = new LatLng(tempLocation.getLatitude(), tempLocation.getLongitude());
        } else if (loc instanceof LatLng) {
            latLng = (LatLng) loc;
        }
        currentLL = latLng;
        mMap.addMarker(new MarkerOptions().position(latLng).title(""));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(zoom);
        mCircle = mMap.addCircle(new CircleOptions()
                .center(latLng)
                .clickable(true)
                .radius(currentRadius)
                .strokeColor(Color.parseColor("#3b5f87")));
    }

    public void getLoc() {
        try {
            Location last = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (last != null) {
                moveToNewPoint(last);
            }
        } catch (SecurityException e) {}
        long minTime = 5000;
        float minDistance = 10.0f;
        try {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, minDistance, mLocationListener);
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, minTime, minDistance, mLocationListener);
        } catch (SecurityException e) {}
    }

    private final LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(final Location location) {
            moveToNewPoint(location);
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
