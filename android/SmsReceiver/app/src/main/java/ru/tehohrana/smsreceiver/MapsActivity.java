package ru.tehohrana.smsreceiver;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.view.View;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    public final static String GPS_LONGITUDE = "ru.tehohrana.SmartMon.GPS_LONGITUDE";
    public final static String GPS_LATITUDE = "ru.tehohrana.SmartMon.GPS_LATITUDE";


    public double gpsLatitude, gpsLongitude;
    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);


        Intent intent = getIntent();
        gpsLatitude = intent.getDoubleExtra(GPS_LATITUDE, 55.55);
        gpsLongitude = intent.getDoubleExtra(GPS_LONGITUDE, 65.55);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // Add a marker in Sydney and move the camera
        LatLng current_position = new LatLng(gpsLatitude, gpsLongitude);
        mMap.addMarker(new MarkerOptions().title("Object_1")
                .snippet("Текущее местоположение")
                .position(current_position));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(current_position,13));
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        // выбираем один вариант
        //mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        //mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        //mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        //mMap.setMapType(GoogleMap.MAP_TYPE_NONE);
    }

    public void onBackBtnPressed(View view) {
        onBackPressed();
    }

    public void onCarBtnClicked(View view) {
        LatLng latLng = new LatLng(gpsLatitude, gpsLongitude);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15);
        mMap.animateCamera(cameraUpdate);
    }
}
