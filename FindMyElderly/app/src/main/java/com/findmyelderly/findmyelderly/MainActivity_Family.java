package com.findmyelderly.findmyelderly;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.firebase.iid.FirebaseInstanceId;

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
    private int batteryLV;
    private int temp_batteryLV = 0;
    private boolean batteryLVChecked;


    //Our Map
    private GoogleMap mMap;


    //notification
    NotificationCompat.Builder notification;
    private static final int uniqueID = 45612;
    private static final String TAG = "MainActivity_Family";

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

        notification = new NotificationCompat.Builder(this);
        //notification.setAutoCancel(true);



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

    private void checkElderlyBatteryLV(int batteryLV){
        if(batteryLV<=40 && batteryLV%5 == 0){
            if(batteryLV!=temp_batteryLV) {
                temp_batteryLV = batteryLV;

                //local notification
                //notification body
                notification.setSmallIcon(R.drawable.ic_clock);
                notification.setTicker("Elderly's phone battery is low");
                notification.setWhen(System.currentTimeMillis());
                notification.setContentTitle("Elderly's phone battery is low");
                notification.setContentText("Elderly's phone battery only have "+batteryLV+"% remaining.");
                //Notification ElderlyLowBatteryAlert = new Notification();

                //intent to get to the page
                Intent intent = new Intent(this, MainActivity_Family.class);
                PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                notification.setContentIntent(pendingIntent);

                //sending out notification
                NotificationManager nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
                nm.notify(uniqueID, notification.build());


                //FCM notification
                String token = FirebaseInstanceId.getInstance().getToken();
                Toast.makeText(MainActivity_Family.this, token, Toast.LENGTH_SHORT).show();
                Log.w("",token);
            }
        }
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
                    batteryLV = userSnapshot.child("batteryLV").getValue(Integer.class);
                }
                //String to display current latitude and longitude
                //DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                //dateTime = df.format(dateTime);

                checkElderlyBatteryLV(batteryLV);

                //String msg = latitude + ", " + longitude+ ", last updated: "+dateTime;
                String msg = address+", last updated: "+dateTime + "battery="+batteryLV+"%";
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