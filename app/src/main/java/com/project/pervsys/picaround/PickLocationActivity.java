package com.project.pervsys.picaround;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import static com.project.pervsys.picaround.utility.Config.LOCATION_EXTRA;
import static com.project.pervsys.picaround.utility.Config.SHARED_MAP_POSITION;

public class PickLocationActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Button mPickLocation;
    private Button mExit;
    private CameraPosition mCameraPosition;

    Button.OnClickListener mExitListener =
            new Button.OnClickListener() {
                @Override
                public void onClick(View v) {
                    handleExitPressed();
                }
            };


    private void handleExitPressed() {
        Intent i = new Intent();
        setResult(RESULT_CANCELED, i);
        finish();
    }

    Button.OnClickListener mPickListener =
            new Button.OnClickListener() {
                @Override
                public void onClick(View v) {
                    handlePickLocationSelected();
                }
            };

    private void handlePickLocationSelected() {
        if (mMap != null) {
            LatLng selectedLatLng = mMap.getCameraPosition().target;
            String location = selectedLatLng.toString();
            Intent data = new Intent();
            data.putExtra(LOCATION_EXTRA, location);
            setResult(RESULT_OK, data);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_location);

        mPickLocation = (Button) findViewById(R.id.pick_location_button);

        mExit = (Button) findViewById(R.id.exit_button_from_pickloc);
        mExit.setOnClickListener(mExitListener);

        // Load map configurations, if available
        SharedPreferences settings = getSharedPreferences(SHARED_MAP_POSITION, 0);
        double latitude = Double.parseDouble(settings.getString("latitude", "0"));
        double longitude = Double.parseDouble(settings.getString("longitude", "0"));
        float zoom = Float.parseFloat(settings.getString("zoom", "0"));

        LatLng startPosition = new LatLng(latitude, longitude);

        mCameraPosition = new CameraPosition.Builder()
                .target(startPosition)
                .zoom(zoom)
                .build();                   // Creates a CameraPosition from the builder
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
        // Restore previous configurations of the map, if available
        if (mCameraPosition != null)
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(mCameraPosition));

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);

        mPickLocation.setOnClickListener(mPickListener);
    }
}