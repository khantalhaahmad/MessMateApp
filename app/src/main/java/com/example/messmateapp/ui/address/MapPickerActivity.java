package com.example.messmateapp.ui.address;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.messmateapp.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import android.location.Address;
import android.location.Geocoder;

import com.example.messmateapp.utils.SessionManager;

import java.util.List;
import java.util.Locale;


public class MapPickerActivity extends AppCompatActivity
        implements OnMapReadyCallback {

    private static final int LOCATION_REQ = 1001;

    private GoogleMap mMap;
    private Marker marker;

    private double lat = 0, lng = 0;

    private FusedLocationProviderClient locationClient;


    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_map_picker);

        locationClient =
                LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment map =
                (SupportMapFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.map);

        if (map != null) {
            map.getMapAsync(this);
        }


        findViewById(R.id.btnDone).setOnClickListener(v -> {

            if (lat == 0 && lng == 0) {

                Toast.makeText(
                        this,
                        "Please select location",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            String address = getAddressFromLatLng(lat, lng);

            SessionManager session = new SessionManager(this);

            session.saveLocation(address, lat, lng);

            setResult(RESULT_OK);
            finish();
        });
    }

    private String getAddressFromLatLng(double lat, double lng) {

        try {

            Geocoder geo =
                    new Geocoder(this, Locale.getDefault());

            List<Address> list =
                    geo.getFromLocation(lat, lng, 1);

            if (!list.isEmpty()) {

                Address a = list.get(0);

                String area = a.getSubLocality();
                String city = a.getLocality();

                if (area != null)
                    return area + ", " + city;

                return city;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return "Selected Location";
    }

    /* ================= Map Ready ================= */

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {

        mMap = googleMap;

        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        enableLocation();

        mMap.setOnMapClickListener(this::moveMarker);

        mMap.setOnCameraIdleListener(() -> {

            if (marker != null) {

                LatLng pos = marker.getPosition();

                lat = pos.latitude;
                lng = pos.longitude;
            }
        });
    }


    /* ================= Enable Location ================= */

    private void enableLocation() {

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_REQ
            );

            return;
        }

        mMap.setMyLocationEnabled(true);

        locationClient.getLastLocation()
                .addOnSuccessListener(this::moveToCurrent);
    }


    /* ================= Move To Current ================= */

    private void moveToCurrent(Location loc) {

        if (loc == null) return;

        LatLng cur =
                new LatLng(loc.getLatitude(), loc.getLongitude());

        lat = cur.latitude;
        lng = cur.longitude;

        mMap.animateCamera(
                CameraUpdateFactory.newLatLngZoom(cur, 17f)
        );

        placeMarker(cur);
    }


    /* ================= Move Marker ================= */

    private void moveMarker(LatLng pos) {

        lat = pos.latitude;
        lng = pos.longitude;

        placeMarker(pos);

        mMap.animateCamera(
                CameraUpdateFactory.newLatLng(pos)
        );
    }


    private void placeMarker(LatLng pos) {

        if (marker != null) {
            marker.remove();
        }

        marker =
                mMap.addMarker(
                        new MarkerOptions()
                                .position(pos)
                                .title("Delivery Location")
                );
    }


    /* ================= Permission Result ================= */

    @Override
    public void onRequestPermissionsResult(
            int code,
            @NonNull String[] permissions,
            @NonNull int[] results
    ) {

        super.onRequestPermissionsResult(code, permissions, results);

        if (code == LOCATION_REQ &&
                results.length > 0 &&
                results[0] == PackageManager.PERMISSION_GRANTED) {

            enableLocation();

        } else {

            Toast.makeText(
                    this,
                    "Location permission needed",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }
}
