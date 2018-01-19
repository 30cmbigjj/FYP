package com.findmyelderly.findmyelderly;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
import java.util.Locale;


public class MainActivity_Family extends FragmentActivity implements
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        GoogleMap.OnMarkerDragListener,
        GoogleMap.OnMapLongClickListener,
        View.OnClickListener {

    private Button logout;
    private Button edit;
    private ImageButton buttonCurrent;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private double longitude;
    private double latitude;
    private String currentUserId;
    private com.google.firebase.database.Query mQueryMF;
    private TextView tt;
    private String dateTime;
    private String address;


    //Our Map
    private GoogleMap mMap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main__family);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        currentUserId = user.getUid();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        edit = (Button) findViewById(R.id.button2);
        logout = (Button) findViewById(R.id.logout);
        buttonCurrent = (ImageButton) findViewById(R.id.buttonCurrent);
        tt = (TextView) findViewById(R.id.tt);


        buttonCurrent.setOnClickListener(this);

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(MainActivity_Family.this, HomeActivity.class));
            }
        });
        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity_Family.this, EditActivity.class));
            }
        });

    }

    private String getCompleteAddressString(double LATITUDE, double LONGITUDE) {
        String strAdd = "";
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);
            if (addresses != null) {
                Address returnedAddress = addresses.get(0);
                StringBuilder strReturnedAddress = new StringBuilder("");

                for (int i = 0; i <= returnedAddress.getMaxAddressLineIndex(); i++) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n");
                }
                strAdd = strReturnedAddress.toString();
                Log.w("", strReturnedAddress.toString());
            } else {
                Log.w("", "No Address returned!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.w("", "Canont get Address!");
        }
        return strAdd;
    }

    private void getCurrentLocation() {
        mQueryMF = mDatabase.child("users").orderByChild("familyId").equalTo(currentUserId);

        mQueryMF.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot userSnapshot: dataSnapshot.getChildren()) {
                    latitude = userSnapshot.child("latitude").getValue(Double.class);
                    longitude = userSnapshot.child("longitude").getValue(Double.class);
                    dateTime = userSnapshot.child("dateTime").getValue(String.class);
                    address = getCompleteAddressString(latitude,longitude);
                }
                //String to display current latitude and longitude
                //DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                //dateTime = df.format(dateTime);

                //String msg = latitude + ", " + longitude+ ", last updated: "+dateTime;
                String msg = address+", last updated: "+dateTime;
                tt.setText(msg);
                //Creating a LatLng Object to store Coordinates
                LatLng latLng = new LatLng(latitude, longitude);
                //Adding marker to map
                mMap.addMarker(new MarkerOptions()
                        .position(latLng) //setting position
                        .draggable(true) //Making the marker draggable
                        .title("Current Location")); //Adding a title

                //Moving the camera
                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

                //Animating the camera
                mMap.animateCamera(CameraUpdateFactory.zoomTo(20));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

    }

//Function to move the map
/*private void moveMap() {
    //Displaying current coordinates in toast
    //Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
}*/

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng latLng = new LatLng(22.316333,114.180298);
        mMap.addMarker(new MarkerOptions().position(latLng).draggable(true));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.setOnMarkerDragListener(this);
        mMap.setOnMapLongClickListener(this);
    }

    @Override
    public void onConnected(Bundle bundle) {
        getCurrentLocation();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        //Clearing all the markers
        mMap.clear();

        //Adding a new marker to the current pressed position
        mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .draggable(true));
    }

    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        //Getting the coordinates
        latitude = marker.getPosition().latitude;
        longitude = marker.getPosition().longitude;
    }

    @Override
    public void onClick(View v) {
        if (v == buttonCurrent) {
            getCurrentLocation();
        }
    }
}