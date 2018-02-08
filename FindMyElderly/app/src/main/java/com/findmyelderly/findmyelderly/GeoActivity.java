package com.findmyelderly.findmyelderly;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Map;

public class GeoActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,OnConnectionFailedListener,ResultCallback<Status> {
    protected GoogleApiClient mGoogleApiClient;
    protected ArrayList<Geofence> mGeofenceList;
    private Button mAddGeoFencesButton;
    private static final int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 111;
    private float radius =0.1f;
    private Button submit;

    private DatabaseReference mDatabase= FirebaseDatabase.getInstance().getReference();
    private FirebaseAuth mAuth= FirebaseAuth.getInstance();
    private FirebaseUser mUser=mAuth.getCurrentUser();





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.geofence_main);

        /*submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                radius = Float.valueOf(radiuss.getText().toString());
            }
            });*/
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this, // Activity
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_FINE_LOCATION);
        }
        mGeofenceList = new ArrayList<Geofence>();

        populateGeofenceList();
        buildGoogleApiClient();
    }




    @Override
    protected void onStop() {
        super.onStop();
        if(mGoogleApiClient.isConnecting()||mGoogleApiClient.isConnected()){
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(!mGoogleApiClient.isConnecting()||!mGoogleApiClient.isConnected()){
            mGoogleApiClient.connect();
        }
    }
    //2.449484, -76.594959

    /*
    Override methods Google
     */

    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }


    @Override
    public void onResult(Status status) {
        /*EditText radiuss   = (EditText)findViewById(R.id.radius);
        radius = Float.valueOf(radiuss.getText().toString());
        if(radius == 0) {
            Toast.makeText(this, "圍欄半徑不可以設成0", Toast.LENGTH_SHORT).show();
        }*/
        if(status.isSuccess()){
            Toast.makeText(this, "已加入圍欄", Toast.LENGTH_SHORT).show();

        }else {
            String errorMessage = GeofenceErrorMessages.getErrorStr(this, status.getStatusCode());
            Log.e("", "error");
        }

    }

    /*
    My methods
     */

    public void populateGeofenceList(){

        mDatabase.child("users").child(mUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                //float radius = snapshot.child("radius").getValue(Float.class);


        for(Map.Entry<String,LatLng>entry:Constants.POPAYAN_LANDMARKS.entrySet()){
            mGeofenceList.add(new Geofence.Builder().setRequestId(entry.getKey())
                    .setCircularRegion(entry.getValue().latitude,entry.getValue().longitude,Constants.GEOFENCE_RADIUS_IN_METERS)
            .setExpirationDuration(Constants.GEOFENCE_EXPIRATION_IN_MILLISECONDS)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                    .build());
        }
            }
        @Override
        public void onCancelled(DatabaseError databaseError) {
        }
    });
    }
    protected synchronized void buildGoogleApiClient(){
        mGoogleApiClient=new GoogleApiClient.Builder(this).addOnConnectionFailedListener(this).
                addConnectionCallbacks(this).
                addApi(LocationServices.API).
                build();


    }
    private PendingIntent getGeofencePendingIntent(){
        Intent intent=new Intent (this,GeofenceTransitionsIntentService.class);
        return PendingIntent.getService(this,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public void addGeofencesButton(View view){//Geofences button Handler
        if(!mGoogleApiClient.isConnected()){
            Toast.makeText(this, getString(R.string.not_conected), Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            LocationServices.GeofencingApi.addGeofences(mGoogleApiClient, getGeofencingRequest(), getGeofencePendingIntent()).setResultCallback(this);
        }catch (SecurityException sE){
            Log.i("Error",sE.toString());
        }
    }
    private GeofencingRequest getGeofencingRequest(){
        GeofencingRequest.Builder builder=new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(mGeofenceList);
        return builder.build();
    }



}
