package com.example.messmateapp.ui.address;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.messmateapp.R;
import com.example.messmateapp.data.model.AddressDto;
import com.example.messmateapp.data.repository.AddressRepositoryImpl;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;
import java.util.Locale;

public class AddAddressActivity extends AppCompatActivity
        implements OnMapReadyCallback {

    /* ================= UI ================= */

    private EditText etHouse, etArea, etCity, etState, etPin, etLandmark;
    private ProgressBar progressBar;
    private RadioGroup rgLabel;
    private View btnUseLocation;


    /* ================= MAP ================= */

    private GoogleMap mMap;
    private FusedLocationProviderClient locationClient;

    private double lat = 0, lng = 0;

    private static final int LOCATION_REQ = 200;

    /* ================= DATA ================= */

    private AddressRepositoryImpl repo;

    /* ================= Lifecycle ================= */

    @Override
    protected void onCreate(Bundle b) {

        super.onCreate(b);
        setContentView(R.layout.activity_add_address);

        initViews();

        repo = new AddressRepositoryImpl(this);

        locationClient =
                LocationServices.getFusedLocationProviderClient(this);

        /* Load Map */

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.mapFragment);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        /* Use Current Location Button */

        btnUseLocation.setOnClickListener(v ->
                fetchCurrentLocation()
        );


        /* Save */

        findViewById(R.id.btnSave)
                .setOnClickListener(v -> saveAddress());
    }

    /* ================= Init ================= */

    private void initViews() {

        etHouse = findViewById(R.id.etHouse);
        etArea = findViewById(R.id.etArea);
        etCity = findViewById(R.id.etCity);
        etState = findViewById(R.id.etState);
        etPin = findViewById(R.id.etPin);
        etLandmark = findViewById(R.id.etLandmark);

        progressBar = findViewById(R.id.progressBar);

        rgLabel = findViewById(R.id.rgLabel);

        // ✅ NEW
        btnUseLocation = findViewById(R.id.btnUseLocation);
    }

    /* =================================================
       MAP READY
       ================================================= */

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {

        mMap = googleMap;

        if (checkPermission()) {

            mMap.setMyLocationEnabled(true);

            fetchCurrentLocation();
        }

        /* When user moves map */

        mMap.setOnCameraIdleListener(() -> {

            LatLng center =
                    mMap.getCameraPosition().target;

            lat = center.latitude;
            lng = center.longitude;

            fetchAddressFromLatLng(lat, lng);
        });
    }

    /* =================================================
       PERMISSION
       ================================================= */

    private boolean checkPermission() {

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            return true;
        }

        ActivityCompat.requestPermissions(
                this,
                new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION
                },
                LOCATION_REQ
        );

        return false;
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults
    ) {

        super.onRequestPermissionsResult(
                requestCode, permissions, grantResults);

        if (requestCode == LOCATION_REQ
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            if (mMap != null) {

                mMap.setMyLocationEnabled(true);

                fetchCurrentLocation();
            }
        }
    }

    /* =================================================
       CURRENT LOCATION (LIVE GPS)
       ================================================= */

    private void fetchCurrentLocation() {

        if (!checkPermission()) return;

        progressBar.setVisibility(View.VISIBLE);

        locationClient.getCurrentLocation(
                com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY,
                null
        ).addOnSuccessListener(location -> {

            progressBar.setVisibility(View.GONE);

            if (location != null && mMap != null) {

                lat = location.getLatitude();
                lng = location.getLongitude();

                LatLng latLng = new LatLng(lat, lng);

                mMap.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(latLng, 17f)
                );

                fetchAddressFromLatLng(lat, lng);

            } else {

                Toast.makeText(
                        this,
                        "Unable to get location. Turn on GPS",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }

    /* =================================================
       LAT LNG → ADDRESS (AUTO FILL)
       ================================================= */

    private void fetchAddressFromLatLng(double lat, double lng) {

        try {

            Geocoder geocoder =
                    new Geocoder(this, Locale.getDefault());

            List<Address> list =
                    geocoder.getFromLocation(lat, lng, 1);

            if (list != null && !list.isEmpty()) {

                Address address = list.get(0);

                if (address.getSubLocality() != null)
                    etArea.setText(address.getSubLocality());

                if (address.getLocality() != null)
                    etCity.setText(address.getLocality());

                if (address.getAdminArea() != null)
                    etState.setText(address.getAdminArea()); // ✅ STATE

                if (address.getPostalCode() != null)
                    etPin.setText(address.getPostalCode());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* =================================================
   SAVE ADDRESS (ZOMATO STYLE)
   ================================================= */

    private void saveAddress() {

        String house = etHouse.getText().toString().trim();
        String area = etArea.getText().toString().trim();
        String city = etCity.getText().toString().trim();
        String state = etState.getText().toString().trim();
        String pin = etPin.getText().toString().trim();
        String landmark = etLandmark.getText().toString().trim();

        /* Validation */

        if (house.isEmpty()) {
            etHouse.setError("Enter house");
            return;
        }

        if (area.isEmpty()) {
            etArea.setError("Enter area");
            return;
        }

        if (city.isEmpty()) {
            etCity.setError("Enter city");
            return;
        }

        if (state.isEmpty()) {
            etState.setError("Enter state");
            return;
        }

        if (pin.length() != 6) {
            etPin.setError("Enter valid pincode");
            return;
        }

        if (lat == 0 || lng == 0) {

            Toast.makeText(
                    this,
                    "Location not detected",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        /* Label */

        String label = "Home";

        int selectedId = rgLabel.getCheckedRadioButtonId();

        if (selectedId != -1) {

            RadioButton rb = findViewById(selectedId);

            if (rb != null) {
                label = rb.getText().toString();
            }
        }

        /* DTO */

        AddressDto address = new AddressDto();

        address.setLabel(label);
        address.setHouse(house);
        address.setArea(area);
        address.setCity(city);
        address.setState(state);
        address.setPincode(pin);   // ✅ FIXED
        address.setLandmark(landmark);
        address.setLat(lat);
        address.setLng(lng);


        // 🔥 Disable button to avoid double click
        findViewById(R.id.btnSave).setEnabled(false);

        progressBar.setVisibility(View.VISIBLE);

        /* API */

        repo.addAddress(address)
                .observe(this, res -> {

                    if (res.isLoading()) return;

                    progressBar.setVisibility(View.GONE);

                    // Re-enable button
                    findViewById(R.id.btnSave).setEnabled(true);

                    if (res.isSuccess()) {

                        Toast.makeText(
                                this,
                                "Address added & selected",
                                Toast.LENGTH_SHORT
                        ).show();

                        // 🔥 Tell Checkout to refresh
                        setResult(RESULT_OK);

                        finish();

                    } else {

                        Toast.makeText(
                                this,
                                res.getMessage() != null
                                        ? res.getMessage()
                                        : "Add address failed",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }
}
