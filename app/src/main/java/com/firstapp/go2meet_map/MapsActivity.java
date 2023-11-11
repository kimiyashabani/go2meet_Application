package com.firstapp.go2meet_map;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.location.CurrentLocationRequest;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.FusedLocationProviderClient;
import android.location.LocationManager;
import android.location.LocationRequest;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Context;


import com.firstapp.go2meet_map.databinding.ActivityMapsBinding;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class MapsActivity extends FragmentActivity
        implements OnMapReadyCallback, GoogleMap.OnMapClickListener {

    private GoogleMap mMap;

    private ActivityMapsBinding binding;
    private RadioGroup radioGroup = findViewById(R.id.radioGroup);
    private RadioButton normalMap = findViewById(R.id.normal_map);
    private RadioButton satelliteMap = findViewById(R.id.satellite_map);
    private RadioButton hybridMap = findViewById(R.id.hybrid_map);
    private RadioButton terrainMap = findViewById(R.id.terrain_map);
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private FusedLocationProviderClient fusedLocationProviderClient ;
     private LocationCallback locationCallback;
     private static final String TAG = "MapActivity";

     double longitude;
     double latitude;
    private static final float DEFAULT_ZOOM = 15f;

    //widgets
    private EditText searchText;

    @RequiresApi(api = Build.VERSION_CODES.S)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        searchText = (EditText) findViewById(R.id.inputSearch);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        if(checkLocationPermission()){
            requestCurrentLocation();
            init();
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mMap.setOnMapClickListener(this);


        radioGroup.setOnCheckedChangeListener(
                new RadioGroup.OnCheckedChangeListener(){

                    public void onCheckedChanged(RadioGroup group,int checkedId){
                        if (checkedId == normalMap.getId()) {
                            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

                        } else if (checkedId == satelliteMap.getId()) {
                            mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);

                        } else if (checkedId == hybridMap.getId()) {
                            mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

                        } else if (checkedId == terrainMap.getId()) {
                            mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                        }
                    }
                });
    }

    private boolean checkLocationPermission(){
        //Here we check if we have the location permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            return true;
        }else {
            //if else we should request for location permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            return false;
        }
    }
    //We did not have permission so we requested for it and here we should handle the result of permission REQUEST:
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //permission was granted
                if (checkLocationPermission()){
                    mMap.setMyLocationEnabled(true);
                    requestCurrentLocation();
                }
            }else {
                //permission was denied
                showToast(this,"he current location cannot be get because there is no permisson to do so\"");
            }
        }

    }
    private void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    private void init(){
        Log.d(TAG,"init: initializing");
        searchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH
                || actionId == EditorInfo.IME_ACTION_DONE
                || event.getAction() == event.ACTION_DOWN
                || event.getAction() == event.KEYCODE_ENTER){
                    //now we execute our method for searching that will find and show the places
                    //geoLocate();

                }
                return false;
            }
        });
    }
    /**   MAKING THE LIST OF SEARCH BAR **/
    /*private void geoLocate(){
        Log.d(TAG,"Geolocate : geolocating");
        String searchedString = searchText.getText().toString();
        Geocoder geocoder = new Geocoder(MapsActivity.this);
        List<Address> list = new ArrayList<>();
        try {
            list = geocoder.getFromLocationName(searchedString , 1);
        } catch (IOException e){
            Log.e(TAG, "geolocate : IOException" + e.getMessage());
        }
        if (list.size() > 0){
            Address address = list.get(0);
            moveCamera(new LatLng(address.getLatitude(), address.getLongitude()), DEFAULT_ZOOM);
        }
    }*/

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


        LatLng sydney = new LatLng(-34, 151); // Add a marker in Sydney and move the camera
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        if(checkLocationPermission()){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    public void requestCurrentLocation(){
        try{
            CurrentLocationRequest locationRequest = new CurrentLocationRequest.Builder()
                    .setPriority(LocationRequest.QUALITY_HIGH_ACCURACY)
                    .setDurationMillis(60000)
                    .build();
            //this request location updates
            fusedLocationProviderClient.getCurrentLocation(locationRequest, null)
                    .addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();

                            }else{

                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    });

        } catch (SecurityException e){

        }
            }

    private void moveCamera(LatLng latLng, float zoom){
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,zoom));
    }
    private void setUpMap(GoogleMap googleMap){

        // parsed lat and lang ???

    }
    @Override
    public void onMapClick( LatLng point ) {

        double latitude = point.latitude;
        double longitude = point.longitude;
        LatLng centerPoint = new LatLng(latitude, longitude);
        double radiusMeters = 1000; // 1 kilometer
        CircleOptions circleOptions = new CircleOptions()
                .center(centerPoint)
                .radius(radiusMeters)
                .strokeWidth(2) // Set the stroke width (border) of the circle
                .strokeColor(Color.BLUE) // Set the color of the circle's border
                .fillColor(Color.argb(70, 0, 0, 255)); // Set the color and transparency of the circle's interior

        Circle circle = mMap.addCircle(circleOptions);
    }




}