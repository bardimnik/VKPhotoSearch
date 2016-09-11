package com.opiumfive.vkphotosearch;

import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Гугл-карта для выбора точки поиска изображений
     *
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapLongClickListener(this);
        LatLng moscow = new LatLng(55.75, 37.61);
        mMap.addMarker(new MarkerOptions().position(moscow).title("Marker in Moscow"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(moscow));

        Toast.makeText(getApplicationContext(),R.string.maps_activity_toast, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        Intent intent = new Intent();
        mMap.addMarker(new MarkerOptions().position(latLng).title("New Marker"));
        intent.putExtra("lat", latLng.latitude);
        intent.putExtra("long", latLng.longitude);
        setResult(RESULT_OK, intent);
        finish();
    }
}
