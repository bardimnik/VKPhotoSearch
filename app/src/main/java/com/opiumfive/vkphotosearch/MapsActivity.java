package com.opiumfive.vkphotosearch;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKApiPhoto;
import com.vk.sdk.api.model.VKPhotoArray;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener, GoogleMap.OnMapClickListener {

    private static final String DIALOG_IMAGE = "image";
    private SeekBar mSeekBar;
    private GoogleMap mMap;
    private LocationManager mLocationManager;
    private Circle mCircle;
    private LatLng currentLL;
    private int currentRadius = 5000;
    private TextView count;
    private TextView meters;

    private VKRequest myRequest;
    private VKPhotoArray photoArray = new VKPhotoArray();  // массив объектов фото
    VKRequest.VKRequestListener mRequestListener = new VKRequest.VKRequestListener() {
        @Override
        public void onComplete(VKResponse response) {
            //photoArray = ; // полученные фото в gson
            VKPhotoArray tempArray = (VKPhotoArray) response.parsedModel;

            if (tempArray != null && !tempArray.isEmpty() && mMap != null) {
                photoArray = null;
                int cou = 0;
                for (VKApiPhoto ph : tempArray) {
                    float[] distance = new float[1];
                    Location.distanceBetween(currentLL.latitude, currentLL.longitude, ph.lat, ph.lon, distance);
                    boolean inside = distance[0] <= (float) currentRadius;
                    if (inside) {
                        if (photoArray == null) {
                            photoArray = new VKPhotoArray();
                        }
                        photoArray.add(ph);
                        cou++;
                    }
                }
                count.setText(cou + getString(R.string.photos));
                if (cou > 0) {
                    mMap.setOnCircleClickListener(new GoogleMap.OnCircleClickListener() {
                        @Override
                        public void onCircleClick(Circle circle) {
                            VKApiPhoto resPhoto = null;
                            for (VKApiPhoto ph : photoArray) {
                                LatLng center = circle.getCenter();
                                double radius = circle.getRadius() * 2;
                                float[] distance = new float[1];
                                Location.distanceBetween(ph.lat, ph.lon, center.latitude, center.longitude, distance);
                                boolean clicked = distance[0] < radius;
                                if (clicked) {
                                    resPhoto = ph;
                                    break;
                                }
                            }
                            if (resPhoto != null) {
                                final String url_big = getBiggestNotEmptyImage(resPhoto);
                                final String uid = "id" + resPhoto.owner_id;
                                FragmentManager fm = getSupportFragmentManager();
                                ImageFragment.newInstance(url_big, uid).show(fm, DIALOG_IMAGE);
                            }
                        }
                    });
                    for (VKApiPhoto ph : photoArray) {
                        LatLng latLng = new LatLng(ph.lat, ph.lon);
                        Circle c = mMap.addCircle(new CircleOptions()
                                .center(latLng)
                                .clickable(true)
                                .radius(10)
                                .strokeColor(Color.parseColor("#ff0000")));

                    }
                }
            } else {
                mMap.setOnCircleClickListener(null);
            }
        }

        @Override
        public void onError(VKError error) {

        }

        @Override
        public void onProgress(VKRequest.VKProgressType progressType, long bytesLoaded,
                               long bytesTotal) {

        }

        @Override
        public void attemptFailed(VKRequest request, int attemptNumber, int totalAttempts) {
        }
    };
    private final LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(final Location location) {
            moveToNewPoint(location, false);
            try {
                mLocationManager.removeUpdates(mLocationListener);
            } catch (SecurityException ex) {
            }
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

    private void toggleRadius(float position) {
        currentRadius = (int) (10f + (5000f - 10f) / 100f * position);
        meters.setText(currentRadius + "m");
        if (mCircle != null) {
            mCircle.setRadius(currentRadius);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        }

        count = findViewById(R.id.count);
        meters = findViewById(R.id.meters);

        findViewById(R.id.myLocation).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestPermis();
            }
        });

        findViewById(R.id.ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                returnCoo(currentLL.latitude, currentLL.longitude);
            }
        });

        findViewById(R.id.chooseRadius).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // при нажатии на кнопку радиуса
            }
        });

        mSeekBar = findViewById(R.id.seekBar);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                toggleRadius(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                moveToNewPoint(currentLL, true);
            }
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
        moveToNewPoint(preset, false);
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        //moveToNewPoint(latLng);
    }

    @Override
    public void onMapClick(LatLng latLng) {
        moveToNewPoint(latLng, true);
    }

    public void returnCoo(double lat, double longi) {
        Intent intent = new Intent(this, SearchActivity.class);
        LatLng latLng = new LatLng(lat, longi);
        mMap.addMarker(new MarkerOptions().position(latLng).title(""));
        intent.putExtra("lat", latLng.latitude);
        intent.putExtra("long", latLng.longitude);
        intent.putExtra("rad", currentRadius);
        startActivity(intent);
        setResult(RESULT_OK, intent);
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
                Toast.makeText(getApplicationContext(), R.string.no_grant, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void moveToNewPoint(Object loc, boolean isChangeRad) {
        mMap.clear();
        if (mCircle != null) {
            mCircle.remove();
        }
        LatLng latLng = null;
        if (loc instanceof Location) {
            Location tempLocation = (Location) loc;
            latLng = new LatLng(tempLocation.getLatitude(), tempLocation.getLongitude());
        } else if (loc instanceof LatLng) {
            latLng = (LatLng) loc;
        }
        currentLL = latLng;
        mMap.addMarker(new MarkerOptions().position(latLng).title(""));
        if (isChangeRad) {
            mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
        } else {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12f));
        }
        mCircle = mMap.addCircle(new CircleOptions()
                .center(latLng)
                .radius(currentRadius)
                .strokeColor(Color.parseColor("#3b5f87")));

        VKRequest request = VKRequest.getRegisteredRequest(VKApi.photos().search("", latLng.latitude, latLng.longitude, 0, 0, 500, currentRadius).registerObject());
        myRequest = request;
        myRequest.executeWithListener(mRequestListener);
    }

    public void getLoc() {
        try {
            Location last = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (last != null) {
                moveToNewPoint(last, false);
            }
        } catch (SecurityException e) {
        }
        long minTime = 5000;
        float minDistance = 10.0f;
        try {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, minDistance, mLocationListener);
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, minTime, minDistance, mLocationListener);
        } catch (SecurityException e) {
        }
    }

    public String getBiggestNotEmptyImage(VKApiPhoto photo) {
        if (!photo.photo_2560.isEmpty())
            return photo.photo_2560;
        if (!photo.photo_1280.isEmpty())
            return photo.photo_1280;
        if (!photo.photo_807.isEmpty())
            return photo.photo_807;
        if (!photo.photo_604.isEmpty())
            return photo.photo_604;
        return "";
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (myRequest != null)
            myRequest.cancel();
    }
}
